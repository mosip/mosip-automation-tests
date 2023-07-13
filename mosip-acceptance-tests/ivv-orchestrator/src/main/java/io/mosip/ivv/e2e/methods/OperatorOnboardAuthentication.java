package io.mosip.ivv.e2e.methods;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import io.mosip.admin.fw.util.AdminTestUtil;
import io.mosip.admin.fw.util.TestCaseDTO;
import io.mosip.authentication.fw.precon.JsonPrecondtion;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.e2e.constant.E2EConstants;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.ivv.orchestrator.TestRunner;
import io.mosip.testscripts.BioAuth;

public class OperatorOnboardAuthentication extends BaseTestCaseUtil implements StepInterface {
	static Logger logger = Logger.getLogger(OperatorOnboardAuthentication.class);
	private static final String BIOMETRIC_FACE = "regproc/OperatorOnboardAuth/operatorOnboardAuth.yml";
	Properties deviceProp =null;
	Properties uinResidentDataPathFinalProps = new Properties();
	BioAuth bioAuth = new BioAuth();
	String bioResponse = null;

	@Override
	public void run() throws RigInternalError {
		//AuthPartnerProcessor.startProcess();
		//step.getScenario().getUinPersonaProp().put("8497438207", "C:\\Users\\NEEHAR~1.GAR\\AppData\\Local\\Temp\\residents_11835704006002908658\\5645168410.json");
		List<String> modalityList = new ArrayList<>();
		String personFilePathvalue = null;
		String deviceInfoFilePath = null;
		String uins = null;
		List<String> uinList = null;
		if (step.getParameters() == null || step.getParameters().isEmpty() || step.getParameters().size() < 1) {
			logger.error("Parameter is  missing from DSL step");
			this.hasError=true;throw new RigInternalError("Modality paramter is  missing in step: " + step.getName());
		} else {
			deviceInfoFilePath = step.getParameters().get(0);
			if (!StringUtils.isBlank(deviceInfoFilePath)) {
				deviceInfoFilePath = TestRunner.getExternalResourcePath()
						+ props.getProperty("ivv.path.deviceinfo.folder") + deviceInfoFilePath + ".properties";
				deviceProp = AdminTestUtil.getproperty(deviceInfoFilePath);
			} else
				{this.hasError=true;throw new RigInternalError("deviceInfo file path Parameter is  missing from DSL step");}
		}
		if (step.getParameters().size() == 2) {
			uins = step.getParameters().get(1);
			if (!StringUtils.isBlank(uins))
				uinList = new ArrayList<>(Arrays.asList(uins.split("@@")));
		} else if (step.getParameters().size() == 3) {    //  e2e_bioAuthentication(faceDevice,$$uin,$$personaFilePath)
			uins = step.getParameters().get(1);
			String _personaFilePath = step.getParameters().get(2);
			if (uins.startsWith("$$") && _personaFilePath.startsWith("$$")) {
				uins = step.getScenario().getVariables().get(uins);
				_personaFilePath = step.getScenario().getVariables().get(_personaFilePath);
				uinList = new ArrayList<>();
				uinList.add(uins);
				step.getScenario().getUinPersonaProp().put(uins, _personaFilePath);
			}
		}
		else
			uinList = new ArrayList<>(step.getScenario().getUinPersonaProp().stringPropertyNames());

		for (String uin : uinList) {



			if (step.getScenario().getUinPersonaProp().containsKey(uin))
				personFilePathvalue = step.getScenario().getUinPersonaProp().getProperty(uin);
			else
			{this.hasError=true;	throw new RigInternalError("Persona doesn't exist for the given UIN " + uin);
			}
			String bioType=null, bioSubType=null;

			String modalityToLog = null;
			String modalityKeyTogetBioValue = null;
			if(deviceProp != null) {
				bioType= deviceProp.getProperty("bioType");

				bioSubType=	deviceProp.getProperty("bioSubType");
				switch(bioType) {
				case E2EConstants.FACEBIOTYPE:
					modalityList.add(E2EConstants.FACEFETCH);
					modalityToLog = bioType;
					modalityKeyTogetBioValue = E2EConstants.FACEFETCH;
					break;
				case E2EConstants.IRISBIOTYPE:
					modalityList.add(E2EConstants.IRISFETCH);
					modalityToLog = bioSubType+"_"+bioType;
					modalityKeyTogetBioValue = (bioSubType.equalsIgnoreCase("left"))? E2EConstants.LEFT_EYE:E2EConstants.RIGHT_EYE;
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

			/*

		deviceInfoFilePath = TestRunner.getExternalResourcePath()
				+ props.getProperty("ivv.path.deviceinfo.folder") + "faceDevice" + ".properties";
		deviceProp = AdminTestUtil.getproperty(deviceInfoFilePath);
		String uin="8497438207";
		personFilePathvalue = step.getScenario().getUinPersonaProp().getProperty(uin);
		String modalityToLog = null;
		String modalityKeyTogetBioValue = null;
		modalityList.add(E2EConstants.FACEFETCH);

		modalityToLog = "FACE";
		modalityKeyTogetBioValue = "face_encrypted";
			 */
			bioResponse = packetUtility.retrieveBiometric(personFilePathvalue, modalityList,step);

			String fileName = BIOMETRIC_FACE;
			bioAuth.isInternal = false;
			Object[] casesList = bioAuth.getYmlTestData(fileName);

			if (bioResponse != null && !bioResponse.isEmpty() && modalityKeyTogetBioValue!= null) {
				String bioValue = JsonPrecondtion.getValueFromJson(bioResponse, modalityKeyTogetBioValue);
				if(bioValue== null || bioValue.length()<100)
					{
					this.hasError=true;throw new RigInternalError("Not able to get the bio value for field "+modalityToLog+" from persona");
					}
				for (Object object : casesList) {
					TestCaseDTO test = (TestCaseDTO) object;
					packetUtility.operatorOnboardAuth(modalityToLog, bioValue, "dsl1", test, bioAuth,"USERID",deviceProp,step);
				}
			}
		}
	}
}
