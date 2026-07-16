package io.mosip.testrig.dslrig.packetcreator.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.testrig.dslrig.dataprovider.util.DataProviderConstants;
import io.mosip.testrig.dslrig.dataprovider.util.ReadEmail;
import io.mosip.testrig.dslrig.dataprovider.util.ServiceException;
import io.mosip.testrig.dslrig.packetcreator.service.ResidentService;
import io.mosip.testrig.dslrig.packetcreator.openapi.OpenApiDocumentation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "ResidentController", description = "Resident service helpers: RID status, UIN lookup, card download, processing stages.")
@OpenApiDocumentation.StandardErrorResponses
public class ResidentController {

	private static final Logger logger = LoggerFactory.getLogger(ResidentController.class);

	@Autowired
	ResidentService residentService;

	@Value("${mosip.test.persona.configpath}")
	private String personaConfigPath;

	@Operation(summary = "Get the RID status from the resident")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successfully retrived the RID status") })
	@GetMapping(value = "/resident/status/{rid}/{contextKey}")
	public @ResponseBody String getRIDStatus(@PathVariable("rid") String rid,
			@PathVariable("contextKey") String contextKey) {

		if (personaConfigPath != null && !personaConfigPath.equals("")) {
			DataProviderConstants.setResource(personaConfigPath);
		}
		try {
			return residentService.getRIDStatus(rid, contextKey);
		} catch (ServiceException se) {
			throw se;
		} catch (Exception e) {
			logger.error("getRIDStatus", e);
			throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "GET_RID_STATUS_FAIL", null, e, e.getMessage());
		}
	}

	@Operation(summary = "Get the UIN with respect to RID from resident")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successfully retrived the UIN with respect to RID") })
	@GetMapping(value = "/resident/uin/{rid}/{contextKey}")
	public @ResponseBody String getUINByRid(@PathVariable("rid") String rid,
			@PathVariable("contextKey") String contextKey) {
		String err = "{\"Status\": \"Failed\",\"Error\":\"%s\"}";

		if (personaConfigPath != null && !personaConfigPath.equals("")) {
			DataProviderConstants.setResource(personaConfigPath);
		}

		try {
			return residentService.getUINByRID(rid, contextKey);
		} catch (ServiceException se) {
			throw se;
		} catch (Exception e) {
			logger.error("getUINByRid", e);
			throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "GET_UIN_BY_RID_FAIL", null, e, e.getMessage());
		}
	}


	@Operation(
			summary = "Download card for a UIN",
			description = "Calls resident service to download the physical/digital card PDF for the given UIN using the persona file at the path in the request body.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Download result or file path as JSON/plain string") })
	@OpenApiDocumentation.PersonaPathRequestBody
	@PostMapping(value = "/resident/card/{uin}/{contextKey}")
	public @ResponseBody String downloadCard(@RequestBody String personaPath, @PathVariable("uin") String uin,
			@PathVariable("contextKey") String contextKey) {
		String err = "{\"Status\": \"Failed\",\"Error\":\"%s\"}";

		if (personaConfigPath != null && !personaConfigPath.equals("")) {
			DataProviderConstants.setResource(personaConfigPath);
		}

		try {
			return residentService.downloadCard(personaPath, uin, contextKey);
		} catch (ServiceException se) {
			throw se;
		} catch (Exception e) {
			logger.error("downloadCard", e);
			throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "DOWNLOAD_CARD_FAIL", null, e, e.getMessage());
		}


	}

	@Operation(summary = "Get the additional information od the registration id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successfully retrived the reg id informations") })
	@GetMapping(value = "/resident/additionalReqId/{contextKey}")
	public @ResponseBody String getAdditionalInfoReqId(@PathVariable("contextKey") String contextKey) {

		if (personaConfigPath != null && !personaConfigPath.equals("")) {
			DataProviderConstants.setResource(personaConfigPath);
		}
		try {
			List<String> getadditionalInfoReqIds = ReadEmail.getadditionalInfoReqIds();
			if (!getadditionalInfoReqIds.isEmpty() && getadditionalInfoReqIds.size() > 0)
				return getadditionalInfoReqIds.get(0);
		} catch (ServiceException se) {
			throw se;
		} catch (Exception e) {
			logger.error("AdditionalRequestId", e);
			throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "GET_ADDITIONAL_REQ_ID_FAIL", null, e, e.getMessage());
		}
		return "{Failed}";
	}


	@Operation(summary = "Get the stages with respect to the RID")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successfully retrived the stages for the RID") })
	@GetMapping(value = "/resident/stages/{rid}/{contextKey}")
	public @ResponseBody String getStagesByRid(@PathVariable("rid") String rid,
			@PathVariable("contextKey") String contextKey) {
		String r = null;
		if (personaConfigPath != null && !personaConfigPath.equals("")) {
			DataProviderConstants.setResource(personaConfigPath);
		}

		try {
			return residentService.getStagesByRID(rid, contextKey).getBody().asString();
		} catch (ServiceException se) {
			throw se;
		} catch (Exception e) {
			logger.error("getStagesByRid", e);
			throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "GET_STAGES_BY_RID_FAIL", null, e, e.getMessage());

		}
	}

}
