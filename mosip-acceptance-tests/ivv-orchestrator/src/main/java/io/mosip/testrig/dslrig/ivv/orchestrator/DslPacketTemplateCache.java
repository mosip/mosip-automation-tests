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
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Orchestrator-side cache for packet template HTTP responses (avoids duplicate
 * packet-creator calls for the same persona/process/quality under parallel runs).
 */
public final class DslPacketTemplateCache {

	private static final Logger logger = LoggerFactory.getLogger(DslPacketTemplateCache.class);
	private static final int MAX_ENTRIES = 256;
	private static final ConcurrentHashMap<String, String> RESPONSE_CACHE = new ConcurrentHashMap<>();

	/**
	 * Per-persona update counter. Incremented by any DSL step that modifies persona
	 * file content on the packet-creator side (updateDemoOrBioDetails,
	 * UpdateBioExceptionInPersona, updateResidentWithUIN, updateResidentWithRID).
	 *
	 * In Docker, persona files live inside the packet-creator container so
	 * Files.getLastModifiedTime() is not observable from the orchestrator. The
	 * counter is used as the version component of the cache key instead of mtime,
	 * ensuring a cache miss after any explicit persona update without disabling the
	 * cache for scenarios that call getPacketTemplate twice with an unchanged persona.
	 */
	private static final ConcurrentHashMap<String, AtomicInteger> UPDATE_COUNTERS = new ConcurrentHashMap<>();

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
			String pathStr = p.toString();
			long version;
			if (Files.exists(p)) {
				// Locally: file is on the orchestrator filesystem; use mtime for
				// fine-grained differentiation (catches changes from any source).
				version = Files.getLastModifiedTime(p).toMillis();
			} else {
				// Docker: persona files live inside the packet-creator container.
				// mtime is not observable here; use the update counter instead.
				// The counter starts at 0 and is incremented by explicit DSL update
				// steps, so the cache key changes whenever the persona is modified.
				AtomicInteger counter = UPDATE_COUNTERS.get(pathStr);
				version = (counter != null) ? counter.get() : 0L;
			}
			normalized.add(pathStr + "@" + version);
		}
		Collections.sort(normalized);
		return contextKey + "|" + process + "|" + qualityScore + "|" + generateValidCbeff + "|"
				+ String.join(",", normalized);
	}

	/**
	 * Must be called after any DSL step that modifies persona file content on the
	 * packet-creator side (e.g. updateDemoOrBioDetails, UpdateBioExceptionInPersona,
	 * updateResidentWithUIN, updateResidentWithRID). Increments the version counter
	 * so the next getPacketTemplate call for this persona gets a fresh cache miss
	 * and fetches an up-to-date template instead of returning stale cached data.
	 */
	public static void incrementUpdateCounter(String personaPath) {
		if (personaPath == null || personaPath.isBlank()) {
			return;
		}
		try {
			Path p = Paths.get(personaPath).toAbsolutePath().normalize();
			UPDATE_COUNTERS.computeIfAbsent(p.toString(), k -> new AtomicInteger(0)).incrementAndGet();
		} catch (Exception e) {
			logger.warn("Ignoring malformed persona path '{}': {}", personaPath, e.getMessage());
		}
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
		UPDATE_COUNTERS.clear();
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
