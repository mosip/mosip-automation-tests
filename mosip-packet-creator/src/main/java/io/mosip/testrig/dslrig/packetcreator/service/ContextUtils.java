package io.mosip.testrig.dslrig.packetcreator.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.mosip.testrig.dslrig.dataprovider.models.ExecContext;
import io.mosip.testrig.dslrig.dataprovider.models.setup.MosipMachineModel;
import io.mosip.testrig.dslrig.dataprovider.preparation.MosipDataSetup;
import io.mosip.testrig.dslrig.dataprovider.util.RestClient;
import io.mosip.testrig.dslrig.dataprovider.variables.VariableManager;

@Component
public class ContextUtils {

	private String machineId = null;

	@Value("${mosip.test.persona.configpath}")
	private String personaConfigPath;

	static Logger logger = LoggerFactory.getLogger(ContextUtils.class);

	public Properties loadServerContext(String ctxName) {
		String filePath = personaConfigPath + "/server.context." + ctxName + ".properties";
		Properties p = new Properties();

		try(FileReader reader = new FileReader(filePath);) {
			
			p.load(reader);
		} catch (IOException e) {

			logger.error("loadServerContext " + e.getMessage());
		}
		return p;
	}

	public String createUpdateServerContext(Properties props, String ctxName) {

		String filePath = personaConfigPath + "/server.context." + ctxName + ".properties";
		try (FileWriter fr = new FileWriter(filePath);) {

			props.store(fr, "Server Context Attributes");

			Properties pp = loadServerContext(ctxName);
			pp.forEach((k, v) -> {
				VariableManager.setVariableValue(ctxName, k.toString(), v.toString());
			});

			String generatePrivateKey = VariableManager.getVariableValue(ctxName, "generatePrivateKey").toString();// pp.getProperty("generatePrivateKey");

			boolean isRequired = Boolean.parseBoolean(generatePrivateKey);
			if (isRequired)
				generateKeyAndUpdateMachineDetail(pp, ctxName);
		
	// Remove the temp directories created for the same context	
	// Commenting below line as it is impacting the scenarios where we need to reuse the persona file and documents with different set of data 	
    //			clearPacketGenFolders(ctxName);

		} catch (IOException e) {
			logger.error("write:createUpdateServerContext " + e.getMessage());
			return e.getMessage();
		}
		return "true";
	}

	private void clearPacketGenFolders(String ctxName) {
		// TODO Auto-generated method stub

		if (VariableManager.getVariableValue(ctxName, "residents_") != null)
			deleteDirectoryPath(VariableManager.getVariableValue(ctxName, "residents_").toString());

		if (VariableManager.getVariableValue(ctxName, "packets_") != null)
			deleteDirectoryPath(VariableManager.getVariableValue(ctxName, "packets_").toString());

		if (VariableManager.getVariableValue(ctxName, "preregIds_") != null)
			deleteDirectoryPath(VariableManager.getVariableValue(ctxName, "preregIds_").toString());

		if (VariableManager.getVariableValue(ctxName, "Passport_") != null)
			deleteDirectoryPath(VariableManager.getVariableValue(ctxName, "Passport_").toString());

		if (VariableManager.getVariableValue(ctxName, "DrivingLic_") != null)
			deleteDirectoryPath(VariableManager.getVariableValue(ctxName, "DrivingLic_").toString());
	}

	public String createExecutionContext(String serverContextKey) {

		String uid = UUID.randomUUID().toString();
		ExecContext context = new ExecContext();
		context.setKey(uid);
		Properties p = loadServerContext(serverContextKey);
		context.setProperties(p);
		return uid;
	}

	public static String ProcessFromTemplate(String src, String templatePacketLocation) {
		String process = null;
		if (templatePacketLocation == null)
			return process;
		Path fPath = Path.of(templatePacketLocation + "/" + src.toUpperCase());
		for (File f : fPath.toFile().listFiles()) {
			if (f.isDirectory()) {
				process = f.getName();
				break;
			}

		}
		return process;
	}

