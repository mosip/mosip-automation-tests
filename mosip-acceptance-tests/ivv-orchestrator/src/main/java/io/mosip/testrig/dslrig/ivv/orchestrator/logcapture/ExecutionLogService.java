package io.mosip.testrig.dslrig.ivv.orchestrator.logcapture;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import io.mosip.testrig.apirig.testrunner.BaseTestCase;
import io.mosip.testrig.dslrig.ivv.orchestrator.S3Adapter;
import io.mosip.testrig.dslrig.ivv.orchestrator.dslConfigManager;

/**
 * Captures Kubernetes service logs during DSL execution, archives them locally, uploads to MinIO/S3,
 * and exposes download links for the HTML report.
 */
public final class ExecutionLogService {

	private static final Logger LOGGER = Logger.getLogger(ExecutionLogService.class);

	private static final Object LOCK = new Object();

	private static volatile boolean started;
	private static volatile boolean finalized;
	private static volatile Instant executionStart;
	private static volatile String executionFolderName;
	private static volatile Path executionDir;

	private ExecutionLogService() {
	}

	public static void markExecutionStart() {
		LogCaptureConfig config = LogCaptureConfig.getInstance();
		if (!config.isEnabled()) {
			return;
		}
		synchronized (LOCK) {
			if (started) {
				return;
			}
			executionStart = Instant.now();
			executionFolderName = "execution_"
					+ DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss").withZone(ZoneOffset.UTC)
							.format(executionStart);
			executionDir = Paths.get(System.getProperty("user.dir"), config.getLocalLogPath(), executionFolderName);
			started = true;
			finalized = false;
			LOGGER.info("Log capture started for execution window: " + executionFolderName);
			if (!KubectlLogCollector.isKubectlAvailable()) {
				LOGGER.warn("kubectl not available; log capture will produce empty or error summaries");
			}
		}
	}

	/**
	 * Idempotent finalize: capture logs, summarize, compress, upload. Safe to call from {@code finally}.
	 */
	public static void finalizeCaptureAndUpload() {
		LogCaptureConfig config = LogCaptureConfig.getInstance();
		if (!config.isEnabled()) {
			return;
		}
		synchronized (LOCK) {
			if (finalized) {
				return;
			}
			finalized = true;
		}

		try {
			doFinalize(config);
		} catch (Exception e) {
			LOGGER.error("Execution log archival failed: " + e.getMessage(), e);
			ExecutionLogContext.setLatest(ExecutionLogContext.failure(e.getMessage()));
		}
	}

	private static void doFinalize(LogCaptureConfig config) throws IOException {
		if (!started || executionDir == null) {
			markExecutionStart();
		}
		Path logsDir = executionDir.resolve("logs");
		Files.createDirectories(logsDir);

		KubectlLogCollector collector = new KubectlLogCollector(config);
		List<KubectlLogCollector.CaptureResult> results = new ArrayList<>();

		for (LogCaptureTarget target : config.resolvedTargets()) {
			Path serviceLog = logsDir.resolve(target.getLogFileName());
			LOGGER.info("Capturing logs for " + target.getLogLabel());
			results.add(collector.captureTarget(target, executionStart, serviceLog));
		}

		Path combinedLog = null;
		if (config.isGenerateCombinedLog()) {
			combinedLog = logsDir.resolve("combined.log");
			writeCombinedLog(results, combinedLog);
		}

		if (config.isGenerateFailureSummary()) {
			new LogFailureSummaryGenerator().generate(results, executionDir);
		}

		Path archiveZip = null;
		String localArchivePath = null;
		if (config.isCompressBeforeUpload()) {
			archiveZip = executionDir.resolve("logs.zip");
			LogArchiveCompressor.zipDirectory(logsDir, archiveZip);
			localArchivePath = archiveZip.toAbsolutePath().toString();
		}

		String s3Key = null;
		String downloadUrl = null;
		if (shouldUpload(config)) {
			s3Key = uploadArchives(config, logsDir, combinedLog, archiveZip);
			downloadUrl = buildDownloadUrl(config, s3Key);
		} else if (localArchivePath != null) {
			downloadUrl = "file:///" + archiveZip.toAbsolutePath().toString().replace('\\', '/');
		}

		List<String> failedServices = List.of();
		Path summaryJson = executionDir.resolve("failure-summary.json");
		if (Files.isRegularFile(summaryJson)) {
			failedServices = extractFailedServicesFromSummary(summaryJson);
		}

		ExecutionLogContext.setLatest(ExecutionLogContext.success(executionFolderName, executionDir, logsDir,
				combinedLog, archiveZip, s3Key, downloadUrl, localArchivePath, failedServices));
		LOGGER.info("Execution log archival complete. downloadUrl=" + downloadUrl);
	}

