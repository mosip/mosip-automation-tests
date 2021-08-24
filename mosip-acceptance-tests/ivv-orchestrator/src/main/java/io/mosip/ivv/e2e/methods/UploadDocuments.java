package io.mosip.ivv.e2e.methods;

import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;

public class UploadDocuments extends BaseTestCaseUtil implements StepInterface {

	@Override
	public void run() throws RigInternalError {
		Boolean isForChildPacket = false;
		if (!step.getParameters().isEmpty() && step.getParameters().size() == 1) { // used for child packet processing
			isForChildPacket = Boolean.parseBoolean(step.getParameters().get(0));
			if (isForChildPacket && !generatedResidentData.isEmpty() && prid_updateResident != null)
				packetUtility.uploadDocuments(generatedResidentData.get(0), prid_updateResident, contextInuse);
		} else {
			if (!step.getParameters().isEmpty() && step.getParameters().size() > 1) { // "$$var=e2e_uploadDocuments($$personaFilePath,$$prid)"
				String personaFilePath = step.getParameters().get(0);
				String prid = step.getParameters().get(1);
				if (personaFilePath.startsWith("$$") && prid.startsWith("$$")) {
					personaFilePath = step.getScenario().getVariables().get(personaFilePath);
					prid = step.getScenario().getVariables().get(prid);
					packetUtility.uploadDocuments(personaFilePath, prid, contextInuse);
				}
			} else {
				for (String resDataPath : residentPathsPrid.keySet()) {
					packetUtility.uploadDocuments(resDataPath, residentPathsPrid.get(resDataPath), contextInuse);
				}
			}
		}
	}
}
