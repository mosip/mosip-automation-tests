package io.mosip.testrig.dslrig.ivv.e2e.methods;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import io.mosip.testrig.apirig.dto.TestCaseDTO;
import io.mosip.testrig.apirig.testrunner.JsonPrecondtion;
import io.mosip.testrig.apirig.utils.AdminTestException;
import io.mosip.testrig.apirig.utils.AuthenticationTestException;
import io.mosip.testrig.apirig.testrunner.BaseTestCase;
import io.mosip.testrig.apirig.testscripts.PatchWithPathParam;
import io.mosip.testrig.apirig.testscripts.SimplePost;
import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testrig.dslrig.ivv.orchestrator.CenterHelper;
import io.restassured.response.Response;

@Scope("prototype")
@Component
public class HolidayDeclaration extends BaseTestCaseUtil implements StepInterface {
	static Logger logger = Logger.getLogger(HolidayDeclaration.class);
	private static final String GenerateHolidayYml = "ivv_masterdata/Holiday/CreateHoliday.yml";
	private static final String UpdateHolidayStatus = "ivv_masterdata/UpdateHolidayStatus/UpdateHolidayStatus.yml";
	SimplePost holidayDeclaration = new SimplePost();
	CenterHelper centerHelper = new CenterHelper();
	PatchWithPathParam activateHoliday = new PatchWithPathParam();
	String holidayId = "";

	@Override
	public void run() throws RigInternalError {
		step.getScenario().getVidPersonaProp().clear();

		String holidayLocationCode = centerHelper.getLocationCodeHoliday();
		Object[] testObj = holidayDeclaration.getYmlTestData(GenerateHolidayYml);
		TestCaseDTO test = (TestCaseDTO) testObj[0];
		Object[] testObjPatch = activateHoliday.getYmlTestData(UpdateHolidayStatus);
		TestCaseDTO testPatch = (TestCaseDTO) testObjPatch[0];

		String input2 = testPatch.getInput();
		String input = test.getInput();
		input = JsonPrecondtion.parseAndReturnJsonContent(input, holidayLocationCode, "locationCode");
		input = JsonPrecondtion.parseAndReturnJsonContent(input, BaseTestCase.getLanguageList().get(0), "langCode");
		input = JsonPrecondtion.parseAndReturnJsonContent(input,
				LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), "holidayDate");
		input = JsonPrecondtion.parseAndReturnJsonContent(input,
				"mosip" + BaseTestCase.generateRandomAlphaNumericString(6), "holidayDesc");
		input = JsonPrecondtion.parseAndReturnJsonContent(input, BaseTestCase.generateRandomAlphaNumericString(6),
				"holidayName");

		test.setInput(input);

		try {
			holidayDeclaration.test(test);
			Response response = holidayDeclaration.response;
			if (response != null) {
				JSONObject jsonResp = new JSONObject(response.getBody().asString());
				holidayId = jsonResp.getJSONObject("response").get("holidayId").toString();
			}

		} catch (AuthenticationTestException | AdminTestException e) {
			this.hasError = true;
			throw new RigInternalError(e.getMessage());
		}

		try {
			String stringValue = String.valueOf(holidayId);
			input2 = JsonPrecondtion.parseAndReturnJsonContent(input2, stringValue, "holidayId");
			input2 = JsonPrecondtion.parseAndReturnJsonContent(input2, "true", "isActive");
			testPatch.setInput(input2);

			activateHoliday.test(testPatch);
			Response response = activateHoliday.response;

			if (response != null) {
				JSONObject jsonResp = new JSONObject(response.getBody().asString());
				logger.info(jsonResp.getJSONObject("response"));
			}

		} catch (Exception e) {
			this.hasError = true;
			throw new RigInternalError(e.getMessage());
		}
		if (step.getOutVarName() != null) {
			step.getScenario().getVariables().put(step.getOutVarName(), holidayId);
			return;
		}
	}
}
