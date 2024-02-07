package io.mosip.testrig.dslrig.packetcreator.service;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.everit.json.schema.ValidationException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.mosip.testrig.dslrig.dataprovider.BiometricDataProvider;
import io.mosip.testrig.dslrig.dataprovider.PacketTemplateProvider;
import io.mosip.testrig.dslrig.dataprovider.PhotoProvider;
import io.mosip.testrig.dslrig.dataprovider.ResidentDataProvider;
import io.mosip.testrig.dslrig.dataprovider.models.AppointmentModel;
import io.mosip.testrig.dslrig.dataprovider.models.AppointmentTimeSlotModel;
import io.mosip.testrig.dslrig.dataprovider.models.BiometricDataModel;
import io.mosip.testrig.dslrig.dataprovider.models.CenterDetailsModel;
import io.mosip.testrig.dslrig.dataprovider.models.DynamicFieldValueModel;
import io.mosip.testrig.dslrig.dataprovider.models.IrisDataModel;
import io.mosip.testrig.dslrig.dataprovider.models.MosipDocTypeModel;
import io.mosip.testrig.dslrig.dataprovider.models.MosipDocument;
import io.mosip.testrig.dslrig.dataprovider.models.MosipIndividualTypeModel;
import io.mosip.testrig.dslrig.dataprovider.models.ResidentModel;
import io.mosip.testrig.dslrig.dataprovider.models.mds.MDSDeviceCaptureModel;
import io.mosip.testrig.dslrig.dataprovider.models.setup.MosipMachineModel;
import io.mosip.testrig.dslrig.dataprovider.preparation.MosipDataSetup;
import io.mosip.testrig.dslrig.dataprovider.preparation.MosipMasterData;
import io.mosip.testrig.dslrig.dataprovider.test.CreatePersona;
import io.mosip.testrig.dslrig.dataprovider.test.ResidentPreRegistration;
import io.mosip.testrig.dslrig.dataprovider.test.prereg.PreRegistrationSteps;
import io.mosip.testrig.dslrig.dataprovider.util.CommonUtil;
import io.mosip.testrig.dslrig.dataprovider.util.DataProviderConstants;
import io.mosip.testrig.dslrig.dataprovider.util.Gender;
import io.mosip.testrig.dslrig.dataprovider.util.ResidentAttribute;
import io.mosip.testrig.dslrig.dataprovider.util.RestClient;
import io.mosip.testrig.dslrig.dataprovider.variables.VariableManager;
import io.mosip.testrig.dslrig.packetcreator.dto.AppointmentDto;
import io.mosip.testrig.dslrig.packetcreator.dto.BioExceptionDto;
import io.mosip.testrig.dslrig.packetcreator.dto.MockABISExpectationsDto;
import io.mosip.testrig.dslrig.packetcreator.dto.PersonaRequestDto;
import io.mosip.testrig.dslrig.packetcreator.dto.PersonaRequestType;
import io.mosip.testrig.dslrig.packetcreator.dto.RidSyncReqResponseDTO;
import io.mosip.testrig.dslrig.packetcreator.dto.RidSyncRequestData;
import io.mosip.testrig.dslrig.packetcreator.dto.UpdatePersonaDto;

@Service
public class PacketSyncService {

	private static final String UNDERSCORE = "_";
	private static final Logger logger = LoggerFactory.getLogger(PacketSyncService.class);

	// String constants
	private static final String STATUS = "status";
	private static final String SUCCESS = "SUCCESS";
	private static final String RESPONSE = "response";
	private static final String REGISTRATIONID = "registrationId";
	private static final String MOSIP_TEST_REGCLIENT_CENTERID = "mosip.test.regclient.centerid";
	private static final String MOSIP_TEST_REGCLIENT_MACHINEID = "mosip.test.regclient.machineid";
	private static final String STATUS_SUCCESS = "{\"status\":\"Success\"}";
	private static final String MODALITY = "Modality : ";

	@Autowired
	private APIRequestUtil apiRequestUtil;

	@Autowired
	private CryptoUtil cryptoUtil;

	@Autowired
	private PreregSyncService preregSyncService;

	@Autowired
	private ZipUtils zipUtils;
	@Autowired
	private PacketMakerService packetMakerService;

	@Autowired
	private PacketSyncService packetSyncService;
	@Autowired
	private ContextUtils contextUtils;

	@Value("${mosip.test.primary.langcode}")
	private String primaryLangCode;

	@Value("${mosip.test.packet.template.process:NEW}")
	private String process;

	@Value("${mosip.test.packet.template.source:REGISTRATION_CLIENT}")
	private String src;

	@Value("${mosip.test.regclient.centerid}")
	private String centerId;

	@Value("${mosip.test.regclient.machineid}")
	private String machineId;

	@Value("${mosip.test.packet.syncapi}")
	private String syncapi;

	@Value("${mosip.test.packet.uploadapi}")
	private String uploadapi;

	@Value("${mosip.test.prereg.mapfile:Preregistration.properties}")
	private String preRegMapFile;

	@Value("${mosip.test.persona.configpath}")
	private String personaConfigPath;

	@Value("${mosip.test.baseurl}")
	private String baseUrl;

	@Value("${mosip.version:1.2}")
	private String mosipVersion;

	@Value("${packetmanager.zip.datetime.pattern:yyyyMMddHHmmss}")
	private String zipDatetimePattern;

	@Value("${mosip.test.idrepo.idvidpath}")
	private String idvid;

	void loadServerContextProperties(String contextKey) {

		if (contextKey != null && !contextKey.equals("")) {

			Properties props = contextUtils.loadServerContext(contextKey);
			props.forEach((k, v) -> {
				String key = k.toString().trim();
				String ns = VariableManager.NS_DEFAULT;

				if (!key.startsWith("mosip.test")) {

					VariableManager.setVariableValue(contextKey, key, v);
				}

			});
		}
	}

