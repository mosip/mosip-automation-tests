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

public class EkycOtp extends BaseTestCaseUtil implements StepInterface {
	static Logger logger = Logger.getLogger(EkycOtp.class);
	private static final String EKYCOTP = "idaData/EkycOtp/EkycOtp.yml";
	Properties uinResidentDataPathFinalProps = new Properties();
	OtpAuthNew otpauth = new OtpAuthNew();

	@Override
	public void run() throws RigInternalError {
		// AuthPartnerProcessor.startProcess();
		// uinPersonaProp.put("7209149850",
		// "C:\\Users\\username\\AppData\\Local\\Temp\\residents_629388943910840643\\604866048660486.json");

		String uins = null;
		String vids = null;
		List<String> uinList = null;
		List<String> idType = BaseTestCase.getSupportedIdTypesValueFromActuator();
		List<String> vidList = null;

		

		Object[] casesListUIN = null;
		Object[] casesListVID = null;

		if (step.getParameters().isEmpty() || step.getParameters().size() < 1) {
			logger.error("Parameter is  missing from DSL step");
			throw new RigInternalError("Modality paramter is  missing in step: " + step.getName());
		} else {
		}

		// Fetching UIN

		if (step.getParameters().size() == 4) { // "e2e_ekycOtp(uin,$$uin,vid,$$vid)"
			uins = step.getParameters().get(1);
			if (uins.startsWith("$$")) {
				uins = step.getScenario().getVariables().get(uins);
				uinList = new ArrayList<>(Arrays.asList(uins.split("@@")));
			}
		} else if (step.getParameters().size() == 4) {
			uins = step.getParameters().get(1);
			if (!StringUtils.isBlank(uins))
				uinList = new ArrayList<>(Arrays.asList(uins.split("@@")));
		} else
			uinList = new ArrayList<>(uinPersonaProp.stringPropertyNames());

		// Fetching VID

		if (step.getParameters().size() == 4) { // "e2e_ekycOtp(uin,$$uin,vid,$$vid)"
			vids = step.getParameters().get(3);
			if (vids.startsWith("$$")) {
				vids = step.getScenario().getVariables().get(vids);
				vidList = new ArrayList<>(Arrays.asList(vids.split("@@")));
			}
		} else if (step.getParameters().size() == 4) {
			vids = step.getParameters().get(3);
			if (!StringUtils.isBlank(vids))
				vidList = new ArrayList<>(Arrays.asList(vids.split("@@")));
		} else
			vidList = new ArrayList<>(vidPersonaProp.stringPropertyNames());

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

		// test.setEndPoint(test.getEndPoint().replace("$PartnerKey$",
		// props.getProperty("partnerKey")));

		for (String uin : uinList) {
			Object[] testObj = otpauth.getYmlTestData(EKYCOTP);
			TestCaseDTO test = (TestCaseDTO) testObj[0];
			String input = test.getInput();

			if (idType.contains("UIN") || idType.contains("uin")) {
				casesListUIN = otpauth.getYmlTestData(EKYCOTP);
			}

			input = JsonPrecondtion.parseAndReturnJsonContent(input, uin, "individualId");

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
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (AdminTestException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
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
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (AdminTestException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

		}

	}
}
