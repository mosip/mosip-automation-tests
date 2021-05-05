package io.mosip.ivv.e2e.methods;

import org.apache.log4j.Logger;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;

public class SendOtp  extends BaseTestCaseUtil implements StepInterface {
	Logger logger = Logger.getLogger(SendOtp.class);
	
	@Override
	public void run() throws RigInternalError {
		String emailOrPhone ="test.automation@gmail.com";
		if (step.getParameters() == null || step.getParameters().isEmpty()) {
			//logger.info("Parameter is  missing from DSL step");
		} else {
			emailOrPhone =step.getParameters().get(0);
		}
		for (String resDataPath : residentTemplatePaths.keySet()) {
			//Reporter.log("<b><u>"+"PreRegister and upload packet testCase: "+count+ "</u></b>");
		//String resDataPath="C:\\Users\\ALOK~1.KUM\\AppData\\Local\\Temp\\residents_6736584730385608513\\468584685846858.json";
			packetUtility.requestOtp(resDataPath,contextInuse,emailOrPhone);
		}
	}

}
