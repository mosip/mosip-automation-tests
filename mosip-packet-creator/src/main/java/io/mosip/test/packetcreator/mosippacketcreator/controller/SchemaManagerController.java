package io.mosip.test.packetcreator.mosippacketcreator.controller;

import java.util.Properties;

import org.mosip.dataprovider.util.DataProviderConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.test.packetcreator.mosippacketcreator.service.SchemaManagerService;
import io.swagger.annotations.Api;

@Api(value = "SchemaManagerController", description = "REST API for managing IDschemas")
@RestController
public class SchemaManagerController {
    
    @Value("${mosip.test.persona.configpath}")
    private String personaConfigPath;

    @Autowired
    SchemaManagerService schemaManagerService;

    private static final Logger logger = LoggerFactory.getLogger(SchemaManagerController.class);

    

    @PutMapping(value = "/schema/{id}")
    public @ResponseBody String modifySchema( @PathVariable("id") String id, @RequestParam(value = "version", defaultValue = "1") String version){


    
        try{
            if(personaConfigPath != null && !personaConfigPath.equals("")){
                DataProviderConstants.RESOURCE = personaConfigPath;
            }
            return schemaManagerService.modifySchema(1,id);
        }
        catch (Exception ex){
            logger.error("modifySchema", ex);
        }

        return "{\"Failed\"}";
    }

    @GetMapping(value = "/schema")
    public @ResponseBody String getSchema(){

        int i;
        String schema;
        try{
            if(personaConfigPath != null && !personaConfigPath.equals("")){
                DataProviderConstants.RESOURCE = personaConfigPath;
            }

            schema = schemaManagerService.getSchema();
            return schema;

        }
        catch(Exception e){
            logger.error("getSchema", e);
            return "{\"Failed\"}";
        }
    }

    

}
