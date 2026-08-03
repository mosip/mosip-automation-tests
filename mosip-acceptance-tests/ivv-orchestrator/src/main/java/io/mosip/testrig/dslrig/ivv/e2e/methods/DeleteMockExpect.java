package io.mosip.testrig.dslrig.ivv.e2e.methods;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testrig.dslrig.ivv.orchestrator.dslConfigManager;

public class DeleteMockExpect extends BaseTestCaseUtil implements StepInterface {
	public static Logger logger = Logger.getLogger(DeleteMockExpect.class);

	static {
		if (dslConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	@Override
	public void run() throws RigInternalError {
		String modalityHashValue = null;
		if (!step.getParameters().isEmpty() && step.getParameters().get(0).startsWith("$$")) {
			modalityHashValue = step.getScenario().getVariables().get(step.getParameters().get(0));
		}

		if (String.valueOf(modalityHashValue).equals("null") || String.valueOf(modalityHashValue).isEmpty()) {
			String url = baseUrl + props.getProperty("deleteMockExpectation");
			deleteRequest(url, "deleteMockExpectation", step);
		} else {
			String[] hashValues = modalityHashValue.split(",");
			for (String hashValue : hashValues) {
				hashValue = hashValue.replaceAll("[A-Za-z ]+=", "").replace("{", "").replace("}", "");
				String url = baseUrl + props.getProperty("deleteMockExpectation") + "/" + hashValue;
				deleteRequest(url, "deleteMockExpectation for hash value: " + hashValue, step);
			}
		}
	}
}
