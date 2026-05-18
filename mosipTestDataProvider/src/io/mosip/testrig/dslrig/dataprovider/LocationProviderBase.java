package io.mosip.testrig.dslrig.dataprovider;

import io.mosip.testrig.dslrig.dataprovider.util.RestClient;

public class LocationProviderBase {


	protected static String Application_Id = "ZpEp9ui1KRmZRlE7JO7f5ISIPXV4ZBbnNUzT6ok9"; 
	protected static String REST_API_Key = ""; 


	protected static String baseURL = "https://parseapi.back4app.com/classes";


	protected RestClient client;

	public LocationProviderBase() {
		client = new RestClient(baseURL);

		client.addHeader("X-Parse-Application-Id",Application_Id);
		client.addHeader("X-Parse-REST-API-Key",REST_API_Key);

	}
}