	public String generateResidentData(int count, PersonaRequestDto residentRequestDto, String contextKey) {

		loadServerContextProperties(contextKey);
		VariableManager.setVariableValue(contextKey, "process", "NEW");
		Properties props = residentRequestDto.getRequests().get(PersonaRequestType.PR_ResidentAttribute);
		Gender enumGender = Gender.Any;
		ResidentDataProvider provider = new ResidentDataProvider();
		if (props.containsKey("Gender")) {
			enumGender = Gender.valueOf(props.get("Gender").toString()); // Gender.valueOf(residentRequestDto.getGender());
		}
		provider.addCondition(ResidentAttribute.RA_Count, count);

		if (props.containsKey("Age")) {

			provider.addCondition(ResidentAttribute.RA_Age, ResidentAttribute.valueOf(props.get("Age").toString()));
		} else
			provider.addCondition(ResidentAttribute.RA_Age, ResidentAttribute.RA_Adult);

		if (props.containsKey("SkipGaurdian")) {
			provider.addCondition(ResidentAttribute.RA_SKipGaurdian, props.get("SkipGaurdian"));
		}
		provider.addCondition(ResidentAttribute.RA_Gender, enumGender);

		String primaryLanguage = "eng";
		if (props.containsKey("PrimaryLanguage")) {
			primaryLanguage = props.get("PrimaryLanguage").toString();
			provider.addCondition(ResidentAttribute.RA_PRIMARAY_LANG, primaryLanguage);
		}

		if (props.containsKey("SecondaryLanguage")) {
			provider.addCondition(ResidentAttribute.RA_SECONDARY_LANG, props.get("SecondaryLanguage").toString());
		}
		if (props.containsKey("Finger")) {
			provider.addCondition(ResidentAttribute.RA_Finger, Boolean.parseBoolean(props.get("Finger").toString()));
		}
		if (props.containsKey("Iris")) {
			provider.addCondition(ResidentAttribute.RA_Iris, Boolean.parseBoolean(props.get("Iris").toString()));
		}
		if (props.containsKey("Face")) {
			provider.addCondition(ResidentAttribute.RA_Photo, Boolean.parseBoolean(props.get("Face").toString()));
		}
		if (props.containsKey("Document")) {
			provider.addCondition(ResidentAttribute.RA_Document,
					Boolean.parseBoolean(props.get("Document").toString()));
		}
		if (props.containsKey("Invalid")) {
			List<String> invalidList = Arrays.asList(props.get("invalid").toString().split(",", -1));
			provider.addCondition(ResidentAttribute.RA_InvalidList, invalidList);
		}
		if (props.containsKey("Miss")) {

			List<String> missedList = Arrays.asList(props.get("Miss").toString().split(",", -1));
			provider.addCondition(ResidentAttribute.RA_MissList, missedList);
			RestClient.logInfo(contextKey, "before Genrate: missthese:" + missedList.toString());
		}
		if (props.containsKey("ThirdLanguage")) {

			provider.addCondition(ResidentAttribute.RA_THIRD_LANG, props.get("ThirdLanguage").toString());
		}
		if (props.containsKey("SchemaVersion")) {

			provider.addCondition(ResidentAttribute.RA_SCHEMA_VERSION, props.get("SchemaVersion").toString());
		}

		RestClient.logInfo(contextKey, "before Genrate");
		List<ResidentModel> lst = provider.generate(contextKey);
		RestClient.logInfo(contextKey, "After Genrate");

		JSONArray outIds = new JSONArray();

		try {
			String tmpDir;

			tmpDir = Files.createTempDirectory("residents_").toFile().getAbsolutePath();

			VariableManager.setVariableValue(contextKey, "residents_", tmpDir);

			for (ResidentModel r : lst) {
				Path tempPath = Path.of(tmpDir, r.getId() + ".json");
				r.setPath(tempPath.toString());

				String jsonStr = r.toJSONString();

				CommonUtil.write(tempPath, jsonStr.getBytes());

				JSONObject id = new JSONObject();
				id.put("id", r.getId());
				id.put("path", tempPath.toFile().getAbsolutePath());
				outIds.put(id);
			}
		} catch (IOException e) {
			logger.error(e.getMessage());
			return "{\"" + e.getMessage() + "\"}";
		}

		JSONObject response = new JSONObject();
		response.put(STATUS, SUCCESS);
		response.put(RESPONSE, outIds);
		return response.toString();
	}

	public JSONObject makePacketAndSync(String preregId, String templateLocation, String personaPath, String contextKey,
			String additionalInfoReqId) throws Exception {
		// By default , get the rid from packet sync
		return makePacketAndSync(preregId, templateLocation, personaPath, contextKey, additionalInfoReqId, true, true);
	}

	public JSONObject makePacketAndSync(String preregId, String templateLocation, String personaPath, String contextKey,
			String additionalInfoReqId, boolean getRidFromSync, boolean genarateValidCbeff) throws Exception {
		if (RestClient.isDebugEnabled(contextKey))
			logger.info("makePacketAndSync for PRID : {}", preregId);

		Path idJsonPath = null;
		Path docPath = null;
		preregId = preregId.trim();
		if (!preregId.equals("0") && !preregId.equals("01")) {
			String location = preregSyncService.downloadPreregPacket(preregId, contextKey);
			if (RestClient.isDebugEnabled(contextKey))
				logger.info("Downloaded the prereg packet in {} ", location);
			File targetDirectory = Path.of(preregSyncService.getWorkDirectory(), preregId).toFile();
			if (!targetDirectory.exists() && !targetDirectory.mkdir())
				throw new Exception("Failed to create target directory ! PRID : " + preregId);

			if (!zipUtils.unzip(location, targetDirectory.getAbsolutePath(), contextKey))
				throw new Exception("Failed to unzip pre-reg packet >> " + preregId);

			idJsonPath = Path.of(targetDirectory.getAbsolutePath(), "ID.json");
			if (RestClient.isDebugEnabled(contextKey))
				logger.info("Unzipped the prereg packet {}, ID.json exists : {}", preregId,
						idJsonPath.toFile().exists());

		} else {

			if (templateLocation != null) {
				// get idJson From Template itself
				idJsonPath = ContextUtils.idJsonPathFromTemplate(src, templateLocation);
			} else
				idJsonPath = createIDJsonFromPersona(personaPath, contextKey);

		}
		if (templateLocation != null) {
			process = ContextUtils.ProcessFromTemplate(src, templateLocation);
		}
		String packetPath = packetMakerService.createContainer(idJsonPath.toString(), templateLocation, src, process,
				preregId, contextKey, true, additionalInfoReqId);

		String response = null;
		if (RestClient.isDebugEnabled(contextKey))
			logger.info("Packet created : {}", packetPath);

		if (getRidFromSync) {

			response = packetSyncService.syncPacketRid(packetPath, "dummy", "APPROVED", "dummy", null, contextKey,
					additionalInfoReqId);
			if (RestClient.isDebugEnabled(contextKey))
				logger.info("RID Sync response : {}", response);
			JSONObject functionResponse = new JSONObject();
			JSONObject nobj = new JSONObject();

			JSONArray packets = new JSONArray(response);
			if (packets.length() > 0) {
				JSONObject resp = (JSONObject) packets.get(0);
				if (resp.getString(STATUS).equals(SUCCESS)) {

					String rid = resp.getString(REGISTRATIONID);
					response = packetSyncService.uploadPacket(packetPath, contextKey);
					if (RestClient.isDebugEnabled(contextKey))
						logger.info("Packet Sync response : {}", response);
					JSONObject obj = new JSONObject(response);
					if (obj.getString(STATUS).equals("Packet has reached Packet Receiver")) {

						functionResponse.put(RESPONSE, nobj);
						nobj.put(STATUS, SUCCESS);
						nobj.put(REGISTRATIONID, rid);
						return functionResponse;
					}
				}
			}
			functionResponse.put(RESPONSE, nobj);
			nobj.put(STATUS, "Failed");

			return functionResponse;

		} else {

			JSONObject functionResponse = new JSONObject();
			JSONObject nobj = new JSONObject();
			response = packetSyncService.uploadPacket(packetPath, contextKey);
			if (RestClient.isDebugEnabled(contextKey))
				logger.info("Packet Upload response : {}", response);
			JSONObject obj = new JSONObject(response);
			if (obj.getString(STATUS).equals("Packet has reached Packet Receiver")) {
				functionResponse.put(RESPONSE, nobj);
				nobj.put(STATUS, SUCCESS);

				// Get the rid from the packet template
				nobj.put(REGISTRATIONID, packetMakerService.getNewRegId());

				return functionResponse;
			}

			functionResponse.put(RESPONSE, nobj);
			nobj.put(STATUS, "Failed");

			return functionResponse;

		}

	}

