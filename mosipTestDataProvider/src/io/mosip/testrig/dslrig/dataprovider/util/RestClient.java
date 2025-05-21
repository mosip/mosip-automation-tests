package io.mosip.testrig.dslrig.dataprovider.util;

import static io.restassured.RestAssured.given;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
//import org.json.JSONArray;
import org.json.JSONObject;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;

import io.mosip.testrig.dslrig.dataprovider.mds.HttpRCapture;
import io.mosip.testrig.dslrig.dataprovider.variables.VariableManager;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Cookie;
import io.restassured.http.Header;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class RestClient {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(RestClient.class);

	static String dataKey = "response";
	static String errorKey = "errors";
	static Map<String, String> tokens = new HashMap<String, String>();
	static String refreshToken;

	// String constants
	private static final String URLBASE = "urlBase";
	private static final String ADMIN = "admin";
	private static final String REGPROC = "regproc";
	private static final String CRVS = "crvs";
	private static final String AUTHORIZATION = "Authorization";
	private static final String SYSTEM = "system";
	private static final String PREREG = "prereg";
	private static final String SET_COOKIE = "Set-Cookie";
	private static final String RESIDENT = "resident";
	private static final String ERRORCODE = "errorCode";
	private static final String USERNAME = "userName";
	private static final String PASSWORD = "password";
	private static final String APPID = "appId";
	private static final String CLIENTID = "clientId";
	private static final String METADATA = "metadata";
	private static final String VERSION = "version";
	private static final String REQUESTTIME = "requesttime";
	private static final String REQUEST = "request";
	private static final String AUTHURL = " Auth URL";
	private static final String POST2SLACK = "post2slack";

	static {
		// RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());

	}
	String _urlBase;

	int http_status;
	Properties headers;

	public static Boolean isValidToken(String role, String contextKey) {

		Object obj = VariableManager.getVariableValue(contextKey, "urlSwitched");
		if (obj != null) {
			Boolean bClear = Boolean.valueOf(obj.toString());
			if (bClear)
				return false;
		}

		Object urlBase = VariableManager.getVariableValue(contextKey, URLBASE);

		if (urlBase != null) {
			String token = tokens.get(urlBase.toString().trim() + role);
			return isValidTokenOffline(token, contextKey);
		} else {
			return false;
		}
	}

	public static boolean isValidTokenOffline(String cookie, String contextKey) {
		boolean bReturn = false;
		if (cookie == null)
			return bReturn;
		try {
			DecodedJWT decodedJWT = JWT.decode(cookie);
			long expirationTime = decodedJWT.getExpiresAt().getTime();
			if (expirationTime < System.currentTimeMillis()) {
				logInfo(contextKey, "The token is expired");
			} else {
				bReturn = true;
				logInfo(contextKey, "The token is not expired");
			}
		} catch (JWTDecodeException e) {
			logger.error("The token is invalid");
		}
		return bReturn;
	}

	public static void clearToken() {
		tokens.clear();
		refreshToken = null;
	}

	public int status() {
		return http_status;
	}

	public RestClient(String urlBase) {
		_urlBase = urlBase;
		headers = new Properties();
	}

	public RestClient() {
		headers = new Properties();
	}

	public static String constructQueryParam(Properties queryParam) throws UnsupportedEncodingException {

		StringBuilder builder = new StringBuilder();

		if (queryParam != null) {
			builder.append("?");
			queryParam.forEach((k, v) -> {
				builder.append(k + "=" + v).append("&");
			});
		}
		if (builder.length() > 2 && builder.substring(builder.length() - 2, builder.length() - 1).equals("&")) {
			builder.replace(builder.length() - 2, builder.length() - 1, "");
		}
		return URLEncoder.encode(builder.toString().trim(), "UTF-8");
	}

	public void addHeader(String header, String value) {
		headers.put(header, value);
	}

	// method used with admin role
	public static Response getAdmin(String url, JSONObject requestParams, JSONObject pathParam, String contextKey)
			throws Exception {

		String role = ADMIN;
		if (!isValidToken(role, contextKey)) {
			initToken_admin(contextKey);

		}
		boolean bDone = false;
		int nLoop = 0;
		Response response = null;

		try {
			while (!bDone) {

				String token = tokens
						.get(VariableManager.getVariableValue(contextKey, URLBASE).toString().trim() + role);
				Cookie kukki = new Cookie.Builder(AUTHORIZATION, token).build();
				Map<String, Object> mapParam = requestParams == null ? null : requestParams.toMap();
				Map<String, Object> mapPathParam = pathParam == null ? null : pathParam.toMap();

				if (isDebugEnabled(contextKey)) {
					response = given().log().all().cookie(kukki).contentType(ContentType.JSON).queryParams(mapParam)
							.get(url, mapPathParam).then().log().all().extract().response();
				} else {
					response = given().cookie(kukki).contentType(ContentType.JSON).queryParams(mapParam).get(url,
							mapPathParam);
				}
				if (response.getStatusCode() == 401) {
					if (nLoop >= 1)
						bDone = true;
					else {
						initToken(contextKey);
						nLoop++;
					}
				} else
					bDone = true;
			}

			if (isDebugEnabled(contextKey) && response != null) {
				logInfo(contextKey, response.getBody().asString());

			}
			checkErrorResponse(response.getBody().asString());

		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return response;
	}

	public static boolean isDebugEnabled(String contextKey) {
		boolean debugEnabled = false;

		String enableDebugflag = VariableManager.getVariableValue(contextKey, "enableDebug").toString();
		if (enableDebugflag != null && !enableDebugflag.isEmpty())
			debugEnabled = enableDebugflag.equalsIgnoreCase("yes");
		return debugEnabled;
	}

	public static void logInfo(String contextKey, String message) {
		if (isDebugEnabled(contextKey))
			logger.info(message);
	}

	// method used with admin role
	public static JSONObject getAdminPreReg(String url, JSONObject requestParams, JSONObject pathParam,
			String contextKey) throws Exception {

		String role = ADMIN;
		if (!isValidToken(role, contextKey)) {
			initToken_admin(contextKey);

		}
		boolean bDone = false;
		int nLoop = 0;
		Response response = null;

		try {
			while (!bDone) {

				String token = tokens
						.get(VariableManager.getVariableValue(contextKey, URLBASE).toString().trim() + role);
				Cookie kukki = new Cookie.Builder(AUTHORIZATION, token).build();
				Map<String, Object> mapParam = requestParams == null ? null : requestParams.toMap();
				Map<String, Object> mapPathParam = pathParam == null ? null : pathParam.toMap();

				if (isDebugEnabled(contextKey)) {
					response = given().log().all().cookie(kukki).contentType(ContentType.JSON).queryParams(mapParam)
							.get(url, mapPathParam).then().log().all().extract().response();
				} else {
					response = given().cookie(kukki).contentType(ContentType.JSON).queryParams(mapParam).get(url,
							mapPathParam);
				}

				if (response.getStatusCode() == 401) {
					if (nLoop >= 1)
						bDone = true;
					else {
						initToken(contextKey);
						nLoop++;
					}
				} else
					bDone = true;
			}

			if (response != null) {
				if (isDebugEnabled(contextKey)) {
					logInfo(contextKey, response.getBody().asString());
				}
				checkErrorResponse(response.getBody().asString());
			}

		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return new JSONObject(response.getBody().asString()).getJSONObject(dataKey);
	}

	// method used with system role
	public static JSONObject get(String url, JSONObject requestParams, JSONObject pathParam, String contextKey)
			throws Exception {

		String role = SYSTEM;
		if (!isValidToken(role, contextKey)) {
			initToken(contextKey);

		}
		boolean bDone = false;
		int nLoop = 0;
		Response response = null;

		try {
			while (!bDone) {

				String token = tokens
						.get(VariableManager.getVariableValue(contextKey, URLBASE).toString().trim() + role);
				Cookie kukki = new Cookie.Builder(AUTHORIZATION, token).build();
				Map<String, Object> mapParam = requestParams == null ? null : requestParams.toMap();
				Map<String, Object> mapPathParam = pathParam == null ? null : pathParam.toMap();

				if (isDebugEnabled(contextKey)) {
					response = given().log().all().cookie(kukki).contentType(ContentType.JSON).queryParams(mapParam)
							.get(url, mapPathParam).then().log().all().extract().response();
				} else {
					response = given().cookie(kukki).contentType(ContentType.JSON).queryParams(mapParam).get(url,
							mapPathParam);
				}

				if (response.getStatusCode() == 401) {
					if (nLoop >= 1)
						bDone = true;
					else {
						initToken(contextKey);
						nLoop++;
					}
				} else
					bDone = true;
			}

			if (isDebugEnabled(contextKey) && response != null) {
				logInfo(contextKey, response.getBody().asString());
				checkErrorResponse(response.getBody().asString());
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return new JSONObject(response.getBody().asString()).getJSONObject(dataKey);
	}
	
	public static Response getWithoutParams(String url, String cookie) {

		Cookie.Builder builder = new Cookie.Builder("Authorization", cookie);
		Response getResponse;
			getResponse = given().cookie(builder.build()).relaxedHTTPSValidation().log().all().when().get(url);
		return getResponse;
	}

	// method used with system role
	public static JSONArray getDoc(String url, JSONObject requestParams, JSONObject pathParam, String contextKey)
			throws Exception {

		String role = SYSTEM;
		if (!isValidToken(role, contextKey)) {
			initToken(contextKey);

		}
		boolean bDone = false;
		int nLoop = 0;
		Response response = null;

		while (!bDone) {

			String token = tokens.get(VariableManager.getVariableValue(contextKey, URLBASE).toString().trim() + role);

			Cookie kukki = new Cookie.Builder(AUTHORIZATION, token).build();
			Map<String, Object> mapParam = requestParams == null ? null : requestParams.toMap();
			Map<String, Object> mapPathParam = pathParam == null ? null : pathParam.toMap();

			if (isDebugEnabled(contextKey)) {
				response = given().log().all().cookie(kukki).contentType(ContentType.JSON).queryParams(mapParam)
						.get(url, mapPathParam).then().log().all().extract().response();
			} else {
				response = given().cookie(kukki).contentType(ContentType.JSON).queryParams(mapParam).get(url,
						mapPathParam);
			}

			if (response.getStatusCode() == 401) {
				if (nLoop >= 1)
					bDone = true;
				else {
					initToken(contextKey);
					nLoop++;
				}
			} else
				bDone = true;
		}

		if (isDebugEnabled(contextKey) && response != null) {
			logInfo(contextKey, response.getBody().asString());

		}
		checkErrorResponse(response.getBody().asString());

		return new JSONObject(response.getBody().asString()).getJSONArray(dataKey);
	}

	public static JSONObject getNoAuth(String url, JSONObject requestParams, JSONObject pathParam, String contextKey)
			throws Exception {
		String role = PREREG;
		if (!isValidToken(role, contextKey)) {
			initPreregToken(url, requestParams, contextKey);
		}
		String token = tokens.get(VariableManager.getVariableValue(contextKey, URLBASE).toString().trim() + role);

		Response response = null;

		Cookie kukki = new Cookie.Builder(AUTHORIZATION, token).build();
		Map<String, Object> mapParam = requestParams == null ? null : requestParams.toMap();

		Map<String, Object> mapPathParam = pathParam == null ? null : pathParam.toMap();

		if (isDebugEnabled(contextKey)) {
			response = given().log().all().cookie(kukki).contentType(ContentType.JSON).queryParams(mapParam)
					.get(url, mapPathParam).then().log().all().extract().response();
		} else {
			response = given().cookie(kukki).contentType(ContentType.JSON).queryParams(mapParam).get(url, mapPathParam);
		}

		if (isDebugEnabled(contextKey) && response != null) {
			logInfo(contextKey, response.getBody().asString());
		}

		if (response != null) {
			checkErrorResponse(response.getBody().asString());
		}

		return new JSONObject(response.getBody().asString()).getJSONObject(dataKey);
	}

	public static JSONObject uploadFile(String url, String filePath, JSONObject requestData, String contextKey)
			throws Exception {
		String role = PREREG;

		if (!isValidToken(role, contextKey)) {
			if (role.equalsIgnoreCase(ADMIN)) {
				initToken_admin(contextKey);
			} else if (role.equalsIgnoreCase(PREREG)) {
			} else {
				initToken(contextKey);
			}

		}

		Response response = null;

		String token = tokens.get(VariableManager.getVariableValue(contextKey, URLBASE).toString().trim() + role);

		Cookie kukki = new Cookie.Builder(AUTHORIZATION, token).build();

		if (requestData != null) {
			if (isDebugEnabled(contextKey))
				response = given().log().all().cookie(kukki).multiPart("file", new File(filePath))
						.param("Document request", requestData.toString()).post(url).then().log().all().extract()
						.response();
			else
				response = given().cookie(kukki).multiPart("file", new File(filePath))
						.param("Document request", requestData.toString()).post(url);
		} else {
			if (isDebugEnabled(contextKey))
				response = given().log().all().cookie(kukki).multiPart("file", new File(filePath)).post(url).then()
						.log().all().extract().response();
			else
				response = given().cookie(kukki).multiPart("file", new File(filePath)).post(url);
		}

		checkErrorResponse(response.getBody().asString());
		return new JSONObject(response.getBody().asString()).getJSONObject(dataKey);
	}

	public static JSONObject uploadFiles(String url, List<String> filePaths, JSONObject requestData, String contextKey)
			throws Exception {
		String role = ADMIN;

		if (!isValidToken(role, contextKey)) {
			if (role.equalsIgnoreCase(ADMIN)) {
				initToken_admin(contextKey);
			} else if (role.equalsIgnoreCase(PREREG)) {
			} else {
				initToken(contextKey);
			}

		}

		Response response = null;
		RequestSpecification spec = null;
		String token = tokens.get(VariableManager.getVariableValue(contextKey, URLBASE).toString().trim() + role);

		Cookie kukki = new Cookie.Builder(AUTHORIZATION, token).build();

		if (isDebugEnabled(contextKey))
			spec = given().log().all().cookie(kukki);
		else
			spec = given().cookie(kukki);
		for (String fName : filePaths)
			spec = spec.multiPart("files", new File(fName));
		if (requestData != null) {
			Iterator<String> paramKeys = requestData.keys();
			while (paramKeys.hasNext()) {
				String key = paramKeys.next();
				spec = spec.formParam(key, requestData.get(key));

			}
		}

		if (isDebugEnabled(contextKey))
			response = spec.post(url).then().log().all().extract().response();
		else
			response = spec.post(url);

		checkErrorResponse(response.getBody().asString());
		return new JSONObject(response.getBody().asString()).getJSONObject(dataKey);
	}

	public static JSONObject postNoAuth(String url, JSONObject jsonRequest, String contextKey) throws Exception {
		return postNoAuth(url, jsonRequest, ADMIN, contextKey);
	}

	public static JSONObject postNoAuthvalidate(String url, JSONObject jsonRequest, String role, String contextKey)
			throws Exception {

		if (!isValidToken(role, contextKey)) {
			if (role.equalsIgnoreCase(ADMIN)) {
				initToken_admin(contextKey);
			} else if (role.equalsIgnoreCase(PREREG)) {
			} else {
				initToken(contextKey);
			}

		}

		String token = tokens.get(VariableManager.getVariableValue(contextKey, URLBASE).toString().trim() + role);

		Response response = null;
		logInfo(contextKey, "Request: " + jsonRequest.toString());
		try {
			if (isDebugEnabled(contextKey))
				response = given().log().all().contentType(ContentType.JSON).body(jsonRequest.toString()).post(url)
						.then().log().all().extract().response();
			else
				response = given().contentType(ContentType.JSON).body(jsonRequest.toString()).post(url);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		if (response != null) {
			logInfo(contextKey, "Response: " + response.getBody().asString());
		} else {
			logInfo(contextKey, "Response: null");
		}

		for (Header h : response.getHeaders()) {
			logInfo(contextKey, h.getName() + "=" + h.getValue());
		}
		String cookie = response.getHeader(SET_COOKIE);
		if (cookie != null) {
			token = cookie.split("=")[1];
			token = token.split(";")[0];
			tokens.put(VariableManager.getVariableValue(contextKey, URLBASE).toString().trim() + role, token);
		}
		logInfo(contextKey, token);
		checkErrorResponse(response.getBody().asString());

		return new JSONObject(response.getBody().asString()).getJSONObject(dataKey);
	}

	public static JSONObject postNoAuth(String url, JSONObject jsonRequest, String role, String contextKey)
			throws Exception {

		if (!isValidToken(role, contextKey)) {
			if (role.equalsIgnoreCase(ADMIN)) {
				initToken_admin(contextKey);
			} else if (role.equalsIgnoreCase(PREREG)) {
			} else {
				initToken(contextKey);
			}

		}

		String token = tokens.get(VariableManager.getVariableValue(contextKey, URLBASE).toString().trim() + role);

		Response response = null;
		logInfo(contextKey, "Request: " + jsonRequest.toString());
		try {
			Cookie kukki = new Cookie.Builder(AUTHORIZATION, token).build();
			if (isDebugEnabled(contextKey))
				response = given().log().all().cookie(kukki).contentType(ContentType.JSON).body(jsonRequest.toString())
						.post(url).then().log().all().extract().response();
			else
				response = given().cookie(kukki).contentType(ContentType.JSON).body(jsonRequest.toString()).post(url);

		} catch (Exception e) {
			logger.error(e.getMessage());
		}

		if (isDebugEnabled(contextKey)) {
			if (response != null) {
				logInfo(contextKey, "Response: " + response.getBody().asString());
			} else {
				logInfo(contextKey, "Response: null");
			}
		}

		for (Header h : response.getHeaders()) {
			logInfo(contextKey, h.getName() + "=" + h.getValue());
		}
		String cookie = response.getHeader(SET_COOKIE);
		if (cookie != null) {
			token = cookie.split("=")[1];

			tokens.put(VariableManager.getVariableValue(contextKey, URLBASE).toString().trim() + role, token);
		}
		logInfo(contextKey, token);

		checkErrorResponse(response.getBody().asString());

		return new JSONObject(response.getBody().asString()).getJSONObject(dataKey);
	}

	public static JSONObject putNoAuth(String url, JSONObject jsonRequest, String contextKey) throws Exception {
		return postNoAuth(url, jsonRequest, SYSTEM, contextKey);
	}

	public static JSONObject putNoAuth(String url, JSONObject jsonRequest, String role, String contextKey)
			throws Exception {

		if (!isValidToken(role, contextKey)) {
			if (role.equalsIgnoreCase(ADMIN)) {
				initToken_admin(contextKey);
			} else if (role.equalsIgnoreCase(PREREG)) {
			} else {
				initToken(contextKey);
			}

		}
		String token = tokens.get(VariableManager.getVariableValue(contextKey, URLBASE).toString().trim() + role);

		Response response = null;
		logInfo(contextKey, "Request:" + jsonRequest.toString());
		Cookie kukki = new Cookie.Builder(AUTHORIZATION, token).build();

		if (isDebugEnabled(contextKey))
			response = given().log().all().cookie(kukki).contentType(ContentType.JSON).body(jsonRequest.toString())
					.put(url).then().log().all().extract().response();
		else
			response = given().cookie(kukki).contentType(ContentType.JSON).body(jsonRequest.toString()).put(url);

		String cookie = response.getHeader(SET_COOKIE);
		if (cookie != null) {
			token = cookie.split("=")[1];
			tokens.put(VariableManager.getVariableValue(contextKey, URLBASE).toString().trim() + role, token);
		}
		logInfo(contextKey, token);
		logInfo(contextKey, "Response:" + response.getBody().asString());
		checkErrorResponse(response.getBody().asString());

		return new JSONObject(response.getBody().asString());
	}

	public static JSONObject deleteNoAuth(String url, JSONObject jsonRequest, String contextKey) throws Exception {

		String role = ADMIN;
		if (!isValidToken(role, contextKey)) {
			initToken_admin(contextKey);
		}
		String token = tokens.get(VariableManager.getVariableValue(contextKey, URLBASE).toString().trim() + role);

		Response response = null;

		logInfo(contextKey, "Request:" + jsonRequest.toString());
		Cookie kukki = new Cookie.Builder(AUTHORIZATION, token).build();

		if (isDebugEnabled(contextKey))
			response = given().log().all().cookie(kukki).contentType(ContentType.JSON).body(jsonRequest.toString())
					.delete(url).then().log().all().extract().response();
		else
			response = given().cookie(kukki).contentType(ContentType.JSON).body(jsonRequest.toString()).delete(url);

		String cookie = response.getHeader(SET_COOKIE);
		if (cookie != null) {
			token = cookie.split("=")[1];
			tokens.put(VariableManager.getVariableValue(contextKey, URLBASE).toString().trim() + role, token);
		}
		logInfo(contextKey, token);
		logInfo(contextKey, "Response:" + response.getBody().asString());
		checkErrorResponse(response.getBody().asString());

		return new JSONObject(response.getBody().asString()).getJSONObject(dataKey);
	}

	public static JSONObject delete(String url, JSONObject jsonRequest, String contextKey) throws Exception {

		String role = RESIDENT;
		if (!isValidToken(role, contextKey)) {
			initToken_Resident(contextKey);
		}
		String token = tokens.get(VariableManager.getVariableValue(contextKey, URLBASE).toString().trim() + role);

		Response response = null;

		logInfo(contextKey, "Request:" + jsonRequest.toString());
		Cookie kukki = new Cookie.Builder(AUTHORIZATION, token).build();
		if (isDebugEnabled(contextKey))
			response = given().log().all().cookie(kukki).contentType(ContentType.JSON).body(jsonRequest.toString())
					.delete(url).then().log().all().extract().response();
		else
			response = given().cookie(kukki).contentType(ContentType.JSON).body(jsonRequest.toString()).delete(url);
		String cookie = response.getHeader(SET_COOKIE);
		if (cookie != null) {
			token = cookie.split("=")[1];
			tokens.put(VariableManager.getVariableValue(contextKey, URLBASE).toString().trim() + role, token);
		}
		logInfo(contextKey, token);
		logInfo(contextKey, "Response:" + response.getBody().asString());

		return new JSONObject(response.getBody().asString());
	}

	public static String deleteExpectation(String url, JSONObject jsonRequest, String contextKey) throws Exception {

		String role = RESIDENT;
		if (!isValidToken(role, contextKey)) {
			initToken_Resident(contextKey);
		}
		String token = tokens.get(VariableManager.getVariableValue(contextKey, URLBASE).toString().trim() + role);

		Response response = null;

		logInfo(contextKey, "Request:" + jsonRequest.toString());
		Cookie kukki = new Cookie.Builder(AUTHORIZATION, token).build();
		if (isDebugEnabled(contextKey))
			response = given().log().all().cookie(kukki).contentType(ContentType.JSON).body(jsonRequest.toString())
					.delete(url).then().log().all().extract().response();
		else
			response = given().cookie(kukki).contentType(ContentType.JSON).body(jsonRequest.toString()).delete(url);
		String cookie = response.getHeader(SET_COOKIE);
		if (cookie != null) {
			token = cookie.split("=")[1];
			tokens.put(VariableManager.getVariableValue(contextKey, URLBASE).toString().trim() + role, token);
		}
		logInfo(contextKey, token);
		logInfo(contextKey, "Response:" + response.getBody().asString());

		return response.getBody().asString();
	}

	public static JSONObject deleteNoAuthWithQueryParam(String url, JSONObject jsonRequest, String contextKey)
			throws Exception {

		String role = RESIDENT;
		if (!isValidToken(role, contextKey)) {
			initToken_Resident(contextKey);
		}
		String token = tokens.get(VariableManager.getVariableValue(contextKey, URLBASE).toString().trim() + role);

		Response response = null;

		logInfo(contextKey, "Request:" + jsonRequest.toString());
		Cookie kukki = new Cookie.Builder(AUTHORIZATION, token).build();

		if (isDebugEnabled(contextKey))
			response = given().log().all().cookie(kukki).contentType(ContentType.JSON).queryParams(jsonRequest.toMap())
					.delete(url).then().log().all().extract().response();
		else
			response = given().cookie(kukki).contentType(ContentType.JSON).queryParams(jsonRequest.toMap()).delete(url);

		String cookie = response.getHeader(SET_COOKIE);
		if (cookie != null) {
			token = cookie.split("=")[1];
			tokens.put(VariableManager.getVariableValue(contextKey, URLBASE).toString().trim() + role, token);
		}
		logInfo(contextKey, token);
		logInfo(contextKey, "Response:" + response.getBody().asString());
		checkErrorResponse(response.getBody().asString());

		return new JSONObject(response.getBody().asString()).getJSONObject(dataKey);
	}

	public static JSONObject post(String url, JSONObject jsonRequest, String contextKey) throws Exception {
		return post(url, jsonRequest, SYSTEM, contextKey);
	}

	public static Response post(String url, String requestBody, String contextKey) throws Exception {
		Response response = null;
		if (isDebugEnabled(contextKey))
			response = RestAssured.given().log().all().baseUri(url).contentType(ContentType.JSON).and()
					.body(requestBody).when().post().then().log().all().extract().response();
		else
			response = RestAssured.given().baseUri(url).contentType(ContentType.JSON).and().body(requestBody).when()
					.post().then().extract().response();

		return response;

	}

	public static JSONObject post(String url, JSONObject jsonRequest, String role, String contextKey) throws Exception {
		if (!isValidToken(role, contextKey)) {
			if (role.equalsIgnoreCase(RESIDENT)) {
				initToken_Resident(contextKey);
			} else if (role.equalsIgnoreCase(ADMIN)) {
				initToken_admin(contextKey);
			} else if (role.equalsIgnoreCase(REGPROC)) {
				initToken_Regproc(contextKey);
			} else if (role.equalsIgnoreCase(CRVS)) {
				initToken_crvs1(contextKey);
			}else {
				initToken(contextKey);
			}
		}
		boolean bDone = false;
		int nLoop = 0;
		Response response = null;

		while (!bDone) {
			String token = tokens.get(VariableManager.getVariableValue(contextKey, URLBASE).toString().trim() + role);

			Cookie kukki = new Cookie.Builder(AUTHORIZATION, token).build();
			logInfo(contextKey, "Request:" + jsonRequest.toString());
			if (isDebugEnabled(contextKey))
				response = given().log().all().cookie(kukki).contentType(ContentType.JSON).body(jsonRequest.toString())
						.post(url).then().log().all().extract().response();
			else
				response = given().cookie(kukki).contentType(ContentType.JSON).body(jsonRequest.toString()).post(url);
			if (response.getStatusCode() == 401 || response.getStatusCode() == 500) {
				if (nLoop >= 1)
					bDone = true;
				else {
					if (role.equalsIgnoreCase(RESIDENT)) {
						initToken_Resident(contextKey);
					} else if (role.equalsIgnoreCase(ADMIN)) {
						initToken_admin(contextKey);
					} else {
						initToken(contextKey);
					}
					nLoop++;
				}
			} else
				bDone = true;

		}

		String cookie = response.getHeader(SET_COOKIE);
		if (cookie != null) {

			String token = cookie.split("=")[1];

			tokens.put(VariableManager.getVariableValue(contextKey, URLBASE).toString().trim() + role, token);
		}
		if (response.getBody().asString().startsWith("{")) {
			logInfo(contextKey, "Response:" + response.getBody().asString());
			checkErrorResponse(response.getBody().asString());

			return new JSONObject(response.getBody().asString()).getJSONObject(dataKey);
		} else {
			JSONObject responseJson = new JSONObject();
			responseJson.put("status", response.getBody().asString());
			return responseJson;
		}
	}

	public static JSONObject put(String url, JSONObject jsonRequest,String role, String contextKey) throws Exception {
		if (!isValidToken(role, contextKey)) {
			if (role.equalsIgnoreCase(RESIDENT)) {
				initToken_Resident(contextKey);
			} else if (role.equalsIgnoreCase(ADMIN)) {
				initToken_admin(contextKey);
			} else if (role.equalsIgnoreCase(REGPROC)) {
				initToken_Regproc(contextKey);
			} else if (role.equalsIgnoreCase(CRVS)) {
				initToken_crvs1(contextKey);
			}else {
				initToken(contextKey);
			}
		}
		boolean bDone = false;
		int nLoop = 0;
		Response response = null;

		while (!bDone) {
			String token = tokens.get(VariableManager.getVariableValue(contextKey, URLBASE).toString().trim() + role);

			Cookie kukki = new Cookie.Builder(AUTHORIZATION, token).build();
			logInfo(contextKey, "Request:" + jsonRequest.toString());
			if (isDebugEnabled(contextKey)) {
				response = given().log().all().cookie(kukki).contentType(ContentType.JSON).body(jsonRequest.toString())
						.put(url).then().log().all().extract().response();
			} else
				response = given().cookie(kukki).contentType(ContentType.JSON).body(jsonRequest.toString()).put(url);
			if (response.getStatusCode() == 401 || response.getStatusCode() == 500) {
				if (nLoop >= 1)
					bDone = true;
				else {
					initToken(contextKey);
					nLoop++;
				}
			} else
				bDone = true;

		}

		String cookie = response.getHeader(SET_COOKIE);
		if (cookie != null) {

			String token = cookie.split("=")[1];
			tokens.put(VariableManager.getVariableValue(contextKey, URLBASE).toString().trim() + role, token);

		}

		if (response.getBody().asString().startsWith("{")) {
		    logInfo(contextKey, "Response: " + response.getBody().asString());
		    checkErrorResponse(response.getBody().asString());
		    JSONObject jsonBody = new JSONObject(response.getBody().asString());

		    Object data = jsonBody.opt("response"); // corrected key

		    if (data instanceof JSONObject) {
		        return (JSONObject) data;
		    } else if (data instanceof JSONArray) {
		        // Wrap JSONArray in a JSONObject
		        JSONObject wrapper = new JSONObject();
		        wrapper.put("response", data);
		        return wrapper;
		    } else {
		        throw new RuntimeException("Unexpected data type for key: response");
		    }
		} else {
		    return new JSONObject("{\"status\":\"" + response.getBody().asString() + "\"}");
		}

	}
	
	public static String getToken(String role, String contextKey) throws Exception {
		if (!isValidToken(role, contextKey)) {
			if (role.equalsIgnoreCase(RESIDENT)) {
				initToken_Resident(contextKey);
			} else if (role.equalsIgnoreCase(ADMIN)) {
				initToken_admin(contextKey);
			} else if (role.equalsIgnoreCase(REGPROC)) {
				initToken_Regproc(contextKey);
			} else if (role.equalsIgnoreCase(CRVS)) {
				initToken_crvs1(contextKey);
			} else {
				initToken(contextKey);
			}
		}
		String token = tokens.get(VariableManager.getVariableValue(contextKey, URLBASE).toString().trim() + role);
		return token;

	}

	public static JSONObject putAdminPrereg(String url, JSONObject jsonRequest, String contextKey) throws Exception {
		String role = SYSTEM;
		if (!isValidToken(role, contextKey)) {
			initToken(contextKey);
		}
		boolean bDone = false;
		int nLoop = 0;
		Response response = null;

		while (!bDone) {
			String token = tokens.get(VariableManager.getVariableValue(contextKey, URLBASE).toString().trim() + role);

			Cookie kukki = new Cookie.Builder(AUTHORIZATION, token).build();
			logInfo(contextKey, "Request:" + jsonRequest.toString());
			if (isDebugEnabled(contextKey)) {
				response = given().log().all().cookie(kukki).contentType(ContentType.JSON).body(jsonRequest.toString())
						.put(url).then().log().all().extract().response();
			} else
				response = given().cookie(kukki).contentType(ContentType.JSON).body(jsonRequest.toString()).put(url);
			if (response.getStatusCode() == 401 || response.getStatusCode() == 500) {
				if (nLoop >= 1)
					bDone = true;
				else {
					initToken(contextKey);
					nLoop++;
				}
			} else
				bDone = true;

		}

		String cookie = response.getHeader(SET_COOKIE);
		if (cookie != null) {

			String token = cookie.split("=")[1];
			tokens.put(VariableManager.getVariableValue(contextKey, URLBASE).toString().trim() + role, token);

		}
		if (response.getBody().asString().startsWith("{")) {
			logInfo(contextKey, "Response:" + response.getBody().asString());
			checkErrorResponse(response.getBody().asString());
			return new JSONObject(response.getBody().asString()).getJSONObject(dataKey);
		} else {
			return new JSONObject("{\"status\":\"" + response.getBody().asString() + "\"}");
		}
	}

	public static JSONObject putPreRegStatus(String url, JSONObject jsonRequest, String contextKey) throws Exception {
		String role = PREREG;// //system
		if (!isValidToken(role, contextKey)) {
			initToken(contextKey);
		}
		boolean bDone = false;
		int nLoop = 0;
		Response response = null;

		while (!bDone) {
			String token = tokens.get(VariableManager.getVariableValue(contextKey, URLBASE).toString().trim() + role);

			Cookie kukki = new Cookie.Builder(AUTHORIZATION, token).build();
			logInfo(contextKey, "Request:" + jsonRequest.toString());
			if (isDebugEnabled(contextKey)) {
				response = given().log().all().cookie(kukki).contentType(ContentType.JSON).body(jsonRequest.toString())
						.put(url).then().log().all().extract().response();
			} else
				response = given().cookie(kukki).contentType(ContentType.JSON).body(jsonRequest.toString()).put(url);
			if (response.getStatusCode() == 401 || response.getStatusCode() == 500) {
				if (nLoop >= 1)
					bDone = true;
				else {
					initToken(contextKey);
					nLoop++;
				}
			} else
				bDone = true;

		}

		String cookie = response.getHeader(SET_COOKIE);
		if (cookie != null) {

			String token = cookie.split("=")[1];

			tokens.put(VariableManager.getVariableValue(contextKey, URLBASE).toString().trim() + role, token);
		}
		if (response.getBody().asString().startsWith("{")) {
			logInfo(contextKey, "Response:" + response.getBody().asString());
			checkErrorResponse(response.getBody().asString());
			return new JSONObject(response.getBody().asString());
		} else {
			return new JSONObject("{\"status\":\"" + response.getBody().asString() + "\"}");
		}
	}

	public static JSONObject patch(String url, JSONObject jsonRequest, String contextKey) throws Exception {
		String role = SYSTEM;
		if (!isValidToken(role, contextKey)) {
			initToken(contextKey);
		}
		boolean bDone = false;
		int nLoop = 0;
		Response response = null;

		while (!bDone) {
			String token = tokens.get(VariableManager.getVariableValue(contextKey, URLBASE).toString().trim() + role);

			Cookie kukki = new Cookie.Builder(AUTHORIZATION, token).build();
			logInfo(contextKey, "Request:" + jsonRequest.toString());
			if (isDebugEnabled(contextKey))
				response = given().log().all().cookie(kukki).contentType(ContentType.JSON).body(jsonRequest.toString())
						.patch(url).then().log().all().extract().response();
			else
				response = given().cookie(kukki).contentType(ContentType.JSON).body(jsonRequest.toString()).patch(url);
			if (response.getStatusCode() == 401 || response.getStatusCode() == 500) {
				if (nLoop >= 1)
					bDone = true;
				else {
					initToken(contextKey);
					nLoop++;
				}
			} else
				bDone = true;

		}

		String cookie = response.getHeader(SET_COOKIE);
		if (cookie != null) {

			String token = cookie.split("=")[1];

			tokens.put(VariableManager.getVariableValue(contextKey, URLBASE).toString().trim() + role, token);
		}
		if (response.getBody().asString().startsWith("{")) {
			logInfo(contextKey, "Response:" + response.getBody().asString());
			checkErrorResponse(response.getBody().asString());

			return new JSONObject(response.getBody().asString()).getJSONObject(dataKey);
		} else {
			return new JSONObject("{\"status\":\"" + response.getBody().asString() + "\"}");
		}
	}

	public static boolean initPreregToken(String url, JSONObject requestBody, String contextKey) {
		try {
			String jsonBody = requestBody.toString();
			logInfo(contextKey, "Prereg logger " + jsonBody);

			Response response = null;
			try {
				if (isDebugEnabled(contextKey))
					response = given().log().all().contentType("application/json").body(jsonBody).post(url).then().log()
							.all().extract().response();
				else
					response = given().contentType("application/json").body(jsonBody).post(url);
			} catch (Exception e) {
				logger.error(e.getMessage());
			}

			if (response != null) {
				if (response.getStatusCode() != 200 || response.toString().contains(ERRORCODE)) {
					return false;
				} else {
					return true;
				}
			} else {
				return false;
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			return false;
		}
	}

	public static boolean initToken(String contextKey) {
		try {

			JSONObject requestBody = new JSONObject();
			JSONObject nestedRequest = new JSONObject();
			nestedRequest.put(USERNAME, VariableManager.getVariableValue(contextKey, "admin_userName").toString());
			nestedRequest.put(PASSWORD, VariableManager.getVariableValue(contextKey, "admin_password").toString());
			nestedRequest.put(APPID, VariableManager.getVariableValue(contextKey, "mosip_admin_app_id").toString());
			nestedRequest.put(CLIENTID,
					VariableManager.getVariableValue(contextKey, "mosip_admin_client_id").toString());
			nestedRequest.put("clientSecret",
					VariableManager.getVariableValue(contextKey, "mosip_admin_client_secret").toString());
			requestBody.put(METADATA, new JSONObject());
			requestBody.put(VERSION, "1.0");
			requestBody.put("id", "mosip.authentication.useridPwd");
			requestBody.put(REQUESTTIME, CommonUtil.getUTCDateTime(LocalDateTime.now()).toString());
			requestBody.put(REQUEST, nestedRequest);

			String authUrl = VariableManager.getVariableValue(contextKey, URLBASE).toString().trim()
					+ VariableManager.getVariableValue(VariableManager.NS_DEFAULT, "authManagerURL").toString().trim();
			String jsonBody = requestBody.toString();
			logInfo(contextKey, contextKey + " InitToken logger " + authUrl + AUTHURL + jsonBody);

			Response response = null;
			try {
				if (isDebugEnabled(contextKey))
					response = given().log().all().contentType("application/json").body(jsonBody).post(authUrl).then()
							.log().all().extract().response();
				else
					response = given().contentType("application/json").body(jsonBody).post(authUrl);
			} catch (Exception e) {
				logger.error(e.getMessage());
			}
			if (response != null) {
				if (response.getStatusCode() != 200 || response.toString().contains(ERRORCODE)) {
					boolean bSlackit = VariableManager.getVariableValue(contextKey, POST2SLACK) == null ? false
							: Boolean.parseBoolean(VariableManager.getVariableValue(contextKey, POST2SLACK).toString());
					if (bSlackit)
						SlackIt.postMessage(null, authUrl + " Failed to authenticate, Is "
								+ VariableManager.getVariableValue(contextKey, URLBASE).toString() + " down ?");

					return false;
				}
			}

			String responseBody = response.getBody().asString();
			String token = new JSONObject(responseBody).getJSONObject(dataKey).getString("token");
			tokens.put(VariableManager.getVariableValue(contextKey, URLBASE).toString().trim() + SYSTEM, token);
			return true;
		} catch (Exception ex) {

		}
		return false;

	}

	public static boolean initToken_admin(String contextKey) {
		try {

			JSONObject requestBody = new JSONObject();
			JSONObject nestedRequest = new JSONObject();
			nestedRequest.put(USERNAME, VariableManager.getVariableValue(contextKey, "admin_userName").toString());
			nestedRequest.put(PASSWORD, VariableManager.getVariableValue(contextKey, "admin_password").toString());
			nestedRequest.put(APPID, VariableManager.getVariableValue(contextKey, "mosip_admin_app_id").toString());
			nestedRequest.put(CLIENTID,
					VariableManager.getVariableValue(contextKey, "mosip_admin_client_id").toString());
			nestedRequest.put("clientSecret",
					VariableManager.getVariableValue(contextKey, "mosip_admin_client_secret").toString());
			requestBody.put(METADATA, new JSONObject());
			requestBody.put(VERSION, "1.0");
			requestBody.put("id", "mosip.authentication.useridPwd");
			requestBody.put(REQUESTTIME, CommonUtil.getUTCDateTime(LocalDateTime.now()).toString());
			requestBody.put(REQUEST, nestedRequest);

			String authUrl = VariableManager.getVariableValue(contextKey, URLBASE).toString().trim()
					+ VariableManager.getVariableValue(VariableManager.NS_DEFAULT, "authManagerURL").toString().trim();
			String jsonBody = requestBody.toString();
			logInfo(contextKey, contextKey + " InitToken_admin logger " + authUrl + AUTHURL + jsonBody);
			Response response = null;
			try {
				if (isDebugEnabled(contextKey))
					response = given().log().all().contentType("application/json").body(jsonBody).post(authUrl).then()
							.log().all().extract().response();
				else
					response = given().contentType("application/json").body(jsonBody).post(authUrl);

			} catch (Exception e) {
				logger.error(e.getMessage());
			}

			if (response != null && (response.getStatusCode() != 200 || response.toString().contains(ERRORCODE))) {
				boolean bSlackit = VariableManager.getVariableValue(contextKey, POST2SLACK) == null ? false
						: Boolean.parseBoolean(VariableManager.getVariableValue(contextKey, POST2SLACK).toString());
				if (bSlackit)
					SlackIt.postMessage(null, authUrl + " Failed to authenticate, Is "
							+ VariableManager.getVariableValue(contextKey, URLBASE).toString() + " down ?");

				return false;
			}
			String responseBody = response.getBody().asString();
			String token = new JSONObject(responseBody).getJSONObject(dataKey).getString("token");

			tokens.put(VariableManager.getVariableValue(contextKey, URLBASE).toString().trim() + ADMIN, token);

			return true;
		} catch (Exception ex) {

		}
		return false;

	}

	public static boolean initToken_Resident(String contextKey) {
		try {
			JSONObject requestBody = new JSONObject();
			JSONObject nestedRequest = new JSONObject();
			nestedRequest.put(USERNAME, VariableManager.getVariableValue(contextKey, "operatorId"));
			nestedRequest.put(PASSWORD, VariableManager.getVariableValue(contextKey, PASSWORD));
			nestedRequest.put(APPID, VariableManager.getVariableValue(contextKey, "mosip_resident_app_id"));
			nestedRequest.put(CLIENTID, VariableManager.getVariableValue(contextKey, "mosip_resident_client_id"));
			nestedRequest.put("secretKey",
					VariableManager.getVariableValue(contextKey, "mosip_resident_client_secret"));
			requestBody.put(METADATA, new JSONObject());
			requestBody.put(VERSION, "string");
			requestBody.put("id", "string");
			requestBody.put(REQUESTTIME, CommonUtil.getUTCDateTime(LocalDateTime.now()).toString());
			requestBody.put(REQUEST, nestedRequest);

			String authUrl = VariableManager.getVariableValue(contextKey, URLBASE).toString().trim()
					+ "v1/authmanager/authenticate/clientidsecretkey";

			String jsonBody = requestBody.toString();
			logInfo(contextKey, contextKey + " initToken_Resident logger " + authUrl + AUTHURL + jsonBody);

			Response response = null;
			try {
				if (isDebugEnabled(contextKey))
					response = given().log().all().contentType("application/json").body(jsonBody).post(authUrl).then()
							.log().all().extract().response();
				else
					response = given().contentType("application/json").body(jsonBody).post(authUrl);
			} catch (Exception e) {
				logger.error(e.getMessage());
			}

			if (response != null && (response.getStatusCode() != 200 || response.toString().contains(ERRORCODE))) {
				boolean bSlackit = VariableManager.getVariableValue(contextKey, POST2SLACK) == null ? false
						: Boolean.parseBoolean(VariableManager.getVariableValue(contextKey, POST2SLACK).toString());
				if (bSlackit)
					SlackIt.postMessage(null, authUrl + " Failed to authenticate, Is "
							+ VariableManager.getVariableValue(contextKey, URLBASE).toString() + " down ?");

				return false;
			}
			String token = response.getCookie(AUTHORIZATION);
			tokens.put(VariableManager.getVariableValue(contextKey, URLBASE).toString().trim() + RESIDENT, token);

			return true;
		} catch (Exception ex) {

		}
		return false;

	}
	
	public static boolean initToken_Regproc(String contextKey) {
		try {
			JSONObject requestBody = new JSONObject();
			JSONObject nestedRequest = new JSONObject();
			nestedRequest.put(USERNAME, VariableManager.getVariableValue(contextKey, "operatorId"));
			nestedRequest.put(PASSWORD, VariableManager.getVariableValue(contextKey, PASSWORD));
			nestedRequest.put(APPID, VariableManager.getVariableValue(contextKey, "mosip_regprocclient_app_id"));
			nestedRequest.put(CLIENTID, VariableManager.getVariableValue(contextKey, "mosip_regproc_client_id"));
			nestedRequest.put("secretKey",
					VariableManager.getVariableValue(contextKey, "mosip_regproc_client_secret"));
			requestBody.put(METADATA, new JSONObject());
			requestBody.put(VERSION, "string");
			requestBody.put("id", "string");
			requestBody.put(REQUESTTIME, CommonUtil.getUTCDateTime(LocalDateTime.now()).toString());
			requestBody.put(REQUEST, nestedRequest);

			String authUrl = VariableManager.getVariableValue(contextKey, URLBASE).toString().trim()
					+ "v1/authmanager/authenticate/clientidsecretkey";

			String jsonBody = requestBody.toString();
			logInfo(contextKey, contextKey + " initToken_Resident logger " + authUrl + AUTHURL + jsonBody);

			Response response = null;
			try {
				if (isDebugEnabled(contextKey))
					response = given().log().all().contentType("application/json").body(jsonBody).post(authUrl).then()
							.log().all().extract().response();
				else
					response = given().contentType("application/json").body(jsonBody).post(authUrl);
			} catch (Exception e) {
				logger.error(e.getMessage());
			}

			if (response != null && (response.getStatusCode() != 200 || response.toString().contains(ERRORCODE))) {
				boolean bSlackit = VariableManager.getVariableValue(contextKey, POST2SLACK) == null ? false
						: Boolean.parseBoolean(VariableManager.getVariableValue(contextKey, POST2SLACK).toString());
				if (bSlackit)
					SlackIt.postMessage(null, authUrl + " Failed to authenticate, Is "
							+ VariableManager.getVariableValue(contextKey, URLBASE).toString() + " down ?");

				return false;
			}
			String token = response.getCookie(AUTHORIZATION);
			tokens.put(VariableManager.getVariableValue(contextKey, URLBASE).toString().trim() + REGPROC, token);

			return true;
		} catch (Exception ex) {

		}
		return false;

	}
	
	public static boolean initToken_crvs1(String contextKey) {
		try {
			JSONObject requestBody = new JSONObject();
			JSONObject nestedRequest = new JSONObject();
			nestedRequest.put(USERNAME, VariableManager.getVariableValue(contextKey, "operatorId"));
			nestedRequest.put(PASSWORD, VariableManager.getVariableValue(contextKey, PASSWORD));
			nestedRequest.put(APPID, VariableManager.getVariableValue(contextKey, "mosip_crvs1_app_id"));
			nestedRequest.put(CLIENTID, VariableManager.getVariableValue(contextKey, "mosip_crvs1_client_id"));
			nestedRequest.put("secretKey",
					VariableManager.getVariableValue(contextKey, "mosip_crvs1_client_secret"));
			requestBody.put(METADATA, new JSONObject());
			requestBody.put(VERSION, "string");
			requestBody.put("id", "string");
			requestBody.put(REQUESTTIME, CommonUtil.getUTCDateTime(LocalDateTime.now()).toString());
			requestBody.put(REQUEST, nestedRequest);

			String authUrl = VariableManager.getVariableValue(contextKey, URLBASE).toString().trim()
					+ "v1/authmanager/authenticate/clientidsecretkey";

			String jsonBody = requestBody.toString();
			logInfo(contextKey, contextKey + " initToken_Resident logger " + authUrl + AUTHURL + jsonBody);

			Response response = null;
			try {
				if (isDebugEnabled(contextKey))
					response = given().log().all().contentType("application/json").body(jsonBody).post(authUrl).then()
							.log().all().extract().response();
				else
					response = given().contentType("application/json").body(jsonBody).post(authUrl);
			} catch (Exception e) {
				logger.error(e.getMessage());
			}

			if (response != null && (response.getStatusCode() != 200 || response.toString().contains(ERRORCODE))) {
				boolean bSlackit = VariableManager.getVariableValue(contextKey, POST2SLACK) == null ? false
						: Boolean.parseBoolean(VariableManager.getVariableValue(contextKey, POST2SLACK).toString());
				if (bSlackit)
					SlackIt.postMessage(null, authUrl + " Failed to authenticate, Is "
							+ VariableManager.getVariableValue(contextKey, URLBASE).toString() + " down ?");

				return false;
			}
			String token = response.getCookie(AUTHORIZATION);
			tokens.put(VariableManager.getVariableValue(contextKey, URLBASE).toString().trim() + CRVS, token);

			return true;
		} catch (Exception ex) {

		}
		return false;

	}

	private static void checkErrorResponse(String response) throws Exception {
		JSONObject jsonObject = new JSONObject(response);
		boolean err = false;
		if (jsonObject.has(errorKey)) {
			Object errObject = jsonObject.get(errorKey);
			if (errObject instanceof JSONArray) {
				if (!((JSONArray) errObject).isEmpty())
					err = true;
			} else if (errObject != JSONObject.NULL)
				err = true;
		}
		if (err)
			throw new Exception(String.valueOf(jsonObject.get(errorKey)));

	}

	public String get(String uri, Properties queryParam) throws IOException {

		StringBuilder builder = new StringBuilder();

		String strUrl = _urlBase + uri + constructQueryParam(queryParam);
		URL url = new URL(strUrl);

		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setDoOutput(true);
		conn.setRequestMethod("GET");
		conn.setRequestProperty("Content-Type", "application/json");
		conn.setRequestProperty("Accept", "application/json");
		headers.forEach((k, v) -> {
			conn.addRequestProperty(k.toString(), v.toString());
		});
		http_status = conn.getResponseCode();

		if (http_status == 200) {

		try(BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));){	

			String output;

			while ((output = br.readLine()) != null) {
				builder.append(output);
			}
		}
		catch (Exception e) {
			// TODO: handle exception
		}
		}
		return builder.toString();
	}

	public static JSONArray getJsonArray(String url, JSONObject requestParams, JSONObject pathParam, String contextKey)
			throws Exception {

		String role = SYSTEM;
		if (!isValidToken(role, contextKey)) {
			initToken(contextKey);
		}
		boolean bDone = false;
		int nLoop = 0;
		Response response = null;

		while (!bDone) {

			String token = tokens.get(VariableManager.getVariableValue(contextKey, URLBASE).toString().trim() + role);

			Cookie kukki = new Cookie.Builder(AUTHORIZATION, token).build();
			Map<String, Object> mapParam = requestParams == null ? null : requestParams.toMap();
			Map<String, Object> mapPathParam = pathParam == null ? null : pathParam.toMap();

			if (isDebugEnabled(contextKey))
				response = given().log().all().cookie(kukki).contentType(ContentType.JSON).queryParams(mapParam)
						.get(url, mapPathParam).then().log().all().extract().response();

			else
				response = given().cookie(kukki).contentType(ContentType.JSON).queryParams(mapParam).get(url,
						mapPathParam);

			if (response.getStatusCode() == 401) {
				if (nLoop >= 1)
					bDone = true;
				else {
					initToken(contextKey);
					nLoop++;
				}
			} else
				bDone = true;
		}

		if (response != null) {
			logInfo(contextKey, "hello");

		}
		checkErrorResponse(response.getBody().asString());

		return new JSONObject(response.getBody().asString()).getJSONArray(dataKey);
	}

	public static String rawHttp(HttpRCapture httpRCapture, String jsonBody, String contextKey) throws IOException {

		String result = "";
		try (CloseableHttpClient httpClient = HttpClients.createDefault();) {
			httpRCapture.setEntity(new StringEntity(jsonBody));
			HttpResponse response = httpClient.execute(httpRCapture);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				result = EntityUtils.toString(entity);
				logInfo(contextKey, result);
			}
		}
		return result;
	}

	public static boolean checkActuator(String url, String contextKey) {

		String urlAct = url + "/actuator/health";

		String role = SYSTEM;
		if (!isValidToken(role, contextKey)) {
			initToken(contextKey);
		}
		boolean bDone = false;
		int nLoop = 0;
		Response response = null;

		while (!bDone) {

			String token = tokens.get(VariableManager.getVariableValue(contextKey, URLBASE).toString().trim() + role);

			Cookie kukki = new Cookie.Builder(AUTHORIZATION, token).build();
			if (isDebugEnabled(contextKey))
				response = given().log().all().cookie(kukki).contentType(ContentType.JSON).get(url).then().log().all()
						.extract().response();
			else
				response = given().cookie(kukki).contentType(ContentType.JSON).get(url);

			if (response.getStatusCode() == 401) {
				if (nLoop >= 1)
					bDone = true;
				else {
					initToken(contextKey);
					nLoop++;
				}
			} else
				bDone = true;
		}

		if (response != null && response.getStatusCode() == 200) {

			logInfo(contextKey, response.getBody().asString());

			JSONObject jsonResponse = new JSONObject(response.getBody().asString());

			if (jsonResponse.getString("status").equals("UP")) {
				return true;
			}
			return false;
		} else
			return false;
	}

	public static boolean checkActuatorNoAuth(String url, String contextKey) {

		String urlAct = url;

		Response response = null;
		if (isDebugEnabled(contextKey))
			response = given().log().all().contentType(ContentType.JSON).get(urlAct).then().log().all().extract()
					.response();
		else
			response = given().contentType(ContentType.JSON).get(urlAct);
		if (response != null && response.getStatusCode() == 200) {

			logInfo(contextKey, response.getBody().asString());

			JSONObject jsonResponse = new JSONObject(response.getBody().asString());

			if (jsonResponse.getString("status").equals("UP")) {
				return true;
			}
			return false;
		} else
			return false;
	}

}
