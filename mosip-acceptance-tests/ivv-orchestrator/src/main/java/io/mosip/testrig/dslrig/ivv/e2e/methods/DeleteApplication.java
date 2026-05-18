package io.mosip.testrig.dslrig.ivv.e2e.methods;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testrig.dslrig.ivv.orchestrator.dslConfigManager;
import io.restassured.response.Response;

public class DeleteApplication extends BaseTestCaseUtil implements StepInterface {
	public static Logger logger = Logger.getLogger(DeleteApplication.class);

	static {
		if (dslConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	@Override
	public void run() throws RigInternalError {
		for (String resDataPath : step.getScenario().getResidentPathsPrid().keySet()) {
			String prid = step.getScenario().getResidentPathsPrid().get(resDataPath);
			if (!StringUtils.isEmpty(prid))
				deleteApplication(prid);
			else {
				this.hasError = true;
				throw new RigInternalError("PRID cannot be null or empty");
			}
		}

	}

	private void deleteApplication(String prid) throws RigInternalError {
		String url = baseUrl + props.getProperty("deleteApplication") + prid;
		Response response = deleteRequestWithQueryParam(url, step.getScenario().getCurrentStep(), "DeleteApplication",
				step);
		if (!response.getBody().asString().contains(prid)) {

			this.hasError = true;
			throw new RigInternalError("Unable to DeleteApplication for Prid: " + prid);
		}
	}

}
