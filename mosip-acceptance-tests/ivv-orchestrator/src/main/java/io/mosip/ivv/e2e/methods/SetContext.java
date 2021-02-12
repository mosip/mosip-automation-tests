package io.mosip.ivv.e2e.methods;

import static io.restassured.RestAssured.given;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.e2e.constant.E2EConstants;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.service.BaseTestCase;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class SetContext extends BaseTestCaseUtil implements StepInterface {
	Logger logger = Logger.getLogger(SetContext.class);
	
	 

@Override
public void run() throws RigInternalError {
	constantIntializer();
	String contextKeyValue=null;
	if (step.getParameters() == null || step.getParameters().isEmpty() ||step.getParameters().size()<1) {
		logger.warn("SetContext Arugemnt is  Missing : Please pass the argument from DSL sheet");
	} else {
		contextKeyValue=step.getParameters().get(0);
		contextKey.put("contextKey",contextKeyValue );
	}
	createContext(contextKeyValue,BaseTestCase.ApplnURI+"/");
	
}

	private String createContext(String key, String baseUrl) {
		String url = this.baseUrl + "/servercontext/" + key;
		JSONObject jsonReq = new JSONObject();
		jsonReq.put("urlBase", baseUrl);
		jsonReq.put("mosip.test.baseurl", baseUrl);
		jsonReq.put("mosip.test.regclient.machineid", E2EConstants.MACHINE_ID);
		jsonReq.put("mosip.test.regclient.centerid", E2EConstants.CENTER_ID);
		jsonReq.put("regclient.centerid", E2EConstants.CENTER_ID);
		jsonReq.put("mosip.test.regclient.userid", E2EConstants.USER_ID);
		jsonReq.put("prereg.operatorId", E2EConstants.USER_ID);
		jsonReq.put("mosip.test.regclient.password", E2EConstants.USER_PASSWD);
		jsonReq.put("prereg.password", E2EConstants.USER_PASSWD);
		jsonReq.put("mosip.test.regclient.supervisorid", E2EConstants.SUPERVISOR_ID);
		jsonReq.put("prereg.preconfiguredOtp", E2EConstants.PRECONFIGURED_OTP);

		Response response = given().contentType(ContentType.JSON).body(jsonReq.toString()).post(url);
		return response.getBody().asString();

	}
}
