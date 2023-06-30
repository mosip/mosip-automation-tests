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
	public String _additionalInfoReqId=null;
	@Override
	public void run() throws RigInternalError {
		if (step.getParameters().isEmpty()) {
			this.hasError=true;
			this.hasError=true;throw new RigInternalError(
					"Arugemnt is  missing pass the argument (NEW/LOST/UPDATE) from DSL scenario sheet");
		} else if(step.getParameters().size()==2 && !step.getParameters().get(1).startsWith("$$")) {
			process = step.getParameters().get(0);
			Properties personaIdValue = null;
			String packetPath = null;
			if (step.getParameters().size() == 2) {
				String personaId = step.getParameters().get(1);
				personaIdValue = PacketUtility.getParamsFromArg(personaId, "@@");
				for (String id : personaIdValue.stringPropertyNames()) {
					String value = personaIdValue.get(id).toString();
					if (step.getScenario().getResidentPersonaIdPro().get(value) == null)
						{
						this.hasError=true;
						throw new RigInternalError("Persona id : [" + value + "] is not present is the system");
						}
					String personaPath = step.getScenario().getResidentPersonaIdPro().get(value).toString();
					step.getScenario().getResidentTemplatePaths().put(personaPath, step.getScenario().getResidentTemplatePaths().get(personaPath));
				}
			}
			for (String resDataPath : step.getScenario().getResidentTemplatePaths().keySet()) {
				String templatePath = step.getScenario().getResidentTemplatePaths().get(resDataPath);
				String idJosn = templatePath + "/REGISTRATION_CLIENT/" + process + "/rid_id/" + "ID.json";
				packetPath = createPacket(idJosn, templatePath,null);  //    3rd argument is _additionalInfoReqId here pass null
				step.getScenario().getTemplatePacketPath().put(templatePath, packetPath);
				// this is inserted for storing rid with resident data it will be deleted in RIDSync
				step.getScenario().getRidPersonaPath().put(packetPath, resDataPath);
			}
		} else {
			process = step.getParameters().get(0); // "$$zipPacketPath=e2e_packetcreator(NEW,$$templatePath)"  --> now  "$$zipPacketPath=e2e_packetcreator(NEW,$$templatePath,$$additionalInfoReqId)" 
			String _templatePath = step.getParameters().get(1);

			
			if (step.getParameters().size() > 2) {
				_additionalInfoReqId = step.getParameters().get(2);
				if (!_additionalInfoReqId.isEmpty() && _additionalInfoReqId.startsWith("$$"))
					_additionalInfoReqId = step.getScenario().getVariables().get(_additionalInfoReqId);
			}
			
			if (_templatePath.startsWith("$$")) {
				_templatePath = step.getScenario().getVariables().get(_templatePath);
				String _idJosn = _templatePath + "/REGISTRATION_CLIENT/" + process + "/rid_id/" + "ID.json";
				String _packetPath = createPacket(_idJosn, _templatePath,_additionalInfoReqId);
				if (step.getOutVarName() != null)
					step.getScenario().getVariables().put(step.getOutVarName(), _packetPath);
			}
		}

	}

	private String createPacket(String idJsonPath, String templatePath,String additionalInfoReqId) throws RigInternalError {
		String url = baseUrl + props.getProperty("packetCretorUrl");
		JSONObject jsonReq = new JSONObject();
		jsonReq.put("idJsonPath", idJsonPath);
		jsonReq.put("process", process);
		jsonReq.put("source", E2EConstants.SOURCE);
		jsonReq.put("templatePath", templatePath);
		jsonReq.put("additionalInfoReqId", additionalInfoReqId);
		
		//Response response = postRequestWithPathParamAndBody(url, jsonReq.toString(), step.getScenario().getContextInuse(), "CreatePacket");
		Response response = postRequest(url, jsonReq.toString(), "CreatePacket",step);
		if (!response.getBody().asString().toLowerCase().contains("zip"))
			{
			this.hasError=true;
			throw new RigInternalError("Unable to get packet from packet utility");
			
			}
		return response.getBody().asString().replaceAll("\\\\", "\\\\\\\\");

	}

}
