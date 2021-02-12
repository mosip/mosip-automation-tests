package org.mosip.dataprovider;

import org.mosip.dataprovider.util.RestClient;

public class LocationProviderBase {

	//protected static String CSCAPI_KEY = "SlVtWERyMWVCTnJQTmFsTnFmbTZXYU55d0FaZFV2ckhxOGRSV2xHbw==";
	protected static String Application_Id = "ZpEp9ui1KRmZRlE7JO7f5ISIPXV4ZBbnNUzT6ok9"; // This is your app's application id
	protected static String REST_API_Key = "PjX5iVdznEGZ0DJpl98TdwOxGEXDiewTJw7opRGf"; // This is your app's REST API key
    
	//protected static String baseURL = "https://api.countrystatecity.in/v1"; //countries/MO/";
	
	protected static String baseURL = "https://parseapi.back4app.com/classes";
	
	//https://parseapi.back4app.com/classes/Continentscountriescities_Subdivisions_States_Provinces?limit=99999&order=Subdivision_Name&where=" + where
	
	protected RestClient client;
	
	public LocationProviderBase() {
		client = new RestClient(baseURL);
		//client.addHeader("X-CSCAPI-KEY", CSCAPI_KEY);
		client.addHeader("X-Parse-Application-Id",Application_Id);
		client.addHeader("X-Parse-REST-API-Key",REST_API_Key);
		
	}
}
