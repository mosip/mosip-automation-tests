package io.mosip.testrig.dslrig.ivv.e2e.methods;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testrig.dslrig.ivv.orchestrator.dslConfigManager;

public class UpdateBioExceptionInPersona extends BaseTestCaseUtil implements StepInterface {
	static Logger logger = Logger.getLogger(UpdateBioExceptionInPersona.class);

	static {
		if (dslConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	@Override
	public void run() throws RigInternalError {
		List<String> exceptionArray = new ArrayList<String>();
		if (step.getParameters() == null || step.getParameters().isEmpty()) {
			logger.error("Parameter is  missing from DSL step");
			this.hasError = true;
			throw new RigInternalError("bioType paramter is  missing in step: " + step.getName());
		}
		if (!step.getParameters().isEmpty()) { // "var1=e2e_updateDemoOrBioDetails(0,0,0,$$personaPath)"
			String personaFilePath = step.getParameters().get(0);

			if (step.getParameters().size() <= 2) {
				String[] str = step.getParameters().get(1).split("@@");
				for (String s : str)
					exceptionArray.add(s);
			}

			if (personaFilePath.startsWith("$$")) {
				personaFilePath = step.getScenario().getVariables().get(personaFilePath);
				packetUtility.updateBioException(personaFilePath, exceptionArray, step);
			}
		}
	}
}
