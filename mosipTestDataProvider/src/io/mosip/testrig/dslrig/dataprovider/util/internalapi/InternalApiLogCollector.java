package io.mosip.testrig.dslrig.dataprovider.util.internalapi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * In-memory store of outbound API exchanges, keyed by Packet Creator /
 * Data Provider context id.
 */
public final class InternalApiLogCollector {

	private static final AtomicLong SEQ = new AtomicLong(1L);
	private static final Map<String, List<InternalApiLogExchange>> BY_CONTEXT = new ConcurrentHashMap<>();

	private InternalApiLogCollector() {
	}

	public static long nextSequence() {
		return SEQ.getAndIncrement();
	}

	/** Bucket for outbound calls where no MOSIP context namespace exists (e.g. JVM-wide flag only). */
	public static final String GLOBAL_LOG_KEY = "_dslrig_internal_api_global_";

	public static void record(String contextKey, InternalApiLogExchange exchange) {
		if (exchange == null) {
			return;
		}
		String key = contextKey == null || contextKey.isBlank() ? GLOBAL_LOG_KEY : contextKey;
		BY_CONTEXT.computeIfAbsent(key, k -> Collections.synchronizedList(new ArrayList<>())).add(exchange);
	}

	/**
	 * Snapshot of exchanges for a context (for reporting without removing).
	 */
	public static List<InternalApiLogExchange> snapshot(String contextKey) {
		if (contextKey == null || contextKey.isBlank()) {
			return snapshot(GLOBAL_LOG_KEY);
		}
		List<InternalApiLogExchange> list = BY_CONTEXT.get(contextKey);
		if (list == null || list.isEmpty()) {
			return List.of();
		}
		synchronized (list) {
			return List.copyOf(list);
		}
	}

	/**
	 * Remove and return all exchanges for the context.
	 */
	public static List<InternalApiLogExchange> drain(String contextKey) {
		String key = contextKey == null || contextKey.isBlank() ? GLOBAL_LOG_KEY : contextKey;
		List<InternalApiLogExchange> removed = BY_CONTEXT.remove(key);
		if (removed == null || removed.isEmpty()) {
			return List.of();
		}
		synchronized (removed) {
			return new ArrayList<>(removed);
		}
	}

	public static void clear(String contextKey) {
		String key = contextKey == null || contextKey.isBlank() ? GLOBAL_LOG_KEY : contextKey;
		BY_CONTEXT.remove(key);
	}
}
