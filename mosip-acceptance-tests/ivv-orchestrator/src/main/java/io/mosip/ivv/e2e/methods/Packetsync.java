package io.mosip.ivv.e2e.methods;

import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;

public class Packetsync extends BaseTestCaseUtil implements StepInterface {

	@Override
	public void run() throws RigInternalError {
		if(!step.getParameters().isEmpty() && step.getParameters().size()==1) { //"$$var=e2e_packetsync($$zipPacketPath)"
			String _zipPacketPath=step.getParameters().get(0);
			if(_zipPacketPath.startsWith("$$")){
				_zipPacketPath=step.getScenario().getVariables().get(_zipPacketPath);
				packetUtility.packetSync(_zipPacketPath, contextInuse);
			}
		}else {
			for (String packetPath : templatePacketPath.values())
				packetUtility.packetSync(packetPath, contextInuse);
		}
		
	}

}
