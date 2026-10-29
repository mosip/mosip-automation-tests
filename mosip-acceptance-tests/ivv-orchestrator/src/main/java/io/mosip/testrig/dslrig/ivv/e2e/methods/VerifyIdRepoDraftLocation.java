package io.mosip.testrig.dslrig.ivv.e2e.methods;

import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Reporter;

import io.mosip.testrig.apirig.utils.ConfigManager;
import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testrig.dslrig.ivv.orchestrator.util.IdRepoDraftLocationChecker;
import io.restassured.response.Response;

/**
 * Confirms demographic and biometric objects are in the idrepo draft location, published
 * after processing, or removed after rejection.
 */
public class VerifyIdRepoDraftLocation extends BaseTestCaseUtil implements StepInterface {

	private static final Logger logger = Logger.getLogger(VerifyIdRepoDraftLocation.class);

	private static final String TXN_CREATE_DRAFT = "CREATE_DRAFT";
	/** Stages that run after CREATE_DRAFT; seeing any of them means the draft point has passed. */
	private static final Set<String> STAGES_AFTER_CREATE_DRAFT = Set.of("BIOMETRIC_AUTHENTICATION",
			"DEMOGRAPHIC_VERIFICATION", "BIOGRAPHIC_VERIFICATION", "MANUAL_ADJUDICATION", "VERIFICATION",
			"UIN_GENERATOR");

	static {
		if (Boolean.TRUE.equals(ConfigManager.IsDebugEnabled()))
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	@Override
	public void run() throws RigInternalError {
		if (step.getParameters() == null || step.getParameters().size() < 2) {
			this.hasError = true;
			throw new RigInternalError(
					"verify id repo draft location needs registration ID and location phase (draft, published, or rejected)");
		}
		String rid = resolveScenarioVariable(step.getParameters().get(0));
		String phase = step.getParameters().get(1) == null ? "" : step.getParameters().get(1).trim();
		if (rid == null || rid.isBlank() || rid.startsWith("$$")) {
			this.hasError = true;
			throw new RigInternalError("Registration ID is missing for idrepo draft check: " + rid);
		}
		try {
			IdRepoDraftLocationChecker.Phase parsedPhase = IdRepoDraftLocationChecker.parsePhase(phase);
			if (parsedPhase == IdRepoDraftLocationChecker.Phase.DRAFT) {
				waitForCreateDraft(rid);
			}
			IdRepoDraftLocationChecker.verify(rid, parsedPhase);
		} catch (RigInternalError e) {
			this.hasError = true;
			logger.error(e.getMessage());
			Reporter.log(e.getMessage(), true);
			throw e;
		}
	}

	/**
	 * Under load CREATE_DRAFT can run several minutes after upload, so the MinIO draft check only
	 * starts once the RID stages show the draft was created.
	 */
	private void waitForCreateDraft(String rid) throws RigInternalError {
		long waitTimeMs = Long.parseLong(props.getProperty("waitTime").trim());
		int loopCount = Integer.parseInt(props.getProperty("loopCount").trim());
		String lastStage = "no RID stages yet";

		for (int attempt = 0; attempt < loopCount; attempt++) {
			JSONArray stages = fetchStages(rid);
			if (stages != null) {
				JSONObject createDraft = null;
				String laterStage = null;
				for (int i = 0; i < stages.length(); i++) {
					JSONObject row = stages.getJSONObject(i);
					String txn = row.optString("transactionTypeCode").toUpperCase();
					if (TXN_CREATE_DRAFT.equals(txn)) {
						createDraft = row;
					} else if (laterStage == null && STAGES_AFTER_CREATE_DRAFT.contains(txn)) {
						laterStage = txn + " " + row.optString("statusCode");
					}
				}
				if (createDraft != null) {
					String status = createDraft.optString("statusCode");
					if ("FAILED".equalsIgnoreCase(status) || "ERROR".equalsIgnoreCase(status)) {
						throw new RigInternalError("CREATE_DRAFT " + status + " for RID " + rid + " ("
								+ createDraft.optString("subStatusCode") + ": "
								+ createDraft.optString("statusComment") + "), so no idrepo draft was created");
					}
					logger.info("CREATE_DRAFT " + status + " seen for RID " + rid);
					return;
				}
				if (laterStage != null) {
					logger.info(laterStage + " seen for RID " + rid + " without a CREATE_DRAFT row");
					return;
				}
				if (stages.length() > 0) {
					JSONObject last = stages.getJSONObject(stages.length() - 1);
					lastStage = last.optString("transactionTypeCode") + " " + last.optString("statusCode");
				}
			}
			try {
				Thread.sleep(waitTimeMs);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new RigInternalError("Interrupted while waiting for CREATE_DRAFT of RID " + rid);
			}
		}
		throw new RigInternalError("RID " + rid + " did not reach CREATE_DRAFT within "
				+ (waitTimeMs * loopCount / 1000) + "s (last stage: " + lastStage + ")");
	}

	private JSONArray fetchStages(String rid) {
		try {
			Response response = getRequestSilent(baseUrl + props.getProperty("ridStatus") + rid, step);
			String body = response != null ? response.getBody().asString() : null;
			if (body == null || body.isBlank()) {
				return null;
			}
			JSONObject res = new JSONObject(body);
			JSONObject responseObj = res.optJSONObject("response");
			return responseObj == null ? null : responseObj.optJSONArray("packetStatusUpdateList");
		} catch (JSONException e) {
			logger.warn("RID stage response for " + rid + " was not JSON: " + e.getMessage());
			return null;
		} catch (Exception e) {
			logger.warn("RID stage poll failed for " + rid + ": " + e.getMessage());
			return null;
		}
	}
}
