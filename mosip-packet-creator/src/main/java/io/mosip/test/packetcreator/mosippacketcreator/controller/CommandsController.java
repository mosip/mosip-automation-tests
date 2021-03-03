package io.mosip.test.packetcreator.mosippacketcreator.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.test.packetcreator.mosippacketcreator.service.TestcaseExecutionService;

@RestController
public class CommandsController {

	@Autowired
    TestcaseExecutionService testcaseExecutionService;
	
	@GetMapping(value = "/exec/{testcaseId}/{IsSynchronous}")
    public @ResponseBody String execJob(@PathVariable("testcaseId") String testcaseId,
    		@PathVariable(name="IsSynchronous", required=true) Optional<Boolean> isSync) {
		boolean bSync = false;
		if(isSync.isPresent())
			bSync = isSync.get();
        return testcaseExecutionService.execute(testcaseId, bSync);
              
    }
}
