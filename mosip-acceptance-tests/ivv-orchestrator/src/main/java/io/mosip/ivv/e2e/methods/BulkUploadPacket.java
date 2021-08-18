package io.mosip.ivv.e2e.methods;

import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.ivv.orchestrator.PacketUtility;
import io.restassured.response.Response;

public class BulkUploadPacket extends BaseTestCaseUtil implements StepInterface {
	Logger logger = Logger.getLogger(BulkUploadPacket.class);

	@Override
	public void run() throws RigInternalError {
		JSONArray packetPathArray = new JSONArray();
		Properties personaIdValue = null;
		if (!step.getParameters().isEmpty() && step.getParameters().size() == 1) { /// id1=878787877@@id2=8878787989
			String personaId = step.getParameters().get(0);
			personaIdValue = PacketUtility.getParamsFromArg(personaId, "@@");
			for (String id : personaIdValue.stringPropertyNames()) {
				String value = personaIdValue.get(id).toString();
				if (residentPersonaIdPro.get(value) == null) {
					logger.error("Persona id : [" + value + "] is not present is the system");
					throw new RigInternalError("Persona id : [" + value + "] is not present is the system");
				}
				String personaPath = residentPersonaIdPro.get(value).toString();
				String templatePath = residentTemplatePaths.get(personaPath);
				if (StringUtils.isBlank(templatePath))
					throw new RigInternalError(
							"Template path is not present in the system for persona id : [" + value + "]");
				String packetPath  = templatePacketPath.get(templatePath);
				if (packetPath != null && !packetPath.isEmpty())
					packetPathArray.put(packetPath);
			}
		}else if(!step.getParameters().isEmpty() && step.getParameters().size()>1) {  // "e2e_bulkUploadPacket($$zipPacketPath,$$zipPacketPath2)"
			String _zipPacketPath = null;
			for (int i = 0; i < step.getParameters().size(); i++) {
				_zipPacketPath = step.getParameters().get(i);
				if (_zipPacketPath.startsWith("$$")) {
					_zipPacketPath = step.getScenario().getVariables().get(_zipPacketPath);
					packetPathArray.put(_zipPacketPath);
				}
			}

		} else {
			for (String packetPath : templatePacketPath.values())
				packetPathArray.put(packetPath);
		}
		String url = baseUrl + props.getProperty("bulkupload");
		Response response = packetUtility.postRequestWithQueryParamAndBody(url, packetPathArray.toString(),
				contextInuse, "BulkUpload");
		if (!response.getBody().asString().toLowerCase().contains("success"))
			throw new RigInternalError("Unable to perform bulkupload from packet utility");
	}

}
