package io.mosip.testrig.dslrig.dataprovider;

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
import java.util.Arrays;
//import java.util.Arrays;
import java.util.Base64;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.codec.binary.Hex;
import io.mosip.testrig.dslrig.dataprovider.mds.MDSClient;
import io.mosip.testrig.dslrig.dataprovider.mds.MDSClientInterface;
import io.mosip.testrig.dslrig.dataprovider.mds.MDSClientNoMDS;
import io.mosip.testrig.dslrig.dataprovider.models.BioModality;
//import org.apache.commons.io.IOUtils;
//import org.apache.commons.lang3.tuple.Pair;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.jamesmurty.utils.XMLBuilder;
//import java.util.Date;

import io.mosip.mock.sbi.test.CentralizedMockSBI;
import io.mosip.testrig.dslrig.dataprovider.variables.VariableManager;

public class BiometricDataProvider {

	public static HashMap<String, Integer> portmap = new HashMap();
	private static final Logger logger = LoggerFactory.getLogger(BiometricDataProvider.class);

	// String constants
	private static final String XMLNS = "xmlns";
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
	private static final String AUTHCERTSPATH = "authCertsPath";
	private static final String LEFTEYE = "leftEye";
	private static final String RIGHTEYE = "rightEye";
	private static final String RIGHT = "Right";
	private static final String MOUNTPATH = "mountPath";
	private static final String DIRPATH = "dirPath ";
	private static final String SCENARIO = "scenario";

	static String buildBirIris(String irisInfo, String irisName, String jtwSign, String payload, String qualityScore,
			boolean genarateValidCbeff, String exception) throws ParserConfigurationException,
			FactoryConfigurationError, TransformerException, FileNotFoundException {
		String today = CommonUtil.getUTCDateTime(null);
		XMLBuilder builder = XMLBuilder.create("BIR").a(XMLNS, "http://standards.iso.org/iso-iec/19785/-3/ed-2/")
				.e(VERSION).e(MAJOR).t("1").up().e(MINOR).t("1").up().up().e(CBEFFVERSION).e(MAJOR).t("1").up().e(MINOR)
				.t("1").up().up().e(BIRINFO).e(INTEGRITY).t(FALSE).up().up().e(BDBINFO).e(FORMAT).e(ORGANIZATION)
				.t(MOSIP).up().e("Type").t("9").up().up().e(CREATIONDATE).t(today).up().e("Type").t("Iris").up()
				.e(SUBTYPE).t(irisName).up().e(LEVEL).t("Raw").up().e(PURPOSE).t(ENROLL).up().e(QUALITY).e(ALGORITHM)
				.e(ORGANIZATION).t("HMAC").up().e("Type").t(SHA_256).up().up().e(SCORE).t(qualityScore).up().up().up()
				.e("BDB").t(irisInfo).up().up();
		if (jtwSign != null && payload != null) {
			jtwSign = Base64.getEncoder().encodeToString(jtwSign.getBytes());
			builder.e("SB").t(jtwSign).up().

					e(OTHERS).e(ENTRY).a("key", EXCEPTION).t(exception).up().e(ENTRY).a("key", RETRIES).t("1").up()
					.e(ENTRY).a("key", SDK_SCORE).t("0.0").up().e(ENTRY).a("key", FORCE_CAPTURED).t(FALSE).up().e(ENTRY)
					.a("key", PAYLOAD).t(payload).up().e(ENTRY).a("key", SPEC_VERSION).t("0.9.5").up().up();
		}

		return builder.asString(null);
	}

