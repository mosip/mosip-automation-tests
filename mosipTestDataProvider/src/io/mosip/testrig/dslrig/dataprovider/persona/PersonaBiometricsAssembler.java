package io.mosip.testrig.dslrig.dataprovider.persona;

import java.io.IOException;
import java.text.ParseException;
import java.util.Base64;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lowagie.text.DocumentException;

import io.mosip.testrig.dslrig.dataprovider.BiometricDataProvider;
import io.mosip.testrig.dslrig.dataprovider.DocumentProvider;
import io.mosip.testrig.dslrig.dataprovider.PhotoProvider;
import io.mosip.testrig.dslrig.dataprovider.models.BiometricDataModel;
import io.mosip.testrig.dslrig.dataprovider.models.IrisDataModel;
import io.mosip.testrig.dslrig.dataprovider.models.MosipDocument;
import io.mosip.testrig.dslrig.dataprovider.models.ResidentModel;
import io.mosip.testrig.dslrig.dataprovider.util.CommonUtil;
import io.mosip.testrig.dslrig.dataprovider.util.ResidentAttribute;
import io.mosip.testrig.dslrig.dataprovider.variables.VariableManager;

/**
 * Finger/iris/face capture, documents, and doc indexes — deferred off the demographics hot path
 * until packet template generation (or explicit assembly).
 */
public final class PersonaBiometricsAssembler {

	private static final Logger logger = LoggerFactory.getLogger(PersonaBiometricsAssembler.class);

	public static final String ATTR_DEFERRED = "biometricsDeferred";
	public static final String ATTR_ASSEMBLED = "biometricsAssembled";
	private static final String HINT_PREFIX = "assembly.";

	private PersonaBiometricsAssembler() {
	}

	public static boolean isLazyEnabled(String contextKey) {
		try {
			Object perContext = VariableManager.getVariableValue(contextKey, "lazyPersonaBiometrics");
			if (perContext != null) {
				return Boolean.parseBoolean(perContext.toString());
			}
		} catch (Exception ignored) {
			// fall through
		}
		try {
			Object global = VariableManager.getVariableValue(VariableManager.NS_DEFAULT, "lazyPersonaBiometrics");
			if (global != null) {
				return Boolean.parseBoolean(global.toString());
			}
		} catch (Exception ignored) {
			// default on for throughput
		}
		return true;
	}

	public static void markDeferred(ResidentModel resident, Properties attributeList) {
		storeAssemblyHints(resident, attributeList);
		Hashtable<String, String> attrs = resident.getAddtionalAttributes();
		attrs.put(ATTR_DEFERRED, "true");
		attrs.remove(ATTR_ASSEMBLED);
	}

	public static boolean needsAssembly(ResidentModel resident) {
		if (resident == null) {
			return false;
		}
		Hashtable<String, String> attrs = resident.getAddtionalAttributes();
		if (attrs != null && "true".equals(attrs.get(ATTR_ASSEMBLED))) {
			return false;
		}
		if (resident.getBiometric() != null && resident.getBiometric().getFingerPrint() != null) {
			return false;
		}
		if (attrs != null && "true".equals(attrs.get(ATTR_DEFERRED))) {
			return true;
		}
		return resident.getBiometric() == null;
	}

	public static void ensureAssembled(ResidentModel resident, Properties attributeList, String contextKey)
			throws Exception {
		if (resident == null || !needsAssembly(resident)) {
			assembleGuardianIfNeeded(resident, attributeList, contextKey);
			return;
		}
		Properties attrs = attributeList != null ? attributeList : hintsFromResident(resident);
		assemble(resident, attrs, contextKey);
		assembleGuardianIfNeeded(resident, attrs, contextKey);
	}

	private static void assembleGuardianIfNeeded(ResidentModel resident, Properties attributeList, String contextKey)
			throws Exception {
		if (resident == null || resident.getGuardian() == null) {
			return;
		}
		ensureAssembled(resident.getGuardian(), attributeList, contextKey);
	}

