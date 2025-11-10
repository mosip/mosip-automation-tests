package io.mosip.testrig.dslrig.ivv.e2e.methods;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import io.mosip.testrig.apirig.utils.AdminTestUtil;
import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testrig.dslrig.ivv.orchestrator.dslConfigManager;

public class UpdateDemoOrBioDetails extends BaseTestCaseUtil implements StepInterface {
	static Logger logger = Logger.getLogger(UpdateDemoOrBioDetails.class);

	static {
		if (dslConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	@Override
	public void run() throws RigInternalError {
		String bioType = null;
		String missFields = null;
		String updateAttribute = null;
		String blocklistedWord = null;
		String testPersona = null;
		if (step.getParameters() == null || step.getParameters().isEmpty()) {
			logger.error("Parameter is  missing from DSL step");
			this.hasError = true;
			throw new RigInternalError("bioType paramter is  missing in step: " + step.getName());
		} else {
			bioType = step.getParameters().get(0);
			if (step.getParameters().size() > 1)
				missFields = step.getParameters().get(1);
			if (step.getParameters().size() > 2)
				updateAttribute = step.getParameters().get(2);

			if (!updateAttribute.contentEquals("0")) {
				Object selectedHandlesObj = step.getScenario().getObjectVariables().get(updateAttribute);
				if (updateAttribute.contains("$$selectedHandles") && selectedHandlesObj instanceof Map<?, ?>) {
					@SuppressWarnings("unchecked")
				    Map<String, JSONObject> selectedHandles = (Map<String, JSONObject>) selectedHandlesObj;

					String handleValues = generateHandleValues(selectedHandles);
					updateAttribute = updateAttribute.replace("$$selectedHandles", handleValues);

				} else if (updateAttribute.contains("$$")) {
					blocklistedWord = updateAttribute.substring(5);
					updateAttribute = updateAttribute.replace(blocklistedWord,
							step.getScenario().getVariables().get(blocklistedWord));
				}
			}
		}
		List<String> regenAttributeList = (bioType != null) ? Arrays.asList(bioType.split("@@")) : new ArrayList<>();
		List<String> missFieldsAttributeList = (missFields != null) ? Arrays.asList(missFields.split("@@"))
				: new ArrayList<>();
		List<String> updateAttributeList = (updateAttribute != null) ? Arrays.asList(updateAttribute.split("@@"))
				: new ArrayList<>();

		if (!step.getParameters().isEmpty() && step.getParameters().size() > 3) { // "var1=e2e_updateDemoOrBioDetails(0,0,0,$$personaPath)"
			String personaFilePath = step.getParameters().get(3);

			if (step.getParameters().size() == 5) {
				testPersona = step.getParameters().get(4);
				testPersona = step.getScenario().getVariables().get(testPersona);
			}

			if (personaFilePath.startsWith("$$")) {
				personaFilePath = step.getScenario().getVariables().get(personaFilePath);
				packetUtility.updateDemoOrBioDetail(personaFilePath, testPersona,
						(regenAttributeList.get(0).equalsIgnoreCase("0")) ? null : regenAttributeList,
						(missFieldsAttributeList.get(0).equalsIgnoreCase("0")) ? new ArrayList<>()
								: missFieldsAttributeList,
						(updateAttributeList.get(0).equalsIgnoreCase("0")) ? new ArrayList<>() : updateAttributeList,
						step);
			}
		} else {
			for (String resDataPath : step.getScenario().getResidentTemplatePaths().keySet()) {
				packetUtility.updateDemoOrBioDetail(resDataPath, testPersona,
						(regenAttributeList.get(0).equalsIgnoreCase("0")) ? null : regenAttributeList,
						(missFieldsAttributeList.get(0).equalsIgnoreCase("0")) ? new ArrayList<>()
								: missFieldsAttributeList,
						updateAttributeList, step);

			}
		}
	}
	
	public static String generateHandleValues(String fieldName, JSONObject fieldSchema) {
	    String type = fieldSchema.optString("type", "string");
	    String pattern = extractRegexPattern(fieldSchema);

	    // For uniqueness
	    String uniqueSuffix = String.valueOf(System.currentTimeMillis());

	    if ("string".equals(type)) {
	        // If pattern is digits only (e.g., ^[0-9]{10}$ or ^\d{10}$)
	        if (pattern != null && pattern.matches("^\\(?[\\d\\[\\]{}()^$+.*?]+\\)?$")) {
	            int digits = extractExpectedDigits(pattern);
	            return AdminTestUtil.generateRandomNumberString(digits);
	        }

//	        if ("email".equalsIgnoreCase(format) || fieldName.toLowerCase().contains("email")) {
//	            return "handleUpdate_" + uniqueSuffix + "@example.com";
//	        }

	        return "handleUpdate_" + fieldName + "_" + uniqueSuffix;
	    }

	    if ("number".equals(type) || "integer".equals(type)) {
	        int digits = extractExpectedDigits(pattern);
	        return AdminTestUtil.generateRandomNumberString(digits);
	    }

	    if ("boolean".equals(type)) {
	        return String.valueOf(System.currentTimeMillis() % 2 == 0);
	    }

	    return "handleUpdate_" + fieldName + "_" + uniqueSuffix;
	}

	
	private static String extractRegexPattern(JSONObject fieldSchema) {
	    if (fieldSchema.has("validators")) {
	        JSONArray validators = fieldSchema.getJSONArray("validators");
	        for (int i = 0; i < validators.length(); i++) {
	            JSONObject validator = validators.getJSONObject(i);
	            if ("regex".equalsIgnoreCase(validator.optString("type")) ||
	                validator.optString("validator").startsWith("^")) {
	                return validator.optString("validator");
	            }
	        }
	    }
	    return null;
	}

	private static int extractExpectedDigits(String pattern) {
	    if (pattern != null) {
	        // Supports both \d{10} and [0-9]{10}
	        Matcher m1 = Pattern.compile("\\\\d\\{(\\d+)}").matcher(pattern);
	        if (m1.find()) return Integer.parseInt(m1.group(1));

	        Matcher m2 = Pattern.compile("\\[0-9]{(\\d+)}").matcher(pattern);
	        if (m2.find()) return Integer.parseInt(m2.group(1));
	    }
	    return 6; // default
	}
	
	public static String generateHandleValues(Map<String, JSONObject> selectedHandles) {
	    List<String> handleValuePairs = new ArrayList<>();

	    for (Map.Entry<String, JSONObject> entry : selectedHandles.entrySet()) {
	        String handleKey = entry.getKey();
	        JSONObject handleSchema = entry.getValue();

	        String sampleValue = generateHandleValues(handleKey, handleSchema); 
	        handleValuePairs.add(handleKey + "=" + sampleValue);
	    }

	    return String.join("@@", handleValuePairs);
	}


}
