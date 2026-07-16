package io.mosip.testrig.dslrig.dataprovider.biometric;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Paths;

import io.mosip.testrig.dslrig.dataprovider.FaceVariationGenerator;
import io.mosip.testrig.dslrig.dataprovider.FingerprintVariationGenerator;
import io.mosip.testrig.dslrig.dataprovider.IrisVariationGenerator;
import io.mosip.testrig.dslrig.dataprovider.PhotoProvider;
import io.mosip.testrig.dslrig.dataprovider.models.BiometricDataModel;
import io.mosip.testrig.dslrig.dataprovider.models.IrisDataModel;
import io.mosip.testrig.dslrig.dataprovider.models.ResidentModel;
import io.mosip.testrig.dslrig.dataprovider.util.CommonUtil;
import io.mosip.testrig.dslrig.dataprovider.util.DataProviderConstants;
import io.mosip.testrig.dslrig.dataprovider.util.RestClient;
import io.mosip.testrig.dslrig.dataprovider.variables.VariableManager;

public final class FileBiometricLoader {

	private static final Logger logger = LoggerFactory.getLogger(FileBiometricLoader.class);

	private static final String DIRPATH = "dirPath ";
	private static final String SCENARIO = "scenario";

	private static final Map<String, Integer> FINGER_INDEX_MAP = Map.of("leftthumb", 0, "leftindex", 1, "leftmiddle",
			2, "leftring", 3, "leftlittle", 4, "rightthumb", 5, "rightindex", 6, "rightmiddle", 7, "rightring", 8,
			"rightlittle", 9);

	/** Deterministic impression index in [1, folderCount] from scenario id — safe under parallel runs. */
	static int deterministicImpression(int scenarioNumber, int folderCount) {
		if (folderCount <= 0) {
			return 1;
		}
		return Math.floorMod(scenarioNumber - 1, folderCount) + 1;
	}

	static int parseScenarioNumber(String contextKey) {
		String beforescenario = VariableManager.getVariableValue(contextKey, SCENARIO).toString();
		String afterscenario = beforescenario.contains(":")
				? beforescenario.substring(0, beforescenario.indexOf(':'))
				: beforescenario;
		if (afterscenario.contains("_")) {
			afterscenario = afterscenario.replace("_", "0");
		}
		return Integer.valueOf(afterscenario);
	}

	private FileBiometricLoader() {
	}

	public static Hashtable<Integer, List<File>> impressionCaptureList(String contextKey) {

		String dirPath = System.getProperty("java.io.tmpdir")
				+ VariableManager.getVariableValue(contextKey, "mosip.test.persona.fingerprintdatapath").toString();
		RestClient.logInfo(contextKey, DIRPATH + dirPath);
		Hashtable<Integer, List<File>> tblFiles = new Hashtable<Integer, List<File>>();
		File dir = new File(dirPath);

		File listDir[] = dir.listFiles();
		if (listDir == null || listDir.length == 0) {
			logger.error("No fingerprint directories found in {}", dirPath);
			return tblFiles;
		}
		int numberOfSubfolders = listDir.length;

		int currentScenarioNumber = parseScenarioNumber(contextKey);
		int impressionToPick = deterministicImpression(currentScenarioNumber, numberOfSubfolders);

		RestClient.logInfo(contextKey, "currentScenarioNumber=" + currentScenarioNumber + " numberOfSubfolders="
				+ numberOfSubfolders + " impressionToPick=" + impressionToPick);
		List<File> lst = new LinkedList<File>();
		lst = CommonUtil.listFiles(dirPath + String.format("/Impression_%d/fp_1/", impressionToPick));
		tblFiles.put(impressionToPick, lst);
		return tblFiles;
	}

