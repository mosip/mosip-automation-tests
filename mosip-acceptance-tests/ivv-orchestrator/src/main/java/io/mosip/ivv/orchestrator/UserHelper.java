package io.mosip.ivv.orchestrator;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import io.mosip.testrig.apirig.admin.fw.util.TestCaseDTO;
import io.mosip.testrig.apirig.authentication.fw.precon.JsonPrecondtion;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.apirig.service.BaseTestCase;
import io.mosip.testrig.apirig.testscripts.DeleteWithParam;
import io.mosip.testrig.apirig.testscripts.PatchWithPathParam;
import io.mosip.testrig.apirig.testscripts.SimplePost;
import io.restassured.response.Response;

public class UserHelper extends BaseTestCaseUtil {
	public Logger logger = Logger.getLogger(MachineHelper.class);
	

	private static final String DeleteCenterMapping = "ivv_masterdata/DeleteCenterMapping/DeleteCenterMapping.yml";
	private static final String DeleteZoneMapping = "ivv_masterdata/DeleteZoneMapping/DeleteZoneMapping.yml";
	private static final String UserCenterMapping = "ivv_masterdata/UserCenterMapping/UserCenterMapping.yml";
	private static final String CreateZoneUser = "ivv_masterdata/ZoneUser/CreateZoneUser.yml";	
	private static final String UpdateZoneUserStatus = "ivv_masterdata/UpdateZoneUserStatus/UpdateZoneUserStatus.yml";
	private static final String UpdateUserCenterMappingStatus = "ivv_masterdata/UpdateUserCenterMappingStatus/UpdateUserCenterMappingStatus.yml";
	private static final String ZoneUserSearch = "ivv_masterdata/ZoneUser/ZoneUserSearch.yml";
	private static final String LostRid = "ivv_masterdata/LostRid/LostRid.yml";
	
	DeleteWithParam DeleteWithParam=new DeleteWithParam(); 
	SimplePost simplepost=new SimplePost();
	PatchWithPathParam patchWithPathParam=new PatchWithPathParam();

	public void deleteCenterMapping(String user) throws RigInternalError {
		try {
			Object[] testObjPutDcom=DeleteWithParam.getYmlTestData(DeleteCenterMapping);

			TestCaseDTO testPutDcom=(TestCaseDTO)testObjPutDcom[0];
			String input=testPutDcom.getInput();
			input = JsonPrecondtion.parseAndReturnJsonContent(input,
					user, "id");

			testPutDcom.setInput(input);

			DeleteWithParam.test(testPutDcom);
			Response response= DeleteWithParam.response;

			if (response!= null)
			{
				JSONObject jsonResp = new JSONObject(response.getBody().asString());
				logger.info( jsonResp.getJSONObject("response"));}

		} catch (Exception e) {
			//throw new RigInternalError(e.getMessage());

		}

	}
	
	public void deleteZoneMapping(String user,HashMap<String,String> map) throws RigInternalError {
		try {
			Object[] testObjPutDcom=DeleteWithParam.getYmlTestData(DeleteZoneMapping);

			TestCaseDTO testPutDcom=(TestCaseDTO)testObjPutDcom[0];
			String input=testPutDcom.getInput();
			testPutDcom.setEndPoint(testPutDcom.getEndPoint().replace("changeid", user));

			testPutDcom.setEndPoint(testPutDcom.getEndPoint().replace("changezone",map.get("userzonecode")));
			
			testPutDcom.setInput(input);
			DeleteWithParam.test(testPutDcom);
			Response response= DeleteWithParam.response;

			if (response!= null)
			{
				JSONObject jsonResp = new JSONObject(response.getBody().asString());
				logger.info( jsonResp.getJSONObject("response"));}

		} catch (Exception e) {
			//throw new RigInternalError(e.getMessage());

		}

	}

	public void createCenterMapping(String user, HashMap<String, String> map,int centerNum) throws RigInternalError {
		
		try {
			Object[] testObjPutDcom=simplepost.getYmlTestData(UserCenterMapping);

			TestCaseDTO testPutDcom=(TestCaseDTO)testObjPutDcom[0];
			String input=testPutDcom.getInput();
			input = JsonPrecondtion.parseAndReturnJsonContent(input,
					user, "id");
			input = JsonPrecondtion.parseAndReturnJsonContent(input,
					map.get("centerId"+centerNum), "regCenterId");
			input = JsonPrecondtion.parseAndReturnJsonContent(input,
					BaseTestCase.languageCode,"langCode");
			testPutDcom.setInput(input);
			simplepost.test(testPutDcom);
			Response response= simplepost.response;

			if (response!= null)
			{
				JSONObject jsonResp = new JSONObject(response.getBody().asString());
				logger.info( jsonResp.getJSONObject("response"));}

		} catch (Exception e) {
this.hasError=true;
			throw new RigInternalError(e.getMessage());

		}

	}

