package io.mosip.ivv.e2e.methods;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Properties;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.testng.Reporter;
import io.mosip.admin.fw.util.AdminTestException;
import io.mosip.admin.fw.util.AdminTestUtil;
import io.mosip.admin.fw.util.TestCaseDTO;
import io.mosip.authentication.fw.precon.JsonPrecondtion;
import io.mosip.authentication.fw.util.AuthPartnerProcessor;
import io.mosip.authentication.fw.util.AuthenticationTestException;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.e2e.constant.E2EConstants;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.ivv.orchestrator.TestRunner;
import io.mosip.service.BaseTestCase;
import io.mosip.testscripts.BioAuth;
import io.mosip.testscripts.BioAuthOld;
import io.mosip.testscripts.DemoAuth;
import io.mosip.testscripts.DemoAuthSimplePostForAutoGenId;
import io.mosip.testscripts.MultiFactorAuth;
import io.mosip.testscripts.MultiFactorAuthNew;
import io.mosip.testscripts.OtpAuth;

//"e2e_multiFactorAuthentication(faceDevice,phoneNumber,UIN,$$uin,$$personaFilePath)"
public class MultiFactorAuthentication extends BaseTestCaseUtil implements StepInterface {
	static Logger logger = Logger.getLogger(MultiFactorAuthentication.class);
	private static final String MULTIFACTOR = "idaData/MultiFactorAuth/MultiFactorAuth.yml";
	Properties deviceProp = null;
	Properties uinResidentDataPathFinalProps = new Properties();
	OtpAuth otpAuth = new OtpAuth();
	MultiFactorAuthNew multiFactorAuth = new MultiFactorAuthNew();
	BioAuth bioAuth = new BioAuth();
	DemoAuthSimplePostForAutoGenId demoAuth = new DemoAuthSimplePostForAutoGenId();
	List<String> demoAuthList = null;
	List<String> bioAuthList = null;
	String individualType = null;
	String individualIdAuth = null;
	String vids = null;
	List<String> uinList = null;
	List<String> vidList = null;
	Object[] casesListUIN = null;
	Object[] casesListVID = null;
	String bioResponse = null;
	String demofields = null;
	String multiFactorResponse = null;
	String uins = null;
	String demoResponse = null;
	List<String> idType = BaseTestCase.getSupportedIdTypesValueFromActuator();

