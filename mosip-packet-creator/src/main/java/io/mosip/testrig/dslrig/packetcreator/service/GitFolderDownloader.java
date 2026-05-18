package io.mosip.testrig.dslrig.packetcreator.service;

import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GitFolderDownloader {

    private static final Logger logger = LoggerFactory.getLogger(GitFolderDownloader.class);


    private static final String GITHUB_BASE_URL = "https://github.com/jayesh12234/mosip-automation-tests/archive/refs/heads/";


    private static final String DEFAULT_BRANCH = "develop";


    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir") + File.separator + "profile_resource";


    private static final String PROFILE_RESOURCE_BASE = "mosip-packet-creator/src/main/resources/dockersupport/centralized/mosip-packet-creator/profile_resource/";


    private static final String[] REQUIRED_FOLDERS = {
            "location_data",
            "names_data",
            "templates_data",
            "Address",
            "CBEFF_validated_data",
            "documents_data",
            "face_data",
            "fp_data",
            "iris_data"
    };


	public static void getProfileResourceFromGit() {
		if (!System.getProperty("os.name").equalsIgnoreCase("Linux")) {
			try {
				String repoUrl = GITHUB_BASE_URL + DEFAULT_BRANCH + ".zip";
				logger.info("🗑️ Cleaning up old temp directory...");
				deleteOldTempDir();

				logger.info("📥 Downloading required folders from GitHub branch: {}", DEFAULT_BRANCH);
				Path zipFilePath = downloadZip(repoUrl);

				logger.info("✅ Download complete. Extracting required folders...");
				extractRequiredFolders(zipFilePath);

				Files.deleteIfExists(zipFilePath); 
				logger.info("✅ Extraction complete. Required files are in: {}", TEMP_DIR);
			} catch (Exception e) {
				logger.error("❌ Error while processing Git folder download: ", e);
			}
		}
	}

    private static void deleteOldTempDir() throws IOException {
        Path tempPath = Paths.get(TEMP_DIR);
        if (Files.exists(tempPath)) {
            try (Stream<Path> paths = Files.walk(tempPath)) {
                paths.sorted((p1, p2) -> p2.compareTo(p1)) 
                .forEach(path -> {
                    try {
                        Files.delete(path);
                        logger.debug("Deleted: {}", path);
                    } catch (IOException e) {
                        logger.error("❌ Failed to delete {}", path, e);
                        throw new RuntimeException(e);
                    }
                });
            }
            logger.info("🗑️ Deleted old temp directory: {}", TEMP_DIR);
        }
    }

    private static Path downloadZip(String repoUrl) throws IOException {
        Path zipPath = Files.createTempFile("repo", ".zip");
        try (InputStream in = new URL(repoUrl).openStream();
             FileOutputStream out = new FileOutputStream(zipPath.toFile())) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
        logger.info("📥 Downloaded ZIP file: {}", zipPath);
        return zipPath;
    }

    	private static void extractRequiredFolders(Path zipFilePath) throws IOException {
    	    Path tempDir = Paths.get(TEMP_DIR);
    	    Files.createDirectories(tempDir);

    	    String rootFolder = null;

    	    try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipFilePath))) {
    	        ZipEntry entry;
    	        while ((entry = zis.getNextEntry()) != null) {
    	            if (rootFolder == null) {
    	                rootFolder = entry.getName().split("/")[0]; 
    	                logger.info("📁 Detected root folder: {}", rootFolder);
    	            }

    	            for (String requiredSubPath : REQUIRED_FOLDERS) {
    	                String requiredPath = rootFolder + "/" + PROFILE_RESOURCE_BASE + requiredSubPath;

    	                if (entry.getName().startsWith(requiredPath)) {


    	                    String relativePath = entry.getName().substring((rootFolder + "/" + PROFILE_RESOURCE_BASE).length()).replaceFirst("^/+", "");

    	                    if (relativePath.isEmpty()) continue; 

    	                    Path targetPath = tempDir.resolve(relativePath).normalize();


    	                    if (!targetPath.startsWith(tempDir)) {
    	                        throw new IOException("🚨 Security Warning! Attempted Zip Slip: " + entry.getName());
    	                    }

    	                    if (entry.isDirectory()) {
    	                        Files.createDirectories(targetPath);
    	                    } else {
    	                        Files.createDirectories(targetPath.getParent()); 
    	                        Files.copy(zis, targetPath, StandardCopyOption.REPLACE_EXISTING);
    	                    }

    	                    logger.info("✅ Extracted: {}", targetPath);
    	                }
    	            }
    	        }
    	    }
    	}

}
