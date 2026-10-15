package io.mosip.testrig.dslrig.ivv.e2e.methods;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.testng.Reporter;

import io.mosip.testrig.apirig.dbaccess.DBManager;
import io.mosip.testrig.apirig.utils.ConfigManager;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.S3Adapter;

/**
 * Draft and rejected checks use the uppercase SHA-256 of the registration id:
 * objects live under {@code idrepo/_draft/{ridHash}} and must not be at {@code idrepo/{ridHash}}.
 * After processing, that draft prefix is removed and demographic and biometric data is published
 * under {@code idrepo/{uinHash}}, where {@code uinHash} is {@code idrepo.uin.uin_hash} with the
 * leading {@code 17_} prefix stripped.
 */
final class IdRepoDraftLocationChecker {

	private static final Logger logger = Logger.getLogger(IdRepoDraftLocationChecker.class);
	private static final String[] OBJECTS = { "demographic", "biometric" };

	enum Phase {
		DRAFT, PUBLISHED, REJECTED
	}

	private IdRepoDraftLocationChecker() {
	}

	static Phase parsePhase(String phase) throws RigInternalError {
		if ("draft".equalsIgnoreCase(phase)) {
			return Phase.DRAFT;
		}
		if ("published".equalsIgnoreCase(phase)) {
			return Phase.PUBLISHED;
		}
		if ("rejected".equalsIgnoreCase(phase) || "deleted".equalsIgnoreCase(phase)
				|| "removed".equalsIgnoreCase(phase)) {
			return Phase.REJECTED;
		}
		throw new RigInternalError("location phase must be draft, published, or rejected, got: " + phase);
	}

	static void verify(String rid, Phase phase) throws RigInternalError {
		pollUntilVerified(new S3Adapter(), idrepoBucket(), rid, phase);
	}

	private static void pollUntilVerified(S3Adapter minio, String bucket, String rid, Phase phase)
			throws RigInternalError {
		int waitSec = waitSeconds(phase);
		long deadline = System.currentTimeMillis() + waitSec * 1000L;
		String failure = null;
		String ridHash = ridHash(rid);
		String publishedHash = null;
		String draftPrefix = "_draft/" + ridHash;
		int attempt = 0;
		do {
			attempt++;
			String hash = ridHash;
			if (phase == Phase.PUBLISHED) {
				if (publishedHash == null) {
					publishedHash = lookupPublishedHash(rid);
				}
				hash = publishedHash;
			}
			if (hash == null) {
				failure = "No uin_hash in idrepo.uin for reg_id " + rid;
			} else {
				if (attempt == 1 || (failure != null && failure.startsWith("No uin_hash"))) {
					String note = "RID " + rid + " idrepo hash " + ridHash
							+ (phase == Phase.PUBLISHED ? " published hash " + hash : "")
							+ " bucket " + bucket + " phase " + phase;
					logger.info(note);
					Reporter.log(note, true);
				}
				logger.info("Checking idrepo " + phase + " attempt " + attempt + " draft " + draftPrefix
						+ " location " + hash);
				failure = checkPhase(minio, bucket, draftPrefix, hash, phase);
			}
			if (failure != null) {
				logger.info("Idrepo check not satisfied yet: " + failure);
			} else {
				String ok = successMessage(bucket, draftPrefix, hash, phase);
				logger.info(ok);
				Reporter.log(ok, true);
				return;
			}
			try {
				Thread.sleep(5000L);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new RigInternalError("Interrupted while checking idrepo objects for " + rid);
			}
		} while (System.currentTimeMillis() < deadline);

		String reportedHash = phase == Phase.PUBLISHED ? publishedHash : ridHash;
		throw new RigInternalError(failure + " (RID " + rid + ", hash " + reportedHash + ", waited " + waitSec + "s)");
	}

	private static String checkPhase(S3Adapter minio, String bucket, String draftPrefix, String hash, Phase phase)
			throws RigInternalError {
		switch (phase) {
		case DRAFT:
			return checkDraft(minio, bucket, draftPrefix, hash);
		case PUBLISHED:
			return checkPublished(minio, bucket, draftPrefix, hash);
		case REJECTED:
		default:
			return checkRejected(minio, bucket, draftPrefix, hash);
		}
	}

