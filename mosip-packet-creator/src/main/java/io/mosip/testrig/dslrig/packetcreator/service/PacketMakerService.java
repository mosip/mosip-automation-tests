package io.mosip.testrig.dslrig.packetcreator.service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;
import javax.xml.bind.DatatypeConverter;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import io.mosip.testrig.dslrig.dataprovider.test.CreatePersona;
import io.mosip.testrig.dslrig.dataprovider.util.CommonUtil;
import io.mosip.testrig.dslrig.dataprovider.util.RestClient;
import io.mosip.testrig.dslrig.dataprovider.variables.VariableManager;

@Service
public class PacketMakerService {

	Logger logger = LoggerFactory.getLogger(PacketMakerService.class);

	private static final String UNDERSCORE = "_";
	private static final String PACKET_META_FILENAME = "packet_meta_info.json";
	private static final String PACKET_DATA_HASH_FILENAME = "packet_data_hash.txt";
	private static final String PACKET_OPERATION_HASH_FILENAME = "packet_operations_hash.txt";

	// String constants
	private static final String MOSIPTEST_REGCLIENT_SUPERVISORID = "mosip.test.regclient.supervisorid";
	private static final String MOSIP_TEST_REGCLIENT_USERID = "mosip.test.regclient.userid";
	private static final String MOSIP_TEST_REGCLIENT_supervisorP = "mosip.test.regclient.supervisorpwd";
	private static final String MOSIP_TEST_REGCLIENT_PASSWORD = "mosip.test.regclient.password";
	private static final String EVIDENCE = "evidence";
	private static final String OPTIONAL = "optional";
	private static final String IDENTITY = "identity";
	private static final String FORMAT = "format";
	private static final String VALUE = "value";
	private static final String CONTEXT = "_context";
	private static final String MOSIP_TEST_TEMP = "mosip.test.temp";
	private static final String MOUNTPATH = "mountPath";
	private static final String METADATA = "metaData";
	private static final String OPERATIONSDATA = "operationsData";
	private static final String FALSE = "false";
	private static final String UNENCZIP = "_unenc.zip";
	private static final String JSON = ".json";
	private static final String UTF8 = "UTF-8";
	private static final String HASHSEQUENCE1 = "hashSequence1";
	private static final String LABEL = "label";
	private static final String CHANGESUPERVISORNAMETODIFFCASE = "changeSupervisorNameToDiffCase";

	private String tempLogPath;

	@Value("${mosip.test.regclient.store:/home/sasikumar/Documents/MOSIP/packetcreator}")
	private String finalDestination;

	@Value("${mosip.test.packet.template.location:/home/sasikumar/Documents/MOSIP/packetcreator/template}")
	private String templateFolder;

	@Value("${mosip.test.packet.template.source:REGISTRATION_CLIENT}")
	private String src;

	@Value("${mosip.test.packet.template.process:NEW}")
	private String process;

	@Value("${mosip.test.regclient.centerid}")
	private String centerId;

	@Value("${mosip.test.regclient.machineid}")
	private String machineId;

	@Value("${mosip.test.rid.seq.initialvalue}")
	private int counter;

	@Value("${mosip.test.regclient.userid}")
	private String officerId;

	@Value("${mosip.test.regclient.supervisorid}")
	private String supervisorId;

	@Value("${mosip.test.regclient.supervisorpwd}")
	private String supervisorP;

	@Value("${mosip.test.regclient.password}")
	private String officerP;

	@Value("${mosip.test.regclient.supervisorBiometricFileName}")
	private String supervisorBiometricFileName;

	@Value("${mosip.test.regclient.officerBiometricFileName}")
	private String officerBiometricFileName;

	@Autowired
	private CryptoUtil cryptoUtil;

	@Autowired
	private ZipUtils zipper;

	@Autowired
	private SchemaUtil schemaUtil;

	private String workDirectory;
	private String defaultTemplateLocation;

	@Autowired
	private ContextUtils contextUtils;

	private PacketSyncService packetSyncService;

	private String newRegId;

	@Value("${mosip.version:1.2}")
	private String mosipVersion;

	@Value("${packetmanager.zip.datetime.pattern:yyyyMMddHHmmss}")
	private String zipDatetimePattern;

	@Value("${mosip.test.persona.configpath}")
	private String personaConfigPath;

	public PacketMakerService(@Lazy PacketSyncService packetSyncService) {
		this.packetSyncService = packetSyncService;
	}

	@PostConstruct
	public void initService() {
		if (workDirectory != null)
			return;
		try {
			workDirectory = Files.createTempDirectory("pktcreator").toFile().getAbsolutePath();
			logger.info("CURRENT WORK DIRECTORY --> {}", workDirectory);
			File folder = new File(templateFolder);
			File[] files = folder.listFiles();
			if (files != null && files.length > 0) {
				File templateName = folder.listFiles()[0];
				defaultTemplateLocation = templateName.getAbsolutePath();
			}

		} catch (Exception ex) {
			logger.error("", ex);
		}

	}

	public String getNewRegId() {
		return newRegId;
	}

	public String getWorkDirectory() {
		return workDirectory;
	}

	public static String getRegIdFromPacketPath(String packetPath) {
		// leaf node of packet path is regid

		Path container = Path.of(packetPath);
		String rid = container.getName(container.getNameCount() - 1).toString().split("-")[0];
		return rid;
	}

