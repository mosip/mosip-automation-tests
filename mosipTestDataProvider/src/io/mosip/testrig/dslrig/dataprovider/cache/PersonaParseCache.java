package io.mosip.testrig.dslrig.dataprovider.cache;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.testrig.dslrig.dataprovider.models.ResidentModel;
import io.mosip.testrig.dslrig.dataprovider.util.CommonUtil;
import io.mosip.testrig.dslrig.dataprovider.variables.VariableManager;

/**
 * Caches persona file bytes keyed by absolute path and lastModified time to avoid
 * repeated disk reads under parallel execution. Parsing still produces a fresh model
 * per call so callers cannot mutate a shared instance.
 */
public final class PersonaParseCache {

	private static final Logger logger = LoggerFactory.getLogger(PersonaParseCache.class);
	private static final ObjectMapper MAPPER = new ObjectMapper();
	private static final int MAX_ENTRIES = 256;
	/** Total cached bytes ceiling; persona files carry Base64 biometrics and are multi-MB. */
	private static final long MAX_CACHED_BYTES = 64L * 1024 * 1024;

	private static final ConcurrentHashMap<String, byte[]> BYTES_CACHE = new ConcurrentHashMap<>();
	private static final java.util.concurrent.atomic.AtomicLong CACHED_BYTES =
			new java.util.concurrent.atomic.AtomicLong();

	private PersonaParseCache() {
	}

	public static boolean isEnabled(String contextKey) {
		try {
			Object flag = VariableManager.getVariableValue(contextKey, "disablePersonaParseCache");
			if (flag != null && Boolean.parseBoolean(flag.toString())) {
				return false;
			}
		} catch (Exception e) {
			logger.debug("disablePersonaParseCache lookup failed for context {}; defaulting to enabled",
					contextKey, e);
		}
		return true;
	}

	public static ResidentModel readPersona(String filePath, String contextKey) throws IOException {
		Path absolute = CommonUtil.validateReadablePath(filePath, contextKey);
		String cacheKey = absolute + "@" + Files.getLastModifiedTime(absolute) + "#" + Files.size(absolute);

		byte[] bytes;
		if (isEnabled(contextKey)) {
			byte[] cachedBytes = BYTES_CACHE.get(cacheKey);
			if (cachedBytes != null) {
				bytes = cachedBytes;
			} else {
				bytes = CommonUtil.read(absolute.toString());
				if (bytes == null) {
					throw new IOException("Unable to read persona file: " + absolute);
				}
				if (BYTES_CACHE.putIfAbsent(cacheKey, bytes) == null) {
					CACHED_BYTES.addAndGet(bytes.length);
				}
				evictIfNeeded();
			}
		} else {
			bytes = CommonUtil.read(absolute.toString());
			if (bytes == null) {
				throw new IOException("Unable to read persona file: " + absolute);
			}
		}

		ResidentModel model = MAPPER.readValue(bytes, ResidentModel.class);
		model.setPath(filePath);
		return model;
	}

	public static void clear() {
		BYTES_CACHE.clear();
		CACHED_BYTES.set(0);
	}

	// BYTES_CACHE is a ConcurrentHashMap, so iteration order is arbitrary rather than
	// least-recently-used; eviction below removes whichever entries the iterator yields first.
	private static void evictIfNeeded() {
		Iterator<String> keys = BYTES_CACHE.keySet().iterator();
		while ((BYTES_CACHE.size() > MAX_ENTRIES || CACHED_BYTES.get() > MAX_CACHED_BYTES) && keys.hasNext()) {
			byte[] removed = BYTES_CACHE.remove(keys.next());
			if (removed != null) {
				CACHED_BYTES.addAndGet(-removed.length);
			}
		}
	}
}
