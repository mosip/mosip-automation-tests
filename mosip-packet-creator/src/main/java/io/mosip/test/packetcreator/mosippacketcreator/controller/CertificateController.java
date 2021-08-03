package io.mosip.test.packetcreator.mosippacketcreator.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.mosip.test.packetcreator.mosippacketcreator.service.CertificateService;
import io.swagger.annotations.Api;

import org.mosip.dataprovider.util.DataProviderConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Api(value = "CertificateController", description = "REST API for uploading certificates")
@RestController
public class CertificateController {
    
    @Value("${mosip.test.persona.configpath}")
    private String personaConfigPath;

    @Autowired
    CertificateService certificateService;

    private static final Logger logger = LoggerFactory.getLogger(CertificateController.class);

    // @PostMapping(value = "/certificate/CA")
    // public @ResponseBody String uploadCACertificate(@RequestParam(value = "certificate", defaultValue = "root_tiger_ca.cer") String certificateFile){
        
    //     try{
    //         if(personaConfigPath != null && !personaConfigPath.equals("")){
    //             DataProviderConstants.RESOURCE = personaConfigPath;
    //         }
    //         return certificateService.uploadStoredCACertificate(certificateFile);
    //     }
    //     catch (Exception ex){
    //         logger.error("uploadCACertificate", ex);
    //     }

    //     return "failed";
    // }

    // @PostMapping(value = "/certificate/partner")
    // public @ResponseBody String uploadPartnerCertificate(
    //     @RequestParam("certificate") MultipartFile certificateFile, 
    //     @RequestParam(value="name", defaultValue="ABC Bank") String orgName, 
    //     @RequestParam(value = "id", defaultValue = "9876") String partnerID
    // ){

    //     try{
    //         if(personaConfigPath != null && !personaConfigPath.equals("")){
    //             DataProviderConstants.RESOURCE = personaConfigPath;
    //         }

    //         return certificateService.uploadPartnerCertificate(certificateFile, orgName, partnerID);
    //     }
    //     catch (Exception ex){
    //         logger.error("uploadPartnerCertificate", ex);
    //     }

    //     return "failed";
    // }

    @PutMapping(value = "/certificate/generate/root")
    public @ResponseBody String generateAndUploadRootCertificate(
        @RequestParam(value = "issuer", defaultValue = "C=AU, O=The Legion of the Bouncy Castle, OU=Bouncy Primary Certificate") String issuer,
        @RequestParam(value = "alias", defaultValue = "Root CA") String alias,
        @RequestParam(value = "validYears", defaultValue = "3") int validYears
    ){

        try{
            if(personaConfigPath != null && !personaConfigPath.equals("")){
                DataProviderConstants.RESOURCE = personaConfigPath;
            }
            return certificateService.generateAndUploadRootCertificate(issuer, alias, validYears);
        }
        catch (Exception ex){
            logger.error("generateAndUploadCACertificate", ex);
        }

        return "failed";

    }

    @PutMapping(value = "/certificate/generate/int")
    public @ResponseBody String generateAndUploadIntCertificate(
        @RequestParam(value = "issuer", defaultValue = "C=IN, O=EFG Company, OU=Certificate, E=abc@efg.com") String issuer,
        @RequestParam(value = "alias", defaultValue = "Int CA") String alias,
        @RequestParam(value = "validYears", defaultValue = "3") int validYears,
        @RequestParam(value = "RootAlias", defaultValue = "Root CA") String rootAlias
    ){

        try{
            if(personaConfigPath != null && !personaConfigPath.equals("")){
                DataProviderConstants.RESOURCE = personaConfigPath;
            }
            return certificateService.generateAndUploadIntCertificate(issuer, alias, validYears, rootAlias);
        }
        catch (Exception ex){
            logger.error("generateAndUploadCACertificate", ex);
        }

        return "failed";

    }

    @PutMapping(value = "/certificate/generate/partner")
    public @ResponseBody String generateAndUploadPartnerCertificate(
        @RequestParam(value = "issuer", defaultValue = "C=IN, O=ABC Bank,L=Bangalore,CN=ABC Bank, OU=Account Opening,E=bank@efg.com") String issuer,
        @RequestParam(value = "alias", defaultValue = "ABC Bank") String alias,
        @RequestParam(value = "validYears", defaultValue = "3") int validYears,
        @RequestParam(value = "RootAlias", defaultValue = "Int CA") String rootAlias,
        @RequestParam(value = "PartnerID", defaultValue = "9876") String partnerID
    ){

        try{
            if(personaConfigPath != null && !personaConfigPath.equals("")){
                DataProviderConstants.RESOURCE = personaConfigPath;
            }
            return certificateService.generateAndUploadPartnerCertificate(issuer, alias, validYears, rootAlias, partnerID);
        }
        catch (Exception ex){
            logger.error("generateAndUploadCACertificate", ex);
        }

        return "failed";

    }

    
    
}
