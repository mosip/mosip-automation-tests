package io.mosip.ivv.e2e.methods;

import java.util.HashMap;

import org.apache.commons.collections4.map.HashedMap;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;
import io.restassured.response.Response;


public class GetAdditionalReqId extends BaseTestCaseUtil implements StepInterface {  //  $$additionalReqId=e2e_getAdditionalReqId(10)

	Logger logger = Logger.getLogger(GetAdditionalReqId.class);
	@Override
	public void run() throws RigInternalError {
		int counter = 0;
		int repeats = 10; // taking from dsl argument
		if (!step.getParameters().isEmpty() && step.getParameters().size() > 0)
			repeats = Integer.parseInt(step.getParameters().get(0));
		String url = baseUrl + props.getProperty("getAdditionalInfoReqId");
		
		HashMap<Long, JSONObject> map=ReadWebSocket.map;
		
		while (counter < repeats) {
			logger.info("*******Checking the email for AdditionalInfoReqId...*******");
			Response response = getRequest(url, "Get addtionalInfoRequestId");
			String additonalInfoRequestId = response.getBody().asString();
			if (additonalInfoRequestId != null && !additonalInfoRequestId.isEmpty()
					&& !additonalInfoRequestId.equals("{Failed}")) {
				logger.info("AdditionalInfoReqId retrieved: " + additonalInfoRequestId);
				if (step.getOutVarName() != null)
					step.getScenario().getVariables().put(step.getOutVarName(), additonalInfoRequestId);
				return;
			}
			counter++;
			try {
				logger.info("waiting for 10 Sec");
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				logger.error(e.getMessage());
			}
		}
		logger.error("AdditionalInfoReqId not found even after " + repeats + " retries");
		throw new RigInternalError("Failed to retrieve the value for addtionalInfoRequestId from email");

	}

}