	public static Path idJsonPathFromTemplate(String src, String templatePacketLocation) {
		Path fPath = Path.of(templatePacketLocation + "/" + src.toUpperCase());
		String process = null;

		for (File f : fPath.toFile().listFiles()) {
			if (f.isDirectory()) {
				process = f.getName();
				break;
			}

		}
		if (process != null) {
			fPath = Path.of(templatePacketLocation + "/" + src.toUpperCase() + "/" + process + "/rid_id/ID.json");
			return fPath;
		}
		return null;
	}

	public void generateKeyAndUpdateMachineDetail(Properties contextProperties, String contextKey) {
		KeyPairGenerator keyGenerator = null;
		boolean isMachineDetailFound = false;
		machineId = contextProperties.getProperty("mosip.test.regclient.machineid");
		if (machineId == null || machineId.isEmpty())
			throw new RuntimeException("MachineId is null or empty!");

		try {
			keyGenerator = KeyPairGenerator.getInstance("RSA");
			keyGenerator.initialize(2048, new SecureRandom());
			final KeyPair keypair = keyGenerator.generateKeyPair();

			createKeyFile(String.valueOf(personaConfigPath) + File.separator + "privatekeys" + File.separator
					+ VariableManager.getVariableValue(contextKey, "db-server").toString() + "." + machineId
					+ ".reg.key", keypair.getPrivate().getEncoded());

			final String publicKey = java.util.Base64.getEncoder().encodeToString(keypair.getPublic().getEncoded());
			if (RestClient.isDebugEnabled(contextKey)) {
				logger.info("publicKey: " + publicKey);
			}
			if (publicKey != null && !publicKey.isEmpty()) {
				List<MosipMachineModel> machines = null;
				String status = contextProperties.getProperty("machineStatus");
				if (status != null && status.equalsIgnoreCase("deactive"))
					machines = MosipDataSetup.searchMachineDetail(machineId, "eng", contextKey);
				else
					machines = MosipDataSetup.getMachineDetail(machineId, " ", contextKey);
				if (machines != null && !machines.isEmpty()) {
					for (MosipMachineModel mosipMachineModel : machines) {
						if (mosipMachineModel != null && mosipMachineModel.getId().equalsIgnoreCase(machineId)) { // removed
							mosipMachineModel.setSignPublicKey(publicKey);
							mosipMachineModel.setPublicKey(publicKey);
							mosipMachineModel.setName(RandomStringUtils.randomAlphanumeric(10).toUpperCase());
							MosipDataSetup.updateMachine(mosipMachineModel, contextKey);
							isMachineDetailFound = true;
							break;
						}
					}
					if (!isMachineDetailFound)
						throw new RuntimeException("MachineId : " + machineId + " details not found in DB.");
				} else
					throw new RuntimeException("MachineId : " + machineId + " details not found in DB.");
			}
		} catch (NoSuchAlgorithmException e) {
			logger.error(e.getMessage());
		}
	}

	private static void createKeyFile(final String fileName, final byte[] key) {
		logger.info("Creating file : " + fileName);
		try (final FileOutputStream os = new FileOutputStream(fileName);) {
			Throwable t = null;
			try {

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
			// logger.error(e.getMessage());
		}
	}

	public void deleteDirectoryPath(String path) {
		if (path != null && !path.isEmpty()) {
			File file = new File(path);
			if (file.exists()) {
				do {
					deleteIt(file);
				} while (file.exists());
			} else {
			}
		}
	}

	private void deleteIt(File file) {
		if (file.isDirectory()) {
			String fileList[] = file.list();
			if (fileList.length == 0) {
				if (!file.delete()) {
					logger.info("Files deleted");
				}
			} else {
				int size = fileList.length;
				for (int i = 0; i < size; i++) {
					String fileName = fileList[i];
					String fullPath = file.getPath() + "/" + fileName;
					File fileOrFolder = new File(fullPath);
					deleteIt(fileOrFolder);
				}
			}
		} else {
			if (!file.delete()) {
				logger.info("Files deleted");
			}
		}
	}

}