	public String packPacketContainer(String packetPath, String source, String proc, String contextKey,
			boolean isValidChecksum) throws Exception {

		String retPath = "";
		if (contextKey != null && !contextKey.equals("")) {

			Properties props = contextUtils.loadServerContext(contextKey);
			props.forEach((k, v) -> {
				if (k.toString().equals("mosip.test.packet.template.source")) {
					src = v.toString();
				}
				if (k.toString().equals("mosip.test.packet.template.process")) {
					process = v.toString();
				}

				else if (k.toString().equals("mosip.test.regclient.centerid")) {
					centerId = v.toString();
				} else if (k.toString().equals("mosip.test.regclient.machineid")) {
					machineId = v.toString();
				} else if (k.toString().equals(MOSIPTEST_REGCLIENT_SUPERVISORID)) {
					supervisorId = v.toString();
				} else if (k.toString().equals(MOSIP_TEST_REGCLIENT_USERID)) {
					officerId = v.toString();
				} else if (k.toString().equals(MOSIP_TEST_REGCLIENT_supervisorP)) {
					supervisorP = v.toString();
				} else if (k.toString().equals(MOSIP_TEST_REGCLIENT_PASSWORD)) {
					officerP = v.toString();
				}
			});
		}
		if (source != null)
			src = source;

		String regId = getRegIdFromPacketPath(packetPath);
		String tempPacketRootFolder = Path.of(packetPath).toString();

		if (proc != null)
			process = proc;
		else {
			String tprocess = ContextUtils.ProcessFromTemplate(src, packetPath);
			if (tprocess != null)
				process = tprocess;
		}
		RestClient.logInfo(contextKey, "packPacketContainer:src=" + src + ",process=" + process + "PacketRoot="
				+ tempPacketRootFolder + " regid=" + regId);
		try {
			packPacket(getPacketRoot(getProcessRoot(tempPacketRootFolder), regId, "id"), regId, "id", contextKey);
		} catch (Throwable e) {
			logger.error(" ID Packet FAIL to Pack", e);

		}

		try {
			packPacket(getPacketRoot(getProcessRoot(tempPacketRootFolder), regId, EVIDENCE), regId, EVIDENCE,
					contextKey);
		} catch (Throwable e) {
			logger.error(" EVIDENCE Packet FAIL to Pack", e);

		}
		try {
			packPacket(getPacketRoot(getProcessRoot(tempPacketRootFolder), regId, OPTIONAL), regId, OPTIONAL,
					contextKey);
		} catch (Throwable e) {
			logger.error(" OPTIONAL Packet FAIL to Pack", e);

		}
		try {
			packContainer(tempPacketRootFolder, contextKey);

		} catch (Throwable e) {
			logger.error(" packContainer FAIL to Pack", e);

		}
		retPath = Path.of(Path.of(tempPacketRootFolder) + ".zip").toString();

		return retPath;

	}

	public String createPacketFromTemplate(String templatePath, String personaPath, String contextKey,
			String additionalInfoReqId) throws Exception {

		RestClient.logInfo(contextKey, "createPacketFromTemplate");

		Path idJsonPath = null;
		// Fix for change in Demodata
		if (templatePath != null) {
			process = ContextUtils.ProcessFromTemplate(src, templatePath);
			// get idJson From Template itself
			idJsonPath = ContextUtils.idJsonPathFromTemplate(src, templatePath);
		} else
			idJsonPath = packetSyncService.createIDJsonFromPersona(personaPath, contextKey);

		String packetPath = createContainer((idJsonPath == null ? null : idJsonPath.toString()), templatePath, src,
				process, null, contextKey, false, additionalInfoReqId);
		if (RestClient.isDebugEnabled(contextKey))
			logger.info("createPacketFromTemplate:Packet created : {}", packetPath);
		// newRegId
		JSONObject retObj = new JSONObject();
		retObj.put("packet", packetPath);
		retObj.put("regId", newRegId);

		return retObj.toString();
	}

	/*
	 * Create packet with our without Encryption
	 */
	public String createContainer(String dataFile, String templatePacketLocation, String source, String processArg,
			String preregId, String contextKey, boolean bZip, String additionalInfoReqId) throws Exception {

		String packetPath = "";
		if (contextKey != null && !contextKey.equals("")) {

			Properties props = contextUtils.loadServerContext(contextKey);
			props.forEach((k, v) -> {
				if (k.toString().equals("mosip.test.packet.template.source")) {
					src = v.toString();
				}
				if (k.toString().equals("mosip.test.packet.template.process")) {
					process = v.toString();
				}

				else if (k.toString().equals("mosip.test.regclient.centerid")) {
					centerId = v.toString();
				} else if (k.toString().equals("mosip.test.regclient.machineid")) {
					machineId = v.toString();
				} else if (k.toString().equals(MOSIPTEST_REGCLIENT_SUPERVISORID)) {
					supervisorId = v.toString();
				} else if (k.toString().equals(MOSIP_TEST_REGCLIENT_USERID)) {
					officerId = v.toString();
				} else if (k.toString().equals("mosip.version")) {
					mosipVersion = v.toString();
				} else if (k.toString().equals(MOSIP_TEST_REGCLIENT_supervisorP)) {
					supervisorP = v.toString();
				} else if (k.toString().equals(MOSIP_TEST_REGCLIENT_PASSWORD)) {
					officerP = v.toString();
				}
			});
		}

		String templateLocation = (null == templatePacketLocation) ? defaultTemplateLocation : templatePacketLocation;

		String regId = generateRegId();
		String appId = (additionalInfoReqId == null) ? regId
				: additionalInfoReqId.replace("-BIOMETRIC_CORRECTION-1", "");
		if (additionalInfoReqId != null)
			regId = appId;
		newRegId = regId;
		if (source != null && !source.equals(""))
			src = source;
		if (processArg != null && !processArg.equals(""))
			process = processArg;
		else {
			String tprocess = ContextUtils.ProcessFromTemplate(src, templatePacketLocation);
			if (tprocess != null)
				process = tprocess;
		}
		RestClient.logInfo(contextKey, "src=" + src + ",process=" + process);
		String tempPacketRootFolder = createTempTemplate(templateLocation, appId);

		// update document file here
		createPacket(tempPacketRootFolder, regId, dataFile, "id", preregId, contextKey);
		if (bZip)
			packPacket(getPacketRoot(getProcessRoot(tempPacketRootFolder), regId, "id"), regId, "id", contextKey);
		createPacket(tempPacketRootFolder, regId, dataFile, EVIDENCE, preregId, contextKey);
		if (bZip)
			packPacket(getPacketRoot(getProcessRoot(tempPacketRootFolder), regId, EVIDENCE), regId, EVIDENCE,
					contextKey);
		createPacket(tempPacketRootFolder, regId, dataFile, OPTIONAL, preregId, contextKey);
		if (bZip) {
			packPacket(getPacketRoot(getProcessRoot(tempPacketRootFolder), regId, OPTIONAL), regId, OPTIONAL,
					contextKey);
			packContainer(tempPacketRootFolder, contextKey);

			packetPath = Path.of(Path.of(tempPacketRootFolder) + ".zip").toString();
		} else {
			packetPath = tempPacketRootFolder;
		}

		return packetPath;

	}

