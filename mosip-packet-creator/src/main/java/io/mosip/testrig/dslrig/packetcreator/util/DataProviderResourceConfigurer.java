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

	public static void configure(String path) {
		if (path == null || path.isEmpty()) {
			return;
		}
		try {
			DataProviderConstants.class.getMethod("setResource", String.class).invoke(null, path);
		} catch (NoSuchMethodException e) {
			DataProviderConstants.RESOURCE = path;
		} catch (Exception e) {
			logger.warn("Failed to set DataProviderConstants resource path", e);
		}
	}
}
