package io.mosip.ivv.orchestrator;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.testng.Reporter;

import io.mosip.admin.fw.util.TestCaseDTO;
import io.mosip.authentication.fw.precon.JsonPrecondtion;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.testscripts.GetWithParam;
import io.mosip.testscripts.GetWithParamForAutoGenId;
import io.mosip.testscripts.GetWithQueryParam;
import io.mosip.testscripts.PatchWithPathParam;
import io.mosip.testscripts.PutWithPathParam;
import io.mosip.testscripts.SimplePost;
import io.mosip.testscripts.SimplePut;
import io.restassured.response.Response;

public class SyncDataHelper extends BaseTestCaseUtil {
	public Logger logger = Logger.getLogger(SyncDataHelper.class);
	

	private static final String Publickeyverify = "syncdata/Publickeyverify/Publickeyverify.yml";
	private static final String GetClientSettings = "syncdata/clientsettings/GetClientSettings.yml";
	private static final String GetLatestIdSchema = "syncdata/latestidschema/Getlatestidschema.yml";
	private static final String GetConfigKeyIndex="syncdata/configkeyindex/GetConfigKeyIndex.yml";
	
	SimplePost simplepost=new SimplePost() ;
	PatchWithPathParam patchwithpathparam=new PatchWithPathParam();
	SimplePut simpleput=new SimplePut();
	PutWithPathParam putwithpathparam=new PutWithPathParam();
    GetWithParam getWithParam=new GetWithParam();
    GetWithQueryParam getWithQueryParam=new GetWithQueryParam();


		public String verifyPublicKey(HashMap<String, String> machineDetailsmap) throws RigInternalError {
		try {
			String keyIndex =null;
			Object[] testObjPost=simplepost.getYmlTestData(Publickeyverify);

			TestCaseDTO testPost=(TestCaseDTO)testObjPost[0];
			String input=testPost.getInput();
			input = JsonPrecondtion.parseAndReturnJsonContent(input,
					machineDetailsmap.get("machineName"), "machineName");
			input = JsonPrecondtion.parseAndReturnJsonContent(input,
					machineDetailsmap.get("publicKey"), "publicKey");
			input = JsonPrecondtion.parseAndReturnJsonContent(input,
					machineDetailsmap.get("signPublicKey"), "signPublicKey");
			testPost.setInput(input);
			simplepost.test(testPost);
			Response response= simplepost.response;

			if (response!= null)
			{
				JSONObject jsonResp = new JSONObject(response.getBody().asString());
				logger.info( jsonResp.getJSONObject("response"));
				 keyIndex = jsonResp.getJSONObject("response").getString("keyIndex"); 
			
			}
			logger.info("keyIndex="+keyIndex);

			
			return keyIndex;
		} catch (Exception e) {
			throw new RigInternalError(e.getMessage());

		}

	}



		public void getClientsettings(HashMap<String, String> machineDetailsmap) throws RigInternalError {
			try {	String lastSyncTime =null;
			Object[] testObjPost=getWithParam.getYmlTestData(GetClientSettings);

			TestCaseDTO testPost=(TestCaseDTO)testObjPost[0];
			String input=testPost.getInput();
			
			String keystring=machineDetailsmap.get("keyindex");
			testPost.setEndPoint(testPost.getEndPoint().replace("change", keystring));
			getWithParam.test(testPost);
			Response response= getWithParam.response;

			if (response!= null)
			{
				JSONObject jsonResp = new JSONObject(response.getBody().asString());
				
				lastSyncTime = jsonResp.getJSONObject("response").getString("lastSyncTime"); 
				logger.info(lastSyncTime);
			
			
			}
			
		} catch (Exception e) {
			throw new RigInternalError(e.getMessage());

		}
		}



		public void getlatestidschema() throws RigInternalError {
			try {
			Object[] testObjPost=getWithParam.getYmlTestData(GetLatestIdSchema);

			TestCaseDTO testPost=(TestCaseDTO)testObjPost[0];
			
			getWithParam.test(testPost);
			Response response= getWithParam.response;

			if (response!= null)
			{
				JSONObject jsonResp = new JSONObject(response.getBody().asString());
				
			
				logger.info(jsonResp.getJSONObject("response").getString("schemaJson"));

			
			}
			
		} catch (Exception e) {
			throw new RigInternalError(e.getMessage());

		}
		}



		public void getConfigsKeyindex(HashMap<String, String> machineDetailsmap) throws RigInternalError {
		
			try {	String lastSyncTime =null;
			Object[] testObjPost=getWithParam.getYmlTestData(GetConfigKeyIndex);

			TestCaseDTO testPost=(TestCaseDTO)testObjPost[0];
			String keystring=machineDetailsmap.get("keyindex");
			testPost.setEndPoint(testPost.getEndPoint().replace("change", keystring));
			getWithParam.test(testPost);
			Response response= getWithParam.response;

			if (response!= null)
			{
				JSONObject jsonResp = new JSONObject(response.getBody().asString());
			  logger.info(jsonResp.getJSONObject("response").getJSONObject("configDetail").getString("registrationConfiguration"));
			  logger.info(machineDetailsmap);
			   
			}
			
		} catch (Exception e) {
			throw new RigInternalError(e.getMessage());

		}
		}

	
}
