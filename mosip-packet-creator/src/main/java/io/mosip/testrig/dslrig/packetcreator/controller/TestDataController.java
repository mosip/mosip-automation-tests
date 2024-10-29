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
import org.springframework.context.annotation.Lazy;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "TestDataController", description = "REST APIs for Test data")
public class TestDataController {

	private static final Logger logger = LoggerFactory.getLogger(TestDataController.class);

	@Value("${mosip.test.welcome}")
	private String welcomeMessage;

	@Value("${mosip.test.persona.configpath}")
	private String personaConfigPath;

	@Value("${mosip.test.persona.Angulipath}")
	private String personaAnguliPath;

	PacketMakerService pkm;
	PacketSyncService packetSyncService;

	@Autowired
	PreregSyncService pss;

	@Autowired
	APIRequestUtil apiUtil;

	@Autowired
	CryptoUtil cryptoUtil;

	@Autowired
	private JobScheduler jobScheduler;

	@Autowired
	PacketJobService packetJobService;

	@Autowired
	ContextUtils contextUtils;

	@Value("${mosip.test.baseurl}")
	private String baseUrl;

	public TestDataController(@Lazy PacketSyncService packetSyncService, @Lazy PacketMakerService pkm,
			@Lazy PacketJobService packetJobService) {
		this.packetSyncService = packetSyncService;
		this.pkm = pkm;
		this.packetJobService = packetJobService;
	}

	@Operation(summary = "Initialize the server context")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successfully created the server context") })
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

	@Operation(summary = "Retrieve the server context")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successfully retrived the server context") })
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

	@Operation(summary = "Create the packet for the context")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successfully created the packet") })
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

	/*
	 * @Operation(summary = "Get the API test data")
	 * 
	 * @ApiResponses(value = { @ApiResponse(responseCode = "200", description =
	 * "Successfully retrived the test data") })
	 * 
	 * @GetMapping(value = "/auth/{contextKey}") public @ResponseBody String
	 * getAPITestData(@PathVariable("contextKey") String contextKey) { return
	 * String.valueOf(apiUtil.initToken(contextKey)); }
	 */

	@Operation(summary = "Clear the token")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Token is cleared successfully") })
	@GetMapping(value = "/clearToken/{contextKey}")
	public @ResponseBody String ClearToken(@PathVariable("contextKey") String contextKey) {
		VariableManager.setVariableValue(contextKey, "urlSwitched", true);
		return "Success";
		// return String.valueOf(apiUtil.initToken());
	}

	@Operation(summary = "Sync the pre-registration data")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successfully synced the pre-registration data") })
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

	@Operation(summary = "Get the pre-registration data")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successfully retrived the pre-registration data") })
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

	@Operation(summary = "Encrypt the data")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successfully encrypted the data") })
	@GetMapping(value = "/encrypt/{contextKey}")
	public @ResponseBody String encryptData(@PathVariable("contextKey") String contextKey) throws Exception {
		return Base64.getUrlEncoder().encodeToString(cryptoUtil.encrypt("test".getBytes(), "referenceId", contextKey));
	}

