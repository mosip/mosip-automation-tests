package io.mosip.testrig.dslrig.ivv.orchestrator;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

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
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

import io.mosip.kernel.core.util.StringUtils;

public class S3Adapter {
	public static Logger logger = Logger.getLogger(S3Adapter.class); 
	private AmazonS3 connection = null;

	private int maxRetry = 20;

	private int maxConnection = 200;

	private int retry = 0;

	private boolean useAccountAsBucketname = true;

	private static final String SEPARATOR = "/";
	private static final String MINIO_SIGNER = "MinioS3V4NoPort";

	private List<String> existingBuckets = new ArrayList<>();

	static {
		SignerFactory.registerSigner(MINIO_SIGNER, MinioS3Signer.class);
	}

	private AmazonS3 getConnection(String bucketName) {
		if (connection != null)
			return connection;

		logger.info("ConfigManager.getS3UserKey() :: "+dslConfigManager.getS3UserKey());
		logger.info("ConfigManager.getS3Host() :: "+dslConfigManager.getS3Host());
		logger.info("ConfigManager.getS3Region() :: "+dslConfigManager.getS3Region());
		logger.info("ConfigManager.getS3SecretKey() :: "+dslConfigManager.getS3SecretKey());
		try {
			AWSCredentials awsCredentials = new BasicAWSCredentials(dslConfigManager.getS3UserKey(),
					dslConfigManager.getS3SecretKey());
			ClientConfiguration configuration = new ClientConfiguration().withMaxConnections(maxConnection)
					.withMaxErrorRetry(maxRetry);
			configuration.setSignerOverride(MINIO_SIGNER);
			connection = AmazonS3ClientBuilder.standard()
					.withCredentials(new AWSStaticCredentialsProvider(awsCredentials)).enablePathStyleAccess()
					.withClientConfiguration(configuration)
					.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(
							normalizeUrl(dslConfigManager.getS3Host()), signingRegion(dslConfigManager.getS3Region())))
					.build();

			connection.doesBucketExistV2(bucketName);
			retry = 0;
		} catch (Exception e) {
			if (retry >= maxRetry) {

				retry = 0;
				connection = null;


			} else {
				connection = null;
				retry = retry + 1;

				getConnection(bucketName);
			}
		}
		return connection;
	}

	public boolean putObject(String account, final String container, String source, String process, String objectName, File file) {
		String finalObjectName = null;
		String bucketName = null;
	       logger.info("useAccountAsBucketname:: "+useAccountAsBucketname);
		if (useAccountAsBucketname) {
				finalObjectName = getName(container, source, process, objectName);
				bucketName = account;
		} else {
				finalObjectName = getName(source, process, objectName);
				bucketName = container;
		}
		logger.info("bucketName :: "+bucketName);
		AmazonS3 connection = getConnection(bucketName);
			if (!doesBucketExists(bucketName)) {
				connection.createBucket(bucketName);
				if (useAccountAsBucketname)
					existingBuckets.add(bucketName);
			}
			PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, finalObjectName, file);
			ObjectMetadata objectMetadata = new ObjectMetadata();
			objectMetadata.setHttpExpiresDate(new DateTime().plusDays(1).toDate());
			putObjectRequest.setMetadata(objectMetadata);
			connection.putObject(putObjectRequest);
			return true;
		}

	/**
	 * MinIO has no rename. Copy the object to the new key and delete the original.
	 */
	public void moveObject(String bucket, String fromKey, String toKey) {
		AmazonS3 client = getConnection(bucket);
		if (client == null) {
			throw new IllegalStateException("MinIO connection failed for bucket " + bucket);
		}
		if (!client.doesObjectExist(bucket, fromKey)) {
			throw new IllegalStateException("MinIO object not found: " + bucket + "/" + fromKey);
		}
		if (client.doesObjectExist(bucket, toKey)) {
			throw new IllegalStateException("MinIO target already exists: " + bucket + "/" + toKey);
		}
		client.copyObject(bucket, fromKey, bucket, toKey);
		client.deleteObject(bucket, fromKey);
	}

	private boolean doesBucketExists(String bucketName) {

		if (useAccountAsBucketname && existingBuckets.contains(bucketName))
			return true;


		else if (useAccountAsBucketname && !existingBuckets.contains(bucketName)) {
			boolean doesBucketExistsInObjectStore = connection.doesBucketExistV2(bucketName);
			if (doesBucketExistsInObjectStore)
				existingBuckets.add(bucketName);
			return doesBucketExistsInObjectStore;
		} else
			return connection.doesBucketExistV2(bucketName);
	}

	public static String getName(String container, String source, String process, String objectName) {
		String finalObjectName = "";
		if (StringUtils.isNotEmpty(container))
			finalObjectName = container + SEPARATOR;
		if (StringUtils.isNotEmpty(source))
			finalObjectName = finalObjectName + source + SEPARATOR;
		if (StringUtils.isNotEmpty(process))
			finalObjectName = finalObjectName + process + SEPARATOR;

		finalObjectName = finalObjectName + objectName;

		return finalObjectName;
	}

	public static String getName(String source, String process, String objectName) {
		String finalObjectName = "";
		if (StringUtils.isNotEmpty(source))
			finalObjectName = source + SEPARATOR;
		if (StringUtils.isNotEmpty(process))
			finalObjectName = finalObjectName + process + SEPARATOR;

		finalObjectName = finalObjectName + objectName;

		return finalObjectName;
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
		if (url == null) {
			return null;
		}
		String trimmed = url.trim();
		while (trimmed.endsWith("/")) {
			trimmed = trimmed.substring(0, trimmed.length() - 1);
		}
		return trimmed;
	}

}
