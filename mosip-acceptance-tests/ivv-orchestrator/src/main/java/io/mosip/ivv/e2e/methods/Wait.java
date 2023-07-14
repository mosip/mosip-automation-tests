package io.mosip.ivv.e2e.methods;

import org.apache.log4j.Logger;
import org.testng.Reporter;

import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.ivv.orchestrator.PacketUtility;

public class Wait extends BaseTestCaseUtil implements StepInterface {

	Logger logger = Logger.getLogger(Wait.class);

	@Override
	public void run() throws RigInternalError {
		int waitFromActuator = 0;
		Long waitTime = DEFAULT_WAIT_TIME;
		if (step.getParameters() == null || step.getParameters().isEmpty()) {
			logger.warn("Wait Time is Missing : Taking default Time as 30 Sec");
		}

	// Pass flag as true in the step to get the wait time from the regproc actuator for the reprocessor to kick in  
		else if (step.getParameters().size() == 1 && step.getParameters().get(0).contains("true")) {
			Boolean flag = Boolean.parseBoolean(step.getParameters().get(0));
			if (flag) {
				waitFromActuator = PacketUtility.getActuatorDelay();
				waitTime = TIME_IN_MILLISEC * waitFromActuator;
			}

		} else {
			waitTime = TIME_IN_MILLISEC * Integer.parseInt(step.getParameters().get(0));
		}

		try {
			Reporter.log("Total waiting for: " + waitTime / 1000 + " Sec", true);
			Reporter.log("Starting Waiting: " + getDateTime(), true);
			Thread.sleep(waitTime);
			Reporter.log("Waiting Done: " + getDateTime(), true);
		} catch (NumberFormatException e) {
			logger.error(e.getMessage());
		} catch (InterruptedException e) {
			logger.error(e.getMessage());
			Thread.currentThread().interrupt();
		}
	}

	

}
