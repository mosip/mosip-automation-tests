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

public class SetContext extends BaseTestCaseUtil implements StepInterface {
	static Logger logger = Logger.getLogger(SetContext.class);

	static {
		if (ConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	@Override
	public void run() throws RigInternalError {
		constantIntializer();
		String contextKeyValue = "dev_context";

		String userAndMachineDetailParam = null;
		String mosipVersion = null;
		boolean generatePrivateKey = Boolean.FALSE;
		String status = null;
		String negative = "valid";
		boolean invalidCertFlag = Boolean.FALSE;
		String consent = "";
		boolean changeSupervisorNameToDiffCase = Boolean.FALSE;
		String invalidEncryptedHashFlag = "";
		String invalidCheckSum = "";
		// neeha scenario = step.getScenario().getId() + ":" +
		// step.getScenario().getDescription();
		HashMap<String, String> map = new HashMap<String, String>();
		HashMap<String, String> dummyholder = new HashMap<String, String>();
		if (step.getParameters() == null || step.getParameters().isEmpty() || step.getParameters().size() < 1) {
			logger.warn("SetContext Arugemnt is  Missing : Please pass the argument from DSL sheet");
		} else {
			contextKeyValue = step.getParameters().get(0);
			contextKeyValue = System.getProperty("env.user") + "_context";
			// step.getScenario().getContextInuse()put("contextKey",contextKeyValue );
			step.getScenario().getCurrentStep().put(contextKeyValue, "true");
			step.getScenario().getCurrentStep().clear();
			step.getScenario().getCurrentStep().put("contextKey", contextKeyValue);
			if (step.getParameters().size() > 1) { // machineid=112121@@.......
				String value = step.getParameters().get(1);
				if (!(value.equalsIgnoreCase("-1")) && value.contains("@@"))
					userAndMachineDetailParam = value;
				else if (value.startsWith("$$")) {

					map = step.getScenario().getVariables();
				}
			}
			if (step.getParameters().size() > 2) { // 1@@2(mosip.version)
				List<String> version = PacketUtility.getParamsArg(step.getParameters().get(2), "@@");
				if (!(version.contains("-1")))
					mosipVersion = version.get(0) + "." + version.get(1);
			}
			if (step.getParameters().size() > 3) // true/false (want to generate privatekey)
				generatePrivateKey = Boolean.parseBoolean(step.getParameters().get(3));

			if (step.getParameters().size() > 4) // deactivate
				status = step.getParameters().get(4);
			if (step.getParameters().size() > 5) // for negative operator and supervisor
				negative = step.getParameters().get(5);

			if (step.getParameters().size() == 5 && step.getParameters().get(4).contains("true"))
				invalidCertFlag = Boolean.parseBoolean(step.getParameters().get(4));

			// consent value either "yes" or "no"
			if (step.getParameters().size() == 6
					&& (step.getParameters().get(5).contains("yes") || step.getParameters().get(5).contains("no")))
				consent = step.getParameters().get(5);

			// supervisorIDFlag
			if (step.getParameters().size() > 6 && step.getParameters().get(6).contains("true"))
				changeSupervisorNameToDiffCase = Boolean.parseBoolean(step.getParameters().get(6));

			// encryptedHashFlag
			if (step.getParameters().size() > 7 && step.getParameters().get(7).contains("invalidEncryptedHash"))
				invalidEncryptedHashFlag = step.getParameters().get(7);
			// checksumFlag
			if (step.getParameters().size() == 9 && step.getParameters().get(8).contains("invalidCheckSum"))
				invalidCheckSum = step.getParameters().get(8);

		}

		if (userAndMachineDetailParam != null)
			packetUtility.createContexts(contextKeyValue, userAndMachineDetailParam, mosipVersion, generatePrivateKey,
					status, BaseTestCase.ApplnURI + "/", step);
		else if (map != null)
			packetUtility.createContexts(negative, contextKeyValue, map, mosipVersion, generatePrivateKey, status,
					BaseTestCase.ApplnURI + "/", step, invalidCertFlag, consent, changeSupervisorNameToDiffCase,
					invalidEncryptedHashFlag, invalidCheckSum);
	}
}
