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
			if (isForChildPacket && prid_updateResident != null && templatPath_updateResident != null)
				rid_updateResident=packetUtility.generateAndUploadPacket(prid_updateResident, templatPath_updateResident, contextInuse,
						responseStatus);
		} else {
			if (!step.getParameters().isEmpty() && step.getParameters().size() == 2) {  // "$$rid=e2e_generateAndUploadPacket($$prid,$$templatePath)"
				String prid = step.getParameters().get(0);
				String templatePath = step.getParameters().get(1);
				if (prid.startsWith("$$") && templatePath.startsWith("$$")) {
					prid = step.getScenario().getVariables().get(prid);
					templatePath = step.getScenario().getVariables().get(templatePath);
					String rid = packetUtility.generateAndUploadPacket(prid, templatePath, contextInuse, "success");
					if (step.getOutVarName() != null)
						step.getScenario().getVariables().put(step.getOutVarName(), rid);

				}
			} else {
				for (String resDataPath : residentTemplatePaths.keySet()) {
					String rid = packetUtility.generateAndUploadPacket(residentPathsPrid.get(resDataPath),
							residentTemplatePaths.get(resDataPath), contextInuse, responseStatus);
					if (rid != null) {
						pridsAndRids.put(residentPathsPrid.get(resDataPath), rid);
						ridPersonaPath.put(rid, resDataPath);
					}

				}

			}

		}
	}
}
