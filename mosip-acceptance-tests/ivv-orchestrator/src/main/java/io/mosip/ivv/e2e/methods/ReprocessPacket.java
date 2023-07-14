package io.mosip.ivv.e2e.methods;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;
import io.restassured.response.Response;

public class ReprocessPacket extends BaseTestCaseUtil implements StepInterface {
	Logger logger = Logger.getLogger(ReprocessPacket.class);

	@Override
	public void run() throws RigInternalError {

		JSONObject myJSONObject = null;
		String rid = null;
		Boolean flag = false;
		
		if (step.getParameters().size() > 1) {
			rid = step.getScenario().getVariables().get(step.getParameters().get(0));
			flag = Boolean.parseBoolean(step.getParameters().get(1));
			
		}
		
//		{
//			  "reg_type": "NEW",
//			  "rid": "10004102090002620230525140932",
//			  "isValid": true,
//			  "internalError": false,
//			  "messageBusAddress": "abcd",
//			  "retryCount": 5,
//			  "iteration": 1,
//			  "workflowInstanceId": "6a2e9f62-583e-4924-9ec4-347bd3169c0a"}
		
		JSONObject jsonReq = new JSONObject();
		jsonReq.put("rid", rid);
		jsonReq.put("reg_type", "NEW");
		


		
Response response = postRequest(baseUrl + props.getProperty("reprocessPacket"),jsonReq.toString(), "Reprocess the rid", step);

		// Check these two keys statusCode,transactionTypeCode

		JSONObject res = new JSONObject(response.getBody().asString());
		JSONArray arr = res.getJSONObject("response").getJSONArray("packetStatusUpdateList");
		for (Object myObject : arr) {
			myJSONObject = (JSONObject) myObject;


		}
		logger.info(res.toString());
		if (flag.equals(true)) {
			logger.info("RESPONSE= contains" );
			logger.info("subStatusCode= " + myJSONObject.getString("subStatusCode"));

		} else {
			logger.error("RESPONSE= doesn't contain" + arr);
			throw new RuntimeException("RESPONSE= doesn't contain" );
		}

	}
}