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
		String missFields = null;
		if (step.getParameters() == null || step.getParameters().isEmpty()) {
			logger.error("Parameter is  missing from DSL step");
			throw new RigInternalError("bioType paramter is  missing in step: " + step.getName());
		} else {
			bioType = step.getParameters().get(0);
			if(step.getParameters().size()>1)
			missFields=step.getParameters().get(1);
		}
		List<String> regenAttributeList=(bioType!=null)?Arrays.asList(bioType.split("@@")):null;
		List<String> missFieldsAttributeList=(missFields!=null)?Arrays.asList(missFields.split("@@")):null;
		for (String resDataPath : residentTemplatePaths.keySet()) {
			//packetUtility.updateBiometric(resDataPath, regenAttributeList,missFieldsAttributeList);
			packetUtility.updateBiometric(resDataPath, (regenAttributeList.get(0).equalsIgnoreCase("0"))?null:regenAttributeList,missFieldsAttributeList);
		}

	}
}
