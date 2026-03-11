package io.mosip.testrig.dslrig.ivv.e2e.methods;

import static org.testng.Assert.assertTrue;

import java.util.HashMap;
import java.util.LinkedHashMap;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.testng.Reporter;

import io.mosip.testrig.apirig.testrunner.OTPListener;
import io.mosip.testrig.apirig.utils.NotificationListener;
import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testrig.dslrig.ivv.orchestrator.dslConfigManager;

public class VerifyNotification extends BaseTestCaseUtil implements StepInterface {
	public static Logger logger = Logger.getLogger(CheckStatus.class);
	public HashMap<String, String> tempPridAndRid = null;
	public HashMap<String, String> ridStatusMap = new LinkedHashMap<>();

	static {
		if (dslConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	@Override
	public void run() throws RigInternalError {
		String notificationValue = null;
		String emailId = "";

		if (step.getParameters() == null || step.getParameters().isEmpty() || step.getParameters().size() < 2) {
			logger.error("Parameter is missing from DSL step");
			throw new RigInternalError("Missing required parameters: notificationValue, emailIdVar in step: " + step.getName());
		} else if (step.getParameters().size() >= 2) {
			notificationValue = step.getParameters().get(0);
			emailId = step.getScenario().getVariables().get(step.getParameters().get(1));
		}
		if(dslConfigManager.checkNotification().equalsIgnoreCase("no") || dslConfigManager.checkNotification().equalsIgnoreCase("false")) {
			logger.info("Notification verification is disabled in config. Skipping step: " + step.getName());
			Reporter.log("Notification verification is disabled in config. Skipping step: " + step.getName() + " emailId - "+emailId, true);
			NotificationListener.markRequestRemove();
			return;
		}
		try {
		String notification = NotificationListener.getNotification(emailId, notificationValue);
		logger.info("Notification received successfully for verification");

		if (notification == null || !notification.toLowerCase().contains(notificationValue.toLowerCase())) {

			logger.error("Expected notification text '" + notificationValue + "' not found in received notification");
			throw new RigInternalError(
					"Notification verification failed! Expected text not found: " + notificationValue);

		}
		String successMsg = "✅ Notification verification passed for: " + notificationValue;
		logger.info(successMsg);
		Reporter.log(successMsg, true);
		}finally {
		NotificationListener.markRequestRemove();
		}

	}

}

