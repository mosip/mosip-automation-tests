package io.mosip.ivv.e2e.methods;

import org.apache.commons.lang.StringUtils;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;
import io.restassured.response.Response;

public class DeleteMockExpect extends BaseTestCaseUtil implements StepInterface {

	@Override
	public void run() throws RigInternalError {
		for (String resDataPath : residentPathsPrid.keySet()) {
			String id = residentPathsPrid.get(resDataPath);
			if (!StringUtils.isEmpty(prid))
				deleteExpectation(prid);
			
		}

	}

	private void deleteExpectation(String id) throws RigInternalError {
		String url = baseUrl + props.getProperty("deleteExpectation") + id;
		Response response = deleteReqestWithQueryParam(url, contextInuse, "DeleteApplication");
//		if (!response.getBody().asString().contains(prid))
//			throw new RigInternalError("Unable to DeleteApplication for Prid: "+prid);
	}	}


