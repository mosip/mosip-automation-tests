package io.mosip.ivv.e2e.methods;

import java.util.HashMap;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.e2e.constant.E2EConstants;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;
import io.restassured.response.Response;

public class GetPingHealth extends BaseTestCaseUtil implements StepInterface {
	Logger logger = Logger.getLogger(GetPingHealth.class);

	@Override
	public void run() throws RigInternalError {

		String modules = null;
		HashMap<String, String> context=null;
		if (step.getParameters().isEmpty() || step.getParameters().size() < 1) {
			context = contextInuse;
			modules = "";
		} else {
			context = contextInuse;
			if (step.getParameters().size() == 1)
				modules = step.getParameters().get(0);
			else
				modules = "";
		}
		String uri=baseUrl + "/ping?contextKey="+context.get(E2EConstants.CONTEXTKEY)+"&module="+modules;
		Response response = getRequest(uri, "Health Check");
		JSONObject res = new JSONObject(response.asString());
		logger.info(res.toString());
		if (res.get("status").equals(true)) {
			logger.info("RESPONSE=" + res.toString());
		} else {
			logger.error("RESPONSE=" + res.toString());
			throw new RuntimeException("Health check status" + res.toString());
		}

	}
}