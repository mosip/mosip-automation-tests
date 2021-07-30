package io.mosip.test.packetcreator.mosippacketcreator.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import io.mosip.test.packetcreator.mosippacketcreator.dto.SelfRegisterDto;

import io.mosip.test.packetcreator.mosippacketcreator.service.PartnerService;
import io.swagger.annotations.Api;

import org.mosip.dataprovider.util.DataProviderConstants;


@Api(value = "PartnerManagerController", description = "REST API for partner controls")
@RestController
public class PartnerManagerController {
    
    @Value("${mosip.test.partnerManager.configpath}")
    private String partnerConfigPath;

    @Autowired
    PartnerService partnerService;

    private static final Logger logger = LoggerFactory.getLogger(PartnerManagerController.class);

    @PostMapping(value = "/partners")
    public @ResponseBody String selfRegisterPartner(
        @RequestBody SelfRegisterDto selfRegister
    ){
        try {
            if(partnerConfigPath != null && !partnerConfigPath.equals("")){
                DataProviderConstants.RESOURCE = partnerConfigPath;
            }
            return partnerService.selfRegister(selfRegister);
        } catch (Exception e) {
            logger.error("selfRegisterPartner", e);
            return "{\"falied\"}";
        }
    }

    @PostMapping(value = "/partners/{partnerId}")
    public @ResponseBody String updatePartnerStatus(
        @PathVariable(value = "partnerId") String partnerId,
        @RequestParam(value = "status") String status
    ){
        try {
            if(partnerConfigPath != null && !partnerConfigPath.equals("")){
                DataProviderConstants.RESOURCE = partnerConfigPath;
            }
            return partnerService.updatePartnerStatus(partnerId, status);
        } catch (Exception e) {
            logger.error("updatePartnerStatus", e);
            return "{\"falied\"}";
        }
    }

    @PostMapping(value = "/partner/APIRequest/submit")
    public @ResponseBody String submitAPIRequest(
        @RequestParam(value = "PartnerID") String partnerID,
        @RequestParam(value = "PolicyName")String policyName,
        @RequestParam(value = "useCaseDescription") String useCaseDesc
    ){
        try {
            if(partnerConfigPath != null && !partnerConfigPath.equals("")){
                DataProviderConstants.RESOURCE = partnerConfigPath;
            }
            return partnerService.submitPartnerAPIKeyRequest(partnerID, policyName, useCaseDesc);
        } catch (Exception e) {
            logger.error("submitApiRequest", e);
            return "{\"falied\"}";
        }
    }

    @PostMapping(value = "/partner/APIRequest/approve")
    public @ResponseBody String approveAPIRequest(@RequestParam(value = "APIReqeustID") String ID){
        
        try {
            if(partnerConfigPath != null && !partnerConfigPath.equals("")){
                DataProviderConstants.RESOURCE = partnerConfigPath;
            }
            return partnerService.approvePartnerAPIKeyRequest(ID);
        } catch (Exception e) {
            logger.error("approveAPIRequest", e);
            return "{\"falied\"}";
        }
    }
    

}
