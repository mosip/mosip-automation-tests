package io.mosip.testrig.dslrig.ivv.e2e.methods;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import io.mosip.testrig.apirig.testrunner.OTPListener;
import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testrig.dslrig.ivv.orchestrator.dslConfigManager;

public class ValidateOtp extends BaseTestCaseUtil implements StepInterface {
	static Logger logger = Logger.getLogger(ValidateOtp.class);

	static {
		if (dslConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	@Override
	public void run() throws RigInternalError {

		Boolean isForChildPacket = false;
//		Properties kernelprops=ConfigManager.propsKernel;
		String emailId = dslConfigManager.getproperty("usePreConfiguredEmail");

		if (step.getParameters().isEmpty()) {
			// emailOrPhone =step.getParameters().get(0);
			for (String resDataPath : step.getScenario().getResidentTemplatePaths().keySet()) {
				packetUtility.verifyOtp(resDataPath, step.getScenario().getCurrentStep(), emailId, step,
						OTPListener.getOtp(emailId));
			}
		} else if (!step.getParameters().isEmpty() && step.getParameters().size() == 1
				&& !step.getParameters().get(0).startsWith("$$")) { // used for child packet processing
			isForChildPacket = Boolean.parseBoolean(step.getParameters().get(0));
			if (isForChildPacket && !step.getScenario().getGeneratedResidentData().isEmpty())
				packetUtility.verifyOtp(step.getScenario().getGeneratedResidentData().get(0),
						step.getScenario().getCurrentStep(), emailId, step, OTPListener.getOtp(emailId));
		} else {
			String personaFilePath = step.getParameters().get(0); // "$$var=e2e_validateOtp($$personaFilePath)"
			if (personaFilePath.startsWith("$$")) {
				personaFilePath = step.getScenario().getVariables().get(personaFilePath);
				packetUtility.verifyOtp(personaFilePath, step.getScenario().getCurrentStep(), emailId, step,
						OTPListener.getOtp(emailId));
			}
		}
	}
}
