package io.mosip.test.packetcreator.mosippacketcreator.service;

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
import org.mosip.dataprovider.BiometricDataProvider;
import org.mosip.dataprovider.PacketTemplateProvider;
import org.mosip.dataprovider.PhotoProvider;
import org.mosip.dataprovider.ResidentDataProvider;
import org.mosip.dataprovider.models.AppointmentModel;
import org.mosip.dataprovider.models.AppointmentTimeSlotModel;
import org.mosip.dataprovider.models.BiometricDataModel;
import org.mosip.dataprovider.models.CenterDetailsModel;
import org.mosip.dataprovider.models.DynamicFieldValueModel;
import org.mosip.dataprovider.models.IrisDataModel;
import org.mosip.dataprovider.models.MosipDocTypeModel;
import org.mosip.dataprovider.models.MosipDocument;
import org.mosip.dataprovider.models.MosipIndividualTypeModel;
import org.mosip.dataprovider.models.ResidentModel;
import org.mosip.dataprovider.models.mds.MDSDeviceCaptureModel;
import org.mosip.dataprovider.models.setup.MosipMachineModel;
import org.mosip.dataprovider.preparation.MosipDataSetup;
import org.mosip.dataprovider.preparation.MosipMasterData;
import org.mosip.dataprovider.test.CreatePersona;
import org.mosip.dataprovider.test.ResidentPreRegistration;
import org.mosip.dataprovider.test.prereg.PreRegistrationSteps;
import org.mosip.dataprovider.util.CommonUtil;
import org.mosip.dataprovider.util.DataProviderConstants;
import org.mosip.dataprovider.util.Gender;
import org.mosip.dataprovider.util.ResidentAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.mosip.test.packetcreator.mosippacketcreator.dto.AppointmentDto;
import io.mosip.test.packetcreator.mosippacketcreator.dto.BioExceptionDto;
import io.mosip.test.packetcreator.mosippacketcreator.dto.MockABISExpectationsDto;
import io.mosip.test.packetcreator.mosippacketcreator.dto.PersonaRequestDto;
import io.mosip.test.packetcreator.mosippacketcreator.dto.PersonaRequestType;
import io.mosip.test.packetcreator.mosippacketcreator.dto.RidSyncReqResponseDTO;
import io.mosip.test.packetcreator.mosippacketcreator.dto.RidSyncRequestData;
import io.mosip.test.packetcreator.mosippacketcreator.dto.UpdatePersonaDto;
import variables.VariableManager;


@Service
public class PacketSyncService {

	private static final String UNDERSCORE = "_";
	private static final Logger logger = LoggerFactory.getLogger(PacketSyncService.class);

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

	//	@Value("${mosip.test.env.mapperpath}")
	//	private String mapperFilePath;

	@Value("${mosip.test.idrepo.idvidpath}")
	private String idvid;

	void loadServerContextProperties(String contextKey) {

		if(contextKey != null && !contextKey.equals("")) {

			Properties props = contextUtils.loadServerContext(contextKey);
			props.forEach((k,v)->{
				String key = k.toString().trim();
				String ns = VariableManager.NS_DEFAULT;

				if(!key.startsWith("mosip.test")) {


					VariableManager.setVariableValue(contextKey,key, v);
				}

			});
		}
	}

