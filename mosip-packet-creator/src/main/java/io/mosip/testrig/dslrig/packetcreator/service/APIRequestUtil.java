package io.mosip.testrig.dslrig.packetcreator.service;

import static io.restassured.RestAssured.given;

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import io.mosip.testrig.dslrig.dataprovider.preparation.MosipDataSetup;
import io.mosip.testrig.dslrig.dataprovider.util.AuthTokenStore;
import io.mosip.testrig.dslrig.dataprovider.util.RestClient;
import io.mosip.testrig.dslrig.dataprovider.util.SlackIt;
import io.mosip.testrig.dslrig.dataprovider.variables.VariableManager;
import io.restassured.http.ContentType;
import io.restassured.http.Cookie;
import io.restassured.response.Response;

@Component
public class APIRequestUtil {

    private static final String UNDERSCORE = "_";
    private static final String SERVER_API_TRACE_KEY = "packetCreator.serverApiTrace";
    private static final int MAX_SERVER_API_TRACE_ENTRIES = 50;
    private static final int MAX_SERVER_API_TRACE_VALUE_LENGTH = 10000;

    Logger logger = LoggerFactory.getLogger(APIRequestUtil.class);
	private static final String DATEFORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";


    @Value("${mosip.test.regclient.userid}")
    private String operatorId;

    @Value("${mosip.test.regclient.password}")
    private String password;

    @Value("${mosip.test.regclient.clientid}")
    private String clientId;

    @Value("${mosip.test.regclient.appId}")
    private String appId;

    @Value("${mosip.test.regclient.secretkey}")
    private String secretKey;

    @Value("${mosip.test.authmanager.url}")
    private String authManagerURL;

    @Value("${mosip.test.authmanager.prereg.url:/v1/authmanager/authenticate/clientidsecretkey}")
    private String preregAuthManagerURL;

    @Value("${mosip.test.jwks.url}")
    private String jwksUrl;

    @Value("${mosip.test.jwt.issuer}")
    private String jwtIssuer;

    final String dataKey = "response";
    final String errorKey = "errors";

    @Value("${mosip.test.baseurl}")
    private String baseUrl;

    @Value("${mosip.test.post2slack}")
    private boolean bSlackit;

    @Autowired
    ContextUtils contextUtils;

	void loadContext(String context) {
		Properties props = contextUtils.loadServerContext(context);
		props.forEach((k, v) -> {
			if (k.equals("mosip.test.regclient.userid"))
				operatorId = v.toString();
			else if (k.equals("mosip.test.regclient.password"))
				password = v.toString();
			else if (k.equals("mosip.test.authmanager.url"))
				authManagerURL = v.toString();
			else if (k.equals("mosip.test.regclient.clientid"))
				clientId = v.toString();
			else if (k.equals("mosip.test.regclient.appId"))
				appId = v.toString();
			else if (k.equals("mosip.test.regclient.secretkey"))
				secretKey = v.toString();
			else if (k.equals("mosip.test.baseurl"))
				baseUrl = v.toString();

		});

	}

    /**
     * @deprecated Use {@link #clearRunScopedCache(String)} — global token clearing is unsafe under parallel runs.
     */
    @Deprecated
    public void clearToken() {
    	throw new UnsupportedOperationException(
    			"Global clearToken() clears all contexts; use clearRunScopedCache(contextKey) instead.");
    }

    public void clearRunScopedCache(String contextKey) {
    	RestClient.clearRunScopedCache(contextKey);
    }

    private boolean shouldForceAuthRefresh(String contextKey) {
    	try {
    		Object obj = VariableManager.getVariableValue(contextKey, "urlSwitched");
    		if (obj != null) {
    			return Boolean.parseBoolean(obj.toString());
    		}
    	} catch (Exception ignored) {
    		// default: allow run-cache reuse
    	}
    	return false;
    }

    public void resetServerApiTrace(String contextKey) {
        if (contextKey == null || contextKey.isBlank()) {
            return;
        }
        VariableManager.removeVariableValue(contextKey, SERVER_API_TRACE_KEY);
    }