	@Operation(summary = "Synchronize the packet")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "RID synced successfully") })
	@PostMapping(value = "/ridsync/{contextKey}")
	public @ResponseBody String syncRid(@RequestBody SyncRidDto syncRidDto,
			@PathVariable("contextKey") String contextKey) throws Exception {

		return packetSyncService.syncPacketRid(syncRidDto.getContainerPath(), syncRidDto.getName(),
				syncRidDto.getSupervisorStatus(), syncRidDto.getSupervisorComment(), syncRidDto.getProcess(),
				contextKey, syncRidDto.getAdditionalInfoReqId());
	}

	@Operation(summary = "RID sync request response")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "RID successfully synced with request response") })
	@PostMapping(value = "/ridsyncreq/{contextKey}")
	public @ResponseBody RidSyncReqResponseDTO syncRidRequest(@RequestBody RidSyncReqRequestDto syncRidDto,
			@PathVariable("contextKey") String contextKey) throws Exception {

		return packetSyncService.syncPacketRidRequest(syncRidDto.getContainerPath(), syncRidDto.getName(),
				syncRidDto.getSupervisorStatus(), syncRidDto.getSupervisorComment(), syncRidDto.getProcess(),
				contextKey, syncRidDto.getAdditionalInfoReqId());
	}

	@Operation(summary = "Sync the packet")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successfully synced the packet") })
	@PostMapping(value = "/packetsync/{contextKey}")
	public @ResponseBody String packetsync(@RequestBody PreRegisterRequestDto path,
			@PathVariable("contextKey") String contextKey) throws Exception {
		try {
			return packetSyncService.uploadPacket(path.getPersonaFilePath().get(0), contextKey);
		} catch (Exception e) {
			// We need to explicitly catch the exception to handle negative scenarios ,
			// where packet sync is expected to fail
			return e.getMessage();
		}
	}

	/*
	 * @Operation(summary = "Start the job")
	 * 
	 * @ApiResponses(value = { @ApiResponse(responseCode = "200", description =
	 * "Job is started successfully") })
	 * 
	 * @GetMapping(value = "/startjob/{contextKey}") public @ResponseBody String
	 * startJob(@PathVariable("contextKey") String contextKey) { String response =
	 * jobScheduler.scheduleRecurrently(() -> packetJobService.execute(contextKey),
	 * Cron.every5minutes()); return response; }
	 */

	@Operation(summary = "Make the packet and sync it")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successfully retrived the packet and synced") })
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

	@Operation(summary = "Generate the resident data")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successfully generated the resident data") })
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
			RestClient.logInfo(contextKey, "personaAnguliPath =" + DataProviderConstants.ANGULI_PATH);

			RestClient.logInfo(contextKey, "Resource Path=" + DataProviderConstants.RESOURCE);
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

	@Operation(summary = "Update the resident data")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Resident data is successfully updated") })
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
			return packetSyncService.updateResidentData(personaRequestDto.getRequests(), uin, rid, contextKey);

		} catch (Exception ex) {
			logger.error("registerResident", ex);
		}
		return "{Failed}";

	}

	@Operation(summary = "Pre-registering the resident")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successfully pre-registered the resident") })
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
	@Operation(summary = "Requesting the OTP")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OTP requested successfully") })
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

	@Operation(summary = "Verifying the OTP")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OTP verified successfully") })
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
	@Operation(summary = "Booking the Appointment for a given pre-registration-Id")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Appointment booked successfully") })
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

	@Operation(summary = "Uploading the document for a given pre-registration-Id")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Document uploaded successfully") })
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
	@Operation(summary = "Sync and upload the packet")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successfully registered for pre-registration") })
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

			RestClient.logInfo(contextKey, "packet-Sync: personaPath=" + (personaPath == null ? "N/A" : personaPath));
			RestClient.logInfo(contextKey,
					"packet-Sync: TemplatePath=" + preRegisterRequestDto.getPersonaFilePath().get(0));

			return packetSyncService.preRegToRegister(preRegisterRequestDto.getPersonaFilePath().get(0), preregId,
					personaPath, contextKey, preRegisterRequestDto.getAdditionalInfoReqId(), getRidFromSync,
					genarateValidCbeff);

		} catch (Exception ex) {
			logger.error("createPacket", ex);
			return ex.getMessage();
		}
	}

	@Operation(summary = "Delete Booking appointment for a given pre-registration-Id")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successfully deleted the appointment") })
	@DeleteMapping(value = "/preregistration/v1/applications/appointment/{contextKey}")
	public @ResponseBody String deleteAppointment(@RequestParam(name = "preRegistrationId") String preregId,
			@PathVariable("contextKey") String contextKey) {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("preRegistrationId", preregId);
		return packetSyncService.discardBooking(map, contextKey);

	}

	@Operation(summary = "Update appointment for a given PreRegID")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successfully updated the appointment") })
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

	@Operation(summary = "Discard Applications for a given pre-registration-Id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successfully discarded the application") })
	@DeleteMapping(value = "/preregistration/v1/applications/{preregid}/{contextKey}")
	public @ResponseBody String discardApplication(@PathVariable("preregid") String preregId,
			@PathVariable("contextKey") String contextKey) {

		return packetSyncService.deleteApplication(preregId, contextKey);

	}

}
