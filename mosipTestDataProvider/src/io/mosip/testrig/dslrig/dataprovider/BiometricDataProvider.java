package io.mosip.testrig.dslrig.dataprovider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.mosip.testrig.dslrig.dataprovider.biometric.CbeffAssembler;
import io.mosip.testrig.dslrig.dataprovider.biometric.CbeffBirBuilder;
import io.mosip.testrig.dslrig.dataprovider.biometric.FileBiometricLoader;
import io.mosip.testrig.dslrig.dataprovider.biometric.MdsCaptureService;
import io.mosip.testrig.dslrig.dataprovider.models.BioModality;
import io.mosip.testrig.dslrig.dataprovider.models.BiometricDataModel;
import io.mosip.testrig.dslrig.dataprovider.models.IrisDataModel;
import io.mosip.testrig.dslrig.dataprovider.models.ResidentModel;
import io.mosip.testrig.dslrig.dataprovider.models.mds.MDSRCaptureModel;

public class BiometricDataProvider {

	private static final Logger logger = LoggerFactory.getLogger(BiometricDataProvider.class);

	private static final String LEFTEYE = "leftEye";
	private static final String RIGHTEYE = "rightEye";
	private static final String PAYLOAD = "PAYLOAD";
	private static final String FALSE = "false";

	public static final String AUTHCERTSPATH = "authCertsPath";

	public static List<BioModality> getModalitiesByType(List<BioModality> bioExceptions, String type) {
		return MdsCaptureService.getModalitiesByType(bioExceptions, type);
	}

	public static MDSRCaptureModel regenBiometricViaMDS(ResidentModel resident, String contextKey, String purpose,
			String qualityScore, String process) throws Exception {
		return MdsCaptureService.regenBiometricViaMDS(resident, contextKey, purpose, qualityScore, process);
	}

	public static String toCBEFFFromCapture(List<String> bioFilter, MDSRCaptureModel capture, String toFile,
			List<String> missAttribs, boolean genarateValidCbeff, List<BioModality> exceptionlist, String contextKey)
			throws Exception {
		return CbeffAssembler.toCBEFFFromCapture(bioFilter, capture, toFile, missAttribs, genarateValidCbeff,
				exceptionlist, contextKey);
	}

	public static String toCBEFF(List<String> bioFilter, BiometricDataModel biometricDataModel, String toFile,
			boolean genarateValidCbeff, String contextKey) throws Exception {
		return CbeffAssembler.toCBEFF(bioFilter, biometricDataModel, toFile, genarateValidCbeff, contextKey);
	}

	public static Hashtable<Integer, List<File>> impressionCaptureList(String contextKey) {
		return FileBiometricLoader.impressionCaptureList(contextKey);
	}

	public static BiometricDataModel getBiometricData(Boolean bFinger, String contextKey) throws Exception {
		return FileBiometricLoader.getBiometricData(bFinger, contextKey);
	}

	public static BiometricDataModel updateFingerData(String contextKey) throws Exception {
		return FileBiometricLoader.updateFingerData(contextKey);
	}

	public static BiometricDataModel updateSelectedFingerData(ResidentModel model, String contextKey,
			String fingerArgument) throws Exception {
		return FileBiometricLoader.updateSelectedFingerData(model, contextKey, fingerArgument);
	}

	public static IrisDataModel loadIris(String filePath, String subModality, IrisDataModel im) throws Exception {
		return FileBiometricLoader.loadIris(filePath, subModality, im);
	}

	public static List<IrisDataModel> generateIris(int count, String contextKey) throws Exception {
		return FileBiometricLoader.generateIris(count, contextKey);
	}

	static List<IrisDataModel> updateIris(String contextKey) throws Exception {
		return FileBiometricLoader.updateIris(contextKey);
	}

	static List<IrisDataModel> updateSelectedIris(ResidentModel model, String contextKey, String irisArgument)
			throws Exception {
		return FileBiometricLoader.updateSelectedIris(model, contextKey, irisArgument);
	}

	static byte[][] updateFaceData(String contextKey) {
		return FileBiometricLoader.updateFaceData(contextKey);
	}

	public static File[] getRandomIrisVariation(File[] listOfFiles) {
		return FileBiometricLoader.getRandomIrisVariation(listOfFiles);
	}

	public static void main(String[] args) throws Exception {

		try {

			String value = CbeffBirBuilder.buildBirFinger("addfdfd", "finger", "jwtSign", PAYLOAD, null, true, FALSE,
					null);
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
}
