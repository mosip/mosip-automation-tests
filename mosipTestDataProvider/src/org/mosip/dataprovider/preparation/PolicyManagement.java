package org.mosip.dataprovider.preparation;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import javax.print.DocFlavor.STRING;

import org.json.JSONArray;
import org.json.JSONObject;
import org.mosip.dataprovider.util.RestClient;
import org.mosip.dataprovider.util.CommonUtil;

import variables.VariableManager;

public class PolicyManagement {
    

    public static Object getCache(String key,String contextKey) {
		try {
		return VariableManager.getVariableValue(contextKey,key);
		}catch(Exception e) {
			
		}
		return null;
	}
	public static void setCache(String key, Object value,String contextKey) {
		
		VariableManager.setVariableValue(contextKey,key,  value);
	}

    public static String createNewPolicyGroup(String groupname, String policydesc,String contextKey){

		//System.out.println(VariableManager.getVariableValue(VariableManager.NS_POLICIES,"policymanagement").toString());
        //System.out.print(VariableManager.getVariableValue("urlBase").toString());
		String url = VariableManager.getVariableValue(contextKey,"urlBase").toString() +
		VariableManager.getVariableValue(VariableManager.NS_DEFAULT,"policymanagement").toString() + "group/new";

		try {
			JSONObject policyGroupDetails = new JSONObject();
			policyGroupDetails.put("desc", policydesc);
			policyGroupDetails.put("name", groupname);

			JSONObject newPolicyGroup = new JSONObject();
			newPolicyGroup.put("id", "string");
			newPolicyGroup.put("metadata", new JSONObject());
			newPolicyGroup.put("request", policyGroupDetails);
			newPolicyGroup.put("requesttime", CommonUtil.getUTCDateTime(LocalDateTime.now()));
			newPolicyGroup.put("version", "v1.0");

			JSONObject resp = RestClient.post(url, newPolicyGroup,contextKey);
			return resp.toString();
		}
		catch(Exception e){
			e.printStackTrace();
			return "{\"post failed\"}";
		}

    }

	public static String getPolicyGroupIDByName(String policygrpname,String contextKey){

		String url = VariableManager.getVariableValue(contextKey,"urlBase").toString() + 
			VariableManager.getVariableValue(VariableManager.NS_DEFAULT, "policymanagement").toString() + "group/all";
		String groupID = "{\"groupName Does not exist\"}";
		try {
			JSONArray resp = RestClient.getJsonArray(url, new JSONObject(), new JSONObject(),contextKey);
			
			for(int i=0; i<resp.length(); i++){
				JSONObject policyElement = resp.getJSONObject(i);
				JSONObject policyGroup = (JSONObject) policyElement.get("policyGroup");
				String policyGroupName = (String) policyGroup.get("name");
				if(policyGroupName.equals(policygrpname)){
					groupID = (String) policyGroup.get("id");
				}
			}
			
			return groupID;

		} catch (Exception e) {
			//TODO: handle exception
			e.printStackTrace();
			return "{\"post failed\"}";
		}
		
	}


	public static String createPolicyUnderGroup(String grpname, String policyname, String policydesc, String policyType,  JSONObject policiesDefnJson,String contextKey){

		String url = VariableManager.getVariableValue(contextKey,"urlBase").toString() + 
			VariableManager.getVariableValue(VariableManager.NS_DEFAULT, "policymanagement").toString();
		try {

			JSONObject policyDefn = new JSONObject();
			policyDefn.put("desc", policydesc);
			policyDefn.put("name", policyname);
			policyDefn.put("policies", policiesDefnJson);
			policyDefn.put("policyGroupName", grpname);
			policyDefn.put("policyId", "string");
			policyDefn.put("policyType",  policyType);
			policyDefn.put("version", "string");


			JSONObject newPolicyUnderGroup = new JSONObject();
			newPolicyUnderGroup.put("id", "string");
			newPolicyUnderGroup.put("metadata", new JSONObject());
			newPolicyUnderGroup.put("request", policyDefn);
			newPolicyUnderGroup.put("requesttime", CommonUtil.getUTCDateTime(LocalDateTime.now()));
			newPolicyUnderGroup.put("version", "v1.0");


			JSONObject resp = RestClient.post(url, newPolicyUnderGroup,contextKey);
			return resp.toString();
			
		} catch (Exception e) {
			//TODO: handle exception
			e.printStackTrace();
			return "{\"post failed\"}";
		}

	}


	public static String publishPolicy(String policyId, String groupId,String contextKey){

		String url = VariableManager.getVariableValue(contextKey,"urlBase").toString() + 
			VariableManager.getVariableValue(VariableManager.NS_DEFAULT, "policymanagement").toString() + policyId + "/group/" + groupId + "/publish";
		try {
			JSONObject resp = RestClient.post(url, new JSONObject(),contextKey);
			return resp.toString();
			
		} catch (Exception e) {
			//TODO: handle exception
			e.printStackTrace();
			return "{\"post failed\"}";
		}

	}

	public static void main(String[] args) {
		
		// String resp = getPolicyGroupIDByName("policyGroupBanking");
		
	}
}
