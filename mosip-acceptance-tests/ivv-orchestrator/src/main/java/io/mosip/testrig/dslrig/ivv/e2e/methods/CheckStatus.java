package io.mosip.testrig.dslrig.ivv.e2e.methods;

import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.testng.Reporter;

import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.dtos.Scenario;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testrig.dslrig.ivv.orchestrator.DslReportLogUtil;
import io.mosip.testrig.dslrig.ivv.orchestrator.ScenarioParkScheduler;
import io.mosip.testrig.dslrig.ivv.orchestrator.ScenarioYieldException;
import io.mosip.testrig.dslrig.ivv.orchestrator.dslConfigManager;
import io.restassured.response.Response;

public class CheckStatus extends BaseTestCaseUtil implements StepInterface {
	/** Some packets need well over the typical ~2 min; never give up on a packet sooner than this. */
	private static final long MIN_PACKET_STATUS_WAIT_MS = 7 * 60 * 1000L;
	/** Poll progress per step, kept while the scenario is parked between polls and the step is re-run. */
	private static final Map<Scenario.Step, PollState> POLL_STATES = Collections
			.synchronizedMap(new IdentityHashMap<>());

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

	/**
	 * Polls every RID until it leaves under-processing, {@code loopCount} polls are used, or the
	 * wall-clock cap passes. On a park worker the scenario is parked between polls (the step re-runs
	 * and continues from {@link #POLL_STATES}) instead of holding the worker in {@code Thread.sleep}.
	 */
	public void checkStatus(String _ridStatusParam, String _expectedRidProcessed, Scenario.Step step)
			throws RigInternalError {
		long waitMs = Long.parseLong(props.getProperty("waitTime"));
		long maxTotalWaitMs = Math.max(getMaxPacketStatusWaitTimeMs(), MIN_PACKET_STATUS_WAIT_MS);
		int statusSocketTimeoutMs = getPacketStatusSocketTimeoutMs();
		int maxLoop = Math.max(Integer.parseInt(props.getProperty("loopCount")),
				(int) ((MIN_PACKET_STATUS_WAIT_MS + waitMs - 1) / waitMs));
		boolean parkBetweenPolls = dslConfigManager.isParkResumeEnabled()
				&& ScenarioParkScheduler.isRunningOnWorker();
		PollState poll = POLL_STATES.computeIfAbsent(step,
				s -> new PollState(tempPridAndRid.values(), maxTotalWaitMs));
		boolean parked = false;
		try {
			while (poll.hasPending()) {
				if (!poll.pollDue) {
					long now = System.currentTimeMillis();
					if (now >= poll.deadline) {
						poll.stopPending("max wait time (" + (maxTotalWaitMs / 60000) + " min) reached");
						break;
					}
					long sleepMs = Math.min(waitMs, poll.deadline - now);
					logger.info("Waiting for " + (sleepMs / 1000) + " sec to get packet processed | Loop: "
							+ (poll.maxCounter() + 1) + "/" + maxLoop);
					poll.pollDue = true;
					if (parkBetweenPolls) {
						parked = true;
						throw ScenarioYieldException.repeatStep(sleepMs);
					}
					Thread.sleep(sleepMs);
				}
				poll.pollDue = false;
				pollPendingOnce(poll, maxLoop, maxTotalWaitMs, statusSocketTimeoutMs, step);
			}

			for (RidPoll rp : poll.rids.values()) {
				reportFinalStatus(rp, poll, _ridStatusParam, statusSocketTimeoutMs, step);
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
		} finally {
			if (!parked) {
				POLL_STATES.remove(step);
			}
		}

	}

	private void pollPendingOnce(PollState poll, int maxLoop, long maxTotalWaitMs, int statusSocketTimeoutMs,
			Scenario.Step step) {
		for (RidPoll rp : poll.rids.values()) {
			if (rp.done) {
				continue;
			}
			long now = System.currentTimeMillis();
			if (now >= poll.deadline) {
				rp.stop("max wait time (" + (maxTotalWaitMs / 60000) + " min) reached");
				logger.info("Packet status polling stopped: " + rp.stopReason + " for RID " + rp.rid);
				continue;
			}
			rp.counter++;
			int callTimeoutMs = (int) Math.min(statusSocketTimeoutMs, poll.deadline - now);
			try {
				Response response = getRequestSilent(baseUrl + getRidStatusUrl + rp.rid, step, callTimeoutMs);
				rp.ridStatus = response.asString().toLowerCase();
				rp.gotSuccessfulPoll = true;
				rp.lastPollError = null;
				logger.info("RID Status: " + rp.ridStatus + " | Loop Count: " + rp.counter);
				if (isTerminalPacketStatus(rp.ridStatus)) {
					rp.stop("status left under-processing");
					continue;
				}
				if (!rp.ridStatus.contains("under")) {
					logger.warn("Ignoring non-terminal status body for RID " + rp.rid + " on loop " + rp.counter
							+ ": " + rp.ridStatus);
				}
			} catch (Exception pollEx) {
				rp.lastPollError = rootMessage(pollEx);
				logger.warn("Status poll failed for RID " + rp.rid + " on loop " + rp.counter + ": "
						+ rp.lastPollError);
				if (System.currentTimeMillis() >= poll.deadline) {
					rp.stop("max wait time reached after poll error");
					continue;
				}
			}
			if (rp.counter >= maxLoop) {
				rp.done = true;
			}
		}
	}

	private void reportFinalStatus(RidPoll rp, PollState poll, String _ridStatusParam, int statusSocketTimeoutMs,
			Scenario.Step step) {
		if (!rp.gotSuccessfulPoll && rp.lastPollError != null) {
			rp.ridStatus = "unknown (poll error)";
		}
		String statusUrl = baseUrl + getRidStatusUrl + rp.rid;
		try {
			long remainingForFinal = poll.deadline - System.currentTimeMillis();
			if (remainingForFinal > 0) {
				int finalTimeoutMs = (int) Math.min(statusSocketTimeoutMs, remainingForFinal);
				Response finalResponse = getRequest(statusUrl, "Check rid status: " + rp.rid, step, finalTimeoutMs);
				rp.ridStatus = finalResponse.asString().toLowerCase();
				rp.gotSuccessfulPoll = true;
				rp.lastPollError = null;
			}
		} catch (Exception finalEx) {
			rp.lastPollError = rootMessage(finalEx);
			logger.warn("Final status fetch failed for RID " + rp.rid + ": " + rp.lastPollError);
			DslReportLogUtil.reportRequestAndResponse("Check rid status: " + rp.rid, null,
					addContextToUrl(statusUrl, step), null, "Error: " + rp.lastPollError, true);
			if (!rp.gotSuccessfulPoll) {
				rp.ridStatus = "unknown (read timed out)";
			}
		}
		String message = buildPacketProcessingReport(rp.rid, rp.ridStatus, _ridStatusParam, poll.startTime,
				rp.counter, rp.stopReason, rp.lastPollError);
		logger.info(message);
		Reporter.log(message, true);
		ridStatusMap.put(rp.rid, rp.ridStatus);
	}

	private static final class PollState {
		final long startTime = System.currentTimeMillis();
		final long deadline;
		final Map<String, RidPoll> rids = new LinkedHashMap<>();
		boolean pollDue;

		PollState(Collection<String> ridValues, long maxTotalWaitMs) {
			this.deadline = startTime + maxTotalWaitMs;
			for (String rid : ridValues) {
				rids.putIfAbsent(rid, new RidPoll(rid));
			}
		}

		boolean hasPending() {
			return rids.values().stream().anyMatch(r -> !r.done);
		}

		int maxCounter() {
			return rids.values().stream().mapToInt(r -> r.counter).max().orElse(0);
		}

		void stopPending(String reason) {
			for (RidPoll rp : rids.values()) {
				if (!rp.done) {
					rp.stop(reason);
					logger.info("Packet status polling stopped: " + reason + " for RID " + rp.rid);
				}
			}
		}
	}

	private static final class RidPoll {
		final String rid;
		int counter;
		String ridStatus = "under processing";
		String lastPollError;
		boolean gotSuccessfulPoll;
		String stopReason = "max poll loops reached";
		boolean done;

		RidPoll(String rid) {
			this.rid = rid;
		}

		void stop(String reason) {
			this.stopReason = reason;
			this.done = true;
		}
	}

	/**
	 * Keep polling through under-processing and through packet-creator error bodies
	 * such as {@code {Failed}}. Only a real packet status (processed, rejected,
	 * reregister, failed) ends the wait before {@code loopCount}.
	 */
	private static boolean isTerminalPacketStatus(String ridStatus) {
		if (ridStatus == null || ridStatus.isBlank()) {
			return false;
		}
		String status = ridStatus.trim().toLowerCase();
		if (status.contains("under")) {
			return false;
		}
		if (status.equals("{failed}") || status.startsWith("{") || status.startsWith("[")
				|| status.contains("exception") || status.contains("timed out")
				|| status.contains("cannot invoke") || status.contains("error")) {
			return false;
		}
		return status.contains("processed") || status.contains("rejected") || status.contains("reregister")
				|| status.equals("failed");
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
		String displayStatus = ridStatus == null || ridStatus.isBlank() ? "UNKNOWN" : ridStatus.toUpperCase();
		StringBuilder sb = new StringBuilder();
		sb.append("<div style='")
				.append("padding:8px;")
				.append("border-radius:6px;")
				.append("background-color:#eef6ff;")
				.append("border-left:5px solid #2f80ed;")
				.append("font-family:Arial;'>")
				.append("<b style='color:#2f80ed;'>Packet Processing Report</b><br>")
				.append("<span><b>RID:</b> ").append(rid).append("</span><br>")
				.append("<span><b>Status:</b> <span style='color:").append(statusColor).append(";'>")
				.append(displayStatus).append("</span></span><br>")
				.append("<span><b>Expected:</b> ").append(expectedStatus.toUpperCase()).append("</span><br>")
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
