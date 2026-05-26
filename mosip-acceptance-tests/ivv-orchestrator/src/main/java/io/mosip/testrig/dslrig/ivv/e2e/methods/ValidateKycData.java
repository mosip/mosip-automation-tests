package io.mosip.testrig.dslrig.ivv.e2e.methods;

import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.testng.Reporter;

import io.mosip.testrig.apirig.utils.ConfigManager;
import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.FeatureNotSupportedError;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.e2e.constant.E2EConstants;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testrig.dslrig.ivv.orchestrator.DslReportLogUtil;

public class ValidateKycData extends BaseTestCaseUtil implements StepInterface {

	private static final Logger logger = Logger.getLogger(ValidateKycData.class);

	private static final String KEYS_TO_VALIDATE_PROP = "keysToValidateInKYC";
	private static final String ALWAYS_VALIDATE_PROP = "kycAlwaysValidateFields";

	private static final Set<String> VALIDATE_ALL_TOKENS = Set.of("*", "all", "any");

	/** Demo-auth request field names mapped to common decrypted eKYC JSON keys. */
	private static final Map<String, String> DEMO_FIELD_TO_KYC_KEY = Map.of(
			E2EConstants.DEMONAME, "name_eng",
			E2EConstants.DEMODOB, E2EConstants.DEMODOB,
			E2EConstants.DEMOEMAIL, "emailId",
			E2EConstants.DEMOYMLPHONE, "phoneNumber",
			E2EConstants.DEMOPHONE, "phoneNumber",
			E2EConstants.DEMOAGE, "age");

	private static final Set<String> DEMO_AUTH_FIELD_NAMES = Set.of(
			E2EConstants.DEMONAME,
			E2EConstants.DEMODOB,
			E2EConstants.DEMOEMAIL,
			E2EConstants.DEMOYMLPHONE,
			E2EConstants.DEMOPHONE,
			E2EConstants.DEMOAGE,
			E2EConstants.DEMOGENDER);

	String responce = "";
	String newResponse = "";
	JSONObject responseJson;

	@Override
	public void run() throws RigInternalError, FeatureNotSupportedError {
		try {
			if (step.getParameters() == null || step.getParameters().isEmpty()) {
				logger.error("Parameter is  missing from DSL step");
				assertTrue(false, " paramter is  missing in step: " + step.getName());
				return;
			}
			if (step.getParameters().size() < 2) {
				throw new RigInternalError("validateKycData requires KYC field and response variable parameters");
			}

			String requestedFields = step.getParameters().get(0);
			responce = step.getParameters().get(1);
			newResponse = step.getScenario().getVariables().get(responce);
			responseJson = new JSONObject(newResponse);

			List<String> fieldsToValidate = resolveFieldsToValidate(requestedFields);
			boolean requestedDemoAuthField = fieldsToValidate.stream()
					.anyMatch(f -> DEMO_AUTH_FIELD_NAMES.contains(f.toLowerCase(Locale.ROOT)));

			for (String requested : fieldsToValidate) {
				String resolvedKey = resolveKycResponseKey(requested, responseJson);
				if (resolvedKey == null) {
					throw new RigInternalError(buildMissingFieldMessage(requested));
				}
				assertKycFieldPresent(resolvedKey, requested);
			}

			if (requestedDemoAuthField) {
				for (String supplemental : configuredSupplementalFields()) {
					if (fieldsToValidate.stream().anyMatch(f -> f.equalsIgnoreCase(supplemental))) {
						continue;
					}
					String resolvedKey = resolveKycResponseKey(supplemental, responseJson);
					if (resolvedKey == null) {
						throw new RigInternalError(buildMissingFieldMessage(supplemental));
					}
					assertKycFieldPresent(resolvedKey, supplemental);
				}
			}

			DslReportLogUtil.reportRequest("decryptEkycData", newResponse);
		} catch (RigInternalError e) {
			this.hasError = true;
			logger.error(e.getMessage());
			throw e;
		} catch (Exception e) {
			this.hasError = true;
			logger.error(e.getMessage());
			throw new RigInternalError("Failed to validate kyc data: " + e.getMessage());
		}
	}

