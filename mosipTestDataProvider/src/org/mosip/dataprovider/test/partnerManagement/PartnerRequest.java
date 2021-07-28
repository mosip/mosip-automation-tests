package org.mosip.dataprovider.test.partnerManagement;

import java.time.LocalDateTime;

import org.json.JSONObject;
import org.mosip.dataprovider.util.CommonUtil;
import org.mosip.dataprovider.util.RestClient;

import variables.VariableManager;

public class PartnerRequest {
    
    public static String submitAPIKeyRequest(String partnerID, String policyName, String useCaseDesc){

        String url = VariableManager.getVariableValue("urlBase").toString() +
        VariableManager.getVariableValue("partner").toString() +
        partnerID +
        VariableManager.getVariableValue("apiKeyRequest").toString();
        

        JSONObject request = new JSONObject();
        request.put("policyName", policyName);
        request.put("useCaseDescription", useCaseDesc);

        JSONObject payload = new JSONObject();

        payload.put("id", "string");
        payload.put("metadata", new JSONObject());
        payload.put("request", request);

        payload.put("requesttime", CommonUtil.getUTCDateTime(LocalDateTime.now()));
		payload.put("version", "v1.0");
        
        JSONObject resp;

        try{
            resp = RestClient.patch(url, payload);
            return resp.toString();
        }
        catch(Exception ex){
            ex.printStackTrace();
            return ex.getMessage();
        }
    }

    public static String approvePartnerAPIKeyRequest(String apiKeyRequestID){

        String url = VariableManager.getVariableValue("urlBase").toString() +
        VariableManager.getVariableValue("partner").toString() +
        "apikey/" + apiKeyRequestID;

        JSONObject request = new JSONObject();
        request.put("status", "Approved");
        

        JSONObject payload = new JSONObject();

        payload.put("id", "string");
        payload.put("metadata", new JSONObject());
        payload.put("request", request);

        payload.put("requesttime", CommonUtil.getUTCDateTime(LocalDateTime.now()));
		payload.put("version", "v1.0");


        JSONObject resp;
        try{
            resp = RestClient.patch(url, payload);
            return resp.toString();
        }
        catch(Exception ex){
            ex.printStackTrace();
            return ex.getMessage();
        }
        

    }

}
