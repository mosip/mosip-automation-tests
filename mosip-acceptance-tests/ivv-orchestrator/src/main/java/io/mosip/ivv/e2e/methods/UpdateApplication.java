package io.mosip.ivv.e2e.methods;

import org.apache.log4j.Logger;
import org.testng.Reporter;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;

public class UpdateApplication extends BaseTestCaseUtil implements StepInterface {
	Logger logger = Logger.getLogger(UpdateApplication.class);

	@Override
	public void run() throws RigInternalError {
		for (String resDataPath : residentTemplatePaths.keySet()) {
			Reporter.log("<b><u>" + "UpdateApplication testCase </u></b>");
			//packetUtility.updateApplication(resDataPath, residentPathsPrid, contextKey);
		}
	}
}
