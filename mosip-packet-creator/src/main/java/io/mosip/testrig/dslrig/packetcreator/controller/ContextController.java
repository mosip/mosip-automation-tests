package io.mosip.testrig.dslrig.packetcreator.controller;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.testrig.apirig.utils.ErrorCodes;
import io.mosip.testrig.dslrig.dataprovider.util.DataProviderConstants;
import io.mosip.testrig.dslrig.dataprovider.util.ServiceException;
import io.mosip.testrig.dslrig.dataprovider.variables.VariableManager;
import io.mosip.testrig.dslrig.packetcreator.service.CommandsService;
import io.mosip.testrig.dslrig.packetcreator.service.ContextUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "ContextController", description = "REST APIs for context management")
public class ContextController {

	@Autowired
	ContextUtils contextUtils;
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
	        throw se; // let global exception handler process it
	    } catch (Exception ex) {
	        logger.error("createServerContext", ex);
	        throw new ServiceException(
	                HttpStatus.INTERNAL_SERVER_ERROR,
	                "CREATE_SERVER_CONTEXT_FAIL",
	                ex.getMessage()
	        );
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
            throw se; // let global exception handler process it
        } catch (Exception ex) {
            logger.error("checkContext", ex);
            throw new ServiceException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "CHECK_CONTEXT_FAIL",
                    ex.getMessage()
            );
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
            throw se; // let global exception handler process it
        } catch (Exception ex) {
            logger.error("resetContextData", ex);
            throw new ServiceException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "RESET_CONTEXT_FAIL",
                    ex.getMessage()
            );
        }
        return bRet;
    }

    @Operation(summary = "Reset the server context data")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Context data reset successfully") })
    @GetMapping(value = "/resetContextData/{contextKey}")
    public @ResponseBody String resetContextData(@PathVariable("contextKey") String contextKey) {
        try {
            return VariableManager.deleteNameSpace(
                    VariableManager.getVariableValue(contextKey, "urlBase").toString() + "run_context");
        } catch (ServiceException se) {
            throw se; // let global exception handler process it
        } catch (Exception ex) {
            logger.error("resetContextData", ex);
            throw new ServiceException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "RESET_CONTEXT_FAIL",
                    ex.getMessage()
            );
        }
    }
}