    public JSONArray getServerApiTrace(String contextKey) {
        if (contextKey == null || contextKey.isBlank()) {
            return new JSONArray();
        }
        Object traceObj = MosipDataSetup.fromCacheValue(
                VariableManager.getVariableValue(contextKey, SERVER_API_TRACE_KEY));
        if (traceObj instanceof JSONArray) {
            return new JSONArray(((JSONArray) traceObj).toString());
        }
        if (traceObj instanceof String stored && !stored.isBlank()) {
            return new JSONArray(stored);
        }
        return new JSONArray();
    }

    public JSONArray consumeServerApiTrace(String contextKey) {
        JSONArray trace = getServerApiTrace(contextKey);
        resetServerApiTrace(contextKey);
        return trace;
    }

    private String trimTraceString(String value) {
        if (value == null || value.length() <= MAX_SERVER_API_TRACE_VALUE_LENGTH) {
            return value;
        }
        return value.substring(0, MAX_SERVER_API_TRACE_VALUE_LENGTH) + "...[truncated]";
    }

    private Object limitTraceValue(Object value) {
        if (value == null || value == JSONObject.NULL) {
            return JSONObject.NULL;
        }
        if (value instanceof JSONObject || value instanceof JSONArray) {
            String serialized = value.toString();
            if (serialized.length() <= MAX_SERVER_API_TRACE_VALUE_LENGTH) {
                return value;
            }
            return trimTraceString(serialized);
        }
        return trimTraceString(String.valueOf(value));
    }

    private JSONArray limitTraceEntries(JSONArray trace) {
        if (trace.length() <= MAX_SERVER_API_TRACE_ENTRIES) {
            return trace;
        }
        JSONArray limited = new JSONArray();
        int startIndex = trace.length() - MAX_SERVER_API_TRACE_ENTRIES;
        for (int i = startIndex; i < trace.length(); i++) {
            limited.put(trace.get(i));
        }
        return limited;
    }

