package io.mosip.testrig.dslrig.ivv.e2e.methods;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.testng.Reporter;

import io.mosip.testrig.apirig.utils.AdminTestException;
import io.mosip.testrig.apirig.utils.AdminTestUtil;
import io.mosip.testrig.apirig.auth.testscripts.PostWithAutogenIdWithOtpGenerate;
import io.mosip.testrig.apirig.auth.testscripts.PostWithBodyWithOtpGenerate;
import io.mosip.testrig.apirig.dto.TestCaseDTO;
import io.mosip.testrig.apirig.masterdata.testscripts.SimplePost;
import io.mosip.testrig.apirig.utils.AuthenticationTestException;
import io.mosip.testrig.apirig.utils.SecurityXSSException;
import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testrig.dslrig.ivv.orchestrator.dslConfigManager;

public class CredentialRequest extends BaseTestCaseUtil implements StepInterface {
	private static final String CredentialIssue_YML = "preReg/credentialIssue/credentialIssue.yml";
	private static final String CredentialIssueWithoutOtp_YML = "preReg/credentialIssue/credentialIssuewithoutotp.yml";
	public static Logger logger = Logger.getLogger(CredentialRequest.class);

	static {
		if (dslConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	@SuppressWarnings("static-access")
	@Override
	public void run() throws RigInternalError {
		String emailId = "";
		if (!step.getParameters().isEmpty() && step.getParameters().size() == 2) {
			String _uin = step.getParameters().get(0);

			if (_uin.startsWith("$$")) {
				_uin = step.getScenario().getVariables().get(_uin);
				step.getScenario().getUinReqIds().clear();
				step.getScenario().getUinReqIds().put(_uin, "uin");
			}

		}

		if (step.getParameters().size() == 2 && step.getParameters().get(1).startsWith("$$")) {
			emailId = step.getParameters().get(1);
			if (emailId.startsWith("$$")) {
				emailId = step.getScenario().getVariables().get(emailId);
			}
		}
		try {
			Thread.sleep(30000);
		} catch (InterruptedException e) {
			logger.error(e.getMessage());
			Thread.currentThread().interrupt();
		}

		String fileName = "";

		if (emailId == null || emailId.isEmpty()) {

			fileName = CredentialIssueWithoutOtp_YML;
			SimplePost postwithoutotp = new SimplePost();
			Object[] casesList = postwithoutotp.getYmlTestData(fileName);
			Object[] testCaseList = filterTestCases(casesList);
			logger.info("No. of TestCases in Yml file : " + testCaseList.length);
			try {
				for (Object object : testCaseList) {
					for (String uin : this.step.getScenario().getUinReqIds().keySet()) {

						TestCaseDTO test = (TestCaseDTO) object;
						test.setInput(test.getInput().replace("$UIN$", uin).replace("$UIN$", uin));
						test.setOutput(test.getOutput().replace("$UIN$", uin));
						Reporter.log("<b><u>" + test.getTestCaseName() + "</u></b>");

						long startTime = System.currentTimeMillis();
						logger.info(this.getClass().getSimpleName() + " starts at..." + startTime + " MilliSec");
						postwithoutotp.test(test);
						long stopTime = System.currentTimeMillis();
						long elapsedTime = stopTime - startTime;
						logger.info("Time taken to execute " + this.getClass().getSimpleName() + ": " + elapsedTime
								+ " MilliSec");
						JSONObject response = new JSONObject(postwithoutotp.response.asString());
						if (!response.get("response").toString().equals("null")) {
							JSONObject responseJson = new JSONObject(response.get("response").toString());
							this.step.getScenario().getUinReqIds().put(uin, responseJson.get("requestId").toString());
							if (step.getOutVarName() != null)
								step.getScenario().getVariables().put(step.getOutVarName(),
										responseJson.get("requestId").toString());
						}
					}
				}
			} catch (AuthenticationTestException | AdminTestException | SecurityXSSException e) {
				logger.error(e.getMessage());
				this.hasError = true;
				throw new RigInternalError("Failed at credential issuance Response validation");
			}
		} else {

			fileName = CredentialIssue_YML;
			PostWithAutogenIdWithOtpGenerate postWithOtp = new PostWithAutogenIdWithOtpGenerate();
			Object[] casesList = postWithOtp.getYmlTestData(fileName);
			Object[] testCaseList = filterTestCases(casesList);
			logger.info("No. of TestCases in Yml file : " + testCaseList.length);
			try {
				for (Object object : testCaseList) {
					for (String uin : this.step.getScenario().getUinReqIds().keySet()) {

						TestCaseDTO test = (TestCaseDTO) object;
						test.setInput(test.getInput().replace("$UIN$", uin).replace("$UIN$", uin));
						test.setInput(test.getInput().replace("$OTP$", emailId).replace("$OTP$", emailId));
						test.setInput(test.getInput().replace("$TRANSACTIONID$", AdminTestUtil.generateRandomNumberString(10)));
						test.setOutput(test.getOutput().replace("$UIN$", uin));
						Reporter.log("<b><u>" + test.getTestCaseName() + "</u></b>");

						long startTime = System.currentTimeMillis();
						logger.info(this.getClass().getSimpleName() + " starts at..." + startTime + " MilliSec");
						postWithOtp.test(test);
						long stopTime = System.currentTimeMillis();
						long elapsedTime = stopTime - startTime;
						logger.info("Time taken to execute " + this.getClass().getSimpleName() + ": " + elapsedTime
								+ " MilliSec");
						JSONObject response = new JSONObject(postWithOtp.response.asString());
						if (!response.get("response").toString().equals("null")) {
							JSONObject responseJson = new JSONObject(response.get("response").toString());
							this.step.getScenario().getUinReqIds().put(uin, responseJson.get("requestId").toString());
							if (step.getOutVarName() != null)
								step.getScenario().getVariables().put(step.getOutVarName(),
										responseJson.get("requestId").toString());
						}
					}
				}
			} catch (AuthenticationTestException | AdminTestException | SecurityXSSException | NumberFormatException | InterruptedException e) {
				logger.error(e.getMessage());
				this.hasError = true;
				throw new RigInternalError("Failed at credential issuance Response validation");
			}
		}
	}
}
