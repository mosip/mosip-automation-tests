package io.mosip.testrig.dslrig.ivv.orchestrator.logcapture;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

/**
 * Holds paths and URLs from the latest log archival run for HTML report injection.
 */
public final class ExecutionLogContext {

	private static volatile ExecutionLogContext latest;

	private final String executionFolderName;
	private final Path executionDir;
	private final Path logsDir;
	private final Path combinedLog;
	private final Path archiveZip;
	private final String s3ObjectKey;
	private final String downloadUrl;
	private final String localArchivePath;
	private final List<String> failedServices;
	private final boolean finalized;
	private final String errorMessage;

	private ExecutionLogContext(String executionFolderName, Path executionDir, Path logsDir, Path combinedLog,
			Path archiveZip, String s3ObjectKey, String downloadUrl, String localArchivePath,
			List<String> failedServices, boolean finalized, String errorMessage) {
		this.executionFolderName = executionFolderName;
		this.executionDir = executionDir;
		this.logsDir = logsDir;
		this.combinedLog = combinedLog;
		this.archiveZip = archiveZip;
		this.s3ObjectKey = s3ObjectKey;
		this.downloadUrl = downloadUrl;
		this.localArchivePath = localArchivePath;
		this.failedServices = failedServices == null ? List.of() : failedServices;
		this.finalized = finalized;
		this.errorMessage = errorMessage;
	}

	public static void setLatest(ExecutionLogContext context) {
		latest = context;
	}

	public static ExecutionLogContext getLatest() {
		return latest;
	}

	public static ExecutionLogContext empty() {
		return new ExecutionLogContext(null, null, null, null, null, null, null, null,
				Collections.emptyList(), false, null);
	}

	public static ExecutionLogContext success(String executionFolderName, Path executionDir, Path logsDir,
			Path combinedLog, Path archiveZip, String s3ObjectKey, String downloadUrl, String localArchivePath,
			List<String> failedServices) {
		return new ExecutionLogContext(executionFolderName, executionDir, logsDir, combinedLog, archiveZip,
				s3ObjectKey, downloadUrl, localArchivePath, failedServices, true, null);
	}

	public static ExecutionLogContext failure(String errorMessage) {
		return new ExecutionLogContext(null, null, null, null, null, null, null, null,
				Collections.emptyList(), true, errorMessage);
	}

	public String getExecutionFolderName() {
		return executionFolderName;
	}

	public Path getExecutionDir() {
		return executionDir;
	}

	public Path getLogsDir() {
		return logsDir;
	}

	public Path getCombinedLog() {
		return combinedLog;
	}

	public Path getArchiveZip() {
		return archiveZip;
	}

	public String getS3ObjectKey() {
		return s3ObjectKey;
	}

	public String getDownloadUrl() {
		return downloadUrl;
	}

	public String getLocalArchivePath() {
		return localArchivePath;
	}

	public List<String> getFailedServices() {
		return failedServices;
	}

	public boolean isFinalized() {
		return finalized;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public boolean hasDownloadLink() {
		return downloadUrl != null && !downloadUrl.isBlank();
	}
}
