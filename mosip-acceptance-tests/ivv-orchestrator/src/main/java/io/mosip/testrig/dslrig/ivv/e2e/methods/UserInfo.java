package io.mosip.testrig.dslrig.ivv.e2e.methods;

import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import com.nimbusds.jose.jwk.RSAKey;
import io.mosip.testrig.apirig.dto.TestCaseDTO;
import io.mosip.testrig.apirig.esignet.testscripts.GetWithParam;
import io.mosip.testrig.apirig.esignet.testscripts.SimplePostForAutoGenId;
import io.mosip.testrig.apirig.resident.testscripts.SimplePostForAutoGenIdForUrlEncoded;
import io.mosip.testrig.apirig.testrunner.JsonPrecondtion;
import io.mosip.testrig.apirig.utils.AdminTestException;
import io.mosip.testrig.apirig.utils.AuthenticationTestException;
import io.mosip.testrig.apirig.utils.SecurityXSSException;
import io.mosip.testrig.apirig.testrunner.BaseTestCase;
import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.FeatureNotSupportedError;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testrig.dslrig.ivv.orchestrator.GlobalConstants;
import io.mosip.testrig.dslrig.ivv.orchestrator.PacketUtility;
import io.mosip.testrig.dslrig.ivv.orchestrator.dslConfigManager;
import io.restassured.response.Response;

public class UserInfo extends BaseTestCaseUtil implements StepInterface {
	static Logger logger = Logger.getLogger(UserInfo.class);
	private static final String AuthorizationCodeYml = "idaData/AuthorizationCode/AuthorizationCode.yml";
	private static final String GenerateTokenYml = "idaData/GenerateToken/GenerateToken.yml";
	private static final String GetUserInfoYml = "idaData/GetOidcUserInfo/GetOidcUserInfo.yml";
	SimplePostForAutoGenId authorizationCode = new SimplePostForAutoGenId();
	SimplePostForAutoGenIdForUrlEncoded generateToken = new SimplePostForAutoGenIdForUrlEncoded();
	GetWithParam getUserInfo = new GetWithParam();
	String clientId = "";
	String transactionId1 = "";
	String transactionId2 = "";
	String urlEncodedResp1 = "";
	String urlEncodedResp2 = "";
	String code = "";
	String redirectUri = "";
	String esignetAccessToken = "";
	String data = "";
	List<String> idType = BaseTestCase.getSupportedIdTypesValueFromActuator();

