package io.mosip.test.packetcreator.mosippacketcreator.controller;

import java.util.Base64;
import java.util.Properties;

import io.mosip.test.packetcreator.mosippacketcreator.dto.PacketCreateDto;
import io.mosip.test.packetcreator.mosippacketcreator.dto.PersonaRequestDto;
import io.mosip.test.packetcreator.mosippacketcreator.dto.PreRegisterRequestDto;


import io.mosip.test.packetcreator.mosippacketcreator.dto.SyncRidDto;
import io.mosip.test.packetcreator.mosippacketcreator.service.*;
import variables.VariableManager;

import org.jobrunr.scheduling.JobScheduler;
import org.jobrunr.scheduling.cron.Cron;
import org.mosip.dataprovider.models.setup.MosipMachineModel;
import org.mosip.dataprovider.util.DataProviderConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.web.bind.annotation.*;

@RestController
public class TestDataController {

    private static final Logger logger = LoggerFactory.getLogger(TestDataController.class);
    
    @Value("${mosip.test.welcome}")
    private String welcomeMessage;

    @Value("${mosip.test.persona.configpath}")
	private String personaConfigPath;
    
    @Value("${mosip.test.persona.Angulipath}")
	private String personaAnguliPath;
    
    @Autowired
    PacketMakerService pkm;

    @Autowired
    PreregSyncService pss;

    @Autowired
    APIRequestUtil apiUtil;

    @Autowired
    CryptoUtil cryptoUtil;

    @Autowired
    PacketSyncService packetSyncService;

    @Autowired
    JobScheduler jobScheduler;

    @Autowired
    PacketJobService packetJobService;

    @Autowired
    ContextUtils contextUtils;
    
    @Autowired
    CommandsService testcaseExecutionService;

    @Value("${mosip.test.baseurl}")
    private String baseUrl;

    @PostMapping(value = "/servercontext/{contextKey}")
    public @ResponseBody String createServerContext(@RequestBody Properties contextProperties, @PathVariable("contextKey") String contextKey) {
    	Boolean bRet = false;
    	try{
    		bRet = contextUtils.createUpdateServerContext(contextProperties, contextKey);
    	 } catch (Exception ex){
              logger.error("createServerContext", ex);
         }
    	return bRet.toString();
    }
    @GetMapping(value = "/servercontext/{contextKey}")
    public @ResponseBody Properties getServerContext( @PathVariable("contextKey") String contextKey) {
    	Properties bRet = null;
    	try{
    		bRet = contextUtils.loadServerContext( contextKey);
    	 } catch (Exception ex){
              logger.error("createServerContext", ex);
         }
    	return bRet;
    }
    @PostMapping(value = "/packetcreator/{contextKey}")
    public @ResponseBody String createPacket(@RequestBody PacketCreateDto packetCreateDto, 
    		@PathVariable("contextKey") String contextKey) {
        try{
            return pkm.createContainer(null,packetCreateDto.getIdJsonPath(), packetCreateDto.getTemplatePath(),
            		packetCreateDto.getSource(), packetCreateDto.getProcess(), null,contextKey, true,packetCreateDto.getAdditionalInfoReqId());
        } catch (Exception ex){
             logger.error("", ex);
        }
        return "Failed!";
    }

    @GetMapping(value = "/auth")
    public @ResponseBody String getAPITestData() {
        return String.valueOf(apiUtil.initToken());
    }

    @GetMapping(value = "/clearToken")
    public @ResponseBody String ClearToken() {
    	VariableManager.setVariableValue("urlSwitched",true);
    	return "Success";
    	//return String.valueOf(apiUtil.initToken());
    }
    
    
    @GetMapping(value = "/sync")
    public @ResponseBody String syncPreregData() {
        try {
            pss.syncPrereg();
            return "All Done!";
        } catch (Exception exception) {
            logger.error("", exception);
            return exception.getMessage();
        }
    }
        
    @GetMapping(value = "/sync/{preregId}")
    public @ResponseBody String getPreregData(@PathVariable("preregId") String preregId,
    		@RequestParam(name="contextKey",required = false) String contextKey){
        try{
            return pss.downloadPreregPacket(preregId, contextKey);
        } catch(Exception exception){
            logger.error("", exception);
            return "Failed";
        }
    }

    @GetMapping(value = "/encrypt")
    public @ResponseBody String encryptData(
    		@RequestParam(name="contextKey",required = false) String contextKey) throws Exception {
        return Base64.getUrlEncoder().encodeToString(cryptoUtil.encrypt("test".getBytes(), "referenceId", contextKey));
    }

