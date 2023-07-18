package io.mosip.testrig.dslrig.ivv.e2e.methods;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import io.mosip.testrig.apirig.admin.fw.util.AdminTestUtil;
import io.mosip.testrig.apirig.admin.fw.util.TestCaseDTO;
import io.mosip.testrig.apirig.authentication.fw.precon.JsonPrecondtion;
import io.mosip.testrig.apirig.service.BaseTestCase;
import io.mosip.testrig.apirig.testscripts.BioAuth;
import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.e2e.constant.E2EConstants;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testrig.dslrig.ivv.orchestrator.TestRunner;

public class BioAuthentication extends BaseTestCaseUtil implements StepInterface {
	static Logger logger = Logger.getLogger(BioAuthentication.class);
	private static final String BIOMETRIC_FACE = "idaData/BioAuth/BioAuth.yml";
	Properties deviceProp = null;
	Properties uinResidentDataPathFinalProps = new Properties();
	BioAuth bioAuth = new BioAuth();
	String bioResponse = null;
	List<String> idType = BaseTestCase.getSupportedIdTypesValueFromActuator();
	List<Object> casesListUIN = null;
	List<Object> casesListVID = null;

	@Override
	public void run() throws RigInternalError {
		// AuthPartnerProcessor.startProcess();
		// step.getScenario().getUinPersonaProp().put("9683481379",
		// "C:\\Users\\Sohan.Dey\\Downloads\\residents_10857486596570242644\\7660996440.json");

		String deviceInfoFilePath = null;
		String uins = null;
		String vids = null;
		List<String> uinList = null;
		List<String> vidList = null;
		if (step.getParameters() == null || step.getParameters().isEmpty() || step.getParameters().size() < 1) {
			logger.error("Parameter is  missing from DSL step");
this.hasError=true;
			throw new RigInternalError("Modality paramter is  missing in step: " + step.getName());
		} else {
			deviceInfoFilePath = step.getParameters().get(0);
			if (!StringUtils.isBlank(deviceInfoFilePath)) {
				deviceInfoFilePath = TestRunner.getExternalResourcePath()
						+ props.getProperty("ivv.path.deviceinfo.folder") + deviceInfoFilePath + ".properties";
				deviceProp = AdminTestUtil.getproperty(deviceInfoFilePath);
			} else
				{this.hasError=true;throw new RigInternalError("deviceInfo file path Parameter is  missing from DSL step");
		}}
		if (step.getParameters().size() == 2) {
			uins = step.getParameters().get(1);
			if (!StringUtils.isBlank(uins))
				uinList = new ArrayList<>(Arrays.asList(uins.split("@@")));
		} else if (step.getParameters().size() == 4) { // e2e_bioAuthentication(faceDevice,$$uin,$$personaFilePath)
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
		} else if (step.getParameters().size() == 4) { // e2e_bioAuthentication(faceDevice,$$uin,$$personaFilePath)
			vids = step.getParameters().get(2);
			String _personaFilePath = step.getParameters().get(3);
			if (vids.startsWith("$$") && _personaFilePath.startsWith("$$")) {
				vids = step.getScenario().getVariables().get(vids);
				_personaFilePath = step.getScenario().getVariables().get(_personaFilePath);
				vidList = new ArrayList<>();
				vidList.add(vids);
				step.getScenario().getVidPersonaProp().put(vids, _personaFilePath);
			}
		} else
			vidList = new ArrayList<>(step.getScenario().getVidPersonaProp().stringPropertyNames());

		for (String uin : uinList) {
			String personFilePathvalue = null;
			if (step.getScenario().getUinPersonaProp().containsKey(uin))
				personFilePathvalue = step.getScenario().getUinPersonaProp().getProperty(uin);
			else
				{
				this.hasError=true;throw new RigInternalError("Persona doesn't exist for the given UIN " + uin);
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
					 this.hasError=true;
					throw new RigInternalError("Given BIO Type in device property file is not valid");
				}
			}

			bioResponse = packetUtility.retrieveBiometric(personFilePathvalue, modalityList,step);

			//System.out.println("bioMetricValue= " + bioResponse);

			String fileName = BIOMETRIC_FACE;
			bioAuth.isInternal = false;
			Object[] casesListUIN = null; // bioAuth.getYmlTestData(fileName);
			Object[] casesListVID = null; // bioAuth.getYmlTestData(fileName);
			// Get the id types from server
//			if idtypes.contains "UIN" { casesListUIN = bioAuth.getYmlTestData(fileName);
//			if idtypes.contains "VID" { casesListVID = bioAuth.getYmlTestData(fileName);

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
				String bioValue = JsonPrecondtion.getValueFromJson(bioResponse, modalityKeyTogetBioValue);

				byte[] decodedBioMetricValue = Base64.getUrlDecoder().decode(bioValue);
				bioValue = Base64.getEncoder().encodeToString(decodedBioMetricValue);

				if (bioValue == null || bioValue.length() < 100) {
					this.hasError=true;	throw new RigInternalError(
							"Not able to get the bio value for field " + modalityToLog + " from persona");
				}
				if (idType.contains("UIN") || idType.contains("uin")) {
					casesListUIN = bioAuth.getYmlTestData(fileName);
				}

				if (casesListUIN != null) {
					for (Object object : casesListUIN) {
						TestCaseDTO test = (TestCaseDTO) object;
						packetUtility.bioAuth(modalityToLog, bioValue, uin, deviceProp, test, bioAuth,step);
					}
				}

			}
		}

		for (String vid : vidList) {
			String personFilePathvalue = null;
			if (step.getScenario().getVidPersonaProp().containsKey(vid))
				personFilePathvalue = step.getScenario().getVidPersonaProp().getProperty(vid);
			else
				{this.hasError=true;throw new RigInternalError("Persona doesn't exist for the given VID " + vid);
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
					this.hasError=true;throw new RigInternalError("Given BIO Type in device property file is not valid");
				}
			}

			bioResponse = packetUtility.retrieveBiometric(personFilePathvalue, modalityList,step);

			//System.out.println("bioMetricValue= " + bioResponse);

			String fileName = BIOMETRIC_FACE;
			bioAuth.isInternal = false;
			Object[] casesListUIN = null; // bioAuth.getYmlTestData(fileName);
			Object[] casesListVID = null; // bioAuth.getYmlTestData(fileName);
			// Get the id types from server
//		if idtypes.contains "UIN" { casesListUIN = bioAuth.getYmlTestData(fileName);
//		if idtypes.contains "VID" { casesListVID = bioAuth.getYmlTestData(fileName);

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
				String bioValue = JsonPrecondtion.getValueFromJson(bioResponse, modalityKeyTogetBioValue);

				byte[] decodedBioMetricValue = Base64.getUrlDecoder().decode(bioValue);
				bioValue = Base64.getEncoder().encodeToString(decodedBioMetricValue);

				if (bioValue == null || bioValue.length() < 100)
					{this.hasError=true;throw new RigInternalError(
							"Not able to get the bio value for field " + modalityToLog + " from persona");
					}
				if (idType.contains("VID") || idType.contains("vid")) {
					casesListVID = bioAuth.getYmlTestData(fileName);
				}

				if (casesListVID != null) {
					for (Object object : casesListVID) {
						TestCaseDTO test = (TestCaseDTO) object;
						packetUtility.bioAuth(modalityToLog, bioValue, vid, deviceProp, test, bioAuth,step);
					}
				}

			}
		}
	}

}
