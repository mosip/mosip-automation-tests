package io.mosip.ivv.e2e.methods;

import java.security.NoSuchAlgorithmException;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import io.mosip.admin.fw.util.AdminTestException;
import io.mosip.admin.fw.util.TestCaseDTO;
import io.mosip.authentication.fw.precon.JsonPrecondtion;
import io.mosip.authentication.fw.util.AuthenticationTestException;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.ivv.orchestrator.PacketUtility;
import io.mosip.testrunner.MosipTestRunner;
import io.mosip.testscripts.PostWithOnlyPathParam;
import io.mosip.testscripts.SimplePost;
import io.mosip.testscripts.SimplePostForAutoGenId;
import io.mosip.testscripts.SimplePut;
import io.restassured.response.Response;

public class OidcClient extends BaseTestCaseUtil implements StepInterface {
	static Logger logger = Logger.getLogger(OidcClient.class);
	
	private static final String CreatePolicyGroupYml = "idaData/PmsIntegration/DefinePolicyGroup/DefinePolicyGroup.yml";
	private static final String DefinePolicyYml = "idaData/PmsIntegration/DefinePolicy/DefinePolicy.yml";
	private static final String PublishPolicyYml = "idaData/PmsIntegration/PublishPolicy/PublishPolicy.yml";
	private static final String CreatePartnerYml = "idaData/PmsIntegration/CreatePartner/CreatePartner.yml";
	private static final String UploadCACertificateYml = "idaData/PmsIntegration/UploadCertificate/UploadCertificate.yml";
	private static final String UploadPartnerCertificateYml = "idaData/PmsIntegration/UploadCert/UploadCert.yml";
	private static final String RequestAPIKeyForAuthPartnerYml = "idaData/PmsIntegration/RequestAPIKey/RequestAPIKey.yml";
	private static final String ApproveAPIKeyYml = "idaData/PmsIntegration/ApproveAPIKey/ApproveAPIKey.yml";
	private static final String OidcClientYml = "idaData/OidcClient/OIDCClient.yml";
	SimplePostForAutoGenId createPolicyGroup = new SimplePostForAutoGenId();
	SimplePostForAutoGenId definePolicy = new SimplePostForAutoGenId();
	PostWithOnlyPathParam publishPolicy = new PostWithOnlyPathParam();
	SimplePostForAutoGenId createPartner = new SimplePostForAutoGenId();
	SimplePost uploadCACertificate = new SimplePost();
	SimplePost uploadInterCertificate = new SimplePost();
	SimplePost uploadPartnerCertificate = new SimplePost();
	SimplePostForAutoGenId requestAPIKeyForAuthPartner = new SimplePostForAutoGenId();
	SimplePut approveAPIKey = new SimplePut();
	SimplePostForAutoGenId oidcClient=new SimplePostForAutoGenId();
	
