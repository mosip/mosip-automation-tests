package io.mosip.testrig.dslrig.ivv.e2e.methods;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import io.mosip.testrig.apirig.auth.testscripts.OtpAuthNew;
import io.mosip.testrig.apirig.dto.TestCaseDTO;
import io.mosip.testrig.apirig.testrunner.JsonPrecondtion;
import io.mosip.testrig.apirig.utils.AdminTestException;
import io.mosip.testrig.apirig.utils.AuthenticationTestException;
import io.mosip.testrig.apirig.utils.SecurityXSSException;
import io.mosip.testrig.apirig.testrunner.BaseTestCase;
import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.FeatureNotSupportedError;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testrig.dslrig.ivv.orchestrator.dslConfigManager;

public class OtpAuthentication extends BaseTestCaseUtil implements StepInterface {
	static Logger logger = Logger.getLogger(OtpAuthentication.class);
	private static final String OTPAUTHYml = "idaData/OtpAuth/OtpAuth.yml";
	Properties uinResidentDataPathFinalProps = new Properties();

	OtpAuthNew otpauth = new OtpAuthNew();

	static {
		if (dslConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	@Override
	public void run() throws RigInternalError, FeatureNotSupportedError {

		String uins = null;
		String vids = null;
		List<String> uinList = null;
		List<String> idType = BaseTestCase.getSupportedIdTypesValueFromActuator();
		List<String> vidList = null;
		String emailId = "";

		Object[] casesListUIN = null;
		Object[] casesListVID = null;

		if (step.getParameters().isEmpty() || step.getParameters().size() < 1) {
			logger.error("Parameter is  missing from DSL step");
			this.hasError = true;
			throw new RigInternalError("Modality paramter is  missing in step: " + step.getName());
		}

		if (step.getParameters().size() == 5 && step.getParameters().get(4).startsWith("$$")) {
			emailId = step.getParameters().get(4);
			if (emailId.startsWith("$$")) {
				emailId = step.getScenario().getVariables().get(emailId);
			}
			if (emailId == null || (emailId != null && emailId.isBlank())) {
				// in somecases Email Id is not passed so OTP Authentication is not supported
				throw new FeatureNotSupportedError("Email id is Empty hence we cannot perform OTP Authentication");

			}
		}

		// Fetching UIN

		if (step.getParameters().size() == 5) { // "e2e_otpAuthentication(uin,$$uin,vid,$$vid,$$email)"
			uins = step.getParameters().get(1);

			if (uins.startsWith("$$")) {
				uins = step.getScenario().getVariables().get(uins);
				uinList = new ArrayList<>(Arrays.asList(uins.split("@@")));
			}
		} else
			uinList = new ArrayList<>(step.getScenario().getUinPersonaProp().stringPropertyNames());

		// Fetching VID

		if (step.getParameters().size() == 5) { // "e2e_otpAuthentication(uin,$$uin,vid,$$vid,$$email)"
			vids = step.getParameters().get(3);
			if (vids.startsWith("$$")) {
				vids = step.getScenario().getVariables().get(vids);
				vidList = new ArrayList<>(Arrays.asList(vids.split("@@")));
			}
		} else
			vidList = new ArrayList<>(step.getScenario().getVidPersonaProp().stringPropertyNames());

		if (BaseTestCase.getSupportedIdTypesValueFromActuator().contains("UIN")
				|| BaseTestCase.getSupportedIdTypesValueFromActuator().contains("uin")) {

			casesListUIN = otpauth.getYmlTestData(OTPAUTHYml);

		}

		else if (BaseTestCase.getSupportedIdTypesValueFromActuator().contains("VID")
				|| BaseTestCase.getSupportedIdTypesValueFromActuator().contains("vid")) {
			casesListVID = otpauth.getYmlTestData(OTPAUTHYml);
		}

		else {
			casesListUIN = otpauth.getYmlTestData(OTPAUTHYml);
			casesListVID = otpauth.getYmlTestData(OTPAUTHYml);
		}

		for (String uin : uinList) {
			Object[] testObj = otpauth.getYmlTestData(OTPAUTHYml);
			TestCaseDTO test = (TestCaseDTO) testObj[0];
			String input = test.getInput();

			if (idType.contains("UIN") || idType.contains("uin")) {
				casesListUIN = otpauth.getYmlTestData(OTPAUTHYml);
			}

			input = JsonPrecondtion.parseAndReturnJsonContent(input, uin, "individualId");
			input = JsonPrecondtion.parseAndReturnJsonContent(input, uin, "sendOtp.individualId");
			input = JsonPrecondtion.parseAndReturnJsonContent(input, emailId, "otpChannel");
			
			test.setEndPoint(test.getEndPoint().replace("$PartnerKey$", partnerKeyUrl));
			test.setEndPoint(test.getEndPoint().replace("$PartnerName$", partnerId));
			test.setEndPoint(test.getEndPoint().replace("uinnumber", uin));

			if (casesListUIN != null) {
				for (Object object : casesListUIN) {
					test.setInput(input);
					try {
						otpauth.test(test);
					} catch (AuthenticationTestException e) {
						logger.error(e.getMessage());
					} catch (AdminTestException e) {
						logger.error(e.getMessage());
					} catch (SecurityXSSException e) {
						logger.error(e.getMessage());
					}
				}
			}

		}

		for (String vid : vidList) {
			Object[] testObj = otpauth.getYmlTestData(OTPAUTHYml);
			TestCaseDTO test = (TestCaseDTO) testObj[0];
			String input = test.getInput();

			if (idType.contains("VID") || idType.contains("vid")) {
				casesListVID = otpauth.getYmlTestData(OTPAUTHYml);
			}

			input = JsonPrecondtion.parseAndReturnJsonContent(input, vid, "individualId");
			input = JsonPrecondtion.parseAndReturnJsonContent(input, vid, "sendOtp.individualId");
			input = JsonPrecondtion.parseAndReturnJsonContent(input, "VID", "individualIdType");
			input = JsonPrecondtion.parseAndReturnJsonContent(input, "VID", "sendOtp.individualIdType");
			input = JsonPrecondtion.parseAndReturnJsonContent(input, emailId, "otpChannel");

			test.setEndPoint(test.getEndPoint().replace("$PartnerKey$", partnerKeyUrl));
			test.setEndPoint(test.getEndPoint().replace("$PartnerName$", partnerId));
			test.setEndPoint(test.getEndPoint().replace("uinnumber", vid));

			if (casesListVID != null) {
				for (Object object : casesListVID) {
					test.setInput(input);
					try {
						otpauth.test(test);
					} catch (AuthenticationTestException | AdminTestException | SecurityXSSException e) {
						this.hasError = true;
						logger.error(e.getMessage());
						throw new RigInternalError("Otp Auth failed ");
					}
				}
			}

		}

	}
}
