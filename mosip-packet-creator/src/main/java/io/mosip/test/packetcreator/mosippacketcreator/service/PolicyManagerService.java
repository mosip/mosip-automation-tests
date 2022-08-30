package io.mosip.test.packetcreator.mosippacketcreator.service;

import org.springframework.stereotype.Component;
import org.mosip.dataprovider.preparation.PolicyManagement;

import org.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class PolicyManagerService {
    
    private static final Logger logger = LoggerFactory.getLogger(PolicyManagerService.class);

    public String createPolicyGroup(String name, String desc,String contextKey){

        try{
            return PolicyManagement.createNewPolicyGroup(name, desc,contextKey);
        }
        catch(Exception e){

            logger.error("createPolicyGroupService", e);
            return "{\"Failed\"}";
        }
        
    }


    public String getPolicyGroupID(String groupname,String contextKey){

        try{
            return PolicyManagement.getPolicyGroupIDByName(groupname,contextKey);
        }
        catch(Exception e){

            logger.error("getPolicyaGroupIdService", e);
            return "{\"Failed\"}";
        }
        
    }

    public String createPolicyUnderGroup(String groupname, String policyname, String policydesc, String policytype, JSONObject policyJson,String contextKey){

        try{
            return PolicyManagement.createPolicyUnderGroup(groupname, policyname, policydesc, policytype, policyJson,contextKey);
        }
        catch(Exception e){

            logger.error("createPolicyUnderAGroupService", e);
            return "{\"Failed\"}";
        }
    }

   
    public String publishPolicy(String policyId, String policygroupId,String contextKey){

        try{
            return PolicyManagement.publishPolicy(policyId, policygroupId,contextKey);
        }
        catch(Exception e){

            logger.error("publishPolicyService", e);
            return "{\"Failed\"}";
        }
    }
}
