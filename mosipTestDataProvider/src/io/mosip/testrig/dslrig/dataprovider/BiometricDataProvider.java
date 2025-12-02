package io.mosip.testrig.dslrig.dataprovider;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.codec.binary.Hex;
import io.mosip.testrig.dslrig.dataprovider.mds.MDSClient;
import io.mosip.testrig.dslrig.dataprovider.mds.MDSClientInterface;
import io.mosip.testrig.dslrig.dataprovider.mds.MDSClientNoMDS;
import io.mosip.testrig.dslrig.dataprovider.models.BioModality;
import io.mosip.testrig.dslrig.dataprovider.models.BiometricDataModel;
import io.mosip.testrig.dslrig.dataprovider.models.IrisDataModel;
import io.mosip.testrig.dslrig.dataprovider.models.ResidentModel;
import io.mosip.testrig.dslrig.dataprovider.models.mds.MDSDevice;
import io.mosip.testrig.dslrig.dataprovider.models.mds.MDSDeviceCaptureModel;
import io.mosip.testrig.dslrig.dataprovider.models.mds.MDSRCaptureModel;
import io.mosip.testrig.dslrig.dataprovider.test.registrationclient.RegistrationSteps;
import io.mosip.testrig.dslrig.dataprovider.util.CommonUtil;
import io.mosip.testrig.dslrig.dataprovider.util.DataProviderConstants;
import io.mosip.testrig.dslrig.dataprovider.util.FPClassDistribution;
import io.mosip.testrig.dslrig.dataprovider.util.RestClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.jamesmurty.utils.XMLBuilder;
import io.mosip.mock.sbi.test.CentralizedMockSBI;
import io.mosip.testrig.dslrig.dataprovider.variables.VariableManager;

public class BiometricDataProvider {

	public static HashMap<String, Integer> portmap = new HashMap();
	private static final Logger logger = LoggerFactory.getLogger(BiometricDataProvider.class);

	// String constants
	private static final String XMLNS = "xmlns";
	private static final String XMLNS_URL = "http://standards.iso.org/iso-iec/19785/-3/ed-2/";
	private static final String MAJOR = "Major";
	private static final String MINOR = "Minor";
	private static final String CBEFFVERSION = "CBEFFVersion";
	private static final String VERSION = "Version";
	private static final String FALSE = "false";
	private static final String BDBINFO = "BDBInfo";
	private static final String BIRINFO = "BIRInfo";
	private static final String INTEGRITY = "Integrity";
	private static final String FORMAT = "Format";
	private static final String CREATIONDATE = "CreationDate";
	private static final String ORGANIZATION = "Organization";
	private static final String MOSIP = "Mosip";
	private static final String SUBTYPE = "Subtype";
	private static final String PURPOSE = "Purpose";
	private static final String LEVEL = "Level";
	private static final String SHA_256 = "SHA-256";
	private static final String ENROLL = "Enroll";
	private static final String QUALITY = "Quality";
	private static final String ALGORITHM = "Algorithm";
	private static final String SCORE = "Score";
	private static final String EXCEPTION = "EXCEPTION";
	private static final String OTHERS = "others";
	private static final String ENTRY = "entry";
	private static final String RETRIES = "RETRIES";
	private static final String SDK_SCORE = "SDK_SCORE";
	private static final String FORCE_CAPTURED = "FORCE_CAPTURED";
	private static final String PAYLOAD = "PAYLOAD";
	private static final String SPEC_VERSION = "SPEC_VERSION";
	public static final String AUTHCERTSPATH = "authCertsPath";
	private static final String LEFTEYE = "leftEye";
	private static final String RIGHTEYE = "rightEye";
	private static final String RIGHT = "Right";
	private static final String DIRPATH = "dirPath ";
	private static final String SCENARIO = "scenario";

	static String buildBirIris(String irisInfo, String irisName, String jtwSign, String payload, String qualityScore,
			boolean genarateValidCbeff, String exception, String contextKey) throws ParserConfigurationException,
	FactoryConfigurationError, TransformerException, FileNotFoundException {
		String today = CommonUtil.getUTCDateTime(null);
		XMLBuilder builder = XMLBuilder.create("BIR").a(XMLNS, XMLNS_URL)
				.e(VERSION).e(MAJOR).t("1").up().e(MINOR).t("1").up().up().e(CBEFFVERSION).e(MAJOR).t("1").up().e(MINOR)
				.t("1").up().up().e(BIRINFO).e(INTEGRITY).t(FALSE).up().up().e(BDBINFO).e(FORMAT).e(ORGANIZATION)
				.t(MOSIP).up().e("Type").t("9").up().up().e(CREATIONDATE).t(today).up().e("Type").t("Iris").up()
				.e(SUBTYPE).t(irisName).up().e(LEVEL).t("Raw").up().e(PURPOSE).t(ENROLL).up().e(QUALITY).e(ALGORITHM)
				.e(ORGANIZATION).t("HMAC").up().e("Type").t(SHA_256).up().up().e(SCORE).t((int) Math.round(Double.parseDouble(qualityScore)) + "").up().up().up()
				.e("BDB").t(irisInfo).up().up();
		if (jtwSign != null && payload != null) {
			jtwSign = Base64.getEncoder().encodeToString(jtwSign.getBytes());
			builder.e("SB").t(jtwSign).up().

			e(OTHERS).e(ENTRY).a("key", EXCEPTION).t(exception).up().e(ENTRY).a("key", RETRIES).t("1").up()
			.e(ENTRY).a("key", SDK_SCORE).t("0.0").up().e(ENTRY).a("key", FORCE_CAPTURED).t(FALSE).up().e(ENTRY)
			.a("key", PAYLOAD).t(payload).up().e(ENTRY).a("key", SPEC_VERSION).t("0.9.5").up().up();
		}
		if (Double.parseDouble(qualityScore) >= 80)
			VariableManager.setVariableValue(contextKey, "Biometric_Quality-Iris", "level-9");
		else
			VariableManager.setVariableValue(contextKey, "Biometric_Quality-Iris", "level-2");
		return builder.asString(null);
	}

