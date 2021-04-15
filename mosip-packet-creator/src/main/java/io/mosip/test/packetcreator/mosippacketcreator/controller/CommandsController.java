package io.mosip.test.packetcreator.mosippacketcreator.controller;

import java.io.IOException;
import java.util.Optional;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


import io.mosip.test.packetcreator.mosippacketcreator.service.CommandsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "CommandsController", description = "REST APIs for Command Center,x Kubernetes Control")
@RestController
public class CommandsController {

	@Autowired
    CommandsService commandsService;
	
	@GetMapping(value = "/exec/{testcaseId}/{IsSynchronous}")
    public @ResponseBody String execJob(@PathVariable("testcaseId") String testcaseId,
    		@PathVariable(name="IsSynchronous", required=true) Optional<Boolean> isSync) {
		boolean bSync = false;
		if(isSync.isPresent())
			bSync = isSync.get();
        return commandsService.execute(testcaseId, bSync);
              
    }
	@ApiOperation(value = "Upload a file to packet-utility configured folder. API Returns the Path", response = String.class)
	
	@PostMapping("/uploadFile")
	public @ResponseBody String uploadFile(@RequestParam("file") MultipartFile file) {

		String fileName ="";
		try {
			 fileName = commandsService.storeFile(file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return fileName;
	}
	@PostMapping("/writeFile")
	public @ResponseBody String writeToFile(@RequestParam("offset") long offset,
			@RequestBody Properties reqestData ) {


		try {

			return commandsService.writeToFile(reqestData, offset);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "{Failed}";
	}
	
	/*
	@GetMapping("/kube/all")
	public @ResponseBody String getAllPods(@RequestParam(name="contextKey",required = false) String contextKey) {

		String response ="";
		try {
			 response = commandsService.getAllPods(contextKey);
		} catch (IOException | ApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return response;
	}*/
	
}