	public static BiometricDataModel getBiometricData(Boolean bFinger, String contextKey) throws Exception {

		BiometricDataModel data = new BiometricDataModel();

		if (bFinger) {

			Object val = VariableManager.getVariableValue(VariableManager.NS_DEFAULT, "enableExternalBiometricSource");
			boolean bExternalSrc = false;
			if (val != null)
				bExternalSrc = Boolean.valueOf(val.toString());
			if (bExternalSrc) {

				String bioSrc = VariableManager.getVariableValue(VariableManager.NS_DEFAULT, "externalBiometricsource")
						.toString();

				String[] fingerPrints = new String[10];
				String[] fingerPrintHash = new String[10];
				byte[][] fingerPrintRaw = new byte[10][1];

				for (int i = 0; i < 10; i++) {
					String modalityName = DataProviderConstants.MDSProfileFingerNames[i];
					modalityName = modalityName.replace('_', ' ');
					String fPath = bioSrc + modalityName + ".jp2";
					byte[] fdata = CommonUtil.read(fPath);
					fingerPrintRaw[i] = fdata;
					fingerPrints[i] = Base64.getEncoder().encodeToString(fdata);
					try {
						fingerPrintHash[i] = CommonUtil.getHexEncodedHash(fdata);

					} catch (Exception e) {
						logger.error(e.getMessage());
					}
					data.setFingerPrint(fingerPrints);
					data.setFingerHash(fingerPrintHash);
					data.setFingerRaw(fingerPrintRaw);

				}

				return data;
			}

			String dirPath = System.getProperty("java.io.tmpdir") + VariableManager
					.getVariableValue(contextKey, "mosip.test.persona.fingerprintdatapath").toString();
			RestClient.logInfo(contextKey, DIRPATH + dirPath);
			File dir = new File(dirPath);

			File listDir[] = null;
			if (dir.isDirectory()) {

				listDir = dir.listFiles(new FileFilter() {
					@Override
					public boolean accept(File file) {

						return file.isDirectory() && file.getName().startsWith("Impression");
					}
				});
			} else {
				logger.error(dirPath + " is not a directory.");
			}
			if (listDir == null || listDir.length == 0) {
				logger.error("No impression directories found in {}", dirPath);
				return data;
			}
			int numberOfSubfolders = listDir.length;

			int currentScenarioNumber = parseScenarioNumber(contextKey);
			int impressionToPick = deterministicImpression(currentScenarioNumber, numberOfSubfolders);
			dirPath = FingerprintVariationGenerator.fingerprintVariationGenerator(contextKey, currentScenarioNumber,
					impressionToPick);

			RestClient.logInfo(contextKey, "currentScenarioNumber=" + currentScenarioNumber + " numberOfSubfolders="
					+ numberOfSubfolders + " impressionToPick=" + impressionToPick);

			List<File> firstSet = CommonUtil.listFiles(dirPath + "/fp/");

			String[] fingerPrints = new String[10];
			String[] fingerPrintHash = new String[10];
			byte[][] fingerPrintRaw = new byte[10][1];
			RestClient.logInfo(contextKey, "Impression used " + impressionToPick);

			int index = 0;
			for (File f : firstSet) {

				if (index > 9)
					break;

				byte[] fdata;
				try {
					fdata = CommonUtil.read(f.getAbsolutePath());
					fingerPrintRaw[index] = fdata;
					fingerPrints[index] = Base64.getEncoder().encodeToString(fdata);

					fingerPrintHash[index] = CommonUtil.getHexEncodedHash(fdata);

				} catch (Exception e) {
					logger.error(e.getMessage());
				}
				index++;

			}
			data.setFingerPrint(fingerPrints);
			data.setFingerHash(fingerPrintHash);
			data.setFingerRaw(fingerPrintRaw);
			CommonUtil.deleteOldTempDir(dirPath);
		}

		return data;
	}