	static String buildBirFinger(String fingerInfo, String fingerName, String jtwSign, String payload,

			String qualityScore, boolean generateValidCbeff, String exception) throws ParserConfigurationException,
			FactoryConfigurationError, TransformerException, FileNotFoundException {
		String today = CommonUtil.getUTCDateTime(null);
		XMLBuilder builder = null;
		String bdbKey = "BDB";
		if (generateValidCbeff == false)
			bdbKey = "invalidBDB";
		builder = XMLBuilder.create("BIR").a(XMLNS, "http://standards.iso.org/iso-iec/19785/-3/ed-2/").e(VERSION)
				.e(MAJOR).t("1").up().e(MINOR).t("1").up().up().e(CBEFFVERSION).e(MAJOR).t("1").up().e(MINOR).t("1")
				.up().up().e(BIRINFO).e(INTEGRITY).t(FALSE).up().up().e(BDBINFO).e(FORMAT).e(ORGANIZATION).t(MOSIP).up()
				.e("Type").t("7").up().up().e(CREATIONDATE).t(today).up().e("Type").t("Finger").up().e(SUBTYPE)
				.t(fingerName).up().e(LEVEL).t("Raw").up().e(PURPOSE).t(ENROLL).up().e(QUALITY).e(ALGORITHM)
				.e(ORGANIZATION).t("HMAC").up().e("Type").t(SHA_256).up().up().e(SCORE).t(qualityScore).up().up().up()
				.e(bdbKey).t(fingerInfo).up().up();
		if (jtwSign != null && payload != null) {
			jtwSign = Base64.getEncoder().encodeToString(jtwSign.getBytes());
			builder.e("SB").t(jtwSign).up().

					e(OTHERS).e(ENTRY).a("key", EXCEPTION).t(exception).up().e(ENTRY).a("key", RETRIES).t("1").up()
					.e(ENTRY).a("key", SDK_SCORE).t("0.0").up().e(ENTRY).a("key", FORCE_CAPTURED).t(FALSE).up().e(ENTRY)
					.a("key", PAYLOAD).t(payload).up().e(ENTRY).a("key", SPEC_VERSION).t("0.9.5").up().up();
		}

		return builder.asString(null);
	}

	static String buildBirFace(String faceInfo, String jtwSign, String payload, String qualityScore,
			boolean genarateValidCbeff, String exception) throws ParserConfigurationException,
			FactoryConfigurationError, TransformerException, FileNotFoundException {
		String today = CommonUtil.getUTCDateTime(null);
		XMLBuilder builder = XMLBuilder.create("BIR").a(XMLNS, "http://standards.iso.org/iso-iec/19785/-3/ed-2/")
				.e(VERSION).e(MAJOR).t("1").up().e(MINOR).t("1").up().up().e(CBEFFVERSION).e(MAJOR).t("1").up().e(MINOR)
				.t("1").up().up().e(BIRINFO).e(INTEGRITY).t(FALSE).up().up().e(BDBINFO).e(FORMAT).e(ORGANIZATION)
				.t(MOSIP).up().e("Type").t("8").up().up().e(CREATIONDATE).t(today).up().e("Type").t("Face").up()
				.e(SUBTYPE).t("").up().e(LEVEL).t("Raw").up().e(PURPOSE).t(ENROLL).up().e(QUALITY).e(ALGORITHM)
				.e(ORGANIZATION).t("HMAC").up().e("Type").t(SHA_256).up().up().e(SCORE).t(qualityScore).up().up().up()
				.e("BDB").t(faceInfo).up().up();
		if (jtwSign != null && payload != null) {
			jtwSign = Base64.getEncoder().encodeToString(jtwSign.getBytes());
			builder.e("SB").t(jtwSign).up().

					e(OTHERS).e(ENTRY).a("key", EXCEPTION).t(exception).up().e(ENTRY).a("key", RETRIES).t("1").up()
					.e(ENTRY).a("key", SDK_SCORE).t("0.0").up().e(ENTRY).a("key", FORCE_CAPTURED).t(FALSE).up().e(ENTRY)
					.a("key", PAYLOAD).t(payload).up().e(ENTRY).a("key", SPEC_VERSION).t("0.9.5").up().up();

		}

		return builder.asString(null);
	}

