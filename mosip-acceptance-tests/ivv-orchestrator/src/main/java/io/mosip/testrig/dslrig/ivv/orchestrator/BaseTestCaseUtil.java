package io.mosip.testrig.dslrig.ivv.orchestrator;

import static io.restassured.RestAssured.given;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.ws.rs.core.MediaType;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Reporter;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import io.mosip.testrig.apirig.utils.AdminTestUtil;
import io.mosip.testrig.apirig.dto.TestCaseDTO;
import io.mosip.testrig.apirig.testrunner.JsonPrecondtion;
import io.mosip.testrig.apirig.utils.RestClient;
import io.mosip.testrig.apirig.utils.GlobalConstants;
import io.mosip.testrig.dslrig.ivv.orchestrator.util.SensitiveLogMasker;
import io.mosip.testrig.apirig.utils.KernelAuthentication;
import io.mosip.testrig.apirig.testrunner.BaseTestCase;
import io.mosip.testrig.dslrig.ivv.core.base.BaseStep;
import io.mosip.testrig.dslrig.ivv.core.dtos.Scenario;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.e2e.constant.E2EConstants;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class BaseTestCaseUtil extends BaseStep {
	private static final Logger logger = LoggerFactory.getLogger(BaseTestCaseUtil.class);

	public static Properties props = new AdminTestUtil()
			.getproperty(TestRunner.getExternalResourcePath() + "/config/test-orchestrator_mz.properties");

	public static String baseUrl = dslConfigManager.getpacketUtilityBaseUrl();

	public static final long DEFAULT_WAIT_TIME = 30000l;
	public static final long TIME_IN_MILLISEC = 1000l;


	private static final int INTERNAL_API_LOG_FETCH_CONNECT_MS = 10_000;

	private static final int INTERNAL_API_LOG_FETCH_READ_MS = 30_000;

	/** Prevents orchestrator threads hanging forever on packet-creator / resident/* calls. */
	private static final int PACKET_CREATOR_CONNECT_MS = 15_000;

	private static final int PACKET_CREATOR_SOCKET_MS = 120_000;

	private static final RestAssuredConfig PACKET_CREATOR_HTTP_CONFIG = RestAssuredConfig.config()
			.httpClient(HttpClientConfig.httpClientConfig()
					.setParam("http.connection.timeout", PACKET_CREATOR_CONNECT_MS)
					.setParam("http.socket.timeout", PACKET_CREATOR_SOCKET_MS));

	public static PacketUtility packetUtility = new PacketUtility();
	public static Hashtable<String, Map<String, String>> hashtable = new Hashtable<>();

	public static Map<String, String> sceanrioExecutionStatistics = Collections
			.synchronizedMap(new HashMap<String, String>());


	public static String partnerKeyUrl = null;
	public static String kycPartnerKeyUrl = null;
	public static String partnerId = null;
	public static String kycPartnerId = null;
	public static HashMap<String, HashMap<String, String>> prereqDataSet = new HashMap<String, HashMap<String, String>>();

	/** In-memory key used by {@link io.mosip.testrig.dslrig.ivv.e2e.methods.WritePreReq} / ReadPreReq. */
	public static String prereqStoragePath(String index) {
		return TestRunner.getExternalResourcePath() + "/config/" + BaseTestCase.environment + "_prereqdata_"
				+ index + ".properties";
	}

	public static boolean hasPrereqData(String index) {
		if (index == null || index.isBlank()) {
			return false;
		}
		synchronized (prereqDataSet) {
			return prereqDataSet.containsKey(prereqStoragePath(index));
		}
	}

	public static String prereqDataSummary() {
		synchronized (prereqDataSet) {
			return prereqDataSet.isEmpty() ? "(none)" : String.join(", ", prereqDataSet.keySet());
		}
	}

	public static String extentReportName="";
    public static long exectionStartTime = 0;
    public static long exectionEndTime = 0;
	public static JSONArray regProcActuatorResponseArray = null;
	public static String regProcWaitInterval = "";

	public static String getExtentReportName() {
		return extentReportName;
	}

	public static void setExtentReportName(String emailableReportName) {
		BaseTestCaseUtil.extentReportName = emailableReportName;
	}

	public BaseTestCaseUtil() {
	}

	public String getDateTime() {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MMddHHmmssSSS");
		LocalDateTime now = LocalDateTime.now();
		return "DSL" + dtf.format(now);
	}

	public String getDateTimePrint() {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-mmm-yyyy hh:mm:ss.s");
		LocalDateTime now = LocalDateTime.now();
		return "DSL Time: " + dtf.format(now);
	}

	public String encoder(String text) {
		return Base64.getEncoder().encodeToString(text.getBytes());
	}

	public String readProperty() {
		String preRegistrationId = null;
		FileInputStream inputStrem = null;
		Properties props = new Properties();
		try {
			inputStrem = new FileInputStream(TestResources.getResourcePath() + "preReg/autoGeneratedId.properties");
			props.load(inputStrem);
			preRegistrationId = props.getProperty("CreatePrereg_All_Valid_Smoke_sid_preRegistrationId");
		} catch (IOException e) {
			logger.error(e.getMessage());
		} finally {
			PacketUtility.closeInputStream(inputStrem);
		}
		return preRegistrationId;
	}

	public Object[] filterTestCases(Object[] testCases) {
		String testlable = BaseTestCase.testLevel;
		List<Object> filteredCases = new ArrayList<>();
		if (testlable.equalsIgnoreCase("smoke")) {
			for (Object object : testCases) {
				TestCaseDTO test = (TestCaseDTO) object;
				if (test.getTestCaseName().toLowerCase().contains(testlable.toLowerCase()))
					filteredCases.add(object);
			}
			return filteredCases.toArray();
		}
		return testCases;
	}

	public TestCaseDTO filterOutTestCase(Object[] testCases, String testLabel) {
		TestCaseDTO test = null;
		for (Object object : testCases) {
			test = (TestCaseDTO) object;
			if (test.getTestCaseName().toLowerCase().contains(testLabel.toLowerCase()))
				return test;
		}
		return test;
	}

	protected static String addContextToUrl(String url, Scenario.Step step) {

		String context = buildPacketCreatorContextKey(step.getScenario());

		if (url.contains("?")) {
			String urlArr[] = url.split("\\?");
			return urlArr[0] + "/" + context + "?" + urlArr[1];
		} else if (url.contains("mockmv"))
			return url;
		else
			return url + "/" + context;
	}


	public static String buildPacketCreatorContextKey(Scenario scenario) {
		String scenarioId = scenario.getId();
		return System.getProperty("env.user") + "_S" + scenarioId + "_context";
	}

	private static boolean isPacketCreatorUrl(String url) {
		String pcBase = dslConfigManager.getpacketUtilityBaseUrl();
		return pcBase != null && !pcBase.isEmpty() && url != null && url.startsWith(pcBase);
	}

	private static RequestSpecification givenHttpClient(String url) {
		RequestSpecification spec = given();
		if (isPacketCreatorUrl(url)) {
			spec = spec.config(PACKET_CREATOR_HTTP_CONFIG);
		}
		return spec.relaxedHTTPSValidation().contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON);
	}

	public static void appendOutboundInternalApiLogsAfterPacketCreatorCall(Scenario.Step step, String resolvedUrl) {
		if (!dslConfigManager.isInternalApiLoggingForReport() || step == null || resolvedUrl == null) {
			return;
		}
		String pcBase = dslConfigManager.getpacketUtilityBaseUrl();
		if (pcBase == null || pcBase.isEmpty() || !resolvedUrl.startsWith(pcBase)) {
			return;
		}
		try {
			String contextKey = buildPacketCreatorContextKey(step.getScenario());
			String pathKey = java.net.URLEncoder.encode(contextKey, java.nio.charset.StandardCharsets.UTF_8)
					.replace("+", "%20");
			String logUrl = pcBase + "/context/internalApiLogs/" + pathKey + "?clear=true&reportHints=true";
			RestAssuredConfig timeoutConfig = RestAssuredConfig.config()
					.httpClient(HttpClientConfig.httpClientConfig()
							.setParam("http.connection.timeout", INTERNAL_API_LOG_FETCH_CONNECT_MS)
							.setParam("http.socket.timeout", INTERNAL_API_LOG_FETCH_READ_MS));
			io.restassured.response.Response r = io.restassured.RestAssured.given().config(timeoutConfig)
					.relaxedHTTPSValidation().get(logUrl);
			String body;
			if (r.getStatusCode() != 200) {
				body = "Internal API log fetch failed: HTTP " + r.getStatusCode() + "\nURL: " + logUrl + "\nResponse: "
						+ r.asString();
			} else {
				body = r.getBody().asString();
				if (body == null) {
					body = "";
				}
			}
			if (r.getStatusCode() == 200 && (body.isBlank() || body.contains("no internal API calls recorded"))) {
				return;
			}
			String escaped = org.testng.internal.Utils.escapeHtml(SensitiveLogMasker.mask(body));

			escaped = escaped.replace("[[DSL_B]]", "<b>").replace("[[DSL_E]]", "</b>");
			StringBuilder block = new StringBuilder(512 + escaped.length());
			block.append("<div class=\"dsl-internal-api-log\">");
			block.append("<div class=\"dsl-internal-api-log__title\">");
			block.append("Outbound internal API traffic (Packet Creator / Data Provider) for this call");
			block.append("</div>");
			block.append("<div class=\"dsl-internal-api-log__toolbar\">");
			block.append("Internal outbound calls (request + response per call)");
			block.append("</div>");
			block.append("<div class=\"dsl-internal-api-log__body\">");
			block.append("<pre class=\"dsl-internal-api-log__pre\">");
			block.append(escaped);
			block.append("</pre></div></div>");
			Reporter.log(block.toString(), false);
		} catch (Exception e) {
			logger.warn("Could not attach internal API log after packet creator call for context "
					+ buildPacketCreatorContextKey(step.getScenario()), e);
			Reporter.log("<pre>Internal API log error: " + org.testng.internal.Utils.escapeHtml(e.getMessage())
					+ "</pre>", false);
		}
	}

	public static Response getRequest(String url, String opsToLog, Scenario.Step step) {
		url = addContextToUrl(url, step);
		Response getResponse = null;
		if (dslConfigManager.IsDebugEnabled()) {
			getResponse = givenHttpClient(url).log().all().when().get(url).then().log().all().extract().response();
		} else {
			getResponse = givenHttpClient(url).when().get(url).then().extract().response();
		}
		DslReportLogUtil.reportRequestAndResponse(null, getResponse.getHeaders().asList().toString(), url, null,
				getResponse.getBody().asString(),true);
		appendOutboundInternalApiLogsAfterPacketCreatorCall(step, url);
		return getResponse;
	}

	public static Response getRequestSilent(String url, Scenario.Step step) {
		url = addContextToUrl(url, step);
		Response getResponse = null;

		if (dslConfigManager.IsDebugEnabled()) {
			getResponse = givenHttpClient(url).log().all().when().get(url).then().log().all().extract().response();
		} else {
			getResponse = givenHttpClient(url).when().get(url).then().extract().response();
		}
		return getResponse;
	}

	public static Response getRequestWithQueryParam(String url, HashMap<String, String> contextKey, String opsToLog,
			Scenario.Step step) {
		url = addContextToUrl(url, step);

		Response getResponse = null;
		if (dslConfigManager.IsDebugEnabled()) {
			getResponse = given().relaxedHTTPSValidation().queryParams(contextKey).accept("*/*").log().all().when()
					.get(url).then().log().all().extract().response();
		} else {
			getResponse = given().relaxedHTTPSValidation().queryParams(contextKey).accept("*/*").when().get(url).then()
					.extract().response();
		}

		DslReportLogUtil.reportRequestAndResponse(null, getResponse.getHeaders().asList().toString(), url, null,
				getResponse.getBody().asString());
		appendOutboundInternalApiLogsAfterPacketCreatorCall(step, url);
		return getResponse;
	}

	public Response postRequest(String url, String body, String opsToLog, Scenario.Step step) {
		url = addContextToUrl(url, step);

		Response apiResponse = RestClient.postRequest(url, body, MediaType.APPLICATION_JSON,
				MediaType.APPLICATION_JSON);
		DslReportLogUtil.reportRequestAndResponse(null, apiResponse.getHeaders().asList().toString(), url, body,
				apiResponse.getBody().asString(),true);
		appendOutboundInternalApiLogsAfterPacketCreatorCall(step, url);
		return apiResponse;
	}

	public Response putRequestWithBody(String url, String body, String opsToLog, Scenario.Step step) {
		url = addContextToUrl(url, step);
		Response puttResponse = null;
		if (dslConfigManager.IsDebugEnabled()) {
			puttResponse = given().relaxedHTTPSValidation().body(body).contentType(MediaType.APPLICATION_JSON)
					.accept("*/*").log().all().when().put(url).then().log().all().extract().response();
		} else {
			puttResponse = given().relaxedHTTPSValidation().body(body).contentType(MediaType.APPLICATION_JSON)
					.accept("*/*").when().put(url).then().extract().response();
		}

		DslReportLogUtil.reportRequestAndResponse(null, puttResponse.getHeaders().asList().toString(), url, body,
				puttResponse.asString(),true);
		appendOutboundInternalApiLogsAfterPacketCreatorCall(step, url);

		return puttResponse;
	}

	public Response putRequestWithBody(String url, String body, Scenario.Step step) {
		url = addContextToUrl(url, step);
		Response puttResponse = null;
		if (dslConfigManager.IsDebugEnabled()) {
			puttResponse = given().relaxedHTTPSValidation().body(body).contentType(MediaType.APPLICATION_JSON)
					.accept("*/*").log().all().when().put(url).then().log().all().extract().response();
		} else {
			puttResponse = given().relaxedHTTPSValidation().body(body).contentType(MediaType.APPLICATION_JSON)
					.accept("*/*").when().put(url).then().extract().response();
		}

		DslReportLogUtil.reportRequestAndResponse(null, puttResponse.getHeaders().asList().toString(), url, body,
				puttResponse.getBody().asString());
		appendOutboundInternalApiLogsAfterPacketCreatorCall(step, url);
		return puttResponse;
	}

	public Response putRequest(String url, String opsToLog, Scenario.Step step) {
		url = addContextToUrl(url, step);

		Response putResponse = null;
		if (dslConfigManager.IsDebugEnabled()) {
			putResponse = given().relaxedHTTPSValidation().contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON).log().all().when().put(url).then().log().all().extract()
					.response();
		} else {
			putResponse = given().relaxedHTTPSValidation().contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON).when().put(url).then().extract().response();
		}

		DslReportLogUtil.reportRequestAndResponse(null, putResponse.getHeaders().asList().toString(), url, null,
				putResponse.getBody().asString());
		appendOutboundInternalApiLogsAfterPacketCreatorCall(step, url);
		return putResponse;
	}

	public Response deleteRequest(String url, String opsToLog, Scenario.Step step) {
		url = addContextToUrl(url, step);

		Response deleteResponse = null;
		if (dslConfigManager.IsDebugEnabled()) {
			deleteResponse = given().relaxedHTTPSValidation().contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON).log().all().when().delete(url).then().log().all().extract()
					.response();
		} else {
			deleteResponse = given().relaxedHTTPSValidation().contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON).when().delete(url).then().extract().response();
		}

		DslReportLogUtil.reportRequestAndResponse(null, deleteResponse.getHeaders().asList().toString(), url, null,
				deleteResponse.getBody().asString());
		appendOutboundInternalApiLogsAfterPacketCreatorCall(step, url);

		return deleteResponse;
	}

	public Response deleteRequestWithoutStep(String url, String opsToLog) {

		Response deleteResponse = null;
		if (dslConfigManager.IsDebugEnabled()) {
			deleteResponse = given().relaxedHTTPSValidation().contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON).log().all().when().delete(url).then().log().all().extract()
					.response();
		} else {
			deleteResponse = given().relaxedHTTPSValidation().contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON).when().delete(url).then().extract().response();
		}

		DslReportLogUtil.reportRequestAndResponse(null, deleteResponse.getHeaders().asList().toString(), url, null,
				deleteResponse.getBody().asString());

		return deleteResponse;
	}

	public Response deleteRequestWithQueryParam(String url, HashMap<String, String> map, String opsToLog,
			Scenario.Step step) {
		url = addContextToUrl(url, step);

		Response deleteResponse = null;
		if (dslConfigManager.IsDebugEnabled()) {
			deleteResponse = given().relaxedHTTPSValidation().queryParams(map).accept("*/*").log().all().when()
					.delete(url).then().log().all().extract().response();
		} else {
			deleteResponse = given().relaxedHTTPSValidation().queryParams(map).accept("*/*").when().delete(url).then()
					.extract().response();
		}

		DslReportLogUtil.reportRequestAndResponse(null, deleteResponse.getHeaders().asList().toString(), url, null,
				deleteResponse.getBody().asString());
		appendOutboundInternalApiLogsAfterPacketCreatorCall(step, url);

		return deleteResponse;
	}

	public Response putRequestWithQueryParamAndBody(String url, String body, HashMap<String, String> map,
			String opsToLog, Scenario.Step step) {
		url = addContextToUrl(url, step);
		Response apiResponse = RestClient.putRequestWithQueryParamAndBody(url, body, map, MediaType.APPLICATION_JSON,
				"*/*");
		DslReportLogUtil.reportRequestAndResponse(null, apiResponse.getHeaders().asList().toString(), url, body,
				apiResponse.getBody().asString());
		appendOutboundInternalApiLogsAfterPacketCreatorCall(step, url);
		return apiResponse;
	}

	public Response postRequestWithQueryParamAndBody(String url, String body, HashMap<String, String> map,
			String opsToLog, Scenario.Step step) {
		url = addContextToUrl(url, step);
		Response apiResponse = RestClient.postRequestWithQueryParamAndBody(url, body, map, MediaType.APPLICATION_JSON,
				MediaType.APPLICATION_JSON);

		DslReportLogUtil.reportRequestAndResponse(null, apiResponse.getHeaders().asList().toString(), url, body,
				apiResponse.asString(),true);
		appendOutboundInternalApiLogsAfterPacketCreatorCall(step, url);
		return apiResponse;
	}

	public Response postRequestWithPathParamAndBody(String url, String body, HashMap<String, String> map,
			String opsToLog, Scenario.Step step) {
		url = addContextToUrl(url, step);
		Response apiResponse = null;
		if (dslConfigManager.IsDebugEnabled()) {
			apiResponse = given().contentType(ContentType.JSON).pathParams(map).body(body).log().all().when().post(url)
					.then().log().all().extract().response();
		} else {
			apiResponse = given().contentType(ContentType.JSON).pathParams(map).body(body).when().post(url).then()
					.extract().response();
		}

		DslReportLogUtil.reportRequestAndResponse(null, apiResponse.getHeaders().asList().toString(), url, body,
				apiResponse.getBody().asString());
		appendOutboundInternalApiLogsAfterPacketCreatorCall(step, url);

		return apiResponse;
	}

	public Response postReqestWithCookiesAndBody(String url, String body, String token, String opsToLog,
			Scenario.Step step) {
		Response postResponse = null;
		if (dslConfigManager.IsDebugEnabled()) {
			postResponse = given().relaxedHTTPSValidation().body(body).contentType(MediaType.APPLICATION_JSON)
					.accept("*/*").log().all().when().cookie("Authorization", token).post(url).then().log().all()
					.extract().response();
		} else {
			postResponse = given().relaxedHTTPSValidation().body(body).contentType(MediaType.APPLICATION_JSON)
					.accept("*/*").when().cookie("Authorization", token).post(url).then().extract().response();
		}

		DslReportLogUtil.reportRequestAndResponse(null, postResponse.getHeaders().asList().toString(), url, body,
				postResponse.getBody().asString());
		appendOutboundInternalApiLogsAfterPacketCreatorCall(step, url);

		return postResponse;
	}

	public Response putRequestWithQueryParam(String url, HashMap<String, String> map, String opsToLog,
			Scenario.Step step) {
		url = addContextToUrl(url, step);

		Response puttResponse = null;
		if (dslConfigManager.IsDebugEnabled()) {
			puttResponse = given().queryParams(map).relaxedHTTPSValidation().log().all().when().put(url).then().log()
					.all().extract().response();
		} else {
			puttResponse = given().queryParams(map).relaxedHTTPSValidation().when().put(url).then().extract()
					.response();
		}

		DslReportLogUtil.reportRequestAndResponse(null, puttResponse.getHeaders().asList().toString(), url, null,
				puttResponse.getBody().asString());
		appendOutboundInternalApiLogsAfterPacketCreatorCall(step, url);
		return puttResponse;
	}

	public static Response getRequestWithCookie(String url, String contentHeader, String acceptHeader,
			String cookieName, String cookieValue) {
		logger.info("REST-ASSURED: Sending a GET request to " + url);
		Response getResponse = null;
		if (dslConfigManager.IsDebugEnabled()) {
			getResponse = given().relaxedHTTPSValidation().cookie(cookieName, cookieValue).log().all().when().get(url)
					.then().log().all().extract().response();
		} else {
			getResponse = given().relaxedHTTPSValidation().cookie(cookieName, cookieValue).when().get(url).then()
					.extract().response();
		}

		DslReportLogUtil.reportRequestAndResponse(null, getResponse.getHeaders().asList().toString(), url, null,
				getResponse.asString(),true);
		return getResponse;
	}

	public static Response patchWithQueryParamAndCookie(String url, String jsonInput, String cookieName, String role,
			String testCaseName) {
		AdminTestUtil test = new AdminTestUtil();
		Response response = null;
		String token = "";
		jsonInput = test.inputJsonKeyWordHandeler(jsonInput, testCaseName);
		HashMap<String, String> map = null;
		try {
			map = new Gson().fromJson(jsonInput, new TypeToken<HashMap<String, String>>() {
			}.getType());
		} catch (Exception e) {
			logger.error(
					GlobalConstants.ERROR_STRING_1 + jsonInput + GlobalConstants.EXCEPTION_STRING_1 + e.getMessage());
		}
		KernelAuthentication auth = new KernelAuthentication();
		token = auth.getTokenByRole(role);
		logger.info(GlobalConstants.GET_REQ_STRING + url);
		DslReportLogUtil.reportRequest(null, jsonInput, url);
		try {
			response = RestClient.patchRequestWithCookieAndQueryParm(url, map, MediaType.APPLICATION_JSON,
					MediaType.APPLICATION_JSON, cookieName, token);
			DslReportLogUtil.reportResponse(response.getHeaders().asList().toString(), url, response);
			return response;
		} catch (Exception e) {
			logger.error(GlobalConstants.EXCEPTION_STRING_2 + e.getMessage());
			return response;
		}
	}

	public void constantIntializer() {
		E2EConstants.MACHINE_ID = props.getProperty("machine_id");
		E2EConstants.CENTER_ID = props.getProperty("center_id");
		E2EConstants.USER_ID = props.getProperty("user_id");
		E2EConstants.USER_PASSWD = props.getProperty("user_passwd");
		E2EConstants.SUPERVISOR_ID = props.getProperty("supervisor_id");
		E2EConstants.PRECONFIGURED_OTP = props.getProperty("preconfigured_otp");
	}

	public static String getBioValueFromJson(String filePath) {
		String bioMetricData = null;
		try {
			String jsonObj = new String(Files.readAllBytes(Paths.get(filePath)), "UTF-8");
			bioMetricData = JsonPrecondtion.getValueFromJson(jsonObj, "response.(documents)[0].value");
		} catch (JsonParseException e) {
			logger.error(e.getMessage());
		} catch (JsonMappingException e) {
			logger.error(e.getMessage());
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage());
		} catch (IOException e) {
			logger.error(e.getMessage());
		}

		return bioMetricData;
	}

	public static String getRegprocWaitFromActuator() {
		String url = BaseTestCase.ApplnURI + dslConfigManager.getproperty("actuatorRegprocEndpoint");

		if (regProcWaitInterval != null && !regProcWaitInterval.isEmpty())
			return regProcWaitInterval;

		try {
			if (regProcActuatorResponseArray == null) {
				Response response = null;
				JSONObject responseJson = null;
				response = RestClient.getRequest(url, MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON);
				DslReportLogUtil.reportResponse(response.getHeaders().asList().toString(), url, response);

				responseJson = new JSONObject(response.getBody().asString());
				regProcActuatorResponseArray = responseJson.getJSONArray("propertySources");
			}

			for (int i = 0, size = regProcActuatorResponseArray.length(); i < size; i++) {
				JSONObject eachJson = regProcActuatorResponseArray.getJSONObject(i);
				if (eachJson.get("name").toString().contains("registration-processor-default.properties")) {
					regProcWaitInterval = eachJson.getJSONObject(GlobalConstants.PROPERTIES)
							.getJSONObject("registration.processor.reprocess.minutes").get(GlobalConstants.VALUE)
							.toString();
					break;
				}
			}
			return regProcWaitInterval;
		} catch (Exception e) {
			logger.error(GlobalConstants.EXCEPTION_STRING_2 + e);
			return regProcWaitInterval;
		}
	}

	protected String resolveScenarioVariable(String value) throws RigInternalError {
		return resolveScenarioVariable(step, value);
	}

	protected boolean referencesScenarioVariable(String value) {
		return referencesScenarioVariable(step, value);
	}

	protected static boolean referencesScenarioVariable(Scenario.Step step, String value) {
		if (org.apache.commons.lang.StringUtils.isBlank(value)) {
			return false;
		}
		String normalized = value.trim().replaceAll("/\\*[^*]*\\*/", "").trim();
		if (normalized.startsWith("$$")) {
			return true;
		}
		return step.getScenario().getVariables().containsKey("$$" + toDslVariableName(normalized));
	}

	protected static String resolveScenarioVariable(Scenario.Step step, String value) throws RigInternalError {
		if (org.apache.commons.lang.StringUtils.isBlank(value)) {
			return value;
		}
		String normalized = value.trim().replaceAll("/\\*[^*]*\\*/", "").trim();
		if (normalized.startsWith("$$")) {
			String resolved = step.getScenario().getVariables().get(normalized);
			if (resolved != null && !resolved.isBlank()) {
				return resolved;
			}
			throw new RigInternalError("Scenario variable is not set or empty: " + normalized);
		}
		String variableKey = "$$" + toDslVariableName(normalized);
		if (step.getScenario().getVariables().containsKey(variableKey)) {
			String resolved = step.getScenario().getVariables().get(variableKey);
			if (resolved != null && !resolved.isBlank()) {
				return resolved;
			}
			throw new RigInternalError("Scenario variable is not set or empty: " + variableKey);
		}
		return normalized;
	}

	/**
	 * Resolves persona and packet template path parameters and fails fast when a Gherkin
	 * shorthand (e.g. {@code template}) was not translated to a {@code $$} variable in DSL.
	 */
	protected static String[] resolvePersonaAndTemplatePaths(Scenario.Step step) throws RigInternalError {
		String residentParam = step.getParameters().get(0);
		String templateParam = step.getParameters().get(1);
		String residentPath = resolveScenarioVariable(step, residentParam);
		String templatePath = resolveScenarioVariable(step, templateParam);
		assertResolvedScenarioFilePath(step, residentParam, residentPath, "Persona file path");
		assertResolvedScenarioFilePath(step, templateParam, templatePath, "Packet template path");
		return new String[] { residentPath, templatePath };
	}

	protected static void assertResolvedScenarioFilePath(Scenario.Step step, String param, String resolved,
			String label) throws RigInternalError {
		String normalized = param == null ? "" : param.trim().replaceAll("/\\*[^*]*\\*/", "").trim();
		if (resolved == null || resolved.isBlank()) {
			throw new RigInternalError(label + " is not set or empty: " + normalized);
		}
		if (!normalized.startsWith("$$") && resolved.equals(normalized)) {
			String variableKey = "$$" + toDslVariableName(normalized);
			if (step.getScenario().getVariables().containsKey(variableKey)) {
				throw new RigInternalError(label + " must reference scenario variable " + variableKey
						+ " in DSL (got unresolved literal [" + normalized + "])");
			}
			throw new RigInternalError(label + " is not a valid file path; unresolved reference [" + normalized
					+ "]. Use 'the saved packet template path' or $$templatePath in Gherkin.");
		}
		if (!resolved.contains("/") && !resolved.contains("\\")) {
			throw new RigInternalError(label + " is not a valid file path: [" + resolved + "]");
		}
	}

	private static String toDslVariableName(String displayName) {
		String[] words = displayName.trim().split("\\s+");
		if (words.length == 0) {
			return displayName;
		}
		StringBuilder sb = new StringBuilder(words[0].toLowerCase(Locale.ROOT));
		for (int i = 1; i < words.length; i++) {
			String w = words[i];
			if (!w.isEmpty()) {
				sb.append(Character.toUpperCase(w.charAt(0)));
				if (w.length() > 1) {
					sb.append(w.substring(1).toLowerCase(Locale.ROOT));
				}
			}
		}
		return sb.toString();
	}

	/** Fail fast when MOSIP returns {@code errors} or a null {@code response} body. */
	protected static void assertMosipResponseOk(JSONObject jsonResp, String operation) throws RigInternalError {
		if (jsonResp == null) {
			throw new RigInternalError(operation + " failed: empty response body");
		}
		if (jsonResp.has("errors") && !jsonResp.isNull("errors")) {
			Object errors = jsonResp.get("errors");
			if (errors instanceof JSONArray && ((JSONArray) errors).length() > 0) {
				throw new RigInternalError(operation + " failed: " + errors);
			}
			if (!(errors instanceof JSONArray)) {
				throw new RigInternalError(operation + " failed: response has errors");
			}
		}
		if (!jsonResp.has("response") || jsonResp.isNull("response")) {
			throw new RigInternalError(operation + " failed: response object is null");
		}
	}

}