    private void addServerApiTrace(String contextKey, String method, String url, Object request,
            Map<String, String> headers, Response response) {
        try {
            if (contextKey == null || contextKey.isBlank()) {
                return;
            }
            JSONArray trace = getServerApiTrace(contextKey);
            JSONObject entry = new JSONObject();
            entry.put("method", method);
            entry.put("url", url);
            entry.put("request", limitTraceValue(request));
            entry.put("headers", headers == null ? new JSONObject() : limitTraceValue(new JSONObject(headers)));
            entry.put("statusCode", response == null ? JSONObject.NULL : response.getStatusCode());
            entry.put("response", response == null ? JSONObject.NULL : trimTraceString(response.getBody().asString()));
            trace.put(entry);
            VariableManager.setVariableValue(contextKey, SERVER_API_TRACE_KEY,
                    MosipDataSetup.toCacheValue(limitTraceEntries(trace)));
        } catch (Exception ex) {
            logger.warn("Failed to record server API trace", ex);
        }
    }
    public JSONObject get(String baseUrl,String url, JSONObject requestParams, JSONObject pathParam,String contextKey) throws Exception {
    	this.baseUrl = baseUrl;
    	logger.info(url);
    	if (!isValidToken(contextKey)){
            initToken(contextKey);
        }
    	boolean bDone = false;
    	int nLoop  = 0;
    	Response response =null;

    	while(!bDone) {

    		Cookie kukki = new Cookie.Builder("Authorization", AuthTokenStore.get(contextKey, AuthTokenStore.ROLE_SYSTEM)).build();
    		response = given().cookie(kukki).contentType(ContentType.JSON).queryParams(requestParams.toMap()).get(url,pathParam.toMap());
    		if(response.getStatusCode() == 401) {
    			if(nLoop >= 1)
    				bDone = true;
    			else {
    				initToken(contextKey);
    				nLoop++;
    			}
    		}
    		else
    			bDone = true;
    	}
        if (response == null) {
            throw new Exception("No response received for GET " + url);
        }
        addServerApiTrace(contextKey, "GET", url,
                new JSONObject().put("queryParams", requestParams == null ? new JSONObject() : requestParams)
                        .put("pathParams", pathParam == null ? new JSONObject() : pathParam),
                null, response);

        checkErrorResponse(response.getBody().asString());

        return new JSONObject(response.getBody().asString()).getJSONObject(dataKey);
    }
    public JSONObject getJsonObject(String baseUrl,String url, JSONObject requestParams, JSONObject pathParam,String contextKey) throws Exception {
    	this.baseUrl = baseUrl;
    	logger.info(url);
    	if (!isValidToken(contextKey)){
            initToken(contextKey);
        }
    	boolean bDone = false;
    	int nLoop  = 0;
    	Response response =null;

    	while(!bDone) {

    		Cookie kukki = new Cookie.Builder("Authorization", AuthTokenStore.get(contextKey, AuthTokenStore.ROLE_SYSTEM)).build();
    		response = given().cookie(kukki).contentType(ContentType.JSON).queryParams(requestParams.toMap()).get(url,pathParam.toMap());
    		if(response.getStatusCode() == 401) {
    			if(nLoop >= 1)
    				bDone = true;
    			else {
    				initToken(contextKey);
    				nLoop++;
    			}
    		}
    		else
    			bDone = true;
    	}
        if (response == null) {
            throw new Exception("No response received for GET " + url);
        }
        addServerApiTrace(contextKey, "GET", url,
                new JSONObject().put("queryParams", requestParams == null ? new JSONObject() : requestParams)
                        .put("pathParams", pathParam == null ? new JSONObject() : pathParam),
                null, response);

        checkErrorResponse(response.getBody().asString());

        return new JSONObject(response.getBody().asString());
    }
    public JSONObject getPreReg(String baseUrl,String url, JSONObject requestParams, JSONObject pathParam,String contextKey) throws Exception {
    	this.baseUrl = baseUrl;
    	if (!isValidToken(contextKey)){


        		initToken_prereg(contextKey);

        }

    	boolean bDone = false;
    	int nLoop  = 0;
    	Response response =null;

    	while(!bDone) {

    		Cookie kukki = new Cookie.Builder("Authorization",AuthTokenStore.get(contextKey, AuthTokenStore.ROLE_SYSTEM)).build();
    		response = given().cookie(kukki).contentType(ContentType.JSON).queryParams(requestParams.toMap()).get(url,pathParam.toMap());
    		if(response.getStatusCode() == 401) {
    			if(nLoop >= 1)
    				bDone = true;
    			else {

    				initToken_prereg(contextKey);
    				nLoop++;
    			}
    		}
    		else
    			bDone = true;
    	}
        if (response == null) {
            throw new Exception("No response received for GET " + url);
        }
        addServerApiTrace(contextKey, "GET", url,
                new JSONObject().put("queryParams", requestParams == null ? new JSONObject() : requestParams)
                        .put("pathParams", pathParam == null ? new JSONObject() : pathParam),
                null, response);

        checkErrorResponse(response.getBody().asString());

        return new JSONObject(response.getBody().asString()).getJSONObject(dataKey);
    }


    public JSONObject post(String baseUrl,String url, JSONObject jsonRequest,String contextKey) throws Exception {
    	this.baseUrl = baseUrl;

    	if (!isValidToken(contextKey)){
            initToken(contextKey);
        }

    	boolean bDone = false;
    	int nLoop  = 0;
    	Response response =null;

    	while(!bDone) {
    		Cookie kukki = new Cookie.Builder("Authorization", AuthTokenStore.get(contextKey, AuthTokenStore.ROLE_SYSTEM)).build();
    		response = given().cookie(kukki).contentType(ContentType.JSON).body(jsonRequest.toString()).post(url);
    		if(response.getStatusCode() == 401) {
    			if(nLoop >= 1)
    				bDone = true;
    			else {
    				initToken(contextKey);
    				nLoop++;
    			}
    		}
    		else
    			bDone = true;
    	}
        if (response == null) {
            throw new Exception("No response received for POST " + url);
        }
        addServerApiTrace(contextKey, "POST", url, jsonRequest, null, response);
        checkErrorResponse(response.getBody().asString());

        return new JSONObject(response.getBody().asString()).getJSONObject(dataKey);
    }


