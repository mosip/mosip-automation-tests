package io.mosip.testrig.dslrig.ivv.e2e.methods;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import io.mosip.testrig.apirig.admin.fw.util.AdminTestException;
import io.mosip.testrig.apirig.admin.fw.util.TestCaseDTO;
import io.mosip.testrig.apirig.authentication.fw.precon.JsonPrecondtion;
import io.mosip.testrig.apirig.authentication.fw.util.AuthenticationTestException;
import io.mosip.testrig.apirig.kernel.util.ConfigManager;
import io.mosip.testrig.apirig.service.BaseTestCase;
import io.mosip.testrig.apirig.testscripts.SimplePost;
import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.FeatureNotSupportedError;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;

public class IdpAuthentication extends BaseTestCaseUtil implements StepInterface {
	static Logger logger = Logger.getLogger(IdpAuthentication.class);
	private static final String AuthenticateUserYml = "idaData/AuthenticateUser/AuthenticateUser.yml";
	private static final String OtpUserYml = "idaData/SendOtpForIdp/SendOtp.yml";
	SimplePost authenticateUser = new SimplePost();

	@Override
	public void run() throws RigInternalError, FeatureNotSupportedError {
		
		// check if esignet is installed on the target system
		if (!ConfigManager.IseSignetDeployed()) {
			throw new FeatureNotSupportedError("eSignet is not deployed. Hence skipping the step");
		}

		String uins = null;
		String vids = null;
		String authType = null;
		String emailId = null;
		List<String> uinList = null;
		List<String> vidList = null;
		String transactionId1 = "";
		String transactionId2 = "";
		Object[] casesListUIN = null;
		Object[] casesListVID = null;
		List<String> idType = BaseTestCase.getSupportedIdTypesValueFromActuator();

		Object[] testObj = authenticateUser.getYmlTestData(AuthenticateUserYml);

		TestCaseDTO test = (TestCaseDTO) testObj[0];

		if (step.getParameters() == null || step.getParameters().isEmpty() || step.getParameters().size() < 1) {
			logger.error("transactionId parameter is  missing from DSL step");
			this.hasError=true;
			throw new RigInternalError("transactionId paramter is  missing in step: " + step.getName());
		} else {
			transactionId1 = (String) step.getScenario().getOidcClientProp().get("transactionId1");
			System.out.println(transactionId1);

		}

		if (step.getParameters() == null || step.getParameters().isEmpty() || step.getParameters().size() < 1) {
			logger.error("transa from DSL step");
			this.hasError=true;
			throw new RigInternalError(
					"transactionId parameter is  missingctionId paramter is  missing in step: " + step.getName());
		} else {
			transactionId2 = (String) step.getScenario().getOidcClientProp().get("transactionId2");
			System.out.println(transactionId2);

		}
		if (step.getParameters().size() == 6 && step.getParameters().get(1).startsWith("$$")) {
			uins = step.getParameters().get(1); //"e2e_IdpAuthentication($$transactionId1,$$uin,OTP,$$email,$$vid,$$transactionId2)"
			if (uins.startsWith("$$")) {
				uins = step.getScenario().getVariables().get(uins);
				uinList = new ArrayList<>(Arrays.asList(uins.split("@@")));
			}
		} else if (step.getParameters().size() == 6) {
			uins = step.getParameters().get(1);
			if (!StringUtils.isBlank(uins))
				uinList = new ArrayList<>(Arrays.asList(uins.split("@@")));
		} else
			uinList = new ArrayList<>(step.getScenario().getUinPersonaProp().stringPropertyNames());

		// Fetching VID
		if (step.getParameters().size() == 6 && step.getParameters().get(1).startsWith("$$")) {
			vids = step.getParameters().get(4);
			if (vids.startsWith("$$")) {
				vids = step.getScenario().getVariables().get(vids);
				vidList = new ArrayList<>(Arrays.asList(vids.split("@@")));
			}
		} else if (step.getParameters().size() == 6) {
			vids = step.getParameters().get(4);
			if (!StringUtils.isBlank(vids))
				vidList = new ArrayList<>(Arrays.asList(vids.split("@@")));
		} else
			vidList = new ArrayList<>(step.getScenario().getVidPersonaProp().stringPropertyNames());

		if (step.getParameters().size() == 6) {
			authType = step.getParameters().get(2);

		}


		if (step.getParameters().size() == 6) {
			emailId = step.getParameters().get(3);
			if (emailId.startsWith("$$")) {
				emailId = step.getScenario().getVariables().get(emailId);
			}

		}

		if (step.getParameters().size() == 6 && step.getParameters().get(2).contains("OTP")) {

			Object[] testObjForOtp = authenticateUser.getYmlTestData(OtpUserYml);

			if (BaseTestCase.getSupportedIdTypesValueFromActuator().contains("UIN")
					|| BaseTestCase.getSupportedIdTypesValueFromActuator().contains("uin")) {

				casesListUIN = authenticateUser.getYmlTestData(OtpUserYml);

			}

			else if (BaseTestCase.getSupportedIdTypesValueFromActuator().contains("VID")
					|| BaseTestCase.getSupportedIdTypesValueFromActuator().contains("vid")) {
				casesListVID = authenticateUser.getYmlTestData(OtpUserYml);
			}

			else {
				casesListUIN = authenticateUser.getYmlTestData(OtpUserYml);
				casesListVID = authenticateUser.getYmlTestData(OtpUserYml);
			}

			TestCaseDTO testForOtp = (TestCaseDTO) testObjForOtp[0];

			String input = testForOtp.getInput();

			for (String uin : uinList) {

				input = JsonPrecondtion.parseAndReturnJsonContent(input, transactionId1, "transactionId");

				input = JsonPrecondtion.parseAndReturnJsonContent(input, uin, "individualId");

				input = JsonPrecondtion.parseAndReturnJsonContent(input,
						step.getScenario().getOidcClientProp().getProperty("urlEncodedResp1"), "encodedHash");

				testForOtp.setInput(input);

				if (idType.contains("UIN") || idType.contains("uin")) {
					casesListUIN = authenticateUser.getYmlTestData(OtpUserYml);
				}
				if (casesListUIN != null) {
					for (Object object : casesListUIN) {
						test.setInput(input);
						test = (TestCaseDTO) object;
						try {
							authenticateUser.test(testForOtp);
						} catch (AuthenticationTestException e) {
							logger.error(e.getMessage());
						} catch (AdminTestException e) {
							logger.error(e.getMessage());
						}
					}
				}

			}

			for (String vid : vidList) {

				input = JsonPrecondtion.parseAndReturnJsonContent(input, transactionId2, "transactionId");

				input = JsonPrecondtion.parseAndReturnJsonContent(input, vid, "individualId");

				input = JsonPrecondtion.parseAndReturnJsonContent(input,
						step.getScenario().getOidcClientProp().getProperty("urlEncodedResp2"), "encodedHash");

				testForOtp.setInput(input);

				if (idType.contains("VID") || idType.contains("vid")) {
					casesListVID = authenticateUser.getYmlTestData(OtpUserYml);
				}
				if (casesListVID != null) {
					for (Object object : casesListVID) {
						test.setInput(input);
						test = (TestCaseDTO) object;
						try {
							authenticateUser.test(testForOtp);
						} catch (AuthenticationTestException e) {
							logger.error(e.getMessage());
						} catch (AdminTestException e) {
							logger.error(e.getMessage());
						}
					}
				}

			}

		}

		for (String uin : uinList) {

			if (idType.contains("UIN") || idType.contains("uin")) {
				casesListUIN = authenticateUser.getYmlTestData(AuthenticateUserYml);
			}

			if (BaseTestCase.getSupportedIdTypesValueFromActuator().contains("UIN")
					|| BaseTestCase.getSupportedIdTypesValueFromActuator().contains("uin")) {

				casesListUIN = authenticateUser.getYmlTestData(AuthenticateUserYml);

			}

			else if (BaseTestCase.getSupportedIdTypesValueFromActuator().contains("VID")
					|| BaseTestCase.getSupportedIdTypesValueFromActuator().contains("vid")) {
				casesListVID = authenticateUser.getYmlTestData(AuthenticateUserYml);
			}

			else {
				casesListUIN = authenticateUser.getYmlTestData(AuthenticateUserYml);
				casesListVID = authenticateUser.getYmlTestData(AuthenticateUserYml);
			}

			if (idType.contains("UIN") || idType.contains("uin")) {
				casesListUIN = authenticateUser.getYmlTestData(AuthenticateUserYml);
			}
			if (casesListUIN != null) {
				for (Object object : casesListUIN) {
					test = (TestCaseDTO) object;
					String input = test.getInput();
					input = JsonPrecondtion.parseAndReturnJsonContent(input, transactionId1, "transactionId");
					input = JsonPrecondtion.parseAndReturnJsonContent(input,
							step.getScenario().getOidcClientProp().getProperty("urlEncodedResp1"), "encodedHash");
					input = JsonPrecondtion.parseAndReturnJsonContent(input, uin, "individualId");
					input = JsonPrecondtion.parseAndReturnJsonContent(input, authType, "authFactorType");
					input = JsonPrecondtion.parseAndReturnJsonContent(input, emailId, "challenge");

					test.setInput(input);

					try {
						authenticateUser.test(test);
					} catch (AuthenticationTestException e) {
						logger.error(e.getMessage());
					} catch (AdminTestException e) {
						logger.error(e.getMessage());
					}
				}
			}

		}
		for (String vid : vidList) {

			if (idType.contains("VID") || idType.contains("vid")) {
				casesListVID = authenticateUser.getYmlTestData(AuthenticateUserYml);
			}

			if (BaseTestCase.getSupportedIdTypesValueFromActuator().contains("VID")
					|| BaseTestCase.getSupportedIdTypesValueFromActuator().contains("vid")) {

				casesListVID = authenticateUser.getYmlTestData(AuthenticateUserYml);

			}

			else if (BaseTestCase.getSupportedIdTypesValueFromActuator().contains("UIN")
					|| BaseTestCase.getSupportedIdTypesValueFromActuator().contains("uin")) {
				casesListUIN = authenticateUser.getYmlTestData(AuthenticateUserYml);
			}

			else {
				casesListUIN = authenticateUser.getYmlTestData(AuthenticateUserYml);
				casesListVID = authenticateUser.getYmlTestData(AuthenticateUserYml);
			}

			if (casesListVID != null) {
				for (Object object : casesListVID) {

					test = (TestCaseDTO) object;

					String input = test.getInput();
					input = JsonPrecondtion.parseAndReturnJsonContent(input, transactionId2, "transactionId");
					input = JsonPrecondtion.parseAndReturnJsonContent(input,
							step.getScenario().getOidcClientProp().getProperty("urlEncodedResp2"), "encodedHash");
					input = JsonPrecondtion.parseAndReturnJsonContent(input, vid, "individualId");
					input = JsonPrecondtion.parseAndReturnJsonContent(input, authType, "authFactorType");
					input = JsonPrecondtion.parseAndReturnJsonContent(input, emailId, "challenge");

					test.setInput(input);

					try {
						authenticateUser.test(test);
					} catch (AuthenticationTestException e) {
						logger.error(e.getMessage());
					} catch (AdminTestException e) {
						logger.error(e.getMessage());
					}
				}
			}

		}
	}
}
