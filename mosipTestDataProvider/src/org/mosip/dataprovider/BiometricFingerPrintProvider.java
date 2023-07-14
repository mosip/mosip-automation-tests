package org.mosip.dataprovider;

import java.io.BufferedReader;
import java.io.File;
//import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
//import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
//import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
//import java.time.LocalDateTime;
//import java.time.ZoneId;
import java.util.ArrayList;
//import java.util.Arrays;
import java.util.Base64;
import java.util.Hashtable;
import java.util.List;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.codec.binary.Hex;
//import org.apache.commons.io.IOUtils;
//import org.apache.commons.lang3.tuple.Pair;
import org.mosip.dataprovider.models.BiometricDataModel;
import org.mosip.dataprovider.models.IrisDataModel;
import org.mosip.dataprovider.util.CommonUtil;
import org.mosip.dataprovider.util.DataProviderConstants;
import org.mosip.dataprovider.util.FPClassDistribution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jamesmurty.utils.XMLBuilder;
//import java.util.Date;

import variables.VariableManager;

public class BiometricFingerPrintProvider {
	private static final Logger logger = LoggerFactory.getLogger(BiometricFingerPrintProvider.class);

	// String constants
	private static final String XMLNS = "xmlns";
	private static final String VERSION = "Version";
	private static final String MAJOR = "Major";
	private static final String MINOR = "Minor";
	private static final String CBEFFVERSION = "CBEFFVersion";
	private static final String BIRINFO = "BIRInfo";
	private static final String FALSE = "false";
	private static final String INTEGRITY = "Integrity";
	private static final String BDBINFO = "BDBInfo";
	private static final String ORGANIZATION = "Organization";
	private static final String MOSIP = "Mosip";
	private static final String FORMAT = "Format";
	private static final String CREATIONDATE = "CreationDate";
	private static final String LEVEL = "Level";
	private static final String ENROLL = "Enroll";
	private static final String PURPOSE = "Purpose";
	private static final String ALGORITHM = "Algorithm";
	private static final String SCORE = "Score";
	private static final String SHA25 = "SHA-25";
	private static final String QUALITY = "Quality";

	public static String[] fingerName = { "Left Thumb", "Left IndexFinger", "Left MiddleFinger", "Left RingFinger",
			"Left LittleFinger", "Right Thumb", "Right IndexFinger", "Right MiddleFinger", "Right RingFinger",
			"Right LittleFinger" };

	static String buildBirIris(String irisInfo, String irisName) throws ParserConfigurationException,
			FactoryConfigurationError, TransformerException, FileNotFoundException {
		String today = CommonUtil.getUTCDateTime(null);
		XMLBuilder builder = XMLBuilder.create("BIR").a(XMLNS, "http://standards.iso.org/iso-iec/19785/-3/ed-2/")
				.e(VERSION).e(MAJOR).t("1").up().e(MINOR).t("1").up().up().e(CBEFFVERSION).e(MAJOR).t("1").up().e(MINOR)
				.t("1").up().up().e(BIRINFO).e(INTEGRITY).t(FALSE).up().up().e(BDBINFO).e(FORMAT).e(ORGANIZATION)
				.t(MOSIP).up().e("Type").t("9").up().up().e(CREATIONDATE).t(today).up().e("Type").t("Iris").up()
				.e("Subtype").t(irisName).up().e(LEVEL).t("Raw").up().e(PURPOSE).t(ENROLL).up().e(QUALITY).e(ALGORITHM)
				.e(ORGANIZATION).t("HMAC").up().e("Type").t("SHA-256").up().up().e(SCORE).t("100").up().up().up()
				.e("BDB").t(irisInfo).up().up();

		return builder.asString(null);
	}

	static String buildBirFinger(String fingerInfo, String fingerName) throws ParserConfigurationException,
			FactoryConfigurationError, TransformerException, FileNotFoundException {
		String today = CommonUtil.getUTCDateTime(null);

		XMLBuilder builder = XMLBuilder.create("BIR").a(XMLNS, "http://standards.iso.org/iso-iec/19785/-3/ed-2/")
				.e(VERSION).e(MAJOR).t("1").up().e(MINOR).t("1").up().up().e(CBEFFVERSION).e(MAJOR).t("1").up().e(MINOR)
				.t("1").up().up().e(BIRINFO).e(INTEGRITY).t(FALSE).up().up().e(BDBINFO).e(FORMAT).e(ORGANIZATION)
				.t(MOSIP).up().e("Type").t("7").up().up().e(CREATIONDATE).t(today).up().e("Type").t("Finger").up()
				.e("Subtype").t(fingerName).up().e(LEVEL).t("Raw").up().e(PURPOSE).t(ENROLL).up().e(QUALITY)
				.e(ALGORITHM).e(ORGANIZATION).t("HMAC").up().e("Type").t("SHA-256").up().up().e(SCORE).t("100").up()
				.up().up().e("BDB").t(fingerInfo).up().up();

		return builder.asString(null);
	}

