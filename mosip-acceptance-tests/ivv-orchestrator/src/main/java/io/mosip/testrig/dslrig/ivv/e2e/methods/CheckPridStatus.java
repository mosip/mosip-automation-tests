package io.mosip.testrig.dslrig.ivv.e2e.methods;

import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import io.mosip.testrig.apirig.testrunner.BaseTestCase;
import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testrig.dslrig.ivv.orchestrator.dslConfigManager;
import io.restassured.response.Response;

public class CheckPridStatus extends BaseTestCaseUtil implements StepInterface {
	public static Logger logger = Logger.getLogger(CheckStatus.class);

	static {
		if (dslConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	@Override
	public void run() throws RigInternalError {
		String pridStatus = null;
		if (step.getParameters() == null || step.getParameters().isEmpty()) {
			logger.error("Parameter is  missing from DSL step");
			assertTrue(false, "Paramter is  missing in step: " + step.getName());
		} else {
			pridStatus = step.getParameters().get(0);
		}
		for (String resDataPath : step.getScenario().getResidentPathsPrid().keySet()) {
			String prid = step.getScenario().getResidentPathsPrid().get(resDataPath);
			if (!StringUtils.isEmpty(prid))
				checkPridStatus(prid, pridStatus);
			else {
				this.hasError = true;
				throw new RigInternalError("PRID cannot be null or empty");
			}
		}
	}

	private void checkPridStatus(String prid, String pridStatus) throws RigInternalError {
		String status = null;
		List<String> statusList = Arrays.asList("booked", "expired", "pending_status");
		if ((pridStatus != null && !pridStatus.isEmpty()) && statusList.contains(pridStatus.toLowerCase())) {
			status = pridStatus.toLowerCase();
		} else {

			this.hasError = true;
			throw new RigInternalError("DSL argument cannot be null or empty or not supported");
		}
		String url = BaseTestCase.ApplnURI + props.getProperty("checkPridStatus") + prid;
		Response response = getRequest(url, "CheckPridStatus", step);
		if (!response.getBody().asString().toLowerCase().contains(status)) {
			this.hasError = true;
			throw new RigInternalError("Falied to check status of prid :" + prid);
		}
	}

}
