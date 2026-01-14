package io.mosip.testrig.dslrig.packetcreator.controller;


import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.testrig.apirig.utils.ErrorCodes;
import io.mosip.testrig.dslrig.dataprovider.util.DataProviderConstants;
import io.mosip.testrig.dslrig.dataprovider.util.RestClient;
import io.mosip.testrig.dslrig.dataprovider.util.ServiceException;
import io.mosip.testrig.dslrig.packetcreator.dto.ExternalPacketRequestDTO;
import io.mosip.testrig.dslrig.packetcreator.dto.PacketCreateDto;
import io.mosip.testrig.dslrig.packetcreator.dto.PacketReprocessDto;
import io.mosip.testrig.dslrig.packetcreator.dto.PreRegisterRequestDto;
import io.mosip.testrig.dslrig.packetcreator.dto.RidSyncReqRequestDto;
import io.mosip.testrig.dslrig.packetcreator.dto.RidSyncReqResponseDTO;
import io.mosip.testrig.dslrig.packetcreator.dto.SyncRidDto;
import io.mosip.testrig.dslrig.packetcreator.service.ContextUtils;
import io.mosip.testrig.dslrig.packetcreator.service.PacketMakerService;
import io.mosip.testrig.dslrig.packetcreator.service.PacketSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "PacketController", description = "REST APIs for Packet processing")
public class PacketController {