    @PostMapping(value = "/ridsync")
    public @ResponseBody String syncRid(@RequestBody SyncRidDto syncRidDto,
    		@RequestParam(name="contextKey",required = false) String contextKey) throws Exception {
    	
        return packetSyncService.syncPacketRid(syncRidDto.getContainerPath(), syncRidDto.getName(),
                syncRidDto.getSupervisorStatus(), syncRidDto.getSupervisorComment(), syncRidDto.getProcess(), contextKey,syncRidDto.getAdditionalInfoReqId());
    }

    @PostMapping(value = "/packetsync")
    public @ResponseBody String packetsync(@RequestBody PreRegisterRequestDto path, 
    		@RequestParam(name="contextKey",required = false) String contextKey) throws Exception {
        return packetSyncService.uploadPacket(path.getPersonaFilePath().get(0), contextKey);
    }

    @GetMapping(value = "/startjob")
    public @ResponseBody String startJob() {
        String response = jobScheduler.scheduleRecurrently(()->packetJobService.execute(),
                Cron.every5minutes());
        return response;
    }
    
    @GetMapping(value = "/makepacketandsync/{preregId}")
    public @ResponseBody String makePacketAndSync(@PathVariable("preregId") String preregId,
    		@RequestParam(name="contextKey",required = false) String contextKey) {

    	try{    	
    		if(personaConfigPath !=null && !personaConfigPath.equals("")) {
    			DataProviderConstants.RESOURCE = personaConfigPath;
    		}
    		return packetSyncService.makePacketAndSync(preregId,null, null,contextKey,null).toString();
    	
    	} catch (Exception ex){
             logger.error("makePacketAndSync", ex);
    	}
    	return "{Failed}";
    }
    @PostMapping(value = "/resident/{count}")
    public @ResponseBody String generateResidentData(@RequestBody PersonaRequestDto residentRequestDto,
    		@PathVariable("count") int count,
    		@RequestParam(name="contextKey",required = false) String contextKey) {

    	try{
    		logger.info("Persona Config Path="+ personaConfigPath );
    		if(personaConfigPath !=null && !personaConfigPath.equals("")) {
    			DataProviderConstants.RESOURCE = personaConfigPath;
    		}
    		if(personaAnguliPath !=null && !personaAnguliPath.equals("")) {
    			DataProviderConstants.ANGULI_PATH = personaAnguliPath;
    		}
    		logger.info("personaAnguliPath ="+ DataProviderConstants.ANGULI_PATH );
    		
    		logger.info("Resource Path="+ DataProviderConstants.RESOURCE );
    		logger.info("DOC_Template Path="+ DataProviderConstants.RESOURCE+DataProviderConstants.DOC_TEMPLATE_PATH);
    		
    		//clear all tokens
    //		VariableManager.setVariableValue("urlSwitched", "true");
    		
    		return packetSyncService.generateResidentData(count,residentRequestDto, contextKey).toString();
    	
    	} catch (Exception ex){
             logger.error("generateResidentData", ex);
    	}
    	return "{Failed}";
    }    
    @PostMapping(value = "/updateresident")
    public @ResponseBody String updateResidentData(@RequestBody PersonaRequestDto personaRequestDto ,
    		//@PathVariable("id") int id,
    		@RequestParam(name = "UIN", required = false) String uin,
    		@RequestParam(name = "RID", required = false) String rid
    		) {
    	
    	try{    	
    		if(personaConfigPath !=null && !personaConfigPath.equals("")) {
    			DataProviderConstants.RESOURCE = personaConfigPath;
    		}
    		//String uin = "";
    		//String rid = "1234567890";
    		return packetSyncService.updateResidentData(personaRequestDto.getRequests(), uin,rid);
    	
    	} catch (Exception ex){
             logger.error("registerResident", ex);
    	}
    	return "{Failed}";
    	
    }
    @PostMapping(value = "/preregister/")
    public @ResponseBody String preRegisterResident(@RequestBody PreRegisterRequestDto preRegisterRequestDto,
    		@RequestParam(name="contextKey",required = false) String contextKey) {

    	try{    	
    		if(personaConfigPath !=null && !personaConfigPath.equals("")) {
    			DataProviderConstants.RESOURCE = personaConfigPath;
    		}
    		return packetSyncService.preRegisterResident(preRegisterRequestDto.getPersonaFilePath(), contextKey);
    	
    	} catch (Exception ex){
             logger.error("registerResident", ex);
    	}
    	return "{Failed}";
    }
    /*
     * to : email | mobile
     */
    @PostMapping(value = "/requestotp/{to}") 
    public @ResponseBody String requestOtp(@RequestBody PreRegisterRequestDto preRegisterRequestDto, @PathVariable("to") String to,
    		@RequestParam(name="contextKey",required = false) String contextKey) {

    	try{    	
    		if(personaConfigPath !=null && !personaConfigPath.equals("")) {
    			DataProviderConstants.RESOURCE = personaConfigPath;
    		}
    		return packetSyncService.requestOtp(preRegisterRequestDto.getPersonaFilePath(), to, contextKey);
    	
    	} catch (Exception ex){
             logger.error("requestOtp", ex);
    	}
    	return "{Failed}";
    }
    
