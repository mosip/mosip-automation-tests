package io.mosip.testrig.dslrig.packetcreator.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

//import io.kubernetes.client.util.ClientBuilder;

// import io.kubernetes.client.util.KubeConfig;

// import io.kubernetes.client.openapi.ApiClient;
// import io.kubernetes.client.openapi.ApiException;
// import io.kubernetes.client.openapi.Configuration;
// import io.kubernetes.client.openapi.apis.CoreV1Api;
// import io.kubernetes.client.openapi.models.V1Pod;
// import io.kubernetes.client.openapi.models.V1PodList;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
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
import org.springframework.security.crypto.codec.Base64;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import io.mosip.testrig.dslrig.dataprovider.util.CommonUtil;
import io.mosip.testrig.dslrig.dataprovider.util.RestClient;
import io.mosip.testrig.dslrig.dataprovider.variables.VariableManager;

@Service
public class CommandsService {


	
	private static final Logger logger = LoggerFactory.getLogger(CommandsService.class);

	public String checkContext(String contextKey, String module, String eSignetDeployed) throws IOException {

		//Properties props = CommonUtil.loadServerContextProperties(contextKey,personaConfigPath);//delete later
		String baseUrl = VariableManager.getVariableValue(contextKey, "mosip.test.baseurl").toString();
		// v1/keymanager/decrypt
		/// v1/keymanager/encrypt

		String pinglistfile=VariableManager.getVariableValue(VariableManager.NS_DEFAULT, "mosip.test.pinglistfile").toString();
		
			
		File file = new File(pinglistfile);
		// FileReader fr=new FileReader(file);
		// BufferedReader br=new BufferedReader(fr);
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
				// enhanced to support module
				String controllerPath = line.trim();
				String modName = null;
				String[] parts = controllerPath.split("=");
				if (parts.length > 1) {

					// Perform the health check only if esignet is deployed
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
					Boolean bRet1 = RestClient.checkActuatorNoAuth(baseUrl + controllerPath.trim(), contextKey);
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
		} catch (Exception ex) {
			logger.error(ex.getMessage());
		}

		return retJson.toString();
	}

	public String writeToFile(String contextKey, Properties requestData, long offset) throws IOException {

		// take file name
		String filePath = requestData.getProperty("filePath");
		String base64data = requestData.getProperty("base64data");
		byte[] data = Base64.decode(base64data.getBytes());
		File myFile = new File(filePath);
		// Create the accessor with read-write access.

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

		String propertyPath=VariableManager.getVariableValue(VariableManager.NS_DEFAULT, "mosip.test.testcase.propertypath").toString();
	
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

		String uploadPath=VariableManager.getVariableValue(VariableManager.NS_DEFAULT, "mosip.test.uploads").toString();

		String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
		fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
		File uploadFolder = new File(uploadPath);
		if (!uploadFolder.exists() || !uploadFolder.isDirectory()) {
			uploadFolder.mkdir();
		}
		String fileName = UUID.randomUUID().toString() + fileExtension;
		Path targetLocation = Path.of(uploadPath + "/" + fileName);
//		Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
		CommonUtil.copyMultipartFileWithBuffer(file, targetLocation);
		return targetLocation.toString();
	}

	public String generatekey(String contextKey, String machineId) {
		KeyPairGenerator keyGenerator = null;
		try {
			String personaConfigPath=VariableManager.getVariableValue(VariableManager.NS_DEFAULT, "mosip.test.persona.configpath").toString();
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
