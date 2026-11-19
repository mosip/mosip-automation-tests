package io.mosip.testrig.dslrig.dataprovider.util.internalapi;


public final class InternalApiLogging {

	private InternalApiLogging() {
	}

	/**
	 * Always capture outbound calls when a context exists so the orchestrator can
	 * attach failed-only APIs to the DSL report even when {@code enableDebug=no}.
	 * Report attachment itself is gated in the orchestrator.
	 */
	public static boolean isEnabled(String contextKey) {
		return contextKey != null && !contextKey.isBlank();
	}
}
