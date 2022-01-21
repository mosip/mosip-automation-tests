package io.mosip.test.packetcreator.mosippacketcreator.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import java.io.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mosip.dataprovider.test.CreatePersona;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.databind.ObjectWriter;
import org.springframework.util.StringUtils;

@Service
public class PacketMakerService {
    
    Logger logger = LoggerFactory.getLogger(PacketMakerService.class);

    private static final String UNDERSCORE = "_";
    private static final String PACKET_META_FILENAME = "packet_meta_info.json";
    private static final String PACKET_DATA_HASH_FILENAME = "packet_data_hash.txt";
    private static final String PACKET_OPERATION_HASH_FILENAME = "packet_operations_hash.txt";

    @Value("${mosip.test.temp:/tmp/}")
    private String tempLogPath;
    
    @Value("${mosip.test.regclient.store:/home/sasikumar/Documents/MOSIP/packetcreator}")
    private String finalDestination;

    @Value("${mosip.test.packet.template.location:/home/sasikumar/Documents/MOSIP/packetcreator/template}")
    private String templateFolder;

    @Value("${mosip.test.packet.template.source:REGISTRATION_CLIENT}")
    private String src;

    @Value("${mosip.test.packet.template.process:NEW}")
    private String process;

    @Value("${mosip.test.regclient.centerid}")
    private String centerId;

    @Value("${mosip.test.regclient.machineid}")
    private String machineId;

    @Value("${mosip.test.rid.seq.initialvalue}")
    private int counter;

    @Value("${mosip.test.regclient.userid}")
    private String officerId;

    @Value("${mosip.test.regclient.supervisorid}")
    private String supervisorId;

   
    @Autowired
    private CryptoUtil cryptoUtil;

    @Autowired
    private ZipUtils zipper;

    @Autowired
    private SchemaUtil schemaUtil;

    private String workDirectory;
    private String defaultTemplateLocation;

    @Autowired
    private ContextUtils contextUtils;
   
    @Autowired
    private PacketSyncService packetSyncService;
    
    private String newRegId;
    
    
	@Value("${mosip.version:1.2}")
	private String mosipVersion;
	
	@Value("${packetmanager.zip.datetime.pattern:yyyyMMddHHmmss}")
	private String zipDatetimePattern;
    
    @PostConstruct
    public void initService(){
        if (workDirectory != null) return;
        try{
            workDirectory = Files.createTempDirectory("pktcreator").toFile().getAbsolutePath();
            logger.info("CURRENT WORK DIRECTORY --> {}", workDirectory);
            File folder = new File(templateFolder);
            File[] files = folder.listFiles();
            if(files != null && files.length > 0) {
            	File templateName = folder.listFiles()[0];
            	defaultTemplateLocation = templateName.getAbsolutePath();
            }
            
        } catch(Exception ex) {
            logger.error("", ex);
        }
       
    }

