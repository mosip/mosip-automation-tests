package io.mosip.testrig.dslrig.ivv.e2e.methods;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import io.mosip.testrig.apirig.dto.TestCaseDTO;
import io.mosip.testrig.apirig.idrepo.testscripts.UpdateIdentityForArrayHandles;
import io.mosip.testrig.apirig.testrunner.JsonPrecondtion;
import io.mosip.testrig.apirig.utils.AdminTestException;
import io.mosip.testrig.apirig.utils.AuthenticationTestException;
import io.mosip.testrig.apirig.utils.SecurityXSSException;
import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.FeatureNotSupportedError;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testrig.dslrig.ivv.orchestrator.dslConfigManager;

public class UpdateIdentityWithArrayHandles extends BaseTestCaseUtil implements StepInterface {
	static Logger logger = Logger.getLogger(UpdateIdentityWithArrayHandles.class);
	private static final String updateIdentityWithHandlesYml = "idRepo/UpdateIdentityArrayHandle/UpdateIdentityArrayHandle.yml";
	UpdateIdentityForArrayHandles updateIdentity = new UpdateIdentityForArrayHandles();

	static {
		if (dslConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	@Override
	public void run() throws RigInternalError, FeatureNotSupportedError {

		String uin = "";

		Object[] testObj = updateIdentity.getYmlTestData(updateIdentityWithHandlesYml);

		TestCaseDTO test = (TestCaseDTO) testObj[0];

		if (!step.getParameters().isEmpty()) {
			
			if (step.getParameters().get(0).startsWith("$$")) {
				uin = (String) step.getScenario().getVariables().get(step.getParameters().get(0));
			}
			else {
				uin = step.getParameters().get(0);
			}
			
			logger.info(uin);
		}

		String input = test.getInput();
		input = JsonPrecondtion.parseAndReturnJsonContent(input, uin, "UIN");
		test.setInput(input);

		try {
			updateIdentity.test(test);

		} catch (AuthenticationTestException | AdminTestException | SecurityXSSException e) {
			logger.error(e.getMessage());
			this.hasError = true;
			throw new RigInternalError(e.getMessage());

		}

	}
}
