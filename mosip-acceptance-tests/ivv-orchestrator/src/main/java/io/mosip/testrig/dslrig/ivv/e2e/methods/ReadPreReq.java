package io.mosip.testrig.dslrig.ivv.e2e.methods;

import java.util.HashMap;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.testng.Reporter;
import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testrig.dslrig.ivv.orchestrator.dslConfigManager;

public class ReadPreReq extends BaseTestCaseUtil implements StepInterface {
	static Logger logger = Logger.getLogger(ReadPreReq.class);

	static {
		if (dslConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	@Override
	public void run() throws RigInternalError {

		String appendedkey = null;
		if (step.getParameters() == null || step.getParameters().isEmpty()) {
			logger.warn("PreRequisite Arugemnt is  Missing : Please pass the argument from DSL sheet");
			throw new RigInternalError("PreRequisite index is missing — pass PRE_REQUISITE_DATA_INDEX (e.g. 2)");
		}
		appendedkey = step.getParameters().get(0);
		if (appendedkey == null || appendedkey.isBlank()) {
			throw new RigInternalError("PreRequisite index is blank");
		}

		String path;
		try {
			path = prereqStoragePath(appendedkey);
			logger.info("ReadPreReq index=" + appendedkey + " path=" + path);
			HashMap<String, String> prereq;
			synchronized (prereqDataSet) {
				prereq = prereqDataSet.get(path);
			}
			if (prereq == null) {
				String msg = "PreRequisite Data is not set properly — missing entry for " + path
						+ ". Index " + appendedkey
						+ " is written in Scenario 0 (WritePreReq($$details2,2)) after track-2 machine/zone setup."
						+ " If Scenario 0 failed or track2b (steps 17-25) did not complete, this read will fail."
						+ " Stored prereq keys: " + prereqDataSummary();
				logger.error(msg);
				Reporter.log("<span style='color:red;'>" + msg + "</span>");
				throw new RigInternalError(msg);
			}
			if (step.getOutVarName() != null) {
				step.getScenario().getVariables().putAll(prereq);
			}
			Reporter.log("Loaded the prereq data for executing the scenario (index " + appendedkey + ")<br>");

		} catch (RigInternalError e) {
			this.hasError = true;
			throw e;
		} catch (Exception e) {
			this.hasError = true;
			logger.error("ReadPreReq failed for index " + appendedkey, e);
			throw new RigInternalError("PreRequisite Data is not set properly — " + e.getMessage());
		}
	}

}
