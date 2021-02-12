package io.mosip.ivv.e2e.methods;

import org.testng.Reporter;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;

public class PreRegister extends BaseTestCaseUtil implements StepInterface {

	@Override
	public void run() throws RigInternalError {
		int count=1;
		for (String resDataPath : residentTemplatePaths.keySet()) {
			Reporter.log("<b><u>"+"PreRegister and upload packet testCase: "+count+ "</u></b>");
			count++;
			packetUtility.requestOtp(resDataPath,contextKey);
			packetUtility.verifyOtp(resDataPath,contextKey);
			String prid=packetUtility.preReg(resDataPath,contextKey);
			residentPathsPrid.put(resDataPath, prid);
		}
		
	}
	

}
