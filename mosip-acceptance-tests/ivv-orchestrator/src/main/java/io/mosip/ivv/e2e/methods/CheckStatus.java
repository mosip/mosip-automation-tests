package io.mosip.ivv.e2e.methods;

import static org.testng.Assert.assertTrue;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.apache.log4j.Logger;
import org.testng.Reporter;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.core.utils.Utils;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;
import io.restassured.response.Response;

public class CheckStatus extends BaseTestCaseUtil implements StepInterface {
	private String getRidStatusUrl = "/resident/status/";
	Logger logger = Logger.getLogger(CheckStatus.class);
	public HashMap<String, String> tempPridAndRid = null;

	@Override
    public void run() throws RigInternalError {
		String _ridStatusParam =null;
		if (step.getParameters() == null || step.getParameters().isEmpty()) {
			logger.error("Parameter is  missing from DSL step");
			assertTrue(false,"StatusCode paramter is  missing in step: "+step.getName());
		} else if(step.getParameters().size()==1){
			_ridStatusParam =step.getParameters().get(0);
			if(tempPridAndRid ==null)
	    		tempPridAndRid =pridsAndRids;
			checkStatus(_ridStatusParam);
		}else {
			if (step.getParameters().size() == 2) {   // "$$var=e2e_checkStatus(processed,$$rid)"
				_ridStatusParam = step.getParameters().get(0);
				String _rid = step.getParameters().get(1);
				if (_rid.startsWith("$$")) {
					_rid = step.getScenario().getVariables().get(_rid);
					if (tempPridAndRid == null) {
						tempPridAndRid = new HashMap<>();
						tempPridAndRid.put("rid", _rid);
						checkStatus(_ridStatusParam);
					}
				}
			}
		}
	}
    
	public void checkStatus(String _ridStatusParam) throws RigInternalError {
		List<String> allowedParam = Arrays.asList("processed", "rejected", "failed");
		if (!(allowedParam.contains(_ridStatusParam.toLowerCase())))
			throw new RigInternalError(
					"Parameter : " + _ridStatusParam + "not supported only allowed are [processed/rejected/failed]");
		try {
			for (String rid : this.tempPridAndRid.values()) {
				int counter = 0;
				String ridStatus = "under";
				while (ridStatus.contains("under") && counter < Integer.parseInt(props.getProperty("loopCount"))) {
					counter++;
					logger.info("Waiting for 30 sec to get packet procesed");
					Thread.sleep(Long.parseLong(props.getProperty("waitTime")));
					long startTime = System.currentTimeMillis();
					logger.info(this.getClass().getSimpleName() + " starts at..." + startTime + " MilliSec");
					Utils.auditLog.info(this.getClass().getSimpleName() + " Rid :" + rid);
					Response response = getRequest(baseUrl + getRidStatusUrl + rid,
							"Check rid status: " + rid);
					long stopTime = System.currentTimeMillis();
					long elapsedTime = stopTime - startTime;
					logger.info("Time taken to execute " + this.getClass().getSimpleName() + ": " + elapsedTime
							+ " MilliSec");
					Reporter.log("<b><u>" + "Time taken to execute " + this.getClass().getSimpleName() + ": "
							+ elapsedTime + " MilliSec" + "</u></b>");
					logger.info("Response from check RID status : " + rid + " => " + response.asString());
					ridStatus = response.asString().toLowerCase();
				}
				if (!ridStatus.contains(_ridStatusParam.toLowerCase()))
					throw new RigInternalError("Failed at Packet Processing");
			}
		} catch (InterruptedException e) {
			logger.error("Failed due to thread sleep: " + e.getMessage());
		}

	}

}