	static String buildBirFace(String faceInfo) throws ParserConfigurationException, FactoryConfigurationError,
			TransformerException, FileNotFoundException {
		String today = CommonUtil.getUTCDateTime(null);

		XMLBuilder builder = XMLBuilder.create("BIR").a(XMLNS, "http://standards.iso.org/iso-iec/19785/-3/ed-2/")
				.e(VERSION).e(MAJOR).t("1").up().e(MINOR).t("1").up().up().e(CBEFFVERSION).e(MAJOR).t("1").up().e(MINOR)
				.t("1").up().up().e(BIRINFO).e(INTEGRITY).t(FALSE).up().up().e(BDBINFO).e(FORMAT).e(ORGANIZATION)
				.t(MOSIP).up().e("Type").t("8").up().up().e(CREATIONDATE).t(today).up().e("Type").t("Face").up()
				.e(LEVEL).t("Raw").up().e(PURPOSE).t(ENROLL).up().e(QUALITY).e(ALGORITHM).e(ORGANIZATION).t("HMAC").up()
				.e("Type").t("SHA-256").up().up().e(SCORE).t("100").up().up().up().e("BDB").t(faceInfo).up().up();

		return builder.asString(null);
	}

	public static String toCBEFF(BiometricDataModel biometricDataModel, String toFile) throws Exception {
		String retXml = "";

		XMLBuilder builder = XMLBuilder.create("BIR").a(XMLNS, "http://standards.iso.org/iso-iec/19785/-3/ed-2/")

				.e(BIRINFO).e(INTEGRITY).t(FALSE).up().up();
		builder.getDocument().setXmlStandalone(true);

		// Step 1: convert finger print
		String[] fingerPrint = biometricDataModel.getFingerPrint();
		for (int i = 0; i < fingerPrint.length; i++) {
			String strFinger = fingerPrint[i];
			String strFingerXml = buildBirFinger(strFinger, fingerName[i]);
			XMLBuilder fbuilder = XMLBuilder.parse(strFingerXml);
			builder = builder.importXMLBuilder(fbuilder);
		}

		// Step 2: Add Face
		if (biometricDataModel.getEncodedPhoto() != null) {
			String faceXml = buildBirFace(biometricDataModel.getEncodedPhoto());
			builder = builder.importXMLBuilder(XMLBuilder.parse(faceXml));
		}

		// Step 3: Add IRIS
		IrisDataModel irisInfo = biometricDataModel.getIris();
		if (irisInfo != null) {
			String irisXml = buildBirIris(irisInfo.getLeft(), "Left");
			builder = builder.importXMLBuilder(XMLBuilder.parse(irisXml));
			irisXml = buildBirIris(irisInfo.getRight(), "Right");
			builder = builder.importXMLBuilder(XMLBuilder.parse(irisXml));
		}

		if (toFile != null) {
			PrintWriter writer = new PrintWriter(new FileOutputStream(toFile));
			builder.toWriter(true, writer, null);
		}
		retXml = builder.asString(null);
		return retXml;
	}