	public static BiometricDataModel updateFingerData(String contextKey) throws Exception {

		BiometricDataModel data = new BiometricDataModel();

		String dirPath = System.getProperty("java.io.tmpdir")
				+ VariableManager.getVariableValue(contextKey, "mosip.test.persona.fingerprintdatapath").toString();
		RestClient.logInfo(contextKey, DIRPATH + dirPath);
		File dir = new File(dirPath);

		File listDir[] = null;
		if (dir.isDirectory()) {

			listDir = dir.listFiles(new FileFilter() {
				@Override
				public boolean accept(File file) {

					return file.isDirectory() && file.getName().startsWith("Impression");
				}
			});
		} else {
			logger.error(dirPath + " is not a directory.");
		}
		if (listDir == null || listDir.length == 0) {
			logger.error("No impression directories found in {}", dirPath);
			return data;
		}
		int numberOfSubfolders = listDir.length;

		int currentScenarioNumber = parseScenarioNumber(contextKey);
		int impressionToPick = deterministicImpression(currentScenarioNumber, numberOfSubfolders);

		dirPath = FingerprintVariationGenerator.fingerprintVariationGenerator(contextKey, currentScenarioNumber,
				impressionToPick);

		List<File> firstSet = CommonUtil.listFiles(dirPath + "/fp/");

		String[] fingerPrints = new String[10];
		String[] fingerPrintHash = new String[10];
		byte[][] fingerPrintRaw = new byte[10][1];
		RestClient.logInfo(contextKey, "Impression used " + impressionToPick);

		int index = 0;
		for (File f : firstSet) {

			if (index > 9)
				break;

			byte[] fdata;
			try {
				fdata = CommonUtil.read(f.getAbsolutePath());
				fingerPrintRaw[index] = fdata;
				fingerPrints[index] = Base64.getEncoder().encodeToString(fdata);

				fingerPrintHash[index] = CommonUtil.getHexEncodedHash(fdata);

			} catch (Exception e) {
				logger.error(e.getMessage());
			}
			index++;

		}
		data.setFingerPrint(fingerPrints);
		data.setFingerHash(fingerPrintHash);
		data.setFingerRaw(fingerPrintRaw);
		CommonUtil.deleteOldTempDir(dirPath);

		return data;
	}

	public static BiometricDataModel updateSelectedFingerData(ResidentModel model, String contextKey,
			String fingerArgument) throws Exception {

		BiometricDataModel data = model.getBiometric();

		String[] fingerPrints = data.getFingerPrint();
		String[] fingerPrintHash = data.getFingerHash();
		byte[][] fingerPrintRaw = data.getFingerRaw();

		if (fingerPrints == null)
			fingerPrints = new String[10];
		if (fingerPrintHash == null)
			fingerPrintHash = new String[10];
		if (fingerPrintRaw == null)
			fingerPrintRaw = new byte[10][1];

		String dirPath = System.getProperty("java.io.tmpdir")
				+ VariableManager.getVariableValue(contextKey, "mosip.test.persona.fingerprintdatapath").toString();

		RestClient.logInfo(contextKey, DIRPATH + dirPath);

		File dir = new File(dirPath);
		if (!dir.isDirectory()) {
			logger.error(dirPath + " is not a directory.");
			return data;
		}

		File[] listDir = dir.listFiles(f -> f.isDirectory() && f.getName().startsWith("Impression"));
		if (listDir == null || listDir.length == 0) {
			logger.error("No impression directories found in {}", dirPath);
			return data;
		}
		int numberOfSubfolders = listDir.length;
		int currentScenarioNumber = parseScenarioNumber(contextKey);
		int impressionToPick = deterministicImpression(currentScenarioNumber, numberOfSubfolders);

		RestClient.logInfo(contextKey, "Impression used " + impressionToPick);

		dirPath = FingerprintVariationGenerator.fingerprintVariationGenerator(contextKey, currentScenarioNumber,
				impressionToPick);

		List<File> firstSet = CommonUtil.listFiles(dirPath + "/fp/");
		Collections.sort(firstSet, Comparator.comparing(File::getName));

		Set<Integer> indexesToUpdate = Arrays.stream(fingerArgument.split(",")).map(String::trim)
				.map(String::toLowerCase).map(FINGER_INDEX_MAP::get).filter(Objects::nonNull)
				.collect(Collectors.toSet());

		for (Integer index : indexesToUpdate) {
			if (index >= firstSet.size()) {
				logger.warn("Fingerprint file not found for index {}", index);
				continue;
			}

			try {
				File f = firstSet.get(index);
				byte[] fdata = CommonUtil.read(f.getAbsolutePath());

				fingerPrintRaw[index] = fdata;
				fingerPrints[index] = Base64.getEncoder().encodeToString(fdata);
				fingerPrintHash[index] = CommonUtil.getHexEncodedHash(fdata);

				RestClient.logInfo(contextKey, "Updated finger index " + index);

			} catch (Exception e) {
				logger.error("Error updating finger index " + index, e);
			}
		}

		data.setFingerPrint(fingerPrints);
		data.setFingerHash(fingerPrintHash);
		data.setFingerRaw(fingerPrintRaw);

		CommonUtil.deleteOldTempDir(dirPath);
		return data;
	}

