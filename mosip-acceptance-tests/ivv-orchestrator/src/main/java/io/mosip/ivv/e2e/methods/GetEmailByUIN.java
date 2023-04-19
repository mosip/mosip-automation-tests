package io.mosip.ivv.e2e.methods;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONObject;
import io.mosip.admin.fw.util.AdminTestException;
import io.mosip.admin.fw.util.TestCaseDTO;
import io.mosip.authentication.fw.precon.JsonPrecondtion;
import io.mosip.authentication.fw.util.AuthenticationTestException;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testscripts.GetWithParam;
import io.restassured.response.Response;

public class GetEmailByUIN extends BaseTestCaseUtil implements StepInterface {

	private static final String GetEmail = "idaData/RetrieveIdentityByUin/RetrieveIdentityByUin.yml";
	GetWithParam getEmail = new GetWithParam();
	
	
	
	@Override
    public void run() throws RigInternalError {
		step.getScenario().getVidPersonaProp().clear();
		String emailId ="";
		String uins = null;
		List<String> uinList = null;
		
		Object[] testObj = getEmail.getYmlTestData(GetEmail);

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
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (AdminTestException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
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