    public String getWorkDirectory() {
        return workDirectory;
    }
    public static String getRegIdFromPacketPath(String packetPath) {
    	//leaf node of packet path is regid
    	
    	//return Path.of(packetPath).getFileName().toString();
    	Path container = Path.of(packetPath);
    	String rid = container.getName(container.getNameCount()-1).toString().split("-")[0];
    	return rid;
    }
    public String packPacketContainer(String packetPath,String source,String proc, String contextKey, boolean isValidChecksum) throws Exception {

    	String retPath = "";
    	if(contextKey != null && !contextKey.equals("")) {
    		
    		Properties props = contextUtils.loadServerContext(contextKey);
    		props.forEach((k,v)->{
    			if(k.toString().equals("mosip.test.packet.template.source")) {
    				src = v.toString();
    			}
    			if(k.toString().equals("mosip.test.packet.template.process")) {
    				process = v.toString();
    			}
    
    			else
    			if(k.toString().equals("mosip.test.regclient.centerid")) {
        			centerId = v.toString();
        		}
    			else
        		if(k.toString().equals("mosip.test.regclient.machineid")) {
        			machineId = v.toString();
            	}	
        		else
            		if(k.toString().equals("mosip.test.regclient.supervisorid")) {
            			supervisorId = v.toString();
                	}
            		else if (k.toString().equals("mosip.test.regclient.userid")) {
                        officerId = v.toString();
                    }
    			
    		});
    	}
    	if(source != null)
    		src = source;
     
    	String regId = getRegIdFromPacketPath(packetPath);
    	String tempPacketRootFolder = Path.of(packetPath).toString();
    	
        if(proc != null)
        	process = proc;
        else {
        	String tprocess = ContextUtils.ProcessFromTemplate(src,packetPath);
        	if(tprocess != null)
        		process = tprocess;
        }
        logger.info("packPacketContainer:src="+ src + ",process=" + process + "PacketRoot=" + tempPacketRootFolder +" regid=" + regId);
      
        packPacket(getPacketRoot(getProcessRoot(tempPacketRootFolder), regId, "id"), regId, "id",contextKey);
      
        packPacket(getPacketRoot(getProcessRoot(tempPacketRootFolder), regId, "evidence"), regId, "evidence",contextKey);
        packPacket(getPacketRoot(getProcessRoot(tempPacketRootFolder), regId, "optional"), regId, "optional",contextKey);
        packContainer(tempPacketRootFolder,contextKey);
        
        retPath = Path.of(Path.of(tempPacketRootFolder) + ".zip").toString();
        
        return retPath;

    }
    public String createPacketFromTemplate(String templatePath, String personaPath, String contextKey,String additionalInfoReqId) throws Exception {

    	logger.info("createPacketFromTemplate" );
    	
    	Path idJsonPath = null;
    	//Fix for change in Demodata
    	 if(templatePath != null) {
         	process = ContextUtils.ProcessFromTemplate(src, templatePath);
         	//get idJson From Template itself
         	idJsonPath = ContextUtils.idJsonPathFromTemplate(src, templatePath);
    	 }
    	 else
    		 idJsonPath = packetSyncService.createIDJsonFromPersona(personaPath, contextKey);
     	
    	String packetPath = createContainer( null,
    			(idJsonPath == null ? null: idJsonPath.toString()),
    			templatePath,src,process, null,contextKey,false,additionalInfoReqId);

    	logger.info("createPacketFromTemplate:Packet created : {}", packetPath);
    	//newRegId
    	JSONObject retObj = new JSONObject();
    	retObj.put("packet", packetPath);
    	retObj.put("regId", newRegId);
    	
    	return retObj.toString();
    }


    /*
     * Create packet with our without Encryption
     */
    public String createContainer(Path docPath, String dataFile, String templatePacketLocation, String source, String processArg, String preregId, String contextKey, boolean bZip,String additionalInfoReqId) throws Exception{
    	
    	String retPath = "";
    	if(contextKey != null && !contextKey.equals("")) {
    		
    		Properties props = contextUtils.loadServerContext(contextKey);
    		props.forEach((k,v)->{
    			if(k.toString().equals("mosip.test.packet.template.source")) {
    				src = v.toString();
    			}
    			if(k.toString().equals("mosip.test.packet.template.process")) {
    				process = v.toString();
    			}
    
    			else
    			if(k.toString().equals("mosip.test.regclient.centerid")) {
        			centerId = v.toString();
        		}
    			else
        		if(k.toString().equals("mosip.test.regclient.machineid")) {
        			machineId = v.toString();
            	}	
        		else
            		if(k.toString().equals("mosip.test.regclient.supervisorid")) {
            			supervisorId = v.toString();
                	}
            		else if (k.toString().equals("mosip.test.regclient.userid")) {
                        officerId = v.toString();
                    }
					else if (k.toString().equals("mosip.version")) {
					mosipVersion = v.toString();
				}
    		});
    	}
    	
        String templateLocation = (null == templatePacketLocation)?defaultTemplateLocation: templatePacketLocation;
       
        String regId = generateRegId();
        String appId = ( additionalInfoReqId == null) ? regId: additionalInfoReqId;
        newRegId = regId;
        if(source != null && !source.equals(""))
        	src = source;
        if(processArg != null && !processArg.equals(""))
        	process = processArg;
        else {
        	String tprocess = ContextUtils.ProcessFromTemplate(src,templatePacketLocation);
        	if(tprocess != null)
        		process = tprocess;
        }
        logger.info("src="+ src + ",process=" + process);
        String tempPacketRootFolder = createTempTemplate(templateLocation, appId);
      
        // tempPacketRootFolder=C:\Users\ALOK~1.KUM\AppData\Local\Temp\pktcreator14605878540379887785\10001100771000120211108051810-10001_10077-20211108051810
		/*
		 * if (docPath != null) { String newloc = tempPacketRootFolder + File.separator
		 * + src + File.separator + process + File.separator + appId + "_id"; for (File
		 * f : new File(newloc).listFiles()) { if (f.getName().endsWith(".pdf"))
		 * f.delete(); } try { FileUtils.copyDirectory(docPath.toFile(), new
		 * File(newloc)); } catch (IOException e) { e.printStackTrace(); } }
		 */
        
        //update document file here
        createPacket(tempPacketRootFolder, regId, dataFile, "id",preregId,contextKey);
        if(bZip)
        	packPacket(getPacketRoot(getProcessRoot(tempPacketRootFolder), regId, "id"), regId, "id",contextKey);
        createPacket(tempPacketRootFolder, regId, dataFile, "evidence",preregId,contextKey);
        if(bZip)
        	packPacket(getPacketRoot(getProcessRoot(tempPacketRootFolder), regId, "evidence"), regId, "evidence",contextKey);
        createPacket(tempPacketRootFolder, regId, dataFile, "optional",preregId,contextKey);
        if(bZip) {
        	packPacket(getPacketRoot(getProcessRoot(tempPacketRootFolder), regId, "optional"), regId, "optional",contextKey);
        	packContainer(tempPacketRootFolder,contextKey);
        
        	retPath = Path.of(Path.of(tempPacketRootFolder) + ".zip").toString();
        }
        else
        {
        	retPath = tempPacketRootFolder;
        }
        return retPath;
        
    }

