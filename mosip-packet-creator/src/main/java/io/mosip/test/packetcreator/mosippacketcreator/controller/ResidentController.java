package io.mosip.test.packetcreator.mosippacketcreator.controller;


import org.mosip.dataprovider.test.registrationclient.RegistrationSteps;
import org.mosip.dataprovider.util.DataProviderConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.test.packetcreator.mosippacketcreator.service.APIRequestUtil;
import io.mosip.test.packetcreator.mosippacketcreator.service.PreregSyncService;
import io.mosip.test.packetcreator.mosippacketcreator.service.ResidentService;
import variables.VariableManager;



@RestController
public class ResidentController {

	  private static final Logger logger = LoggerFactory.getLogger(ResidentController.class);


	  @Autowired
	  ResidentService residentService;

	  @Value("${mosip.test.persona.configpath}")
		private String personaConfigPath;
	    

	  @GetMapping(value = "/resident/status/{rid}")
	  public @ResponseBody String getRIDStatus( @PathVariable("rid") String rid) {

		if(personaConfigPath !=null && !personaConfigPath.equals("")) {
  			DataProviderConstants.RESOURCE = personaConfigPath;
  		}
		  try {
			return residentService.getRIDStatus(rid);
		} catch (Exception e) {
			   logger.error("getRIDStatus", e);
		}
		return "{Failed}";
	  }
	
	  @GetMapping(value = "/resident/uin/{rid}")
	  public @ResponseBody String getUINByRid( @PathVariable("rid") String rid) {

		  if(personaConfigPath !=null && !personaConfigPath.equals("")) {
	  			DataProviderConstants.RESOURCE = personaConfigPath;
	  		}

 
		  try {
			return residentService.getUINByRID(rid);
		  }catch (Exception e) {
			   logger.error("getUINByRid", e);
		  }
		  return "{Failed}";
	  }
	
}
