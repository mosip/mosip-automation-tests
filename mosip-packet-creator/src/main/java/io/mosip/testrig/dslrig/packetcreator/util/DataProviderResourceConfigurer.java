package io.mosip.testrig.dslrig.packetcreator.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.mosip.testrig.dslrig.dataprovider.util.DataProviderConstants;

/**
 * Sets the dataprovider resource root across dslrig-dataprovider versions that may or may not
 * expose {@link DataProviderConstants#setResource(String)}.
 */
public final class DataProviderResourceConfigurer {

	private static final Logger logger = LoggerFactory.getLogger(DataProviderResourceConfigurer.class);

	private DataProviderResourceConfigurer() {
	}

	private static final String DEFAULT_RESOURCE_PATH = "src/main/resource/";

	public static void configure(String path) {
		String resolved = (path == null || path.isEmpty()) ? DEFAULT_RESOURCE_PATH : path;
		try {
			DataProviderConstants.class.getMethod("setResource", String.class).invoke(null, resolved);
		} catch (NoSuchMethodException e) {
			DataProviderConstants.RESOURCE = resolved;
		} catch (Exception e) {
			logger.error("Failed to set DataProviderConstants resource path to {}", resolved, e);
			throw new IllegalStateException("Failed to configure dataprovider resource path: " + resolved, e);
		}
	}
}
