package io.mosip.testrig.dslrig.ivv.e2e.methods;

import static org.testng.Assert.assertTrue;

import java.util.Properties;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testrig.dslrig.ivv.orchestrator.PacketUtility;
import io.mosip.testrig.dslrig.ivv.orchestrator.dslConfigManager;

public class CreateAndUploadExternalPacket extends BaseTestCaseUtil implements StepInterface {
	static Logger logger = Logger.getLogger(CreateAndUploadExternalPacket.class);

	static {
		if (dslConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	@Override
	public void run() throws RigInternalError {
		String process = null;
		String personaPath = null;
		String source = null;
		Properties personaIdValue = null;
		if (step.getParameters().isEmpty() && !step.getScenario().getGeneratedResidentData().isEmpty()) {
			logger.error("Parameter is  missing from DSL step");
			assertTrue(false, "process paramter is  missing in step: " + step.getName());
		} else {
			source = step.getParameters().get(0);
			if (step.getParameters().size() > 1) {
				process = step.getParameters().get(1);
			}
			if (step.getParameters().size() > 2) {
				String personaId = step.getParameters().get(2);
				if (personaId.startsWith("$$")) {
					personaPath = step.getScenario().getVariables().get(personaId);
					step.getScenario().getResidentTemplatePaths().clear();
				} else {
					personaIdValue = PacketUtility.getParamsFromArg(personaId, "@@");
					for (String id : personaIdValue.stringPropertyNames()) {
						String value = personaIdValue.get(id).toString();
						if (step.getScenario().getResidentPersonaIdPro().get(value) == null) {
							this.hasError = true;
							throw new RigInternalError("Persona id : [" + value + "] is not present is the system");
						}
						personaPath = step.getScenario().getResidentPersonaIdPro().get(value).toString();
					}

				}
				step.getScenario().getResidentTemplatePaths().put(personaPath, null);
			}

			String rid = packetUtility.createUploadPacket(step.getScenario().getResidentTemplatePaths().keySet(),
					source, process, step.getScenario().getCurrentStep(), step);
			if (rid == null || rid.isEmpty()) {
				throw new RuntimeException("Unable to upload CRVS packet: RID is null or empty");
			}
			if (step.getOutVarName() != null)
				step.getScenario().getVariables().put(step.getOutVarName(), rid);

		}

	}

}