	static {
		if (dslConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	@Override
	public void run() throws RigInternalError, FeatureNotSupportedError {

		if (dslConfigManager.isInServiceNotDeployedList(GlobalConstants.ESIGNET)) {
			throw new FeatureNotSupportedError("eSignet is not deployed. Hence skipping the step");
		}

		Object[] testObjForAuthorizationCode = authorizationCode.getYmlTestData(AuthorizationCodeYml);
		Object[] testObjForGenerateToken = generateToken.getYmlTestData(GenerateTokenYml);
		Object[] testObjForGetUserInfo = generateToken.getYmlTestData(GetUserInfoYml);

		TestCaseDTO testAuthorization = (TestCaseDTO) testObjForAuthorizationCode[0];
		TestCaseDTO testGenerateToken = (TestCaseDTO) testObjForGenerateToken[0];
		TestCaseDTO testGetUserInfo = (TestCaseDTO) testObjForGetUserInfo[0];

		if (step.getParameters() == null || step.getParameters().isEmpty() || step.getParameters().size() < 1) {
			logger.error("transactionId parameter is  missing from DSL step");
			this.hasError = true;
			throw new RigInternalError("transactionId paramter is  missing in step: " + step.getName());
		} else {

			if (idType.contains("VID") || idType.contains("vid")) {
				transactionId2 = (String) step.getScenario().getOidcClientProp().get("transactionId2");
				urlEncodedResp2 = (String) step.getScenario().getOidcClientProp().get("urlEncodedResp2");
				logger.info(transactionId2);
			}

			else if (idType.contains("UIN") || idType.contains("uin")) {
				transactionId1 = (String) step.getScenario().getOidcClientProp().get("transactionId1");
				urlEncodedResp1 = (String) step.getScenario().getOidcClientProp().get("urlEncodedResp1");
				logger.info(transactionId1);
			}

			else {

				transactionId2 = (String) step.getScenario().getOidcClientProp().get("transactionId2");
				logger.info(transactionId2);

			}

		}
		if (step.getParameters().size() == 2 || step.getParameters().get(1).startsWith("$$")) {
			if (step.getParameters().get(1).startsWith("$$")) {

				clientId = (String) step.getScenario().getOidcClientProp().get("clientId");

			}

			else {
				clientId = step.getParameters().get(1);
			}
		}

		// Auth Code API Call

		String inputForAuthorization = testAuthorization.getInput();

		if (idType.contains("VID") || idType.contains("vid")) {

			inputForAuthorization = JsonPrecondtion.parseAndReturnJsonContent(inputForAuthorization, transactionId2,
					"transactionId");

			inputForAuthorization = JsonPrecondtion.parseAndReturnJsonContent(inputForAuthorization,
					step.getScenario().getOidcClientProp().getProperty("urlEncodedResp2"), "encodedHash");

			testAuthorization.setInput(inputForAuthorization);

			try {
				authorizationCode.test(testAuthorization);

				Response response = authorizationCode.response;
				if (response != null) {
					JSONObject jsonResp = new JSONObject(response.getBody().asString()); // "$$transactionId=e2e_OAuthDetailsRequest($$clientId)"
					code = jsonResp.getJSONObject("response").getString("code");
					redirectUri = jsonResp.getJSONObject("response").getString("redirectUri");
					step.getScenario().getOidcClientProp().put("code", code);

				}

			} catch (AuthenticationTestException | AdminTestException | NoSuchAlgorithmException | SecurityXSSException e) {
				this.hasError = true;
				throw new RigInternalError(e.getMessage());

			}
		}

		else if (idType.contains("UIN") || idType.contains("uin")) {

			inputForAuthorization = JsonPrecondtion.parseAndReturnJsonContent(inputForAuthorization, transactionId1,
					"transactionId");
			inputForAuthorization = JsonPrecondtion.parseAndReturnJsonContent(inputForAuthorization,
					step.getScenario().getOidcClientProp().getProperty("urlEncodedResp1"), "encodedHash");

			testAuthorization.setInput(inputForAuthorization);

			try {
				authorizationCode.test(testAuthorization);

				Response response = authorizationCode.response;
				if (response != null) {
					JSONObject jsonResp = new JSONObject(response.getBody().asString()); // "$$transactionId=e2e_OAuthDetailsRequest($$clientId)"
					code = jsonResp.getJSONObject("response").getString("code");
					redirectUri = jsonResp.getJSONObject("response").getString("redirectUri");
					step.getScenario().getOidcClientProp().put("code", code);

				}

			} catch (AuthenticationTestException | AdminTestException | NoSuchAlgorithmException | SecurityXSSException e) {
				this.hasError = true;
				throw new RigInternalError(e.getMessage());

			}
		}

		String inputForGenerateToken = testGenerateToken.getInput();

		inputForGenerateToken = JsonPrecondtion.parseAndReturnJsonContent(inputForGenerateToken, clientId, "client_id");
		inputForGenerateToken = JsonPrecondtion.parseAndReturnJsonContent(inputForGenerateToken, code, "code");
		inputForGenerateToken = JsonPrecondtion.parseAndReturnJsonContent(inputForGenerateToken, redirectUri,
				"redirect_uri");
		String oidcJwkKey = (String) step.getScenario().getOidcPmsProp().get("oidcJwkKey" + step.getScenario().getId());

		String oidcJWKKeyString = oidcJwkKey;
		// String oidcJWKKeyString = props.getProperty("privateKey");
		logger.info("oidcJWKKeyString =" + oidcJWKKeyString);
		RSAKey oidcJWKKey1;
		try {
			oidcJWKKey1 = RSAKey.parse(oidcJWKKeyString);
			data = PacketUtility.signJWKKey(clientId, oidcJWKKey1);
			logger.info("oidcJWKKey1 =" + oidcJWKKey1);
		} catch (java.text.ParseException e) {
			logger.error(e.getMessage());
		}

		inputForGenerateToken = JsonPrecondtion.parseAndReturnJsonContent(inputForGenerateToken, data,
				"client_assertion");

		testGenerateToken.setInput(inputForGenerateToken);

		try {
			generateToken.test(testGenerateToken);
		} catch (NoSuchAlgorithmException | AuthenticationTestException | AdminTestException | SecurityXSSException e) {
			logger.error(e.getMessage());
		}

		Response response = generateToken.response;
		if (response != null) {
			JSONObject jsonResp = new JSONObject(response.getBody().asString());
			esignetAccessToken = jsonResp.get("access_token").toString();
			logger.info(jsonResp.toString());
		}

		// User Info API CALL //

		String inputForGetUserInfo = testGetUserInfo.getInput();

		inputForGetUserInfo = JsonPrecondtion.parseAndReturnJsonContent(inputForGetUserInfo, esignetAccessToken,
				"idpAccessToken");

		testGetUserInfo.setInput(inputForGetUserInfo);

		try {
			getUserInfo.test(testGetUserInfo);

			Response response2 = getUserInfo.response;
			logger.info(response2.toString());

		} catch (AuthenticationTestException | AdminTestException | SecurityXSSException e) {
			this.hasError = true;
			throw new RigInternalError(e.getMessage());

		}

	}

}
