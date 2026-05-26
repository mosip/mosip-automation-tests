package io.mosip.testrig.dslrig.ivv.orchestrator.logcapture;

/**
 * A single Kubernetes workload to capture logs from (namespace + deployment/statefulset name).
 */
public final class LogCaptureTarget {

	private final String namespace;
	private final String workload;
	private final String kind;

	public LogCaptureTarget(String namespace, String workload, String kind) {
		this.namespace = namespace;
		this.workload = workload;
		this.kind = kind == null || kind.isBlank() ? "deployment" : kind;
	}

	public String getNamespace() {
		return namespace;
	}

	public String getWorkload() {
		return workload;
	}

	public String getKind() {
		return kind;
	}

	/** Unique label for reports and combined.log sections. */
	public String getLogLabel() {
		return namespace + "/" + workload;
	}

	/** Safe local file name, e.g. {@code regproc-regproc-camel.log}. */
	public String getLogFileName() {
		return (namespace + "-" + workload).replace('/', '-') + ".log";
	}

	public String getWorkloadRef() {
		return kind + "/" + workload;
	}

	/**
	 * Parses {@code namespace:workload} or {@code namespace:kind/workload}.
	 * Workload-only entries use {@code defaultNamespace}.
	 */
	public static LogCaptureTarget parse(String entry, String defaultNamespace) {
		String trimmed = entry.trim();
		if (trimmed.isEmpty()) {
			throw new IllegalArgumentException("empty log capture target");
		}
		if (!trimmed.contains(":")) {
			return new LogCaptureTarget(defaultNamespace, trimmed, "deployment");
		}
		int colon = trimmed.indexOf(':');
		String ns = trimmed.substring(0, colon).trim();
		String rest = trimmed.substring(colon + 1).trim();
		if (rest.contains("/")) {
			int slash = rest.indexOf('/');
			String kind = rest.substring(0, slash).trim();
			String workload = rest.substring(slash + 1).trim();
			return new LogCaptureTarget(ns, workload, kind);
		}
		return new LogCaptureTarget(ns, rest, "deployment");
	}
}
