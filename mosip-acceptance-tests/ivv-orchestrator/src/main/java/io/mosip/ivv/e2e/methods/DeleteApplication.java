package io.mosip.ivv.e2e.methods;

import org.apache.commons.lang.StringUtils;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;
import io.restassured.response.Response;

public class DeleteApplication extends BaseTestCaseUtil implements StepInterface {

	@Override
	public void run() throws RigInternalError {
		for (String resDataPath : residentPathsPrid.keySet()) {
			String prid = residentPathsPrid.get(resDataPath);
			if (!StringUtils.isEmpty(prid))
				deleteApplication(prid);
			else
				throw new RigInternalError("PRID cannot be null or empty");
		}

	}

	private void deleteApplication(String prid) throws RigInternalError {
		String url = baseUrl + props.getProperty("deleteApplication") + prid;
		Response response = deleteReqestWithQueryParam(url, contextInuse, "DeleteApplication");
		if (!response.getBody().asString().contains(prid))
			throw new RigInternalError("Unable to DeleteApplication for Prid: "+prid);
	}

}
