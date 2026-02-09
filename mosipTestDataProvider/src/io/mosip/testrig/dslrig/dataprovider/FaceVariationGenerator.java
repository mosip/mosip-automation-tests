package io.mosip.testrig.dslrig.dataprovider;

import io.mosip.testrig.dslrig.dataprovider.variables.VariableManager;
import javax.imageio.ImageIO;
import org.slf4j.LoggerFactory;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.List;
import java.util.UUID;

public final class FaceVariationGenerator {
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(FaceVariationGenerator.class);

	private FaceVariationGenerator() {
	}

	/*
	 * ========================================================= BACKWARD COMPATIBLE
	 * ENTRY POINT (DO NOT CHANGE CALLERS)
	 * =========================================================
	 */
	public static String faceVariationGenerator(String contextKey, int currentScenarioNumber, int impressionToPick)
			throws Exception {

		String inputFaceTemplatePath = System.getProperty("java.io.tmpdir")
				+ VariableManager.getVariableValue(contextKey, "mosip.test.persona.facedatapath") + "/"
				+ String.format("face%04d.jpg", impressionToPick);

		String outputUniqueFaceDataPath = System.getProperty("java.io.tmpdir")
				+ VariableManager.getVariableValue(contextKey, "mosip.test.persona.facedatapath") + "/output/"
				+ currentScenarioNumber;

		Files.createDirectories(Paths.get(outputUniqueFaceDataPath));

		generateNonMatchingFace(inputFaceTemplatePath, outputUniqueFaceDataPath, "face",
				"face" + impressionToPick + ".png");

		return outputUniqueFaceDataPath;
	}

	/*
	 * ========================================================= CORE NON-MATCHING
	 * FACE ENGINE =========================================================
	 */
	private static void generateNonMatchingFace(String inputPath, String outputDir, String facePath,
			String originalName) throws Exception {

		BufferedImage base = ImageIO.read(new File(inputPath));
		if (base == null)
			throw new IllegalStateException("Unable to read face image: " + inputPath);

		SecureRandom r = SecureRandom.getInstanceStrong();
		BufferedImage img = copy(base);

		// 1️⃣ STRONG ROTATION (breaks pose normalization)
		rotate(img, r);

		// 2️⃣ EYE OCCLUSION (kills embeddings)
		occludeEyes(img, r);

		// 3️⃣ NOSE BRIDGE BLOCKING
		blockNose(img, r);

		// 4️⃣ RANDOM LANDMARK MASKING
		maskBlocks(img, r);

		// 5️⃣ LOCAL FEATURE INVERSION
		localInvert(img, r);

		String uniqueName = "NONMATCH_" + System.currentTimeMillis() + "_" + UUID.randomUUID() + "_" + originalName;

		Path outDir = Paths.get(outputDir, facePath);
		Files.createDirectories(outDir);

		Path out = outDir.resolve(uniqueName);
		ImageIO.write(img, "png", out.toFile());

		logger.info("[FACE-NONMATCH] " + sha256(Files.readAllBytes(out)));
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

	private static void occludeEyes(BufferedImage img, SecureRandom r) {
		Graphics2D g = img.createGraphics();
		g.setColor(Color.BLACK);

		int y = img.getHeight() / 3;
		int h = img.getHeight() / 8;

		g.fillRect(0, y, img.getWidth(), h);
		g.dispose();
	}

	private static void blockNose(BufferedImage img, SecureRandom r) {
		Graphics2D g = img.createGraphics();
		g.setColor(Color.BLACK);

		int w = img.getWidth() / 6;
		int h = img.getHeight() / 3;

		int x = (img.getWidth() - w) / 2;
		int y = img.getHeight() / 3;

		g.fillRect(x, y, w, h);
		g.dispose();
	}

	private static void maskBlocks(BufferedImage img, SecureRandom r) {
		Graphics2D g = img.createGraphics();
		g.setColor(Color.BLACK);

		int blocks = 3 + r.nextInt(3);
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
		for (int i = 0; i < 7000; i++) {
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