	static String buildBirExceptionPhoto(String faceInfo, String jtwSign, String payload, String qualityScore,
			boolean genarateValidCbeff, String exception) throws ParserConfigurationException,
			FactoryConfigurationError, TransformerException, FileNotFoundException {
		String today = CommonUtil.getUTCDateTime(null);
		XMLBuilder builder = XMLBuilder.create("BIR").a(XMLNS, "http://standards.iso.org/iso-iec/19785/-3/ed-2/")
				.e(VERSION).e(MAJOR).t("1").up().e(MINOR).t("1").up().up().e(CBEFFVERSION).e(MAJOR).t("1").up().e(MINOR)
				.t("1").up().up().e(BIRINFO).e(INTEGRITY).t(FALSE).up().up().e(BDBINFO).e(FORMAT).e(ORGANIZATION)
				.t(MOSIP).up().e("Type").t("8").up().up().e(CREATIONDATE).t(today).up().e("Type").t("ExceptionPhoto")
				.up().e(SUBTYPE).t("").up().e(LEVEL).t("Raw").up().e(PURPOSE).t(ENROLL).up().e(QUALITY).e(ALGORITHM)
				.e(ORGANIZATION).t("HMAC").up().e("Type").t(SHA_256).up().up().e(SCORE).t(qualityScore).up().up().up()
				.e("BDB").t(faceInfo).up().up();
		if (jtwSign != null && payload != null) {
			jtwSign = Base64.getEncoder().encodeToString(jtwSign.getBytes());
			builder.e("SB").t(jtwSign).up().

					e(OTHERS).e(ENTRY).a("key", EXCEPTION).t(exception).up().e(ENTRY).a("key", RETRIES).t("1").up()
					.e(ENTRY).a("key", SDK_SCORE).t("0.0").up().e(ENTRY).a("key", FORCE_CAPTURED).t(FALSE).up().e(ENTRY)
					.a("key", PAYLOAD).t(payload).up().e(ENTRY).a("key", SPEC_VERSION).t("0.9.5").up().up();

		}

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

				String certsDir = System.getenv(AUTHCERTSPATH) == null
						? VariableManager.getVariableValue(contextKey, AUTHCERTSPATH).toString()
						: System.getenv(AUTHCERTSPATH);

				if (certsDir == null || certsDir.length() == 0) {
					certsDir = System.getProperty("java.io.tmpdir") + File.separator + "AUTHCERTS";
				}

				Path p12path = Paths.get(certsDir,
						"DSL-IDA-" + VariableManager.getVariableValue(contextKey, "db-server"));

				logger.info("p12path" + p12path);

				port = CentralizedMockSBI.startSBI(contextKey, "Registration", "Biometric Device", p12path.toString());

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
			logger.info("mds score is changed to : " + qualityScore);

			biodata = resident.getBiometric();

		}

		catch (Throwable t) {
			logger.error(" Port issue "+ contextKey, t);
			t.getStackTrace();
			return capture;
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
			logger.error(" Face get capture   fail"+ contextKey, t);
			t.getStackTrace();
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
			logger.error(" IRIS get capture  fail"+ contextKey, t);
			t.getStackTrace();
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
			logger.error("Finger get capture fail"+ contextKey, t);
			t.getStackTrace();
		}

		try {
			// Step 4 : Exceptionphoto face capture
			if (bioExceptions != null && !bioExceptions.isEmpty()) {

				List<MDSDevice> exceptionfaceDevices = mds.getRegDeviceInfo(DataProviderConstants.MDS_DEVICE_TYPE_FACE);
				MDSDevice exceptionDevice = exceptionfaceDevices.get(0);
				try {
					capture = mds.captureFromRegDevice(exceptionDevice, capture,
							DataProviderConstants.MDS_DEVICE_TYPE_FACE, null, 60,
							exceptionDevice.getDeviceSubId().get(0), port, contextKey, bioexceptionlist);
					// rename the key with exception_photo
				} catch (Throwable t) {
					logger.error("Exception photo capture failure"+ contextKey, t);
					t.getStackTrace();
				}

			}

		}

