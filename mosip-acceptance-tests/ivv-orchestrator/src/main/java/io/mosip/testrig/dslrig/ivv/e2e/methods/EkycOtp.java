package io.mosip.testrig.dslrig.ivv.e2e.methods;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import io.mosip.testrig.apirig.dto.TestCaseDTO;
import io.mosip.testrig.apirig.testrunner.JsonPrecondtion;
import io.mosip.testrig.apirig.utils.AdminTestException;
import io.mosip.testrig.apirig.utils.AuthenticationTestException;
import io.mosip.testrig.apirig.testrunner.BaseTestCase;
import io.mosip.testrig.apirig.testscripts.OtpAuthNew;
import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.FeatureNotSupportedError;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testrig.dslrig.ivv.orchestrator.dslConfigManager;

@Scope("prototype")
@Component
public class EkycOtp extends BaseTestCaseUtil implements StepInterface {
	static Logger logger = Logger.getLogger(EkycOtp.class);
	private static final String EKYCOTP = "idaData/EkycOtp/EkycOtp.yml";
	Properties uinResidentDataPathFinalProps = new Properties();

	static {
		if (dslConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

   OtpAuthNew otpauth = new OtpAuthNew();

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
				throw new FeatureNotSupportedError("Email id is Empty hence we cannot perform Ekyc OTP Authentication");

			}
		}

		if (step.getParameters().size() == 5) {
			uins = step.getParameters().get(1);
			if (uins.startsWith("$$")) {
				uins = step.getScenario().getVariables().get(uins);
				uinList = new ArrayList<>(Arrays.asList(uins.split("@@")));
			}
		} else
			uinList = new ArrayList<>(step.getScenario().getUinPersonaProp().stringPropertyNames());

		if (step.getParameters().size() == 5) {
			vids = step.getParameters().get(3);
			if (vids.startsWith("$$")) {
				vids = step.getScenario().getVariables().get(vids);
				vidList = new ArrayList<>(Arrays.asList(vids.split("@@")));
			}
		} else
			vidList = new ArrayList<>(step.getScenario().getVidPersonaProp().stringPropertyNames());

		if (BaseTestCase.getSupportedIdTypesValueFromActuator().contains("UIN")
				|| BaseTestCase.getSupportedIdTypesValueFromActuator().contains("uin")) {

			casesListUIN = otpauth.getYmlTestData(EKYCOTP);

		}

		else if (BaseTestCase.getSupportedIdTypesValueFromActuator().contains("VID")
				|| BaseTestCase.getSupportedIdTypesValueFromActuator().contains("vid")) {
			casesListVID = otpauth.getYmlTestData(EKYCOTP);
		}

		else {
			casesListUIN = otpauth.getYmlTestData(EKYCOTP);
			casesListVID = otpauth.getYmlTestData(EKYCOTP);
		}

		for (String uin : uinList) {
			Object[] testObj = otpauth.getYmlTestData(EKYCOTP);
			TestCaseDTO test = (TestCaseDTO) testObj[0];
			String input = test.getInput();

			if (idType.contains("UIN") || idType.contains("uin")) {
				casesListUIN = otpauth.getYmlTestData(EKYCOTP);
			}

			input = JsonPrecondtion.parseAndReturnJsonContent(input, uin, "individualId");
			input = JsonPrecondtion.parseAndReturnJsonContent(input, uin, "sendOtp.individualId");
			test.setEndPoint(test.getEndPoint().replace("$PartnerKey$", partnerKeyUrl));
			test.setEndPoint(test.getEndPoint().replace("$PartnerName$", partnerId));
			test.setEndPoint(test.getEndPoint().replace("uinnumber", uin));

			if (casesListUIN != null) {
				for (Object object : casesListUIN) {
					test.setInput(input);
					try {
						otpauth.test(test);
					} catch (AuthenticationTestException e) {
						this.hasError = true;
						logger.error(e.getMessage());
						throw new RigInternalError("EkycOtp Auth failed ");
					} catch (AdminTestException e) {
						this.hasError = true;
						logger.error(e.getMessage());
						throw new RigInternalError("EkycOtp Auth failed");
					}
				}
			}

		}

		for (String vid : vidList) {
			Object[] testObj = otpauth.getYmlTestData(EKYCOTP);
			TestCaseDTO test = (TestCaseDTO) testObj[0];
			String input = test.getInput();

			if (idType.contains("VID") || idType.contains("vid")) {
				casesListVID = otpauth.getYmlTestData(EKYCOTP);
			}

			input = JsonPrecondtion.parseAndReturnJsonContent(input, vid, "individualId");
			input = JsonPrecondtion.parseAndReturnJsonContent(input, vid, "sendOtp.individualId");
			input = JsonPrecondtion.parseAndReturnJsonContent(input, "VID", "individualIdType");
			input = JsonPrecondtion.parseAndReturnJsonContent(input, "VID", "sendOtp.individualIdType");

			test.setEndPoint(test.getEndPoint().replace("$PartnerKey$", partnerKeyUrl));
			test.setEndPoint(test.getEndPoint().replace("$PartnerName$", partnerId));
			test.setEndPoint(test.getEndPoint().replace("uinnumber", vid));

			if (casesListVID != null) {
				for (Object object : casesListVID) {
					test.setInput(input);
					try {
						otpauth.test(test);
					} catch (AuthenticationTestException | AdminTestException e) {
						this.hasError = true;
						logger.error(e.getMessage());
						throw new RigInternalError("EkycOtp Auth failed ");
					}
				}
			}

		}

	}
}
