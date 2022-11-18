package io.mosip.ivv.e2e.methods;

import org.apache.commons.lang.StringUtils;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;
import io.restassured.response.Response;

public class DeleteMockExpect extends BaseTestCaseUtil implements StepInterface {

	@Override
	public void run() throws RigInternalError {
		
		String url = baseUrl + props.getProperty("deleteMockExpectation");
		Response response = deleteRequest(url, "deleteMockExpectation");
		
	}
}