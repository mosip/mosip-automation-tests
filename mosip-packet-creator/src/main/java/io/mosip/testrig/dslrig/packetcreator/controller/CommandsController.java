package io.mosip.testrig.dslrig.packetcreator.controller;

import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.testrig.dslrig.packetcreator.service.CommandsService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController

@Tag(name = "CommandsController", description = "REST APIs for Command Center,x Kubernetes Control")
public class CommandsController {
	private static final Logger logger = LoggerFactory.getLogger(CommandsController.class);

	@Autowired
	CommandsService commandsService;

//  @GetMapping(value = "/exec/{testcaseId}/{IsSynchronous}/{contextKey}")
//  
//  @Operation(summary = "Executing the job")
//  
//  @ApiResponses(value = { @ApiResponse(responseCode = "200", description =
//  "Job executed successfully") }) public @ResponseBody String
//  execJob(@PathVariable("testcaseId") String testcaseId,
//  
//  @PathVariable(name = "IsSynchronous", required = true) Optional<Boolean>
//  isSync,
//  
//  @PathVariable("contextKey") String contextKey) { boolean bSync = false; if
//  (isSync.isPresent()) bSync = isSync.get(); return
//  commandsService.execute(testcaseId, bSync);
//  
//  }
//  
//  @PostMapping("/uploadFile/{contextKey}")
//  
//  @Operation(summary =
//  "Upload a file to packet-utility configured folder. API Returns the Path",
//  responses = {
//  
//  @ApiResponse(responseCode = "200", description = "Successfully uploaded") })
//  public @ResponseBody String uploadFile(@RequestParam("file") MultipartFile
//  file,
//  
//  @PathVariable("contextKey") String contextKey) {
//  
//  String fileName = ""; try { fileName = commandsService.storeFile(file); }
//  catch (IOException e) { logger.error(e.getMessage()); } return fileName; }
//  
//  @PutMapping("/status/{contextKey}")
//  
//  @Operation(summary = "Update Status of execution", description =
//  "Update the status of an execution identified by the given key. The status can be 'inUse' or 'Free'."
//  , responses = {
//  
//  @ApiResponse(responseCode = "200", description =
//  "Status updated successfully") }) public @ResponseBody String
//  updateStatus(@RequestParam("key") String key, @RequestParam("status") String
//  status,
//  
//  @PathVariable("contextKey") String contextKey
//  
//  ) {
//  
//  String timeStamp = CommonUtil.getUTCDateTime(null);
//  VariableManager.setVariableValue(contextKey, key, status);
//  VariableManager.setVariableValue(contextKey, key + "_ts", timeStamp);
//  
//  return "{\"Success\"}"; }
//  
//  @GetMapping("/status/{contextKey}")
//  
//  @Operation(summary = "Get Status of execution", description =
//  "Retrieve the status of an execution identified by the given key. The status can be 'inUse' or 'Free'."
//  , responses = {
//  
//  @ApiResponse(responseCode = "200", description =
//  "Status retrieved successfully") }) public @ResponseBody String
//  getStatus(@RequestParam("key") String key,
//  
//  @PathVariable("contextKey") String contextKey) {
//  
//  try { String ts = VariableManager.getVariableValue(contextKey, key +
//  "_ts").toString(); String stsVal =
//  VariableManager.getVariableValue(contextKey, key).toString(); JSONObject json
//  = new JSONObject(); json.put(key, stsVal); json.put("ts", ts); return
//  json.toString();
//  
//  } catch (Exception e) {
//  
//  } return "{\"Free\"}"; }

	@PostMapping("/writeFile/{offset}/{contextKey}")
	@Operation(summary = "Creating the file")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "File created successfully") })
	public @ResponseBody String writeToFile(
			// @RequestParam("offset") long offset,
			@PathVariable("offset") long offset, @RequestBody Properties reqestData,
			@PathVariable("contextKey") String contextKey) {
		try {
			return commandsService.writeToFile(contextKey, reqestData, offset);
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		return "{Failed}";
	}

	@GetMapping(value = "/generatekey/{machineId}/{contextKey}")
	@Operation(summary = "Generate private key based on machineId", description = "Generate a private key based on the provided machine ID.", responses = {
			@ApiResponse(responseCode = "200", description = "Private key generated successfully") })
	public String generatekey(@PathVariable String machineId, @PathVariable("contextKey") String contextKey) {
		try {
			return commandsService.generatekey(contextKey, machineId);
		} catch (Exception e) {

			logger.error(e.getMessage());
		}
		return "{Failed}";
	}

}