    /**
     * 
     * @param templateFile - template folder location.
     * @param dataFile - JSON file name whose content has to be merged
     * @return - the merged JSON as a generic map Map<?,?>
     */
     Map<?,?> mergeJSON(String templateFile, String dataFile) throws Exception{
        try (InputStream inputStream = new FileInputStream(dataFile) ) {
            String dataToMerge = new String(inputStream.readAllBytes(),StandardCharsets.UTF_8);
            JSONObject data = new JSONObject(dataToMerge);
            
            //SKV - custom json merge
            try (InputStream inputStream2 = new FileInputStream(templateFile) ) {
            	String templateData = new String(inputStream2.readAllBytes(),StandardCharsets.UTF_8);
            	JSONObject data1 = new JSONObject(templateData);
                  
            	JSONObject result  = merge(data1,data);
            	return result.toMap();
            }         
            //return mergeJSON(templateFile, data);
        }
    }

    /**
     * 
     * @param templateFile - template folder location.
     * @param data - JSONObject whose content has to be merged
     * @return - the merged JSON as a generic map Map<?,?>
     */
     JSONObject mergeJSONObject(String templateFile, JSONObject data) throws Exception{
    	/*
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setDefaultMergeable(false).configOverride(ArrayList.class).setMergeable(false);
        Map<?,?> genericJSONObject = objectMapper.readValue(Paths.get(templateFile).toFile(), Map.class);
        String dataToMerge = data.toString();
        ObjectReader objectReader = objectMapper.readerForUpdating(genericJSONObject);
        return objectReader.readValue(dataToMerge);*/
        try (InputStream inputStream2 = new FileInputStream(templateFile) ) {
        	String templateData = new String(inputStream2.readAllBytes(),StandardCharsets.UTF_8);
        	JSONObject data1 = new JSONObject(templateData);
        	logger.info("templatejson:" + templateData);
        	logger.info("preregjson:" + data.toString());
        	JSONObject result  = merge(data1,data);
        	logger.info("mergedjson:" + result.toString());
        	
        	return result;
        	
        }
    }

