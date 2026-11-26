package io.mosip.testrig.dslrig.dataprovider;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.AlphaComposite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.mosip.testrig.dslrig.dataprovider.util.CommonUtil;
import io.mosip.testrig.dslrig.dataprovider.util.PacketSizeUtil;
import io.mosip.testrig.dslrig.dataprovider.variables.VariableManager;

public class PhotoProvider {
	private static final Logger logger = LoggerFactory.getLogger(PhotoProvider.class);
	/** Minimum JPEG bytes for large-face persona biometrics (packet zip is padded separately in {@link PacketSizeUtil}). */
	private static final int LARGE_FACE_MIN_JPEG_BYTES = PacketSizeUtil.DEFAULT_MIN_PACKET_BYTES;
	/**
	 * Large-face negative tests only need byte length > max packet size. Prefer COM-segment
	 * padding over BufferedImage upscale — upscaling under full-suite concurrency OOMs the
	 * packet-creator JVM (Scenario_241) and cascades to later scenarios (e.g. Scenario_181).
	 */
	private static final Object LARGE_FACE_LOCK = new Object();
	static String Photo_File_Format = "/face%04d.jpg";

	static byte[][] getPhoto(String contextKey) {
		return getPhoto(contextKey, false, false);
	}

	static byte[][] getPhoto(String contextKey, boolean generateLargeFace) {
		return getPhoto(contextKey, generateLargeFace, false);
	}

	public static byte[][] getPhoto(String contextKey, boolean generateLargeFace, boolean generateObstructedFace) {

		byte[] bencoded = null;
		byte[] bData = null;
		try {

			String templateDirPath = System.getProperty("java.io.tmpdir")
					+ VariableManager.getVariableValue(contextKey, "mosip.test.persona.facedatapath").toString();

			File dir = new File(templateDirPath);
			FileFilter filter = new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					return !pathname.isDirectory() && pathname.getName().startsWith("face");
				}
			};
			File[] listDir = dir.listFiles(filter);
			if (listDir == null || listDir.length == 0) {
				throw new IllegalStateException("No face templates found in " + templateDirPath);
			}
			int numberOfSubfolders = listDir.length;

			String beforescenario = VariableManager.getVariableValue(contextKey, "scenario").toString();
			String afterscenario = beforescenario.contains(":")
					? beforescenario.substring(0, beforescenario.indexOf(':'))
					: beforescenario;
			if (afterscenario.contains("_")) {
				afterscenario = afterscenario.replace("_", "0");
			}
			int currentScenarioNumber = Integer.valueOf(afterscenario);

			// Deterministic impression pick — Math.random() caused biometric collisions under parallel runs.
			int impressionToPick = Math.floorMod(currentScenarioNumber - 1, numberOfSubfolders) + 1;

			// Returns scenario output directory; face file is under <dir>/face/
			String outputDirPath = FaceVariationGenerator.faceVariationGenerator(contextKey, currentScenarioNumber,
					impressionToPick);

			logger.info("currentScenarioNumber=" + currentScenarioNumber + " numberOfSubfolders=" + numberOfSubfolders
					+ " impressionToPick=" + impressionToPick + " outputDir=" + outputDirPath);

			List<File> generatedFaces = CommonUtil.listFiles(outputDirPath + "/face/");
			if (generatedFaces == null || generatedFaces.isEmpty()) {
				throw new IllegalStateException("No generated face file under " + outputDirPath + "/face/");
			}

			Object val = VariableManager.getVariableValue(VariableManager.NS_DEFAULT, "enableExternalBiometricSource");
			boolean bExternalSrc = false;
			BufferedImage img = null;

			if (val != null)
				bExternalSrc = Boolean.valueOf(val.toString());

