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
import io.mosip.testscripts.OtpAuth;

public class EkycOtp extends BaseTestCaseUtil implements StepInterface {
	static Logger logger = Logger.getLogger(EkycOtp.class);
	private static final String EKYCOTP = "idaData/EkycOtp/EkycOtp.yml";
	Properties uinResidentDataPathFinalProps = new Properties();
	OtpAuth otpauth=new OtpAuth() ;

	@Override
	public void run() throws RigInternalError {
		 AuthPartnerProcessor.startProcess();
		//uinPersonaProp.put("7209149850", "C:\\Users\\username\\AppData\\Local\\Temp\\residents_629388943910840643\\604866048660486.json");

		String uins = null;
		String individualidtype = null;
		List<String> uinList = null;
		if (step.getParameters().isEmpty() || step.getParameters().size() < 1) {
			logger.error("Parameter is  missing from DSL step");
			throw new RigInternalError("Modality paramter is  missing in step: " + step.getName());
		} else {
			individualidtype = step.getParameters().get(0); 
		}
		
		if (step.getParameters().size() == 2) { // "e2e_ekycOtp(uin,$$uin)"
			uins = step.getParameters().get(1);
			if (uins.startsWith("$$")) {
				uins = step.getScenario().getVariables().get(uins);
				uinList = new ArrayList<>(Arrays.asList(uins.split("@@")));
			}
		} else if (step.getParameters().size() == 2) {
			uins = step.getParameters().get(1);
			if (!StringUtils.isBlank(uins))
				uinList = new ArrayList<>(Arrays.asList(uins.split("@@")));
		} else
			uinList = new ArrayList<>(uinPersonaProp.stringPropertyNames());
		
		Object[] testObj = otpauth.getYmlTestData(EKYCOTP);
		TestCaseDTO test = (TestCaseDTO) testObj[0];
		test.setEndPoint(test.getEndPoint().replace("$PartnerKey$", props.getProperty("partnerKey")));
	
	for (String uin : uinList) {
		String input=test.getInput();
		 input = JsonPrecondtion.parseAndReturnJsonContent(input,
					uin, "individualId");
		 input = JsonPrecondtion.parseAndReturnJsonContent(input,
				 individualidtype, "individualIdType");
		 input = JsonPrecondtion.parseAndReturnJsonContent(input,
					uin, "sendOtp.individualId");
		 input = JsonPrecondtion.parseAndReturnJsonContent(input,
				 individualidtype, "sendOtp.individualIdType");
		 input= input.replace("$PartnerKey$", props.getProperty("partnerKey"));
		test.setInput(input);
		try {
			otpauth.test(test);
		} catch (AuthenticationTestException | AdminTestException e) {
			throw new RigInternalError(e.getMessage());

			} finally {
				// AuthPartnerProcessor.authPartherProcessor.destroyForcibly();
			}
		}

	}
}
		