  /*  public boolean createPacketRandom(String containerRootFolder, String regId, String templateFilePath, String type){
        //TODO: Create a file from the templateFilePath then call the normal createPacket
        return true;
    }
*/
    private List<String> getMissingAttributeList(String schemaJson, JSONObject idJson){
    	//SKV - check missing mandatory attributes
        JSONObject schema = new JSONObject(schemaJson);
        schema = schema.getJSONObject("properties");
        schema  = schema.getJSONObject("identity");
        JSONArray schemaReqd = schema.getJSONArray("required");

        List<String> notFound = new ArrayList<String>();
        
        for(int i=0; i < schemaReqd.length();i++) {
        	String attrName = schemaReqd.getString(i);
        		//check whether merged map contain this key
        	if( !idJson.has(attrName))
        		notFound.add(attrName);
        }
        return notFound;

    }
    private String fillMissingAttributes( List<String> missingAttributes, String dataToMerge) {
    	
    	JSONObject data = new JSONObject(dataToMerge);
    	JSONObject jb = new JSONObject(dataToMerge).getJSONObject("identity");
        for(String s: missingAttributes) {
        	if(s.toLowerCase().matches(".*individual.*biometric.*")) {
        		JSONObject bio = new JSONObject();
        		bio.put("format", "cbeff");
        		bio.put("version", "1");
        		bio.put("value", "individualBiometrics_bio_CBEFF");
        		jb.put(s, bio);
        	}
        	else if(s.toLowerCase().contains("city"))
        	{
        		//copy from city value
        	
        		JSONArray cityArr = jb.getJSONArray( "City");
        		jb.put(s, cityArr);
        		
        	}
        	//pobCountry
        	else if(s.toLowerCase().contains("country"))
        	{
        	    JSONObject bio = new JSONObject();
        	    bio.put("language", "eng");
        	    bio.put("value","Abra");
        	    jb.put(s, new JSONArray().put( bio));
    		
        	}
        	//province
        	else if(s.toLowerCase().contains("province"))
        	{
        		//copy from city value
        	
        		JSONArray cityArr = jb.getJSONArray( "province");
        		jb.put(s, cityArr);
        		
        	}
        	else if(s.toLowerCase().matches(".*proof.*address.*")) {
        		JSONObject bio = new JSONObject();
        		bio.put("type", "DOC023");
        		bio.put("format", "PDF");
        		bio.put("value", "proofOfAddress");
        		jb.put(s, bio);
        	}
        	else if(s.toLowerCase().matches(".*proof.*identity.*")) {
        		JSONObject bio = new JSONObject();
        		bio.put("type", "DOC018");
        		bio.put("format", "PDF");
        		bio.put("value", "proofOfIdentity");
        		jb.put(s, bio);
        	}
        	else
        	if(s.toLowerCase().matches(".*parent.*biometric.*")) {
        		JSONObject bio = new JSONObject();
        		bio.put("format", "cbeff");
        		bio.put("version", "1");
        		bio.put("value", "individualBiometrics_bio_CBEFF");
        		jb.put(s, bio);
        	}
        	else
        	{
        		JSONObject bio = new JSONObject();
        		bio.put("language", "eng");
        		bio.put("value","101755");
        		jb.put(s, bio);
        	}

        }
    	data.put("identity", jb);
    	return data.toString();
    }
    
