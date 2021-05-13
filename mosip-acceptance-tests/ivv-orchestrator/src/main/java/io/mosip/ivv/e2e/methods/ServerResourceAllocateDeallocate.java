package io.mosip.ivv.e2e.methods;

import org.apache.log4j.Logger;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.e2e.constant.E2EConstants;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;

public class ServerResourceAllocateDeallocate extends BaseTestCaseUtil implements StepInterface {
	Logger logger = Logger.getLogger(ServerResourceAllocateDeallocate.class);

	@Override
	public void run() throws RigInternalError {
		String allocateOrDeallocate = null;
		if (step.getParameters() == null || step.getParameters().isEmpty() || step.getParameters().size() < 1) {
			logger.error("Parameter[allocate/deallocate] is  missing from DSL step");
			throw new RigInternalError("Parameter[allocate/deallocate] is  missing from DSL step");
		} else {
			allocateOrDeallocate = step.getParameters().get(0);
			switch (allocateOrDeallocate.toLowerCase()) {
			case E2EConstants.ALLOCATE:
				packetUtility.serverResourceStatusManager(E2EConstants.FREE, E2EConstants.INUSE);
				break;
			case E2EConstants.DE_ALLOCATE:
				packetUtility.serverResourceStatusManager(E2EConstants.INUSE, E2EConstants.FREE);
				break;
			default: {
				logger.error("Parameter: " + allocateOrDeallocate + " is not allowed");
				throw new RigInternalError("Parameter: " + allocateOrDeallocate + " is not allowed");
			}

			}
		}

	}

}
