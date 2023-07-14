package io.mosip.test.packetcreator.mosippacketcreator.service;

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
import org.mosip.dataprovider.models.ExecContext;
import org.mosip.dataprovider.models.setup.MosipMachineModel;
import org.mosip.dataprovider.preparation.MosipDataSetup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import variables.VariableManager;

@Component
public class ContextUtils {

	private String machineId = null;

	@Value("${mosip.test.persona.configpath}")
	private String personaConfigPath;

	Logger logger = LoggerFactory.getLogger(ContextUtils.class);

	public Properties loadServerContext(String ctxName) {
		String filePath = personaConfigPath + "/server.context." + ctxName + ".properties";
		Properties p = new Properties();

		FileReader reader = null;
		try {
			reader = new FileReader(filePath);
			p.load(reader);
		} catch (IOException e) {

			logger.error("loadServerContext " + e.getMessage());
		}

		finally {
			try {
				if (reader != null)
					reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return p;
	}

	public Boolean createUpdateServerContext(Properties props, String ctxName) {

		Boolean bRet = true;
		String filePath = personaConfigPath + "/server.context." + ctxName + ".properties";
		try (FileWriter fr = new FileWriter(filePath);) {

			props.store(fr, "Server Context Attributes");
			bRet = true;

			Properties pp = loadServerContext(ctxName);
			pp.forEach((k, v) -> {
				VariableManager.setVariableValue(ctxName, k.toString(), v.toString());
			});

			String generatePrivateKey = VariableManager.getVariableValue(ctxName, "generatePrivateKey").toString();// pp.getProperty("generatePrivateKey");

			boolean isRequired = Boolean.parseBoolean(generatePrivateKey);
			if (isRequired)
				generateKeyAndUpdateMachineDetail(pp, ctxName);
		} catch (IOException e) {
			logger.error("write:createUpdateServerContext " + e.getMessage());
			bRet = false;
		}
		return bRet;
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
			System.out.println("publicKey: " + publicKey);
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
		System.out.println("Creating file : " + fileName);
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

}
