package io.mosip.testrig.dslrig.ivv.e2e.methods;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import io.mosip.testrig.apirig.admin.fw.util.AdminTestException;
import io.mosip.testrig.apirig.admin.fw.util.TestCaseDTO;
import io.mosip.testrig.apirig.authentication.fw.precon.JsonPrecondtion;
import io.mosip.testrig.apirig.authentication.fw.util.AuthenticationTestException;
import io.mosip.testrig.apirig.kernel.util.ConfigManager;
import io.mosip.testrig.apirig.testscripts.SimplePostForAutoGenId;
import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.restassured.response.Response;

public class GenerateVIDWithoutOTP extends BaseTestCaseUtil implements StepInterface {
	static Logger logger = Logger.getLogger(GenerateVIDWithoutOTP.class);
	private static final String GenerateVID = "idaData/CreateVID/CreateVid.yml";
	Properties uinResidentDataPathFinalProps = new Properties();
	SimplePostForAutoGenId generatevid = new SimplePostForAutoGenId();
	
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
		if (step.getParameters() == null || step.getParameters().isEmpty() || step.getParameters().size() < 1) {
			logger.error("VID Type[Perpetual/Temporary] parameter is  missing from DSL step");
			this.hasError=true;throw new RigInternalError("VID Type[Perpetual/Temporary] paramter is  missing in step: " + step.getName());
		} else {
			vidtype = step.getParameters().get(0);

		}
		if (step.getParameters().size() == 2 && step.getParameters().get(1).startsWith("$$")) { 
			uins = step.getParameters().get(1);  //"$$vidwithoutotp=e2e_GenerateVID(Perpetual,$$uin)"
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
		

		Object[] testObj = generatevid.getYmlTestData(GenerateVID);

		TestCaseDTO test = (TestCaseDTO) testObj[0];
		

		for (String uin : uinList) {
			String input = test.getInput();
			input = JsonPrecondtion.parseAndReturnJsonContent(input, uin, "UIN");
			input = JsonPrecondtion.parseAndReturnJsonContent(input, vidtype, "vidType");

			test.setInput(input);

			try {
				try {
					generatevid.test(test);
				} catch (NoSuchAlgorithmException e) {
					logger.error(e.getMessage());
				}
				Response response = generatevid.response;
				if (response != null) {
					JSONObject jsonResp = new JSONObject(response.getBody().asString());
					String vid = jsonResp.getJSONObject("response").getString("VID");
					if (step.getOutVarName() != null)
						step.getScenario().getVariables().put(step.getOutVarName(), vid);
					else
						step.getScenario().getVidPersonaProp().put(vid, uin);

					logger.info(step.getScenario().getVidPersonaProp());
				}

			} catch (AuthenticationTestException | AdminTestException e) {
				this.hasError=true;throw new RigInternalError(e.getMessage());

			}
		}

	}
}
