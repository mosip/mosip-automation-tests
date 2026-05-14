package io.mosip.testrig.dslrig.dataprovider.util.internalapi;

import io.mosip.testrig.dslrig.dataprovider.variables.VariableManager;

/**
 * Feature flag for outbound (internal) HTTP logging. When enabled, all calls
 * routed through {@link io.mosip.testrig.dslrig.dataprovider.util.RestClient}
 * capture request/response metadata for diagnostics.
 * <p>
 * Enable with either:
 * <ul>
 * <li>{@code -Ddslrig.internal.api.logging=true} on the JVM</li>
 * <li>Variable {@code internalApiLogging=yes} in the Packet Creator / Data
 * Provider context namespace (same mechanism as {@code enableDebug})</li>
 * </ul>
 */
public final class InternalApiLogging {

	private InternalApiLogging() {
	}

	public static boolean isEnabled(String contextKey) {
		if (Boolean.parseBoolean(System.getProperty("dslrig.internal.api.logging", "false"))) {
			return true;
		}
		if (contextKey == null || contextKey.isBlank()) {
			return false;
		}
		Object v = VariableManager.getVariableValue(contextKey, "internalApiLogging");
		return v != null && "yes".equalsIgnoreCase(String.valueOf(v).trim());
	}
}
