package io.mosip.testrig.dslrig.ivv.e2e.methods;

import javax.ws.rs.core.MediaType;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import io.mosip.testrig.apirig.global.utils.GlobalConstants;
import io.mosip.testrig.apirig.kernel.util.ConfigManager;
import io.mosip.testrig.apirig.kernel.util.KernelAuthentication;
import io.mosip.testrig.apirig.service.BaseTestCase;
import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;

import io.restassured.response.Response;

public class GetBlocklistedWord extends BaseTestCaseUtil implements StepInterface {

	static Logger logger = Logger.getLogger(GetBlocklistedWord.class);
	KernelAuthentication kernelAuthLib = new KernelAuthentication();
	
	static {
		if (ConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	@Override
	public void run() throws RigInternalError {
		String token = kernelAuthLib.getTokenByRole("admin");

		String url = BaseTestCase.ApplnURI + props.getProperty("blocklistedWord") + BaseTestCase.languageCode;

		Response response = null;
		String blocklistedWord = null;

		try {
			response = BaseTestCaseUtil.getRequestWithCookie(url, MediaType.APPLICATION_JSON,
					MediaType.APPLICATION_JSON, GlobalConstants.AUTHORIZATION, token);

			if (response != null) {
				JSONObject jsonObject = new JSONObject(response.getBody().asString());
				JSONArray blocklistedWords = jsonObject.getJSONObject("response").getJSONArray("blocklistedwords");

					blocklistedWord = blocklistedWords.getJSONObject(0).getString("word");
					logger.info("blocklistedWord is :" + blocklistedWord);
					if (step.getOutVarName() != null)
						step.getScenario().getVariables().put(step.getOutVarName(), blocklistedWord);
					return;
			}

		} catch (Exception e) {
			this.hasError = true;
			logger.error(e.getMessage());
			throw new RigInternalError(response.getBody().asString());
		}
	}
}