    public JSONArray syncRid(String baseUrl,String url, String requestBody, String timestamp,String contextKey) throws Exception {
    	this.baseUrl = baseUrl;
		String centerId= VariableManager.getVariableValue(contextKey, "mosip.test.regclient.centerid").toString();
		String machineId=VariableManager.getVariableValue(contextKey, "machineid").toString();
    	loadContext(contextKey);
    	 AuthTokenStore.remove(contextKey, AuthTokenStore.ROLE_SYSTEM);
    	if (!isValidToken(contextKey)){
            initToken(contextKey);
        }

    	boolean bDone = false;
    	int nLoop  = 0;
    	Response response =null;

    	while(!bDone) {

    		ObjectMapper objectMapper = new ObjectMapper();
    		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    		objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    		String outputJson = objectMapper.writeValueAsString(requestBody);

            String authToken = AuthTokenStore.get(contextKey, AuthTokenStore.ROLE_SYSTEM);
    		Cookie kukki = new Cookie.Builder("Authorization", authToken).build();
    		response = given().cookie(kukki)
                .header("timestamp", timestamp)
                .header("Center-Machine-RefId", centerId + UNDERSCORE + machineId)
                .contentType(ContentType.JSON).body(outputJson).post(url);

    		if(response.getStatusCode() == 401) {
    			if(nLoop >= 1)
    				bDone = true;
    			else {
    				initToken(contextKey);
    				nLoop++;
    			}
    		}
    		else
    			bDone = true;
    	}
        if (response == null) {
            throw new Exception("No response received for POST " + url);
        }
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("timestamp", timestamp);
        headers.put("Center-Machine-RefId", centerId + UNDERSCORE + machineId);
        addServerApiTrace(contextKey, "POST", url, requestBody, headers, response);
        checkErrorResponse(response.getBody().asString());

        return new JSONObject(response.getBody().asString()).getJSONArray(dataKey);
    }

    public JSONObject uploadFile(String baseUrl,String url, String filePath, String contextKey) throws Exception {
    	this.baseUrl = baseUrl;


    	loadContext(contextKey);
    	AuthTokenStore.remove(contextKey, AuthTokenStore.ROLE_SYSTEM);


    	if (!isValidToken(contextKey)){
            initToken(contextKey);
        }
    	File f = new File(filePath);

        Cookie kukki = new Cookie.Builder("Authorization", AuthTokenStore.get(contextKey, AuthTokenStore.ROLE_SYSTEM)
            	).build();
        Response response = given().cookie(kukki).multiPart("file", f.getCanonicalFile()).post(url);
        addServerApiTrace(contextKey, "POST", url,
                new JSONObject().put("filePath", filePath),
                null, response);
        checkErrorResponse(response.getBody().asString());

        return new JSONObject(response.getBody().asString()).getJSONObject(dataKey);
    }

    private boolean isValidToken(String contextKey) throws Exception {
    	if (shouldForceAuthRefresh(contextKey)) {
    		return false;
    	}
    	String token = AuthTokenStore.get(contextKey, AuthTokenStore.ROLE_SYSTEM);
    	if (token == null || token.isEmpty()) {
    		token = getRunCachedInternalAuthToken(contextKey);
    		if (token != null && !token.isEmpty()) {
    			AuthTokenStore.put(contextKey, AuthTokenStore.ROLE_SYSTEM, token);
    		}
    	}
    	if (token == null || token.isEmpty()) {
    		return false;
    	}
    	return RestClient.isValidTokenOffline(token, contextKey);
    }

    private JSONObject buildInternalUseridPwdRequest(String contextKey) {
    	JSONObject nestedRequest = new JSONObject();
    	nestedRequest.put("userName", VariableManager.getVariableValue(contextKey,"admin_userName").toString());
    	nestedRequest.put("password", VariableManager.getVariableValue(contextKey,"admin_password").toString());
    	nestedRequest.put("appId", VariableManager.getVariableValue(contextKey,"mosip_admin_app_id").toString());
    	nestedRequest.put("clientId", VariableManager.getVariableValue(contextKey,"mosip_admin_client_id").toString());
    	nestedRequest.put("clientSecret", VariableManager.getVariableValue(contextKey,"mosip_admin_client_secret").toString());
    	return nestedRequest;
    }

