package io.mosip.testrig.dslrig.ivv.e2e.methods;

import static io.restassured.RestAssured.given;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Reporter;

import io.mosip.testrig.apirig.testrunner.BaseTestCase;
import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testrig.dslrig.ivv.orchestrator.dslConfigManager;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class LoadKnownIssuesByEnv extends BaseTestCaseUtil implements StepInterface {

	private static final String DEFAULT_IDREPO_ACTUATOR_INFO_PATH = "idrepository/v1/identity/actuator/info";

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
		String targetBaseUrl = BaseTestCase.ApplnURI;
		if (targetBaseUrl == null || targetBaseUrl.isBlank()) {
			this.hasError = true;
			throw new RigInternalError("env.endpoint (ApplnURI) is not configured");
		}
		String actuatorPath = props.getProperty("idrepoActuatorInfoPath");
		if (actuatorPath == null || actuatorPath.isBlank()) {
			actuatorPath = DEFAULT_IDREPO_ACTUATOR_INFO_PATH;
		}
		String infoUrl = joinBaseUrlAndPath(targetBaseUrl.trim(), actuatorPath.trim());
		Response response = given().config(getIdRepoActuatorHttpConfig()).contentType(ContentType.JSON)
				.accept(ContentType.JSON).get(infoUrl);
		if (response == null || response.getStatusCode() != 200) {
			this.hasError = true;
			String responseBody = response == null ? "" : response.getBody().asString();
			throw new RigInternalError("Failed to fetch id-repository actuator info from " + infoUrl + " (HTTP "
					+ (response == null ? "no response" : response.getStatusCode()) + "): " + responseBody);
		}
		String version;
		try {
			JSONObject actuatorInfo = new JSONObject(response.getBody().asString());
			version = actuatorInfo.getJSONObject("build").getString("version");
		} catch (JSONException e) {
			this.hasError = true;
			throw new RigInternalError("Malformed id-repository actuator info response from " + infoUrl + ": "
					+ e.getMessage());
		}
		dslConfigManager.initKnownIssuesFromIdRepoVersion(version);
		String envLabel = version.startsWith("1.2") ? "Java 11" : "Java 21";
		String message = "Detected id-repository version " + version + " (" + envLabel
				+ "); using known issues from " + dslConfigManager.getKnownIssuesSourceFile();
		logger.info(message);
		Reporter.log(message + "<br>");
	}

	private static String joinBaseUrlAndPath(String baseUrl, String path) {
		String base = baseUrl.trim();
		String apiPath = path.trim();
		if (base.endsWith("/") && apiPath.startsWith("/")) {
			return base + apiPath.substring(1);
		}
		if (!base.endsWith("/") && !apiPath.startsWith("/")) {
			return base + "/" + apiPath;
		}
		return base + apiPath;
	}
}
