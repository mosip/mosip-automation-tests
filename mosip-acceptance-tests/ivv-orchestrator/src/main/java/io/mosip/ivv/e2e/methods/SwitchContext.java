package io.mosip.ivv.e2e.methods;

import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.ivv.orchestrator.PacketUtility;
import io.mosip.testrig.apirig.service.BaseTestCase;

public class SwitchContext extends BaseTestCaseUtil implements StepInterface {
	Logger logger = Logger.getLogger(SwitchContext.class);

	@Override
	public void run() throws RigInternalError {
		String contextKeyValue = null;
		String mosipVersion = null;
		boolean generatePrivateKey =Boolean.FALSE;
		HashMap<String, String> map = new HashMap<String, String>();
		
		if (step.getParameters() == null || step.getParameters().isEmpty() || step.getParameters().size() < 1) {
			logger.warn("SwitchContext Arugemnt is  Missing : Please pass the argument from DSL sheet");
		} else {
			contextKeyValue = step.getParameters().get(0);
			if (step.getParameters().size() >1) {
				String userAndMachineDetailParam = step.getParameters().get(1);
				if (userAndMachineDetailParam.startsWith("$$")) {
					map = step.getScenario().getVariables();
				}
				if (step.getParameters().size() > 2) {   // 1@@2(mosip.version)
					List<String> version = PacketUtility.getParamsArg(step.getParameters().get(2), "@@");
					if (!(version.contains("-1")))
						mosipVersion = version.get(0) + "." + version.get(1);
				}
				if (step.getParameters().size() > 3)  // true/false  (want to generate privatekey)
					generatePrivateKey = Boolean.parseBoolean(step.getParameters().get(3));
				if (map != null)
					packetUtility.createContexts("",contextKeyValue, map, mosipVersion,generatePrivateKey,null,BaseTestCase.ApplnURI + "/",step);
					
				else if (userAndMachineDetailParam != null)
				packetUtility.createContexts(contextKeyValue, userAndMachineDetailParam, mosipVersion,generatePrivateKey,null,BaseTestCase.ApplnURI + "/",step);
				
				
				
				step.getScenario().getCurrentStep().put(contextKeyValue, "true");
			} else {
				if (!step.getScenario().getCurrentStep().containsKey(contextKeyValue)) {
					this.hasError=true;
					throw new RigInternalError(contextKeyValue + " is not present in the system");}
				step.getScenario().getCurrentStep().clear();

			}
			step.getScenario().getCurrentStep().put("contextKey", contextKeyValue);
		}

	}

}
