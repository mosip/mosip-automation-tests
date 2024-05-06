package io.mosip.testrig.dslrig.ivv.e2e.methods;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;
import io.mosip.testrig.apirig.dto.TestCaseDTO;
import io.mosip.testrig.apirig.testrunner.JsonPrecondtion;
import io.mosip.testrig.apirig.utils.ConfigManager;

import io.mosip.testrig.apirig.utils.AdminTestException;
import io.mosip.testrig.apirig.utils.AuthenticationTestException;
import io.mosip.testrig.apirig.utils.KernelAuthentication;
import io.mosip.testrig.apirig.testscripts.SimplePost;
import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.restassured.response.Response;

public class CheckTags extends BaseTestCaseUtil implements StepInterface {
	private static final Logger logger = Logger.getLogger(CheckTags.class);
	KernelAuthentication kernelAuthLib = new KernelAuthentication();
	private static final String CheckPacketTags = "regproc/GetPacketTagsInfo/GetPacketTagsInfo.yml";
	SimplePost checkPacketTags = new SimplePost();

	static {
		if (ConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	@Override
	public void run() throws RigInternalError {

		String rid = null;
		Response response = null;
		JSONObject jsonObject = null;
		String jsonFromPacketCreator = null;
		if (step.getParameters().size() == 1 && step.getParameters().get(0).startsWith("$$")) {
			rid = step.getParameters().get(0);
			if (rid.startsWith("$$")) {
				rid = step.getScenario().getVariables().get(rid);
			}

			String packetTagsUri = baseUrl + "/packet/getTags";
			response = getRequest(packetTagsUri, "Get tags from context", step);
			if (response != null && response.getStatusCode() == 200) {
				logger.info(response.getBody().asString());
				JSONObject jsonResponse = new JSONObject(response.getBody().asString());
				jsonFromPacketCreator = jsonResponse.toString();
			}

			Object[] testObj = checkPacketTags.getYmlTestData(CheckPacketTags);
			TestCaseDTO test = (TestCaseDTO) testObj[0];
			String input = test.getInput();
			input = JsonPrecondtion.parseAndReturnJsonContent(input, rid, "id");
			test.setInput(input);

			try {
				checkPacketTags.test(test);
			} catch (AuthenticationTestException e) {
				logger.error(e.getMessage());
			} catch (AdminTestException e) {
				logger.error(e.getMessage());
			}

			response = checkPacketTags.response;
			JSONObject responseJson = new JSONObject(response.asString());
			JSONObject tags = responseJson.getJSONObject("response").getJSONObject("tags");
			String jsonFromServer = tags.toString();

			String tagsMismatched = comparePacketTags(jsonFromServer, jsonFromPacketCreator);
			if (tagsMismatched != null && !tagsMismatched.isEmpty()) {
				this.hasError = true;
				throw new RigInternalError("Packet Tags comparison Failed :" + tagsMismatched);
			}

		}

	}

	public static String comparePacketTags(String jsonFromServer, String jsonFromPacketCreator) {
		String tagMismatched = "";

		ObjectMapper objectMapper = new ObjectMapper();
		try {
			JsonNode nodeFromServer = objectMapper.readTree(jsonFromServer);
			JsonNode nodePacketCreator = objectMapper.readTree(jsonFromPacketCreator);

			// Convert JSON nodes to Map for easier comparison
			Map<String, String> mapFromServer = objectMapper.convertValue(nodeFromServer, Map.class);
			Map<String, String> mapPacketCreator = objectMapper.convertValue(nodePacketCreator, Map.class);

			// Compare key-value pairs
			for (Map.Entry<String, String> entry : mapFromServer.entrySet()) {
				String key = entry.getKey();
				String valueFromServer = entry.getValue();
				String valuePacketCreator = mapPacketCreator.get(key);

				if (valuePacketCreator != null && valuePacketCreator.equals(valueFromServer)) {
					logger.info("Key :" + key + "has the same value in both JSONs: " + valueFromServer);
				} else {
					logger.info("Key '" + key + "' has different values in the two JSONs.");
					tagMismatched += "Key :" + key + "   Value from server : " + valueFromServer
							+ "    Value from packet creator : " + valuePacketCreator + " ---- ";
				}
			}
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		return tagMismatched;
	}
}
