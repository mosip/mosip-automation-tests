package io.mosip.testrig.dslrig.ivv.e2e.methods;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.testng.Reporter;

import java.net.URI;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Request;
import com.amazonaws.SignableRequest;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.SignerFactory;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.internal.AWSS3V4Signer;

import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testrig.dslrig.ivv.orchestrator.dslConfigManager;

/**
 * Renames the three packet-manager sub-packet objects for one registration.
 * MinIO has no rename, so each object is copied to a {@code .hold} key and the old key is deleted.
 * The {@code .hold} names are kept. When hold seconds is set, the step waits with those names.
 */
public class RenamePacketObjects extends BaseTestCaseUtil implements StepInterface {

	private static final Logger logger = Logger.getLogger(RenamePacketObjects.class);
	private static final String HOLD_SUFFIX = ".hold";
	private static final String PACKET_SOURCE = "REGISTRATION_CLIENT";
	private static final String MINIO_SIGNER = "MinioS3V4NoPort";
	private static final String[] OBJECT_SUFFIXES = { "_optional", "_id", "_evidence" };

	static {
		SignerFactory.registerSigner(MINIO_SIGNER, MinioS3Signer.class);
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
		String url = normalizeUrl(required("s3-host", dslConfigManager.getS3Host()));
		String accessKey = required("s3-user-key", dslConfigManager.getS3UserKey());
		String secretKey = required("s3-user-secret", dslConfigManager.getS3SecretKey());
		String region = signingRegion(dslConfigManager.getS3Region());
		report("MinIO " + url + " user " + accessKey + " bucket " + bucket);

		ClientConfiguration configuration = new ClientConfiguration().withMaxErrorRetry(3);
		configuration.setSignerOverride(MINIO_SIGNER);
		AmazonS3 client = AmazonS3ClientBuilder.standard()
				.withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)))
				.enablePathStyleAccess()
				.withClientConfiguration(configuration)
				.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(url, region))
				.build();

		RigInternalError failure = null;
		try {
			if ("hold".equalsIgnoreCase(action) || "rename".equalsIgnoreCase(action)) {
				for (String suffix : OBJECT_SUFFIXES) {
					String from = objectKey(rid, process, suffix);
					String to = objectKey(rid, process, suffix + HOLD_SUFFIX);
					move(client, bucket, from, to);
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
			failure = new RigInternalError("MinIO rename failed: " + e.getMessage());
		} finally {
			client.shutdown();
		}

		if (failure != null) {
			this.hasError = true;
			throw failure;
		}
	}

	private static void move(AmazonS3 client, String bucket, String fromKey, String toKey) throws RigInternalError {
		if (!client.doesObjectExist(bucket, fromKey)) {
			throw new RigInternalError("MinIO object not found: " + bucket + "/" + fromKey);
		}
		if (client.doesObjectExist(bucket, toKey)) {
			throw new RigInternalError("MinIO target already exists: " + bucket + "/" + toKey);
		}
		client.copyObject(bucket, fromKey, bucket, toKey);
		client.deleteObject(bucket, fromKey);
	}

	private static String objectKey(String rid, String process, String suffix) {
		return rid + "/" + PACKET_SOURCE + "/" + process + "/" + rid + suffix;
	}

	private static String required(String name, String value) throws RigInternalError {
		if (value == null || value.isBlank()) {
			throw new RigInternalError("Missing dsl.properties value: " + name);
		}
		return value.trim();
	}

	/**
	 * qadraft publishes MinIO through nginx on port 9000. Nginx removes that port from the Host
	 * header before MinIO checks the signature, and the MinIO console signs the hostname alone.
	 * The AWS SDK signs {@code host:9000}. For HTTPS on a non-443 port, sign the hostname only and
	 * still connect to the original port.
	 */
	public static class MinioS3Signer extends AWSS3V4Signer {
		@Override
		public void sign(SignableRequest<?> request, AWSCredentials credentials) {
			URI endpoint = request.getEndpoint();
			if (!(request instanceof Request) || !signWithoutPort(endpoint)) {
				super.sign(request, credentials);
				return;
			}
			Request<?> mutable = (Request<?>) request;
			URI signingEndpoint = URI.create(endpoint.getScheme() + "://" + endpoint.getHost());
			mutable.setEndpoint(signingEndpoint);
			try {
				super.sign(request, credentials);
			} finally {
				mutable.setEndpoint(endpoint);
			}
		}

		private static boolean signWithoutPort(URI endpoint) {
			return endpoint != null && "https".equalsIgnoreCase(endpoint.getScheme()) && endpoint.getPort() > 0
					&& endpoint.getPort() != 443;
		}
	}

	private static String signingRegion(String region) {
		if (region == null || region.isBlank() || "null".equalsIgnoreCase(region.trim())) {
			return "us-east-1";
		}
		return region.trim();
	}

	private static String normalizeUrl(String url) {
		String trimmed = url.trim();
		while (trimmed.endsWith("/")) {
			trimmed = trimmed.substring(0, trimmed.length() - 1);
		}
		return trimmed;
	}

	private static void report(String message) {
		logger.info(message);
		Reporter.log(message, true);
	}
}
