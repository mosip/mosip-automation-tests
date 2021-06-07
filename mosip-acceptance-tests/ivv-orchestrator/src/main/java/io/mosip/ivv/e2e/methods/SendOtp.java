package io.mosip.ivv.e2e.methods;

import org.apache.log4j.Logger;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;

public class SendOtp extends BaseTestCaseUtil implements StepInterface {
	Logger logger = Logger.getLogger(SendOtp.class);

	@Override
	public void run() throws RigInternalError {
		Boolean isForChildPacket = false;
		String emailOrPhone = "test.automation@gmail.com";
		if (step.getParameters().isEmpty()) {
			// emailOrPhone =step.getParameters().get(0);
			for (String resDataPath : residentTemplatePaths.keySet()) {
				packetUtility.requestOtp(resDataPath, contextInuse, emailOrPhone);
			}
		} else if (!step.getParameters().isEmpty() && step.getParameters().size() == 1) { // used for child packet processing
			isForChildPacket = Boolean.parseBoolean(step.getParameters().get(0));
			if (isForChildPacket && !generatedResidentData.isEmpty())
				packetUtility.requestOtp(generatedResidentData.get(0), contextInuse, emailOrPhone);
		}
	}

}