	static String buildBirFinger(String fingerInfo, String fingerName, String jtwSign, String payload,
			String qualityScore, boolean generateValidCbeff, String exception, String contextKey)
					throws ParserConfigurationException, FactoryConfigurationError, TransformerException,
					FileNotFoundException {
		String today = CommonUtil.getUTCDateTime(null);
		XMLBuilder builder = null;
		String bdbKey = "BDB";
		if (generateValidCbeff == false)
			bdbKey = "invalidBDB";
		builder = XMLBuilder.create("BIR").a(XMLNS, XMLNS_URL).e(VERSION)
				.e(MAJOR).t("1").up().e(MINOR).t("1").up().up().e(CBEFFVERSION).e(MAJOR).t("1").up().e(MINOR).t("1")
				.up().up().e(BIRINFO).e(INTEGRITY).t(FALSE).up().up().e(BDBINFO).e(FORMAT).e(ORGANIZATION).t(MOSIP).up()
				.e("Type").t("7").up().up().e(CREATIONDATE).t(today).up().e("Type").t("Finger").up().e(SUBTYPE)
				.t(fingerName).up().e(LEVEL).t("Raw").up().e(PURPOSE).t(ENROLL).up().e(QUALITY).e(ALGORITHM)
				.e(ORGANIZATION).t("HMAC").up().e("Type").t(SHA_256).up().up().e(SCORE).t((int) Math.round(Double.parseDouble(qualityScore)) + "").up().up().up()
				.e(bdbKey).t(fingerInfo).up().up();
		if (jtwSign != null && payload != null) {
			jtwSign = Base64.getEncoder().encodeToString(jtwSign.getBytes());
			builder.e("SB").t(jtwSign).up().

			e(OTHERS).e(ENTRY).a("key", EXCEPTION).t(exception).up().e(ENTRY).a("key", RETRIES).t("1").up()
			.e(ENTRY).a("key", SDK_SCORE).t("0.0").up().e(ENTRY).a("key", FORCE_CAPTURED).t(FALSE).up().e(ENTRY)
			.a("key", PAYLOAD).t(payload).up().e(ENTRY).a("key", SPEC_VERSION).t("0.9.5").up().up();
		}
		if (Double.parseDouble(qualityScore) >= 80)
			VariableManager.setVariableValue(contextKey, "Biometric_Quality-Finger", "level-9");
		else
			VariableManager.setVariableValue(contextKey, "Biometric_Quality-Finger", "level-2");
		return builder.asString(null);
	}

	static String buildBirFace(String faceInfo, String jtwSign, String payload, String qualityScore,
			boolean genarateValidCbeff, String exception, String contextKey) throws ParserConfigurationException,
	FactoryConfigurationError, TransformerException, FileNotFoundException {
		String today = CommonUtil.getUTCDateTime(null);
		XMLBuilder builder = XMLBuilder.create("BIR").a(XMLNS, XMLNS_URL)
				.e(VERSION).e(MAJOR).t("1").up().e(MINOR).t("1").up().up().e(CBEFFVERSION).e(MAJOR).t("1").up().e(MINOR)
				.t("1").up().up().e(BIRINFO).e(INTEGRITY).t(FALSE).up().up().e(BDBINFO).e(FORMAT).e(ORGANIZATION)
				.t(MOSIP).up().e("Type").t("8").up().up().e(CREATIONDATE).t(today).up().e("Type").t("Face").up()
				.e(SUBTYPE).t("").up().e(LEVEL).t("Raw").up().e(PURPOSE).t(ENROLL).up().e(QUALITY).e(ALGORITHM)
				.e(ORGANIZATION).t("HMAC").up().e("Type").t(SHA_256).up().up().e(SCORE).t((int) Math.round(Double.parseDouble(qualityScore)) + "").up().up().up()
				.e("BDB").t(faceInfo).up().up();
		if (jtwSign != null && payload != null) {
			jtwSign = Base64.getEncoder().encodeToString(jtwSign.getBytes());
			builder.e("SB").t(jtwSign).up().

			e(OTHERS).e(ENTRY).a("key", EXCEPTION).t(exception).up().e(ENTRY).a("key", RETRIES).t("1").up()
			.e(ENTRY).a("key", SDK_SCORE).t("0.0").up().e(ENTRY).a("key", FORCE_CAPTURED).t(FALSE).up().e(ENTRY)
			.a("key", PAYLOAD).t(payload).up().e(ENTRY).a("key", SPEC_VERSION).t("0.9.5").up().up();

		}
		if (Double.parseDouble(qualityScore) >= 80)
			VariableManager.setVariableValue(contextKey, "Biometric_Quality-Face", "level-9");
		else
			VariableManager.setVariableValue(contextKey, "Biometric_Quality-Face", "level-2");
		return builder.asString(null);
	}

	static String buildBirExceptionPhoto(String faceInfo, String jtwSign, String payload, String qualityScore,
			boolean genarateValidCbeff, String exception, String contextKey) throws ParserConfigurationException,
	FactoryConfigurationError, TransformerException, FileNotFoundException {
		String today = CommonUtil.getUTCDateTime(null);
		XMLBuilder builder = XMLBuilder.create("BIR").a(XMLNS, XMLNS_URL)
				.e(VERSION).e(MAJOR).t("1").up().e(MINOR).t("1").up().up().e(CBEFFVERSION).e(MAJOR).t("1").up().e(MINOR)
				.t("1").up().up().e(BIRINFO).e(INTEGRITY).t(FALSE).up().up().e(BDBINFO).e(FORMAT).e(ORGANIZATION)
				.t(MOSIP).up().e("Type").t("8").up().up().e(CREATIONDATE).t(today).up().e("Type").t("ExceptionPhoto")
				.up().e(SUBTYPE).t("").up().e(LEVEL).t("Raw").up().e(PURPOSE).t(ENROLL).up().e(QUALITY).e(ALGORITHM)
				.e(ORGANIZATION).t("HMAC").up().e("Type").t(SHA_256).up().up().e(SCORE).t((int) Math.round(Double.parseDouble(qualityScore)) + "").up().up().up()
				.e("BDB").t(faceInfo).up().up();
		if (jtwSign != null && payload != null) {
			jtwSign = Base64.getEncoder().encodeToString(jtwSign.getBytes());
			builder.e("SB").t(jtwSign).up().

			e(OTHERS).e(ENTRY).a("key", EXCEPTION).t(exception).up().e(ENTRY).a("key", RETRIES).t("1").up()
			.e(ENTRY).a("key", SDK_SCORE).t("0.0").up().e(ENTRY).a("key", FORCE_CAPTURED).t(FALSE).up().e(ENTRY)
			.a("key", PAYLOAD).t(payload).up().e(ENTRY).a("key", SPEC_VERSION).t("0.9.5").up().up();

		}
		VariableManager.setVariableValue(contextKey, "EXCEPTION_BIOMETRICS", "exception");
		return builder.asString(null);
	}

	public static List<BioModality> getModalitiesByType(List<BioModality> bioExceptions, String type) {
		List<BioModality> lst = new ArrayList<BioModality>();

		for (BioModality m : bioExceptions) {
			if (m.getType().equalsIgnoreCase(type)) {
				lst.add(m);
			}
		}
		return lst;
	}

