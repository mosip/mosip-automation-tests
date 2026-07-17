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

	/**
	 * Builds a cache key from the persona file paths and their local modification
	 * timestamps. Returns {@code null} if any persona file is not accessible on the
	 * orchestrator's local filesystem (e.g. Docker, where persona files live inside
	 * the packet-creator container). A {@code null} return tells the caller to skip
	 * both the cache lookup and the cache store, preventing stale template hits when
	 * the same persona is reused across multiple packet operations in a scenario.
	 */
	static String buildKey(String contextKey, String process, String qualityScore, boolean generateValidCbeff,
			List<String> personaFilePaths) throws IOException {
		List<String> normalized = new ArrayList<>(personaFilePaths.size());
		for (String path : personaFilePaths) {
			Path p = Paths.get(path).toAbsolutePath().normalize();
			if (!Files.exists(p)) {
				return null;
			}
			long modified = Files.getLastModifiedTime(p).toMillis();
			normalized.add(p + "@" + modified);
		}
		Collections.sort(normalized);
		return contextKey + "|" + process + "|" + qualityScore + "|" + generateValidCbeff + "|"
				+ String.join(",", normalized);
	}

	static String get(String cacheKey) {
		if (cacheKey == null) {
			return null;
		}
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