	private List<String> resolveFieldsToValidate(String requestedFields) {
		if (requestedFields == null || requestedFields.isBlank()
				|| VALIDATE_ALL_TOKENS.contains(requestedFields.trim().toLowerCase(Locale.ROOT))) {
			return configuredKeysToValidate();
		}
		List<String> fields = new ArrayList<>();
		for (String part : requestedFields.split("@@")) {
			for (String field : part.split(",")) {
				String trimmed = field.trim();
				if (!trimmed.isEmpty()) {
					fields.add(trimmed);
				}
			}
		}
		return fields;
	}

	private static List<String> configuredKeysToValidate() {
		String configured = ConfigManager.getproperty(KEYS_TO_VALIDATE_PROP);
		if (configured == null || configured.isBlank()) {
			return List.of("photo");
		}
		return Arrays.asList(configured.split(","));
	}

	private static List<String> configuredSupplementalFields() {
		String configured = ConfigManager.getproperty(ALWAYS_VALIDATE_PROP);
		if (configured == null || configured.isBlank()) {
			return List.of("photo");
		}
		if ("none".equalsIgnoreCase(configured.trim())) {
			return List.of();
		}
		return Arrays.asList(configured.split(","));
	}

	/**
	 * Maps a DSL/Gherkin field token to an actual key present in decrypted eKYC JSON
	 * (e.g. demo {@code name} -> {@code name_eng}, {@code phone} -> {@code phoneNumber}).
	 */
	static String resolveKycResponseKey(String requested, JSONObject responseJson) {
		if (requested == null || requested.isBlank() || responseJson == null) {
			return null;
		}
		String trimmed = requested.trim();
		if (responseJson.has(trimmed) && !responseJson.isNull(trimmed)) {
			return trimmed;
		}

		String alias = DEMO_FIELD_TO_KYC_KEY.get(trimmed.toLowerCase(Locale.ROOT));
		if (alias == null) {
			alias = DEMO_FIELD_TO_KYC_KEY.get(trimmed);
		}
		if (alias != null && responseJson.has(alias) && !responseJson.isNull(alias)) {
			return alias;
		}

		if (E2EConstants.DEMONAME.equalsIgnoreCase(trimmed)) {
			String nameKey = firstKeyMatchingPrefix(responseJson, "name_");
			if (nameKey != null) {
				return nameKey;
			}
		}

		String suffixed = trimmed + "_eng";
		if (responseJson.has(suffixed) && !responseJson.isNull(suffixed)) {
			return suffixed;
		}

		for (String candidate : configuredKeysToValidate()) {
			if (candidate.equalsIgnoreCase(trimmed) && responseJson.has(candidate) && !responseJson.isNull(candidate)) {
				return candidate;
			}
		}

		String caseInsensitive = firstKeyEqualsIgnoreCase(responseJson, trimmed);
		if (caseInsensitive != null) {
			return caseInsensitive;
		}

		return null;
	}

	private static String firstKeyMatchingPrefix(JSONObject json, String prefix) {
		Iterator<String> keys = json.keys();
		while (keys.hasNext()) {
			String key = keys.next();
			if (key.startsWith(prefix) && !json.isNull(key)) {
				return key;
			}
		}
		return null;
	}

	private static String firstKeyEqualsIgnoreCase(JSONObject json, String requested) {
		Iterator<String> keys = json.keys();
		while (keys.hasNext()) {
			String key = keys.next();
			if (key.equalsIgnoreCase(requested) && !json.isNull(key)) {
				return key;
			}
		}
		return null;
	}

	private void assertKycFieldPresent(String resolvedKey, String requestedLabel) throws RigInternalError {
		if (!responseJson.has(resolvedKey) || responseJson.isNull(resolvedKey)) {
			throw new RigInternalError(buildMissingFieldMessage(requestedLabel));
		}
		Object value = responseJson.get(resolvedKey);
		if (value instanceof String stringValue && stringValue.isBlank()) {
			throw new RigInternalError(resolvedKey + " is empty in decryptEkycData");
		}
		String logLabel = resolvedKey.equals(requestedLabel) ? resolvedKey : requestedLabel + " (as " + resolvedKey + ")";
		logger.info(logLabel + " data is there");
		Reporter.log("<b style=\"background-color: #0A0;\">Marking test case as passed. As " + logLabel
				+ " data is there in a decryptEkycData</b><br>\n");
	}

	private String buildMissingFieldMessage(String requested) {
		Set<String> available = new LinkedHashSet<>();
		Iterator<String> keys = responseJson.keys();
		while (keys.hasNext()) {
			available.add(keys.next());
		}
		return requested + " Data is not there in a decryptEkycData. Available keys: " + available;
	}
}
