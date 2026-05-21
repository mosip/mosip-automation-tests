package io.mosip.testrig.dslrig.packetcreator.controller;

import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.testrig.apirig.utils.ErrorCodes;
import io.mosip.testrig.dslrig.dataprovider.util.ServiceException;
import io.mosip.testrig.dslrig.packetcreator.service.CommandsService;
import io.mosip.testrig.dslrig.packetcreator.openapi.OpenApiDocumentation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@OpenApiDocumentation.StandardErrorResponses
@Tag(name = "CommandsController", description = "File write utilities and registration-machine private key generation for DSL scenarios.")
public class CommandsController {
	private static final Logger logger = LoggerFactory.getLogger(CommandsController.class);

	@Autowired
	CommandsService commandsService;


	@PostMapping("/writeFile/{offset}/{contextKey}")
	@Operation(summary = "Creating the file")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "File created successfully") })
	public @ResponseBody String writeToFile(

			@PathVariable("offset") long offset, @RequestBody Properties reqestData,
			@PathVariable("contextKey") String contextKey) {
		try {
			return commandsService.writeToFile(contextKey, reqestData, offset);
		} catch (ServiceException se) {
			throw se; 
		} catch (IOException e) {
			logger.error("writeToFile", e);
			throw new ServiceException(
					HttpStatus.INTERNAL_SERVER_ERROR,
                    "WRITE_TO_FILE_FAIL",
                    e.getMessage()
			);
		}
	}

	@GetMapping(value = "/generatekey/{machineId}/{contextKey}")
	@Operation(summary = "Generate private key based on machineId", description = "Generate a private key based on the provided machine ID.", responses = {
			@ApiResponse(responseCode = "200", description = "Private key generated successfully") })
	public String generatekey(@PathVariable String machineId, @PathVariable("contextKey") String contextKey) {
		try {
			return commandsService.generatekey(contextKey, machineId);
		} catch (ServiceException se) {
			throw se; 
		} catch (Exception e) {
			logger.error("generatekey", e);
			throw new ServiceException(
					HttpStatus.INTERNAL_SERVER_ERROR,
                    "GENERATE_KEY_FAIL",
                    e.getMessage()
			);
		}
	}

}
