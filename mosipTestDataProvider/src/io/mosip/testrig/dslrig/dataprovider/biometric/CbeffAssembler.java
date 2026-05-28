package io.mosip.testrig.dslrig.dataprovider.biometric;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.jamesmurty.utils.XMLBuilder;

import io.mosip.testrig.dslrig.dataprovider.models.BioModality;
import io.mosip.testrig.dslrig.dataprovider.models.BiometricDataModel;
import io.mosip.testrig.dslrig.dataprovider.models.IrisDataModel;
import io.mosip.testrig.dslrig.dataprovider.models.mds.MDSDeviceCaptureModel;
import io.mosip.testrig.dslrig.dataprovider.models.mds.MDSRCaptureModel;
import io.mosip.testrig.dslrig.dataprovider.util.CommonUtil;
import io.mosip.testrig.dslrig.dataprovider.util.DataProviderConstants;
import io.mosip.testrig.dslrig.dataprovider.util.RestClient;
import io.mosip.testrig.dslrig.dataprovider.variables.VariableManager;

public final class CbeffAssembler {

	private static final Logger logger = LoggerFactory.getLogger(CbeffAssembler.class);

	private static final String XMLNS = "xmlns";
	private static final String XMLNS_URL = "http://standards.iso.org/iso-iec/19785/-3/ed-2/";
	private static final String BIRINFO = "BIRInfo";
	private static final String INTEGRITY = "Integrity";
	private static final String FALSE = "false";
	private static final String OTHERS = "others";
	private static final String LEFTEYE = "leftEye";
	private static final String RIGHTEYE = "rightEye";
	private static final String RIGHT = "Right";
	private static final String EXCEPTION = "EXCEPTION";

	private CbeffAssembler() {
	}

	public static String toCBEFFFromCapture(List<String> bioFilter, MDSRCaptureModel capture, String toFile,
			List<String> missAttribs, boolean genarateValidCbeff, List<BioModality> exceptionlist, String contextKey)
			throws Exception {
		logger.info("CAPTURE XML DATA: " + capture.toString());

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

		try {
			if (bioFilter.contains("face")) {

				List<MDSDeviceCaptureModel> lstFaceData = capture.getLstBiometrics()
						.get(DataProviderConstants.MDS_DEVICE_TYPE_FACE);
				bioSubType.add("face");
				String faceXml = CbeffBirBuilder.buildBirFace(lstFaceData.get(0).getBioValue(), lstFaceData.get(0).getSb(),
						lstFaceData.get(0).getPayload(), lstFaceData.get(0).getQualityScore(), genarateValidCbeff,
						FALSE, contextKey);
				builder = builder.importXMLBuilder(XMLBuilder.parse(faceXml));

			}
		} catch (Exception e) {
			logger.error("buildBirFace failed");
			logger.error(e.getMessage());
		}

		try {
			if (bioFilter.contains(LEFTEYE) && bioFilter.contains(RIGHTEYE)) {

				List<MDSDeviceCaptureModel> lstIrisData = capture.getLstBiometrics()
						.get(DataProviderConstants.MDS_DEVICE_TYPE_IRIS);
				logger.info("IRIS DATA XML DATA: " + lstIrisData.toString());

				builder = xmlbuilderIris(bioFilter, lstIrisData, bioSubType, builder, genarateValidCbeff, exceptionlist,
						contextKey);
			}
			if (exceptionlist != null && !exceptionlist.isEmpty()) {
				builder = xmlbuilderIrisExcep(bioFilter, exceptionlist, bioSubType, builder, genarateValidCbeff,
						contextKey);
			}
		} catch (Exception e) {
			logger.error("xmlbuilderIris failed");
			logger.error(e.getMessage());
		}

		try {
			if (exceptionlist != null && !exceptionlist.isEmpty()) {
				List<MDSDeviceCaptureModel> lstFaceData = capture.getLstBiometrics().get(EXCEPTION);
				bioSubType.add("exceptionphoto");
				String faceXml = CbeffBirBuilder.buildBirExceptionPhoto(lstFaceData.get(1).getBioValue(),
						lstFaceData.get(1).getSb(),
						lstFaceData.get(1).getPayload(), lstFaceData.get(1).getQualityScore(), genarateValidCbeff,
						FALSE, contextKey);
				builder = builder.importXMLBuilder(XMLBuilder.parse(faceXml));
			}
		} catch (Exception e) {
			logger.error("buildBirExceptionPhoto failed");
			logger.error(e.getMessage());
		}

		if (mosipVersion != null && mosipVersion.startsWith("1.2") && !bioSubType.isEmpty()) {
			builder.e(OTHERS).e("Key").t("CONFIGURED").up().e("Value")
					.t(bioSubType.toString().substring(1, bioSubType.toString().length() - 1)).up().up();
		}
		if (toFile != null) {
			Path safePath = CommonUtil.validateOutputPath(toFile, contextKey);
			try (FileOutputStream fos = new FileOutputStream(safePath.toFile());
					PrintWriter writer = new PrintWriter(fos)) {
				builder.toWriter(true, writer, null);
			}
		}

		retXml = builder.asString(null);
		return retXml;
	}