	public static BiometricDataModel getBiometricData(Boolean bFinger, String contextKey) {

		BiometricDataModel data = new BiometricDataModel();

		File tmpDir;

		if (bFinger) {
			Boolean bAnguli = Boolean.parseBoolean(
					VariableManager.getVariableValue(VariableManager.NS_DEFAULT, "enableAnguli").toString());
			if (bAnguli) {

				try {
					tmpDir = Files.createTempDirectory("fps").toFile();
					Hashtable<Integer, List<File>> prints = generateFingerprint(tmpDir.getAbsolutePath(), 10, 2, 4,
							FPClassDistribution.arch);
					List<File> firstSet = prints.get(1);

					String[] fingerPrints = new String[10];
					int index = 0;
					for (File f : firstSet) {

						if (index > 9)
							break;
						Path path = Paths.get(f.getAbsolutePath());
						byte[] fdata = Files.readAllBytes(path);
						fingerPrints[index] = Base64.getEncoder().encodeToString(fdata);

						// fingerPrints[index]= Hex.encodeHexString( fdata ) ;
						// fingerPrints[index]= fingerPrints[index].toUpperCase();

						// delete file
						index++;

					}
					data.setFingerPrint(fingerPrints);
					tmpDir.deleteOnExit();

				} catch (IOException e) {
					logger.error(e.getMessage());
				}
			} else {
				// reach cached finger prints from folder
				String dirPath = VariableManager.getVariableValue(contextKey, "mountPath").toString() + VariableManager
						.getVariableValue(contextKey, "mosip.test.persona.fingerprintdatapath").toString();
				Hashtable<Integer, List<File>> tblFiles = new Hashtable<Integer, List<File>>();
				for (int i = 1; i <= 2; i++) {

					List<File> lst = CommonUtil.listFiles(dirPath + String.format("/Impression_%d/fp_1/", i));
					tblFiles.put(i, lst);
				}
				String[] fingerPrints = new String[10];
				List<File> firstSet = tblFiles.get(1);

				int index = 0;
				for (File f : firstSet) {

					if (index > 9)
						break;
					Path path = Paths.get(f.getAbsolutePath());
					byte[] fdata;
					try {
						fdata = Files.readAllBytes(path);
						fingerPrints[index] = Base64.getEncoder().encodeToString(fdata);
					} catch (IOException e) {
						logger.error(e.getMessage());
					}
					index++;

				}
				data.setFingerPrint(fingerPrints);
			}
		}
		return data;
	}

	// generate using Anguli

	static Hashtable<Integer, List<File>> generateFingerprint(String outDir, int nFingerPrints,
			int nImpressionsPerPrints, int nThreads, FPClassDistribution classDist) {

		Hashtable<Integer, List<File>> tblFiles = new Hashtable<Integer, List<File>>();

		String[] commands = { DataProviderConstants.ANGULI_PATH + "/Anguli.exe", "-outdir", outDir, "-numT",
				String.format("%d", nThreads), "-num", String.format("%d", nFingerPrints), "-ni",
				String.format("%d", nImpressionsPerPrints), "-cdist", classDist.name() };

		ProcessBuilder pb = new ProcessBuilder(commands);
		pb.directory(new File(DataProviderConstants.ANGULI_PATH));

		try {
			Process proc = pb.start(); // rt.exec(commands);
			BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
			// Read any errors from the attempted command
			String s;

			while ((s = stdError.readLine()) != null) {
				logger.info(s);
			}
			// read from outdir
			for (int i = 1; i <= nImpressionsPerPrints; i++) {

				List<File> lst = CommonUtil.listFiles(outDir + String.format("/Impression_%d/fp_1/", i));
				tblFiles.put(i, lst);
			}
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		return tblFiles;
	}

	// Left Eye, Right Eye
	static List<IrisDataModel> generateIris(int count, String contextKey) throws IOException {

		List<IrisDataModel> retVal = new ArrayList<IrisDataModel>();

		String srcPath = VariableManager.getVariableValue(contextKey, "mountPath").toString()
				+ VariableManager.getVariableValue(contextKey, "mosip.test.persona.irisdatapath").toString();

		int[] index = CommonUtil.generateRandomNumbers(count, 224, 1);
		for (int i = 0; i < count; i++) {
			String fPathL = srcPath + String.format("%03d", index[i]) + "/01_L.bmp";
			String fPathR = srcPath + String.format("%03d", index[i]) + "/05_R.bmp";

			String leftIrisData = "";
			String rightIrisData = "";
			if (Files.exists(Paths.get(fPathL))) {
				byte[] fdata = Files.readAllBytes(Paths.get(fPathL));
				leftIrisData = Hex.encodeHexString(fdata);
			}
			if (Files.exists(Paths.get(fPathR))) {
				byte[] fdata = Files.readAllBytes(Paths.get(fPathR));
				rightIrisData = Hex.encodeHexString(fdata);
			}
			if (leftIrisData.equals(""))
				leftIrisData = rightIrisData;
			else if (rightIrisData.equals(""))
				rightIrisData = leftIrisData;
			IrisDataModel m = new IrisDataModel();
			m.setLeft(leftIrisData);
			m.setRight(rightIrisData);
			retVal.add(m);
		}

		return retVal;
	}

	public static void main(String[] args) {

		BiometricDataModel bio = getBiometricData(true, "contextKey");

		String xml = "";
		try {
			xml = toCBEFF(bio, "cbeffallfingersOut.xml");

		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		logger.info(xml);
	}
}