	public static IrisDataModel loadIris(String filePath, String subModality, IrisDataModel im) throws Exception {

		IrisDataModel m = im;
		if (m == null)
			m = new IrisDataModel();
		String irisData = "";
		String irisHash = "";

		if (Files.exists(Paths.get(filePath))) {
			byte[] fdata = CommonUtil.read(filePath);
			irisData = Hex.encodeHexString(fdata);
			irisHash = CommonUtil.getHexEncodedHash(fdata);
			if (subModality.equals("left")) {
				m.setLeftHash(irisHash);
				m.setLeft(irisData);
			} else {
				m.setRightHash(irisHash);
				m.setRight(irisData);
			}
		}

		return m;
	}

	public static List<IrisDataModel> generateIris(int count, String contextKey) throws Exception {
		List<IrisDataModel> retVal = new ArrayList<IrisDataModel>();

		IrisDataModel m = new IrisDataModel();

		Object val = VariableManager.getVariableValue(VariableManager.NS_DEFAULT, "enableExternalBiometricSource");
		boolean bExternalSrc = false;

		if (val != null)
			bExternalSrc = Boolean.valueOf(val.toString());

		if (bExternalSrc) {

			String bioSrc = VariableManager.getVariableValue(VariableManager.NS_DEFAULT, "externalBiometricsource")
					.toString();
			String fPathL = bioSrc + "Left Iris.jp2";
			String fPathR = bioSrc + "Right Iris.jp2";

			String leftIrisData = "";
			String rightIrisData = "";
			String irisHash = "";
			byte[] fdata = null;
			byte[] frdata = null;
			if (Files.exists(Paths.get(fPathL))) {
				fdata = CommonUtil.read(fPathL);
				leftIrisData = Hex.encodeHexString(fdata);
				irisHash = CommonUtil.getHexEncodedHash(fdata);
				m.setLeftHash(irisHash);
			}
			if (Files.exists(Paths.get(fPathR))) {
				frdata = CommonUtil.read(fPathR);
				rightIrisData = Hex.encodeHexString(frdata);
				irisHash = CommonUtil.getHexEncodedHash(frdata);
				m.setRightHash(irisHash);
			}
			if (leftIrisData.equals("")) {
				fdata = frdata;
				leftIrisData = rightIrisData;
			} else if (rightIrisData.equals("")) {
				frdata = fdata;
				rightIrisData = leftIrisData;
			}
			m.setLeft(leftIrisData);
			m.setRight(rightIrisData);
			m.setRawLeft(fdata);
			m.setRawRight(frdata);
			retVal.add(m);
		} else {

			String srcPath = System.getProperty("java.io.tmpdir")
					+ VariableManager.getVariableValue(contextKey, "mosip.test.persona.irisdatapath").toString();

			int num = new File(srcPath).list().length;
			int[] index = CommonUtil.generateRandomNumbers(count, num, 1);
			String leftbmp = null;
			String rightbmp = null;

			RestClient.logInfo(contextKey, DIRPATH + srcPath);
			File dir = new File(srcPath);

			File listDir[] = dir.listFiles();
			if (listDir == null || listDir.length == 0) {
				logger.error("No iris directories found in {}", srcPath);
				return retVal;
			}
			int numberOfSubfolders = listDir.length;

			int currentScenarioNumber = parseScenarioNumber(contextKey);
			int impressionToPick = deterministicImpression(currentScenarioNumber, numberOfSubfolders);
			srcPath = IrisVariationGenerator.irisVariationGenerator(contextKey, currentScenarioNumber,
					impressionToPick);

			File folder = new File(srcPath + "/iris/");
			logger.info(srcPath + "/iris/");

			File[] listOfFiles = folder.listFiles();

			for (File file : listOfFiles) {
				if (file.getName().contains("L")) {
					leftbmp = file.getName();
				} else {
					rightbmp = file.getName();
				}
			}

			if (leftbmp == null) {
				leftbmp = rightbmp;
			}
			if (rightbmp == null) {
				rightbmp = leftbmp;
			}
			String fPathL = srcPath + "/" + "iris" + "/" + leftbmp;
			String fPathR = srcPath + "/" + "iris" + "/" + rightbmp;
			String leftIrisData = "";
			String rightIrisData = "";
			String irisHash = "";
			byte[] fldata = null;
			byte[] frdata = null;
			if (Files.exists(Paths.get(fPathL))) {
				fldata = CommonUtil.read(fPathL);
				leftIrisData = Hex.encodeHexString(fldata);
				irisHash = CommonUtil.getHexEncodedHash(fldata);
				m.setLeftHash(irisHash);
			}
			if (Files.exists(Paths.get(fPathR))) {
				frdata = CommonUtil.read(fPathR);
				rightIrisData = Hex.encodeHexString(frdata);
				irisHash = CommonUtil.getHexEncodedHash(frdata);
				m.setRightHash(irisHash);
			}
			if (leftIrisData.equals("")) {
				fldata = frdata;
				leftIrisData = rightIrisData;
			} else if (rightIrisData.equals("")) {
				frdata = fldata;
				rightIrisData = leftIrisData;
			}
			m.setLeft(leftIrisData);
			m.setRight(rightIrisData);
			m.setRawLeft(fldata);
			m.setRawRight(frdata);
			retVal.add(m);
			CommonUtil.deleteOldTempDir(srcPath);
		}

		return retVal;
	}

