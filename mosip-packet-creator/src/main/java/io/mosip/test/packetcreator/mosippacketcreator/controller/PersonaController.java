package io.mosip.test.packetcreator.mosippacketcreator.controller;

import java.util.List;

import org.mosip.dataprovider.util.DataProviderConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.test.packetcreator.mosippacketcreator.dto.PersonaRequestDto;
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
	 @Value("${mosip.test.persona.Angulipath}")
		private String personaAnguliPath;
	   
	private static final Logger logger = LoggerFactory.getLogger(PersonaController.class);


	@ApiOperation(value = "Update given persona record with the given list of attribute values", response = String.class)
	@PutMapping(value = "/persona/{id}")
	public @ResponseBody String updatePersonaData(@RequestBody List<UpdatePersonaDto> personaRequestDto , @PathVariable("id") String id) {
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
	@ApiOperation(value = "Create persona record as per the given specification", response = String.class)
	@PostMapping(value = "/persona/{count}")
	public @ResponseBody String generateResidentData(@RequestBody PersonaRequestDto residentRequestDto,
	    		@PathVariable("count") int count,
	    		@RequestParam(name="contextKey",required = false) String contextKey) {

	    	try{
	    		logger.info("Persona Config Path="+ personaConfigPath );
	    		if(personaConfigPath !=null && !personaConfigPath.equals("")) {
	    			DataProviderConstants.RESOURCE = personaConfigPath;
	    		}
	    		if(personaAnguliPath !=null && !personaAnguliPath.equals("")) {
	    			DataProviderConstants.ANGULI_PATH = personaAnguliPath;
	    		}
	    		logger.info("personaAnguliPath ="+ DataProviderConstants.ANGULI_PATH );
	    		
	    		logger.info("Resource Path="+ DataProviderConstants.RESOURCE );
	    		logger.info("DOC_Template Path="+ DataProviderConstants.RESOURCE+DataProviderConstants.DOC_TEMPLATE_PATH);
	    		
	    		//clear all tokens
	    //		VariableManager.setVariableValue("urlSwitched", "true");
	    		
	    		return packetSyncService.generateResidentData(count,residentRequestDto, contextKey).toString();
	    	
	    	} catch (Exception ex){
	             logger.error("generateResidentData", ex);
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
