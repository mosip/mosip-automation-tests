package org.mosip.dataprovider.test.partnerManagement;

import java.time.LocalDateTime;
import org.json.JSONObject;
import org.mosip.dataprovider.util.CommonUtil;
import org.mosip.dataprovider.util.RestClient;

import variables.VariableManager;

public class CertificateUploader{

    public static String uploadCACertificate(String certificateData ,String partnerDomain){

        String url = VariableManager.getVariableValue("urlBase").toString() +
        VariableManager.getVariableValue("partner").toString() +
        VariableManager.getVariableValue("CACertificate").toString();

        JSONObject payload = new JSONObject();
        JSONObject request = new JSONObject();

        payload.put("id", "string");
        payload.put("metadata", new JSONObject());

        request.put("certificateData", certificateData);
        request.put("partnerDomain", partnerDomain);

        payload.put("request", request);

        payload.put("requesttime", CommonUtil.getUTCDateTime(LocalDateTime.now()));
		payload.put("version", "1.0");

        JSONObject resp;
        try{
            resp = RestClient.post(url, payload);
            return resp.toString();
        }
        catch(Exception e){
            e.printStackTrace();
            return e.getMessage();
        }

    }

    public static String uploadPartnerString(String certificateData, String orgName, String partnerID, String partnerDomain){

        String url = VariableManager.getVariableValue("urlBase").toString() +
        VariableManager.getVariableValue("partner").toString() +
        VariableManager.getVariableValue("partnerCertificate").toString();

        JSONObject payload = new JSONObject();
        JSONObject request = new JSONObject();

        payload.put("id", "string");
        payload.put("metadata", new JSONObject());

        request.put("certificateData", certificateData);
        request.put("organizationName", orgName);
        request.put("partnerDomain", partnerDomain);
        request.put("partnerId", partnerID);
        request.put("partnerType", "Auth_Partner");

        payload.put("request", request);

        payload.put("requesttime", CommonUtil.getUTCDateTime(LocalDateTime.now()));
		payload.put("version", "v1.0");

        JSONObject resp;
        try{
            resp = RestClient.post(url, payload);
            return resp.toString();
        }
        catch(Exception e){
            e.printStackTrace();
            return e.getMessage();
        }


    }

    public static void main(String[] args){

        System.out.println(uploadCACertificate("certificateData", "partnerDomain"));
    }
}

