package org.mosip.dataprovider.util;

import java.io.BufferedReader;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;

import java.net.URL;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.Collections;
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
import org.mosip.dataprovider.mds.HttpRCapture;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import variables.VariableManager;

import static io.restassured.RestAssured.given;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.http.Cookie;
import io.restassured.http.Header;

public class RestClient {
	private static final org.slf4j.Logger logger= org.slf4j.LoggerFactory.getLogger(RestClient.class);
	
	static String dataKey = "response";
    static String errorKey = "errors";
	//static String token;
	static Map<String, String> tokens = new HashMap<String,String>();
	static String refreshToken;

	static {
	//	initToken(contextKey);
		RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
	}
	String _urlBase;

	int http_status;
	Properties headers;
	
	public static Boolean isValidToken(String role,String contextKey) {
		
		Object obj = VariableManager.getVariableValue(contextKey,"urlSwitched");
    	if(obj != null) {
    		Boolean bClear = Boolean.valueOf(obj.toString());
    		if(bClear)
    			return false;
    	}
    	
    	//String token = tokens.get(role);
    	String token= tokens.get(VariableManager.getVariableValue(contextKey,"urlBase").toString().trim()+role);
    	return  !(null == token);
    	
	}
	
	public static void clearToken() {
//		tokens.remove("system");
//		tokens.remove("resident");
//		tokens.remove("admin");
		tokens.clear();
		//token = null;
		refreshToken = null;
	}
	public int status() {
		return http_status;
	}
	public RestClient(String urlBase){
		_urlBase = urlBase;
		headers = new Properties();
	}
	public RestClient(){
		headers = new Properties();
	}
	public static String constructQueryParam(Properties queryParam) throws UnsupportedEncodingException {
		
		StringBuilder builder = new StringBuilder();
		
		
		if(queryParam != null) {
			builder.append( "?");
			queryParam.forEach( (k,v) -> {
				builder.append( k + "=" + v).append("&");
			});
		}
		if(builder.length() > 2 &&
				builder.substring(builder.length() -2 , builder.length()-1).equals("&")) {
			builder.replace(builder.length() -2 , builder.length()-1, "");
		}
		return URLEncoder.encode( builder.toString().trim(),"UTF-8");
	}
	public void addHeader(String header, String value) {
		headers.put(header,  value);
	}
	//method used with system role
	public static JSONObject get(String url, JSONObject requestParams, JSONObject pathParam,String contextKey) throws Exception {
       
		String role = "system";
        if (!isValidToken(role,contextKey)){
        	initToken(contextKey);
        	
        }
    	boolean bDone = false;
    	int nLoop  = 0;
    	Response response =null;

    	//Stict http validation errors - fix
    	
    	/*if(url.contains("//")) {
    		url = url.replace("//", "/"); 		
    	}*/
    	try {
    	while(!bDone) {

    	//	String token = tokens.get(role);
    		String token= tokens.get(VariableManager.getVariableValue(contextKey,"urlBase").toString().trim()+role);
    		Cookie kukki = new Cookie.Builder("Authorization", token).build();
    		Map<String,Object> mapParam = requestParams == null ? null: requestParams.toMap();
        		//new Gson().fromJson(requestParams.toString(), HashMap.class);
    		Map<String,Object> mapPathParam =pathParam == null ? null: pathParam.toMap();
        
        	//new Gson().fromJson(pathParam.toString(), HashMap.class);
        
    		response = given().cookie(kukki).contentType(ContentType.JSON).queryParams(mapParam).get(url,mapPathParam );
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

        if(response != null) {
        	System.out.println(response.getBody().asString());
        	
        }
        checkErrorResponse(response.getBody().asString());

       }
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    	 return new JSONObject(response.getBody().asString()).getJSONObject(dataKey);
    }
	
	//method used with system role
		public static JSONArray getDoc(String url, JSONObject requestParams, JSONObject pathParam,String contextKey) throws Exception {
	       
			String role = "system";
	        if (!isValidToken(role,contextKey)){
	        	initToken(contextKey);
	        	
	        }
	    	boolean bDone = false;
	    	int nLoop  = 0;
	    	Response response =null;

	    	//Stict http validation errors - fix
	    	
	    	/*if(url.contains("//")) {
	    		url = url.replace("//", "/"); 		
	    	}*/
	    	while(!bDone) {

	    		//String token = tokens.get(role);
	    		String token= tokens.get(VariableManager.getVariableValue(contextKey,"urlBase").toString().trim()+role);
	    		    	
	    		Cookie kukki = new Cookie.Builder("Authorization", token).build();
	    		Map<String,Object> mapParam = requestParams == null ? null: requestParams.toMap();
	        		//new Gson().fromJson(requestParams.toString(), HashMap.class);
	    		Map<String,Object> mapPathParam =pathParam == null ? null: pathParam.toMap();
	        
	        	//new Gson().fromJson(pathParam.toString(), HashMap.class);
	        
	    		response = given().cookie(kukki).contentType(ContentType.JSON).queryParams(mapParam).get(url,mapPathParam );
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

	        if(response != null) {
	        	System.out.println(response.getBody().asString());
	        	
	        }
	        checkErrorResponse(response.getBody().asString());

	        return new JSONObject(response.getBody().asString()).getJSONArray(dataKey);
	    }
	
	
	public static JSONObject getNoAuth(String url, JSONObject requestParams, JSONObject pathParam, String contextKey) throws Exception {
		if (!isValidToken("resident",contextKey)){
	           initToken_Resident(contextKey);
	        }
		String token = tokens.get(VariableManager.getVariableValue(contextKey,"urlBase").toString().trim()+"resident");
	
		Response response =null;


    	Cookie kukki = new Cookie.Builder("Authorization", token).build();
    	Map<String,Object> mapParam = requestParams == null ? null: requestParams.toMap();

    	Map<String,Object> mapPathParam =pathParam == null ? null: pathParam.toMap();

    	response = given().cookie(kukki).contentType(ContentType.JSON).queryParams(mapParam).get(url,mapPathParam );

        if(response != null) {
        	System.out.println(response.getBody().asString());        	
        }
        checkErrorResponse(response.getBody().asString());

        return new JSONObject(response.getBody().asString()).getJSONObject(dataKey);
    }
	
	public static JSONObject uploadFile(String url, String filePath, JSONObject requestData,String contextKey) throws Exception {
		String role = "resident";
	
		if (!isValidToken(role,contextKey)){
            initToken(contextKey);
        }


		 Response response =null;

		//Stict http validation errors - fix
		 /*
	    	if(url.contains("//")) {
	    		url = url.replace("//", "/"); 		
	    	}
	*/
	   // String token = tokens.get(role);
	    String token= tokens.get(VariableManager.getVariableValue(contextKey,"urlBase").toString().trim()+role);
		
	    Cookie kukki = new Cookie.Builder("Authorization", token).build();
        
	    if(requestData != null) {
	    		response = given().cookie(kukki)
        			.multiPart("file", new File(filePath))
        			.param("Document request",requestData.toString())
        			//.multiPart("Document request",requestData)
        			.post(url);
        
	    }
	    else
	    {
	    		response = given().cookie(kukki)
            		.multiPart("file", new File(filePath))
            		.post(url);
	    }

        checkErrorResponse(response.getBody().asString());
        return new JSONObject(response.getBody().asString()).getJSONObject(dataKey);
    }
	public static JSONObject uploadFiles(String url, List<String> filePaths, JSONObject requestData,String contextKey) throws Exception {
		String role = "admin";
	
		if (!isValidToken(role,contextKey)){
          // initToken(contextKey);
           initToken_admin(contextKey);
        }


		 Response response =null;

		//Stict http validation errors - fix
		 /*
	    	if(url.contains("//")) {
	    		url = url.replace("//", "/"); 		
	    	}
	*/
	   // String token = tokens.get(role);
	    String token= tokens.get(VariableManager.getVariableValue(contextKey,"urlBase").toString().trim()+role);
		
	    Cookie kukki = new Cookie.Builder("Authorization", token).build();
        
	    RequestSpecification spec = given().cookie(kukki);
    	for(String fName: filePaths)
    		spec = spec.multiPart("files", new File(fName));
    	if(requestData != null) {
    		Iterator<String> paramKeys = requestData.keys();
    		while(paramKeys.hasNext()) {
    			String key = paramKeys.next();
    			spec = spec.formParam(key,requestData.get(key));
    		
    		}
    	}
	    response = spec.post(url);

        checkErrorResponse(response.getBody().asString());
        return new JSONObject(response.getBody().asString()).getJSONObject(dataKey);
    }

/*
	public static JSONObject getNoCookie(String url, JSONObject requestParams, JSONObject pathParam) throws Exception {
	       
        if (null == token){
            initToken(contextKey);
        }
        Response response = given().contentType(ContentType.JSON).queryParams(requestParams.toMap()).get(url,pathParam.toMap());

        if(response != null) {
        	System.out.println(response.getBody().asString());
        	
        	
        }
        checkErrorResponse(response.getBody().asString());

        return new JSONObject(response.getBody().asString()).getJSONObject(dataKey);
    }
*/
	public static JSONObject postNoAuth(String url, JSONObject jsonRequest,String contextKey) throws Exception {

		String role = "resident";
		if (!isValidToken(role,contextKey)){
	           initToken_Resident(contextKey);
	        }
	
		
		//String token = tokens.get(role);
		String token= tokens.get(VariableManager.getVariableValue(contextKey,"urlBase").toString().trim()+role);
		
		Response response =null;

	    System.out.println("Request:"+ jsonRequest.toString());
	    Cookie kukki = new Cookie.Builder("Authorization", token).build();
        
	    response = given().cookie(kukki).contentType(ContentType.JSON).body(jsonRequest.toString()).post(url);
	    for(Header h: response.getHeaders()) {
	    	System.out.println(h.getName() +"="+ h.getValue());
	    }
	    String cookie = response.getHeader("Set-Cookie");
    	/*if(cookie == null) {
    		cookie = response.getHeader("Cookies");
    	}*/
    	if(cookie != null) {
    		token = cookie.split("=")[1];
    		 tokens.put(VariableManager.getVariableValue(contextKey,"urlBase").toString().trim()+role,token);
    	}
    	System.out.println(token);
        System.out.println("Response:"+response.getBody().asString());
        checkErrorResponse(response.getBody().asString());

        return new JSONObject(response.getBody().asString()).getJSONObject(dataKey);
    }

	public static JSONObject postNoAuthSystem(String url, JSONObject jsonRequest,String contextKey) throws Exception {

		String role = "system";
		if (!isValidToken(role,contextKey)){
			initToken(contextKey);
	        }
	
		
		//String token = tokens.get(role);
		String token= tokens.get(VariableManager.getVariableValue(contextKey,"urlBase").toString().trim()+role);
		
		Response response =null;

	    System.out.println("Request:"+ jsonRequest.toString());
	    Cookie kukki = new Cookie.Builder("Authorization", token).build();
        
	    response = given().cookie(kukki).contentType(ContentType.JSON).body(jsonRequest.toString()).post(url);
	    for(Header h: response.getHeaders()) {
	    	System.out.println(h.getName() +"="+ h.getValue());
	    }
	    String cookie = response.getHeader("Set-Cookie");
    	/*if(cookie == null) {
    		cookie = response.getHeader("Cookies");
    	}*/
    	if(cookie != null) {
    		token = cookie.split("=")[1];
    		 tokens.put(VariableManager.getVariableValue(contextKey,"urlBase").toString().trim()+role,token);
    	}
    	System.out.println(token);
        System.out.println("Response:"+response.getBody().asString());
        checkErrorResponse(response.getBody().asString());

        return new JSONObject(response.getBody().asString()).getJSONObject(dataKey);
    }

	public static JSONObject putNoAuth(String url, JSONObject jsonRequest,String contextKey) throws Exception {

		String role = "resident";
		if (!isValidToken(role,contextKey)){
	           initToken_Resident(contextKey);
	        }
		//String token = tokens.get(role);
		String token= tokens.get(VariableManager.getVariableValue(contextKey,"urlBase").toString().trim()+role);
		
		Response response =null;

	    System.out.println("Request:"+ jsonRequest.toString());
	    Cookie kukki = new Cookie.Builder("Authorization", token).build();
        
	    response = given().cookie(kukki).contentType(ContentType.JSON).body(jsonRequest.toString()).put(url);


	    String cookie = response.getHeader("Set-Cookie");
    	/*if(cookie == null) {
    		cookie = response.getHeader("Cookies");
    	}*/
    	if(cookie != null) {
    		token = cookie.split("=")[1];
    		 tokens.put(VariableManager.getVariableValue(contextKey,"urlBase").toString().trim()+role,token);
    	}
    	System.out.println(token);
        System.out.println("Response:"+response.getBody().asString());
        checkErrorResponse(response.getBody().asString());

        return new JSONObject(response.getBody().asString()).getJSONObject(dataKey);
    }
	public static JSONObject deleteNoAuth(String url, JSONObject jsonRequest,String contextKey) throws Exception {

		String role = "resident";
		if (!isValidToken(role,contextKey)){
	           initToken_Resident(contextKey);
	        }
		//String token = tokens.get(role);
		String token= tokens.get(VariableManager.getVariableValue(contextKey,"urlBase").toString().trim()+role);
		
		Response response =null;

	    System.out.println("Request:"+ jsonRequest.toString());
	    Cookie kukki = new Cookie.Builder("Authorization", token).build();
        
	   response = given().cookie(kukki).contentType(ContentType.JSON).body(jsonRequest.toString()).delete(url);
	    
	    String cookie = response.getHeader("Set-Cookie");
    	/*if(cookie == null) {
    		cookie = response.getHeader("Cookies");
    	}*/
    	if(cookie != null) {
    		token = cookie.split("=")[1];
    		 tokens.put(VariableManager.getVariableValue(contextKey,"urlBase").toString().trim()+role,token);
    	}
    	System.out.println(token);
        System.out.println("Response:"+response.getBody().asString());
        checkErrorResponse(response.getBody().asString());

        return new JSONObject(response.getBody().asString()).getJSONObject(dataKey);
    }
	public static JSONObject delete(String url, JSONObject jsonRequest,String contextKey) throws Exception {

		String role = "resident";
		if (!isValidToken(role,contextKey)){
	           initToken_Resident(contextKey);
	        }
		//String token = tokens.get(role);
		String token= tokens.get(VariableManager.getVariableValue(contextKey,"urlBase").toString().trim()+role);
		
		Response response =null;

	    System.out.println("Request:"+ jsonRequest.toString());
	    Cookie kukki = new Cookie.Builder("Authorization", token).build();
        
	   response = given().cookie(kukki).contentType(ContentType.JSON).body(jsonRequest.toString()).delete(url);
	    
	    String cookie = response.getHeader("Set-Cookie");
    	/*if(cookie == null) {
    		cookie = response.getHeader("Cookies");
    	}*/
    	if(cookie != null) {
    		token = cookie.split("=")[1];
    		 tokens.put(VariableManager.getVariableValue(contextKey,"urlBase").toString().trim()+role,token);
    	}
    	System.out.println(token);
        System.out.println("Response:"+response.getBody().asString());
        

        return new JSONObject(response.getBody().asString());
    }

	public static JSONObject deleteNoAuthWithQueryParam(String url, JSONObject jsonRequest,String contextKey) throws Exception {

		String role = "resident";
		if (!isValidToken(role,contextKey)){
	           initToken_Resident(contextKey);
	        }
	//	String token = tokens.get(role);
		String token= tokens.get(VariableManager.getVariableValue(contextKey,"urlBase").toString().trim()+role);
		
		Response response =null;

	    System.out.println("Request:"+ jsonRequest.toString());
	    Cookie kukki = new Cookie.Builder("Authorization", token).build();
        
	    response = given().cookie(kukki).contentType(ContentType.JSON).queryParams(jsonRequest.toMap()).delete(url);
	    
	    String cookie = response.getHeader("Set-Cookie");
    	/*if(cookie == null) {
    		cookie = response.getHeader("Cookies");
    	}*/
    	if(cookie != null) {
    		token = cookie.split("=")[1];
    		 tokens.put(VariableManager.getVariableValue(contextKey,"urlBase").toString().trim()+role,token);
    	}
    	System.out.println(token);
        System.out.println("Response:"+response.getBody().asString());
        checkErrorResponse(response.getBody().asString());

        return new JSONObject(response.getBody().asString()).getJSONObject(dataKey);
    }

	public static JSONObject post(String url, JSONObject jsonRequest,String contextKey) throws Exception {
		return post(url,jsonRequest,"system",contextKey);
	}
	public static JSONObject post(String url, JSONObject jsonRequest,String role,String contextKey) throws Exception {
		//String role = "system";
		if (!isValidToken(role,contextKey)) {
		if(role.equalsIgnoreCase("resident")) {
			initToken_Resident(contextKey);
		}
		else if(role.equalsIgnoreCase("admin")) {
			initToken_admin(contextKey);
		}else {
			initToken(contextKey);
		}
		}
		/*
		 * if (!isValidToken(role,contextKey)){ if (usedResidentToken) initToken_Resident(); else
		 * initToken(contextKey); }
		 */
		boolean bDone = false;
	    int nLoop  = 0;
	    Response response =null;

	  //Stict http validation errors - fix
    	/*if(url.contains("//")) {
    		url = url.replace("//", "/"); 		
    	} */
	    
	    while(!bDone) {
	    	//String token = tokens.get(role);
	    	String token= tokens.get(VariableManager.getVariableValue(contextKey,"urlBase").toString().trim()+role);
    		
	    	Cookie kukki = new Cookie.Builder("Authorization", token).build();
	    	System.out.println("Request:"+ jsonRequest.toString());
          
	    	response = given().cookie(kukki).contentType(ContentType.JSON).body(jsonRequest.toString()).post(url);
	    	if(response.getStatusCode() == 401 || response.getStatusCode() == 500 ) {
    			if(nLoop >= 1)
    				bDone = true;
    			else {
    				if(role.equalsIgnoreCase("resident")) {
    					initToken_Resident(contextKey);
    				}
    				else if(role.equalsIgnoreCase("admin")) {
    					initToken_admin(contextKey);
    				}else {
    					initToken(contextKey);
    				}
    				nLoop++;
    			}
    		}
    		else
    			bDone = true;
	    
	    }
	    	
	    String cookie = response.getHeader("set-cookie");
    	if(cookie != null) {
    		
    		String token = cookie.split("=")[1];
    	
    		  tokens.put(VariableManager.getVariableValue(contextKey,"urlBase").toString().trim()+role, token);
    	}
    	if(response.getBody().asString().startsWith("{")) {
    		System.out.println("Response:"+response.getBody().asString());
    		checkErrorResponse(response.getBody().asString());
    	
    		return new JSONObject(response.getBody().asString()).getJSONObject(dataKey);
    	}
    	else {
    		return new JSONObject("{\"status\":\""+ response.getBody().asString() + "\"}" );
    	}
    }
	
	public static JSONObject put(String url, JSONObject jsonRequest,String contextKey) throws Exception {
		String role = "system";
		if (!isValidToken(role,contextKey)){
            initToken(contextKey);
        }
		boolean bDone = false;
	    int nLoop  = 0;
	    Response response =null;

	  //Stict http validation errors - fix
    	/*if(url.contains("//")) {
    		url = url.replace("//", "/"); 		
    	} */
	    
	    while(!bDone) {
	    	//String token = tokens.get(role);
	    	String token= tokens.get(VariableManager.getVariableValue(contextKey,"urlBase").toString().trim()+role);
    		
	    	Cookie kukki = new Cookie.Builder("Authorization", token).build();
	    	System.out.println("Request:"+ jsonRequest.toString());
	    	response = given().cookie(kukki).contentType(ContentType.JSON).body(jsonRequest.toString()).put(url);
	    	if(response.getStatusCode() == 401 || response.getStatusCode() == 500 ) {
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
	    	
	    String cookie = response.getHeader("set-cookie");
    	if(cookie != null) {
    		
    		String token = cookie.split("=")[1];
    		  tokens.put(VariableManager.getVariableValue(contextKey,"urlBase").toString().trim()+role, token);
    		
    	}
    	if(response.getBody().asString().startsWith("{")) {
    		System.out.println("Response:"+response.getBody().asString());
    		checkErrorResponse(response.getBody().asString());
    		return new JSONObject(response.getBody().asString()).getJSONObject(dataKey);
    	}
    	else {
    		return new JSONObject("{\"status\":\""+ response.getBody().asString() + "\"}" );
    	}
    }
	
	public static JSONObject putPreRegStatus(String url, JSONObject jsonRequest,String contextKey) throws Exception {
		String role = "resident";// //system
		if (!isValidToken(role,contextKey)){
           initToken(contextKey);
        }
		boolean bDone = false;
	    int nLoop  = 0;
	    Response response =null;

	    
	    while(!bDone) {
	    	//String token = tokens.get(role);
	    	String token= tokens.get(VariableManager.getVariableValue(contextKey,"urlBase").toString().trim()+role);
    		
	    	Cookie kukki = new Cookie.Builder("Authorization", token).build();
	    	System.out.println("Request:"+ jsonRequest.toString());
	    	response = given().cookie(kukki).contentType(ContentType.JSON).body(jsonRequest.toString()).put(url);
	    	if(response.getStatusCode() == 401 || response.getStatusCode() == 500 ) {
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
	    	
	    String cookie = response.getHeader("set-cookie");
    	if(cookie != null) {
    		
    		String token = cookie.split("=")[1];
    		
    		  tokens.put(VariableManager.getVariableValue(contextKey,"urlBase").toString().trim()+role, token);
    	}
    	if(response.getBody().asString().startsWith("{")) {
    		System.out.println("Response:"+response.getBody().asString());
    		checkErrorResponse(response.getBody().asString());
    		return new JSONObject(response.getBody().asString());
    	}
    	else {
    		return new JSONObject("{\"status\":\""+ response.getBody().asString() + "\"}" );
    	}
    }

	public static JSONObject patch(String url, JSONObject jsonRequest,String contextKey) throws Exception {
		String role = "system";
		if (!isValidToken(role,contextKey)){
           initToken(contextKey);
        }
		boolean bDone = false;
	    int nLoop  = 0;
	    Response response =null;

	  //Stict http validation errors - fix
    	/*if(url.contains("//")) {
    		url = url.replace("//", "/"); 		
    	} */
	    
	    while(!bDone) {
	    //	String token = tokens.get(role);
	    	String token= tokens.get(VariableManager.getVariableValue(contextKey,"urlBase").toString().trim()+role);
    		
	    	Cookie kukki = new Cookie.Builder("Authorization", token).build();
	    	System.out.println("Request:"+ jsonRequest.toString());
          
	    	response = given().cookie(kukki).contentType(ContentType.JSON).body(jsonRequest.toString()).patch(url);
	    	if(response.getStatusCode() == 401 || response.getStatusCode() == 500 ) {
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
	    	
	    String cookie = response.getHeader("set-cookie");
    	if(cookie != null) {
    		
    		String token = cookie.split("=")[1];
    		
    		  tokens.put(VariableManager.getVariableValue(contextKey,"urlBase").toString().trim()+role, token);
    	}
    	if(response.getBody().asString().startsWith("{")) {
    		System.out.println("Response:"+response.getBody().asString());
    		checkErrorResponse(response.getBody().asString());
    	
    		return new JSONObject(response.getBody().asString()).getJSONObject(dataKey);
    	}
    	else {
    		return new JSONObject("{\"status\":\""+ response.getBody().asString() + "\"}" );
    	}
    }

	public  static boolean initToken(String contextKey){
	        try {		
	        
				JSONObject requestBody = new JSONObject();
				JSONObject nestedRequest = new JSONObject();
				nestedRequest.put("userName", VariableManager.getVariableValue(contextKey,"admin_userName").toString());
				nestedRequest.put("password",  VariableManager.getVariableValue(contextKey,"admin_password").toString());
	            nestedRequest.put("appId", VariableManager.getVariableValue(contextKey,"mosip_admin_app_id").toString());
	            nestedRequest.put("clientId",  VariableManager.getVariableValue(contextKey,"mosip_admin_client_id").toString());
	            nestedRequest.put("clientSecret",  VariableManager.getVariableValue(contextKey,"mosip_admin_client_secret").toString());
				requestBody.put("metadata",new JSONObject());
				requestBody.put("version", "1.0");
				requestBody.put("id", "mosip.authentication.useridPwd");
				requestBody.put("requesttime", CommonUtil.getUTCDateTime(LocalDateTime.now()).toString());
				requestBody.put("request", nestedRequest);

	            //authManagerURL
	            //String AUTH_URL = "v1/authmanager/authenticate/internal/useridPwd";
				//VariableManager.NS_DEFAULT
				//VariableManager.NS_DEFAULT
				String authUrl = VariableManager.getVariableValue(contextKey,"urlBase").toString().trim() + VariableManager.getVariableValue(VariableManager.NS_DEFAULT,"authManagerURL").toString().trim();
				String jsonBody = requestBody.toString(); 
				logger.info("Neeharika initToken logger " + authUrl + " Auth URL"+  jsonBody);
				//System.out.println("Neeharika Syso " + authUrl + " Auth URL"+  jsonBody);
				Response response =null;
				try {
					response = given()
	            		.contentType("application/json")
	            		.body(jsonBody)
	            		.post(authUrl);
					
				}catch(Exception e) {
					e.printStackTrace();
				}
					
	            if (response.getStatusCode() != 200 || response.toString().contains("errorCode")) {
	            	boolean bSlackit = VariableManager.getVariableValue(contextKey,"post2slack") == null ? false : Boolean.parseBoolean(VariableManager.getVariableValue(contextKey,"post2slack").toString()) ;
	            	if(bSlackit)
	            		SlackIt.postMessage(null,
	            				authUrl  + " Failed to authenticate, Is " +  VariableManager.getVariableValue(contextKey,"urlBase").toString() + " down ?");
	            	
	            	return false;
	            }
				String responseBody = response.getBody().asString();
				String token = new JSONObject(responseBody).getJSONObject(dataKey).getString("token");
	            //refreshToken = new JSONObject(response.getBody().asString()).getJSONObject(dataKey).getString("refreshToken");
	           
	          // String token=response.getCookie("Authorization");
	            tokens.put(VariableManager.getVariableValue(contextKey,"urlBase").toString().trim()+"system", token);
	            //tokens.put("admin", token);
				return true;	
			}
			catch(Exception  ex){
				
			}
	        return false;
	  		
	        //https://dev.mosip.net/v1/authmanager/authenticate/internal/useridPwd
	    }
	public  static boolean initToken_admin(String contextKey){
        try {		
        	
			JSONObject requestBody = new JSONObject();
			JSONObject nestedRequest = new JSONObject();
			nestedRequest.put("userName", VariableManager.getVariableValue(contextKey,"admin_userName").toString());
			nestedRequest.put("password",  VariableManager.getVariableValue(contextKey,"admin_password").toString());
            nestedRequest.put("appId", VariableManager.getVariableValue(contextKey,"mosip_admin_app_id").toString());
            nestedRequest.put("clientId",  VariableManager.getVariableValue(contextKey,"mosip_admin_client_id").toString());
            nestedRequest.put("clientSecret",  VariableManager.getVariableValue(contextKey,"mosip_admin_client_secret").toString());
			requestBody.put("metadata",new JSONObject());
			requestBody.put("version", "1.0");
			requestBody.put("id", "mosip.authentication.useridPwd");
			requestBody.put("requesttime", CommonUtil.getUTCDateTime(LocalDateTime.now()).toString());
			requestBody.put("request", nestedRequest);

            //authManagerURL
            //String AUTH_URL = "v1/authmanager/authenticate/internal/useridPwd";
			
			String authUrl = VariableManager.getVariableValue(contextKey,"urlBase").toString().trim() + VariableManager.getVariableValue(VariableManager.NS_DEFAULT,"authManagerURL").toString().trim();
			String jsonBody = requestBody.toString(); 
			logger.info("Neeharika initToken_admin logger " + authUrl + " Auth URL"+  jsonBody);
			Response response =null;
			try {
				response = given()
            		.contentType("application/json")
            		.body(jsonBody)
            		.post(authUrl);
				
			}catch(Exception e) {
				e.printStackTrace();
			}
				
            if (response.getStatusCode() != 200 || response.toString().contains("errorCode")) {
            	boolean bSlackit = VariableManager.getVariableValue(contextKey,"post2slack") == null ? false : Boolean.parseBoolean(VariableManager.getVariableValue(contextKey,"post2slack").toString()) ;
            	if(bSlackit)
            		SlackIt.postMessage(null,
            				authUrl  + " Failed to authenticate, Is " +  VariableManager.getVariableValue(contextKey,"urlBase").toString() + " down ?");
            	
            	return false;
            }
           String responseBody = response.getBody().asString();
           String token = new JSONObject(responseBody).getJSONObject(dataKey).getString("token");
            //refreshToken = new JSONObject(response.getBody().asString()).getJSONObject(dataKey).getString("refreshToken");
           
            //String token=response.getCookie("Authorization");
            
            tokens.put(VariableManager.getVariableValue(contextKey,"urlBase").toString().trim()+"admin", token);
          
			return true;	
		}
		catch(Exception  ex){
			
		}
        return false;
  		
        //https://dev.mosip.net/v1/authmanager/authenticate/internal/useridPwd
    }
	public  static boolean initToken_Resident(String contextKey){
        try {		
			JSONObject requestBody = new JSONObject();
			JSONObject nestedRequest = new JSONObject();
			nestedRequest.put("userName", VariableManager.getVariableValue(contextKey,"operatorId"));
			nestedRequest.put("password",  VariableManager.getVariableValue(contextKey,"password"));
			nestedRequest.put("appId", VariableManager.getVariableValue(contextKey,"mosip_resident_app_id"));
			nestedRequest.put("clientId", VariableManager.getVariableValue(contextKey,"mosip_resident_client_id"));
			nestedRequest.put("secretKey", VariableManager.getVariableValue(contextKey,"mosip_resident_client_secret"));
			requestBody.put("metadata",new JSONObject());
			requestBody.put("version", "string");
			requestBody.put("id", "string");
			requestBody.put("requesttime", CommonUtil.getUTCDateTime(LocalDateTime.now()).toString());
			requestBody.put("request", nestedRequest);

            //authManagerURL
            //String AUTH_URL = "v1/authmanager/authenticate/internal/useridPwd";
			String authUrl = VariableManager.getVariableValue(contextKey,"urlBase").toString().trim() +"v1/authmanager/authenticate/clientidsecretkey";
			
			//String authUrl = VariableManager.getVariableValue(contextKey,"urlBase").toString().trim() + VariableManager.getVariableValue(contextKey,"authManagerURL").toString().trim();
			String jsonBody = requestBody.toString(); 
			logger.info("Neeharika initToken_Resident logger " + authUrl + " Auth URL"+  jsonBody);
			
			Response response =null;
			try {
				response = given()
            		.contentType("application/json")
            		.body(jsonBody)
            		.post(authUrl);
				
			}catch(Exception e) {
				e.printStackTrace();
			}
				
            if (response.getStatusCode() != 200 || response.toString().contains("errorCode")) {
            	boolean bSlackit = VariableManager.getVariableValue(contextKey,"post2slack") == null ? false : Boolean.parseBoolean(VariableManager.getVariableValue(contextKey,"post2slack").toString()) ;
            	if(bSlackit)
            		SlackIt.postMessage(null,
            				authUrl  + " Failed to authenticate, Is " +  VariableManager.getVariableValue(contextKey,"urlBase").toString() + " down ?");
            	
            	return false;
            }
           // String responseBody = response.getBody().asString();
            //token = new JSONObject(responseBody).getJSONObject(dataKey).getString("token");
            //refreshToken = new JSONObject(response.getBody().asString()).getJSONObject(dataKey).getString("refreshToken");
            String token=response.getCookie("Authorization");
            tokens.put(VariableManager.getVariableValue(contextKey,"urlBase").toString().trim()+"resident", token);
            
			return true;	
		}
		catch(Exception  ex){
			
		}
        return false;
  		
        //https://dev.mosip.net/v1/authmanager/authenticate/internal/useridPwd
    }
	 private static void checkErrorResponse(String response) throws Exception {
	        //TODO: Handle 401 or token expiry
	        JSONObject jsonObject =  new JSONObject(response);
	        boolean err = false;
	        if(jsonObject.has(errorKey)) {
	        	Object errObject = jsonObject.get(errorKey) ;
	        	if(errObject instanceof JSONArray){
	        		if(!((JSONArray)errObject).isEmpty())
	        		err= true;
	        	}
	        	else
	        	if(errObject != JSONObject.NULL)
	        		err = true;
	        }
	        if(err)	
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
		headers.forEach( (k,v) ->{
			conn.addRequestProperty(k.toString(), v.toString());
		});
		http_status = conn.getResponseCode();
		 
		if (http_status == 200) {
		
	        BufferedReader br = new BufferedReader(new InputStreamReader(
	            (conn.getInputStream())));

	        String output;
	        
	        while ((output = br.readLine()) != null) {
	            builder.append(output);
	        }

		}
		return builder.toString();
	}

	public static JSONArray getJsonArray(String url, JSONObject requestParams, JSONObject pathParam,String contextKey) throws Exception {
       
		String role = "system";
        if (!isValidToken(role,contextKey)){
        	initToken(contextKey);
        }
    	boolean bDone = false;
    	int nLoop  = 0;
    	Response response =null;

    	//Stict http validation errors - fix
    	
    	/*if(url.contains("//")) {
    		url = url.replace("//", "/"); 		
    	}*/
    	while(!bDone) {

    		//String token = tokens.get(role);
    		String token= tokens.get(VariableManager.getVariableValue(contextKey,"urlBase").toString().trim()+role);
    		
    		Cookie kukki = new Cookie.Builder("Authorization", token).build();
    		Map<String,Object> mapParam = requestParams == null ? null: requestParams.toMap();
        		//new Gson().fromJson(requestParams.toString(), HashMap.class);
    		Map<String,Object> mapPathParam =pathParam == null ? null: pathParam.toMap();
        
        	//new Gson().fromJson(pathParam.toString(), HashMap.class);
        
    		response = given().cookie(kukki).contentType(ContentType.JSON).queryParams(mapParam).get(url,mapPathParam );
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

        if(response != null) {
        	System.out.println("hello");
        	
        }
        checkErrorResponse(response.getBody().asString());

        return new JSONObject(response.getBody().asString()).getJSONArray(dataKey);
    }

	public static String rawHttp(HttpRCapture httpRCapture, String jsonBody) throws IOException {
	
		 String result ="";
		//StringBuilder builder = new StringBuilder();

		CloseableHttpClient httpClient = HttpClients.createDefault();
		 httpRCapture.setEntity(new StringEntity(jsonBody));
		HttpResponse response = httpClient.execute(httpRCapture);
		HttpEntity entity = response.getEntity();
        if (entity != null) {
            
            result = EntityUtils.toString(entity);
            System.out.println(result);
        }
		return result;
		/*
		URL _url = new URL(url);
		
		
		HttpURLConnection conn = (HttpURLConnection) _url.openConnection();
		conn.setDoOutput(true);
		conn.setRequestMethod(custMethod);
		conn.setRequestProperty("Content-Type", "application/json");
		conn.setRequestProperty("Accept", "application/json");

		conn.setRequestProperty( "Content-Length", Integer.toString( jsonBody.length() ));
		conn.setUseCaches( false );
		conn.setRequestProperty( "charset", "utf-8");
		
		try( DataOutputStream wr = new DataOutputStream( conn.getOutputStream())) {
		   wr.writeBytes( jsonBody );
		}
		int status = conn.getResponseCode();
		 
		if (status == 200) {
		
	        BufferedReader br = new BufferedReader(new InputStreamReader(
	            (conn.getInputStream())));

	        String output;
	        
	        while ((output = br.readLine()) != null) {
	            builder.append(output);
	        }

		}
		return builder.toString();
		*/
	}

	public static boolean checkActuator(String url,String contextKey) {
	
		String urlAct = url + "/actuator/health";

		String role = "system";
        if (!isValidToken(role,contextKey)){
        	initToken(contextKey);
        }
    	boolean bDone = false;
    	int nLoop  = 0;
    	Response response =null;

    	//Stict http validation errors - fix
    	
    	/*if(url.contains("//")) {
    		url = url.replace("//", "/"); 		
    	}*/
    	while(!bDone) {

    		//String token = tokens.get(role);
    		String token= tokens.get(VariableManager.getVariableValue(contextKey,"urlBase").toString().trim()+role);
    		
    		Cookie kukki = new Cookie.Builder("Authorization", token).build();

    		response = given().cookie(kukki).contentType(ContentType.JSON).get(url);

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
    	
        if(response != null && 	response.getStatusCode() == 200 ) {
        	
        	System.out.println(response.getBody().asString());        	

        	JSONObject jsonResponse = new JSONObject(response.getBody().asString());
        	
        	if(jsonResponse.getString("status").equals("UP")) {
        		return true;
        	}
        	return false;
        }
        else
        	return false;
	}

	public static boolean checkActuatorNoAuth(String url) {
		
		String urlAct = url + "/actuator/health";

    	Response response =null;

    		response = given().contentType(ContentType.JSON).get(urlAct);

   	
        if(response != null && 	response.getStatusCode() == 200 ) {
        	
        	System.out.println(response.getBody().asString());        	

        	JSONObject jsonResponse = new JSONObject(response.getBody().asString());
        	
        	if(jsonResponse.getString("status").equals("UP")) {
        		return true;
        	}
        	return false;
        }
        else
        	return false;
	}
	

}
