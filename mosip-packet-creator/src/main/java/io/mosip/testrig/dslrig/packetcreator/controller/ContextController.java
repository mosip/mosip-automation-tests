package io.mosip.testrig.dslrig.packetcreator.controller;


import java.util.List;

import java.util.Map;

import java.util.Properties;


import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.http.HttpStatus;

import org.springframework.http.MediaType;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.RequestBody;

import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.bind.annotation.ResponseBody;

import org.springframework.web.bind.annotation.RestController;


import io.mosip.testrig.dslrig.dataprovider.util.DataProviderConstants;

import io.mosip.testrig.dslrig.dataprovider.util.ServiceException;

import io.mosip.testrig.dslrig.dataprovider.util.internalapi.InternalApiLogCollector;

import io.mosip.testrig.dslrig.dataprovider.util.internalapi.InternalApiLogExchange;

import io.mosip.testrig.dslrig.dataprovider.util.internalapi.InternalApiLogFormatter;

import io.mosip.testrig.dslrig.dataprovider.variables.VariableManager;

import io.mosip.testrig.dslrig.packetcreator.service.CommandsService;

import io.mosip.testrig.dslrig.packetcreator.service.ContextResetService;

import io.mosip.testrig.dslrig.packetcreator.service.ContextUtils;

import io.mosip.testrig.dslrig.packetcreator.openapi.OpenApiDocumentation;

import io.swagger.v3.oas.annotations.Operation;

import io.swagger.v3.oas.annotations.responses.ApiResponse;

import io.swagger.v3.oas.annotations.responses.ApiResponses;

import io.swagger.v3.oas.annotations.tags.Tag;


@RestController

@OpenApiDocumentation.StandardErrorResponses

@Tag(name = "ContextController", description = "Initialize MOSIP test context variables, verify environment, reset data, and export internal API logs.")

public class ContextController {


	@Autowired

	ContextUtils contextUtils;

	@Autowired

	ContextResetService contextResetService;

	@Value("${mosip.test.persona.configpath}")

	private String personaConfigPath;


	@Autowired

	CommandsService commandsService;


	private static final Logger logger = LoggerFactory.getLogger(ContextController.class);


	@Operation(summary = "Initialize the server context")

	@ApiResponses(value = {

			@ApiResponse(responseCode = "200", description = "Successfully created the server context") })

	@PostMapping(value = "/context/server/{contextKey}")

	public ResponseEntity<?> createServerContext(@RequestBody Properties contextProperties,

	                                             @PathVariable("contextKey") String contextKey) {


	    logger.info("-------------------- Scenario : " 

	                + contextProperties.getProperty("scenario")

	                + " --------------------");


	    try {

	        if (personaConfigPath != null && !personaConfigPath.isEmpty()) {

	            DataProviderConstants.RESOURCE = personaConfigPath;

	        }


	        String result = contextUtils.createUpdateServerContext(contextProperties, contextKey);


	        return ResponseEntity.ok(

	                Map.of(

	                    "success", result,

	                    "context", contextKey,

	                    "message", "Server context created"

	                )

	        );


	    } catch (ServiceException se) {

	        throw se; 

	    } catch (Exception ex) {

	        logger.error("createServerContext", ex);

			throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "CREATE_SERVER_CONTEXT_FAIL", null, ex);

	    }

	}


	@Operation(summary = "Fetch id-repository identity-service actuator info",
			description = "Calls {targetBaseUrl}/idrepository/v1/identity/actuator/info and returns the actuator JSON "
					+ "(including build.version) for Java 11 vs Java 21 environment detection in the DSL before-suite.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Actuator info retrieved successfully") })
	@GetMapping("/env/idrepoActuatorInfo/{contextKey}")
	public @ResponseBody String getIdRepoActuatorInfo(@PathVariable("contextKey") String contextKey,
			@RequestParam(name = "targetBaseUrl", required = false) String targetBaseUrl) {
		try {
			return commandsService.getIdRepoActuatorInfo(contextKey, targetBaseUrl);
		} catch (ServiceException se) {
			throw se;
		}
	}

	@GetMapping("/ping/{eSignetDeployed}/{contextKey}")


	@Operation(summary = "Verify target environment", description = "Verify if the target environment (context) is available.", responses = {


			@ApiResponse(responseCode = "200", description = "Target environment verified successfully") })

	public @ResponseBody String checkContext(@RequestParam(name = "module", required = false) String module,


			@PathVariable String eSignetDeployed, @PathVariable("contextKey") String contextKey) {


		try {


			return commandsService.checkContext(contextKey, module, eSignetDeployed);


		} catch (ServiceException se) {

            throw se; 

        } catch (Exception ex) {

            logger.error("checkContext", ex);

			throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "CHECK_CONTEXT_FAIL", null, ex);

        }

    }


    @Operation(summary = "Retrieve the server context")

    @ApiResponses(value = {

            @ApiResponse(responseCode = "200", description = "Successfully retrieved the server context") })

    @GetMapping(value = "/context/server/{contextKey}")

    public @ResponseBody Properties getServerContext(@PathVariable("contextKey") String contextKey) {

        Properties bRet = null;

        try {

            bRet = contextUtils.loadServerContext(contextKey);

        }  catch (ServiceException se) {

            throw se; 

        } catch (Exception ex) {

            logger.error("getServerContext", ex);

            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "GET_SERVER_CONTEXT_FAIL", null, ex);

        }

        return bRet;

    }


    @Operation(summary = "Reset the server context data",
			description = "Full reset: clears packet temp folders, run-scoped MOSIP cache (same as /runCache/clear), "
					+ "and deletes the entire context variable namespace.")

    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Context data reset successfully") })

    @GetMapping(value = "/resetContextData/{contextKey}")

    public @ResponseBody String resetContextData(@PathVariable("contextKey") String contextKey) {

		try {

			contextResetService.resetContextData(contextKey);

			return "true";

		} catch (ServiceException se) {

            throw se; 

        } catch (Exception ex) {

            logger.error("resetContextData", ex);

            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "RESET_CONTEXT_FAIL", null, ex);


        }

    }


	@Operation(summary = "Retrieve formatted internal (outbound) API log for a context", description = "Used by the DSL orchestrator to attach outbound MOSIP traffic to TestNG reports. "

			+ "Optional reportHints=true embeds markers for bold labels in HTML reports. "

			+ "Does not log inbound DSL calls to Packet Creator.")

	@GetMapping(value = "/context/internalApiLogs/{contextKey}", produces = MediaType.TEXT_PLAIN_VALUE)

	public @ResponseBody String getInternalApiLogs(@PathVariable("contextKey") String contextKey,

			@RequestParam(name = "clear", defaultValue = "true") boolean clear,

			@RequestParam(name = "reportHints", defaultValue = "false") boolean reportHints) {

		List<InternalApiLogExchange> list = clear ? InternalApiLogCollector.drain(contextKey)

				: InternalApiLogCollector.snapshot(contextKey);

		return InternalApiLogFormatter.format(list, reportHints);

	}

}
