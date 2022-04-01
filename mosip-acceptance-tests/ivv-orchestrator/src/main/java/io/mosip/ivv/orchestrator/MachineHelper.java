package io.mosip.ivv.orchestrator;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import io.mosip.admin.fw.util.TestCaseDTO;
import io.mosip.authentication.fw.precon.JsonPrecondtion;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.service.BaseTestCase;
import io.mosip.testscripts.PatchWithPathParam;
import io.mosip.testscripts.PatchWithPathParamsAndBody;
import io.mosip.testscripts.PatchWithQueryParam;
import io.mosip.testscripts.PutWithPathParam;
import io.mosip.testscripts.SimplePatch;
import io.mosip.testscripts.SimplePost;
import io.mosip.testscripts.SimplePostForAutoGenId;
import io.mosip.testscripts.SimplePut;
import io.restassured.response.Response;

/**
 * 
 * @author Neeharika.Garg
 * MachineHelper : Here handled CRUD operations
 * To create machine below are the steps to be followed
 *          String machinetypecode=machineHelper.createMachineType();
			String machinetypestatus=machineHelper.activateMachineType(machinetypecode,activecheck);
				System.out.println(machinetypecode + " " + machinetypestatus);
				String machinespecId=machineHelper.createMachineSpecification(machinetypecode);
			    String machinespecstatus=machineHelper.activateMachineSpecification(machinespecId,activecheck);
				String machineId=machineHelper.createMachine(machinespecId,id);
				String machineStatus=machineHelper.activateMachine(machineId,activecheck);
 */
public class MachineHelper extends BaseTestCaseUtil {
	public Logger logger = Logger.getLogger(MachineHelper.class);
	private final String CreateMachine = "masterdata/Machine/CreateMachine.yml";
	private final String UpdateMachineStatus = "masterdata/UpdateMachineStatus/UpdateMachineStatus.yml";
	
	
	private final String CreateMachineSpecification = "masterdata/MachineSpecification/CreateMachineSpecification.yml";
	private final String CreateMachineType = "masterdata/MachineType/CreateMachineType.yml";
	private final String UpdateMachineTypeStatus = "masterdata/UpdateMachineTypeStatus/UpdateMachineTypeStatus.yml";
	private final String UpdateMachineSpecificationStatus = "masterdata/UpdateMachineSpecificationStatus/UpdateMachineSpecificationStatus.yml";
	SimplePost sp = new SimplePost();
	SimplePostForAutoGenId simplepost = new SimplePostForAutoGenId();
	PatchWithPathParam patchwithpathparam = new PatchWithPathParam();
	SimplePut simpleput = new SimplePut();
	PutWithPathParam putwithpathparam = new PutWithPathParam();
	SimplePatch simplePatch = new SimplePatch();
	PatchWithPathParamsAndBody patchWithPathParamsAndBody = new PatchWithPathParamsAndBody();
	PatchWithQueryParam patchWithQueryParam=new PatchWithQueryParam();


	public String createMachineType() throws RigInternalError {
		try {
			Object[] testObjPost = sp.getYmlTestData(CreateMachineType);
			TestCaseDTO testPost = (TestCaseDTO) testObjPost[0];

			String input = testPost.getInput();
			input = JsonPrecondtion.parseAndReturnJsonContent(input, appendDate, "code");
			input = JsonPrecondtion.parseAndReturnJsonContent(input, appendDate, "name");
			input = JsonPrecondtion.parseAndReturnJsonContent(input, appendDate, "description");
			testPost.setInput(input);

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

			throw new RigInternalError(e.getMessage());

		}

	}