	@Override
	public void run() throws RigInternalError {
		// AuthPartnerProcessor.startProcess();
		// uinPersonaProp.put("2310290713",
		// "C:\\\\Users\\\\user\\\\AppData\\\\Local\\\\Temp\\\\residents_8783170256176160783\\\\915849158491584.json");

		List<String> demoFetchList = null;
		TestCaseDTO test = null;

		if (step.getParameters().isEmpty() || step.getParameters().size() < 1) {
			logger.error("Parameter is  missing from DSL step");
			throw new RigInternalError("Modality paramter is  missing in step: " + step.getName());
		}

		if (step.getParameters().size() > 1) {
			for (int i = 0; i <= step.getParameters().size(); i++) {
				if (i == 0) {
					if (!StringUtils.isBlank(step.getParameters().get(i))) {
						bioAuthList = Arrays.asList(step.getParameters().get(i).split("@@"));
					} else {
						bioAuthList = new ArrayList<String>(uinPersonaProp.stringPropertyNames());
					}
				}

				if (i == 1) {
					if (!StringUtils.isBlank(step.getParameters().get(i))) {
						demoAuthList = Arrays.asList(step.getParameters().get(i).split("@@"));
						demoFetchList = new ArrayList<String>();
						demoFetchList.add(E2EConstants.DEMOFETCH);
					} else {
						demoAuthList = new ArrayList<String>(uinPersonaProp.stringPropertyNames());
					}
				}

				if (i == 2) {
					if (!StringUtils.isBlank(step.getParameters().get(i))) {
						individualType = step.getParameters().get(i);
					} else {
						individualType = uinPersonaProp.stringPropertyNames().iterator().next();
					}
				}

				if (i == 3) {
					if (step.getParameters().get(i).startsWith("$$")) {
						individualIdAuth = step.getParameters().get(i);
						individualIdAuth = step.getScenario().getVariables().get(individualIdAuth);
						uinList = new ArrayList<>();
						uinList.add(individualIdAuth);
					} else if (!step.getParameters().get(i).equals("0")) {
						individualIdAuth = step.getParameters().get(i);
						uinList = new ArrayList<>();
						uinList.add(individualIdAuth);// uin actual value
					} else {
						individualIdAuth = uinPersonaProp.stringPropertyNames().iterator().next();
						uinList = new ArrayList<>();
						uinList.add(individualIdAuth);
					}
				}

				if (i == 5) {
					if (step.getParameters().get(i).startsWith("$$")) {
						vids = step.getParameters().get(i);
						vids = step.getScenario().getVariables().get(vids);
						vidList = new ArrayList<>();
						vidList.add(vids);
					} else if (!step.getParameters().get(i).equals("0")) {
						vids = step.getParameters().get(i); // vid actual value
					} else {
						vids = vidPersonaProp.stringPropertyNames().iterator().next();
					}
				}
			}
			Object[] testObj = multiFactorAuth.getYmlTestData(MULTIFACTOR);

			for (String uin : uinList) {

				if (idType.contains("UIN") || idType.contains("uin")) {
					casesListUIN = multiFactorAuth.getYmlTestData(MULTIFACTOR);
				}

				if (BaseTestCase.getSupportedIdTypesValueFromActuator().contains("UIN")
						|| BaseTestCase.getSupportedIdTypesValueFromActuator().contains("uin")) {

					casesListUIN = multiFactorAuth.getYmlTestData(MULTIFACTOR);

				}

				else if (BaseTestCase.getSupportedIdTypesValueFromActuator().contains("VID")
						|| BaseTestCase.getSupportedIdTypesValueFromActuator().contains("vid")) {
					casesListVID = multiFactorAuth.getYmlTestData(MULTIFACTOR);
				}

				else {
					casesListUIN = multiFactorAuth.getYmlTestData(MULTIFACTOR);
					casesListVID = multiFactorAuth.getYmlTestData(MULTIFACTOR);
				}

				if (casesListUIN != null) {
					for (Object object : casesListUIN) {
						test = (TestCaseDTO) object;
						test = demoAuthE2eTest(demoFetchList, uin, test);
						test = bioAuthE2eTest(bioAuthList, uin, test);
						test = otpAuthE2eTest(uin, test);
						
						try {
							try {
								multiFactorAuth.test(test);
							} catch (AdminTestException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						} catch (AuthenticationTestException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}


			}

			// For VID

			for (String vid : vidList) {

				if (idType.contains("VID") || idType.contains("vid")) {
					casesListVID = multiFactorAuth.getYmlTestData(MULTIFACTOR);
				}

				if (BaseTestCase.getSupportedIdTypesValueFromActuator().contains("UIN")
						|| BaseTestCase.getSupportedIdTypesValueFromActuator().contains("uin")) {

					casesListUIN = multiFactorAuth.getYmlTestData(MULTIFACTOR);

				}

				else if (BaseTestCase.getSupportedIdTypesValueFromActuator().contains("VID")
						|| BaseTestCase.getSupportedIdTypesValueFromActuator().contains("vid")) {
					casesListVID = multiFactorAuth.getYmlTestData(MULTIFACTOR);
				}

				else {
					casesListUIN = multiFactorAuth.getYmlTestData(MULTIFACTOR);
					casesListVID = multiFactorAuth.getYmlTestData(MULTIFACTOR);
				}

				if (casesListVID != null) {
					for (Object object : casesListVID) {
						test = (TestCaseDTO) object;
						test = demoAuthE2eTest(demoFetchList, vid, test);
						test = bioAuthE2eTest(bioAuthList, vid, test);
						test = otpAuthE2eTest(vid, test);
						try {
							try {
								multiFactorAuth.test(test);
							} catch (AdminTestException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						} catch (AuthenticationTestException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}


			}

		}
	}

	private TestCaseDTO otpAuthE2eTest(String individualIdAuth, TestCaseDTO test) throws RigInternalError {
		test.setEndPoint(test.getEndPoint().replace("$PartnerKey$", partnerKeyUrl));
		test.setEndPoint(test.getEndPoint().replace("$PartnerName$", partnerId));
		test.setEndPoint(test.getEndPoint().replace("uinnumber", individualIdAuth));

		String input = test.getInput();
		input = JsonPrecondtion.parseAndReturnJsonContent(input, individualIdAuth, "individualId");
		/*
		 * input = JsonPrecondtion.parseAndReturnJsonContent(input, individualType,
		 * "individualIdType");
		 */
		/*
		 * input = JsonPrecondtion.parseAndReturnJsonContent(input, individualIdAuth,
		 * "sendOtp.individualId"); input =
		 * JsonPrecondtion.parseAndReturnJsonContent(input, individualType,
		 * "sendOtp.individualIdType");
		 */
		input = input.replace("$PartnerKey$", partnerKeyUrl);
		test.setInput(input);
		return test;

	}

	private TestCaseDTO demoAuthE2eTest(List<String> demoFetchList, String individualIdAuth, TestCaseDTO testInput)
			throws RigInternalError {

		String personFilePathvalue = null;
		if (step.getParameters().size() > 4) {
			String _personFilePath = step.getParameters().get(4);
			if (_personFilePath.startsWith("$$")) {
				_personFilePath = step.getScenario().getVariables().get(_personFilePath);
				personFilePathvalue = _personFilePath;
			}
		} else if (uinPersonaProp.containsKey(individualIdAuth))
			personFilePathvalue = uinPersonaProp.getProperty(individualIdAuth);
		else
			throw new RigInternalError("Persona doesn't exist for the given UIN " + individualIdAuth);
		demoResponse = packetUtility.retrieveBiometric(personFilePathvalue, demoFetchList);

		// testInput.setEndPoint(testInput.getEndPoint().replace("$PartnerKey$",
		// partnerKeyUrl));

		String demoFieldValueKey = null;
		String demoValue = null;
		String input = null;

		for (String demoField : demoAuthList) {
			switch (demoField) {
			case "dob":
				demoFieldValueKey = E2EConstants.DEMODOB;
				break;
			case "emailId":
				demoFieldValueKey = E2EConstants.DEMOEMAIL;
				break;
			case "phoneNumber":
				demoFieldValueKey = E2EConstants.DEMOPHONE;
				break;
			case "age":
				demoFieldValueKey = E2EConstants.DEMOAGE;
				break;
			default:
				throw new RigInternalError("Given DEMO doesn't match with the options in the script");
			}

			demoValue = JsonPrecondtion.getValueFromJson(demoResponse,
					E2EConstants.DEMOFETCH + "." + demoFieldValueKey);
			if (demoValue == null)
				throw new RigInternalError("Received null value from Persona for" + demoField);
			input = testInput.getInput();

			input = JsonPrecondtion.parseAndReturnJsonContent(input, demoField, "key");
			input = JsonPrecondtion.parseAndReturnJsonContent(input, demoValue, "value");
			// testInput=filterOutTestCase(testObj,testFilterKey);
			testInput.setInput(input);
		}
		return testInput;

	}

	private TestCaseDTO bioAuthE2eTest(List<String> bioAuthList, String uin, TestCaseDTO test) throws RigInternalError {
		String deviceInfoFilePath = null;
		for (String bioAuth : bioAuthList) {
			deviceInfoFilePath = bioAuth;
			if (!StringUtils.isBlank(deviceInfoFilePath)) {
				deviceInfoFilePath = TestRunner.getExternalResourcePath()
						+ props.getProperty("ivv.path.deviceinfo.folder") + deviceInfoFilePath + ".properties";
				deviceProp = AdminTestUtil.getproperty(deviceInfoFilePath);
			} else
				throw new RigInternalError("deviceInfo file path Parameter is  missing from DSL step");
			String personFilePathvalue = null;
			if (step.getParameters().size() > 4) {
				String _personFilePath = step.getParameters().get(4);
				if (_personFilePath.startsWith("$$")) {
					_personFilePath = step.getScenario().getVariables().get(_personFilePath);
					personFilePathvalue = _personFilePath;
				}
			} else if (uinPersonaProp.containsKey(uin))
				personFilePathvalue = uinPersonaProp.getProperty(uin);
			else
				throw new RigInternalError("Persona doesn't exist for the given UIN " + uin);

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
					throw new RigInternalError("Given BIO Type in device property file is not valid");
				}
			}

			multiFactorResponse = packetUtility.retrieveBiometric(personFilePathvalue, modalityList);
			System.out.println("saddjha");
			if (multiFactorResponse != null && !multiFactorResponse.isEmpty() && modalityKeyTogetBioValue != null) {
				String bioValue = JsonPrecondtion.getValueFromJson(multiFactorResponse, modalityKeyTogetBioValue);
				byte[] decodedBioMetricValue = Base64.getUrlDecoder().decode(bioValue);
				bioValue = Base64.getEncoder().encodeToString(decodedBioMetricValue);
				test = bioAuth(modalityToLog, bioValue, uin, deviceProp, test, this.bioAuth);
				System.out.println(test);
			}
		}
		System.out.println(test);
		return test;

	}

	private TestCaseDTO bioAuth(String modalityToLog, String bioValue, String uin, Properties deviceProps,
			TestCaseDTO test, BioAuth bioAuth2) {

		String input = test.getInput();
		input = JsonPrecondtion.parseAndReturnJsonContent(input, uin, "individualId");
		input = JsonPrecondtion.parseAndReturnJsonContent(input, deviceProps.getProperty("bioSubType"), "bioSubType");
		input = JsonPrecondtion.parseAndReturnJsonContent(input, deviceProps.getProperty("bioType"), "bioType");
		input = JsonPrecondtion.parseAndReturnJsonContent(input, deviceProps.getProperty("deviceCode"), "deviceCode");
		input = JsonPrecondtion.parseAndReturnJsonContent(input, deviceProps.getProperty("deviceProviderID"),
				"deviceProviderID");
		input = JsonPrecondtion.parseAndReturnJsonContent(input, deviceProps.getProperty("deviceServiceID"),
				"deviceServiceID");
		input = JsonPrecondtion.parseAndReturnJsonContent(input, deviceProps.getProperty("deviceServiceVersion"),
				"deviceServiceVersion");
		input = JsonPrecondtion.parseAndReturnJsonContent(input, deviceProps.getProperty("deviceProvider"),
				"deviceProvider");
		input = JsonPrecondtion.parseAndReturnJsonContent(input, deviceProps.getProperty("deviceSubType"),
				"deviceSubType");
		input = JsonPrecondtion.parseAndReturnJsonContent(input, deviceProps.getProperty("make"), "make");
		input = JsonPrecondtion.parseAndReturnJsonContent(input, deviceProps.getProperty("model"), "model");
		input = JsonPrecondtion.parseAndReturnJsonContent(input, deviceProps.getProperty("serialNo"), "serialNo");
		input = JsonPrecondtion.parseAndReturnJsonContent(input, deviceProps.getProperty("type"), "type");
		/*
		 * input = JsonPrecondtion.parseAndReturnJsonContent(input,
		 * deviceProps.getProperty("individualIdType"), "individualIdType");
		 */
		input = JsonPrecondtion.parseAndReturnJsonContent(input, bioValue, "bioValue");
		test.setInput(input);

		Reporter.log("<b><u>" + test.getTestCaseName() + "_" + modalityToLog + "</u></b>");

		return test;
	}

}
