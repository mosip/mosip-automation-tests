package io.mosip.testrig.dslrig.packetcreator.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.testrig.dslrig.dataprovider.preparation.RunCacheService;
import io.mosip.testrig.dslrig.dataprovider.util.DataProviderConstants;
import io.mosip.testrig.dslrig.dataprovider.util.ServiceException;
import io.mosip.testrig.dslrig.packetcreator.openapi.OpenApiDocumentation;
import io.mosip.testrig.dslrig.packetcreator.service.ContextResetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@OpenApiDocumentation.StandardErrorResponses
@Tag(name = "RunCacheController", description = "Warm or clear per-run MOSIP API cache (masterdata, auth tokens).")
public class RunCacheController {

	private static final Logger logger = LoggerFactory.getLogger(RunCacheController.class);

	@Value("${mosip.test.persona.configpath}")
	private String personaConfigPath;

	@Autowired
	private ContextResetService contextResetService;

	@Operation(summary = "Warm run-scoped cache",
			description = "Fetches auth tokens and frequently used masterdata once and stores them in the run cache "
					+ "({urlBase}run_context, MasterdataCache md:get:/md:entity: keys) for reuse across all scenarios. "
					+ "Call once per DSL context/worker before scenarios; pair with clear after the suite.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Cache warmed") })
	@PostMapping(value = "/runCache/warm/{contextKey}")
	public @ResponseBody ResponseEntity<Map<String, Object>> warmRunCache(@PathVariable("contextKey") String contextKey) {
		try {
			if (personaConfigPath != null && !personaConfigPath.isEmpty()) {
				DataProviderConstants.setResource(personaConfigPath);
			}
			Map<String, Object> summary = RunCacheService.warm(contextKey);
			return ResponseEntity.ok(summary);
		} catch (ServiceException se) {
			throw se;
		} catch (Exception ex) {
			logger.error("warmRunCache", ex);
			throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "WARM_RUN_CACHE_FAIL", null, ex);
		}
	}

	@Operation(summary = "Clear run-scoped cache",
			description = "Clears the {urlBase}run_context namespace and in-memory auth tokens (shared with resetContextData). "
					+ "Does not delete the context namespace or packet temp folders. Call after the test suite completes.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Cache cleared") })
	@PostMapping(value = "/runCache/clear/{contextKey}")
	public @ResponseBody String clearRunCache(@PathVariable("contextKey") String contextKey) {
		try {
			contextResetService.clearRunScopedCache(contextKey);
			return "true";
		} catch (ServiceException se) {
			throw se;
		} catch (Exception ex) {
			logger.error("clearRunCache", ex);
			throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "CLEAR_RUN_CACHE_FAIL", null, ex);
		}
	}
}
