package io.mosip.testrig.dslrig.packetcreator.cache;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import io.mosip.testrig.dslrig.dataprovider.variables.VariableManager;

/**
 * Run-scoped cache for packet template generation results in packet-creator.
 */
public final class PacketTemplateCache {

	private static final ConcurrentHashMap<String, String> RESPONSE_CACHE = new ConcurrentHashMap<>();

	private PacketTemplateCache() {
	}

	public static boolean isEnabled(String contextKey) {
		try {
			Object flag = VariableManager.getVariableValue(contextKey, "disablePacketTemplateCache");
			if (flag != null && Boolean.parseBoolean(flag.toString())) {
				return false;
			}
		} catch (Exception ignored) {
			// default enabled
		}
		return true;
	}

	public static String buildKey(String contextKey, String process, String qualityScore, boolean generateValidCbeff,
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

	public static String get(String cacheKey) {
		return RESPONSE_CACHE.get(cacheKey);
	}

	public static void put(String cacheKey, String responseJson) {
		if (cacheKey != null && responseJson != null && !responseJson.isBlank()) {
			RESPONSE_CACHE.put(cacheKey, responseJson);
		}
	}

	public static void clear() {
		RESPONSE_CACHE.clear();
	}
}
