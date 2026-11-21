package io.mosip.testrig.dslrig.packetcreator.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Keeps a long-lived packet-creator process healthy without depending on suite
 * teardown: periodically deletes stale scratch/temp artifacts and, when idle,
 * clears in-memory caches so heap pressure does not accumulate over months/years.
 */
@Service
@ConditionalOnProperty(name = "mosip.test.packetcreator.maintenance.enabled", havingValue = "true", matchIfMissing = true)
public class PacketCreatorMaintenanceService {

	private static final Logger logger = LoggerFactory.getLogger(PacketCreatorMaintenanceService.class);

	private final PacketMakerService packetMakerService;
	private final PreregSyncService preregSyncService;
	private final ContextResetService contextResetService;

	@Value("${mosip.test.persona.configpath:}")
	private String personaConfigPath;

	@Value("${mosip.test.packetcreator.purge.min-age-ms:" + PacketTempPurge.DEFAULT_MIN_AGE_MS + "}")
	private long purgeMinAgeMs;

	@Value("${mosip.test.packetcreator.maintenance.mount-path:}")
	private String mountPath;

	@Value("${mosip.test.packetcreator.maintenance.temp-path:/packets/}")
	private String tempPath;

	@Value("${mosip.test.packetcreator.maintenance.idle-cache-clear-ms:3600000}")
	private long idleCacheClearMs;

	public PacketCreatorMaintenanceService(PacketMakerService packetMakerService, PreregSyncService preregSyncService,
			ContextResetService contextResetService) {
		this.packetMakerService = packetMakerService;
		this.preregSyncService = preregSyncService;
		this.contextResetService = contextResetService;
	}

	@Scheduled(fixedDelayString = "${mosip.test.packetcreator.maintenance.fixed-delay-ms:1800000}",
			initialDelayString = "${mosip.test.packetcreator.maintenance.initial-delay-ms:300000}")
	public void runMaintenance() {
		int active = PacketCreatorActivityTracker.activeCount();
		long idleMs = PacketCreatorActivityTracker.idleMillis();
		long ageMs = active > 0 ? Math.max(purgeMinAgeMs, PacketTempPurge.DEFAULT_MIN_AGE_MS) : purgeMinAgeMs;
		try {
			PacketTempPurge.Stats stats = new PacketTempPurge.Stats();
			List<String> protectRoots = activeWorkRoots();
			stats.add(packetMakerService.purgeWorkDirectory(ageMs));
			stats.add(ContextUtils.purgeOrphanScratchDirs(ageMs, protectRoots));
			stats.add(ContextUtils.purgeMountedTempDir(mountPath, tempPath, ageMs));
			stats.add(ContextUtils.purgeGeneratedResourceArtifacts(personaConfigPath, ageMs));
			logger.info(
					"Periodic packet-creator maintenance purge: {} activeRequests={} idleMs={} minAgeMs={} mountPath={} tempPath={}",
					stats.summary(), active, idleMs, ageMs, mountPath, tempPath);
		} catch (Exception e) {
			logger.warn("Periodic packet-creator disk maintenance failed: {}", e.getMessage());
		}

		if (active == 0 && idleMs >= idleCacheClearMs) {
			try {
				contextResetService.clearAfterSuite("maintenance_idle_context");
				PacketCreatorActivityTracker.touchBusinessActivity();
				logger.info("Periodic idle cache clear completed after {} ms idle", idleMs);
			} catch (Exception e) {
				logger.warn("Periodic idle cache clear failed: {}", e.getMessage());
			}
		}
	}

	private List<String> activeWorkRoots() {
		List<String> roots = new ArrayList<>();
		if (packetMakerService != null && packetMakerService.getWorkDirectory() != null) {
			roots.add(packetMakerService.getWorkDirectory());
		}
		if (preregSyncService != null && preregSyncService.getWorkDirectory() != null) {
			roots.add(preregSyncService.getWorkDirectory());
		}
		return roots;
	}
}
