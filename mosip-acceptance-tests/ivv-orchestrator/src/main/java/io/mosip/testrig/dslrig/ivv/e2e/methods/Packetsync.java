package io.mosip.testrig.dslrig.ivv.e2e.methods;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import io.mosip.testrig.apirig.utils.ConfigManager;
import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;

public class Packetsync extends BaseTestCaseUtil implements StepInterface {
	public static Logger logger = Logger.getLogger(Packetsync.class);
	
	static {
		if (ConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	@Override
	public void run() throws RigInternalError {
		boolean expectedToPass = true;
		if(!step.getParameters().isEmpty() && step.getParameters().size()>=1) { //"$$var=e2e_packetsync($$zipPacketPath)"
			String _zipPacketPath=step.getParameters().get(0);
			if ( step.getParameters().size()==2) {
				expectedToPass = Boolean.parseBoolean(step.getParameters().get(1));
			}
			if(_zipPacketPath.startsWith("$$")){
				_zipPacketPath=step.getScenario().getVariables().get(_zipPacketPath);
				packetUtility.packetSync(_zipPacketPath, step.getScenario().getCurrentStep(),step,expectedToPass);
			}
		}
		else {
			for (String packetPath : step.getScenario().getTemplatePacketPath().values())
				packetUtility.packetSync(packetPath, step.getScenario().getCurrentStep(),step,expectedToPass);
		}
		
	}

}
