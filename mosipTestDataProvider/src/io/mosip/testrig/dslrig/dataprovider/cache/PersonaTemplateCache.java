package io.mosip.testrig.dslrig.dataprovider.cache;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.mosip.testrig.dslrig.dataprovider.variables.VariableManager;

/**
 * Run-scoped cache for packet template generation results. Keyed by persona file
 * content identity (path + lastModified) so updates invalidate automatically.
 */
public final class PersonaTemplateCache {

	private static final Logger logger = LoggerFactory.getLogger(PersonaTemplateCache.class);
	private static final int MAX_ENTRIES = 256;
	private static final ConcurrentHashMap<String, String> RESPONSE_CACHE = new ConcurrentHashMap<>();

	private PersonaTemplateCache() {
	}

	public static boolean isEnabled(String contextKey) {
		try {
			Object flag = VariableManager.getVariableValue(contextKey, "disablePacketTemplateCache");
			if (flag != null && Boolean.parseBoolean(flag.toString())) {
				return false;
			}
		} catch (Exception e) {
			logger.debug("disablePacketTemplateCache lookup failed for context {}; defaulting to enabled",
					contextKey, e);
		}
		return true;
	}

	public static String buildKey(String contextKey, String process, String qualityScore, boolean generateValidCbeff,
			List<String> personaFilePaths) throws IOException {
		List<String> normalized = new ArrayList<>(personaFilePaths.size());
		for (String path : personaFilePaths) {
			Path p = Paths.get(path).toAbsolutePath().normalize();
			String modified = Files.exists(p) ? Files.getLastModifiedTime(p).toString() : "missing";
			normalized.add(p + "@" + modified);
		}
		Collections.sort(normalized);
		return contextKey + "|" + process + "|" + qualityScore + "|" + generateValidCbeff + "|"
				+ String.join(",", normalized);
	}

	public static String get(String cacheKey) {
		return RESPONSE_CACHE.get(cacheKey);
	}

	public static void put(String cacheKey, String responseJson) {
		if (cacheKey != null && responseJson != null && !responseJson.isBlank()) {
			RESPONSE_CACHE.put(cacheKey, responseJson);
			evictIfNeeded();
		}
	}

	public static void clear() {
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
