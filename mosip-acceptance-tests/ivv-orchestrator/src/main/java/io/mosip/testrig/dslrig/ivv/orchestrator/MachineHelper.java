package io.mosip.testrig.dslrig.ivv.orchestrator;

import java.util.HashMap;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import io.mosip.testrig.apirig.admin.fw.util.TestCaseDTO;
import io.mosip.testrig.apirig.authentication.fw.precon.JsonPrecondtion;
import io.mosip.testrig.apirig.kernel.util.ConfigManager;
import io.mosip.testrig.apirig.service.BaseTestCase;
import io.mosip.testrig.apirig.testscripts.PatchWithPathParam;
import io.mosip.testrig.apirig.testscripts.PatchWithPathParamsAndBody;
import io.mosip.testrig.apirig.testscripts.PatchWithQueryParam;
import io.mosip.testrig.apirig.testscripts.PutWithPathParam;
import io.mosip.testrig.apirig.testscripts.SimplePatch;
import io.mosip.testrig.apirig.testscripts.SimplePost;
import io.mosip.testrig.apirig.testscripts.SimplePostForAutoGenId;
import io.mosip.testrig.apirig.testscripts.SimplePut;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.restassured.response.Response;

public class MachineHelper extends BaseTestCaseUtil {
	public Logger logger = Logger.getLogger(MachineHelper.class);
	private final String CreateMachine = "ivv_masterdata/Machine/CreateMachine.yml";
	private final String UpdateMachineStatus = "ivv_masterdata/UpdateMachineStatus/UpdateMachineStatus.yml";
	
	
	private final String CreateMachineSpecification = "ivv_masterdata/MachineSpecification/CreateMachineSpecification.yml";
	private final String CreateMachineType = "ivv_masterdata/MachineType/CreateMachineType.yml";
	private final String UpdateMachineTypeStatus = "ivv_masterdata/UpdateMachineTypeStatus/UpdateMachineTypeStatus.yml";
	private final String UpdateMachineSpecificationStatus = "ivv_masterdata/UpdateMachineSpecificationStatus/UpdateMachineSpecificationStatus.yml";
	private final String UpdateMachine="ivv_masterdata/Machine/UpdateMachine.yml";
	private final String DcomMachine="ivv_masterdata/DecommisionMachine/DecommisionMachine.yml";
	SimplePost sp = new SimplePost();
	SimplePostForAutoGenId simplepost = new SimplePostForAutoGenId();
	PatchWithPathParam patchwithpathparam = new PatchWithPathParam();
	SimplePut simpleput = new SimplePut();
	PutWithPathParam putwithpathparam = new PutWithPathParam();
	SimplePatch simplePatch = new SimplePatch();
	PatchWithPathParamsAndBody patchWithPathParamsAndBody = new PatchWithPathParamsAndBody();
	PatchWithQueryParam patchWithQueryParam=new PatchWithQueryParam();

