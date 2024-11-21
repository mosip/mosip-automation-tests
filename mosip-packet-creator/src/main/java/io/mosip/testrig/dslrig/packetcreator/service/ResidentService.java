package io.mosip.testrig.dslrig.packetcreator.service;


import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.mosip.testrig.dslrig.dataprovider.models.ResidentModel;
import io.mosip.testrig.dslrig.dataprovider.test.registrationclient.RegistrationSteps;
import io.mosip.testrig.dslrig.dataprovider.variables.VariableManager;
import io.mosip.testrig.dslrig.packetcreator.controller.ResidentController;
import io.restassured.response.Response;


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
			ResidentModel resident = ResidentModel.readPersona(personaPath,context);
			RegistrationSteps steps = new RegistrationSteps();
			String resp = steps.downloadCard(resident, uin,context);
			  
			return resp;
	  }
	  public String getRIDStatus(String rid, String context) {
//		  VariableManager.Init(context); 
		  loadServerContextProperties(context);
		 
		  RegistrationSteps steps = new RegistrationSteps();
		  try {
			  return steps.getRIDStatus(rid,context);
		  } catch (Exception e) {
			  logger.error("getRIDStatus", e);
		  }
		  return "{Failed}";
	  }
	  public String getUINByRID(String rid, String context) throws Exception {
		  
//		  VariableManager.Init(context);
		  loadServerContextProperties(context);
		  RegistrationSteps steps = new RegistrationSteps();
		//  try {
			 return steps.getUINByRID(rid,context);
		 // } catch (Exception e) {
		//	  logger.error("getUINByRID", e);
		  //}
		  //return "{Failed}";
	  }
	  
  public Response getStagesByRID(String rid, String context) throws Exception {
		  
//		  VariableManager.Init(context);
		  loadServerContextProperties(context);
		  RegistrationSteps steps = new RegistrationSteps();
		//  try {
			 return steps.getStagesByRID(rid,context);
		 // } catch (Exception e) {
		//	  logger.error("getUINByRID", e);
		  //}
		  //return "{Failed}";
	  }
}
