package io.mosip.testrig.dslrig.ivv.orchestrator.util;

import org.json.JSONObject;

import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;

/**
 * Validates packet-creator run-cache warm/clear HTTP responses for DSL steps.
 */
public final class RunCacheStepSupport {

	private static final String STATUS_FAILED = "failed";

	private RunCacheStepSupport() {
	}

	public static void assertClearSucceeded(String body) throws RigInternalError {
		if (body == null || !"true".equals(body.trim())) {
			throw new RigInternalError("Clearing run cache failed: " + body);
		}
	}

	public static void assertWarmSucceeded(String body) throws RigInternalError {
		if (body == null || body.isBlank()) {
			throw new RigInternalError("Warming run cache failed: empty response body");
		}
		try {
			JSONObject summary = new JSONObject(body.trim());
			for (String key : summary.keySet()) {
				if (key.startsWith("auth")) {
					assertAuthStatus(summary, key);
				}
			}
		} catch (RigInternalError e) {
			throw e;
		} catch (Exception e) {
			throw new RigInternalError(
					"Warming run cache failed: invalid JSON response. " + body + " (" + e.getMessage() + ")");
		}
	}

	private static void assertAuthStatus(JSONObject summary, String field) throws RigInternalError {
		if (!summary.has(field)) {
			return;
		}
		if (STATUS_FAILED.equalsIgnoreCase(summary.optString(field))) {
			throw new RigInternalError(
					"Warming run cache failed: " + field + " could not be initialized. " + summary);
		}
	}
}
