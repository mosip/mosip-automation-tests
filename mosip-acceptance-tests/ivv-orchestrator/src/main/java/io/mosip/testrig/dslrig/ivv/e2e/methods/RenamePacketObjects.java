package io.mosip.testrig.dslrig.ivv.e2e.methods;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.testng.Reporter;

import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.e2e.constant.E2EConstants;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testrig.dslrig.ivv.orchestrator.S3Adapter;
import io.mosip.testrig.dslrig.ivv.orchestrator.dslConfigManager;

/**
 * Renames the three packet-manager sub-packet objects for one registration.
 * MinIO has no rename, so {@link S3Adapter} copies each object to a {@code .hold} key and deletes the old key.
 * The {@code .hold} names are kept. When hold seconds is set, the step waits with those names.
 */
public class RenamePacketObjects extends BaseTestCaseUtil implements StepInterface {

	private static final Logger logger = Logger.getLogger(RenamePacketObjects.class);
	private static final String HOLD_SUFFIX = ".hold";
	private static final String[] OBJECT_SUFFIXES = { "_optional", "_id", "_evidence" };

	static {
		if (dslConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	@Override
	public void run() throws RigInternalError {
		if (step.getParameters() == null || step.getParameters().size() < 3) {
			this.hasError = true;
			throw new RigInternalError(
					"rename packet objects needs registration ID, packet process, and object action (rename)");
		}

		String rid = resolveScenarioVariable(step.getParameters().get(0));
		String process = step.getParameters().get(1) == null ? "" : step.getParameters().get(1).trim();
		String action = step.getParameters().get(2) == null ? "" : step.getParameters().get(2).trim();
		if (rid == null || rid.isBlank() || rid.startsWith("$$")) {
			this.hasError = true;
			throw new RigInternalError("Registration ID is missing for rename packet objects: " + rid);
		}
		if (process.isBlank()) {
			this.hasError = true;
			throw new RigInternalError("Packet process is missing for rename packet objects");
		}

		int holdSeconds = 0;
		if (step.getParameters().size() >= 4 && step.getParameters().get(3) != null
				&& !step.getParameters().get(3).isBlank()) {
			try {
				holdSeconds = Integer.parseInt(step.getParameters().get(3).trim());
			} catch (NumberFormatException e) {
				this.hasError = true;
				throw new RigInternalError("Invalid hold seconds: " + step.getParameters().get(3));
			}
		}

		String bucket = required("minio-bucket", dslConfigManager.getMinioBucket());
		String url = required("s3-host", dslConfigManager.getS3Host());
		String accessKey = required("s3-user-key", dslConfigManager.getS3UserKey());
		required("s3-user-secret", dslConfigManager.getS3SecretKey());
		report("MinIO " + url + " user " + accessKey + " bucket " + bucket);

		S3Adapter minio = new S3Adapter();
		RigInternalError failure = null;
		try {
			if ("hold".equalsIgnoreCase(action) || "rename".equalsIgnoreCase(action)) {
				for (String suffix : OBJECT_SUFFIXES) {
					String objectName = rid + suffix;
					String from = S3Adapter.getName(rid, E2EConstants.SOURCE, process, objectName);
					String to = S3Adapter.getName(rid, E2EConstants.SOURCE, process, objectName + HOLD_SUFFIX);
					minio.moveObject(bucket, from, to);
					report("Renamed " + bucket + "/" + from + " to " + suffix + HOLD_SUFFIX);
				}
				if (holdSeconds > 0) {
					sleepWithCountdown(TIME_IN_MILLISEC * holdSeconds, "MinIO packet objects renamed");
				}
			} else {
				this.hasError = true;
				throw new RigInternalError("object action must be rename, got: " + action);
			}
		} catch (RigInternalError e) {
			failure = e;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			failure = new RigInternalError("Interrupted while packet objects were renamed in MinIO");
		} catch (RuntimeException e) {
			String message = e.getMessage();
			failure = new RigInternalError(message != null && message.startsWith("MinIO ") ? message
					: "MinIO rename failed: " + message);
		}

		if (failure != null) {
			this.hasError = true;
			throw failure;
		}
	}

	private static String required(String name, String value) throws RigInternalError {
		if (value == null || value.isBlank()) {
			throw new RigInternalError("Missing dsl.properties value: " + name);
		}
		return value.trim();
	}

	private static void report(String message) {
		logger.info(message);
		Reporter.log(message, true);
	}
}