	/**
	 * 
	 * @param templateFile - template folder location.
	 * @param dataFile     - JSON file name whose content has to be merged
	 * @return - the merged JSON as a generic map Map<?,?>
	 */
	/*
	 * Map<?, ?> mergeJSON(String templateFile, String dataFile) throws Exception {
	 * try (InputStream inputStream = new FileInputStream(dataFile)) { String
	 * dataToMerge = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
	 * JSONObject data = new JSONObject(dataToMerge);
	 * 
	 * // SKV - custom json merge try (InputStream inputStream2 = new
	 * FileInputStream(templateFile)) { String templateData = new
	 * String(inputStream2.readAllBytes(), StandardCharsets.UTF_8); JSONObject data1
	 * = new JSONObject(templateData);
	 * 
	 * JSONObject result = merge(data1, data); return result.toMap(); } // return
	 * mergeJSON(templateFile, data); } }
	 */

	public Map<?, ?> mergeJSON(String templateFile, String dataFile) throws Exception {
		try (InputStream inputStream = new BufferedInputStream(new FileInputStream(dataFile));
				InputStream inputStream2 = new BufferedInputStream(new FileInputStream(templateFile))) {
			JSONObject data = new JSONObject(new JSONTokener(inputStream));
			JSONObject data1 = new JSONObject(new JSONTokener(inputStream2));
			JSONObject result = merge(data1, data);
			return result.toMap();
		}
	}

	/**
	 * 
	 * @param templateFile - template folder location.
	 * @param data         - JSONObject whose content has to be merged
	 * @return - the merged JSON as a generic map Map<?,?>
	 */

	public JSONObject mergeJSONObject(String templateFile, JSONObject data, String contextKey) throws Exception {
		try (BufferedReader reader = Files.newBufferedReader(Paths.get(templateFile))) {
			JSONObject data1 = new JSONObject(new JSONTokener(reader));
			// Log strategically, especially if the JSON data is large
			if (logger.isInfoEnabled()) {
				logger.info("templatejson: {}", data1);
				logger.info("preregjson: {}", data);
			}
			JSONObject result = merge(data1, data);
			// Log strategically, especially if the merged JSON data is large
			if (logger.isInfoEnabled()) {
				logger.info("mergedjson: {}", result);
			}
			return result;
		}
	}

	private List<String> getMissingAttributeList(String schemaJson, JSONObject idJson) {
		// SKV - check missing mandatory attributes
		JSONObject schema = new JSONObject(schemaJson);
		schema = schema.getJSONObject("properties");
		schema = schema.getJSONObject(IDENTITY);
		JSONArray schemaReqd = schema.getJSONArray("required");

		List<String> notFound = new ArrayList<String>();

		for (int i = 0; i < schemaReqd.length(); i++) {
			String attrName = schemaReqd.getString(i);
			// check whether merged map contain this key
			if (!idJson.has(attrName))
				notFound.add(attrName);
		}
		return notFound;

	}

	private String fillMissingAttributes(List<String> missingAttributes, String dataToMerge) {

		JSONObject data = new JSONObject(dataToMerge);
		JSONObject jb = new JSONObject(dataToMerge).getJSONObject(IDENTITY);
		for (String s : missingAttributes) {
			if (s.toLowerCase().matches(".*individual.*biometric.*")) {
				JSONObject bio = new JSONObject();
				bio.put(FORMAT, "cbeff");
				bio.put("version", "1");
				bio.put(VALUE, "individualBiometrics_bio_CBEFF");
				jb.put(s, bio);
			} else if (s.toLowerCase().contains("city")) {
				// copy from city value

				JSONArray cityArr = jb.getJSONArray("City");
				jb.put(s, cityArr);

			}
			// pobCountry
			else if (s.toLowerCase().contains("country")) {
				JSONObject bio = new JSONObject();
				bio.put("language", "eng");
				bio.put(VALUE, "Abra");
				jb.put(s, new JSONArray().put(bio));

			}
			// province
			else if (s.toLowerCase().contains("province")) {
				// copy from city value

				JSONArray cityArr = jb.getJSONArray("province");
				jb.put(s, cityArr);

			} else if (s.toLowerCase().matches(".*proof.*address.*")) {
				JSONObject bio = new JSONObject();
				bio.put("type", "DOC023");
				bio.put(FORMAT, "PDF");
				bio.put(VALUE, "proofOfAddress");
				jb.put(s, bio);
			} else if (s.toLowerCase().matches(".*proof.*identity.*")) {
				JSONObject bio = new JSONObject();
				bio.put("type", "DOC018");
				bio.put(FORMAT, "PDF");
				bio.put(VALUE, "proofOfIdentity");
				jb.put(s, bio);
			} else if (s.toLowerCase().matches(".*parent.*biometric.*")) {
				JSONObject bio = new JSONObject();
				bio.put(FORMAT, "cbeff");
				bio.put("version", "1");
				bio.put(VALUE, "individualBiometrics_bio_CBEFF");
				jb.put(s, bio);
			} else {
				JSONObject bio = new JSONObject();
				bio.put("language", "eng");
				bio.put(VALUE, "101755");
				jb.put(s, bio);
			}

		}
		data.put(IDENTITY, jb);
		return data.toString();
	}

