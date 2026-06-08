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


}
