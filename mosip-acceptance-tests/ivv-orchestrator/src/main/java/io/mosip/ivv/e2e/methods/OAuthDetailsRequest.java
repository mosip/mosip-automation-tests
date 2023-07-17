package io.mosip.ivv.e2e.methods;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import io.mosip.testrig.apirig.admin.fw.util.AdminTestException;
import io.mosip.testrig.apirig.admin.fw.util.TestCaseDTO;
import io.mosip.testrig.apirig.authentication.fw.precon.JsonPrecondtion;
import io.mosip.testrig.apirig.authentication.fw.util.AuthenticationTestException;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.FeatureNotSupportedError;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testrig.apirig.kernel.util.ConfigManager;
import io.mosip.testrig.apirig.testscripts.SimplePostForAutoGenId;
import io.restassured.response.Response;

public class OAuthDetailsRequest extends BaseTestCaseUtil implements StepInterface {
	static Logger logger = Logger.getLogger(OAuthDetailsRequest.class);
	private static final String OAuthDetailsYml = "idaData/OAuthDetailsRequest/OAuthDetailsRequest.yml";
	SimplePostForAutoGenId oAuthDetails = new SimplePostForAutoGenId();

	@Override
	public void run() throws RigInternalError, FeatureNotSupportedError {

		// check if esignet is installed on the target system
		if (!ConfigManager.IseSignetDeployed()) {
			throw new FeatureNotSupportedError("eSignet is not deployed. Hence skipping the step");
		}

		String clientId = "";

		Object[] testObj = oAuthDetails.getYmlTestData(OAuthDetailsYml);

		TestCaseDTO test = (TestCaseDTO) testObj[0];

		if (!step.getParameters().isEmpty()) {

			if (step.getParameters().get(0).startsWith("$$")) {

				clientId = (String) step.getScenario().getOidcClientProp().get("clientId");

			}

			else {
				clientId = step.getParameters().get(0);
			}
			System.out.println(clientId);
		}

		String input = test.getInput();
		input = JsonPrecondtion.parseAndReturnJsonContent(input, clientId, "clientId");
		test.setInput(input);

		try {
			try {
				oAuthDetails.test(test);
			} catch (NoSuchAlgorithmException e) {
				logger.error(e.getMessage());
			}

			Response response = oAuthDetails.response;
			if (response != null) {
				JSONObject jsonResp = new JSONObject(response.getBody().asString()); // "$$transactionId=e2e_OAuthDetailsRequest($$clientId)"

				String transactionId = jsonResp.getJSONObject("response").getString("transactionId");

				if (step.getParameters().get(1).contains("transactionId1")) {
					step.getScenario().getOidcClientProp().put("transactionId1", transactionId); // "$$clientId=e2e_OidcClient()"
					Gson gson = new Gson();
					JsonObject json = gson.fromJson(response.getBody().asString(), JsonObject.class);
					String responseJsonString = json.getAsJsonObject("response").toString();

					MessageDigest digest = MessageDigest.getInstance("SHA-256");
					byte[] hash = digest.digest(responseJsonString.getBytes(StandardCharsets.UTF_8));
					String urlEncodedResp = Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
					step.getScenario().getOidcClientProp().put("urlEncodedResp1", urlEncodedResp);
					System.out.println(step.getScenario().getOidcClientProp());
				} else if (step.getParameters().get(1).contains("transactionId2")) {

					step.getScenario().getOidcClientProp().put("transactionId2", transactionId); // "$$clientId=e2e_OidcClient()"
					Gson gson = new Gson();
					JsonObject json = gson.fromJson(response.getBody().asString(), JsonObject.class);
					String responseJsonString = json.getAsJsonObject("response").toString();

					MessageDigest digest = MessageDigest.getInstance("SHA-256");
					byte[] hash = digest.digest(responseJsonString.getBytes(StandardCharsets.UTF_8));
					String urlEncodedResp = Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
					step.getScenario().getOidcClientProp().put("urlEncodedResp2", urlEncodedResp);
					System.out.println(step.getScenario().getOidcClientProp());

				}

			}

		} catch (AuthenticationTestException | AdminTestException | NoSuchAlgorithmException e) {
			this.hasError = true;
			throw new RigInternalError(e.getMessage());

		}

	}
}
