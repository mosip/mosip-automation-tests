package io.mosip.ivv.e2e.methods;

import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;

public class UpdatePreRegStatus extends BaseTestCaseUtil implements StepInterface {
   String status="Pending_Appointment";
	@Override
	public void run() throws RigInternalError {
		if (!step.getParameters().isEmpty() && step.getParameters().size() == 1) {
			status = step.getParameters().get(0);
		}
		for (String resDataPath : residentPathsPrid.keySet()) {
			packetUtility.updatePreRegStatus(residentPathsPrid.get(resDataPath), status,contextInuse);
		}
	}

}
