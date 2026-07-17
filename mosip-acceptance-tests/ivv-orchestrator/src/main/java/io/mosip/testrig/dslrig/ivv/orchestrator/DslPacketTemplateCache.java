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
			long modified = Files.exists(p) ? Files.getLastModifiedTime(p).toMillis() : 0L;
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

	/**
	 * Drop cached templates for personas that were modified. On Docker, persona paths are
	 * remote so mtime in {@link #buildKey} stays 0 and a later getPacketTemplate would
	 * otherwise reuse a stale response (step appears skipped in the report).
	 */
	static void invalidateForPersonaPaths(String... personaPaths) {
		if (personaPaths == null || personaPaths.length == 0 || RESPONSE_CACHE.isEmpty()) {
			return;
		}
		for (String path : personaPaths) {
			if (path == null || path.isBlank()) {
				continue;
			}
			String needle;
			try {
				needle = Paths.get(path).toAbsolutePath().normalize().toString();
			} catch (Exception e) {
				needle = path.trim();
			}
			final String match = needle;
			RESPONSE_CACHE.keySet().removeIf(key -> key != null && key.contains(match));
		}
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
