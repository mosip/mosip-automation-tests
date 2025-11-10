package io.mosip.testrig.dslrig.ivv.e2e.methods;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testrig.dslrig.ivv.orchestrator.dslConfigManager;

public class UpdatePreRegStatus extends BaseTestCaseUtil implements StepInterface {
	String status = "Pending_Appointment";
	String response = null;
	public static Logger logger = Logger.getLogger(UpdatePreRegStatus.class);

	static {
		if (dslConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	@Override
	public void run() throws RigInternalError {
		if (!step.getParameters().isEmpty() && step.getParameters().size() > 1
				&& step.getParameters().get(1).startsWith("$$")) { // "$$var=e2e_updatePreRegStatus(0,$$prid)"
			status = (step.getParameters().get(0).equalsIgnoreCase("0")) ? this.status : step.getParameters().get(0);
			String prid = step.getParameters().get(1);
			prid = step.getScenario().getVariables().get(prid);
			String validFlag = step.getParameters().get(2);
			if (validFlag.equalsIgnoreCase("valid")) { // VALID Scenario
				response = packetUtility.updatePreRegStatus(prid, status, step.getScenario().getCurrentStep(), step);
				packetUtility.preRegStatusValidResponse(response);// VALID
			} else {
				status = "Pending_Appointment"; // INVALID Scenario
				response = packetUtility.updatePreRegStatus(prid, status, step.getScenario().getCurrentStep(), step);
				packetUtility.preRegStatusInValidResponse(response);
			}
		} else if (!step.getParameters().isEmpty()) {
			status = (step.getParameters().size() > 0) ? step.getParameters().get(0) : this.status;
			for (String resDataPath : step.getScenario().getResidentPathsPrid().keySet()) {
				response = packetUtility.updatePreRegStatus(step.getScenario().getResidentPathsPrid().get(resDataPath),
						status, step.getScenario().getCurrentStep(), step);
				packetUtility.preRegStatusValidResponse(response); // Valid
			}
		}
	}
}
