package io.mosip.test.packetcreator.mosippacketcreator.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ServerSetupService {

	 private static final Logger logger = LoggerFactory.getLogger(ServerSetupService.class);
		
	 public String generate(String specs) {
		 return specs;
	 }
}
