package io.mosip.testrig.dslrig.ivv.orchestrator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import io.mosip.kernel.core.util.StringUtils;

public class S3Adapter {
	public static Logger logger = Logger.getLogger(S3Adapter.class); 
	private AmazonS3 connection = null;

	private int maxRetry = 2;

	private int maxConnection = 200;

	private int retry = 0;

	private boolean useAccountAsBucketname = true;

	private static final String SEPARATOR = "/";

	private List<String> existingBuckets = new ArrayList<>();

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
			connection = AmazonS3ClientBuilder.standard()
					.withCredentials(new AWSStaticCredentialsProvider(awsCredentials)).enablePathStyleAccess()
					.withClientConfiguration(
							new ClientConfiguration().withMaxConnections(maxConnection).withMaxErrorRetry(maxRetry))
					.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(dslConfigManager.getS3Host(),
							dslConfigManager.getS3Region()))
					.build();

			logger.info("Checking MinIO bucket " + bucketName);
			connection.doesBucketExistV2(bucketName);
			logger.info("MinIO bucket check finished for " + bucketName);
			retry = 0;
		} catch (Exception e) {
			logger.error("MinIO connection failed for bucket " + bucketName + ": " + e.getMessage(), e);
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

	public boolean objectExists(String bucket, String key) {
		AmazonS3 client = getConnection(bucket);
		if (client == null) {
			throw new IllegalStateException("MinIO connection failed for bucket " + bucket);
		}
		logger.info("MinIO object exists? " + bucket + "/" + key);
		boolean exists = client.doesObjectExist(bucket, key);
		logger.info("MinIO object " + bucket + "/" + key + " exists=" + exists);
		return exists;
	}

	public long objectSize(String bucket, String key) {
		AmazonS3 client = getConnection(bucket);
		if (client == null) {
			throw new IllegalStateException("MinIO connection failed for bucket " + bucket);
		}
		return client.getObjectMetadata(bucket, key).getContentLength();
	}

	public List<String> listObjectKeys(String bucket, String prefix) {
		AmazonS3 client = getConnection(bucket);
		if (client == null) {
			throw new IllegalStateException("MinIO connection failed for bucket " + bucket);
		}
		List<String> keys = new ArrayList<>();
		try {
			ObjectListing listing = client.listObjects(bucket, prefix);
			while (listing != null) {
				if (listing.getObjectSummaries() != null) {
					for (S3ObjectSummary summary : listing.getObjectSummaries()) {
						keys.add(summary.getKey());
					}
				}
				if (listing.isTruncated()) {
					listing = client.listNextBatchOfObjects(listing);
				} else {
					break;
				}
			}
		} catch (Exception e) {
			logger.warn("Failed to list objects in bucket " + bucket + " with prefix " + prefix + ": " + e.getMessage());
		}
		return keys;
	}

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

}
