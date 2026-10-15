package io.mosip.testrig.dslrig.ivv.e2e.methods;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.testng.Reporter;

import io.mosip.testrig.apirig.utils.ConfigManager;
import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;

/**
 * Confirms demographic and biometric objects are in the idrepo draft location, published
 * after processing, or removed after rejection.
 */
public class VerifyIdRepoDraftLocation extends BaseTestCaseUtil implements StepInterface {

	private static final Logger logger = Logger.getLogger(VerifyIdRepoDraftLocation.class);

	static {
		if (Boolean.TRUE.equals(ConfigManager.IsDebugEnabled()))
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	@Override
	public void run() throws RigInternalError {
		if (step.getParameters() == null || step.getParameters().size() < 2) {
			this.hasError = true;
			throw new RigInternalError(
					"verify id repo draft location needs registration ID and location phase (draft, published, or rejected)");
		}
		String rid = resolveScenarioVariable(step.getParameters().get(0));
		String phase = step.getParameters().get(1) == null ? "" : step.getParameters().get(1).trim();
		if (rid == null || rid.isBlank() || rid.startsWith("$$")) {
			this.hasError = true;
			throw new RigInternalError("Registration ID is missing for idrepo draft check: " + rid);
		}
		try {
			IdRepoDraftLocationChecker.verify(rid, IdRepoDraftLocationChecker.parsePhase(phase));
		} catch (RigInternalError e) {
			this.hasError = true;
			logger.error(e.getMessage());
			Reporter.log(e.getMessage(), true);
			throw e;
		}
	}
}
