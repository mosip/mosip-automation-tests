package io.mosip.testrig.dslrig.dataprovider.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Prepares a clean, per-scope output directory for generated biometric variations. */
public final class BiometricOutputScope {

	private static final Logger logger = LoggerFactory.getLogger(BiometricOutputScope.class);

	private BiometricOutputScope() {
	}

	/**
	 * Clears leftovers so callers do not pick an older NONMATCH_* file under parallel or
	 * repeated runs, then creates the directory.
	 *
	 * @return the prepared modality directory
	 */
	public static Path prepare(String outputDir, String modality) throws IOException {
		Path modalityDir = Paths.get(outputDir, modality);
		if (Files.isDirectory(modalityDir)) {
			try (Stream<Path> stream = Files.list(modalityDir)) {
				stream.forEach(p -> {
					try {
						Files.deleteIfExists(p);
					} catch (Exception e) {
						logger.warn("Unable to clear stale {} file {}: {}", modality, p, e.getMessage());
					}
				});
			}
		}
		Files.createDirectories(modalityDir);
		return modalityDir;
	}
}
