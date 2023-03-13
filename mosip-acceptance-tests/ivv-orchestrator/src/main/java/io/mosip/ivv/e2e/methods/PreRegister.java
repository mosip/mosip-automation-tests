package io.mosip.ivv.e2e.methods;

import org.testng.Reporter;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;

public class PreRegister extends BaseTestCaseUtil implements StepInterface {

	@Override
	public void run() throws RigInternalError {
		Boolean isForChildPacket = false;
		if (!step.getParameters().isEmpty() && step.getParameters().size() == 1 && !step.getParameters().get(0).startsWith("$$")) { // used for child packet processing
			isForChildPacket = Boolean.parseBoolean(step.getParameters().get(0));
			if (isForChildPacket && !step.getScenario().getGeneratedResidentData().isEmpty())
				step.getScenario().setPrid_updateResident(packetUtility.preReg(step.getScenario().getGeneratedResidentData().get(0), step.getScenario().getCurrentStep()));
		} 
		else {
			if (step.getParameters().size() == 1 && step.getParameters().get(0).startsWith("$$")) {   // "$$prid= e2e_preRegister($$personaFilePath)"
				String personaFilePath = step.getParameters().get(0);
				personaFilePath = step.getScenario().getVariables().get(personaFilePath);
				String prid = packetUtility.preReg(personaFilePath, step.getScenario().getCurrentStep());
				if(step.getOutVarName()!=null)
					 step.getScenario().getVariables().put(step.getOutVarName(), prid);
			} else {
				int count = 1;
				for (String resDataPath : step.getScenario().getResidentTemplatePaths().keySet()) {
					Reporter.log("<b><u>" + "PreRegister testCase: " + count + "</u></b>");
					count++;
					String prid = packetUtility.preReg(resDataPath, step.getScenario().getCurrentStep());
					step.getScenario().getResidentPathsPrid().put(resDataPath, prid);
				}
			}
		}
	}

}
