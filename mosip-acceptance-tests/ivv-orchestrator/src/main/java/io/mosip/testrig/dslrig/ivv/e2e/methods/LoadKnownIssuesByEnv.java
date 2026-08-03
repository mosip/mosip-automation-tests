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
		int maxAttempts = parseIntOrDefault(System.getProperty("env.idrepoActuatorLoadAttempts"), 3);
		long baseDelayMs = parseLongOrDefault(System.getProperty("env.idrepoActuatorLoadRetryMs"), 3000L);
		Response response = null;
		RuntimeException lastError = null;
		for (int attempt = 1; attempt <= maxAttempts; attempt++) {
			if (attempt > 1) {
				logger.warn("Retrying id-repository actuator fetch (attempt " + attempt + "/" + maxAttempts + ")");
				sleepQuietly(baseDelayMs * attempt);
				if (Thread.currentThread().isInterrupted()) {
					break;
				}
			}
			lastError = null;
			try {
				response = given().config(getIdRepoActuatorHttpConfig()).contentType(ContentType.JSON)
						.accept(ContentType.JSON).get(infoUrl);
				if (response != null && response.getStatusCode() == 200) {
					break;
				}
			} catch (RuntimeException e) {
				lastError = e;
				response = null;
			}
		}
		if (lastError != null) {
			this.hasError = true;
			throw new RigInternalError("Failed to fetch id-repository actuator info from " + infoUrl, lastError);
		}
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
					+ e.getMessage(), e);
		}
		try {
			dslConfigManager.initKnownIssuesFromIdRepoVersion(version);
		} catch (IllegalArgumentException | IllegalStateException e) {
			this.hasError = true;
			throw new RigInternalError("Failed to load known issues for id-repository version " + version, e);
		}
		String knownIssuesSourceFile = dslConfigManager.getKnownIssuesSourceFile();
		String envLabel = "config/java11Known_Issues.txt".equals(knownIssuesSourceFile) ? "Java 11" : "Java 21";
		String message = "Detected id-repository version " + version + " (" + envLabel
				+ "); using known issues from " + knownIssuesSourceFile;
		logger.info(message);
		Reporter.log(message + "<br>");
	}

	private static String joinBaseUrlAndPath(String baseUrl, String path) {
		String base = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
		String relative = path.startsWith("/") ? path.substring(1) : path;
		return java.net.URI.create(base).resolve(relative).toString();
	}

	private static int parseIntOrDefault(String value, int defaultValue) {
		if (value == null || value.isBlank()) {
			return defaultValue;
		}
		try {
			return Integer.parseInt(value.trim());
		} catch (NumberFormatException e) {
			logger.warn("Invalid value '" + value + "', using default " + defaultValue);
			return defaultValue;
		}
	}

	private static long parseLongOrDefault(String value, long defaultValue) {
		if (value == null || value.isBlank()) {
			return defaultValue;
		}
		try {
			return Long.parseLong(value.trim());
		} catch (NumberFormatException e) {
			logger.warn("Invalid value '" + value + "', using default " + defaultValue);
			return defaultValue;
		}
	}

	private static void sleepQuietly(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
}
