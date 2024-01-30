package io.mosip.testrig.dslrig.packetcreator.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.testrig.dslrig.dataprovider.util.DataProviderConstants;
import io.mosip.testrig.dslrig.dataprovider.variables.VariableManager;
import io.mosip.testrig.dslrig.packetcreator.service.CertificateService;
import io.swagger.annotations.Api;

@Api(value = "CertificateController", description = "REST API for uploading certificates")
@RestController
public class CertificateController {

    @Autowired
    CertificateService certificateService;

    private static final Logger logger = LoggerFactory.getLogger(CertificateController.class);

    @PutMapping(value = "/certificate/generate/root/{contextKey}")
    public @ResponseBody String generateAndUploadRootCertificate(
        @RequestParam(value = "issuer", defaultValue = "C=AU, O=The Legion of the Bouncy Castle, OU=Bouncy Primary Certificate") String issuer,
        @RequestParam(value = "alias", defaultValue = "Root CA") String alias,
        @RequestParam(value = "validYears", defaultValue = "3") int validYears,
        @PathVariable("contextKey") String contextKey) 
    {

        try{
        
            return certificateService.generateAndUploadRootCertificate(issuer, alias, validYears,contextKey);
        }
        catch (Exception ex){
            logger.error("generateAndUploadCACertificate", ex);
        }

        return "failed";

    }

    @PutMapping(value = "/certificate/generate/int/{contextKey}")
    public @ResponseBody String generateAndUploadIntCertificate(
        @RequestParam(value = "issuer", defaultValue = "C=IN, O=EFG Company, OU=Certificate, E=abc@efg.com") String issuer,
        @RequestParam(value = "alias", defaultValue = "Int CA") String alias,
        @RequestParam(value = "validYears", defaultValue = "3") int validYears,
        @RequestParam(value = "RootAlias", defaultValue = "Root CA") String rootAlias,
        @PathVariable("contextKey") String contextKey 
    ){

        try{
           
            return certificateService.generateAndUploadIntCertificate(issuer, alias, validYears, rootAlias,contextKey);
        }
        catch (Exception ex){
            logger.error("generateAndUploadCACertificate", ex);
        }

        return "failed";

    }

    @PutMapping(value = "/certificate/generate/partner/{contextKey}")
    public @ResponseBody String generateAndUploadPartnerCertificate(
        @RequestParam(value = "issuer", defaultValue = "C=IN, O=ABC Bank,L=Bangalore,CN=ABC Bank, OU=Account Opening,E=bank@efg.com") String issuer,
        @RequestParam(value = "alias", defaultValue = "ABC Bank") String alias,
        @RequestParam(value = "validYears", defaultValue = "3") int validYears,
        @RequestParam(value = "RootAlias", defaultValue = "Int CA") String rootAlias,
        @RequestParam(value = "PartnerID", defaultValue = "9876") String partnerID,
        @PathVariable("contextKey") String contextKey 
    ){

        try{
           
            return certificateService.generateAndUploadPartnerCertificate(issuer, alias, validYears, rootAlias, partnerID,contextKey);
        }
        catch (Exception ex){
            logger.error("generateAndUploadCACertificate", ex);
        }

        return "failed";

    }

}