    private String getRunCachedInternalAuthToken(String contextKey) {
    	JSONObject nestedRequest = buildInternalUseridPwdRequest(contextKey);
    	String cacheKey = MosipDataSetup.authCacheKey(MosipDataSetup.AUTH_INTERNAL_USERID_PWD_PATH,
    			credFingerprint(nestedRequest));
    	Object cached = MosipDataSetup.getCache(cacheKey, MosipDataSetup.getRunContextNamespace(contextKey));
    	return cached instanceof String ? (String) cached : null;
    }

    private void cacheInternalAuthToken(String contextKey, JSONObject nestedRequest, String token) {
    	if (token == null || token.isEmpty()) {
    		return;
    	}
    	String cacheKey = MosipDataSetup.authCacheKey(MosipDataSetup.AUTH_INTERNAL_USERID_PWD_PATH,
    			credFingerprint(nestedRequest));
    	MosipDataSetup.setCache(cacheKey, token, MosipDataSetup.getRunContextNamespace(contextKey));
    }

    private static String credFingerprint(JSONObject nestedRequest) {
    	StringBuilder sb = new StringBuilder();
    	for (String key : new String[] { "userName", "password", "appId", "clientId", "clientSecret" }) {
    		if (nestedRequest.has(key) && !nestedRequest.isNull(key)) {
    			sb.append(key).append('=').append(nestedRequest.get(key)).append(';');
    		}
    	}
    	return Integer.toHexString(sb.toString().hashCode());
    }


    public boolean initToken_prereg(String contextKey){
        try {	


			JSONObject requestBody = new JSONObject();
			JSONObject nestedRequest = buildInternalUseridPwdRequest(contextKey);

			String urlBase = VariableManager.getVariableValue(contextKey,"urlBase").toString().trim();
			if (!shouldForceAuthRefresh(contextKey)) {
				String cachedToken = getRunCachedInternalAuthToken(contextKey);
				if (cachedToken != null && !cachedToken.isEmpty()) {
					AuthTokenStore.put(contextKey, AuthTokenStore.ROLE_SYSTEM, cachedToken);
					return true;
				}
			}

			requestBody.put("metadata", new JSONObject());
			requestBody.put("version", "1.0");
			requestBody.put("id", "mosip.authentication.useridPwd");
			requestBody.put("requesttime", getUTCDateTime(LocalDateTime.now()));
			requestBody.put("request", nestedRequest);


            Response response = given().contentType("application/json").body(requestBody.toString()).post(baseUrl + authManagerURL);
            addServerApiTrace(contextKey, "POST", baseUrl + authManagerURL, requestBody, null, response);

			if(RestClient.isDebugEnabled(contextKey))
            logger.info("Authtoken generation request response: {}", response.asString());
			if(response.getStatusCode() == 401) {
				throw new Exception("401 - Unauthorized");

			}
            if (response.getStatusCode() != 200 ||  response.toString().contains("errorCode")) {
            	if(bSlackit)
            		SlackIt.postMessage(null,
            				baseUrl + authManagerURL + " Failed to authenticate, Is " + baseUrl + " down ?");

            	return false;
            }
           String token = new JSONObject(response.getBody().asString()).getJSONObject(dataKey).getString("token");

           AuthTokenStore.put(contextKey, AuthTokenStore.ROLE_SYSTEM, token);
           cacheInternalAuthToken(contextKey, nestedRequest, token);


			return true;	
		}
		catch(Exception  ex){
            logger.error("",ex);
            if(bSlackit)
        		SlackIt.postMessage(null,
        				baseUrl + authManagerURL + " Failed to authenticate, Is " + baseUrl + " down ?");

            return false;
		}
    }


