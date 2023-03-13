package io.mosip.ivv.e2e.methods;

import org.apache.log4j.Logger;

import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;

public class UpdateResidentWithUIN extends BaseTestCaseUtil implements StepInterface {
	Logger logger = Logger.getLogger(UpdateResidentWithUIN.class);

	@Override
	public void run() throws RigInternalError {
		Boolean isForChildPacket = false;
		if (!step.getParameters().isEmpty() && step.getParameters().size() == 1) { // used for child packet processing
			isForChildPacket = Boolean.parseBoolean(step.getParameters().get(0));
			if (isForChildPacket && !step.getScenario().getGeneratedResidentData().isEmpty()  && step.getScenario().getUin_updateResident()!=null) 
				packetUtility.updateResidentUIN(step.getScenario().getGeneratedResidentData().get(0), step.getScenario().getUin_updateResident());
		} else {
			if (!step.getParameters().isEmpty() && step.getParameters().size() == 2) {  //"e2e_updateResidentWithUIN($$personaFilePath,$$uin)"
				String personaFilePath = step.getParameters().get(0);
				String _uin = step.getParameters().get(1);
				if (personaFilePath.startsWith("$$") && _uin.startsWith("$$")) {
					personaFilePath = step.getScenario().getVariables().get(personaFilePath);
					_uin = step.getScenario().getVariables().get(_uin);
					packetUtility.updateResidentUIN(personaFilePath, _uin);
				}
			} else {
				for (String uin : step.getScenario().getUinPersonaProp().stringPropertyNames()) {
					packetUtility.updateResidentUIN(step.getScenario().getUinPersonaProp().getProperty(uin), uin);
				}
			}
		}
	}
}
