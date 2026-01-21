package io.mosip.testrig.dslrig.ivv.e2e.methods;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import io.mosip.testrig.apirig.utils.GetCredentialTableStackTrace;
import io.mosip.testrig.apirig.utils.GlobalMethods;
import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.dtos.Scenario;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testrig.dslrig.ivv.orchestrator.dslConfigManager;
import io.restassured.response.Response;

public class CheckRIDStatus extends BaseTestCaseUtil implements StepInterface {
	private static final Logger logger = Logger.getLogger(CheckRIDStatus.class);
	public HashMap<String, String> ridStatusMap = new LinkedHashMap<>();


	static {
		if (dslConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	@Override
	public void run() throws RigInternalError {
		String Status=null;
		step.getScenario().getVidPersonaProp().clear();
		String rid = null;
		if (step.getParameters().size() >= 2) {
			Status = step.getParameters().get(0);
		}
		if (step.getParameters().size() == 2 && step.getParameters().get(1).startsWith("$$")) {
			rid = step.getParameters().get(1);
			if (rid.startsWith("$$")) {
				rid = step.getScenario().getVariables().get(rid);
			}
		}
		checkStatus(Status,rid);
	}
	
	public void checkStatus(String ridStatusParam, String Rid)
			throws RigInternalError {
		String waitTime = props.getProperty("waitTime");
		try {
				int counter = 0;
				String ridStatus = "";
				while (!ridStatus.equalsIgnoreCase(ridStatusParam) && counter < Integer.parseInt(props.getProperty("loopCount"))) {
					counter++;
					logger.info("Waiting for " + Long.parseLong(waitTime) / 1000 + " sec to get UIN Stored");
					Thread.sleep(Long.parseLong(waitTime));
					long startTime = System.currentTimeMillis();
					logger.info(this.getClass().getSimpleName() + " starts at..." + startTime + " MilliSec");
					logger.info(this.getClass().getSimpleName() + " Rid :" + Rid);
					if (Rid == null)
						logger.info("RID is null");
					ridStatus=GetCredentialTableStackTrace.getStatusFromCredentialTransactionTable(Rid);
					GlobalMethods.ReportRequestAndResponse(null, null, null, null,
							ridStatus,true);
					long stopTime = System.currentTimeMillis();
					long elapsedTime = stopTime - startTime;
					logger.info("Time taken to execute " + this.getClass().getSimpleName() + ": " + elapsedTime
							+ " MilliSec");
					logger.info("Response from check RID status : " + Rid + " => " + ridStatus);
				}
				ridStatusMap.put(Rid, ridStatus);

			
			if (ridStatusMap.size() == 1) {
				if (!ridStatusMap.entrySet().iterator().next().getValue().contains(ridStatusParam)) {
					this.hasError = true;
					throw new RigInternalError("Failed at UIN Stored");
				}
			} else if (ridStatusMap.size() > 1 && !Rid.isEmpty()) {
				List<String> params = Arrays.asList("any", "all");
				String matchingParam = params.stream().filter(p -> p.equalsIgnoreCase(Rid))
						.findFirst().orElseThrow(() -> new RigInternalError(
								"Parameter : " + Rid + " not supported, only allowed are " + params));

				long packetProcessed = ridStatusMap.values().stream()
						.filter(v -> v.equalsIgnoreCase(ridStatusParam.toLowerCase())).count();
				if (Rid.equalsIgnoreCase("any") && packetProcessed != 1) {
					this.hasError = true;
					throw new RigInternalError("Failed at UIN Stored");
				} else if (Rid.equalsIgnoreCase("all") && packetProcessed != ridStatusMap.size()) {
					this.hasError = true;
					throw new RigInternalError("Failed at UIN Stored");
				}
			}

		} catch (InterruptedException e) {
			logger.error("Failed due to thread sleep: " + e.getMessage());
			Thread.currentThread().interrupt();
		}

	}
}
