package io.mosip.testrig.dslrig.packetcreator.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.testrig.dslrig.dataprovider.util.DataProviderConstants;
import io.mosip.testrig.dslrig.packetcreator.dto.SelfRegisterDto;
import io.mosip.testrig.dslrig.packetcreator.service.PartnerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "PartnerManagerController", description = "REST API for partner controls")
@RestController
public class PartnerManagerController {

	@Value("${mosip.test.partnerManager.configpath}")
	private String partnerConfigPath;

	@Autowired
	PartnerService partnerService;

	private static final Logger logger = LoggerFactory.getLogger(PartnerManagerController.class);

	@Operation(summary = "Self registration in patner management service")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Self registration is successfully") })
	@PostMapping(value = "/partners/{contextKey}")
	public @ResponseBody String selfRegisterPartner(@RequestBody SelfRegisterDto selfRegister,
			@PathVariable("contextKey") String contextKey) {
		try {
			if (partnerConfigPath != null && !partnerConfigPath.equals("")) {
				DataProviderConstants.RESOURCE = partnerConfigPath;
			}
			return partnerService.selfRegister(selfRegister, contextKey);
		} catch (Exception e) {
			logger.error("selfRegisterPartner", e);
			return "{\"falied\"}";
		}
	}

	@Operation(summary = "Updating the patner status in patner management service")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successfully updated the partner status") })
	@PostMapping(value = "/partners/{partnerId}/{contextKey}")
	public @ResponseBody String updatePartnerStatus(@PathVariable(value = "partnerId") String partnerId,
			@RequestParam(value = "status") String status, @PathVariable("contextKey") String contextKey) {
		try {
			if (partnerConfigPath != null && !partnerConfigPath.equals("")) {
				DataProviderConstants.RESOURCE = partnerConfigPath;
			}
			return partnerService.updatePartnerStatus(contextKey, partnerId, status);
		} catch (Exception e) {
			logger.error("updatePartnerStatus", e);
			return "{\"falied\"}";
		}
	}

	@Operation(summary = "Submiting API request in patner management service")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "API request is successfully") })
	@PostMapping(value = "/partner/APIRequest/submit/{contextKey}")
	public @ResponseBody String submitAPIRequest(@RequestParam(value = "PartnerID") String partnerID,
			@RequestParam(value = "PolicyName") String policyName,
			@RequestParam(value = "useCaseDescription") String useCaseDesc,
			@PathVariable("contextKey") String contextKey) {
		try {
			if (partnerConfigPath != null && !partnerConfigPath.equals("")) {
				DataProviderConstants.RESOURCE = partnerConfigPath;
			}
			return partnerService.submitPartnerAPIKeyRequest(contextKey, partnerID, policyName, useCaseDesc);
		} catch (Exception e) {
			logger.error("submitApiRequest", e);
			return "{\"falied\"}";
		}
	}

	@Operation(summary = "Approving API request in patner management service")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "API request is approved successfully") })
	@PostMapping(value = "/partner/APIRequest/approve/{contextKey}")
	public @ResponseBody String approveAPIRequest(@RequestParam(value = "APIReqeustID") String ID,
			@PathVariable("contextKey") String contextKey) {

		try {
			if (partnerConfigPath != null && !partnerConfigPath.equals("")) {
				DataProviderConstants.RESOURCE = partnerConfigPath;
			}
			return partnerService.approvePartnerAPIKeyRequest(contextKey, ID);
		} catch (Exception e) {
			logger.error("approveAPIRequest", e);
			return "{\"falied\"}";
		}
	}

}
