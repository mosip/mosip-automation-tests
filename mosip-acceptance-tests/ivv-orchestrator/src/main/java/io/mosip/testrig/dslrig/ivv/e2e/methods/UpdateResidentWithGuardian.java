package io.mosip.testrig.dslrig.ivv.e2e.methods;

import java.util.LinkedHashMap;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.testng.Reporter;

import io.mosip.testrig.apirig.utils.ConfigManager;
import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;

public class UpdateResidentWithGuardian extends BaseTestCaseUtil implements StepInterface {
	static Logger logger = Logger.getLogger(UpdateResidentWithGuardian.class);
	
	static {
		if (ConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	@Override
	public void run() throws RigInternalError {
		String gaurdianStatus="processed";
		if (!step.getParameters().isEmpty() && step.getParameters().size() == 2) {   //"var=e2e_updateResidentWithGuardian($$guardianPersonaFilePath,$$childPersonaFilePath)"
			String guardianPersonaFilePath = step.getParameters().get(0);
			String childPersonaFilePath = step.getParameters().get(1);
			if (guardianPersonaFilePath.startsWith("$$") && childPersonaFilePath.startsWith("$$")) {
				guardianPersonaFilePath = step.getScenario().getVariables().get(guardianPersonaFilePath);
				childPersonaFilePath = step.getScenario().getVariables().get(childPersonaFilePath);
				packetUtility.updateResidentWithGuardianSkippingPreReg(guardianPersonaFilePath, childPersonaFilePath,
						step.getScenario().getCurrentStep(),step);
			}
		} else {
			step.getScenario().setResidentPathGuardianRid(new LinkedHashMap<String, String>());
			CheckStatus checkStatus = new CheckStatus();
			for (String path : step.getScenario().getResidentTemplatePaths().keySet()) {
				step.getScenario().getResidentPathGuardianRid().put(path, packetUtility.updateResidentGuardian(path,step));
				Reporter.log("<b><u>Checking Status Of Created Guardians</u></b>");
				checkStatus.tempPridAndRid = step.getScenario().getResidentPathGuardianRid();
				//checkStatus.checkStatus(gaurdianStatus);
			}
		}
	}

}