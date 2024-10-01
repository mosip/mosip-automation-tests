package io.mosip.testrig.dslrig.ivv.e2e.methods;

import java.util.HashMap;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import io.mosip.testrig.apirig.testrunner.BaseTestCase;
import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testrig.dslrig.ivv.orchestrator.dslConfigManager;
import io.restassured.response.Response;

@Scope("prototype")
@Component
public class PostMockMv extends BaseTestCaseUtil implements StepInterface {
	static Logger logger = Logger.getLogger(PostMockMv.class);

	static {
		if (dslConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	@Override
	public void run() throws RigInternalError {

		String rid = "", uri = null, decision = null;
		HashMap<String, String> context = null;
		if (step.getParameters() == null || step.getParameters().isEmpty() || step.getParameters().size() < 1) {
			logger.error("Parameter is  missing from DSL step");
			this.hasError = true;
			throw new RigInternalError("PostMockMv paramter is  missing in step: " + step.getName());
		} else {
			rid = step.getParameters().get(0);
			rid = step.getScenario().getVariables().get(rid);
			decision = step.getParameters().get(1);
		}

		uri = BaseTestCase.ApplnURI + props.getProperty("setMockMVExpectation");
		JSONObject jo = new JSONObject();

		jo.put("rid", rid);
		jo.put("mockMvDecision", decision);
		Response response = postRequest(uri, jo.toString(), "MockMv", step);

	}
}