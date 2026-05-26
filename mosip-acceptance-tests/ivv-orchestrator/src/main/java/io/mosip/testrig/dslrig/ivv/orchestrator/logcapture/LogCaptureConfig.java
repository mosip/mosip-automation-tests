package io.mosip.testrig.dslrig.ivv.orchestrator.logcapture;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import io.mosip.testrig.dslrig.ivv.orchestrator.TestRunner;

/**
 * Loads {@code config/log-capture.properties} with env-var overrides (Kubernetes configmap keys).
 */
public final class LogCaptureConfig {

	private static final Logger LOGGER = Logger.getLogger(LogCaptureConfig.class);

	private static volatile LogCaptureConfig instance;

	private final boolean enabled;
	private final String namespace;
	private final String captureMode;
	private final String localLogPath;
	private final boolean uploadToS3;
	private final boolean s3Enabled;
	private final String s3ReportFolder;
	private final boolean uploadCombinedLog;
	private final boolean uploadServiceWiseLogs;
	private final boolean compressBeforeUpload;
	private final List<String> services;
	private final List<String> targetEntries;
	private final Set<String> excludeServices;
	private final Set<String> logLevels;
	private final boolean generateCombinedLog;
	private final boolean generateFailureSummary;
	private final int kubectlTimeoutSeconds;
	private final long maxLogBytesPerService;

	private LogCaptureConfig(Properties props) {
		enabled = parseBoolean(props, "logCapture.enabled", false);
		namespace = get(props, "logCapture.namespace", "mosip");
		captureMode = get(props, "logCapture.captureMode", "execution-window");
		localLogPath = get(props, "logCapture.localLogPath", "reports/logs");
		uploadToS3 = parseBoolean(props, "logCapture.uploadToS3", true);
		s3Enabled = parseBoolean(props, "logCapture.s3.enabled", true);
		s3ReportFolder = get(props, "logCapture.s3.reportFolder", "execution-logs");
		uploadCombinedLog = parseBoolean(props, "logCapture.s3.uploadCombinedLog", true);
		uploadServiceWiseLogs = parseBoolean(props, "logCapture.s3.uploadServiceWiseLogs", true);
		compressBeforeUpload = parseBoolean(props, "logCapture.s3.compressBeforeUpload", true);
		services = splitList(get(props, "logCapture.services", ""));
		targetEntries = splitList(get(props, "logCapture.targets", ""));
		excludeServices = new LinkedHashSet<>(splitList(get(props, "logCapture.excludeServices", "")));
		logLevels = splitList(get(props, "logCapture.logLevels", "INFO,WARN,ERROR")).stream()
				.map(String::toUpperCase)
				.collect(Collectors.toCollection(LinkedHashSet::new));
		generateCombinedLog = parseBoolean(props, "logCapture.generateCombinedLog", true);
		generateFailureSummary = parseBoolean(props, "logCapture.generateFailureSummary", true);
		kubectlTimeoutSeconds = parseInt(props, "logCapture.kubectlTimeoutSeconds", 300);
		maxLogBytesPerService = parseLong(props, "logCapture.maxLogBytesPerService", 52_428_800L);
	}

	public static LogCaptureConfig getInstance() {
		if (instance == null) {
			synchronized (LogCaptureConfig.class) {
				if (instance == null) {
					instance = load();
				}
			}
		}
		return instance;
	}

	public static void reload() {
		synchronized (LogCaptureConfig.class) {
			instance = load();
		}
	}

