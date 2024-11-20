package io.mosip.testrig.dslrig.packetcreator.controller;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import java.io.ByteArrayInputStream;
//import java.util.HashMap;
//import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.core.io.InputStreamResource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.core.websub.spi.SubscriptionClient;
import io.mosip.kernel.websub.api.annotation.PreAuthenticateContentAndVerifyIntent;
//import io.mosip.kernel.websub.api.annotation.PreAuthenticateContentAndVerifyIntent;
import io.mosip.kernel.websub.api.model.SubscriptionChangeRequest;
import io.mosip.kernel.websub.api.model.SubscriptionChangeResponse;
import io.mosip.kernel.websub.api.model.UnsubscriptionRequest;
import io.mosip.testrig.dslrig.dataprovider.variables.VariableManager;
import io.mosip.testrig.dslrig.packetcreator.dto.webSubEventModel;
import io.mosip.testrig.dslrig.packetcreator.service.CryptoCoreUtil;
import io.swagger.v3.oas.annotations.tags.Tag;


@RequestMapping(value = "/print")
@RestController
@Tag(name = "PrintController", description = "REST APIs for Websub subscription client")
public class PrintController {

	SubscriptionClient<SubscriptionChangeRequest,UnsubscriptionRequest, SubscriptionChangeResponse> sb; 
	@Autowired
	CryptoCoreUtil cryptoCoreUtil;
	
	@Value("${mosip.test.print.topic}")
	private String topic;

	@Value("${mosip.test.print.event.secret}")
	private String websubSecret;
	
	private static final Logger logger = LoggerFactory.getLogger(preRegController.class);
	   
	 @PostMapping(value = "/print/callback",consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	 @PreAuthenticateContentAndVerifyIntent(secret = "${mosip.test.print.event.secret}",
	 		callback = "/print/print/callback",topic = "${mosip.test.print.topic}")
	 	public ResponseEntity<String>  printPost(@RequestBody webSubEventModel  eventModel,
	 			@PathVariable("contextKey") String contextKey) throws Exception {
	
		 logger.info("Print callback fired");
		 String credential = eventModel.getEvent().getData().get("credential").toString();
		 String ecryptionPin = eventModel.getEvent().getData().get("protectionKey").toString();
		 String decodedCrdential = cryptoCoreUtil.decrypt(credential);
		 logger.info(decodedCrdential);
		 saveCreds(decodedCrdential,contextKey);
		 return new ResponseEntity<>("successfully printed", HttpStatus.OK);
	 }
	
	 private void saveCreds(String creds,String contextKey) {
		File credsPath = new File(VariableManager.getVariableValue(contextKey,"mountPath").toString()+VariableManager.getVariableValue(contextKey,"mosip.test.temp").toString() + "/creds/");
		if (!credsPath.exists()){
			credsPath.mkdirs();
		}
		JSONObject jsonCred = new JSONObject(creds);
		String fileName = credsPath.getAbsolutePath() + "/"+ jsonCred.getJSONObject("credentialSubject").getString("UIN") + ".cred";
		
		
		try  (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))){
			
					
			writer.write(creds);
			writer.close();

		 } catch (IOException e) {
			 logger.error(e.getMessage());
		}
		    
		
	 }
	 private String getSignature(String sign, String crdential) {
			String signHeader = sign.split("\\.")[0];
			String signData = sign.split("\\.")[2];
			String signature = signHeader + "." + crdential + "." + signData;
			return signature;
		}
	 
	
	 @PostMapping(value = "/print/register/{callbackurl}")
	 public @ResponseBody String registerPrintSubscription(@RequestBody String request,
			 @RequestParam("callbackurl") String cbUrl,
			 @RequestParam("webSubHubUrl") String webSubHubUrl) {
	
		 //String websubSecret = "{cipher}29ef73e366406ea1e7ac1d43e8d96002c3bd814a8b8cde9a961d897f2dadede5";
		 //String topic="mpartner-default-print/CREDENTIAL_ISSUED";
		 
		 SubscriptionChangeRequest subscriptionRequest = new SubscriptionChangeRequest();
		 subscriptionRequest.setCallbackURL(cbUrl);
		 subscriptionRequest.setHubURL(webSubHubUrl + "/hub");
		 subscriptionRequest.setSecret(websubSecret);
		 subscriptionRequest.setTopic(topic);
		 SubscriptionChangeResponse resp =  sb.subscribe(subscriptionRequest);
		 return resp.getTopic();
		 //return request;
		 
	 }
		 
	
}


