package io.mosip.testrig.dslrig.packetcreator.service;


import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PreregSyncService {
    Logger logger = LoggerFactory.getLogger(PreregSyncService.class);

    @Value("${mosip.test.baseurl}")
    private String baseUrl;

    @Value("${mosip.test.prereg.syncapi}")
    private String syncapi;

    @Value("${mosip.test.regclient.centerid}")
	private String centerId;
	
	@Autowired
	private APIRequestUtil apiUtil;

	
    private LocalDateTime lastSyncTime;

    private String workDirectory;

    @Autowired
    private ContextUtils contextUtils;
   
    
    
    @PostConstruct
    public void init() {
		if (workDirectory != null) return;
		try{
			workDirectory = Files.createTempDirectory("prereg").toFile().getAbsolutePath();
			logger.info("CURRENT PRE_REG WORK DIRECTORY --> {}", workDirectory);
			apiUtil.clearToken();
		} catch(Exception ex){
			logger.error("", ex);
		}
	}

	public String getWorkDirectory() {
		return workDirectory;
	}

    public JSONObject syncPrereg(String contextKey) throws Exception {
		if(lastSyncTime == null) {
			lastSyncTime = LocalDateTime.now();
			lastSyncTime = lastSyncTime.minus(6, ChronoUnit.DAYS);
		}
		LocalDateTime currentSyncTime = LocalDateTime.now();
		JSONObject syncRequest = new JSONObject();
		syncRequest.put("registrationCenterId", centerId);
		syncRequest.put("fromDate",APIRequestUtil.getUTCDate(lastSyncTime));
		syncRequest.put("toDate",APIRequestUtil.getUTCDate(currentSyncTime));

		JSONObject wrapper = new JSONObject();
		//wrapper.put("metadata", "");
		wrapper.put("version", "1.0");
		wrapper.put("id", "mosip.pre-registration.datasync.fetch.ids");
		wrapper.put("requesttime", APIRequestUtil.getUTCDateTime(null));
		wrapper.put("request", syncRequest);

		logger.info("pre-reg sync request {}", wrapper);

		JSONObject preregResponse = apiUtil.post(baseUrl,baseUrl + syncapi, wrapper,contextKey);
		logger.info("sync responded with {} pre-reg ids", preregResponse.get("countOfPreRegIds"));
		lastSyncTime = currentSyncTime;		
       return (JSONObject) preregResponse.get("preRegistrationIds");
    }

    public String downloadPreregPacket(String preregId, String contextKey) throws Exception{
    	
    	if(contextKey != null && !contextKey.equals("")) {
    		
    		Properties props = contextUtils.loadServerContext(contextKey);
    		props.forEach((k,v)->{
    			if(k.toString().equals("mosip.test.baseurl")) {
    				baseUrl = v.toString().trim();
    			}
    			
    		});
    	}
    	//Fix:MOSIP-13932- Auth API signature changed
    	logger.info("Before getPreReg");
		JSONObject preregResponse = apiUtil.getPreReg(baseUrl,baseUrl + syncapi+"/"+preregId, new JSONObject(), new JSONObject(),contextKey);
		logger.info("Downloaded data for prereg id {} ", preregResponse.getString("pre-registration-id"));
		Path temPath = Path.of(workDirectory, preregId+".zip");
		byte[] bytes=Base64.getDecoder().decode(preregResponse.getString("zip-bytes"));
		
		Files.write(temPath, bytes ,StandardOpenOption.CREATE);

		logger.info("Wrote prereg id {} to {} ", preregResponse.getString("pre-registration-id"), temPath.toString());
        return temPath.toString();
	}
	
	/*public void syncAndDownload() throws Exception {
		JSONObject jb = syncPrereg();
		while(jb.keys().hasNext()) {
			String prid = (String)jb.get("pre-registration-id");
			try {
				String location = downloadPreregPacket(prid);
				logger.info("downloaded the prereg packet in {} ", location);

				File targetDirectory = Path.of(workDirectory, prid).toFile();
				if(!targetDirectory.exists()  && !targetDirectory.mkdir())
					throw new Exception("Failed to create target directory");

				zipUtils.unzip(location, targetDirectory.getAbsolutePath());

			} catch (Exception iox){
				logger.error("Failed for PRID : {}", prid, iox);
			}
		}
	}*/

}