package io.mosip.testrig.dslrig.ivv.e2e.methods;

import java.util.HashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testrig.dslrig.ivv.orchestrator.PersonaDataManager;
import io.mosip.testrig.dslrig.ivv.orchestrator.dslConfigManager;
import io.restassured.response.Response;

public class GetUINByRid extends BaseTestCaseUtil implements StepInterface {

	private String getIdentityUrl = "/resident/uin/";
	static Logger logger = Logger.getLogger(GetUINByRid.class);
	Boolean isForChildPacket = false;

	static {
		if (dslConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	@SuppressWarnings("static-access")
	@Override
	public void run() throws RigInternalError {
		if (!step.getParameters().isEmpty() && !(step.getParameters().get(0).startsWith("$$"))) {
			isForChildPacket = Boolean.parseBoolean(step.getParameters().get(0));
			if (isForChildPacket && !StringUtils.isEmpty(step.getScenario().getRid_updateResident())) {
				HashMap<String, String> rid = new HashMap<>();
				rid.put("rid", step.getScenario().getRid_updateResident());
				getIdentity(rid);
			}
		} else if (!step.getParameters().isEmpty() && step.getParameters().get(0).startsWith("$$")) {
			String ridKey = step.getParameters().get(0);
			String rid = step.getScenario().getVariables().get(ridKey);
			if (rid == null || rid.isBlank()) {
				throw new RigInternalError("RID variable " + ridKey + " is not set");
			}
			HashMap<String, String> ridMap = new HashMap<>();
			ridMap.put("0", rid);
			getIdentity(ridMap);
		} else
			getIdentity(this.step.getScenario().getPridsAndRids());
	}

	public void getIdentity(HashMap<String, String> rids) throws RigInternalError {
		step.getScenario().getUinReqIds().clear();
		step.getScenario().getUinPersonaProp().clear();
		long waitMs;
		int maxLoop;
		try {
			waitMs = Long.parseLong(props.getProperty("waitTime", "5000"));
			maxLoop = Integer.parseInt(props.getProperty("loopCount", "80"));
			if (waitMs <= 0) {
				throw new NumberFormatException("waitTime must be positive");
			}
			if (maxLoop <= 0) {
				throw new NumberFormatException("loopCount must be positive");
			}
			String uinWaitOverride = dslConfigManager.getUinWaitTime();
			if (uinWaitOverride != null && !uinWaitOverride.isBlank()) {
				long uinWaitMs = Long.parseLong(uinWaitOverride.trim());
				if (uinWaitMs <= 0) {
					throw new NumberFormatException("uinWaitTime must be positive");
				}
				maxLoop = Math.max(1, (int) ((uinWaitMs + waitMs - 1) / waitMs));
			}
		} catch (NumberFormatException e) {
			throw new RigInternalError("Invalid UIN polling configuration (waitTime, loopCount, or uinWaitTime): "
					+ e.getMessage());
		}

		for (String rid : rids.values()) {
			if (rid == null || rid.isBlank()) {
				continue;
			}
			long startTime = System.currentTimeMillis();
			logger.info(this.getClass().getSimpleName() + " starts at..." + startTime + " MilliSec");
			String uin = null;
			Response response = null;
			for (int attempt = 1; attempt <= maxLoop; attempt++) {
				response = getRequest(baseUrl + getIdentityUrl + rid, "Get uin by rid: " + rid, step);
				uin = response != null ? response.asString() : null;
				if (isValidUinResponse(uin)) {
					logger.info("UIN retrieved for RID " + rid + " on attempt " + attempt);
					break;
				}
				logger.info("UIN not ready for RID " + rid + " (attempt " + attempt + "/" + maxLoop + "), waiting "
						+ (waitMs / 1000) + "s");
				try {
					Thread.sleep(waitMs);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					throw new RigInternalError("Interrupted while waiting for UIN for RID: " + rid);
				}
			}
			long stopTime = System.currentTimeMillis();
			logger.info("Time taken to execute " + this.getClass().getSimpleName() + ": " + (stopTime - startTime)
					+ " MilliSec");
			if (response != null) {
				logger.info("Response from get Identity for RID: " + rid + " " + response.asString());
			}

			if (!isValidUinResponse(uin)) {
				logger.error("Issue while fetching identity for RID: " + rid + " Response: "
						+ (response != null ? response.toString() : "null"));
				this.hasError = true;
				throw new RigInternalError(
						"Not able to fetch UIN for RID: " + rid + " after " + maxLoop + " attempts (packet may be PROCESSED before ID repo has UIN)");
			}

			if (step.getOutVarName() != null) {
				step.getScenario().getVariables().put(step.getOutVarName(), uin);
			} else if (isForChildPacket) {
				step.getScenario().setUin_updateResident(uin);
			} else {
				step.getScenario().getUinReqIds().put(uin, null);
				if (!step.getScenario().getUinPersonaProp().containsKey(uin)) {
					step.getScenario().getUinPersonaProp().put(uin, step.getScenario().getRidPersonaPath().get(rid));
				}
			}
			PersonaDataManager.setVariableValue(step.getScenario().getId(), "UIN", uin);
		}
	}

	private static boolean isValidUinResponse(String uin) {
		if (StringUtils.isEmpty(uin)) {
			return false;
		}
		String trimmed = uin.trim();
		return !trimmed.contains("errorCode") && !trimmed.startsWith("{") && !trimmed.startsWith("[");
	}

}
