package io.mosip.testrig.dslrig.ivv.e2e.methods;

import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.testng.Reporter;

import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.dtos.Scenario;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testrig.dslrig.ivv.orchestrator.DslReportLogUtil;
import io.mosip.testrig.dslrig.ivv.orchestrator.dslConfigManager;
import io.restassured.response.Response;

public class CheckStatus extends BaseTestCaseUtil implements StepInterface {
	private String getRidStatusUrl = "/resident/status/";
	public static Logger logger = Logger.getLogger(CheckStatus.class);
	public HashMap<String, String> tempPridAndRid = null;
	public HashMap<String, String> ridStatusMap = new LinkedHashMap<>();

	static {
		if (dslConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	@Override
	public void run() throws RigInternalError {
		String _ridStatusParam = null;
		String _expectedRidProcessed = "";

		if (step.getParameters() == null || step.getParameters().isEmpty()) {
			logger.error("Parameter is  missing from DSL step");
			assertTrue(false, "StatusCode paramter is  missing in step: " + step.getName());
		} else if (step.getParameters().size() == 1) {
			_ridStatusParam = step.getParameters().get(0);
			if (tempPridAndRid == null)
				tempPridAndRid = step.getScenario().getPridsAndRids();
			checkStatus(_ridStatusParam, _expectedRidProcessed, step);
		} else if (step.getParameters().size() >= 2) {
			_ridStatusParam = resolveScenarioVariable(step, step.getParameters().get(0));
			tempPridAndRid = new HashMap<>();
			tempPridAndRid.put("rid", resolveScenarioVariable(step, step.getParameters().get(1)));
			if (step.getParameters().size() == 3) {
				throw new RigInternalError(
						"CheckStatus expects 2 parameters (status, rid) or 4 (status, rid, rid2, any|all); got 3 in step: "
								+ step.getName());
			}
			if (step.getParameters().size() > 3) {
				tempPridAndRid.put("rid2", resolveScenarioVariable(step, step.getParameters().get(2)));
				_expectedRidProcessed = resolveScenarioVariable(step, step.getParameters().get(3));
			}
			checkStatus(_ridStatusParam, _expectedRidProcessed, step);
		}
	}

	public void checkStatus(String _ridStatusParam, String _expectedRidProcessed, Scenario.Step step)
			throws RigInternalError {
		long waitMs = getPacketStatusPollIntervalMs();
		long maxTotalWaitMs = getMaxPacketStatusWaitTimeMs();
		int statusSocketTimeoutMs = getPacketStatusSocketTimeoutMs();
		try {
			for (String rid : tempPridAndRid.values()) {
				int counter = 0;
				String ridStatus = "under processing";
				String lastPollError = null;
				boolean gotSuccessfulPoll = false;
				String stopReason = "max poll loops reached";
				int maxLoop = getPacketStatusMaxLoopCount();
				long startTime = System.currentTimeMillis();
				long deadline = startTime + maxTotalWaitMs;
				while (counter < maxLoop) {
					long now = System.currentTimeMillis();
					if (now >= deadline) {
						stopReason = "max wait time (" + (maxTotalWaitMs / 60000) + " min) reached";
						logger.info("Packet status polling stopped: " + stopReason + " for RID " + rid);
						break;
					}
					long remainingMs = deadline - now;
					long sleepMs = Math.min(waitMs, remainingMs);
					counter++;
					logger.info("Waiting for " + (sleepMs / 1000) + " sec to get packet processed | Loop: "
							+ counter + "/" + maxLoop);
					Thread.sleep(sleepMs);
					now = System.currentTimeMillis();
					if (now >= deadline) {
						stopReason = "max wait time (" + (maxTotalWaitMs / 60000) + " min) reached";
						logger.info("Packet status polling stopped: " + stopReason + " for RID " + rid);
						break;
					}
					int callTimeoutMs = (int) Math.min(statusSocketTimeoutMs, deadline - now);
					try {
						Response response = getRequestSilent(baseUrl + getRidStatusUrl + rid, step, callTimeoutMs);
						ridStatus = response.asString().toLowerCase();
						gotSuccessfulPoll = true;
						lastPollError = null;
						logger.info("RID Status: " + ridStatus + " | Loop Count: " + counter);
						if (!ridStatus.contains("under")) {
							stopReason = "status left under-processing";
							break;
						}
					} catch (Exception pollEx) {
						lastPollError = rootMessage(pollEx);
						logger.warn("Status poll failed for RID " + rid + " on loop " + counter + ": " + lastPollError);
						if (System.currentTimeMillis() >= deadline) {
							stopReason = "max wait time reached after poll error";
							break;
						}
					}
				}
				if (!gotSuccessfulPoll && lastPollError != null) {
					ridStatus = "unknown (poll error)";
				}
				String statusUrl = baseUrl + getRidStatusUrl + rid;
				try {
					long remainingForFinal = deadline - System.currentTimeMillis();
					if (remainingForFinal > 0) {
						int finalTimeoutMs = (int) Math.min(statusSocketTimeoutMs, remainingForFinal);
						Response finalResponse = getRequest(statusUrl, "Check rid status: " + rid, step,
								finalTimeoutMs);
						ridStatus = finalResponse.asString().toLowerCase();
						gotSuccessfulPoll = true;
						lastPollError = null;
					}
				} catch (Exception finalEx) {
					lastPollError = rootMessage(finalEx);
					logger.warn("Final status fetch failed for RID " + rid + ": " + lastPollError);
					DslReportLogUtil.reportRequestAndResponse("Check rid status: " + rid, null,
							addContextToUrl(statusUrl, step), null, "Error: " + lastPollError, true);
					if (!gotSuccessfulPoll) {
						ridStatus = "unknown (read timed out)";
					}
				}
				String message = buildPacketProcessingReport(rid, ridStatus, _ridStatusParam, startTime, counter,
						stopReason, lastPollError);
				logger.info(message);
				Reporter.log(message, true);
				ridStatusMap.put(rid, ridStatus);
			}
			if (ridStatusMap.size() == 1) {
				var entry = ridStatusMap.entrySet().iterator().next();
				if (!entry.getValue().contains(_ridStatusParam.toLowerCase())) {
					this.hasError = true;
					throw new RigInternalError("Failed at Packet Processing (RID: " + entry.getKey()
							+ ", status: " + entry.getValue() + ", expected: " + _ridStatusParam
							+ ") — see Packet Processing Report above");
				}
			} else if (ridStatusMap.size() > 1 && !_expectedRidProcessed.isEmpty()) {
				List<String> params = Arrays.asList("any", "all");
				String matchingParam = params.stream().filter(p -> p.equalsIgnoreCase(_expectedRidProcessed))
						.findFirst().orElseThrow(() -> new RigInternalError(
								"Parameter : " + _expectedRidProcessed + " not supported, only allowed are " + params));

				long packetProcessed = ridStatusMap.values().stream()
						.filter(v -> v.equalsIgnoreCase(_ridStatusParam.toLowerCase())).count();
				if (_expectedRidProcessed.equalsIgnoreCase("any") && packetProcessed != 1) {
					this.hasError = true;
					throw new RigInternalError("Failed at Packet Processing");
				} else if (_expectedRidProcessed.equalsIgnoreCase("all") && packetProcessed != ridStatusMap.size()) {
					this.hasError = true;
					throw new RigInternalError("Failed at Packet Processing");
				}
			}

		} catch (InterruptedException e) {
			logger.error("Failed due to thread sleep: " + e.getMessage());
			Thread.currentThread().interrupt();
			this.hasError = true;
			throw new RigInternalError("Packet status check interrupted");
		}

	}

	private static String rootMessage(Throwable t) {
		Throwable root = t;
		while (root.getCause() != null && root.getCause() != root) {
			root = root.getCause();
		}
		String msg = root.getMessage();
		return msg != null && !msg.isBlank() ? msg : root.getClass().getSimpleName();
	}

	private static String buildPacketProcessingReport(String rid, String ridStatus, String expectedStatus,
			long startTime, int pollCount, String stopReason, String lastError) {
		long totalTime = System.currentTimeMillis() - startTime;
		long timeInMinutes = totalTime / 60000;
		long timeInSeconds = (totalTime % 60000) / 1000;
		boolean statusMatches = ridStatus != null
				&& ridStatus.toLowerCase().contains(expectedStatus.toLowerCase());
		String statusColor = statusMatches ? "green" : "#c0392b";
		String displayStatus = ridStatus == null || ridStatus.isBlank() ? "UNKNOWN"
				: escapeHtml(ridStatus.toUpperCase());
		StringBuilder sb = new StringBuilder();
		sb.append("<div style='")
				.append("padding:8px;")
				.append("border-radius:6px;")
				.append("background-color:#eef6ff;")
				.append("border-left:5px solid #2f80ed;")
				.append("font-family:Arial;'>")
				.append("<b style='color:#2f80ed;'>Packet Processing Report</b><br>")
				.append("<span><b>RID:</b> ").append(escapeHtml(rid)).append("</span><br>")
				.append("<span><b>Status:</b> <span style='color:").append(statusColor).append(";'>")
				.append(displayStatus).append("</span></span><br>")
				.append("<span><b>Expected:</b> ").append(escapeHtml(expectedStatus.toUpperCase())).append("</span><br>")
				.append("<span><b>Time Taken:</b> ").append(timeInMinutes).append(" min ")
				.append(timeInSeconds).append(" sec</span><br>")
				.append("<span><b>Polls:</b> ").append(pollCount).append("</span><br>")
				.append("<span><b>Stop reason:</b> ").append(stopReason).append("</span>");
		if (lastError != null && !lastError.isBlank()) {
			sb.append("<br><span><b>Last error:</b> <span style='color:#c0392b;'>")
					.append(escapeHtml(lastError)).append("</span></span>");
		}
		sb.append("</div>");
		return sb.toString();
	}

	private static String escapeHtml(String text) {
		return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
	}

}
