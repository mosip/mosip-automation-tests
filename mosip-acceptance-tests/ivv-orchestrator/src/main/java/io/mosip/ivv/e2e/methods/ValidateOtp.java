package io.mosip.ivv.e2e.methods;

import static org.testng.Assert.assertTrue;
import org.apache.log4j.Logger;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;

public class ValidateOtp extends BaseTestCaseUtil implements StepInterface {
	Logger logger = Logger.getLogger(ValidateOtp.class);
	
	@Override
	public void run() throws RigInternalError {
		
		String emailOrPhone ="test.automation@gmail.com" ;
		if (step.getParameters() == null || step.getParameters().isEmpty()) {
			//emailOrPhone=" ";
			//logger.error("Parameter is  missing from DSL step");
			//assertTrue(false,"Paramter is  missing in step: "+step.getName());
		} else {
			emailOrPhone =step.getParameters().get(0);
		}
		for (String resDataPath : residentTemplatePaths.keySet()) {
			//Reporter.log("<b><u>"+"PreRegister and upload packet testCase: "+count+ "</u></b>");
			packetUtility.verifyOtp(resDataPath,contextKey,emailOrPhone);
		}
	}
}
