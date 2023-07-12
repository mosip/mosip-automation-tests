package io.mosip.ivv.e2e.methods;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import io.mosip.admin.fw.util.AdminTestException;
import io.mosip.admin.fw.util.TestCaseDTO;
import io.mosip.authentication.fw.precon.JsonPrecondtion;
import io.mosip.authentication.fw.util.AuthPartnerProcessor;
import io.mosip.authentication.fw.util.AuthenticationTestException;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.service.BaseTestCase;
import io.mosip.testscripts.OtpAuth;
import io.mosip.testscripts.OtpAuthNew;

public class OtpAuthentication extends BaseTestCaseUtil implements StepInterface {
	static Logger logger = Logger.getLogger(EkycOtp.class);
	private static final String OTPAUTH = "idaData/OtpAuth/OtpAuth.yml";
	Properties uinResidentDataPathFinalProps = new Properties();
	OtpAuthNew otpauth = new OtpAuthNew();

	@Override
	public void run() throws RigInternalError {
		// AuthPartnerProcessor.startProcess();
		// step.getScenario().getUinPersonaProp().put("7209149850",
		// "C:\\Users\\username\\AppData\\Local\\Temp\\residents_629388943910840643\\604866048660486.json");

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
			this.hasError=true;throw new RigInternalError("Modality paramter is  missing in step: " + step.getName());
		} else {
		}
		
		if (step.getParameters().size() == 5 && step.getParameters().get(4).startsWith("$$")) { 
			emailId = step.getParameters().get(4);
			if (emailId.startsWith("$$")) {
				emailId = step.getScenario().getVariables().get(emailId);
			}
		}

		// Fetching UIN

		if (step.getParameters().size() == 5) { //"e2e_otpAuthentication(uin,$$uin,vid,$$vid,$$email)"
			uins = step.getParameters().get(1);
			if (uins.startsWith("$$")) {
				uins = step.getScenario().getVariables().get(uins);
				uinList = new ArrayList<>(Arrays.asList(uins.split("@@")));
			}
		}  else
			uinList = new ArrayList<>(step.getScenario().getUinPersonaProp().stringPropertyNames());

		// Fetching VID

		if (step.getParameters().size() == 5) { //"e2e_otpAuthentication(uin,$$uin,vid,$$vid,$$email)"
			vids = step.getParameters().get(3);
			if (vids.startsWith("$$")) {
				vids = step.getScenario().getVariables().get(vids);
				vidList = new ArrayList<>(Arrays.asList(vids.split("@@")));
			}
		}  else
			vidList = new ArrayList<>(step.getScenario().getVidPersonaProp().stringPropertyNames());

		if (BaseTestCase.getSupportedIdTypesValueFromActuator().contains("UIN")
				|| BaseTestCase.getSupportedIdTypesValueFromActuator().contains("uin")) {

			casesListUIN = otpauth.getYmlTestData(OTPAUTH);

		}

		else if (BaseTestCase.getSupportedIdTypesValueFromActuator().contains("VID")
				|| BaseTestCase.getSupportedIdTypesValueFromActuator().contains("vid")) {
			casesListVID = otpauth.getYmlTestData(OTPAUTH);
		}

		else {
			casesListUIN = otpauth.getYmlTestData(OTPAUTH);
			casesListVID = otpauth.getYmlTestData(OTPAUTH);
		}

		// test.setEndPoint(test.getEndPoint().replace("$PartnerKey$",
		// props.getProperty("partnerKey")));

		for (String uin : uinList) {
			Object[] testObj = otpauth.getYmlTestData(OTPAUTH);
			TestCaseDTO test = (TestCaseDTO) testObj[0];
			String input = test.getInput();

			if (idType.contains("UIN") || idType.contains("uin")) {
				casesListUIN = otpauth.getYmlTestData(OTPAUTH);
			}

			input = JsonPrecondtion.parseAndReturnJsonContent(input, uin, "individualId");
			input = JsonPrecondtion.parseAndReturnJsonContent(input, emailId, "otp");

			test.setEndPoint(test.getEndPoint().replace("$PartnerKey$", partnerKeyUrl));
			test.setEndPoint(test.getEndPoint().replace("$PartnerName$", partnerId));
			test.setEndPoint(test.getEndPoint().replace("uinnumber", uin));

			if (casesListUIN != null) {
				for (Object object : casesListUIN) {
					test.setInput(input);
//					test = (TestCaseDTO) object;
					try {
						otpauth.test(test);
					} catch (AuthenticationTestException e) {
						logger.error(e.getMessage());
					} catch (AdminTestException e) {
						logger.error(e.getMessage());
					}
				}
			}

		}

		for (String vid : vidList) {
			Object[] testObj = otpauth.getYmlTestData(OTPAUTH);
			TestCaseDTO test = (TestCaseDTO) testObj[0];
			String input = test.getInput();

			if (idType.contains("VID") || idType.contains("vid")) {
				casesListVID = otpauth.getYmlTestData(OTPAUTH);
			}

			input = JsonPrecondtion.parseAndReturnJsonContent(input, vid, "individualId");

			test.setEndPoint(test.getEndPoint().replace("$PartnerKey$", partnerKeyUrl));
			test.setEndPoint(test.getEndPoint().replace("$PartnerName$", partnerId));
			test.setEndPoint(test.getEndPoint().replace("uinnumber", vid));

			if (casesListVID != null) {
				for (Object object : casesListVID) {
					test.setInput(input);
//					test = (TestCaseDTO) object;
					try {
						otpauth.test(test);
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
