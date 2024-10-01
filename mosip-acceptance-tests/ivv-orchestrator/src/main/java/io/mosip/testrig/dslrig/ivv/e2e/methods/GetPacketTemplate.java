package io.mosip.testrig.dslrig.ivv.e2e.methods;

import java.util.HashSet;
import java.util.Properties;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testrig.dslrig.ivv.orchestrator.PacketUtility;
import io.mosip.testrig.dslrig.ivv.orchestrator.dslConfigManager;

@Scope("prototype")
@Component
public class GetPacketTemplate extends BaseTestCaseUtil implements StepInterface {
	static Logger logger = Logger.getLogger(GetPacketTemplate.class);

	static {
		if (dslConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	@Override
	public void run() throws RigInternalError {
		String qualityScore = "81";
		boolean genarateValidCbeff = true;
		String process = null;
		String personaPath = null;
		Properties personaIdValue = null;
		if (step.getParameters().isEmpty() && !step.getScenario().getGeneratedResidentData().isEmpty()) {
			JSONArray jsonArray = packetUtility.getTemplate(
					new HashSet<String>(step.getScenario().getGeneratedResidentData()), "NEW",
					step.getScenario().getCurrentStep(), step, qualityScore, genarateValidCbeff);
			JSONObject obj = jsonArray.getJSONObject(0);
			step.getScenario().setTemplatPath_updateResident(obj.get("path").toString());
		} else {
			process = step.getParameters().get(0);
			if (step.getParameters().size() > 1) {
				String personaId = step.getParameters().get(1);
				if (step.getParameters().size() == 3) {
					qualityScore = step.getParameters().get(2);
				}
				if (step.getParameters().size() == 4) {
					genarateValidCbeff = Boolean.parseBoolean(step.getParameters().get(3));
				}
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

			JSONArray resp = packetUtility.getTemplate(step.getScenario().getResidentTemplatePaths().keySet(), process,
					step.getScenario().getCurrentStep(), step, qualityScore, genarateValidCbeff);

			for (int i = 0; i < resp.length(); i++) {
				JSONObject obj = resp.getJSONObject(i);
				String id = obj.get("id").toString();
				String tempFilePath = obj.get("path").toString();
				if (step.getOutVarName() != null)
					step.getScenario().getVariables().put(step.getOutVarName(), tempFilePath);
				for (String residentPath : step.getScenario().getResidentTemplatePaths().keySet()) {
					if (residentPath.contains(id)) {
						step.getScenario().getResidentTemplatePaths().put(residentPath, tempFilePath);
						break;
					}
				}

			}
			for (String residentPath : step.getScenario().getResidentTemplatePaths().keySet()) {
				if (step.getScenario().getResidentTemplatePaths().get(residentPath) == null) {
					this.hasError = true;
					throw new RigInternalError("Unable to get packetTemplate from packet utility");
				}
			}
		}

	}

}
