package io.mosip.ivv.e2e.methods;

import java.util.LinkedHashMap;

import org.apache.log4j.Logger;
import org.testng.Reporter;

import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;

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
				packetUtility.updateResidentWithGuardianSkippingPreReg(guardianPersonaFilePath, childPersonaFilePath,contextInuse);
			}
		} else {
			residentPathGuardianRid = new LinkedHashMap<String, String>();
			for (String path : residentTemplatePaths.keySet()) {
				residentPathGuardianRid.put(path,
						packetUtility.updateResidentWithGuardianSkippingPreReg(path,null, contextInuse));
				Reporter.log("<b><u>Checking Status Of Created Guardians</u></b>");
				CheckStatus checkStatus = new CheckStatus();
				checkStatus.tempPridAndRid = residentPathGuardianRid;
				// checkStatus.checkStatus("processed");
			}
		}
	}
	

}