     boolean createPacket(String containerRootFolder, String regId, String dataFilePath, String type, String preregId, String contextKey) throws Exception{
        String packetRootFolder = getPacketRoot(getProcessRoot(containerRootFolder), regId, type);
        String templateFile = getIdJSONFileLocation(packetRootFolder);

        String dataToMerge = null;
        if(dataFilePath != null)
        	dataToMerge = Files.readString(Path.of(dataFilePath));
        
        JSONObject jb = new JSONObject(dataToMerge).getJSONObject("identity");
       
        // workaround for MOSIP-18123
		
		
		
		  JSONObject jb1 = new JSONObject(dataToMerge); List<String> jsonList =
		  jb.keySet().stream().filter(j ->
		  j.startsWith("proof")).collect(Collectors.toList()); jsonList.forEach(o ->{
		  jb1.getJSONObject("identity").getJSONObject(o).put("value", o);
		 
		  jb1.getJSONObject("identity").getJSONObject(o).remove("refNumber");});
		  
		  dataToMerge = jb1.toString(); System.out.println(jb1);
		 
		 
		 
        //
        
        String schemaVersion = jb.optString("IDSchemaVersion", "0");
        String schemaJson = schemaUtil.getAndSaveSchema(schemaVersion, workDirectory, contextKey);
     
        if(type.equals("id")) {
        	  
        	Files.write(Path.of(tempLogPath + regId + "_schema.json"), schemaJson.getBytes() );
              
        }
      /* 
        if(type.equals("id")){
        		List<String> missingAttributes = getMissingAttributeList(schemaJson, jb);
        
        		dataToMerge = fillMissingAttributes( missingAttributes, dataToMerge);
    	} 
       */
      JSONObject jbToMerge = schemaUtil.getPacketIDData(schemaJson, dataToMerge, type);
        
      JSONObject mergedJsonMap = mergeJSONObject (templateFile, jbToMerge);
      
      if(type.equals("id")) {
    	  List<String> invalidIds =  CreatePersona.validateIDObject(   mergedJsonMap);
    	  Files.write(Path.of(tempLogPath + regId + "_invalidIds.json"), invalidIds.toString().getBytes());
      }
                
        if(!writeJSONFile(mergedJsonMap.toMap(), templateFile)) {
            logger.error("Error creating packet {} ", regId);
            return false;
        }

        /*Debug */
        /*if(type.equals("id")) {
        	writeJSONFile(mergedJsonMap, "c:\\temp\\id_"+regId + ".json");
        }*/
        updatePacketMetaInfo(packetRootFolder, "metaData","registrationId", regId, true);
        if(preregId!=null && !preregId.equalsIgnoreCase("0")) // newly added

        updatePacketMetaInfo(packetRootFolder, "metaData","preRegistrationId", preregId, true);
        
        updatePacketMetaInfo(packetRootFolder, "metaData","creationDate", APIRequestUtil.getUTCDateTime(null), true);
        updatePacketMetaInfo(packetRootFolder, "metaData","machineId", machineId, false);
        updatePacketMetaInfo(packetRootFolder, "metaData","centerId", centerId, false);
        updatePacketMetaInfo(packetRootFolder, "metaData","registrationType",
                StringUtils.capitalize(process.toLowerCase()), false);
        

        updatePacketMetaInfo(packetRootFolder, "operationsData", "officerId", officerId, false);
        updatePacketMetaInfo(packetRootFolder, "operationsData", "supervisorId", supervisorId, false);

        updateAudit(packetRootFolder, regId);

        LinkedList<String> sequence = updateHashSequence1(packetRootFolder);
        LinkedList<String> operations_seq = updateHashSequence2(packetRootFolder);
        updatePacketDataHash(packetRootFolder, sequence, PACKET_DATA_HASH_FILENAME);
        updatePacketDataHash(packetRootFolder, operations_seq, PACKET_OPERATION_HASH_FILENAME);
        return true;
    }

     boolean packPacket(String containerRootFolder, String regId, String type, String contextKey) throws Exception{
        boolean result = zipAndEncrypt(Path.of(containerRootFolder), contextKey);
        if (!result){
            logger.error("Encryption failed!!! ");
            return false;
        }

        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        String encryptedHash = org.apache.commons.codec.binary.Base64.encodeBase64URLSafeString(messageDigest.
                digest(Files.readAllBytes(Path.of(Path.of(containerRootFolder) + ".zip"))));

        String signature = Base64.getEncoder().encodeToString(cryptoUtil.sign(Files.readAllBytes(Path.of(Path.of(containerRootFolder) + "_unenc.zip")),contextKey));

        Path src = Path.of(containerRootFolder + "_unenc.zip");
        Files.copy(src, Path.of(tempLogPath + src.getFileName()),StandardCopyOption.REPLACE_EXISTING );
        
        Files.delete(Path.of(containerRootFolder + "_unenc.zip"));
        FileSystemUtils.deleteRecursively(Path.of(containerRootFolder));

        String containerMetaDataFileLocation = containerRootFolder + ".json";
        return fixContainerMetaData(containerMetaDataFileLocation, regId, type, encryptedHash, signature);
    }

     boolean packContainer(String containerRootFolder, String contextKey) throws Exception{
        Path path = Path.of(containerRootFolder);
      
        boolean result = zipAndEncrypt(path, contextKey);
   
        Path src = Path.of(path + "_unenc.zip");
        
        Files.copy(src, Path.of(tempLogPath + src.getFileName()),StandardCopyOption.REPLACE_EXISTING );
        
        Files.delete(Path.of(path + "_unenc.zip"));
        return result;
    }

