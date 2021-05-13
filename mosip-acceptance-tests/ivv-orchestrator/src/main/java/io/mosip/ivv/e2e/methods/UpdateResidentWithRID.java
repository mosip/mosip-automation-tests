package io.mosip.ivv.e2e.methods;

import org.apache.log4j.Logger;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;

public class UpdateResidentWithRID extends BaseTestCaseUtil implements StepInterface {
	Logger logger = Logger.getLogger(UpdateResidentWithRID.class);

	@Override
	public void run() throws RigInternalError {
		for (String rid : ridPersonaPath.keySet()) {
			packetUtility.updateResidentRid(ridPersonaPath.get(rid), rid);
		}
	}

}
