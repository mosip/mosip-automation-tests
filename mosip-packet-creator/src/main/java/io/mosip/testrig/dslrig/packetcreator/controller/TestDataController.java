package io.mosip.testrig.dslrig.packetcreator.controller;

import java.util.Base64;
import java.util.HashMap;
import java.util.Properties;

import org.jobrunr.scheduling.JobScheduler;
import org.jobrunr.scheduling.cron.Cron;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.testrig.dslrig.dataprovider.util.DataProviderConstants;
import io.mosip.testrig.dslrig.dataprovider.util.RestClient;
import io.mosip.testrig.dslrig.dataprovider.variables.VariableManager;
import io.mosip.testrig.dslrig.packetcreator.dto.PacketCreateDto;
import io.mosip.testrig.dslrig.packetcreator.dto.PersonaRequestDto;
import io.mosip.testrig.dslrig.packetcreator.dto.PreRegisterRequestDto;
import io.mosip.testrig.dslrig.packetcreator.dto.RidSyncReqRequestDto;
import io.mosip.testrig.dslrig.packetcreator.dto.RidSyncReqResponseDTO;
import io.mosip.testrig.dslrig.packetcreator.dto.SyncRidDto;
import io.mosip.testrig.dslrig.packetcreator.service.APIRequestUtil;
import io.mosip.testrig.dslrig.packetcreator.service.ContextUtils;
import io.mosip.testrig.dslrig.packetcreator.service.CryptoUtil;
import io.mosip.testrig.dslrig.packetcreator.service.PacketJobService;
import io.mosip.testrig.dslrig.packetcreator.service.PacketMakerService;
import io.mosip.testrig.dslrig.packetcreator.service.PacketSyncService;
import io.mosip.testrig.dslrig.packetcreator.service.PreregSyncService;
import io.swagger.annotations.ApiOperation;

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

	@Value("${mosip.test.baseurl}")
	private String baseUrl;

	@PostMapping(value = "/servercontext/{contextKey}")
	public @ResponseBody String createServerContext(@RequestBody Properties contextProperties,
			@PathVariable("contextKey") String contextKey) {
		
		try {
			return contextUtils.createUpdateServerContext(contextProperties, contextKey);
		} catch (Exception ex) {
			logger.error("createServerContext", ex);
			return "{\"" + ex.getMessage() + "\"}";
		}
	}

	@GetMapping(value = "/servercontext/{contextKey}")
	public @ResponseBody Properties getServerContext(@PathVariable("contextKey") String contextKey) {
		Properties bRet = null;
		try {
			bRet = contextUtils.loadServerContext(contextKey);
		} catch (Exception ex) {
			logger.error("createServerContext", ex);
		}
		return bRet;
	}

	@PostMapping(value = "/packetcreator/{contextKey}")
	public @ResponseBody String createPacket(@RequestBody PacketCreateDto packetCreateDto,
			@PathVariable("contextKey") String contextKey) {
		try {
			return pkm.createContainer(packetCreateDto.getIdJsonPath(), packetCreateDto.getTemplatePath(),
					packetCreateDto.getSource(), packetCreateDto.getProcess(), null, contextKey, true,
					packetCreateDto.getAdditionalInfoReqId());
		} catch (Exception ex) {
			logger.error("", ex);
		}
		return "Failed!";
	}

	@GetMapping(value = "/auth/{contextKey}")
	public @ResponseBody String getAPITestData(@PathVariable("contextKey") String contextKey) {
		return String.valueOf(apiUtil.initToken(contextKey));
	}

	@GetMapping(value = "/clearToken/{contextKey}")
	public @ResponseBody String ClearToken(@PathVariable("contextKey") String contextKey) {
		VariableManager.setVariableValue(contextKey, "urlSwitched", true);
		return "Success";
		// return String.valueOf(apiUtil.initToken());
	}

	@GetMapping(value = "/sync/{contextKey}")
	public @ResponseBody String syncPreregData(@PathVariable("contextKey") String contextKey) {
		try {
			pss.syncPrereg(contextKey);
			return "All Done!";
		} catch (Exception exception) {
			logger.error("", exception);
			return exception.getMessage();
		}
	}

	@GetMapping(value = "/sync/{preregId}/{contextKey}")
	public @ResponseBody String getPreregData(@PathVariable("preregId") String preregId,
			@PathVariable("contextKey") String contextKey) {
		try {
			return pss.downloadPreregPacket(preregId, contextKey);
		} catch (Exception exception) {
			logger.error("", exception);
			return "Failed";
		}
	}

	@GetMapping(value = "/encrypt/{contextKey}")
	public @ResponseBody String encryptData(@PathVariable("contextKey") String contextKey) throws Exception {
		return Base64.getUrlEncoder().encodeToString(cryptoUtil.encrypt("test".getBytes(), "referenceId", contextKey));
	}

	@PostMapping(value = "/ridsync/{contextKey}")
	public @ResponseBody String syncRid(@RequestBody SyncRidDto syncRidDto,
			@PathVariable("contextKey") String contextKey) throws Exception {

		return packetSyncService.syncPacketRid(syncRidDto.getContainerPath(), syncRidDto.getName(),
				syncRidDto.getSupervisorStatus(), syncRidDto.getSupervisorComment(), syncRidDto.getProcess(),
				contextKey, syncRidDto.getAdditionalInfoReqId());
	}

	@PostMapping(value = "/ridsyncreq/{contextKey}")
	public @ResponseBody RidSyncReqResponseDTO syncRidRequest(@RequestBody RidSyncReqRequestDto syncRidDto,
			@PathVariable("contextKey") String contextKey) throws Exception {

		return packetSyncService.syncPacketRidRequest(syncRidDto.getContainerPath(), syncRidDto.getName(),
				syncRidDto.getSupervisorStatus(), syncRidDto.getSupervisorComment(), syncRidDto.getProcess(),
				contextKey, syncRidDto.getAdditionalInfoReqId());
	}

	@PostMapping(value = "/packetsync/{contextKey}")
	public @ResponseBody String packetsync(@RequestBody PreRegisterRequestDto path,
			@PathVariable("contextKey") String contextKey) throws Exception {
		try {
			return packetSyncService.uploadPacket(path.getPersonaFilePath().get(0), contextKey);
		} catch (Exception e) {
			// We need to explicitly catch the exception to handle negative scenarios , 
			//where packet sync is expected to fail
			return e.getMessage();
		}
	}

	@GetMapping(value = "/startjob/{contextKey}")
	public @ResponseBody String startJob(@PathVariable("contextKey") String contextKey) {
		String response = jobScheduler.scheduleRecurrently(() -> packetJobService.execute(contextKey),
				Cron.every5minutes());
		return response;
	}

	@GetMapping(value = "/makepacketandsync/{preregId}/{getRidFromSync}/{contextKey}")
	public @ResponseBody String makePacketAndSync(@PathVariable("preregId") String preregId,
			@PathVariable("getRidFromSync") boolean getRidFromSync,

			@PathVariable("contextKey") String contextKey) {

		try {
			if (personaConfigPath != null && !personaConfigPath.equals("")) {
				DataProviderConstants.RESOURCE = personaConfigPath;
			}

			return packetSyncService.makePacketAndSync(preregId, null, null, contextKey, null, getRidFromSync, true)
					.toString();

		} catch (Exception ex) {
			logger.error("makePacketAndSync", ex);
		}
		return "{Failed}";
	}

	@PostMapping(value = "/resident/{count}/{contextKey}")
	public @ResponseBody String generateResidentData(@RequestBody PersonaRequestDto residentRequestDto,
			@PathVariable("count") int count, @PathVariable("contextKey") String contextKey) {

		try {
			RestClient.logInfo(contextKey, "Persona Config Path=" + personaConfigPath);
			if (personaConfigPath != null && !personaConfigPath.equals("")) {
				DataProviderConstants.RESOURCE = personaConfigPath;
			}
			if (personaAnguliPath != null && !personaAnguliPath.equals("")) {
				DataProviderConstants.ANGULI_PATH = personaAnguliPath;
			}
			RestClient.logInfo(contextKey,"personaAnguliPath =" + DataProviderConstants.ANGULI_PATH);

			RestClient.logInfo(contextKey,"Resource Path=" + DataProviderConstants.RESOURCE);
			// logger.info("DOC_Template Path="+
			// VariableManager.getVariableValue(contextKey,"mosip.test.persona.documentsdatapath").toString());

			// clear all tokens
			// VariableManager.setVariableValue("urlSwitched", "true");

			return packetSyncService.generateResidentData(count, residentRequestDto, contextKey).toString();

		} catch (Exception ex) {
			logger.error("generateResidentData", ex);
			return "{\"" + ex.getMessage() + "\"}";
		}
	}

	@PostMapping(value = "/updateresident/{contextKey}")
	public @ResponseBody String updateResidentData(@RequestBody PersonaRequestDto personaRequestDto,
			// @PathVariable("id") int id,
			@RequestParam(name = "UIN", required = false) String uin,
			@RequestParam(name = "RID", required = false) String rid, @PathVariable("contextKey") String contextKey) {

		try {
			if (personaConfigPath != null && !personaConfigPath.equals("")) {
				DataProviderConstants.RESOURCE = personaConfigPath;
			}
			// String uin = "";
			// String rid = "1234567890";
			return packetSyncService.updateResidentData(personaRequestDto.getRequests(), uin, rid);

		} catch (Exception ex) {
			logger.error("registerResident", ex);
		}
		return "{Failed}";

	}

	@PostMapping(value = "/preregister/{contextKey}")
	public @ResponseBody String preRegisterResident(@RequestBody PreRegisterRequestDto preRegisterRequestDto,
			@PathVariable("contextKey") String contextKey) {

		try {
			if (personaConfigPath != null && !personaConfigPath.equals("")) {
				DataProviderConstants.RESOURCE = personaConfigPath;
			}
			return packetSyncService.preRegisterResident(preRegisterRequestDto.getPersonaFilePath(), contextKey);

		} catch (Exception ex) {
			logger.error("registerResident", ex);
		}
		return "{Failed}";
	}

	/*
	 * to : email | mobile
	 */
	@PostMapping(value = "/requestotp/{to}/{contextKey}")
	public @ResponseBody String requestOtp(@RequestBody PreRegisterRequestDto preRegisterRequestDto,
			@PathVariable("to") String to, @PathVariable("contextKey") String contextKey) {

		try {
			if (personaConfigPath != null && !personaConfigPath.equals("")) {
				DataProviderConstants.RESOURCE = personaConfigPath;
			}
			return packetSyncService.requestOtp(preRegisterRequestDto.getPersonaFilePath(), to, contextKey);

		} catch (Exception ex) {
			logger.error("requestOtp", ex);
		}
		return "{Failed}";
	}

	@PostMapping(value = "/verifyotp/{to}/{contextKey}")
	public @ResponseBody String verifyOtp(@RequestBody PreRegisterRequestDto preRegisterRequestDto,
			@PathVariable("to") String to, @PathVariable("contextKey") String contextKey) {

		try {
			if (personaConfigPath != null && !personaConfigPath.equals("")) {
				DataProviderConstants.RESOURCE = personaConfigPath;
			}
			return packetSyncService.verifyOtp(preRegisterRequestDto.getPersonaFilePath().get(0), to, null, contextKey);

		} catch (Exception ex) {
			logger.error("verifyOtp", ex);
		}
		return "{Failed}";
	}

	/*
	 * Book first nn th available slot
	 */
	@PostMapping(value = "/bookappointment/{preregid}/{nthSlot}/{contextKey}")
	public @ResponseBody String bookAppointment(@PathVariable("preregid") String preregId,
			@PathVariable("nthSlot") int nthSlot, @PathVariable("contextKey") String contextKey) {

		try {
			if (personaConfigPath != null && !personaConfigPath.equals("")) {
				DataProviderConstants.RESOURCE = personaConfigPath;
			}
			return packetSyncService.bookAppointment(preregId, nthSlot, contextKey);

		} catch (Exception ex) {
			logger.error("bookAppointment", ex);
		}
		return "{\"Failed\"}";
	}

	@PostMapping(value = "/documents/{preregid}/{contextKey}")
	public @ResponseBody String uploadDocuments(@RequestBody PreRegisterRequestDto preRegisterRequestDto,
			@PathVariable("preregid") String preregId, @PathVariable("contextKey") String contextKey) {

		try {
			if (personaConfigPath != null && !personaConfigPath.equals("")) {
				DataProviderConstants.RESOURCE = personaConfigPath;
			}
			return packetSyncService.uploadDocuments(preRegisterRequestDto.getPersonaFilePath().get(0), preregId,
					contextKey);

		} catch (Exception ex) {
			logger.error("uploadDocuments", ex);
		}
		return "{\"Failed\"}";
	}

