package io.mosip.testrig.dslrig.ivv.e2e.methods;


import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testrig.dslrig.ivv.orchestrator.dslConfigManager;

@Scope("prototype")
@Component
public class Skip extends BaseTestCaseUtil implements StepInterface {
	  private static final Logger logger = Logger.getLogger(Skip.class);
	  
		static {
			if (dslConfigManager.IsDebugEnabled())
				logger.setLevel(Level.ALL);
			else
				logger.setLevel(Level.ERROR);
		}

	@Override
	public void run() throws RigInternalError {
		logger.info("Skiping Method");
	}
	
}