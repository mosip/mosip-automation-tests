package io.mosip.test.packetcreator.mosippacketcreator.service;

import org.springframework.stereotype.Component;
import org.mosip.dataprovider.preparation.PolicyManagement;

import org.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class PolicyManagerService {
    
    private static final Logger logger = LoggerFactory.getLogger(PolicyManagerService.class);

    public String createPolicyGroup(String name, String desc){

        try{
            return PolicyManagement.createNewPolicyGroup(name, desc);
        }
        catch(Exception e){

            logger.error("createPolicyGroupService", e);
            return "{\"Failed\"}";
        }
        
    }


    public String getPolicyGroupID(String groupname){

        try{
            return PolicyManagement.getPolicyGroupIDByName(groupname);
        }
        catch(Exception e){

            logger.error("getPolicyaGroupIdService", e);
            return "{\"Failed\"}";
        }
        
    }

    public String createPolicyUnderGroup(String groupname, String policyname, String policydesc, String policytype, JSONObject policyJson){

        try{
            return PolicyManagement.createPolicyUnderGroup(groupname, policyname, policydesc, policytype, policyJson);
        }
        catch(Exception e){

            logger.error("createPolicyUnderAGroupService", e);
            return "{\"Failed\"}";
        }
    }

   
    public String publishPolicy(String policyId, String policygroupId){

        try{
            return PolicyManagement.publishPolicy(policyId, policygroupId);
        }
        catch(Exception e){

            logger.error("publishPolicyService", e);
            return "{\"Failed\"}";
        }
    }
}
