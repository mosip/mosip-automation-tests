package io.mosip.testrig.dslrig.ivv.e2e.methods;

import static org.testng.Assert.assertTrue;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testrig.dslrig.ivv.orchestrator.dslConfigManager;

public class RandomDataAssign extends BaseTestCaseUtil implements StepInterface {
	static Logger logger = Logger.getLogger(RandomDataAssign.class);
	public static String _additionalInfo = null;

	static {
		if (dslConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	@Override
	public void run() throws RigInternalError {
		String data = null;

		if (step.getParameters() == null || step.getParameters().isEmpty()) {
			logger.error("Parameter is  missing from DSL step");
			assertTrue(false, "process paramter is  missing in step: " + step.getName());
		} else if (step.getParameters().size() == 1) {
			data = step.getParameters().get(0);
		}
		if (step.getOutVarName() != null)
			step.getScenario().getVariables().put(step.getOutVarName(), data);
	}
}
