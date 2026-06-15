package io.mosip.testrig.dslrig.ivv.e2e.methods;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.testng.Reporter;

import io.mosip.testrig.apirig.testrunner.BaseTestCase;
import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testrig.dslrig.ivv.orchestrator.dslConfigManager;
import io.restassured.response.Response;

public class LoadKnownIssuesByEnv extends BaseTestCaseUtil implements StepInterface {

	private static final Logger logger = Logger.getLogger(LoadKnownIssuesByEnv.class);

	static {
		if (dslConfigManager.IsDebugEnabled()) {
			logger.setLevel(Level.ALL);
		} else {
			logger.setLevel(Level.ERROR);
		}
	}

	@Override
	public void run() throws RigInternalError {
		String endpoint = props.getProperty("idrepoActuatorInfo");
		if (endpoint == null || endpoint.isBlank()) {
			this.hasError = true;
			throw new RigInternalError("idrepoActuatorInfo property is not configured");
		}
		String targetBaseUrl = BaseTestCase.ApplnURI;
		if (targetBaseUrl == null || targetBaseUrl.isBlank()) {
			this.hasError = true;
			throw new RigInternalError("env.endpoint (ApplnURI) is not configured");
		}
		String query = "?targetBaseUrl=" + URLEncoder.encode(targetBaseUrl.trim(), StandardCharsets.UTF_8);
		String url = baseUrl + endpoint + query;
		Response response = getRequest(url, "Fetch id-repository actuator info for known-issues selection", step);
		if (response == null || response.getStatusCode() != 200) {
			this.hasError = true;
			throw new RigInternalError("Failed to fetch id-repository actuator info from packet creator (HTTP "
					+ (response == null ? "no response" : response.getStatusCode()) + ")");
		}
		JSONObject actuatorInfo = new JSONObject(response.getBody().asString());
		String version = actuatorInfo.getJSONObject("build").getString("version");
		dslConfigManager.initKnownIssuesFromIdRepoVersion(version);
		String envLabel = version.startsWith("1.2") ? "Java 11" : "Java 21";
		String message = "Detected id-repository version " + version + " (" + envLabel
				+ "); using known issues from " + dslConfigManager.getKnownIssuesSourceFile();
		logger.info(message);
		Reporter.log(message + "<br>");
	}
}
