package io.mosip.testrig.dslrig.dataprovider.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory auth tokens keyed by DSL {@code contextKey}, then role ({@code system}, {@code admin}, …).
 * Isolates parallel workers that share the same {@code urlBase} but use different credentials.
 */
public final class AuthTokenStore {

	public static final String ROLE_SYSTEM = "system";
	public static final String ROLE_ADMIN = "admin";
	public static final String ROLE_RESIDENT = "resident";
	public static final String ROLE_REGPROC = "regproc";
	public static final String ROLE_CRVS = "crvs";
	public static final String ROLE_PREREG = "prereg";

	private static final ConcurrentHashMap<String, ConcurrentHashMap<String, String>> BY_CONTEXT = new ConcurrentHashMap<>();

	private AuthTokenStore() {
	}

	public static String get(String contextKey, String role) {
		if (contextKey == null || contextKey.isBlank() || role == null) {
			return null;
		}
		Map<String, String> roles = BY_CONTEXT.get(normalize(contextKey));
		return roles == null ? null : roles.get(role);
	}

	public static void put(String contextKey, String role, String token) {
		if (contextKey == null || contextKey.isBlank() || role == null || token == null || token.isBlank()) {
			return;
		}
		BY_CONTEXT.computeIfAbsent(normalize(contextKey), k -> new ConcurrentHashMap<>()).put(role, token);
	}

	public static void remove(String contextKey, String role) {
		if (contextKey == null || contextKey.isBlank() || role == null) {
			return;
		}
		Map<String, String> roles = BY_CONTEXT.get(normalize(contextKey));
		if (roles != null) {
			roles.remove(role);
		}
	}

	public static void clearContext(String contextKey) {
		if (contextKey == null || contextKey.isBlank()) {
			return;
		}
		BY_CONTEXT.remove(normalize(contextKey));
	}

	/**
	 * Clears tokens for every context. Avoid during parallel DSL runs; prefer
	 * {@link #clearContext(String)} via {@code clearRunScopedCache}.
	 */
	public static void clearAll() {
		BY_CONTEXT.clear();
	}

	private static String normalize(String contextKey) {
		return contextKey.trim();
	}
}
