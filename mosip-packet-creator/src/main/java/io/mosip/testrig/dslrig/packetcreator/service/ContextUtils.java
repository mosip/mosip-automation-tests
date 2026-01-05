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
import java.util.Base64;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import io.mosip.testrig.apirig.utils.ErrorCodes;
import io.mosip.testrig.dslrig.dataprovider.models.ExecContext;
import io.mosip.testrig.dslrig.dataprovider.models.setup.MosipMachineModel;
import io.mosip.testrig.dslrig.dataprovider.preparation.MosipDataSetup;
import io.mosip.testrig.dslrig.dataprovider.util.CommonUtil;
import io.mosip.testrig.dslrig.dataprovider.util.RestClient;
import io.mosip.testrig.dslrig.dataprovider.variables.VariableManager;
import io.mosip.testrig.dslrig.dataprovider.util.ServiceException;

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

	public String createUpdateServerContext(Properties props, String ctxName) throws IOException {
	    String filePath = personaConfigPath + "/server.context." + ctxName + ".properties";
	    try (FileWriter fr = new FileWriter(filePath)) {
	        props.store(fr, "Server Context Attributes");

	        Properties pp = loadServerContext(ctxName);
	        pp.forEach((k, v) -> VariableManager.setVariableValue(ctxName, k.toString(), v.toString()));

	        String generatePrivateKey = VariableManager.getVariableValue(ctxName, "generatePrivateKey").toString();
	        boolean isRequired = Boolean.parseBoolean(generatePrivateKey);
	        if (isRequired) {
	            // let generateKeyAndUpdateMachineDetail throw ServiceException if anything wrong
	            generateKeyAndUpdateMachineDetail(pp, ctxName);
	        }
	    } catch (ServiceException se) {
	        throw se;
	    }
	    return "true";
	}


	public static String clearPacketGenFolders(String ctxName) throws IOException {

	    deleteCommaSeparatedPaths(ctxName, "residents_");
	    deleteCommaSeparatedPaths(ctxName, "packets_");
	    deleteCommaSeparatedPaths(ctxName, "preregIds_");
	    deleteCommaSeparatedPaths(ctxName, "Passport_");
	    deleteCommaSeparatedPaths(ctxName, "DrivingLic_");

	    return "Deleted all packet data successfully";
	}

	private static void deleteCommaSeparatedPaths(String ctxName, String key) throws IOException {
	    Object valueObj = VariableManager.getVariableValue(ctxName, key);
	    if (valueObj != null) {
	        String[] paths = valueObj.toString().split(",");
	        for (String path : paths) {
	            String trimmedPath = path.trim();
	            if (!trimmedPath.isEmpty()) {
	                CommonUtil.deleteOldTempDir(trimmedPath);
	            }
	        }
	    }
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

	    String machineId = contextProperties.getProperty("mosip.test.regclient.machineid");
	    if (machineId == null || machineId.isEmpty()) {
	        throw new ServiceException(
	                HttpStatus.BAD_REQUEST,
	                "MACHINE_ID_MISSING"
	        );
	    }

	    try {
	        KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
	        keyGenerator.initialize(2048, new SecureRandom());
	        KeyPair keypair = keyGenerator.generateKeyPair();

	        String privateKeyPath = personaConfigPath + File.separator + "privatekeys" + File.separator
	                + VariableManager.getVariableValue(contextKey, "db-server") + "." + machineId + ".reg.key";

	        createKeyFile(privateKeyPath, keypair.getPrivate().getEncoded());

	        String publicKey = Base64.getEncoder().encodeToString(keypair.getPublic().getEncoded());
	        if (publicKey == null || publicKey.isEmpty()) {
	            throw new ServiceException(
	                    HttpStatus.INTERNAL_SERVER_ERROR,
	                    "PUBLIC_KEY_EMPTY"
	            );
	        }

	        List<MosipMachineModel> machines;
	        String status = contextProperties.getProperty("machineStatus");

	        if ("deactive".equalsIgnoreCase(status)) {
	            machines = MosipDataSetup.searchMachineDetail(machineId, "eng", contextKey);
	        } else {
	            machines = MosipDataSetup.getMachineDetail(machineId, " ", contextKey);
	        }

	        if (machines == null || machines.isEmpty()) {
	            throw new ServiceException(
	                    HttpStatus.NOT_FOUND,
	                    "MACHINE_NOT_FOUND",
	                    machineId
	            );
	        }

	        boolean updated = false;
	        for (MosipMachineModel model : machines) {
	            if (model != null && machineId.equalsIgnoreCase(model.getId())) {
	                model.setPublicKey(publicKey);
	                model.setSignPublicKey(publicKey);
	                model.setName(RandomStringUtils.randomAlphanumeric(10).toUpperCase());
	                MosipDataSetup.updateMachine(model, contextKey);
	                updated = true;
	                break;
	            }
	        }

	        if (!updated) {
	            throw new ServiceException(
	                    HttpStatus.NOT_FOUND,
	                    "MACHINE_NOT_FOUND",
	                    machineId
	            );
	        }

	    } catch (ServiceException se) {
	        throw se;
	    } catch (NoSuchAlgorithmException e) {
	        throw new ServiceException(
	                HttpStatus.INTERNAL_SERVER_ERROR,
	                "KEYGEN_FAIL",
	                e.getMessage()
	        );
	    } catch (Exception e) {
	        throw new ServiceException(
	                HttpStatus.INTERNAL_SERVER_ERROR,
	                "KEY_FILE_WRITE_FAIL",
	                e.getMessage()
	        );
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
			 logger.error(e.getMessage());
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
