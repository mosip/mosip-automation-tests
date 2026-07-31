package io.mosip.testrig.dslrig.ivv.e2e.methods;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testrig.dslrig.ivv.orchestrator.DslPacketTemplateCache;
import io.mosip.testrig.dslrig.ivv.orchestrator.dslConfigManager;

public class UpdateResidentWithUIN extends BaseTestCaseUtil implements StepInterface {
	static Logger logger = Logger.getLogger(UpdateResidentWithUIN.class);

	static {
		if (dslConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	@Override
	public void run() throws RigInternalError {
		Boolean isForChildPacket = false;
		if (!step.getParameters().isEmpty() && step.getParameters().size() == 1) {
			isForChildPacket = Boolean.parseBoolean(step.getParameters().get(0));
			if (isForChildPacket && !step.getScenario().getGeneratedResidentData().isEmpty()
					&& step.getScenario().getUin_updateResident() != null) {
				String personaFilePath = step.getScenario().getGeneratedResidentData().get(0);
				packetUtility.updateResidentUIN(personaFilePath, step.getScenario().getUin_updateResident(), step);
				DslPacketTemplateCache.incrementUpdateCounter(personaFilePath);
			}
		} else {
			if (!step.getParameters().isEmpty() && step.getParameters().size() == 2) {
				String personaFilePath = step.getParameters().get(0);
				String _uin = step.getParameters().get(1);
				if (personaFilePath.startsWith("$$") && _uin.startsWith("$$")) {
					personaFilePath = step.getScenario().getVariables().get(personaFilePath);
					_uin = step.getScenario().getVariables().get(_uin);
					packetUtility.updateResidentUIN(personaFilePath, _uin, step);
					DslPacketTemplateCache.incrementUpdateCounter(personaFilePath);
				}
			} else {
				for (String uin : step.getScenario().getUinPersonaProp().stringPropertyNames()) {
					String personaFilePath = step.getScenario().getUinPersonaProp().getProperty(uin);
					packetUtility.updateResidentUIN(personaFilePath, uin, step);
					DslPacketTemplateCache.incrementUpdateCounter(personaFilePath);
				}
			}
		}
	}
}