	private static String successMessage(String bucket, String draftPrefix, String hash, Phase phase) {
		switch (phase) {
		case DRAFT:
			return "Draft objects present and main location empty for " + bucket + "/" + draftPrefix;
		case PUBLISHED:
			return "Draft removed and published at " + bucket + "/" + hash;
		case REJECTED:
		default:
			return "Draft removed from " + bucket + "/" + draftPrefix + " and no data at " + bucket + "/" + hash
					+ " for rejected packet";
		}
	}

	private static String checkDraft(S3Adapter minio, String bucket, String draftPrefix, String hash)
			throws RigInternalError {
		List<String> draftKeys = minio.listObjectKeys(bucket, draftPrefix);
		List<String> publishedKeys = minio.listObjectKeys(bucket, hash + "/");
		logger.info("Draft keys under " + bucket + "/" + draftPrefix + ": " + draftKeys);
		for (String name : OBJECTS) {
			String matched = findObject(draftPrefix, name, draftKeys);
			if (matched == null) {
				return "Missing draft " + name + " under " + bucket + "/" + draftPrefix + ". Objects under prefix: "
						+ draftKeys;
			}
			if (minio.objectSize(bucket, matched) <= 0) {
				throw new RigInternalError("Empty idrepo object " + bucket + "/" + matched);
			}
			String published = findObject(hash, name, publishedKeys);
			if (published != null) {
				return "Demographic or biometric data was published before processing: " + bucket + "/" + published;
			}
		}
		return null;
	}

	private static String checkPublished(S3Adapter minio, String bucket, String draftPrefix, String hash)
			throws RigInternalError {
		List<String> draftKeys = minio.listObjectKeys(bucket, draftPrefix);
		List<String> publishedKeys = minio.listObjectKeys(bucket, hash + "/");
		for (String name : OBJECTS) {
			String draft = findObject(draftPrefix, name, draftKeys);
			if (draft != null) {
				return "Draft object still present after processing: " + bucket + "/" + draft;
			}
			String published = findObject(hash, name, publishedKeys);
			if (published == null) {
				return "Missing published " + name + " under " + bucket + "/" + hash + ". Objects under prefix: "
						+ publishedKeys;
			}
			if (minio.objectSize(bucket, published) <= 0) {
				throw new RigInternalError("Empty idrepo object " + bucket + "/" + published);
			}
		}
		return null;
	}

	private static String checkRejected(S3Adapter minio, String bucket, String draftPrefix, String hash) {
		List<String> draftKeys = minio.listObjectKeys(bucket, draftPrefix);
		List<String> publishedKeys = minio.listObjectKeys(bucket, hash + "/");
		for (String name : OBJECTS) {
			String draft = findObject(draftPrefix, name, draftKeys);
			if (draft != null) {
				return "Stale draft object still present after packet rejection: " + bucket + "/" + draft;
			}
			String published = findObject(hash, name, publishedKeys);
			if (published != null) {
				return "Data was published at main location for rejected packet: " + bucket + "/" + published;
			}
		}
		if (draftKeys != null && !draftKeys.isEmpty()) {
			return "Stale draft object(s) still present in " + bucket + "/" + draftPrefix + ": " + draftKeys;
		}
		if (publishedKeys != null && !publishedKeys.isEmpty()) {
			return "Data object(s) present in published location for rejected packet: " + bucket + "/" + hash + ": "
					+ publishedKeys;
		}
		return null;
	}

	/**
	 * Idrepo stores files under {@code Demographics/} and {@code Biometrics/}, not a single object
	 * named demographic or biometric.
	 */
	private static String findObject(String prefix, String name, List<String> keys) {
		if (keys == null || prefix == null || name == null) {
			return null;
		}
		String base = prefix.endsWith("/") ? prefix.substring(0, prefix.length() - 1) : prefix;
		String expect = name.toLowerCase();
		for (String candidate : keys) {
			if (candidate == null || !candidate.startsWith(base + "/")) {
				continue;
			}
			String rest = candidate.substring(base.length() + 1);
			int slash = rest.indexOf('/');
			String segment = slash < 0 ? rest : rest.substring(0, slash);
			if (segment.toLowerCase().startsWith(expect)) {
				return candidate;
			}
		}
		return null;
	}

