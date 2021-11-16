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
						generatedResidentData.get(0), null,contextInuse, "success");
			}
		} else if(step.getParameters().isEmpty()) {  // parent or resident processing e2e_generateAndUploadPacketSkippingPrereg()
			for (String resDataPath : residentTemplatePaths.keySet()) {
				String rid = packetUtility.generateAndUploadPacketSkippingPrereg(
						residentTemplatePaths.get(resDataPath), resDataPath,null, contextInuse, "success");
				if (rid != null) {
					pridsAndRids.put("0", rid);
					ridPersonaPath.put(rid, resDataPath);
				}
			}
			
		}else {
			String residentPath = step.getParameters().get(0);
			String templatePath = step.getParameters().get(1);
			
			String _additionalInfoReqId=null;
			if (step.getParameters().size() > 2) {
				_additionalInfoReqId = step.getParameters().get(2);
				if (!_additionalInfoReqId.isEmpty() && _additionalInfoReqId.startsWith("$$"))
					_additionalInfoReqId = step.getScenario().getVariables().get(_additionalInfoReqId);
			}
			
			if (residentPath.startsWith("$$") && templatePath.startsWith("$$")) { //"$$rid=e2e_generateAndUploadPacketSkippingPrereg($$personaFilePath,$$templatePath)"  --->now "$$rid=e2e_generateAndUploadPacketSkippingPrereg($$personaFilePath,$$templatePath,$$additionalInfoReqId)" 
				residentPath = step.getScenario().getVariables().get(residentPath);
				templatePath = step.getScenario().getVariables().get(templatePath);
				String rid = packetUtility.generateAndUploadPacketSkippingPrereg(templatePath, residentPath,_additionalInfoReqId,
						contextInuse, "success");
				if (step.getOutVarName() != null)
					step.getScenario().getVariables().put(step.getOutVarName(), rid);
			} 
		}
	}

}
