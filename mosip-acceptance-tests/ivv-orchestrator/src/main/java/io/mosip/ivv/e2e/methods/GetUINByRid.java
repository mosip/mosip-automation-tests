package io.mosip.ivv.e2e.methods;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.testng.Reporter;

import io.mosip.authentication.fw.util.FileUtil;
import io.mosip.authentication.fw.util.ReportUtil;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.ivv.orchestrator.TestResources;
import io.mosip.ivv.orchestrator.TestRunner;
import io.mosip.kernel.util.KernelAuthentication;
import io.restassured.response.Response;

public class GetUINByRid extends BaseTestCaseUtil implements StepInterface {

	private String getIdentityUrl = "/resident/uin/";
	private String identitypath = "preReg/identity/";
	Logger logger = Logger.getLogger(GetIdentityByRid.class);
	KernelAuthentication kauth = new KernelAuthentication();
    @SuppressWarnings("static-access")
	@Override
    public void run() throws RigInternalError {
    	getIdentity(this.pridsAndRids);
    }
    public void getIdentity(HashMap<String, String> rids) throws RigInternalError
    {
    	uinReqIds.clear();
    	for(String rid: rids.values())
    	{
    		if(rid!=null) {
    			Reporter.log("<b><u>"+"GetIdentity By Rid"+ "</u></b>");
        		Reporter.log("<pre>" + ReportUtil.getTextAreaJsonMsgHtml("{Rid: "+rid +"}") + "</pre>");
        		long startTime = System.currentTimeMillis();
				logger.info(this.getClass().getSimpleName()+" starts at..."+startTime +" MilliSec");
        		Response response = getRequest(baseUrl+getIdentityUrl+rid, "");
        		long stopTime = System.currentTimeMillis();
				long elapsedTime = stopTime - startTime;
				logger.info("Time taken to execute "+ this.getClass().getSimpleName()+": " +elapsedTime +" MilliSec");
				Reporter.log("<b><u>"+"Time taken to execute "+ this.getClass().getSimpleName()+": " +elapsedTime +" MilliSec"+ "</u></b>");
				logger.info("Response from get Identity for RID: "+rid+" "+response.asString());
    		
    		
    		String uin=response.asString();
    		if(!StringUtils.isEmpty(uin)) {
    			uinReqIds.put(uin, null);
    			if(!uinPersonaProp.containsKey(uin))
    			uinPersonaProp.put(uin, ridPersonaPath.get(rid));
    		}
				/*JSONObject res = new JSONObject(response.asString());
				 * if(!res.get("response").toString().equals("null")) { JSONObject respJson =
				 * new JSONObject(res.get("response").toString()); JSONObject identityJson = new
				 * JSONObject(respJson.get("identity").toString()); String uin =
				 * identityJson.get("UIN").toString(); uinReqIds.put(uin, null);
				 * FileUtil.createFile(new
				 * File(TestResources.getResourcePath()+identitypath+uin+".json"),
				 * response.asString()); }
				 */else
    			{
    				logger.error("Issue while fetching identity for RID: "+rid+" Response: "+response.toString());
    				throw new RigInternalError("Not able to Fetch identity for RID: "+rid);
    			}
				/*
				 * for (String resDataPath : residentTemplatePaths.keySet()) {
				 * uinPersonaProp.put(uin, resDataPath); String propFilePath
				 * =TestRunner.getExeternalResourcePath() +
				 * properties.getProperty("ivv.path.uinpersonafile"); try { FileOutputStream
				 * outputStrem = new FileOutputStream(propFilePath);
				 * uinPersonaProp.store(outputStrem,
				 * "This file contain uin and corresponding persona file path"); } catch
				 * (IOException e) {
				 * logger.error("failed to mapped the uin with personafile path"); } }
				 */
    	}
    	}
    }

}
