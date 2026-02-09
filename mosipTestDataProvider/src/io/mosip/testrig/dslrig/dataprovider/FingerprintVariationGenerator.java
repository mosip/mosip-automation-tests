package io.mosip.testrig.dslrig.dataprovider;

import io.mosip.testrig.dslrig.dataprovider.variables.VariableManager;

import javax.imageio.ImageIO;

import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.List;
import java.util.UUID;

public final class FingerprintVariationGenerator {
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(FingerprintVariationGenerator.class);


	private FingerprintVariationGenerator() {
	}

	/*
	 * ========================================================= BACKWARD COMPATIBLE
	 * ENTRY POINT (DO NOT CHANGE CALLERS)
	 * =========================================================
	 */
	public static String fingerprintVariationGenerator(String contextKey, int currentScenarioNumber,
			int impressionToPick) throws Exception {

		String inputDir = System.getProperty("java.io.tmpdir")
				+ VariableManager.getVariableValue(contextKey, "mosip.test.persona.fingerprintdatapath")
				+ "/Impression_" + impressionToPick + "/fp_1";

		String outputDir = System.getProperty("java.io.tmpdir")
				+ VariableManager.getVariableValue(contextKey, "mosip.test.persona.fingerprintdatapath") + "/output/"
				+ currentScenarioNumber;

		Files.createDirectories(Paths.get(outputDir));

		List<Path> inputs = Files.list(Paths.get(inputDir)).filter(Files::isRegularFile).toList();

		for (Path fp : inputs) {
			generateNonMatchingFingerprint(fp, outputDir, "fp", fp.getFileName().toString());
		}

		return outputDir;
	}

	/*
	 * ========================================================= CORE NON-MATCHING
	 * ENGINE =========================================================
	 */
	private static void generateNonMatchingFingerprint(Path input, String outputDir, String fpPath, String originalName)
			throws Exception {

		BufferedImage base = ImageIO.read(input.toFile());
		if (base == null)
			throw new IllegalStateException("Unable to read finger image: " + input.toString());

		SecureRandom r = SecureRandom.getInstanceStrong();
		BufferedImage img = copy(base);

		// 1️⃣ STRONG ROTATION (kills alignment)
		rotate(img, r);

		// 2️⃣ RIDGE ERASURE BANDS (kills minutiae continuity)
		eraseBands(img, r);

		// 3️⃣ LARGE BLOCK MASKING (kills core/delta)
		maskBlocks(img, r);

		// 4️⃣ LOCAL INVERSION (breaks ridge polarity)
		localInvert(img, r);

		String uniqueName = "NONMATCH_" + System.currentTimeMillis() + "_" + UUID.randomUUID() + "_" + originalName;

		Path outDir = Paths.get(outputDir, fpPath);
		Files.createDirectories(outDir);

		Path out = outDir.resolve(uniqueName);
		ImageIO.write(img, "png", out.toFile());

		logger.info("[FP-NONMATCH] " + sha256(Files.readAllBytes(out)));
	}

	/*
	 * ========================================================= DESTRUCTIVE
	 * OPERATIONS (MATCH BREAKERS)
	 * =========================================================
	 */

	private static void rotate(BufferedImage img, SecureRandom r) {
		double angle = Math.toRadians(15 + r.nextInt(15)); // 15–30°
		BufferedImage out = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());

		Graphics2D g = out.createGraphics();
		g.setTransform(AffineTransform.getRotateInstance(angle, img.getWidth() / 2.0, img.getHeight() / 2.0));
		g.drawImage(img, 0, 0, null);
		g.dispose();

		Graphics2D g2 = img.createGraphics();
		g2.drawImage(out, 0, 0, null);
		g2.dispose();
	}

	private static void eraseBands(BufferedImage img, SecureRandom r) {
		Graphics2D g = img.createGraphics();
		g.setColor(Color.WHITE);

		int bands = 3 + r.nextInt(3);
		for (int i = 0; i < bands; i++) {
			int y = r.nextInt(img.getHeight());
			g.fillRect(0, y, img.getWidth(), 15 + r.nextInt(25));
		}
		g.dispose();
	}

	private static void maskBlocks(BufferedImage img, SecureRandom r) {
		Graphics2D g = img.createGraphics();
		g.setColor(Color.WHITE);

		int blocks = 4 + r.nextInt(4);
		for (int i = 0; i < blocks; i++) {
			int w = img.getWidth() / 5;
			int h = img.getHeight() / 5;
			int x = r.nextInt(img.getWidth() - w);
			int y = r.nextInt(img.getHeight() - h);
			g.fillRect(x, y, w, h);
		}
		g.dispose();
	}

	private static void localInvert(BufferedImage img, SecureRandom r) {
		for (int i = 0; i < 5000; i++) {
			int x = r.nextInt(img.getWidth());
			int y = r.nextInt(img.getHeight());
			int rgb = img.getRGB(x, y);
			img.setRGB(x, y, (~rgb) | 0xFF000000);
		}
	}

	/*
	 * ========================================================= UTILITIES
	 * =========================================================
	 */

	private static BufferedImage copy(BufferedImage img) {
		BufferedImage c = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
		Graphics2D g = c.createGraphics();
		g.drawImage(img, 0, 0, null);
		g.dispose();
		return c;
	}

	private static String sha256(byte[] data) throws Exception {
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		byte[] h = md.digest(data);
		StringBuilder sb = new StringBuilder();
		for (byte b : h)
			sb.append(String.format("%02x", b));
		return sb.toString();
	}
}
