package io.mosip.ivv.e2e.methods;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import io.mosip.admin.fw.util.AdminTestException;
import io.mosip.admin.fw.util.TestCaseDTO;
import io.mosip.authentication.fw.precon.JsonPrecondtion;
import io.mosip.authentication.fw.util.AuthenticationTestException;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testscripts.SimplePost;
import io.mosip.testscripts.SimplePostForAutoGenIdForUrlEncoded;
import io.restassured.response.Response;

public class UserInfo extends BaseTestCaseUtil implements StepInterface {
	static Logger logger = Logger.getLogger(UserInfo.class);
	private static final String AuthorizationCode = "idaData/AuthorizationCode/AuthorizationCode.yml";
	private static final String GenerateToken = "idaData/GenerateToken/GenerateToken.yml";
	SimplePost authorizationCode = new SimplePost();
	SimplePostForAutoGenIdForUrlEncoded generateToken = new SimplePostForAutoGenIdForUrlEncoded();

	@Override
	public void run() throws RigInternalError {

		String clientId = "";
		String transactionId = "";
		String code ="";

		Object[] testObjForAuthorizationCode = authorizationCode.getYmlTestData(AuthorizationCode);
		Object[] testObjForGenerateToken = generateToken.getYmlTestData(GenerateToken);

		TestCaseDTO testAuthorization = (TestCaseDTO) testObjForAuthorizationCode[0];
		TestCaseDTO testGenerateToken = (TestCaseDTO) testObjForGenerateToken[0];

		if (step.getParameters() == null || step.getParameters().isEmpty() || step.getParameters().size() < 1) {
			logger.error("transactionId parameter is  missing from DSL step");
			throw new RigInternalError("transactionId paramter is  missing in step: " + step.getName());
		} else {
			transactionId = (String) oidcClientProp.get("transactionId");
			System.out.println(transactionId);

		}
		if (step.getParameters().size() == 2 && step.getParameters().get(1).startsWith("$$")) {
			clientId = (String) oidcClientProp.get("clientId");
		}

		String inputForAuthorization = testAuthorization.getInput();
		inputForAuthorization = JsonPrecondtion.parseAndReturnJsonContent(inputForAuthorization, transactionId,
				"transactionId");

		testAuthorization.setInput(inputForAuthorization);

		try {
			authorizationCode.test(testAuthorization);

			Response response = authorizationCode.response;
			if (response != null) {
				JSONObject jsonResp = new JSONObject(response.getBody().asString()); // "$$transactionId=e2e_OAuthDetailsRequest($$clientId)"
                 code = jsonResp.getJSONObject("response").getString("code"); 
		        
		        oidcClientProp.put("code", code); 
		        
		        System.out.println(code);
				
				System.out.println(jsonResp.toString());
			}

		} catch (AuthenticationTestException | AdminTestException e) {
			throw new RigInternalError(e.getMessage());

		}
		
		
		
		String inputForGenerateToken = testGenerateToken.getInput();
		
		
		inputForGenerateToken = JsonPrecondtion.parseAndReturnJsonContent(inputForGenerateToken, clientId, "client_id");
		inputForGenerateToken = JsonPrecondtion.parseAndReturnJsonContent(inputForGenerateToken, code, "code");

		testGenerateToken.setInput(inputForGenerateToken);

		
			try {
				generateToken.test(testGenerateToken);
			} catch (NoSuchAlgorithmException | AuthenticationTestException | AdminTestException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			Response response = authorizationCode.response;
			if (response != null) {
				JSONObject jsonResp = new JSONObject(response.getBody().asString());
				System.out.println(jsonResp.toString());
			}

		

		}
		
	}