	boolean createPacket(String containerRootFolder, String regId, String dataFilePath, String type, String preregId,
			String contextKey) throws Exception {
		String packetRootFolder = getPacketRoot(getProcessRoot(containerRootFolder), regId, type);
		String templateFile = getIdJSONFileLocation(packetRootFolder);

		String dataToMerge = null;
		if (dataFilePath != null)
			dataToMerge = Files.readString(Path.of(dataFilePath));
		JSONObject jb=null;
			jb = new JSONObject(dataToMerge).getJSONObject(IDENTITY);
		// workaround for MOSIP-18123

		JSONObject jb1 = new JSONObject(dataToMerge);
		List<String> jsonList = jb.keySet().stream().filter(j -> j.startsWith("proof")).collect(Collectors.toList());
		jsonList.forEach(o -> {
				jb1.getJSONObject(IDENTITY).getJSONObject(o).put(VALUE, o);

				jb1.getJSONObject(IDENTITY).getJSONObject(o).remove("refNumber");
		});

		dataToMerge = jb1.toString();
		RestClient.logInfo(contextKey, dataToMerge);
		try {

			String schemaVersion = jb.optString("IDSchemaVersion", "0");
			String schemaJson = schemaUtil.getAndSaveSchema(schemaVersion, workDirectory, contextKey);

			if (type.equals("id")) {
				Path path = Paths.get(
						VariableManager.getVariableValue(contextKey, MOUNTPATH).toString()
								+ VariableManager.getVariableValue(contextKey, MOSIP_TEST_TEMP).toString(),
						contextKey.replace(CONTEXT, ""), regId + "_schema.json");
				Files.createDirectories(path.getParent());
				try {
					Files.createFile(path);
				} catch (FileAlreadyExistsException e) {
					logger.error("already exists: " + e.getMessage());
				}
				CommonUtil.write(path, schemaJson.getBytes());

			}
			JSONObject jbToMerge = schemaUtil.getPacketIDData(schemaJson, dataToMerge, type);

			JSONObject mergedJsonMap = mergeJSONObject(templateFile, jbToMerge, contextKey);

			if (type.equals("id")) {
				List<String> invalidIds = CreatePersona.validateIDObject(mergedJsonMap, contextKey);
				Path path = Paths.get(
						VariableManager.getVariableValue(contextKey, MOUNTPATH).toString()
								+ VariableManager.getVariableValue(contextKey, MOSIP_TEST_TEMP).toString(),
						contextKey.replace(CONTEXT, ""), regId + "_invalidIds.json");
//To Do check				Files.createDirectories(path.getParent());
				try {
					Files.createFile(path);
				} catch (FileAlreadyExistsException e) {
					logger.error("already exists: " + e.getMessage());
				}
				CommonUtil.write(path, invalidIds.toString().getBytes());
			}

			if (!writeJSONFile(mergedJsonMap.toMap(), templateFile)) {
				logger.error("Error creating packet {} ", regId);
				return false;
			}

			updatePacketMetaInfo(packetRootFolder, METADATA, "registrationId", regId, true);
			if (preregId != null && !preregId.equalsIgnoreCase("0")) // newly added

				updatePacketMetaInfo(packetRootFolder, METADATA, "preRegistrationId", preregId, true);

			if (VariableManager.getVariableValue(contextKey, "invalidDateFlag").toString()
					.equals("invalidCreationDate")) {
				updatePacketMetaInfo(packetRootFolder, METADATA, "creationDate", null, true);
			} else {
				updatePacketMetaInfo(packetRootFolder, METADATA, "creationDate", APIRequestUtil.getUTCDateTime(null),
						true);
			}
			updatePacketMetaInfo(packetRootFolder, METADATA, "machineId", machineId, false);
			updatePacketMetaInfo(packetRootFolder, METADATA, "centerId", centerId, false);
			updatePacketMetaInfo(packetRootFolder, METADATA, "registrationType",
					StringUtils.capitalize(process.toLowerCase()), false);

			// ToRead Context file
			String filePath = personaConfigPath + "/server.context." + contextKey + ".properties";
			Properties p = new Properties();

			try (FileReader reader = new FileReader(filePath);) {
				p.load(reader);
				reader.close();

			} catch (IOException e1) {
				logger.error(e1.getMessage());
			}
			officerId = p.getProperty(MOSIP_TEST_REGCLIENT_USERID);

			if (!VariableManager.getVariableValue(contextKey, "invalidOfficerIDFlag").toString()
					.equals("invalidOfficerID")) {
				updatePacketMetaInfo(packetRootFolder, OPERATIONSDATA, "officerId", officerId, false);
			}
			supervisorId = p.getProperty(MOSIPTEST_REGCLIENT_SUPERVISORID);

			if (VariableManager.getVariableValue(contextKey, CHANGESUPERVISORNAMETODIFFCASE).toString()
					.equalsIgnoreCase("true"))
				supervisorId = generateCaseConvertedString(supervisorId);

			updatePacketMetaInfo(packetRootFolder, OPERATIONSDATA, "supervisorId", supervisorId, false);

			// officerP
			officerP = p.getProperty(MOSIP_TEST_REGCLIENT_PASSWORD);
			if (officerP != null && officerP.equals("invalid"))
				officerP = FALSE; // invalid
			else if (officerP != null && !officerP.equals(""))
				officerP = "true"; // valid
			else
				officerP = FALSE; // null
			updatePacketMetaInfo(packetRootFolder, OPERATIONSDATA, "officerPassword", officerP, false);

			// supervisorP
			supervisorP = p.getProperty(MOSIP_TEST_REGCLIENT_supervisorP);
			if (supervisorP != null && supervisorP.equals("invalid"))
				supervisorP = FALSE; // invalid
			else if (supervisorP != null && !supervisorP.equals(""))
				supervisorP = "true"; // valid
			else
				supervisorP = FALSE; // null
			updatePacketMetaInfo(packetRootFolder, OPERATIONSDATA, "supervisorPassword", supervisorP, false);

			// officerBiometricFileName
			officerBiometricFileName = p.getProperty("mosip.test.regclient.officerBiometricFileName");
			if (officerBiometricFileName != null && officerBiometricFileName.length() > 1) {
			} else
				officerBiometricFileName = null;
			updatePacketMetaInfo(packetRootFolder, OPERATIONSDATA, "officerBiometricFileName", officerBiometricFileName,
					false);

			// supervisorBiometricFileName
			supervisorBiometricFileName = p.getProperty("mosip.test.regclient.supervisorBiometricFileName");
			if (supervisorBiometricFileName != null && supervisorBiometricFileName.length() > 1) {
			} else
				supervisorBiometricFileName = null;
			updatePacketMetaInfo(packetRootFolder, OPERATIONSDATA, "supervisorBiometricFileName",
					supervisorBiometricFileName, false);

			updateAudit(packetRootFolder, regId, contextKey);

			LinkedList<String> sequence = updateHashSequence1(packetRootFolder);
			LinkedList<String> operations_seq = updateHashSequence2(packetRootFolder);
			if (preregId != null && preregId.equals("01")) // to generte invalid hash data
			{
				CommonUtil.write(Path.of(packetRootFolder, PACKET_DATA_HASH_FILENAME),
						"PACKET_DATA_HASH_INVALID_DATA".getBytes());
				CommonUtil.write(Path.of(packetRootFolder, PACKET_OPERATION_HASH_FILENAME),
						"PACKET_OPERATION_HASH_INVALID_DATA".getBytes());
			} else {
				updatePacketDataHash(packetRootFolder, sequence, PACKET_DATA_HASH_FILENAME, contextKey);
				updatePacketDataHash(packetRootFolder, operations_seq, PACKET_OPERATION_HASH_FILENAME, contextKey);
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}

		VariableManager.setVariableValue(contextKey, "META_INFO-OPERATIONS_DATA-supervisorId",
				supervisorId != null ? supervisorId : "");
		VariableManager.setVariableValue(contextKey, "META_INFO-OPERATIONS_DATA-officerId",
				officerId != null ? officerId : "");
		VariableManager.setVariableValue(contextKey, "META_INFO-META_DATA-centerId", centerId != null ? centerId : "");

		return true;
	}

	boolean packPacket(String containerRootFolder, String regId, String type, String contextKey) throws Exception {
		boolean result = zipAndEncrypt(Path.of(containerRootFolder), contextKey);
		if (!result) {
			logger.error("Encryption failed!!! ");
			return false;
		}

		MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");

		String encryptedHashFlag = VariableManager.getVariableValue(contextKey, "invalidEncryptedHashFlag").toString();
		String encryptedHash = null;

		// Make encrypted hash as invalid if "invalidEncryptedHashFlag --yes"
		if (encryptedHashFlag.equalsIgnoreCase("invalidEncryptedHash") && type.equals("id"))
			encryptedHash = "INVALID_ENCRYPTED_HASH";
		else
			encryptedHash = org.apache.commons.codec.binary.Base64.encodeBase64URLSafeString(
					messageDigest.digest(Files.readAllBytes(Path.of(Path.of(containerRootFolder) + ".zip"))));

		String signature = Base64.getUrlEncoder().encodeToString(
				cryptoUtil.sign(Files.readAllBytes(Path.of(Path.of(containerRootFolder) + UNENCZIP)), contextKey));
		Path src = Path.of(containerRootFolder + UNENCZIP);
		Path destination = Path.of(
				VariableManager.getVariableValue(contextKey, MOUNTPATH).toString()
						+ VariableManager.getVariableValue(contextKey, MOSIP_TEST_TEMP).toString(),
				contextKey.replace(CONTEXT, ""), src.getFileName().toString());

		/*
		 * Files.copy(src, destination, StandardCopyOption.REPLACE_EXISTING);
		 */
		CommonUtil.copyFileWithBuffer(src, destination);
		synchronized (this) {
			Files.delete(Path.of(containerRootFolder + UNENCZIP));
			FileSystemUtils.deleteRecursively(Path.of(containerRootFolder));
		}

		String containerMetaDataFileLocation = containerRootFolder + JSON;
		return fixContainerMetaData(containerMetaDataFileLocation, regId, type, encryptedHash, signature);
	}

	boolean packContainer(String containerRootFolder, String contextKey) throws Exception {
		Path path = Path.of(containerRootFolder);

		boolean result = zipAndEncrypt(path, contextKey);

		Path src = Path.of(path + UNENCZIP);
		Path destination = Path.of(
				VariableManager.getVariableValue(contextKey, MOUNTPATH).toString()
						+ VariableManager.getVariableValue(contextKey, MOSIP_TEST_TEMP).toString(),
				contextKey.replace(CONTEXT, ""), src.getFileName().toString());

		/*
		 * Files.copy(src, Path.of(VariableManager.getVariableValue(contextKey,
		 * MOUNTPATH).toString() + VariableManager.getVariableValue(contextKey,
		 * MOSIP_TEST_TEMP).toString(), contextKey.replace(CONTEXT, ""),
		 * src.getFileName().toString()), StandardCopyOption.REPLACE_EXISTING);
		 */

		CommonUtil.copyFileWithBuffer(src, destination);

		Files.delete(Path.of(path + UNENCZIP));
		return result;
	}

	/*
	 * private boolean zipAndEncrypt(Path zipSrcFolder, String contextKey) throws
	 * Exception { Path finalZipFile = Path.of(zipSrcFolder + UNENCZIP);
	 * zipper.zipFolder(zipSrcFolder, finalZipFile,contextKey); try (FileInputStream
	 * zipFile = new FileInputStream(finalZipFile.toFile().getAbsolutePath())) {
	 * boolean result = cryptoUtil.encryptPacket(zipFile.readAllBytes(), centerId +
	 * UNDERSCORE + machineId, Path.of(zipSrcFolder + ".zip").toString(),
	 * contextKey); // Dont uncomment this: Files.delete(finalZipFile); if (!result)
	 * { logger.error("Encryption failed!!! "); return false; } } return true; }
	 */

	public synchronized boolean zipAndEncrypt(Path zipSrcFolder, String contextKey) throws Exception {
		Path finalZipFile = Path.of(zipSrcFolder + UNENCZIP);
		// Use buffered streams
		try (BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(
				new FileOutputStream(finalZipFile.toFile()))) {
			zipper.zipFolder(zipSrcFolder, bufferedOutputStream, contextKey);
		} catch (Exception e) {
			logger.error("Failed to zip the folder ");
			return false;
		}
		try (BufferedInputStream zipFileInputStream = new BufferedInputStream(
				new FileInputStream(finalZipFile.toFile().getAbsolutePath()))) {
			boolean result = cryptoUtil.encryptPacket(zipFileInputStream.readAllBytes(),
					centerId + UNDERSCORE + machineId, Path.of(zipSrcFolder + ".zip").toString(), contextKey);
			// Uncomment the line below if the original zip file is not needed
			// Files.delete(finalZipFile);
			if (!result) {
				logger.error("Encryption failed!!! ");
				return false;
			}
		} catch (Exception e) {
			logger.error("Failed to zip the folder ");
			return false;
		}
		return true;
	}

	/*
	 * private boolean writeJSONFile(Map<?, ?> jsonValue, String fileToWrite) {
	 * ObjectMapper objectMapper = new ObjectMapper(); ObjectWriter jsonWriter =
	 * objectMapper.writer(); try (FileOutputStream fos = new
	 * FileOutputStream(fileToWrite); BufferedOutputStream bos = new
	 * BufferedOutputStream(fos); OutputStreamWriter writer = new
	 * OutputStreamWriter(bos, StandardCharsets.UTF_8)) {
	 * jsonWriter.writeValue(writer, jsonValue); return true; } catch (Exception ex)
	 * { logger.error("", ex); return false; } }
	 */

	private boolean writeJSONFile(Map<?, ?> jsonValue, String fileToWrite) {
		ObjectMapper objectMapper = new ObjectMapper();
		ObjectWriter jsonWriter = objectMapper.writer();
		// You can tweak buffer size (e.g., 32 KB)
		final int BUFFER_SIZE = 32 * 1024;
		try (FileOutputStream fos = new FileOutputStream(fileToWrite);
				BufferedOutputStream bos = new BufferedOutputStream(fos, BUFFER_SIZE);
				OutputStreamWriter writer = new OutputStreamWriter(bos, StandardCharsets.UTF_8)) {
			jsonWriter.writeValue(writer, jsonValue);
			return true;
		} catch (Exception ex) {
			logger.error("", ex);
			return false;
		}
	}

	private String getIdJSONFileLocation(String packetRootFolder) {
		return new File(packetRootFolder + File.separator + "ID".toUpperCase() + JSON).toString();
	}

	private String getProcessRoot(String containerRootFolder) {
		return Path.of(containerRootFolder, src, process).toString();
	}

	private String getPacketRoot(String processRootFolder, String rid, String type) {
		return Path.of(processRootFolder, rid + UNDERSCORE + type.toLowerCase()).toString();
	}

	private String getContainerMetadataJSONFileLocation(String processRootFolder, String rid, String type) {
		return Path.of(processRootFolder, rid + UNDERSCORE + type.toLowerCase() + JSON).toString();
	}

	private String createTempTemplate(String templatePacket, String rid) throws IOException, SecurityException {
		Path sourceDirectory = Paths.get(templatePacket);
		String tempDir = workDirectory + File.separator + rid + "-" + centerId + "_" + machineId + "-"
				+ getcurrentTimeStamp();
		Path targetDirectory = Paths.get(tempDir);
		FileSystemUtils.copyRecursively(sourceDirectory, targetDirectory);
		setupTemplateName(tempDir, rid);
		return targetDirectory.toString();
	}

	private void setupTemplateName(String templateRootPath, String regId) throws SecurityException {
		String finalPath = templateRootPath + File.separator + src + File.separator + process;
		File rootFolder = new File(finalPath);
		File[] listFiles = rootFolder.listFiles();
		boolean assignValue = false;
		if (listFiles != null) {
			for (File f : listFiles) {
				String name = f.getName();
				String finalName = name.replace("rid", regId);
				assignValue = f.renameTo(new File(finalPath + File.separator + finalName));
				if (!assignValue)
					logger.error("Failed to rename the file");
			}
		}
	}

	private String getcurrentTimeStamp() {
		DateTimeFormatter format = DateTimeFormatter.ofPattern(zipDatetimePattern);
		return LocalDateTime.now(ZoneId.of("UTC")).format(format);
	}

	private boolean fixContainerMetaData(String fileToFix, String rid, String type, String encryptedHash,
			String signature) throws IOException, Exception {
		Map<String, String> metaData = new HashMap();
		metaData.put("process", process);
		metaData.put("creationdate", APIRequestUtil.getUTCDateTime(null));
		metaData.put("encryptedhash", encryptedHash);
		metaData.put("signature", signature);
		metaData.put("id", rid);
		metaData.put("source", src);
		metaData.put("packetname", rid + UNDERSCORE + type);

		File containerMetaDataTemp = File.createTempFile("pkm", ".cm");
		writeJSONFile(metaData, containerMetaDataTemp.getAbsolutePath());
		Map<?, ?> mergedJsonMap = mergeJSON(fileToFix, containerMetaDataTemp.getAbsolutePath());
		if (!writeJSONFile(mergedJsonMap, fileToFix)) {
			logger.error("Error creating containerMetaData packet {} ", rid);
			return false;
		}
		return true;

	}

	JSONObject merge(JSONObject mainNode, JSONObject updateNode) {

		Iterator<String> fieldNames = updateNode.keys();

		while (fieldNames.hasNext()) {
			String updatedFieldName = fieldNames.next();
			Object valueToBeUpdatedO = null;
			Object updatedValueO = null;
			if (mainNode.has(updatedFieldName))
				valueToBeUpdatedO = mainNode.get(updatedFieldName);
			if (updateNode.has(updatedFieldName))
				updatedValueO = updateNode.get(updatedFieldName);

			// If the node is an @ArrayNode
			if (valueToBeUpdatedO != null && valueToBeUpdatedO instanceof JSONArray
					&& updatedValueO instanceof JSONArray) {
				JSONArray valueToBeUpdated = (JSONArray) valueToBeUpdatedO;
				JSONArray updatedValue = (JSONArray) updatedValueO;

				// running a loop for all elements of the updated ArrayNode

				for (int i = 0; i < updatedValue.length(); i++) {
					JSONObject updatedChildNode = updatedValue.getJSONObject(i);
					// Create a new Node in the node that should be updated, if there was no
					// corresponding node in it
					// Use-case - where the updateNode will have a new element in its Array
					if (valueToBeUpdated.length() <= i) {
						valueToBeUpdated.put(updatedChildNode);
					}
					// getting reference for the node to be updated
					JSONObject childNodeToBeUpdated = valueToBeUpdated.getJSONObject(i);
					merge(childNodeToBeUpdated, updatedChildNode);
				}
				// if the Node is an @ObjectNode
			} else if (valueToBeUpdatedO != null && valueToBeUpdatedO instanceof JSONObject) {

				merge((JSONObject) valueToBeUpdatedO, (JSONObject) updatedValueO);
			} else {
				if (mainNode instanceof JSONObject) {
					mainNode.put(updatedFieldName, updatedValueO);
				}
			}
		}
		return mainNode;
	}

	private String generateRegId() {
		SimpleDateFormat f = new SimpleDateFormat("yyyyMMddHHmmss");
		f.setTimeZone(TimeZone.getTimeZone("UTC"));
		String currUTCTime = f.format(new Date());
		++counter;
		return centerId + machineId + counter + currUTCTime;
	}

	private LinkedList<String> updateHashSequence1(String packetRootFolder) throws Exception {
		LinkedList<String> sequence = new LinkedList<>();
		Path metaInfoPath = Path.of(packetRootFolder, PACKET_META_FILENAME);
		JSONObject metaInfo;
		try (BufferedReader reader = Files.newBufferedReader(metaInfoPath, StandardCharsets.UTF_8)) {
			metaInfo = new JSONObject(reader.lines().collect(Collectors.joining()));
		}
		metaInfo.getJSONObject(IDENTITY).put(HASHSEQUENCE1, new JSONArray());
		sequence = updateHashSequence(metaInfo, HASHSEQUENCE1, "biometricSequence", sequence,
				getBiometricFiles(packetRootFolder));
		sequence = updateHashSequence(metaInfo, HASHSEQUENCE1, "demographicSequence", sequence,
				getDemographicDocFiles(packetRootFolder));
		// Write the updated JSON metadata back to the file
		try (BufferedWriter writer = Files.newBufferedWriter(metaInfoPath, StandardCharsets.UTF_8)) {
			writer.write(metaInfo.toString());
		}
		return sequence;
	}

	private LinkedList<String> updateHashSequence2(String packetRootFolder) throws Exception {
		LinkedList<String> sequence = new LinkedList<>();
		Path metaInfoPath = Path.of(packetRootFolder, PACKET_META_FILENAME);
		JSONObject metaInfo;
		try (BufferedReader reader = Files.newBufferedReader(metaInfoPath, StandardCharsets.UTF_8)) {
			metaInfo = new JSONObject(reader.lines().collect(Collectors.joining()));
		}
		metaInfo.getJSONObject(IDENTITY).put("hashSequence2", new JSONArray());
		sequence = updateHashSequence(metaInfo, "hashSequence2", "otherFiles", sequence,
				getOperationsFiles(packetRootFolder));
		// Write the updated JSON metadata back to the file
		try (BufferedWriter writer = Files.newBufferedWriter(metaInfoPath, StandardCharsets.UTF_8)) {
			writer.write(metaInfo.toString());
		}
		return sequence;
	}

	private void updatePacketDataHash(String packetRootFolder, LinkedList<String> sequence, String fileName,
			String contextKey) throws Exception {
		MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		for (String path : sequence) {
			out.write(CommonUtil.read(path));
		}
		String packetDataHash = new String(Hex.encode(messageDigest.digest(out.toByteArray()))).toUpperCase();
		// TODO - its failing with Hex.encoded hash, so using the below method to
		// generate hash
		String packetDataHash2 = DatatypeConverter.printHexBinary(messageDigest.digest(out.toByteArray()))
				.toUpperCase();
		if (RestClient.isDebugEnabled(contextKey)) {
			logger.info("sequence packetDataHash >> {} ", packetDataHash);
			logger.info("sequence packetDataHash2 >> {} ", packetDataHash2);
		}
		CommonUtil.write(Path.of(packetRootFolder, fileName), packetDataHash2.getBytes());
	}

	private List<String> getBiometricFiles(String packetRootFolder) {
		List<String> paths = new ArrayList<>();
		File packetFolder = Path.of(packetRootFolder).toFile();
		File[] biometricFiles = packetFolder.listFiles((d, name) -> name.endsWith(".xml"));
		for (File file : biometricFiles) {
			paths.add(file.getAbsolutePath());
		}
		return paths;
	}

	private List<String> getDemographicDocFiles(String packetRootFolder) {
		List<String> paths = new ArrayList<>();
		File packetFolder = Path.of(packetRootFolder).toFile();
		File[] documents = packetFolder
				.listFiles((d, name) -> name.endsWith(".pdf") || name.endsWith(".jpg") || name.equals("ID.json"));
		for (File file : documents) {
			paths.add(file.getAbsolutePath());
		}
		return paths;
	}

	private List<String> getOperationsFiles(String packetRootFolder) {
		List<String> paths = new ArrayList<>();
		File packetFolder = Path.of(packetRootFolder).toFile();
		File[] documents = packetFolder.listFiles((d, name) -> name.equals("audit.json"));
		for (File file : documents) {
			paths.add(file.getAbsolutePath());
		}
		return paths;
	}

	/*
	 * private LinkedList<String> updateHashSequence(JSONObject metaInfo, String
	 * parentKey, String seqName, LinkedList<String> sequence, List<String> files) {
	 * 
	 * JSONObject seqObject = new JSONObject(); if (files != null && files.size() >
	 * 0) { JSONArray list = new JSONArray(); for (String path : files) { File file
	 * = new File(path); String fileName = file.getName();
	 * list.put(fileName.substring(0, fileName.lastIndexOf(".")));
	 * sequence.add(file.getAbsolutePath()); } if (list.length() > 0) {
	 * seqObject.put(LABEL, seqName); seqObject.put(VALUE, list); } } if
	 * (seqObject.length() > 0)
	 * metaInfo.getJSONObject(IDENTITY).getJSONArray(parentKey).put(seqObject);
	 * 
	 * return sequence; }
	 */
	
	
	private LinkedList<String> updateHashSequence(JSONObject metaInfo, String parentKey, String seqName,
			LinkedList<String> sequence, List<String> files) {
		// Early exit if the files list is empty or null
		if (files == null || files.isEmpty()) {
			return sequence;
		}
		JSONArray list = new JSONArray(); // Store the filenames without extensions
		for (String path : files) {
			// Use File object to get file name and absolute path
			File file = new File(path);
			String fileName = file.getName();
			// Extract the file name without the extension
			int lastDotIndex = fileName.lastIndexOf(".");
			String fileNameWithoutExtension = (lastDotIndex != -1) ? fileName.substring(0, lastDotIndex) : fileName;
			// Add the filename without extension to the JSONArray
			list.put(fileNameWithoutExtension);
			// Add the absolute file path to the sequence list
			sequence.add(file.getAbsolutePath()); // Use the absolute path from the File object
		}
		// Only add the sequence object to the metaInfo if files were processed
		if (list.length() > 0) {
			JSONObject seqObject = new JSONObject();
			seqObject.put(LABEL, seqName);
			seqObject.put(VALUE, list);
			metaInfo.getJSONObject(IDENTITY).getJSONArray(parentKey).put(seqObject);
		}
		return sequence;
	}

	/*
	 * private void updatePacketMetaInfo(String packetRootFolder, String parentKey,
	 * String key, String value, boolean parentLevel) throws Exception { String
	 * metaInfo_json = Files.readString(Path.of(packetRootFolder,
	 * PACKET_META_FILENAME)); JSONObject jsonObject = new
	 * JSONObject(metaInfo_json);
	 * 
	 * if (parentLevel) jsonObject.getJSONObject(IDENTITY).put(key, value);
	 * 
	 * boolean updated = false; if
	 * (jsonObject.getJSONObject(IDENTITY).has(parentKey)) { JSONArray metadata =
	 * jsonObject.getJSONObject(IDENTITY).getJSONArray(parentKey); for (int i = 0; i
	 * < metadata.length(); i++) { if
	 * (metadata.getJSONObject(i).getString(LABEL).equals(key)) {
	 * jsonObject.getJSONObject(IDENTITY).getJSONArray(parentKey).getJSONObject(i).
	 * put(VALUE, value); updated = true; } } }
	 * 
	 * if (!updated) { JSONObject rid = new JSONObject(); rid.put(LABEL, key);
	 * rid.put(VALUE, value);
	 * jsonObject.getJSONObject(IDENTITY).getJSONArray(parentKey).put(rid); }
	 * 
	 * CommonUtil.write(Path.of(packetRootFolder, PACKET_META_FILENAME),
	 * jsonObject.toString().getBytes(UTF8)); }
	 */
	
	private void updatePacketMetaInfo(String packetRootFolder, String parentKey, String key, String value,
			boolean parentLevel) throws Exception {
			String metaInfo_json = Files.readString(Path.of(packetRootFolder, PACKET_META_FILENAME));
			JSONObject jsonObject = new JSONObject(metaInfo_json);
			JSONObject identityObj = jsonObject.getJSONObject(IDENTITY);
			if (parentLevel)
				identityObj.put(key, value);
			boolean updated = false;
			if (identityObj.has(parentKey)) {
				JSONArray metadata = identityObj.getJSONArray(parentKey);
				for (int i = 0; i < metadata.length(); i++) {
					JSONObject metaItem = metadata.getJSONObject(i);
					if (metaItem.getString(LABEL).equals(key)) {
						metaItem.put(VALUE, value);
						updated = true;
					}
				}
			}
			if (!updated) {
				JSONObject rid = new JSONObject();
				rid.put(LABEL, key);
				rid.put(VALUE, value);
				jsonObject.getJSONObject(IDENTITY).getJSONArray(parentKey).put(rid);
			}
			CommonUtil.write(Path.of(packetRootFolder, PACKET_META_FILENAME), jsonObject.toString().getBytes(UTF8));
		}

	private String generateCaseConvertedString(String inputString) {
		StringBuilder result = new StringBuilder();
		for (char c : inputString.toCharArray()) {
			if (Character.isUpperCase(c)) {
				result.append(Character.toLowerCase(c));
			} else if (Character.isLowerCase(c)) {
				result.append(Character.toUpperCase(c));
			} else {
				result.append(c);
			}
		}
		return result.toString();
	}

	private void updateAudit(String path, String rid, String contextKey) {
		Path auditfile = Path.of(path, "audit.json");
		if (auditfile.toFile().exists()) {
			try {
				List<String> newLines = new ArrayList<>();
				for (String line : Files.readAllLines(auditfile, StandardCharsets.UTF_8)) {
					newLines.add(line.replaceAll("<RID>", rid));
				}
				Files.write(auditfile, newLines, StandardCharsets.UTF_8);
			} catch (IOException e) {
				if (RestClient.isDebugEnabled(contextKey))
					logger.info("Failed to update audit.json", e);
			}
		}
	}
}
