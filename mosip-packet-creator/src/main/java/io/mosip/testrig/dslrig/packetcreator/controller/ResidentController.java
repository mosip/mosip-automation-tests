package io.mosip.testrig.dslrig.packetcreator.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.testrig.dslrig.dataprovider.util.DataProviderConstants;
import io.mosip.testrig.dslrig.dataprovider.util.ReadEmail;
import io.mosip.testrig.dslrig.packetcreator.service.ResidentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "ResidentController", description = "REST APIs for Resident services")
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
			DataProviderConstants.RESOURCE = personaConfigPath;
		}
		try {
			return residentService.getRIDStatus(rid, contextKey);
		} catch (Exception e) {
			logger.error("getRIDStatus", e);
		}
		return "{Failed}";
	}

	@Operation(summary = "Get the UIN with respect to RID from resident")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successfully retrived the UIN with respect to RID") })
	@GetMapping(value = "/resident/uin/{rid}/{contextKey}")
	public @ResponseBody String getUINByRid(@PathVariable("rid") String rid,
			@PathVariable("contextKey") String contextKey) {
		String err = "{\"Status\": \"Failed\",\"Error\":\"%s\"}";

		if (personaConfigPath != null && !personaConfigPath.equals("")) {
			DataProviderConstants.RESOURCE = personaConfigPath;
		}

		try {
			return residentService.getUINByRID(rid, contextKey);
		} catch (Exception e) {
			logger.error("getUINByRid", e);
			err = String.format(err, e.getMessage());
		}
		return err;
	}

	// resident/v1/req/credential
	@Operation(summary = "download card for the UIN")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successfully downloaded the card for the UIN") })
	@PostMapping(value = "/resident/card/{uin}/{contextKey}")
	public @ResponseBody String downloadCard(@RequestBody String personaPath, @PathVariable("uin") String uin,
			@PathVariable("contextKey") String contextKey) {
		String err = "{\"Status\": \"Failed\",\"Error\":\"%s\"}";

		if (personaConfigPath != null && !personaConfigPath.equals("")) {
			DataProviderConstants.RESOURCE = personaConfigPath;
		}

		try {
			return residentService.downloadCard(personaPath, uin, contextKey);
		} catch (Exception e) {
			logger.error("getUINByRid", e);
			err = String.format(err, e.getMessage());
		}
		return err;

		/*
		 * { "id": "string", "request": { "additionalData": {}, "credentialType":
		 * "string", "encrypt": true, "encryptionKey": "string", "individualId":
		 * "string", "issuer": "string", "otp": "string", "recepiant": "string",
		 * "sharableAttributes": [ "string" ], "transactionID": "string", "user":
		 * "string" }, "requesttime": "string", "version": "string" }
		 * 
		 */

	}

	@Operation(summary = "Get the additional information od the registration id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successfully retrived the reg id informations") })
	@GetMapping(value = "/resident/additionalReqId/{contextKey}")
	public @ResponseBody String getAdditionalInfoReqId(@PathVariable("contextKey") String contextKey) {

		if (personaConfigPath != null && !personaConfigPath.equals("")) {
			DataProviderConstants.RESOURCE = personaConfigPath;
		}
		try {
			List<String> getadditionalInfoReqIds = ReadEmail.getadditionalInfoReqIds();
			if (!getadditionalInfoReqIds.isEmpty() && getadditionalInfoReqIds.size() > 0)
				return getadditionalInfoReqIds.get(0);
		} catch (Exception e) {
			logger.error("AdditionalRequestId", e);
		}
		return "{Failed}";
	}

	/*
	 * @GetMapping(value =
	 * "/resident/setThresholdValue/{qualityScore}/{contextKey}")
	 * public @ResponseBody String setThresholdValue(@PathVariable("qualityScore")
	 * String qualityScore,
	 * 
	 * @PathVariable("contextKey") String contextKey ) {
	 * 
	 * if (personaConfigPath != null && !personaConfigPath.equals("")) {
	 * DataProviderConstants.RESOURCE = personaConfigPath; } try { HashMap<String,
	 * Integer> port=BiometricDataProvider.portmap;
	 * 
	 * //client = new MDSClient(0); // //port --in MDS Admin api -- hit // {"type" :
	 * "Biometric Device","qualityScore": "20", "fromIso" : false} // POST URI -
	 * 127.0.0.1:4501/admin/score
	 * 
	 * RegistrationSteps steps = new RegistrationSteps(); //
	 * steps.setMDSprofile(type, profile);
	 * steps.setMDSscore(port.get("port_"+contextKey),"Biometric Device",
	 * qualityScore, contextKey); // client.setProfile("Default"); //
	 * client.setThresholdValue(qualityScore); return "qualityScore :" +
	 * qualityScore + " is updated"; } catch (Exception e) {
	 * logger.error("ThresholdValue", e); } return "{Failed}"; }
	 * 
	 * 
	 */

	@Operation(summary = "Get the stages with respect to the RID")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successfully retrived the stages for the RID") })
	@GetMapping(value = "/resident/stages/{rid}/{contextKey}")
	public @ResponseBody String getStagesByRid(@PathVariable("rid") String rid,
			@PathVariable("contextKey") String contextKey) {
		String r = null;
		if (personaConfigPath != null && !personaConfigPath.equals("")) {
			DataProviderConstants.RESOURCE = personaConfigPath;
		}

		try {
			return residentService.getStagesByRID(rid, contextKey).getBody().asString();
		} catch (Exception e) {
			logger.error("getStagesByRid", e);

		}
		return r;
	}

}
