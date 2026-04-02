package io.mosip.testrig.dslrig.ivv.e2e.methods;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import io.mosip.testrig.apirig.dto.TestCaseDTO;
import io.mosip.testrig.apirig.masterdata.testscripts.GetWithParam;
import io.mosip.testrig.apirig.testrunner.JsonPrecondtion;
import io.mosip.testrig.apirig.utils.AdminTestException;
import io.mosip.testrig.apirig.utils.AuthenticationTestException;
import io.mosip.testrig.apirig.utils.OutputValidationUtil;
import io.mosip.testrig.apirig.utils.SecurityXSSException;
import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testrig.dslrig.ivv.orchestrator.dslConfigManager;
import io.restassured.response.Response;

public class GetHandlesByUIN extends BaseTestCaseUtil implements StepInterface {
	private static final Logger logger = Logger.getLogger(GetHandlesByUIN.class);
	private static final String getIdentityYml = "idaData/RetrieveIdentityByUin/RetrieveIdentityByUin.yml";
	GetWithParam getIdentity = new GetWithParam();

	static {
		if (dslConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	@Override
	public void run() throws RigInternalError {
		step.getScenario().getHandlePersonaProp().clear();
		
		Map<String, String> handleValueMap = new HashMap<>();
		String uin = null;
		List<String> selectedHandles = new ArrayList<>();
		Object[] testObj = getIdentity.getYmlTestData(getIdentityYml);
		TestCaseDTO test = (TestCaseDTO) testObj[0];
		if (step.getParameters().size() == 1 && step.getParameters().get(0).startsWith("$$")) {
			uin = step.getParameters().get(0);
			if (uin.startsWith("$$")) {
				uin = step.getScenario().getVariables().get(uin);
			}
			String input = test.getInput();
			input = JsonPrecondtion.parseAndReturnJsonContent(input, uin, "id");
			test.setInput(input);

			try {
				getIdentity.test(test);

				Response response = getIdentity.response;
				JSONObject responseJson = new JSONObject(response.asString());
				JSONObject responseData = responseJson.getJSONObject("response");
				if (OutputValidationUtil.doesResponseHasErrors(responseJson.toString())) {
					logger.error("Failed to extract Email From UIN");
					this.hasError = true;
					throw new RigInternalError("Failed to extract Email From UIN: " + step.getName());
				}
				JSONObject identityData = responseData.getJSONObject("identity");
				
			// Extract selectedHandles from response
			if (identityData.has("selectedHandles")) {
				org.json.JSONArray selectedHandlesArray = identityData.getJSONArray("selectedHandles");
				for (int i = 0; i < selectedHandlesArray.length(); i++) {
					selectedHandles.add(selectedHandlesArray.getString(i));
				}
			}             
		        logger.info(handleValueMap);
			} catch (AuthenticationTestException | AdminTestException | SecurityXSSException e) {
				logger.error(e.getMessage());
			}
			if (step.getOutVarName() != null)
				step.getScenario().getObjectVariables().put(step.getOutVarName(), selectedHandles);
		}
	}
}
