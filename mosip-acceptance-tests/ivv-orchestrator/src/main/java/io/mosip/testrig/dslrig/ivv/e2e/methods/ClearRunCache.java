package io.mosip.testrig.dslrig.ivv.e2e.methods;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testrig.dslrig.ivv.orchestrator.dslConfigManager;
import io.mosip.testrig.dslrig.ivv.orchestrator.util.RunCacheStepSupport;
import io.restassured.response.Response;

public class ClearRunCache extends BaseTestCaseUtil implements StepInterface {
	public static Logger logger = Logger.getLogger(ClearRunCache.class);

	static {
		if (dslConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	@Override
	public void run() throws RigInternalError {
		String clearUrl = baseUrl + props.getProperty("clearRunCache");
		String contextKey = buildPacketCreatorContextKey(step.getScenario());
		if ("AFTER_SUITE".equalsIgnoreCase(step.getScenario().getId())) {
			contextKey = System.getProperty("env.user") + "_S0_context";
		}
		Response response = postRequest(clearUrl, "{}", "Clear run-scoped MOSIP API cache", step, contextKey);
		if (response == null || response.getStatusCode() != 200) {
			this.hasError = true;
			throw new RigInternalError("Clearing run cache failed");
		}
		RunCacheStepSupport.assertClearSucceeded(response.getBody().asString());
		logger.info("Run cache cleared");
	}
}
