package io.mosip.testrig.dslrig.ivv.e2e.methods;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testrig.dslrig.ivv.orchestrator.dslConfigManager;
import io.restassured.response.Response;

public class CheckRIDStage extends BaseTestCaseUtil implements StepInterface {
	public static Logger logger = Logger.getLogger(CheckRIDStage.class);

	static {
		if (dslConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	@Override
	public void run() throws RigInternalError {
		String ridStage = null;
		boolean flag = false;
		String transactionTypeCode = null;
		String statusCode = null;
		String subStatusCode = null;
		int expectedCount = 1;
		String waitTime = props.getProperty("waitTime");
		int counter = 0;
		JSONObject res = null;
		JSONArray arr = null;
		String lastBody = null;
		int matchCount = 0;

		if (step.getParameters().size() >= 3) {
			ridStage = step.getScenario().getVariables().get(step.getParameters().get(0));
			transactionTypeCode = step.getParameters().get(1);
			statusCode = step.getParameters().get(2);

			if (step.getParameters().size() == 4) {
				String fourth = step.getParameters().get(3);
				if (isNumeric(fourth)) {
					expectedCount = Integer.parseInt(fourth);
				} else {
					subStatusCode = fourth;
				}
			} else if (step.getParameters().size() >= 5) {
				subStatusCode = step.getParameters().get(3);
				String fifth = step.getParameters().get(4);
				if (isNumeric(fifth)) {
					expectedCount = Integer.parseInt(fifth);
				} else {
					this.hasError = true;
					throw new RigInternalError(
							"Invalid occurrence count for CheckRIDStage: expected a number but got '" + fifth + "'");
				}
			}
		}

		if (expectedCount < 1) {
			this.hasError = true;
			throw new RigInternalError("Occurrence count must be >= 1, got " + expectedCount);
		}

		logger.info("Checking RID stage " + transactionTypeCode + " " + statusCode
				+ (subStatusCode != null ? " subStatus=" + subStatusCode : "")
				+ " expectedCount=" + expectedCount + " for rid=" + ridStage);

		int loopCount = Integer.parseInt(props.getProperty("loopCount"));
		while (counter < loopCount) {
			try {
				Response response = getRequestSilent(baseUrl + props.getProperty("ridStatus") + ridStage, step);
				lastBody = response != null ? response.getBody().asString() : null;
				if (lastBody == null || lastBody.isBlank()) {
					logger.warn("Empty RID stage response for " + ridStage + " (loop " + counter + ")");
				} else {
					res = new JSONObject(lastBody);
					if (!res.has("response") || res.isNull("response")) {
						logger.warn("RID stage response missing 'response' for " + ridStage + " (loop " + counter
								+ "): " + truncate(lastBody));
					} else {
						JSONObject responseObj = res.getJSONObject("response");
						if (!responseObj.has("packetStatusUpdateList")) {
							logger.warn("RID stage response missing packetStatusUpdateList for " + ridStage
									+ " (loop " + counter + ")");
						} else {
							arr = responseObj.getJSONArray("packetStatusUpdateList");
							matchCount = countMatchingEntries(arr, transactionTypeCode, statusCode, subStatusCode);
							if (matchCount >= expectedCount) {
								logger.info("Found " + matchCount + " matching entries (expected >= "
										+ expectedCount + ")");
								flag = true;
							} else {
								logger.info("Found " + matchCount + " matching entries so far (expected >= "
										+ expectedCount + ")");
							}
						}
					}
				}
			} catch (JSONException e) {
				logger.warn("Invalid RID stage JSON for " + ridStage + " (loop " + counter + "): " + e.getMessage()
						+ " body=" + truncate(lastBody));
			} catch (Exception e) {
				logger.warn("RID stage poll failed for " + ridStage + " (loop " + counter + "): " + e.getMessage());
			}

			if (flag)
				break;

			logger.info("Waiting for " + Long.parseLong(waitTime) / 1000 + " sec to get desired response");
			counter++;
			try {
				Thread.sleep(Long.parseLong(waitTime));
			} catch (NumberFormatException | InterruptedException e) {
				logger.error(e.getMessage());
				Thread.currentThread().interrupt();
			}
		}

		Response finalResponse = getRequest(baseUrl + props.getProperty("ridStatus") + ridStage,
				"Final RID Stage Check", step);
		lastBody = finalResponse != null ? finalResponse.getBody().asString() : null;
		try {
			res = lastBody != null ? new JSONObject(lastBody) : new JSONObject();
			logger.info(res.toString());
			if (res.has("response") && !res.isNull("response")) {
				JSONObject responseObj = res.getJSONObject("response");
				if (responseObj.has("packetStatusUpdateList")) {
					matchCount = countMatchingEntries(responseObj.getJSONArray("packetStatusUpdateList"),
							transactionTypeCode, statusCode, subStatusCode);
				}
			}
		} catch (JSONException e) {
			logger.error("Final RID stage response was not JSON: " + truncate(lastBody));
			this.hasError = true;
			throw new RigInternalError("RID stage API returned invalid JSON for " + ridStage + ": " + truncate(lastBody));
		}

		if (!flag) {
			this.hasError = true;
			if (!res.has("response") || res.isNull("response")) {
				throw new RigInternalError("RID stage API missing response for " + ridStage + ": " + truncate(lastBody));
			}
			throw new RigInternalError("RESPONSE = doesn't contain " + transactionTypeCode + " " + statusCode
					+ " at least " + expectedCount + " time(s) (found " + matchCount + ")");
		}
	}

	private static int countMatchingEntries(JSONArray arr, String transactionTypeCode, String statusCode,
			String subStatusCode) {
		int count = 0;
		for (Object myObject : arr) {
			JSONObject myJSONObject = (JSONObject) myObject;
			if (!transactionTypeCode.equalsIgnoreCase(myJSONObject.getString("transactionTypeCode"))) {
				continue;
			}
			if (!statusCode.equalsIgnoreCase(myJSONObject.getString("statusCode"))) {
				continue;
			}
			if (subStatusCode == null
					|| subStatusCode.equalsIgnoreCase(myJSONObject.getString("subStatusCode"))) {
				count++;
			}
		}
		return count;
	}

	private static boolean isNumeric(String value) {
		if (value == null || value.isBlank()) {
			return false;
		}
		try {
			Integer.parseInt(value.trim());
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	private static String truncate(String body) {
		if (body == null) {
			return "null";
		}
		return body.length() > 500 ? body.substring(0, 500) + "..." : body;
	}
}
