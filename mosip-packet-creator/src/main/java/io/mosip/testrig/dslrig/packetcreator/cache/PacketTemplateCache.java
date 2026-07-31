package io.mosip.testrig.dslrig.packetcreator.cache;

import java.io.File;
import java.io.IOException;
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
 * Run-scoped cache for packet template generation results in packet-creator.
 */
public final class PacketTemplateCache {

	private static final Logger logger = LoggerFactory.getLogger(PacketTemplateCache.class);
	private static final int MAX_ENTRIES = 256;
	private static final ConcurrentHashMap<String, String> RESPONSE_CACHE = new ConcurrentHashMap<>();
	/** Same boundary ResidentModel enforces when writing personas: only java.io.tmpdir is trusted. */
	private static final Path ALLOWED_DIR = Paths
			.get(System.getProperty("java.io.tmpdir", System.getProperty("user.dir"))).toAbsolutePath().normalize();

	private PacketTemplateCache() {
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
			String outDir, String preregId, String purpose, List<String> personaFilePaths) throws IOException {
		List<String> normalized = new ArrayList<>(personaFilePaths.size());
		for (String path : personaFilePaths) {
			// Only file-existence/mtime metadata is read here to derive an in-memory cache key;
			// file contents are never accessed and the key is never exposed to callers.
			if (path == null || path.contains("..")) {
				throw new IOException("Invalid persona file path: " + path);
			}
			File f = new File(path).getCanonicalFile();
			Path canonicalPath = f.toPath();
			if (!canonicalPath.startsWith(ALLOWED_DIR)) {
				throw new IOException("Persona file path outside allowed directory: " + path);
			}
			long modified = f.exists() ? f.lastModified() : 0L;
			normalized.add(canonicalPath + "@" + modified);
		}
		Collections.sort(normalized);
		return contextKey + "|" + process + "|" + qualityScore + "|" + generateValidCbeff + "|"
				+ outDir + "|" + preregId + "|" + purpose + "|"
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
