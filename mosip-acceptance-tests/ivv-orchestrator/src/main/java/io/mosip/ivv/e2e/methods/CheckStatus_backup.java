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
import io.mosip.ivv.core.utils.Utils;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testscripts.SimplePost;

public class CheckStatus_backup extends BaseTestCaseUtil implements StepInterface {
	private static final String check_status_YML = "preReg/checkStatus/checkstatus.yml";
	Logger logger = Logger.getLogger(CheckStatus_backup.class);
	public HashMap<String, String> tempPridAndRid = null;

	@Override
    public void run() throws RigInternalError {
		String status_param =null;
		if (step.getParameters() == null || step.getParameters().isEmpty()) {
			logger.error("Parameter is  missing from DSL step");
			assertTrue(false,"StatusCode paramter is  missing in step: "+step.getName());
		} else if(step.getParameters().size()==1){
			status_param =step.getParameters().get(0);
			if(tempPridAndRid ==null)
	    		tempPridAndRid =pridsAndRids;
			checkStatus(status_param);
		}else {
			if (step.getParameters().size() == 2) {
				status_param = step.getParameters().get(0);
				String _rid = step.getParameters().get(1);
				if (_rid.startsWith("$$")) {
					_rid = step.getScenario().getVariables().get(_rid);
					if (tempPridAndRid == null) {
						tempPridAndRid = new HashMap<>();
						tempPridAndRid.put("rid", _rid);
						checkStatus(status_param);
					}
				}
			}
		}
	}
    
	public void checkStatus(String status_param) throws RigInternalError {
		String status_Message=null;
		switch(status_param.toLowerCase()) {
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
			logger.error("Parameter not supported only allowed are [processed/rejected/failed]");
		}
		/*
		 * if(tempPridAndRid ==null) tempPridAndRid =pridsAndRids;
		 */
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
						String status="under";
					while(!packetProcessed && status.contains("under") && counter<Integer.parseInt(props.getProperty("loopCount"))) {
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
						Utils.auditLog.info(this.getClass().getSimpleName()+" Rid :"+rid);
						postScript.test(test);
						long stopTime = System.currentTimeMillis();
						long elapsedTime = stopTime - startTime;
						logger.info("Time taken to execute "+ this.getClass().getSimpleName()+": " +elapsedTime +" MilliSec");
						Reporter.log("<b><u>"+"Time taken to execute "+ this.getClass().getSimpleName()+": " +elapsedTime +" MilliSec"+ "</u></b>");
						packetProcessed = true; 
						} catch (AuthenticationTestException | AdminTestException e) {
							logger.error("Failed at checking Packet status with error: " + e.getMessage());
							status=postScript.response.getBody().asString().toLowerCase();
						}
					}
					//assertTrue(postScript.response.asString().contains("PROCESSED"), "Failed at status check Response validation");
						if(!postScript.response.getBody().asString().toLowerCase().contains(status_param.toLowerCase())) 
						throw new RigInternalError("Failed at Packet Processing");
				}
			}
		} catch (InterruptedException e) {
			logger.error("Failed due to thread sleep: " + e.getMessage());
		}

	}

}
