package io.mosip.testrig.dslrig.dataprovider.util.internalapi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;


public final class InternalApiLogCollector {

	private static final int CONTEXT_TTL_HOURS = 12;
	private static final long CONTEXT_TTL_MILLIS = TimeUnit.HOURS.toMillis(CONTEXT_TTL_HOURS);
	private static final long CLEANUP_INTERVAL_MILLIS = TimeUnit.MINUTES.toMillis(1);

	private static final AtomicLong SEQ = new AtomicLong(1L);
	private static final Map<String, ContextBucket> BY_CONTEXT = new ConcurrentHashMap<>();
	private static volatile long lastCleanupMillis;

	private InternalApiLogCollector() {
	}

	private static final class ContextBucket {
		volatile long lastActivityMillis;
		final List<InternalApiLogExchange> entries = Collections.synchronizedList(new ArrayList<>());

		ContextBucket() {
			lastActivityMillis = System.currentTimeMillis();
		}

		void touch() {
			lastActivityMillis = System.currentTimeMillis();
		}
	}


	private static void maybeExpireStaleContexts() {
		long now = System.currentTimeMillis();
		if (now - lastCleanupMillis < CLEANUP_INTERVAL_MILLIS) {
			return;
		}
		synchronized (InternalApiLogCollector.class) {
			if (now - lastCleanupMillis < CLEANUP_INTERVAL_MILLIS) {
				return;
			}
			lastCleanupMillis = now;
			for (Iterator<Map.Entry<String, ContextBucket>> it = BY_CONTEXT.entrySet().iterator(); it.hasNext();) {
				Map.Entry<String, ContextBucket> e = it.next();
				if (now - e.getValue().lastActivityMillis >= CONTEXT_TTL_MILLIS) {
					it.remove();
				}
			}
		}
	}

	public static long nextSequence() {
		return SEQ.getAndIncrement();
	}


	public static final String GLOBAL_LOG_KEY = "_dslrig_internal_api_global_";

	public static void record(String contextKey, InternalApiLogExchange exchange) {
		if (exchange == null) {
			return;
		}
		maybeExpireStaleContexts();
		String key = contextKey == null || contextKey.isBlank() ? GLOBAL_LOG_KEY : contextKey;
		ContextBucket bucket = BY_CONTEXT.computeIfAbsent(key, k -> new ContextBucket());
		bucket.touch();
		bucket.entries.add(exchange);
	}


	public static List<InternalApiLogExchange> snapshot(String contextKey) {
		maybeExpireStaleContexts();
		if (contextKey == null || contextKey.isBlank()) {
			return snapshot(GLOBAL_LOG_KEY);
		}
		ContextBucket bucket = BY_CONTEXT.get(contextKey);
		if (bucket == null || bucket.entries.isEmpty()) {
			return List.of();
		}
		bucket.touch();
		synchronized (bucket.entries) {
			return List.copyOf(bucket.entries);
		}
	}


	public static List<InternalApiLogExchange> drain(String contextKey) {
		maybeExpireStaleContexts();
		String key = contextKey == null || contextKey.isBlank() ? GLOBAL_LOG_KEY : contextKey;
		ContextBucket removed = BY_CONTEXT.remove(key);
		if (removed == null || removed.entries.isEmpty()) {
			return List.of();
		}
		synchronized (removed.entries) {
			return new ArrayList<>(removed.entries);
		}
	}

	public static void clear(String contextKey) {
		maybeExpireStaleContexts();
		String key = contextKey == null || contextKey.isBlank() ? GLOBAL_LOG_KEY : contextKey;
		BY_CONTEXT.remove(key);
	}
}
