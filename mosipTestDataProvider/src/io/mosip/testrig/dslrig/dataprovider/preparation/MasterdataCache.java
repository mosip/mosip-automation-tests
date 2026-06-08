package io.mosip.testrig.dslrig.dataprovider.preparation;

import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;

import io.mosip.testrig.dslrig.dataprovider.variables.VariableManager;

/**
 * Unified run-scoped cache for MOSIP masterdata (GET JSON, parsed entities, derived values).
 * All entries live in dedicated {@link MasterdataCacheStore} namespaces ({@code md_cache:*}),
 * separate from {@link io.mosip.testrig.dslrig.dataprovider.variables.VariableManager} scenario keys.
 * Legacy unprefixed keys are still read for backward compatibility within the same JVM.
 */
public final class MasterdataCache {

	public static final String CACHE_VERSION = "2";

	public static final String PREFIX_GET = "md:get:";
	public static final String PREFIX_ENTITY = "md:entity:";

	/**
	 * Logical operations performed by {@link RunCacheService#warm(String)} (idempotent per run).
	 */
	public static final List<String> WARM_OPERATIONS = List.of(
			"auth:system",
			"auth:admin",
			"auth:resident",
			"auth:regproc",
			"masterdata:languages",
			"masterdata:genderTypes",
			"masterdata:locationHierarchy",
			"masterdata:dynamicFields",
			"masterdata:idSchemaLatest",
			"masterdata:individualTypes",
			"masterdata:documentCategories",
			"masterdata:validDocuments",
			"masterdata:locationImmediateChildren",
			"masterdata:preregLocationHierarchy",
			"masterdata:preregLoginConfig");

	private MasterdataCache() {
	}

	public static boolean isEnabled(String contextKey) {
		try {
			Object flag = VariableManager.getVariableValue(contextKey, "disableRunMasterdataCache");
			if (flag != null && Boolean.parseBoolean(flag.toString())) {
				return false;
			}
		} catch (Exception ignored) {
			// default enabled
		}
		return true;
	}

	public static boolean isCacheableGetUrl(String url) {
		if (url == null || url.isBlank()) {
			return false;
		}
		String lower = url.toLowerCase(Locale.ROOT);
		if (!lower.contains("/masterdata/")) {
			return false;
		}
		if (lower.contains("/actuator/")) {
			return false;
		}
		return !isBiometricOrMockAbisUrl(lower);
	}

	public static boolean isBiometricOrMockAbisUrl(String urlLowerCase) {
		if (urlLowerCase == null || urlLowerCase.isBlank()) {
			return false;
		}
		return urlLowerCase.contains("/biometric")
				|| urlLowerCase.contains("mock-abis")
				|| (urlLowerCase.contains("/idrepository/") && urlLowerCase.contains("idvid"));
	}

	public static String buildGetKey(String url, JSONObject requestParams, JSONObject pathParam) {
		StringBuilder sb = new StringBuilder(PREFIX_GET);
		sb.append(url);
		if (pathParam != null && !pathParam.isEmpty()) {
			sb.append("|path:").append(pathParam);
		}
		if (requestParams != null && !requestParams.isEmpty()) {
			sb.append("|query:").append(requestParams);
		}
		return sb.toString();
	}

	/**
	 * Normalizes a cache key for storage. Auth keys and existing {@code md:*} keys are unchanged.
	 */
	public static String normalizeKey(String key) {
		if (key == null || key.isBlank()) {
			return key;
		}
		if (key.startsWith("md:") || key.startsWith("auth:")) {
			return key;
		}
		return PREFIX_ENTITY + key;
	}

	public static Object get(String contextKey, String key) {
		return getByNamespace(MosipDataSetup.getRunContextNamespace(contextKey), key);
	}

	public static void put(String contextKey, String key, Object value) {
		putByNamespace(MosipDataSetup.getRunContextNamespace(contextKey), key, value);
	}

	public static Object getByNamespace(String runContextNamespace, String key) {
		if (runContextNamespace == null || key == null) {
			return null;
		}
		String normalized = normalizeKey(key);
		Object cached = MosipDataSetup.readCacheRaw(normalized, runContextNamespace);
		if (cached == null && !normalized.equals(key)) {
			cached = MosipDataSetup.readCacheRaw(key, runContextNamespace);
		}
		return cached;
	}

	public static void putByNamespace(String runContextNamespace, String key, Object value) {
		if (runContextNamespace == null || key == null || value == null) {
			return;
		}
		MosipDataSetup.writeCacheRaw(normalizeKey(key), value, runContextNamespace);
	}

	public static JSONObject getCachedGetJson(String contextKey, String getKey) {
		Object cached = get(contextKey, getKey);
		if (cached instanceof JSONObject json) {
			return copyJsonObject(json);
		}
		return null;
	}

	public static void putCachedGetJson(String contextKey, String getKey, JSONObject value) {
		if (value != null) {
			put(contextKey, getKey, copyJsonObject(value));
		}
	}

	public static JSONArray getCachedGetArray(String contextKey, String getKey) {
		Object cached = get(contextKey, getKey);
		if (cached instanceof JSONArray array) {
			return copyJsonArray(array);
		}
		return null;
	}

	public static void putCachedGetArray(String contextKey, String getKey, JSONArray value) {
		if (value != null) {
			put(contextKey, getKey, copyJsonArray(value));
		}
	}

	private static JSONObject copyJsonObject(JSONObject source) {
		return new JSONObject(source.toString());
	}

	private static JSONArray copyJsonArray(JSONArray source) {
		return new JSONArray(source.toString());
	}

	public static void clearContext(String contextKey) {
		MosipDataSetup.clearRunCache(contextKey);
	}
}