	//this will generate the requested number of resident data
	// Save the data in configured path as JSON
	// return list of resident Ids
	public String generateResidentData(int count,PersonaRequestDto residentRequestDto, String contextKey) {

		loadServerContextProperties(contextKey);
		VariableManager.setVariableValue(contextKey,"process", "NEW");
		Properties props = residentRequestDto.getRequests().get(PersonaRequestType.PR_ResidentAttribute);
		Gender enumGender = Gender.Any;
		ResidentDataProvider provider = new ResidentDataProvider();
		if(props.containsKey("Gender")) {
			enumGender = Gender.valueOf( props.get("Gender").toString()); //Gender.valueOf(residentRequestDto.getGender());
		}
		provider.addCondition(ResidentAttribute.RA_Count, count);

		if(props.containsKey("Age")) {

			provider.addCondition(ResidentAttribute.RA_Age, ResidentAttribute.valueOf(props.get("Age").toString()));
		}
		else
			provider.addCondition(ResidentAttribute.RA_Age, ResidentAttribute.RA_Adult);

		if(props.containsKey("SkipGaurdian")) {
			provider.addCondition(ResidentAttribute.RA_SKipGaurdian, props.get("SkipGaurdian"));
		}
		provider.addCondition(ResidentAttribute.RA_Gender, enumGender);

		String primaryLanguage = "eng";
		if(props.containsKey("PrimaryLanguage")) {
			primaryLanguage = props.get("PrimaryLanguage").toString();
			provider.addCondition(ResidentAttribute.RA_PRIMARAY_LANG, primaryLanguage);
		}



		if(props.containsKey("SecondaryLanguage")) {
			provider.addCondition(ResidentAttribute.RA_SECONDARY_LANG, props.get("SecondaryLanguage").toString());
		}
		if(props.containsKey("Finger")) {
			provider.addCondition(ResidentAttribute.RA_Finger, Boolean.parseBoolean(props.get("Finger").toString()));
		}
		if(props.containsKey("Iris")) {
			provider.addCondition(ResidentAttribute.RA_Iris, Boolean.parseBoolean(props.get("Iris").toString()));
		}
		if(props.containsKey("Face")) {
			provider.addCondition(ResidentAttribute.RA_Photo, Boolean.parseBoolean(props.get("Face").toString()));
		}
		if(props.containsKey("Document")) {
			provider.addCondition(ResidentAttribute.RA_Document, Boolean.parseBoolean(props.get("Document").toString()));
		}
		if(props.containsKey("Invalid")) {
			List<String> invalidList = Arrays.asList(props.get("invalid").toString().split(",", -1));
			provider.addCondition(ResidentAttribute.RA_InvalidList, invalidList);
		}
		if(props.containsKey("Miss")) {

			List<String> missedList = Arrays.asList(props.get("Miss").toString().split(",", -1));
			provider.addCondition(ResidentAttribute.RA_MissList, missedList);
			logger.info("before Genrate: missthese:" + missedList.toString());
		}
		if(props.containsKey("ThirdLanguage")) {

			provider.addCondition(ResidentAttribute.RA_THIRD_LANG, props.get("ThirdLanguage").toString());
		}
		if(props.containsKey("SchemaVersion")) {

			provider.addCondition(ResidentAttribute.RA_SCHEMA_VERSION, props.get("SchemaVersion").toString());
		}

		logger.info("before Genrate");
		List<ResidentModel> lst = provider.generate(contextKey);
		logger.info("After Genrate");

		//ObjectMapper Obj = new ObjectMapper();
		JSONArray outIds = new JSONArray();

		try {
			String tmpDir;

			tmpDir = Files.createTempDirectory("residents_").toFile().getAbsolutePath();

			for(ResidentModel r: lst) {
				Path tempPath = Path.of(tmpDir, r.getId() +".json");
				r.setPath(tempPath.toString());

				String jsonStr = r.toJSONString();

				Files.write(tempPath, jsonStr.getBytes());

				JSONObject id  = new JSONObject();
				id.put("id", r.getId());
				id.put("path", tempPath.toFile().getAbsolutePath());
				outIds.put(id);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		JSONObject response = new JSONObject();
		response.put("status", "SUCCESS");
		response.put("response",outIds);
		return response.toString();
		//"{\"status\":\"SUCCESS\"}";
	}
	public JSONObject makePacketAndSync(String preregId, String templateLocation, String personaPath,String contextKey,String additionalInfoReqId) throws Exception {

		logger.info("makePacketAndSync for PRID : {}", preregId);

		Path idJsonPath = null;
		Path docPath = null;
		preregId = preregId.trim();
		if(!preregId.equals("0") && !preregId.equals("01")  ) {
			String location = preregSyncService.downloadPreregPacket( preregId, contextKey);
			logger.info("Downloaded the prereg packet in {} ", location);
			File targetDirectory = Path.of(preregSyncService.getWorkDirectory(), preregId).toFile();
			if(!targetDirectory.exists()  && !targetDirectory.mkdir())
				throw new Exception("Failed to create target directory ! PRID : " + preregId);

			if(!zipUtils.unzip(location, targetDirectory.getAbsolutePath()))
				throw new Exception("Failed to unzip pre-reg packet >> " + preregId);

			idJsonPath = Path.of(targetDirectory.getAbsolutePath(), "ID.json");
					/*
			 * String idJsonContent = new String(Files.readAllBytes(idJsonPath)); JSONObject
			 * json = new JSONObject(idJsonContent); json=json.getJSONObject("identity");
			 * 
			 * String path = targetDirectory.getAbsolutePath(); docPath =
			 * Files.createDirectories(Paths.get(path + "tempDir")); try {
			 * FileUtils.copyDirectory(new File(path), docPath.toFile()); //
			 * Files.delete(Paths.get(docPath+"/ID.json")); for (File f :
			 * docPath.toFile().listFiles()) { if (f.getName().startsWith("POE_Passport") ||
			 * f.getName().startsWith("ID.json")) f.delete(); } } catch (IOException e) {
			 * e.printStackTrace(); }
			 */




			logger.info("Unzipped the prereg packet {}, ID.json exists : {}", preregId, idJsonPath.toFile().exists());

		}
		else
		{

			if(templateLocation != null) {
				//get idJson From Template itself
				idJsonPath = ContextUtils.idJsonPathFromTemplate(src, templateLocation);
			}
			else
				idJsonPath = createIDJsonFromPersona(personaPath, contextKey);

		}
		if(templateLocation != null) {
			process = ContextUtils.ProcessFromTemplate(src, templateLocation);
		}
		String packetPath = packetMakerService.createContainer(docPath,idJsonPath.toString(),templateLocation,src,process,preregId, contextKey, true,additionalInfoReqId);

		logger.info("Packet created : {}", packetPath);

		String response = packetSyncService.syncPacketRid(packetPath, "dummy", "APPROVED",
				"dummy", null, contextKey,additionalInfoReqId);

		logger.info("RID Sync response : {}", response);
		JSONObject functionResponse = new JSONObject();
		JSONObject nobj = new JSONObject();

		JSONArray packets =  new JSONArray(response);
		if(packets.length() > 0) {
			JSONObject resp = (JSONObject) packets.get(0);
			if(resp.getString("status").equals("SUCCESS")) {
				//RID Sync response : [{"registrationId":"10010100241000120201214134111","status":"SUCCESS"}]
				String rid = resp.getString("registrationId");
				response =  packetSyncService.uploadPacket(packetPath, contextKey);
				logger.info("Packet Sync response : {}", response);
				JSONObject obj =  new JSONObject(response);
				if(obj.getString("status").equals("Packet has reached Packet Receiver")) {

					//{"status":"Packet has reached Packet Receiver"}

					functionResponse.put("response", nobj );
					nobj.put("status", "SUCCESS");
					nobj.put("registrationId", rid);
					return functionResponse;
				}
			}
		}
		functionResponse.put("response", nobj );
		nobj.put("status", "Failed");
		//{"status": "Failed"} or {"status": "Passed"}  instead of "Failed"

return functionResponse;

		

	}

	public  Path createIDJsonFromPersona(String personaFile, String contextKey) throws IOException {

		loadServerContextProperties(contextKey);
		ResidentModel resident = ResidentModel.readPersona(personaFile);
		JSONObject jsonIdentity = CreatePersona.createIdentity(resident,null,contextKey);
		JSONObject jsonWrapper = new JSONObject();
		jsonWrapper.put("identity", jsonIdentity);

		logger.info(jsonWrapper.toString());
		String  tmpDir = Files.createTempDirectory("preregIds_").toFile().getAbsolutePath();
		Path tempPath = Path.of(tmpDir, resident.getId() + "_ID.json");
		Files.write(tempPath, jsonWrapper.toString().getBytes());

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
		for(Entry<Object, Object> entrySet:props.entrySet()) {
			if (entrySet.getKey().equals("mosip.test.regclient.centerid"))
				centerId = entrySet.getValue().toString();
			else if (entrySet.getKey().equals("mosip.test.regclient.machineid"))
				machineId = entrySet.getValue().toString();
		}

		// loadContext(contextKey);
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
				} else if (k.toString().equals("mosip.test.regclient.machineid")) {
					machineId = v.toString();
				} else if (k.toString().equals("mosip.test.primary.langcode")) {
					primaryLangCode = v.toString();
				} else if (k.toString().equals("mosip.test.regclient.centerid")) {
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
		// String rid =
		// container.getName(container.getNameCount()-1).toString().replace(".zip", "");
		// String rid =PacketMakerService.getRegIdFromPacketPath(containerFile);
		if (proc != null && !proc.equals(""))
			process = proc;
		logger.info("Syncing data for RID : {}", rid);
		logger.info("Syncing data: process:", process);

		JSONObject jsonObject = new JSONObject();
		jsonObject.put("registrationId", rid);
		jsonObject.put("langCode", primaryLangCode);
		jsonObject.put("name", name);
		jsonObject.put("email", "");
		jsonObject.put("phone", "");
		jsonObject.put("registrationType", process);

		byte[] fileBytes = Files.readAllBytes(container);

		jsonObject.put("packetHashValue", cryptoUtil.getHexEncodedHash(fileBytes));
		jsonObject.put("packetSize", fileBytes.length);
		jsonObject.put("supervisorStatus", supervisorStatus);
		jsonObject.put("supervisorComment", supervisorComment);

		if (mosipVersion != null && !mosipVersion.isEmpty() && mosipVersion.equals("1.2")) {
			String id = StringUtils.isNotBlank(additionalInfoReqId) ? additionalInfoReqId : rid;
			// String refId = centerId + "_" + machineId;
			/*
			 * String packetId = new StringBuilder() .append(id) .append("-") .append(refId)
			 * .append("-") .append(getcurrentTimeStamp()) .toString();
			 */
			String packetId = (container.getName(container.getNameCount() - 1).toString()).replace(".zip", "");

			jsonObject.put("packetId", packetId);
			jsonObject.put("additionalInfoReqId", id);
			// syncapi=syncapi+"V2";
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

		if(contextKey != null && !contextKey.equals("")) {

			Properties props = contextUtils.loadServerContext(contextKey);
			props.forEach((k,v)->{
				if(k.toString().equals("mosip.test.packet.uploadapi")) {

					uploadapi = v.toString();

				}
				else
					if(k.toString().equals("mosip.test.baseurl")) {
						baseUrl = v.toString();
					}	
			});
		}
		logger.info(baseUrl+uploadapi +",path="+ path);
		JSONObject response = apiRequestUtil.uploadFile(baseUrl, baseUrl+uploadapi, path, contextKey);
		return response.toString();
	}

	public String preRegisterResident(List<String> personaFilePath, String contextKey) throws IOException {
		StringBuilder builder = new StringBuilder();

		loadServerContextProperties(contextKey);

		for(String path: personaFilePath) {
			ResidentModel resident = ResidentModel.readPersona(path);
			String response = PreRegistrationSteps.postApplication(resident , null,contextKey);
			//preregid
			saveRegIDMap(response, path);
			builder.append(response);
		}
		return builder.toString();
	}
	public String updateResidentApplication(String personaFilePath,String preregId, String contextKey) throws IOException {

		loadServerContextProperties(contextKey);
		ResidentModel resident = ResidentModel.readPersona(personaFilePath);
		return PreRegistrationSteps.putApplication(resident,preregId,contextKey);


	}

	public String preRegisterGetApplications(String status,String preregId,String contextKey) {
		loadServerContextProperties(contextKey);
		logger.debug("preRegisterGetApplications preregId=" + preregId);
		return PreRegistrationSteps.getApplications(status,preregId,contextKey);
	}
	void saveRegIDMap(String preRegId, String personaFilePath) {

		Properties p=new Properties();
		try {
			FileReader reader=new FileReader(preRegMapFile);  
			p.load(reader);

		}catch (IOException e) {
			// TODO: handle exception
			logger.error("saveRegIDMap " + e.getMessage());
		}
		p.put(preRegId,  personaFilePath);
		try {

			p.store(new FileWriter(preRegMapFile),"PreRegID to persona mapping file");  
		}catch (IOException e) {
			logger.error("saveRegIDMap " + e.getMessage());
		}
	}
	String getPersona(String preRegId) {
		try {
			FileReader reader=new FileReader(preRegMapFile);  
			Properties p=new Properties();  
			p.load(reader);
			return p.getProperty(preRegId);
		} catch(IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	public String requestOtp(List<String> personaFilePath, String to, String contextKey) throws IOException {
		StringBuilder builder = new StringBuilder();

		loadServerContextProperties(contextKey);

		for(String path: personaFilePath) {
			ResidentModel resident = ResidentModel.readPersona(path);
			ResidentPreRegistration preReg = new ResidentPreRegistration(resident);
			builder.append(preReg.sendOtpTo(to,contextKey));

		}
		return builder.toString();
	}
	public String verifyOtp(String personaFilePath, String to, String otp, String contextKey) throws IOException {

		loadServerContextProperties(contextKey);
		ResidentModel resident = ResidentModel.readPersona(personaFilePath);
		ResidentPreRegistration preReg = new ResidentPreRegistration(resident);

		preReg.fetchOtp(contextKey);
		return preReg.verifyOtp(to,otp,contextKey);

	}
	public String getAvailableAppointments(String contextKey) {
		loadServerContextProperties(contextKey);
		AppointmentModel res = PreRegistrationSteps.getAppointments(contextKey);
		return res.toJSONString();
	}
	public String bookSpecificAppointment(String preregId,AppointmentDto appointmentDto, String contextKey) {

		AppointmentTimeSlotModel ts = new AppointmentTimeSlotModel();
		ts.setFromTime(appointmentDto.getTime_slot_from());
		ts.setToTime(appointmentDto.getTime_slot_to());

		return PreRegistrationSteps.bookAppointment(preregId,appointmentDto.getAppointment_date(),
				Integer.parseInt(appointmentDto.getRegistration_center_id()),
				ts,contextKey);

	}

	public String bookAppointment( String preRegID,int nthSlot, String contextKey) {

		String retVal= "{\"Failed\"}";
		Boolean bBooked = false;

		loadServerContextProperties(contextKey);

		String base = VariableManager.getVariableValue(contextKey,"urlBase").toString().trim();
		String api = VariableManager.getVariableValue(VariableManager.NS_DEFAULT,"appointmentslots").toString().trim();
		String centerId = VariableManager.getVariableValue( contextKey,"centerId").toString().trim();
		logger.info("BookAppointment:" + base +","+ api + ","+centerId);

		AppointmentModel res = PreRegistrationSteps.getAppointments(contextKey);

		for( CenterDetailsModel a: res.getAvailableDates()) {
			if(!a.getHoliday()) {
				for(AppointmentTimeSlotModel ts: a.getTimeslots()) {
					if(ts.getAvailability() > 0) {
						nthSlot--;
						if(nthSlot ==0) {
							retVal =PreRegistrationSteps.bookAppointment(preRegID,a.getDate(),res.getRegCenterId(),ts, contextKey);
							bBooked = true;

							break;
						}
					}
				}
			}
			if(bBooked) break;
		}
		return retVal;
	}
	/*
	 * Book appointment on any specified slot
	 * nThSlot -> min 1
	 */
	public String bookAppointmentSlot( String preRegID,int nthSlot,boolean bHoliday, String contextKey) {

		String retVal= "{\"Failed\"}";
		Boolean bBooked = false;

		loadServerContextProperties(contextKey);


		AppointmentModel res = PreRegistrationSteps.getAppointments(contextKey);

		for( CenterDetailsModel a: res.getAvailableDates()) {
			//if specified book on a holiday
			if(bHoliday) {
				if(a.getHoliday()) {
					for(AppointmentTimeSlotModel ts: a.getTimeslots()) {

						nthSlot--;
						if(nthSlot ==0) {
							retVal =PreRegistrationSteps.bookAppointment(preRegID,a.getDate(),res.getRegCenterId(),ts,contextKey);
							bBooked = true;
							break;
						}
					}
					if(bBooked)
						break;
					else
						continue;
				}
			}

			if(!a.getHoliday()) {
				for(AppointmentTimeSlotModel ts: a.getTimeslots()) {

					nthSlot--;
					if(nthSlot ==0) {
						retVal =PreRegistrationSteps.bookAppointment(preRegID,a.getDate(),res.getRegCenterId(),ts,contextKey);
						bBooked = true;
						break;

					}
				}
			}
			if(bBooked) break;
		}
		return retVal;
	}
	public String cancelAppointment(String preregId, AppointmentDto appointmentDto, String contextKey) {
		loadServerContextProperties(contextKey);

		return PreRegistrationSteps.cancelAppointment(preregId,
				appointmentDto.getTime_slot_from(),
				appointmentDto.getTime_slot_to(),
				appointmentDto.getAppointment_date(),
				appointmentDto.getRegistration_center_id(),contextKey
				);


	}
	public String deleteApplication(String preregId, String contextKey) {
		loadServerContextProperties(contextKey);
		return PreRegistrationSteps.deleteApplication(preregId,contextKey); 	
	}

	public String discardBooking(
			HashMap<String, String> map,String contextKey) {

		return PreRegistrationSteps.discardBooking(map,contextKey); 	
	}


	public String uploadDocuments(String personaFilePath, String preregId, String contextKey) throws IOException {

		String response = "";

		loadServerContextProperties(contextKey);
		ResidentModel resident = ResidentModel.readPersona(personaFilePath);

		//System.out.println("uploadProof " + docCategory);

		for(MosipDocument a: resident.getDocuments()) {
			JSONObject respObject = PreRegistrationSteps.UploadDocument(a.getDocCategoryCode(),
					// a.getType().get(0).getCode(),
					a.getType().get(0).getDocTypeCode(),
					a.getDocCategoryLang(), a.getDocs().get(0) ,preregId,contextKey);
			if(respObject != null)
				response = response + respObject.toString();
		}

		return response;
	}

	public String createPacket(PersonaRequestDto personaRequest, String process, String preregId, String contextKey) throws IOException {

		Path packetDir = null;
		JSONArray packetPaths = new JSONArray();

		//loadServerContextProperties(contextKey);

		packetDir = Files.createTempDirectory("packets_");
		Properties personaFiles = personaRequest.getRequests().get(PersonaRequestType.PR_ResidentList);
		Properties options = personaRequest.getRequests().get(PersonaRequestType.PR_Options);


		List<Object> lstObjects = Arrays.asList(personaFiles.values().toArray());
		List<String> personaFilePaths =  new ArrayList<String>();
		for(Object o: lstObjects) {
			personaFilePaths.add( o.toString());
		}


		if(!packetDir.toFile().exists()) {
			packetDir.toFile().createNewFile();
		}
		PacketTemplateProvider packetTemplateProvider = new PacketTemplateProvider();

		for(String path: personaFilePaths) {
			ResidentModel resident = ResidentModel.readPersona(path);
			String packetPath = packetDir.toString()+File.separator + resident.getId();

			Properties props = contextUtils.loadServerContext(contextKey);
			packetTemplateProvider.generate("registration_client", process, resident, packetPath,preregId,machineId, centerId,contextKey,props,new JSONObject());

			JSONObject obj = new JSONObject();
			obj.put("id",resident.getId());
			obj.put("path", packetPath);
			logger.info("createPacket:" + packetPath);
			packetPaths.put(obj);


		}
		JSONObject response = new JSONObject();
		response.put("packets", packetPaths);
		return response.toString();



	}
	public String createPacketTemplates(List<String> personaFilePaths, String process, String outDir,String preregId, String contextKey) throws IOException {


		Path packetDir = null;
		JSONArray packetPaths = new JSONArray();

		logger.info("createPacket->outDir:" + outDir);


		loadServerContextProperties(contextKey);
		//VariableManager.setVariableValue(contextKey,"mosip.test.env.mapperpath", mapperFilePath);
		if(process != null) {
			VariableManager.setVariableValue(contextKey,"process", process);
		}
		if(outDir == null || outDir.trim().equals("")) {
			packetDir = Files.createTempDirectory("packets_");
		}
		else
		{
			packetDir = Paths.get(outDir);
		}
		if(!packetDir.toFile().exists()) {
			packetDir.toFile().createNewFile();
		}
		PacketTemplateProvider packetTemplateProvider = new PacketTemplateProvider();

		try {
			JSONObject preregResponse=new JSONObject();
			JSONObject queryparam=new JSONObject();
			Properties props = contextUtils.loadServerContext(contextKey);
			if(props.containsKey("mosip.test.regclient.officerBiometricFileName")) {
				queryparam.put("type", "bio");
				String uin=props.getProperty("validUIN");
				baseUrl=props.getProperty("urlBase");
				preregResponse = apiRequestUtil.getJsonObject(baseUrl,baseUrl + idvid+uin,queryparam,new JSONObject(),contextKey);
			}
			for(String path: personaFilePaths) {
				ResidentModel resident = ResidentModel.readPersona(path);
				String packetPath = packetDir.toString()+File.separator + resident.getId();

				machineId=VariableManager.getVariableValue(contextKey,"mosip.test.regclient.machineid").toString();

				centerId=VariableManager.getVariableValue(contextKey,"mosip.test.regclient.centerid").toString();

				packetTemplateProvider.generate("registration_client", process, resident, packetPath , preregId, machineId, centerId,contextKey,props,preregResponse);
				JSONObject obj = new JSONObject();
				obj.put("id",resident.getId());
				obj.put("path", packetPath);
				logger.info("createPacket:" + packetPath);
				packetPaths.put(obj);

			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		JSONObject response = new JSONObject();
		response.put("packets", packetPaths);
		return response.toString();



	}
	public String preRegToRegister( String templatePath, String preRegId,String personaPath, String contextKey,String additionalInfoReqId) throws Exception {

		return makePacketAndSync(preRegId, templatePath, personaPath,contextKey,additionalInfoReqId).toString();


	}
	void updatePersona(Properties updateAttrs, ResidentModel persona) {
		Iterator<Object> it = updateAttrs.keys().asIterator();
		BiometricDataModel bioData = null;

		while(it.hasNext()) {
			String key = it.next().toString();
			String value  = updateAttrs.getProperty(key);
			key = key.toLowerCase().trim();

			//first check whether it is document being updated?

			MosipDocument doc = null;
			for(MosipDocument md: persona.getDocuments()) {
				if(md.getDocCategoryCode().toLowerCase().equals(key) || md.getDocCategoryName().equals(key)) {
					doc = md;
					break;
				}

			}
			if(doc != null) {
				JSONObject jsonDoc = new JSONObject(value);
				String typeName = jsonDoc.has("typeName") ? jsonDoc.get("typeName").toString() : "";
				String typeCode = jsonDoc.has("typeCode") ? jsonDoc.get("typeCode").toString() : "";
				int indx = -1;
				for(MosipDocTypeModel tm: doc.getType()) {
					indx++;
					if( (tm.getDocTypeCode()!=null && tm.getDocTypeCode().equals(typeCode)) || (tm.getDocTypeName()!=null && tm.getDocTypeName().equals(typeName)) )
						break;
				}
				if(indx >=0 && indx < doc.getType().size()) {
					String docFilePath = jsonDoc.has("docPath") ? jsonDoc.getString("docPath").toString() : null;
					if(docFilePath != null)
						doc.getDocs().set(indx, docFilePath);
				}
				continue;

			}
			switch(key) {
			case "face":
			case "photo":
				bioData =persona.getBiometric();
				byte[][] faceData = PhotoProvider.loadPhoto(value );
				//bioData.setEncodedPhoto(Base64.encodeBase64String(faceData[0]));
				bioData.setEncodedPhoto(Base64.encodeBase64URLSafeString(faceData[0]));
				bioData.setRawFaceData(faceData[1]);

				try {
					bioData.setFaceHash(CommonUtil.getHexEncodedHash( faceData[1]));
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					//e1.printStackTrace();
				}

				break;
			case "left_iris":
				bioData =persona.getBiometric();
				IrisDataModel im = bioData.getIris();
				IrisDataModel imUpdated = null;
				try {
					imUpdated = BiometricDataProvider.loadIris(value, "left", im);
					if(imUpdated != null)
						persona.getBiometric().setIris(imUpdated);

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				break;
			case "right_iris":
				bioData =persona.getBiometric();
				IrisDataModel im1 = bioData.getIris();
				IrisDataModel imUpdated1 = null;
				try {
					imUpdated1 = BiometricDataProvider.loadIris(value, "right", im1);
					if(imUpdated1 != null)
						persona.getBiometric().setIris(imUpdated1);

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				break;
				/*	
	    		case "firstname":
	    			persona.getName().setFirstName(value);
	    			break;
	    		case "midname":
	    			persona.getName().setMidName(value);
	    			break;

	    		case "lastname":
	    		case "surname":
	    			persona.getName().setSurName(value);
	    			break;
				 */	
			case "gender":
				persona.setGender(Gender.valueOf(value));
				break;
				/*
	    		case "phone":
	    		case "mobile":
	    		case "mobilephone":
	    		case "mobilenumber":
	    			persona.getContact().setMobileNumber(value);

	    			break;
				 */
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

				DynamicFieldValueModel bg =  persona.getBloodgroup();
				bg.setCode(value);
				break;
			case "maritalstatus":
			case "ms":
				DynamicFieldValueModel ms =  persona.getMaritalStatus();
				ms.setCode(value);
				break;
			case "residencestatus":
			case "rs":
				if(value != null && !value.equals("")) {
					String lang = persona.getPrimaryLanguage();
					String [] parts = value.split("=");
					String msCode = null;
					if(parts.length > 1) {
						lang = parts[0].trim();
						msCode = parts[1].trim();
					}
					else
						msCode = parts[0].trim();

					if(lang.equals(persona.getPrimaryLanguage())) {

						MosipIndividualTypeModel  rs= persona.getResidentStatus();
						rs.setCode(msCode);
					}
					else
					{
						MosipIndividualTypeModel  rs= persona.getResidentStatus_seclang();
						rs.setCode(msCode);
					}
				}
				break;
			default://Added by VS to passthrough attributes
				persona.getAddtionalAttributes().put(key, value);
				break;

			}
		}
	}
	public String getPersonaData(List<UpdatePersonaDto> getPersonaRequest,String contextKey) throws Exception {


		Properties retProp = new Properties();

		for(UpdatePersonaDto req: getPersonaRequest) {

			ResidentModel persona = ResidentModel.readPersona(req.getPersonaFilePath());
			List<String> retrieveAttrs = req.getRetriveAttributeList();
			if(retrieveAttrs != null) {
				for(String attr: retrieveAttrs) {
					Object val = null;
					String key = attr.trim();
					switch(key.toLowerCase()) {
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
						if(persona.getBiometric().getCapture() != null){
							val = persona.getBiometric().getCapture().get(DataProviderConstants.MDS_DEVICE_TYPE_FACE).get(0).getBioValue();
						}
						retProp.put(key, val);
						break;
					case "iris":
						IrisDataModel irisval = persona.getBiometric().getIris();

						retProp.put(key, irisval.toJSONString());
						break;
					case "iris_encrypted":
						IrisDataModel irisvalue = null;
						//String strval = "";
						if(persona.getBiometric().getCapture() != null) {
							irisvalue = new IrisDataModel();

							List<MDSDeviceCaptureModel> lstIrisData =persona.getBiometric().getCapture().get(DataProviderConstants.MDS_DEVICE_TYPE_IRIS);
							for(MDSDeviceCaptureModel cm: lstIrisData) {

								if(cm.getBioSubType().equals("Left"))
									irisvalue.setLeft(cm.getBioValue());
								else
									if(cm.getBioSubType().equals("Right"))
										irisvalue.setRight(cm.getBioValue());
							}
							val = irisvalue; //.toJSONString();
						}

						retProp.put(key, val );
						break;	
					case "finger":
						String [] fps = persona.getBiometric().getFingerPrint();
						for(int i=0;  i < fps.length; i++) {
							retProp.put(DataProviderConstants.displayFingerName[i] , fps[i]);
						}
						break;
					case "finger_encrypted":

						if(persona.getBiometric().getCapture() != null) {

							List<MDSDeviceCaptureModel> lstFingerData =persona.getBiometric().getCapture().get(DataProviderConstants.MDS_DEVICE_TYPE_FINGER);
							for(MDSDeviceCaptureModel cm: lstFingerData) {
								retProp.put(	cm.getBioSubType() , cm.getBioValue());
							}
						}
						break;
					case "fingerraw":
						byte[][] fpsraw = persona.getBiometric().getFingerRaw();
						for(int i=0;  i < fpsraw.length; i++) {
							retProp.put(DataProviderConstants.displayFingerName[i] , fpsraw[i]);
						}
						break;
					case "finger_hash":
					{
						if(persona.getBiometric().getCapture() != null) {

							List<MDSDeviceCaptureModel> lstFingerData =persona.getBiometric().getCapture().get(DataProviderConstants.MDS_DEVICE_TYPE_FINGER);
							for(MDSDeviceCaptureModel cm: lstFingerData) {
								//retProp.put(	cm.getBioSubType() , CommonUtil.getSHA(cm.getBioValue()));
								byte[] valBytes=java.util.Base64.getUrlDecoder().decode(cm.getBioValue());
								retProp.put(	cm.getBioSubType() , CommonUtil.getSHAFromBytes(valBytes));
							}
						}
					}
					break;
					case "iris_hash":
						IrisDataModel irisvalueh = null;
						//String strval = "";
						if(persona.getBiometric().getCapture() != null) {
							irisvalueh = new IrisDataModel();

							List<MDSDeviceCaptureModel> lstIrisData =persona.getBiometric().getCapture().get(DataProviderConstants.MDS_DEVICE_TYPE_IRIS);
							for(MDSDeviceCaptureModel cm: lstIrisData) {

								if(cm.getBioSubType().equals("Left")) {
									//irisvalueh.setLeft(CommonUtil.getSHA( cm.getBioValue()));
									byte[] valBytes=java.util.Base64.getUrlDecoder().decode(cm.getBioValue());
									irisvalueh.setLeft(CommonUtil.getSHAFromBytes(valBytes));
								}
								else
									if(cm.getBioSubType().equals("Right")) {
										//irisvalueh.setRight(CommonUtil.getSHA( cm.getBioValue()));
										byte[] valBytes=java.util.Base64.getUrlDecoder().decode(cm.getBioValue());
										irisvalueh.setRight(CommonUtil.getSHAFromBytes(valBytes));
									}
							}
							val = irisvalueh; //.toJSONString();
						}

						retProp.put(key, val );
						break;	
					case "face_hash":
						if(persona.getBiometric().getCapture() != null){
							val = persona.getBiometric().getCapture().get(DataProviderConstants.MDS_DEVICE_TYPE_FACE).get(0).getBioValue();
							byte[] valBytes=java.util.Base64.getUrlDecoder().decode(val.toString());
							//val = CommonUtil.getSHA(new String(valBytes));
							val = CommonUtil.getSHAFromBytes(valBytes);
						}
						retProp.put(key, val);
						break;	
					case "address":
						JSONObject resp = new JSONObject();
						String secLang = persona.getSecondaryLanguage();
						String[] addr = persona.getAddress();

						if(secLang != null) {
							String[] addr_sec = persona.getAddress_seclang();
							for(int i=0; i < 3; i++) {	
								JSONArray addrJson = new JSONArray();
								JSONObject lineJson = new JSONObject();
								lineJson.put("language", persona.getPrimaryLanguage());
								lineJson.put("value", addr[i]);
								addrJson.put( lineJson);
								lineJson = new JSONObject();
								lineJson.put("language", persona.getSecondaryLanguage());
								lineJson.put("value", addr_sec[i]);
								addrJson.put( lineJson);
								resp.put("addressLine"+(i+1), addrJson);
							}	
						}
						else
						{
							for(int i=0; i < 3; i++) {	
								resp.put("addressLine"+(i+1), addr[i]);
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
		//throw new Exception("TODO: Implement");
		//return "";
	}

	public String updatePersonaData(List<UpdatePersonaDto> updatePersonaRequest,String contextKey) throws Exception {
		String ret ="{Sucess}";
		for(UpdatePersonaDto req: updatePersonaRequest) {
			try {
				ResidentModel persona = ResidentModel.readPersona(req.getPersonaFilePath());
				List<String> regenAttrs = req.getRegenAttributeList();
				if(regenAttrs != null) {
					for(String attr: regenAttrs) {
						ResidentDataProvider.updateBiometric(persona, attr,contextKey);

					}
				}
				Properties updateAttrs = req.getUpdateAttributeList();
				if(updateAttrs != null ) {
					updatePersona(updateAttrs, persona);
				}
				List<String> missList = req.getMissAttributeList();
				if(missList != null && missList.size() >=0)
					persona.setMissAttributes(missList);

				persona.writePersona(req.getPersonaFilePath());

			} catch (IOException e) {
				logger.error("updatePersonaData:"+ e.getMessage());
				//e.printStackTrace();
			}

		}
		return ret;
	}
	public String updateResidentData(Hashtable<PersonaRequestType, Properties> hashtable , String uin, String rid) throws IOException {

		Properties list = hashtable.get(PersonaRequestType.PR_ResidentList);

		String filePathResident =null;
		String filePathParent = null;
		ResidentModel persona = null;
		ResidentModel guardian = null;

		for(Object key: list.keySet()) {
			String keyS = key.toString().toLowerCase();
			if(keyS.startsWith("uin")) {
				filePathResident = list.get(key).toString();
				persona = ResidentModel.readPersona(filePathResident);
				persona.setUIN(uin);
			}
			else
				if(keyS.toString().startsWith("rid")) {
					filePathResident = list.get(key).toString();
					persona = ResidentModel.readPersona(filePathResident);
					persona.setRID(rid);
				}
				else
					if(keyS.toString().startsWith("child")) {
						filePathResident = list.get(key).toString();
						persona = ResidentModel.readPersona(filePathResident);
					}
					else
						if(keyS.startsWith("guardian")) {
							filePathParent = list.get(key).toString();
							guardian = ResidentModel.readPersona(filePathParent);
						}   		
		}
		if(guardian != null)
			persona.setGuardian(guardian);

		Files.write (Paths.get(filePathResident), persona.toJSONString().getBytes());
		return "{\"response\":\"SUCCESS\"}";
	}

	public String updatePersonaBioExceptions(BioExceptionDto personaBERequestDto, String contextKey) {

		logger.info("updatePersonaBioExceptions:"+contextKey);

		loadServerContextProperties(contextKey);
		String ret ="{Sucess}";
		try {
			ResidentModel persona = ResidentModel.readPersona(personaBERequestDto.getPersonaFilePath());

			persona.setBioExceptions(personaBERequestDto.getExceptions());

			persona.writePersona(personaBERequestDto.getPersonaFilePath());
		}catch(Exception e) {
			logger.error("updatePersonaBioExceptions:"+ e.getMessage());
		}
		return null;
	}

	public String bulkuploadPackets(List<String> packetPaths, String contextKey) {

		loadServerContextProperties(contextKey);

		return MosipDataSetup.uploadPackets( packetPaths,contextKey);


	}

	public String setPersonaMockABISExpectation(List<String> personaFilePath, boolean bDuplicate, String contextKey) throws JSONException, NoSuchAlgorithmException, IOException {

		String bdbString ="";
		String [] duplicateBdbs;

		logger.info("setPersonaMockABISExpectation");

		loadServerContextProperties(contextKey);
		for(String personaPath: personaFilePath) {

			ResidentModel persona = ResidentModel.readPersona(personaPath);
			List<MDSDeviceCaptureModel> capDetails =  persona.getBiometric().getCapture().get(DataProviderConstants.MDS_DEVICE_TYPE_FINGER);
			bdbString = capDetails.get(0).getBioValue();

			/*
			 * "gallery": {
    "referenceIds": [
      {
        "referenceId": "<hash of biometric>"
      },
      {
        "referenceId": "<hash of biometric>"
      }
    ]
  }
			 */
			if(bDuplicate) {
				duplicateBdbs = new String[2];
				duplicateBdbs[0] =capDetails.get(1).getBioValue();
				duplicateBdbs[1] =capDetails.get(2).getBioValue();
			}
			else
				duplicateBdbs= null;
			MosipDataSetup.configureMockABISBiometric(bdbString, bDuplicate,duplicateBdbs, DataProviderConstants.DEFAULT_ABIS_DELAY, null ,contextKey);
		}
		return "{\"status\":\"Success\"}";
	}

	String getRegIdFromPacketPath(String packetPath) {
		//leaf node of packet path is regid
		return Path.of(packetPath).getFileName().toString();
	}
	public String validatePacket(String packetPath, String processArg, String contextKey) {

		JSONObject ret = new JSONObject();
		ret.put("status", "Success");
		loadServerContextProperties(contextKey);
		String regId = getRegIdFromPacketPath(packetPath);
		String tempPacketRootFolder = Path.of(packetPath).toString();
		String jsonSchema = MosipMasterData.getIDSchemaSchemaLatestVersion(contextKey);
		String processRoot =  Path.of(tempPacketRootFolder, src, process).toString();
		String packetRoot = Path.of(processRoot, "rid_id").toString();
		String identityJson = CommonUtil.readFromJSONFile(packetRoot + "/ID.json");
		try {

			CommonUtil.validateJSONSchema(jsonSchema, identityJson);

		}catch(ValidationException ex) {

			ret.put("status", "Error");
			ret.put("message", ex.getMessage());

		}
		return ret.toString();
	}

	public String setPersonaMockABISExpectationV2(List<MockABISExpectationsDto> expectations, String contextKey) throws JSONException, NoSuchAlgorithmException, IOException {

		String bdbString ="";
		String [] duplicateBdbs;

		loadServerContextProperties(contextKey);
		for(MockABISExpectationsDto expct : expectations) {

			ResidentModel persona = ResidentModel.readPersona(expct.getPersonaPath());

			List<String> modalities = expct.getModalities();
			List<MDSDeviceCaptureModel> capFingers =  persona.getBiometric().getCapture().get(DataProviderConstants.MDS_DEVICE_TYPE_FINGER);
			List<MDSDeviceCaptureModel> capFace =  persona.getBiometric().getCapture().get(DataProviderConstants.MDS_DEVICE_TYPE_FACE);
			List<MDSDeviceCaptureModel> capIris =  persona.getBiometric().getCapture().get(DataProviderConstants.MDS_DEVICE_TYPE_IRIS);
			List<String> subTypeBdbStr = new ArrayList<String>();
			if(modalities != null) {
				for(String m: modalities) {
					if(m.toLowerCase().contains("finger") || m.toLowerCase().contains("right thumb") || m.toLowerCase().contains("left thumb")) {
						//int pos = ISOConverter.getFingerPos(m.trim());
						for (int i = 0; i < capFingers.size(); i++) {
							MDSDeviceCaptureModel mds = capFingers.get(i);
							if (mds.getBioSubType().equals(m)) {
								bdbString = capFingers.get(i).getBioValue();
								subTypeBdbStr.add(bdbString);
								System.out.println("Modality : " + m);
								break;
							}
						}
					}
					else
						if(m.toLowerCase().contains("iris") || m.toLowerCase().contains("left") || m.toLowerCase().contains("right")) {
							for (int i = 0; i < capIris.size(); i++) {
								MDSDeviceCaptureModel mds = capIris.get(i);
								if (mds.getBioSubType().equals(m)) {
									bdbString = capIris.get(i).getBioValue();
									subTypeBdbStr.add(bdbString);
									System.out.println("Modality : " + m);
									break;
								}
							}
							/*
							 * 
							 * //int pos =
							 * m.toLowerCase().equals(capIris.get(0).getBioSubType().toLowerCase()) ? 0: 1;
							 * bdbString = capIris.get(0).getBioValue(); subTypeBdbStr.add(bdbString);
							 * System.out.println("Modality : "+m);
							 */

						}
						else
							if(m.toLowerCase().contains("face")) {
								bdbString = capFace.get(0).getBioValue();
								subTypeBdbStr.add(bdbString);
								System.out.println("Modality : "+m);
							}

				}

			}
			else {
				bdbString = capFingers.get(0).getBioValue();
				System.out.println("else part -->bdbString : "+bdbString);
				subTypeBdbStr.add(bdbString);
			}


			if(expct.isDuplicate()) {
				List<String> refHashs =expct.getRefHashs();
				if(refHashs != null && refHashs.size() > 0 ) {
					duplicateBdbs = new String[ refHashs.size()];
					for(int i=0; i < duplicateBdbs.length; i++)
						duplicateBdbs[i] = refHashs.get(i);
				}
				else {
					duplicateBdbs = new String[2];
					duplicateBdbs[0] =capFingers.get(1).getBioValue();
					duplicateBdbs[1] =capFingers.get(2).getBioValue();
				}
			}
			else
				duplicateBdbs= null;
			List<String> reponse= new ArrayList<>();
			for(String b:subTypeBdbStr ) {
				String responseStr=MosipDataSetup.configureMockABISBiometric(b, expct.isDuplicate(),duplicateBdbs,
						(expct.getDelaySec() <= 0 ?  DataProviderConstants.DEFAULT_ABIS_DELAY : expct.getDelaySec()),
						expct.getOperation(),contextKey);
				reponse.add(responseStr);
			}
			System.out.println(String.join(", ", reponse));
		}

		return "{\"status\":\"Success\"}";
	}

	public String updateMachine(MosipMachineModel machine,String contextKey) {
		loadServerContextProperties(contextKey);
		MosipDataSetup.updateMachine(machine,contextKey);
		return "{\"status\":\"Success\"}";
	}

	public String updatePreRegistrationStatus(String preregId,String statusCode,String contextKey) {
		loadServerContextProperties(contextKey);
		String status=MosipDataSetup.updatePreRegStatus(preregId,statusCode,contextKey);
		return status;
	}

	public String updatePreRegAppointment(String preregId,String contextKey) {
		String status=PreRegistrationSteps.updatePreRegAppointment(preregId,contextKey);
		return status;
	}

	public String deleteMockAbisExpectations(String id, String contextKey) {
		// TODO Auto-generated method stub
		
		MosipDataSetup.deleteMockAbisExpectations(id,contextKey);
		return null;
	}



}
