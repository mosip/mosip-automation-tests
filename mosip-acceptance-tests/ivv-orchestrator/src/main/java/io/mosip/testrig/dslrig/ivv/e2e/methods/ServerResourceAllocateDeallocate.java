package io.mosip.testrig.dslrig.ivv.e2e.methods;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import io.mosip.testrig.apirig.utils.ConfigManager;
import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.e2e.constant.E2EConstants;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;

public class ServerResourceAllocateDeallocate extends BaseTestCaseUtil implements StepInterface {
	static Logger logger = Logger.getLogger(ServerResourceAllocateDeallocate.class);
	
	static {
		if (ConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	@Override
	public void run() throws RigInternalError {
		String allocateOrDeallocate = null;
		if (step.getParameters() == null || step.getParameters().isEmpty() || step.getParameters().size() < 1) {
			logger.error("Parameter[allocate/deallocate] is  missing from DSL step");
			this.hasError=true;
			throw new RigInternalError("Parameter[allocate/deallocate] is  missing from DSL step");
		} else {
			allocateOrDeallocate = step.getParameters().get(0);
			switch (allocateOrDeallocate.toLowerCase()) {
			case E2EConstants.ALLOCATE:
				packetUtility.serverResourceStatusManager(E2EConstants.FREE, E2EConstants.INUSE,step);
				break;
			case E2EConstants.DE_ALLOCATE:
				packetUtility.serverResourceStatusManager(E2EConstants.INUSE, E2EConstants.FREE,step);
				break;
			default: {
				logger.error("Parameter: " + allocateOrDeallocate + " is not allowed");
				this.hasError=true;
				throw new RigInternalError("Parameter: " + allocateOrDeallocate + " is not allowed");
			}

			}
		}

	}

}
