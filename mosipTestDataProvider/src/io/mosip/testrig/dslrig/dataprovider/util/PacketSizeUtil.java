package io.mosip.testrig.dslrig.dataprovider.util;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.mosip.testrig.dslrig.dataprovider.models.ResidentModel;
import io.mosip.testrig.dslrig.dataprovider.persona.PersonaBiometricsAssembler;

/**
 * Helpers for negative packet-size tests (e.g. scenario 241).
 * <p>
 * Large-face JPEG padding alone is not enough: MDS converts face JPEG to JP2/ISO and
 * shrinks the biometric. Inflating the final packet zip guarantees the upload exceeds
 * {@code registration.processor.max.file.size} on the target env.
 */
public final class PacketSizeUtil {

	private static final Logger logger = LoggerFactory.getLogger(PacketSizeUtil.class);

	/** Slightly above typical {@code registration.processor.max.file.size} (5 MB). */
	public static final int DEFAULT_MIN_PACKET_BYTES = 5 * 1024 * 1024 + 1024;

	private PacketSizeUtil() {
	}

	public static boolean isLargeFaceRequested(String personaPath) {
		if (personaPath == null || personaPath.isBlank()) {
			return false;
		}
		try {
			return isLargeFaceRequested(ResidentModel.readPersona(personaPath));
		} catch (IOException e) {
			logger.warn("Could not read persona for large-face check: {}", e.getMessage());
			return false;
		}
	}

	public static boolean isLargeFaceRequested(ResidentModel resident) {
		if (resident == null) {
			return false;
		}
		Properties hints = PersonaBiometricsAssembler.hintsFromResident(resident);
		Object flag = hints.get(ResidentAttribute.RA_LargeFace);
		return flag != null && Boolean.parseBoolean(flag.toString());
	}

	/**
	 * Appends zero bytes so the file length is at least {@code minBytes}.
	 *
	 * @return size after padding
	 */
	public static long padFileToMinSize(String filePath, int minBytes) throws IOException {
		Path path = Path.of(filePath);
		long size = Files.size(path);
		if (size >= minBytes) {
			return size;
		}
		long need = minBytes - size;
		byte[] chunk = new byte[8192];
		try (OutputStream out = Files.newOutputStream(path, StandardOpenOption.APPEND)) {
			long remaining = need;
			while (remaining > 0) {
				int n = (int) Math.min(remaining, chunk.length);
				out.write(chunk, 0, n);
				remaining -= n;
			}
		}
		long newSize = Files.size(path);
		logger.info("Padded packet file {} from {} to {} bytes (large-face negative test)", filePath, size, newSize);
		return newSize;
	}
}
