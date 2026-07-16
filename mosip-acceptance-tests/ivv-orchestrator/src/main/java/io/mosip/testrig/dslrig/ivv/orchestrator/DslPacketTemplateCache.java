package io.mosip.testrig.dslrig.ivv.orchestrator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Orchestrator-side cache for packet template HTTP responses (avoids duplicate
 * packet-creator calls for the same persona/process/quality under parallel runs).
 */
final class DslPacketTemplateCache {

	private static final int MAX_ENTRIES = 256;
	private static final ConcurrentHashMap<String, String> RESPONSE_CACHE = new ConcurrentHashMap<>();

	private DslPacketTemplateCache() {
	}

	static boolean isEnabled() {
		String flag = BaseTestCaseUtil.props.getProperty("disablePacketTemplateCache");
		return flag == null || !flag.equalsIgnoreCase("true");
	}

	static String buildKey(String contextKey, String process, String qualityScore, boolean generateValidCbeff,
			List<String> personaFilePaths) throws IOException {
		List<String> normalized = new ArrayList<>(personaFilePaths.size());
		for (String path : personaFilePaths) {
			Path p = Paths.get(path).toAbsolutePath().normalize();
			// Persona files live on packet-creator. In Docker the DSL JVM cannot see them, so
			// mtime would always be 0 and a later getPacketTemplate for the same process+path
			// (after updateDemo/updateResident/bioException) would cache-hit and skip the HTTP
			// call — which is exactly what fails for scenarios 13/143/153/181 on Docker only.
			if (!Files.exists(p)) {
				throw new IOException("persona path not local; skip DSL template cache: " + p);
			}
			long modified = Files.getLastModifiedTime(p).toMillis();
			normalized.add(p + "@" + modified);
		}
		Collections.sort(normalized);
		return contextKey + "|" + process + "|" + qualityScore + "|" + generateValidCbeff + "|"
				+ String.join(",", normalized);
	}

	static String get(String cacheKey) {
		return RESPONSE_CACHE.get(cacheKey);
	}

	static void put(String cacheKey, String responseJson) {
		if (cacheKey != null && responseJson != null && !responseJson.isBlank()) {
			RESPONSE_CACHE.put(cacheKey, responseJson);
			evictIfNeeded();
		}
	}

	static void clear() {
		RESPONSE_CACHE.clear();
	}

	private static void evictIfNeeded() {
		int overflow = RESPONSE_CACHE.size() - MAX_ENTRIES;
		if (overflow <= 0) {
			return;
		}
		Iterator<String> keys = RESPONSE_CACHE.keySet().iterator();
		while (overflow > 0 && keys.hasNext()) {
			RESPONSE_CACHE.remove(keys.next());
			overflow--;
		}
	}
}
