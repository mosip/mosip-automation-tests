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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.mosip.testrig.dslrig.dataprovider.util.CommonUtil;
import io.mosip.testrig.dslrig.dataprovider.variables.VariableManager;

public class PhotoProvider {
	private static final Logger logger = LoggerFactory.getLogger(PhotoProvider.class);
	static String Photo_File_Format = "/face%04d.jpg";

	static byte[][] getPhoto(String contextKey) {

		byte[] bencoded = null;
		byte[] bData = null;
		try {

			String dirPath = System.getProperty("java.io.tmpdir")
					+ VariableManager.getVariableValue(contextKey, "mosip.test.persona.facedatapath").toString();

			File dir = new File(dirPath);
			FileFilter filter = new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					return !pathname.isDirectory( ) && pathname.getName().startsWith("face");
				}
			};
			File[] listDir = dir.listFiles(filter);
			int numberOfSubfolders = listDir.length;

			int min = 1;
			int max = numberOfSubfolders;
			int randomNumber = (int) (Math.random() * (max - min)) + min;
			String beforescenario = VariableManager.getVariableValue(contextKey, "scenario").toString();
			String afterscenario = beforescenario.substring(0, beforescenario.indexOf(':'));

			int currentScenarioNumber = Integer.valueOf(afterscenario);

			// If the available impressions are less than scenario number, pick the random
			// one

			// otherwise pick the impression of same of scenario number
			int impressionToPick = (currentScenarioNumber < numberOfSubfolders) ? currentScenarioNumber : randomNumber;
			
			FaceVariationGenerator.faceVariationGenerator(contextKey, currentScenarioNumber, impressionToPick);
			
			logger.info("currentScenarioNumber=" + currentScenarioNumber + " numberOfSubfolders=" + numberOfSubfolders
					+ " impressionToPick=" + impressionToPick);
			
			List<File> firstSet = CommonUtil.listFiles(dirPath+"/output/"+currentScenarioNumber + "/face_data/");
			
			List<File> filteredFiles = firstSet.stream().filter(file -> file.getName().contains("00"+impressionToPick)).toList();

			Object val = VariableManager.getVariableValue(VariableManager.NS_DEFAULT, "enableExternalBiometricSource");
			boolean bExternalSrc = false;
			BufferedImage img = null;

			if (val != null)
				bExternalSrc = Boolean.valueOf(val.toString());

			if (bExternalSrc) {
				// folder where all bio input available
				String bioSrc = VariableManager.getVariableValue(VariableManager.NS_DEFAULT, "externalBiometricsource")
						.toString();
				try (FileInputStream fos = new FileInputStream(bioSrc + "Face.jp2");
						BufferedInputStream bis = new BufferedInputStream(fos)) {
					img = ImageIO.read(bis);
				}
			} else {

				try (FileInputStream fos = new FileInputStream(filteredFiles.get(0));
						BufferedInputStream bis = new BufferedInputStream(fos)) {
					img = ImageIO.read(bis);
					logger.info("Image picked from this path=" + filteredFiles.get(0));
				}
			}
			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			ImageIO.write(img, "jpg", baos);
			baos.flush();
			bData = baos.toByteArray();
			bencoded = encodeFaceImageData(bData);

			baos.close();

		} catch (Exception e) {

			logger.error(e.getMessage());
		}
		return new byte[][] { bencoded, bData };
	}

	static void splitImages(String contextKey) {
		/// 125 x129
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

//		try (DataOutputStream dout = new DataOutputStream(baos)) {

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
//			img = ImageIO.read(new File(faceFile));
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
