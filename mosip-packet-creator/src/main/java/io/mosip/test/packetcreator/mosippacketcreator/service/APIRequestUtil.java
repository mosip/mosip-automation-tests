package io.mosip.test.packetcreator.mosippacketcreator.service;

import java.io.File;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.TimeZone;

import javax.annotation.PostConstruct;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.json.JSONArray;
import org.json.JSONObject;
import org.mosip.dataprovider.util.CommonUtil;
import org.mosip.dataprovider.util.SlackIt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.restassured.http.ContentType;
import io.restassured.http.Cookie;
import io.restassured.response.Response;
import variables.VariableManager;

import static io.restassured.RestAssured.given;

@Component
public class APIRequestUtil {

    private static final String UNDERSCORE = "_";

    Logger logger = LoggerFactory.getLogger(APIRequestUtil.class);

   // private ConfigurableJWTProcessor<SecurityContext> jwtProcessor = null;

    String token;
    String preregToken;
    
    String refreshToken;

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
    
    @Value("${mosip.test.regclient.centerid}")
    private String centerId;

    @Value("${mosip.test.regclient.machineid}")
    private String machineId;

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
    	props.forEach( (k,v) ->{
    		if(k.equals("mosip.test.regclient.userid"))
    			operatorId = v.toString();
    		else
    		if(k.equals("mosip.test.regclient.centerid"))
    			centerId = v.toString();
    		else
        	if(k.equals("mosip.test.regclient.machineid"))
        		machineId = v.toString();
        	else
            if(k.equals("mosip.test.regclient.password"))
            	password = v.toString();
                			
    	});
    	
    }

    public void clearToken() {
    	token =null;
    	preregToken = null;
    }
    public JSONObject get(String baseUrl,String url, JSONObject requestParams, JSONObject pathParam) throws Exception {
    	this.baseUrl = baseUrl;
    	
    	if (!isValidToken()){
            initToken();
        }
    	boolean bDone = false;
    	int nLoop  = 0;
    	Response response =null;

    	while(!bDone) {

    		Cookie kukki = new Cookie.Builder("Authorization", token).build();
    		response = given().cookie(kukki).contentType(ContentType.JSON).queryParams(requestParams.toMap()).get(url,pathParam.toMap());
    		if(response.getStatusCode() == 401) {
    			if(nLoop >= 1)
    				bDone = true;
    			else {
    				initToken();
    				nLoop++;
    			}
    		}
    		else
    			bDone = true;
    	}

        checkErrorResponse(response.getBody().asString());

        return new JSONObject(response.getBody().asString()).getJSONObject(dataKey);
    }

    public JSONObject getPreReg(String baseUrl,String url, JSONObject requestParams, JSONObject pathParam) throws Exception {
    	this.baseUrl = baseUrl;
    	
    	if(preregToken == null) {
    	
            //initPreregToken();
    		initToken_prereg();
        }
    	boolean bDone = false;
    	int nLoop  = 0;
    	Response response =null;

    	while(!bDone) {

    		Cookie kukki = new Cookie.Builder("Authorization",preregToken).build();
    		response = given().cookie(kukki).contentType(ContentType.JSON).queryParams(requestParams.toMap()).get(url,pathParam.toMap());
    		if(response.getStatusCode() == 401) {
    			if(nLoop >= 1)
    				bDone = true;
    			else {
    				//initPreregToken();
    				initToken_prereg();
    				nLoop++;
    			}
    		}
    		else
    			bDone = true;
    	}

        checkErrorResponse(response.getBody().asString());

        return new JSONObject(response.getBody().asString()).getJSONObject(dataKey);
    }


    public JSONObject post(String baseUrl,String url, JSONObject jsonRequest) throws Exception {
    	this.baseUrl = baseUrl;
    	
    	if (!isValidToken()){
            initToken();
        }

    	boolean bDone = false;
    	int nLoop  = 0;
    	Response response =null;
    	//implement a retry if token is invalud/unauthorized
    	while(!bDone) {
    		Cookie kukki = new Cookie.Builder("Authorization", token).build();
    		response = given().cookie(kukki).contentType(ContentType.JSON).body(jsonRequest.toString()).post(url);
    		if(response.getStatusCode() == 401) {
    			if(nLoop >= 1)
    				bDone = true;
    			else {
    				initToken();
    				nLoop++;
    			}
    		}
    		else
    			bDone = true;
    	}
        checkErrorResponse(response.getBody().asString());

        return new JSONObject(response.getBody().asString()).getJSONObject(dataKey);
    }


    public JSONArray syncRid(String baseUrl,String url, String requestBody, String timestamp,String contextKey) throws Exception {
    	this.baseUrl = baseUrl;
    
    	loadContext(contextKey);
    	token=null;
    
    	if (!isValidToken()){
            initToken();
        }

    	boolean bDone = false;
    	int nLoop  = 0;
    	Response response =null;

    	while(!bDone) {
        	
    		ObjectMapper objectMapper = new ObjectMapper();
    		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    		objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    		String outputJson = objectMapper.writeValueAsString(requestBody);

    		Cookie kukki = new Cookie.Builder("Authorization", token).build();
    		response = given().cookie(kukki)
                .header("timestamp", timestamp)
                .header("Center-Machine-RefId", centerId + UNDERSCORE + machineId)
                .contentType(ContentType.JSON).body(outputJson).post(url);

    		if(response.getStatusCode() == 401) {
    			if(nLoop >= 1)
    				bDone = true;
    			else {
    				initToken();
    				nLoop++;
    			}
    		}
    		else
    			bDone = true;
    	}
        checkErrorResponse(response.getBody().asString());

        return new JSONObject(response.getBody().asString()).getJSONArray(dataKey);
    }
 	
    public JSONObject uploadFile(String baseUrl,String url, String filePath, String contextKey) throws Exception {
    	this.baseUrl = baseUrl;
    	
    	//load context
    	loadContext(contextKey);
    	token=null;
    	if (!isValidToken()){
            initToken();
        }
    	File f = new File(filePath);
    	
        Cookie kukki = new Cookie.Builder("Authorization", token).build();
        Response response = given().cookie(kukki).multiPart("file", f.getCanonicalFile()).post(url);
        checkErrorResponse(response.getBody().asString());
        return new JSONObject(response.getBody().asString()).getJSONObject(dataKey);
    }

    private boolean isValidToken() throws Exception {
    	Object obj = VariableManager.getVariableValue("urlSwitched");
    	if(obj != null) {
    		Boolean bClear = Boolean.valueOf(obj.toString());
    		if(bClear)
    			return false;
    	}
    	return !(token == null); 
    	/*
        if(jwtProcessor == null) {
            jwtProcessor = new DefaultJWTProcessor<>();
            jwtProcessor.setJWSTypeVerifier(new DefaultJOSEObjectTypeVerifier<>(new JOSEObjectType("jwt")));
            //fix SA
            JWKSource<SecurityContext> keySource = new RemoteJWKSet<>(new URL(baseUrl + jwksUrl));
            JWSAlgorithm expectedJWSAlg = JWSAlgorithm.RS256;
            JWSKeySelector<SecurityContext> keySelector = new JWSVerificationKeySelector<>(expectedJWSAlg, keySource);
            jwtProcessor.setJWSKeySelector(keySelector);
            jwtProcessor.setJWTClaimsSetVerifier(new DefaultJWTClaimsVerifier(
                    new JWTClaimsSet.Builder().issuer(jwtIssuer).build(),
                    new HashSet<>(Arrays.asList("sub", "iat", "exp", "jti"))));
        }

        try{
            if(token != null) {
                JWTClaimsSet claimsSet = jwtProcessor.process(token, null);
                jwtProcessor.getJWTClaimsSetVerifier().verify(claimsSet, null);
                logger.info("JWT Claim set verified successfully");
                return true;
            }
        } catch (Exception ex) {
            logger.error("JWT verification failed", ex);
        }
        */
       // return false;
    }

   // @PostConstruct
    public boolean initPreregToken() {
    	try {		
			JSONObject requestBody = new JSONObject();
			JSONObject nestedRequest = new JSONObject();
		
			nestedRequest.put("appId", "registrationclient");
            nestedRequest.put("clientId", "mosip-reg-client");
            nestedRequest.put("secretKey", secretKey);
			requestBody.put("metadata", new JSONObject());
			requestBody.put("version", "1.0");
			requestBody.put("id", "test");
			requestBody.put("requesttime", getUTCDateTime(LocalDateTime.now()));
			requestBody.put("request", nestedRequest);

            //authManagerURL
            //String AUTH_URL = "v1/authmanager/authenticate/internal/useridPwd";
            Response response = given().contentType("application/json").body(requestBody.toString()).post(baseUrl + preregAuthManagerURL);
			logger.info("Authtoken generation request response: {}", response.asString());
			if(response.getStatusCode() == 401) {
				throw new Exception("401 - Unauthorized");
				
			}
            if (response.getStatusCode() != 200 ||  response.toString().contains("errorCode")) {
            	if(bSlackit)
            		SlackIt.postMessage(null,
            				baseUrl + preregAuthManagerURL + " Failed to authenticate, Is " + baseUrl + " down ?");
            	
            	return false;
            }
            //token = new JSONObject(response.getBody().asString()).getJSONObject(dataKey).getString("token");
            //refreshToken = new JSONObject(response.getBody().asString()).getJSONObject(dataKey).getString("refreshToken");
            preregToken=response.getCookie("Authorization");
            
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
    
    public boolean initToken_prereg(){
        try {	
        	if(VariableManager.isInit()) {
	        	Object o =VariableManager.getVariableValue("operatorId");
	        	if(o != null)
	        		operatorId = o.toString();
	        	
	        	o =VariableManager.getVariableValue("password");
	        	
	        	if(o != null)
	        		password = o.toString();
	        }
        	
			JSONObject requestBody = new JSONObject();
			JSONObject nestedRequest = new JSONObject();
			nestedRequest.put("userName", operatorId);
			nestedRequest.put("password", password);
			
			nestedRequest.put("appId", VariableManager.getVariableValue("appId"));
			nestedRequest.put("clientId", VariableManager.getVariableValue("clientId"));
			nestedRequest.put("clientSecret", VariableManager.getVariableValue("secretKey"));

			/*
			 * nestedRequest.put("appId", VariableManager.getVariableValue("prereg_appId"));
			 * nestedRequest.put("clientId",
			 * VariableManager.getVariableValue("prereg_clientId"));
			 * nestedRequest.put("secretKey",
			 * VariableManager.getVariableValue("prereg_secretKey"));
			 */
			
			requestBody.put("metadata", new JSONObject());
			requestBody.put("version", "1.0");
			requestBody.put("id", "mosip.authentication.useridPwd");
			requestBody.put("requesttime", getUTCDateTime(LocalDateTime.now()));
			requestBody.put("request", nestedRequest);

            //authManagerURL
            //String AUTH_URL = "v1/authmanager/authenticate/internal/useridPwd";
            Response response = given().contentType("application/json").body(requestBody.toString()).post(baseUrl + authManagerURL);
           // Response response = given().contentType("application/json").body(requestBody.toString()).post(baseUrl + preregAuthManagerURL);
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
           token = new JSONObject(response.getBody().asString()).getJSONObject(dataKey).getString("token");
            //refreshToken = new JSONObject(response.getBody().asString()).getJSONObject(dataKey).getString("refreshToken");
           // preregToken=response.getCookie("Authorization");
            preregToken=token;
            
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
    
   // @PostConstruct
    public boolean initToken(){
        try {	
        	if(VariableManager.isInit()) {
	        	Object o =VariableManager.getVariableValue("operatorId");
	        	if(o != null)
	        		operatorId = o.toString();
	        	
	        	o =VariableManager.getVariableValue("password");
	        	
	        	if(o != null)
	        		password = o.toString();
	        }
        	
			JSONObject requestBody = new JSONObject();
			JSONObject nestedRequest = new JSONObject();
			nestedRequest.put("userName", operatorId);
			nestedRequest.put("password", password);
            nestedRequest.put("appId", appId);
            nestedRequest.put("clientId", clientId);
            nestedRequest.put("clientSecret", secretKey);
			requestBody.put("metadata", "");
			requestBody.put("version", "1.0");
			requestBody.put("id", "test");
			requestBody.put("requesttime", getUTCDateTime(LocalDateTime.now()));
			requestBody.put("request", nestedRequest);

            //authManagerURL
            //String AUTH_URL = "v1/authmanager/authenticate/internal/useridPwd";
            Response response = given().contentType("application/json").body(requestBody.toString()).post(baseUrl + authManagerURL);
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
             token = new JSONObject(response.getBody().asString()).getJSONObject(dataKey).getString("token");
            //refreshToken = new JSONObject(response.getBody().asString()).getJSONObject(dataKey).getString("refreshToken");
            //token=response.getCookie("Authorization");
            
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

    /**
     * 
     * @param time nullable send null if you need current time.
     * @return the date as string as used by our request api.
     */
    public static String getUTCDateTime(LocalDateTime time) {
		String DATEFORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(DATEFORMAT);
        if (time == null){
            time = LocalDateTime.now(TimeZone.getTimeZone("UTC").toZoneId());
        }  
		String utcTime = time.format(dateFormat);
		return utcTime;
    }
    
    /**
     * 
     * @param date nullable send null if you need current time.
     * @return the date as string as used by our request api.
     */
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
        //TODO: Handle 401 or token expiry
        JSONObject jsonObject =  new JSONObject(response);

        if(jsonObject.has(errorKey) && jsonObject.get(errorKey) != JSONObject.NULL) {
            throw new Exception(String.valueOf(jsonObject.get(errorKey)));
        }
    }
    
}
