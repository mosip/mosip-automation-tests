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
			if (!step.getParameters().isEmpty() && step.getParameters().size() == 2) {
				String personaFilePath = step.getParameters().get(0);
				String _rid = step.getParameters().get(1);
				if (personaFilePath.startsWith("$$") && _rid.startsWith("$$")) {
					personaFilePath = step.getScenario().getVariables().get(personaFilePath);
					_rid = step.getScenario().getVariables().get(_rid);
					packetUtility.updateResidentRid(personaFilePath, _rid);
				}
			} else {
				for (String rid : ridPersonaPath.keySet()) {
					packetUtility.updateResidentRid(ridPersonaPath.get(rid), rid);
				}
			}
			
		}
	}
}
