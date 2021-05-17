package io.mosip.ivv.e2e.methods;

import org.apache.commons.lang.StringUtils;

import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;

public class GenerateAndUploadPacket extends BaseTestCaseUtil implements StepInterface {

	@Override
	public void run() throws RigInternalError {
		String responseStatus = "success";
		if (step.getParameters() != null && !step.getParameters().isEmpty() && step.getParameters().size() == 1) { // failed
			responseStatus = step.getParameters().get(0);
			if (StringUtils.isBlank(responseStatus) || !responseStatus.equalsIgnoreCase("failed"))
				throw new RigInternalError("Parameter : [" + responseStatus + "] is not allowed");
		}
		for (String resDataPath : residentTemplatePaths.keySet()) {
			String rid = packetUtility.generateAndUploadPacket(residentPathsPrid.get(resDataPath),
					residentTemplatePaths.get(resDataPath), contextInuse,responseStatus);
			if(rid!=null) {
				pridsAndRids.put(residentPathsPrid.get(resDataPath), rid);
				ridPersonaPath.put(rid, resDataPath);	
			}
			
		}

	}

}
