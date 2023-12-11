package io.mosip.testrig.dslrig.dataprovider.test.partnerManagement;

import java.time.LocalDateTime;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.mosip.testrig.dslrig.dataprovider.util.CommonUtil;
import io.mosip.testrig.dslrig.dataprovider.util.RestClient;
import io.mosip.testrig.dslrig.dataprovider.variables.VariableManager;


public class PartnerRequest {
	private static final Logger logger = LoggerFactory.getLogger(PartnerRequest.class);

    public static String submitAPIKeyRequest(String partnerID, String policyName, String useCaseDesc,String contextKey){
    	

        String url = VariableManager.getVariableValue(contextKey,"urlBase").toString() +
        VariableManager.getVariableValue(contextKey,"partner").toString() +
        partnerID +
        VariableManager.getVariableValue(contextKey,"apiKeyRequest").toString();
        

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
            resp = RestClient.patch(url, payload,contextKey);
            return resp.toString();
        }
        catch(Exception ex){

            logger.error(ex.getMessage());
            return ex.getMessage();
        }
    }

    public static String approvePartnerAPIKeyRequest(String contextKey,String apiKeyRequestID){

        String url = VariableManager.getVariableValue(contextKey,"urlBase").toString() +
        VariableManager.getVariableValue(contextKey,"partner").toString() +
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
            resp = RestClient.patch(url, payload,contextKey);
            return resp.toString();
        }
        catch(Exception ex){
            logger.error(ex.getMessage());
            return ex.getMessage();
        }
        

    }

    public static String selfRegister(JSONObject selfRegisterRequest,String contextKey){

        selfRegisterRequest.put("requesttime", CommonUtil.getUTCDateTime(LocalDateTime.now()));
		selfRegisterRequest.put("version", "v1.0");

		String url = VariableManager.getVariableValue(contextKey,"urlBase").toString() +
		            VariableManager.getVariableValue(contextKey,"partner").toString();


		try {

			JSONObject resp = RestClient.post(url, selfRegisterRequest,contextKey);
			return resp.toString();
		}
		catch(Exception ex){
			logger.error(ex.getMessage());
            return ex.getMessage();
		}

    }

    public static String updatePartnerStatus(String contextKey,String partnerId, String status){

		String url = VariableManager.getVariableValue(contextKey,"urlBase").toString() +
		VariableManager.getVariableValue(contextKey,"partner").toString() + partnerId; 

        JSONObject request = new JSONObject();
        request.put("status", status);

        JSONObject updateStatusReq = new JSONObject();
        updateStatusReq.put("id", "string");
        updateStatusReq.put("metadata", new JSONObject());
        updateStatusReq.put("request", request);
        updateStatusReq.put("requesttime", CommonUtil.getUTCDateTime(LocalDateTime.now()));
		updateStatusReq.put("version", "v1.0");

      

		try {

			JSONObject resp = RestClient.patch(url, updateStatusReq,contextKey);
			return resp.toString();
		}
		catch(Exception ex){
			logger.error(ex.getMessage());
            return ex.getMessage();
		}

    }

}
