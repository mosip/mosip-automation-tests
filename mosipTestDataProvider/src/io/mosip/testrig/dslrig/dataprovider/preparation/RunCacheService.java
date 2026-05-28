package io.mosip.testrig.dslrig.dataprovider.preparation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.mosip.testrig.dslrig.dataprovider.models.MosipLanguage;
import io.mosip.testrig.dslrig.dataprovider.util.RestClient;
import io.mosip.testrig.dslrig.dataprovider.variables.VariableManager;

/**
 * Pre-loads MOSIP masterdata and auth tokens into the per-run cache ({@code urlBase + run_context})
 * so packet generation does not repeat the same GET/POST calls for every scenario.
 * <p>
 * Suite flow: {@code POST /runCache/warm/{contextKey}} once per worker → scenarios →
 * {@code POST /runCache/clear/{contextKey}}. Cached via {@link MasterdataCache} (keys {@code md:get:*},
 * {@code md:entity:*}). See {@link MasterdataCache#WARM_OPERATIONS}.
 */
public final class RunCacheService {

	private static final Logger logger = LoggerFactory.getLogger(RunCacheService.class);

	private RunCacheService() {
	}

	public static Map<String, Object> warm(String contextKey) {
		Map<String, Object> summary = new LinkedHashMap<>();
		summary.put("contextKey", contextKey);
		summary.put("masterdataCacheVersion", MasterdataCache.CACHE_VERSION);

		try {
			Object urlBase = VariableManager.getVariableValue(contextKey, "urlBase");
			if (urlBase == null || urlBase.toString().isBlank()) {
				summary.put("urlBase", "missing");
				logger.warn("Run cache warm: urlBase not set for context {}", contextKey);
				return summary;
			}
			summary.put("urlBase", urlBase.toString().trim());
		} catch (Exception e) {
			summary.put("urlBase", "missing");
			logger.warn("Run cache warm: urlBase not set for context {}", contextKey);
			return summary;
		}

		summary.put("authSystem", RestClient.initToken(contextKey) ? "ok" : "failed");
		summary.put("authAdmin", RestClient.initToken_admin(contextKey) ? "ok" : "failed");
		warmClientSecretTokens(contextKey, summary);

		List<MosipLanguage> languages = MosipMasterData.getConfiguredLanguages(contextKey);
		int langCount = languages != null ? languages.size() : 0;
		summary.put("languages", langCount);

		List<String> langCodes = new ArrayList<>();
		if (languages != null) {
			for (MosipLanguage lang : languages) {
				if (lang != null && lang.getCode() != null) {
					langCodes.add(lang.getCode());
				}
			}
		}
		if (langCodes.isEmpty()) {
			langCodes.add("eng");
		}
		if (!langCodes.contains("ara")) {
			langCodes.add("ara");
		}
		if (!langCodes.contains("eng")) {
			langCodes.add("eng");
		}

		try {
			VariableManager.setVariableValue(contextKey, "langCode",
					langCodes.isEmpty() ? "eng" : langCodes.get(0));
		} catch (Exception e) {
			logger.debug("Could not set langCode on context: {}", e.getMessage());
		}

		for (String langCode : langCodes) {
			try {
				MosipMasterData.getGenderTypes(langCode, contextKey);
				MosipMasterData.getLocationHierarchy(langCode, contextKey);
			} catch (Exception e) {
				logger.warn("Run cache warm: gender/location hierarchy for {} failed: {}", langCode, e.getMessage());
			}
		}

		try {
			MosipMasterData.getAllDynamicFields(contextKey);
			summary.put("dynamicFields", "ok");
		} catch (Exception e) {
			summary.put("dynamicFields", "failed");
			logger.warn("Run cache warm: dynamic fields failed: {}", e.getMessage());
		}

		try {
			VariableManager.setVariableValue(contextKey, "process", "NEW");
			MosipMasterData.getIDSchemaLatestVersion(contextKey);
			MosipMasterData.getIDSchemaSchemaLatestVersion(contextKey);
			summary.put("idSchemaLatest", "ok");
		} catch (Exception e) {
			summary.put("idSchemaLatest", "failed");
			logger.warn("Run cache warm: idschema/latest failed: {}", e.getMessage());
		}

		try {
			MosipMasterData.getIndividualTypes(contextKey);
			summary.put("individualTypes", "ok");
		} catch (Exception e) {
			summary.put("individualTypes", "failed");
		}

		try {
			MosipMasterData.getDocumentCategories(contextKey);
			summary.put("documentCategories", "ok");
		} catch (Exception e) {
			summary.put("documentCategories", "failed");
		}

		int validDocsWarmed = MosipMasterData.warmValidDocumentsCache(contextKey, langCodes);
		summary.put("validDocumentsEndpoints", validDocsWarmed);

		int locationChildrenWarmed = MosipMasterData.warmLocationImmediateChildrenCache(contextKey, langCodes, 80);
		summary.put("locationImmediateChildrenEndpoints", locationChildrenWarmed);

		// Biometric masterdata and mock-abis are intentionally not warmed: each scenario needs unique biometrics.

		for (String langCode : langCodes) {
			try {
				MosipMasterData.getPreregLocHierarchy(langCode, 1, contextKey);
				summary.put("locationHierarchy_" + langCode, "ok");
			} catch (Exception e) {
				summary.put("locationHierarchy_" + langCode, "failed");
				logger.debug("Run cache warm: prereg location for {} failed: {}", langCode, e.getMessage());
			}
		}

		try {
			MosipMasterData.getPreregLoginConfig(contextKey);
			summary.put("preregLoginConfig", "ok");
		} catch (Exception e) {
			summary.put("preregLoginConfig", "failed");
		}

		logger.info("Run cache warm completed for context {}: {}", contextKey, summary);
		return summary;
	}

	private static void warmClientSecretTokens(String contextKey, Map<String, Object> summary) {
		if (hasVariable(contextKey, "mosip_resident_client_id")) {
			summary.put("authResident",
					RestClient.initToken_Resident(contextKey) ? "ok" : "failed");
		}
		if (hasVariable(contextKey, "mosip_regproc_client_id")) {
			summary.put("authRegproc",
					RestClient.initToken_Regproc(contextKey) ? "ok" : "failed");
		}
	}

	private static boolean hasVariable(String contextKey, String name) {
		try {
			Object value = VariableManager.getVariableValue(contextKey, name);
			return value != null && !value.toString().isBlank();
		} catch (Exception e) {
			return false;
		}
	}

	public static void clear(String contextKey) {
		RestClient.clearRunScopedCache(contextKey);
		logger.info("Run cache cleared for context {}", contextKey);
	}
}
