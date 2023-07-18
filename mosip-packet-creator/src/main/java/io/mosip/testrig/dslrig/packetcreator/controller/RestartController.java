package io.mosip.testrig.dslrig.packetcreator.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.testrig.dslrig.packetcreator.MosipPacketCreatorApplication;

@RestController
public class RestartController {
	    
	    @PostMapping("/restart")
	    public void restart() {
	        MosipPacketCreatorApplication.restart();
	    } 
}