	public String createMachineSpecification(String machineTypeCode) throws RigInternalError {
		try {
			Object[] testObjPost = simplepost.getYmlTestData(CreateMachineSpecification);
			TestCaseDTO testPost = (TestCaseDTO) testObjPost[0];

			String input = testPost.getInput();
			input = JsonPrecondtion.parseAndReturnJsonContent(input, appendDate, "brand");
			input = JsonPrecondtion.parseAndReturnJsonContent(input, appendDate, "description");
			input = JsonPrecondtion.parseAndReturnJsonContent(input, appendDate, "id");
			input = JsonPrecondtion.parseAndReturnJsonContent(input, machineTypeCode, "machineTypeCode");
			input = JsonPrecondtion.parseAndReturnJsonContent(input, appendDate, "minDriverversion");
			input = JsonPrecondtion.parseAndReturnJsonContent(input, appendDate, "model");
			input = JsonPrecondtion.parseAndReturnJsonContent(input, appendDate, "name");
			testPost.setInput(input);

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

			throw new RigInternalError(e.getMessage());

		}

	}

	public String createMachineId(String machineSpecId,String regCenterId) throws RigInternalError {
		try {
			String id =null;
			Object[] testObjPost=simplepost.getYmlTestData(CreateMachine);
		
			TestCaseDTO testPost=(TestCaseDTO)testObjPost[0];

			String input=testPost.getInput();
			input = JsonPrecondtion.parseAndReturnJsonContent(input,
					machineSpecId, "machineSpecId");
			input = JsonPrecondtion.parseAndReturnJsonContent(input, appendDate, "id");
			input = JsonPrecondtion.parseAndReturnJsonContent(input, appendDate, "name");
			input = JsonPrecondtion.parseAndReturnJsonContent(input, regCenterId, "regCenterId");
			testPost.setInput(input);
		
				simplepost.test(testPost);
				Response response= simplepost.response;

				if (response!= null)
				{
					JSONObject jsonResp = new JSONObject(response.getBody().asString());
					logger.info( jsonResp.getJSONObject("response"));
					String name = jsonResp.getJSONObject("response").getString("name"); 
					 id = jsonResp.getJSONObject("response").getString("id"); 
					
				}logger.info("id -"+ id);
				return id;
			} catch (Exception e) {
				throw new RigInternalError(e.getMessage());

			}
		

	}
	
	public HashMap createMachine(String machineSpecId,HashMap<String, String> map) throws RigInternalError {
		try {
			String id =null;
			HashMap<String, String> machineDetailsmap=new LinkedHashMap();
			Object[] testObjPost=simplepost.getYmlTestData(CreateMachine);
		
			TestCaseDTO testPost=(TestCaseDTO)testObjPost[0];

			String input=testPost.getInput();
			input = JsonPrecondtion.parseAndReturnJsonContent(input,
					machineSpecId, "machineSpecId");
			input = JsonPrecondtion.parseAndReturnJsonContent(input, appendDate, "id");
			input = JsonPrecondtion.parseAndReturnJsonContent(input, appendDate, "name");
			input = JsonPrecondtion.parseAndReturnJsonContent(input, map.get("centerId"), "regCenterId");
			input = JsonPrecondtion.parseAndReturnJsonContent(input,  map.get("zoneCode"), "zoneCode");
			
			testPost.setInput(input);
			
				simplepost.test(testPost);
				Response response= simplepost.response;

				if (response!= null)
				{
					JSONObject jsonResp = new JSONObject(response.getBody().asString());
					logger.info( jsonResp.getJSONObject("response"));
					String name = jsonResp.getJSONObject("response").getString("name"); 
					 id = jsonResp.getJSONObject("response").getString("id"); 
					 machineDetailsmap.put("machineid", id);
					 machineDetailsmap.put("machineName", name);
					 machineDetailsmap.put("publicKey",BaseTestCase.publickey.replace("\"","" ));
					 machineDetailsmap.put("signPublicKey",BaseTestCase.publickey.replace("\"","" ));
					 machineDetailsmap.putAll(map);
				}logger.info("id -"+ id);
				return machineDetailsmap;
			} catch (Exception e) {
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

			throw new RigInternalError(e.getMessage());

		}

	}

}
