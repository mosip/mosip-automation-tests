package io.mosip.testrig.dslrig.packetcreator.service;

import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GitFolderDownloader {

    private static final Logger logger = LoggerFactory.getLogger(GitFolderDownloader.class);

    private static final String REPO_URL = "https://github.com/mosip/mosip-automation-tests/archive/refs/heads/develop.zip";
    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir") + File.separator + "profile_resource";

    private static final String[] REQUIRED_FOLDERS = {
            "mosip-automation-tests-develop/mosip-packet-creator/src/main/resources/dockersupport/centralized/mountvolume/profile_resource/location_data",
            "mosip-automation-tests-develop/mosip-packet-creator/src/main/resources/dockersupport/centralized/mountvolume/profile_resource/names_data",
            "mosip-automation-tests-develop/mosip-packet-creator/src/main/resources/dockersupport/centralized/mountvolume/profile_resource/templates_data",
            "mosip-automation-tests-develop/mosip-packet-creator/src/main/resources/dockersupport/centralized/mountvolume/profile_resource/Address"

    };

    public static void getProfileResourceFromGit() {
        try {
            logger.info("🗑️ Cleaning up old temp directory...");
            deleteOldTempDir();

            logger.info("📥 Downloading required folders from GitHub...");
            Path zipFilePath = downloadZip();

            logger.info("✅ Download complete. Extracting required folders...");
            extractRequiredFolders(zipFilePath);

            Files.deleteIfExists(zipFilePath); // Cleanup ZIP file
            logger.info("✅ Extraction complete. Required files are in: {}", TEMP_DIR);
        } catch (Exception e) {
            logger.error("❌ Error while processing Git folder download: ", e);
        }
    }

    // 🔹 Delete old TEMP_DIR if it exists
    private static void deleteOldTempDir() throws IOException {
        Path tempPath = Paths.get(TEMP_DIR);
        if (Files.exists(tempPath)) {
            Files.walk(tempPath)
                .sorted((p1, p2) -> p2.compareTo(p1)) // Delete files first
                .forEach(path -> {
                    try {
                        Files.delete(path);
                        logger.debug("Deleted: {}", path);
                    } catch (IOException e) {
                        logger.error("❌ Failed to delete {}", path, e);
                        throw new RuntimeException(e);
                    }
                });
            logger.info("🗑️ Deleted old temp directory: {}", TEMP_DIR);
        }
    }

    private static Path downloadZip() throws IOException {
        Path zipPath = Files.createTempFile("repo", ".zip");
        try (InputStream in = new URL(REPO_URL).openStream();
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
        Files.createDirectories(Paths.get(TEMP_DIR));

        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipFilePath))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                for (String requiredPath : REQUIRED_FOLDERS) {
                    if (entry.getName().startsWith(requiredPath)) {
                        Path targetPath = Paths.get(TEMP_DIR, entry.getName().substring(requiredPath.lastIndexOf("/") + 1));
                        if (entry.isDirectory()) {
                            Files.createDirectories(targetPath);
                        } else {
                            Files.copy(zis, targetPath, StandardCopyOption.REPLACE_EXISTING);
                        }
                        logger.info("✅ Extracted: {}", targetPath);
                    }
                }
            }
        }
    }
}