	/** Uppercase hex SHA-256 of the registration id. Draft and rejected object keys use this. */
	private static String ridHash(String rid) throws RigInternalError {
		if (rid == null || !rid.matches("\\d+")) {
			throw new RigInternalError("RID must be numeric to compute the idrepo draft hash: " + rid);
		}
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hashed = digest.digest(rid.getBytes(StandardCharsets.UTF_8));
			StringBuilder hex = new StringBuilder(hashed.length * 2);
			for (byte b : hashed) {
				hex.append(String.format("%02X", b));
			}
			return hex.toString();
		} catch (Exception e) {
			throw new RigInternalError("Failed to hash RID " + rid + ": " + e.getMessage());
		}
	}

	/**
	 * Published folder is {@code uin_hash} from {@code idrepo.uin}, without the leading {@code 17_}.
	 * Returns null when the row is not visible yet so the caller can keep polling.
	 */
	private static String lookupPublishedHash(String rid) throws RigInternalError {
		if (rid == null || !rid.matches("\\d+")) {
			throw new RigInternalError("Registration ID must be numeric to look up idrepo.uin: " + rid);
		}
		String url = "jdbc:postgresql://" + ConfigManager.getDbServer() + ":" + ConfigManager.getDbPort()
				+ "/mosip_idrepo";
		String sql = "SELECT uin_hash FROM idrepo.uin WHERE reg_id = '" + rid + "'";
		Session session = null;
		try {
			session = DBManager.getDataBaseConnection(url, ConfigManager.getAuditDbUser(),
					ConfigManager.getAuditDbPass(), "idrepo");
			if (session == null) {
				throw new RigInternalError("Could not connect to mosip_idrepo to read uin_hash for " + rid);
			}
			final String[] found = new String[1];
			session.doWork((Connection connection) -> {
				try (Statement statement = connection.createStatement();
						ResultSet rs = statement.executeQuery(sql)) {
					if (rs.next()) {
						Object value = rs.getObject(1);
						found[0] = value == null ? null : value.toString().trim();
					}
				}
			});
			if (found[0] == null || found[0].isBlank()) {
				return null;
			}
			int separator = found[0].lastIndexOf('_');
			if (separator >= 0 && separator < found[0].length() - 1) {
				return found[0].substring(separator + 1);
			}
			return found[0];
		} catch (RigInternalError e) {
			throw e;
		} catch (Exception e) {
			logger.info("uin_hash lookup failed for " + rid + ": " + e.getMessage());
			return null;
		} finally {
			DBManager.closeDataBaseConnection(session);
		}
	}

	private static String idrepoBucket() {
		try {
			String bucket = ConfigManager.getproperty("idrepo-bucket");
			if (bucket != null && !bucket.isBlank()) {
				return bucket.trim();
			}
		} catch (Exception ignored) {
			// fallback to default bucket name
		}
		return "idrepo";
	}

	private static int waitSeconds(Phase phase) {
		String key = switch (phase) {
		case DRAFT -> "idrepoDraftWaitSeconds";
		case REJECTED -> "idrepoRejectedWaitSeconds";
		default -> "idrepoPublishedWaitSeconds";
		};
		int fallback = phase == Phase.DRAFT ? 120 : 60;
		try {
			String configured = ConfigManager.getproperty(key);
			if (configured != null && !configured.isBlank()) {
				return Integer.parseInt(configured.trim());
			}
			if (phase == Phase.REJECTED) {
				configured = ConfigManager.getproperty("idrepoPublishedWaitSeconds");
				if (configured != null && !configured.isBlank()) {
					return Integer.parseInt(configured.trim());
				}
			}
		} catch (Exception ignored) {
			// fallback to default wait seconds
		}
		return fallback;
	}
}
