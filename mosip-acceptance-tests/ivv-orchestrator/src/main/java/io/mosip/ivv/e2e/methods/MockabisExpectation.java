package io.mosip.ivv.e2e.methods;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;

public class MockabisExpectation extends BaseTestCaseUtil implements StepInterface {
	Logger logger = Logger.getLogger(MockabisExpectation.class);

	@Override
	public void run() throws RigInternalError {
		JSONArray filePathArray = new JSONArray();
		boolean duplicate = false;
		if (step.getParameters().size() == 1)
			duplicate = Boolean.parseBoolean(step.getParameters().get(0));
		for (String resDataPath : residentTemplatePaths.keySet())
			filePathArray.put(resDataPath);
		if (filePathArray.length() > 0)
			packetUtility.setMockabisExpectaion(duplicate, filePathArray, contextInuse);
		else {
			logger.error("FilePath cannot be empty");
			throw new RigInternalError("FilePath cannot be empty");
		}
	}

}
