package io.mosip.ivv.e2e.methods;

import java.util.Base64;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;
import io.restassured.response.Response;

public class CorruptPacket extends BaseTestCaseUtil implements StepInterface {
	Logger logger = Logger.getLogger(CorruptPacket.class);

	@Override
	public void run() throws RigInternalError {
		String offset = null;
		String dataToEncdoeInBase64 = null;
		if (!step.getParameters().isEmpty() && step.getParameters().size() == 2) {
			offset = step.getParameters().get(0);
			dataToEncdoeInBase64 = step.getParameters().get(1);
			for (String packetPath : templatePacketPath.values()) {
				corruptPacket(packetPath, offset, dataToEncdoeInBase64);
			}
		} else if (!step.getParameters().isEmpty() && step.getParameters().size() == 3) { // "$$var=e2e_corruptPacket(1024,Hello Auto,$$zipPacketPath)"
			offset = step.getParameters().get(0);
			dataToEncdoeInBase64 = step.getParameters().get(1);
			String _zipPacketPath = step.getParameters().get(2);
			if (_zipPacketPath.startsWith("$$")) {
				_zipPacketPath = step.getScenario().getVariables().get(_zipPacketPath);
				corruptPacket(_zipPacketPath, offset, dataToEncdoeInBase64);
			}
		} else {
			throw new RigInternalError("Parameter is missing");
		}
	}

	private void corruptPacket(String packetPath, String offset, String dataToEncdoeInBase64) throws RigInternalError {
		String url = baseUrl + props.getProperty("writeFile") + "offset=" + offset;
		JSONObject jsonReq = new JSONObject();
		jsonReq.put("filePath", packetPath);
		jsonReq.put("base64data", Base64.getEncoder().encodeToString(dataToEncdoeInBase64.getBytes()));
		Response response = postRequest(url, jsonReq.toString(), "Corrupt Packet");
		if (!response.getBody().asString().toLowerCase().contains(".zip"))
			throw new RigInternalError("Unable to Corrupt Packet");
	}

}
