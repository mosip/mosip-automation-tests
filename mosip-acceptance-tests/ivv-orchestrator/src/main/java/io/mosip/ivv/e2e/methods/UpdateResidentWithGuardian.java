package io.mosip.ivv.e2e.methods;

import java.util.LinkedHashMap;
import org.apache.log4j.Logger;
import org.testng.Reporter;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;

public class UpdateResidentWithGuardian extends BaseTestCaseUtil implements StepInterface {
	Logger logger = Logger.getLogger(UpdateResidentWithGuardian.class);

	@Override
	public void run() throws RigInternalError {
		boolean withRid = true;
		String missingFields = null;
		String gaurdianStatus="processed";
		String emailOrPhone=null;
		if (step.getParameters() == null || step.getParameters().isEmpty() || step.getParameters().size() < 1) {
			logger.warn("UpdateResidentWithGuardian Arugemnt is  Missing : Please pass the argument from DSL sheet");
		} else {
			withRid = Boolean.parseBoolean(step.getParameters().get(0));
			if (step.getParameters().size() > 1) {
				missingFields = step.getParameters().get(1);
				gaurdianStatus= step.getParameters().get(2);
				emailOrPhone=step.getParameters().get(3);
			}
		}
		residentPathGuardianRid = new LinkedHashMap<String, String>();
		CheckStatus checkStatus = new CheckStatus();
		for (String path : residentTemplatePaths.keySet()) {
			residentPathGuardianRid.put(path, packetUtility.updateResidentGuardian(path, withRid, missingFields,emailOrPhone));
			Reporter.log("<b><u>Checking Status Of Created Guardians</u></b>");
			checkStatus.tempPridAndRid = residentPathGuardianRid;
			checkStatus.checkStatus(gaurdianStatus);
		}

	}

}
