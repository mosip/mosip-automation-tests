package io.mosip.testrig.dslrig.ivv.e2e.methods;

import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.restassured.response.Response;

public class DeleteMockExpect extends BaseTestCaseUtil implements StepInterface {

	@Override
	public void run() throws RigInternalError {
		
		String url = baseUrl + props.getProperty("deleteMockExpectation");
		Response response = deleteRequest(url, "deleteMockExpectation",step);
		
	}
}