package io.mosip.testrig.dslrig.ivv.orchestrator.logcapture;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Builds {@code failure-summary.json} and {@code error-summary.txt} from captured service logs.
 */
public class LogFailureSummaryGenerator {

	private static final Pattern ERROR_COUNT = Pattern.compile("\\bERROR\\b", Pattern.CASE_INSENSITIVE);
	private static final Pattern WARN_COUNT = Pattern.compile("\\bWARN(?:ING)?\\b", Pattern.CASE_INSENSITIVE);
	private static final Pattern RESTART = Pattern.compile(
			"(?i)(restart|crashloopbackoff|back-off|oomkilled|liveness probe failed|readiness probe failed)");
	private static final Pattern TIMEOUT = Pattern.compile("(?i)(timeout|timed out|time-out)");
	private static final Pattern API_FAILURE = Pattern.compile(
			"(?i)(http\\s+[45]\\d{2}|status\\s*[=:]\\s*(?:4|5)\\d{2}|failed response|api failure|exception)");

	private final ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

	public SummaryResult generate(List<KubectlLogCollector.CaptureResult> results, Path outputDir) throws IOException {
		List<Map<String, Object>> serviceSummaries = new ArrayList<>();
		List<String> failedServices = new ArrayList<>();
		StringBuilder errorSummaryText = new StringBuilder();
		errorSummaryText.append("DSL Execution Log Error Summary\n");
		errorSummaryText.append("================================\n\n");

		int totalErrors = 0;
		int totalWarnings = 0;
		int totalRestarts = 0;
		int totalTimeouts = 0;
		int totalApiFailures = 0;

		for (KubectlLogCollector.CaptureResult result : results) {
			String content = result.getContent() == null ? "" : result.getContent();
			int errors = countMatches(ERROR_COUNT, content);
			int warnings = countMatches(WARN_COUNT, content);
			int restarts = countMatches(RESTART, content);
			int timeouts = countMatches(TIMEOUT, content);
			int apiFailures = countMatches(API_FAILURE, content);

			totalErrors += errors;
			totalWarnings += warnings;
			totalRestarts += restarts;
			totalTimeouts += timeouts;
			totalApiFailures += apiFailures;

			String label = result.getTarget().getLogLabel();
			boolean failed = result.captureFailed() || errors > 0 || restarts > 0;
			if (failed) {
				failedServices.add(label);
			}

			Map<String, Object> entry = new LinkedHashMap<>();
			entry.put("service", label);
			entry.put("namespace", result.getTarget().getNamespace());
			entry.put("workload", result.getTarget().getWorkload());
			entry.put("captureFailed", result.captureFailed());
			entry.put("captureError", result.getErrorMessage());
			entry.put("errorCount", errors);
			entry.put("warnCount", warnings);
			entry.put("restartEvents", restarts);
			entry.put("timeoutExceptions", timeouts);
			entry.put("apiFailures", apiFailures);
			entry.put("logFile", result.getLogFile() != null ? result.getLogFile().getFileName().toString() : "");
			serviceSummaries.add(entry);

			if (failed || errors > 0 || warnings > 0) {
				errorSummaryText.append("Service: ").append(label).append('\n');
				if (result.getErrorMessage() != null) {
					errorSummaryText.append("  Capture error: ").append(result.getErrorMessage()).append('\n');
				}
				errorSummaryText.append("  ERROR count: ").append(errors).append('\n');
				errorSummaryText.append("  WARN count: ").append(warnings).append('\n');
				errorSummaryText.append("  Restart events: ").append(restarts).append('\n');
				errorSummaryText.append("  Timeout exceptions: ").append(timeouts).append('\n');
				errorSummaryText.append("  API failures: ").append(apiFailures).append('\n');
				errorSummaryText.append('\n');
			}
		}

		Map<String, Object> root = new LinkedHashMap<>();
		root.put("failedServices", failedServices);
		root.put("totalErrorCount", totalErrors);
		root.put("totalWarnCount", totalWarnings);
		root.put("totalRestartEvents", totalRestarts);
		root.put("totalTimeoutExceptions", totalTimeouts);
		root.put("totalApiFailures", totalApiFailures);
		root.put("services", serviceSummaries);

		Path jsonPath = outputDir.resolve("failure-summary.json");
		Path txtPath = outputDir.resolve("error-summary.txt");
		Files.createDirectories(outputDir);
		objectMapper.writeValue(jsonPath.toFile(), root);

		errorSummaryText.append("Totals\n------\n");
		errorSummaryText.append("Failed services: ").append(String.join(", ", failedServices)).append('\n');
		errorSummaryText.append("ERROR count: ").append(totalErrors).append('\n');
		errorSummaryText.append("WARN count: ").append(totalWarnings).append('\n');
		errorSummaryText.append("Restart events: ").append(totalRestarts).append('\n');
		errorSummaryText.append("Timeout exceptions: ").append(totalTimeouts).append('\n');
		errorSummaryText.append("API failures: ").append(totalApiFailures).append('\n');
		Files.writeString(txtPath, errorSummaryText.toString(), StandardCharsets.UTF_8);

		return new SummaryResult(jsonPath, txtPath, failedServices, totalErrors, totalWarnings);
	}

	private static int countMatches(Pattern pattern, String content) {
		int count = 0;
		Matcher m = pattern.matcher(content);
		while (m.find()) {
			count++;
		}
		return count;
	}

	public static final class SummaryResult {
		private final Path jsonPath;
		private final Path textPath;
		private final List<String> failedServices;
		private final int totalErrors;
		private final int totalWarnings;

		public SummaryResult(Path jsonPath, Path textPath, List<String> failedServices, int totalErrors,
				int totalWarnings) {
			this.jsonPath = jsonPath;
			this.textPath = textPath;
			this.failedServices = failedServices;
			this.totalErrors = totalErrors;
			this.totalWarnings = totalWarnings;
		}

		public Path getJsonPath() {
			return jsonPath;
		}

		public Path getTextPath() {
			return textPath;
		}

		public List<String> getFailedServices() {
			return failedServices;
		}

		public int getTotalErrors() {
			return totalErrors;
		}

		public int getTotalWarnings() {
			return totalWarnings;
		}
	}
}
