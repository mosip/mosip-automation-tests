package io.mosip.testrig.dslrig.dataprovider.preparation;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @deprecated Use {@link MasterdataCache} directly. Retained as a thin delegate for {@code RestClient}.
 */
@Deprecated
public final class RunScopedMasterdataCache {

	private RunScopedMasterdataCache() {
	}

	public static boolean isEnabled(String contextKey) {
		return MasterdataCache.isEnabled(contextKey);
	}

	public static boolean isCacheableGetUrl(String url) {
		return MasterdataCache.isCacheableGetUrl(url);
	}

	public static boolean isBiometricOrMockAbisUrl(String urlLowerCase) {
		return MasterdataCache.isBiometricOrMockAbisUrl(urlLowerCase);
	}

	public static String buildCacheKey(String url, JSONObject requestParams, JSONObject pathParam) {
		return MasterdataCache.buildGetKey(url, requestParams, pathParam);
	}

	public static JSONObject getCachedJson(String contextKey, String cacheKey) {
		return MasterdataCache.getCachedGetJson(contextKey, cacheKey);
	}

	public static void putCachedJson(String contextKey, String cacheKey, JSONObject value) {
		MasterdataCache.putCachedGetJson(contextKey, cacheKey, value);
	}

	public static JSONArray getCachedArray(String contextKey, String cacheKey) {
		return MasterdataCache.getCachedGetArray(contextKey, cacheKey);
	}

	public static void putCachedArray(String contextKey, String cacheKey, JSONArray value) {
		MasterdataCache.putCachedGetArray(contextKey, cacheKey, value);
	}
}
