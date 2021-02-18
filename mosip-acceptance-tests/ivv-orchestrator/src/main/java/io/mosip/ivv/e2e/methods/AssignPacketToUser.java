package io.mosip.ivv.e2e.methods;

import static org.testng.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.testng.Reporter;

import io.mosip.admin.fw.util.TestCaseDTO;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.e2e.constant.E2EConstants;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testscripts.SimplePost;

public class AssignPacketToUser extends BaseTestCaseUtil implements StepInterface {
	private static final String ASSIGNDATA_YML = "preReg/assignPacketToUser/assignData.yml";
	Logger logger = Logger.getLogger(AssignPacketToUser.class);
	
	
	@Override
	public void run() throws RigInternalError {
		String fileName=ASSIGNDATA_YML;
		String matchType=null;
		String userId=null;
		if (step.getParameters() == null || step.getParameters().isEmpty() || step.getParameters().size() < 2) {
			logger.error("Parameter is  missing from DSL step");
			assertTrue(false,"matchType paramter is  missing in step: "+step.getName());
		} else {
			userId=step.getParameters().get(0);
			matchType =step.getParameters().get(1);
		}
		SimplePost simplePost= new SimplePost();
		Object[] casesList = simplePost.getYmlTestData(fileName);
		Object[] testCaseList = filterTestCases(casesList);
		logger.info("No. of TestCases in Yml file : " + testCaseList.length);
		try {
			for (Object object : testCaseList) {
				TestCaseDTO test = (TestCaseDTO) object;
				test.setInput(test.getInput().replace("$userId$", userId).replace("$matchType$", matchType));
				test.setOutput(test.getOutput().replace("$mvUsrId$", userId));
				Reporter.log("<b><u>"+test.getTestCaseName()+ "</u></b>");
				
				long startTime = System.currentTimeMillis();
				logger.info(this.getClass().getSimpleName()+" starts at..."+startTime +" MilliSec");
				simplePost.test(test);
				long stopTime = System.currentTimeMillis();
				long elapsedTime = stopTime - startTime;
				logger.info("Time taken to execute "+ this.getClass().getSimpleName()+": " +elapsedTime +" MilliSec");
				Reporter.log("<b><u>"+"Time taken to execute "+ this.getClass().getSimpleName()+": " +elapsedTime +" MilliSec"+ "</u></b>");
				JSONObject response = new JSONObject(simplePost.response.asString());
				if(!response.get("response").toString().equals("null"))
	    		{
					JSONObject responseJson = new JSONObject(response.get("response").toString());
						manualVerificationRid.put(userId, responseJson.get("regId").toString());
					}
				}
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new RigInternalError("Failed at manual verification Response validation");
		}
	}

}
