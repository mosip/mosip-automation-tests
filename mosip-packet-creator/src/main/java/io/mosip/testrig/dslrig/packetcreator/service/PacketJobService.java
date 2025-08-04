package io.mosip.testrig.dslrig.packetcreator.service;

import java.io.File;
import java.nio.file.Path;

import org.jobrunr.jobs.annotations.Job;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import io.mosip.testrig.dslrig.dataprovider.util.RestClient;

@Service
public class PacketJobService {

	private Logger logger = LoggerFactory.getLogger(PacketJobService.class);

	@Autowired
	private PreregSyncService preregSyncService;

	private PacketMakerService packetMakerService;
	private PacketSyncService packetSyncService;

	@Autowired
	private ZipUtils zipUtils;
	
	public PacketJobService(@Lazy PacketSyncService packetSyncService, @Lazy PacketMakerService packetMakerService) {
		this.packetSyncService = packetSyncService;
		this.packetMakerService = packetMakerService;
	}

	@Job(name = "Create Packet with pre-reg sync")
	public void execute(String contextKey) {
		try {
			RestClient.logInfo(contextKey, "started execute job");
			JSONObject jb = preregSyncService.syncPrereg(contextKey);

			JSONArray keys = jb.names();
			for (int i = 0; i < keys.length(); i++) {
				if (RestClient.isDebugEnabled(contextKey))
					logger.info("Started for PRID", keys.get(i));
				String prid = keys.getString(i);
				try {
					String location = preregSyncService.downloadPreregPacket(prid, null);
					if (RestClient.isDebugEnabled(contextKey))
						logger.info("Downloaded the prereg packet in {} ", location);

					File targetDirectory = Path.of(preregSyncService.getWorkDirectory(), prid).toFile();
					if (!targetDirectory.exists() && !targetDirectory.mkdir())
						throw new Exception("Failed to create target directory ! PRID : " + prid);

					if (!zipUtils.unzip(location, targetDirectory.getAbsolutePath(), contextKey))
						throw new Exception("Failed to unzip pre-reg packet >> " + prid);

					Path idJsonPath = Path.of(targetDirectory.getAbsolutePath(), "ID.json");

					if (RestClient.isDebugEnabled(contextKey))
						logger.info("Unzipped the prereg packet {}, ID.json exists : {}", prid,
								idJsonPath.toFile().exists());

					String packetPath = packetMakerService.createContainer(idJsonPath.toString(), null, null, null,
							prid, null, true, null ,null);

					if (RestClient.isDebugEnabled(contextKey))
						logger.info("Packet created : {}", packetPath);

					String response = packetSyncService.syncPacketRid(packetPath, "dummy", "APPROVED", "dummy", null,
							null, null);

					if (RestClient.isDebugEnabled(contextKey))
						logger.info("RID Sync response : {}", response);

					response = packetSyncService.uploadPacket(packetPath, null);

					if (RestClient.isDebugEnabled(contextKey))
						logger.info("Packet Sync response : {}", response);

				} catch (Exception exception) {
					logger.error("Failed for PRID : {}", prid, exception);
				}
			}
		} catch (Throwable t) {
			logger.error("Job Failed", t);
		}
	}

}
