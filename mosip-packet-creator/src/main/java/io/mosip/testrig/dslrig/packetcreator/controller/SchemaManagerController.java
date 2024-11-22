package io.mosip.testrig.dslrig.packetcreator.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.testrig.dslrig.dataprovider.util.DataProviderConstants;
import io.mosip.testrig.dslrig.packetcreator.service.SchemaManagerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "SchemaManagerController", description = "REST API for managing IDschemas")
public class SchemaManagerController {

	@Value("${mosip.test.persona.configpath}")
	private String personaConfigPath;

	@Autowired
	SchemaManagerService schemaManagerService;

	private static final Logger logger = LoggerFactory.getLogger(SchemaManagerController.class);

	@Operation(summary = "Modify the schema")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successfully modified the schema") })
	@PutMapping(value = "/schema/{id}/{contextKey}")
	public @ResponseBody String modifySchema(@PathVariable("id") String id,
			@RequestParam(value = "version", defaultValue = "1") String version,
			@PathVariable("contextKey") String contextKey) {

		try {
			if (personaConfigPath != null && !personaConfigPath.equals("")) {
				DataProviderConstants.RESOURCE = personaConfigPath;
			}
			return schemaManagerService.modifySchema(1, id, contextKey);
		} catch (Exception ex) {
			logger.error("modifySchema", ex);
		}

		return "{\"Failed\"}";
	}

	@Operation(summary = "Get the schema")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successfully retrived the schema") })
	@GetMapping(value = "/schema/{contextKey}")
	public @ResponseBody String getSchema(@PathVariable("contextKey") String contextKey) {

		int i;
		String schema;
		try {
			if (personaConfigPath != null && !personaConfigPath.equals("")) {
				DataProviderConstants.RESOURCE = personaConfigPath;
			}

			schema = schemaManagerService.getSchema(contextKey);
			return schema;

		} catch (Exception e) {
			logger.error("getSchema", e);
			return "{\"Failed\"}";
		}
	}

}
