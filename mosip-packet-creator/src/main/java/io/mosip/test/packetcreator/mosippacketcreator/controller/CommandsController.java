package io.mosip.test.packetcreator.mosippacketcreator.controller;

import java.io.IOException;
import java.util.Optional;
import java.util.Properties;
import org.json.JSONObject;
import org.mosip.dataprovider.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import io.mosip.test.packetcreator.mosippacketcreator.service.CommandsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import variables.VariableManager;

@Api(value = "CommandsController", description = "REST APIs for Command Center,x Kubernetes Control")
@RestController
public class CommandsController {
	
	
	@Autowired
    CommandsService commandsService;
	
	@GetMapping(value = "/exec/{testcaseId}/{IsSynchronous}/{contextKey}")
    public @ResponseBody String execJob(
    		@PathVariable("testcaseId") String testcaseId,
    		@PathVariable(name="IsSynchronous", required=true) Optional<Boolean> isSync,
    		@PathVariable("contextKey") String contextKey
    		) {
		boolean bSync = false;
		if(isSync.isPresent())
			bSync = isSync.get();
        return commandsService.execute(testcaseId, bSync);
              
    }
	
	@ApiOperation(value = "Upload a file to packet-utility configured folder. API Returns the Path", response = String.class)
	
	@PostMapping("/uploadFile/{contextKey}")
	public @ResponseBody String uploadFile(@RequestParam("file") MultipartFile file,
			@PathVariable("contextKey") String contextKey
			) {

		String fileName ="";
		try {
			 fileName = commandsService.storeFile(file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return fileName;
	}
	@ApiOperation(value = "Update Status of execution: Key - any unique key, Status ->  inUse | Free", response = String.class)
	
	@PutMapping("/status/{contextKey}")
	public @ResponseBody String updateStatus(
			@RequestParam("key") String key, @RequestParam("status") String status,
			@PathVariable("contextKey") String contextKey
			
			) {

		String timeStamp = CommonUtil.getUTCDateTime(null);
		VariableManager.setVariableValue(contextKey,key,status);
		VariableManager.setVariableValue(contextKey,key +"_ts",timeStamp);
		
		return "{\"Success\"}";
	}

	@ApiOperation(value = "Get Status of execution: Key - any unique key, Status ->  inUse | Free", response = String.class)
	
	@GetMapping("/status/{contextKey}")
	public @ResponseBody String getStatus(@RequestParam("key") String key,
			@PathVariable("contextKey") String contextKey
			) {

		try {
			String ts = VariableManager.getVariableValue(contextKey,key +"_ts").toString();
			String stsVal = VariableManager.getVariableValue(contextKey,key).toString();
			JSONObject json = new JSONObject();
			json.put(key, stsVal);
			json.put("ts", ts);
			return json.toString();
			
		}catch(Exception e) {
			
		}
		return "{\"Free\"}";
	}

	@PostMapping("/writeFile/{contextKey}")
	public @ResponseBody String writeToFile(@RequestParam("offset") long offset,
			@RequestBody Properties reqestData,
			@PathVariable("contextKey") String contextKey) {


		try {

			return commandsService.writeToFile(contextKey,reqestData, offset);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "{Failed}";
	}
	
	@ApiOperation(value = "Verify target environment (context) is available", response = String.class)
	
	@GetMapping("/ping/{contextKey}")
	public @ResponseBody String checkContext(@RequestParam(name="module", required = false) String module,
			@PathVariable("contextKey") String contextKey) {
	
		try {

			return commandsService.checkContext(contextKey, module);
			
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		return "{Failed}";
	}
	
	@ApiOperation(value = "Generate privatekey based on machineId", response = String.class)
	@GetMapping(value = "/generatekey/{machineId}/{contextKey}")
	public String generatekey(@PathVariable String machineId,
			@PathVariable("contextKey") String contextKey) {
		try {
			return commandsService.generatekey(contextKey,machineId);
		} catch (Exception e) {

			e.printStackTrace();
		}
		return "{Failed}";
	}
    
    
	
}