	public static List<IrisDataModel> updateIris(String contextKey) throws Exception {

		List<IrisDataModel> retVal = new ArrayList<IrisDataModel>();
		IrisDataModel m = new IrisDataModel();
		String srcPath = System.getProperty("java.io.tmpdir")
				+ VariableManager.getVariableValue(contextKey, "mosip.test.persona.irisdatapath").toString();
		String leftbmp = null;
		String rightbmp = null;

		RestClient.logInfo(contextKey, DIRPATH + srcPath);
		File dir = new File(srcPath);

		File listDir[] = dir.listFiles();
		if (listDir == null || listDir.length == 0) {
			logger.error("No iris directories found in {}", srcPath);
			return retVal;
		}
		int numberOfSubfolders = listDir.length;

		int currentScenarioNumber = parseScenarioNumber(contextKey);
		int impressionToPick = deterministicImpression(currentScenarioNumber, numberOfSubfolders);

		srcPath = IrisVariationGenerator.irisVariationGenerator(contextKey, currentScenarioNumber, impressionToPick);

		File folder = new File(srcPath + "/iris/");

		File[] listOfFiles = folder.listFiles();

		for (File file : listOfFiles) {
			if (file.getName().contains("L")) {
				leftbmp = file.getName();
			} else {
				rightbmp = file.getName();
			}
		}

		if (leftbmp == null) {
			leftbmp = rightbmp;
		}
		if (rightbmp == null) {
			rightbmp = leftbmp;
		}
		String fPathL = srcPath + "/iris/" + leftbmp;
		String fPathR = srcPath + "/iris/" + rightbmp;

		String leftIrisData = "";
		String rightIrisData = "";
		String irisHash = "";
		byte[] fldata = null;
		byte[] frdata = null;
		if (Files.exists(Paths.get(fPathL))) {
			fldata = CommonUtil.read(fPathL);
			leftIrisData = Hex.encodeHexString(fldata);
			irisHash = CommonUtil.getHexEncodedHash(fldata);
			m.setLeftHash(irisHash);
		}
		if (Files.exists(Paths.get(fPathR))) {
			frdata = CommonUtil.read(fPathR);
			rightIrisData = Hex.encodeHexString(frdata);
			irisHash = CommonUtil.getHexEncodedHash(frdata);
			m.setRightHash(irisHash);
		}
		if (leftIrisData.equals("")) {
			fldata = frdata;
			leftIrisData = rightIrisData;
		} else if (rightIrisData.equals("")) {
			frdata = fldata;
			rightIrisData = leftIrisData;
		}
		m.setLeft(leftIrisData);
		m.setRight(rightIrisData);
		m.setRawLeft(fldata);
		m.setRawRight(frdata);
		retVal.add(m);
		CommonUtil.deleteOldTempDir(srcPath);
		return retVal;
	}

