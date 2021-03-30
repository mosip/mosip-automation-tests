package io.mosip.test.packetcreator.mosippacketcreator.service;

import org.mosip.dataprovider.test.registrationclient.RegistrationSteps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.mosip.test.packetcreator.mosippacketcreator.controller.ResidentController;
import variables.VariableManager;


@Component
public class ResidentService {

	  
	  
	  private static final Logger logger = LoggerFactory.getLogger(ResidentController.class);
		
	  
	  public String getRIDStatus(String rid) {
		  VariableManager.Init();
		  RegistrationSteps steps = new RegistrationSteps();
		  try {
			  return steps.getRIDStatus(rid);
		  } catch (Exception e) {
			  logger.error("getRIDStatus", e);
		  }
		  return "{Failed}";
	  }
	  public String getUINByRID(String rid) {
		  VariableManager.Init();
		  RegistrationSteps steps = new RegistrationSteps();
		  try {
			  return steps.getUINByRID(rid);
		  } catch (Exception e) {
			  logger.error("getUINByRID", e);
		  }
		  return "{Failed}";
	  }
}