    public boolean initToken(String contextKey){
        try {	


        	if(VariableManager.isInit()) {
	        	Object o =VariableManager.getVariableValue(contextKey,"operatorId");
	        	if(o != null)
	        		operatorId = o.toString();

	        	o =VariableManager.getVariableValue(contextKey,"password");

	        	if(o != null)
	        		password = o.toString();
	        }

			JSONObject requestBody = new JSONObject();
			JSONObject nestedRequest = buildInternalUseridPwdRequest(contextKey);

			String urlBase = VariableManager.getVariableValue(contextKey,"urlBase").toString().trim();
			if (!shouldForceAuthRefresh(contextKey)) {
				String cachedToken = getRunCachedInternalAuthToken(contextKey);
				if (cachedToken != null && !cachedToken.isEmpty()) {
					AuthTokenStore.put(contextKey, AuthTokenStore.ROLE_SYSTEM, cachedToken);
					return true;
				}
			}

			requestBody.put("metadata", "");
			requestBody.put("version", "1.0");
			requestBody.put("id", "test");
			requestBody.put("requesttime", getUTCDateTime(LocalDateTime.now()));
			requestBody.put("request", nestedRequest);


            Response response = given().contentType("application/json").body(requestBody.toString()).post(baseUrl + authManagerURL);
            addServerApiTrace(contextKey, "POST", baseUrl + authManagerURL, requestBody, null, response);
            if(RestClient.isDebugEnabled(contextKey))
            logger.info("Authtoken generation request response: {}", response.asString());
			if(response.getStatusCode() == 401) {
				throw new Exception("401 - Unauthorized");

			}
            if (response.getStatusCode() != 200 ||  response.toString().contains("errorCode")) {
            	if(bSlackit)
            		SlackIt.postMessage(null,
            				baseUrl + authManagerURL + " Failed to authenticate, Is " + baseUrl + " down ?");

            	return false;
            }
            String token=null;
        token= new JSONObject(response.getBody().asString()).getJSONObject(dataKey).getString("token");


            AuthTokenStore.put(contextKey, AuthTokenStore.ROLE_SYSTEM, token);
            cacheInternalAuthToken(contextKey, nestedRequest, token);
			return true;	
		}
		catch(Exception  ex){
            logger.error("",ex);
            if(bSlackit)
        		SlackIt.postMessage(null,
        				baseUrl + authManagerURL + " Failed to authenticate, Is " + baseUrl + " down ?");

            return false;
		}
    }


    public static String getUTCDateTime(LocalDateTime time) {
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(DATEFORMAT);
        if (time == null){
            time = LocalDateTime.now(TimeZone.getTimeZone("UTC").toZoneId());
        }  
		String utcTime = time.format(dateFormat);
		return utcTime;
    }


    public static String getUTCDate(LocalDateTime date) {
		String DATEFORMAT = "yyyy-MM-dd";
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(DATEFORMAT);
        if (date == null){
            date = LocalDateTime.now(TimeZone.getTimeZone("UTC").toZoneId());
        }  
		String utcTime = date.format(dateFormat);
		return utcTime;
	}

	private void checkErrorResponse(String response) throws Exception {
		 logger.info("Check Error Responce : " +response);

        JSONObject jsonObject =  new JSONObject(response);
        if(jsonObject.get(errorKey) != JSONObject.NULL) {
        JSONArray arr=(JSONArray) jsonObject.get(errorKey);

        if(jsonObject.has(errorKey)  && !arr.isEmpty()) {
            throw new Exception(String.valueOf(jsonObject.get(errorKey)));
        }}
    }

	public static String getAdjustedUTCDate(String offsetValue) {

        ZonedDateTime utcDate = ZonedDateTime.now(ZoneOffset.UTC);

        if (offsetValue == null || offsetValue.isBlank()) {
            throw new IllegalArgumentException("Offset value cannot be empty");
        }


        char sign = offsetValue.charAt(0);
        char unit = offsetValue.charAt(offsetValue.length() - 1);

        int value = Integer.parseInt(
                offsetValue.substring(1, offsetValue.length() - 1)
        );


        if (sign == '-') {
            value = -value;
        }

        switch (unit) {
            case 'y':
                utcDate = utcDate.plusYears(value);
                break;

            case 'm':
                utcDate = utcDate.plusMonths(value);
                break;

            case 'd':
                utcDate = utcDate.plusDays(value);
                break;

            default:
                throw new IllegalArgumentException(
                        "Unsupported offset unit: " + unit);
        }

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern(DATEFORMAT);

        return utcDate.format(formatter);
    }

}
