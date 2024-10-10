package io.mosip.testrig.dslrig.ivv.e2e.methods;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testrig.dslrig.ivv.orchestrator.dslConfigManager;
import io.restassured.response.Response;

public class ClearDeviceCertCache extends BaseTestCaseUtil implements StepInterface {
	public static Logger logger = Logger.getLogger(ClearDeviceCertCache.class);

	static {
		if (dslConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	@Override
	public void run() throws RigInternalError {
		Response response = null;

		String clearDeviceCertCache = baseUrl + props.getProperty("clearDeviceCertCache");
		response = getRequest(clearDeviceCertCache, "Clear device cert from mock mds cache", step);
		if (response != null && response.getStatusCode() == 200) {
			String responseString = response.getBody().asString();
			if (responseString.contains("Failed")) {
				this.hasError = true;
				throw new RigInternalError("Clearing device cert from mock mds cache Failed ");
			}
			logger.info(responseString);

		} else {
			this.hasError = true;
			throw new RigInternalError("Clearing device cert from mock mds cache Failed ");
		}
	}
}
