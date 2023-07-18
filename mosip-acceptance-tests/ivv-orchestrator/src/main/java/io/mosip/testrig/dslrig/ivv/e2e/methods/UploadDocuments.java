package io.mosip.testrig.dslrig.ivv.e2e.methods;

import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;

public class UploadDocuments extends BaseTestCaseUtil implements StepInterface {

	@Override
	public void run() throws RigInternalError {
		Boolean isForChildPacket = false;
		if (!step.getParameters().isEmpty() && step.getParameters().size() == 1) { // used for child packet processing
			isForChildPacket = Boolean.parseBoolean(step.getParameters().get(0));
			if (isForChildPacket && !step.getScenario().getGeneratedResidentData().isEmpty() && step.getScenario().getPrid_updateResident() != null)
				packetUtility.uploadDocuments(step.getScenario().getGeneratedResidentData().get(0), 
						step.getScenario().getPrid_updateResident(), step.getScenario().getCurrentStep(),step);
		} else {
			if (!step.getParameters().isEmpty() && step.getParameters().size() > 1) { // "$$var=e2e_uploadDocuments($$personaFilePath,$$prid)"
				String personaFilePath = step.getParameters().get(0);
				String prid = step.getParameters().get(1);
				if (personaFilePath.startsWith("$$") && prid.startsWith("$$")) {
					personaFilePath = step.getScenario().getVariables().get(personaFilePath);
					prid = step.getScenario().getVariables().get(prid);
					packetUtility.uploadDocuments(personaFilePath, prid, step.getScenario().getCurrentStep(),step);
				}
			} else {
				for (String resDataPath : step.getScenario().getResidentPathsPrid().keySet()) {
					packetUtility.uploadDocuments(resDataPath, step.getScenario().getResidentPathsPrid().get(resDataPath), step.getScenario().getCurrentStep(),step);
				}
			}
		}
	}
}
