package io.mosip.testrig.dslrig.dataprovider.biometric;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe MDS port tracking keyed by DSL {@code contextKey}.
 */
public final class MdsPortRegistry {

	private static final ConcurrentHashMap<String, Integer> PORTS = new ConcurrentHashMap<>();

	private MdsPortRegistry() {
	}

	public static void put(String contextKey, int port) {
		if (contextKey == null || contextKey.isBlank()) {
			return;
		}
		PORTS.put(portKey(contextKey), port);
	}

	public static Integer get(String contextKey) {
		if (contextKey == null || contextKey.isBlank()) {
			return null;
		}
		return PORTS.get(portKey(contextKey));
	}

	public static void remove(String contextKey) {
		if (contextKey == null || contextKey.isBlank()) {
			return;
		}
		PORTS.remove(portKey(contextKey));
	}

	private static String portKey(String contextKey) {
		return "port_" + contextKey.trim();
	}
}
