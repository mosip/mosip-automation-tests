package io.mosip.testrig.dslrig.ivv.e2e.methods;

import org.apache.log4j.Logger;

import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;

public class UpdateResidentWithUIN extends BaseTestCaseUtil implements StepInterface {
	Logger logger = Logger.getLogger(UpdateResidentWithUIN.class);

	@Override
	public void run() throws RigInternalError {
		Boolean isForChildPacket = false;
		if (!step.getParameters().isEmpty() && step.getParameters().size() == 1) { // used for child packet processing
			isForChildPacket = Boolean.parseBoolean(step.getParameters().get(0));
			if (isForChildPacket && !step.getScenario().getGeneratedResidentData().isEmpty()  && step.getScenario().getUin_updateResident()!=null) 
				packetUtility.updateResidentUIN(step.getScenario().getGeneratedResidentData().get(0), step.getScenario().getUin_updateResident(),step);
		} else {
			if (!step.getParameters().isEmpty() && step.getParameters().size() == 2) {  //"e2e_updateResidentWithUIN($$personaFilePath,$$uin)"
				String personaFilePath = step.getParameters().get(0);
				String _uin = step.getParameters().get(1);
				if (personaFilePath.startsWith("$$") && _uin.startsWith("$$")) {
					personaFilePath = step.getScenario().getVariables().get(personaFilePath);
					_uin = step.getScenario().getVariables().get(_uin);
					packetUtility.updateResidentUIN(personaFilePath, _uin,step);
				}
			} else {
				for (String uin : step.getScenario().getUinPersonaProp().stringPropertyNames()) {
					packetUtility.updateResidentUIN(step.getScenario().getUinPersonaProp().getProperty(uin), uin,step);
				}
			}
		}
	}
}
