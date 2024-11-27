package io.mosip.testrig.dslrig.packetcreator.controller;

import org.jobrunr.scheduling.JobScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.testrig.dslrig.packetcreator.service.APIRequestUtil;
import io.mosip.testrig.dslrig.packetcreator.service.ContextUtils;
import io.mosip.testrig.dslrig.packetcreator.service.CryptoUtil;
import io.mosip.testrig.dslrig.packetcreator.service.PacketJobService;
import io.mosip.testrig.dslrig.packetcreator.service.PacketMakerService;
import io.mosip.testrig.dslrig.packetcreator.service.PacketSyncService;
import io.mosip.testrig.dslrig.packetcreator.service.PreregSyncService;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "TestDataController", description = "REST APIs for Test data")
public class TestDataController {

	private static final Logger logger = LoggerFactory.getLogger(TestDataController.class);

	@Value("${mosip.test.welcome}")
	private String welcomeMessage;

	@Value("${mosip.test.persona.configpath}")
	private String personaConfigPath;

	@Value("${mosip.test.persona.Angulipath}")
	private String personaAnguliPath;

	PacketMakerService pkm;
	PacketSyncService packetSyncService;

	@Autowired
	PreregSyncService pss;

	@Autowired
	APIRequestUtil apiUtil;

	@Autowired
	CryptoUtil cryptoUtil;

	@Autowired
	private JobScheduler jobScheduler;

	@Autowired
	PacketJobService packetJobService;

	@Autowired
	ContextUtils contextUtils;

	@Value("${mosip.test.baseurl}")
	private String baseUrl;

	public TestDataController(@Lazy PacketSyncService packetSyncService, @Lazy PacketMakerService pkm,
			@Lazy PacketJobService packetJobService) {
		this.packetSyncService = packetSyncService;
		this.pkm = pkm;
		this.packetJobService = packetJobService;
	}

	/*
	 * @Operation(summary = "Initialize the server context")
	 * 
	 * @ApiResponses(value = {
	 * 
	 * @ApiResponse(responseCode = "200", description =
	 * "Successfully created the server context") })
	 * 
	 * @PostMapping(value = "/servercontext/{contextKey}") public @ResponseBody
	 * String createServerContext(@RequestBody Properties contextProperties,
	 * 
	 * @PathVariable("contextKey") String contextKey) {
	 * 
	 * try { return contextUtils.createUpdateServerContext(contextProperties,
	 * contextKey); } catch (Exception ex) { logger.error("createServerContext",
	 * ex); return "{\"" + ex.getMessage() + "\"}"; } }
	 */

	/*
	 * @Operation(summary = "Get the API test data")
	 * 
	 * @ApiResponses(value = { @ApiResponse(responseCode = "200", description =
	 * "Successfully retrived the test data") })
	 * 
	 * @GetMapping(value = "/auth/{contextKey}") public @ResponseBody String
	 * getAPITestData(@PathVariable("contextKey") String contextKey) { return
	 * String.valueOf(apiUtil.initToken(contextKey)); }
	 */

	/*
	 * @Operation(summary = "Clear the token")
	 * 
	 * @ApiResponses(value = { @ApiResponse(responseCode = "200", description =
	 * "Token is cleared successfully") })
	 * 
	 * @GetMapping(value = "/clearToken/{contextKey}") public @ResponseBody String
	 * ClearToken(@PathVariable("contextKey") String contextKey) {
	 * VariableManager.setVariableValue(contextKey, "urlSwitched", true); return
	 * "Success"; // return String.valueOf(apiUtil.initToken()); }
	 */

	/*
	 * @Operation(summary = "Sync the pre-registration data")
	 * 
	 * @ApiResponses(value = {
	 * 
	 * @ApiResponse(responseCode = "200", description =
	 * "Successfully synced the pre-registration data") })
	 * 
	 * @GetMapping(value = "/sync/{contextKey}") public @ResponseBody String
	 * syncPreregData(@PathVariable("contextKey") String contextKey) { try {
	 * pss.syncPrereg(contextKey); return "All Done!"; } catch (Exception exception)
	 * { logger.error("", exception); return exception.getMessage(); } }
	 */

	/*
	 * @Operation(summary = "Get the pre-registration data")
	 * 
	 * @ApiResponses(value = {
	 * 
	 * @ApiResponse(responseCode = "200", description =
	 * "Successfully retrived the pre-registration data") })
	 * 
	 * @GetMapping(value = "/sync/{preregId}/{contextKey}") public @ResponseBody
	 * String getPreregData(@PathVariable("preregId") String preregId,
	 * 
	 * @PathVariable("contextKey") String contextKey) { try { return
	 * pss.downloadPreregPacket(preregId, contextKey); } catch (Exception exception)
	 * { logger.error("", exception); return "Failed"; } }
	 */
	/*
	 * @Operation(summary = "Encrypt the data")
	 * 
	 * @ApiResponses(value = { @ApiResponse(responseCode = "200", description =
	 * "Successfully encrypted the data") })
	 * 
	 * @GetMapping(value = "/encrypt/{contextKey}") public @ResponseBody String
	 * encryptData(@PathVariable("contextKey") String contextKey) throws Exception {
	 * return
	 * Base64.getUrlEncoder().encodeToString(cryptoUtil.encrypt("test".getBytes(),
	 * "referenceId", contextKey)); }
	 */

	/*
	 * @Operation(summary = "Start the job")
	 * 
	 * @ApiResponses(value = { @ApiResponse(responseCode = "200", description =
	 * "Job is started successfully") })
	 * 
	 * @GetMapping(value = "/startjob/{contextKey}") public @ResponseBody String
	 * startJob(@PathVariable("contextKey") String contextKey) { String response =
	 * jobScheduler.scheduleRecurrently(() -> packetJobService.execute(contextKey),
	 * Cron.every5minutes()); return response; }
	 */

	/*
	 * to : email | mobile
	 */

//    @PostMapping(value = "/packet/{process}/{outFolderPath}/{contextKey}")
//    public @ResponseBody String createPackets(@RequestBody PreRegisterRequestDto preRegisterRequestDto,
//    		@PathVariable("process") String process,
//    		@PathVariable("outFolderPath") String outFolderPath,
//    		@PathVariable("contextKey") String contextKey) {
//
//    	try{    	
//    		if(personaConfigPath !=null && !personaConfigPath.equals("")) {
//    			DataProviderConstants.RESOURCE = personaConfigPath;
//    		}
//    		return packetSyncService.createPacketTemplates(preRegisterRequestDto.getPersonaFilePath(),process,outFolderPath, null,contextKey,"Registration");
//    	
//    	} catch (Exception ex){
//             logger.error("createPackets", ex);
//    	}
//    	return "{\"Failed\"}";
//    }

	/*
	 * Download from pre-reg, merge with the given packet template and upload to
	 * register
	 */

}