	private static boolean shouldUpload(LogCaptureConfig config) {
		if (!config.isUploadToS3() || !config.isS3Enabled()) {
			return false;
		}
		return dslConfigManager.getPushReportsToS3().equalsIgnoreCase("yes");
	}

	private static String uploadArchives(LogCaptureConfig config, Path logsDir, Path combinedLog, Path archiveZip)
			throws IOException {
		S3Adapter s3 = new S3Adapter();
		String bucket = dslConfigManager.getS3Account();
		String testLevel = BaseTestCase.testLevel;
		String process = config.getS3ReportFolder();
		String source = executionFolderName;

		if (config.isCompressBeforeUpload() && archiveZip != null && Files.isRegularFile(archiveZip)) {
			s3.putObject(bucket, testLevel, source, process, "logs.zip", archiveZip.toFile());
			return S3Adapter.getName(testLevel, source, process, "logs.zip");
		}

		if (config.isUploadCombinedLog() && combinedLog != null && Files.isRegularFile(combinedLog)) {
			s3.putObject(bucket, testLevel, source, process + "/logs", "combined.log", combinedLog.toFile());
		}

		if (config.isUploadServiceWiseLogs()) {
			try (var stream = Files.list(logsDir)) {
				stream.filter(Files::isRegularFile)
						.filter(p -> p.getFileName().toString().endsWith(".log"))
						.forEach(p -> {
							String name = p.getFileName().toString();
							s3.putObject(bucket, testLevel, source, process + "/logs", name, p.toFile());
						});
			}
		}

		Path failureSummary = executionDir.resolve("failure-summary.json");
		if (Files.isRegularFile(failureSummary)) {
			s3.putObject(bucket, testLevel, source, process, "failure-summary.json", failureSummary.toFile());
		}
		Path errorSummary = executionDir.resolve("error-summary.txt");
		if (Files.isRegularFile(errorSummary)) {
			s3.putObject(bucket, testLevel, source, process, "error-summary.txt", errorSummary.toFile());
		}

		return S3Adapter.getName(testLevel, source, process, "logs.zip");
	}

	private static String buildDownloadUrl(LogCaptureConfig config, String objectKey) {
		if (objectKey == null || objectKey.isBlank()) {
			return null;
		}
		String host = dslConfigManager.getS3Host();
		if (host == null || host.isBlank()) {
			return objectKey;
		}
		String bucket = dslConfigManager.getS3Account();
		String base = host.endsWith("/") ? host.substring(0, host.length() - 1) : host;
		return base + "/" + bucket + "/" + objectKey;
	}

	private static void writeCombinedLog(List<KubectlLogCollector.CaptureResult> results, Path combinedLog)
			throws IOException {
		StringBuilder sb = new StringBuilder();
		for (KubectlLogCollector.CaptureResult result : results) {
			sb.append("===== ").append(result.getTarget().getLogLabel()).append(" =====\n");
			if (result.getErrorMessage() != null) {
				sb.append("[capture-error] ").append(result.getErrorMessage()).append('\n');
			}
			sb.append(result.getContent() == null ? "" : result.getContent());
			if (!sb.isEmpty() && sb.charAt(sb.length() - 1) != '\n') {
				sb.append('\n');
			}
			sb.append('\n');
		}
		Files.writeString(combinedLog, sb.toString(), StandardCharsets.UTF_8);
	}

	@SuppressWarnings("unchecked")
	private static List<String> extractFailedServicesFromSummary(Path summaryJson) {
		try {
			com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
			var node = mapper.readTree(summaryJson.toFile());
			var arr = node.get("failedServices");
			if (arr == null || !arr.isArray()) {
				return List.of();
			}
			List<String> list = new ArrayList<>();
			arr.forEach(n -> list.add(n.asText()));
			return list;
		} catch (Exception e) {
			return List.of();
		}
	}

	public static boolean isEnabled() {
		return LogCaptureConfig.getInstance().isEnabled();
	}
}