	@Override
	public void run() throws RigInternalError {

		String name = null;
		String policygroupId = null;
		String policyId = null;
		String partnerId = null;
		String policyName = null;
		String mappingkey = null;
		String clientId = null;
		Object[] testObj1 = createPolicyGroup.getYmlTestData(CreatePolicyGroupYml);

		TestCaseDTO test1 = (TestCaseDTO) testObj1[0];

		try {
			try {
				createPolicyGroup.test(test1);
			} catch (NoSuchAlgorithmException e) {
				logger.error(e.getMessage());
			}
			Response response = createPolicyGroup.response;
			if (response != null) {
				JSONObject jsonResp = new JSONObject(response.getBody().asString());
				name = jsonResp.getJSONObject("response").getString("name");
				policygroupId = jsonResp.getJSONObject("response").getString("id");

				step.getScenario().getOidcPmsProp().put("name", name);
				step.getScenario().getOidcPmsProp().put("policygroupId", policygroupId);

				System.out.println(name);
			}

		} catch (AuthenticationTestException | AdminTestException e) {
			this.hasError=true;throw new RigInternalError(e.getMessage());

		}

		// DefinePolicy Call

		Object[] testObj2 = definePolicy.getYmlTestData(DefinePolicyYml);

		TestCaseDTO test2 = (TestCaseDTO) testObj2[0];

		String inputForDefinePolicy = test2.getInput();

		inputForDefinePolicy = JsonPrecondtion.parseAndReturnJsonContent(inputForDefinePolicy, name, "policyGroupName");
		test2.setInput(inputForDefinePolicy);

		try {
			try {
				definePolicy.test(test2);
			} catch (NoSuchAlgorithmException e) {
				logger.error(e.getMessage());
			}
			Response response = definePolicy.response;
			if (response != null) {
				JSONObject jsonResp = new JSONObject(response.getBody().asString());
				policyId = jsonResp.getJSONObject("response").getString("id");
				policyName = jsonResp.getJSONObject("response").getString("name");
				step.getScenario().getOidcPmsProp().put("policyId", policyId); // "$$clientId=e2e_OidcClient()"

				System.out.println(policyId);
			}

		} catch (AuthenticationTestException | AdminTestException e) {
			this.hasError=true;throw new RigInternalError(e.getMessage());

		}

		// PublishPolicy Call

		Object[] testObj3 = publishPolicy.getYmlTestData(PublishPolicyYml);

		TestCaseDTO test3 = (TestCaseDTO) testObj3[0];

		String inputForPublishPolicy = test3.getInput();

		inputForPublishPolicy = JsonPrecondtion.parseAndReturnJsonContent(inputForPublishPolicy, policygroupId,
				"policygroupId");
		inputForPublishPolicy = JsonPrecondtion.parseAndReturnJsonContent(inputForPublishPolicy, policyId, "policyId");

		test3.setInput(inputForPublishPolicy);

		try {
			publishPolicy.test(test3);
			Response response = publishPolicy.response;
			if (response != null) {
				JSONObject jsonResp = new JSONObject(response.getBody().asString());

			}

		} catch (AuthenticationTestException | AdminTestException e) {
			this.hasError=true;throw new RigInternalError(e.getMessage());

		}

		// CreatePartner Call

		Object[] testObj4 = createPartner.getYmlTestData(CreatePartnerYml);

		TestCaseDTO test4 = (TestCaseDTO) testObj4[0];

		String inputForCreatePartner = test4.getInput();

		inputForCreatePartner = JsonPrecondtion.parseAndReturnJsonContent(inputForCreatePartner, name, "policyGroup");

		test4.setInput(inputForCreatePartner);

		try {
			try {
				createPartner.test(test4);
			} catch (NoSuchAlgorithmException e) {
				logger.error(e.getMessage());
			}
			Response response = createPartner.response;
			if (response != null) {
				JSONObject jsonResp = new JSONObject(response.getBody().asString());
				partnerId = jsonResp.getJSONObject("response").getString("partnerId");

				step.getScenario().getOidcPmsProp().put("partnerId", partnerId);
			}

		} catch (AuthenticationTestException | AdminTestException e) {
			this.hasError=true;throw new RigInternalError(e.getMessage());

		}

		// Upload CA Call

		Object[] testObj5 = uploadCACertificate.getYmlTestData(UploadCACertificateYml);

		TestCaseDTO test5 = (TestCaseDTO) testObj5[0];
		

		

		String inputForUploadCACertificate = test5.getInput();
		
		inputForUploadCACertificate = JsonPrecondtion.parseAndReturnJsonContent(inputForUploadCACertificate, partnerId,
				"partnerId");


		test5.setInput(inputForUploadCACertificate);

		try {
			uploadCACertificate.test(test5);
			Response response = uploadCACertificate.response;
			if (response != null) {
				JSONObject jsonResp = new JSONObject(response.getBody().asString());
			}

		} catch (AuthenticationTestException | AdminTestException e) {
			this.hasError=true;throw new RigInternalError(e.getMessage());

		}

		// Upload Inter Call

		TestCaseDTO test6 = (TestCaseDTO) testObj5[1];

		String inputForUploadInterCertificate = test6.getInput();
		inputForUploadInterCertificate = JsonPrecondtion.parseAndReturnJsonContent(inputForUploadInterCertificate, partnerId,
				"partnerId");
		test6.setInput(inputForUploadInterCertificate);
		try {
			uploadInterCertificate.test(test6);
			Response response = uploadInterCertificate.response;
			if (response != null) {
				JSONObject jsonResp = new JSONObject(response.getBody().asString());
			}

		} catch (AuthenticationTestException | AdminTestException e) {
			this.hasError=true;throw new RigInternalError(e.getMessage());

		}

		// Upload PartnerCertificate Call

		Object[] testObj7 = uploadPartnerCertificate.getYmlTestData(UploadPartnerCertificateYml);

		TestCaseDTO test7 = (TestCaseDTO) testObj7[0];

		String inputForUploadPartnerCertificate = test7.getInput();

		inputForUploadPartnerCertificate = JsonPrecondtion.parseAndReturnJsonContent(inputForUploadPartnerCertificate,
				partnerId, "partnerId");

		test7.setInput(inputForUploadPartnerCertificate);

		try {
			uploadPartnerCertificate.test(test7);
			Response response = uploadPartnerCertificate.response;
			if (response != null) {
				JSONObject jsonResp = new JSONObject(response.getBody().asString());
			}

		} catch (AuthenticationTestException | AdminTestException e) {
			throw new RigInternalError(e.getMessage());

		}

		// Request API Key For Auth Partner Call

		Object[] testObj8 = requestAPIKeyForAuthPartner.getYmlTestData(RequestAPIKeyForAuthPartnerYml);

		TestCaseDTO test8 = (TestCaseDTO) testObj8[0];

		String inputForRequestAPIKeyForAuthPartner = test8.getInput();

		inputForRequestAPIKeyForAuthPartner = JsonPrecondtion
				.parseAndReturnJsonContent(inputForRequestAPIKeyForAuthPartner, policyName, "policyName");

		test8.setEndPoint(test8.getEndPoint().replace("$PARTNERId$", partnerId));

		test8.setInput(inputForRequestAPIKeyForAuthPartner);

		try {
			try {
				requestAPIKeyForAuthPartner.test(test8);
			} catch (NoSuchAlgorithmException e) {
				logger.error(e.getMessage());
			}
			Response response = requestAPIKeyForAuthPartner.response;
			if (response != null) {
				JSONObject jsonResp = new JSONObject(response.getBody().asString()); 
				mappingkey = jsonResp.getJSONObject("response").getString("mappingkey");

				step.getScenario().getOidcPmsProp().put("mappingkey", partnerId);
			}

		} catch (AuthenticationTestException | AdminTestException e) {
			this.hasError=true;throw new RigInternalError(e.getMessage());

		}

		// Request API Key For Auth Partner Call

		Object[] testObj9 = approveAPIKey.getYmlTestData(ApproveAPIKeyYml);

		TestCaseDTO test9 = (TestCaseDTO) testObj9[0];
		
		test9.setEndPoint(test9.getEndPoint().replace("$MAPPINGKEY$", mappingkey));

		String inputForApproveAPIKey = test9.getInput();


		test9.setInput(inputForApproveAPIKey);

		try {
			approveAPIKey.test(test9);
			Response response = approveAPIKey.response;
			if (response != null) {
				JSONObject jsonResp = new JSONObject(response.getBody().asString());
			}

		} catch (AuthenticationTestException | AdminTestException e) {
			throw new RigInternalError(e.getMessage());

		}
				// OIDC_CLIENT Creation Call

				Object[] testObj11 = oidcClient.getYmlTestData(OidcClientYml);

				TestCaseDTO test11 = (TestCaseDTO) testObj11[0];
				
				String inputForOidcClient = PacketUtility.getJsonFromTemplate(test11.getInput(), test11.getInputTemplate());

				String oidcJwkKey = MosipTestRunner.generateJWKPublicKey();
				step.getScenario().getOidcPmsProp().put("oidcJwkKey"+step.getScenario().getId(), oidcJwkKey);
				
				if (inputForOidcClient.contains("$OIDCJWKKEY$")) {
					inputForOidcClient = inputForOidcClient.replace("$OIDCJWKKEY$", oidcJwkKey.toString());
				}
				
				//String inputForOidcClient = test11.getInput();
				
				if (inputForOidcClient.contains("$POLICYID$")) {
					inputForOidcClient = inputForOidcClient.replace("$POLICYID$", policyId);
				}
				
				if (inputForOidcClient.contains("$PARTNERID$")) {
					inputForOidcClient = inputForOidcClient.replace("$PARTNERID$", partnerId);
				}
				test11.setInput(inputForOidcClient);

				try {
					try {
						oidcClient.test(test11);
					} catch (NoSuchAlgorithmException e) {
						logger.error(e.getMessage());
					}
					Response response = oidcClient.response;
					if (response != null) {
						JSONObject jsonResp = new JSONObject(response.getBody().asString());
						clientId = jsonResp.getJSONObject("response").getString("clientId");
						step.getScenario().getOidcPmsProp().put("clientId", clientId);
						step.getScenario().getOidcClientProp().put("clientId", clientId);
					}

				} catch (AuthenticationTestException | AdminTestException e) {
					this.hasError=true;throw new RigInternalError(e.getMessage());

				}

	}
}
