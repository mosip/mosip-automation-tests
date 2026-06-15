package io.mosip.testrig.dslrig.packetcreator.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;


import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import io.mosip.testrig.dslrig.dataprovider.util.CommonUtil;
import io.mosip.testrig.dslrig.dataprovider.util.RestClient;
import io.mosip.testrig.dslrig.dataprovider.util.ServiceException;

@Service
public class CommandsService {

	@Value("${mosip.test.testcase.propertypath:../deploy/testcases.properties}")
	private String propertyPath;

	@Value("${mosip.test.uploads:../deploy/uploads}")
	private String uploadPath;

	@Value("${mosip.test.baseurl}")
	private String baseUrl;

	@Value("${mosip.test.pinglistfile:../deploy/pinglist.txt}")
	private String pinglistfile;

	@Value("${mosip.test.idrepo.actuator.info.path:idrepository/v1/identity/actuator/info}")
	private String idRepoActuatorInfoPath;

	@Value("${mosip.test.persona.configpath}")
	private String personaConfigPath;

	@Autowired
	private ContextUtils contextUtils;

	private static final Logger logger = LoggerFactory.getLogger(CommandsService.class);

	public String checkContext(String contextKey, String module, String eSignetDeployed) throws IOException {

		Properties props = contextUtils.loadServerContext(contextKey);
		baseUrl = props.getProperty("urlBase");
		String eSignetbaseurl = props.getProperty("eSignetbaseurl");


		File file = new File(pinglistfile);


		JSONObject retJson = new JSONObject();
		String line;
		try (FileReader fr = new FileReader(file); BufferedReader br = new BufferedReader(fr)) {

			List<String> failedAPIs = new ArrayList<String>();
			boolean allModules = false;
			if (module == null || module.equals("")) {
				allModules = true;
			}
			while ((line = br.readLine()) != null) {

				if (line.trim().equals("") || line.trim().startsWith("#"))
					continue;

				boolean bcheck = false;

				String controllerPath = line.trim();
				String modName = null;
				String[] parts = controllerPath.split("=");
				if (parts.length > 1) {


					if (parts[1].contains("esignet") && !eSignetDeployed.equalsIgnoreCase("true"))
						continue;

					controllerPath = parts[1];
					modName = parts[0].trim();
				}
				if (allModules)
					bcheck = true;
				else {
					if (modName == null || module.equalsIgnoreCase(modName))
						bcheck = true;
				}
				if (bcheck) {
					RestClient.logInfo(contextKey, controllerPath);
					Boolean bRet1;
					if (parts[1].contains("esignet") && !eSignetDeployed.equalsIgnoreCase("true"))
						bRet1 = RestClient.checkActuatorNoAuth(eSignetbaseurl+"/" + controllerPath.trim(), contextKey);
					else
						bRet1 = RestClient.checkActuatorNoAuth(baseUrl + controllerPath.trim(), contextKey);
					if (bRet1 == false) {
						failedAPIs.add(line);
					}
				}
			}
			fr.close();
			if (failedAPIs.isEmpty())
				retJson.put("status", true);
			else {
				retJson.put("status", false);
				retJson.put("failed", failedAPIs);
			}
		} catch (ServiceException se) {
            throw se;
        } catch (Exception ex) {
            logger.error("Command execution failed", ex);

            throw new ServiceException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "CHECK_CONTEXT_FAIL",
                    baseUrl,
                    ex,
                    ex.getMessage()
            );
        }

