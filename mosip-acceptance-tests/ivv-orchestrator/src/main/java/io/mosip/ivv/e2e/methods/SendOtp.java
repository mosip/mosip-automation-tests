package io.mosip.ivv.e2e.methods;

import org.testng.Reporter;

import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;

public class SendOtp  extends BaseTestCaseUtil implements StepInterface {

	@Override
	public void run() throws RigInternalError {
		for (String resDataPath : residentTemplatePaths.keySet()) {
			//Reporter.log("<b><u>"+"PreRegister and upload packet testCase: "+count+ "</u></b>");
			packetUtility.requestOtp(resDataPath,contextKey);
		}
	}

}
