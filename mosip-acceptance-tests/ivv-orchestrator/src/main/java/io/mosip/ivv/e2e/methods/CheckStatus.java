package io.mosip.ivv.e2e.methods;

import static org.testng.Assert.assertTrue;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.testng.Reporter;

import io.mosip.admin.fw.util.AdminTestException;
import io.mosip.admin.fw.util.TestCaseDTO;
import io.mosip.authentication.fw.util.AuthenticationTestException;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testscripts.SimplePost;

public class CheckStatus extends BaseTestCaseUtil implements StepInterface {
	private static final String check_status_YML = "preReg/checkStatus/checkstatus.yml";
	Logger logger = Logger.getLogger(CheckStatus.class);
	public HashMap<String, String> tempPridAndRid = null;

	@Override
    public void run() throws RigInternalError {
		String status_Message=null;
		String status_param =null;
		if (step.getParameters() == null || step.getParameters().isEmpty()) {
			logger.error("Parameter is  missing from DSL step");
			assertTrue(false,"StatusCode paramter is  missing in step: "+step.getName());
		} else {
			status_param =step.getParameters().get(0);
			switch(status_param) {
			case "processed":
				status_Message=status_param;
				break;
			case "rejected":
				status_Message="REJECTED - PLEASE VISIT THE NEAREST CENTER FOR DETAILS.";
				break;
			case "failed":
				status_Message="FAILED - PLEASE VISIT THE NEAREST CENTER FOR DETAILS.";
				break;	
			default:
				logger.error("Parameter not supported");
			}
		}
    	//pridsAndRids.put("54253173891651", "10002100741000220210113045712");
		checkStatus(status_param, status_Message);
		
	}
    
	public void checkStatus(String status_param, String status_Message) throws RigInternalError {
		if(tempPridAndRid ==null)
    		tempPridAndRid =pridsAndRids;
    	String fileName = check_status_YML;
    	SimplePost postScript= new SimplePost();
    	Object[] casesList = postScript.getYmlTestData(fileName);
		Object[] testCaseList = filterTestCases(casesList);
		logger.info("No. of TestCases in Yml file : " + testCaseList.length);
		
			boolean packetProcessed = false;
			try {
				for (Object object : testCaseList) {
					for(String rid: this.tempPridAndRid.values()) {
						int counter=0;
					while(!packetProcessed && counter<Integer.parseInt(props.getProperty("loopCount"))) {
						counter++;
						try {
							logger.info("Waiting for 30 sec to get packet procesed");
							Thread.sleep(Long.parseLong(props.getProperty("waitTime")));
							TestCaseDTO test = (TestCaseDTO) object;
						test.setInput(test.getInput().replace("$RID$", rid));
						test.setOutput(test.getOutput().replace("$ridStatus$", status_Message.toUpperCase()));
						Reporter.log("<b><u>"+test.getTestCaseName()+ "</u></b>");
						long startTime = System.currentTimeMillis();
						logger.info(this.getClass().getSimpleName()+" starts at..."+startTime +" MilliSec");
						postScript.test(test);
						long stopTime = System.currentTimeMillis();
						long elapsedTime = stopTime - startTime;
						logger.info("Time taken to execute "+ this.getClass().getSimpleName()+": " +elapsedTime +" MilliSec");
						Reporter.log("<b><u>"+"Time taken to execute "+ this.getClass().getSimpleName()+": " +elapsedTime +" MilliSec"+ "</u></b>");
						packetProcessed = true; 
						} catch (AuthenticationTestException | AdminTestException e) {
							logger.error("Failed at checking Packet status with error: " + e.getMessage());
						}
					}
					//assertTrue(postScript.response.asString().contains("PROCESSED"), "Failed at status check Response validation");
						if(!postScript.response.getBody().asString().toLowerCase().contains(status_param))
						throw new RigInternalError("Failed at Packet Processing");
				}
			}
		} catch (InterruptedException e) {
			logger.error("Failed due to thread sleep: " + e.getMessage());
		}

	}

}