	public static MDSRCaptureModel regenBiometricViaMDS(ResidentModel resident, String contextKey, String purpose,
			String qualityScore) throws Exception {
		CentralizedMockSBI.stopSBI(contextKey);
		BiometricDataModel biodata = null;
		MDSRCaptureModel capture = null;

		MDSClientInterface mds = null;
		String val;
		boolean bNoMDS = true;
		String mdsprofilePath = null;
		String profileName = null;
		int port = 0;
		val = VariableManager.getVariableValue(VariableManager.NS_DEFAULT, "mdsbypass").toString();
		List<String> filteredAttribs = resident.getFilteredBioAttribtures();
		List<BioModality> bioExceptions = resident.getBioExceptions();
		List<String> bioexceptionlist = new ArrayList<String>();

		try {
			if (val == null || val.equals("") || val.equals(FALSE)) {

				val = VariableManager.getVariableValue(VariableManager.NS_DEFAULT, "mdsport").toString();
				mdsprofilePath = VariableManager.getVariableValue(VariableManager.NS_DEFAULT, "mdsprofilepath")
						.toString();

				/*
				 * String certsDir = System.getenv(AUTHCERTSPATH) == null ?
				 * VariableManager.getVariableValue(contextKey, AUTHCERTSPATH).toString() :
				 * System.getenv(AUTHCERTSPATH);
				 * 
				 * if (certsDir == null || certsDir.length() == 0) { certsDir =
				 * System.getProperty("java.io.tmpdir") + File.separator + "AUTHCERTS"; }
				 */

				Path p12path = null;
				boolean invalidCertFlag = Boolean
						.parseBoolean(VariableManager.getVariableValue(contextKey, "invalidCertFlag").toString());

				if (invalidCertFlag)
					p12path = Paths.get(
							VariableManager.getVariableValue(VariableManager.NS_DEFAULT, "invalidCertpath").toString());
				else
					p12path = Paths.get(System.getProperty("java.io.tmpdir"), VariableManager.getVariableValue(contextKey, "db-server").toString());

				RestClient.logInfo(contextKey, "p12path" + p12path);

				int maxLoopCount = Integer.parseInt(
						VariableManager.getVariableValue(VariableManager.NS_DEFAULT, "mdsPortLoopCount").toString());

				while (maxLoopCount > 0) {
					try {
						port = CentralizedMockSBI.startSBI(contextKey, "Registration", "Biometric Device",
								p12path.toString());
					} catch (Exception e) {
						logger.error("Exception occured during startSBI " + contextKey, e);
					}
					if (port != 0) {
						RestClient.logInfo(contextKey, "Found the port " + contextKey + " port number is: " + port);
						break;
					}

					maxLoopCount--;
				}

				if (port == 0) {
					logger.error("Unable to find the port " + contextKey + " port number is: " + port);
					return null;
				}
				// Need to check with Anusha is this below line correct wrt multi run of
				// scenarios for each env.
				// port =
				// CentralizedMockSBI.startSBI(VariableManager.getVariableValue(contextKey,
				// "db-server").toString(), "Registration", "Biometric Device",
				// p12path.toString());

				portmap.put("port_" + contextKey, port);

				mds = new MDSClient(port);
				if (resident.getBioExceptions() != null && !resident.getBioExceptions().isEmpty()) {
					mds.setProfile("Default", port, contextKey);
				} else {
					profileName = "res" + resident.getId();
					mds.createProfile(mdsprofilePath, profileName, resident, contextKey, purpose);
					mds.setProfile(profileName, port, contextKey);
				}

			} else {
				mds = new MDSClientNoMDS();
				bNoMDS = false;
				profileName = "res" + resident.getId();
				mds.createProfile(mdsprofilePath, profileName, resident, contextKey, purpose);
				mds.setProfile(profileName, port, contextKey);
			}

			// Change mockmds quality score

			HashMap<String, Integer> portAsPerKey = BiometricDataProvider.portmap;
			RegistrationSteps steps = new RegistrationSteps();
			steps.setMDSscore(portAsPerKey.get("port_" + contextKey), "Biometric Device", qualityScore, contextKey);
			RestClient.logInfo(contextKey, "mds score is changed to : " + qualityScore);
			biodata = resident.getBiometric();
			// This condition will address those scenarios where we are not passing any
			// biometrics
			if (biodata.getFaceHash() == null && biodata.getFingerHash() == null && biodata.getIris() == null)
				return new MDSRCaptureModel();
		} catch (Throwable t) {
			logger.error(" Port issue " + contextKey, t);
			t.getStackTrace();
			return null;
		}

		// Get Exceptions modalities abd add them to list of string
		if (bioExceptions != null && !bioExceptions.isEmpty()) {
			for (int modalityCount = 0; modalityCount < bioExceptions.size(); modalityCount++)
				bioexceptionlist.add(bioExceptions.get(modalityCount).getSubType().toString());
		}

		// Step 1 : Face get capture
		try {
			if ((filteredAttribs != null && filteredAttribs.contains("face")) && biodata.getRawFaceData() != null) {

				List<MDSDevice> faceDevices = mds.getRegDeviceInfo(DataProviderConstants.MDS_DEVICE_TYPE_FACE);
				MDSDevice faceDevice = faceDevices.get(0);

				capture = mds.captureFromRegDevice(faceDevice, capture, DataProviderConstants.MDS_DEVICE_TYPE_FACE,
						null, 60, faceDevice.getDeviceSubId().get(0), port, contextKey, null);
			}
		}

		catch (Throwable t) {
			logger.error(" Face get capture   fail" + contextKey, t);
			t.getStackTrace();
			return null;
		}

		// Step 2 : IRIS get capture
		try {
			if (biodata.getIris() != null) {
				List<BioModality> irisExceptions = null;
				List<String> listexceptionBio = new ArrayList<String>();

				if (bioExceptions != null && !bioExceptions.isEmpty()) {
					irisExceptions = getModalitiesByType(bioExceptions, "Iris");
					for (BioModality modality : bioExceptions) {
						listexceptionBio.add(modality.getSubType());

					}
				}

				List<MDSDevice> irisDevices = mds.getRegDeviceInfo(DataProviderConstants.MDS_DEVICE_TYPE_IRIS);
				MDSDevice irisDevice = irisDevices.get(0);

				if (irisExceptions == null || irisExceptions.isEmpty()) {
					if (filteredAttribs != null && filteredAttribs.contains(LEFTEYE)) {
						capture = mds.captureFromRegDevice(irisDevice, capture,
								DataProviderConstants.MDS_DEVICE_TYPE_IRIS, null, 60,
								irisDevice.getDeviceSubId().get(0), port, contextKey, null);
					}

					if (irisDevice.getDeviceSubId().size() > 1) {
						if (filteredAttribs != null && filteredAttribs.contains(RIGHTEYE)) {

							capture = mds.captureFromRegDevice(irisDevice, capture,
									DataProviderConstants.MDS_DEVICE_TYPE_IRIS, null, 60,
									irisDevice.getDeviceSubId().get(1), port, contextKey, null);
						}
					}
				} else {
					String[] irisSubTypes = new String[irisExceptions.size()];
					int i = 0;
					for (BioModality bm : irisExceptions) {
						irisSubTypes[i] = bm.getSubType();
						i++;
					}
					for (String f : irisSubTypes) {

						if (f.equalsIgnoreCase(RIGHT)
								&& (filteredAttribs != null && filteredAttribs.contains(LEFTEYE))) {
							capture = mds.captureFromRegDevice(irisDevice, capture,
									DataProviderConstants.MDS_DEVICE_TYPE_IRIS, null, 60,
									irisDevice.getDeviceSubId().get(0), port, contextKey, null);
						} else if (f.equalsIgnoreCase("left")
								&& (filteredAttribs != null && filteredAttribs.contains(RIGHTEYE))) {

							if (irisDevice.getDeviceSubId().size() > 1)
								capture = mds.captureFromRegDevice(irisDevice, capture,
										DataProviderConstants.MDS_DEVICE_TYPE_IRIS, null, 60,
										irisDevice.getDeviceSubId().get(1), port, contextKey, null);
						}
					}
				}
			}

		}

		catch (Throwable t) {
			logger.error(" IRIS get capture  fail" + contextKey, t);
			t.getStackTrace();
			return null;
		}

		try {
			if (biodata.getFingerPrint() != null) {
				List<BioModality> fingerExceptions = null;
				List<MDSDeviceCaptureModel> lstToRemove = new ArrayList<MDSDeviceCaptureModel>();
				List<String> listFingerExceptionBio = new ArrayList<String>();

				if (bioExceptions != null && !bioExceptions.isEmpty()) {

					fingerExceptions = getModalitiesByType(bioExceptions, "Finger");

					for (BioModality modality : bioExceptions) {
						listFingerExceptionBio.add(modality.getSubType());

					}
				}

				List<MDSDevice> fingerDevices = mds.getRegDeviceInfo(DataProviderConstants.MDS_DEVICE_TYPE_FINGER);
				MDSDevice fingerDevice = fingerDevices.get(0);

				for (int i = 0; i < fingerDevice.getDeviceSubId().size(); i++) {
					capture = mds.captureFromRegDevice(fingerDevice, capture,
							DataProviderConstants.MDS_DEVICE_TYPE_FINGER, null, 60,
							fingerDevice.getDeviceSubId().get(i), port, contextKey, null);
				}
				List<MDSDeviceCaptureModel> lstFingers = capture.getLstBiometrics()
						.get(DataProviderConstants.MDS_DEVICE_TYPE_FINGER);
				if (filteredAttribs != null) {
					// schemaNames
					String attr = null;

					for (MDSDeviceCaptureModel mdc : lstFingers) {
						int indx = 0;
						boolean bFound = false;
						for (indx = 0; indx < DataProviderConstants.schemaNames.length; indx++) {
							if (DataProviderConstants.displayFingerName[indx].equals(mdc.getBioSubType())) {
								attr = DataProviderConstants.schemaNames[indx];
								break;
							}
						}
						if (attr != null) {
							for (String a : filteredAttribs) {
								if (a.equals(attr)) {
									bFound = true;
									break;
								}
							}
						}
						if (!bFound)
							lstToRemove.add(mdc);
					}
					lstFingers.removeAll(lstToRemove);
				}

			}
		}

		catch (Throwable t) {
			logger.error("Finger get capture fail" + contextKey, t);
			t.getStackTrace();
			return null;
		}

		try {
			// Step 4 : Exception photo face capture
			if (bioExceptions != null && !bioExceptions.isEmpty()) {

				List<MDSDevice> exceptionfaceDevices = mds.getRegDeviceInfo(DataProviderConstants.MDS_DEVICE_TYPE_FACE);
				MDSDevice exceptionDevice = exceptionfaceDevices.get(0);
				try {
					capture = mds.captureFromRegDevice(exceptionDevice, capture,
							DataProviderConstants.MDS_DEVICE_TYPE_FACE, null, 60,
							exceptionDevice.getDeviceSubId().get(0), port, contextKey, bioexceptionlist);
					// rename the key with exception_photo
				} catch (Throwable t) {
					logger.error("Exception photo capture failure" + contextKey, t);
					t.getStackTrace();
					return null;
				}

			}

		}

		catch (Throwable t) {
			logger.error("Exceptionphoto face capture", t);
			t.getStackTrace();
			return null;
		}

//		mds.removeProfile(mdsprofilePath, profileName, port, contextKey);
		mds.setProfile("Default",port,contextKey);
		CommonUtil.deleteOldTempDir(mdsprofilePath+"/"+ profileName);
		CentralizedMockSBI.stopSBI(contextKey);
		return capture;
	}

