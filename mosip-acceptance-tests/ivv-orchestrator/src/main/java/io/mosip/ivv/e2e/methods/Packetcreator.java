package io.mosip.ivv.e2e.methods;

import java.util.Properties;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.e2e.constant.E2EConstants;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.ivv.orchestrator.PacketUtility;
import io.restassured.response.Response;

public class Packetcreator extends BaseTestCaseUtil implements StepInterface {
	// Packetcreator take two argument (NEW/LOST/UPDATE,id1=23232@@id2=<personaId>)
	Logger logger = Logger.getLogger(Packetcreator.class);
	String process = null;

	@Override
	public void run() throws RigInternalError {
		if (step.getParameters() == null || step.getParameters().isEmpty() || step.getParameters().size() < 1) {
			logger.warn("Arugemnt is  missing pass the argument (NEW/LOST/UPDATE) from DSL scenario sheet");
			throw new RigInternalError(
					"Arugemnt is  missing pass the argument (NEW/LOST/UPDATE) from DSL scenario sheet");
		} else {
			process = step.getParameters().get(0);
		}
		Properties personaIdValue = null;
		if (step.getParameters().size() == 2) {
			String personaId = step.getParameters().get(1);
			personaIdValue = PacketUtility.getParamsFromArg(personaId, "@@");
			for (String id : personaIdValue.stringPropertyNames()) {
				String value = personaIdValue.get(id).toString();
				if (residentPersonaIdPro.get(value) == null)
					throw new RigInternalError("Persona id : [" + value + "] is not present is the system");
				String personaPath = residentPersonaIdPro.get(value).toString();
				residentTemplatePaths.put(personaPath, residentTemplatePaths.get(personaPath));
			}
		}
		String packetPath = null;
		for (String resDataPath : residentTemplatePaths.keySet()) {
			String templatePath = residentTemplatePaths.get(resDataPath);
			String idJosn = templatePath + "/REGISTRATION_CLIENT/" + process + "/rid_id/" + "ID.json";
			packetPath = createPacket(idJosn, templatePath);
			templatePacketPath.put(templatePath, packetPath);
			// this is inserted for storing rid with resident data it will be deleted in RIDSync
			ridPersonaPath.put(packetPath, resDataPath);
		}
	}

	private String createPacket(String idJsonPath, String templatePath) throws RigInternalError {
		String url = baseUrl + props.getProperty("packetCretorUrl");
		JSONObject jsonReq = new JSONObject();
		jsonReq.put("idJsonPath", idJsonPath);
		jsonReq.put("process", process);
		jsonReq.put("source", E2EConstants.SOURCE);
		jsonReq.put("templatePath", templatePath);
		Response response = postRequestWithPathParamAndBody(url, jsonReq.toString(), contextInuse, "CreatePacket");
		if (!response.getBody().asString().toLowerCase().contains("zip"))
			throw new RigInternalError("Unable to get packet from packet utility");
		return response.getBody().asString().replaceAll("\\\\", "\\\\\\\\");

	}

}
