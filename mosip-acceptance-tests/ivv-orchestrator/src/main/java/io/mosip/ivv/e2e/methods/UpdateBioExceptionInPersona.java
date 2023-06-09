 package io.mosip.ivv.e2e.methods;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.log4j.Logger;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;

public class UpdateBioExceptionInPersona extends BaseTestCaseUtil implements StepInterface {
	Logger logger = Logger.getLogger(UpdateBioExceptionInPersona.class);

	@Override
	public void run() throws RigInternalError {
		String[] subStatusCode = new String[2];  
		if (step.getParameters() == null || step.getParameters().isEmpty()) {
			logger.error("Parameter is  missing from DSL step");
			throw new RigInternalError("bioType paramter is  missing in step: " + step.getName());
		} 
		if (!step.getParameters().isEmpty()) {   // "var1=e2e_updateDemoOrBioDetails(0,0,0,$$personaPath)"
			String personaFilePath = step.getParameters().get(0);
			
			if (step.getParameters().size()<=2) {
				subStatusCode = step.getParameters().get(1).split("@@");
			}

			
			if (personaFilePath.startsWith("$$")) {
				personaFilePath = step.getScenario().getVariables().get(personaFilePath);
				packetUtility.updateBioException(personaFilePath,subStatusCode,step);
			}
				}
	}
}
