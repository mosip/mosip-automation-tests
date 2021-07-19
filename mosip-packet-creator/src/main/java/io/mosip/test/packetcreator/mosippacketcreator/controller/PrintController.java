package io.mosip.test.packetcreator.mosippacketcreator.controller;


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
import io.mosip.test.packetcreator.mosippacketcreator.dto.webSubEventModel;
import io.mosip.test.packetcreator.mosippacketcreator.service.CryptoCoreUtil;

import io.swagger.annotations.Api;

@Api(value = "PrintController", description = "REST APIs for Websub subscription client")
@RequestMapping(value = "/print")
@RestController
public class PrintController {

	@Autowired
	SubscriptionClient<SubscriptionChangeRequest,UnsubscriptionRequest, SubscriptionChangeResponse> sb; 
	@Autowired
	CryptoCoreUtil cryptoCoreUtil;
	
//	@Autowired
//	PrintService printService;
	
	@Value("${mosip.test.print.topic}")
	private String topic;

	@Value("${mosip.test.print.event.secret}")
	private String websubSecret;

	@Value("${mosip.test.temp}")
	private String tempPath;
	
	private static final Logger logger = LoggerFactory.getLogger(preRegController.class);
	   

	 @PostMapping(value = "/print/callback",consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	 @PreAuthenticateContentAndVerifyIntent(secret = "${mosip.test.print.event.secret}",
	 		callback = "/print/print/callback",topic = "${mosip.test.print.topic}")
	 	public ResponseEntity<String>  printPost(@RequestBody webSubEventModel  eventModel) throws Exception {
	
		 logger.info("Print callback fired");
		 String credential = eventModel.getEvent().getData().get("credential").toString();
		 String ecryptionPin = eventModel.getEvent().getData().get("protectionKey").toString();
		 String decodedCrdential = cryptoCoreUtil.decrypt(credential);
		 logger.info(decodedCrdential);
		 saveCreds(decodedCrdential);
		 return new ResponseEntity<>("successfully printed", HttpStatus.OK);
	 }
	
	 private void saveCreds(String creds) {
		File credsPath = new File(tempPath + "/creds/");
		if (!credsPath.exists()){
			credsPath.mkdirs();
		}
		JSONObject jsonCred = new JSONObject(creds);
		String fileName = credsPath.getAbsolutePath() + "/"+ jsonCred.getJSONObject("credentialSubject").getString("UIN") + ".cred";
		try { 
			BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));		
			writer.write(creds);
			writer.close();

		 } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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


