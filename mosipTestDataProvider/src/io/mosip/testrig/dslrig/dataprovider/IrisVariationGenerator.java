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

public final class IrisVariationGenerator {
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(IrisVariationGenerator.class);


	private IrisVariationGenerator() {
	}

	/*
	 * ========================================================= BACKWARD COMPATIBLE
	 * ENTRY POINT (DO NOT CHANGE CALLERS)
	 * =========================================================
	 */
	public static String irisVariationGenerator(String contextKey, int currentScenarioNumber, int impressionToPick)
			throws Exception {

		String inputDir = System.getProperty("java.io.tmpdir")
				+ VariableManager.getVariableValue(contextKey, "mosip.test.persona.irisdatapath") + "/"
				+ String.format("%03d", impressionToPick);

		String outputDir = System.getProperty("java.io.tmpdir")
				+ VariableManager.getVariableValue(contextKey, "mosip.test.persona.irisdatapath") + "/output/"
				+ currentScenarioNumber;

		Files.createDirectories(Paths.get(outputDir));

		List<Path> inputs = Files.list(Paths.get(inputDir)).filter(Files::isRegularFile).toList();

		for (Path iris : inputs) {
			generateNonMatchingIris(iris, outputDir, "iris", iris.getFileName().toString());
		}

		return outputDir;
	}

	/*
	 * ========================================================= CORE NON-MATCHING
	 * IRIS ENGINE =========================================================
	 */
	private static void generateNonMatchingIris(Path input, String outputDir, String irisPath, String originalName)
			throws Exception {

		BufferedImage base = ImageIO.read(input.toFile());
		if (base == null)
			throw new IllegalStateException("Unable to read iris image: " + input.toString());

		SecureRandom r = SecureRandom.getInstanceStrong();
		BufferedImage img = copy(base);

		// 1️⃣ STRONG ROTATION (breaks normalization)
		rotate(img, r);

		// 2️⃣ RADIAL BAND ERASURE (destroys iris rings)
		eraseRadialBands(img, r);

		// 3️⃣ SECTOR MASKING (removes texture wedges)
		maskSectors(img, r);

		// 4️⃣ PUPIL DEFORMATION (breaks inner boundary)
		deformPupil(img, r);

		// 5️⃣ LOCAL PHASE DESTRUCTION
		localInvert(img, r);

		String uniqueName = "NONMATCH_" + System.currentTimeMillis() + "_" + UUID.randomUUID() + "_" + originalName;

		Path outDir = Paths.get(outputDir, irisPath);
		Files.createDirectories(outDir);

		Path out = outDir.resolve(uniqueName);
		ImageIO.write(img, "png", out.toFile());

		logger.info("[IRIS-NONMATCH] " + sha256(Files.readAllBytes(out)));
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

	private static void eraseRadialBands(BufferedImage img, SecureRandom r) {
		Graphics2D g = img.createGraphics();
		g.setColor(Color.BLACK);

		int cx = img.getWidth() / 2;
		int cy = img.getHeight() / 2;

		int bands = 3 + r.nextInt(3);
		for (int i = 0; i < bands; i++) {
			int radius = img.getWidth() / 6 + r.nextInt(img.getWidth() / 4);
			int thickness = 15 + r.nextInt(25);
			g.setStroke(new BasicStroke(thickness));
			g.drawOval(cx - radius, cy - radius, radius * 2, radius * 2);
		}
		g.dispose();
	}

	private static void maskSectors(BufferedImage img, SecureRandom r) {
		Graphics2D g = img.createGraphics();
		g.setColor(Color.BLACK);

		int cx = img.getWidth() / 2;
		int cy = img.getHeight() / 2;

		int sectors = 2 + r.nextInt(3);
		for (int i = 0; i < sectors; i++) {
			int start = r.nextInt(360);
			int extent = 40 + r.nextInt(60);
			g.fillArc(cx - img.getWidth() / 2, cy - img.getHeight() / 2, img.getWidth(), img.getHeight(), start,
					extent);
		}
		g.dispose();
	}

	private static void deformPupil(BufferedImage img, SecureRandom r) {
		Graphics2D g = img.createGraphics();
		g.setColor(Color.BLACK);

		int cx = img.getWidth() / 2;
		int cy = img.getHeight() / 2;

		int rx = img.getWidth() / 8 + r.nextInt(20);
		int ry = img.getHeight() / 10 + r.nextInt(20);

		g.fillOval(cx - rx, cy - ry, rx * 2, ry * 2);
		g.dispose();
	}

	private static void localInvert(BufferedImage img, SecureRandom r) {
		for (int i = 0; i < 6000; i++) {
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
