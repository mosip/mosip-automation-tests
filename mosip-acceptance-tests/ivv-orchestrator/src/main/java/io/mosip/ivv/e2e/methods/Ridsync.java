package io.mosip.ivv.e2e.methods;

import static org.testng.Assert.assertTrue;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.e2e.constant.E2EConstants;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.ivv.orchestrator.TestRunner;
import io.restassured.response.Response;

public class Ridsync extends BaseTestCaseUtil implements StepInterface {
	Logger logger = Logger.getLogger(Ridsync.class);
	
	@Override
	public void run() throws RigInternalError {
		String process=null;
		if (step.getParameters() == null || step.getParameters().isEmpty()) {
			logger.error("Parameter is  missing from DSL step");
			assertTrue(false,"process paramter is  missing in step: "+step.getName());
		} else if(step.getParameters().size() == 1){
			process =step.getParameters().get(0);
			pridsAndRids.clear();
			String registrationId=null;
			for (String packetPath : templatePacketPath.values()) {
				registrationId=ridsync(packetPath, E2EConstants.APPROVED_SUPERVISOR_STATUS,process);
				pridsAndRids.put(packetPath, registrationId);
				ridPersonaPath.put(registrationId, ridPersonaPath.get(packetPath));
				ridPersonaPath.remove(packetPath);
			}
			storeProp(pridsAndRids);
		}else
			if(step.getParameters().size()>1) { // "$$rid=e2e_ridsync(NEW,$$zipPacketPath)"
			process =step.getParameters().get(0);
			String _zipPacketPath=step.getParameters().get(1);
			if(_zipPacketPath.startsWith("$$")) {
				_zipPacketPath=step.getScenario().getVariables().get(_zipPacketPath);
				String _rid=ridsync(_zipPacketPath, E2EConstants.APPROVED_SUPERVISOR_STATUS,process);
				if(step.getOutVarName()!=null)
					 step.getScenario().getVariables().put(step.getOutVarName(), _rid);
			}
		}
		
	}

	private String ridsync(String containerPath, String supervisorStatus,String process) throws RigInternalError {
		String url = baseUrl + props.getProperty("ridsyncUrl");
		JSONObject jsonReq = buildRequest(containerPath, supervisorStatus,process);
		Response response = postRequestWithQueryParamAndBody(url, jsonReq.toString(),contextInuse, "Ridsync");
		
		JSONArray jsonArray = new JSONArray(response.asString());
		JSONObject responseJson = new JSONObject(jsonArray.get(0).toString());
		//assertTrue(response.getBody().asString().contains("SUCCESS"),"Unable to do RID sync from packet utility");
		if(!response.getBody().asString().toLowerCase().contains("success"))
			throw new RigInternalError("Unable to do RID sync from packet utility");
		return responseJson.get("registrationId").toString();

	}

	private JSONObject buildRequest(String containerPath, String supervisorStatus,String process) {
		JSONObject jsonReq = new JSONObject();
		jsonReq.put("containerPath", containerPath);
		jsonReq.put("email", "email");
		jsonReq.put("name", "name");
		jsonReq.put("phone", "phone");
		jsonReq.put("process", process);
		jsonReq.put("supervisorComment", "supervisorComment");
		jsonReq.put("supervisorStatus", supervisorStatus);
		return jsonReq;
	}
	
	private static void storeProp(HashMap<String,String> map) {
		Properties prop= new Properties();
		for(String key: map.keySet())
			prop.put(key, map.get(key));
		String filePath=TestRunner.getExeternalResourcePath()
				+ props.getProperty("ivv.path.deviceinfo.folder") +"ridPersonaPathProp.properties";
		OutputStream output = null;
		try {
			output = new FileOutputStream(filePath,true);
			prop.store(output, null);
			output.close();
			output.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