    private boolean zipAndEncrypt(Path zipSrcFolder, String contextKey) throws Exception{
        Path finalZipFile = Path.of(zipSrcFolder + "_unenc.zip");
        zipper.zipFolder(zipSrcFolder, finalZipFile);
        try(FileInputStream zipFile = new FileInputStream(finalZipFile.toFile().getAbsolutePath())){
            boolean result = cryptoUtil.encryptPacket(zipFile.readAllBytes(), centerId + UNDERSCORE + machineId, 
            		Path.of(zipSrcFolder+".zip").toString(), contextKey);
           // Dont uncomment this: Files.delete(finalZipFile);
            if (!result){
                logger.error("Encryption failed!!! ");
                return false;
            }
        }
        return true;
    }

    private boolean writeJSONFile(Map<?,?> jsonValue, String fileToWrite){
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectWriter jsonWriter = objectMapper.writer();
        try (FileOutputStream fos = new FileOutputStream(fileToWrite)) {
            OutputStreamWriter writer = new OutputStreamWriter(fos, "UTF-8");
            jsonWriter.writeValue(writer,jsonValue); 
            return true;
        } catch(Exception ex){
            logger.error("",ex);
            return false;
        }
    }

    private String getIdJSONFileLocation(String packetRootFolder){
        return new File(packetRootFolder+File.separator+ "ID".toUpperCase()+".json").toString();
    }

    private String getProcessRoot(String containerRootFolder){
        return Path.of(containerRootFolder, src, process).toString();
    }

    private String getPacketRoot(String processRootFolder, String rid, String type){
        return Path.of(processRootFolder, rid+ UNDERSCORE + type.toLowerCase()).toString();
    }

    private String getContainerMetadataJSONFileLocation(String processRootFolder, String rid, String type){
        return Path.of(processRootFolder, rid+ UNDERSCORE + type.toLowerCase()+".json").toString();
    }

    private String createTempTemplate(String templatePacket, String rid) throws IOException, SecurityException{
        Path sourceDirectory = Paths.get(templatePacket);
        String tempDir = workDirectory + File.separator + rid+ "-"+  centerId+ "_"+ machineId +"-"+getcurrentTimeStamp();
        Path targetDirectory = Paths.get(tempDir);
        FileSystemUtils.copyRecursively(sourceDirectory, targetDirectory);
		// addtionrequestId!=null ==> addtionrequestId- center_machine-timestamp.zip
		// addtionrequestId==null ==> rid-center_machine-timestamp.zip
		setupTemplateName(tempDir, rid);
        return targetDirectory.toString();    
    }
   
    
    private void setupTemplateName(String templateRootPath, String regId) throws SecurityException{
        String finalPath = templateRootPath + File.separator+ src + File.separator + process;
        File rootFolder = new File(finalPath);
        File[] listFiles= rootFolder.listFiles();
        if(listFiles != null) {
        for(File f: listFiles ){
            String name = f.getName();
            String finalName = name.replace("rid",regId);
            f.renameTo(new File(finalPath + File.separator + finalName));
        }
        }
    }
    
    private String getcurrentTimeStamp() {
		DateTimeFormatter format = DateTimeFormatter.ofPattern(zipDatetimePattern);
		return LocalDateTime.now(ZoneId.of("UTC")).format(format);
	}

    private boolean fixContainerMetaData(String fileToFix,String rid, String type, String encryptedHash, String signature ) throws IOException, Exception{
      //  JSONObject metadata = new JSONObject();
        Map<String, String> metaData = new HashMap();
        metaData.put("process", process);
        metaData.put("creationdate",APIRequestUtil.getUTCDateTime(null));
        //TODO: Encrypted file SHA256 hash
        metaData.put("encryptedhash",encryptedHash);
        metaData.put("signature", signature);
        metaData.put("id",rid);
        metaData.put("source",src);
        //TODO: How to alter this? for now we leave it as is
        //metaData.put("providerversion",);
        //metaData.put("schemaversion",);
        metaData.put("packetname", rid+ UNDERSCORE +type);
        //metaData.put("providername", );
        
        File containerMetaDataTemp = File.createTempFile("pkm", ".cm");
        writeJSONFile(metaData, containerMetaDataTemp.getAbsolutePath());
        Map<?,?> mergedJsonMap = mergeJSON(fileToFix, containerMetaDataTemp.getAbsolutePath());
        if(!writeJSONFile(mergedJsonMap, fileToFix)) {
            logger.error("Error creating containerMetaData packet {} ", rid);
            return false;
        }
        return true;
        
    }

