package io.mosip.test.packetcreator.mosippacketcreator.service;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ContextUtils {

	 @Value("${mosip.test.persona.configpath}")
	    private String personaConfigPath;
	   
	  Logger logger = LoggerFactory.getLogger(ContextUtils.class);
	   public Properties loadServerContext(String ctxName) {
	    	String filePath =  personaConfigPath + "/server.context."+  ctxName + ".properties";
	    	Properties p=new Properties();
	        
	    	FileReader reader;
			try {
				reader = new FileReader(filePath);
				p.load(reader);
			} catch (IOException e) {
				
				logger.error("loadServerContext " + e.getMessage());
			}  
		
			return p;
	    }
	    public Boolean  createUpdateServerContext(Properties props, String ctxName) {
	    	
	    	Boolean bRet = true;
	    	
	    	String filePath =  personaConfigPath + "/server.context."+  ctxName + ".properties";
	    	
	    	Properties p=new Properties();
	    	Properties mergedProperties = new Properties();
	    	try {
	    		FileReader reader=new FileReader(filePath);  
	    		p.load(reader);
	    		
	    		reader.close();
	    		
	    	}catch (IOException e) {
				
	    		logger.error("read:createUpdateServerContext " + e.getMessage());
	    		bRet = false;
			}
	    	
	    	try {
	    		mergedProperties.putAll(p);
	    		mergedProperties.putAll(props);
	    		
	    		mergedProperties.store(new FileWriter(filePath),"Server Context Attributes");  
	    		bRet = true;
	    	}catch (IOException e) {
	    		logger.error("write:createUpdateServerContext " + e.getMessage());
	    		bRet = false;
			}
	    	return bRet;
	    }
}
