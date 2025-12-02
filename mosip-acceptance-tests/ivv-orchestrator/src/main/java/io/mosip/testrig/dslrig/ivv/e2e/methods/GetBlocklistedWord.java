package io.mosip.testrig.dslrig.ivv.e2e.methods;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import io.mosip.testrig.apirig.testrunner.BaseTestCase;
import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testrig.dslrig.ivv.orchestrator.UserHelper;
import io.mosip.testrig.dslrig.ivv.orchestrator.dslConfigManager;
import io.restassured.response.Response;

public class GetBlocklistedWord extends BaseTestCaseUtil implements StepInterface {

	static Logger logger = Logger.getLogger(GetBlocklistedWord.class);
	UserHelper userHelper = new UserHelper();

	static {
		if (dslConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	@Override
	public void run() throws RigInternalError {
		String callType = null;
		String blocklistedWordParam = null;

		if (step.getParameters() == null || step.getParameters().isEmpty() || step.getParameters().size() < 1) {
			logger.error("Method Type[POST/GET/PUT/PATCH] parameter is missing from DSL step");
			this.hasError = true;
			throw new RigInternalError(
					"Method Type[POST/GET/PUT/PATCH] parameter is missing from DSL step: " + step.getName());
		} else {
			callType = step.getParameters().get(0);
		}

		if (step.getParameters().size() >= 2) {
			blocklistedWordParam = step.getParameters().get(1);
		}
		if (blocklistedWordParam == null || blocklistedWordParam.trim().isEmpty()) {
			this.hasError = true;
			throw new RigInternalError("Blocklisted word parameter is required for " + callType);
		}

		String blocklistedWord = null;

		try {
			blocklistedWord = userHelper.createBlocklistedWord(blocklistedWordParam, BaseTestCase.languageCode, step);
			logger.info("Blocklisted word is: " + blocklistedWord);

			if (step.getOutVarName() != null) {
				step.getScenario().getVariables().put(step.getOutVarName(), blocklistedWord);
			}

		} catch (Exception e) {
			this.hasError = true;
			logger.error("Error in GetBlocklistedWord: " + e.getMessage(), e);
			throw new RigInternalError(e.getMessage());
		}
	}
}
