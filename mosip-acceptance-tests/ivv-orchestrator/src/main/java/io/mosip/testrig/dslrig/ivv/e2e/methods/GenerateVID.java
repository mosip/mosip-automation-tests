package io.mosip.testrig.dslrig.ivv.e2e.methods;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import io.mosip.testrig.apirig.admin.fw.util.AdminTestException;
import io.mosip.testrig.apirig.admin.fw.util.TestCaseDTO;
import io.mosip.testrig.apirig.authentication.fw.precon.JsonPrecondtion;
import io.mosip.testrig.apirig.authentication.fw.util.AuthenticationTestException;
import io.mosip.testrig.apirig.testscripts.PostWithBodyWithOtpGenerate;
import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.restassured.response.Response;

public class GenerateVID extends BaseTestCaseUtil implements StepInterface {
	static Logger logger = Logger.getLogger(GenerateVID.class);
	private static final String GenerateVIDYml = "idaData/GenerateVID/createGenerateVID.yml";
	Properties uinResidentDataPathFinalProps = new Properties();
	PostWithBodyWithOtpGenerate generatevid = new PostWithBodyWithOtpGenerate();

	@Override
	public void run() throws RigInternalError {
		step.getScenario().getVidPersonaProp().clear();
		String uins = null;
		String vidtype = null;
		List<String> uinList = null;
		String emailId ="";
		String transactionID = (step.getScenario().getId() + RandomStringUtils.randomNumeric(8)).substring(0, 10);
		
		if (step.getParameters() == null || step.getParameters().isEmpty() || step.getParameters().size() < 1) {
			logger.error("VID Type[Perpetual/Temporary] parameter is  missing from DSL step");
			this.hasError=true;throw new RigInternalError("VID Type[Perpetual/Temporary] paramter is  missing in step: " + step.getName());
		} else {
			vidtype = step.getParameters().get(0);

		}
		if (step.getParameters().size() == 3 && step.getParameters().get(1).startsWith("$$")) { 
			uins = step.getParameters().get(1);  // "$$vid=e2e_GenerateVID(Perpetual,$$uin,$$email)"
			if (uins.startsWith("$$")) {
				uins = step.getScenario().getVariables().get(uins);
				uinList = new ArrayList<>(Arrays.asList(uins.split("@@")));
			}
		} else if (step.getParameters().size() == 2) {
			uins = step.getParameters().get(1);
			if (!StringUtils.isBlank(uins))
				uinList = new ArrayList<>(Arrays.asList(uins.split("@@")));
		} else
			uinList = new ArrayList<>(step.getScenario().getUinPersonaProp().stringPropertyNames());
		
		if (step.getParameters().size() == 3 && step.getParameters().get(2).startsWith("$$")) { 
			emailId = step.getParameters().get(2);  // "$$vid=e2e_GenerateVID(Perpetual,$$uin,$$email)"
			if (emailId.startsWith("$$")) {
				emailId = step.getScenario().getVariables().get(emailId);
			}
		}

		Object[] testObj = generatevid.getYmlTestData(GenerateVIDYml);

		TestCaseDTO test = (TestCaseDTO) testObj[0];
		

		for (String uin : uinList) {
			String input = test.getInput();
			input = JsonPrecondtion.parseAndReturnJsonContent(input, uin, "individualId");
			input = JsonPrecondtion.parseAndReturnJsonContent(input, vidtype, "vidType");
			input = JsonPrecondtion.parseAndReturnJsonContent(input, emailId, "otp");
			input = JsonPrecondtion.parseAndReturnJsonContent(input, uin, "sendOtp.individualId");
			input = JsonPrecondtion.parseAndReturnJsonContent(input, transactionID, "sendOtp.transactionID");
			input = JsonPrecondtion.parseAndReturnJsonContent(input, transactionID, "transactionID");

			test.setInput(input);

			try {
				generatevid.test(test);
				Response response = generatevid.response;
				if (response != null) {
					JSONObject jsonResp = new JSONObject(response.getBody().asString());
					String vid = jsonResp.getJSONObject("response").getString("vid");
					if (step.getOutVarName() != null)
						step.getScenario().getVariables().put(step.getOutVarName(), vid);
					else
						step.getScenario().getVidPersonaProp().put(vid, uin);

					System.out.println(step.getScenario().getVidPersonaProp());
				}

			} catch (AuthenticationTestException | AdminTestException e) {
				this.hasError=true;throw new RigInternalError(e.getMessage());

			}
		}

	}
}
