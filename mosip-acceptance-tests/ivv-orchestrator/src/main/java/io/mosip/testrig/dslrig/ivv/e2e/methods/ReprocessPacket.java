package io.mosip.testrig.dslrig.ivv.e2e.methods;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import io.mosip.testrig.apirig.utils.ConfigManager;
import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.restassured.response.Response;

public class ReprocessPacket extends BaseTestCaseUtil implements StepInterface {
	static Logger logger = Logger.getLogger(ReprocessPacket.class);

	static {
		if (ConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	@Override
	public void run() throws RigInternalError {

		JSONObject myJSONObject = null;
		String rid = null;
		Boolean flag = false;

		if (step.getParameters().size() > 1) {
			rid = step.getScenario().getVariables().get(step.getParameters().get(0));
			flag = Boolean.parseBoolean(step.getParameters().get(1));

		}

		JSONObject jsonReq = new JSONObject();
		jsonReq.put("rid", rid);
		jsonReq.put("reg_type", "NEW");

		Response response = postRequest(baseUrl + props.getProperty("reprocessPacket"), jsonReq.toString(),
				"Reprocess the rid", step);

		JSONObject res = new JSONObject(response.getBody().asString());
		JSONArray arr = res.getJSONObject("response").getJSONArray("packetStatusUpdateList");
		for (Object myObject : arr) {
			myJSONObject = (JSONObject) myObject;

		}
		logger.info(res.toString());
		if (flag.equals(true) && myJSONObject != null) {
			logger.info("RESPONSE= contains");
			logger.info("subStatusCode= " + myJSONObject.getString("subStatusCode"));

		} else {
			logger.error("RESPONSE= doesn't contain" + arr);
			throw new RuntimeException("RESPONSE= doesn't contain");
		}

	}
}