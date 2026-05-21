package io.mosip.testrig.dslrig.ivv.e2e.methods;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testrig.dslrig.ivv.orchestrator.dslConfigManager;

public class UploadPacketWithInvalidHash extends BaseTestCaseUtil implements StepInterface {
	public static Logger logger = Logger.getLogger(UploadPacketWithInvalidHash.class);

	static {
		if (dslConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	@Override
	public void run() throws RigInternalError {
		Boolean isForChildPacket = false;
		boolean getRidFromSync = true;
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
				String rid = packetUtility.generateAndUploadPacketWrongHash(
						step.getScenario().getResidentTemplatePaths().get(resDataPath), resDataPath, null,
						step.getScenario().getCurrentStep(), "success", step, getRidFromSync, invalidMachineFlag);
				if (rid != null) {
					step.getScenario().getPridsAndRids().put("0", rid);
					step.getScenario().getRidPersonaPath().put(rid, resDataPath);
				}
			}

		} else {
			String _additionalInfoReqId = null;
			if (step.getParameters().size() > 3) {
				_additionalInfoReqId = step.getParameters().get(3);
				if (!_additionalInfoReqId.isEmpty() && _additionalInfoReqId.startsWith("$$")) {
					_additionalInfoReqId = resolveScenarioVariable(step, _additionalInfoReqId);
				}
			}

			String[] paths = resolvePersonaAndTemplatePaths(step);
			String rid = packetUtility.generateAndUploadPacketWrongHash(paths[1], paths[0], _additionalInfoReqId,
					step.getScenario().getCurrentStep(), "success", step, getRidFromSync, invalidMachineFlag);
			if (rid == null || rid.isBlank()) {
				throw new RigInternalError("Unable to generate and upload packet; registration ID was not returned");
			}
			if (step.getOutVarName() != null) {
				step.getScenario().getVariables().put(step.getOutVarName(), rid);
			}
		}
	}

}
