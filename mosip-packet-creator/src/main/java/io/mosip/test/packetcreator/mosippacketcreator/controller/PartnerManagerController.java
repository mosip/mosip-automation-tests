package io.mosip.test.packetcreator.mosippacketcreator.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import io.mosip.test.packetcreator.mosippacketcreator.service.PartnerService;
import io.swagger.annotations.Api;

import org.mosip.dataprovider.util.DataProviderConstants;


@Api(value = "PartnerManagerController", description = "REST API for partner controls")
@RestController
public class PartnerManagerController {
    
    @Value("${mosip.test.persona.configpath}")
    private String personaConfigPath;

    @Autowired
    PartnerService partnerService;

    private static final Logger logger = LoggerFactory.getLogger(PartnerManagerController.class);

    @PostMapping(value = "/partner/APIRequest/submit")
    public @ResponseBody String submitAPIRequest(
        @RequestParam(value = "PartnerID") String partnerID,
        @RequestParam(value = "PolicyName")String policyName,
        @RequestParam(value = "useCaseDescription") String useCaseDesc
    ){
        if(personaConfigPath != null && !personaConfigPath.equals("")){
            DataProviderConstants.RESOURCE = personaConfigPath;
        }
        return partnerService.submitPartnerAPIKeyRequest(partnerID, policyName, useCaseDesc);
    }

    @PostMapping(value = "/partner/APIRequest/approve")
    public @ResponseBody String approveAPIRequest(@RequestParam(value = "APIReqeustID") String ID){
        
        if(personaConfigPath != null && !personaConfigPath.equals("")){
            DataProviderConstants.RESOURCE = personaConfigPath;
        }
        return partnerService.approvePartnerAPIKeyRequest(ID);
    }
    

}
