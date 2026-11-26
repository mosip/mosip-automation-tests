package io.mosip.testrig.dslrig.packetcreator.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.slf4j.Logger;
import org.springframework.util.FileSystemUtils;

public final class PacketTempPurge {

	public static final long DEFAULT_MIN_AGE_MS = 20L * 60L * 1000L;

	private PacketTempPurge() {
	}

	public static final class Stats {
		public int deleted;
		public int skippedRecent;
		public int skippedOther;
		public int failed;

		public void add(Stats other) {
			if (other == null) {
				return;
			}
			deleted += other.deleted;
			skippedRecent += other.skippedRecent;
			skippedOther += other.skippedOther;
			failed += other.failed;
		}

		public void skipOther() {
			skippedOther++;
		}

		public String summary() {
			return "deleted=" + deleted + " skippedRecent=" + skippedRecent + " skippedOther=" + skippedOther
					+ " failed=" + failed;
		}
	}

	public static boolean isRecentlyUsed(File entry, long cutoffEpochMs) {
		if (entry == null || !entry.exists()) {
			return false;
		}
		if (entry.lastModified() >= cutoffEpochMs) {
			return true;
		}
		if (!entry.isDirectory()) {
			return false;
		}
		File[] children = entry.listFiles();
		if (children == null) {
			return false;
		}
		for (File child : children) {
			if (isRecentlyUsed(child, cutoffEpochMs)) {
				return true;
			}
		}
		return false;
	}

	public static void deleteIfStale(File entry, long minAgeMs, Stats stats, Logger logger) {
		if (entry == null || !entry.exists()) {
			return;
		}
		long cutoffEpochMs = System.currentTimeMillis() - Math.max(0L, minAgeMs);
		if (minAgeMs > 0 && isRecentlyUsed(entry, cutoffEpochMs)) {
			stats.skippedRecent++;
			if (logger != null) {
				logger.info("Skipping recent/in-use packet artifact: {}", entry.getAbsolutePath());
			}
			return;
		}
		try {
			if (entry.isDirectory()) {
				FileSystemUtils.deleteRecursively(entry.toPath());
			} else {
				Files.deleteIfExists(entry.toPath());
			}
			stats.deleted++;
		} catch (IOException e) {
			stats.failed++;
			if (logger != null) {
				logger.warn("Failed to delete leftover packet artifact {}", entry.getAbsolutePath(), e);
			}
		}
	}
}
