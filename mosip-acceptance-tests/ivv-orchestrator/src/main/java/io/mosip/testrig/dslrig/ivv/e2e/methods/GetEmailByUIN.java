package io.mosip.testrig.dslrig.ivv.e2e.methods;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.json.JSONObject;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import io.mosip.testrig.apirig.admin.fw.util.AdminTestException;
import io.mosip.testrig.apirig.admin.fw.util.TestCaseDTO;
import io.mosip.testrig.apirig.authentication.fw.precon.JsonPrecondtion;
import io.mosip.testrig.apirig.authentication.fw.util.AuthenticationTestException;
import io.mosip.testrig.apirig.kernel.util.ConfigManager;
import io.mosip.testrig.apirig.testscripts.GetWithParam;
import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.restassured.response.Response;

public class GetEmailByUIN extends BaseTestCaseUtil implements StepInterface {
	private static final Logger logger = Logger.getLogger(GetEmailByUIN.class);
	private static final String GetEmailYml = "idaData/RetrieveIdentityByUin/RetrieveIdentityByUin.yml";
	GetWithParam getEmail = new GetWithParam();
	
	static {
		if (ConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}
	
	@Override
    public void run() throws RigInternalError {
		step.getScenario().getVidPersonaProp().clear();
		String emailId ="";
		String uins = null;
		List<String> uinList = null;
		Object[] testObj = getEmail.getYmlTestData(GetEmailYml);
		TestCaseDTO test = (TestCaseDTO) testObj[0];
		if (step.getParameters().size() == 1 && step.getParameters().get(0).startsWith("$$")) { 
			uins = step.getParameters().get(0);  //$$email=e2e_getEmailByUIN($$uin)
			if (uins.startsWith("$$")) {
				uins = step.getScenario().getVariables().get(uins);
				uinList = new ArrayList<>(Arrays.asList(uins.split("@@")));
			}
				
			String input = test.getInput();
			input = JsonPrecondtion.parseAndReturnJsonContent(input, uins, "id");
			test.setInput(input);

				try {
					getEmail.test(test);
				} catch (AuthenticationTestException e) {
					logger.error(e.getMessage());
				} catch (AdminTestException e) {
					logger.error(e.getMessage());
				}

				Response response = getEmail.response;
				JSONObject responseJson = new JSONObject(response.asString());
				JSONObject responseData =  responseJson.getJSONObject("response");

				JSONObject identityData = responseData.getJSONObject("identity");
				String emailData = identityData.getString("email");
				
				if (step.getOutVarName() != null)
					step.getScenario().getVariables().put(step.getOutVarName(), emailData);
				
				

		}
	}
}
