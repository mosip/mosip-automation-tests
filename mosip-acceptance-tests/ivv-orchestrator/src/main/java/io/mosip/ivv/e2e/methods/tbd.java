package io.mosip.ivv.e2e.methods;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class tbd {
public static void main(String[] args) {
//	Response response = RestAssured.given().with().auth().preemptive().basic("admin", "7hbNpX0O3k").
//			header("Content-Type", "application/x-www-form-urlencoded").formParam("grant_type", "password") .formParam("client_id", "admin-cli")
//			 .formParam("username", "admin")
//            .formParam("password", "7hbNpX0O3k")
//			.when().post("https://iam.dev3.mosip.net/auth/realms/master/protocol/openid-connect/token");
//	System.out.println(response.getBody().asString());
	
	Response response = RestAssured.given().with().auth().preemptive().basic("admin", "7hbNpX0O3k").
			header("Content-Type", "application/x-www-form-urlencoded").formParam("grant_type", "password") .formParam("client_id", "admin-cli")
			 .formParam("username", "admin")
            .formParam("password", "7hbNpX0O3k")
			.when().post("https://iam.dev3.mosip.net/auth/realms/master/protocol/openid-connect/token");
	System.out.println(response.getBody().asString());
	  
	
	
}}
