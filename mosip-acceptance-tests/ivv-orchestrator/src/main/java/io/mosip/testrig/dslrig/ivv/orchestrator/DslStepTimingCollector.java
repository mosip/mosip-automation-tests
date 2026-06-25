package io.mosip.testrig.dslrig.ivv.orchestrator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.LongAdder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thread-safe timing samples for DSL hot paths. Does not alter execution flow;
 * callers record start/end around existing steps.
 */
public final class DslStepTimingCollector {

	private static final Logger logger = LoggerFactory.getLogger(DslStepTimingCollector.class);

	private static final ConcurrentHashMap<String, ConcurrentLinkedQueue<Long>> SAMPLES = new ConcurrentHashMap<>();
	private static final ConcurrentHashMap<String, LongAdder> TOTAL_MS = new ConcurrentHashMap<>();
	private static final ConcurrentHashMap<String, LongAdder> COUNT = new ConcurrentHashMap<>();

	private DslStepTimingCollector() {
	}

	public static TimingScope start(String stepName) {
		return new TimingScope(stepName, System.currentTimeMillis());
	}

	public static void record(String stepName, long durationMs, String scenarioId, Long httpMs, Long serverMs) {
		if (stepName == null || durationMs < 0) {
			return;
		}
		SAMPLES.computeIfAbsent(stepName, k -> new ConcurrentLinkedQueue<>()).add(durationMs);
		TOTAL_MS.computeIfAbsent(stepName, k -> new LongAdder()).add(durationMs);
		COUNT.computeIfAbsent(stepName, k -> new LongAdder()).increment();

		logger.info(
				"DSL_TIMING step={} scenarioId={} threadId={} durationMs={} httpMs={} serverMs={}",
				stepName,
				scenarioId == null ? "n/a" : scenarioId,
				Thread.currentThread().getId(),
				durationMs,
				httpMs == null ? "n/a" : httpMs,
				serverMs == null ? "n/a" : serverMs);
	}

	public static String buildReport() {
		if (SAMPLES.isEmpty()) {
			return "DSL timing: no samples recorded.";
		}
		StringBuilder sb = new StringBuilder(512);
		sb.append("=== DSL Step Timing Report ===\n");
		List<String> steps = new ArrayList<>(SAMPLES.keySet());
		Collections.sort(steps);
		for (String step : steps) {
			List<Long> values = new ArrayList<>(SAMPLES.get(step));
			if (values.isEmpty()) {
				continue;
			}
			Collections.sort(values);
			long count = values.size();
			long sum = 0L;
			for (Long v : values) {
				sum += v;
			}
			sb.append(String.format(Locale.ROOT, "%s (n=%d)%n", step, count));
			sb.append(String.format(Locale.ROOT, "  Avg: %d ms%n", sum / count));
			sb.append(String.format(Locale.ROOT, "  Median: %d ms%n", percentile(values, 50)));
			sb.append(String.format(Locale.ROOT, "  P90: %d ms%n", percentile(values, 90)));
			sb.append(String.format(Locale.ROOT, "  P95: %d ms%n", percentile(values, 95)));
			sb.append(String.format(Locale.ROOT, "  P99: %d ms%n", percentile(values, 99)));
			sb.append(String.format(Locale.ROOT, "  Max: %d ms%n", values.get(values.size() - 1)));
		}
		return sb.toString();
	}

	public static void logReport() {
		logger.info("\n{}", buildReport());
	}

	public static void clear() {
		SAMPLES.clear();
		TOTAL_MS.clear();
		COUNT.clear();
	}

	private static long percentile(List<Long> sorted, int pct) {
		if (sorted.isEmpty()) {
			return 0L;
		}
		int index = (int) Math.ceil((pct / 100.0) * sorted.size()) - 1;
		if (index < 0) {
			index = 0;
		}
		if (index >= sorted.size()) {
			index = sorted.size() - 1;
		}
		return sorted.get(index);
	}

	public static final class TimingScope implements AutoCloseable {
		private final String stepName;
		private final long startMs;
		private String scenarioId;
		private Long httpMs;
		private Long serverMs;

		private TimingScope(String stepName, long startMs) {
			this.stepName = stepName;
			this.startMs = startMs;
		}

		public TimingScope scenarioId(String scenarioId) {
			this.scenarioId = scenarioId;
			return this;
		}

		public TimingScope httpMs(long httpMs) {
			this.httpMs = httpMs;
			return this;
		}

		public TimingScope serverMs(Long serverMs) {
			this.serverMs = serverMs;
			return this;
		}

		@Override
		public void close() {
			record(stepName, System.currentTimeMillis() - startMs, scenarioId, httpMs, serverMs);
		}
	}

	public static Long parseEnvoyUpstreamMs(io.restassured.response.Response response) {
		if (response == null) {
			return null;
		}
		String header = response.getHeader("x-envoy-upstream-service-time");
		if (header == null || header.isBlank()) {
			return null;
		}
		try {
			return Long.parseLong(header.trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}
}