//    @PostMapping(value = "/packet/{process}/{outFolderPath}/{contextKey}")
//    public @ResponseBody String createPackets(@RequestBody PreRegisterRequestDto preRegisterRequestDto,
//    		@PathVariable("process") String process,
//    		@PathVariable("outFolderPath") String outFolderPath,
//    		@PathVariable("contextKey") String contextKey) {
//
//    	try{    	
//    		if(personaConfigPath !=null && !personaConfigPath.equals("")) {
//    			DataProviderConstants.RESOURCE = personaConfigPath;
//    		}
//    		return packetSyncService.createPacketTemplates(preRegisterRequestDto.getPersonaFilePath(),process,outFolderPath, null,contextKey,"Registration");
//    	
//    	} catch (Exception ex){
//             logger.error("createPackets", ex);
//    	}
//    	return "{\"Failed\"}";
//    }

	/*
	 * Download from pre-reg, merge with the given packet template and upload to
	 * register
	 */
	@PostMapping(value = "/packet/sync/{preregId}/{getRidFromSync}/{genarateValidCbeff}/{contextKey}")
	public @ResponseBody String preRegToRegister(@RequestBody PreRegisterRequestDto preRegisterRequestDto,
			@PathVariable("preregId") String preregId, @PathVariable("getRidFromSync") boolean getRidFromSync,
			@PathVariable("genarateValidCbeff") boolean genarateValidCbeff,
			@PathVariable("contextKey") String contextKey) {

		try {
			if (personaConfigPath != null && !personaConfigPath.equals("")) {
				DataProviderConstants.RESOURCE = personaConfigPath;
			}
			if (preRegisterRequestDto.getPersonaFilePath().size() == 0) {
				return "{\"Missing Template\"}";
			}
			String personaPath = null;
			if (preRegisterRequestDto.getPersonaFilePath().size() > 1) {
				personaPath = preRegisterRequestDto.getPersonaFilePath().get(1);
			}

			RestClient.logInfo(contextKey,"packet-Sync: personaPath=" + (personaPath == null ? "N/A" : personaPath));
			RestClient.logInfo(contextKey,"packet-Sync: TemplatePath=" + preRegisterRequestDto.getPersonaFilePath().get(0));

			return packetSyncService.preRegToRegister(preRegisterRequestDto.getPersonaFilePath().get(0), preregId,
					personaPath, contextKey, preRegisterRequestDto.getAdditionalInfoReqId(), getRidFromSync,
					genarateValidCbeff);

		} catch (Exception ex) {
			logger.error("createPacket", ex);
		}
		return "{\"Failed\"}";
	}

	@ApiOperation(value = "Delete Booking appointment for a given pre-registration-Id", response = String.class)

	@DeleteMapping(value = "/preregistration/v1/applications/appointment/{contextKey}")
	public @ResponseBody String deleteAppointment(@RequestParam(name = "preRegistrationId") String preregId,
			@PathVariable("contextKey") String contextKey) {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("preRegistrationId", preregId);
		return packetSyncService.discardBooking(map, contextKey);

	}

	@ApiOperation(value = "Update appointment for a given PreRegID ", response = String.class)
	@PutMapping(value = "/preregistration/v1/applications/appointment/{preregid}/{contextKey}")
	public @ResponseBody String updateAppointment(@PathVariable("preregid") String preregid,
			@PathVariable("contextKey") String contextKey) {
		try {

			return packetSyncService.updatePreRegAppointment(preregid, contextKey);

		} catch (Exception ex) {
			logger.error("registerResident", ex);
		}
		return "{Failed}";
	}

	@ApiOperation(value = "Discard Applications for a given pre-registration-Id", response = String.class)
	@DeleteMapping(value = "/preregistration/v1/applications/{preregid}/{contextKey}")
	public @ResponseBody String discardApplication(@PathVariable("preregid") String preregId,
			@PathVariable("contextKey") String contextKey) {

		return packetSyncService.deleteApplication(preregId, contextKey);

	}

}
