package io.mosip.testrig.dslrig.dataprovider.util.internalapi;

import io.mosip.testrig.dslrig.dataprovider.variables.VariableManager;


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