	public MachineHelper() {
		  super();
		if (ConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	public String createMachineType() throws RigInternalError {
		try {
			Object[] testObjPost = sp.getYmlTestData(CreateMachineType);
			TestCaseDTO testPost = (TestCaseDTO) testObjPost[0];

			String input = testPost.getInput();
			input = JsonPrecondtion.parseAndReturnJsonContent(input, getDateTime(), "code");
			input = JsonPrecondtion.parseAndReturnJsonContent(input, getDateTime(), "name");
			input = JsonPrecondtion.parseAndReturnJsonContent(input, getDateTime(), "description");
			input = JsonPrecondtion.parseAndReturnJsonContent(input,
					BaseTestCase.languageCode,"langCode");
			
			testPost.setInput(input);
			
			String output = testPost.getOutput();
			
			output = JsonPrecondtion.parseAndReturnJsonContent(output,
					BaseTestCase.languageCode,"langCode");
			
			testPost.setOutput(output);

			String code = null;
			sp.test(testPost);
			Response response = sp.response;

			if (response != null) {
				JSONObject jsonResp = new JSONObject(response.getBody().asString());
				logger.info(jsonResp.getJSONObject("response"));
				code = jsonResp.getJSONObject("response").getString("code");
				// if (step.getOutVarName() != null)
				// step.getScenario().getVariables().put(step.getOutVarName(), code);

			}
			logger.info("code -"+ code);
			return code;

		} catch (Exception e) {
			this.hasError=true;
			throw new RigInternalError(e.getMessage());

		}

	}

	public String activateMachineType(String codeType, String activecheck) throws RigInternalError {
		try {
			Boolean activeFlag = false;
			Object[] testObjPost = patchWithQueryParam.getYmlTestData(UpdateMachineTypeStatus);
			TestCaseDTO testPost = (TestCaseDTO) testObjPost[0];
			if (activecheck.contains("t") || activecheck.contains("T"))
				activeFlag = true;

			String input = testPost.getInput();
			input = JsonPrecondtion.parseAndReturnJsonContent(input, codeType, "code");

			input = JsonPrecondtion.parseAndReturnJsonContent(input, activeFlag.toString(), "isActive");
			testPost.setInput(input);

			String status = null;
			patchWithQueryParam.test(testPost);
			Response response = patchWithQueryParam.response;

			if (response != null) {
				JSONObject jsonResp = new JSONObject(response.getBody().asString());
				logger.info(jsonResp.getJSONObject("response"));
				status = jsonResp.getJSONObject("response").getString("status");
				// if (step.getOutVarName() != null)
				// step.getScenario().getVariables().put(step.getOutVarName(), code);

			}logger.info("status -"+ status);
			return status;

		} catch (Exception e) {
			this.hasError=true;
			throw new RigInternalError(e.getMessage());

		}

	}

	public String createMachineSpecification(String machineTypeCode) throws RigInternalError {
		try {
			Object[] testObjPost = simplepost.getYmlTestData(CreateMachineSpecification);
			TestCaseDTO testPost = (TestCaseDTO) testObjPost[0];

			String input = testPost.getInput();
			input = JsonPrecondtion.parseAndReturnJsonContent(input, getDateTime(), "brand");
			input = JsonPrecondtion.parseAndReturnJsonContent(input, getDateTime(), "description");
			input = JsonPrecondtion.parseAndReturnJsonContent(input, getDateTime(), "id");
			input = JsonPrecondtion.parseAndReturnJsonContent(input, machineTypeCode, "machineTypeCode");
			input = JsonPrecondtion.parseAndReturnJsonContent(input, getDateTime(), "minDriverversion");
			input = JsonPrecondtion.parseAndReturnJsonContent(input, getDateTime(), "model");
			input = JsonPrecondtion.parseAndReturnJsonContent(input, getDateTime(), "name");
			input = JsonPrecondtion.parseAndReturnJsonContent(input,
					BaseTestCase.languageCode,"langCode");
			
			testPost.setInput(input);

			String output = testPost.getOutput();
			
			output = JsonPrecondtion.parseAndReturnJsonContent(output,
					BaseTestCase.languageCode,"langCode");
			
			testPost.setOutput(output);
			
			String id = null;
			simplepost.test(testPost);
			Response response = simplepost.response;

			if (response != null) {
				JSONObject jsonResp = new JSONObject(response.getBody().asString());
				logger.info(jsonResp.getJSONObject("response"));
				id = jsonResp.getJSONObject("response").getString("id");
				// if (step.getOutVarName() != null)
				// step.getScenario().getVariables().put(step.getOutVarName(), code);

			}logger.info("id -"+ id);
			return id;

		} catch (Exception e) {
			this.hasError=true;
			throw new RigInternalError(e.getMessage());

		}

	}

	public String activateMachineSpecification(String codeType, String activecheck) throws RigInternalError {
		try {
			Boolean activeFlag = false;
			Object[] testObjPost = patchWithQueryParam.getYmlTestData(UpdateMachineSpecificationStatus);
			TestCaseDTO testPost = (TestCaseDTO) testObjPost[0];
			if (activecheck.contains("t") || activecheck.contains("T"))
				activeFlag = true;

			String input = testPost.getInput();
			input = JsonPrecondtion.parseAndReturnJsonContent(input, codeType, "id");

			input = JsonPrecondtion.parseAndReturnJsonContent(input, activeFlag.toString(), "isActive");
			testPost.setInput(input);

			String status = null;
			patchWithQueryParam.test(testPost);
			Response response = patchWithQueryParam.response;

			if (response != null) {
				JSONObject jsonResp = new JSONObject(response.getBody().asString());
				logger.info(jsonResp.getJSONObject("response"));
				status = jsonResp.getJSONObject("response").getString("status");
				// if (step.getOutVarName() != null)
				// step.getScenario().getVariables().put(step.getOutVarName(), code);

			}logger.info("status -"+ status);
			return status;

		} catch (Exception e) {
			this.hasError=true;
			throw new RigInternalError(e.getMessage());

		}

	}	public HashMap createMachine(String machineSpecId,HashMap<String, String> map,int centerCount) throws RigInternalError {
		try {
			String id =null;
			HashMap<String, String> machineDetailsmap=new HashMap<String, String>();
			Object[] testObjPost=simplepost.getYmlTestData(CreateMachine);
		
			TestCaseDTO testPost=(TestCaseDTO)testObjPost[0];

			String input=testPost.getInput();
			input = JsonPrecondtion.parseAndReturnJsonContent(input,
					machineSpecId, "machineSpecId");
			input = JsonPrecondtion.parseAndReturnJsonContent(input, getDateTime(), "id");
			input = JsonPrecondtion.parseAndReturnJsonContent(input, getDateTime(), "name");
			input = JsonPrecondtion.parseAndReturnJsonContent(input, map.get("centerId"+centerCount), "regCenterId");
			input = JsonPrecondtion.parseAndReturnJsonContent(input,  map.get("zoneCode"), "zoneCode");
			input = JsonPrecondtion.parseAndReturnJsonContent(input,
					BaseTestCase.languageCode,"langCode");
			
			map.put("machineSpecId", machineSpecId);
			testPost.setInput(input);
			
			
			String output = testPost.getOutput();
			
			output = JsonPrecondtion.parseAndReturnJsonContent(output,
					BaseTestCase.languageCode,"langCode");
			
			testPost.setOutput(output);
			
			
				simplepost.test(testPost);
				Response response= simplepost.response;

				if (response!= null)
				{
					JSONObject jsonResp = new JSONObject(response.getBody().asString());
					logger.info( jsonResp.getJSONObject("response"));
					String name = jsonResp.getJSONObject("response").getString("name"); 
					 id = jsonResp.getJSONObject("response").getString("id"); 
					 machineDetailsmap.putAll(map);
					 machineDetailsmap.put("machineid", id);
					 machineDetailsmap.put("machineName", name);
					 machineDetailsmap.put("publicKey",BaseTestCase.publickey.replace("\"","" ));
					 machineDetailsmap.put("signPublicKey",BaseTestCase.publickey.replace("\"","" ));
					
				}logger.info("id -"+ id);
				return machineDetailsmap;
			} catch (Exception e) {
				this.hasError=true;
				throw new RigInternalError(e.getMessage());

			}
		

	}
	

	public String activateMachine(String machineId, String activecheck) throws RigInternalError {
		try {
			String activeFlag = "false";
			Object[] testObjPost = patchWithQueryParam.getYmlTestData(UpdateMachineStatus);
			TestCaseDTO testPost = (TestCaseDTO) testObjPost[0];
			if (activecheck.contains("t") || activecheck.contains("T"))
				activeFlag = "true";

			String input = testPost.getInput();
			input = JsonPrecondtion.parseAndReturnJsonContent(input, machineId, "id");

			input = JsonPrecondtion.parseAndReturnJsonContent(input, activeFlag, "isActive");
			testPost.setInput(input);

			String status = null;
			patchWithQueryParam.test(testPost);
			Response response = patchWithQueryParam.response;

			if (response != null) {
				JSONObject jsonResp = new JSONObject(response.getBody().asString());
				logger.info(jsonResp.getJSONObject("response"));
				status = jsonResp.getJSONObject("response").getString("status");
				
			}logger.info("status -"+ status);
			return status;

		} catch (Exception e) {
			this.hasError=true;
			throw new RigInternalError(e.getMessage());

		}

	}

	public HashMap<String, String>  updateMachine(HashMap<String, String> map,int centerCount)throws RigInternalError {
		try {
			String id =null;
			Object[] testObj=simpleput.getYmlTestData(UpdateMachine);
			TestCaseDTO testdto=(TestCaseDTO)testObj[0];

			String input=testdto.getInput();
			input = JsonPrecondtion.parseAndReturnJsonContent(input,
					BaseTestCase.languageCode,"langCode");
			
			input = JsonPrecondtion.parseAndReturnJsonContent(input,
					map.get("machineSpecId"), "machineSpecId");
			input = JsonPrecondtion.parseAndReturnJsonContent(input, map.get("machineid"), "id");
			input = JsonPrecondtion.parseAndReturnJsonContent(input, map.get("machineName"), "name");
			if(centerCount==0)

				input = JsonPrecondtion.parseAndReturnJsonContent(input, "", "regCenterId");
			else
				input = JsonPrecondtion.parseAndReturnJsonContent(input, map.get("centerId"+centerCount), "regCenterId");
			input = JsonPrecondtion.parseAndReturnJsonContent(input,  map.get("zoneCode"), "zoneCode");
			input = JsonPrecondtion.parseAndReturnJsonContent(input,  map.get("publicKey"), "publicKey");
			input = JsonPrecondtion.parseAndReturnJsonContent(input,  map.get("signPublicKey"), "signPublicKey");
			testdto.setInput(input);
			
			String output = testdto.getOutput();
			
			output = JsonPrecondtion.parseAndReturnJsonContent(output,
					BaseTestCase.languageCode,"langCode");
			
			testdto.setOutput(output);
			
			simpleput.test(testdto);
				Response response= simpleput.response;

				if (response!= null)
				{
					JSONObject jsonResp = new JSONObject(response.getBody().asString());
					logger.info( jsonResp.getJSONObject("response"));
					
				}logger.info("id -"+ id);
				return map;
			} catch (Exception e) {
				this.hasError=true;
				throw new RigInternalError(e.getMessage());

			}
		

	}

	public void dcomMachine(String id) throws RigInternalError {
		try {
			Object[] testObjPutDcom=simpleput.getYmlTestData(DcomMachine);

			TestCaseDTO testPutDcom=(TestCaseDTO)testObjPutDcom[0];
			String endPoint=testPutDcom.getEndPoint();
			endPoint=endPoint.replace("id", id);

			testPutDcom.setEndPoint(endPoint);

			putwithpathparam.test(testPutDcom);
			Response response= putwithpathparam.response;

			if (response!= null)
			{
				JSONObject jsonResp = new JSONObject(response.getBody().asString());
				logger.info( jsonResp.getJSONObject("response"));}

		} catch (Exception e) {
			this.hasError=true;
			throw new RigInternalError(e.getMessage());

		}

	}
	
}