	public void createZoneMapping(HashMap<String, String> map, String user) throws RigInternalError {

		try {
			Object[] testObjPutDcom=simplepost.getYmlTestData(CreateZoneUser);

			TestCaseDTO testPutDcom=(TestCaseDTO)testObjPutDcom[0];
			String input=testPutDcom.getInput();
			input = JsonPrecondtion.parseAndReturnJsonContent(input,
					user, "userId");
			input = JsonPrecondtion.parseAndReturnJsonContent(input,
					map.get("zoneCode"), "zoneCode");
			input = JsonPrecondtion.parseAndReturnJsonContent(input,
					BaseTestCase.languageCode,"langCode");
						testPutDcom.setInput(input);
						
						String output=testPutDcom.getOutput();
						output = JsonPrecondtion.parseAndReturnJsonContent(input,
								user, "userId");
						output = JsonPrecondtion.parseAndReturnJsonContent(input,
								map.get("zoneCode"), "zoneCode");
									testPutDcom.setOutput(output);
						
			simplepost.test(testPutDcom);
			Response response= simplepost.response;

			if (response!= null)
			{
				JSONObject jsonResp = new JSONObject(response.getBody().asString());
				logger.info( jsonResp.getJSONObject("response"));}

		} catch (Exception e) {
			
		}
	}

	public void activateZoneMapping(String user, String flag) throws RigInternalError {
	  

		try {
			Object[] testObjPutDcom=patchWithPathParam.getYmlTestData(UpdateZoneUserStatus);

			TestCaseDTO testPutDcom=(TestCaseDTO)testObjPutDcom[0];
			String input=testPutDcom.getInput();
			input = JsonPrecondtion.parseAndReturnJsonContent(input,
					user, "userId");
			input = JsonPrecondtion.parseAndReturnJsonContent(input,
					(flag.contains("t")||flag.contains("T")?"true":"false"), "isActive");
			testPutDcom.setInput(input);
			patchWithPathParam.test(testPutDcom);
			Response response= patchWithPathParam.response;

			if (response!= null)
			{
				JSONObject jsonResp = new JSONObject(response.getBody().asString());
				logger.info( jsonResp.getJSONObject("response"));}

		} catch (Exception e) {
			this.hasError=true;
			throw new RigInternalError(e.getMessage());

		}
	}

	public void activateCenterMapping(String user, String flag) throws RigInternalError {
		  

		try {
			Object[] testObjPutDcom=patchWithPathParam.getYmlTestData(UpdateUserCenterMappingStatus);

			TestCaseDTO testPutDcom=(TestCaseDTO)testObjPutDcom[0];
			String input=testPutDcom.getInput();
			input = JsonPrecondtion.parseAndReturnJsonContent(input,
					user, "id");
			input = JsonPrecondtion.parseAndReturnJsonContent(input,
					(flag.contains("t")||flag.contains("T")?"true":"false"), "isActive");
			testPutDcom.setInput(input);
			patchWithPathParam.test(testPutDcom);
			Response response= patchWithPathParam.response;

			if (response!= null)
			{
				JSONObject jsonResp = new JSONObject(response.getBody().asString());
				logger.info( jsonResp.getJSONObject("response"));}

		} catch (Exception e) {
			this.hasError=true;
			throw new RigInternalError(e.getMessage());

		}
	}

	public HashMap<String, String> createZoneSearch(String user, HashMap<String, String> map) throws RigInternalError {
		

		try {
			String zoneCode=null;
			Object[] testObjPutDcom=simplepost.getYmlTestData(ZoneUserSearch);

			TestCaseDTO testPutDcom=(TestCaseDTO)testObjPutDcom[0];
			String input=testPutDcom.getInput();
			input = JsonPrecondtion.parseAndReturnJsonContent(input,
					user, "value");
			testPutDcom.setInput(input);
			
			
			simplepost.test(testPutDcom);
			
			
			
			
			Response response= simplepost.response;

			if (response!= null)
			{
				JSONObject jsonResp = new JSONObject(response.getBody().asString());
				JSONObject JO_resp=jsonResp.getJSONObject("response");
				JSONArray JA_data=JO_resp.getJSONArray("data");
				for(int i = 0; i < JA_data .length(); i++)
				{
				   JSONObject obj = JA_data.getJSONObject(i);
				   if(obj.getString("userId").equals(user))
				    zoneCode = obj.getString("zoneCode");
				}
				logger.info( JA_data);
				map.put("userzonecode", zoneCode);}
			
		} catch (Exception e) {

		}
		return map;	
	}
	
	public void retrieveLostRid(HashMap<String, String> map, String user) throws RigInternalError {

		try {
			Object[] testObjPutDcom=simplepost.getYmlTestData(LostRid);

			TestCaseDTO testPutDcom=(TestCaseDTO)testObjPutDcom[0];
			String input=testPutDcom.getInput();
			input = JsonPrecondtion.parseAndReturnJsonContent(input,
					user, "userId");
			input = JsonPrecondtion.parseAndReturnJsonContent(input,
					map.get("zoneCode"), "zoneCode");
			input = JsonPrecondtion.parseAndReturnJsonContent(input,
					BaseTestCase.languageCode,"langCode");
						testPutDcom.setInput(input);
						
						String output=testPutDcom.getOutput();
						output = JsonPrecondtion.parseAndReturnJsonContent(input,
								user, "userId");
						output = JsonPrecondtion.parseAndReturnJsonContent(input,
								map.get("zoneCode"), "zoneCode");
									testPutDcom.setOutput(output);
						
			simplepost.test(testPutDcom);
			Response response= simplepost.response;

			if (response!= null)
			{
				JSONObject jsonResp = new JSONObject(response.getBody().asString());
				logger.info( jsonResp.getJSONObject("response"));}

		} catch (Exception e) {
			
		}
	}
}

	