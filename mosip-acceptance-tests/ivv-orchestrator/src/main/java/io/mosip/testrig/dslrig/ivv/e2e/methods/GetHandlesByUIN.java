package io.mosip.testrig.dslrig.ivv.e2e.methods;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import io.mosip.testrig.apirig.dto.TestCaseDTO;
import io.mosip.testrig.apirig.masterdata.testscripts.GetWithParam;
import io.mosip.testrig.apirig.testrunner.JsonPrecondtion;
import io.mosip.testrig.apirig.utils.AdminTestException;
import io.mosip.testrig.apirig.utils.AuthenticationTestException;
import io.mosip.testrig.apirig.utils.OutputValidationUtil;
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
		step.getScenario().getVidPersonaProp().clear();
		
		Map<String, String> handleValueMap = new HashMap<>();
		String uins = null;
		List<String> uinList = null;
		Object[] testObj = getIdentity.getYmlTestData(getIdentityYml);
		TestCaseDTO test = (TestCaseDTO) testObj[0];
		if (step.getParameters().size() == 1 && step.getParameters().get(0).startsWith("$$")) {
			uins = step.getParameters().get(0);
			if (uins.startsWith("$$")) {
				uins = step.getScenario().getVariables().get(uins);
				uinList = new ArrayList<>(Arrays.asList(uins.split("@@")));
			}

			String input = test.getInput();
			input = JsonPrecondtion.parseAndReturnJsonContent(input, uins, "id");
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
				JSONArray selectedHandles = identityData.getJSONArray("selectedHandles");
				
				

		        for (int i = 0; i < selectedHandles.length(); i++) {
		            String handle = selectedHandles.getString(i);
		            if (identityData.has(handle)) {
		                handleValueMap.put(handle, identityData.getString(handle));
		            }
		        }

		        logger.info(handleValueMap);
			} catch (AuthenticationTestException | AdminTestException e) {
				logger.error(e.getMessage());
			} catch (Exception e) {
				logger.error(e.getMessage());
			}
			if (step.getOutVarName() != null)
				step.getScenario().getObjectVariables().put(step.getOutVarName(), handleValueMap);
		}
	}
}
