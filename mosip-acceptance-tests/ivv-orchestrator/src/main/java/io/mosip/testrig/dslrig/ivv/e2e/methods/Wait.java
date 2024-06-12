package io.mosip.testrig.dslrig.ivv.e2e.methods;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import io.mosip.testrig.apirig.utils.ConfigManager;
import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;

public class Wait extends BaseTestCaseUtil implements StepInterface {

	public static Logger logger = Logger.getLogger(Wait.class);

	static {
		if (ConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	@Override
	public void run() throws RigInternalError {
		Long waitTime = DEFAULT_WAIT_TIME;
		if (step.getParameters() == null || step.getParameters().isEmpty()) {
			logger.warn("Wait Time is Missing : Taking default Time as 30 Sec");
		}
		else {
			waitTime = TIME_IN_MILLISEC * Integer.parseInt(step.getParameters().get(0));
		}

		try {
//			Reporter.log("Total waiting for: " + waitTime / 1000 + " Sec " + "Starting Waiting: " + getDateTime());
			Thread.sleep(waitTime);
//			Reporter.log("Waiting Done: " + getDateTime());
		} catch (NumberFormatException e) {
			logger.error(e.getMessage());
		} catch (InterruptedException e) {
			logger.error(e.getMessage());
			Thread.currentThread().interrupt();
		}
	}

}
