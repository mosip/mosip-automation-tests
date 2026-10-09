package io.mosip.testrig.dslrig.ivv.e2e.methods;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testrig.dslrig.ivv.orchestrator.dslConfigManager;
import io.restassured.response.Response;

public class ClearPacketManagerCache extends BaseTestCaseUtil implements StepInterface {
	public static Logger logger = Logger.getLogger(ClearPacketManagerCache.class);

	static {
		if (dslConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	@Override
	public void run() throws RigInternalError {
		String path = props.getProperty("clearPacketManagerCache");
		if (path == null || path.isBlank()) {
			this.hasError = true;
			throw new RigInternalError("clearPacketManagerCache is not set");
		}
		String clearUrl = baseUrl + path;
		String contextKey = buildPacketCreatorContextKey(step.getScenario());
		Response response = postRequest(clearUrl, "{}", "Clear packet manager caches", step, contextKey);
		if (response == null || response.getStatusCode() != 200) {
			this.hasError = true;
			String body = response == null || response.getBody() == null ? "" : response.getBody().asString();
			int status = response == null ? 0 : response.getStatusCode();
			throw new RigInternalError("Clearing packet manager caches failed: " + status + " " + body);
		}
		logger.info("Packet manager caches cleared: " + response.getBody().asString());
	}
}
