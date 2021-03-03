package io.mosip.test.packetcreator.mosippacketcreator.controller;



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

import io.mosip.test.packetcreator.mosippacketcreator.dto.PersonaRequestDto;
import io.mosip.test.packetcreator.mosippacketcreator.service.PacketSyncService;

@RestController
public class PacketController {

	  private static final Logger logger = LoggerFactory.getLogger(TestDataController.class);
	  @Value("${mosip.test.persona.configpath}")
		private String personaConfigPath;
	   
	  @Autowired
	    PacketSyncService packetSyncService;

	  /*
	   * Create a packet from Resident data for the target context
	   * requestDto may contain PersonaRequestType.PR_Options
	   */
	  @PostMapping(value = "/packet/create/{process}")
	  public @ResponseBody String createPacket(@RequestBody PersonaRequestDto requestDto,
			@PathVariable("process") String process,
	    	@RequestParam(name="contextKey",required = false) String contextKey) {

			try{    	
	    		if(personaConfigPath !=null && !personaConfigPath.equals("")) {
	    			DataProviderConstants.RESOURCE = personaConfigPath;
	    		}
	    	//	return packetSyncService.createPackets(requestDto.getPersonaFilePath(),process,null, contextKey);
	    	
	    	} catch (Exception ex){
	             logger.error("createPackets", ex);
	    	}
	    	return "{\"Failed\"}";
	  }
}
