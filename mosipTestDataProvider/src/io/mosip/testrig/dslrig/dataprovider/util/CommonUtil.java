package io.mosip.testrig.dslrig.dataprovider.util;

import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.xml.bind.DatatypeConverter;

import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mifmif.common.regex.Generex;

import io.mosip.testrig.dslrig.dataprovider.preparation.MosipMasterData;
import io.mosip.testrig.dslrig.dataprovider.variables.VariableManager;

import java.io.*;

public class CommonUtil {
	private static final Logger logger = LoggerFactory.getLogger(CommonUtil.class);
	private static SecureRandom rand = new SecureRandom();

	private static String cachedUtcDateformat;
	private static final String DEFAULT_UTC_DATEFORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
	private static final String MOUNT_PATH_KEY = "mountPath";
	private static final String MOSIP_TEST_TEMP_KEY = "mosip.test.temp";

	private static final String PACKETS_TEMP_KEY = "packets_";

	private static Path normalizeAbsolute(Path path) {
		return path.toAbsolutePath().normalize();
	}

	private static Path getOsTempRoot() {
		String tmpDir = System.getProperty("java.io.tmpdir");
		if (tmpDir == null || tmpDir.isBlank()) {
			return null;
		}
		return normalizeAbsolute(Paths.get(tmpDir));
	}

	private static Path getContextTempRoot(String contextKey) {
		if (contextKey == null || contextKey.isBlank()) {
			return null;
		}
		Object mountObj = VariableManager.getVariableValue(contextKey, MOUNT_PATH_KEY);
		Object tempObj = VariableManager.getVariableValue(contextKey, MOSIP_TEST_TEMP_KEY);
		if (mountObj == null || tempObj == null) {
			return null;
		}

		String mountPath = mountObj.toString();
		String tempPath = tempObj.toString();
		if ((mountPath == null || mountPath.isBlank()) && (tempPath == null || tempPath.isBlank())) {
			return null;
		}
		return normalizeAbsolute(Paths.get(String.valueOf(mountPath) + String.valueOf(tempPath)));
	}

	public static Path validateOutputPath(String outputPath, String contextKey) throws IOException {
		if (outputPath == null || outputPath.isBlank()) {
			throw new IOException("Output path is required");
		}

		if (outputPath.contains("..")) {
			throw new IOException("Invalid output path");
		}

		Path resolved = Paths.get(outputPath).toAbsolutePath().normalize();

		Path matchedRoot = findMatchingAllowedRoot(resolved, contextKey);
		if (matchedRoot == null) {
			throw new IOException("Output path is outside allowed temp roots");
		}

		Path normalizedRoot = matchedRoot.toAbsolutePath().normalize();

		Path safePath = buildPathUnderRoot(normalizedRoot, resolved);
		if (!isPathUnderRoot(safePath, normalizedRoot)) {
			throw new IOException("Path traversal attempt detected");
		}
		if (!safePath.startsWith(normalizedRoot)) {
			throw new IOException("Path traversal attempt detected");
		}

		Path safeParent = safePath.getParent();
		if (safeParent == null) {
			throw new IOException("Invalid output path");
		}

		if (!isPathUnderRoot(safeParent, normalizedRoot)) {
			throw new IOException("Path traversal attempt detected");
		}
		if (!safeParent.startsWith(normalizedRoot)) {
			throw new IOException("Path traversal attempt detected");
		}

		Files.createDirectories(safeParent);

		return safePath;
	}

	private static Path findMatchingAllowedRoot(Path candidate, String contextKey) {
		Path normalizedCandidate = candidate.toAbsolutePath().normalize();

		Path osTempRoot = getOsTempRoot();
		if (osTempRoot != null && isPathUnderRoot(normalizedCandidate, osTempRoot)) {
			return osTempRoot;
		}

		Path ctxTempRoot = getContextTempRoot(contextKey);
		if (ctxTempRoot != null && isPathUnderRoot(normalizedCandidate, ctxTempRoot)) {
			return ctxTempRoot;
		}

		Path packetRoot = findMatchingPacketTempRoot(contextKey, normalizedCandidate);
		if (packetRoot != null) {
			return packetRoot;
		}

		return null;
	}

