package io.mosip.ivv.e2e.methods;

import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;

public class UploadPacketWithInvalidHash extends BaseTestCaseUtil implements StepInterface {

	@Override
	public void run() throws RigInternalError {
		Boolean isForChildPacket=false;
		boolean getRidFromSync=true;
		if(!step.getParameters().isEmpty() && step.getParameters().size()==1) {  //  used for child packet processing
			isForChildPacket = Boolean.parseBoolean(step.getParameters().get(0));
			if (isForChildPacket && !step.getScenario().getGeneratedResidentData().isEmpty()  && step.getScenario().getTemplatPath_updateResident()!=null) { 
				step.getScenario().setRid_updateResident( packetUtility.generateAndUploadPacketSkippingPrereg(step.getScenario().getTemplatPath_updateResident(),
						step.getScenario().getGeneratedResidentData().get(0), null,step.getScenario().getCurrentStep(), "success",step,getRidFromSync));
			}
		} else if(step.getParameters().isEmpty()) {  // parent or resident processing e2e_generateAndUploadPacketSkippingPrereg()
			for (String resDataPath : step.getScenario().getResidentTemplatePaths().keySet()) {
				String rid = packetUtility.generateAndUploadPacketWrongHash(
						step.getScenario().getResidentTemplatePaths().get(resDataPath), resDataPath,null, step.getScenario().getCurrentStep(), "success",step,getRidFromSync);
				if (rid != null) {
					step.getScenario().getPridsAndRids().put("0", rid);
					step.getScenario().getRidPersonaPath().put(rid, resDataPath);
				}
			}
			
		}else {
			String residentPath = step.getParameters().get(0);
			String templatePath = step.getParameters().get(1);
						
			String _additionalInfoReqId=null;
			if (step.getParameters().size() > 3) {
				_additionalInfoReqId = step.getParameters().get(3);
				if (!_additionalInfoReqId.isEmpty() && _additionalInfoReqId.startsWith("$$"))
					_additionalInfoReqId = step.getScenario().getVariables().get(_additionalInfoReqId);
			}
			

			if (residentPath.startsWith("$$") && templatePath.startsWith("$$")) { //"$$rid=e2e_generateAndUploadPacketSkippingPrereg($$personaFilePath,$$templatePath)"  --->now "$$rid=e2e_generateAndUploadPacketSkippingPrereg($$personaFilePath,$$templatePath,$$additionalInfoReqId)" 
				residentPath = step.getScenario().getVariables().get(residentPath);
				templatePath = step.getScenario().getVariables().get(templatePath);
				String rid = packetUtility.generateAndUploadPacketWrongHash(templatePath, residentPath,_additionalInfoReqId,
						step.getScenario().getCurrentStep(), "success",step,getRidFromSync);
				if (step.getOutVarName() != null)
					step.getScenario().getVariables().put(step.getOutVarName(), rid);
			} 
		}
	}

}
