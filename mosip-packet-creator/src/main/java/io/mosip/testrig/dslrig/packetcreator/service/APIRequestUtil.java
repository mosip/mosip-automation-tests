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

import io.mosip.testrig.dslrig.dataprovider.util.CommonUtil;
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

	static Map<String, String> tokens = new HashMap<String,String>();
  
    String refreshToken;

    final String dataKey = "response";
    final String errorKey = "errors";

    @Autowired
    ContextUtils contextUtils;

    public void clearToken() {
    	tokens.clear();
    }
    public JSONObject get(String baseUrl,String url, JSONObject requestParams, JSONObject pathParam,String contextKey) throws Exception {
    	
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
    baseUrl=VariableManager.getVariableValue(contextKey, "mosip.test.baseurl").toString();
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
    
    	baseUrl=VariableManager.getVariableValue(contextKey, "mosip.test.baseurl").toString();
        
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
                .header("Center-Machine-RefId", VariableManager.getVariableValue(contextKey, "mosip.test.regclient.centerid").toString() + UNDERSCORE + VariableManager.getVariableValue(contextKey, "mosip.test.regclient.machineid").toString())
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
    baseUrl=VariableManager.getVariableValue(contextKey, "mosip.test.baseurl").toString();
        
    	
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
    
    	
    }

  
    
    public boolean initToken_prereg(String contextKey){
    	boolean bSlackit=false;
    	if(VariableManager.getVariableValue(VariableManager.NS_DEFAULT, "mosip.test.post2slack").toString().equalsIgnoreCase("true")) { bSlackit= true;} else{bSlackit= false;};
    	
    	try {	
        			
        	
			JSONObject requestBody = new JSONObject();
			JSONObject nestedRequest = new JSONObject();
			nestedRequest.put("userName",  VariableManager.getVariableValue(contextKey,"admin_userName").toString());
			nestedRequest.put("password",  VariableManager.getVariableValue(contextKey,"admin_password").toString());
			
			nestedRequest.put("appId", VariableManager.getVariableValue(contextKey,"mosip_admin_app_id").toString());
			nestedRequest.put("clientId", VariableManager.getVariableValue(contextKey,"mosip_admin_client_id").toString());
			nestedRequest.put("clientSecret", VariableManager.getVariableValue(contextKey,"mosip_admin_client_secret").toString());

			requestBody.put("metadata", new JSONObject());
			requestBody.put("version", "1.0");
			requestBody.put("id", "mosip.authentication.useridPwd");
			requestBody.put("requesttime", getUTCDateTime(LocalDateTime.now()));
			requestBody.put("request", nestedRequest);

          
            Response response = given().contentType("application/json").body(requestBody.toString()).post(VariableManager.getVariableValue(contextKey, "mosip.test.baseurl").toString() + VariableManager.getVariableValue(VariableManager.NS_DEFAULT, "mosip.test.authmanager.url").toString());
         	if(RestClient.isDebugEnabled(contextKey))
            logger.info("Authtoken generation request response: {}", response.asString());
			if(response.getStatusCode() == 401) {
				throw new Exception("401 - Unauthorized");
				
			}
            if (response.getStatusCode() != 200 ||  response.toString().contains("errorCode")) {
            	if(bSlackit)
            		SlackIt.postMessage(null,
            				VariableManager.getVariableValue(contextKey, "mosip.test.baseurl").toString() + VariableManager.getVariableValue(VariableManager.NS_DEFAULT, "mosip.test.authmanager.url").toString() + " Failed to authenticate, Is " + VariableManager.getVariableValue(contextKey, "mosip.test.baseurl").toString() + " down ?");
            	
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
        				VariableManager.getVariableValue(contextKey, "mosip.test.baseurl").toString() + VariableManager.getVariableValue(VariableManager.NS_DEFAULT, "mosip.test.authmanager.url").toString() + " Failed to authenticate, Is " + VariableManager.getVariableValue(contextKey, "mosip.test.baseurl").toString() + " down ?");
        	
            return false;
		}
    }
    
   // @PostConstruct
    public boolean initToken(String contextKey){
    	boolean bSlackit=false;
    	if(VariableManager.getVariableValue(VariableManager.NS_DEFAULT, "mosip.test.post2slack").toString().equalsIgnoreCase("true")) { bSlackit= true;} else{bSlackit= false;};
    
        try {	
        	
        	String operatorId=null;
        	String password=null;
        	if(VariableManager.isInit()) {
	        	Object o =VariableManager.getVariableValue(contextKey,"mosip.test.regclient.userid");
	        	if(o != null)
	        		operatorId = o.toString();
	        	
	        	o =VariableManager.getVariableValue(contextKey, "mosip.test.regclient.password").toString();
	        	
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
			
       
            Response response = given().contentType("application/json").body(requestBody.toString()).post(VariableManager.getVariableValue(contextKey, "mosip.test.baseurl").toString() + 
            		VariableManager.getVariableValue(VariableManager.NS_DEFAULT, "mosip.test.authmanager.url").toString());
            if(RestClient.isDebugEnabled(contextKey))
            logger.info("Authtoken generation request response: {}", response.asString());
			if(response.getStatusCode() == 401) {
				throw new Exception("401 - Unauthorized");
				
			}
            if (response.getStatusCode() != 200 ||  response.toString().contains("errorCode")) {
            	if(bSlackit)
            		SlackIt.postMessage(null,
            				VariableManager.getVariableValue(contextKey, "mosip.test.baseurl").toString() + VariableManager.getVariableValue(VariableManager.NS_DEFAULT, "mosip.test.authmanager.url").toString() + " Failed to authenticate, Is " + VariableManager.getVariableValue(contextKey, "mosip.test.baseurl").toString() + " down ?");
            	
            	return false;
            }
            String token=null;
        token= new JSONObject(response.getBody().asString()).getJSONObject(dataKey).getString("token");
          
            tokens.put(VariableManager.getVariableValue(contextKey,"urlBase").toString().trim()+"system",token);
			return true;	
		}
		catch(Exception  ex){
            logger.error("",ex);
            if(bSlackit)
        		SlackIt.postMessage(null,
        				VariableManager.getVariableValue(contextKey, "mosip.test.baseurl").toString() + VariableManager.getVariableValue(VariableManager.NS_DEFAULT, "mosip.test.authmanager.url").toString() + " Failed to authenticate, Is " + VariableManager.getVariableValue(contextKey, "mosip.test.baseurl").toString() + " down ?");
        	
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
        if(jsonObject.get(errorKey) != JSONObject.NULL) {
        JSONArray arr=(JSONArray) jsonObject.get(errorKey);

        if(jsonObject.has(errorKey)  && !arr.isEmpty()) {
            throw new Exception(String.valueOf(jsonObject.get(errorKey)));
        }}
    }
    
}
