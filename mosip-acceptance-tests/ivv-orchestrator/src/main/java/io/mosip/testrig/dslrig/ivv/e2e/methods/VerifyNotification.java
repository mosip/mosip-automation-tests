package io.mosip.testrig.dslrig.ivv.e2e.methods;

import static org.testng.Assert.assertTrue;

import java.util.HashMap;
import java.util.LinkedHashMap;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.testng.Reporter;

import io.mosip.testrig.apirig.testrunner.AllNotificationListner;
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
	    String notifiationValue = null;
	    String emailId = "";

	    if (step.getParameters() == null || step.getParameters().isEmpty()) {
	        logger.error("Parameter is missing from DSL step");
	        throw new RigInternalError("qStatusCode parameter is missing in step: " + step.getName());
	    } else if (step.getParameters().size() >= 2) {
	        notifiationValue = step.getParameters().get(0); // expected text e.g. "UIN successfully generated"
	        emailId = step.getScenario().getVariables().get(step.getParameters().get(1));
	    }

	    String notification = AllNotificationListner.getNotification(emailId);
	    logger.info("Notification received: " + notification);

	    if (notification == null || 
	        !notification.toLowerCase().contains(notifiationValue.toLowerCase())) {
	        
	        logger.error("Expected notification text '" + notifiationValue 
	                    + "' not found in: " + notification);
	        throw new RigInternalError("Notification verification failed! Expected text not found: " 
	                                   + notifiationValue);
	    } else {
	        String successMsg = "✅ Notification verification passed: " + notification;
	        logger.info(successMsg);
	        Reporter.log(successMsg, true);
	    }
	}


	
}