	 JSONObject merge(JSONObject mainNode, JSONObject updateNode) {

	    Iterator<String> fieldNames = updateNode.keys();

	    while (fieldNames.hasNext()) {
	        String updatedFieldName = fieldNames.next();
	        Object valueToBeUpdatedO = null;
	        Object updatedValueO =null;
	        if(mainNode.has(updatedFieldName))
	        	valueToBeUpdatedO = mainNode.get(updatedFieldName);
	        if(updateNode.has(updatedFieldName))
		        updatedValueO = updateNode.get(updatedFieldName);

	        // If the node is an @ArrayNode
	        if (valueToBeUpdatedO != null && valueToBeUpdatedO instanceof JSONArray && 
	            updatedValueO instanceof JSONArray) {
	        	JSONArray valueToBeUpdated = (JSONArray)valueToBeUpdatedO;
	        	JSONArray updatedValue = (JSONArray) updatedValueO;
	        	
	            // running a loop for all elements of the updated ArrayNode
	        	
	            for (int i = 0; i < updatedValue.length(); i++) {
	                JSONObject updatedChildNode = updatedValue.getJSONObject(i);
	                // Create a new Node in the node that should be updated, if there was no corresponding node in it
	                // Use-case - where the updateNode will have a new element in its Array
	                if (valueToBeUpdated.length() <= i) {
	                    valueToBeUpdated.put(updatedChildNode);
	                }
	                // getting reference for the node to be updated
	                JSONObject childNodeToBeUpdated = valueToBeUpdated.getJSONObject(i);
	                merge(childNodeToBeUpdated, updatedChildNode);
	            }
	        // if the Node is an @ObjectNode
	        } else if (valueToBeUpdatedO != null && valueToBeUpdatedO instanceof JSONObject) {
	        	
	            merge((JSONObject)valueToBeUpdatedO,(JSONObject) updatedValueO);
	        } else {
	            if (mainNode instanceof JSONObject) {
	            	 mainNode.put(updatedFieldName,updatedValueO);
	            }
	        }
	    }
	    return mainNode;
	}

    private String generateRegId() {
		SimpleDateFormat f = new SimpleDateFormat("yyyyMMddHHmmss");
		f.setTimeZone(TimeZone.getTimeZone("UTC"));
		String currUTCTime = f.format(new Date());
		++counter;
		return centerId + machineId + counter + currUTCTime;
    }

    private LinkedList<String>  updateHashSequence1(String packetRootFolder) throws Exception {
        LinkedList<String> sequence = new LinkedList<>();
        String metaInfo_json = Files.readString(Path.of(packetRootFolder, PACKET_META_FILENAME));
        JSONObject metaInfo = new JSONObject(metaInfo_json);

        metaInfo.getJSONObject("identity").put("hashSequence1", new JSONArray());

        sequence = updateHashSequence(metaInfo, "hashSequence1", "biometricSequence", sequence,
                getBiometricFiles(packetRootFolder));

        sequence = updateHashSequence(metaInfo, "hashSequence1", "demographicSequence", sequence,
                getDemographicDocFiles(packetRootFolder));

        Files.write(Path.of(packetRootFolder, PACKET_META_FILENAME), metaInfo.toString().getBytes("UTF-8"));

        return sequence;
    }

    private LinkedList<String>  updateHashSequence2(String packetRootFolder) throws Exception {
        LinkedList<String> sequence = new LinkedList<>();
        String metaInfo_json = Files.readString(Path.of(packetRootFolder, PACKET_META_FILENAME));
        JSONObject metaInfo = new JSONObject(metaInfo_json);

        metaInfo.getJSONObject("identity").put("hashSequence2", new JSONArray());

        sequence = updateHashSequence(metaInfo, "hashSequence2", "otherFiles", sequence,
                getOperationsFiles(packetRootFolder));

        Files.write(Path.of(packetRootFolder, PACKET_META_FILENAME), metaInfo.toString().getBytes("UTF-8"));

        return sequence;
    }

    private void updatePacketDataHash(String packetRootFolder, LinkedList<String> sequence, String fileName) throws Exception {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for(String path : sequence) {
            out.write(Files.readAllBytes(Path.of(path)));
        }
        String packetDataHash = new String(Hex.encode(messageDigest.digest(out.toByteArray()))).toUpperCase();
        //TODO - its failing with Hex.encoded hash, so using the below method to generate hash
        String packetDataHash2 = DatatypeConverter.printHexBinary(messageDigest.digest(out.toByteArray())).toUpperCase();
        logger.info("sequence packetDataHash >> {} ", packetDataHash);
        logger.info("sequence packetDataHash2 >> {} ", packetDataHash2);

        Files.write(Path.of(packetRootFolder, fileName), packetDataHash2.getBytes());
    }

