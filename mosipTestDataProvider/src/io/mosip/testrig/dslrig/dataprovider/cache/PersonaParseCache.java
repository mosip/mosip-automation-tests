package io.mosip.testrig.dslrig.dataprovider.cache;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

	private static final ConcurrentHashMap<String, byte[]> BYTES_CACHE = new ConcurrentHashMap<>();

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
		Path absolute = Paths.get(filePath).toAbsolutePath().normalize();
		String cacheKey = absolute + "@" + Files.getLastModifiedTime(absolute);

		byte[] bytes;
		if (isEnabled(contextKey)) {
			byte[] cachedBytes = BYTES_CACHE.get(cacheKey);
			if (cachedBytes != null) {
				bytes = cachedBytes;
			} else {
				bytes = CommonUtil.read(absolute.toString());
				BYTES_CACHE.putIfAbsent(cacheKey, bytes);
				evictIfNeeded();
			}
		} else {
			bytes = CommonUtil.read(absolute.toString());
		}

		ResidentModel model = MAPPER.readValue(bytes, ResidentModel.class);
		model.setPath(filePath);
		return model;
	}

	public static void clear() {
		BYTES_CACHE.clear();
	}

	private static void evictIfNeeded() {
		int overflow = BYTES_CACHE.size() - MAX_ENTRIES;
		if (overflow <= 0) {
			return;
		}
		Iterator<String> keys = BYTES_CACHE.keySet().iterator();
		while (overflow > 0 && keys.hasNext()) {
			BYTES_CACHE.remove(keys.next());
			overflow--;
		}
	}
}
