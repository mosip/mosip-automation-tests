package io.mosip.test.packetcreator.mosippacketcreator.controller;



import java.util.List;

import org.mosip.dataprovider.util.DataProviderConstants;
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


import io.mosip.test.packetcreator.mosippacketcreator.dto.PreRegisterRequestDto;
import io.mosip.test.packetcreator.mosippacketcreator.service.PacketMakerService;
import io.mosip.test.packetcreator.mosippacketcreator.service.PacketSyncService;
import io.swagger.annotations.ApiOperation;

@RestController
public class PacketController {

	  private static final Logger logger = LoggerFactory.getLogger(TestDataController.class);
	  @Value("${mosip.test.persona.configpath}")
		private String personaConfigPath;
	   
	  @Autowired
	    PacketSyncService packetSyncService;

	  @Autowired
	  PacketMakerService packetMakerService;
	  /*
	   * Create a packet from Resident data for the target context
	   * requestDto may contain PersonaRequestType.PR_Options
	   */
	  @PostMapping(value = "/packet/create/")
	  public @ResponseBody String createPacket(@RequestBody PreRegisterRequestDto requestDto,
	    	@RequestParam(name="contextKey",required = false) String contextKey) {

			try{    	
	    		if(personaConfigPath !=null && !personaConfigPath.equals("")) {
	    			DataProviderConstants.RESOURCE = personaConfigPath;
	    		}

	    		return packetMakerService.createPacketFromTemplate(requestDto.getPersonaFilePath().get(0),  requestDto.getPersonaFilePath().get(1), contextKey,requestDto.getAdditionalInfoReqId());
	    		//return packetSyncService.createPackets(requestDto.,process,null, contextKey);
	    		
	    	
	    	} catch (Exception ex){
	             logger.error("createPackets", ex);
	    	}
	    	return "{\"Failed\"}";
	  }
	  @PostMapping(value = "/packet/pack/")
	  public @ResponseBody String packPacket(@RequestBody PreRegisterRequestDto requestDto,
	    	@RequestParam(name="contextKey",required = false) String contextKey
	    	//@RequestParam(name="isValidChecksum",required = false) Boolean isValidcs
			  ) {

			try{    	
	    		if(personaConfigPath !=null && !personaConfigPath.equals("")) {
	    			DataProviderConstants.RESOURCE = personaConfigPath;
	    		}
	    		boolean isValidChecksum = true;
	    		/*if(isValidcs != null)
	    			isValidChecksum = isValidcs;
	    		*/
	    		return packetMakerService.packPacketContainer(requestDto.getPersonaFilePath().get(0),null,null, contextKey,isValidChecksum);
	    		//return packetSyncService.createPackets(requestDto.,process,null, contextKey);
	    		
	    	
	    	} catch (Exception ex){
	             logger.error("createPackets", ex);
	    	}
	    	return "{\"Failed\"}";
	  }

	  @PostMapping(value = "/packet/template/{process}")
	  public @ResponseBody String createTemplate(@RequestBody PreRegisterRequestDto requestDto,
			@PathVariable("process") String process,
	    	@RequestParam(name="contextKey",required = false) String contextKey) {

			try{    	
	    		if(personaConfigPath !=null && !personaConfigPath.equals("")) {
	    			DataProviderConstants.RESOURCE = personaConfigPath;
	    		}

	    		return packetSyncService.createPacketTemplates(requestDto.getPersonaFilePath(),process,null,null, contextKey);
	    		
	    	
	    	} catch (Exception ex){
	             logger.error("createPackets", ex);
	    	}
	    	return "{\"Failed\"}";
	  }

	  @PostMapping(value = "/packet/bulkupload")
	  public @ResponseBody String bulkUploadPackets(@RequestBody List<String> packetPaths,
		    	@RequestParam(name="contextKey",required = false) String contextKey) {

		  try{    	
	    		if(personaConfigPath !=null && !personaConfigPath.equals("")) {
	    			DataProviderConstants.RESOURCE = personaConfigPath;
	    		}

	    		
	    		return packetSyncService.bulkuploadPackets(packetPaths, contextKey) ;
	    	
	    	} catch (Exception ex){
	             logger.error("createPackets", ex);
	    	}
	    	return "{\"Failed\"}";
	  
	  }
	 
	  @ApiOperation(value = "Validate Identity Object as per ID Schema", response = String.class)
		
	  @PostMapping(value = "/packet/validate/{process}")
	  public @ResponseBody String validatePacket(@RequestBody PreRegisterRequestDto requestDto,
			  @PathVariable("process") String process,
			  @RequestParam(name="contextKey",required = false) String contextKey) {

		  try{    	
	    		if(personaConfigPath !=null && !personaConfigPath.equals("")) {
	    			DataProviderConstants.RESOURCE = personaConfigPath;
	    		}

	    		
	    		return packetSyncService.validatePacket(requestDto.getPersonaFilePath().get(0), process, contextKey) ;
	    	
	    	} catch (Exception ex){
	             logger.error("validatePacket", ex);
	    	}
	    	return "{\"Failed\"}";

	  }
	  
}
