package io.mosip.testrig.dslrig.dataprovider;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.mosip.testrig.dslrig.dataprovider.mds.MDSClient;
import io.mosip.testrig.dslrig.dataprovider.models.BiometricDataModel;
import io.mosip.testrig.dslrig.dataprovider.models.IrisDataModel;
import io.mosip.testrig.dslrig.dataprovider.models.ResidentModel;
import io.mosip.testrig.dslrig.dataprovider.persona.PersonaBiometricsAssembler;
import io.mosip.testrig.dslrig.dataprovider.persona.PersonaDemographicsBuilder;
import io.mosip.testrig.dslrig.dataprovider.util.CommonUtil;
import io.mosip.testrig.dslrig.dataprovider.util.Gender;
import io.mosip.testrig.dslrig.dataprovider.util.ResidentAttribute;


public class ResidentDataProvider {
	private static final Logger logger = LoggerFactory.getLogger(ResidentDataProvider.class);
	private static final Set<String> SINGLE_FINGER_TYPES = Set.of(
	        "rightindex",
	        "rightlittle",
	        "rightring",
	        "rightmiddle",
	        "leftindex",
	        "leftlittle",
	        "leftring",
	        "leftmiddle",
	        "leftthumb",
	        "rightthumb"
	);

	Properties attributeList;


	public ResidentDataProvider() {
		attributeList = new Properties();
	}

	public ResidentDataProvider addCondition(ResidentAttribute attributeName, Object attributeValue) {
		attributeList.put(attributeName, attributeValue);
		return this;
	}

	public static ResidentModel genGuardian(Properties attributes, String contextKey) throws Exception {
		Properties attributeList = new Properties();
		attributes.forEach((k, v) -> attributeList.put(k, v));

		attributeList.put(ResidentAttribute.RA_Age, ResidentAttribute.RA_Adult);
		attributeList.put(ResidentAttribute.RA_Gender, Gender.Any);

		ResidentDataProvider provider = new ResidentDataProvider();
		provider.attributeList = attributeList;
		return provider.generate(contextKey).get(0);
	}

	public static ResidentModel updateBiometric(ResidentModel model, String bioType, String contextKey)
			throws Exception {
		PersonaBiometricsAssembler.ensureAssembled(model, PersonaBiometricsAssembler.hintsFromResident(model),
				contextKey);
		boolean bDirty = false;
		model.setFilteredBioAttribtures(null);
		if (bioType.equalsIgnoreCase("finger")) {
			BiometricDataModel bioData = BiometricDataProvider.updateFingerData(contextKey);
			model.getBiometric().setFingerPrint(bioData.getFingerPrint());
			model.getBiometric().setFingerHash(bioData.getFingerHash());
			model.getBiometric().setFingerRaw(bioData.getFingerRaw());
			model.setSkipFinger(false);
			bDirty = true;
		} else if (SINGLE_FINGER_TYPES.contains(bioType.toLowerCase())) {
			BiometricDataModel bioData = BiometricDataProvider.updateSelectedFingerData(model, contextKey, bioType);
			model.getBiometric().setFingerPrint(bioData.getFingerPrint());
			model.getBiometric().setFingerHash(bioData.getFingerHash());
			model.getBiometric().setFingerRaw(bioData.getFingerRaw());
			model.setSkipFinger(false);
			bDirty = true;
		}

		if (bioType.equalsIgnoreCase("iris")) {
			List<IrisDataModel> iris = BiometricDataProvider.updateIris(contextKey);
			if (iris != null && !iris.isEmpty()) {
				model.getBiometric().setIris(iris.get(0));
				bDirty = true;
				model.setSkipIris(false);
			}
		} else if (bioType.equalsIgnoreCase("leftiris") || bioType.equalsIgnoreCase("rightiris")) {
			List<IrisDataModel> iris = BiometricDataProvider.updateSelectedIris(model, contextKey, bioType);
			if (iris != null && !iris.isEmpty()) {
				model.getBiometric().setIris(iris.get(0));
				bDirty = true;
				model.setSkipIris(false);
			}
		}

		if (bioType.equalsIgnoreCase("face")) {
			BiometricDataModel bioData = model.getBiometric();
			byte[][] faceData = BiometricDataProvider.updateFaceData(contextKey);
			bioData.setEncodedPhoto(Base64.getEncoder().encodeToString(faceData[0]));
			bioData.setRawFaceData(faceData[1]);
			bioData.setFaceHash(CommonUtil.getHexEncodedHash(faceData[1]));
			bDirty = true;
		}

		if (bDirty) {
			model.getBiometric().setCbeff(null);
		}

		return model;
	}