	public static String toCBEFFFromCapture(List<String> bioFilter, MDSRCaptureModel capture, String toFile,
			List<String> missAttribs, boolean genarateValidCbeff, List<BioModality> exceptionlist, String contextKey)
					throws Exception {

		String retXml = "";

		String mosipVersion = null;
		try {
			mosipVersion = VariableManager.getVariableValue(VariableManager.NS_DEFAULT, "mosip.version").toString();
		} catch (Exception e) {

		}

		XMLBuilder builder = XMLBuilder.create("BIR").a(XMLNS, XMLNS_URL)
				.e(BIRINFO).e(INTEGRITY).t(FALSE).up().up();

		builder.getDocument().setXmlStandalone(true);

		List<String> bioSubType = new ArrayList<>();

		// Step 1: convert finger print
		try {
			List<MDSDeviceCaptureModel> lstFingerData = capture.getLstBiometrics()
					.get(DataProviderConstants.MDS_DEVICE_TYPE_FINGER);

			builder = xmlbuilderFinger(bioFilter, lstFingerData, bioSubType, builder, exceptionlist, genarateValidCbeff,
					contextKey);

			if (exceptionlist != null && !exceptionlist.isEmpty()) {
				builder = xmlbuilderFingerExep(bioFilter, exceptionlist, bioSubType, builder, genarateValidCbeff,
						contextKey);
			}
		} catch (Exception e) {
			logger.error("xmlbuilderFinger failed" + e.getMessage());
		}

		// Step 2: Add Face

		try {
			if (bioFilter.contains("face")) {

				List<MDSDeviceCaptureModel> lstFaceData = capture.getLstBiometrics()
						.get(DataProviderConstants.MDS_DEVICE_TYPE_FACE);
				bioSubType.add("face");
				String faceXml = buildBirFace(lstFaceData.get(0).getBioValue(), lstFaceData.get(0).getSb(),
						lstFaceData.get(0).getPayload(), lstFaceData.get(0).getQualityScore(), genarateValidCbeff,
						FALSE, contextKey);
				builder = builder.importXMLBuilder(XMLBuilder.parse(faceXml));

			}
		} catch (Exception e) {
			logger.error("buildBirFace failed");
			logger.error(e.getMessage());
		}

		// Step 3: Add IRIS
		try {
			List<MDSDeviceCaptureModel> lstIrisData = capture.getLstBiometrics()
					.get(DataProviderConstants.MDS_DEVICE_TYPE_IRIS);

			builder = xmlbuilderIris(bioFilter, lstIrisData, bioSubType, builder, genarateValidCbeff, exceptionlist,
					contextKey);

			if (exceptionlist != null && !exceptionlist.isEmpty()) {
				builder = xmlbuilderIrisExcep(bioFilter, exceptionlist, bioSubType, builder, genarateValidCbeff,
						contextKey);
			}
		} catch (Exception e) {
			logger.error("xmlbuilderIris failed");
			logger.error(e.getMessage());
		}

		// Step 4: Add Face as an Exception photo

		try {
			if (exceptionlist != null && !exceptionlist.isEmpty()) {
				List<MDSDeviceCaptureModel> lstFaceData = capture.getLstBiometrics().get(EXCEPTION);
				bioSubType.add("exceptionphoto");
				String faceXml = buildBirExceptionPhoto(lstFaceData.get(1).getBioValue(), lstFaceData.get(1).getSb(),
						lstFaceData.get(1).getPayload(), lstFaceData.get(1).getQualityScore(), genarateValidCbeff,
						FALSE, contextKey);
				builder = builder.importXMLBuilder(XMLBuilder.parse(faceXml));
			}
		} catch (Exception e) {
			logger.error("buildBirExceptionPhoto failed");
			logger.error(e.getMessage());
		}

		// Print builder

		if (mosipVersion != null && mosipVersion.startsWith("1.2") && !bioSubType.isEmpty()) {
			builder.e(OTHERS).e("Key").t("CONFIGURED").up().e("Value")
			.t(bioSubType.toString().substring(1, bioSubType.toString().length() - 1)).up().up();
		}
		if (toFile != null) {
			FileOutputStream fos = new FileOutputStream(toFile);
			PrintWriter writer = new PrintWriter(fos);
			builder.toWriter(true, writer, null);
			fos.close();
		}

		retXml = builder.asString(null);
		return retXml;
	}

