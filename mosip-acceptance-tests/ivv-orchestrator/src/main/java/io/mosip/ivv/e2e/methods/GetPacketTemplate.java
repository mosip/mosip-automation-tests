package io.mosip.ivv.e2e.methods;

import java.util.HashSet;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.ivv.orchestrator.PacketUtility;

public class GetPacketTemplate extends BaseTestCaseUtil implements StepInterface {
	Logger logger = Logger.getLogger(GetPacketTemplate.class);

	@Override
	public void run() throws RigInternalError {
		String process = null;
		Properties personaIdValue = null;
		if (step.getParameters().isEmpty() && !generatedResidentData.isEmpty()) {  //  used to child packet processing
			JSONArray jsonArray = packetUtility.getTemplate(new HashSet<String>(generatedResidentData), "NEW",
					contextInuse);
			JSONObject obj = jsonArray.getJSONObject(0);
			templatPath_updateResident = obj.get("path").toString();
		} else {
			process = step.getParameters().get(0);
			if (step.getParameters().size() > 1) {
				String personaId = step.getParameters().get(1);
				personaIdValue = PacketUtility.getParamsFromArg(personaId, "@@");
				for (String id : personaIdValue.stringPropertyNames()) {
					String value = personaIdValue.get(id).toString();
					if (residentPersonaIdPro.get(value) == null)
						throw new RigInternalError("Persona id : [" + value + "] is not present is the system");
					String personaPath = residentPersonaIdPro.get(value).toString();
					residentTemplatePaths.put(personaPath, null);
				}
			}

			JSONArray resp = packetUtility.getTemplate(residentTemplatePaths.keySet(), process, contextInuse);

			for (int i = 0; i < resp.length(); i++) {
				JSONObject obj = resp.getJSONObject(i);
				String id = obj.get("id").toString();
				String tempFilePath = obj.get("path").toString();
				for (String residentPath : residentTemplatePaths.keySet()) {
					if (residentPath.contains(id)) {
						residentTemplatePaths.put(residentPath, tempFilePath);
						break;
					}
				}

			}
			for (String residentPath : residentTemplatePaths.keySet()) {
				if (residentTemplatePaths.get(residentPath) == null)
					throw new RigInternalError("Unable to get packetTemplate from packet utility");
			}
		}

	}

}
