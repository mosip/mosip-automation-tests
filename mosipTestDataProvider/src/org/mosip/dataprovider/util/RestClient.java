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

import java.util.Map;
import java.util.Properties;

import org.json.JSONObject;


import io.restassured.response.Response;
import variables.VariableManager;

import static io.restassured.RestAssured.given;


import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.http.Cookie;
public class RestClient {

	static String dataKey = "response";
    static String errorKey = "errors";
	static String token;
	static String refreshToken;

	static {
		initToken();
		RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
	}
	String _urlBase;

	int http_status;
	Properties headers;
	
	public static Boolean isValidToken() {
		
		Object obj = VariableManager.getVariableValue("urlSwitched");
    	if(obj != null) {
    		Boolean bClear = Boolean.valueOf(obj.toString());
    		if(bClear)
    			return false;
    	}
    	return  !(null == token);
    	
	}
	public static void clearToken() {
		token = null;
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
	public static JSONObject get(String url, JSONObject requestParams, JSONObject pathParam) throws Exception {
       
        if (!isValidToken()){
            initToken();
        }
        Cookie kukki = new Cookie.Builder("Authorization", token).build();
        Map<String,Object> mapParam = requestParams == null ? null: requestParams.toMap();
        	//new Gson().fromJson(requestParams.toString(), HashMap.class);
        Map<String,Object> mapPathParam =pathParam == null ? null: pathParam.toMap();
        
        	//new Gson().fromJson(pathParam.toString(), HashMap.class);
        
        Response response = given().cookie(kukki).contentType(ContentType.JSON).queryParams(mapParam).get(url,mapPathParam );

        if(response != null) {
        	System.out.println(response.getBody().asString());
        	
        }
        checkErrorResponse(response.getBody().asString());

        return new JSONObject(response.getBody().asString()).getJSONObject(dataKey);
    }
	
	public static JSONObject uploadFile(String url, String filePath, JSONObject requestData) throws Exception {
		 if (!isValidToken()){
            initToken();
        }
        Cookie kukki = new Cookie.Builder("Authorization", token).build();
        
        Response response = null;
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
/*
	public static JSONObject getNoCookie(String url, JSONObject requestParams, JSONObject pathParam) throws Exception {
	       
        if (null == token){
            initToken();
        }
        Response response = given().contentType(ContentType.JSON).queryParams(requestParams.toMap()).get(url,pathParam.toMap());

        if(response != null) {
        	System.out.println(response.getBody().asString());
        	
        	
        }
        checkErrorResponse(response.getBody().asString());

        return new JSONObject(response.getBody().asString()).getJSONObject(dataKey);
    }
*/
	public static JSONObject post(String url, JSONObject jsonRequest) throws Exception {
		if (!isValidToken()){
            initToken();
        }
        Cookie kukki = new Cookie.Builder("Authorization", token).build();
        System.out.println("Request:"+ jsonRequest.toString());
          
        Response response = given().cookie(kukki).contentType(ContentType.JSON).body(jsonRequest.toString()).post(url);
        String cookie = response.getHeader("set-cookie");
    	if(cookie != null) {
    		
    		token = cookie.split("=")[1];
    	}
        System.out.println("Response:"+response.getBody().asString());
        checkErrorResponse(response.getBody().asString());

        return new JSONObject(response.getBody().asString()).getJSONObject(dataKey);
    }
	public  static boolean initToken(){
	        try {		
				JSONObject requestBody = new JSONObject();
				JSONObject nestedRequest = new JSONObject();
				nestedRequest.put("userName", VariableManager.getVariableValue(VariableManager.NS_PREREG ,"operatorId"));
				nestedRequest.put("password",  VariableManager.getVariableValue(VariableManager.NS_PREREG ,"password"));
	            nestedRequest.put("appId", VariableManager.getVariableValue(VariableManager.NS_PREREG , "appId"));
	            nestedRequest.put("clientId",  VariableManager.getVariableValue(VariableManager.NS_PREREG ,"clientId"));
	            nestedRequest.put("clientSecret",  VariableManager.getVariableValue(VariableManager.NS_PREREG ,"secretKey"));
				requestBody.put("metadata",new JSONObject());
				requestBody.put("version", "1.0");
				requestBody.put("id", "mosip.authentication.useridPwd");
				requestBody.put("requesttime", CommonUtil.getUTCDateTime(LocalDateTime.now()).toString());
				requestBody.put("request", nestedRequest);

	            //authManagerURL
	            //String AUTH_URL = "v1/authmanager/authenticate/internal/useridPwd";
				
				String authUrl = VariableManager.getVariableValue("urlBase").toString() + VariableManager.getVariableValue("prereg" ,"authManagerURL").toString();
				String jsonBody = requestBody.toString(); 
				
				Response response =null;
				try {
					response = given()
	            		.contentType("application/json")
	            		.body(jsonBody)
	            		.post(authUrl);
					
				}catch(Exception e) {
					e.printStackTrace();
				}

	            if (response.toString().contains("errorCode")) return false;
	            String responseBody = response.getBody().asString();
	            //token = new JSONObject(responseBody).getJSONObject(dataKey).getString("token");
	            //refreshToken = new JSONObject(response.getBody().asString()).getJSONObject(dataKey).getString("refreshToken");
	            token=response.getCookie("Authorization");
	            
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

	        if(jsonObject.has(errorKey) && jsonObject.get(errorKey) != JSONObject.NULL) {
	            throw new Exception(String.valueOf(jsonObject.get(errorKey)));
	        }
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

	
}
