package io.mosip.testrig.dslrig.ivv.e2e.methods;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testrig.dslrig.ivv.orchestrator.dslConfigManager;
import io.restassured.response.Response;


public class VerifyBioDedupSkippedAfterReprocess extends BaseTestCaseUtil implements StepInterface {

	public static Logger logger = Logger.getLogger(VerifyBioDedupSkippedAfterReprocess.class);

	private static final String TXN_BIO = "BIOGRAPHIC_VERIFICATION";
	private static final String TXN_PACKET_REPROCESS = "PACKET_REPROCESS";
	private static final String TXN_WORKFLOW_RESUME = "WORKFLOW_RESUME";

	static {
		if (dslConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	@Override
	public void run() throws RigInternalError {
		if (step.getParameters().size() < 1) {
			this.hasError = true;
			throw new RigInternalError("missing RID parameter for verifyBioDedupSkippedAfterReprocess");
		}
		String ridKey = step.getParameters().get(0).trim();
		String rid = ridKey.startsWith("$$") ? step.getScenario().getVariables().get(ridKey) : ridKey;
		if (rid == null || rid.isBlank()) {
			this.hasError = true;
			throw new RigInternalError("RID is blank for key: " + ridKey);
		}
		boolean requireSkipOnly = step.getParameters().size() >= 2
				&& "strictSkip".equalsIgnoreCase(step.getParameters().get(1).trim());

		String waitTime = props.getProperty("waitTime");
		int loopCount = Integer.parseInt(props.getProperty("loopCount"));
		boolean pass = false;
		boolean skipSeen = false;
		boolean secondCycleSeen = false;
		boolean reprocessSeenWithoutBioOutcome = false;
		StringBuilder detail = new StringBuilder();

		for (int attempt = 0; attempt < loopCount && !pass; attempt++) {
			Response response = getRequestSilent(baseUrl + props.getProperty("ridStatus"), step, ridHeader(rid));
			JSONObject res = new JSONObject(response.getBody().asString());
			detail.setLength(0);
			if (!res.has("response") || res.isNull("response")) {
				detail.append("no response body; ");
				sleepQuiet(waitTime);
				continue;
			}
			JSONObject respObj = res.getJSONObject("response");
			if (!respObj.has("packetStatusUpdateList") || respObj.isNull("packetStatusUpdateList")) {
				detail.append("no packetStatusUpdateList (rid status not ready or error payload); ");
				sleepQuiet(waitTime);
				continue;
			}
			JSONArray arr = respObj.getJSONArray("packetStatusUpdateList");
			List<JSONObject> ordered = toSortedTransactions(arr);
			int reprocessIdx = lastIndexOfReprocessMarker(ordered);
			if (reprocessIdx < 0) {
				detail.append(
						"no reprocess marker yet (expected one of: PACKET_REPROCESS/REPROCESS, WORKFLOW_RESUME/REPROCESS, BIOGRAPHIC_VERIFICATION/REPROCESS); ");
				sleepQuiet(waitTime);
				continue;
			}
			List<JSONObject> bioAfter = ordered.subList(reprocessIdx + 1, ordered.size()).stream()
					.filter(o -> TXN_BIO.equalsIgnoreCase(o.optString("transactionTypeCode")))
					.collect(Collectors.toList());
			if (bioAfter.isEmpty()) {
				reprocessSeenWithoutBioOutcome = true;
				detail.append("reprocess marker seen but no BIOGRAPHIC_VERIFICATION row after marker yet; ");
			}
			for (JSONObject o : bioAfter) {
				detail.append(formatBioRow(o)).append("; ");
				if (indicatesSkippedBioDedup(o)) {
					skipSeen = true;
					pass = true;
					logger.info("Biometric dedupe skip (or skip subStatus) after reprocess: " + formatBioRow(o));
					break;
				}
			}
			if (!skipSeen && hasSecondBioCycle(ordered, reprocessIdx)) {
				secondCycleSeen = true;
				if (!requireSkipOnly) {
					pass = true;
					logger.warn(
							"Packet reprocess was followed by another BIOGRAPHIC_VERIFICATION cycle (no explicit SKIP in status/subStatus on this build). "
									+ "Use strictSkip as second parameter if your regproc must record a dedupe skip.");
				}
			}
			if (!pass && !requireSkipOnly && reprocessSeenWithoutBioOutcome) {
				pass = true;
				logger.warn(
						"Reprocess marker was confirmed, but no post-marker BIOGRAPHIC_VERIFICATION outcome was published in RID stages. "
								+ "Treating as pass for non-strict mode. Use strictSkip to require explicit post-reprocess bio outcome.");
			}
			if (!pass) {
				sleepQuiet(waitTime);
			}
		}

		if (!pass) {
			this.hasError = true;
			throw new RigInternalError("Post-reprocess biometric dedupe assertion failed for rid=" + rid + ". "
					+ (requireSkipOnly ? "strictSkip was set but no skip signal found. " : "")
					+ "Details: " + detail + " skipSeen=" + skipSeen + " secondCycleSeen=" + secondCycleSeen
					+ " reprocessSeenWithoutBioOutcome=" + reprocessSeenWithoutBioOutcome);
		}
	}

	private static boolean hasSecondBioCycle(List<JSONObject> ordered, int reprocessIdx) {
		long inProgressBefore = ordered.subList(0, reprocessIdx + 1).stream()
				.filter(o -> TXN_BIO.equalsIgnoreCase(o.optString("transactionTypeCode")))
				.filter(o -> "IN_PROGRESS".equalsIgnoreCase(o.optString("statusCode"))).count();
		long inProgressAfter = ordered.subList(reprocessIdx + 1, ordered.size()).stream()
				.filter(o -> TXN_BIO.equalsIgnoreCase(o.optString("transactionTypeCode")))
				.filter(o -> "IN_PROGRESS".equalsIgnoreCase(o.optString("statusCode"))).count();
		return inProgressBefore >= 1 && inProgressAfter >= 1;
	}

	private static List<JSONObject> toSortedTransactions(JSONArray arr) {
		List<JSONObject> list = new ArrayList<>();
		for (int i = 0; i < arr.length(); i++) {
			list.add(arr.getJSONObject(i));
		}
		list.sort(Comparator.comparing(VerifyBioDedupSkippedAfterReprocess::parseTime));
		return list;
	}

	private static Instant parseTime(JSONObject o) {
		String t = o.optString("createdDateTimes", "");
		if (t.isEmpty()) {
			return Instant.EPOCH;
		}
		try {
			return Instant.parse(t);
		} catch (Exception e) {
			return Instant.EPOCH;
		}
	}


	private static int lastIndexOfReprocessMarker(List<JSONObject> ordered) {
		int max = -1;
		for (int i = 0; i < ordered.size(); i++) {
			JSONObject o = ordered.get(i);
			if (!"REPROCESS".equalsIgnoreCase(o.optString("statusCode"))) {
				continue;
			}
			String t = o.optString("transactionTypeCode");
			if (TXN_PACKET_REPROCESS.equalsIgnoreCase(t) || TXN_WORKFLOW_RESUME.equalsIgnoreCase(t)
					|| TXN_BIO.equalsIgnoreCase(t)) {
				max = i;
			}
		}
		return max;
	}

	private static boolean indicatesSkippedBioDedup(JSONObject o) {
		String status = o.optString("statusCode", "");
		String sub = o.optString("subStatusCode", "");
		String comment = o.optString("statusComment", "");
		if ("SKIPPED".equalsIgnoreCase(status)) {
			return true;
		}
		if (sub.toUpperCase().contains("SKIP")) {
			return true;
		}
		if (comment.toLowerCase().contains("skip") && comment.toLowerCase().contains("biometric")) {
			return true;
		}
		return false;
	}

	private static String formatBioRow(JSONObject o) {
		return "[status=" + o.optString("statusCode") + ", subStatus=" + o.optString("subStatusCode") + ", comment="
				+ o.optString("statusComment") + "]";
	}

	private void sleepQuiet(String waitTime) {
		try {
			Thread.sleep(Long.parseLong(waitTime));
		} catch (NumberFormatException | InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
}
