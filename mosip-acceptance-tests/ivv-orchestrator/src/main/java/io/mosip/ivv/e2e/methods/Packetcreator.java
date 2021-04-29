package io.mosip.ivv.e2e.methods;

import static org.testng.Assert.assertTrue;

import org.json.JSONObject;

import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.e2e.constant.E2EConstants;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;
import io.restassured.response.Response;

public class Packetcreator extends BaseTestCaseUtil implements StepInterface {
	
	@Override
	public void run() throws RigInternalError {
		
		String packetPath=null;
		for(String resDataPath:residentTemplatePaths.keySet()) {
			String templatePath = residentTemplatePaths.get(resDataPath);
			String idJosn=templatePath + "/REGISTRATION_CLIENT/" + E2EConstants.LOST_PROCESS + "/rid_id/" + "ID.json";
			packetPath=createPacket(idJosn,templatePath);
			templatePacketPath.put(templatePath, packetPath);
			//this is inserted for storing rid with resident data it will be deleted in RIDSync
			ridPersonaPath.put(packetPath, resDataPath);
		}
	}
	
	
	
	private String createPacket(String idJsonPath,String templatePath) throws RigInternalError {
		String url = baseUrl + props.getProperty("packetCretorUrl");
		JSONObject jsonReq = new JSONObject();
		jsonReq.put("idJsonPath", idJsonPath);
		jsonReq.put("process", E2EConstants.LOST_PROCESS);
		jsonReq.put("source", E2EConstants.SOURCE);
		jsonReq.put("templatePath", templatePath);
		Response response =postRequestWithPathParamAndBody(url,jsonReq.toString(),contextKey,"CreatePacket");
		//assertTrue(response.getBody().asString().contains("zip"),"Unable to get packet from packet utility");
		if(!response.getBody().asString().toLowerCase().contains("zip"))
			throw new RigInternalError("Unable to get packet from packet utility");
		return response.getBody().asString().replaceAll("\\\\", "\\\\\\\\");
		
	}

}
