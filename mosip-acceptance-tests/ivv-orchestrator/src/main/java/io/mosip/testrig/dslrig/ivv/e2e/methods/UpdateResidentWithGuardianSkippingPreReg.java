package io.mosip.testrig.dslrig.ivv.e2e.methods;

import java.util.LinkedHashMap;

import org.apache.log4j.Logger;
import org.testng.Reporter;

import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;

public class UpdateResidentWithGuardianSkippingPreReg extends BaseTestCaseUtil implements StepInterface{
	Logger logger = Logger.getLogger(UpdateResidentWithGuardianSkippingPreReg.class);
	@Override
	public void run() throws RigInternalError {
		
		if (!step.getParameters().isEmpty() && step.getParameters().size() == 2) {
			String guardianPersonaFilePath = step.getParameters().get(0);
			String childPersonaFilePath = step.getParameters().get(1);
			if (guardianPersonaFilePath.startsWith("$$") && childPersonaFilePath.startsWith("$$")) {
				guardianPersonaFilePath = step.getScenario().getVariables().get(guardianPersonaFilePath);
				childPersonaFilePath = step.getScenario().getVariables().get(childPersonaFilePath);
				packetUtility.updateResidentWithGuardianSkippingPreReg(guardianPersonaFilePath, 
						childPersonaFilePath,step.getScenario().getCurrentStep(),step);
			}
		} else {
			step.getScenario().setResidentPathGuardianRid( new LinkedHashMap<String, String>());
			for (String path : step.getScenario().getResidentTemplatePaths().keySet()) {
				step.getScenario().getResidentPathGuardianRid().put(path,
						packetUtility.updateResidentWithGuardianSkippingPreReg(path,null, 
								step.getScenario().getCurrentStep(),step));
				Reporter.log("<b><u>Checking Status Of Created Guardians</u></b>");
				CheckStatus checkStatus = new CheckStatus();
				checkStatus.tempPridAndRid = step.getScenario().getResidentPathGuardianRid();
				// checkStatus.checkStatus("processed");
			}
		}
	}
	

}
