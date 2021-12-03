package io.mosip.test.packetcreator.mosippacketcreator.service;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FileOutputStream;
import java.util.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;
import java.util.UUID;
import java.security.KeyPairGenerator;

import org.apache.commons.lang.RandomStringUtils;
import org.mosip.dataprovider.models.ExecContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import variables.VariableManager;

import java.security.SecureRandom;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import org.mosip.dataprovider.models.setup.MosipMachineModel;
import org.mosip.dataprovider.preparation.MosipDataSetup;
@Component
public class ContextUtils {

	private String machineId=null;

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
	    	
	    	Properties pp = loadServerContext(ctxName);
	    	pp.forEach( (k,v)->{
	    		VariableManager.setVariableValue(k.toString(), v.toString());
	    	});
	    	String generatePrivateKey = props.getProperty("generatePrivateKey");
            boolean isRequired = Boolean.parseBoolean(generatePrivateKey);
            if (isRequired)
                generateKeyAndUpdateMachineDetail(props, ctxName);
            
	    	return bRet;
	    }
	    public String createExecutionContext(String serverContextKey) {
	    	
	    	String uid = UUID.randomUUID().toString();
	    	ExecContext context = new ExecContext();
	    	context.setKey(uid);
	    	Properties  p =loadServerContext(serverContextKey);
	    	context.setProperties(p);
	    	//Hashtable tbl = null;
	    	return uid;
	    }
	    public static String  ProcessFromTemplate(String src, String templatePacketLocation) {
	    	String process = null;
	    	if(templatePacketLocation == null)
	    		return process;
	    	Path fPath = Path.of(templatePacketLocation +"/" + src.toUpperCase());
	    	for(File f: fPath.toFile().listFiles()) {
	    		//logger.info("subfolder "+ f.getName());
	    		if(f.isDirectory()) {
	    			process = f.getName();
	    			break;
	    		}
	    			
	    	}
	    	return process;
	    }
	    public static Path idJsonPathFromTemplate(String src, String templatePacketLocation) {
	    	Path fPath = Path.of(templatePacketLocation +"/" + src.toUpperCase());
	    	String process = null;
	    	
	    	for(File f: fPath.toFile().listFiles()) {
	    		//logger.info("subfolder "+ f.getName());
	    		if(f.isDirectory()) {
	    			process = f.getName();
	    			break;
	    		}
	    			
	    	}    	
	    	if(process != null) {
	    		fPath = Path.of(templatePacketLocation +"/" + src.toUpperCase() + "/" + process + "/rid_id/ID.json");
	    		return fPath;
	    	}
	    	return null;
	    }


		public void generateKeyAndUpdateMachineDetail(Properties contextProperties,String contextKey) {
			KeyPairGenerator keyGenerator = null;
			boolean isMachineDetailFound=false;
			machineId = contextProperties.getProperty("mosip.test.regclient.machineid");
			if (machineId == null || machineId.isEmpty())
				throw new RuntimeException("MachineId is null or empty!");
			
			try {
				keyGenerator = KeyPairGenerator.getInstance("RSA");
				keyGenerator.initialize(2048, new SecureRandom());
				final KeyPair keypair = keyGenerator.generateKeyPair();
				
				createKeyFile(String.valueOf(personaConfigPath) + File.separator + "privatekeys"+ File.separator+contextKey+ "." + machineId + ".reg.key", keypair.getPrivate().getEncoded());
				
				final String publicKey = java.util.Base64.getEncoder().encodeToString(keypair.getPublic().getEncoded());
				System.out.println("publicKey: "+publicKey);
				if (publicKey != null && !publicKey.isEmpty()) {
					List<MosipMachineModel> machines =null;
					String status = contextProperties.getProperty("machineStatus");
					if(status != null && status.equalsIgnoreCase("deactive"))
						machines =MosipDataSetup.searchMachineDetail(machineId, "eng");
					else
					 machines = MosipDataSetup.getMachineDetail(machineId, " ");
					if (machines != null && !machines.isEmpty()) {
						for(MosipMachineModel mosipMachineModel:machines) {
							//if(mosipMachineModel!=null && mosipMachineModel.isActive() && mosipMachineModel.getId().equalsIgnoreCase(machineId)) {
								if(mosipMachineModel!=null && mosipMachineModel.getId().equalsIgnoreCase(machineId)) {  //  removed isActive check so, that inactive machine can also be updated (required due to deactive regcenter scenario)
								mosipMachineModel.setSignPublicKey(publicKey);
								mosipMachineModel.setPublicKey(publicKey);
								mosipMachineModel.setName(RandomStringUtils.randomAlphanumeric(10).toUpperCase());
							//	mosipMachineModel.setZoneCode("NTH");
								MosipDataSetup.updateMachine(mosipMachineModel);
								isMachineDetailFound=true;
								break;
							}
						}
						if(!isMachineDetailFound)
							throw new RuntimeException("MachineId : " + machineId + " details not found in DB.");						
					} else
						throw new RuntimeException("MachineId : " + machineId + " details not found in DB.");
				}
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
		}
	    
		private static void createKeyFile(final String fileName, final byte[] key) {
			System.out.println("Creating file : " + fileName);
			try {
				Throwable t = null;
				try {
					final FileOutputStream os = new FileOutputStream(fileName);
					try {
						os.write(key);
					} finally {
						if (os != null) {
							os.close();
						}
					}
				} finally {
					if (t == null) {
						final Throwable exception = null;
						t = exception;
					} else {
						final Throwable exception = null;
						if (t != exception) {
							t.addSuppressed(exception);
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	 	   
}
