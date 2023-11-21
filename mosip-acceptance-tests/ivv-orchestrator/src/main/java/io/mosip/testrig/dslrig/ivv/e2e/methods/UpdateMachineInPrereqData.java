package io.mosip.testrig.dslrig.ivv.e2e.methods;

import java.util.HashMap;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import io.mosip.testrig.apirig.kernel.util.ConfigManager;
import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.e2e.constant.E2EConstants;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;

public class UpdateMachineInPrereqData extends BaseTestCaseUtil implements StepInterface {

	static Logger logger = Logger.getLogger(UpdateMachineInPrereqData.class);

	static {
		if (ConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	@Override
	public void run() throws RigInternalError {

		HashMap<String, String> map = new HashMap<String, String>();
		String preReqData = "";

		if (step.getParameters().size() == 1) {
			preReqData = step.getParameters().get(0);
			if (preReqData.startsWith("$$")) {
				map = step.getScenario().getVariables();
			}
			map.put("machineid", (map.get("machineid") != null) ? "22222" : E2EConstants.MACHINE_ID);
		}
	}
}