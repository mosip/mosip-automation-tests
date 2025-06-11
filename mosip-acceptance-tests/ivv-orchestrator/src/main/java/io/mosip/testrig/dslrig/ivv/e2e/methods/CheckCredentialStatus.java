package io.mosip.testrig.dslrig.ivv.e2e.methods;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.testng.Reporter;
import io.mosip.testrig.apirig.utils.AdminTestException;
import io.mosip.testrig.apirig.dto.TestCaseDTO;
import io.mosip.testrig.apirig.testscripts.GetWithParam;
import io.mosip.testrig.apirig.utils.AuthenticationTestException;
import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testrig.dslrig.ivv.orchestrator.dslConfigManager;

public class CheckCredentialStatus extends BaseTestCaseUtil implements StepInterface {
	private static final String check_status_YML = "preReg/credentialStatus/credentialStatus.yml";
	public static Logger logger = Logger.getLogger(CheckCredentialStatus.class);

	static {
		if (dslConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	@SuppressWarnings("static-access")
	@Override
	public void run() throws RigInternalError {
		if (!step.getParameters().isEmpty() && step.getParameters().size() == 1) {
			String _requestId = step.getParameters().get(0);
			if (_requestId.startsWith("$$")) {
				_requestId = step.getScenario().getVariables().get(_requestId);
				step.getScenario().getUinReqIds().clear();
				step.getScenario().getUinReqIds().put("requestId", _requestId);
			}
		}
		String fileName = check_status_YML;
		GetWithParam getWithPathParam = new GetWithParam();
		Object[] casesList = getWithPathParam.getYmlTestData(fileName);
		Object[] testCaseList = filterTestCases(casesList);
		logger.info("No. of TestCases in Yml file : " + testCaseList.length);

		boolean credentialIssued = false;
		try {
			for (Object object : testCaseList) {
				for (String requestid : this.step.getScenario().getUinReqIds().values()) {
					int counter = 0;
					while (!credentialIssued && counter < Integer.parseInt(props.getProperty("loopCount"))) {
						counter++;
						try {
							logger.info("Waiting for 30 sec to get credential Issued");
							Thread.sleep(Long.parseLong(props.getProperty("waitTime")));
							TestCaseDTO test = (TestCaseDTO) object;
							test.setInput(test.getInput().replace("$requestId$", requestid));
							Reporter.log("<b><u>" + test.getTestCaseName() + "</u></b>");

							long startTime = System.currentTimeMillis();
							logger.info(this.getClass().getSimpleName() + " starts at..." + startTime + " MilliSec");
							getWithPathParam.test(test);
							long stopTime = System.currentTimeMillis();
							long elapsedTime = stopTime - startTime;
							logger.info("Time taken to execute " + this.getClass().getSimpleName() + ": " + elapsedTime
									+ " MilliSec");

							if (getWithPathParam.response.getBody().asString().toLowerCase().contains("printed")
									|| getWithPathParam.response.getBody().asString().toLowerCase()
											.contains("printing"))
								credentialIssued = true;
						} catch (AuthenticationTestException | AdminTestException e) {
							logger.error("Failed at checking Credential status with error: " + e.getMessage());
						}
					}
					if (!getWithPathParam.response.getBody().asString().toLowerCase().contains("printed")
							&& !getWithPathParam.response.getBody().asString().toLowerCase().contains("printing")) {
						this.hasError = true;
						throw new RigInternalError("Failed at credential issuance status check Response validation");
					}
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			Thread.currentThread().interrupt();
			this.hasError = true;
			throw new RigInternalError("Unable to check credential status");
		}
	}
}
