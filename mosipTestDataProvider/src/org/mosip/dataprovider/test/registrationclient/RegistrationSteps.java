package org.mosip.dataprovider.test.registrationclient;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import java.time.LocalDateTime;
import java.util.Base64;

import org.json.JSONObject;
import org.mosip.dataprovider.util.CommonUtil;
import org.mosip.dataprovider.util.RestClient;


import variables.VariableManager;

public class RegistrationSteps {
	static LocalDateTime lastSyncTime;
	static String workDirectory;
	static String workPacketDirectory;
	static String defaultTemplateLocation;
    static String  templateFolder;
	static {
		try {
			workDirectory = Files.createTempDirectory("prereg").toFile().getAbsolutePath();
			workPacketDirectory = Files.createTempDirectory("pktcreator").toFile().getAbsolutePath();
			templateFolder = VariableManager.getVariableValue(VariableManager.NS_REGCLIENT, "packetTemplateLocation").toString();
			
            File folder = new File(templateFolder);
            File templateName = folder.listFiles()[0];
            defaultTemplateLocation = templateName.getAbsolutePath();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}


	public JSONObject syncPrereg() throws Exception {

		 LocalDateTime currentSyncTime = LocalDateTime.now();
		 if(lastSyncTime == null) {
			 lastSyncTime = LocalDateTime.now().minusMinutes(10) ;
			 
		 }
		 String url =  VariableManager.getVariableValue("urlBase") +
					VariableManager.getVariableValue(VariableManager.NS_REGCLIENT, "preRegSyncURL").toString();
			
		 JSONObject syncRequest = new JSONObject();
		 syncRequest.put("registrationCenterId", 
				 VariableManager.getVariableValue(VariableManager.NS_REGCLIENT, "centerId"));
		 syncRequest.put("fromDate",CommonUtil.getUTCDateTime(lastSyncTime));
		 syncRequest.put("toDate",CommonUtil.getUTCDateTime(currentSyncTime ));

		JSONObject wrapper = new JSONObject();
			//wrapper.put("metadata", "");
		wrapper.put("version", "1.0");
		wrapper.put("id", "mosip.pre-registration.datasync.fetch.ids");
		wrapper.put("requesttime", CommonUtil.getUTCDateTime(null));
		wrapper.put("request", syncRequest);

		JSONObject preregResponse = RestClient.post(url, wrapper);

		lastSyncTime = currentSyncTime;		
	    return (JSONObject) preregResponse.get("preRegistrationIds");
	 }
	/*
	 public String downloadPreregPacket(String preregId) throws Exception{

		 String url =  VariableManager.getVariableValue("urlBase") +
					VariableManager.getVariableValue(VariableManager.NS_REGCLIENT, "preRegSyncURL").toString();
		
		JSONObject preregResponse = RestClient.get(url+"/"+preregId, new JSONObject(), new JSONObject());
		
		Path temPath = Path.of(url, null);// (workDirectory, preregId+".zip");
		
		Files.write(temPath, Base64.getDecoder().decode(preregResponse.getString("zip-bytes")));

		return temPath.toString();
	}*/
}
