package io.mosip.ivv.e2e.methods;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.log4j.Logger;
import org.testng.Reporter;

import io.mosip.admin.fw.util.AdminTestUtil;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;

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
			if (flag != false) {
				waitFromActuator = getActuatorDelay();
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
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private int getActuatorDelay() {
		String sequence = null;

		sequence = AdminTestUtil.getRegprocWaitFromActuator();
		String[] numbers = sequence.split(",");
		int commonDifference = Integer.parseInt(numbers[1]) - Integer.parseInt(numbers[0]);

		// Convert wait time from regproc actuator in seconds

		int waitFromActuator = commonDifference * 60;
		return waitFromActuator;
	}

}
