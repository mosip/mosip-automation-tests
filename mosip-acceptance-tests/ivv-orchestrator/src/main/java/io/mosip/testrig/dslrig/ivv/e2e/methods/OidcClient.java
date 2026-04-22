package io.mosip.testrig.dslrig.ivv.e2e.methods;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import io.mosip.testrig.apirig.dto.TestCaseDTO;
import io.mosip.testrig.apirig.idrepo.testscripts.PostWithOnlyPathParam;
import io.mosip.testrig.apirig.auth.testscripts.SimplePost;
import io.mosip.testrig.apirig.auth.testscripts.SimplePostForAutoGenId;
import io.mosip.testrig.apirig.masterdata.testscripts.SimplePut;
import io.mosip.testrig.apirig.testrunner.JsonPrecondtion;
import io.mosip.testrig.apirig.utils.AdminTestException;
import io.mosip.testrig.apirig.utils.AdminTestUtil;
import io.mosip.testrig.apirig.utils.AuthenticationTestException;
import io.mosip.testrig.apirig.utils.SecurityXSSException;
import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.FeatureNotSupportedError;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testrig.dslrig.ivv.orchestrator.GlobalConstants;
import io.mosip.testrig.dslrig.ivv.orchestrator.PacketUtility;
import io.mosip.testrig.dslrig.ivv.orchestrator.dslConfigManager;
import io.restassured.response.Response;

public class OidcClient extends BaseTestCaseUtil implements StepInterface {
	static Logger logger = Logger.getLogger(OidcClient.class);

	private static final String RESPONSE = "response";
	private static final String POLICY_GROUP_ID = "policygroupId";
	private static final String POLICY_NAME = "policyName";
	private static final String PARTNER_ID = "partnerId";
	private static final String CLIENT_ID = "clientId";
	private static final String CREATE_POLICY_GROUP_YML = "idaData/PmsIntegration/DefinePolicyGroup/DefinePolicyGroup.yml";
	private static final String DEFINE_POLICY_YML = "idaData/PmsIntegration/DefinePolicy/DefinePolicy.yml";
	private static final String PUBLISH_POLICY_YML = "idaData/PmsIntegration/PublishPolicy/PublishPolicy.yml";
	private static final String CREATE_PARTNER_YML = "idaData/PmsIntegration/CreatePartner/CreatePartner.yml";
	private static final String UPLOAD_CA_CERTIFICATE_YML = "idaData/PmsIntegration/UploadCertificate/UploadCertificate.yml";
	private static final String UPLOAD_PARTNER_CERTIFICATE_YML = "idaData/PmsIntegration/UploadCert/UploadCert.yml";
	private static final String REQUEST_API_KEY_FOR_AUTH_PARTNER_YML = "idaData/PmsIntegration/RequestAPIKey/RequestAPIKey.yml";
	private static final String APPROVE_API_KEY_YML = "idaData/PmsIntegration/ApproveAPIKey/ApproveAPIKey.yml";
	private static final String OIDC_CLIENT_YML = "idaData/OidcClient/OIDCClient.yml";
	SimplePostForAutoGenId createPolicyGroup = new SimplePostForAutoGenId();
	SimplePostForAutoGenId definePolicy = new SimplePostForAutoGenId();
	PostWithOnlyPathParam publishPolicy = new PostWithOnlyPathParam();
	SimplePostForAutoGenId createPartner = new SimplePostForAutoGenId();
	SimplePost uploadCACertificate = new SimplePost();
	SimplePost uploadInterCertificate = new SimplePost();
	SimplePost uploadPartnerCertificate = new SimplePost();
	SimplePostForAutoGenId requestAPIKeyForAuthPartner = new SimplePostForAutoGenId();
	SimplePut approveAPIKey = new SimplePut();
	SimplePostForAutoGenId oidcClientRequest = new SimplePostForAutoGenId();

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

		String name = executePolicyGroupCreation();
		String policygroupId = step.getScenario().getOidcPmsProp().get(POLICY_GROUP_ID).toString();
		
		String policyId = executeDefinePolicy(name);
		String policyName = step.getScenario().getOidcPmsProp().get(POLICY_NAME) != null ? 
			step.getScenario().getOidcPmsProp().get(POLICY_NAME).toString() : null;

		executePublishPolicy(policygroupId, policyId);

		String partnerId = executeCreatePartner(name);

		executeUploadCertificates(partnerId);

		String mappingkey = executeRequestAPIKey(policyName, partnerId);

		executeApproveAPIKey(mappingkey);

