package org.mosip.dataprovider.test.registrationclient;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
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
			templateFolder = VariableManager.getVariableValue( "packetTemplateLocation").toString();
			
            File folder = new File(templateFolder);
            if(folder.listFiles() != null) {
            	File templateName = folder.listFiles()[0];
            	defaultTemplateLocation = templateName.getAbsolutePath();
            }

		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		
	}


	public JSONObject syncPrereg() throws Exception {

		 LocalDateTime currentSyncTime = LocalDateTime.now();
		 if(lastSyncTime == null) {
			 lastSyncTime = LocalDateTime.now().minusMinutes(10) ;
			 
		 }
		 String url =  VariableManager.getVariableValue("urlBase").toString().trim() +
					VariableManager.getVariableValue( "preRegSyncURL").toString().trim();
			
		 JSONObject syncRequest = new JSONObject();
		 syncRequest.put("registrationCenterId", 
				 VariableManager.getVariableValue( "centerId"));
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

	public String getRIDStatus(String rid) throws Exception {
	
		String uri =  "/resident/v1/rid/check-status";
		String url = VariableManager.getVariableValue("urlBase").toString().trim() + uri ;

		JSONObject req = new JSONObject();
		JSONObject reqWrapper = new JSONObject();
		reqWrapper.put("id", "mosip.resident.checkstatus");
		reqWrapper.put("requesttime", CommonUtil.getUTCDateTime(LocalDateTime.now()));
		reqWrapper.put("version", "v1");
		req.put("individualId", rid);
		req.put("individualIdType", "RID");
		reqWrapper.put("request", req);


		JSONObject response =RestClient.post(url,reqWrapper);
		return response.get("ridStatus").toString();
		
	}
	public String getUINByRID(String rid) throws Exception {
		
		String uri =  "/idrepository/v1/identity/idvid/" + rid;
		String url = VariableManager.getVariableValue("urlBase").toString().trim() + uri ;

		/*JSONObject req = new JSONObject();
		JSONObject reqWrapper = new JSONObject();
		reqWrapper.put("id", "mosip.resident.checkstatus");
		reqWrapper.put("requesttime", CommonUtil.getUTCDateTime(LocalDateTime.now()));
		reqWrapper.put("version", "v1");
		req.put("individualId", rid);
		req.put("individualIdType", "RID");
		reqWrapper.put("request", req);
		 */

		JSONObject response =RestClient.get(url,new JSONObject(),new JSONObject());
		return response.getJSONObject("identity").getString("UIN");
		
	}
	
}
