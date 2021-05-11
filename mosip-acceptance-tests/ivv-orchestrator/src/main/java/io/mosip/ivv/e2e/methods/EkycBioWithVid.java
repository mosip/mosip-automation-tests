package io.mosip.ivv.e2e.methods;

import io.mosip.admin.fw.util.AdminTestUtil;
import io.mosip.admin.fw.util.TestCaseDTO;
import io.mosip.authentication.fw.precon.JsonPrecondtion;
import io.mosip.authentication.fw.util.AuthPartnerProcessor;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.e2e.constant.E2EConstants;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.ivv.orchestrator.TestRunner;
import io.mosip.testscripts.BioAuth;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class EkycBioWithVid extends BaseTestCaseUtil implements StepInterface {
	static Logger logger = Logger.getLogger(EkycBioWithVid.class);
	private static final String BIOMETRIC_FACE = "idaData/EkycBio/EkycBio.yml";
	Properties deviceProp =null;
	Properties uinResidentDataPathFinalProps = new Properties();
	BioAuth bioAuth = new BioAuth();
	String bioResponse = null;

	@Override
	public void run() throws RigInternalError {
		 AuthPartnerProcessor.startProcess();
		//uinPersonaProp.put("7209149850", "C:\\Users\\username\\AppData\\Local\\Temp\\residents_629388943910840643\\604866048660486.json");

		 String deviceInfoFilePath = null;
		String vids = null;
		List<String> vidList = null;
		if (step.getParameters() == null || step.getParameters().isEmpty() || step.getParameters().size() < 1) {
			logger.error("Parameter is  missing from DSL step");
			throw new RigInternalError("Modality paramter is  missing in step: " + step.getName());
		} else {
			deviceInfoFilePath = step.getParameters().get(0);
			if (!StringUtils.isBlank(deviceInfoFilePath)) {
				deviceInfoFilePath = TestRunner.getExeternalResourcePath()
						+ props.getProperty("ivv.path.deviceinfo.folder") + deviceInfoFilePath + ".properties";
				deviceProp = AdminTestUtil.getproperty(deviceInfoFilePath);
			} else
				throw new RigInternalError("deviceInfo file path Parameter is  missing from DSL step");
		}
		if (step.getParameters().size() == 2) {
			vids = step.getParameters().get(1);
			if (!StringUtils.isBlank(vids))
				vidList = new ArrayList<>(Arrays.asList(vids.split("@@")));
		} else
			vidList = new ArrayList<>(vidPersonaProp.stringPropertyNames());

		for (String vid : vidList) {
			String personFilePathvalue = null;
			if (vidPersonaProp.containsKey(vid))
				personFilePathvalue = vidPersonaProp.getProperty(vid);
			else
				throw new RigInternalError("Persona doesn't exist for the given VID " + vid);
			
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
					throw new RigInternalError("Given BIO Type in device property file is not valid");
				}
			}
			deviceProp.setProperty("individualIdType", "VID");
			bioResponse = packetUtility.retrieveBiometric(personFilePathvalue, modalityList);

			String fileName = BIOMETRIC_FACE;
			bioAuth.isInternal = false;
			Object[] casesList = bioAuth.getYmlTestData(fileName);

			if (bioResponse != null && !bioResponse.isEmpty() && modalityKeyTogetBioValue!= null) {
					String bioValue = JsonPrecondtion.getValueFromJson(bioResponse, modalityKeyTogetBioValue);
					if(bioValue== null || bioValue.length()<100)
						throw new RigInternalError("Not able to get the bio value for field "+modalityToLog+" from persona");
					for (Object object : casesList) {
						TestCaseDTO test = (TestCaseDTO) object;
						packetUtility.bioAuth(modalityToLog, bioValue, vid, deviceProp, test, bioAuth);
				}
			}
		}
	}


}
