package io.mosip.testrig.dslrig.ivv.e2e.methods;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import io.mosip.testrig.apirig.idrepo.testscripts.UpdateIdentity;
import io.mosip.testrig.apirig.dto.TestCaseDTO;
import io.mosip.testrig.apirig.testrunner.BaseTestCase;
import io.mosip.testrig.apirig.testrunner.JsonPrecondtion;
import io.mosip.testrig.apirig.utils.AdminTestException;
import io.mosip.testrig.apirig.utils.AuthenticationTestException;
import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.FeatureNotSupportedError;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testrig.dslrig.ivv.orchestrator.dslConfigManager;

public class DeactivateUIN extends BaseTestCaseUtil implements StepInterface {
	
	static Logger logger = Logger.getLogger(DeactivateUIN.class);
	private static final String DEACTIVATEUIN = "idaData/DeactivateUIN/UpdateIdentity.yml";

	static {
		if (dslConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	UpdateIdentity  updateIdentity  = new UpdateIdentity();

	@Override
	public void run() throws RigInternalError, FeatureNotSupportedError {
		String uins = null;
		List<String> uinList = null;
		String emailId = "";
		Object[] casesListUIN = null;

		if (step.getParameters().isEmpty() || step.getParameters().size() < 1) {
			logger.error("Parameter is  missing from DSL step");
			this.hasError = true;
			throw new RigInternalError("Modality paramter is  missing in step: " + step.getName());
		}
		if (step.getParameters().size() == 2) {
			emailId = step.getParameters().get(1);
			if (emailId.startsWith("$$")) {
				emailId = step.getScenario().getVariables().get(emailId);
			}
			if (emailId == null || (emailId != null && emailId.isBlank())) {
				throw new FeatureNotSupportedError("Email id is Empty hence we cannot perform deactivate uin");

			}
			
		}
		if (step.getParameters().size() == 2 && step.getParameters().get(0).startsWith("$$")) {
			uins = step.getParameters().get(0);
			if (uins.startsWith("$$")) {
				uins = step.getScenario().getVariables().get(uins);
				uinList = new ArrayList<>(Arrays.asList(uins.split("@@")));
			}
		} else
			uinList = new ArrayList<>(step.getScenario().getUinPersonaProp().stringPropertyNames());

		

		if (BaseTestCase.getSupportedIdTypesValueFromActuator().contains("UIN")
				|| BaseTestCase.getSupportedIdTypesValueFromActuator().contains("uin")) {

			casesListUIN = updateIdentity.getYmlTestData(DEACTIVATEUIN);

		}


		else {
			casesListUIN = updateIdentity.getYmlTestData(DEACTIVATEUIN);
		}

		for (String uin : uinList) {
			Object[] testObj = updateIdentity.getYmlTestData(DEACTIVATEUIN);
			TestCaseDTO test = (TestCaseDTO) testObj[0];
			String input = test.getInput();
			
			input = JsonPrecondtion.parseAndReturnJsonContent(input, uin, "UIN");
			input = JsonPrecondtion.parseAndReturnJsonContent(input, emailId, "email");
			

			if (casesListUIN != null) {
				for (Object object : casesListUIN) {
					test.setInput(input);
					try {
						updateIdentity.test(test);
					} catch (AuthenticationTestException e) {
						this.hasError = true;
						logger.error(e.getMessage());
						throw new RigInternalError("DEACTIVATEUIN failed ");
					} catch (AdminTestException e) {
						this.hasError = true;
						logger.error(e.getMessage());
						throw new RigInternalError("DEACTIVATEUIN failed");
					}
				}
			}

		}

	}
}
