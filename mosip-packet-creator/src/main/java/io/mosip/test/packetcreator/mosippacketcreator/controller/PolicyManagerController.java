package io.mosip.test.packetcreator.mosippacketcreator.controller;

import org.json.JSONObject;

import org.mosip.dataprovider.util.DataProviderConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.test.packetcreator.mosippacketcreator.dto.PolicyCreateDto;
import io.mosip.test.packetcreator.mosippacketcreator.service.PolicyManagerService;
import io.swagger.annotations.Api;

@Api(value="Policy Management Controller", description = "REST Apis for policy management")
@RestController
public class PolicyManagerController {

    @Value("${mosip.test.persona.configpath}")
    private String policyManagerConfigPath;


    @Autowired
    PolicyManagerService policyManagerService;

    private static final Logger logger = LoggerFactory.getLogger(PolicyManagerController.class);

    @PostMapping("policies/group/new")
    public @ResponseBody String createPolicyGroup(@RequestParam(value="name") String name, @RequestParam(value="desc") String desc){

        try {
            if(policyManagerConfigPath != null && !policyManagerConfigPath.equals("")){
                DataProviderConstants.RESOURCE = policyManagerConfigPath;
            }
            return policyManagerService.createPolicyGroup(name, desc);
        } catch (Exception e) {
            //TODO: handle exception
            logger.error("createPolicyGroup", e);
            return "{\"Failed\"}";
        }

    }

    @GetMapping("policies/group/")
    public @ResponseBody String getPolicyGroup(@RequestParam(value="grpname") String groupname){

        try {
            if(policyManagerConfigPath != null && !policyManagerConfigPath.equals("")){
                DataProviderConstants.RESOURCE = policyManagerConfigPath;
            }
            return policyManagerService.getPolicyGroupID(groupname);
        } catch (Exception e) {
            //TODO: handle exception
            logger.error("createPolicyGroup", e);
            return "{\"Failed\"}";
        }

    }


    @PostMapping("policies/")
    public @ResponseBody String createPolicyUnderGroup(@RequestBody PolicyCreateDto policyDetails){

        try {
            if(policyManagerConfigPath != null && !policyManagerConfigPath.equals("")){
                DataProviderConstants.RESOURCE = policyManagerConfigPath;
            }
            JSONObject policiesJson = new JSONObject();
            policiesJson.put("allowedAuthTypes", policyDetails.getPolicies().getAllowedAuthTypes());
            policiesJson.put("allowedKycAttributes", policyDetails.getPolicies().getAllowedKycAttributes());
            policiesJson.put("authTokenType", policyDetails.getPolicies().getAuthTokenType());
            return policyManagerService.createPolicyUnderGroup(policyDetails.getPolicyGroupName(), policyDetails.getName(), policyDetails.getDesc(), policyDetails.getPolicyType(), policiesJson);
        } catch (Exception e) {
            //TODO: handle exception
            logger.error("createPolicyUnderGroup", e);
            return "{\"Failed\"}";
        }

    }

    @PostMapping("/policies/{policyId}/group/{policygroupId}/publish")
    public @ResponseBody String publishPolicy(@PathVariable("policyId") String policyId, @PathVariable("policygroupId") String policygroupId){

        try {
            if(policyManagerConfigPath != null && !policyManagerConfigPath.equals("")){
                DataProviderConstants.RESOURCE = policyManagerConfigPath;
            }
            return policyManagerService.publishPolicy(policyId, policygroupId);
        } catch (Exception e) {
            //TODO: handle exception
            logger.error("createPolicyUnderGroup", e);
            return "{\"Failed\"}";
        }

    }



    
}
