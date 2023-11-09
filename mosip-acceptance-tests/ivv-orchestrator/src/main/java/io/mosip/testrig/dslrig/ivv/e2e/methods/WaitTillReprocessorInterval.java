package io.mosip.testrig.dslrig.ivv.e2e.methods;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.testng.Reporter;

import io.mosip.testrig.apirig.kernel.util.ConfigManager;
import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testrig.dslrig.ivv.orchestrator.PacketUtility;

public class WaitTillReprocessorInterval extends BaseTestCaseUtil implements StepInterface {

	public static Logger logger = Logger.getLogger(WaitTillReprocessorInterval.class);

	static {
		if (ConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	@Override
	public void run() throws RigInternalError {
		int waitFromActuator = 0;
		Long waitTime = DEFAULT_WAIT_TIME;

		waitFromActuator = PacketUtility.getActuatorDelay();
		waitTime = TIME_IN_MILLISEC * waitFromActuator;

		try {
			Reporter.log("Total waiting for: " + waitTime / 1000 + " Sec" + " Starting Waiting: " + getDateTime());
			Thread.sleep(waitTime);
			Reporter.log("Waiting Done: " + getDateTime());
		} catch (NumberFormatException e) {
			logger.error(e.getMessage());
		} catch (InterruptedException e) {
			logger.error(e.getMessage());
			Thread.currentThread().interrupt();
		}
	}

}
