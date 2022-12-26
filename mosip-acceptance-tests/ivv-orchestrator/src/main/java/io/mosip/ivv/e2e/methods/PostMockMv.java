package io.mosip.ivv.e2e.methods;

import java.util.HashMap;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.e2e.constant.E2EConstants;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.service.BaseTestCase;
import io.restassured.response.Response;

public class PostMockMv extends BaseTestCaseUtil implements StepInterface {
	Logger logger = Logger.getLogger(PostMockMv.class);

	@Override
	public void run() throws RigInternalError {

		String rid = "",uri=null,decision=null;
		HashMap<String, String> context=null;
		if (step.getParameters() == null || step.getParameters().isEmpty() || step.getParameters().size() < 1) {
			logger.error("Parameter is  missing from DSL step");
			throw new RigInternalError("PostMockMv paramter is  missing in step: " + step.getName());
		} else {
			rid = step.getParameters().get(0);
			rid = step.getScenario().getVariables().get(rid);
			decision=step.getParameters().get(1);
		}
		
		 uri=BaseTestCase.ApplnURI+ props.getProperty("setMockMVExpectation");
		JSONObject jo=new JSONObject();
		
		jo.put("rid", rid);
		jo.put("mockMvDecision", decision);
		Response response = postRequest(uri, jo.toString(), "MockMv");
	//	JSONObject res = new JSONObject(response.asString());
//		logger.info(response.toString());
//		
//		if (response.toString().contains("Successfully inserted expectation")) {
//			logger.info("RESPONSE=" + response.toString());
//		} else {
//			logger.error("RESPONSE=" + response.toString());
//			throw new RuntimeException("Mock mv" + response.toString());
//		}

	}
}