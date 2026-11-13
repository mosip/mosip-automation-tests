package io.mosip.testrig.dslrig.dataprovider.test.registrationclient;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.nio.file.Files;
import java.time.LocalDateTime;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.testrig.dslrig.dataprovider.models.ResidentModel;
import io.mosip.testrig.dslrig.dataprovider.util.CommonUtil;
import io.mosip.testrig.dslrig.dataprovider.util.RestClient;
import io.mosip.testrig.dslrig.dataprovider.variables.VariableManager;
import io.restassured.response.Response;

public class RegistrationSteps {
	private static final Logger logger = LoggerFactory.getLogger(RegistrationSteps.class);
	static LocalDateTime lastSyncTime;
	static String workDirectory;
	static String workPacketDirectory;
	static String defaultTemplateLocation;
	static String templateFolder;
	static {
		try {
			workDirectory = Files.createTempDirectory("prereg").toFile().getAbsolutePath();
			workPacketDirectory = Files.createTempDirectory("pktcreator").toFile().getAbsolutePath();
			templateFolder = VariableManager.getVariableValue("", "packetTemplateLocation").toString().trim();

			File folder = new File(templateFolder);
			if (folder.listFiles() != null) {
				File templateName = folder.listFiles()[0];
				defaultTemplateLocation = templateName.getAbsolutePath();
			}

		} catch (Exception e) {
			logger.error(e.getMessage());
		}

	}

	public String downloadCard(ResidentModel resident, String uin, String contextKey) throws Exception {
		String url = VariableManager.getVariableValue(contextKey, "urlBase").toString().trim()
				+ VariableManager.getVariableValue(contextKey, "residentCredentialAPI").toString().trim() + uin;
		JSONObject wrapperJson = new JSONObject();
		JSONObject reqJson = new JSONObject();

		wrapperJson.put("id", "none");
		wrapperJson.put("requesttime", CommonUtil.getUTCDateTime(null));
		wrapperJson.put("version", "1.0");

		reqJson.put("credentialType", "euin");
		reqJson.put("encrypt", false);
		reqJson.put("encryptionKey", "abc");
		reqJson.put("individualId", uin);
		reqJson.put("issuer", "mpartner-default-print");
		reqJson.put("otp", "111111");
		reqJson.put("recepiant", resident.getContact().getEmailId());
		reqJson.put("transactionID", resident.getId());
		reqJson.put("user", resident.getContact().getEmailId());
		wrapperJson.put("request", reqJson);

		JSONObject apiResponse = RestClient.post(url, wrapperJson, contextKey);

		return apiResponse.toString();

	}

	public JSONObject syncPrereg(String contextKey) throws Exception {

		LocalDateTime currentSyncTime = LocalDateTime.now();
		if (lastSyncTime == null) {
			lastSyncTime = LocalDateTime.now().minusMinutes(10);

		}
		String url = VariableManager.getVariableValue(contextKey, "urlBase").toString().trim()
				+ VariableManager.getVariableValue(contextKey, "preRegSyncURL").toString().trim();

		JSONObject syncRequest = new JSONObject();
		syncRequest.put("registrationCenterId", VariableManager.getVariableValue("centerId", contextKey));
		syncRequest.put("fromDate", CommonUtil.getUTCDateTime(lastSyncTime));
		syncRequest.put("toDate", CommonUtil.getUTCDateTime(currentSyncTime));

		JSONObject wrapper = new JSONObject();

		wrapper.put("version", "1.0");
		wrapper.put("id", "mosip.pre-registration.datasync.fetch.ids");
		wrapper.put("requesttime", CommonUtil.getUTCDateTime(null));
		wrapper.put("request", syncRequest);

		JSONObject preregResponse = RestClient.post(url, wrapper, contextKey);

		lastSyncTime = currentSyncTime;
		return (JSONObject) preregResponse.get("preRegistrationIds");
	}

	public String getRIDStatus(String rid, String contextKey) throws Exception {

		String uri = "resident/v1/rid/check-status";
		String url = VariableManager.getVariableValue(contextKey, "urlBase").toString().trim() + uri;

		JSONObject req = new JSONObject();
		JSONObject reqWrapper = new JSONObject();
		reqWrapper.put("id", "mosip.resident.checkstatus");
		reqWrapper.put("requesttime", CommonUtil.getUTCDateTime(LocalDateTime.now()));
		reqWrapper.put("version", "v1");
		req.put("individualId", rid);
		req.put("individualIdType", "RID"); 
		reqWrapper.put("request", req);

		JSONObject response = RestClient.post(url, reqWrapper, "resident", contextKey);
		return response.get("ridStatus").toString();

	}

	public String getWorkflowStatus(String rid, String contextKey) throws Exception {
		String uri = "registrationprocessor/v1/workflowmanager/workflow/search";
		String url = VariableManager.getVariableValue(contextKey, "urlBase").toString().trim() + uri;

		JSONObject filter = new JSONObject();
		filter.put("columnName", "regId");
		filter.put("value", rid);
		JSONObject sort = new JSONObject();
		sort.put("sortField", "updateDateTime");
		sort.put("sortType", "desc");
		JSONObject pagination = new JSONObject();
		pagination.put("pageStart", 0);
		pagination.put("pageFetch", 10);
		JSONObject request = new JSONObject();
		request.put("filters", new JSONArray().put(filter));
		request.put("sort", sort);
		request.put("pagination", pagination);

		JSONObject wrapper = new JSONObject();
		wrapper.put("id", "mosip.registration.processor.workflow.search");
		wrapper.put("version", "v1");
		wrapper.put("requesttime", CommonUtil.getUTCDateTime(null));
		wrapper.put("request", request);

		JSONObject response = RestClient.post(url, wrapper, "regproc", contextKey);
		if (response == null || !response.has("data") || response.isNull("data")
				|| response.getJSONArray("data").length() == 0) {
			return "processing";
		}
		return response.getJSONArray("data").getJSONObject(0).optString("statusCode", "processing");
	}

	public String getUINByRID(String rid, String contextKey) throws Exception {

		String uri = "idrepository/v1/identity/idvid/" + rid;
		String url = VariableManager.getVariableValue(contextKey, "urlBase").toString().trim() + uri;

		JSONObject response = RestClient.get(url, new JSONObject(), new JSONObject(), contextKey);
		return response.getJSONObject("identity").getString("UIN");

	}

	public void setMDSscore(long port, String type, String qualityScore, String contextKey) {

		try {
			String requestBody = "{\"type\":\"" + type + "\",\"qualityScore\":\"" + qualityScore
					+ "\",\"fromIso\":false}";

			Response response = RestClient.post("http://127.0.0.1:" + port + "/admin/score", requestBody, contextKey);
			;
			logger.info(response.toString());

			assertEquals(200, response.statusCode());

			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode jsonNode = objectMapper.readTree(response.getBody().asString());

			String errorInfo = jsonNode.get("errorInfo").asText();
			assertEquals("Success", errorInfo);
			logger.info("errorInfo: " + errorInfo);
		} catch (Exception e) {
			logger.error("Issue with the Rest Assured MOCKMDS Score Request{}", e);
		}
	}

	public Response getStagesByRID(String rid, String contextKey) throws Exception {

		String uri = VariableManager.getVariableValue(contextKey, "ridStageStatus") + "?rid=" + rid + "&langCode="
				+ VariableManager.getVariableValue(contextKey, "baselang");
		String url = VariableManager.getVariableValue(contextKey, "urlBase").toString().trim() + uri;
		Response response = RestClient.getAdmin(url, new JSONObject(), new JSONObject(), contextKey);

		return response;

	}
}
