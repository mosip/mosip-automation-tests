package io.mosip.test.packetcreator.mosippacketcreator.controller;

import java.util.Properties;

import org.mosip.dataprovider.util.DataProviderConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.test.packetcreator.mosippacketcreator.service.ContextUtils;
import variables.VariableManager;

@RestController
public class ContextController {

	@Autowired
    ContextUtils contextUtils;
	@Value("${mosip.test.persona.configpath}")
    private String personaConfigPath;
	private static final Logger logger = LoggerFactory.getLogger(TestDataController.class);
	 
	  @PostMapping(value = "/context/server/{contextKey}")
	    public @ResponseBody String createServerContext(
	    		@RequestBody Properties contextProperties,
	    		@PathVariable("contextKey") String contextKey) {
	    	Boolean bRet = false;
	    	try{
	    		if (personaConfigPath != null && !personaConfigPath.equals(""))
                    DataProviderConstants.RESOURCE = personaConfigPath;
                VariableManager.Init();

 
/**
                String generatePrivateKey = contextProperties.getProperty("generatePrivateKey");
                boolean isRequired = Boolean.parseBoolean(generatePrivateKey);
                if (isRequired)
                    contextUtils.generateKeyAndUpdateMachineDetail(contextProperties, contextKey);
 **/
	    		bRet = contextUtils.createUpdateServerContext(contextProperties, contextKey);
	    	 } catch (Exception ex){
	              logger.error("createServerContext", ex);
	         }
	    	return bRet.toString();
	    }
	    @GetMapping(value = "/context/server/{contextKey}")
	    public @ResponseBody Properties getServerContext( @PathVariable("contextKey") String contextKey) {
	    	Properties bRet = null;
	    	try{
	    		bRet = contextUtils.loadServerContext( contextKey);
	    	 } catch (Exception ex){
	              logger.error("createServerContext", ex);
	         }
	    	return bRet;
	    }
}
