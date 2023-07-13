package io.mosip.ivv.e2e.methods;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.testng.Reporter;

import io.mosip.admin.fw.util.AdminTestException;
import io.mosip.admin.fw.util.TestCaseDTO;
import io.mosip.authentication.fw.util.AuthenticationTestException;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testscripts.PostWithBodyWithOtpGenerate;

public class CredentialRequest  extends BaseTestCaseUtil implements StepInterface {
	private static final String CredentialIssue_YML = "preReg/credentialIssue/credentialIssue.yml";
	Logger logger = Logger.getLogger(CredentialRequest.class);

    @SuppressWarnings("static-access")
	@Override
    public void run() throws RigInternalError {
    	String emailId ="";
		if (!step.getParameters().isEmpty() && step.getParameters().size() == 2) { // "$$requestId=e2e_credentialRequest($$uin)"
			String _uin = step.getParameters().get(0);

			if (_uin.startsWith("$$")) {
				_uin = step.getScenario().getVariables().get(_uin);
				// if (step.getScenario().getUinReqIds() == null)
				// neeha step.getScenario().getUinReqIds() = new HashMap<>();
				step.getScenario().getUinReqIds().clear();
				step.getScenario().getUinReqIds().put(_uin, "uin");
			}

		}

		if (step.getParameters().size() == 2 && step.getParameters().get(1).startsWith("$$")) {
			emailId = step.getParameters().get(1);
			if (emailId.startsWith("$$")) { // "$$requestId=e2e_credentialRequest($$uin,$$email)"
				emailId = step.getScenario().getVariables().get(emailId);
			}
		}
    	try {
			Thread.sleep(30000);
		} catch (InterruptedException e) {
			logger.error(e.getMessage());
			Thread.currentThread().interrupt();
		}
    	String fileName = CredentialIssue_YML;
    	PostWithBodyWithOtpGenerate postWithOtp= new PostWithBodyWithOtpGenerate();
    	Object[] casesList = postWithOtp.getYmlTestData(fileName);
		Object[] testCaseList = filterTestCases(casesList);
		logger.info("No. of TestCases in Yml file : " + testCaseList.length);
		try {
			for (Object object : testCaseList) {
				for(String uin: this.step.getScenario().getUinReqIds().keySet()) {
					
				TestCaseDTO test = (TestCaseDTO) object;
				test.setInput(test.getInput().replace("$UIN$", uin).replace("$UIN$", uin));
				test.setInput(test.getInput().replace("$OTP$", emailId).replace("$OTP$", emailId));
				test.setOutput(test.getOutput().replace("$UIN$", uin));
				Reporter.log("<b><u>"+test.getTestCaseName()+ "</u></b>");
				
				long startTime = System.currentTimeMillis();
				logger.info(this.getClass().getSimpleName()+" starts at..."+startTime +" MilliSec");
				postWithOtp.test(test);
				long stopTime = System.currentTimeMillis();
				long elapsedTime = stopTime - startTime;
				logger.info("Time taken to execute "+ this.getClass().getSimpleName()+": " +elapsedTime +" MilliSec");
				Reporter.log("<b><u>"+"Time taken to execute "+ this.getClass().getSimpleName()+": " +elapsedTime +" MilliSec"+ "</u></b>");
				JSONObject response = new JSONObject(postWithOtp.response.asString());
				if(!response.get("response").toString().equals("null"))
	    		{
					JSONObject responseJson = new JSONObject(response.get("response").toString());
						this.step.getScenario().getUinReqIds().put(uin, responseJson.get("requestId").toString());
						if(step.getOutVarName()!=null)
							 step.getScenario().getVariables().put(step.getOutVarName(), responseJson.get("requestId").toString());
					}
				}
			}
		} catch (AuthenticationTestException | AdminTestException e) {
			logger.error(e.getMessage());
			this.hasError=true;
			//assertFalse(true, "Failed at credential issuance Response validation");
			throw new RigInternalError("Failed at credential issuance Response validation");
		}
	}

}
