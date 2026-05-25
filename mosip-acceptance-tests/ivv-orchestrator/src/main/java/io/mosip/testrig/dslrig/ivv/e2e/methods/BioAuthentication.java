package io.mosip.testrig.dslrig.ivv.e2e.methods;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import io.mosip.testrig.apirig.auth.testscripts.BioAuth;
import io.mosip.testrig.apirig.dto.TestCaseDTO;
import io.mosip.testrig.apirig.utils.AdminTestUtil;
import io.mosip.testrig.apirig.testrunner.JsonPrecondtion;
import io.mosip.testrig.apirig.testrunner.BaseTestCase;
import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.e2e.constant.E2EConstants;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testrig.dslrig.ivv.orchestrator.TestRunner;
import io.mosip.testrig.dslrig.ivv.orchestrator.dslConfigManager;

public class BioAuthentication extends BaseTestCaseUtil implements StepInterface {
	public static Logger logger = Logger.getLogger(BioAuthentication.class);
	private static final String BIOMETRIC_FACE = "idaData/BioAuth/BioAuth.yml";
	private static final String BIOMETRIC_FACE_NEGATIVE = "idaData/BioAuth/BioAuthNegative.yml";
	Properties deviceProp = null;
	Properties uinResidentDataPathFinalProps = new Properties();
	String bioResponse = null;
	List<String> idType = BaseTestCase.getSupportedIdTypesValueFromActuator();
	List<Object> casesListUIN = null;
	List<Object> casesListVID = null;

	BioAuth bioAuth = new BioAuth();