    @PostMapping(value = "/verifyotp/{to}")
    public @ResponseBody String verifyOtp(@RequestBody PreRegisterRequestDto preRegisterRequestDto,@PathVariable("to") String to,
    		@RequestParam(name="contextKey",required = false) String contextKey) {

    	try{    	
    		if(personaConfigPath !=null && !personaConfigPath.equals("")) {
    			DataProviderConstants.RESOURCE = personaConfigPath;
    		}
    		return packetSyncService.verifyOtp(preRegisterRequestDto.getPersonaFilePath().get(0), to, null, contextKey);
    	
    	} catch (Exception ex){
             logger.error("verifyOtp", ex);
    	}
    	return "{Failed}";
    }
    /*
     * Book first nn th available slot
     */
    @PostMapping(value = "/bookappointment/{preregid}/{nthSlot}")
    public @ResponseBody String bookAppointment(@PathVariable("preregid") String preregId,@PathVariable("nthSlot") int  nthSlot,
    		@RequestParam(name="contextKey",required = false) String contextKey) {

    	try{    	
    		if(personaConfigPath !=null && !personaConfigPath.equals("")) {
    			DataProviderConstants.RESOURCE = personaConfigPath;
    		}
    		return packetSyncService.bookAppointment(preregId,nthSlot, contextKey);
    	
    	} catch (Exception ex){
             logger.error("bookAppointment", ex);
    	}
    	return "{\"Failed\"}";
    }
    
    @PostMapping(value = "/documents/{preregid}")
    public @ResponseBody String uploadDocuments(@RequestBody PreRegisterRequestDto preRegisterRequestDto,@PathVariable("preregid") String preregId,
    		@RequestParam(name="contextKey",required = false) String contextKey) {

    	try{    	
    		if(personaConfigPath !=null && !personaConfigPath.equals("")) {
    			DataProviderConstants.RESOURCE = personaConfigPath;
    		}
    		return packetSyncService.uploadDocuments(preRegisterRequestDto.getPersonaFilePath().get(0),preregId, contextKey);
    	
    	} catch (Exception ex){
             logger.error("uploadDocuments", ex);
    	}
    	return "{\"Failed\"}";
    }
    
    @PostMapping(value = "/packet/{process}/{outFolderPath}")
    public @ResponseBody String createPackets(@RequestBody PreRegisterRequestDto preRegisterRequestDto,
    		@PathVariable("process") String process,
    		@PathVariable("outFolderPath") String outFolderPath,
    		@RequestParam(name="contextKey",required = false) String contextKey) {

    	try{    	
    		if(personaConfigPath !=null && !personaConfigPath.equals("")) {
    			DataProviderConstants.RESOURCE = personaConfigPath;
    		}
    		return packetSyncService.createPacketTemplates(preRegisterRequestDto.getPersonaFilePath(),process,outFolderPath, null,contextKey);
    	
    	} catch (Exception ex){
             logger.error("createPackets", ex);
    	}
    	return "{\"Failed\"}";
    }
    	  
    /*
     * Download from pre-reg, merge with the given packet template and upload to register
     */
    @PostMapping(value = "/packet/sync/{preregId}")
    public @ResponseBody String preRegToRegister(@RequestBody PreRegisterRequestDto preRegisterRequestDto,
    		@PathVariable("preregId") String preregId,
    		@RequestParam(name="contextKey",required = false) String contextKey) {

    	try{    	
    		if(personaConfigPath !=null && !personaConfigPath.equals("")) {
    			DataProviderConstants.RESOURCE = personaConfigPath;
    		}
    		if(preRegisterRequestDto.getPersonaFilePath().size() ==0) {
    			return "{\"Missing Template\"}";
    		}
    		String personaPath = null;
    		if(preRegisterRequestDto.getPersonaFilePath().size() > 1) {
        		personaPath = preRegisterRequestDto.getPersonaFilePath().get(1);
    		}
    		logger.info("packet-Sync: personaPath="+ (personaPath == null ? "N/A": personaPath));
    		logger.info("packet-Sync: TemplatePath="+ preRegisterRequestDto.getPersonaFilePath().get(0));
    		
    		return packetSyncService.preRegToRegister(preRegisterRequestDto.getPersonaFilePath().get(0),preregId, personaPath, contextKey,preRegisterRequestDto.getAdditionalInfoReqId());
    	
    	} catch (Exception ex){
             logger.error("createPacket", ex);
    	}
    	return "{\"Failed\"}";
    }
    
}
