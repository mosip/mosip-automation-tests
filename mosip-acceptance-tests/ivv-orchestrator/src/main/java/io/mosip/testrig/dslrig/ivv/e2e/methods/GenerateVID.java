package io.mosip.testrig.dslrig.ivv.e2e.methods;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import io.mosip.testrig.apirig.dto.TestCaseDTO;
import io.mosip.testrig.apirig.testrunner.JsonPrecondtion;
import io.mosip.testrig.apirig.utils.ConfigManager;
import io.mosip.testrig.apirig.utils.AdminTestException;
import io.mosip.testrig.apirig.utils.AuthenticationTestException;
import io.mosip.testrig.apirig.testscripts.PostWithBodyWithOtpGenerate;
import io.mosip.testrig.apirig.testscripts.SimplePost;
import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testrig.dslrig.ivv.orchestrator.PersonaDataManager;
import io.restassured.response.Response;

@Scope("prototype")
@Component
public class GenerateVID extends BaseTestCaseUtil implements StepInterface {
	static Logger logger = Logger.getLogger(GenerateVID.class);
	private static final String GenerateVIDYml = "idaData/GenerateVID/createGenerateVID.yml";
	private static final String GenerateVID = "idaData/CreateVID/CreateVid.yml";
	Properties uinResidentDataPathFinalProps = new Properties();
	PostWithBodyWithOtpGenerate generatevid = new PostWithBodyWithOtpGenerate();

	SimplePost generatevidwithoutotp = new SimplePost();
	static {
		if (ConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	@Override
	public void run() throws RigInternalError {
		step.getScenario().getVidPersonaProp().clear();
		String uins = null;
		String vidtype = null;
		List<String> uinList = null;
		String emailId = "";
		boolean getOtpByPhone = Boolean.FALSE;
		String vid = "";
		String transactionID = (step.getScenario().getId() + RandomStringUtils.randomNumeric(11));
		transactionID = transactionID.substring(0, 10);
		logger.info(transactionID);

		if (step.getParameters() == null || step.getParameters().isEmpty() || step.getParameters().size() < 1) {
			logger.error("VID Type[Perpetual/Temporary] parameter is  missing from DSL step");
			this.hasError = true;
			throw new RigInternalError("VID Type[Perpetual/Temporary] paramter is  missing in step: " + step.getName());
		} else {
			vidtype = step.getParameters().get(0);

		}
		if (step.getParameters().size() == 3 && step.getParameters().get(1).startsWith("$$")) {
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
			uinList = new ArrayList<>(step.getScenario().getUinPersonaProp().stringPropertyNames());

		if (step.getParameters().size() == 3 && step.getParameters().get(2).startsWith("$$")) {
			emailId = step.getParameters().get(2);

			if (emailId.contentEquals("$$phone"))
				getOtpByPhone = true;

			if (emailId.startsWith("$$")) {
				emailId = step.getScenario().getVariables().get(emailId);
			}
		}

		if (emailId != null && !emailId.isEmpty()) {
			Object[] testObj = generatevid.getYmlTestData(GenerateVIDYml);

			TestCaseDTO test = (TestCaseDTO) testObj[0];

			for (String uin : uinList) {
				String input = test.getInput();
				input = JsonPrecondtion.parseAndReturnJsonContent(input, uin, "individualId");
				input = JsonPrecondtion.parseAndReturnJsonContent(input, vidtype, "vidType");
				if (getOtpByPhone)
					input = JsonPrecondtion.parseAndReturnJsonContent(input, emailId + "@phone", "otp");
				else
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
						vid = jsonResp.getJSONObject("response").getString("vid");
						if (step.getOutVarName() != null)
							step.getScenario().getVariables().put(step.getOutVarName(), vid);
						else
							step.getScenario().getVidPersonaProp().put(vid, uin);

						logger.info(step.getScenario().getVidPersonaProp());
					}

				} catch (AuthenticationTestException | AdminTestException e) {
					this.hasError = true;
					throw new RigInternalError(e.getMessage());

				}
			}
		} else {
			Object[] testObj = generatevidwithoutotp.getYmlTestData(GenerateVID);

			TestCaseDTO test = (TestCaseDTO) testObj[0];

			for (String uin : uinList) {
				String input = test.getInput();
				input = JsonPrecondtion.parseAndReturnJsonContent(input, uin, "UIN");
				input = JsonPrecondtion.parseAndReturnJsonContent(input, vidtype, "vidType");

				test.setInput(input);

				try {
					generatevidwithoutotp.test(test);

					Response response = generatevidwithoutotp.response;
					if (response != null) {
						JSONObject jsonResp = new JSONObject(response.getBody().asString());
						vid = jsonResp.getJSONObject("response").getString("VID");
						if (step.getOutVarName() != null)
							step.getScenario().getVariables().put(step.getOutVarName(), vid);
						else
							step.getScenario().getVidPersonaProp().put(vid, uin);

						logger.info(step.getScenario().getVidPersonaProp());
					}

				} catch (AuthenticationTestException | AdminTestException e) {
					this.hasError = true;
					throw new RigInternalError(e.getMessage());

				}
			}
		}
		PersonaDataManager.setVariableValue(step.getScenario().getId(), "VID", vid);
	}
}
