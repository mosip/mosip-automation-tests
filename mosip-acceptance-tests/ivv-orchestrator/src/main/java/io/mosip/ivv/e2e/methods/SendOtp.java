package io.mosip.ivv.e2e.methods;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.kernel.util.ConfigManager;

public class SendOtp extends BaseTestCaseUtil implements StepInterface {
	Logger logger = Logger.getLogger(SendOtp.class);

	@Override
	public void run() throws RigInternalError {
		Boolean isForChildPacket = false;
		Properties kernelprops=ConfigManager.propsKernel;
		String emailId=kernelprops.getProperty("usePreConfiguredEmail");

		if (step.getParameters().isEmpty()) {
			// emailOrPhone =step.getParameters().get(0);
			for (String resDataPath : step.getScenario().getResidentTemplatePaths().keySet()) {
				packetUtility.requestOtp(resDataPath, step.getScenario().getCurrentStep(), emailId, step);
			}
		} else if (!step.getParameters().isEmpty() && step.getParameters().size() == 1
				&& !step.getParameters().get(0).startsWith("$$")) { // used for child packet processing
			isForChildPacket = Boolean.parseBoolean(step.getParameters().get(0));
			if (isForChildPacket && !step.getScenario().getGeneratedResidentData().isEmpty())
				packetUtility.requestOtp(step.getScenario().getGeneratedResidentData().get(0),
						step.getScenario().getCurrentStep(), emailId, step);
		} else {
			String personaFilePath = step.getParameters().get(0); // "$$var=e2e_sendOtp($$personaFilePath)"
			if (personaFilePath.startsWith("$$")) {
				personaFilePath = step.getScenario().getVariables().get(personaFilePath);
				packetUtility.requestOtp(personaFilePath, step.getScenario().getCurrentStep(), emailId, step);
			}
		}
	}

}
