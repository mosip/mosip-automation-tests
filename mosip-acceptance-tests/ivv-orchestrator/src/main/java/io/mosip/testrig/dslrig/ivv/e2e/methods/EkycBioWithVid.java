package io.mosip.testrig.dslrig.ivv.e2e.methods;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import io.mosip.testrig.apirig.dto.TestCaseDTO;
import io.mosip.testrig.apirig.testrunner.JsonPrecondtion;
import io.mosip.testrig.apirig.utils.AdminTestUtil;
import io.mosip.testrig.apirig.testscripts.BioAuth;
import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.e2e.constant.E2EConstants;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testrig.dslrig.ivv.orchestrator.TestRunner;
import io.mosip.testrig.dslrig.ivv.orchestrator.dslConfigManager;

public class EkycBioWithVid extends BaseTestCaseUtil implements StepInterface {
	static Logger logger = Logger.getLogger(EkycBioWithVid.class);
	private static final String BIOMETRIC_FACE = "idaData/EkycBio/EkycBio.yml";
	Properties deviceProp = null;
	BioAuth bioAuth = new BioAuth();
	String bioResponse = null;

	static {
		if (dslConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	@Override
	public void run() throws RigInternalError {
		String _personFilePath = null;
		String deviceInfoFilePath = null;
		String vids = null;
		List<String> vidList = null;
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
			vids = step.getParameters().get(1);
			if (!StringUtils.isBlank(vids))
				vidList = new ArrayList<>(Arrays.asList(vids.split("@@")));
		} else if (step.getParameters().size() > 2) { // "e2e_EkycBio(faceDevice,$$vid,$$personaFilePath)"
			vids = step.getParameters().get(1);
			_personFilePath = step.getParameters().get(2);
			if (vids.startsWith("$$") && _personFilePath.startsWith("$$")) {
				vids = step.getScenario().getVariables().get(vids);
				_personFilePath = step.getScenario().getVariables().get(_personFilePath);
				vidList = new ArrayList<>(Arrays.asList(vids.split("@@")));
			}
		} else
			vidList = new ArrayList<>(step.getScenario().getVidPersonaProp().stringPropertyNames());

		for (String vid : vidList) {
			String personFilePathvalue = null;
			if (step.getParameters().size() > 2) {
				personFilePathvalue = _personFilePath;
			} else if (step.getScenario().getVidPersonaProp().containsKey(vid)) {
				String uin = step.getScenario().getVidPersonaProp().get(vid).toString();
				personFilePathvalue = step.getScenario().getUinPersonaProp().get(uin).toString();
			} else {
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
			deviceProp.setProperty("individualIdType", "VID");
			bioResponse = packetUtility.retrieveBiometric(personFilePathvalue, modalityList, step);

			String fileName = BIOMETRIC_FACE;
			bioAuth.isInternal = false;
			Object[] casesList = bioAuth.getYmlTestData(fileName);

			if (bioResponse != null && !bioResponse.isEmpty() && modalityKeyTogetBioValue != null) {
				String bioValue = JsonPrecondtion.getValueFromJson(bioResponse, modalityKeyTogetBioValue);

				byte[] decodedBioMetricValue = Base64.getUrlDecoder().decode(bioValue);
				bioValue = Base64.getEncoder().encodeToString(decodedBioMetricValue);

				if (bioValue == null || bioValue.length() < 100)
					throw new RigInternalError(
							"Not able to get the bio value for field " + modalityToLog + " from persona");
				for (Object object : casesList) {
					TestCaseDTO test = (TestCaseDTO) object;
					packetUtility.bioAuth(modalityToLog, bioValue, vid, deviceProp, test, bioAuth, step);
				}
			}
		}
	}
}
