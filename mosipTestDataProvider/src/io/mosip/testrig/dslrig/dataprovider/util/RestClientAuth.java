package io.mosip.testrig.dslrig.dataprovider.util;

import java.time.LocalDateTime;

import org.json.JSONObject;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;

import io.mosip.testrig.dslrig.dataprovider.preparation.MosipDataSetup;
import io.mosip.testrig.dslrig.dataprovider.variables.VariableManager;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

/**
 * MOSIP auth token acquisition and validation (extracted from {@link RestClient}).
 */
public final class RestClientAuth {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(RestClientAuth.class);

	private static final String URLBASE = "urlBase";
	private static final String AUTHORIZATION = "Authorization";
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
	private static final String ERRORCODE = "errorCode";
	private static final String DATA_KEY = "response";

	private RestClientAuth() {
	}

	public static boolean isValidToken(String role, String contextKey) {
		if (shouldForceAuthRefresh(contextKey)) {
			return false;
		}
		try {
			Object urlBase = VariableManager.getVariableValue(contextKey, URLBASE);
			if (urlBase == null) {
				return false;
			}
			String token = AuthTokenStore.get(contextKey, role);
			if (token == null || token.isEmpty()) {
				restoreAuthTokenFromRunCache(role, contextKey);
				token = AuthTokenStore.get(contextKey, role);
			}
			return token != null && !token.isEmpty() && isValidTokenOffline(token, contextKey);
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean isValidTokenOffline(String cookie, String contextKey) {
		if (cookie == null) {
			return false;
		}
		try {
			DecodedJWT decodedJWT = JWT.decode(cookie);
			long expirationTime = decodedJWT.getExpiresAt().getTime();
			if (expirationTime < System.currentTimeMillis()) {
				RestClient.logInfo(contextKey, "The token is expired");
				return false;
			}
			RestClient.logInfo(contextKey, "The token is not expired");
			return true;
		} catch (JWTDecodeException e) {
			logger.error("The token is invalid");
			return false;
		}
	}

	public static boolean initToken(String contextKey) {
		return authenticateInternal(contextKey, AuthTokenStore.ROLE_SYSTEM, "mosip.authentication.useridPwd");
	}

	public static boolean initToken_admin(String contextKey) {
		return authenticateInternal(contextKey, AuthTokenStore.ROLE_ADMIN, "mosip.authentication.useridPwd");
	}

	public static boolean initToken_Resident(String contextKey) {
		try {
			return authenticateClientSecret(contextKey, AuthTokenStore.ROLE_RESIDENT,
					buildClientSecretRequest(contextKey, "mosip_resident_app_id", "mosip_resident_client_id",
							"mosip_resident_client_secret"));
		} catch (Exception ex) {
			logger.debug("initToken_Resident failed: {}", ex.getMessage());
			return false;
		}
	}

	public static boolean initToken_Regproc(String contextKey) {
		try {
			return authenticateClientSecret(contextKey, AuthTokenStore.ROLE_REGPROC,
					buildClientSecretRequest(contextKey, "mosip_regprocclient_app_id", "mosip_regproc_client_id",
							"mosip_regproc_client_secret"));
		} catch (Exception ex) {
			logger.debug("initToken_Regproc failed: {}", ex.getMessage());
			return false;
		}
	}

	public static boolean initToken_crvs1(String contextKey) {
		try {
			return authenticateClientSecret(contextKey, AuthTokenStore.ROLE_CRVS,
					buildClientSecretRequest(contextKey, "mosip_crvs1_app_id", "mosip_crvs1_client_id",
							"mosip_crvs1_client_secret"));
		} catch (Exception ex) {
			logger.debug("initToken_crvs1 failed: {}", ex.getMessage());
			return false;
		}
	}

	private static boolean authenticateInternal(String contextKey, String role, String requestId) {
		try {
			JSONObject nestedRequest = buildAdminNestedRequest(contextKey);
			JSONObject requestBody = wrapAuthRequest(nestedRequest, requestId, new JSONObject());

			if (!shouldForceAuthRefresh(contextKey)) {
				String cached = getRunCachedAuthTokenForRequest(MosipDataSetup.AUTH_INTERNAL_USERID_PWD_PATH,
						nestedRequest, false, contextKey);
				if (cached != null && !cached.isEmpty()) {
					AuthTokenStore.put(contextKey, role, cached);
					return true;
				}
			}

			String authUrl = VariableManager.getVariableValue(contextKey, URLBASE).toString().trim()
					+ VariableManager.getVariableValue(VariableManager.NS_DEFAULT, "authManagerURL").toString().trim();
			Response response = postJson(contextKey, authUrl, requestBody.toString());
			if (response.getStatusCode() != 200 || response.toString().contains(ERRORCODE)) {
				notifyAuthFailure(contextKey, authUrl);
				return false;
			}
			String token = new JSONObject(response.getBody().asString()).getJSONObject(DATA_KEY).getString("token");
			AuthTokenStore.put(contextKey, role, token);
			cacheAuthTokenForRequest(MosipDataSetup.AUTH_INTERNAL_USERID_PWD_PATH, nestedRequest, false, contextKey,
					token);
			return true;
		} catch (Exception ex) {
			logger.debug("authenticateInternal failed for role {}: {}", role, ex.getMessage());
			return false;
		}
	}

	private static boolean authenticateClientSecret(String contextKey, String role, JSONObject nestedRequest) {
		try {
			JSONObject requestBody = wrapAuthRequest(nestedRequest, "string", new JSONObject());

			if (!shouldForceAuthRefresh(contextKey)) {
				String cached = getRunCachedAuthTokenForRequest(MosipDataSetup.AUTH_CLIENT_ID_SECRET_PATH,
						nestedRequest, true, contextKey);
				if (cached != null && !cached.isEmpty()) {
					AuthTokenStore.put(contextKey, role, cached);
					return true;
				}
			}

			String authUrl = VariableManager.getVariableValue(contextKey, URLBASE).toString().trim()
					+ MosipDataSetup.AUTH_CLIENT_ID_SECRET_PATH;
			Response response = postJson(contextKey, authUrl, requestBody.toString());
			if (response == null || response.getStatusCode() != 200 || response.toString().contains(ERRORCODE)) {
				notifyAuthFailure(contextKey, authUrl);
				return false;
			}
			String token = response.getCookie(AUTHORIZATION);
			AuthTokenStore.put(contextKey, role, token);
			cacheAuthTokenForRequest(MosipDataSetup.AUTH_CLIENT_ID_SECRET_PATH, nestedRequest, true, contextKey, token);
			RestClient.checkErrorResponse(response.getBody().asString(), authUrl);
			return true;
		} catch (Exception ex) {
			logger.debug("authenticateClientSecret failed for role {}: {}", role, ex.getMessage());
			return false;
		}
	}

	private static JSONObject buildAdminNestedRequest(String contextKey) throws Exception {
		JSONObject nestedRequest = new JSONObject();
		nestedRequest.put(USERNAME, VariableManager.getVariableValue(contextKey, "admin_userName").toString());
		nestedRequest.put(PASSWORD, VariableManager.getVariableValue(contextKey, "admin_password").toString());
		nestedRequest.put(APPID, VariableManager.getVariableValue(contextKey, "mosip_admin_app_id").toString());
		nestedRequest.put(CLIENTID, VariableManager.getVariableValue(contextKey, "mosip_admin_client_id").toString());
		nestedRequest.put("clientSecret",
				VariableManager.getVariableValue(contextKey, "mosip_admin_client_secret").toString());
		return nestedRequest;
	}

	private static JSONObject buildClientSecretRequest(String contextKey, String appIdKey, String clientIdKey,
			String secretKey) throws Exception {
		JSONObject nestedRequest = new JSONObject();
		nestedRequest.put(USERNAME, VariableManager.getVariableValue(contextKey, "operatorId"));
		nestedRequest.put(PASSWORD, VariableManager.getVariableValue(contextKey, PASSWORD));
		nestedRequest.put(APPID, VariableManager.getVariableValue(contextKey, appIdKey));
		nestedRequest.put(CLIENTID, VariableManager.getVariableValue(contextKey, clientIdKey));
		nestedRequest.put("secretKey", VariableManager.getVariableValue(contextKey, secretKey));
		return nestedRequest;
	}

	private static JSONObject wrapAuthRequest(JSONObject nestedRequest, String requestId, Object metadata) {
		JSONObject requestBody = new JSONObject();
		requestBody.put(METADATA, metadata);
		requestBody.put(VERSION, metadata instanceof JSONObject ? "1.0" : "string");
		requestBody.put("id", requestId);
		requestBody.put(REQUESTTIME, CommonUtil.getUTCDateTime(LocalDateTime.now()).toString());
		requestBody.put(REQUEST, nestedRequest);
		return requestBody;
	}

	private static Response postJson(String contextKey, String authUrl, String jsonBody) throws Exception {
		RestClient.logInfo(contextKey, contextKey + AUTHURL + authUrl + jsonBody);
		RequestSpecification spec = RestClient.requestSpec(contextKey).contentType("application/json").body(jsonBody);
		if (RestClient.isDebugEnabled(contextKey)) {
			return spec.log().all().post(authUrl).then().log().all().extract().response();
		}
		Response response = spec.post(authUrl);
		if (response == null) {
			throw new ServiceException(org.springframework.http.HttpStatus.BAD_GATEWAY, "REST_NO_RESPONSE", authUrl);
		}
		RestClient.checkErrorResponse(response.getBody().asString(), authUrl);
		return response;
	}

	private static void notifyAuthFailure(String contextKey, String authUrl) {
		try {
			boolean slack = VariableManager.getVariableValue(contextKey, POST2SLACK) != null
					&& Boolean.parseBoolean(VariableManager.getVariableValue(contextKey, POST2SLACK).toString());
			if (slack) {
				SlackIt.postMessage(null, authUrl + " Failed to authenticate, Is "
						+ VariableManager.getVariableValue(contextKey, URLBASE).toString() + " down ?");
			}
		} catch (Exception ignored) {
			// optional notification
		}
	}

	private static boolean shouldForceAuthRefresh(String contextKey) {
		try {
			Object obj = VariableManager.getVariableValue(contextKey, "urlSwitched");
			if (obj != null) {
				return Boolean.parseBoolean(obj.toString());
			}
		} catch (Exception ignored) {
			// allow run-cache reuse
		}
		return false;
	}

	private static void restoreAuthTokenFromRunCache(String role, String contextKey) {
		String token = getRunCachedAuthTokenForRole(role, contextKey);
		if (token != null && !token.isEmpty()) {
			AuthTokenStore.put(contextKey, role, token);
		}
	}

	private static String getRunCachedAuthTokenForRole(String role, String contextKey) {
		JSONObject nestedRequest = buildNestedRequestForRole(role, contextKey);
		if (nestedRequest == null) {
			return null;
		}
		boolean clientSecretAuth = isClientSecretAuthRole(role);
		String authPath = clientSecretAuth ? MosipDataSetup.AUTH_CLIENT_ID_SECRET_PATH
				: MosipDataSetup.AUTH_INTERNAL_USERID_PWD_PATH;
		return getRunCachedAuthTokenForRequest(authPath, nestedRequest, clientSecretAuth, contextKey);
	}

	private static boolean isClientSecretAuthRole(String role) {
		return AuthTokenStore.ROLE_RESIDENT.equals(role) || AuthTokenStore.ROLE_REGPROC.equals(role)
				|| AuthTokenStore.ROLE_CRVS.equals(role);
	}

	private static JSONObject buildNestedRequestForRole(String role, String contextKey) {
		try {
			if (AuthTokenStore.ROLE_ADMIN.equals(role) || AuthTokenStore.ROLE_SYSTEM.equals(role)) {
				return buildAdminNestedRequest(contextKey);
			}
			if (AuthTokenStore.ROLE_RESIDENT.equals(role)) {
				return buildClientSecretRequest(contextKey, "mosip_resident_app_id", "mosip_resident_client_id",
						"mosip_resident_client_secret");
			}
			if (AuthTokenStore.ROLE_REGPROC.equals(role)) {
				return buildClientSecretRequest(contextKey, "mosip_regprocclient_app_id", "mosip_regproc_client_id",
						"mosip_regproc_client_secret");
			}
			if (AuthTokenStore.ROLE_CRVS.equals(role)) {
				return buildClientSecretRequest(contextKey, "mosip_crvs1_app_id", "mosip_crvs1_client_id",
						"mosip_crvs1_client_secret");
			}
		} catch (Exception e) {
			logger.debug("Could not build auth request for role {}: {}", role, e.getMessage());
		}
		return null;
	}

	private static String getRunCachedAuthTokenForRequest(String authPath, JSONObject nestedRequest,
			boolean clientSecretAuth, String contextKey) {
		String cacheKey = MosipDataSetup.authCacheKey(authPath, credFingerprint(nestedRequest, clientSecretAuth));
		Object cached = MosipDataSetup.getCache(cacheKey, MosipDataSetup.getRunContextNamespace(contextKey));
		return cached instanceof String ? (String) cached : null;
	}

	private static void cacheAuthTokenForRequest(String authPath, JSONObject nestedRequest, boolean clientSecretAuth,
			String contextKey, String token) {
		if (token == null || token.isEmpty()) {
			return;
		}
		String cacheKey = MosipDataSetup.authCacheKey(authPath, credFingerprint(nestedRequest, clientSecretAuth));
		MosipDataSetup.setCache(cacheKey, token, MosipDataSetup.getRunContextNamespace(contextKey));
	}

	private static String credFingerprint(JSONObject nestedRequest, boolean clientSecretAuth) {
		StringBuilder sb = new StringBuilder();
		appendJsonField(sb, nestedRequest, USERNAME);
		appendJsonField(sb, nestedRequest, PASSWORD);
		appendJsonField(sb, nestedRequest, APPID);
		appendJsonField(sb, nestedRequest, CLIENTID);
		if (clientSecretAuth) {
			appendJsonField(sb, nestedRequest, "secretKey");
		}
		appendJsonField(sb, nestedRequest, "clientSecret");
		return Integer.toHexString(sb.toString().hashCode());
	}

	private static void appendJsonField(StringBuilder sb, JSONObject nestedRequest, String key) {
		if (nestedRequest.has(key) && !nestedRequest.isNull(key)) {
			sb.append(key).append('=').append(nestedRequest.get(key)).append(';');
		}
	}
}
