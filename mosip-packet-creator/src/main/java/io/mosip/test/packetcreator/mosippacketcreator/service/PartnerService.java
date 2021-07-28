package io.mosip.test.packetcreator.mosippacketcreator.service;

import org.mosip.dataprovider.test.partnerManagement.PartnerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PartnerService {
    
    private static Logger logger = LoggerFactory.getLogger(PartnerService.class);
    
    public String submitPartnerAPIKeyRequest(String partnerID, String policyName, String useCaseDesc){
        return PartnerRequest.submitAPIKeyRequest(partnerID, policyName, useCaseDesc);
    }

    public String approvePartnerAPIKeyRequest(String apiKeyRequestID){
        return PartnerRequest.approvePartnerAPIKeyRequest(apiKeyRequestID);
    }

}
