package org.mosip.dataprovider.test;


import org.json.JSONObject;
import org.mosip.dataprovider.util.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.cucumber.java.en.Then;
import variables.VariableManager;

public class ResidentRegistration {
	private static final Logger logger = LoggerFactory.getLogger(ResidentRegistration.class);

	@Then("^register$")
	public static void register(String contextKey) {
		String retVal="";
		String url = VariableManager.getVariableValue(contextKey,"packetUtilityBaseUrl") +
				VariableManager.getVariableValue(contextKey, "makeandsyncpacket").toString();
		String preRegID = VariableManager.getVariableValue(contextKey,"PRID").toString();
		String rid ="";
		try {
			JSONObject response = RestClient.get(url + preRegID,new JSONObject(),new JSONObject(),contextKey);
			rid = response.get("registrationId").toString();
			VariableManager.setVariableValue("RID", rid,contextKey);
			retVal = response.toString();
			
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		System.out.println(rid);
	}
	@Then("^check registration status$")
	public static void checkRegistrationStatus(String contextKey) {
		String RegID = VariableManager.getVariableValue(contextKey,"RID").toString();
		String url = VariableManager.getVariableValue(contextKey,"packetUtilityBaseUrl") +
				VariableManager.getVariableValue( contextKey,"makeandsyncpacket").toString();
		
	}
}
