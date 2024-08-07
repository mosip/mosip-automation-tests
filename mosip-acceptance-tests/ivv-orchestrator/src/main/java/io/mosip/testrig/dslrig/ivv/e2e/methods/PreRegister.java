package io.mosip.testrig.dslrig.ivv.e2e.methods;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.testng.Reporter;

import io.mosip.testrig.apirig.utils.ConfigManager;
import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;

@Scope("prototype")
@Component
public class PreRegister extends BaseTestCaseUtil implements StepInterface {
	public static Logger logger = Logger.getLogger(PreRegister.class);

	static {
		if (ConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	@Override
	public void run() throws RigInternalError {
		Boolean isForChildPacket = false;
		if (!step.getParameters().isEmpty() && step.getParameters().size() == 1
				&& !step.getParameters().get(0).startsWith("$$")) { // used for child packet processing
			isForChildPacket = Boolean.parseBoolean(step.getParameters().get(0));
			if (isForChildPacket && !step.getScenario().getGeneratedResidentData().isEmpty())
				step.getScenario().setPrid_updateResident(
						packetUtility.preReg(step.getScenario().getGeneratedResidentData().get(0),
								step.getScenario().getCurrentStep(), step));
		} else {
			if (step.getParameters().size() == 1 && step.getParameters().get(0).startsWith("$$")) { // "$$prid=
																									// e2e_preRegister($$personaFilePath)"
				String personaFilePath = step.getParameters().get(0);
				personaFilePath = step.getScenario().getVariables().get(personaFilePath);
				String prid = packetUtility.preReg(personaFilePath, step.getScenario().getCurrentStep(), step);
				if (step.getOutVarName() != null)
					step.getScenario().getVariables().put(step.getOutVarName(), prid);
			} else {
				int count = 1;
				for (String resDataPath : step.getScenario().getResidentTemplatePaths().keySet()) {
					Reporter.log("<b><u>" + "PreRegister testCase: " + count + "</u></b>");
					count++;
					String prid = packetUtility.preReg(resDataPath, step.getScenario().getCurrentStep(), step);
					step.getScenario().getResidentPathsPrid().put(resDataPath, prid);
				}
			}
		}
	}

}
