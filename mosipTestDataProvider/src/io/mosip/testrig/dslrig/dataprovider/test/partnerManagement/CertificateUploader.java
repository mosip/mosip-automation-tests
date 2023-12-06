package io.mosip.testrig.dslrig.dataprovider.test.partnerManagement;

import java.time.LocalDateTime;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.mosip.testrig.dslrig.dataprovider.util.CommonUtil;
import io.mosip.testrig.dslrig.dataprovider.util.RestClient;
import io.mosip.testrig.dslrig.dataprovider.variables.VariableManager;

public class CertificateUploader{
	private static final Logger logger = LoggerFactory.getLogger(CertificateUploader.class);
    public static String uploadCACertificate(String certificateData ,String partnerDomain, String contextKey){

        String url = VariableManager.getVariableValue(contextKey,"urlBase").toString()+
        VariableManager.getVariableValue(contextKey,"partner").toString() +
        VariableManager.getVariableValue(contextKey,"CACertificate").toString();

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
            resp = RestClient.post(url, payload,contextKey);
            return resp.toString();
        }
        catch(Exception e){
            logger.error(e.getMessage());
            return e.getMessage();
        }

    }

    public static String uploadPartnerString(String certificateData, String orgName, String partnerID, String partnerDomain,String contextKey){

        String url = VariableManager.getVariableValue(contextKey,"urlBase").toString() +
        VariableManager.getVariableValue(contextKey,"partner").toString() +
        VariableManager.getVariableValue(contextKey,"partnerCertificate").toString();

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
            resp = RestClient.post(url, payload,contextKey);
            return resp.toString();
        }
        catch(Exception e){
            logger.error(e.getMessage());
            return e.getMessage();
        }


    }

    public static void main(String[] args){
String data=uploadCACertificate("certificateData", "partnerDomain","contextKey");
        logger.info(data);
    }
}

