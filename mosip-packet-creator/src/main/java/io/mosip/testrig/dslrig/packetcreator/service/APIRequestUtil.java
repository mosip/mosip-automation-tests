package io.mosip.testrig.dslrig.packetcreator.service;

import static io.restassured.RestAssured.given;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
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

import io.mosip.testrig.dslrig.dataprovider.util.RestClient;
import io.mosip.testrig.dslrig.dataprovider.util.SlackIt;
import io.mosip.testrig.dslrig.dataprovider.variables.VariableManager;
import io.restassured.http.ContentType;
import io.restassured.http.Cookie;
import io.restassured.response.Response;

@Component
public class APIRequestUtil {

    private static final String UNDERSCORE = "_";

    Logger logger = LoggerFactory.getLogger(APIRequestUtil.class);

   // private ConfigurableJWTProcessor<SecurityContext> jwtProcessor = null;
	static Map<String, String> tokens = new HashMap<String,String>();
    //String token;
  //  String preregToken;
    
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
          	else
                if(k.equals("mosip.test.authmanager.url"))
                	authManagerURL = v.toString();
            	else
                    if(k.equals("mosip.test.regclient.clientid"))
                    	clientId = v.toString();
                	else
                        if(k.equals("mosip.test.regclient.appId"))
                        	appId = v.toString();
                    	else
                            if(k.equals("mosip.test.regclient.secretkey"))
                            	secretKey = v.toString();
                            else
                                if(k.equals("mosip.test.baseurl"))
                                	baseUrl = v.toString();
    		
    	});
    	
    }

    public void clearToken() {
    	tokens.clear();
    	//preregToken = null;
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

    		Cookie kukki = new Cookie.Builder("Authorization", tokens.get(VariableManager.getVariableValue(contextKey,"urlBase").toString().trim()+"system")).build();
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

    		Cookie kukki = new Cookie.Builder("Authorization", tokens.get(VariableManager.getVariableValue(contextKey,"urlBase").toString().trim()+"system")).build();
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

        checkErrorResponse(response.getBody().asString());

        return new JSONObject(response.getBody().asString());
    }
    public JSONObject getPreReg(String baseUrl,String url, JSONObject requestParams, JSONObject pathParam,String contextKey) throws Exception {
    	this.baseUrl = baseUrl;
    	if (!isValidToken(contextKey)){
            	
                //initPreregToken();
        		initToken_prereg(contextKey);
            
        }
    	
    	boolean bDone = false;
    	int nLoop  = 0;
    	Response response =null;

    	while(!bDone) {

    		Cookie kukki = new Cookie.Builder("Authorization",tokens.get(VariableManager.getVariableValue(contextKey,"urlBase").toString().trim()+"system")).build();
    		response = given().cookie(kukki).contentType(ContentType.JSON).queryParams(requestParams.toMap()).get(url,pathParam.toMap());
    		if(response.getStatusCode() == 401) {
    			if(nLoop >= 1)
    				bDone = true;
    			else {
    				//initPreregToken();
    				initToken_prereg(contextKey);
    				nLoop++;
    			}
    		}
    		else
    			bDone = true;
    	}

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
    	//implement a retry if token is invalud/unauthorized
    	while(!bDone) {
    		Cookie kukki = new Cookie.Builder("Authorization", tokens.get(VariableManager.getVariableValue(contextKey,"urlBase").toString().trim()+"system")).build();
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
        checkErrorResponse(response.getBody().asString());

        return new JSONObject(response.getBody().asString()).getJSONObject(dataKey);
    }


    public JSONArray syncRid(String baseUrl,String url, String requestBody, String timestamp,String contextKey) throws Exception {
    	this.baseUrl = baseUrl;
    
    	loadContext(contextKey);
    	 tokens.put(VariableManager.getVariableValue(contextKey,"urlBase").toString().trim()+"system",null);
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

    		Cookie kukki = new Cookie.Builder("Authorization", tokens.get(VariableManager.getVariableValue(contextKey,"urlBase").toString().trim()+"system")).build();
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
        checkErrorResponse(response.getBody().asString());

        return new JSONObject(response.getBody().asString()).getJSONArray(dataKey);
    }
 	
    public JSONObject uploadFile(String baseUrl,String url, String filePath, String contextKey) throws Exception {
    	this.baseUrl = baseUrl;
    	
    	//load context
    	loadContext(contextKey);
    	tokens.put(VariableManager.getVariableValue(contextKey,"urlBase").toString().trim()+"system",null);
    	
    	//token=null;
    	if (!isValidToken(contextKey)){
            initToken(contextKey);
        }
    	File f = new File(filePath);
    	
        Cookie kukki = new Cookie.Builder("Authorization", tokens.get(VariableManager.getVariableValue(contextKey,"urlBase").toString().trim()+"system")
            	).build();
        Response response = given().cookie(kukki).multiPart("file", f.getCanonicalFile()).post(url);
        checkErrorResponse(response.getBody().asString());
       
        return new JSONObject(response.getBody().asString()).getJSONObject(dataKey);
    }

    private boolean isValidToken(String contextKey) throws Exception {
    	Object obj = VariableManager.getVariableValue(contextKey,"urlSwitched");
    	if(obj != null) {
    		Boolean bClear = Boolean.valueOf(obj.toString());
    		if(bClear)
    			
    			return false;
    	}
    	String token= tokens.get(VariableManager.getVariableValue(contextKey,"urlBase").toString().trim()+"system");
    	return  !(null == token);
    
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
  /*  public boolean initPreregToken() {
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
 */
    
    public boolean initToken_prereg(String contextKey){
        try {	
        	
        	
			JSONObject requestBody = new JSONObject();
			JSONObject nestedRequest = new JSONObject();
			nestedRequest.put("userName",  VariableManager.getVariableValue(contextKey,"admin_userName").toString());
			nestedRequest.put("password",  VariableManager.getVariableValue(contextKey,"admin_password").toString());
			
			nestedRequest.put("appId", VariableManager.getVariableValue(contextKey,"mosip_admin_app_id").toString());
			nestedRequest.put("clientId", VariableManager.getVariableValue(contextKey,"mosip_admin_client_id").toString());
			nestedRequest.put("clientSecret", VariableManager.getVariableValue(contextKey,"mosip_admin_client_secret").toString());

		
			
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
           
           tokens.put(VariableManager.getVariableValue(contextKey,"urlBase").toString().trim()+"system",token);
            //refreshToken = new JSONObject(response.getBody().asString()).getJSONObject(dataKey).getString("refreshToken");
           // preregToken=response.getCookie("Authorization");
            
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
			JSONObject nestedRequest = new JSONObject();
			nestedRequest.put("userName", VariableManager.getVariableValue(contextKey,"admin_userName").toString() );
			nestedRequest.put("password", VariableManager.getVariableValue(contextKey,"admin_password").toString() );
            nestedRequest.put("appId", VariableManager.getVariableValue(contextKey,"mosip_admin_app_id").toString());
            nestedRequest.put("clientId", VariableManager.getVariableValue(contextKey,"mosip_admin_client_id").toString());
            nestedRequest.put("clientSecret", VariableManager.getVariableValue(contextKey,"mosip_admin_client_secret").toString());
			requestBody.put("metadata", "");
			requestBody.put("version", "1.0");
			requestBody.put("id", "test");
			requestBody.put("requesttime", getUTCDateTime(LocalDateTime.now()));
			requestBody.put("request", nestedRequest);
			
            //authManagerURL
            //String AUTH_URL = "v1/authmanager/authenticate/internal/useridPwd";
            Response response = given().contentType("application/json").body(requestBody.toString()).post(baseUrl + authManagerURL);
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
            //refreshToken = new JSONObject(response.getBody().asString()).getJSONObject(dataKey).getString("refreshToken");
            //token=response.getCookie("Authorization");
          
        		
        	//String	token=  post(baseUrl,authManagerURL,requestBody).getString("token");
            tokens.put(VariableManager.getVariableValue(contextKey,"urlBase").toString().trim()+"system",token);
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
		 logger.info("Check Error Responce : " +response);
        //TODO: Handle 401 or token expiry
        JSONObject jsonObject =  new JSONObject(response);
        if(jsonObject.get(errorKey) != JSONObject.NULL) {
        JSONArray arr=(JSONArray) jsonObject.get(errorKey);

        if(jsonObject.has(errorKey)  && !arr.isEmpty()) {
            throw new Exception(String.valueOf(jsonObject.get(errorKey)));
        }}
    }
    
}
