package io.mosip.testrig.dslrig.ivv.e2e.methods;

import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testrig.dslrig.ivv.orchestrator.PacketUtility;
import io.mosip.testrig.dslrig.ivv.orchestrator.dslConfigManager;
import io.restassured.response.Response;

public class BulkUploadPacket extends BaseTestCaseUtil implements StepInterface {
	public static Logger logger = Logger.getLogger(BulkUploadPacket.class);

	static {
		if (dslConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	@Override
	public void run() throws RigInternalError {
		JSONArray packetPathArray = new JSONArray();
		Properties personaIdValue = null;
		if (!step.getParameters().isEmpty() && step.getParameters().size() == 1) { /// id1=878787877@@id2=8878787989
			String personaId = step.getParameters().get(0);
			personaIdValue = PacketUtility.getParamsFromArg(personaId, "@@");
			for (String id : personaIdValue.stringPropertyNames()) {
				String value = personaIdValue.get(id).toString();
				if (step.getScenario().getResidentPersonaIdPro().get(value) == null) {
					logger.error("Persona id : [" + value + "] is not present is the system");
					this.hasError = true;
					throw new RigInternalError("Persona id : [" + value + "] is not present is the system");
				}
				String personaPath = step.getScenario().getResidentPersonaIdPro().get(value).toString();
				String templatePath = step.getScenario().getResidentTemplatePaths().get(personaPath);
				if (StringUtils.isBlank(templatePath)) {
					this.hasError = true;
					throw new RigInternalError(
							"Template path is not present in the system for persona id : [" + value + "]");
				}
				String packetPath = step.getScenario().getTemplatePacketPath().get(templatePath);
				if (packetPath != null && !packetPath.isEmpty())
					packetPathArray.put(packetPath);
			}
		} else if (!step.getParameters().isEmpty() && step.getParameters().size() > 1) { // "e2e_bulkUploadPacket($$zipPacketPath,$$zipPacketPath2)"
			String _zipPacketPath = null;
			for (int i = 0; i < step.getParameters().size(); i++) {
				_zipPacketPath = step.getParameters().get(i);
				if (_zipPacketPath.startsWith("$$")) {
					_zipPacketPath = step.getScenario().getVariables().get(_zipPacketPath);
					packetPathArray.put(_zipPacketPath);
				}
			}

		} else {
			for (String packetPath : step.getScenario().getTemplatePacketPath().values())
				packetPathArray.put(packetPath);
		}
		String url = baseUrl + props.getProperty("bulkupload");
		Response response = packetUtility.postRequestWithQueryParamAndBody(url, packetPathArray.toString(),
				step.getScenario().getCurrentStep(), "BulkUpload", step);
		if (!response.getBody().asString().toLowerCase().contains("success")) {
			this.hasError = true;
			throw new RigInternalError("Unable to perform bulkupload from packet utility");
		}
	}

}
