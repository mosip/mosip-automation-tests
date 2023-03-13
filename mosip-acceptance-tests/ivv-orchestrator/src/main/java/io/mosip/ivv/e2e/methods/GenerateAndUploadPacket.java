package io.mosip.ivv.e2e.methods;

import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;

public class GenerateAndUploadPacket extends BaseTestCaseUtil implements StepInterface {

	@Override
	public void run() throws RigInternalError {
		String responseStatus = "success";
		Boolean isForChildPacket = false;
		if (!step.getParameters().isEmpty() && step.getParameters().size() == 1) { // used for child packet processing
			isForChildPacket = Boolean.parseBoolean(step.getParameters().get(0));
			if (isForChildPacket && step.getScenario().getPrid_updateResident() != null && step.getScenario().getTemplatPath_updateResident() != null)
				step.getScenario().setRid_updateResident(packetUtility.generateAndUploadPacket(step.getScenario().getPrid_updateResident(), 
						step.getScenario().getTemplatPath_updateResident(), step.getScenario().getCurrentStep(),
						responseStatus));
		} else {
			if (!step.getParameters().isEmpty() && step.getParameters().size() == 2) {  // "$$rid=e2e_generateAndUploadPacket($$prid,$$templatePath)"
				String prid = step.getParameters().get(0);
				String templatePath = step.getParameters().get(1);
				if (prid.startsWith("$$") && templatePath.startsWith("$$")) {
					prid = step.getScenario().getVariables().get(prid);
					templatePath = step.getScenario().getVariables().get(templatePath);
					String rid = packetUtility.generateAndUploadPacket(prid, templatePath, step.getScenario().getCurrentStep(), "success");
					if (step.getOutVarName() != null)
						step.getScenario().getVariables().put(step.getOutVarName(), rid);

				}
			} else {
				for (String resDataPath : step.getScenario().getResidentTemplatePaths().keySet()) {
					String rid = packetUtility.generateAndUploadPacket(step.getScenario().getResidentPathsPrid().get(resDataPath),
							step.getScenario().getResidentTemplatePaths().get(resDataPath), step.getScenario().getCurrentStep(), responseStatus);
					if (rid != null) {
						step.getScenario().getPridsAndRids().put(step.getScenario().getResidentPathsPrid().get(resDataPath), rid);
						step.getScenario().getRidPersonaPath().put(rid, resDataPath);
					}

				}

			}

		}
	}
}
