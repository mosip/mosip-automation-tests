package io.mosip.test.packetcreator.mosippacketcreator.service;


import java.util.Properties;

import org.mosip.dataprovider.models.ResidentModel;
import org.mosip.dataprovider.test.registrationclient.RegistrationSteps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;

import io.mosip.test.packetcreator.mosippacketcreator.controller.ResidentController;
import variables.VariableManager;


@Component
public class ResidentService {

	 @Autowired
	    private ContextUtils contextUtils;
	    
	  
	  private static final Logger logger = LoggerFactory.getLogger(ResidentController.class);

	  void loadServerContextProperties(String contextKey) {
	    	
	    	if(contextKey != null && !contextKey.equals("")) {
	    		
	    		Properties props = contextUtils.loadServerContext(contextKey);
	    		props.forEach((k,v)->{
	    			String key = k.toString().trim();
	    			String ns = VariableManager.NS_DEFAULT;
	    			
	    			if(!key.startsWith("mosip.test")) {
	    	
						
	    				VariableManager.setVariableValue(ns,key, v);
	    			}
	    			
	    		});
	    	}
	    }


	  public String downloadCard(String personaPath, String uin, String context) throws Exception{
		  
			loadServerContextProperties(context);
			ResidentModel resident = ResidentModel.readPersona(personaPath);
			RegistrationSteps steps = new RegistrationSteps();
			String resp = steps.downloadCard(resident, uin);
			  
			return resp;
	  }
	  public String getRIDStatus(String rid, String context) {
		  VariableManager.Init(); 
		  loadServerContextProperties(context);
		 
		  RegistrationSteps steps = new RegistrationSteps();
		  try {
			  return steps.getRIDStatus(rid);
		  } catch (Exception e) {
			  logger.error("getRIDStatus", e);
		  }
		  return "{Failed}";
	  }
	  public String getUINByRID(String rid, String context) throws Exception {
		  
		  VariableManager.Init();
		  loadServerContextProperties(context);
		  RegistrationSteps steps = new RegistrationSteps();
		//  try {
			 return steps.getUINByRID(rid);
		 // } catch (Exception e) {
		//	  logger.error("getUINByRID", e);
		  //}
		  //return "{Failed}";
	  }
}
