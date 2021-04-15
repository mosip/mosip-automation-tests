package io.mosip.ivv.e2e.methods;

import static org.testng.Assert.assertTrue;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.service.BaseTestCase;
import io.restassured.response.Response;

public class CheckPridStatus extends BaseTestCaseUtil implements StepInterface {
	Logger logger = Logger.getLogger(CheckStatus.class);

	@Override
	public void run() throws RigInternalError {
		String pridStatus = null;
		if (step.getParameters() == null || step.getParameters().isEmpty()) {
			logger.error("Parameter is  missing from DSL step");
			assertTrue(false, "Paramter is  missing in step: " + step.getName());
		} else {
			pridStatus = step.getParameters().get(0);
		}
		for (String resDataPath : residentPathsPrid.keySet()) {
			String prid = residentPathsPrid.get(resDataPath);
			if (!StringUtils.isEmpty(prid))
				checkPridStatus(prid, pridStatus);
			else
				throw new RigInternalError("PRID cannot be null or empty");
		}
	}

	private void checkPridStatus(String prid, String pridStatus) throws RigInternalError {
		String status = null;
		List<String> statusList = Arrays.asList("booked", "expired", "pending_status");
		if ((pridStatus != null && !pridStatus.isEmpty()) && statusList.contains(pridStatus.toLowerCase())) {
			status = pridStatus.toLowerCase();
		} else {
			throw new RigInternalError("DSL argument cannot be null or empty or not supported");
		}
		String url = BaseTestCase.ApplnURI + props.getProperty("checkPridStatus") + prid;
		Response response = getRequest(url, "CheckPridStatus");
		if (!response.getBody().asString().toLowerCase().contains(status))
			throw new RigInternalError("Falied to check status of prid :" + prid);
	}

}
