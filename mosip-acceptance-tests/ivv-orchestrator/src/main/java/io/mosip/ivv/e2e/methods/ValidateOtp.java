package io.mosip.ivv.e2e.methods;

import org.apache.log4j.Logger;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;

public class ValidateOtp extends BaseTestCaseUtil implements StepInterface {
	Logger logger = Logger.getLogger(ValidateOtp.class);
	
	@Override
	public void run() throws RigInternalError {
		
		Boolean isForChildPacket = false;
		String emailOrPhone = "test.automation@gmail.com";
		if (step.getParameters().isEmpty()) {
			// emailOrPhone =step.getParameters().get(0);
			for (String resDataPath : step.getScenario().getResidentTemplatePaths().keySet()) {
				packetUtility.verifyOtp(resDataPath,step.getScenario().getCurrentStep(),emailOrPhone,step);
			}
		} else if (!step.getParameters().isEmpty() && step.getParameters().size() == 1
				&& !step.getParameters().get(0).startsWith("$$")) { // used for child packet processing
			isForChildPacket = Boolean.parseBoolean(step.getParameters().get(0));
			if (isForChildPacket && !step.getScenario().getGeneratedResidentData().isEmpty())
				packetUtility.verifyOtp(step.getScenario().getGeneratedResidentData().get(0), step.getScenario().getCurrentStep(), emailOrPhone,step);
		}else {
			String personaFilePath = step.getParameters().get(0);    //"$$var=e2e_validateOtp($$personaFilePath)"
			if (personaFilePath.startsWith("$$")) {
				personaFilePath = step.getScenario().getVariables().get(personaFilePath);
				packetUtility.verifyOtp(personaFilePath, step.getScenario().getCurrentStep(), emailOrPhone,step);
			}
		}
	}
}
