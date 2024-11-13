package io.mosip.testrig.dslrig.packetcreator.controller;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.mock.sbi.devicehelper.SBIDeviceHelper;
import io.mosip.testrig.dslrig.dataprovider.BiometricDataProvider;
import io.mosip.testrig.dslrig.dataprovider.util.DataProviderConstants;
import io.mosip.testrig.dslrig.dataprovider.util.RestClient;
import io.mosip.testrig.dslrig.dataprovider.variables.VariableManager;
import io.mosip.testrig.dslrig.packetcreator.dto.PacketCreateDto;
import io.mosip.testrig.dslrig.packetcreator.dto.PreRegisterRequestDto;
import io.mosip.testrig.dslrig.packetcreator.dto.RidSyncReqRequestDto;
import io.mosip.testrig.dslrig.packetcreator.dto.RidSyncReqResponseDTO;
import io.mosip.testrig.dslrig.packetcreator.dto.SyncRidDto;
import io.mosip.testrig.dslrig.packetcreator.service.PacketMakerService;
import io.mosip.testrig.dslrig.packetcreator.service.PacketSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "PacketController", description = "REST APIs for Packet processing")
public class PacketController {

	private static final Logger logger = LoggerFactory.getLogger(TestDataController.class);
	@Value("${mosip.test.persona.configpath}")
	private String personaConfigPath;

	private PacketSyncService packetSyncService;
	private PacketMakerService packetMakerService;

	public PacketController(@Lazy PacketSyncService packetSyncService, @Lazy PacketMakerService packetMakerService) {
		this.packetSyncService = packetSyncService;
		this.packetMakerService = packetMakerService;
	}

	/*
	 * Create a packet from Resident data for the target context requestDto may
	 * contain PersonaRequestType.PR_Options
	 */
	@Operation(summary = "Create a packet")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Packet created successfully") })
	@PostMapping(value = "/packet/create/{contextKey}")
	public @ResponseBody String createPacket(@RequestBody PreRegisterRequestDto requestDto,
			@PathVariable("contextKey") String contextKey) {

		try {
			if (personaConfigPath != null && !personaConfigPath.equals("")) {
				DataProviderConstants.RESOURCE = personaConfigPath;
			}

			return packetMakerService.createPacketFromTemplate(requestDto.getPersonaFilePath().get(0),
					requestDto.getPersonaFilePath().get(1), contextKey, requestDto.getAdditionalInfoReqId());
			// return packetSyncService.createPackets(requestDto.,process,null, contextKey);

		} catch (Exception ex) {
			logger.error("createPackets", ex);
		}
		return "{\"Failed\"}";
	}

	/*
	 * @Operation(summary = "Pack the packet")
	 * 
	 * @ApiResponses(value = { @ApiResponse(responseCode = "200", description =
	 * "Packet packed successfully") })
	 * 
	 * @PostMapping(value = "/packet/pack/{contextKey}") public @ResponseBody String
	 * packPacket(@RequestBody PreRegisterRequestDto requestDto,
	 * 
	 * @PathVariable("contextKey") String contextKey
	 * // @RequestParam(name="isValidChecksum",required = false) Boolean isValidcs )
	 * {
	 * 
	 * try { if (personaConfigPath != null && !personaConfigPath.equals("")) {
	 * DataProviderConstants.RESOURCE = personaConfigPath; } boolean isValidChecksum
	 * = true;
	 * 
	 * if(isValidcs != null) isValidChecksum = isValidcs;
	 * 
	 * return
	 * packetMakerService.packPacketContainer(requestDto.getPersonaFilePath().get(0)
	 * , null, null, contextKey, isValidChecksum); // return
	 * packetSyncService.createPackets(requestDto.,process,null, contextKey);
	 * 
	 * } catch (Exception ex) { logger.error("createPackets", ex); } return
	 * "{\"Failed\"}"; }
	 */

	@Operation(summary = "Create the packet template for synchronization and upload")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "CBEFF template created successfully") })
	@PostMapping(value = "/packet/template/{process}/{qualityScore}/{genarateValidCbeff}/{contextKey}")
	public @ResponseBody String createTemplate(@RequestBody PreRegisterRequestDto requestDto,
			@PathVariable("process") String process, @PathVariable("qualityScore") String qualityScore,
			@PathVariable("genarateValidCbeff") boolean genarateValidCbeff,
			@PathVariable("contextKey") String contextKey) {

		try {
			if (personaConfigPath != null && !personaConfigPath.equals("")) {
				DataProviderConstants.RESOURCE = personaConfigPath;
			}

			return packetSyncService.createPacketTemplates(requestDto.getPersonaFilePath(), process, null, null,
					contextKey, "Registration", qualityScore, genarateValidCbeff);

		} catch (Exception ex) {
			logger.error("createTemplate", ex);
			return "{\"" + ex.getMessage() + "\"}";
		}
	}

	@Operation(summary = "Bulk upload of packets")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Bulk upload of packet is successful") })
	@PostMapping(value = "/packet/bulkupload/{contextKey}")
	public @ResponseBody String bulkUploadPackets(@RequestBody List<String> packetPaths,
			@PathVariable("contextKey") String contextKey) {

		try {
			if (personaConfigPath != null && !personaConfigPath.equals("")) {
				DataProviderConstants.RESOURCE = personaConfigPath;
			}

			return packetSyncService.bulkuploadPackets(packetPaths, contextKey);

		} catch (Exception ex) {
			logger.error("createPackets", ex);
		}
		return "{\"Failed\"}";

	}
	
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
	
	@Operation(summary = "Create the packet for the context")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successfully created the packet") })
	@PostMapping(value = "/packetcreator/{contextKey}")
	public @ResponseBody String createPacket(@RequestBody PacketCreateDto packetCreateDto,
			@PathVariable("contextKey") String contextKey) {
		try {
			return packetMakerService.createContainer(packetCreateDto.getIdJsonPath(), packetCreateDto.getTemplatePath(),
					packetCreateDto.getSource(), packetCreateDto.getProcess(), null, contextKey, true,
					packetCreateDto.getAdditionalInfoReqId());
		} catch (Exception ex) {
			logger.error("", ex);
		}
		return "Failed!";
	}

	@Operation(summary = "Fetch the tags related to the packet")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successfully retrieved the tags of the packet") })
	@GetMapping(value = "/packet/getTags/{contextKey}")
	public @ResponseBody String getPacketTags(@PathVariable("contextKey") String contextKey) {
		try {
			return packetSyncService.getPacketTags(contextKey);

		} catch (Exception ex) {
			logger.error("get tags", ex);
		}
		return "{\"Failed\"}";
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

	/*
	 * @Operation(summary = "Validate Identity Object as per ID Schema")
	 * 
	 * @ApiResponses(value = { @ApiResponse(responseCode = "200", description =
	 * "Validation successful") })
	 * 
	 * @PostMapping(value = "/packet/validate/{process}/{contextKey}")
	 * public @ResponseBody String validatePacket(@RequestBody PreRegisterRequestDto
	 * requestDto,
	 * 
	 * @PathVariable("process") String process, @PathVariable("contextKey") String
	 * contextKey) {
	 * 
	 * try { if (personaConfigPath != null && !personaConfigPath.equals("")) {
	 * DataProviderConstants.RESOURCE = personaConfigPath; }
	 * 
	 * return
	 * packetSyncService.validatePacket(requestDto.getPersonaFilePath().get(0),
	 * process, contextKey);
	 * 
	 * } catch (Exception ex) { logger.error("validatePacket", ex); } return
	 * "{\"Failed\"}";
	 * 
	 * }
	 */

}