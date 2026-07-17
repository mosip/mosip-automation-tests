package io.mosip.testrig.dslrig.dataprovider.mds;

import static io.restassured.RestAssured.given;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.cucumber.messages.internal.com.google.common.io.Files;
import io.mosip.testrig.dslrig.dataprovider.models.IrisDataModel;
import io.mosip.testrig.dslrig.dataprovider.models.JWTTokenModel;
import io.mosip.testrig.dslrig.dataprovider.models.ResidentModel;
import io.mosip.testrig.dslrig.dataprovider.models.mds.MDSDevice;
import io.mosip.testrig.dslrig.dataprovider.models.mds.MDSDeviceCaptureModel;
import io.mosip.testrig.dslrig.dataprovider.models.mds.MDSRCaptureModel;
import io.mosip.testrig.dslrig.dataprovider.util.CommonUtil;
import io.mosip.testrig.dslrig.dataprovider.util.DataProviderConstants;
import io.mosip.testrig.dslrig.dataprovider.util.RestClient;
import io.mosip.testrig.dslrig.dataprovider.variables.VariableManager;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class MDSClient implements MDSClientInterface {

	private static final Logger logger = LoggerFactory.getLogger(MDSClient.class);
	public int port;
	public static String MDSURL = "http://127.0.0.1:";

	public MDSClient(int port) {
		if (port == 0)
			this.port = 4501;
		else
			this.port = port;
	}


	public void createProfileold(String profilePath, String profile, ResidentModel resident, String contextKey,
			String purpose) throws Exception {


		File profDir1 = new File(profilePath + "/" + profile);
		File profDir = new File(profilePath + "/" + profile + "/" + purpose);
		if (!profDir1.exists())
			profDir1.mkdir();
		if (!profDir.exists())
			profDir.mkdir();

		File defProfile = new File(profilePath + "/" + "Default" + "/" + purpose);

		File[] defFiles = defProfile.listFiles();
		for (File f : defFiles) {
			try {
				Files.copy(f, new File(profDir.getAbsolutePath() + File.separator + f.getName()));
			} catch (IOException e) {
				logger.error(e.getMessage());
			}
		}
		ISOConverter convert = new ISOConverter();
		try {
			if (!resident.getSkipFace()) {
				byte[] face = resident.getBiometric().getRawFaceData();
				convert.convertFace(face, profDir + "/" + "Face.iso");
			}
			if (!resident.getSkipIris()) {

				IrisDataModel iris = resident.getBiometric().getIris();
				if (iris != null) {

					if (iris.getRawLeft() != null)
						convert.convertIris(iris.getRawLeft(), profDir + "/" + "Left_Iris.iso", "Left");
					if (iris.getRawRight() != null)
						convert.convertIris(iris.getRawRight(), profDir + "/" + "Right_Iris.iso", "Right");
				}
			}
			if (!resident.getSkipFinger()) {
				byte[][] fingerData = resident.getBiometric().getFingerRaw();
				for (int i = 0; i < 10; i++) {
					String fingerName = DataProviderConstants.displayFingerName[i];
					String outFileName = DataProviderConstants.MDSProfileFingerNames[i];
					if (fingerData[i] != null) {
						convert.convertFinger(fingerData[i], profDir + "/" + outFileName + ".iso", fingerName, purpose);
					}
				}
			}

		} catch (IOException e) {
			logger.error(e.getMessage());
		}

	}


	public void createProfile(String profilePath, String profile, ResidentModel resident, String contextKey,
			String purpose) throws Exception {

		File profDir1 = new File(profilePath + "/" + profile);
		File profDir = new File(profilePath + "/" + profile + "/" + purpose);
		if (!profDir1.exists())
			profDir1.mkdir();
		if (!profDir.exists())
			profDir.mkdir();


		ISOConverter convert = new ISOConverter();
		try {
			if (!resident.getSkipFace()) {
				byte[] face = resident.getBiometric().getRawFaceData();
				convert.convertFace(face, profDir + "/" + "Face.iso");
			}
			if (!resident.getSkipIris()) {

				IrisDataModel iris = resident.getBiometric().getIris();
				if (iris != null) {
					logger.info("IRIS_DATA : " + iris);
					logger.info("IRIS_DATA_PATH : " + profDir + "/" + "Left_Iris.iso");
					if (logger.isDebugEnabled() && iris.getRawLeft() != null) {
						logger.debug("IRIS_RAW_DATA_left size={} bytes", iris.getRawLeft().length);
					}
					if (logger.isDebugEnabled() && iris.getRawRight() != null) {
						logger.debug("IRIS_RAW_DATA_right size={} bytes", iris.getRawRight().length);
					}
					if (iris.getRawLeft() != null)
						convert.convertIris(iris.getRawLeft(), profDir + "/" + "Left_Iris.iso", "Left");
					if (iris.getRawRight() != null)
						convert.convertIris(iris.getRawRight(), profDir + "/" + "Right_Iris.iso", "Right");
				}
			}
			if (!resident.getSkipFinger()) {
				byte[][] fingerData = resident.getBiometric().getFingerRaw();
				for (int i = 0; i < 10; i++) {
					String fingerName = DataProviderConstants.displayFingerName[i];
					String outFileName = DataProviderConstants.MDSProfileFingerNames[i];
					if (fingerData[i] != null) {
						convert.convertFinger(fingerData[i], profDir + "/" + outFileName + ".iso", fingerName, purpose);
					}
				}
			}

		} catch (IOException e) {
			logger.error(e.getMessage());
		}

	}

	public void updateProfile(String profilePath, String profile, ResidentModel resident, String contextKey,
			String purpose) throws Exception {
		File profDir1 = new File(profilePath + "/" + profile);
		File profDir = new File(profilePath + "/" + profile + "/" + purpose);
		if (!profDir1.exists())
			profDir1.mkdir();
		if (!profDir.exists())
			profDir.mkdir();
		String biometricTypes = VariableManager.getVariableValue(contextKey, "regenAttribute") != null
				? VariableManager.getVariableValue(contextKey, "regenAttribute").toString().toLowerCase()
				: null;

		if (biometricTypes == null || biometricTypes.trim().isEmpty()) {
			logger.info("No biometric attributes provided for update. Skipping profile update.");
			return;
		}


		Set<String> biometricsToUpdate = Arrays.stream(biometricTypes.split(",")).map(String::trim)
				.filter(s -> !s.isEmpty()).collect(Collectors.toSet());


		Set<String> normalizedFingerSet = biometricsToUpdate.stream().map(s -> s.toLowerCase().replace(" ", ""))
				.collect(Collectors.toSet());

		ISOConverter convert = new ISOConverter();

		try {


			if (biometricsToUpdate.contains("face")) {

				byte[] face = resident.getBiometric().getRawFaceData();
				if (face != null) {
					convert.convertFace(face, profDir + "/Face.iso");
					logger.info("Face updated for profile {}", profile);
				}
			}


			boolean updateAllIris = biometricsToUpdate.contains("iris");
			boolean updateLeftIris = updateAllIris || biometricsToUpdate.contains("leftiris");
			boolean updateRightIris = updateAllIris || biometricsToUpdate.contains("rightiris");

			if ((updateLeftIris || updateRightIris)) {

				IrisDataModel iris = resident.getBiometric().getIris();
				if (iris != null) {
					if (logger.isDebugEnabled() && iris.getRawLeft() != null) {
						logger.debug("IRIS_RAW_DATA_left size={} bytes", iris.getRawLeft().length);
					}
					if (logger.isDebugEnabled() && iris.getRawRight() != null) {
						logger.debug("IRIS_RAW_DATA_right size={} bytes", iris.getRawRight().length);
					}
					if (updateLeftIris && iris.getRawLeft() != null) {
						convert.convertIris(iris.getRawLeft(), profDir + "/Left_Iris.iso", "Left");
					}

					if (updateRightIris && iris.getRawRight() != null) {
						convert.convertIris(iris.getRawRight(), profDir + "/Right_Iris.iso", "Right");
					}

					logger.info("Iris updated for profile {}", profile);
				}
			}


			boolean updateAllFingers = normalizedFingerSet.contains("finger");

			byte[][] fingerData = resident.getBiometric().getFingerRaw();

			for (int i = 0; i < 10; i++) {

				String fingerKey = DataProviderConstants.schemaFingerNames[i].toLowerCase().replace(" ", "");

				if (updateAllFingers || normalizedFingerSet.contains(fingerKey)) {

					if (fingerData != null && fingerData[i] != null) {

						String fingerName = DataProviderConstants.displayFingerName[i];
						String outFileName = DataProviderConstants.MDSProfileFingerNames[i];

						convert.convertFinger(fingerData[i], profDir + "/" + outFileName + ".iso", fingerName, purpose);

						logger.info("Updated finger {} for profile {}", fingerName, profile);
					}
				}
			}

		} catch (IOException e) {
			logger.error("Error while updating profile {}", profile, e);
			throw e;
		}
	}


	public void removeProfile(String profilePath, String profile, int port, String contextKey) {
		setProfile("Default", port, contextKey);
		File profDir = new File(profilePath + "/" + profile);
		boolean isFileDeleted = false;
		boolean isProfDirDeleted = false;
		if (profDir.exists()) {

			File[] files = profDir.listFiles();


			for (File file : files) {
				boolean isDeleted = file.delete();
				if (!isDeleted) {
					if (RestClient.isDebugEnabled(contextKey)) {
						 logger.info("File {} deleted successfully", file.getName());
					}
				}
				isFileDeleted = file.delete();
				if (!isFileDeleted) {
					if (RestClient.isDebugEnabled(contextKey)) {
						 logger.info("File {} deleted successfully", file.getName());
					}
				}
			}
			isProfDirDeleted = profDir.delete();
			if (!isProfDirDeleted) {
				if (RestClient.isDebugEnabled(contextKey)) {
					logger.info("Profile directory {} deleted successfully", profDir.getName());
				}else {
					    logger.warn("Failed to delete profile directory {}", profDir.getName());
				 }
			}
		}

	}

	public void setProfile(String profile, int port, String contextKey) {

		String url = MDSURL + port + "/admin/profile";
		JSONObject body = new JSONObject();
		body.put("profileId", profile);
		body.put("type", "Biometric Device");

		try {
			logger.info("Inside Setprofile");
			HttpRCapture capture = new HttpRCapture(url);
			capture.setMethod("POST");
			String response = RestClient.rawHttp(capture, body.toString(), contextKey);
			JSONObject respObject = new JSONObject(response);

		} catch (Exception ex) {
			logger.error(ex.getMessage());
		}

	}


	public List<MDSDevice> getRegDeviceInfo(String type) {

		List<MDSDevice> devices = null;

		String url = MDSURL + port;
		JSONObject body = new JSONObject();
		body.put("type", type);
		Response response = given().contentType(ContentType.JSON).body(body.toString()).post(url);
		if (response.getStatusCode() == 200) {
			String resp = response.getBody().asString();

			if (resp != null) {
				JSONArray deviceArray = new JSONArray(resp);
				ObjectMapper objectMapper = new ObjectMapper();

				try {
					devices = objectMapper.readValue(deviceArray.toString(),
							objectMapper.getTypeFactory().constructCollectionType(List.class, MDSDevice.class));

				} catch (IOException e) {
					logger.error(e.getMessage());
				}
			}
		}
		return devices;
	}


	public MDSRCaptureModel captureFromRegDevice(MDSDevice device, MDSRCaptureModel rCaptureModel, String type,
			String bioSubType, int reqScore, String deviceSubId, int port, String contextKey,
			List<String> listbioexception) {
		String mosipVersion = null;
		;
		try {
			mosipVersion = VariableManager.getVariableValue(VariableManager.NS_DEFAULT, "mosip.version").toString();
		} catch (Exception e) {

		}

		if (rCaptureModel == null)
			rCaptureModel = new MDSRCaptureModel();

		String url = MDSURL + port + "/capture";
		JSONObject jsonReq = new JSONObject();
		jsonReq.put("env", "Developer");
		jsonReq.put("purpose", "Registration");
		jsonReq.put("specVersion", "0.9.5");
		jsonReq.put("timeout", "120000");
		jsonReq.put("captureTime", CommonUtil.getUTCDateTime(null));
		jsonReq.put("domainUri", "automated");
		jsonReq.put("transactionId", "123456789123");
		JSONObject bio = new JSONObject();
		bio.put("type", type);

		bio.put("count", 1);
		bio.put("deviceSubId", deviceSubId);

		if (type.equalsIgnoreCase("finger")) {

			switch (deviceSubId) {
			case "1":
				bio.put("count", 4);

				break;
			case "2":
				bio.put("count", 4);

				break;
			case "3":
				bio.put("count", 2);
				break;
			}

		}

		bio.put("requestedScore", reqScore);

		bio.put("deviceId", device.getDeviceId());
		if (listbioexception != null && !listbioexception.isEmpty())
			bio.put("exception", listbioexception);

		JSONArray arr = new JSONArray();
		arr.put(bio);
		jsonReq.put("bio", arr);

		try {
			HttpRCapture capture = new HttpRCapture(url);
			capture.setMethod("RCAPTURE");
			String response = RestClient.rawHttp(capture, jsonReq.toString(), contextKey);

			JSONObject respObject = new JSONObject(response);
			JSONArray bioArray = respObject.getJSONArray("biometrics");
			logger.info("DATA bioArray " + type + " :" + bioArray.toString());

			List<MDSDeviceCaptureModel> lstBiometrics = rCaptureModel.getLstBiometrics().get(type);
			if (lstBiometrics == null)
				lstBiometrics = new ArrayList<MDSDeviceCaptureModel>();

			if (!CollectionUtils.isEmpty(listbioexception) && type.equalsIgnoreCase("face"))
				rCaptureModel.getLstBiometrics().put("exception", lstBiometrics);
			else
				rCaptureModel.getLstBiometrics().put(type, lstBiometrics);

			List<String> retriableErrorCodes = new ArrayList<String>();
			retriableErrorCodes.add("703");
			retriableErrorCodes.add("710");


			while (bioArray.length() == 1 && retriableErrorCodes
					.contains(bioArray.getJSONObject(0).getJSONObject("error").getString("errorCode"))) {
				logger.info("Check if Rcapture returns an error response if on error, retry based on Error ;code. ");
				response = RestClient.rawHttp(capture, jsonReq.toString(), contextKey);

				respObject = new JSONObject(response);
				bioArray = respObject.getJSONArray("biometrics");
			}

			for (int i = 0; i < bioArray.length(); i++) {
				JSONObject bioObject = bioArray.getJSONObject(i);
				String data = bioObject.getString("data");
				logger.info("DATA DATA : " + data);

				String hash = bioObject.getString("hash");
				JWTTokenModel jwtTok = new JWTTokenModel(data);
				logger.info("jwtTok DATA : " + jwtTok);
				JSONObject jsonPayload = new JSONObject(jwtTok.getJwtPayload());
				String jwtSign = jwtTok.getJwtSign();
				MDSDeviceCaptureModel model = new MDSDeviceCaptureModel();
				model.setBioType(CommonUtil.getJSONObjectAttribute(jsonPayload, "bioType", ""));
				model.setBioSubType(CommonUtil.getJSONObjectAttribute(jsonPayload, "bioSubType", ""));
				model.setQualityScore(CommonUtil.getJSONObjectAttribute(jsonPayload, "qualityScore", ""));
				model.setBioValue(CommonUtil.getJSONObjectAttribute(jsonPayload, "bioValue", ""));
				model.setDeviceServiceVersion(
						CommonUtil.getJSONObjectAttribute(jsonPayload, "deviceServiceVersion", ""));
				model.setDeviceCode(CommonUtil.getJSONObjectAttribute(jsonPayload, "deviceCode", ""));
				model.setHash(hash);
				if (mosipVersion != null && mosipVersion.startsWith("1.2")) {
					model.setSb(jwtSign); 


					String BIOVALUE_KEY = "bioValue";
					String BIOVALUE_PLACEHOLDER = "\"<bioValue>\"";
					int bioValueKeyIndex = jwtTok.getJwtPayload().indexOf(BIOVALUE_KEY) + (BIOVALUE_KEY.length() + 1);
					int bioValueStartIndex = jwtTok.getJwtPayload().indexOf('"', bioValueKeyIndex);
					int bioValueEndIndex = jwtTok.getJwtPayload().indexOf('"', (bioValueStartIndex + 1));
					String bioValue = jwtTok.getJwtPayload().substring(bioValueStartIndex, (bioValueEndIndex + 1));
					String payload = jwtTok.getJwtPayload().replace(bioValue, BIOVALUE_PLACEHOLDER);
					model.setPayload(payload);
				}
				lstBiometrics.add(model);
				logger.info("MODEL DATA : " + model);
				logger.info("MODEL DATA : " + model.getBioValue());
			}

		} catch (IOException e) {
			logger.error(e.getMessage());
		}

		return rCaptureModel;
	}

	public void setThresholdValue(String qualityScore) {

		String url = MDSURL + port + "/admin/score";
		JSONObject body = new JSONObject();
		body.put("type", "Biometric Device");
		body.put("qualityScore", qualityScore);
		body.put("fromIso", false);

		try {


			Response response = given().contentType(ContentType.JSON).body(body.toString()).post(url);
			String resp = response.getBody().asString();
			logger.info(resp);

		} catch (Exception ex) {
			logger.error(ex.getMessage());
		}

	}

	public static void main(String[] args) {

		MDSClient client = new MDSClient(0);


		List<MDSDevice> d = client.getRegDeviceInfo("Iris");
		d.forEach(dv -> {
			logger.info(dv.toJSONString());
		});

		List<MDSDevice> f = client.getRegDeviceInfo("Finger");

		f.forEach(dv -> {
			logger.info(dv.toJSONString());


		});


	}

	@Override
	public List<MDSDevice> getRegDeviceInfo(String type, String contextKey) {

		return null;
	}

}
