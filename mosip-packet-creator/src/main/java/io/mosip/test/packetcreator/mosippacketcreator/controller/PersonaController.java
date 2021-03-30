package io.mosip.test.packetcreator.mosippacketcreator.controller;

import java.util.List;

import org.mosip.dataprovider.util.DataProviderConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;


import org.springframework.web.bind.annotation.RequestBody;

import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;


import io.mosip.test.packetcreator.mosippacketcreator.dto.UpdatePersonaDto;
import io.mosip.test.packetcreator.mosippacketcreator.service.PacketSyncService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "PersonaController", description = "REST APIs for Persona management")
@RestController
public class PersonaController {

	@Value("${mosip.test.persona.configpath}")
	private String personaConfigPath;
	@Autowired
	PacketSyncService packetSyncService;

	private static final Logger logger = LoggerFactory.getLogger(PersonaController.class);


	
	@ApiOperation(value = "Update given persona record with the given list of attribute values", response = String.class)
	@PostMapping(value = "/persona/")
	public @ResponseBody String updatePersonaData(@RequestBody List<UpdatePersonaDto> personaRequestDto ) {
	    	try{    	
	    		if(personaConfigPath !=null && !personaConfigPath.equals("")) {
	    			DataProviderConstants.RESOURCE = personaConfigPath;
	    		}
	    		return packetSyncService.updatePersonaData(personaRequestDto);
	    	
	    	} catch (Exception ex){
	             logger.error("updatePersonaData", ex);
	    	}
	    	return "{Failed}";
	    	
	 }
	@ApiOperation(value = "Return from the given persona record , list of specified attribute values", response = String.class)
	@GetMapping(value = "/persona/")
	public @ResponseBody String getPersonaData(@RequestBody List<UpdatePersonaDto> personaRequestDto ) {
	    	try{    	
	    		if(personaConfigPath !=null && !personaConfigPath.equals("")) {
	    			DataProviderConstants.RESOURCE = personaConfigPath;
	    		}
	    		return packetSyncService.getPersonaData(personaRequestDto);
	    	
	    	} catch (Exception ex){
	             logger.error("updatePersonaData", ex);
	    	}
	    	return "{Failed}";
	    	
	 }
}