			if (bExternalSrc) {

				String bioSrc = VariableManager.getVariableValue(VariableManager.NS_DEFAULT, "externalBiometricsource")
						.toString();
				try (FileInputStream fos = new FileInputStream(bioSrc + "Face.jp2");
						BufferedInputStream bis = new BufferedInputStream(fos)) {
					img = ImageIO.read(bis);
				}
			} else {
				File faceFile = generatedFaces.get(0);
				try (FileInputStream fos = new FileInputStream(faceFile);
						BufferedInputStream bis = new BufferedInputStream(fos)) {
					img = ImageIO.read(bis);
					logger.info("Image picked from this path=" + faceFile.getAbsolutePath());
				}
			}
			if (img == null) {
				throw new IllegalStateException("Failed to decode face image");
			}
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			BufferedImage sourceImage = img;

			if (generateLargeFace) {
				synchronized (LARGE_FACE_LOCK) {
					baos.reset();
					ImageIO.write(sourceImage, "jpg", baos);
					baos.flush();
					bData = padJpegToMinSize(baos.toByteArray(), LARGE_FACE_MIN_JPEG_BYTES);
					logger.info("Large face JPEG padded to {} bytes (no upscale)", bData.length);
				}
			} else {
				if (generateObstructedFace) {
					img = applyFaceObstruction(img);
				}
				ImageIO.write(img, "jpg", baos);
				baos.flush();
				bData = baos.toByteArray();
			}
			bencoded = encodeFaceImageData(bData);

