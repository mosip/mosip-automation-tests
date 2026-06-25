package io.mosip.testrig.dslrig.dataprovider.cache;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;

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

	private static final ObjectMapper MAPPER = new ObjectMapper();

	private static final ConcurrentHashMap<String, byte[]> BYTES_CACHE = new ConcurrentHashMap<>();

	private PersonaParseCache() {
	}

	public static boolean isEnabled(String contextKey) {
		try {
			Object flag = VariableManager.getVariableValue(contextKey, "disablePersonaParseCache");
			if (flag != null && Boolean.parseBoolean(flag.toString())) {
				return false;
			}
		} catch (Exception ignored) {
			// default enabled
		}
		return true;
	}

	public static ResidentModel readPersona(String filePath) throws IOException {
		Path absolute = Paths.get(filePath).toAbsolutePath().normalize();
		long modified = Files.getLastModifiedTime(absolute).toMillis();
		String cacheKey = absolute + "@" + modified;

		byte[] bytes;
		if (isEnabled(null)) {
			byte[] cachedBytes = BYTES_CACHE.get(cacheKey);
			if (cachedBytes != null) {
				bytes = cachedBytes;
			} else {
				bytes = CommonUtil.read(absolute.toString());
				BYTES_CACHE.putIfAbsent(cacheKey, bytes);
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
}
