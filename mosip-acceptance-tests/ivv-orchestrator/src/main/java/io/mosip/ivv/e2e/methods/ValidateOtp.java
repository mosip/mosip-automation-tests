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
			for (String resDataPath : residentTemplatePaths.keySet()) {
				packetUtility.verifyOtp(resDataPath,contextInuse,emailOrPhone);
			}
		} else if (!step.getParameters().isEmpty() && step.getParameters().size() == 1
				&& !step.getParameters().get(0).startsWith("$$")) { // used for child packet processing
			isForChildPacket = Boolean.parseBoolean(step.getParameters().get(0));
			if (isForChildPacket && !generatedResidentData.isEmpty())
				packetUtility.verifyOtp(generatedResidentData.get(0), contextInuse, emailOrPhone);
		}else {
			String personaFilePath = step.getParameters().get(0);    //"$$var=e2e_validateOtp($$personaFilePath)"
			if (personaFilePath.startsWith("$$")) {
				personaFilePath = step.getScenario().getVariables().get(personaFilePath);
				packetUtility.verifyOtp(personaFilePath, contextInuse, emailOrPhone);
			}
		}
	}
}
