package io.mosip.ivv.e2e.methods;

import static org.testng.Assert.assertTrue;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import org.apache.log4j.Logger;
import org.testng.Reporter;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.dtos.Scenario;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.core.utils.Utils;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;
import io.restassured.response.Response;

public class CheckStatus extends BaseTestCaseUtil implements StepInterface {
	private String getRidStatusUrl = "/resident/status/";
	Logger logger = Logger.getLogger(CheckStatus.class);
	public HashMap<String, String> tempPridAndRid = null;
	public HashMap<String, String> ridStatusMap = new LinkedHashMap<>();

	@Override
    public void run() throws RigInternalError {
		String _ridStatusParam =null;
		String _expectedRidProcessed="";
		if (step.getParameters() == null || step.getParameters().isEmpty()) {
			logger.error("Parameter is  missing from DSL step");
			assertTrue(false,"StatusCode paramter is  missing in step: "+step.getName());
		} else if(step.getParameters().size()==1){
			_ridStatusParam =step.getParameters().get(0);
			if(tempPridAndRid ==null)
	    		tempPridAndRid =step.getScenario().getPridsAndRids();
			checkStatus(_ridStatusParam, _expectedRidProcessed,step);
		}else {
			if (step.getParameters().size() >= 2) {   // "$$var=e2e_checkStatus(processed,$$rid)"  
				_ridStatusParam = step.getParameters().get(0);
				String _rid = step.getParameters().get(1);
				if (_rid.startsWith("$$")) {
					_rid = step.getScenario().getVariables().get(_rid); //11000000101010101000000000
					if(_rid==null)
						System.out.println("neeha");
					if (tempPridAndRid == null) {
						tempPridAndRid = new HashMap<>();
						tempPridAndRid.put("rid", _rid); //11000000101010101000000000
						if (step.getParameters().size() > 3) {  // "$$var=e2e_checkStatus(processed,$$rid,$$rid2,any)"  in case of BulkUpload
							String _rid2 = step.getParameters().get(2);
							_expectedRidProcessed=step.getParameters().get(3);
							if (_rid2.startsWith("$$")) {
								_rid2 = step.getScenario().getVariables().get(_rid2);
								tempPridAndRid.put("rid2", _rid2);
							}
						}
						checkStatus(_ridStatusParam, _expectedRidProcessed,step);
					}
				}
			}
		}
	}
    
	public void checkStatus(String _ridStatusParam,String _expectedRidProcessed,Scenario.Step step) throws RigInternalError {
		String waitTime=props.getProperty("waitTime");
		List<String> allowedParam = Arrays.asList("processed", "rejected", "failed","reregister");
		if (!(allowedParam.contains(_ridStatusParam.toLowerCase())))
			{this.hasError=true;throw new RigInternalError(
					"Parameter : " + _ridStatusParam + "not supported only allowed are [processed/rejected/failed/reregister]");}
		try {
			for (String rid : tempPridAndRid.values()) {
				int counter = 0;
				String ridStatus = "under";
				while (ridStatus.contains("under") && counter < Integer.parseInt(props.getProperty("loopCount"))) {
					counter++;
					logger.info("Waiting for " +Long.parseLong(waitTime)/1000+ " sec to get packet procesed");
					Thread.sleep(Long.parseLong(waitTime));
					long startTime = System.currentTimeMillis();
					logger.info(this.getClass().getSimpleName() + " starts at..." + startTime + " MilliSec");
					Utils.auditLog.info(this.getClass().getSimpleName() + " Rid :" + rid);
					if(rid==null) 
						System.out.println("NEeha");
					Response response = getRequest(baseUrl + getRidStatusUrl + rid,
							"Check rid status: " + rid,step);
					long stopTime = System.currentTimeMillis();
					long elapsedTime = stopTime - startTime;
					logger.info("Time taken to execute " + this.getClass().getSimpleName() + ": " + elapsedTime
							+ " MilliSec");
					Reporter.log("<b><u>" + "Time taken to execute " + this.getClass().getSimpleName() + ": "
							+ elapsedTime + " MilliSec" + "</u></b>");
					logger.info("Response from check RID status : " + rid + " => " + response.asString());
					ridStatus = response.asString().toLowerCase();
				}
				ridStatusMap.put(rid, ridStatus);
				/*
				 * if (!ridStatus.contains(_ridStatusParam.toLowerCase())) throw new
				 * RigInternalError("Failed at Packet Processing");
				 */
				 
			}
			if (ridStatusMap.size() == 1) {
				if (!ridStatusMap.entrySet().iterator().next().getValue().contains(_ridStatusParam.toLowerCase()))
					{this.hasError=true;throw new RigInternalError("Failed at Packet Processing");}
			} else if (ridStatusMap.size() > 1 && !_expectedRidProcessed.isEmpty()) {
				List<String> params = Arrays.asList("any", "all");
				params.stream().filter(p -> p.equalsIgnoreCase(_expectedRidProcessed)).findFirst()
						.orElseThrow(() -> new RigInternalError(
								"Parameter : " + _expectedRidProcessed + " not supported only allowed are " + params));
				long packetProcessed = ridStatusMap.values().stream()
						.filter(v -> v.equalsIgnoreCase(_ridStatusParam.toLowerCase())).count();
				if (_expectedRidProcessed.equalsIgnoreCase("any") && packetProcessed != 1)
				{	this.hasError=true;throw new RigInternalError("Failed at Packet Processing");}
				else if (_expectedRidProcessed.equalsIgnoreCase("all") && packetProcessed != ridStatusMap.size())
					{this.hasError=true;throw new RigInternalError("Failed at Packet Processing");
			}}
			
		} catch (InterruptedException e) {
			logger.error("Failed due to thread sleep: " + e.getMessage());
			Thread.currentThread().interrupt();
		}

	}

}
