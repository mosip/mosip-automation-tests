package io.mosip.ivv.e2e.methods;

import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;

public class GenerateAndUploadPacketSkippingPrereg extends BaseTestCaseUtil implements StepInterface {

	@Override
	public void run() throws RigInternalError {
		Boolean isForChildPacket=false;
		if(!step.getParameters().isEmpty() && step.getParameters().size()==1) {  //  used for child packet processing
			isForChildPacket = Boolean.parseBoolean(step.getParameters().get(0));
			if (isForChildPacket && !generatedResidentData.isEmpty()  && templatPath_updateResident!=null) { 
				rid_updateResident = packetUtility.generateAndUploadPacketSkippingPrereg(templatPath_updateResident,
						generatedResidentData.get(0), contextInuse, "success");
			}
		} else {
			String residentPath = step.getParameters().get(0);
			String templatePath = step.getParameters().get(1);
			if (residentPath.startsWith("$$") && templatePath.startsWith("$$")) { //"$$rid=e2e_generateAndUploadPacketSkippingPrereg($$personaFilePath,$$templatePath)"
				residentPath = step.getScenario().getVariables().get(residentPath);
				templatePath = step.getScenario().getVariables().get(templatePath);
				String rid = packetUtility.generateAndUploadPacketSkippingPrereg(templatePath, residentPath,
						contextInuse, "success");
				if (step.getOutVarName() != null)
					step.getScenario().getVariables().put(step.getOutVarName(), rid);
			} else {
				for (String resDataPath : residentTemplatePaths.keySet()) {
					String rid = packetUtility.generateAndUploadPacketSkippingPrereg(
							residentTemplatePaths.get(resDataPath), resDataPath, contextInuse, "success");
					if (rid != null) {
						pridsAndRids.put("0", rid);
						ridPersonaPath.put(rid, resDataPath);
					}
				}
			}
		}
	}

}
