package io.mosip.ivv.e2e.methods;

import org.apache.log4j.Logger;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;

public class UpdateResidentWithRID extends BaseTestCaseUtil implements StepInterface {
	Logger logger = Logger.getLogger(UpdateResidentWithRID.class);

	@Override
	public void run() throws RigInternalError {
		Boolean isForChildPacket = false;
		if (!step.getParameters().isEmpty() && step.getParameters().size() == 1) { // used for child packet processing
			isForChildPacket = Boolean.parseBoolean(step.getParameters().get(0));
			if (isForChildPacket && !generatedResidentData.isEmpty() && rid_updateResident != null)
				packetUtility.updateResidentRid(generatedResidentData.get(0), rid_updateResident);
		} else {
			for (String rid : ridPersonaPath.keySet()) {
				packetUtility.updateResidentRid(ridPersonaPath.get(rid), rid);
			}
		}
	}
}
