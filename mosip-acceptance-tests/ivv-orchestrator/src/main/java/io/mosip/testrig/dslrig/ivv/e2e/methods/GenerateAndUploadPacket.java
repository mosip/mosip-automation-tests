package io.mosip.testrig.dslrig.ivv.e2e.methods;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testrig.dslrig.ivv.orchestrator.dslConfigManager;

public class GenerateAndUploadPacket extends BaseTestCaseUtil implements StepInterface {
	public static Logger logger = Logger.getLogger(GenerateAndUploadPacket.class);

	static {
		if (dslConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	@Override
	public void run() throws RigInternalError {
		String responseStatus = "success";
		Boolean isForChildPacket = false;
		if (!step.getParameters().isEmpty() && step.getParameters().size() == 1) {
			isForChildPacket = Boolean.parseBoolean(step.getParameters().get(0));
			if (isForChildPacket && step.getScenario().getPrid_updateResident() != null
					&& step.getScenario().getTemplatPath_updateResident() != null)
				step.getScenario().setRid_updateResident(packetUtility.generateAndUploadPacket(
						step.getScenario().getPrid_updateResident(), step.getScenario().getTemplatPath_updateResident(),
						step.getScenario().getCurrentStep(), responseStatus, step));
		} else {
			if (!step.getParameters().isEmpty() && step.getParameters().size() == 2) {
				String prid = step.getParameters().get(0);
				String templatePath = step.getParameters().get(1);
				if (prid.startsWith("$$") && templatePath.startsWith("$$")) {
					prid = step.getScenario().getVariables().get(prid);
					templatePath = step.getScenario().getVariables().get(templatePath);
					String rid = packetUtility.generateAndUploadPacket(prid, templatePath,
							step.getScenario().getCurrentStep(), "success", step);
					if (step.getOutVarName() != null)
						step.getScenario().getVariables().put(step.getOutVarName(), rid);

				}
			} else {
				for (String resDataPath : step.getScenario().getResidentTemplatePaths().keySet()) {
					String rid = packetUtility.generateAndUploadPacket(
							step.getScenario().getResidentPathsPrid().get(resDataPath),
							step.getScenario().getResidentTemplatePaths().get(resDataPath),
							step.getScenario().getCurrentStep(), responseStatus, step);
					if (rid != null) {
						step.getScenario().getPridsAndRids()
								.put(step.getScenario().getResidentPathsPrid().get(resDataPath), rid);
						step.getScenario().getRidPersonaPath().put(rid, resDataPath);
					}

				}

			}

		}
	}
}
