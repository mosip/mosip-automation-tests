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
			if (isForChildPacket && !step.getScenario().getGeneratedResidentData().isEmpty() && step.getScenario().getRid_updateResident() != null)
				packetUtility.updateResidentRid(step.getScenario().getGeneratedResidentData().get(0), step.getScenario().getRid_updateResident(),step);
		} else {
			if (!step.getParameters().isEmpty() && step.getParameters().size() == 2) {
				String personaFilePath = step.getParameters().get(0);
				String _rid = step.getParameters().get(1);
				if (personaFilePath.startsWith("$$") && _rid.startsWith("$$")) {
					personaFilePath = step.getScenario().getVariables().get(personaFilePath);
					_rid = step.getScenario().getVariables().get(_rid);
					packetUtility.updateResidentRid(personaFilePath, _rid,step);
				}
			} else {
				for (String rid : step.getScenario().getRidPersonaPath().keySet()) {
					packetUtility.updateResidentRid(step.getScenario().getRidPersonaPath().get(rid), rid,step);
				}
			}
			
		}
	}
}
