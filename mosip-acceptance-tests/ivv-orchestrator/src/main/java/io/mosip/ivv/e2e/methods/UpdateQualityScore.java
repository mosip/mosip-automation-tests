package io.mosip.ivv.e2e.methods;

import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;

public class UpdateQualityScore extends BaseTestCaseUtil implements StepInterface { // $$var=e2e_updateQualityScore(15)

	@Override
	public void run() throws RigInternalError {
		int qualityScore = 0;   // taking from dsl argument
		if (!step.getParameters().isEmpty() && step.getParameters().size() == 1)
			qualityScore = Integer.parseInt(step.getParameters().get(0));
		else
			throw new RigInternalError("QualityScore is missing");
		String url = baseUrl + props.getProperty("setThresholdValue") + qualityScore;
		getRequest(url, "Update qualityScore : " + qualityScore);

	}

}
