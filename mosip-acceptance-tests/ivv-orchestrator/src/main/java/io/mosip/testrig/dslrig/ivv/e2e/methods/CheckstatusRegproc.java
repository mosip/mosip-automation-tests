package io.mosip.testrig.dslrig.ivv.e2e.methods;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testrig.dslrig.ivv.orchestrator.dslConfigManager;
import io.restassured.response.Response;

/**
 * Sends the RID to packet creator {@code /resident/workflowstatus/{rid}} and repeats
 * until the returned status matches. Packet creator calls workflow search.
 */
public class CheckstatusRegproc extends BaseTestCaseUtil implements StepInterface {
	private static final Logger logger = Logger.getLogger(CheckstatusRegproc.class);
	private String workflowStatusUrl = "/resident/workflowstatus/";

	static {
		if (dslConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	@Override
	public void run() throws RigInternalError {
		if (step.getParameters() == null || step.getParameters().isEmpty()) {
			throw new RigInternalError("checkstatusRegproc expects status and rid");
		}
		String expectedStatus = resolveScenarioVariable(step, step.getParameters().get(0));
		List<String> rids = new ArrayList<>();
		if (step.getParameters().size() == 1) {
			rids.addAll(step.getScenario().getPridsAndRids().values());
		} else {
			rids.add(resolveScenarioVariable(step, step.getParameters().get(1)));
		}
		for (String rid : rids) {
			checkstatusRegproc(expectedStatus, rid);
		}
	}

	public void checkstatusRegproc(String expectedStatus, String rid) throws RigInternalError {
		if (rid == null || rid.isBlank()) {
			this.hasError = true;
			throw new RigInternalError("RID is null - cannot check regproc workflow status");
		}
		String waitTime = props.getProperty("waitTime");
		int loopCount = Integer.parseInt(props.getProperty("loopCount"));
		String status = "";
		int counter = 0;
		try {
			while (!status.equalsIgnoreCase(expectedStatus) && counter < loopCount) {
				logger.info("Waiting for " + Long.parseLong(waitTime) / 1000 + " sec to get regproc workflow status");
				Thread.sleep(Long.parseLong(waitTime));
				Response response = getRequestSilent(baseUrl + workflowStatusUrl + rid, step);
				status = response == null || response.asString() == null ? "" : response.asString().trim();
				logger.info("Regproc workflow status for " + rid + " => " + status + " | Loop: " + (counter + 1));
				counter++;
			}
		} catch (InterruptedException e) {
			logger.error("Failed due to thread sleep: " + e.getMessage());
			Thread.currentThread().interrupt();
			this.hasError = true;
			throw new RigInternalError("Regproc workflow status check interrupted");
		}
		if (!status.equalsIgnoreCase(expectedStatus)) {
			this.hasError = true;
			throw new RigInternalError("Failed at regproc workflow status (RID: " + rid + ", status: " + status
					+ ", expected: " + expectedStatus + ")");
		}
	}
}
