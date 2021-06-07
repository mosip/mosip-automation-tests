package io.mosip.ivv.e2e.methods;

import org.testng.Reporter;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;

public class PreRegister extends BaseTestCaseUtil implements StepInterface {

	@Override
	public void run() throws RigInternalError {
		Boolean isForChildPacket = false;
		if (!step.getParameters().isEmpty() && step.getParameters().size() == 1) { // used for child packet processing
			isForChildPacket = Boolean.parseBoolean(step.getParameters().get(0));
			if (isForChildPacket && !generatedResidentData.isEmpty())
				prid_updateResident = packetUtility.preReg(generatedResidentData.get(0), contextInuse);
		} else {
			int count = 1;
			for (String resDataPath : residentTemplatePaths.keySet()) {
				Reporter.log("<b><u>" + "PreRegister testCase: " + count + "</u></b>");
				count++;
				String prid = packetUtility.preReg(resDataPath, contextInuse);
				residentPathsPrid.put(resDataPath, prid);
			}
		}
	}

}
