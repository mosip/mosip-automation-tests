package io.mosip.testrig.dslrig.dataprovider.preparation;

import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;

import io.mosip.testrig.dslrig.dataprovider.variables.VariableManager;

/**
 * Run-scoped cache for idempotent MOSIP masterdata GET responses.
 * Keyed by full URL + query/path params in the shared {@code {urlBase}run_context} namespace.
 */
public final class RunScopedMasterdataCache {

	private static final String CACHE_PREFIX = "md:get:";

	private RunScopedMasterdataCache() {
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
		// Biometric masterdata and mock-abis are not run-cached: each scenario needs its own capture/expectations.
		return !isBiometricOrMockAbisUrl(lower);
	}

	/** URLs that must not use run-scoped cache (unique biometrics per scenario). */
	public static boolean isBiometricOrMockAbisUrl(String urlLowerCase) {
		if (urlLowerCase == null || urlLowerCase.isBlank()) {
			return false;
		}
		return urlLowerCase.contains("/biometric")
				|| urlLowerCase.contains("mock-abis")
				|| (urlLowerCase.contains("/idrepository/") && urlLowerCase.contains("idvid"));
	}

	public static String buildCacheKey(String url, JSONObject requestParams, JSONObject pathParam) {
		StringBuilder sb = new StringBuilder(CACHE_PREFIX);
		sb.append(url);
		if (pathParam != null && !pathParam.isEmpty()) {
			sb.append("|path:").append(pathParam.toString());
		}
		if (requestParams != null && !requestParams.isEmpty()) {
			sb.append("|query:").append(requestParams.toString());
		}
		return sb.toString();
	}

	public static JSONObject getCachedJson(String contextKey, String cacheKey) {
		Object cached = MosipDataSetup.getCache(cacheKey, MosipDataSetup.getRunContextNamespace(contextKey));
		return cached instanceof JSONObject ? (JSONObject) cached : null;
	}

	public static void putCachedJson(String contextKey, String cacheKey, JSONObject value) {
		if (value != null) {
			MosipDataSetup.setCache(cacheKey, value, MosipDataSetup.getRunContextNamespace(contextKey));
		}
	}

	public static JSONArray getCachedArray(String contextKey, String cacheKey) {
		Object cached = MosipDataSetup.getCache(cacheKey, MosipDataSetup.getRunContextNamespace(contextKey));
		return cached instanceof JSONArray ? (JSONArray) cached : null;
	}

	public static void putCachedArray(String contextKey, String cacheKey, JSONArray value) {
		if (value != null) {
			MosipDataSetup.setCache(cacheKey, value, MosipDataSetup.getRunContextNamespace(contextKey));
		}
	}
}
