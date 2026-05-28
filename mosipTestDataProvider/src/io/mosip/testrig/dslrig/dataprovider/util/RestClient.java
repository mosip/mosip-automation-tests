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

import org.json.JSONObject;
import org.springframework.http.HttpStatus;

import io.mosip.testrig.dslrig.dataprovider.mds.HttpRCapture;
import io.mosip.testrig.dslrig.dataprovider.preparation.MosipDataSetup;
import io.mosip.testrig.dslrig.dataprovider.preparation.MasterdataCache;
import io.mosip.testrig.dslrig.dataprovider.util.internalapi.InternalApiLogging;
import io.mosip.testrig.dslrig.dataprovider.util.internalapi.InternalApiManualRecorder;
import io.mosip.testrig.dslrig.dataprovider.util.internalapi.InternalApiRestAssuredFilter;
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

	/** Extracts cookie value from {@code Set-Cookie} (stops at first {@code ;}). */
	static String tokenFromSetCookie(String setCookieHeader) {
		if (setCookieHeader == null || setCookieHeader.isBlank()) {
			return null;
		}
		int eq = setCookieHeader.indexOf('=');
		if (eq < 0 || eq + 1 >= setCookieHeader.length()) {
			return null;
		}
		String value = setCookieHeader.substring(eq + 1);
		int semi = value.indexOf(';');
		return (semi >= 0 ? value.substring(0, semi) : value).trim();
	}

	static {


	}

	static RequestSpecification requestSpec(String contextKey) {
		RequestSpecification spec = given();
		if (InternalApiLogging.isEnabled(contextKey)) {
			spec = spec.filter(new InternalApiRestAssuredFilter(contextKey));
		}
		return spec;
	}

	String _urlBase;

	int http_status;
	Properties headers;

	public static Boolean isValidToken(String role, String contextKey) {
		return RestClientAuth.isValidToken(role, contextKey);
	}

	public static boolean isValidTokenOffline(String cookie, String contextKey) {
		return RestClientAuth.isValidTokenOffline(cookie, contextKey);
	}

	/**
	 * @deprecated Use {@link #clearRunScopedCache(String)} — global token clearing is unsafe under parallel runs.
	 */
	@Deprecated
	public static void clearToken() {
		throw new UnsupportedOperationException(
				"Global clearToken() clears all contexts; use clearRunScopedCache(contextKey) instead.");
	}

	public static void clearRunScopedCache(String contextKey) {
		MosipDataSetup.clearRunCache(contextKey);
		AuthTokenStore.clearContext(contextKey);
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

				String token = AuthTokenStore.get(contextKey, role);
				Cookie kukki = new Cookie.Builder(AUTHORIZATION, token).build();
				Map<String, Object> mapParam = requestParams == null ? null : requestParams.toMap();
				Map<String, Object> mapPathParam = pathParam == null ? null : pathParam.toMap();

				if (isDebugEnabled(contextKey)) {
					response = requestSpec(contextKey).log().all().cookie(kukki).contentType(ContentType.JSON).queryParams(mapParam)
							.get(url, mapPathParam).then().log().all().extract().response();
				} else {
					response = requestSpec(contextKey).cookie(kukki).contentType(ContentType.JSON).queryParams(mapParam).get(url,
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
			if (response != null) {
				checkErrorResponse(response.getBody().asString(), url);
			} else {
				throw new ServiceException(HttpStatus.BAD_GATEWAY, "REST_NO_RESPONSE", url);
			}

		} catch (ServiceException se) {
			throw se;
		} catch (Exception e) {
			logger.error("GET failed for url {} : {}", url, e.getMessage(), e);
			throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "REST_CALL_FAIL", url, e, e.getMessage());
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

				String token = AuthTokenStore.get(contextKey, role);
				Cookie kukki = new Cookie.Builder(AUTHORIZATION, token).build();
				Map<String, Object> mapParam = requestParams == null ? null : requestParams.toMap();
				Map<String, Object> mapPathParam = pathParam == null ? null : pathParam.toMap();

				if (isDebugEnabled(contextKey)) {
					response = requestSpec(contextKey).log().all().cookie(kukki).contentType(ContentType.JSON).queryParams(mapParam)
							.get(url, mapPathParam).then().log().all().extract().response();
				} else {
					response = requestSpec(contextKey).cookie(kukki).contentType(ContentType.JSON).queryParams(mapParam).get(url,
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
				checkErrorResponse(response.getBody().asString(), url);
			}

		} catch (ServiceException se) {
			throw se;
		} catch (Exception e) {
			logger.error("GET failed for url {} : {}", url, e.getMessage(), e);
			throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "REST_CALL_FAIL", url, e, e.getMessage());
		}

		return new JSONObject(response.getBody().asString()).getJSONObject(dataKey);
	}


	public static JSONObject get(String url, JSONObject requestParams, JSONObject pathParam, String contextKey)
			throws Exception {

		String runCacheKey = null;
		if (MasterdataCache.isEnabled(contextKey) && MasterdataCache.isCacheableGetUrl(url)) {
			runCacheKey = MasterdataCache.buildGetKey(url, requestParams, pathParam);
			JSONObject cached = MasterdataCache.getCachedGetJson(contextKey, runCacheKey);
			if (cached != null) {
				return cached;
			}
		}

		String role = SYSTEM;
		if (!isValidToken(role, contextKey)) {
			initToken(contextKey);

		}
		boolean bDone = false;
		int nLoop = 0;
		Response response = null;

		try {
			while (!bDone) {

				String token = AuthTokenStore.get(contextKey, role);
				Cookie kukki = new Cookie.Builder(AUTHORIZATION, token).build();
				Map<String, Object> mapParam = requestParams == null ? null : requestParams.toMap();
				Map<String, Object> mapPathParam = pathParam == null ? null : pathParam.toMap();

				if (isDebugEnabled(contextKey)) {
					response = requestSpec(contextKey).log().all().cookie(kukki).contentType(ContentType.JSON).queryParams(mapParam)
							.get(url, mapPathParam).then().log().all().extract().response();
				} else {
					response = requestSpec(contextKey).cookie(kukki).contentType(ContentType.JSON).queryParams(mapParam).get(url,
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
			if (response == null) {
				throw new ServiceException(HttpStatus.BAD_GATEWAY, "REST_NO_RESPONSE", url);
			}
			checkErrorResponse(response.getBody().asString(), url);
		} catch (ServiceException se) {
			throw se;
		} catch (Exception e) {
			logger.error("GET failed for url {} : {}", url, e.getMessage(), e);
			throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "REST_CALL_FAIL", url, e, e.getMessage());
		}

		JSONObject fullResp = new JSONObject(response.getBody().asString());

		JSONObject result;
		if (fullResp.has(dataKey)) {
			Object respObj = fullResp.get(dataKey);

			if (respObj instanceof JSONObject) {
				result = (JSONObject) respObj;
			} else if (respObj instanceof JSONArray) {
				result = fullResp;
			} else {
				result = fullResp;
			}
		} else {
			result = fullResp;
		}

		if (runCacheKey != null) {
			MasterdataCache.putCachedGetJson(contextKey, runCacheKey, result);
		}
		return result;
	}

	public static Response getWithoutCookie(String url) {

		Response response = null;
		try {
			response = requestSpec(null)
					.relaxedHTTPSValidation()
					.log().all()
					.when()
					.get(url);

			if (response == null) {
				throw new ServiceException(HttpStatus.BAD_GATEWAY, "REST_NO_RESPONSE", url);
			}
		} catch (ServiceException se) {
			throw se;
		} catch (Exception e) {
			logger.error("GET failed for url {} : {}", url, e.getMessage(), e);
			throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "REST_CALL_FAIL", url, e, e.getMessage());
		}
		return response;
	}


	public static JSONObject getWithoutAuth(String url, JSONObject requestParams, JSONObject pathParam,
			String contextKey)
			throws Exception {

		Response response = null;

		try {
			Map<String, Object> mapParam = requestParams == null ? null : requestParams.toMap();
			Map<String, Object> mapPathParam = pathParam == null ? null : pathParam.toMap();

			if (isDebugEnabled(contextKey)) {
				response = requestSpec(contextKey).log().all().contentType(ContentType.JSON).queryParams(mapParam)
						.get(url, mapPathParam).then().log().all().extract().response();
			} else {
				response = requestSpec(contextKey).contentType(ContentType.JSON).queryParams(mapParam).get(url, mapPathParam);
			}

			if (isDebugEnabled(contextKey) && response != null) {
				logInfo(contextKey, response.getBody().asString());
			}
			if (response == null) {
				throw new ServiceException(HttpStatus.BAD_GATEWAY, "REST_NO_RESPONSE", url);
			}
			checkErrorResponse(response.getBody().asString(), url);
		} catch (ServiceException se) {
			throw se;
		} catch (Exception e) {
			logger.error("GET failed for url {} : {}", url, e.getMessage(), e);
			throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "REST_CALL_FAIL", url, e, e.getMessage());
		}

		JSONObject fullResp = new JSONObject(response.getBody().asString());

		if (fullResp.has(dataKey)) {
			Object respObj = fullResp.get(dataKey);

			if (respObj instanceof JSONObject) {
				return (JSONObject) respObj;
			} else if (respObj instanceof JSONArray) {
				return fullResp;
			}
		}

		return fullResp;
	}


	public static JSONArray getDoc(String url, JSONObject requestParams, JSONObject pathParam, String contextKey)
			throws Exception {

		String runCacheKey = null;
		if (MasterdataCache.isEnabled(contextKey) && MasterdataCache.isCacheableGetUrl(url)) {
			runCacheKey = MasterdataCache.buildGetKey(url, requestParams, pathParam);
			JSONArray cached = MasterdataCache.getCachedGetArray(contextKey, runCacheKey);
			if (cached != null) {
				return cached;
			}
		}

		String role = SYSTEM;
		if (!isValidToken(role, contextKey)) {
			initToken(contextKey);

		}
		boolean bDone = false;
		int nLoop = 0;
		Response response = null;

		while (!bDone) {

			String token = AuthTokenStore.get(contextKey, role);

			Cookie kukki = new Cookie.Builder(AUTHORIZATION, token).build();
			Map<String, Object> mapParam = requestParams == null ? null : requestParams.toMap();
			Map<String, Object> mapPathParam = pathParam == null ? null : pathParam.toMap();

			if (isDebugEnabled(contextKey)) {
				response = requestSpec(contextKey).log().all().cookie(kukki).contentType(ContentType.JSON).queryParams(mapParam)
						.get(url, mapPathParam).then().log().all().extract().response();
			} else {
				response = requestSpec(contextKey).cookie(kukki).contentType(ContentType.JSON).queryParams(mapParam).get(url,
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
		if (response == null) {
			throw new ServiceException(HttpStatus.BAD_GATEWAY, "REST_NO_RESPONSE", url);
		}
		try {
			checkErrorResponse(response.getBody().asString(), url);
		} catch (ServiceException se) {
			throw se;
		} catch (Exception e) {
			logger.error("GET failed for url {} : {}", url, e.getMessage(), e);
			throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "REST_CALL_FAIL", url, e, e.getMessage());
		}

		JSONArray result = new JSONObject(response.getBody().asString()).getJSONArray(dataKey);
		if (runCacheKey != null) {
			MasterdataCache.putCachedGetArray(contextKey, runCacheKey, result);
		}
		return result;
	}

	public static JSONObject getNoAuth(String url, JSONObject requestParams, JSONObject pathParam, String contextKey)
			throws Exception {
		String role = PREREG;
		if (!isValidToken(role, contextKey)) {
			initPreregToken(url, requestParams, contextKey);
		}
		String token = AuthTokenStore.get(contextKey, role);

		Response response = null;

		Cookie kukki = new Cookie.Builder(AUTHORIZATION, token).build();
		Map<String, Object> mapParam = requestParams == null ? null : requestParams.toMap();

		Map<String, Object> mapPathParam = pathParam == null ? null : pathParam.toMap();

		if (isDebugEnabled(contextKey)) {
			response = requestSpec(contextKey).log().all().cookie(kukki).contentType(ContentType.JSON).queryParams(mapParam)
					.get(url, mapPathParam).then().log().all().extract().response();
		} else {
			response = requestSpec(contextKey).cookie(kukki).contentType(ContentType.JSON).queryParams(mapParam).get(url, mapPathParam);
		}

		if (isDebugEnabled(contextKey) && response != null) {
			logInfo(contextKey, response.getBody().asString());
		}

		if (response != null) {
			try {
				checkErrorResponse(response.getBody().asString(), url);
			} catch (ServiceException se) {
				throw se;
			} catch (Exception e) {
				logger.error("GET failed for url {} : {}", url, e.getMessage(), e);
				throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "REST_CALL_FAIL", url, e, e.getMessage());
			}
		} else
			throw new ServiceException(HttpStatus.BAD_GATEWAY, "REST_NO_RESPONSE", url);

		return new JSONObject(response.getBody().asString()).getJSONObject(dataKey);
	}

	public static JSONObject uploadFile(String url, String filePath, JSONObject requestData, String contextKey)
			throws Exception {
		String role = PREREG;

		if (!isValidToken(role, contextKey)) {
			initToken(contextKey);
		}

		Response response = null;

		String token = AuthTokenStore.get(contextKey, role);

		Cookie kukki = new Cookie.Builder(AUTHORIZATION, token).build();

		if (requestData != null) {
			if (isDebugEnabled(contextKey))
				response = requestSpec(contextKey).log().all().cookie(kukki).multiPart("file", new File(filePath))
						.param("Document request", requestData.toString()).post(url).then().log().all().extract()
						.response();
			else
				response = requestSpec(contextKey).cookie(kukki).multiPart("file", new File(filePath))
						.param("Document request", requestData.toString()).post(url);
		} else {
			if (isDebugEnabled(contextKey))
				response = requestSpec(contextKey).log().all().cookie(kukki).multiPart("file", new File(filePath)).post(url).then()
						.log().all().extract().response();
			else
				response = requestSpec(contextKey).cookie(kukki).multiPart("file", new File(filePath)).post(url);
		}
		if (response == null) {
			throw new ServiceException(HttpStatus.BAD_GATEWAY, "REST_NO_RESPONSE", url);
		}
		try {
			checkErrorResponse(response.getBody().asString(), url);
		} catch (ServiceException se) {
			throw se;
		} catch (Exception e) {
			logger.error("POST (file upload) failed for url {} : {}", url, e.getMessage(), e);
			throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "REST_CALL_FAIL", url, e, e.getMessage());
		}

		return new JSONObject(response.getBody().asString()).getJSONObject(dataKey);
	}

	public static JSONObject uploadFiles(String url, List<String> filePaths, JSONObject requestData, String contextKey)
			throws Exception {
		String role = ADMIN;

		if (!isValidToken(role, contextKey)) {
			initToken_admin(contextKey);
		}

		Response response = null;
		RequestSpecification spec = null;
		String token = AuthTokenStore.get(contextKey, role);

		Cookie kukki = new Cookie.Builder(AUTHORIZATION, token).build();

		if (isDebugEnabled(contextKey))
			spec = requestSpec(contextKey).log().all().cookie(kukki);
		else
			spec = requestSpec(contextKey).cookie(kukki);
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
		if (response == null) {
			throw new ServiceException(HttpStatus.BAD_GATEWAY, "REST_NO_RESPONSE", url);
		}
		try {
			checkErrorResponse(response.getBody().asString(), url);
		} catch (ServiceException se) {
			throw se;
		} catch (Exception e) {
			logger.error("POST (files upload) failed for url {} : {}", url, e.getMessage(), e);
			throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "REST_CALL_FAIL", url, e, e.getMessage());
		}
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

		String token = AuthTokenStore.get(contextKey, role);

		Response response = null;
		logInfo(contextKey, "Request: " + jsonRequest.toString());
		try {
			if (isDebugEnabled(contextKey))
				response = requestSpec(contextKey).log().all().contentType(ContentType.JSON).body(jsonRequest.toString()).post(url)
						.then().log().all().extract().response();
			else
				response = requestSpec(contextKey).contentType(ContentType.JSON).body(jsonRequest.toString()).post(url);
		} catch (ServiceException se) {
			throw se;
		} catch (Exception e) {
			logger.error("GET failed for url {} : {}", url, e.getMessage(), e);
			throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "REST_CALL_FAIL", url, e, e.getMessage());
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

			String refreshed = tokenFromSetCookie(cookie);
			if (refreshed != null) {
				token = refreshed;
				AuthTokenStore.put(contextKey, role, token);
			}

		}
		checkErrorResponse(response.getBody().asString(), url);

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

		String token = AuthTokenStore.get(contextKey, role);

		Response response = null;
		logInfo(contextKey, "Request: " + jsonRequest.toString());
		try {
			Cookie kukki = new Cookie.Builder(AUTHORIZATION, token).build();
			if (isDebugEnabled(contextKey))
				response = requestSpec(contextKey).log().all().cookie(kukki).contentType(ContentType.JSON).body(jsonRequest.toString())
						.post(url).then().log().all().extract().response();
			else
				response = requestSpec(contextKey).cookie(kukki).contentType(ContentType.JSON).body(jsonRequest.toString()).post(url);

			if (isDebugEnabled(contextKey)) {
				if (response != null) {
					logInfo(contextKey, "Response: " + response.getBody().asString());
				} else {
					logInfo(contextKey, "Response: null");
				}
			}
			if (response == null) {
				throw new ServiceException(HttpStatus.BAD_GATEWAY, "REST_NO_RESPONSE", url);
			}
			for (Header h : response.getHeaders()) {
				logInfo(contextKey, h.getName() + "=" + h.getValue());
			}
			String cookie = response.getHeader(SET_COOKIE);
			String refreshed = tokenFromSetCookie(cookie);
			if (refreshed != null) {
				token = refreshed;
				AuthTokenStore.put(contextKey, role, token);
			}
			logInfo(contextKey, token);
			checkErrorResponse(response.getBody().asString(), url);
		} catch (ServiceException se) {
			throw se;
		} catch (Exception e) {
			logger.error("POST failed for url {} : {}", url, e.getMessage(), e);
			throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "REST_CALL_FAIL", url, e, e.getMessage());
		}
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
		String token = AuthTokenStore.get(contextKey, role);

		Response response = null;
		logInfo(contextKey, "Request:" + jsonRequest.toString());
		Cookie kukki = new Cookie.Builder(AUTHORIZATION, token).build();

		if (isDebugEnabled(contextKey))
			response = requestSpec(contextKey).log().all().cookie(kukki).contentType(ContentType.JSON).body(jsonRequest.toString())
					.put(url).then().log().all().extract().response();
		else
			response = requestSpec(contextKey).cookie(kukki).contentType(ContentType.JSON).body(jsonRequest.toString()).put(url);

		String cookie = response.getHeader(SET_COOKIE);
		if (cookie != null) {
			token = tokenFromSetCookie(cookie);
			AuthTokenStore.put(contextKey, role, token);
		}
		logInfo(contextKey, token);
		logInfo(contextKey, "Response:" + response.getBody().asString());
		checkErrorResponse(response.getBody().asString(), url);

		return new JSONObject(response.getBody().asString());
	}

	public static JSONObject deleteNoAuth(String url, JSONObject jsonRequest, String contextKey) throws Exception {

		String role = ADMIN;
		if (!isValidToken(role, contextKey)) {
			initToken_admin(contextKey);
		}
		String token = AuthTokenStore.get(contextKey, role);

		Response response = null;

		logInfo(contextKey, "Request:" + jsonRequest.toString());
		Cookie kukki = new Cookie.Builder(AUTHORIZATION, token).build();

		if (isDebugEnabled(contextKey))
			response = requestSpec(contextKey).log().all().cookie(kukki).contentType(ContentType.JSON).body(jsonRequest.toString())
					.delete(url).then().log().all().extract().response();
		else
			response = requestSpec(contextKey).cookie(kukki).contentType(ContentType.JSON).body(jsonRequest.toString()).delete(url);

		String cookie = response.getHeader(SET_COOKIE);
		if (cookie != null) {
			token = tokenFromSetCookie(cookie);
			AuthTokenStore.put(contextKey, role, token);
		}
		logInfo(contextKey, token);
		logInfo(contextKey, "Response:" + response.getBody().asString());
		checkErrorResponse(response.getBody().asString(), url);

		return new JSONObject(response.getBody().asString()).getJSONObject(dataKey);
	}

	public static JSONObject delete(String url, JSONObject jsonRequest, String contextKey) throws Exception {

		String role = RESIDENT;
		if (!isValidToken(role, contextKey)) {
			initToken_Resident(contextKey);
		}
		String token = AuthTokenStore.get(contextKey, role);

		Response response = null;

		logInfo(contextKey, "Request:" + jsonRequest.toString());
		Cookie kukki = new Cookie.Builder(AUTHORIZATION, token).build();
		if (isDebugEnabled(contextKey))
			response = requestSpec(contextKey).log().all().cookie(kukki).contentType(ContentType.JSON).body(jsonRequest.toString())
					.delete(url).then().log().all().extract().response();
		else
			response = requestSpec(contextKey).cookie(kukki).contentType(ContentType.JSON).body(jsonRequest.toString()).delete(url);
		String cookie = response.getHeader(SET_COOKIE);
		if (cookie != null) {
			token = tokenFromSetCookie(cookie);
			AuthTokenStore.put(contextKey, role, token);
		}
		logInfo(contextKey, token);
		logInfo(contextKey, "Response:" + response.getBody().asString());
		checkErrorResponse(response.getBody().asString(), url);
		return new JSONObject(response.getBody().asString());
	}

	public static String deleteExpectation(String url, JSONObject jsonRequest, String contextKey) throws Exception {

		String role = RESIDENT;
		if (!isValidToken(role, contextKey)) {
			initToken_Resident(contextKey);
		}
		String token = AuthTokenStore.get(contextKey, role);

		Response response = null;

		logInfo(contextKey, "Request:" + jsonRequest.toString());
		Cookie kukki = new Cookie.Builder(AUTHORIZATION, token).build();
		if (isDebugEnabled(contextKey))
			response = requestSpec(contextKey).log().all().cookie(kukki).contentType(ContentType.JSON).body(jsonRequest.toString())
					.delete(url).then().log().all().extract().response();
		else
			response = requestSpec(contextKey).cookie(kukki).contentType(ContentType.JSON).body(jsonRequest.toString()).delete(url);
		String cookie = response.getHeader(SET_COOKIE);
		if (cookie != null) {
			token = tokenFromSetCookie(cookie);
			AuthTokenStore.put(contextKey, role, token);
		}
		logInfo(contextKey, token);
		logInfo(contextKey, "Response:" + response.getBody().asString());
		checkErrorResponse(response.getBody().asString(), url);
		return response.getBody().asString();
	}

	public static JSONObject deleteNoAuthWithQueryParam(String url, JSONObject jsonRequest, String contextKey)
			throws Exception {

		String role = RESIDENT;
		if (!isValidToken(role, contextKey)) {
			initToken_Resident(contextKey);
		}
		String token = AuthTokenStore.get(contextKey, role);

		Response response = null;

		logInfo(contextKey, "Request:" + jsonRequest.toString());
		Cookie kukki = new Cookie.Builder(AUTHORIZATION, token).build();

		if (isDebugEnabled(contextKey))
			response = requestSpec(contextKey).log().all().cookie(kukki).contentType(ContentType.JSON).queryParams(jsonRequest.toMap())
					.delete(url).then().log().all().extract().response();
		else
			response = requestSpec(contextKey).cookie(kukki).contentType(ContentType.JSON).queryParams(jsonRequest.toMap()).delete(url);

		String cookie = response.getHeader(SET_COOKIE);
		if (cookie != null) {
			token = tokenFromSetCookie(cookie);
			AuthTokenStore.put(contextKey, role, token);
		}
		logInfo(contextKey, token);
		logInfo(contextKey, "Response:" + response.getBody().asString());
		checkErrorResponse(response.getBody().asString(), url);

		return new JSONObject(response.getBody().asString()).getJSONObject(dataKey);
	}

	public static JSONObject post(String url, JSONObject jsonRequest, String contextKey) throws Exception {
		return post(url, jsonRequest, SYSTEM, contextKey);
	}

	public static Response post(String url, String requestBody, String contextKey) throws Exception {
		Response response = null;
		if (isDebugEnabled(contextKey))
			response = requestSpec(contextKey).log().all().baseUri(url).contentType(ContentType.JSON).and()
					.body(requestBody).when().post().then().log().all().extract().response();
		else
			response = requestSpec(contextKey).baseUri(url).contentType(ContentType.JSON).and().body(requestBody).when()
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
			} else {
				initToken(contextKey);
			}
		}
		boolean bDone = false;
		int nLoop = 0;
		Response response = null;

		while (!bDone) {
			String token = AuthTokenStore.get(contextKey, role);

			Cookie kukki = new Cookie.Builder(AUTHORIZATION, token).build();
			logInfo(contextKey, "Request:" + jsonRequest.toString());
			if (isDebugEnabled(contextKey))
				response = requestSpec(contextKey).log().all().cookie(kukki).contentType(ContentType.JSON).body(jsonRequest.toString())
						.post(url).then().log().all().extract().response();
			else
				response = requestSpec(contextKey).cookie(kukki).contentType(ContentType.JSON).body(jsonRequest.toString()).post(url);
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

			String token = tokenFromSetCookie(cookie);

			AuthTokenStore.put(contextKey, role, token);
		}
		if (response.getBody().asString().startsWith("{")) {
			logInfo(contextKey, "Response:" + response.getBody().asString());
			checkErrorResponse(response.getBody().asString(), url);

			return new JSONObject(response.getBody().asString()).getJSONObject(dataKey);
		} else {
			JSONObject responseJson = new JSONObject();
			responseJson.put("status", response.getBody().asString());
			return responseJson;
		}
	}

	public static JSONObject put(String url, JSONObject jsonRequest, String role, String contextKey) throws Exception {
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
		boolean bDone = false;
		int nLoop = 0;
		Response response = null;

		while (!bDone) {
			String token = AuthTokenStore.get(contextKey, role);

			Cookie kukki = new Cookie.Builder(AUTHORIZATION, token).build();
			logInfo(contextKey, "Request:" + jsonRequest.toString());
			if (isDebugEnabled(contextKey)) {
				response = requestSpec(contextKey).log().all().cookie(kukki).contentType(ContentType.JSON).body(jsonRequest.toString())
						.put(url).then().log().all().extract().response();
			} else
				response = requestSpec(contextKey).cookie(kukki).contentType(ContentType.JSON).body(jsonRequest.toString()).put(url);
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
		checkErrorResponse(response.getBody().asString(), url);
		String cookie = response.getHeader(SET_COOKIE);
		if (cookie != null) {

			String token = tokenFromSetCookie(cookie);
			AuthTokenStore.put(contextKey, role, token);

		}

		if (response.getBody().asString().startsWith("{")) {
			logInfo(contextKey, "Response: " + response.getBody().asString());
			checkErrorResponse(response.getBody().asString(), url);
			JSONObject jsonBody = new JSONObject(response.getBody().asString());

			Object data = jsonBody.opt("response"); 

			if (data instanceof JSONObject) {
				return (JSONObject) data;
			} else if (data instanceof JSONArray) {

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
		String token = AuthTokenStore.get(contextKey, role);
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
			String token = AuthTokenStore.get(contextKey, role);

			Cookie kukki = new Cookie.Builder(AUTHORIZATION, token).build();
			logInfo(contextKey, "Request:" + jsonRequest.toString());
			if (isDebugEnabled(contextKey)) {
				response = requestSpec(contextKey).log().all().cookie(kukki).contentType(ContentType.JSON).body(jsonRequest.toString())
						.put(url).then().log().all().extract().response();
			} else
				response = requestSpec(contextKey).cookie(kukki).contentType(ContentType.JSON).body(jsonRequest.toString()).put(url);
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

			String token = tokenFromSetCookie(cookie);
			AuthTokenStore.put(contextKey, role, token);

		}
		if (response.getBody().asString().startsWith("{")) {
			logInfo(contextKey, "Response:" + response.getBody().asString());
			checkErrorResponse(response.getBody().asString(), url);
			return new JSONObject(response.getBody().asString()).getJSONObject(dataKey);
		} else {
			return new JSONObject("{\"status\":\"" + response.getBody().asString() + "\"}");
		}
	}

	public static JSONObject putPreRegStatus(String url, JSONObject jsonRequest, String contextKey) throws Exception {
		String role = PREREG;
		if (!isValidToken(role, contextKey)) {
			initToken(contextKey);
		}
		boolean bDone = false;
		int nLoop = 0;
		Response response = null;

		while (!bDone) {
			String token = AuthTokenStore.get(contextKey, role);

			Cookie kukki = new Cookie.Builder(AUTHORIZATION, token).build();
			logInfo(contextKey, "Request:" + jsonRequest.toString());
			if (isDebugEnabled(contextKey)) {
				response = requestSpec(contextKey).log().all().cookie(kukki).contentType(ContentType.JSON).body(jsonRequest.toString())
						.put(url).then().log().all().extract().response();
			} else
				response = requestSpec(contextKey).cookie(kukki).contentType(ContentType.JSON).body(jsonRequest.toString()).put(url);
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

			String token = tokenFromSetCookie(cookie);

			AuthTokenStore.put(contextKey, role, token);
		}
		if (response.getBody().asString().startsWith("{")) {
			logInfo(contextKey, "Response:" + response.getBody().asString());
			checkErrorResponse(response.getBody().asString(), url);
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
			String token = AuthTokenStore.get(contextKey, role);

			Cookie kukki = new Cookie.Builder(AUTHORIZATION, token).build();
			logInfo(contextKey, "Request:" + jsonRequest.toString());
			if (isDebugEnabled(contextKey))
				response = requestSpec(contextKey).log().all().cookie(kukki).contentType(ContentType.JSON).body(jsonRequest.toString())
						.patch(url).then().log().all().extract().response();
			else
				response = requestSpec(contextKey).cookie(kukki).contentType(ContentType.JSON).body(jsonRequest.toString()).patch(url);
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

			String token = tokenFromSetCookie(cookie);

			AuthTokenStore.put(contextKey, role, token);
		}
		if (response.getBody().asString().startsWith("{")) {
			logInfo(contextKey, "Response:" + response.getBody().asString());
			checkErrorResponse(response.getBody().asString(), url);

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
					response = requestSpec(contextKey).log().all().contentType("application/json").body(jsonBody).post(url).then().log()
							.all().extract().response();
				else
					response = requestSpec(contextKey).contentType("application/json").body(jsonBody).post(url);
			} catch (Exception e) {
				logger.error(e.getMessage());
			}
			if (response == null) {
				throw new ServiceException(HttpStatus.BAD_GATEWAY, "REST_NO_RESPONSE", url);
			}
			checkErrorResponse(response.getBody().asString(), url);
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
		return RestClientAuth.initToken(contextKey);
	}

	public static boolean initToken_admin(String contextKey) {
		return RestClientAuth.initToken_admin(contextKey);
	}

	public static boolean initToken_Resident(String contextKey) {
		return RestClientAuth.initToken_Resident(contextKey);
	}

	public static boolean initToken_Regproc(String contextKey) {
		return RestClientAuth.initToken_Regproc(contextKey);
	}

	public static boolean initToken_crvs1(String contextKey) {
		return RestClientAuth.initToken_crvs1(contextKey);
	}


	static void checkErrorResponse(String response, String url) {


		if (response == null || response.trim().isEmpty()) {
			throw new ServiceException(HttpStatus.BAD_GATEWAY, "REST_NO_RESPONSE", url);
		}

		JSONObject json;
		try {
			json = new JSONObject(response);
		} catch (Exception e) {
			throw new ServiceException(
					HttpStatus.INTERNAL_SERVER_ERROR,
					"REST_INVALID_RESPONSE",
					url,
					e,
					response);

		}


		if (!json.has(errorKey)) {
			return;
		}

		Object errObject = json.get(errorKey);


		if (errObject == JSONObject.NULL) {
			return;
		}


		if (errObject instanceof JSONArray) {
			JSONArray errors = (JSONArray) errObject;
			if (errors.isEmpty()) {
				return;
			}

			JSONObject err = errors.getJSONObject(0);
			throw new ServiceException(HttpStatus.BAD_REQUEST, err.optString("errorCode", "UNKNOWN"), url, null,
					err.optString("message", err.toString()));
		}


		if (errObject instanceof JSONObject) {
			JSONObject err = (JSONObject) errObject;
			throw new ServiceException(HttpStatus.BAD_REQUEST, err.optString("errorCode", "UNKNOWN"), url, null,
					err.optString("message", err.toString()));
		}

		throw new ServiceException(HttpStatus.BAD_REQUEST, "UNKNOWN", url, null, errObject.toString());
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

			try (BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));) {

				String output;

				while ((output = br.readLine()) != null) {
					builder.append(output);
				}
			} catch (Exception e) {

			}
		}
		if (InternalApiLogging.isEnabled(null)) {
			StringBuilder reqH = new StringBuilder();
			reqH.append("Content-Type: application/json\nAccept: application/json\n");
			headers.forEach((k, v) -> reqH.append(k.toString()).append(": ").append(v.toString()).append('\n'));
			String rh = String.valueOf(conn.getHeaderFields());
			InternalApiManualRecorder.record(null, "GET", strUrl, reqH.toString(), "", http_status, rh,
					builder.toString());
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

			String token = AuthTokenStore.get(contextKey, role);

			Cookie kukki = new Cookie.Builder(AUTHORIZATION, token).build();
			Map<String, Object> mapParam = requestParams == null ? null : requestParams.toMap();
			Map<String, Object> mapPathParam = pathParam == null ? null : pathParam.toMap();

			if (isDebugEnabled(contextKey))
				response = requestSpec(contextKey).log().all().cookie(kukki).contentType(ContentType.JSON).queryParams(mapParam)
						.get(url, mapPathParam).then().log().all().extract().response();

			else
				response = requestSpec(contextKey).cookie(kukki).contentType(ContentType.JSON).queryParams(mapParam).get(url,
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
		if (response == null) {
			throw new ServiceException(HttpStatus.BAD_GATEWAY, "REST_NO_RESPONSE", url);
		}
		checkErrorResponse(response.getBody().asString(), url);

		return new JSONObject(response.getBody().asString()).getJSONArray(dataKey);
	}

	public static String rawHttp(HttpRCapture httpRCapture, String jsonBody, String contextKey) throws IOException {

		String uriStr = httpRCapture.getURI() != null ? httpRCapture.getURI().toString() : "";
		String method = httpRCapture.getMethod() != null ? httpRCapture.getMethod() : "POST";
		StringBuilder reqHeaders = new StringBuilder();
		try {
			for (org.apache.http.Header h : httpRCapture.getAllHeaders()) {
				reqHeaders.append(h.getName()).append(": ").append(h.getValue()).append('\n');
			}
		} catch (Exception ignored) {
		}

		String result = "";
		try (CloseableHttpClient httpClient = HttpClients.createDefault();) {
			httpRCapture.setEntity(new StringEntity(jsonBody));
			HttpResponse response = httpClient.execute(httpRCapture);
			int code = response.getStatusLine().getStatusCode();
			StringBuilder respHeaders = new StringBuilder();
			for (org.apache.http.Header h : response.getAllHeaders()) {
				respHeaders.append(h.getName()).append(": ").append(h.getValue()).append('\n');
			}
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				result = EntityUtils.toString(entity);
				logInfo(contextKey, result);
			}
			InternalApiManualRecorder.record(contextKey, method, uriStr, reqHeaders.toString(), jsonBody, code,
					respHeaders.toString(), result);
		} catch (IOException e) {
			InternalApiManualRecorder.recordFailure(contextKey, method, uriStr, reqHeaders.toString(), jsonBody,
					e.getMessage());
			throw e;
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

			String token = AuthTokenStore.get(contextKey, role);

			Cookie kukki = new Cookie.Builder(AUTHORIZATION, token).build();
			if (isDebugEnabled(contextKey))
				response = requestSpec(contextKey).log().all().cookie(kukki).contentType(ContentType.JSON).get(urlAct).then().log().all()
						.extract().response();
			else
				response = requestSpec(contextKey).cookie(kukki).contentType(ContentType.JSON).get(urlAct);

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
		checkErrorResponse(response.getBody().asString(), url);
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
			response = requestSpec(contextKey).log().all().contentType(ContentType.JSON).get(urlAct).then().log().all().extract()
					.response();
		else
			response = requestSpec(contextKey).contentType(ContentType.JSON).get(urlAct);

		if (response != null && response.getStatusCode() == 200) {
			checkErrorResponse(response.getBody().asString(), urlAct);
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
