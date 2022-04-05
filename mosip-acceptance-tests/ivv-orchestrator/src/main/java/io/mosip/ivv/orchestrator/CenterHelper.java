package io.mosip.ivv.orchestrator;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import io.mosip.admin.fw.util.TestCaseDTO;
import io.mosip.authentication.fw.precon.JsonPrecondtion;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.testscripts.PatchWithPathParam;
import io.mosip.testscripts.PutWithPathParam;
import io.mosip.testscripts.SimplePost;
import io.mosip.testscripts.SimplePut;
import io.restassured.response.Response;
/*
 * TO perform center CRUD operations below is the fllow
 *          String centerId=centerHelper.centerCreate();
			centerHelper.centerUpdate(centerId);
			centerHelper.centerStatusUpdate(centerId,activeFlag);
			//centerHelper.centerDcom(centerId);
 */
public class CenterHelper extends BaseTestCaseUtil {
	public Logger logger = Logger.getLogger(MachineHelper.class);
	

	private static final String CreateRegistrationCenter = "masterdata/RegistrationCenter/CreateRegistrationCenter.yml";
	private static final String UpdateRegCentStatus = "masterdata/UpdateRegCentStatus/UpdateRegCentStatus.yml";
	private static final String UpdateRegistrationCenterNonLanguage = "masterdata/UpdateRegistrationCenterNonLanguage/UpdateRegistrationCenterNonLanguage.yml";
	private static final String UpdateRegistrationCenterLang = "masterdata/UpdateRegistrationCenterLang/UpdateRegistrationCenterLang.yml";
	private static final String DecommissionRegCenter = "masterdata/DecommissionRegCenter/DecommissionRegCenter.yml";
	
	SimplePost simplepost=new SimplePost() ;
	PatchWithPathParam patchwithpathparam=new PatchWithPathParam();
	SimplePut simpleput=new SimplePut();
	PutWithPathParam putwithpathparam=new PutWithPathParam();




	public void centerUpdate(String centerId,String zone) throws RigInternalError {
		try {
			Object[] testObjPut=simpleput.getYmlTestData(UpdateRegistrationCenterNonLanguage);

			TestCaseDTO testPut=(TestCaseDTO)testObjPut[0];
			
			String input=testPut.getInput();
			input = JsonPrecondtion.parseAndReturnJsonContent(input,
					centerId, "id");
			//input = JsonPrecondtion.parseAndReturnJsonContent(input,
				//	"MOSIP_DSL", "name");
			input = JsonPrecondtion.parseAndReturnJsonContent(input,
					zone,"zoneCode");
			testPut.setInput(input);

		
			
			String output=testPut.getOutput();
			output = JsonPrecondtion.parseAndReturnJsonContent(output,
					zone,"zoneCode");
			testPut.setOutput(output);
			simpleput.test(testPut);
			Response response= simpleput.response;

			if (response!= null)
			{
				JSONObject jsonResp = new JSONObject(response.getBody().asString());
				logger.info( jsonResp.getJSONObject("response"));}

		} catch (Exception e) {
			throw new RigInternalError(e.getMessage());

		}

	}



	public void centerDcom(String id) throws RigInternalError {
		try {
			Object[] testObjPutDcom=simpleput.getYmlTestData(DecommissionRegCenter);

			TestCaseDTO testPutDcom=(TestCaseDTO)testObjPutDcom[0];
			String input=testPutDcom.getInput();
			input = JsonPrecondtion.parseAndReturnJsonContent(input,
					id, "regCenterID");

			testPutDcom.setInput(input);

			putwithpathparam.test(testPutDcom);
			Response response= putwithpathparam.response;

			if (response!= null)
			{
				JSONObject jsonResp = new JSONObject(response.getBody().asString());
				logger.info( jsonResp.getJSONObject("response"));}

		} catch (Exception e) {
			throw new RigInternalError(e.getMessage());

		}

	}

	public void centerStatusUpdate(String id,Boolean activeFlag) throws RigInternalError {
		try {
			Object[] testObjPatch=patchwithpathparam.getYmlTestData(UpdateRegCentStatus);

			TestCaseDTO testPatch=(TestCaseDTO)testObjPatch[0];
			
			String input=testPatch.getInput();
			input = JsonPrecondtion.parseAndReturnJsonContent(input,
					id, "id");
			input = JsonPrecondtion.parseAndReturnJsonContent(input,
					activeFlag.toString(), "isActive");
			testPatch.setInput(input);

			patchwithpathparam.test(testPatch);
			Response response= patchwithpathparam.response;

			if (response!= null)
			{
				JSONObject jsonResp = new JSONObject(response.getBody().asString());
				logger.info( jsonResp.getJSONObject("response"));}

		} catch (Exception e) {
			throw new RigInternalError(e.getMessage());

		}

	}

	public String centerCreate(String zone) throws RigInternalError {
		try {
			String id =null;
			Object[] testObjPost=simplepost.getYmlTestData(CreateRegistrationCenter);

			TestCaseDTO testPost=(TestCaseDTO)testObjPost[0];
			String input=testPost.getInput();
			input = JsonPrecondtion.parseAndReturnJsonContent(input,
					appendDate, "name");
			input = JsonPrecondtion.parseAndReturnJsonContent(input,
					zone,"zoneCode");
			testPost.setInput(input);
			
			String output=testPost.getOutput();
			output = JsonPrecondtion.parseAndReturnJsonContent(output,
					zone,"zoneCode");
			testPost.setOutput(output);
			
			simplepost.test(testPost);
			Response response= simplepost.response;

			if (response!= null)
			{
				JSONObject jsonResp = new JSONObject(response.getBody().asString());
				logger.info( jsonResp.getJSONObject("response"));
				String name = jsonResp.getJSONObject("response").getString("name"); 
				 id = jsonResp.getJSONObject("response").getString("id"); 
			
			}
			logger.info("id="+id);
			return id;
		} catch (Exception e) {
			throw new RigInternalError(e.getMessage());

		}

	}


	
}