    private static final Logger logger = LoggerFactory.getLogger(PacketController.class);
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

		} catch (ServiceException se) {
            throw se; // let global exception handler process it
        } catch (Exception ex) {
            logger.error("createPacket", ex);
            throw new ServiceException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "CREATE_PACKET_FROM_TEMPLATE_FAIL",
                    ex.getMessage()
            );
        }
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

		} catch (ServiceException se) {
            throw se;
        } catch (Exception ex) {
            logger.error("createTemplate", ex);
            throw new ServiceException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "CREATE_TEMPLATE_FAIL",
                    ex.getMessage()
            );
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

		} catch (ServiceException se) {
            throw se;
        } catch (Exception ex) {
            logger.error("bulkUploadPackets", ex);
            throw new ServiceException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "BULK_UPLOAD_PACKETS_FAIL",
                    ex.getMessage()
            );
        }
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

		} catch (ServiceException se) {
            throw se;
        } catch (Exception ex) {
            logger.error("makePacketAndSync", ex);
            throw new ServiceException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "MAKE_PACKET_AND_SYNC_FAIL",
                    ex.getMessage()
            );
        }
	}
	
	@Operation(summary = "Create the packet for the context")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successfully created the packet") })
	@PostMapping(value = "/packetcreator/{contextKey}")
	public @ResponseBody String createPacket(@RequestBody PacketCreateDto packetCreateDto,
			@PathVariable("contextKey") String contextKey) {
		try {
			return packetMakerService.createContainer(packetCreateDto.getIdJsonPath(), packetCreateDto.getTemplatePath(),
					packetCreateDto.getSource(), packetCreateDto.getProcess(), null, contextKey, true,
					packetCreateDto.getAdditionalInfoReqId() ,null);
		} catch (ServiceException se) {
            throw se;
        } catch (Exception ex) {
            logger.error("createPacket", ex);
            throw new ServiceException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "CREATE_PACKET_CONTAINER_FAIL",
                    ex.getMessage()
            );
        }
	}

	@Operation(summary = "Fetch the tags related to the packet")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successfully retrieved the tags of the packet") })
	@GetMapping(value = "/packet/getTags/{contextKey}")
	public @ResponseBody String getPacketTags(@PathVariable("contextKey") String contextKey) {
		try {
			return packetSyncService.getPacketTags(contextKey);

		} catch (ServiceException se) {
            throw se;
        } catch (Exception ex) {
            logger.error("getPacketTags", ex);
            throw new ServiceException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "GET_PACKET_TAGS_FAIL",
                    ex.getMessage()
            );
        }
		
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
		} catch (ServiceException se) {
            throw se;
        } catch (Exception e) {
            // We need to explicitly catch the exception to handle negative scenarios ,
            // where packet sync is expected to fail
            logger.error("packetsync", e);
            throw new ServiceException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "PACKET_SYNC_FAIL",
                    e.getMessage()
            );
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

		} catch (ServiceException se) {
            throw se;
        } catch (Exception ex) {
            logger.error("preRegToRegister", ex);
            throw new ServiceException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "PRE_REG_TO_REGISTER_FAIL",
                    ex.getMessage()
            );
        }
	}
	
	@Operation(summary = "Reprocess the packet")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successfully Reprocessed the packet") })
	@PostMapping(value = "/packet/reprocess/{contextKey}")
	public @ResponseBody String packetReprocess(@RequestBody PacketReprocessDto requestDto,
			@PathVariable("contextKey") String contextKey) throws Exception {
		try {
			return packetSyncService.reprocessPacket(requestDto.getRID() ,requestDto.getWorkflowInstanceId(), contextKey);

		} catch (ServiceException se) {
            throw se;
        } catch (Exception ex) {
            logger.error("packetReprocess", ex);
            throw new ServiceException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "PACKET_REPROCESS_FAIL",
                    ex.getMessage()
            );
        }
	}
	
	@Operation(summary = "Create the external packet and upload")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "External packet and upload successfully") })
	@PostMapping(value = "/packetmanager/createPacket/{process}/{rid}/{introducerInfoToken}/{contextKey}")
	public @ResponseBody String createCRVSPacket(@RequestBody ExternalPacketRequestDTO requestDto,
			@PathVariable("process") String process,
			@PathVariable("rid") String rid,
			@PathVariable("introducerInfoToken") boolean validateToken,
			@PathVariable("contextKey") String contextKey) {

		try {
			if (personaConfigPath != null && !personaConfigPath.equals("")) {
				DataProviderConstants.RESOURCE = personaConfigPath;
			}

			return packetSyncService.createPacketUpload(requestDto.getPersonaFilePath(),requestDto.getSource(), process, requestDto.getUin(), rid,
					validateToken,contextKey);

		} catch (ServiceException se) {
            throw se;
        } catch (Exception ex) {
            logger.error("createCRVSPacket", ex);
            throw new ServiceException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "CREATE_CRVSPACKET_FAIL",
                    ex.getMessage()
            );
        }
	}

	@Operation(summary = "sync the external packet")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "sync the external packet successfully") })
	@PostMapping(value = "/sync/externalPacket/{rid}/{contextKey}")
	public @ResponseBody String syncCRVSPacket(@PathVariable("rid") String rid,
			@PathVariable("contextKey") String contextKey) {

		try {
			if (personaConfigPath != null && !personaConfigPath.equals("")) {
				DataProviderConstants.RESOURCE = personaConfigPath;
			}

			return packetSyncService.syncAndUpload(rid, contextKey);

		} catch (ServiceException se) {
            throw se;
        } catch (Exception ex) {
            logger.error("syncCRVSPacket", ex);
            throw new ServiceException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "SYNC_CRVSPACKET_FAIL",
                    ex.getMessage()
            );
        }
	}
	
	@Operation(summary = "delete the packet template and resident data")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successfully synced the packet") })
	@DeleteMapping(value = "/delete/packetdata/{contextKey}")
	public @ResponseBody String deletePacketData(@PathVariable("contextKey") String contextKey) throws Exception {
		try {
			return ContextUtils.clearPacketGenFolders(contextKey);
		} catch (ServiceException se) {
            throw se;
        } catch (Exception e) {
            logger.error("deletePacketData", e);
            throw new ServiceException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "DELETE_PACKET_DATA_FAIL",
                    e.getMessage()
            );
        }

    }

}