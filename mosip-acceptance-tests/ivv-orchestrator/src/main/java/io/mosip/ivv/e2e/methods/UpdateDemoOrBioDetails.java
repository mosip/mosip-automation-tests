 package io.mosip.ivv.e2e.methods;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;

public class UpdateDemoOrBioDetails extends BaseTestCaseUtil implements StepInterface {
	Logger logger = Logger.getLogger(UpdateDemoOrBioDetails.class);

	@Override
	public void run() throws RigInternalError {
		String bioType = null;
		String missFields = null;
		String updateAttribute = null;
		if (step.getParameters() == null || step.getParameters().isEmpty()) {
			logger.error("Parameter is  missing from DSL step");
			this.hasError=true;
			throw new RigInternalError("bioType paramter is  missing in step: " + step.getName());
		} else {
			bioType = step.getParameters().get(0);
			if(step.getParameters().size()>1)
			missFields=step.getParameters().get(1);
			if(step.getParameters().size()>2)
			updateAttribute=step.getParameters().get(2);
		}
		List<String> regenAttributeList=(bioType!=null)?Arrays.asList(bioType.split("@@")):null;
		List<String> missFieldsAttributeList=(missFields!=null)?Arrays.asList(missFields.split("@@")):null;
		List<String> updateAttributeList=(updateAttribute!=null)?Arrays.asList(updateAttribute.split("@@")):null;
		
		if (!step.getParameters().isEmpty() && step.getParameters().size() > 3) {   // "var1=e2e_updateDemoOrBioDetails(0,0,0,$$personaPath)"
			String personaFilePath = step.getParameters().get(3);
			if (personaFilePath.startsWith("$$")) {
				personaFilePath = step.getScenario().getVariables().get(personaFilePath);
				packetUtility.updateDemoOrBioDetail(personaFilePath,
						(regenAttributeList.get(0).equalsIgnoreCase("0")) ? null : regenAttributeList,
						(missFieldsAttributeList.get(0).equalsIgnoreCase("0")) ? new ArrayList<>(): missFieldsAttributeList,
						(updateAttributeList.get(0).equalsIgnoreCase("0")) ? new ArrayList<>() : updateAttributeList,step);
			}
		} else {
			for (String resDataPath : step.getScenario().getResidentTemplatePaths().keySet()) {
				packetUtility.updateDemoOrBioDetail(resDataPath,
						(regenAttributeList.get(0).equalsIgnoreCase("0")) ? null : regenAttributeList,
						(missFieldsAttributeList.get(0).equalsIgnoreCase("0")) ? new ArrayList<>()
								: missFieldsAttributeList,
						updateAttributeList,step);

			}
		}
	}
}