		catch (Throwable t) {
			logger.error("Exceptionphoto face capture", t);
			t.getStackTrace();
		}

		mds.removeProfile(mdsprofilePath, profileName, port, contextKey);
		CentralizedMockSBI.stopSBI(contextKey);
		return capture;
	}

	public static String toCBEFFFromCapture(List<String> bioFilter, MDSRCaptureModel capture, String toFile,
			List<String> missAttribs, boolean genarateValidCbeff, List<BioModality> exceptionlist) throws Exception {

		String retXml = "";

		String mosipVersion = null;
		try {
			mosipVersion = VariableManager.getVariableValue(VariableManager.NS_DEFAULT, "mosip.version").toString();
		} catch (Exception e) {

		}

		XMLBuilder builder = XMLBuilder.create("BIR").a(XMLNS, "http://standards.iso.org/iso-iec/19785/-3/ed-2/")
				.e(BIRINFO).e(INTEGRITY).t(FALSE).up().up();

		builder.getDocument().setXmlStandalone(true);

		List<String> bioSubType = new ArrayList<>();

		// Step 1: convert finger print
		try {
			List<MDSDeviceCaptureModel> lstFingerData = capture.getLstBiometrics()
					.get(DataProviderConstants.MDS_DEVICE_TYPE_FINGER);

			builder = xmlbuilderFinger(bioFilter, lstFingerData, bioSubType, builder, exceptionlist,
					genarateValidCbeff);

			if (exceptionlist != null && !exceptionlist.isEmpty()) {
				builder = xmlbuilderFingerExep(bioFilter, exceptionlist, bioSubType, builder, genarateValidCbeff);
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
						FALSE);
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

			builder = xmlbuilderIris(bioFilter, lstIrisData, bioSubType, builder, genarateValidCbeff, exceptionlist);

			if (exceptionlist != null && !exceptionlist.isEmpty()) {
				builder = xmlbuilderIrisExcep(bioFilter, exceptionlist, bioSubType, builder, genarateValidCbeff);
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
						FALSE);
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
			PrintWriter writer = new PrintWriter(new FileOutputStream(toFile));
			builder.toWriter(true, writer, null);
		}

		retXml = builder.asString(null);
		return retXml;
	}

	private static XMLBuilder xmlbuilderIris(List<String> bioFilter, List<MDSDeviceCaptureModel> lstIrisData,
			List<String> bioSubType, XMLBuilder builder, boolean genarateValidCbeff, List<BioModality> exceptionlst)

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

		logger.info("withoutExceptionList is: " + listWithoutExceptions);

		try {
			if (lstIrisData != null) {
				String irisXml = "";
				for (MDSDeviceCaptureModel cm : lstIrisData) {

					if (listWithoutExceptions.contains(LEFTEYE) && cm.getBioSubType().equals("Left")) {
						irisXml = buildBirIris(cm.getBioValue(), "Left", cm.getSb(), cm.getPayload(),
								cm.getQualityScore(), genarateValidCbeff, FALSE);
						builder = builder.importXMLBuilder(XMLBuilder.parse(irisXml));
						bioSubType.add("Left");
					}
					if (listWithoutExceptions.contains(RIGHTEYE) && cm.getBioSubType().equals(RIGHT)) {

						irisXml = buildBirIris(cm.getBioValue(), RIGHT, cm.getSb(), cm.getPayload(),
								cm.getQualityScore(), genarateValidCbeff, FALSE);
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
			List<String> bioSubType, XMLBuilder builder, boolean genarateValidCbeff) {
		try {
			if (lstIrisData != null) {
				for (BioModality finger : lstIrisData) {
					if (!finger.getType().equalsIgnoreCase("Iris"))
						continue;

					String strFingerXml = buildBirIris(finger.getType(), finger.getSubType(),
							Arrays.toString(new byte[0]), "", "0", genarateValidCbeff, "true");
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
			List<String> bioSubType, XMLBuilder builder, List<BioModality> exceptionlst, boolean genarateValidCbeff) {
		List<String> listWithoutExceptions = bioFilter;
		if (exceptionlst != null && !exceptionlst.isEmpty()) {
			List<String> exceptions = exceptionlst.stream().map(BioModality::getSubType).collect(Collectors.toList());
			logger.info("exceptions" + exceptions);
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
					logger.info("fingerData is: " + fingerData);
					if (i >= 0 && fingerData != null && currentCM != null) {
						String strFinger = DataProviderConstants.displayFingerName[i];
						String strFingerXml = buildBirFinger(fingerData, strFinger, currentCM.getSb(),
								currentCM.getPayload(), currentCM.getQualityScore(), genarateValidCbeff, FALSE);
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
			List<String> bioSubType, XMLBuilder builder, boolean genarateValidCbeff)
			throws ParserConfigurationException, FactoryConfigurationError, TransformerException, SAXException,
			IOException {

		if (lstFingerData != null) {
			for (BioModality finger : lstFingerData) {
				if (finger.getType().equalsIgnoreCase("eye") || finger.getType().equalsIgnoreCase("face"))
					continue;

				String strFingerXml = buildBirFinger(finger.getType(), finger.getSubType(),
						Arrays.toString(new byte[0]), "", "0", genarateValidCbeff, "true");
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
			boolean genarateValidCbeff) throws Exception {
		String retXml = "";

		XMLBuilder builder = XMLBuilder.create("BIR").a(XMLNS, "http://standards.iso.org/iso-iec/19785/-3/ed-2/")
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
						genarateValidCbeff, FALSE);

				XMLBuilder fbuilder = XMLBuilder.parse(strFingerXml);
				builder = builder.importXMLBuilder(fbuilder);
			}

		}

		// Step 2: Add Face
		if (bioFilter.contains("Face")) {
			if (biometricDataModel.getEncodedPhoto() != null) {
				String faceXml = buildBirFace(biometricDataModel.getEncodedPhoto(), null, null, qualityScore,
						genarateValidCbeff, "true");
				builder = builder.importXMLBuilder(XMLBuilder.parse(faceXml));
			}
		}

		// Step 3: Add IRIS
		IrisDataModel irisInfo = biometricDataModel.getIris();
		if (irisInfo != null) {
			String irisXml = "";
			if (bioFilter.contains(LEFTEYE)) {
				irisXml = buildBirIris(irisInfo.getLeft(), "Left", null, null, qualityScore, genarateValidCbeff,
						"true");
				builder = builder.importXMLBuilder(XMLBuilder.parse(irisXml));
			}
			if (bioFilter.contains(RIGHTEYE)) {
				irisXml = buildBirIris(irisInfo.getRight(), RIGHT, null, null, qualityScore, genarateValidCbeff,
						"true");
				builder = builder.importXMLBuilder(XMLBuilder.parse(irisXml));
			}
		}

		if (toFile != null) {
			PrintWriter writer = new PrintWriter(new FileOutputStream(toFile));
			builder.toWriter(true, writer, null);
		}
		retXml = builder.asString(null);
		return retXml;
	}

	public static Hashtable<Integer, List<File>> impressionCaptureList(String contextKey) {
		// reach cached finger prints from folder
		String dirPath = VariableManager.getVariableValue(contextKey, MOUNTPATH).toString()
				+ VariableManager.getVariableValue(contextKey, "mosip.test.persona.fingerprintdatapath").toString();
		logger.info(DIRPATH + dirPath);
		Hashtable<Integer, List<File>> tblFiles = new Hashtable<Integer, List<File>>();
		File dir = new File(dirPath);

		File listDir[] = dir.listFiles();
		int numberOfSubfolders = listDir.length;

		int min = 1;
		int max = numberOfSubfolders;
		int randomNumber = (int) (Math.random() * (max - min)) + min;
		String beforescenario = VariableManager.getVariableValue(contextKey, SCENARIO).toString();
		String afterscenario = beforescenario.substring(0, beforescenario.indexOf(':'));

		int currentScenarioNumber = Integer.valueOf(afterscenario);

		// If the available impressions are less than scenario number, pick the random
		// one

		// otherwise pick the impression of same of scenario number
		int impressionToPick = (currentScenarioNumber < numberOfSubfolders) ? currentScenarioNumber : randomNumber;

		logger.info("currentScenarioNumber=" + currentScenarioNumber + " numberOfSubfolders=" + numberOfSubfolders
				+ " impressionToPick=" + impressionToPick);
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
					byte[] fdata = Files.readAllBytes(Paths.get(fPath));
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
							FPClassDistribution.arch);
					List<File> firstSet = prints.get(1);

					String[] fingerPrints = new String[10];
					String[] fingerPrintHash = new String[10];
					byte[][] fingerPrintRaw = new byte[10][1];

					int index = 0;
					for (File f : firstSet) {

						if (index > 9)
							break;
						Path path = Paths.get(f.getAbsolutePath());
						byte[] fdata = Files.readAllBytes(path);
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
				String dirPath = VariableManager.getVariableValue(contextKey, MOUNTPATH).toString() + VariableManager
						.getVariableValue(contextKey, "mosip.test.persona.fingerprintdatapath").toString();
				logger.info(DIRPATH + dirPath);
				Hashtable<Integer, List<File>> tblFiles = new Hashtable<Integer, List<File>>();
				File dir = new File(dirPath);

				File listDir[] = dir.listFiles();
				int numberOfSubfolders = listDir.length;

				int min = 1;
				int max = numberOfSubfolders;
				int randomNumber = (int) (Math.random() * (max - min)) + min;
				String beforescenario = VariableManager.getVariableValue(contextKey, SCENARIO).toString();
				String afterscenario = beforescenario.substring(0, beforescenario.indexOf(':'));

				int currentScenarioNumber = Integer.valueOf(afterscenario);

				// If the available impressions are less than scenario number, pick the random
				// one

				// otherwise pick the impression of same of scenario number
				int impressionToPick = (currentScenarioNumber < numberOfSubfolders) ? currentScenarioNumber
						: randomNumber;

				logger.info("currentScenarioNumber=" + currentScenarioNumber + " numberOfSubfolders="
						+ numberOfSubfolders + " impressionToPick=" + impressionToPick);

				for (int i = min; i <= max; i++) {

					List<File> lst = CommonUtil.listFiles(dirPath + String.format("/Impression_%d/fp_1/", i));
					tblFiles.put(i, lst);
				}

				String[] fingerPrints = new String[10];
				String[] fingerPrintHash = new String[10];
				byte[][] fingerPrintRaw = new byte[10][1];
				List<File> firstSet = tblFiles.get(impressionToPick);
				logger.info("Impression used " + impressionToPick);

				int index = 0;
				for (File f : firstSet) {

					if (index > 9)
						break;
					Path path = Paths.get(f.getAbsolutePath());
					byte[] fdata;
					try {
						fdata = Files.readAllBytes(path);
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

			}

		}
		return data;
	}

	// generate using Anguli

	static Hashtable<Integer, List<File>> generateFingerprint(String outDir, int nFingerPrints,
			int nImpressionsPerPrints, int nThreads, FPClassDistribution classDist) {

		Hashtable<Integer, List<File>> tblFiles = new Hashtable<Integer, List<File>>();

		// C:\Mosip.io\gitrepos\biometric-data\anguli
		String[] commands = { DataProviderConstants.ANGULI_PATH + "/Anguli.exe", "-outdir", outDir, "-numT",
				String.format("%d", nThreads), "-num", String.format("%d", nFingerPrints), "-ni",
				String.format("%d", nImpressionsPerPrints), "-cdist", classDist.name() };
		logger.info("Anguli commands" + commands);
		ProcessBuilder pb = new ProcessBuilder(commands);
		pb.directory(new File(DataProviderConstants.ANGULI_PATH));

		try {
			Process proc = pb.start(); // rt.exec(commands);
			BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
			// Read any errors from the attempted command
			// logger.info("Error:\n");
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

	public static IrisDataModel loadIris(String filePath, String subModality, IrisDataModel im) throws Exception {

		IrisDataModel m = im;
		if (m == null)
			m = new IrisDataModel();
		String irisData = "";
		String irisHash = "";

		if (Files.exists(Paths.get(filePath))) {
			byte[] fdata = Files.readAllBytes(Paths.get(filePath));
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
				fdata = Files.readAllBytes(Paths.get(fPathL));
				leftIrisData = Hex.encodeHexString(fdata);
				irisHash = CommonUtil.getHexEncodedHash(fdata);
				m.setLeftHash(irisHash);
			}
			if (Files.exists(Paths.get(fPathR))) {
				frdata = Files.readAllBytes(Paths.get(fPathR));
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
			String srcPath = VariableManager.getVariableValue(contextKey, MOUNTPATH).toString()
					+ VariableManager.getVariableValue(contextKey, "mosip.test.persona.irisdatapath").toString();

			int num = new File(srcPath).list().length;
			int[] index = CommonUtil.generateRandomNumbers(count, num, 1);
			String leftbmp = null;
			String rightbmp = null;
			// reach cached finger prints from folder
			logger.info(DIRPATH + srcPath);
			Hashtable<Integer, List<File>> tblFiles = new Hashtable<Integer, List<File>>();
			File dir = new File(srcPath);

			File listDir[] = dir.listFiles();
			int numberOfSubfolders = listDir.length;

			int min = 1;
			int max = numberOfSubfolders;
			int randomNumber = (int) (Math.random() * (max - min)) + min;
			String beforescenario = VariableManager.getVariableValue(contextKey, SCENARIO).toString();
			String afterscenario = beforescenario.substring(0, beforescenario.indexOf(':'));

			int currentScenarioNumber = Integer.valueOf(afterscenario);

			// If the available impressions are less than scenario number, pick the random
			// one

			// otherwise pick the impression of same of scenario number
			int impressionToPick = (currentScenarioNumber < numberOfSubfolders) ? currentScenarioNumber : randomNumber;

			File folder = new File(srcPath + "/" + String.format("%03d", impressionToPick));

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
			String fPathL = srcPath + "/" + String.format("%03d", impressionToPick) + "/" + leftbmp;
			String fPathR = srcPath + "/" + String.format("%03d", impressionToPick) + "/" + rightbmp;

			String leftIrisData = "";
			String rightIrisData = "";
			String irisHash = "";
			byte[] fldata = null;
			byte[] frdata = null;
			if (Files.exists(Paths.get(fPathL))) {
				fldata = Files.readAllBytes(Paths.get(fPathL));
				leftIrisData = Hex.encodeHexString(fldata);
				irisHash = CommonUtil.getHexEncodedHash(fldata);
				m.setLeftHash(irisHash);
			}
			if (Files.exists(Paths.get(fPathR))) {
				frdata = Files.readAllBytes(Paths.get(fPathR));
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
		}

		return retVal;
	}

	public static void main(String[] args) {

		try {

			String value = buildBirFinger("addfdfd", "finger", "jwtSign", PAYLOAD, null, true, FALSE);
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
			xml = toCBEFF(lstBioAttributes, bio, "cbeffallfingersOut.xml", true);

		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		logger.info(xml);
	}
}
