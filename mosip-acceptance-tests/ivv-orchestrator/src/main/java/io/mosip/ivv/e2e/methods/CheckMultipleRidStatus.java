package io.mosip.ivv.e2e.methods;

import static org.testng.Assert.assertTrue;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.log4j.Logger;
import org.testng.Reporter;
import io.mosip.admin.fw.util.AdminTestException;
import io.mosip.admin.fw.util.TestCaseDTO;
import io.mosip.authentication.fw.precon.JsonPrecondtion;
import io.mosip.authentication.fw.util.AuthenticationTestException;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.core.utils.Utils;
import io.mosip.ivv.e2e.constant.E2EConstants;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testscripts.SimplePost;

public class CheckMultipleRidStatus extends BaseTestCaseUtil implements StepInterface {
	private static final String check_status_YML = "preReg/checkStatus/checkstatus.yml";
	Logger logger = Logger.getLogger(CheckMultipleRidStatus.class);
	public HashMap<String, String> tempPridAndRid = null;
	public HashMap<String, String> ridStatusMap = new LinkedHashMap<>();

	@Override
    public void run() throws RigInternalError {
		String status_param =null;
		if (step.getParameters() == null || step.getParameters().isEmpty()) {
			logger.error("Parameter is  missing from DSL step");
			assertTrue(false,"StatusCode paramter is  missing in step: "+step.getName());
		} else {
			status_param =step.getParameters().get(0);
		}
		checkStatus(status_param);
	}
    
	public void checkStatus(String status_param) throws RigInternalError {
		String status_Message=null;
		switch(status_param.toLowerCase()) {
		case E2EConstants.PROCESSED:
			status_Message=status_param;
			break;
		case E2EConstants.REJECTED:
			status_Message=E2EConstants.REJECTED_MSG;
			break;
		case E2EConstants.FAILED:
			status_Message=E2EConstants.FAILED_MSG;
			break;
		default:
			throw new RigInternalError("Parameter not supported only allowed are [processed/rejected/failed]");
		}
		if(tempPridAndRid ==null)
    		tempPridAndRid =pridsAndRids;
		
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
						Utils.auditLog.info(this.getClass().getSimpleName()+" Rid :"+rid);
						postScript.test(test);
						ridStatusMap.put(rid, JsonPrecondtion.getValueFromJson(postScript.response.getBody().asString(), "response.ridStatus"));
						long stopTime = System.currentTimeMillis();
						long elapsedTime = stopTime - startTime;
						logger.info("Time taken to execute "+ this.getClass().getSimpleName()+": " +elapsedTime +" MilliSec");
						Reporter.log("<b><u>"+"Time taken to execute "+ this.getClass().getSimpleName()+": " +elapsedTime +" MilliSec"+ "</u></b>");
						packetProcessed = true;
						if(tempPridAndRid.size() > 1)
						status_Message="processed";
						} catch (AuthenticationTestException | AdminTestException e) {
							logger.error("Failed at checking Packet status with error: " + e.getMessage());
							status=postScript.response.getBody().asString().toLowerCase();
							ridStatusMap.put(rid, JsonPrecondtion.getValueFromJson(postScript.response.getBody().asString(), "response.ridStatus"));
							if(tempPridAndRid.size()==1) {
								if(!ridStatusMap.containsValue(E2EConstants.UNDER_PROCESSING_MSG))
									throw new RigInternalError(ridStatusMap.get(rid));
							}
						}
					}
					}	
		} catch (InterruptedException e) {
			logger.error("Failed due to thread sleep: " + e.getMessage());
		}
		List<String> valuesList =ridStatusMap.values().stream().collect(Collectors.toList());
		if (tempPridAndRid != null && tempPridAndRid.size() > 1) {
			if (valuesList.size() != new HashSet<String>(ridStatusMap.values()).size())
				assertTrue(false, "Testcase is failed");
			else
				assertTrue(true, "Testcase is passed");
		}else if( tempPridAndRid.size()==1 && !valuesList.contains(status_Message.toUpperCase()))
			assertTrue(false, "Testcase is failed");
	}

}