	public Path createIDJsonFromPersona(String personaFile, String contextKey) throws IOException {

		loadServerContextProperties(contextKey);
		ResidentModel resident = ResidentModel.readPersona(personaFile);
		JSONObject jsonIdentity = CreatePersona.createIdentity(resident, null, contextKey);
		JSONObject jsonWrapper = new JSONObject();
		jsonWrapper.put("identity", jsonIdentity);
		RestClient.logInfo(contextKey, jsonWrapper.toString());
		String tmpDir = Files.createTempDirectory("preregIds_").toFile().getAbsolutePath();

		VariableManager.setVariableValue(contextKey, "preregIds_", tmpDir);

		Path tempPath = Path.of(tmpDir, resident.getId() + "_ID.json");
		CommonUtil.write(tempPath, jsonWrapper.toString().getBytes());

		return tempPath;

	}

	public String syncPacketRid(String containerFile, String name, String supervisorStatus, String supervisorComment,
			String proc, String contextKey, String additionalInfoReqId) throws Exception {

		RidSyncRequestData ridSyncRequestData = prepareRidSyncRequest(containerFile, name, supervisorStatus,
				supervisorComment, proc, contextKey, additionalInfoReqId);
		JSONArray response = apiRequestUtil.syncRid(baseUrl, baseUrl + syncapi, ridSyncRequestData.getRequestBody(),
				APIRequestUtil.getUTCDateTime(ridSyncRequestData.getTimestamp()), contextKey);

		return response.toString();
	};

	public RidSyncReqResponseDTO syncPacketRidRequest(String containerFile, String name, String supervisorStatus,
			String supervisorComment, String proc, String contextKey, String additionalInfoReqId) throws Exception {

		RidSyncRequestData ridSyncRequestData = prepareRidSyncRequest(containerFile, name, supervisorStatus,
				supervisorComment, proc, contextKey, additionalInfoReqId);
		String centerId = null;
		String machineId = null;

		Properties props = contextUtils.loadServerContext(contextKey);
		for (Entry<Object, Object> entrySet : props.entrySet()) {
			if (entrySet.getKey().equals(MOSIP_TEST_REGCLIENT_CENTERID))
				centerId = entrySet.getValue().toString();
			else if (entrySet.getKey().equals(MOSIP_TEST_REGCLIENT_MACHINEID))
				machineId = entrySet.getValue().toString();
		}

		Map<String, String> headers = new HashMap<>();
		headers.put("timestamp", APIRequestUtil.getUTCDateTime(ridSyncRequestData.getTimestamp()));
		headers.put("Center-Machine-RefId", centerId + UNDERSCORE + machineId);

		return new RidSyncReqResponseDTO(headers, ridSyncRequestData.getRequestBody());

	}

