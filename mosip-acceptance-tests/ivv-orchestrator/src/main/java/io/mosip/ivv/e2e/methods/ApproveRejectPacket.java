package io.mosip.ivv.e2e.methods;

import static org.testng.Assert.assertTrue;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.testng.Reporter;
import io.mosip.admin.fw.util.TestCaseDTO;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testscripts.SimplePostForAutoGenId;

public class ApproveRejectPacket extends BaseTestCaseUtil implements StepInterface {
	private static final String DECISSIONDATA_YML = "preReg/approveRejectPacket/decissionData.yml";
	Logger logger = Logger.getLogger(ApproveRejectPacket.class);
	
	@Override
	public void run() throws RigInternalError {
		String status_Code=null;
		if (step.getParameters() == null || step.getParameters().isEmpty()) {
			logger.error("Parameter is  missing from DSL step");
			assertTrue(false,"StatusCode paramter is  missing in step: "+step.getName());
		} else {
			status_Code =step.getParameters().get(0);
		}
		SimplePostForAutoGenId simplePostForAutoGenId= new SimplePostForAutoGenId();
		Object[] casesList = simplePostForAutoGenId.getYmlTestData(DECISSIONDATA_YML);
		Object[] testCaseList = filterTestCases(casesList);
		simplePostForAutoGenId.idKeyName="regId";
		logger.info("No. of TestCases in Yml file : " + testCaseList.length);
		try {
			for (Object object : testCaseList) {
				TestCaseDTO test = (TestCaseDTO) object;
				for(String keys:manualVerificationRid.keySet()) {
					test.setInput(test.getInput()
							.replace("$mvUsrId$", keys)
							.replace("$regId$", manualVerificationRid.get(keys))
							.replace("$statusCode$", status_Code));
					test.setOutput(test.getOutput()
							.replace("$mvUsrId$", keys)
							.replace("$statusCode$", status_Code)
							);
				}
				Reporter.log("<b><u>"+test.getTestCaseName()+ "</u></b>");
				long startTime = System.currentTimeMillis();
				logger.info(this.getClass().getSimpleName()+" starts at..."+startTime +" MilliSec");
				simplePostForAutoGenId.test(test);
				long stopTime = System.currentTimeMillis();
				long elapsedTime = stopTime - startTime;
				logger.info("Time taken to execute "+ this.getClass().getSimpleName()+": " +elapsedTime +" MilliSec");
				Reporter.log("<b><u>"+"Time taken to execute "+ this.getClass().getSimpleName()+": " +elapsedTime +" MilliSec"+ "</u></b>");
				JSONObject response = new JSONObject(simplePostForAutoGenId.response.asString());
				if(!response.get("response").toString().equals("null"))
				{
					JSONObject responseJson = new JSONObject(response.get("response").toString());
					statusCode = responseJson.get("statusCode").toString();
					pridsAndRids.clear();
					pridsAndRids.put(null, responseJson.get("regId").toString());
				}

			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new RigInternalError("Failed at decission data(approved or reject) Response validation");
		}
	}

}
