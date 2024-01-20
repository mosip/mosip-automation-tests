package io.mosip.testrig.dslrig.packetcreator.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.testrig.dslrig.dataprovider.util.DataProviderConstants;
import io.mosip.testrig.dslrig.packetcreator.dto.SelfRegisterDto;
import io.mosip.testrig.dslrig.packetcreator.service.PartnerService;
import io.swagger.annotations.Api;


@Api(value = "PartnerManagerController", description = "REST API for partner controls")
@RestController
public class PartnerManagerController {
    

    @Autowired
    PartnerService partnerService;

    private static final Logger logger = LoggerFactory.getLogger(PartnerManagerController.class);

    @PostMapping(value = "/partners/{contextKey}")
    public @ResponseBody String selfRegisterPartner(
        @RequestBody SelfRegisterDto selfRegister,
        @PathVariable("contextKey") String contextKey
    ){
        try {

            return partnerService.selfRegister(selfRegister,contextKey);
        } catch (Exception e) {
            logger.error("selfRegisterPartner", e);
            return "{\"falied\"}";
        }
    }

    @PostMapping(value = "/partners/{partnerId}/{contextKey}")
    public @ResponseBody String updatePartnerStatus(
        @PathVariable(value = "partnerId") String partnerId,
        @RequestParam(value = "status") String status,
        @PathVariable("contextKey") String contextKey
    ){
        try {

            return partnerService.updatePartnerStatus(contextKey,partnerId, status);
        } catch (Exception e) {
            logger.error("updatePartnerStatus", e);
            return "{\"falied\"}";
        }
    }

    @PostMapping(value = "/partner/APIRequest/submit/{contextKey}")
    public @ResponseBody String submitAPIRequest(
        @RequestParam(value = "PartnerID") String partnerID,
        @RequestParam(value = "PolicyName")String policyName,
        @RequestParam(value = "useCaseDescription") String useCaseDesc,
        @PathVariable("contextKey") String contextKey
    ){
        try {

            return partnerService.submitPartnerAPIKeyRequest(contextKey,partnerID, policyName, useCaseDesc);
        } catch (Exception e) {
            logger.error("submitApiRequest", e);
            return "{\"falied\"}";
        }
    }

    @PostMapping(value = "/partner/APIRequest/approve/{contextKey}")
    public @ResponseBody String approveAPIRequest(@RequestParam(value = "APIReqeustID") String ID,
    		 @PathVariable("contextKey") String contextKey
    		){
        
        try {

            return partnerService.approvePartnerAPIKeyRequest(contextKey,ID);
        } catch (Exception e) {
            logger.error("approveAPIRequest", e);
            return "{\"falied\"}";
        }
    }
    

}