	private RidSyncRequestData prepareRidSyncRequest(String containerFile, String name, String supervisorStatus,
			String supervisorComment, String proc, String contextKey, String additionalInfoReqId)
			throws Exception, Exception {
		if (contextKey != null && !contextKey.equals("")) {

			Properties props = contextUtils.loadServerContext(contextKey);
			props.forEach((k, v) -> {
				if (k.toString().equals("mosip.test.packet.syncapi")) {
					syncapi = v.toString();
				} else if (k.toString().equals(MOSIP_TEST_REGCLIENT_MACHINEID)) {
					machineId = v.toString();
				} else if (k.toString().equals("mosip.test.primary.langcode")) {
					primaryLangCode = v.toString();
				} else if (k.toString().equals(MOSIP_TEST_REGCLIENT_CENTERID)) {
					centerId = v.toString();
				} else if (k.toString().equals("mosip.test.baseurl")) {
					baseUrl = v.toString();
				} else if (k.toString().equals("mosip.version")) {
					mosipVersion = v.toString();
				}

			});
		}
		Path container = Path.of(containerFile);
		String rid = null;
		if (container.getName(container.getNameCount() - 1).toString().contains("-")) {
			rid = PacketMakerService.getRegIdFromPacketPath(containerFile);
		} else {
			rid = container.getName(container.getNameCount() - 1).toString().replace(".zip", "");
		}
		if (proc != null && !proc.equals(""))
			process = proc;
		if (RestClient.isDebugEnabled(contextKey)) {
			logger.info("Syncing data for RID : {}", rid);
			logger.info("Syncing data: process:", process);
		}
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(REGISTRATIONID, rid);
		jsonObject.put("langCode", primaryLangCode);
		jsonObject.put("name", name);
		jsonObject.put("email", "");
		jsonObject.put("phone", "");
		jsonObject.put("registrationType", process);

		byte[] fileBytes = CommonUtil.read(containerFile);

		String checkSum = VariableManager.getVariableValue(contextKey, "invalidCheckSum").toString();
		// Provide invalid checksum before the sync conditionally
		if (checkSum.equalsIgnoreCase("invalidCheckSum"))
			jsonObject.put("packetHashValue", "INVALID_CHECKSUM");
		else
			jsonObject.put("packetHashValue", cryptoUtil.getHexEncodedHash(fileBytes));

		jsonObject.put("packetSize", fileBytes.length);
		jsonObject.put("supervisorStatus", supervisorStatus);
		jsonObject.put("supervisorComment", supervisorComment);

		if (mosipVersion != null && !mosipVersion.isEmpty() && mosipVersion.equals("1.2")) {
			String id = StringUtils.isNotBlank(additionalInfoReqId) ? additionalInfoReqId : rid;
			String packetId = (container.getName(container.getNameCount() - 1).toString()).replace(".zip", "");

			jsonObject.put("packetId", packetId);
			jsonObject.put("additionalInfoReqId", id);
		}

		JSONArray list = new JSONArray();
		list.put(jsonObject);

		JSONObject wrapper = new JSONObject();
		wrapper.put("id", "mosip.registration.sync");
		wrapper.put("requesttime", APIRequestUtil.getUTCDateTime(LocalDateTime.now(ZoneOffset.UTC)));
		wrapper.put("version", "1.0");
		wrapper.put("request", list);

		String packetCreatedDateTime = rid.substring(rid.length() - 14);
		String formattedDate = packetCreatedDateTime.substring(0, 8) + "T"
				+ packetCreatedDateTime.substring(packetCreatedDateTime.length() - 6);
		LocalDateTime timestamp = LocalDateTime.parse(formattedDate, DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss"));

		String requestBody = Base64.encodeBase64URLSafeString(cryptoUtil.encrypt(wrapper.toString().getBytes("UTF-8"),
				centerId + UNDERSCORE + machineId, timestamp, contextKey));
		return new RidSyncRequestData(requestBody, timestamp);
	}

	public String uploadPacket(String path, String contextKey) throws Exception {

		if (contextKey != null && !contextKey.equals("")) {

			Properties props = contextUtils.loadServerContext(contextKey);
			props.forEach((k, v) -> {
				if (k.toString().equals("mosip.test.packet.uploadapi")) {

					uploadapi = v.toString();

				} else if (k.toString().equals("mosip.test.baseurl")) {
					baseUrl = v.toString();
				}
			});
		}
		// To do -- We need to mark supervisor status as approved or rejected
		// conditionally
		VariableManager.setVariableValue(contextKey, "SUPERVISOR_APPROVAL_STATUS", "APPROVED");

		// To do -- Need to review these two below tags once the conclusion happens what
		// tags will be set on the packet
		VariableManager.setVariableValue(contextKey, "META_INFO-CAPTURED_REGISTERED_DEVICES-Finger",
				"MOSIP-FINGER01-2345678901");
		VariableManager.setVariableValue(contextKey, "META_INFO-CAPTURED_REGISTERED_DEVICES-Face",
				"MOSIP-FACE01-2345678901");

		logger.debug("Tags set while generating the packet: "
				+ VariableManager.getVariableValue(contextKey, "META_INFO-OPERATIONS_DATA-supervisorId")
				+ VariableManager.getVariableValue(contextKey, "Biometric_Quality-Iris")
				+ VariableManager.getVariableValue(contextKey, "INTRODUCER_AVAILABILITY")
				+ VariableManager.getVariableValue(contextKey, "META_INFO-CAPTURED_REGISTERED_DEVICES-Finger")
				+ VariableManager.getVariableValue(contextKey, "META_INFO-META_DATA-centerId")
				+ VariableManager.getVariableValue(contextKey, "Biometric_Quality-Face")
				+ VariableManager.getVariableValue(contextKey, "Biometric_Quality-Finger")
				+ VariableManager.getVariableValue(contextKey, "EXCEPTION_BIOMETRICS")
				+ VariableManager.getVariableValue(contextKey, "ID_OBJECT-gender")
				+ VariableManager.getVariableValue(contextKey, "META_INFO-CAPTURED_REGISTERED_DEVICES-Face")
				+ VariableManager.getVariableValue(contextKey, "AGE_GROUP")
				+ VariableManager.getVariableValue(contextKey, "SUPERVISOR_APPROVAL_STATUS")
				+ VariableManager.getVariableValue(contextKey, "META_INFO-OPERATIONS_DATA-officerId")
				+ VariableManager.getVariableValue(contextKey, "ID_OBJECT-residenceStatus"));

		RestClient.logInfo(contextKey, baseUrl + uploadapi + ",path=" + path);
		JSONObject response = apiRequestUtil.uploadFile(baseUrl, baseUrl + uploadapi, path, contextKey);
		if (!RestClient.isDebugEnabled(contextKey)) {
			if (VariableManager.getVariableValue(contextKey, "mosip.test.temp") != null
					&& VariableManager.getVariableValue(contextKey, "mountPath") != null) {

				deleteDirectoryPath(VariableManager.getVariableValue(contextKey, "mountPath").toString()
						+ VariableManager.getVariableValue(contextKey, "mosip.test.temp").toString()
						+ contextKey.substring(0, contextKey.lastIndexOf("_context")), contextKey);
			}
		}
		return response.toString();
	}

	public void deleteDirectoryPath(String path, String contextKey) {
		if (path != null && !path.isEmpty()) {
			File file = new File(path);
			if (file.exists()) {
				do {
					deleteIt(file, contextKey);
				} while (file.exists());
			} else {
			}
		}
	}

	private void deleteIt(File file, String contextKey) {
		if (file.isDirectory()) {
			String fileList[] = file.list();
			if (fileList.length == 0) {
				if (!file.delete()) {
					RestClient.logInfo(contextKey, "Files deleted");
				}
			} else {
				int size = fileList.length;
				for (int i = 0; i < size; i++) {
					String fileName = fileList[i];
					String fullPath = file.getPath() + "/" + fileName;
					File fileOrFolder = new File(fullPath);
					deleteIt(fileOrFolder, contextKey);
				}
			}
		} else {
			if (!file.delete()) {
				RestClient.logInfo(contextKey, "Files deleted");
			}
		}
	}

	public String preRegisterResident(List<String> personaFilePath, String contextKey) throws IOException {
		StringBuilder builder = new StringBuilder();

		loadServerContextProperties(contextKey);

		for (String path : personaFilePath) {
			ResidentModel resident = ResidentModel.readPersona(path);
			String response = PreRegistrationSteps.postApplication(resident, null, contextKey);
			// preregid
			saveRegIDMap(response, path);
			builder.append(response);
		}
		return builder.toString();
	}

	public String updateResidentApplication(String personaFilePath, String preregId, String contextKey)
			throws IOException {

		loadServerContextProperties(contextKey);
		ResidentModel resident = ResidentModel.readPersona(personaFilePath);
		return PreRegistrationSteps.putApplication(resident, preregId, contextKey);

	}

	public String preRegisterGetApplications(String status, String preregId, String contextKey) {
		loadServerContextProperties(contextKey);
		return PreRegistrationSteps.getApplications(status, preregId, contextKey);
	}

	void saveRegIDMap(String preRegId, String personaFilePath) {
		Properties p = new Properties();
		try (FileReader reader = new FileReader(preRegMapFile); FileWriter writer = new FileWriter(preRegMapFile);) {

			p.load(reader);
			p.put(preRegId, personaFilePath);

			p.store(writer, "PreRegID to persona mapping file");

		} catch (IOException e) {
			logger.error("saveRegIDMap " + e.getMessage());
		}

	}

	String getPersona(String preRegId) {
		try (FileReader reader = new FileReader(preRegMapFile)) {
			Properties p = new Properties();
			p.load(reader);
			return p.getProperty(preRegId);
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		return null;
	}

	public String requestOtp(List<String> personaFilePath, String to, String contextKey) throws IOException {
		StringBuilder builder = new StringBuilder();

		loadServerContextProperties(contextKey);

		for (String path : personaFilePath) {
			ResidentModel resident = ResidentModel.readPersona(path);
			ResidentPreRegistration preReg = new ResidentPreRegistration(resident);
			builder.append(preReg.sendOtpTo(to, contextKey));

		}
		return builder.toString();
	}

	public String verifyOtp(String personaFilePath, String to, String otp, String contextKey) throws IOException {

		loadServerContextProperties(contextKey);
		ResidentModel resident = ResidentModel.readPersona(personaFilePath);
		ResidentPreRegistration preReg = new ResidentPreRegistration(resident);

		preReg.fetchOtp(contextKey);
		return preReg.verifyOtp(to, otp, contextKey);

	}

	public String getAvailableAppointments(String contextKey) {
		loadServerContextProperties(contextKey);
		AppointmentModel res = PreRegistrationSteps.getAppointments(contextKey);
		return res.toJSONString();
	}

	public String bookSpecificAppointment(String preregId, AppointmentDto appointmentDto, String contextKey) {

		AppointmentTimeSlotModel ts = new AppointmentTimeSlotModel();
		ts.setFromTime(appointmentDto.getTime_slot_from());
		ts.setToTime(appointmentDto.getTime_slot_to());

		return PreRegistrationSteps.bookAppointment(preregId, appointmentDto.getAppointment_date(),
				appointmentDto.getRegistration_center_id(), ts, contextKey);

	}

	public String bookAppointment(String preRegID, int nthSlot, String contextKey) {

		String retVal = "{\"Failed\"}";
		Boolean bBooked = false;

		loadServerContextProperties(contextKey);

		String base = VariableManager.getVariableValue(contextKey, "urlBase").toString().trim();
		String api = VariableManager.getVariableValue(VariableManager.NS_DEFAULT, "appointmentslots").toString().trim();

		AppointmentModel res = PreRegistrationSteps.getAppointments(contextKey);

		for (CenterDetailsModel a : res.getAvailableDates()) {
			if (!a.getHoliday()) {
				for (AppointmentTimeSlotModel ts : a.getTimeslots()) {
					if (ts.getAvailability() > 0) {
						nthSlot--;
						if (nthSlot == 0) {
							retVal = PreRegistrationSteps.bookAppointment(preRegID, a.getDate(), res.getRegCenterId(),
									ts, contextKey);
							bBooked = true;

							break;
						}
					}
				}
			}
			if (bBooked)
				break;
		}
		return retVal;
	}

	/*
	 * Book appointment on any specified slot nThSlot -> min 1
	 */
	public String bookAppointmentSlot(String preRegID, int nthSlot, boolean bHoliday, String contextKey) {

		String retVal = "{\"Failed\"}";
		Boolean bBooked = false;

		loadServerContextProperties(contextKey);

		AppointmentModel res = PreRegistrationSteps.getAppointments(contextKey);

		for (CenterDetailsModel a : res.getAvailableDates()) {
			// if specified book on a holiday
			if (bHoliday) {
				if (a.getHoliday()) {
					for (AppointmentTimeSlotModel ts : a.getTimeslots()) {

						nthSlot--;
						if (nthSlot == 0) {
							retVal = PreRegistrationSteps.bookAppointment(preRegID, a.getDate(), res.getRegCenterId(),
									ts, contextKey);
							bBooked = true;
							break;
						}
					}
					if (bBooked)
						break;
					else
						continue;
				}
			}

			if (!a.getHoliday()) {
				for (AppointmentTimeSlotModel ts : a.getTimeslots()) {

					nthSlot--;
					if (nthSlot == 0) {
						retVal = PreRegistrationSteps.bookAppointment(preRegID, a.getDate(), res.getRegCenterId(), ts,
								contextKey);
						bBooked = true;
						break;

					}
				}
			}
			if (bBooked)
				break;
		}
		return retVal;
	}

	public String cancelAppointment(String preregId, AppointmentDto appointmentDto, String contextKey) {
		loadServerContextProperties(contextKey);

		return PreRegistrationSteps.cancelAppointment(preregId, appointmentDto.getTime_slot_from(),
				appointmentDto.getTime_slot_to(), appointmentDto.getAppointment_date(),
				appointmentDto.getRegistration_center_id(), contextKey);

	}

	public String deleteApplication(String preregId, String contextKey) {
		loadServerContextProperties(contextKey);
		return PreRegistrationSteps.deleteApplication(preregId, contextKey);
	}

	public String discardBooking(HashMap<String, String> map, String contextKey) {

		return PreRegistrationSteps.discardBooking(map, contextKey);
	}

	public String uploadDocuments(String personaFilePath, String preregId, String contextKey) throws IOException {

		String response = "";

		loadServerContextProperties(contextKey);
		ResidentModel resident = ResidentModel.readPersona(personaFilePath);

		for (MosipDocument a : resident.getDocuments()) {
			JSONObject respObject = PreRegistrationSteps.UploadDocument(a.getDocCategoryCode(),
					a.getType().get(0).getDocTypeCode(), a.getDocCategoryLang(), a.getDocs().get(0), preregId,
					contextKey);
			if (respObject != null)
				response = response + respObject.toString();
		}

		return response;
	}

	public String createPacketTemplates(List<String> personaFilePaths, String process, String outDir, String preregId,
			String contextKey, String purpose, String qualityScore, boolean genarateValidCbeff) throws IOException {
		boolean packetDirCreated = false;
		Path packetDir = null;
		JSONArray packetPaths = new JSONArray();

		RestClient.logInfo(contextKey, "createPacketTemplates->outDir:" + outDir);

		loadServerContextProperties(contextKey);
		if (process != null) {
			VariableManager.setVariableValue(contextKey, "process", process);
		}
		if (outDir == null || outDir.trim().equals("")) {
			packetDir = Files.createTempDirectory("packets_");
			VariableManager.setVariableValue(contextKey, "packets_", packetDir.toFile().getAbsolutePath());

			RestClient.logInfo(contextKey, "packetDir=" + packetDir);
		} else {
			packetDir = Paths.get(outDir);
			RestClient.logInfo(contextKey, "packetDir=" + packetDir);
		}
		if (!packetDir.toFile().exists()) {
			packetDirCreated = packetDir.toFile().createNewFile();
			if (packetDirCreated)
				RestClient.logInfo(contextKey, "packetDirCreated:" + packetDirCreated);
		}
		PacketTemplateProvider packetTemplateProvider = new PacketTemplateProvider();

		try {
			JSONObject preregResponse = new JSONObject();
			JSONObject queryparam = new JSONObject();
			Properties props = contextUtils.loadServerContext(contextKey);
			if (props.containsKey("mosip.test.regclient.officerBiometricFileName")) {
				queryparam.put("type", "bio");
				String uin = props.getProperty("validUIN");
				baseUrl = props.getProperty("urlBase");
				preregResponse = apiRequestUtil.getJsonObject(baseUrl, baseUrl + idvid + uin, queryparam,
						new JSONObject(), contextKey);
			}
			for (String path : personaFilePaths) {
				ResidentModel resident = ResidentModel.readPersona(path);
				String packetPath = packetDir.toString() + File.separator + resident.getId();
				RestClient.logInfo(contextKey, "packetPath=" + packetPath);
				machineId = VariableManager.getVariableValue(contextKey, MOSIP_TEST_REGCLIENT_MACHINEID).toString();

				centerId = VariableManager.getVariableValue(contextKey, MOSIP_TEST_REGCLIENT_CENTERID).toString();

				String returnMsg = packetTemplateProvider.generate("registration_client", process, resident, packetPath, preregId,
						machineId, centerId, contextKey, props, preregResponse, purpose, qualityScore,
						genarateValidCbeff);
				if (!returnMsg.equalsIgnoreCase("Success"))
					return "{\"" + returnMsg + "\"}";

				JSONObject obj = new JSONObject();
				obj.put("id", resident.getId());
				obj.put("path", packetPath);
				RestClient.logInfo(contextKey, "createPacket:" + packetPath);
				RestClient.logInfo(contextKey, "obj=" + obj);
				packetPaths.put(obj);

			}
		} catch (Exception e) {
			logger.error("createPacketTemplates", e);
			return "{\"" + e.getMessage() + "\"}";
		}

		JSONObject response = new JSONObject();
		response.put("packets", packetPaths);
		return response.toString();

	}

	public String preRegToRegister(String templatePath, String preRegId, String personaPath, String contextKey,
			String additionalInfoReqId, boolean getRidFromSync, boolean genarateValidCbeff) throws Exception {

		return makePacketAndSync(preRegId, templatePath, personaPath, contextKey, additionalInfoReqId, getRidFromSync,
				genarateValidCbeff).toString();

	}

	void updatePersona(Properties updateAttrs, ResidentModel persona) {
		Iterator<Object> it = updateAttrs.keys().asIterator();
		BiometricDataModel bioData = null;

		while (it.hasNext()) {
			String key = it.next().toString();
			String value = updateAttrs.getProperty(key);
			key = key.toLowerCase().trim();

			// first check whether it is document being updated?

			MosipDocument doc = null;
			for (MosipDocument md : persona.getDocuments()) {
				if (md.getDocCategoryCode().toLowerCase().equals(key) || md.getDocCategoryName().equals(key)) {
					doc = md;
					break;
				}

			}
			if (doc != null) {
				JSONObject jsonDoc = new JSONObject(value);
				String typeName = jsonDoc.has("typeName") ? jsonDoc.get("typeName").toString() : "";
				String typeCode = jsonDoc.has("typeCode") ? jsonDoc.get("typeCode").toString() : "";
				int indx = -1;
				for (MosipDocTypeModel tm : doc.getType()) {
					indx++;
					if ((tm.getDocTypeCode() != null && tm.getDocTypeCode().equals(typeCode))
							|| (tm.getDocTypeName() != null && tm.getDocTypeName().equals(typeName)))
						break;
				}
				if (indx >= 0 && indx < doc.getType().size()) {
					String docFilePath = jsonDoc.has("docPath") ? jsonDoc.getString("docPath").toString() : null;
					if (docFilePath != null)
						doc.getDocs().set(indx, docFilePath);
				}
				continue;

			}
			switch (key) {
			case "face":
			case "photo":
				bioData = persona.getBiometric();
				byte[][] faceData = PhotoProvider.loadPhoto(value);
				bioData.setEncodedPhoto(Base64.encodeBase64URLSafeString(faceData[0]));
				bioData.setRawFaceData(faceData[1]);

				try {
					bioData.setFaceHash(CommonUtil.getHexEncodedHash(faceData[1]));
				} catch (Exception e1) {
				}

				break;
			case "left_iris":
				bioData = persona.getBiometric();
				IrisDataModel im = bioData.getIris();
				IrisDataModel imUpdated = null;
				try {
					imUpdated = BiometricDataProvider.loadIris(value, "left", im);
					if (imUpdated != null)
						persona.getBiometric().setIris(imUpdated);

				} catch (Exception e) {
					logger.error(e.getMessage());
				}

				break;
			case "right_iris":
				bioData = persona.getBiometric();
				IrisDataModel im1 = bioData.getIris();
				IrisDataModel imUpdated1 = null;
				try {
					imUpdated1 = BiometricDataProvider.loadIris(value, "right", im1);
					if (imUpdated1 != null)
						persona.getBiometric().setIris(imUpdated1);

				} catch (Exception e) {
					logger.error(e.getMessage());
				}

				break;
			case "gender":
				persona.setGender(Gender.valueOf(value));
				break;
			case "email":
			case "emailid":
				persona.getContact().setEmailId(value);

				break;

			case "dob":
			case "dateofbirth":
				persona.setDob(value);
				break;
			case "bloodgroup":
			case "bg":

				DynamicFieldValueModel bg = persona.getBloodgroup();
				bg.setCode(value);
				break;
			case "maritalstatus":
			case "ms":
				DynamicFieldValueModel ms = persona.getMaritalStatus();
				ms.setCode(value);
				break;
			case "residencestatus":
			case "rs":
				if (value != null && !value.equals("")) {
					String lang = persona.getPrimaryLanguage();
					String[] parts = value.split("=");
					String msCode = null;
					if (parts.length > 1) {
						lang = parts[0].trim();
						msCode = parts[1].trim();
					} else
						msCode = parts[0].trim();

					if (lang.equals(persona.getPrimaryLanguage())) {

						MosipIndividualTypeModel rs = persona.getResidentStatus();
						rs.setCode(msCode);
					} else {
						MosipIndividualTypeModel rs = persona.getResidentStatus_seclang();
						rs.setCode(msCode);
					}
				}
				break;
			default:// Added by VS to passthrough attributes
				persona.getAddtionalAttributes().put(key, value);
				break;

			}
		}
	}

	public String getPersonaData(List<UpdatePersonaDto> getPersonaRequest, String contextKey) throws Exception {

		Properties retProp = new Properties();

		for (UpdatePersonaDto req : getPersonaRequest) {

			ResidentModel persona = ResidentModel.readPersona(req.getPersonaFilePath());
			List<String> retrieveAttrs = req.getRetriveAttributeList();
			if (retrieveAttrs != null) {
				for (String attr : retrieveAttrs) {
					Object val = null;
					String key = attr.trim();
					switch (key.toLowerCase()) {
					case "demodata":
						val = persona.loadDemoData();
						retProp.put(key, val);
						break;
					case "faceraw":
						val = persona.getBiometric().getRawFaceData();
						retProp.put(key, val);
						break;

					case "face":
						val = persona.getBiometric().getEncodedPhoto();
						retProp.put(key, val);
						break;
					case "face_encrypted":
						if (persona.getBiometric().getCapture() != null) {
							val = persona.getBiometric().getCapture().get(DataProviderConstants.MDS_DEVICE_TYPE_FACE)
									.get(0).getBioValue();
						}
						retProp.put(key, val);
						break;
					case "iris":
						IrisDataModel irisval = persona.getBiometric().getIris();

						retProp.put(key, irisval.toJSONString());
						break;
					case "iris_encrypted":
						IrisDataModel irisvalue = null;
						if (persona.getBiometric().getCapture() != null) {
							irisvalue = new IrisDataModel();

							List<MDSDeviceCaptureModel> lstIrisData = persona.getBiometric().getCapture()
									.get(DataProviderConstants.MDS_DEVICE_TYPE_IRIS);
							for (MDSDeviceCaptureModel cm : lstIrisData) {

								if (cm.getBioSubType().equals("Left"))
									irisvalue.setLeft(cm.getBioValue());
								else if (cm.getBioSubType().equals("Right"))
									irisvalue.setRight(cm.getBioValue());
							}
							val = irisvalue;
						}

						retProp.put(key, val);
						break;
					case "finger":
						String[] fps = persona.getBiometric().getFingerPrint();
						for (int i = 0; i < fps.length; i++) {
							retProp.put(DataProviderConstants.displayFingerName[i], fps[i]);
						}
						break;
					case "finger_encrypted":

						if (persona.getBiometric().getCapture() != null) {

							List<MDSDeviceCaptureModel> lstFingerData = persona.getBiometric().getCapture()
									.get(DataProviderConstants.MDS_DEVICE_TYPE_FINGER);
							for (MDSDeviceCaptureModel cm : lstFingerData) {
								retProp.put(cm.getBioSubType(), cm.getBioValue());
							}
						}
						break;
					case "fingerraw":
						byte[][] fpsraw = persona.getBiometric().getFingerRaw();
						for (int i = 0; i < fpsraw.length; i++) {
							retProp.put(DataProviderConstants.displayFingerName[i], fpsraw[i]);
						}
						break;
					case "finger_hash": {
						if (persona.getBiometric().getCapture() != null) {

							List<MDSDeviceCaptureModel> lstFingerData = persona.getBiometric().getCapture()
									.get(DataProviderConstants.MDS_DEVICE_TYPE_FINGER);
							for (MDSDeviceCaptureModel cm : lstFingerData) {
								byte[] valBytes = java.util.Base64.getUrlDecoder().decode(cm.getBioValue());
								retProp.put(cm.getBioSubType(), CommonUtil.getSHAFromBytes(valBytes));
							}
						}
					}
						break;
					case "iris_hash":
						IrisDataModel irisvalueh = null;
						if (persona.getBiometric().getCapture() != null) {
							irisvalueh = new IrisDataModel();

							List<MDSDeviceCaptureModel> lstIrisData = persona.getBiometric().getCapture()
									.get(DataProviderConstants.MDS_DEVICE_TYPE_IRIS);
							for (MDSDeviceCaptureModel cm : lstIrisData) {

								if (cm.getBioSubType().equals("Left")) {
									byte[] valBytes = java.util.Base64.getUrlDecoder().decode(cm.getBioValue());
									irisvalueh.setLeft(CommonUtil.getSHAFromBytes(valBytes));
								} else if (cm.getBioSubType().equals("Right")) {
									byte[] valBytes = java.util.Base64.getUrlDecoder().decode(cm.getBioValue());
									irisvalueh.setRight(CommonUtil.getSHAFromBytes(valBytes));
								}
							}
							val = irisvalueh;
						}

						retProp.put(key, val);
						break;
					case "face_hash":
						if (persona.getBiometric().getCapture() != null) {
							val = persona.getBiometric().getCapture().get(DataProviderConstants.MDS_DEVICE_TYPE_FACE)
									.get(0).getBioValue();
							byte[] valBytes = java.util.Base64.getUrlDecoder().decode(val.toString());
							val = CommonUtil.getSHAFromBytes(valBytes);
						}
						retProp.put(key, val);
						break;
					case "address":
						JSONObject resp = new JSONObject();
						String secLang = persona.getSecondaryLanguage();
						String[] addr = persona.getAddress();

						if (secLang != null) {
							String[] addr_sec = persona.getAddress_seclang();
							for (int i = 0; i < 3; i++) {
								JSONArray addrJson = new JSONArray();
								JSONObject lineJson = new JSONObject();
								lineJson.put("language", persona.getPrimaryLanguage());
								lineJson.put("value", addr[i]);
								addrJson.put(lineJson);
								lineJson = new JSONObject();
								lineJson.put("language", persona.getSecondaryLanguage());
								lineJson.put("value", addr_sec[i]);
								addrJson.put(lineJson);
								resp.put("addressLine" + (i + 1), addrJson);
							}
						} else {
							for (int i = 0; i < 3; i++) {
								resp.put("addressLine" + (i + 1), addr[i]);
							}
						}
						retProp.put(key, resp);
						break;
					}

				}
			}

		}
		JSONObject jsonProps = new JSONObject(retProp);
		return jsonProps.toString();
	}

	public String updatePersonaData(List<UpdatePersonaDto> updatePersonaRequest, String contextKey) throws Exception {
		String ret = "{Sucess}";
		for (UpdatePersonaDto req : updatePersonaRequest) {
			try {
				ResidentModel persona = ResidentModel.readPersona(req.getPersonaFilePath());
				List<String> regenAttrs = req.getRegenAttributeList();
				if (regenAttrs != null) {
					for (String attr : regenAttrs) {
						ResidentDataProvider.updateBiometric(persona, attr, contextKey);

					}
				}
				Properties updateAttrs = req.getUpdateAttributeList();
				if (updateAttrs != null) {
					updatePersona(updateAttrs, persona);
				}
				List<String> missList = req.getMissAttributeList();
				if (missList != null && !missList.isEmpty())
					persona.setMissAttributes(missList);

				persona.writePersona(req.getPersonaFilePath());

			} catch (IOException e) {
				logger.error("updatePersonaData:" + e.getMessage());
			}

		}
		return ret;
	}

	public String updateResidentData(Hashtable<PersonaRequestType, Properties> hashtable, String uin, String rid)
			throws IOException {

		Properties list = hashtable.get(PersonaRequestType.PR_ResidentList);

		String filePathResident = null;
		String filePathParent = null;
		ResidentModel persona = null;
		ResidentModel guardian = null;

		for (Object key : list.keySet()) {
			String keyS = key.toString().toLowerCase();
			if (keyS.startsWith("uin")) {
				filePathResident = list.get(key).toString();
				persona = ResidentModel.readPersona(filePathResident);
				persona.setUIN(uin);
			} else if (keyS.toString().startsWith("rid")) {
				filePathResident = list.get(key).toString();
				persona = ResidentModel.readPersona(filePathResident);
				persona.setRID(rid);
			} else if (keyS.toString().startsWith("child")) {
				filePathResident = list.get(key).toString();
				persona = ResidentModel.readPersona(filePathResident);
			} else if (keyS.startsWith("guardian")) {
				filePathParent = list.get(key).toString();
				guardian = ResidentModel.readPersona(filePathParent);
			}
		}
		if (guardian != null && persona != null)
			persona.setGuardian(guardian);
		if (persona != null) {
			CommonUtil.write(Paths.get(filePathResident), persona.toJSONString().getBytes());
			return "{\"response\":\"SUCCESS\"}";
		} else {
			return "{\"response\":\"FAIL\"}";
		}
	}

	public String updatePersonaBioExceptions(BioExceptionDto personaBERequestDto, String contextKey) {

		RestClient.logInfo(contextKey, "updatePersonaBioExceptions:" + contextKey);

		loadServerContextProperties(contextKey);
		String ret = "{Sucess}";
		try {
			ResidentModel persona = ResidentModel.readPersona(personaBERequestDto.getPersonaFilePath());

			persona.setBioExceptions(personaBERequestDto.getExceptions());

			persona.writePersona(personaBERequestDto.getPersonaFilePath());
		} catch (Exception e) {
			logger.error("updatePersonaBioExceptions:" + e.getMessage());
		}
		return null;
	}

	public String bulkuploadPackets(List<String> packetPaths, String contextKey) {

		loadServerContextProperties(contextKey);

		return MosipDataSetup.uploadPackets(packetPaths, contextKey);

	}

	public String getPacketTags(String contextKey) {

//		loadServerContextProperties(contextKey);

		JSONObject packetTags = new JSONObject();

		packetTags.put("META_INFO-OPERATIONS_DATA-supervisorId",
				VariableManager.getVariableValue(contextKey, "META_INFO-OPERATIONS_DATA-supervisorId") == null
						? "--TAG_VALUE_NOT_AVAILABLE--"
						: VariableManager.getVariableValue(contextKey, "META_INFO-OPERATIONS_DATA-supervisorId")
								.toString());

		packetTags.put("Biometric_Quality-Iris",
				VariableManager.getVariableValue(contextKey, "Biometric_Quality-Iris") == null
						? "--TAG_VALUE_NOT_AVAILABLE--"
						: VariableManager.getVariableValue(contextKey, "Biometric_Quality-Iris").toString());

		packetTags.put("INTRODUCER_AVAILABILITY",
				VariableManager.getVariableValue(contextKey, "INTRODUCER_AVAILABILITY").toString());

		packetTags.put("META_INFO-CAPTURED_REGISTERED_DEVICES-Finger",
				VariableManager.getVariableValue(contextKey, "META_INFO-CAPTURED_REGISTERED_DEVICES-Finger") == null
						? "--TAG_VALUE_NOT_AVAILABLE--"
						: VariableManager.getVariableValue(contextKey, "META_INFO-CAPTURED_REGISTERED_DEVICES-Finger")
								.toString());

		packetTags.put("META_INFO-META_DATA-centerId",
				VariableManager.getVariableValue(contextKey, "META_INFO-META_DATA-centerId") == null
						? "--TAG_VALUE_NOT_AVAILABLE--"
						: VariableManager.getVariableValue(contextKey, "META_INFO-META_DATA-centerId").toString());

		packetTags.put("Biometric_Quality-Face",
				VariableManager.getVariableValue(contextKey, "Biometric_Quality-Face") == null
						? "--TAG_VALUE_NOT_AVAILABLE--"
						: VariableManager.getVariableValue(contextKey, "Biometric_Quality-Face").toString());

		packetTags.put("Biometric_Quality-Finger",
				VariableManager.getVariableValue(contextKey, "Biometric_Quality-Finger") == null
						? "--TAG_VALUE_NOT_AVAILABLE--"
						: VariableManager.getVariableValue(contextKey, "Biometric_Quality-Finger").toString());

		packetTags.put("EXCEPTION_BIOMETRICS",
				VariableManager.getVariableValue(contextKey, "EXCEPTION_BIOMETRICS") == null
						? "--TAG_VALUE_NOT_AVAILABLE--"
						: VariableManager.getVariableValue(contextKey, "EXCEPTION_BIOMETRICS").toString());

		packetTags.put("ID_OBJECT-gender",
				VariableManager.getVariableValue(contextKey, "ID_OBJECT-gender") == null ? "--TAG_VALUE_NOT_AVAILABLE--"
						: VariableManager.getVariableValue(contextKey, "ID_OBJECT-gender").toString());

		packetTags.put("META_INFO-CAPTURED_REGISTERED_DEVICES-Face",
				VariableManager.getVariableValue(contextKey, "META_INFO-CAPTURED_REGISTERED_DEVICES-Face") == null
						? "--TAG_VALUE_NOT_AVAILABLE--"
						: VariableManager.getVariableValue(contextKey, "META_INFO-CAPTURED_REGISTERED_DEVICES-Face")
								.toString());

		packetTags.put("AGE_GROUP",
				VariableManager.getVariableValue(contextKey, "AGE_GROUP") == null ? "--TAG_VALUE_NOT_AVAILABLE--"
						: VariableManager.getVariableValue(contextKey, "AGE_GROUP").toString());

		packetTags.put("SUPERVISOR_APPROVAL_STATUS",
				VariableManager.getVariableValue(contextKey, "SUPERVISOR_APPROVAL_STATUS") == null
						? "--TAG_VALUE_NOT_AVAILABLE--"
						: VariableManager.getVariableValue(contextKey, "SUPERVISOR_APPROVAL_STATUS").toString());

		packetTags.put("META_INFO-OPERATIONS_DATA-officerId",
				VariableManager.getVariableValue(contextKey, "META_INFO-OPERATIONS_DATA-officerId") == null
						? "--TAG_VALUE_NOT_AVAILABLE--"
						: VariableManager.getVariableValue(contextKey, "META_INFO-OPERATIONS_DATA-officerId")
								.toString());

		packetTags.put("ID_OBJECT-residenceStatus",
				VariableManager.getVariableValue(contextKey, "ID_OBJECT-residenceStatus") == null
						? "--TAG_VALUE_NOT_AVAILABLE--"
						: VariableManager.getVariableValue(contextKey, "ID_OBJECT-residenceStatus").toString());

		return packetTags.toString();

	}

	String getRegIdFromPacketPath(String packetPath) {
		// leaf node of packet path is regid
		return Path.of(packetPath).getFileName().toString();
	}

	public String validatePacket(String packetPath, String processArg, String contextKey) {

		JSONObject ret = new JSONObject();
		ret.put(STATUS, SUCCESS);
		loadServerContextProperties(contextKey);
		String regId = getRegIdFromPacketPath(packetPath);
		String tempPacketRootFolder = Path.of(packetPath).toString();
		String jsonSchema = MosipMasterData.getIDSchemaSchemaLatestVersion(contextKey);
		String processRoot = Path.of(tempPacketRootFolder, src, process).toString();
		String packetRoot = Path.of(processRoot, "rid_id").toString();
		String identityJson = CommonUtil.readFromJSONFile(packetRoot + "/ID.json");
		try {

			CommonUtil.validateJSONSchema(jsonSchema, identityJson);

		} catch (ValidationException ex) {

			ret.put(STATUS, "Error");
			ret.put("message", ex.getMessage());

		}
		return ret.toString();
	}

	public String setPersonaMockABISExpectation(List<MockABISExpectationsDto> expectations, String contextKey)
			throws JSONException, NoSuchAlgorithmException, IOException {

		String bdbString = "";
		String[] duplicateBdbs;

		loadServerContextProperties(contextKey);
		for (MockABISExpectationsDto expct : expectations) {

			ResidentModel persona = ResidentModel.readPersona(expct.getPersonaPath());

			List<String> modalities = expct.getModalities();
			List<MDSDeviceCaptureModel> capFingers = persona.getBiometric().getCapture()
					.get(DataProviderConstants.MDS_DEVICE_TYPE_FINGER);
			List<MDSDeviceCaptureModel> capFace = persona.getBiometric().getCapture()
					.get(DataProviderConstants.MDS_DEVICE_TYPE_FACE);
			List<MDSDeviceCaptureModel> capIris = persona.getBiometric().getCapture()
					.get(DataProviderConstants.MDS_DEVICE_TYPE_IRIS);
			List<String> subTypeBdbStr = new ArrayList<String>();
			if (modalities != null) {
				for (String m : modalities) {
					if (m.toLowerCase().contains("finger") || m.toLowerCase().contains("right thumb")
							|| m.toLowerCase().contains("left thumb")) {
						for (int i = 0; i < capFingers.size(); i++) {
							MDSDeviceCaptureModel mds = capFingers.get(i);
							if (mds.getBioSubType().equals(m)) {
								bdbString = capFingers.get(i).getBioValue();
								subTypeBdbStr.add(bdbString);
								RestClient.logInfo(contextKey, MODALITY + m);
								break;
							}
						}
					} else if (m.toLowerCase().contains("iris") || m.toLowerCase().contains("left")
							|| m.toLowerCase().contains("right")) {
						for (int i = 0; i < capIris.size(); i++) {
							MDSDeviceCaptureModel mds = capIris.get(i);
							if (mds.getBioSubType().equals(m)) {
								bdbString = capIris.get(i).getBioValue();
								subTypeBdbStr.add(bdbString);
								RestClient.logInfo(contextKey, MODALITY + m);
								break;
							}
						}

					} else if (m.toLowerCase().contains("face")) {
						bdbString = capFace.get(0).getBioValue();
						subTypeBdbStr.add(bdbString);
						RestClient.logInfo(contextKey, MODALITY + m);
					}

				}

			} else {
				bdbString = capFingers.get(0).getBioValue();
				RestClient.logInfo(contextKey, "else part -->bdbString : " + bdbString);
				subTypeBdbStr.add(bdbString);
			}

			if (expct.isDuplicate()) {
				List<String> refHashs = expct.getRefHashs();
				if (refHashs != null && refHashs.size() > 0) {
					duplicateBdbs = new String[refHashs.size()];
					for (int i = 0; i < duplicateBdbs.length; i++)
						duplicateBdbs[i] = refHashs.get(i);
				} else {
					duplicateBdbs = new String[2];
					duplicateBdbs[0] = capFingers.get(1).getBioValue();
					duplicateBdbs[1] = capFingers.get(2).getBioValue();
				}
			} else
				duplicateBdbs = null;
			List<String> reponse = new ArrayList<>();
			for (String b : subTypeBdbStr) {
				String responseStr = MosipDataSetup.configureMockABISBiometric(b, expct.isDuplicate(), duplicateBdbs,
						(expct.getDelaySec() <= 0 ? DataProviderConstants.DEFAULT_ABIS_DELAY : expct.getDelaySec()),
						expct.getOperation(), contextKey, expct.getStatusCode(), expct.getFailureReason());
				reponse.add(responseStr);
			}
			RestClient.logInfo(contextKey, String.join(", ", reponse));
		}

		return STATUS_SUCCESS;
	}

	public String updateMachine(MosipMachineModel machine, String contextKey) {
		loadServerContextProperties(contextKey);
		MosipDataSetup.updateMachine(machine, contextKey);
		return STATUS_SUCCESS;
	}

	public String updatePreRegistrationStatus(String preregId, String statusCode, String contextKey) {
		loadServerContextProperties(contextKey);
		String status = MosipDataSetup.updatePreRegStatus(preregId, statusCode, contextKey);
		return status;
	}

	public String updatePreRegAppointment(String preregId, String contextKey) {
		String status = PreRegistrationSteps.updatePreRegAppointment(preregId, contextKey);
		return status;
	}

	public String deleteMockAbisExpectations(String contextKey) {

		return MosipDataSetup.deleteMockAbisExpectations(contextKey);

	}

}
