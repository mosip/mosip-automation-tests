package io.mosip.testrig.dslrig.ivv.e2e.methods;

import java.util.Map;
import javax.cache.Cache;

import org.json.JSONArray;
import org.json.JSONObject;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import io.mosip.testrig.apirig.dto.TestCaseDTO;
import io.mosip.testrig.apirig.masterdata.testscripts.GetWithParam;
import io.mosip.testrig.apirig.testrunner.JsonPrecondtion;
import io.mosip.testrig.apirig.testrunner.BaseTestCase;
import io.mosip.testrig.apirig.utils.OutputValidationUtil;
import io.mosip.testrig.apirig.utils.S3Adapter;
import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testrig.dslrig.ivv.orchestrator.PersonaDataManager;
import io.mosip.testrig.dslrig.ivv.orchestrator.TestRunner;
import io.mosip.testrig.dslrig.ivv.orchestrator.dslConfigManager;
import io.restassured.response.Response;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class WritePersonaData extends BaseTestCaseUtil implements StepInterface {
	private static final Logger logger = Logger.getLogger(WritePersonaData.class);
	private static final String GetIdentityYml = "idaData/RetrieveIdentityByUin/RetrieveIdentityByUin.yml";
	GetWithParam getIdentity = new GetWithParam();

	static {
		if (dslConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	@Override
	public void run() throws RigInternalError {
		String jsonFilePath = TestRunner.getExternalResourcePath() + "/config/personaData.json";
		JSONArray jsonArray = new JSONArray();
		for (Map.Entry<String, Cache<String, Object>> entry : PersonaDataManager.personaDataCollection.entrySet()) {
			Cache<String, Object> personaCache = entry.getValue();
			String uin = (String) personaCache.get("UIN");
			String vid = (String) personaCache.get("VID");

			if (vid != null && !vid.isEmpty() && uin != null && !uin.isEmpty()) {
				JSONObject identityData = null;
				Object[] testObj = getIdentity.getYmlTestData(GetIdentityYml);
				TestCaseDTO test = (TestCaseDTO) testObj[0];
				String input = test.getInput();
				input = JsonPrecondtion.parseAndReturnJsonContent(input, uin, "id");
				test.setInput(input);
				try {
					getIdentity.test(test);
					Response response = getIdentity.response;
					JSONObject responseJson = new JSONObject(response.asString());
					JSONObject responseData = responseJson.getJSONObject("response");
					if (OutputValidationUtil.doesResponseHasErrors(responseJson.toString())) {
						logger.error("Failed to extract Email From UIN");
						this.hasError = true;
						throw new RigInternalError("Failed to extract Email From UIN: " + step.getName());
					}
					identityData = responseData.getJSONObject("identity");

					JSONObject json = new JSONObject();
					json.put("EmailID", identityData.getString("email"));
					json.put("PhoneNumber", identityData.getString("phone"));
					json.put("DateOfBirth", identityData.getString("dateOfBirth"));
					json.put("Gender", identityData.getJSONArray("gender").getJSONObject(0).getString("value"));
					json.put("Address", identityData.getJSONArray("addressLine1").getJSONObject(0).getString("value"));
					json.put("UIN", uin);
					json.put("VID", vid);
					json.put("fullName", identityData.getJSONArray("fullName").getJSONObject(0).getString("value"));
					json.put("ID", personaCache.get("PersonaID"));
					jsonArray.put(json);
					// Write the updated JSON back to the file

				} catch (Exception e) {
					logger.error(e.getMessage());
				}
			} else
				continue;

		}
		writeJSONArrayToFile(jsonArray, jsonFilePath);
		if (dslConfigManager.getPushReportsToS3().equalsIgnoreCase("yes")) {
			File jsonFile = new File(jsonFilePath);
			S3Adapter s3Adapter = new S3Adapter();
			boolean isStoreSuccess = false;
			try {
				isStoreSuccess = s3Adapter.putObject(dslConfigManager.getS3Account(), BaseTestCase.testLevel, null,
						null, "personaData.json", jsonFile);
				logger.info("isStoreSuccess:: " + isStoreSuccess);
			} catch (Exception e) {
				logger.error("error occured while pushing the object" + e.getMessage());
			}
		}
	}

	private static void writeJSONArrayToFile(JSONArray jsonArray, String filePath) {
		try (FileWriter fileWriter = new FileWriter(filePath)) {
			// Convert the JSONArray to a string and write it to the file
			String jsonString = jsonArray.toString();
			fileWriter.write(jsonString);
			logger.info("JSONArray has been written to the file.");
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}

}
