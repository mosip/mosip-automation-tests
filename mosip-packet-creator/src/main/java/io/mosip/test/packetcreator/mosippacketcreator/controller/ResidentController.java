package io.mosip.test.packetcreator.mosippacketcreator.controller;


import java.util.List;

import org.mosip.dataprovider.mds.MDSClient;
import org.mosip.dataprovider.util.DataProviderConstants;
import org.mosip.dataprovider.util.ReadEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.test.packetcreator.mosippacketcreator.dto.UpdatePersonaDto;
import io.mosip.test.packetcreator.mosippacketcreator.service.ResidentService;
import io.swagger.annotations.ApiOperation;


@RestController
public class ResidentController {

	  private static final Logger logger = LoggerFactory.getLogger(ResidentController.class);


	  @Autowired
	  ResidentService residentService;

	  @Value("${mosip.test.persona.configpath}")
		private String personaConfigPath;
	    

	  @GetMapping(value = "/resident/status/{rid}")
	  public @ResponseBody String getRIDStatus( @PathVariable("rid") String rid, @RequestParam(name="contextKey",required = false) String contextKey) {

		if(personaConfigPath !=null && !personaConfigPath.equals("")) {
  			DataProviderConstants.RESOURCE = personaConfigPath;
  		}
		  try {
			return residentService.getRIDStatus(rid, contextKey);
		} catch (Exception e) {
			   logger.error("getRIDStatus", e);
		}
		return "{Failed}";
	  }
	
	  @GetMapping(value = "/resident/uin/{rid}")
	  public @ResponseBody String getUINByRid( @PathVariable("rid") String rid, @RequestParam(name="contextKey",required = false) String contextKey) {
		  String err = "{\"Status\": \"Failed\",\"Error\":\"%s\"}";
		  
		  if(personaConfigPath !=null && !personaConfigPath.equals("")) {
	  			DataProviderConstants.RESOURCE = personaConfigPath;
	  		}

 
		  try {
			return residentService.getUINByRID(rid, contextKey);
		  }catch (Exception e) {
			   logger.error("getUINByRid", e);
			   err = String.format(err, e.getMessage());
		  }
		  return err;
	  }
	
	  //resident/v1/req/credential
	  @ApiOperation(value = "download card for the UIN", response = String.class)
	  @PostMapping(value = "/resident/card/{uin}")
	  public @ResponseBody String downloadCard(@RequestBody String personaPath, @PathVariable("uin") String uin, @RequestParam(name="contextKey",required = false) String contextKey) {
		  String err = "{\"Status\": \"Failed\",\"Error\":\"%s\"}";

		  if(personaConfigPath !=null && !personaConfigPath.equals("")) {
	  			DataProviderConstants.RESOURCE = personaConfigPath;
	  		}

		  try {
			return residentService.downloadCard(personaPath, uin, contextKey);
		  }catch (Exception e) {
			   logger.error("getUINByRid", e);
			   err = String.format(err, e.getMessage());
		  }
		  return err;
	  
		 /*
		  * {
			  "id": "string",
			  "request": {
			    "additionalData": {},
			    "credentialType": "string",
			    "encrypt": true,
			    "encryptionKey": "string",
			    "individualId": "string",
			    "issuer": "string",
			    "otp": "string",
			    "recepiant": "string",
			    "sharableAttributes": [
			      "string"
			    ],
			    "transactionID": "string",
			    "user": "string"
			  },
			  "requesttime": "string",
			  "version": "string"
		}
		  * 
		  */
		 
	  }
	  
		@GetMapping(value = "/resident/additionalReqId")
		public @ResponseBody String getAdditionalInfoReqId() {

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

		@GetMapping(value = "/resident/setThresholdValue/{qualityScore}")
		public @ResponseBody String setThresholdValue(@PathVariable("qualityScore") String qualityScore) {

			if (personaConfigPath != null && !personaConfigPath.equals("")) {
				DataProviderConstants.RESOURCE = personaConfigPath;
			}
			try {
				MDSClient client = new MDSClient(0);
				client.setProfile("Default");
				client.setThresholdValue(qualityScore);
				return "qualityScore :" + qualityScore + " is updated";
			} catch (Exception e) {
				logger.error("ThresholdValue", e);
			}
			return "{Failed}";
		}
		
}
