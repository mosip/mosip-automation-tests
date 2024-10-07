package io.mosip.testrig.dslrig.ivv.e2e.methods;

import static org.testng.Assert.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.testng.Reporter;
import io.mosip.testrig.apirig.dto.TestCaseDTO;
import io.mosip.testrig.apirig.testrunner.JsonPrecondtion;
import io.mosip.testrig.apirig.utils.AdminTestException;
import io.mosip.testrig.apirig.utils.AuthenticationTestException;
import io.mosip.testrig.apirig.testscripts.SimplePost;
import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.e2e.constant.E2EConstants;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testrig.dslrig.ivv.orchestrator.dslConfigManager;

public class CheckMultipleRidStatus extends BaseTestCaseUtil implements StepInterface {
	private static final String check_status_YML = "preReg/checkStatus/checkstatus.yml";
	public static Logger logger = Logger.getLogger(CheckMultipleRidStatus.class);
	public HashMap<String, String> tempPridAndRid = null;
	public HashMap<String, String> ridStatusMap = new LinkedHashMap<>();

	static {
		if (dslConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	@Override
	public void run() throws RigInternalError {
		String status_param = null;
		if (step.getParameters() == null || step.getParameters().isEmpty()) {
			logger.error("Parameter is  missing from DSL step");
			assertTrue(false, "StatusCode paramter is  missing in step: " + step.getName());
		} else {
			status_param = step.getParameters().get(0);
		}
		checkStatus(status_param);
	}

	public void checkStatus(String status_param) throws RigInternalError {
		String status_Message = null;
		switch (status_param.toLowerCase()) {
		case E2EConstants.PROCESSED:
			status_Message = status_param;
			break;
		case E2EConstants.REJECTED:
			status_Message = E2EConstants.REJECTED_MSG;
			break;
		case E2EConstants.FAILED:
			status_Message = E2EConstants.FAILED_MSG;
			break;
		default:
			this.hasError = true;
			throw new RigInternalError("Parameter not supported only allowed are [processed/rejected/failed]");
		}
		if (tempPridAndRid == null)
			tempPridAndRid = step.getScenario().getPridsAndRids();

		try {
			for (String rid : this.tempPridAndRid.values()) {
				boolean packetProcessed = false;
				SimplePost postScript = new SimplePost();
				Object[] casesList = postScript.getYmlTestData(check_status_YML);
				Object[] testCaseList = filterTestCases(casesList);
				logger.info("No. of TestCases in Yml file : " + testCaseList.length);
				int counter = 0;
				String status = "under";
				while (!packetProcessed && status.contains("under")
						&& counter < Integer.parseInt(props.getProperty("loopCount"))) {
					counter++;
					try {
						logger.info("Waiting for 30 sec to get packet procesed");
						Thread.sleep(Long.parseLong(props.getProperty("waitTime")));
						TestCaseDTO test = (TestCaseDTO) testCaseList[0];
						test.setInput(test.getInput().replace("$RID$", rid));
						test.setOutput(test.getOutput().replace("$ridStatus$", status_Message.toUpperCase()));
						Reporter.log("<b><u>" + test.getTestCaseName() + "</u></b>");
						long startTime = System.currentTimeMillis();
						logger.info(this.getClass().getSimpleName() + " starts at..." + startTime + " MilliSec");
						logger.info(this.getClass().getSimpleName() + " Rid :" + rid);
						postScript.test(test);
						ridStatusMap.put(rid, JsonPrecondtion.getValueFromJson(postScript.response.getBody().asString(),
								"response.ridStatus"));
						long stopTime = System.currentTimeMillis();
						long elapsedTime = stopTime - startTime;
						logger.info("Time taken to execute " + this.getClass().getSimpleName() + ": " + elapsedTime
								+ " MilliSec");
						packetProcessed = true;
						if (tempPridAndRid.size() > 1)
							status_Message = "processed";
					} catch (AuthenticationTestException | AdminTestException e) {
						logger.error("Failed at checking Packet status with error: " + e.getMessage());
						status = postScript.response.getBody().asString().toLowerCase();
						ridStatusMap.put(rid, JsonPrecondtion.getValueFromJson(postScript.response.getBody().asString(),
								"response.ridStatus"));
						if (tempPridAndRid.size() == 1) {
							if (!ridStatusMap.containsValue(E2EConstants.UNDER_PROCESSING_MSG)) {

								this.hasError = true;
								throw new RigInternalError(ridStatusMap.get(rid));
							}
						}
					}
				}
			}
		} catch (InterruptedException e) {
			logger.error("Failed due to thread sleep: " + e.getMessage());
			Thread.currentThread().interrupt();
		}
		List<String> valuesList = ridStatusMap.values().stream().collect(Collectors.toList());
		if (tempPridAndRid != null) {
			if (tempPridAndRid.size() > 1) {
				if (valuesList.size() != new HashSet<String>(ridStatusMap.values()).size())
					assertTrue(false, "Testcase is failed");
				else
					assertTrue(true, "Testcase is passed");
			} else if (tempPridAndRid.size() == 1 && !valuesList.contains(status_Message.toUpperCase()))
				assertTrue(false, "Testcase is failed");
		} else {
			logger.info("tempPridAndRid is Null");
		}

	}

}
