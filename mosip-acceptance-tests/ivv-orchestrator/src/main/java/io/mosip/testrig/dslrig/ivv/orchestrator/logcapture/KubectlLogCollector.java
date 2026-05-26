package io.mosip.testrig.dslrig.ivv.orchestrator.logcapture;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

/**
 * Captures pod logs via {@code kubectl} for configured MOSIP workloads (multi-namespace).
 */
public class KubectlLogCollector {

	private static final Logger LOGGER = Logger.getLogger(KubectlLogCollector.class);

	private static final Pattern LEVEL_LINE = Pattern.compile("\\b(ERROR|WARN|WARNING|INFO)\\b");

	private static final String[] FALLBACK_KINDS = { "deployment", "statefulset", "daemonset" };

	private final LogCaptureConfig config;

	public KubectlLogCollector(LogCaptureConfig config) {
		this.config = config;
	}

	public CaptureResult captureTarget(LogCaptureTarget target, Instant since, Path outputFile) {
		StringBuilder raw = new StringBuilder();
		int exitCode = -1;
		String errorMessage = null;

		for (String strategy : buildStrategies(target)) {
			List<String> command = buildKubectlCommand(target, since, strategy);
			CaptureAttempt attempt = runKubectl(command);
			if (attempt.content != null && !attempt.content.isBlank()) {
				raw.append(attempt.content);
				exitCode = attempt.exitCode;
				errorMessage = null;
				break;
			}
			if (attempt.errorMessage != null) {
				errorMessage = attempt.errorMessage;
			}
			exitCode = attempt.exitCode;
		}

		String filtered = filterByLogLevels(raw.toString(), config.getLogLevels());
		try {
			Files.createDirectories(outputFile.getParent());
			Files.writeString(outputFile, filtered, StandardCharsets.UTF_8);
		} catch (IOException e) {
			LOGGER.error("Could not write log file " + outputFile + ": " + e.getMessage());
		}

		return new CaptureResult(target, exitCode, errorMessage, filtered, outputFile);
	}

	private List<String> buildStrategies(LogCaptureTarget target) {
		List<String> strategies = new ArrayList<>();
		strategies.add(target.getWorkloadRef());
		for (String kind : FALLBACK_KINDS) {
			String ref = kind + "/" + target.getWorkload();
			if (!strategies.contains(ref)) {
				strategies.add(ref);
			}
		}
		strategies.add("label:app.kubernetes.io/name=" + target.getWorkload());
		strategies.add("label:app=" + target.getWorkload());
		return strategies;
	}

	private List<String> buildKubectlCommand(LogCaptureTarget target, Instant since, String strategy) {
		List<String> cmd = new ArrayList<>();
		cmd.add("kubectl");
		cmd.add("logs");
		cmd.add("-n");
		cmd.add(target.getNamespace());
		cmd.add("--all-containers=true");
		cmd.add("--timestamps=true");
		if (since != null && "execution-window".equalsIgnoreCase(config.getCaptureMode())) {
			cmd.add("--since-time=" + DateTimeFormatter.ISO_INSTANT.format(since));
		}
		if (strategy.startsWith("label:")) {
			cmd.add("-l");
			cmd.add(strategy.substring("label:".length()));
		} else {
			cmd.add(strategy);
		}
		return cmd;
	}

	private CaptureAttempt runKubectl(List<String> command) {
		StringBuilder raw = new StringBuilder();
		int exitCode = -1;
		String errorMessage = null;
		try {
			ProcessBuilder pb = new ProcessBuilder(command);
			pb.redirectErrorStream(true);
			Process process = pb.start();
			boolean finished = process.waitFor(config.getKubectlTimeoutSeconds(), TimeUnit.SECONDS);
			if (!finished) {
				process.destroyForcibly();
				errorMessage = "kubectl timed out after " + config.getKubectlTimeoutSeconds() + "s";
			} else {
				exitCode = process.exitValue();
				try (InputStream in = process.getInputStream();
						BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
					String line;
					long bytes = 0;
					while ((line = reader.readLine()) != null) {
						bytes += line.length();
						if (bytes > config.getMaxLogBytesPerService()) {
							raw.append("\n... [truncated at ").append(config.getMaxLogBytesPerService())
									.append(" bytes] ...\n");
							break;
						}
						raw.append(line).append('\n');
					}
				}
				if (exitCode != 0 && raw.length() == 0) {
					errorMessage = "kubectl exit " + exitCode + " for " + String.join(" ", command);
				}
			}
		} catch (Exception e) {
			errorMessage = e.getMessage();
			LOGGER.error("kubectl failed: " + e.getMessage());
		}
		return new CaptureAttempt(exitCode, errorMessage, raw.toString());
	}

	static String filterByLogLevels(String content, Set<String> levels) {
		if (content == null || content.isEmpty() || levels == null || levels.isEmpty()) {
			return content == null ? "" : content;
		}
		StringBuilder out = new StringBuilder();
		for (String line : content.split("\n")) {
			if (line.isEmpty() || matchesLevel(line, levels)) {
				out.append(line).append('\n');
			}
		}
		return out.toString();
	}

	private static boolean matchesLevel(String line, Set<String> levels) {
		java.util.regex.Matcher m = LEVEL_LINE.matcher(line.toUpperCase(Locale.ROOT));
		if (!m.find()) {
			return true;
		}
		String level = m.group(1);
		if ("WARNING".equals(level)) {
			level = "WARN";
		}
		return levels.contains(level);
	}

	public static boolean isKubectlAvailable() {
		try {
			Process p = new ProcessBuilder("kubectl", "version", "--client=true").redirectErrorStream(true).start();
			return p.waitFor(15, TimeUnit.SECONDS) && p.exitValue() == 0;
		} catch (Exception e) {
			return false;
		}
	}

	private static final class CaptureAttempt {
		final int exitCode;
		final String errorMessage;
		final String content;

		CaptureAttempt(int exitCode, String errorMessage, String content) {
			this.exitCode = exitCode;
			this.errorMessage = errorMessage;
			this.content = content;
		}
	}

	public static final class CaptureResult {
		private final LogCaptureTarget target;
		private final int exitCode;
		private final String errorMessage;
		private final String content;
		private final Path logFile;

		public CaptureResult(LogCaptureTarget target, int exitCode, String errorMessage, String content,
				Path logFile) {
			this.target = target;
			this.exitCode = exitCode;
			this.errorMessage = errorMessage;
			this.content = content;
			this.logFile = logFile;
		}

		public LogCaptureTarget getTarget() {
			return target;
		}

		/** @deprecated use {@link #getTarget()}{@code .getLogLabel()} */
		@Deprecated
		public String getService() {
			return target.getLogLabel();
		}

		public int getExitCode() {
			return exitCode;
		}

		public String getErrorMessage() {
			return errorMessage;
		}

		public String getContent() {
			return content;
		}

		public Path getLogFile() {
			return logFile;
		}

		public boolean hasContent() {
			return content != null && !content.isBlank();
		}

		public boolean captureFailed() {
			return errorMessage != null || (exitCode > 1 && !hasContent());
		}
	}
}