	private static Path findMatchingPacketTempRoot(String contextKey, Path candidate) {
		if (contextKey == null || contextKey.isBlank()) {
			return null;
		}
		Object packetsVar = VariableManager.getVariableValue(contextKey, PACKETS_TEMP_KEY);
		if (packetsVar == null) {
			return null;
		}
		for (String segment : packetsVar.toString().split(",")) {
			String trimmed = segment.trim();
			if (trimmed.isEmpty()) {
				continue;
			}
			Path packetRoot = normalizeAbsolute(Paths.get(trimmed));
			if (isPathUnderRoot(candidate, packetRoot)) {
				return packetRoot;
			}
		}
		return null;
	}

	/**
	 * Checks that {@code path} lies under {@code root}, using normalized prefix
	 * checks first and real-path resolution when symlinks, junctions, or short
	 * names would otherwise break {@link Path#startsWith}.
	 */
	private static boolean isPathUnderRoot(Path path, Path root) {
		Path normalizedPath = path.toAbsolutePath().normalize();
		Path normalizedRoot = root.toAbsolutePath().normalize();
		if (normalizedPath.toString().contains("..")) {
			return false;
		}
		if (normalizedPath.startsWith(normalizedRoot)) {
			return true;
		}
		Path canonicalRoot = toCanonicalTrustedPath(root);
		if (!canonicalRoot.equals(normalizedRoot) && normalizedPath.startsWith(canonicalRoot)) {
			return true;
		}
		try {
			Path canonicalPath = toCanonicalCandidatePath(normalizedPath);
			return canonicalPath.startsWith(canonicalRoot) || canonicalPath.startsWith(normalizedRoot);
		} catch (IOException ignored) {
			return false;
		}
	}

	/**
	 * Resolves symlinks/junctions for trusted roots only.
	 */
	private static Path toCanonicalTrustedPath(Path trustedRoot) {
		Path normalizedPath = trustedRoot.toAbsolutePath().normalize();
		try {
			if (Files.exists(normalizedPath)) {
				return normalizedPath.toRealPath();
			}
			Path parent = normalizedPath.getParent();
			if (parent != null && Files.exists(parent)) {
				return parent.toRealPath().resolve(normalizedPath.getFileName()).normalize();
			}
		} catch (IOException ignored) {
			// fall through
		}
		return normalizedPath;
	}

	/**
	 * Resolves a candidate path for containment checks after {@code ..} rejection.
	 */
	private static Path toCanonicalCandidatePath(Path normalizedPath) throws IOException {
		if (Files.exists(normalizedPath)) {
			return normalizedPath.toRealPath();
		}
		Path parent = normalizedPath.getParent();
		if (parent != null && Files.exists(parent)) {
			return parent.toRealPath().resolve(normalizedPath.getFileName()).normalize();
		}
		return normalizedPath;
	}

	private static Path buildPathUnderRoot(Path root, Path candidate) throws IOException {
		Path normalizedRoot = root.toAbsolutePath().normalize();
		Path normalizedCandidate = candidate.toAbsolutePath().normalize();
		Path canonicalRoot = toCanonicalTrustedPath(root);

		Path baseRoot;
		Path relative;
		if (normalizedCandidate.startsWith(normalizedRoot)) {
			baseRoot = normalizedRoot;
			relative = normalizedRoot.relativize(normalizedCandidate);
		} else if (normalizedCandidate.startsWith(canonicalRoot)) {
			baseRoot = canonicalRoot;
			relative = canonicalRoot.relativize(normalizedCandidate);
		} else {
			Path canonicalCandidate = toCanonicalCandidatePath(normalizedCandidate);
			if (canonicalCandidate.startsWith(canonicalRoot)) {
				baseRoot = canonicalRoot;
				relative = canonicalRoot.relativize(canonicalCandidate);
			} else if (canonicalCandidate.startsWith(normalizedRoot)) {
				baseRoot = normalizedRoot;
				relative = normalizedRoot.relativize(canonicalCandidate);
			} else {
				throw new IOException("Invalid output path");
			}
		}

		if (relative.isAbsolute() || containsUnsafePathComponent(relative)) {
			throw new IOException("Invalid output path");
		}
		Path safePath = baseRoot;
		for (int i = 0; i < relative.getNameCount(); i++) {
			safePath = safePath.resolve(relative.getName(i));
		}
		return safePath.normalize().toAbsolutePath();
	}