	public static List<IrisDataModel> updateSelectedIris(ResidentModel model, String contextKey, String irisArgument)
			throws Exception {

		List<IrisDataModel> retVal = new ArrayList<>();

		IrisDataModel m = model.getBiometric().getIris();
		if (m == null) {
			m = new IrisDataModel();
		}

		String srcPath = System.getProperty("java.io.tmpdir")
				+ VariableManager.getVariableValue(contextKey, "mosip.test.persona.irisdatapath").toString();

		RestClient.logInfo(contextKey, DIRPATH + srcPath);

		File dir = new File(srcPath);
		File[] listDir = dir.listFiles();
		if (listDir == null || listDir.length == 0) {
			logger.error("No subdirectories found in {}", srcPath);
			retVal.add(m);
			return retVal;
		}
		int numberOfSubfolders = listDir.length;
		int currentScenarioNumber = parseScenarioNumber(contextKey);
		int impressionToPick = deterministicImpression(currentScenarioNumber, numberOfSubfolders);

		srcPath = IrisVariationGenerator.irisVariationGenerator(contextKey, currentScenarioNumber, impressionToPick);

		File folder = new File(srcPath + "/iris/");

		File[] listOfFiles = folder.listFiles();
		if (listOfFiles == null || listOfFiles.length == 0) {
			logger.warn("No iris files found in folder {}", folder.getAbsolutePath());
			retVal.add(m);
			return retVal;
		}
		String leftbmp = null;
		String rightbmp = null;

		for (File file : listOfFiles) {
			if (file.getName().contains("L")) {
				leftbmp = file.getName();
			} else {
				rightbmp = file.getName();
			}
		}

		String fPathL = leftbmp == null ? null
				: srcPath + "/iris/" + leftbmp;

		String fPathR = rightbmp == null ? null
				: srcPath + "/iris/" + rightbmp;

		Set<String> irisToUpdate = Arrays.stream(irisArgument.split(",")).map(String::trim).map(String::toLowerCase)
				.collect(Collectors.toSet());

		if (irisToUpdate.contains("leftiris") && fPathL != null && Files.exists(Paths.get(fPathL))) {

			byte[] fldata = CommonUtil.read(fPathL);
			m.setLeft(Hex.encodeHexString(fldata));
			m.setLeftHash(CommonUtil.getHexEncodedHash(fldata));
			m.setRawLeft(fldata);
		}

		if (irisToUpdate.contains("rightiris") && fPathR != null && Files.exists(Paths.get(fPathR))) {

			byte[] frdata = CommonUtil.read(fPathR);
			m.setRight(Hex.encodeHexString(frdata));
			m.setRightHash(CommonUtil.getHexEncodedHash(frdata));
			m.setRawRight(frdata);
		}

		retVal.add(m);
		CommonUtil.deleteOldTempDir(srcPath);
		return retVal;
	}