	private static XMLBuilder xmlbuilderIris(List<String> bioFilter, List<MDSDeviceCaptureModel> lstIrisData,
			List<String> bioSubType, XMLBuilder builder, boolean genarateValidCbeff, List<BioModality> exceptionlst,
			String contextKey)

	{
		List<String> listWithoutExceptions = bioFilter;
		if (exceptionlst != null && !exceptionlst.isEmpty()) {
			List<String> exceptions = exceptionlst.stream().map(BioModality::getSubType).collect(Collectors.toList());
			List<String> schemaName = new ArrayList<String>();
			for (String ex : exceptions) {
				schemaName.add(getschemaName(ex));
			}
			listWithoutExceptions = bioFilter.stream().filter(bioAttribute -> !schemaName.contains(bioAttribute))
					.collect(Collectors.toList());
		}
		RestClient.logInfo(contextKey, "withoutExceptionList is: " + listWithoutExceptions);

		try {
			if (lstIrisData != null) {
				String irisXml = "";
				for (MDSDeviceCaptureModel cm : lstIrisData) {

					if (listWithoutExceptions.contains(LEFTEYE) && cm.getBioSubType().equals("Left")) {
						irisXml = buildBirIris(cm.getBioValue(), "Left", cm.getSb(), cm.getPayload(),
								cm.getQualityScore(), genarateValidCbeff, FALSE, contextKey);
						builder = builder.importXMLBuilder(XMLBuilder.parse(irisXml));
						bioSubType.add("Left");
					}
					if (listWithoutExceptions.contains(RIGHTEYE) && cm.getBioSubType().equals(RIGHT)) {

						irisXml = buildBirIris(cm.getBioValue(), RIGHT, cm.getSb(), cm.getPayload(),
								cm.getQualityScore(), genarateValidCbeff, FALSE, contextKey);
						builder = builder.importXMLBuilder(XMLBuilder.parse(irisXml));
						bioSubType.add(RIGHT);
					}
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}

		return builder;
	}

	private static XMLBuilder xmlbuilderIrisExcep(List<String> bioFilter, List<BioModality> lstIrisData,
			List<String> bioSubType, XMLBuilder builder, boolean genarateValidCbeff, String contextKey) {
		try {
			if (lstIrisData != null) {
				for (BioModality finger : lstIrisData) {
					if (!finger.getType().equalsIgnoreCase("Iris"))
						continue;

					String strFingerXml = buildBirIris(finger.getType(), finger.getSubType(),
							Arrays.toString(new byte[0]), "", "0", genarateValidCbeff, "true", contextKey);
					XMLBuilder fbuilder = XMLBuilder.parse(strFingerXml);
					builder = builder.importXMLBuilder(fbuilder);
				}

			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}

		return builder;
	}

	private static String getschemaName(String name) {
		// First check if it falls in all modaities
		for (int i = 0; i < 13; i++) {
			String displayFingerName = DataProviderConstants.displayFullName[i];
			if (displayFingerName.equalsIgnoreCase(name) == true)
				return DataProviderConstants.schemaNames[i];
		}

		// Other wise just return
		return name;
	}

	private static XMLBuilder xmlbuilderFinger(List<String> bioFilter, List<MDSDeviceCaptureModel> lstFingerData,
			List<String> bioSubType, XMLBuilder builder, List<BioModality> exceptionlst, boolean genarateValidCbeff,
			String contextKey) {
		List<String> listWithoutExceptions = bioFilter;
		if (exceptionlst != null && !exceptionlst.isEmpty()) {
			List<String> exceptions = exceptionlst.stream().map(BioModality::getSubType).collect(Collectors.toList());
			RestClient.logInfo(contextKey, "exceptions" + exceptions);
			List<String> schemaName = new ArrayList<String>();
			for (String ex : exceptions) {
				schemaName.add(getschemaName(ex));
			}
			listWithoutExceptions = bioFilter.stream().filter(bioAttribute -> !schemaName.contains(bioAttribute))
					.collect(Collectors.toList());
		}

		// getschemaName(BioModality::getSubType)

		int i = 0;
		String fingerData = null;

		try {
			if (lstFingerData != null) {
				for (String finger : listWithoutExceptions) {
					if (finger.toLowerCase().contains("eye") || finger.toLowerCase().equals("face"))
						continue;
					i = Arrays.asList(DataProviderConstants.schemaNames).indexOf(finger);
					String displayName = DataProviderConstants.displayFingerName[i];
					MDSDeviceCaptureModel currentCM = null;
					for (MDSDeviceCaptureModel cm : lstFingerData) {
						if (cm.getBioSubType().equals(displayName)) {
							fingerData = cm.getBioValue();
							bioSubType.add(finger);
							currentCM = cm;
							break;
						}
					}
					RestClient.logInfo(contextKey, "fingerData is: " + fingerData);
					if (i >= 0 && fingerData != null && currentCM != null) {
						String strFinger = DataProviderConstants.displayFingerName[i];
						String strFingerXml = buildBirFinger(fingerData, strFinger, currentCM.getSb(),
								currentCM.getPayload(), currentCM.getQualityScore(), genarateValidCbeff, FALSE,
								contextKey);
						XMLBuilder fbuilder = XMLBuilder.parse(strFingerXml);
						builder = builder.importXMLBuilder(fbuilder);
					}

				}
			}

		} catch (Exception e1) {
			logger.error(e1.getMessage());
		}
		return builder;
	}

	private static XMLBuilder xmlbuilderFingerExep(List<String> bioFilter, List<BioModality> lstFingerData,
			List<String> bioSubType, XMLBuilder builder, boolean genarateValidCbeff, String contextKey)
					throws ParserConfigurationException, FactoryConfigurationError, TransformerException, SAXException,
					IOException {

		if (lstFingerData != null) {
			for (BioModality finger : lstFingerData) {
				if (finger.getType().equalsIgnoreCase("iris") || finger.getType().equalsIgnoreCase("face"))
					continue;

				String strFingerXml = buildBirFinger(finger.getType(), finger.getSubType(),
						Arrays.toString(new byte[0]), "", "0", genarateValidCbeff, "true", contextKey);
				XMLBuilder fbuilder = XMLBuilder.parse(strFingerXml);
				builder = builder.importXMLBuilder(fbuilder);
			}

		}

		return builder;
	}

	/*
	 * Construct CBEFF format XML file from biometric data
	 */
	public static String toCBEFF(List<String> bioFilter, BiometricDataModel biometricDataModel, String toFile,
			boolean genarateValidCbeff, String contextKey) throws Exception {
		String retXml = "";

		XMLBuilder builder = XMLBuilder.create("BIR").a(XMLNS, XMLNS_URL)
				.e(BIRINFO).e(INTEGRITY).t(FALSE).up().up();

		builder.getDocument().setXmlStandalone(true);

		// Step 1: convert finger print
		String[] fingerPrint = biometricDataModel.getFingerPrint();

		// get qualityScore
		String qualityScore = null;
		Hashtable<String, List<MDSDeviceCaptureModel>> capture = biometricDataModel.getCapture();
		Enumeration<List<MDSDeviceCaptureModel>> elements = capture.elements();
		while (elements.hasMoreElements()) {
			List<MDSDeviceCaptureModel> nextElement = elements.nextElement();
			qualityScore = nextElement.get(0).getQualityScore();
			break;
		}

		int i = 0;
		for (String finger : bioFilter) {
			if (finger.toLowerCase().contains("eye") || finger.toLowerCase().equals("face"))
				continue;
			i = Arrays.asList(DataProviderConstants.schemaNames).indexOf(finger);

			if (i >= 0) {
				String strFinger = DataProviderConstants.displayFingerName[i];
				String strFingerXml = buildBirFinger(fingerPrint[i], strFinger, null, null, qualityScore,
						genarateValidCbeff, FALSE, contextKey);

				XMLBuilder fbuilder = XMLBuilder.parse(strFingerXml);
				builder = builder.importXMLBuilder(fbuilder);
			}

		}

		// Step 2: Add Face
		if (bioFilter.contains("Face")) {
			if (biometricDataModel.getEncodedPhoto() != null) {
				String faceXml = buildBirFace(biometricDataModel.getEncodedPhoto(), null, null, qualityScore,
						genarateValidCbeff, "true", contextKey);
				builder = builder.importXMLBuilder(XMLBuilder.parse(faceXml));
			}
		}

		// Step 3: Add IRIS
		IrisDataModel irisInfo = biometricDataModel.getIris();
		if (irisInfo != null) {
			String irisXml = "";
			if (bioFilter.contains(LEFTEYE)) {
				irisXml = buildBirIris(irisInfo.getLeft(), "Left", null, null, qualityScore, genarateValidCbeff, "true",
						contextKey);
				builder = builder.importXMLBuilder(XMLBuilder.parse(irisXml));
			}
			if (bioFilter.contains(RIGHTEYE)) {
				irisXml = buildBirIris(irisInfo.getRight(), RIGHT, null, null, qualityScore, genarateValidCbeff, "true",
						contextKey);
				builder = builder.importXMLBuilder(XMLBuilder.parse(irisXml));
			}
		}

		if (toFile != null) {
			FileOutputStream fos = new FileOutputStream(toFile);
			PrintWriter writer = new PrintWriter(fos);
			builder.toWriter(true, writer, null);
			fos.close();
		}
		retXml = builder.asString(null);
		return retXml;
	}

	public static Hashtable<Integer, List<File>> impressionCaptureList(String contextKey) {
		// reach cached finger prints from folder
		String dirPath = System.getProperty("java.io.tmpdir")
				+ VariableManager.getVariableValue(contextKey, "mosip.test.persona.fingerprintdatapath").toString();
		RestClient.logInfo(contextKey, DIRPATH + dirPath);
		Hashtable<Integer, List<File>> tblFiles = new Hashtable<Integer, List<File>>();
		File dir = new File(dirPath);

		File listDir[] = dir.listFiles();
		int numberOfSubfolders = listDir.length;

		int min = 1;
		int max = numberOfSubfolders;
		int randomNumber = (int) (Math.random() * (max - min)) + min;
		String beforescenario = VariableManager.getVariableValue(contextKey, SCENARIO).toString();
		String afterscenario = beforescenario.substring(0, beforescenario.indexOf(':'));
		if (afterscenario.contains("_")) {
			afterscenario = afterscenario.replace("_", "0");
		}
		int currentScenarioNumber = Integer.valueOf(afterscenario);

		// If the available impressions are less than scenario number, pick the random
		// one

		// otherwise pick the impression of same of scenario number
		int impressionToPick = (currentScenarioNumber < numberOfSubfolders) ? currentScenarioNumber : randomNumber;

		RestClient.logInfo(contextKey, "currentScenarioNumber=" + currentScenarioNumber + " numberOfSubfolders="
				+ numberOfSubfolders + " impressionToPick=" + impressionToPick);
		List<File> lst = new LinkedList<File>();
		lst = CommonUtil.listFiles(dirPath + String.format("/Impression_%d/fp_1/", impressionToPick));
		tblFiles.put(impressionToPick, lst);
		return tblFiles;
	}

	public static BiometricDataModel getBiometricData(Boolean bFinger, String contextKey) throws IOException {

		BiometricDataModel data = new BiometricDataModel();

		File tmpDir;

		if (bFinger) {

			Object val = VariableManager.getVariableValue(VariableManager.NS_DEFAULT, "enableExternalBiometricSource");
			boolean bExternalSrc = false;
			if (val != null)
				bExternalSrc = Boolean.valueOf(val.toString());
			if (bExternalSrc) {
				// folder where all bio input available
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

			Boolean bAnguli = Boolean.parseBoolean(
					VariableManager.getVariableValue(VariableManager.NS_DEFAULT, "enableAnguli").toString());
			if (bAnguli) {

				// else case
				try {
					tmpDir = Files.createTempDirectory("fps").toFile();
					Hashtable<Integer, List<File>> prints = generateFingerprint(tmpDir.getAbsolutePath(), 10, 2, 4,
							FPClassDistribution.arch, contextKey);
					List<File> firstSet = prints.get(1);

					String[] fingerPrints = new String[10];
					String[] fingerPrintHash = new String[10];
					byte[][] fingerPrintRaw = new byte[10][1];

					int index = 0;
					for (File f : firstSet) {

						if (index > 9)
							break;

						byte[] fdata = CommonUtil.read(f.getAbsolutePath());
						fingerPrintRaw[index] = fdata;
						fingerPrints[index] = Base64.getEncoder().encodeToString(fdata);

						// delete file
						try {
							fingerPrintHash[index] = CommonUtil.getHexEncodedHash(fdata);

						} catch (Exception e) {
							logger.error(e.getMessage());
						}

						index++;

					}
					data.setFingerPrint(fingerPrints);
					data.setFingerHash(fingerPrintHash);
					data.setFingerRaw(fingerPrintRaw);
					tmpDir.deleteOnExit();

				} catch (IOException e) {
					logger.error(e.getMessage());
				}
			} else {
				// reach cached finger prints from folder
				String dirPath = System.getProperty("java.io.tmpdir") + VariableManager
						.getVariableValue(contextKey, "mosip.test.persona.fingerprintdatapath").toString();
				RestClient.logInfo(contextKey, DIRPATH + dirPath);
				Hashtable<Integer, List<File>> tblFiles = new Hashtable<Integer, List<File>>();
				File dir = new File(dirPath);

				File listDir[]=null;
				if (dir.isDirectory()) {
					// Use FileFilter to filter files
					listDir = dir.listFiles(new FileFilter() {
						@Override
						public boolean accept(File file) {
							// Check if it's a directory and starts with "Impression"
							return file.isDirectory() && file.getName().startsWith("Impression");
						}
					});		           
				} else {
					logger.error(dirPath + " is not a directory.");
				}
				int numberOfSubfolders = listDir.length;

				int min = 1;
				int max = numberOfSubfolders;
				int randomNumber = (int) (Math.random() * (max - min)) + min;
				String beforescenario = VariableManager.getVariableValue(contextKey, SCENARIO).toString();
				String afterscenario = beforescenario.substring(0, beforescenario.indexOf(':'));
				if (afterscenario.contains("_")) {
					afterscenario = afterscenario.replace("_", "0");
				}
				int currentScenarioNumber = Integer.valueOf(afterscenario);

				// If the available impressions are less than scenario number, pick the random
				// one

				// otherwise pick the impression of same of scenario number
				int impressionToPick = (currentScenarioNumber < numberOfSubfolders) ? currentScenarioNumber
						: randomNumber;
				dirPath = FingerprintVariationGenerator.fingerprintVariationGenerator(contextKey, currentScenarioNumber, impressionToPick);

				RestClient.logInfo(contextKey, "currentScenarioNumber=" + currentScenarioNumber + " numberOfSubfolders="
						+ numberOfSubfolders + " impressionToPick=" + impressionToPick);


				List<File> firstSet = CommonUtil.listFiles(dirPath + "/" + String.format("/Impression_%d/fp_1/", impressionToPick));


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

		}
		
		return data;
	}

	public static BiometricDataModel updateFingerData(String contextKey) throws IOException {

		BiometricDataModel data = new BiometricDataModel();
		// reach cached finger prints from folder
		String dirPath = System.getProperty("java.io.tmpdir")
				+ VariableManager.getVariableValue(contextKey, "mosip.test.persona.fingerprintdatapath").toString();
		RestClient.logInfo(contextKey, DIRPATH + dirPath);
		Hashtable<Integer, List<File>> tblFiles = new Hashtable<Integer, List<File>>();
		File dir = new File(dirPath);

		File listDir[]=null;
		if (dir.isDirectory()) {
			// Use FileFilter to filter files
			listDir = dir.listFiles(new FileFilter() {
				@Override
				public boolean accept(File file) {
					// Check if it's a directory and starts with "Impression"
					return file.isDirectory() && file.getName().startsWith("Impression");
				}
			});		           
		} else {
			logger.error(dirPath + " is not a directory.");
		}
		int numberOfSubfolders = listDir.length;

		int min = 1;
		int max = numberOfSubfolders;
		int randomNumber;
		String beforescenario = VariableManager.getVariableValue(contextKey, SCENARIO).toString();
		String afterscenario = beforescenario.substring(0, beforescenario.indexOf(':'));
		if (afterscenario.contains("_")) {
			afterscenario = afterscenario.replace("_", "0");
		}
		int currentScenarioNumber = Integer.valueOf(afterscenario);

		// Generate a random number that is not equal to currentScenarioNumber
		randomNumber = (int) (Math.random() * (max - min)) + min;
		int impressionToPick = (currentScenarioNumber < numberOfSubfolders) ? currentScenarioNumber : randomNumber;

		dirPath = FingerprintVariationGenerator.fingerprintVariationGenerator(contextKey, currentScenarioNumber, impressionToPick);

		List<File> firstSet = CommonUtil.listFiles(dirPath + "/" + String.format("/Impression_%d/fp_1/", impressionToPick));

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

	// generate using Anguli

	static Hashtable<Integer, List<File>> generateFingerprint(String outDir, int nFingerPrints,
			int nImpressionsPerPrints, int nThreads, FPClassDistribution classDist, String contextKey) {

		Hashtable<Integer, List<File>> tblFiles = new Hashtable<Integer, List<File>>();

		// C:\Mosip.io\gitrepos\biometric-data\anguli
		String[] commands = { DataProviderConstants.ANGULI_PATH + "/Anguli.exe", "-outdir", outDir, "-numT",
				String.format("%d", nThreads), "-num", String.format("%d", nFingerPrints), "-ni",
				String.format("%d", nImpressionsPerPrints), "-cdist", classDist.name() };
		RestClient.logInfo(contextKey, "Anguli commands" + commands);
		ProcessBuilder pb = new ProcessBuilder(commands);
		pb.directory(new File(DataProviderConstants.ANGULI_PATH));

		try {
			Process proc = pb.start(); // rt.exec(commands);
			BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
			// Read any errors from the attempted command
			// logger.info("Error:\n");
			String s;

			while ((s = stdError.readLine()) != null) {
				RestClient.logInfo(contextKey, s);
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

	// Left Eye, Right Eye
	static List<IrisDataModel> generateIris(int count, String contextKey) throws Exception {
		List<IrisDataModel> retVal = new ArrayList<IrisDataModel>();

		IrisDataModel m = new IrisDataModel();

		Object val = VariableManager.getVariableValue(VariableManager.NS_DEFAULT, "enableExternalBiometricSource");
		boolean bExternalSrc = false;
		// BufferedImage img = null;

		if (val != null)
			bExternalSrc = Boolean.valueOf(val.toString());

		if (bExternalSrc) {

			// folder where all bio input available
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
			// reach cached finger prints from folder
			RestClient.logInfo(contextKey, DIRPATH + srcPath);
			Hashtable<Integer, List<File>> tblFiles = new Hashtable<Integer, List<File>>();
			File dir = new File(srcPath);

			File listDir[] = dir.listFiles();
			int numberOfSubfolders = listDir.length;

			int min = 1;
			int max = numberOfSubfolders;
			int randomNumber = (int) (Math.random() * (max - min)) + min;
			String beforescenario = VariableManager.getVariableValue(contextKey, SCENARIO).toString();
			String afterscenario = beforescenario.substring(0, beforescenario.indexOf(':'));
			if (afterscenario.contains("_")) {
				afterscenario = afterscenario.replace("_", "0");
			}

			int currentScenarioNumber = Integer.valueOf(afterscenario);

			// If the available impressions are less than scenario number, pick the random
			// one
			// otherwise pick the impression of same of scenario number
			int impressionToPick = (currentScenarioNumber < numberOfSubfolders) ? currentScenarioNumber : randomNumber;
			srcPath = IrisVariationGenerator.irisVariationGenerator(contextKey,currentScenarioNumber,impressionToPick);

			File folder = new File(srcPath+"/" + String.format("%03d", impressionToPick));
			logger.info(srcPath+"/" + String.format("%03d", impressionToPick));

			File[] listOfFiles = folder.listFiles();
			//			listOfFiles=getRandomIrisVariation(listOfFiles);

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
			String fPathL = srcPath + "/" + String.format("%03d", impressionToPick) + "/" + leftbmp;
			String fPathR = srcPath + "/" + String.format("%03d", impressionToPick) + "/" + rightbmp;
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

	// Left Eye, Right Eye
	static List<IrisDataModel> updateIris(String contextKey) throws Exception {

		List<IrisDataModel> retVal = new ArrayList<IrisDataModel>();
		IrisDataModel m = new IrisDataModel();
		String srcPath = System.getProperty("java.io.tmpdir")
				+ VariableManager.getVariableValue(contextKey, "mosip.test.persona.irisdatapath").toString();
		String leftbmp = null;
		String rightbmp = null;
		// reach cached finger prints from folder
		RestClient.logInfo(contextKey, DIRPATH + srcPath);
		File dir = new File(srcPath);

		File listDir[] = dir.listFiles();
		int numberOfSubfolders = listDir.length;

		int min = 1;
		int max = numberOfSubfolders;
		int randomNumber;
		String beforescenario = VariableManager.getVariableValue(contextKey, SCENARIO).toString();
		String afterscenario = beforescenario.substring(0, beforescenario.indexOf(':'));
		if (afterscenario.contains("_")) {
			afterscenario = afterscenario.replace("_", "0");
		}
		int currentScenarioNumber = Integer.valueOf(afterscenario);

		// Generate a random number that is not equal to currentScenarioNumber
		randomNumber = (int) (Math.random() * (max - min)) + min;
		int impressionToPick = (currentScenarioNumber < numberOfSubfolders) ? currentScenarioNumber : randomNumber;

		srcPath = IrisVariationGenerator.irisVariationGenerator(contextKey,currentScenarioNumber,impressionToPick);

		File folder = new File(srcPath +"/" + String.format("%03d", impressionToPick));

		File[] listOfFiles = folder.listFiles();
		//		listOfFiles=getRandomIrisVariation(listOfFiles);
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
		String fPathL = srcPath + "/" + String.format("%03d", impressionToPick) + "/" + leftbmp;
		String fPathR = srcPath + "/" + String.format("%03d", impressionToPick) + "/" + rightbmp;

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

	static byte[][] updateFaceData(String contextKey) {

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
			int numberOfSubfolders = listDir.length;

			int min = 1;
			int max = numberOfSubfolders;
			int randomNumber;
			String beforescenario = VariableManager.getVariableValue(contextKey, SCENARIO).toString();
			String afterscenario = beforescenario.substring(0, beforescenario.indexOf(':'));
			if (afterscenario.contains("_")) {
				afterscenario = afterscenario.replace("_", "0");
			}
			int currentScenarioNumber = Integer.valueOf(afterscenario);

			// Generate a random number that is not equal to currentScenarioNumber
			randomNumber = (int) (Math.random() * (max - min)) + min;
			int impressionToPick = (currentScenarioNumber < numberOfSubfolders) ? currentScenarioNumber : randomNumber;

			dirPath=FaceVariationGenerator.faceVariationGenerator(contextKey, currentScenarioNumber, impressionToPick);

			List<File> firstSet = CommonUtil.listFiles(dirPath + "/face_data/");
			
			List<File> filteredFiles = firstSet.stream().filter(file -> file.getName().contains("00"+impressionToPick)).toList();
			BufferedImage img = null;

			try (FileInputStream fos = new FileInputStream(filteredFiles.get(0));
					BufferedInputStream bis = new BufferedInputStream(fos)) {
				img = ImageIO.read(bis);
				logger.info("Image picked from this path=" + filteredFiles.get(0));
			}
			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			ImageIO.write(img, "jpg", baos);
			baos.flush();
			bData = baos.toByteArray();
			bencoded = PhotoProvider.encodeFaceImageData(bData);

			baos.close();
			CommonUtil.deleteOldTempDir(dirPath);
		} catch (Exception e) {

			logger.error(e.getMessage());
		}
		return new byte[][] { bencoded, bData };
	}

	public static void main(String[] args) {

		try {

			String value = buildBirFinger("addfdfd", "finger", "jwtSign", PAYLOAD, null, true, FALSE, null);
			logger.info(value);
		} catch (FileNotFoundException e2) {
			logger.error(e2.getMessage());
		} catch (ParserConfigurationException e2) {
			logger.error(e2.getMessage());
		} catch (FactoryConfigurationError e2) {
			logger.error(e2.getMessage());
		} catch (TransformerException e2) {
			logger.error(e2.getMessage());
		}

		try {
			List<IrisDataModel> m = generateIris(1, "contextKey");
			m.forEach(im -> {
				logger.info(im.getLeftHash());
				logger.info(im.getRightHash());

			});
		} catch (Exception e1) {
			logger.error(e1.getMessage());
		}

		BiometricDataModel bio = null;
		try {
			bio = getBiometricData(true, "contextkey");
		} catch (IOException e1) {
			logger.error(e1.getMessage());
		}

		String xml = "";
		List<String> lstBioAttributes = new ArrayList<String>();
		lstBioAttributes.add(LEFTEYE);
		lstBioAttributes.add(RIGHTEYE);

		try {
			xml = toCBEFF(lstBioAttributes, bio, "cbeffallfingersOut.xml", true, null);

		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		logger.info(xml);
	}

	public static File[] getRandomIrisVariation(File[] listOfFiles) {
		if (listOfFiles == null || listOfFiles.length == 0) {
			System.out.println("No image files found in the directory.");
			return null;
		}

		// Maps to store matching left and right iris images
		Map<String, File> leftIrisImages = new HashMap<>();
		Map<String, File> rightIrisImages = new HashMap<>();

		// Categorizing left and right images
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

		// Find common image names
		List<String> commonNames = new ArrayList<>(leftIrisImages.keySet());
		commonNames.retainAll(rightIrisImages.keySet());

		if (commonNames.isEmpty()) {
			System.out.println("No matching left and right iris images found.");
			return null;
		}

		// Pick one random image pair
		Random random = new Random();
		String selectedName = commonNames.get(random.nextInt(commonNames.size()));

		File selectedLeftImage = leftIrisImages.get(selectedName);
		File selectedRightImage = rightIrisImages.get(selectedName);

		// Store selected files in an array
		File[] selectedImages = {selectedLeftImage, selectedRightImage};

		// Print selected images
		System.out.println("Selected Left Iris Image: " + selectedLeftImage.getAbsolutePath());
		System.out.println("Selected Right Iris Image: " + selectedRightImage.getAbsolutePath());

		return selectedImages;
	}

}
