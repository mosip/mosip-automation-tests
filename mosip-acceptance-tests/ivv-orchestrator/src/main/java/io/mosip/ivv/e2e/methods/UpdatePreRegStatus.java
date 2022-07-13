package io.mosip.ivv.e2e.methods;

import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;

public class UpdatePreRegStatus extends BaseTestCaseUtil implements StepInterface {
	String status = "Pending_Appointment";
String response=null;
	@Override
	public void run() throws RigInternalError {
		if (!step.getParameters().isEmpty() && step.getParameters().size() > 1
				&& step.getParameters().get(1).startsWith("$$")) {    // "$$var=e2e_updatePreRegStatus(0,$$prid)"
			status = (step.getParameters().get(0).equalsIgnoreCase("0")) ? this.status : step.getParameters().get(0);
			String prid = step.getParameters().get(1);
			prid = step.getScenario().getVariables().get(prid);
			String validFlag = step.getParameters().get(2);
			if(validFlag.equalsIgnoreCase("valid")) {  //VALID Scenario
			response=packetUtility.updatePreRegStatus(prid, status, contextInuse);
			packetUtility.preRegStatusValidResponse(response);//VALID
			}
			else {
				 status = "Pending_Appointment";  //INVALID Scenario
				response=packetUtility.updatePreRegStatus(prid, status, contextInuse);
				packetUtility.preRegStatusInValidResponse(response);
			}
		} else if (!step.getParameters().isEmpty()) {
			status = (step.getParameters().size() > 0) ? step.getParameters().get(0) : this.status;
			for (String resDataPath : residentPathsPrid.keySet()) {
				response=packetUtility.updatePreRegStatus(residentPathsPrid.get(resDataPath), status, contextInuse);
				packetUtility.preRegStatusValidResponse(response); //Valid
			}
		}
	}
}
