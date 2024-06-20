package io.mosip.testrig.dslrig.ivv.e2e.methods;

import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import io.mosip.testrig.apirig.kernel.util.ConfigManager;
import io.mosip.testrig.apirig.service.BaseTestCase;
import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testrig.dslrig.ivv.orchestrator.PacketUtility;

public class SwitchContext extends BaseTestCaseUtil implements StepInterface {
	static Logger logger = Logger.getLogger(SwitchContext.class);
	
	static {
		if (ConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	@Override
	public void run() throws RigInternalError {
		String contextKeyValue = null;
		boolean generatePrivateKey =Boolean.FALSE;
		boolean invalidCertFlag =Boolean.FALSE;
		HashMap<String, String> map = new HashMap<String, String>();
		String consent="";
		boolean supervisorFlag=Boolean.FALSE;
		String invalidEncryptedHashFlag = "";
		String invalidCheckSum = "";	
		String invalidIdSchemaFlag = "";
		String skipBiometricClassificationFlag = "";
		String skipApplicantDocumentsFlag = "";
		if (step.getParameters() == null || step.getParameters().isEmpty() || step.getParameters().size() < 1) {
			logger.warn("SwitchContext Arugemnt is  Missing : Please pass the argument from DSL sheet");
		} else {
			contextKeyValue = step.getParameters().get(0);
			if (step.getParameters().size() >1) {
				String userAndMachineDetailParam = step.getParameters().get(1);
				if (userAndMachineDetailParam.startsWith("$$")) {
					map = step.getScenario().getVariables();
				}

				if (step.getParameters().size() > 2)  // true/false  (want to generate privatekey)
					generatePrivateKey = Boolean.parseBoolean(step.getParameters().get(2));
				if (map != null)
					packetUtility.createContexts("",contextKeyValue, map, generatePrivateKey,null,BaseTestCase.ApplnURI + "/",step,invalidCertFlag,consent,supervisorFlag,invalidEncryptedHashFlag,invalidCheckSum,invalidIdSchemaFlag,skipBiometricClassificationFlag,skipApplicantDocumentsFlag);
					
				else if (userAndMachineDetailParam != null)
				packetUtility.createContexts(contextKeyValue, userAndMachineDetailParam, generatePrivateKey,null,BaseTestCase.ApplnURI + "/",step);
				
				
				
				step.getScenario().getCurrentStep().put(contextKeyValue, "true");
			} else {
				if (!step.getScenario().getCurrentStep().containsKey(contextKeyValue)) {
					this.hasError=true;
					throw new RigInternalError(contextKeyValue + " is not present in the system");}
				step.getScenario().getCurrentStep().clear();

			}
			step.getScenario().getCurrentStep().put("contextKey", contextKeyValue);
		}

	}

}
