package io.mosip.testrig.dslrig.ivv.e2e.methods;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import io.mosip.testrig.apirig.utils.ConfigManager;
import io.mosip.testrig.apirig.testrunner.MockSMTPListener;
import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;

public class GetAdditionalReqId extends BaseTestCaseUtil implements StepInterface { // $$additionalReqId=e2e_getAdditionalReqId(10)

	static Logger logger = Logger.getLogger(GetAdditionalReqId.class);
	
	static {
		if (ConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	@Override
	public void run() throws RigInternalError {

		String email = null;
		if (!step.getParameters().isEmpty() && step.getParameters().size() > 0)
			email = step.getParameters().get(0) + "@mosip.io";
		
			
	
		String additonalInfoRequestId = MockSMTPListener.getAdditionalReqId(email).trim();
	

			if (additonalInfoRequestId != null && !additonalInfoRequestId.isEmpty()
					&& !additonalInfoRequestId.equals("{Failed}")) {
            
			logger.info("AdditionalInfoReqId retrieved for emailID: " + email + ": " + additonalInfoRequestId);
			 additonalInfoRequestId = additonalInfoRequestId+ "-BIOMETRIC_CORRECTION-1";
			 
				if (step.getOutVarName() != null)
					step.getScenario().getVariables().put(step.getOutVarName(), additonalInfoRequestId);
				return;
			}

		this.hasError=true;
		throw new RigInternalError("Failed to retrieve the value for addtionalInfoRequestId from email");

	}

}
