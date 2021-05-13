package io.mosip.ivv.e2e.methods;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.assertj.core.util.Arrays;
import org.json.JSONArray;
import org.json.JSONObject;

import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.ivv.orchestrator.TestRunner;
import io.restassured.response.Response;

public class GetResidentData extends BaseTestCaseUtil implements StepInterface {
	Logger logger = Logger.getLogger(GetResidentData.class);

	@Override
	public void run() throws RigInternalError {
		cleanData();
		int nofResident=1;
		Boolean bAdult=false;
		Boolean bSkipGuardian=false;
		String gender=null;
		String missFields = null;
		if (step.getParameters() == null || step.getParameters().isEmpty() ||step.getParameters().size()<4) {
			logger.warn("GetResidentData Arugemnt is  Missing : Please pass the argument from DSL sheet");
		} else {
			nofResident=Integer.parseInt(step.getParameters().get(0));
			bAdult = Boolean.parseBoolean(step.getParameters().get(1));
			bSkipGuardian = Boolean.parseBoolean(step.getParameters().get(2));
			gender = step.getParameters().get(3);
			if(step.getParameters().size()>4)
			missFields = step.getParameters().get(4).replaceAll("@@", ",");			 
		}
		//false,true,any
		//List<String> generateDResidentData = packetUtility.generateResidents(nofResident,bAdult,bSkipGuardian,gender,missFields,contextInuse);
		Response response = packetUtility.generateResident(nofResident,bAdult,bSkipGuardian,gender,missFields,contextInuse);

		JSONArray resp = new JSONObject(response.getBody().asString()).getJSONArray("response");
		List<String> residentPaths = new ArrayList<>();
		for (int i = 0; i < resp.length(); i++) {
			JSONObject obj = resp.getJSONObject(i);
			String resFilePath = obj.get("path").toString();
			String id = obj.get("id").toString();
			residentPaths.add(resFilePath);
			residentTemplatePaths.put(resFilePath, null);
			residentPersonaIdPro.put(id, resFilePath);
			
		}
		if(!residentPersonaIdPro.isEmpty())
		storeProp(residentPersonaIdPro);
		
	}
	
	private static void storeProp(Properties prop) {
		String filePath=TestRunner.getExeternalResourcePath()
				+ props.getProperty("ivv.path.deviceinfo.folder") +"residentPersonaIdPro.properties";
		OutputStream output = null;
		try {
			output = new FileOutputStream(filePath);
			prop.store(output, null);
			output.close();
			output.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	

	private void cleanData() {
		pridsAndRids.clear();
		uinReqIds.clear();
		residentTemplatePaths.clear();
		residentPathsPrid.clear();
		templatePacketPath.clear();
	}
}