			baos.close();
			// Only delete the per-scenario output folder — never the shared face templates.
			CommonUtil.deleteOldTempDir(outputDirPath);

		} catch (Exception e) {

			logger.error("getPhoto failed: {}", e.getMessage(), e);
			throw new IllegalStateException("getPhoto failed: " + e.getMessage(), e);
		}
		return new byte[][] { bencoded, bData };
	}

	/**
	 * Inflates JPEG payload size via COM segments without allocating larger BufferedImages.
	 * Used for negative packet-size tests that only need byte length to exceed the processor limit.
	 */
	static byte[] padJpegToMinSize(byte[] jpeg, int minBytes) {
		if (jpeg == null || jpeg.length >= minBytes) {
			return jpeg;
		}
		int eoi = jpeg.length - 2;
		while (eoi > 0 && !(jpeg[eoi] == (byte) 0xFF && jpeg[eoi + 1] == (byte) 0xD9)) {
			eoi--;
		}
		boolean hasEoi = eoi > 0;
		int insertAt = hasEoi ? eoi : jpeg.length;
		int need = minBytes - jpeg.length;
		ByteArrayOutputStream out = new ByteArrayOutputStream(minBytes + 64);
		out.write(jpeg, 0, insertAt);
		int remaining = need;
		while (remaining > 0) {
			int chunk = Math.min(remaining, 65533);
			int segmentLength = chunk + 2;
			out.write(0xFF);
			out.write(0xFE);
			out.write((segmentLength >> 8) & 0xFF);
			out.write(segmentLength & 0xFF);
			for (int i = 0; i < chunk; i++) {
				out.write(0);
			}
			remaining -= chunk;
		}
		if (hasEoi) {
			out.write(jpeg, insertAt, jpeg.length - insertAt);
		} else {
			out.write(0xFF);
			out.write(0xD9);
		}
		return out.toByteArray();
	}

	private static BufferedImage applyFaceObstruction(BufferedImage source) {
		if (source == null) {
			throw new IllegalArgumentException("source image must not be null");
		}
		int type = source.getType() == 0 ? BufferedImage.TYPE_INT_RGB : source.getType();
		BufferedImage output = new BufferedImage(source.getWidth(), source.getHeight(), type);
		Graphics2D g2d = output.createGraphics();
		g2d.drawImage(source, 0, 0, null);

		int width = output.getWidth();
		int height = output.getHeight();


		g2d.setColor(new Color(28, 28, 28, 235));
		int maskY = (int) (height * 0.58);
		g2d.fillRoundRect((int) (width * 0.16), maskY, (int) (width * 0.68), (int) (height * 0.28), 30, 30);


		g2d.setColor(new Color(18, 18, 18, 245));
		g2d.fillRoundRect((int) (width * 0.12), (int) (height * 0.02), (int) (width * 0.76), (int) (height * 0.24), 26,
				26);
		g2d.fillRect((int) (width * 0.20), (int) (height * 0.21), (int) (width * 0.60), (int) (height * 0.06));


		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.72f));
		g2d.setColor(new Color(255, 255, 255));
		g2d.fillOval((int) (width * 0.55), (int) (height * 0.28), (int) (width * 0.26), (int) (height * 0.12));
		g2d.fillRoundRect((int) (width * 0.50), (int) (height * 0.34), (int) (width * 0.34), (int) (height * 0.04), 14,
				14);

		g2d.dispose();
		return output;
	}

	static void splitImages(String contextKey) {

		try {
			final BufferedImage source = ImageIO
					.read(new File(System.getProperty("java.io.tmpdir")
							+ VariableManager.getVariableValue(contextKey, "mosip.test.persona.facedatapath").toString()
							+ "/female/celebrities.jpg"));
			int idx = 0;
			for (int y = 0; y < source.getHeight() - 129; y += 129) {
				for (int x = 0; x < source.getWidth() - 125; x += 125) {
					ImageIO.write(source.getSubimage(x, y, 125, 129), "jpg",
							new File(System.getProperty("java.io.tmpdir")
									+ VariableManager.getVariableValue(contextKey, "mosip.test.persona.facedatapath")
											.toString()
									+ "/female/photo_" + idx++ + ".jpg"));
				}
			}
		} catch (IOException e) {
			logger.error(e.getMessage());
		}

	}

	static public byte[] encodeFaceImageData(byte[] faceImage) {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();


		try (BufferedOutputStream bos = new BufferedOutputStream(baos);
				DataOutputStream dout = new DataOutputStream(bos)) {

			byte[] format = new byte[4];

			dout.write(format);
			byte[] version = new byte[4];
			dout.write(version);
			int recordLength = 100;
			dout.writeInt(recordLength);

			short numberofRepresentionRecord = 1;
			dout.writeShort(numberofRepresentionRecord);

			byte certificationFlag = 1;
			dout.writeByte(certificationFlag);

			byte[] temporalSequence = new byte[2];
			dout.write(temporalSequence);

			ByteArrayOutputStream rdoutArray = new ByteArrayOutputStream();

			try (DataOutputStream rdout = new DataOutputStream(rdoutArray)) {
				byte[] captureDetails = new byte[14];
				rdout.write(captureDetails, 0, 14);
				byte noOfQualityBlocks = 0;
				rdout.writeByte(noOfQualityBlocks);

				short noOfLandmarkPoints = 0;
				rdout.writeShort(noOfLandmarkPoints);
				byte[] facialInformation = new byte[15];
				rdout.write(facialInformation, 0, 15);

				byte faceType = 1;
				rdout.writeByte(faceType);
				byte imageDataType = 0;
				rdout.writeByte(imageDataType);

				byte[] otherImageInformation = new byte[9];
				rdout.write(otherImageInformation, 0, otherImageInformation.length);
				int lengthOfImageData = faceImage.length;
				rdout.writeInt(lengthOfImageData);

				rdout.write(faceImage, 0, lengthOfImageData);

			}
			byte[] representationData = rdoutArray.toByteArray();
			int representationLength = representationData.length + 4;
			dout.writeInt(representationLength);

			dout.write(representationData, 0, representationData.length);

		} catch (Exception ex) {
			logger.error(ex.getMessage());
		}
		return baos.toByteArray();

	}

	public static void main(String[] args) throws IOException {

	}

	public static byte[][] loadPhoto(String faceFile) {

		byte[] bencoded = null;
		byte[] bData = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		BufferedImage img;
		try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(faceFile));) {

			img = ImageIO.read(bis);
			ImageIO.write(img, "jpg", baos);
			baos.flush();
			bData = baos.toByteArray();
			bencoded = encodeFaceImageData(bData);
			baos.close();

		} catch (IOException e) {
			logger.error(e.getMessage());
		}

		return new byte[][] { bencoded, bData };

	}
}