		executeOidcClientCreation(policyId, partnerId);
	}

	private String executePolicyGroupCreation() throws RigInternalError {
		Object[] testObj1 = createPolicyGroup.getYmlTestData(CREATE_POLICY_GROUP_YML);
		TestCaseDTO test1 = (TestCaseDTO) testObj1[0];

		try {
			createPolicyGroup.test(test1);
		} catch (Exception e) {
			logger.error(e.getMessage());
			this.hasError = true;
			throw new RigInternalError(e.getMessage());
		}

		Response response = createPolicyGroup.response;
		if (response != null) {
			JSONObject jsonResp = new JSONObject(response.getBody().asString());
			String name = jsonResp.getJSONObject(RESPONSE).getString("name");
			String policygroupId = jsonResp.getJSONObject(RESPONSE).getString("id");
			step.getScenario().getOidcPmsProp().put("name", name);
			step.getScenario().getOidcPmsProp().put(POLICY_GROUP_ID, policygroupId);
			logger.info(name);
			return name;
		}
		return null;
	}

	private String executeDefinePolicy(String name) throws RigInternalError {
		Object[] testObj2 = definePolicy.getYmlTestData(DEFINE_POLICY_YML);
		TestCaseDTO test2 = (TestCaseDTO) testObj2[0];
		String inputForDefinePolicy = test2.getInput();
		inputForDefinePolicy = JsonPrecondtion.parseAndReturnJsonContent(inputForDefinePolicy, name, "policyGroupName");
		test2.setInput(inputForDefinePolicy);

		try {
			definePolicy.test(test2);
		} catch (Exception e) {
			logger.error(e.getMessage());
			this.hasError = true;
			throw new RigInternalError(e.getMessage());
		}

		Response response = definePolicy.response;
		if (response != null) {
			JSONObject jsonResp = new JSONObject(response.getBody().asString());
			String policyId = jsonResp.getJSONObject(RESPONSE).getString("id");
			String policyName = jsonResp.getJSONObject(RESPONSE).getString("name");
			step.getScenario().getOidcPmsProp().put("policyId", policyId);
			step.getScenario().getOidcPmsProp().put(POLICY_NAME, policyName);
			logger.info(policyId);
			return policyId;
		}
		return null;
	}

	private void executePublishPolicy(String policygroupId, String policyId) throws RigInternalError {
		Object[] testObj3 = publishPolicy.getYmlTestData(PUBLISH_POLICY_YML);
		TestCaseDTO test3 = (TestCaseDTO) testObj3[0];
		String inputForPublishPolicy = test3.getInput();
		inputForPublishPolicy = JsonPrecondtion.parseAndReturnJsonContent(inputForPublishPolicy, policygroupId, "policygroupId");
		inputForPublishPolicy = JsonPrecondtion.parseAndReturnJsonContent(inputForPublishPolicy, policyId, "policyId");
		test3.setInput(inputForPublishPolicy);

		try {
			publishPolicy.test(test3);
		} catch (AuthenticationTestException | AdminTestException | SecurityXSSException e) {
			logger.error(e.getMessage());
			this.hasError = true;
			throw new RigInternalError(e.getMessage());
		}
	}

	private String executeCreatePartner(String name) throws RigInternalError {
		Object[] testObj4 = createPartner.getYmlTestData(CREATE_PARTNER_YML);
		TestCaseDTO test4 = (TestCaseDTO) testObj4[0];
		String inputForCreatePartner = test4.getInput();
		inputForCreatePartner = JsonPrecondtion.parseAndReturnJsonContent(inputForCreatePartner, name, "policyGroup");
		test4.setInput(inputForCreatePartner);

		try {
			createPartner.test(test4);
		} catch (Exception e) {
			logger.error(e.getMessage());
			this.hasError = true;
			throw new RigInternalError(e.getMessage());
		}

		Response response = createPartner.response;
		if (response != null) {
			JSONObject jsonResp = new JSONObject(response.getBody().asString());
			String partnerId = jsonResp.getJSONObject(RESPONSE).getString(PARTNER_ID);
			step.getScenario().getOidcPmsProp().put(PARTNER_ID, partnerId);
			return partnerId;
		}
		return null;
	}

	private void executeUploadCertificates(String partnerId) throws RigInternalError {
		executeUploadCACertificate(partnerId);
		executeUploadInterCertificate(partnerId);
		executeUploadPartnerCertificate(partnerId);
	}

	private void executeUploadCACertificate(String partnerId) throws RigInternalError {
		Object[] testObj5 = uploadCACertificate.getYmlTestData(UPLOAD_CA_CERTIFICATE_YML);
		TestCaseDTO test5 = (TestCaseDTO) testObj5[0];
		String inputForUploadCACertificate = test5.getInput();
		inputForUploadCACertificate = JsonPrecondtion.parseAndReturnJsonContent(inputForUploadCACertificate, partnerId, PARTNER_ID);
		test5.setInput(inputForUploadCACertificate);

		try {
			uploadCACertificate.test(test5);
		} catch (AuthenticationTestException | AdminTestException | SecurityXSSException e) {
			logger.error(e.getMessage());
			this.hasError = true;
			throw new RigInternalError(e.getMessage());
		}
	}

	private void executeUploadInterCertificate(String partnerId) throws RigInternalError {
		Object[] testObj5 = uploadCACertificate.getYmlTestData(UPLOAD_CA_CERTIFICATE_YML);
		TestCaseDTO test6 = (TestCaseDTO) testObj5[1];
		String inputForUploadInterCertificate = test6.getInput();
		inputForUploadInterCertificate = JsonPrecondtion.parseAndReturnJsonContent(inputForUploadInterCertificate, partnerId, PARTNER_ID);
		test6.setInput(inputForUploadInterCertificate);

		try {
			uploadInterCertificate.test(test6);
		} catch (AuthenticationTestException | AdminTestException | SecurityXSSException e) {
			logger.error(e.getMessage());
			this.hasError = true;
			throw new RigInternalError(e.getMessage());
		}
	}

	private void executeUploadPartnerCertificate(String partnerId) throws RigInternalError {
		Object[] testObj7 = uploadPartnerCertificate.getYmlTestData(UPLOAD_PARTNER_CERTIFICATE_YML);
		TestCaseDTO test7 = (TestCaseDTO) testObj7[0];
		String inputForUploadPartnerCertificate = test7.getInput();
		inputForUploadPartnerCertificate = JsonPrecondtion.parseAndReturnJsonContent(inputForUploadPartnerCertificate, partnerId, PARTNER_ID);
		test7.setInput(inputForUploadPartnerCertificate);

		try {
			uploadPartnerCertificate.test(test7);
		} catch (AuthenticationTestException | AdminTestException | SecurityXSSException e) {
			logger.error(e.getMessage());
			this.hasError = true;
			throw new RigInternalError(e.getMessage());
		}
	}

	private String executeRequestAPIKey(String policyName, String partnerId) throws RigInternalError {
		Object[] testObj8 = requestAPIKeyForAuthPartner.getYmlTestData(REQUEST_API_KEY_FOR_AUTH_PARTNER_YML);
		TestCaseDTO test8 = (TestCaseDTO) testObj8[0];
		String inputForRequestAPIKeyForAuthPartner = test8.getInput();
		inputForRequestAPIKeyForAuthPartner = JsonPrecondtion.parseAndReturnJsonContent(inputForRequestAPIKeyForAuthPartner, policyName, POLICY_NAME);
		test8.setEndPoint(test8.getEndPoint().replace("$PARTNERId$", partnerId));
		test8.setInput(inputForRequestAPIKeyForAuthPartner);

		try {
			requestAPIKeyForAuthPartner.test(test8);
		} catch (Exception e) {
			logger.error(e.getMessage());
			this.hasError = true;
			throw new RigInternalError(e.getMessage());
		}

		Response response = requestAPIKeyForAuthPartner.response;
		if (response != null) {
			JSONObject jsonResp = new JSONObject(response.getBody().asString());
			String mappingkey = jsonResp.getJSONObject(RESPONSE).getString("mappingkey");
			step.getScenario().getOidcPmsProp().put("mappingkey", partnerId);
			return mappingkey;
		}
		return null;
	}

	private void executeApproveAPIKey(String mappingkey) throws RigInternalError {
		Object[] testObj9 = approveAPIKey.getYmlTestData(APPROVE_API_KEY_YML);
		TestCaseDTO test9 = (TestCaseDTO) testObj9[0];
		test9.setEndPoint(test9.getEndPoint().replace("$MAPPINGKEY$", mappingkey));
		String inputForApproveAPIKey = test9.getInput();
		test9.setInput(inputForApproveAPIKey);

		try {
			approveAPIKey.test(test9);
		} catch (AdminTestException | SecurityXSSException e) {
			logger.error(e.getMessage());
			this.hasError = true;
			throw new RigInternalError(e.getMessage());
		}
	}

	private String executeOidcClientCreation(String policyId, String partnerId) throws RigInternalError {
		Object[] testObj11 = oidcClientRequest.getYmlTestData(OIDC_CLIENT_YML);
		TestCaseDTO test11 = (TestCaseDTO) testObj11[0];
		String inputForOidcClient = PacketUtility.getJsonFromTemplate(test11.getInput(), test11.getInputTemplate());
		String oidcJwkKey = AdminTestUtil.generateJWKPublicKey();

		step.getScenario().getOidcPmsProp().put("oidcJwkKey" + step.getScenario().getId(), oidcJwkKey);
		inputForOidcClient = inputForOidcClient.replace("$OIDCJWKKEY$", oidcJwkKey);
		inputForOidcClient = inputForOidcClient.replace("$POLICYID$", policyId);
		inputForOidcClient = inputForOidcClient.replace("$PARTNERID$", partnerId);
		test11.setInput(inputForOidcClient);

		try {
			oidcClientRequest.test(test11);
		} catch (Exception e) {
			logger.error(e.getMessage());
			this.hasError = true;
			throw new RigInternalError(e.getMessage());
		}

		Response response = oidcClientRequest.response;
		if (response != null) {
			JSONObject jsonResp = new JSONObject(response.getBody().asString());
			String clientId = jsonResp.getJSONObject(RESPONSE).getString(CLIENT_ID);
			step.getScenario().getOidcPmsProp().put(CLIENT_ID, clientId);
			step.getScenario().getOidcClientProp().put(CLIENT_ID, clientId);
			return clientId;
		}
		return null;
	}
}
