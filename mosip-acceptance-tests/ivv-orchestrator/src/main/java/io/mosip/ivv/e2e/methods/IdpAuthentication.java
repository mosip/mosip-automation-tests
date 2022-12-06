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
import io.restassured.response.Response;

public class IdpAuthentication extends BaseTestCaseUtil implements StepInterface {
	static Logger logger = Logger.getLogger(IdpAuthentication.class);
	private static final String AuthenticateUser = "idaData/AuthenticateUser/AuthenticateUser.yml";
	private static final String OtpUser = "idaData/SendOtpForIdp/SendOtp.yml";
	SimplePost authenticateUser=new SimplePost() ;

	@Override
	public void run() throws RigInternalError {
		
		String uins = null;
		String authType = null;
		String pin = null;
		List<String> uinList = null;
		String transactionId = "";

		Object[] testObj = authenticateUser.getYmlTestData(AuthenticateUser);

		TestCaseDTO test = (TestCaseDTO) testObj[0];
		
		
		if (step.getParameters() == null || step.getParameters().isEmpty() || step.getParameters().size() < 1) {
			logger.error("transactionId parameter is  missing from DSL step");
			throw new RigInternalError("transactionId paramter is  missing in step: " + step.getName());
		} else {
			transactionId = (String) oidcClientProp.get("transactionId");
			System.out.println(transactionId);

		}
		if(step.getParameters().size() == 4 && step.getParameters().get(1).startsWith("$$")) { //"e2e_PinAuthentication($$transactionId,$$uin)"
			uins = step.getParameters().get(1);
			if (uins.startsWith("$$")) {
				uins = step.getScenario().getVariables().get(uins);
				uinList = new ArrayList<>(Arrays.asList(uins.split("@@")));
			}
		}
		else if (step.getParameters().size() == 4) {
			uins = step.getParameters().get(1);
			if (!StringUtils.isBlank(uins))
				uinList = new ArrayList<>(Arrays.asList(uins.split("@@")));
		}else
			uinList = new ArrayList<>(uinPersonaProp.stringPropertyNames());
		
		
		if (step.getParameters().size() == 4) {
			authType = step.getParameters().get(2);

		}
		
		if (step.getParameters().size() == 4) {
			pin = step.getParameters().get(3);

		}
		
		if(step.getParameters().size() == 4 && step.getParameters().get(2).contains("OTP")) {
			
			Object[] testObjForOtp = authenticateUser.getYmlTestData(OtpUser);

			TestCaseDTO testForOtp = (TestCaseDTO) testObjForOtp[0];
			
			String input = testForOtp.getInput();
			
			for (String uin : uinList) {
				
				input = JsonPrecondtion.parseAndReturnJsonContent(input, transactionId, "transactionId");
				
				input = JsonPrecondtion.parseAndReturnJsonContent(input,
						uin, "individualId");
				
				testForOtp.setInput(input);
				
				
				
				try {
					authenticateUser.test(testForOtp);

					Response response = authenticateUser.response;
					System.out.println(response);
					

				} catch (AuthenticationTestException | AdminTestException e) {
					throw new RigInternalError(e.getMessage());

				}
			}
			
			
			
		}
		
		

		for (String uin : uinList) {

		String input = test.getInput();
		input = JsonPrecondtion.parseAndReturnJsonContent(input, transactionId, "transactionId");
		
		input = JsonPrecondtion.parseAndReturnJsonContent(input,
				uin, "individualId");
		input = JsonPrecondtion.parseAndReturnJsonContent(input,
				authType, "authFactorType");
		input = JsonPrecondtion.parseAndReturnJsonContent(input,
				pin, "challenge");
		
		test.setInput(input);

		try {
			authenticateUser.test(test);

			Response response = authenticateUser.response;
			if (response != null) {
				JSONObject jsonResp = new JSONObject(response.getBody().asString()); // "$$transactionId=e2e_OAuthDetailsRequest($$clientId)"

			}

		} catch (AuthenticationTestException | AdminTestException e) {
			throw new RigInternalError(e.getMessage());

		}

	}
	}
}
		
		