	public static void assemble(ResidentModel resident, Properties attributeList, String contextKey) throws Exception {
		if (resident == null || attributeList == null) {
			return;
		}

		Object oAttr = attributeList.get(ResidentAttribute.RA_Iris);
		boolean bIrisRequired = oAttr == null || Boolean.parseBoolean(oAttr.toString());

		Object bFinger = attributeList.get(ResidentAttribute.RA_Finger);
		Boolean skipFinger = bFinger == null ? false : !(Boolean) bFinger;
		resident.setSkipFinger(skipFinger);

		Object bPhoto = attributeList.get(ResidentAttribute.RA_Photo);
		Boolean skipFace = bPhoto == null ? false : !(Boolean) bPhoto;
		resident.setSkipFace(skipFace);

		Object bIris = attributeList.get(ResidentAttribute.RA_Iris);
		Boolean skipIris = bIris == null ? false : !(Boolean) bIris;
		resident.setSkipIris(skipIris);

		boolean fingerCapture = bFinger == null || Boolean.TRUE.equals(bFinger);
		BiometricDataModel bioData;
		try {
			bioData = BiometricDataProvider.getBiometricData(fingerCapture, contextKey);
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw e;
		}

		if (bIrisRequired && !Boolean.TRUE.equals(skipIris)) {
			try {
				List<IrisDataModel> irisList = BiometricDataProvider.generateIris(1, contextKey);
				if (irisList != null && !irisList.isEmpty()) {
					bioData.setIris(irisList.get(0));
				}
			} catch (Exception e) {
				logger.error(e.getMessage());
			}
		}

		boolean bFace = bPhoto == null || Boolean.TRUE.equals(bPhoto);
		if (bFace) {
			Object largeFaceFlag = attributeList.get(ResidentAttribute.RA_LargeFace);
			boolean generateLargeFace = largeFaceFlag != null
					&& Boolean.parseBoolean(largeFaceFlag.toString());
			Object obstructedFaceFlag = attributeList.get(ResidentAttribute.RA_ObstructedFace);
			boolean generateObstructedFace = obstructedFaceFlag != null
					&& Boolean.parseBoolean(obstructedFaceFlag.toString());

			byte[][] faceData = PhotoProvider.getPhoto(contextKey, generateLargeFace, generateObstructedFace);
			bioData.setEncodedPhoto(Base64.getEncoder().encodeToString(faceData[0]));
			bioData.setRawFaceData(faceData[1]);
			try {
				bioData.setFaceHash(CommonUtil.getHexEncodedHash(faceData[1]));
			} catch (Exception e1) {
				logger.debug("Face hash skipped: {}", e1.getMessage());
			}
		}

		resident.setBiometric(bioData);

		oAttr = attributeList.get(ResidentAttribute.RA_Document);
		boolean bDocRequired = oAttr == null || Boolean.parseBoolean(oAttr.toString());
		if (bDocRequired) {
			try {
				Object lowQualityDocumentAttr = attributeList.get(ResidentAttribute.RA_LowQualityDocument);
				boolean generateLowQualityDocuments = lowQualityDocumentAttr != null
						&& Boolean.parseBoolean(lowQualityDocumentAttr.toString());
				resident.setDocuments(DocumentProvider.generateDocuments(resident, contextKey,
						generateLowQualityDocuments));
				Object docCategoryAttr = attributeList.get(ResidentAttribute.RA_DocumentCategory);
				if (docCategoryAttr != null && !docCategoryAttr.toString().trim().isEmpty()) {
					String requiredDocCategory = docCategoryAttr.toString().trim();
					resident.setDocuments(resident.getDocuments().stream()
							.filter(doc -> doc != null && doc.getDocCategoryCode() != null
									&& doc.getDocCategoryCode().equalsIgnoreCase(requiredDocCategory))
							.collect(Collectors.toList()));
				}
			} catch (DocumentException | IOException | ParseException e) {
				logger.error(e.getMessage());
			}
		}

		if (resident.getDocuments() != null) {
			for (MosipDocument doc : resident.getDocuments()) {
				if (doc == null || doc.getDocs() == null || doc.getDocs().isEmpty()) {
					continue;
				}
				String id = doc.getDocCategoryCode();
				int index = CommonUtil.generateRandomNumbers(1, doc.getDocs().size() - 1, 0)[0];
				resident.getDocIndexes().put(id, index);
			}
		}

		resident.getAddtionalAttributes().put(ATTR_ASSEMBLED, "true");
		resident.getAddtionalAttributes().remove(ATTR_DEFERRED);
	}

	public static void storeAssemblyHints(ResidentModel resident, Properties attributeList) {
		if (resident == null || attributeList == null) {
			return;
		}
		Hashtable<String, String> target = resident.getAddtionalAttributes();
		attributeList.forEach((key, value) -> {
			if (key instanceof ResidentAttribute ra && value != null) {
				target.put(HINT_PREFIX + ra.name(), value.toString());
			}
		});
	}

	public static Properties hintsFromResident(ResidentModel resident) {
		Properties props = new Properties();
		if (resident == null || resident.getAddtionalAttributes() == null) {
			return props;
		}
		resident.getAddtionalAttributes().forEach((key, value) -> {
			if (key != null && key.startsWith(HINT_PREFIX) && value != null) {
				String enumName = key.substring(HINT_PREFIX.length());
				try {
					ResidentAttribute ra = ResidentAttribute.valueOf(enumName);
					props.put(ra, parseHintValue(ra, value));
				} catch (IllegalArgumentException ignored) {
					// skip unknown keys
				}
			}
		});
		return props;
	}

	private static Object parseHintValue(ResidentAttribute ra, String value) {
		return switch (ra) {
			case RA_Finger, RA_Photo, RA_Iris, RA_Document, RA_LargeFace, RA_ObstructedFace, RA_LowQualityDocument,
					RA_SKipGaurdian ->
				Boolean.parseBoolean(value);
			case RA_Age -> ResidentAttribute.valueOf(value);
			case RA_Gender -> io.mosip.testrig.dslrig.dataprovider.util.Gender.valueOf(value);
			case RA_SCHEMA_VERSION -> Double.parseDouble(value);
			default -> value;
		};
	}

	public static void ensureBiometricShell(ResidentModel resident) {
		if (resident != null && resident.getBiometric() == null) {
			resident.setBiometric(new BiometricDataModel());
		}
	}
}
