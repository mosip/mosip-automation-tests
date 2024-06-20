package io.mosip.testrig.dslrig.ivv.e2e.methods;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import io.mosip.testrig.apirig.kernel.util.ConfigManager;
import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;

public class LoopWindow extends BaseTestCaseUtil implements StepInterface{
	
	 private static final Logger logger = Logger.getLogger(LoopWindow.class);
	  
		static {
			if (ConfigManager.IsDebugEnabled())
				logger.setLevel(Level.ALL);
			else
				logger.setLevel(Level.ERROR);
		}

	@Override
	public void run() throws RigInternalError {
		logger.info("Loop window ");
	}
	
}
