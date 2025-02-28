package io.mosip.testrig.dslrig.ivv.e2e.methods;

import static org.testng.Assert.assertTrue;



import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.testng.Reporter;

import io.mosip.testrig.apirig.utils.GlobalMethods;
import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.FeatureNotSupportedError;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;

public class ValidateKycData  extends BaseTestCaseUtil implements StepInterface {
	String data = "";
	String responce = "";
	static Logger logger = Logger.getLogger(ValidateKycData.class);
	String newResponse= "";
	JSONObject responseJson;
	@Override
	public void run() throws RigInternalError, FeatureNotSupportedError {
		try {
			if (step.getParameters() == null || step.getParameters().isEmpty()) {
				logger.error("Parameter is  missing from DSL step");
				assertTrue(false, " paramter is  missing in step: " + step.getName());
			} else if (step.getParameters().size() == 2) {
				data = step.getParameters().get(0);
				responce = step.getParameters().get(1);
			}
			newResponse= step.getScenario().getVariables().get(responce);
			responseJson = new JSONObject(newResponse);

			if(responseJson.has(data)) {
				logger.info(data+" data is there");
				GlobalMethods.reportRequest("decryptEkycData", newResponse);
	            Reporter.log("<b style=\"background-color: #0A0;\">Marking test case as passed. As "+data+" data is there in a decryptEkycData</b><br>\n" );
			}else {
				throw new RigInternalError(data+" Data is not there in a decryptEkycData");
			}
			
		} catch (Exception e) {
			this.hasError = true;
			logger.error(e.getMessage());


		}

	}

}