	static {
		if (dslConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	@Override
	public void run() throws RigInternalError {

		String deviceInfoFilePath = null;
		String uins = null;
		String vids = null;
		List<String> uinList = null;
		List<String> vidList = null;
		String  SceanrioFlow= "POSTIVE";

		if (step.getParameters() == null || step.getParameters().isEmpty() || step.getParameters().size() < 1) {
			logger.error("Parameter is  missing from DSL step");
			this.hasError = true;
			throw new RigInternalError("Modality paramter is  missing in step: " + step.getName());
		} else {
			deviceInfoFilePath = step.getParameters().get(0);
			if (!StringUtils.isBlank(deviceInfoFilePath)) {
				deviceInfoFilePath = TestRunner.getExternalResourcePath()
						+ props.getProperty("ivv.path.deviceinfo.folder") + deviceInfoFilePath + ".properties";
				deviceProp = AdminTestUtil.getproperty(deviceInfoFilePath);
			} else {
				this.hasError = true;
				throw new RigInternalError("deviceInfo file path Parameter is  missing from DSL step");
			}
		}
		if (step.getParameters().size() == 2) {
			uins = step.getParameters().get(1);
			if (!StringUtils.isBlank(uins))
				uinList = new ArrayList<>(Arrays.asList(uins.split("@@")));
		} else if (step.getParameters().size() == 4 || step.getParameters().size() == 5) { 
			uins = step.getParameters().get(1);
			String _personaFilePath = step.getParameters().get(3);
			if (uins.startsWith("$$") && _personaFilePath.startsWith("$$")) {
				uins = step.getScenario().getVariables().get(uins);
				_personaFilePath = step.getScenario().getVariables().get(_personaFilePath);
				uinList = new ArrayList<>();
				uinList.add(uins);
				step.getScenario().getUinPersonaProp().put(uins, _personaFilePath);
			}
		} else
			uinList = new ArrayList<>(step.getScenario().getUinPersonaProp().stringPropertyNames());

		if (step.getParameters().size() == 2) {
			vids = step.getParameters().get(1);
			if (!StringUtils.isBlank(vids))
				vidList = new ArrayList<>(Arrays.asList(vids.split("@@")));
		} else if (step.getParameters().size() == 4 || step.getParameters().size() == 5) { 
			vids = step.getParameters().get(2);
			String _personaFilePath = step.getParameters().get(3);
			if (vids.startsWith("$$") && _personaFilePath.startsWith("$$")) {
				vids = step.getScenario().getVariables().get(vids);
				_personaFilePath = step.getScenario().getVariables().get(_personaFilePath);
				vidList = new ArrayList<>();
				vidList.add(vids);
				step.getScenario().getVidPersonaProp().put(vids, _personaFilePath);
			}
		}else
			vidList = new ArrayList<>(step.getScenario().getVidPersonaProp().stringPropertyNames());
		if (step.getParameters().size() == 5) { 
			 SceanrioFlow= step.getParameters().get(4);
			}
		for (String uin : uinList) {
			String personFilePathvalue = null;
			if (step.getScenario().getUinPersonaProp().containsKey(uin))
				personFilePathvalue = step.getScenario().getUinPersonaProp().getProperty(uin);
			else {
				this.hasError = true;
				throw new RigInternalError("Persona doesn't exist for the given UIN " + uin);
			}

			String bioType = null, bioSubType = null;
			List<String> modalityList = new ArrayList<>();
			String modalityToLog = null;
			String modalityKeyTogetBioValue = null;
			if (deviceProp != null) {
				bioType = deviceProp.getProperty("bioType");
				bioSubType = deviceProp.getProperty("bioSubType");
				switch (bioType) {
				case E2EConstants.FACEBIOTYPE:
					modalityList.add(E2EConstants.FACEFETCH);
					modalityToLog = bioType;
					modalityKeyTogetBioValue = E2EConstants.FACEFETCH;
					break;
				case E2EConstants.IRISBIOTYPE:
					modalityList.add(E2EConstants.IRISFETCH);
					modalityToLog = bioSubType + "_" + bioType;
					modalityKeyTogetBioValue = (bioSubType.equalsIgnoreCase("left")) ? E2EConstants.LEFT_EYE
							: E2EConstants.RIGHT_EYE;
					break;
				case E2EConstants.FINGERBIOTYPE:
					modalityList.add(E2EConstants.FINGERFETCH);
					modalityToLog = bioSubType;
					modalityKeyTogetBioValue = bioSubType;
					break;
				default:
					this.hasError = true;
					throw new RigInternalError("Given BIO Type in device property file is not valid");
				}
			}

			bioResponse = packetUtility.retrieveBiometric(personFilePathvalue, modalityList, step);
			if (bioResponse == null || bioResponse.isBlank()) {
				this.hasError = true;
				throw new RigInternalError("retrieveBiometric returned empty response for modality " + modalityToLog
						+ " and persona " + personFilePathvalue);
			}

			String fileName = null;
			if(SceanrioFlow.equalsIgnoreCase("ERROR"))
				fileName = BIOMETRIC_FACE_NEGATIVE;
			else
				fileName = BIOMETRIC_FACE;
			bioAuth.isInternal = false;
			Object[] casesListUIN = null;
			Object[] casesListVID = null;

			if (BaseTestCase.getSupportedIdTypesValueFromActuator().contains("UIN")
					|| BaseTestCase.getSupportedIdTypesValueFromActuator().contains("uin")) {

				casesListUIN = bioAuth.getYmlTestData(fileName);

			}

			else if (BaseTestCase.getSupportedIdTypesValueFromActuator().contains("VID")
					|| BaseTestCase.getSupportedIdTypesValueFromActuator().contains("vid")) {
				casesListVID = bioAuth.getYmlTestData(fileName);
			}

			else {
				casesListUIN = bioAuth.getYmlTestData(fileName);
				casesListVID = bioAuth.getYmlTestData(fileName);
			}

			if (bioResponse != null && !bioResponse.isEmpty() && modalityKeyTogetBioValue != null) {
				String bioValue = resolveBioValueForAuth(bioResponse, modalityKeyTogetBioValue, modalityToLog,
						personFilePathvalue);
				if (idType.contains("UIN") || idType.contains("uin")) {
					casesListUIN = bioAuth.getYmlTestData(fileName);
				}

				if (casesListUIN != null) {
					for (Object object : casesListUIN) {
						TestCaseDTO test = (TestCaseDTO) object;
						packetUtility.bioAuth(modalityToLog, bioValue, uin, deviceProp, test, bioAuth, step);
					}
				}

			}
		}

		for (String vid : vidList) {
			Object[] testObj = null;
			if(SceanrioFlow.equalsIgnoreCase("ERROR"))
				testObj=bioAuth.getYmlTestData(BIOMETRIC_FACE_NEGATIVE);
			else
				testObj=bioAuth.getYmlTestData(BIOMETRIC_FACE);

			TestCaseDTO test = (TestCaseDTO) testObj[0];
			String input = test.getInput();
			String personFilePathvalue = null;
			if (step.getScenario().getVidPersonaProp().containsKey(vid))
				personFilePathvalue = step.getScenario().getVidPersonaProp().getProperty(vid);
			else {
				this.hasError = true;
				throw new RigInternalError("Persona doesn't exist for the given VID " + vid);
			}
			String bioType = null, bioSubType = null;
			List<String> modalityList = new ArrayList<>();
			String modalityToLog = null;
			String modalityKeyTogetBioValue = null;
			if (deviceProp != null) {
				bioType = deviceProp.getProperty("bioType");
				bioSubType = deviceProp.getProperty("bioSubType");
				switch (bioType) {
				case E2EConstants.FACEBIOTYPE:
					modalityList.add(E2EConstants.FACEFETCH);
					modalityToLog = bioType;
					modalityKeyTogetBioValue = E2EConstants.FACEFETCH;
					break;
				case E2EConstants.IRISBIOTYPE:
					modalityList.add(E2EConstants.IRISFETCH);
					modalityToLog = bioSubType + "_" + bioType;
					modalityKeyTogetBioValue = (bioSubType.equalsIgnoreCase("left")) ? E2EConstants.LEFT_EYE
							: E2EConstants.RIGHT_EYE;
					break;
				case E2EConstants.FINGERBIOTYPE:
					modalityList.add(E2EConstants.FINGERFETCH);
					modalityToLog = bioSubType;
					modalityKeyTogetBioValue = bioSubType;
					break;
				default:
					this.hasError = true;
					throw new RigInternalError("Given BIO Type in device property file is not valid");
				}
			}

			bioResponse = packetUtility.retrieveBiometric(personFilePathvalue, modalityList, step);
			if (bioResponse == null || bioResponse.isBlank()) {
				this.hasError = true;
				throw new RigInternalError("retrieveBiometric returned empty response for modality " + modalityToLog
						+ " and persona " + personFilePathvalue);
			}
			String fileName = null;
			if(SceanrioFlow.equalsIgnoreCase("ERROR"))
				fileName = BIOMETRIC_FACE_NEGATIVE;
			else
				fileName = BIOMETRIC_FACE;
			bioAuth.isInternal = false;
			Object[] casesListUIN = null;
			Object[] casesListVID = null;

			input = JsonPrecondtion.parseAndReturnJsonContent(input, "VID", "individualIdType");

			if (BaseTestCase.getSupportedIdTypesValueFromActuator().contains("UIN")
					|| BaseTestCase.getSupportedIdTypesValueFromActuator().contains("uin")) {

				casesListUIN = bioAuth.getYmlTestData(fileName);

			}

			else if (BaseTestCase.getSupportedIdTypesValueFromActuator().contains("VID")
					|| BaseTestCase.getSupportedIdTypesValueFromActuator().contains("vid")) {
				casesListVID = bioAuth.getYmlTestData(fileName);
			}

			else {
				casesListUIN = bioAuth.getYmlTestData(fileName);
				casesListVID = bioAuth.getYmlTestData(fileName);
			}

			if (bioResponse != null && !bioResponse.isEmpty() && modalityKeyTogetBioValue != null) {
				String bioValue = resolveBioValueForAuth(bioResponse, modalityKeyTogetBioValue, modalityToLog,
						personFilePathvalue);
				if (idType.contains("VID") || idType.contains("vid")) {
					casesListVID = bioAuth.getYmlTestData(fileName);
				}

				if (casesListVID != null) {
					for (Object object : casesListVID) {
						test.setInput(input);
						packetUtility.bioAuth(modalityToLog, bioValue, vid, deviceProp, test, bioAuth, step);						
					}
				}
			}
		}
	}

	private String resolveBioValueForAuth(String bioResponse, String modalityKey, String modalityToLog,
			String personaPath) throws RigInternalError {
		String bioValue = extractBioValueFromPersonaResponse(bioResponse, modalityKey);
		if (bioValue == null || bioValue.isBlank() || "null".equalsIgnoreCase(bioValue.trim())) {
			this.hasError = true;
			throw new RigInternalError("Not able to get the bio value for field " + modalityToLog
					+ " (key " + modalityKey + ") from persona " + personaPath
					+ ". Packet creator returned null or missing MDS capture (for old-bio persona, clone after the first NEW packet is processed).");
		}
		bioValue = bioValue.trim().replaceAll("\\s+", "");
		try {
			byte[] decodedBioMetricValue = Base64.getUrlDecoder().decode(bioValue);
			bioValue = Base64.getEncoder().encodeToString(decodedBioMetricValue);
		} catch (IllegalArgumentException e) {
			try {
				byte[] decodedBioMetricValue = Base64.getDecoder().decode(bioValue);
				bioValue = Base64.getEncoder().encodeToString(decodedBioMetricValue);
			} catch (IllegalArgumentException e2) {
				this.hasError = true;
				throw new RigInternalError("Invalid biometric data for field " + modalityToLog + " from persona "
						+ personaPath + ": " + e.getMessage());
			}
		}
		if (bioValue.length() < 100) {
			this.hasError = true;
			throw new RigInternalError(
					"Not able to get the bio value for field " + modalityToLog + " from persona " + personaPath);
		}
		return bioValue;
	}

	/**
	 * Reads a biometric attribute from packet-creator persona JSON (e.g. {@code face_encrypted},
	 * {@code iris_encrypted.left}). Uses {@link JSONObject} for null-safe parsing; supports dotted
	 * paths; falls back to {@link JsonPrecondtion} for legacy responses.
	 */
	private String extractBioValueFromPersonaResponse(String bioResponse, String modalityKey) {
		if (bioResponse == null || bioResponse.isBlank() || modalityKey == null || modalityKey.isBlank()) {
			return null;
		}
		String trimmed = bioResponse.trim();
		try {
			String nested = getNestedJsonString(new JSONObject(trimmed), modalityKey);
			if (isUsableBioValue(nested)) {
				return nested;
			}
		} catch (org.json.JSONException ignored) {
			// fall through to JsonPrecondtion
		}
		String fromPrecond = JsonPrecondtion.getValueFromJson(trimmed, modalityKey);
		return isUsableBioValue(fromPrecond) ? fromPrecond : null;
	}

	private static boolean isUsableBioValue(String value) {
		if (value == null || value.isBlank() || "null".equalsIgnoreCase(value.trim())) {
			return false;
		}
		String t = value.trim();
		return !t.startsWith("{") && !t.startsWith("[");
	}

	private static String getNestedJsonString(JSONObject json, String path) {
		if (json == null || path == null || path.isBlank()) {
			return null;
		}
		int dot = path.indexOf('.');
		if (dot < 0) {
			if (!json.has(path) || json.isNull(path)) {
				return null;
			}
			return valueAsBioString(json.get(path));
		}
		String parentKey = path.substring(0, dot);
		String childPath = path.substring(dot + 1);
		if (!json.has(parentKey) || json.isNull(parentKey)) {
			return null;
		}
		Object parent = json.get(parentKey);
		if (parent instanceof JSONObject) {
			return getNestedJsonString((JSONObject) parent, childPath);
		}
		if (parent instanceof String) {
			try {
				return getNestedJsonString(new JSONObject((String) parent), childPath);
			} catch (org.json.JSONException e) {
				return null;
			}
		}
		return null;
	}

	private static String valueAsBioString(Object raw) {
		if (raw == null || raw == JSONObject.NULL) {
			return null;
		}
		return raw instanceof String ? (String) raw : raw.toString();
	}

}
