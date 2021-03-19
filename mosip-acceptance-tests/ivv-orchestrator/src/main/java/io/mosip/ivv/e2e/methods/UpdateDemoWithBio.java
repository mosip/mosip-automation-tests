package io.mosip.ivv.e2e.methods;

import java.util.Arrays;
import java.util.List;
import org.apache.log4j.Logger;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;

public class UpdateDemoWithBio extends BaseTestCaseUtil implements StepInterface {
	Logger logger = Logger.getLogger(UpdateDemoWithBio.class);

	@Override
	public void run() throws RigInternalError {
		String bioType = null;
		if (step.getParameters() == null || step.getParameters().isEmpty()) {
			logger.error("Parameter is  missing from DSL step");
			throw new RigInternalError("bioType paramter is  missing in step: " + step.getName());
		} else {
			bioType = step.getParameters().get(0);
		}
		String[] inputBioTypeReceived = bioType.split("@@");
		List<String> regenAttributeList=Arrays.asList(inputBioTypeReceived);
		for (String resDataPath : residentTemplatePaths.keySet()) {
			packetUtility.updateBiometric(resDataPath, regenAttributeList);
		}

	}
}