	public static byte[][] updateFaceData(String contextKey) {

		byte[] bencoded = null;
		byte[] bData = null;
		try {

			String dirPath = System.getProperty("java.io.tmpdir")
					+ VariableManager.getVariableValue(contextKey, "mosip.test.persona.facedatapath").toString();

			File dir = new File(dirPath);
			FileFilter filter = new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					return !pathname.isDirectory();
				}
			};
			File[] listDir = dir.listFiles(filter);
			if (listDir == null || listDir.length == 0) {
				logger.error("No face files found in {}", dirPath);
				return new byte[][] { null, null };
			}
			int numberOfSubfolders = listDir.length;

			int currentScenarioNumber = parseScenarioNumber(contextKey);
			int impressionToPick = deterministicImpression(currentScenarioNumber, numberOfSubfolders);

			String outputDirPath = FaceVariationGenerator.faceVariationGenerator(contextKey, currentScenarioNumber,
					impressionToPick);
			List<File> generatedFaces = CommonUtil.listFiles(outputDirPath + "/face/");
			if (generatedFaces == null || generatedFaces.isEmpty()) {
				logger.error("No generated face file under {}", outputDirPath + "/face/");
				return new byte[][] { null, null };
			}

			BufferedImage img = null;
			File faceFile = generatedFaces.get(0);

			try (FileInputStream fos = new FileInputStream(faceFile);
					BufferedInputStream bis = new BufferedInputStream(fos)) {
				img = ImageIO.read(bis);
				logger.info("Image picked from this path=" + faceFile.getAbsolutePath());
			}
			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			ImageIO.write(img, "jpg", baos);
			baos.flush();
			bData = baos.toByteArray();
			bencoded = PhotoProvider.encodeFaceImageData(bData);

			baos.close();
			CommonUtil.deleteOldTempDir(outputDirPath);
		} catch (Exception e) {

			logger.error(e.getMessage());
		}
		return new byte[][] { bencoded, bData };
	}

	public static File[] getRandomIrisVariation(File[] listOfFiles) {
		if (listOfFiles == null || listOfFiles.length == 0) {
			logger.warn("No image files found in the directory.");
			return null;
		}

		Map<String, File> leftIrisImages = new HashMap<>();
		Map<String, File> rightIrisImages = new HashMap<>();

		for (File file : listOfFiles) {
			String fileName = file.getName();
			if (fileName.endsWith("_L.bmp")) {
				String baseName = fileName.replaceAll("_[0-9]+_L\\.bmp", "").replace("_L.bmp", "");
				leftIrisImages.put(baseName, file);
			} else if (fileName.endsWith("_R.bmp")) {
				String baseName = fileName.replaceAll("_[0-9]+_R\\.bmp", "").replace("_R.bmp", "");
				rightIrisImages.put(baseName, file);
			}
		}

		List<String> commonNames = new ArrayList<>(leftIrisImages.keySet());
		commonNames.retainAll(rightIrisImages.keySet());

		if (commonNames.isEmpty()) {
			logger.warn("No matching left and right iris images found.");
			return null;
		}

		Random random = new Random();
		String selectedName = commonNames.get(random.nextInt(commonNames.size()));

		File selectedLeftImage = leftIrisImages.get(selectedName);
		File selectedRightImage = rightIrisImages.get(selectedName);

		File[] selectedImages = { selectedLeftImage, selectedRightImage };

		logger.info("Selected Left Iris Image: {}", selectedLeftImage.getAbsolutePath());
		logger.info("Selected Right Iris Image: {}", selectedRightImage.getAbsolutePath());

		return selectedImages;
	}
}