	public static ResidentModel updateBiometricWithTestPersona(ResidentModel model, ResidentModel testModel,
			String bioType, String contextKey) throws Exception {
		PersonaBiometricsAssembler.ensureBiometricShell(model);
		if (testModel == null || testModel.getBiometric() == null) {
			logger.warn("Test persona biometric is missing for bioType '{}'; falling back to generated biometric update.",
					bioType);
			return updateBiometric(model, bioType, contextKey);
		}
		model.setFilteredBioAttribtures(null);
		boolean bDirty = false;
		BiometricDataModel testBiometric = testModel.getBiometric();
		if (bioType.equalsIgnoreCase("finger")) {
			model.getBiometric().setFingerHash(testBiometric.getFingerHash());
			model.getBiometric().setFingerPrint(testBiometric.getFingerPrint());
			model.getBiometric().setFingerRaw(testBiometric.getFingerRaw());
			model.setSkipFinger(false);
			bDirty = true;
		} else if (bioType.equalsIgnoreCase("iris")) {
			if (testBiometric.getIris() != null) {
				model.getBiometric().setIris(testBiometric.getIris());
				model.setSkipIris(false);
				bDirty = true;
			}
		} else if (bioType.equalsIgnoreCase("face")) {
			if (testBiometric.getEncodedPhoto() != null || testBiometric.getRawFaceData() != null) {
				model.getBiometric().setEncodedPhoto(testBiometric.getEncodedPhoto());
				model.getBiometric().setFaceHash(testBiometric.getFaceHash());
				model.getBiometric().setRawFaceData(testBiometric.getRawFaceData());
				bDirty = true;
			}
		}
		if (!bDirty) {
			logger.warn("Test persona has no '{}' biometric payload; falling back to generated biometric update.", bioType);
			return updateBiometric(model, bioType, contextKey);
		}
		if (bDirty) {
			model.setFilteredBioAttribtures(null);
			model.getBiometric().setCbeff(null);
		}
		return model;
	}

	public List<ResidentModel> generate(String contextKey) throws Exception {
		List<ResidentModel> residents = new ArrayList<>();
		boolean lazyBiometrics = PersonaBiometricsAssembler.isLazyEnabled(contextKey);
		PersonaDemographicsBuilder.Context demoCtx = PersonaDemographicsBuilder.prepare(attributeList, contextKey);

		for (int i = 0; i < demoCtx.count; i++) {
			ResidentModel res = PersonaDemographicsBuilder.populate(i, demoCtx, attributeList, contextKey);
			if (lazyBiometrics) {
				PersonaBiometricsAssembler.markDeferred(res, attributeList);
			} else {
				PersonaBiometricsAssembler.assemble(res, attributeList, contextKey);
			}
			residents.add(res);
		}
		return residents;
	}

	public static void main(String[] args) throws Exception {
		ResidentDataProvider residentProvider = new ResidentDataProvider();
		residentProvider
				.addCondition(ResidentAttribute.RA_SECONDARY_LANG, "ara")
				.addCondition(ResidentAttribute.RA_Gender, Gender.Any)
				.addCondition(ResidentAttribute.RA_Age, ResidentAttribute.RA_Adult);

		List<ResidentModel> lst = residentProvider.generate("contextKey");
		for (ResidentModel r : lst) {
			PersonaBiometricsAssembler.ensureAssembled(r, null, "contextKey");
		}
		MDSClient cli = new MDSClient(0);

		for (ResidentModel r : lst) {
			logger.info(r.toJSONString());
			cli.createProfile("C:\\Mosip.io\\gitrepos\\mosip-mock-services\\MockMDS\\target\\Profile\\", "tst1", r,
					"contextKey", "Registration");
		}
	}
}