	private static LogCaptureConfig load() {
		Properties props = new Properties();
		try (InputStream in = LogCaptureConfig.class.getClassLoader().getResourceAsStream("config/log-capture.properties")) {
			if (in != null) {
				props.load(in);
			}
		} catch (IOException e) {
			LOGGER.warn("Could not load classpath log-capture.properties: " + e.getMessage());
		}
		try {
			Path external = Paths.get(TestRunner.getGlobalResourcePath(), "config", "log-capture.properties");
			if (Files.isRegularFile(external)) {
				try (InputStream in = Files.newInputStream(external)) {
					Properties externalProps = new Properties();
					externalProps.load(in);
					props.putAll(externalProps);
				}
			}
		} catch (Exception e) {
			LOGGER.debug("Skipping external log-capture.properties: " + e.getMessage());
		}
		for (String key : new ArrayList<>(props.stringPropertyNames())) {
			String env = System.getenv(key);
			if (env != null && !env.isBlank()) {
				props.setProperty(key, env);
			}
		}
		LogCaptureConfig config = new LogCaptureConfig(props);
		LOGGER.info("Log capture config: enabled=" + config.enabled + ", defaultNamespace=" + config.namespace
				+ ", targets=" + config.resolvedTargets().size());
		return config;
	}

	private static String get(Properties props, String key, String defaultValue) {
		String v = props.getProperty(key);
		return (v == null || v.isBlank()) ? defaultValue : v.trim();
	}

	private static boolean parseBoolean(Properties props, String key, boolean defaultValue) {
		String v = props.getProperty(key);
		if (v == null || v.isBlank()) {
			return defaultValue;
		}
		return "true".equalsIgnoreCase(v.trim()) || "yes".equalsIgnoreCase(v.trim());
	}

	private static int parseInt(Properties props, String key, int defaultValue) {
		try {
			return Integer.parseInt(get(props, key, String.valueOf(defaultValue)));
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	private static long parseLong(Properties props, String key, long defaultValue) {
		try {
			return Long.parseLong(get(props, key, String.valueOf(defaultValue)));
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	private static List<String> splitList(String csv) {
		if (csv == null || csv.isBlank()) {
			return Collections.emptyList();
		}
		return Arrays.stream(csv.split(","))
				.map(String::trim)
				.filter(s -> !s.isEmpty())
				.collect(Collectors.toList());
	}

	public boolean isEnabled() {
		return enabled;
	}

	public String getNamespace() {
		return namespace;
	}

	public String getCaptureMode() {
		return captureMode;
	}

	public String getLocalLogPath() {
		return localLogPath;
	}

	public boolean isUploadToS3() {
		return uploadToS3;
	}

	public boolean isS3Enabled() {
		return s3Enabled;
	}

	public String getS3ReportFolder() {
		return s3ReportFolder;
	}

	public boolean isUploadCombinedLog() {
		return uploadCombinedLog;
	}

	public boolean isUploadServiceWiseLogs() {
		return uploadServiceWiseLogs;
	}

	public boolean isCompressBeforeUpload() {
		return compressBeforeUpload;
	}

	public List<String> getServices() {
		return services;
	}

	public Set<String> getExcludeServices() {
		return excludeServices;
	}

	public Set<String> getLogLevels() {
		return logLevels;
	}

	public boolean isGenerateCombinedLog() {
		return generateCombinedLog;
	}

	public boolean isGenerateFailureSummary() {
		return generateFailureSummary;
	}

	public int getKubectlTimeoutSeconds() {
		return kubectlTimeoutSeconds;
	}

	public long getMaxLogBytesPerService() {
		return maxLogBytesPerService;
	}

	public List<String> resolvedServices() {
		List<String> resolved = new ArrayList<>();
		for (String service : services) {
			if (!excludeServices.contains(service)) {
				resolved.add(service);
			}
		}
		return resolved;
	}

	/**
	 * Workloads to capture. Uses {@code logCapture.targets} ({@code namespace:workload}) when set;
	 * otherwise falls back to {@code logCapture.services} in {@link #namespace}.
	 */
	public List<LogCaptureTarget> resolvedTargets() {
		List<LogCaptureTarget> targets = new ArrayList<>();
		if (!targetEntries.isEmpty()) {
			for (String entry : targetEntries) {
				LogCaptureTarget target = LogCaptureTarget.parse(entry, namespace);
				if (!excludeServices.contains(target.getWorkload())) {
					targets.add(target);
				}
			}
			return targets;
		}
		for (String service : resolvedServices()) {
			targets.add(new LogCaptureTarget(namespace, service, "deployment"));
		}
		return targets;
	}
}
