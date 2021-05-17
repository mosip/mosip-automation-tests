package io.mosip.test.packetcreator.mosippacketcreator.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.test.packetcreator.mosippacketcreator.MosipPacketCreatorApplication;

@RestController
public class RestartController {
	    
	    @PostMapping("/restart")
	    public void restart() {
	        MosipPacketCreatorApplication.restart();
	    } 
}