    private List<String> getBiometricFiles(String packetRootFolder) {
        List<String> paths = new ArrayList<>();
        File packetFolder = Path.of(packetRootFolder).toFile();
        File[] biometricFiles = packetFolder.listFiles((d, name) -> name.endsWith(".xml"));
        for(File file : biometricFiles) {
            paths.add(file.getAbsolutePath());
        }
        return paths;
    }

    private List<String> getDemographicDocFiles(String packetRootFolder) {
        List<String> paths = new ArrayList<>();
        File packetFolder = Path.of(packetRootFolder).toFile();
        File[] documents = packetFolder.listFiles((d, name) -> name.endsWith(".pdf") ||
                name.endsWith(".jpg") || name.equals("ID.json"));
        //File[] documents = packetFolder.listFiles((d, name) -> name.equals("ID.json"));
        for(File file : documents) {
            paths.add(file.getAbsolutePath());
        }
        return paths;
    }

    //TODO - add operators biometric files
    private List<String> getOperationsFiles(String packetRootFolder) {
        List<String> paths = new ArrayList<>();
        File packetFolder = Path.of(packetRootFolder).toFile();
        File[] documents = packetFolder.listFiles((d, name) -> name.equals("audit.json"));
        for(File file : documents) {
            paths.add(file.getAbsolutePath());
        }
        return paths;
    }

    private LinkedList<String> updateHashSequence(JSONObject metaInfo, String parentKey, String seqName,
                                                  LinkedList<String> sequence, List<String> files) {

        JSONObject seqObject = new JSONObject();
        if(files != null && files.size() > 0) {
            JSONArray list = new JSONArray();
            for(String path : files) {
                File file = new File(path);
                String fileName = file.getName();
                list.put(fileName.substring(0, fileName.lastIndexOf(".")));
                sequence.add(file.getAbsolutePath());
            }
            if(list.length() > 0) {
                seqObject.put("label", seqName);
                seqObject.put("value", list);
            }
        }
        if(seqObject.length() > 0)
            metaInfo.getJSONObject("identity").getJSONArray(parentKey).put(seqObject);

        return sequence;
    }

    private void updatePacketMetaInfo(String packetRootFolder, String parentKey, String key, String value, boolean parentLevel) throws Exception {
        String metaInfo_json = Files.readString(Path.of(packetRootFolder, PACKET_META_FILENAME));
        JSONObject jsonObject = new JSONObject(metaInfo_json);

        if(parentLevel)
            jsonObject.getJSONObject("identity").put(key, value);

        boolean updated = false;
        if(jsonObject.getJSONObject("identity").has(parentKey)) {
            JSONArray metadata = jsonObject.getJSONObject("identity").getJSONArray(parentKey);
            for(int i=0;i<metadata.length();i++) {
                if(metadata.getJSONObject(i).getString("label").equals(key)) {
                    jsonObject.getJSONObject("identity").getJSONArray(parentKey)
                            .getJSONObject(i).put("value", value);
                    updated = true;
                }
            }
        }

        if(!updated)  {
            JSONObject rid = new JSONObject();
            rid.put("label", key);
            rid.put("value", value);
            jsonObject.getJSONObject("identity").getJSONArray(parentKey).put(rid);
        }

        Files.write(Path.of(packetRootFolder, PACKET_META_FILENAME), jsonObject.toString().getBytes("UTF-8"));
    }

    private void updateAudit(String path, String rid) {
        Path auditfile = Path.of(path, "audit.json");
        if(auditfile.toFile().exists()) {
            try {
                List<String> newLines = new ArrayList<>();
                for(String line : Files.readAllLines(auditfile, StandardCharsets.UTF_8)) {
                    newLines.add(line.replaceAll("<RID>", rid));
                }
                Files.write(auditfile, newLines, StandardCharsets.UTF_8);
            } catch (IOException e) {
                logger.info("Failed to update audit.json", e);
            }
        }
    }
}
