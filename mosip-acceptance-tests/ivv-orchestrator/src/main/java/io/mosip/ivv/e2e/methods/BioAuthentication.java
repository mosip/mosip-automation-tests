package io.mosip.ivv.e2e.methods;

import static io.restassured.RestAssured.given;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
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
import io.mosip.testscripts.BioAuth;
import io.restassured.response.Response;

public class BioAuthentication extends BaseTestCaseUtil implements StepInterface {
	static Logger logger = Logger.getLogger(BioAuthentication.class);
	private static final String BIOMETRIC_FACE = "ida/BioAuth/BioAuth.yml";
	Properties deviceProp =null;
	Properties uinResidentDataPathFinalProps = new Properties();
	BioAuth bioAuth = new BioAuth();
	String bioResponse = null;

	@Override
	public void run() throws RigInternalError {
		// AuthPartnerProcessor.startProcess();
		//uinPersonaProp.put("5174650156", "C:\\Users\\username\\AppData\\Local\\Temp\\residents_17396353760056333062\\913439134391343.json");

		String deviceInfoFilePath = null;
		String uins = null;
		List<String> uinList = null;
		if (step.getParameters() == null || step.getParameters().isEmpty() || step.getParameters().size() < 1) {
			logger.error("Parameter is  missing from DSL step");
			throw new RigInternalError("Modality paramter is  missing in step: " + step.getName());
		} else {
			deviceInfoFilePath = step.getParameters().get(0);
			if (!StringUtils.isBlank(deviceInfoFilePath)) {
				deviceInfoFilePath = TestRunner.getExeternalResourcePath()
						+ properties.getProperty("ivv.path.deviceinfo.folder") + deviceInfoFilePath + ".properties";
				deviceProp = AdminTestUtil.getproperty(deviceInfoFilePath);
			} else
				throw new RigInternalError("deviceInfo file path Parameter is  missing from DSL step");
		}
		if (step.getParameters().size() == 2) {
			uins = step.getParameters().get(1);
			if (!StringUtils.isBlank(uins))
				uinList = new ArrayList<>(Arrays.asList(uins.split("@@")));
		} else
			uinList = new ArrayList<>(uinPersonaProp.stringPropertyNames());

		for (String uin : uinList) {
			String personFilePathvalue = null;
			if (uinPersonaProp.containsKey(uin))
				personFilePathvalue = uinPersonaProp.getProperty(uin);
			else
				throw new RigInternalError("Persona doesn't exist for the given UIN " + uin);
			
			String bioType=null, bioSubType=null;
			List<String> modalityList = new ArrayList<>();
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
					modalityList.add(E2EConstants.FACEFETCH);
					modalityToLog = bioSubType+"_"+bioType;
					modalityKeyTogetBioValue = (bioSubType.equalsIgnoreCase("left"))? E2EConstants.LEFT_EYE:E2EConstants.RIGHT_EYE;
					break;
				case E2EConstants.FINGERBIOTYPE:
					modalityList.add(E2EConstants.FACEFETCH);
					modalityToLog = bioSubType;
					modalityKeyTogetBioValue = bioSubType;
					break;
				default:
					throw new RigInternalError("Given BIO Type in device property file is not valid");
				}
			}
			
			bioResponse = packetUtility.retrieveBiometric(personFilePathvalue, modalityList);

			String fileName = BIOMETRIC_FACE;
			bioAuth.isInternal = false;
			Object[] casesList = bioAuth.getYmlTestData(fileName);

			if (bioResponse != null && !bioResponse.isEmpty() && modalityKeyTogetBioValue!= null) {
					String bioValue = JsonPrecondtion.getValueFromJson(bioResponse, modalityKeyTogetBioValue);
					for (Object object : casesList) {
						TestCaseDTO test = (TestCaseDTO) object;
						packetUtility.bioAuth(modalityToLog, bioValue, uin, deviceProp, test, bioAuth);
				}
			}
		}
	}

	

}
