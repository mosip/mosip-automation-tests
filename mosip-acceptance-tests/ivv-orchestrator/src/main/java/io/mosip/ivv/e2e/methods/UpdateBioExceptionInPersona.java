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
		List<String> exceptionArray = new ArrayList<String>();
		if (step.getParameters() == null || step.getParameters().isEmpty()) {
			logger.error("Parameter is  missing from DSL step");
			throw new RigInternalError("bioType paramter is  missing in step: " + step.getName());
		} 
		if (!step.getParameters().isEmpty()) {   // "var1=e2e_updateDemoOrBioDetails(0,0,0,$$personaPath)"
			String personaFilePath = step.getParameters().get(0);
			
			if (step.getParameters().size()<=2) {
				String [] str=step.getParameters().get(1).split("@@");
				for(String s: str)
				exceptionArray.add(s);
				// Finger:Left IndexFinger@@Finger:Right IndexFinger@@Iris:Left
			}

			
			if (personaFilePath.startsWith("$$")) {
				personaFilePath = step.getScenario().getVariables().get(personaFilePath);
				packetUtility.updateBioException(personaFilePath,exceptionArray,step);
			}
				}
	}
}
