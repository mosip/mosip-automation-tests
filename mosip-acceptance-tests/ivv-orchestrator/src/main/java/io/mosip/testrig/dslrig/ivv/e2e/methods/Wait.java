package io.mosip.testrig.dslrig.ivv.e2e.methods;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.testng.Reporter;

import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testrig.dslrig.ivv.orchestrator.dslConfigManager;

public class Wait extends BaseTestCaseUtil implements StepInterface {

	public static Logger logger = Logger.getLogger(Wait.class);

	static {
		if (dslConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	@Override
	public void run() throws RigInternalError {
		long waitTime = DEFAULT_WAIT_TIME;

		String uinWaitTime = dslConfigManager.getUinWaitTime();
		String nextPacketUploadWaitTime = dslConfigManager.getNextPacketUploadWaitTime();

		String param = (step.getParameters() != null && !step.getParameters().isEmpty())
				? step.getParameters().get(0)
				: null;

		try {
			if (param == null || param.isBlank()) {
				logger.info("Wait Time is missing : Taking default Time as 30 Sec");
				waitTime = TIME_IN_MILLISEC * 30;

			} else if ("PACKET_UPLOAD_WAIT_TIME".equalsIgnoreCase(param)) {
				// Handle nextPacketUploadWaitTime
				if (nextPacketUploadWaitTime == null || nextPacketUploadWaitTime.isBlank()) {
					logger.info("Next Packet Upload Wait Time missing : Taking default Time as 15 Min");
					waitTime = TIME_IN_MILLISEC * 60 * 15;
				} else {
					waitTime = TIME_IN_MILLISEC * Integer.parseInt(nextPacketUploadWaitTime);
				}

			} else if ("UIN_WAIT_TIME".equalsIgnoreCase(param)) {
				// Handle uinWaitTime
				if (uinWaitTime == null || uinWaitTime.isBlank()) {
					logger.info("UIN Wait Time missing : Taking default Time as 90 Sec");
					waitTime = TIME_IN_MILLISEC * 90;

				} else {
					waitTime = TIME_IN_MILLISEC * Integer.parseInt(uinWaitTime);
				}

			} else {
				// param is numeric → wait that many seconds
				waitTime = TIME_IN_MILLISEC * Integer.parseInt(param);
			}

			String finalMsg = " Wait Time  " + (waitTime / 1000) + " seconds";
			logger.info(finalMsg);
			Reporter.log(finalMsg, true);
			Thread.sleep(waitTime);

		} catch (NumberFormatException e) {
			logger.error("Invalid wait time format: " + e.getMessage());
		} catch (InterruptedException e) {
			logger.error(e.getMessage());
			Thread.currentThread().interrupt();
		}
	}
}
