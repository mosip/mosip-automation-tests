package io.mosip.testrig.dslrig.ivv.e2e.methods;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testrig.dslrig.ivv.orchestrator.dslConfigManager;

public class GenerateAndUploadPacketSkippingPreregWithInvalidCbeff extends BaseTestCaseUtil implements StepInterface {
	public static Logger logger = Logger.getLogger(GenerateAndUploadPacketSkippingPreregWithInvalidCbeff.class);

	static {
		if (dslConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	@Override
	public void run() throws RigInternalError {
		boolean getRidFromSync = true;
		Boolean isForChildPacket = false;
		String invalidMachineFlag = "";
		if (!step.getParameters().isEmpty() && step.getParameters().size() == 1) {
			isForChildPacket = Boolean.parseBoolean(step.getParameters().get(0));
			if (isForChildPacket && !step.getScenario().getGeneratedResidentData().isEmpty()
					&& step.getScenario().getTemplatPath_updateResident() != null) {
				step.getScenario().setRid_updateResident(packetUtility.generateAndUploadPacketSkippingPrereg(
						step.getScenario().getTemplatPath_updateResident(),
						step.getScenario().getGeneratedResidentData().get(0), null, step.getScenario().getCurrentStep(),
						"success", step, getRidFromSync, invalidMachineFlag));
			}
		} else if (step.getParameters().isEmpty()) {
			for (String resDataPath : step.getScenario().getResidentTemplatePaths().keySet()) {
				String rid = packetUtility.generateAndUploadPacketSkippingPrereg(
						step.getScenario().getResidentTemplatePaths().get(resDataPath), resDataPath, null,
						step.getScenario().getCurrentStep(), "success", step, getRidFromSync, invalidMachineFlag);
				if (rid != null) {
					step.getScenario().getPridsAndRids().put("0", rid);
					step.getScenario().getRidPersonaPath().put(rid, resDataPath);
				}
			}

		} else {
			String residentPath = step.getParameters().get(0);
			String templatePath = step.getParameters().get(1);

			if (step.getParameters().size() == 3) {
				getRidFromSync = Boolean.parseBoolean(step.getParameters().get(2));

			}

			String _additionalInfoReqId = null;
			if (step.getParameters().size() > 3) {
				_additionalInfoReqId = step.getParameters().get(3);
				if (!_additionalInfoReqId.isEmpty() && _additionalInfoReqId.startsWith("$$"))
					_additionalInfoReqId = step.getScenario().getVariables().get(_additionalInfoReqId);
			}

			if (residentPath.startsWith("$$") && templatePath.startsWith("$$")) {
				residentPath = step.getScenario().getVariables().get(residentPath);
				templatePath = step.getScenario().getVariables().get(templatePath);
				String rid = packetUtility.generateAndUploadWithInvalidCbeffPacketSkippingPrereg(templatePath,
						residentPath, _additionalInfoReqId, step.getScenario().getCurrentStep(), "success", step,
						getRidFromSync, invalidMachineFlag);
				if (step.getOutVarName() != null)
					step.getScenario().getVariables().put(step.getOutVarName(), rid);
			}
		}
	}

}