	private static boolean containsUnsafePathComponent(Path relative) {
		for (Path component : relative) {
			String name = component.toString();
			if (name.equals("..") || name.equals(".") || name.isEmpty()) {
				return true;
			}
		}
		return relative.toString().contains("..");
	}

	public static void initializeUTCDateFormat(String contextKey) {
		if (cachedUtcDateformat != null)
			return;
		String dateFormat = MosipMasterData.getValueFromActuators("mosip.utc-datetime-pattern", contextKey);
		if (dateFormat == null || dateFormat.isEmpty())
			dateFormat = DEFAULT_UTC_DATEFORMAT;
		cachedUtcDateformat = dateFormat;
		logger.info("UTC Date Format initialized: {}", dateFormat);
	}

	public static boolean isExists(List<String> missList, String categoryCode) {
		if (missList != null) {
			for (String s : missList) {
				if (s.equals(categoryCode))
					return true;
			}
		}
		return false;
	}

	public static String getJSONObjectAttribute(JSONObject obj, String attrName, String defValue) {
		if (obj.has(attrName))
			return obj.getString(attrName);
		return defValue;
	}

	public static String getHexEncodedHash(byte[] data) throws Exception {
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
			messageDigest.update(data);
			return DatatypeConverter.printHexBinary(messageDigest.digest()).toUpperCase();
		} catch (Exception ex) {
			throw new Exception("Invalid getHexEncodedHash " + ex.getMessage());
		}
	}

	public static String toCaptialize(String text) {
		return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
	}

	public static int[] generateRandomNumbers(int count, int max, int min) {
		int[] rand_nums = new int[count];

		for (int i = 0; i < count; i++) {
			rand_nums[i] = rand.nextInt((max - min) + 1) + min;
		}

		return rand_nums;
	}

	public static String getUTCDateTime(LocalDateTime time) {
		return getUTCDateTime(time, null);
	}

	public static String getUTCDateTime(LocalDateTime time, String contextKey) {
		String pattern = cachedUtcDateformat;
		if (pattern == null || pattern.isEmpty()) {
			initializeUTCDateFormat(contextKey);
			pattern = cachedUtcDateformat;
		}
		if (pattern == null || pattern.isEmpty()) {
			pattern = DEFAULT_UTC_DATEFORMAT;
		}
		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(pattern);
		if (time == null) {
			time = LocalDateTime.now(TimeZone.getTimeZone("UTC").toZoneId());
		}
		return time.format(dateFormat);
	}

	public static String genStringAsperRegex(String regex, String contextKey) throws Exception {
		if (Generex.isValidPattern(regex)) {

			Generex generex = new Generex(regex);

			String randomStr = generex.random();
			if (RestClient.isDebugEnabled(contextKey)) {
				logger.info(randomStr);
			}

			boolean bFound = false;
			do {
				bFound = false;
				if (randomStr.startsWith("^")) {
					int idx = randomStr.indexOf("^");
					randomStr = randomStr.substring(idx + 1);
					bFound = true;
				}
				if (randomStr.endsWith("$")) {
					int idx = randomStr.indexOf("$");
					randomStr = randomStr.substring(0, idx);
					bFound = true;
				}
			} while (bFound);
			return randomStr;
		}
		throw new Exception("invalid regex");

	}

	public static String readFromJSONFile(String filePath) {

		StringBuilder builder = new StringBuilder();
		try (FileReader reader = new FileReader(filePath);) {

			char[] cbuf = new char[1024];
			int n = 0;
			while ((n = reader.read(cbuf)) > 0) {
				builder.append(new String(cbuf, 0, n));
			}
			reader.close();
		} catch (IOException e) {
		}

		return builder.toString();

	}

	public static void CopyRecursivly(Path sourceDirectory, Path targetDirectory) throws IOException {

		try (Stream<Path> paths = Files.walk(sourceDirectory)) {
			paths.forEach(sourcePath -> {

				Path targetPath = targetDirectory.resolve(sourceDirectory.relativize(sourcePath));

				try {
					Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException e) {
					logger.error(e.getMessage());
				}

			});
		}
	}

	public static String generateRandomString(int len) {
		StringBuilder builder = new StringBuilder();
		String alphabet = "abcdefghijklmn opqrstuvwxyz_123456789";

		if (len == 0)
			len = 20;

		for (int i = 0; i < len; i++) {
			builder.append(alphabet.charAt(rand.nextInt(alphabet.length())));
		}
		return builder.toString();
	}

	public static List<File> listFiles(String dirPath) {

		List<File> lstFiles = new ArrayList<File>();

		File dir = new File(dirPath);
		File[] files = dir.listFiles();
		for (File f : files)
			lstFiles.add(f);

		return lstFiles;

	}

	public static String getSHAFromBytes(byte[] byteArray) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		return bytesToHex(md.digest(byteArray));
	}

	private static String bytesToHex(byte[] hash) {
		StringBuffer hexString = new StringBuffer();
		for (int i = 0; i < hash.length; i++) {
			String hex = Integer.toHexString(0xff & hash[i]);
			if (hex.length() == 1) {
				hexString.append('0');
			}
			hexString.append(hex);
		}
		return hexString.toString();
	}

	public static String getSHA(String cbeffStr) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		return bytesToHex(md.digest(cbeffStr.getBytes(StandardCharsets.UTF_8)));
	}

	public static void validateJSONSchema(String schemaJson, String identity) throws ValidationException {

		JSONObject jsonSchema = new JSONObject(schemaJson);

		JSONObject jsonSubject = new JSONObject(identity);

		Schema schema = SchemaLoader.load(jsonSchema);
		schema.validate(jsonSubject);
	}

	public static void saveToTemp(String data, String fileName) {
		try {
			CommonUtil.write(Paths.get("/temp/" + fileName), data.getBytes());
		} catch (IOException e) {
		}
	}

	public static Properties String2Props(String str) {
		Properties props = new Properties();
		String[] parts = str.split(",");
		for (String p : parts) {
			String[] v = p.split("=");
			if (v.length > 0) {
				props.put(v[0].trim(), v[1].trim());
			}
		}
		return props;
	}

	public static void copyFileWithBuffer(Path source, Path destination) {
		try (BufferedInputStream in = new BufferedInputStream(Files.newInputStream(source));
				BufferedOutputStream out = new BufferedOutputStream(Files.newOutputStream(destination))) {
			byte[] buffer = new byte[8192];
			int bytesRead;
			while ((bytesRead = in.read(buffer)) != -1) {
				out.write(buffer, 0, bytesRead);
			}

			out.flush();
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

	public static void copyMultipartFileWithBuffer(MultipartFile sourceFile, Path destination) {
		try (InputStream inputStream = sourceFile.getInputStream();
				BufferedOutputStream outputStream = new BufferedOutputStream(Files.newOutputStream(destination))) {

			byte[] buffer = new byte[8192];
			int bytesRead;

			while ((bytesRead = inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, bytesRead);
			}

			outputStream.flush();
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}

	public static void write(Path filePath, byte[] bytes) throws IOException {
		Files.write(filePath, bytes);
	}

	public static void write(byte[] bytes, File file) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		try (OutputStream outputStream = new FileOutputStream(file)) {
			mapper.writeValue(outputStream, bytes);
		}
	}

	public static byte[] read(String path) {
		byte[] data = null;
		try {
			data = Files.readAllBytes(Paths.get(path));
		} catch (Exception e) {
			logger.error(e.getMessage());
		}

		return data;
	}

	public static void main(String[] args) throws Exception {
		String regex5 = "^(1869|18[7-9][0-9]|19[0-9][0-9]|20[0-9][0-9])/([0][1-9]|1[0-2])/([0][1-9]|[1-2][0-9]|3[01])$";
		String rex = regex5;
		String contextKey = null;
		String values = genStringAsperRegex(rex, contextKey);
		Pattern p = Pattern.compile(rex);
		Matcher m = p.matcher(values);
		boolean b = m.matches();

	}

	public static void deleteOldTempDir(String folderPath) throws IOException {
		deleteOldTempDir(folderPath, null);
	}

	public static void deleteOldTempDir(String folderPath, String contextKey) throws IOException {
		if (folderPath == null || folderPath.isBlank()) {
			return;
		}

		if (folderPath.contains("..")) {
			logger.warn("Refusing to delete path with traversal sequence: {}", folderPath);
			return;
		}

		Path tempPath = normalizeAbsolute(Paths.get(folderPath));
		Path osTempRoot = getOsTempRoot();
		Path ctxTempRoot = getContextTempRoot(contextKey);

		Path normalizedOsTempRoot = osTempRoot != null ? normalizeAbsolute(osTempRoot) : null;
		Path normalizedCtxTempRoot = ctxTempRoot != null ? normalizeAbsolute(ctxTempRoot) : null;

		boolean underOsTemp = normalizedOsTempRoot != null && tempPath.startsWith(normalizedOsTempRoot);
		boolean underCtxTemp = normalizedCtxTempRoot != null && tempPath.startsWith(normalizedCtxTempRoot);

		if (!underOsTemp && !underCtxTemp) {
			logger.warn("Refusing to delete path outside allowed temp roots: {}", tempPath);
			return;
		}

		if (Files.exists(tempPath)) {
			try (Stream<Path> paths = Files.walk(tempPath)) {
				paths.sorted((p1, p2) -> p2.compareTo(p1))
						.forEach(path -> {
							try {
								Path normalizedPath = normalizeAbsolute(path);
								if (normalizedPath.toString().contains("..")) {
									logger.warn("Skipping deletion of path with traversal sequence: {}", normalizedPath);
									return;
								}

								boolean pathUnderOsTemp = normalizedOsTempRoot != null
										&& normalizedPath.startsWith(normalizedOsTempRoot);
								boolean pathUnderCtxTemp = normalizedCtxTempRoot != null
										&& normalizedPath.startsWith(normalizedCtxTempRoot);

								if (!pathUnderOsTemp && !pathUnderCtxTemp) {
									logger.warn("Skipping deletion of path outside allowed roots: {}", normalizedPath);
									return;
								}

								Files.delete(normalizedPath);
								logger.info("Deleted: {}", normalizedPath);
							} catch (IOException e) {
								logger.error("❌ Failed to delete {}", path, e);
								throw new RuntimeException(e);
							}
						});
			}
			logger.info("🗑️ Deleted old temp directory: {}", tempPath);
		}
	}

	public static void createFileIfNotExists(Path path) {
		createFileIfNotExists(path, null);
	}

	public static void createFileIfNotExists(Path path, String contextKey) {
		Path normalized = normalizeAbsolute(path);
		Path osTempRoot = getOsTempRoot();
		Path ctxTempRoot = getContextTempRoot(contextKey);
		boolean allowed = (osTempRoot != null && normalized.startsWith(osTempRoot))
				|| (ctxTempRoot != null && normalized.startsWith(ctxTempRoot));
		if (!allowed) {
			logger.warn("Refusing to create file outside allowed temp roots: {}", normalized);
			return;
		}

		try {
			Files.createDirectories(normalized.getParent());
			Files.createFile(normalized);
		} catch (FileAlreadyExistsException e) {
			logger.error("File already exists: {}", normalized);
		} catch (IOException e) {

			logger.error("Error creating file: {}", normalized, e);
		}
	}

	public static void addTempDir(String contextKey, String variableKey, String newDir) {
		try {

			Object existing = VariableManager.getVariableValue(contextKey, variableKey);

			String updatedValue = (existing == null || existing.toString().isEmpty())
					? newDir
					: existing + "," + newDir;

			VariableManager.setVariableValue(
					contextKey,
					variableKey,
					updatedValue);

		} catch (Exception e) {
			logger.error("Failed to add temp dir: {}", newDir, e);
		}
	}

	public static void cleanupPreregArtifacts(String location, File targetDirectory) {
		if (location != null && !location.isBlank()) {
			try {
				CommonUtil.deleteOldTempDir(location);
			} catch (Exception ex) {
				logger.warn("Failed to delete prereg zip {}", location, ex);
			}
		}
		if (targetDirectory != null && targetDirectory.exists()) {
			try {
				CommonUtil.deleteOldTempDir(targetDirectory.getAbsolutePath());
			} catch (Exception ex) {
				logger.warn("Failed to delete prereg working directory {}", targetDirectory.getAbsolutePath(), ex);
			}
		}
	}
}
