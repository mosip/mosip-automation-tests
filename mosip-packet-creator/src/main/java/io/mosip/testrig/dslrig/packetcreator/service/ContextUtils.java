package io.mosip.testrig.dslrig.packetcreator.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import io.mosip.testrig.dslrig.dataprovider.models.ExecContext;
import io.mosip.testrig.dslrig.dataprovider.models.setup.MosipMachineModel;
import io.mosip.testrig.dslrig.dataprovider.preparation.MosipDataSetup;
import io.mosip.testrig.dslrig.dataprovider.util.CommonUtil;
import io.mosip.testrig.dslrig.dataprovider.variables.VariableManager;
import io.mosip.testrig.dslrig.dataprovider.util.ServiceException;

@Component
public class ContextUtils {

	private static final Pattern CONTEXT_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9._-]+$");
	private static final String SERVER_CONTEXT_FILE_PREFIX = "server.context.";
	private static final String PROPERTIES_SUFFIX = ".properties";
	private static final String MDS_PROFILE_DIR = "Profile";
	private static final String PRIVATE_KEYS_DIR = "privatekeys";
	private static final String DEFAULT_MDS_PROFILE = "Default";
	private static final String GENERATED_MDS_PROFILE_PREFIX = "res";

	@Value("${mosip.test.persona.configpath}")
	private String personaConfigPath;

	static Logger logger = LoggerFactory.getLogger(ContextUtils.class);

	private static final ConcurrentHashMap<String, Properties> SERVER_CONTEXT_CACHE = new ConcurrentHashMap<>();

	public Properties loadServerContext(String ctxName) {
		Properties cached = SERVER_CONTEXT_CACHE.computeIfAbsent(ctxName, this::loadServerContextFromDisk);
		Properties copy = new Properties();
		copy.putAll(cached);
		return copy;
	}

	private Properties loadServerContextFromDisk(String ctxName) {
		Properties p = new Properties();

		try (FileReader reader = new FileReader(resolveServerContextPath(ctxName).toFile())) {
			p.load(reader);
		} catch (IOException e) {
			logger.error("loadServerContext " + e.getMessage());
		}
		return p;
	}

	public void invalidateServerContextCache(String ctxName) {
		if (ctxName != null) {
			SERVER_CONTEXT_CACHE.remove(ctxName);
		}
	}

	public String createUpdateServerContext(Properties props, String ctxName) throws IOException {
	    Path filePath = resolveServerContextPath(ctxName);

	    try (FileWriter fr = new FileWriter(filePath.toFile())) {

	        props.store(fr, "Server Context Attributes");

	        invalidateServerContextCache(ctxName);
	        Properties pp = loadServerContext(ctxName);
	        pp.forEach((k, v) ->
	                VariableManager.setVariableValue(ctxName, k.toString(), v.toString())
	        );
            CommonUtil.initializeUTCDateFormat(ctxName); 
	        Object generatePrivateKeyObj =
	                VariableManager.getVariableValue(ctxName, "generatePrivateKey");

	        boolean isRequired =
	                generatePrivateKeyObj != null &&
	                Boolean.parseBoolean(generatePrivateKeyObj.toString());

	        if (isRequired) {
	            generateKeyAndUpdateMachineDetail(pp, ctxName);
	        }
	    } catch (ServiceException se) {
	        throw se;
	    }
	    return "true";
	}

	private void validateContextName(String ctxName) {
	    if (ctxName == null || !CONTEXT_NAME_PATTERN.matcher(ctxName).matches()) {
	        throw new IllegalArgumentException("Invalid context name");
	    }
	}

	private Path resolveServerContextPath(String ctxName) {
	    validateContextName(ctxName);
	    Path baseDir = Paths.get(personaConfigPath).normalize();
	    Path filePath = baseDir
	            .resolve(SERVER_CONTEXT_FILE_PREFIX + ctxName + PROPERTIES_SUFFIX)
	            .normalize();
	    if (!filePath.startsWith(baseDir)) {
	        throw new SecurityException("Path traversal attempt detected");
	    }
	    return filePath;
	}


	public static String clearPacketGenFolders(String ctxName) throws IOException {

	    deleteCommaSeparatedPaths(ctxName, "residents_");
	    deleteCommaSeparatedPaths(ctxName, "packets_");
	    deleteCommaSeparatedPaths(ctxName, "preregIds_");
	    deleteCommaSeparatedPaths(ctxName, "Passport_");
	    deleteCommaSeparatedPaths(ctxName, "DrivingLic_");

	    return "Deleted all packet data successfully";
	}

	public static PacketTempPurge.Stats purgeMountedTempDir(String mountPath, String tempPath, long minAgeMs)
			throws IOException {
	    PacketTempPurge.Stats stats = new PacketTempPurge.Stats();
	    if (mountPath == null || mountPath.isBlank() || tempPath == null || tempPath.isBlank()) {
	        return stats;
	    }
	    Path resolved = Paths.get(mountPath + tempPath).toAbsolutePath().normalize();
	    if (resolved.getNameCount() < 2 || resolved.equals(resolved.getRoot())) {
	        logger.warn("Refusing to purge suspiciously shallow mounted temp path: {}", resolved);
	        return stats;
	    }
	    File[] entries = resolved.toFile().listFiles();
	    if (entries == null) {
	        return stats;
	    }
	    for (File entry : entries) {
	        PacketTempPurge.deleteIfStale(entry, minAgeMs, stats, logger);
	    }
	    return stats;
	}

	public static PacketTempPurge.Stats purgeOrphanScratchDirs(long minAgeMs) throws IOException {
	    PacketTempPurge.Stats stats = new PacketTempPurge.Stats();
	    String[] prefixes = { "residents_", "packets_", "preregIds_", "docs_" };
	    File tmpRoot = new File(System.getProperty("java.io.tmpdir"));
	    File[] entries = tmpRoot.listFiles();
	    if (entries == null) {
	        return stats;
	    }
	    for (File entry : entries) {
	        if (!entry.isDirectory()) {
	            continue;
	        }
	        String name = entry.getName();
	        for (String prefix : prefixes) {
	            if (name.startsWith(prefix)) {
	                PacketTempPurge.deleteIfStale(entry, minAgeMs, stats, logger);
	                break;
	            }
	        }
	    }
	    return stats;
	}

	public static PacketTempPurge.Stats purgeGeneratedResourceArtifacts(String personaConfigPath, long minAgeMs) {
		PacketTempPurge.Stats stats = new PacketTempPurge.Stats();
		if (personaConfigPath == null || personaConfigPath.isBlank()) {
			return stats;
		}
		Path baseDir = Paths.get(personaConfigPath).toAbsolutePath().normalize();
		File resourceDir = baseDir.toFile();
		if (!resourceDir.isDirectory()) {
			logger.warn("Skipping resource artifact purge; path is not a directory: {}", baseDir);
			return stats;
		}
		purgeGeneratedServerContextFiles(resourceDir, minAgeMs, stats);
		purgeResourceSubdir(baseDir.resolve(MDS_PROFILE_DIR), baseDir, minAgeMs, stats, true);
		purgeResourceSubdir(baseDir.resolve(PRIVATE_KEYS_DIR), baseDir, minAgeMs, stats, false);
		return stats;
	}

	private static void purgeGeneratedServerContextFiles(File resourceDir, long minAgeMs, PacketTempPurge.Stats stats) {
		File[] entries = resourceDir.listFiles();
		if (entries == null) {
			return;
		}
		for (File entry : entries) {
			if (!entry.isFile() || !isGeneratedServerContextFile(entry.getName())) {
				continue;
			}
			boolean existed = entry.exists();
			PacketTempPurge.deleteIfStale(entry, minAgeMs, stats, logger);
			if (existed && !entry.exists()) {
				String ctxName = contextNameFromServerContextFile(entry.getName());
				if (ctxName != null) {
					SERVER_CONTEXT_CACHE.remove(ctxName);
				}
			}
		}
	}

	private static void purgeResourceSubdir(Path dir, Path baseDir, long minAgeMs, PacketTempPurge.Stats stats,
			boolean generatedMdsProfilesOnly) {
		Path normalized = dir.normalize();
		if (!normalized.startsWith(baseDir) || !normalized.toFile().isDirectory()) {
			return;
		}
		File[] children = normalized.toFile().listFiles();
		if (children == null) {
			return;
		}
		for (File child : children) {
			if (generatedMdsProfilesOnly
					&& (!child.isDirectory() || !isGeneratedMdsProfile(child.getName()))) {
				stats.skipOther();
				continue;
			}
			PacketTempPurge.deleteIfStale(child, minAgeMs, stats, logger);
		}
	}

	private static boolean isGeneratedServerContextFile(String name) {
		return name != null && name.startsWith(SERVER_CONTEXT_FILE_PREFIX) && name.endsWith(PROPERTIES_SUFFIX);
	}

	private static String contextNameFromServerContextFile(String name) {
		if (!isGeneratedServerContextFile(name)) {
			return null;
		}
		return name.substring(SERVER_CONTEXT_FILE_PREFIX.length(), name.length() - PROPERTIES_SUFFIX.length());
	}

	private static boolean isGeneratedMdsProfile(String name) {
		return name != null && name.startsWith(GENERATED_MDS_PROFILE_PREFIX)
				&& !DEFAULT_MDS_PROFILE.equalsIgnoreCase(name);
	}

	private static void deleteCommaSeparatedPaths(String ctxName, String key) throws IOException {
	    Object valueObj = VariableManager.getVariableValue(ctxName, key);
	    if (valueObj != null) {
	        String[] paths = valueObj.toString().split(",");
	        for (String path : paths) {
	            String trimmedPath = path.trim();
	            if (!trimmedPath.isEmpty()) {
	                CommonUtil.deleteOldTempDir(trimmedPath, ctxName);
	            }
	        }
	        VariableManager.removeVariableValue(ctxName, key);
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

		if (!CONTEXT_NAME_PATTERN.matcher(machineId).matches()) {
			throw new ServiceException(HttpStatus.BAD_REQUEST, "INVALID_MACHINE_ID");
		}

	    try {
	        KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
	        keyGenerator.initialize(2048, new SecureRandom());
	        KeyPair keypair = keyGenerator.generateKeyPair();

			Object dbServer = VariableManager.getVariableValue(contextKey, "db-server");
			if (dbServer == null) {
				throw new ServiceException(HttpStatus.BAD_REQUEST, "DB_SERVER_MISSING");
			}
			String dbServerStr = dbServer.toString();
			if (!CONTEXT_NAME_PATTERN.matcher(dbServerStr).matches()) {
				throw new ServiceException(HttpStatus.BAD_REQUEST, "INVALID_DB_SERVER_NAME");
			}
			Path privateKeysDir = Paths.get(personaConfigPath, "privatekeys").normalize();
			Path privateKeyFilePath = privateKeysDir.resolve(dbServerStr + "." + machineId + ".reg.key").normalize();
			if (!privateKeyFilePath.startsWith(privateKeysDir)) {
				throw new ServiceException(HttpStatus.BAD_REQUEST, "INVALID_KEY_PATH");
				}
			String privateKeyPath = privateKeyFilePath.toString();

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
