package org.mosip.dataprovider.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;

import java.net.URL;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
//import org.json.JSONArray;
import org.json.JSONObject;
import org.mosip.dataprovider.mds.HttpRCapture;

import io.restassured.response.Response;
import variables.VariableManager;

import static io.restassured.RestAssured.given;


import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.http.Cookie;
import io.restassured.http.Header;

public class RestClient {

	static String dataKey = "response";
    static String errorKey = "errors";
	//static String token;
	static Map<String, String> tokens = new HashMap<String,String>();
	static String refreshToken;

	static {
	//	initToken();
		RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
	}
	String _urlBase;

	int http_status;
	Properties headers;
	
	public static Boolean isValidToken(String role) {
		
		Object obj = VariableManager.getVariableValue("urlSwitched");
    	if(obj != null) {
    		Boolean bClear = Boolean.valueOf(obj.toString());
    		if(bClear)
    			return false;
    	}
    	
    	String token = tokens.get(role);
    	
    	return  !(null == token);
    	
	}
	public static void clearToken() {
		tokens.remove("system");
		tokens.remove("resident");
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
	public static JSONObject get(String url, JSONObject requestParams, JSONObject pathParam) throws Exception {
       
		String role = "system";
        if (!isValidToken(role)){
        	initToken();
        }
    	boolean bDone = false;
    	int nLoop  = 0;
    	Response response =null;

    	while(!bDone) {

    		String token = tokens.get(role);
        	
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
    				initToken();
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

        return new JSONObject(response.getBody().asString()).getJSONObject(dataKey);
    }
	public static JSONObject getNoAuth(String url, JSONObject requestParams, JSONObject pathParam) throws Exception {
	       
		String token = tokens.get("resident");
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
	
	public static JSONObject uploadFile(String url, String filePath, JSONObject requestData) throws Exception {
		String role = "resident";
	
		if (!isValidToken(role)){
            initToken();
        }


		 Response response =null;


	    String token = tokens.get(role);
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
	public static JSONObject postNoAuth(String url, JSONObject jsonRequest) throws Exception {

		String role = "resident";
		String token = tokens.get(role);
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
    		tokens.put(role,token);
    	}
    	System.out.println(token);
        System.out.println("Response:"+response.getBody().asString());
        checkErrorResponse(response.getBody().asString());

        return new JSONObject(response.getBody().asString()).getJSONObject(dataKey);
    }

	public static JSONObject post(String url, JSONObject jsonRequest) throws Exception {
		String role = "system";
		if (!isValidToken(role)){
            initToken();
        }
		boolean bDone = false;
	    int nLoop  = 0;
	    Response response =null;

	    
	    while(!bDone) {
	    	String token = tokens.get(role);
	    	
	    	Cookie kukki = new Cookie.Builder("Authorization", token).build();
	    	System.out.println("Request:"+ jsonRequest.toString());
          
	    	response = given().cookie(kukki).contentType(ContentType.JSON).body(jsonRequest.toString()).post(url);
	    	if(response.getStatusCode() == 401 || response.getStatusCode() == 500 ) {
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
	    	
	    String cookie = response.getHeader("set-cookie");
    	if(cookie != null) {
    		
    		String token = cookie.split("=")[1];
    		tokens.put(role, token);
    	}
        System.out.println("Response:"+response.getBody().asString());
        checkErrorResponse(response.getBody().asString());

        return new JSONObject(response.getBody().asString()).getJSONObject(dataKey);
    }
	public  static boolean initToken(){
	        try {		
				JSONObject requestBody = new JSONObject();
				JSONObject nestedRequest = new JSONObject();
				nestedRequest.put("userName", VariableManager.getVariableValue("operatorId"));
				nestedRequest.put("password",  VariableManager.getVariableValue("password"));
	            nestedRequest.put("appId", VariableManager.getVariableValue("appId"));
	            nestedRequest.put("clientId",  VariableManager.getVariableValue("clientId"));
	            nestedRequest.put("clientSecret",  VariableManager.getVariableValue("secretKey"));
				requestBody.put("metadata",new JSONObject());
				requestBody.put("version", "1.0");
				requestBody.put("id", "mosip.authentication.useridPwd");
				requestBody.put("requesttime", CommonUtil.getUTCDateTime(LocalDateTime.now()).toString());
				requestBody.put("request", nestedRequest);

	            //authManagerURL
	            //String AUTH_URL = "v1/authmanager/authenticate/internal/useridPwd";
				
				String authUrl = VariableManager.getVariableValue("urlBase").toString().trim() + VariableManager.getVariableValue("authManagerURL").toString().trim();
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
					
	            if (response.getStatusCode() != 200 || response.toString().contains("errorCode")) {
	            	boolean bSlackit = VariableManager.getVariableValue("post2slack") == null ? false : Boolean.parseBoolean(VariableManager.getVariableValue("post2slack").toString()) ;
	            	if(bSlackit)
	            		SlackIt.postMessage(null,
	            				authUrl  + " Failed to authenticate, Is " +  VariableManager.getVariableValue("urlBase").toString() + " down ?");
	            	
	            	return false;
	            }
	           // String responseBody = response.getBody().asString();
	            //token = new JSONObject(responseBody).getJSONObject(dataKey).getString("token");
	            //refreshToken = new JSONObject(response.getBody().asString()).getJSONObject(dataKey).getString("refreshToken");
	            String token=response.getCookie("Authorization");
	            tokens.put("system", token);
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

	
}
