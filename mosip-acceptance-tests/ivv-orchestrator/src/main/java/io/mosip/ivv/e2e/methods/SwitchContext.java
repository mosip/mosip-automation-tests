package io.mosip.ivv.e2e.methods;

import org.apache.log4j.Logger;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.service.BaseTestCase;

public class SwitchContext extends BaseTestCaseUtil implements StepInterface {
	Logger logger = Logger.getLogger(SwitchContext.class);

	@Override
	public void run() throws RigInternalError {
		String contextKeyValue = null;
		if (step.getParameters() == null || step.getParameters().isEmpty() || step.getParameters().size() < 1) {
			logger.warn("SwitchContext Arugemnt is  Missing : Please pass the argument from DSL sheet");
		} else {
			contextKeyValue = step.getParameters().get(0);
			if (step.getParameters().size() == 2) {
				String userAndMachineDetailParam = step.getParameters().get(1);
				packetUtility.createContexts(contextKeyValue, userAndMachineDetailParam, BaseTestCase.ApplnURI + "/");
				contextKey.put(contextKeyValue, "true");
			} else {
				if (!contextKey.containsKey(contextKeyValue))
					throw new RigInternalError(contextKeyValue + " is not present in the system");
				contextInuse.clear();

			}
			contextInuse.put("contextKey", contextKeyValue);
		}

	}

}
