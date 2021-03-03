package org.mosip.dataprovider.test;


import org.json.JSONObject;

import org.mosip.dataprovider.util.RestClient;

import io.cucumber.java.en.Then;
import variables.VariableManager;

public class ResidentRegistration {

	@Then("^register$")
	public static void register() {
		String retVal="";
		String url = VariableManager.getVariableValue("packetutilURLBase") +
				VariableManager.getVariableValue( "makeandsyncpacket").toString();
		String preRegID = VariableManager.getVariableValue("PRID").toString();
		String rid ="";
		try {
			JSONObject response = RestClient.get(url + preRegID,new JSONObject(),new JSONObject());
			rid = response.get("registrationId").toString();
			VariableManager.setVariableValue("RID", rid);
			retVal = response.toString();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(rid);
	}
	@Then("^check registration status$")
	public static void checkRegistrationStatus() {
		String RegID = VariableManager.getVariableValue("RID").toString();
		String url = VariableManager.getVariableValue("packetutilURLBase") +
				VariableManager.getVariableValue( "makeandsyncpacket").toString();
		
	}
}