	public static String toCBEFF(List<String> bioFilter, BiometricDataModel biometricDataModel, String toFile,
			boolean genarateValidCbeff, String contextKey) throws Exception {
		String retXml = "";

		XMLBuilder builder = XMLBuilder.create("BIR").a(XMLNS, XMLNS_URL)
				.e(BIRINFO).e(INTEGRITY).t(FALSE).up().up();

		builder.getDocument().setXmlStandalone(true);

		String[] fingerPrint = biometricDataModel.getFingerPrint();

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
				String strFingerXml = CbeffBirBuilder.buildBirFinger(fingerPrint[i], strFinger, null, null, qualityScore,
						genarateValidCbeff, FALSE, contextKey);

				XMLBuilder fbuilder = XMLBuilder.parse(strFingerXml);
				builder = builder.importXMLBuilder(fbuilder);
			}

		}

		if (bioFilter.contains("Face")) {
			if (biometricDataModel.getEncodedPhoto() != null) {
				String faceXml = CbeffBirBuilder.buildBirFace(biometricDataModel.getEncodedPhoto(), null, null, qualityScore,
						genarateValidCbeff, "true", contextKey);
				builder = builder.importXMLBuilder(XMLBuilder.parse(faceXml));
			}
		}

		IrisDataModel irisInfo = biometricDataModel.getIris();
		if (irisInfo != null) {
			String irisXml = "";
			if (bioFilter.contains(LEFTEYE)) {
				irisXml = CbeffBirBuilder.buildBirIris(irisInfo.getLeft(), "Left", null, null, qualityScore,
						genarateValidCbeff, "true",
						contextKey);
				builder = builder.importXMLBuilder(XMLBuilder.parse(irisXml));
			}
			if (bioFilter.contains(RIGHTEYE)) {
				irisXml = CbeffBirBuilder.buildBirIris(irisInfo.getRight(), RIGHT, null, null, qualityScore,
						genarateValidCbeff, "true",
						contextKey);
				builder = builder.importXMLBuilder(XMLBuilder.parse(irisXml));
			}
		}

		if (toFile != null) {
			Path safePath = CommonUtil.validateOutputPath(toFile, contextKey);
			try (FileOutputStream fos = new FileOutputStream(safePath.toFile());
					PrintWriter writer = new PrintWriter(fos)) {
				builder.toWriter(true, writer, null);
			}
		}
		retXml = builder.asString(null);
		return retXml;
	}

	private static XMLBuilder xmlbuilderIris(List<String> bioFilter, List<MDSDeviceCaptureModel> lstIrisData,
			List<String> bioSubType, XMLBuilder builder, boolean genarateValidCbeff, List<BioModality> exceptionlst,
			String contextKey) {
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
				String leftQuality = null;
				String rightQuality = null;
				boolean leftPresent = false;
				boolean rightPresent = false;

				for (MDSDeviceCaptureModel cm : lstIrisData) {

					if (listWithoutExceptions.contains(LEFTEYE) && cm.getBioSubType().equals("Left")) {
						irisXml = CbeffBirBuilder.buildBirIris(
								cm.getBioValue(),
								"Left",
								cm.getSb(),
								cm.getPayload(),
								cm.getQualityScore(),
								genarateValidCbeff,
								FALSE,
								contextKey);

						builder = builder.importXMLBuilder(XMLBuilder.parse(irisXml));
						bioSubType.add("Left");

						leftQuality = cm.getQualityScore();
						leftPresent = true;
					} else if (!listWithoutExceptions.contains(LEFTEYE)) {
						VariableManager.appendVariableValue(contextKey, "EXCEPTION_BIOMETRICS", "LE");

					}

					if (listWithoutExceptions.contains(RIGHTEYE) && cm.getBioSubType().equals(RIGHT)) {
						irisXml = CbeffBirBuilder.buildBirIris(
								cm.getBioValue(),
								RIGHT,
								cm.getSb(),
								cm.getPayload(),
								cm.getQualityScore(),
								genarateValidCbeff,
								FALSE,
								contextKey);

						builder = builder.importXMLBuilder(XMLBuilder.parse(irisXml));
						bioSubType.add(RIGHT);

						rightQuality = cm.getQualityScore();
						rightPresent = true;
					} else if (!listWithoutExceptions.contains(RIGHTEYE)) {
						VariableManager.appendVariableValue(contextKey, "EXCEPTION_BIOMETRICS", "RE");

					}
				}

				String finalValue;

				if (!leftPresent && !rightPresent) {
					finalValue = "--Biometrics-Not-Available--";
				} else if ((leftQuality != null && Double.parseDouble(leftQuality) >= 80)
						|| (rightQuality != null && Double.parseDouble(rightQuality) >= 80)) {
					finalValue = "level-9";
				} else {
					finalValue = "level-2";
				}

				VariableManager.setVariableValue(
						contextKey,
						"Biometric_Quality-Iris",
						finalValue);
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

					String strFingerXml = CbeffBirBuilder.buildBirIris(
							finger.getType(),
							finger.getSubType(),
							Arrays.toString(new byte[0]),
							"",
							"0",
							genarateValidCbeff,
							"true",
							contextKey);

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

		for (int i = 0; i < 13; i++) {
			String displayFingerName = DataProviderConstants.displayFullName[i];
			if (displayFingerName.equalsIgnoreCase(name) == true)
				return DataProviderConstants.schemaNames[i];
		}

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
				VariableManager.appendVariableValue(contextKey, "EXCEPTION_BIOMETRICS", getFingerCode(ex));
			}
			listWithoutExceptions = bioFilter.stream().filter(bioAttribute -> !schemaName.contains(bioAttribute))
					.collect(Collectors.toList());
		}

		int i = 0;
		String fingerData = null;
		boolean fingerPresent = false;
		String maxQuality = null;

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

						String strFingerXml = CbeffBirBuilder.buildBirFinger(
								fingerData,
								strFinger,
								currentCM.getSb(),
								currentCM.getPayload(),
								currentCM.getQualityScore(),
								genarateValidCbeff,
								FALSE,
								contextKey);

						XMLBuilder fbuilder = XMLBuilder.parse(strFingerXml);
						builder = builder.importXMLBuilder(fbuilder);

						fingerPresent = true;

						String q = currentCM.getQualityScore();

						if (maxQuality == null || Double.parseDouble(q) > Double.parseDouble(maxQuality))
							maxQuality = q;

					}
				}

				String finalValue;

				if (!fingerPresent) {
					finalValue = "--Biometrics-Not-Available--";
				} else if (maxQuality != null && Double.parseDouble(maxQuality) >= 80) {
					finalValue = "level-9";
				} else {
					finalValue = "level-2";
				}

				VariableManager.setVariableValue(contextKey, "Biometric_Quality-Finger", finalValue);
			}

		} catch (Exception e1) {
			logger.error(e1.getMessage());
		}

		return builder;
	}

	private static String getFingerCode(String finger) {
		if (finger == null)
			return "";
		String f = finger.toLowerCase().replace("finger:", "");
		String side = f.contains("right") ? "R" : f.contains("left") ? "L" : "";
		String type = "";
		if (f.contains("thumb"))
			type = "T";
		else if (f.contains("index"))
			type = "I";
		else if (f.contains("middle"))
			type = "M";
		else if (f.contains("ring"))
			type = "R";
		else if (f.contains("little"))
			type = "L";
		if (type.isEmpty())
			return "";
		return side + type;
	}

	private static XMLBuilder xmlbuilderFingerExep(List<String> bioFilter, List<BioModality> lstFingerData,
			List<String> bioSubType, XMLBuilder builder, boolean genarateValidCbeff, String contextKey)
			throws ParserConfigurationException, FactoryConfigurationError, TransformerException, SAXException,
			IOException {

		if (lstFingerData != null) {
			for (BioModality finger : lstFingerData) {
				if (finger.getType().equalsIgnoreCase("iris") || finger.getType().equalsIgnoreCase("face"))
					continue;

				String strFingerXml = CbeffBirBuilder.buildBirFinger(finger.getType(), finger.getSubType(),
						Arrays.toString(new byte[0]), "", "0", genarateValidCbeff, "true", contextKey);
				XMLBuilder fbuilder = XMLBuilder.parse(strFingerXml);
				builder = builder.importXMLBuilder(fbuilder);
			}

		}

		return builder;
	}
}
