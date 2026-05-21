package io.mosip.testrig.dslrig.ivv.e2e.methods;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testrig.dslrig.ivv.orchestrator.dslConfigManager;

public class CloneResidentData extends BaseTestCaseUtil implements StepInterface {
	static Logger logger = Logger.getLogger(CloneResidentData.class);

	static {
		if (dslConfigManager.IsDebugEnabled()) {
			logger.setLevel(Level.ALL);
		} else {
			logger.setLevel(Level.ERROR);
		}
	}

	@Override
	public void run() throws RigInternalError {
		if (step.getParameters() == null || step.getParameters().isEmpty()) {
			this.hasError = true;
			throw new RigInternalError("Resident data path parameter is missing in step: " + step.getName());
		}

		String personaFilePath = step.getParameters().get(0);
		if (personaFilePath.startsWith("$$")) {
			personaFilePath = step.getScenario().getVariables().get(personaFilePath);
		}

		if (personaFilePath == null || personaFilePath.isBlank()) {
			this.hasError = true;
			throw new RigInternalError("Unable to resolve resident data path in step: " + step.getName());
		}

		String clonedPath = packetUtility.cloneResidentData(personaFilePath, step);
		if (clonedPath == null || clonedPath.isBlank()) {
			this.hasError = true;
			throw new RigInternalError("Clone resident data returned empty path for: " + personaFilePath);
		}

		if (step.getOutVarName() != null) {
			step.getScenario().getVariables().put(step.getOutVarName(), clonedPath);
		}
		logger.info("Cloned resident data path: " + clonedPath);
	}
}
