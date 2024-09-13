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
import io.mosip.testrig.dslrig.dataprovider.variables.VariableManager;
import io.mosip.testrig.dslrig.packetcreator.dto.PreRegisterRequestDto;
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
	@Operation(summary = "Creating packet")
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

	@Operation(summary = "Packing the packet")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Packet packed successfully") })
	@PostMapping(value = "/packet/pack/{contextKey}")
	public @ResponseBody String packPacket(@RequestBody PreRegisterRequestDto requestDto,
			@PathVariable("contextKey") String contextKey
	// @RequestParam(name="isValidChecksum",required = false) Boolean isValidcs
	) {

		try {
			if (personaConfigPath != null && !personaConfigPath.equals("")) {
				DataProviderConstants.RESOURCE = personaConfigPath;
			}
			boolean isValidChecksum = true;
			/*
			 * if(isValidcs != null) isValidChecksum = isValidcs;
			 */
			return packetMakerService.packPacketContainer(requestDto.getPersonaFilePath().get(0), null, null,
					contextKey, isValidChecksum);
			// return packetSyncService.createPackets(requestDto.,process,null, contextKey);

		} catch (Exception ex) {
			logger.error("createPackets", ex);
		}
		return "{\"Failed\"}";
	}

	@Operation(summary = "Create the CBEFF template for packet processing")
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

	@Operation(summary = "Bulk uploading of the packets")
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

	@Operation(summary = "Get the tags of the packet")
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

	@Operation(summary = "Get the device certificate cache")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Validation successful") })
	@GetMapping(value = "/clearDeviceCertCache/{contextKey}")
	public @ResponseBody String clearDeviceCertCache(@PathVariable("contextKey") String contextKey) {
		try {
			Path p12path = null;
			String certsDir = System.getenv(BiometricDataProvider.AUTHCERTSPATH) == null
					? VariableManager.getVariableValue(contextKey, BiometricDataProvider.AUTHCERTSPATH).toString()
					: System.getenv(BiometricDataProvider.AUTHCERTSPATH);

			if (certsDir == null || certsDir.length() == 0) {
				certsDir = System.getProperty("java.io.tmpdir") + File.separator + "AUTHCERTS";
			}

			p12path = Paths.get(certsDir, "DSL-IDA-" + VariableManager.getVariableValue(contextKey, "db-server"));

			SBIDeviceHelper.evictKeys(p12path.toString());
			return "{\"Success\"}";
		} catch (Exception ex) {
			logger.error("Clear device certificate cache ", ex);
		}
		return "{\"Failed\"}";
	}

	@Operation(summary = "Validate Identity Object as per ID Schema")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Validation successful") })
	@PostMapping(value = "/packet/validate/{process}/{contextKey}")
	public @ResponseBody String validatePacket(@RequestBody PreRegisterRequestDto requestDto,
			@PathVariable("process") String process, @PathVariable("contextKey") String contextKey) {

		try {
			if (personaConfigPath != null && !personaConfigPath.equals("")) {
				DataProviderConstants.RESOURCE = personaConfigPath;
			}

			return packetSyncService.validatePacket(requestDto.getPersonaFilePath().get(0), process, contextKey);

		} catch (Exception ex) {
			logger.error("validatePacket", ex);
		}
		return "{\"Failed\"}";

	}

}