        return retJson.toString();
	}

	public String getIdRepoActuatorInfo(String contextKey, String targetBaseUrlOverride) {
		String envBaseUrl = resolveTargetBaseUrl(contextKey, targetBaseUrlOverride);
		String infoUrl = normalizeBaseUrl(envBaseUrl) + idRepoActuatorInfoPath;
		RestClient.logInfo(contextKey, "Fetching id-repository actuator info from " + infoUrl);
		io.restassured.response.Response response = RestClient.getWithoutCookie(infoUrl);
		if (response == null || response.getStatusCode() != 200) {
			int status = response == null ? -1 : response.getStatusCode();
			throw new ServiceException(HttpStatus.BAD_GATEWAY, "IDREPO_ACTUATOR_INFO_FAIL",
					"Failed to fetch id-repository actuator info from " + infoUrl + " (HTTP " + status + ")");
		}
		String body = response.getBody().asString();
		JSONObject json = new JSONObject(body);
		if (!json.has("build") || !json.getJSONObject("build").has("version")) {
			throw new ServiceException(HttpStatus.BAD_GATEWAY, "IDREPO_ACTUATOR_INFO_INVALID",
					"Actuator info response missing build.version: " + body);
		}
		return json.toString();
	}

	private String resolveTargetBaseUrl(String contextKey, String targetBaseUrlOverride) {
		if (targetBaseUrlOverride != null && !targetBaseUrlOverride.isBlank()) {
			return targetBaseUrlOverride.trim();
		}
		Properties props = contextUtils.loadServerContext(contextKey);
		String fromContext = props.getProperty("urlBase");
		if (fromContext != null && !fromContext.isBlank()) {
			return fromContext.trim();
		}
		if (baseUrl != null && !baseUrl.isBlank()) {
			return baseUrl.trim();
		}
		throw new ServiceException(HttpStatus.BAD_REQUEST, "TARGET_BASE_URL_MISSING",
				"targetBaseUrl query parameter, context urlBase, or mosip.test.baseurl must be configured");
	}

	private static String normalizeBaseUrl(String url) {
		String normalized = url.trim();
		if (normalized.endsWith("/")) {
			return normalized.substring(0, normalized.length() - 1);
		}
		return normalized;
	}

	public String writeToFile(String contextKey, Properties requestData, long offset) throws IOException {


		String filePath = requestData.getProperty("filePath");
		String base64data = requestData.getProperty("base64data");
		byte[] data = Base64.decode(base64data.getBytes());
		File myFile = new File(filePath);


		try (RandomAccessFile accessor = new RandomAccessFile(myFile, "rws");) {
			accessor.seek(offset);
			accessor.write(data);
		} catch (Exception ex) {
			logger.error(ex.getMessage());
		}
		return filePath;
	}

	public String execute(String testcaseId, boolean bSync) {
		String result = "Success";
		Properties props = new Properties();

		logger.info("execute Testcase:" + testcaseId);

		try (InputStream input = new FileInputStream(propertyPath)) {
			props.load(input);
			if (props.containsKey(testcaseId)) {
				String testcaseFilePath = props.get(testcaseId).toString();
				Path filePath = Path.of(testcaseFilePath);

				ProcessBuilder pb = new ProcessBuilder("cmd", "/c", filePath.getFileName().toString());
				File dir = filePath.getParent().toFile();
				pb.directory(dir);
				Process p = pb.start();
				logger.info("Exec Testcase:" + testcaseId + ":" + "folder:" + dir.toString() + ":"
						+ filePath.getFileName().toString());
				if (bSync) {
					try {
						p.waitFor();
						logger.info("Exec Testcase:" + testcaseId + " execution completed");
					} catch (InterruptedException e) {
						logger.error(e.getMessage());
						Thread.currentThread().interrupt();
					}
				}
			} else
				result = "{Failed : testcaseID not found}";

		} catch (IOException e) {
			logger.error(e.getMessage());
			result = "{Failed}";
		}
		return result;
	}

	public String storeFile(MultipartFile file) throws IOException {
		String fileExtension = "";
		String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
		fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
		File uploadFolder = new File(uploadPath);
		if (!uploadFolder.exists() || !uploadFolder.isDirectory()) {
			uploadFolder.mkdir();
		}
		String fileName = UUID.randomUUID().toString() + fileExtension;
		Path targetLocation = Path.of(uploadPath + "/" + fileName);

		CommonUtil.copyMultipartFileWithBuffer(file, targetLocation);
		return targetLocation.toString();
	}

	public String generatekey(String contextKey, String machineId) {
		KeyPairGenerator keyGenerator = null;
		try {
			keyGenerator = KeyPairGenerator.getInstance("RSA");
			keyGenerator.initialize(2048, new SecureRandom());
			final KeyPair keypair = keyGenerator.generateKeyPair();
			createKeyFile(String.valueOf(personaConfigPath) + File.separator + "privatekeys//" + machineId + ".reg.key",
					keypair.getPrivate().getEncoded());
			final String publicKey = java.util.Base64.getEncoder().encodeToString(keypair.getPublic().getEncoded());
			return publicKey;
		} catch (NoSuchAlgorithmException e) {
			logger.error(e.getMessage());
		}
		return null;

	}

	private static void createKeyFile(final String fileName, final byte[] key) {
		logger.info("Creating file : " + fileName);
		try {
			Throwable t = null;
			try {
				final FileOutputStream os = new FileOutputStream(fileName);
				try {
					os.write(key);
				} finally {
					if (os != null) {
						os.close();
					}
				}
			} finally {
				final Throwable exception = null;
				t = exception;

			}
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}
}
