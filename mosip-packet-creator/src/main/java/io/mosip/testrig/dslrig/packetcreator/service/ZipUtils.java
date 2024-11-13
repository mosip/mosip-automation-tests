package io.mosip.testrig.dslrig.packetcreator.service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.mosip.testrig.dslrig.dataprovider.util.RestClient;

@Component
public class ZipUtils {
	Logger logger = LoggerFactory.getLogger(ZipUtils.class);
	public static boolean isWindows = System.getProperty("os.name").toLowerCase().contains("windows") ? true : false;

	public void zipFolder(Path sourceFolderPath, BufferedOutputStream stream, String contextKey) throws IOException {
		try (ZipOutputStream zos = new ZipOutputStream(stream)) {
			Files.walkFileTree(sourceFolderPath, new SimpleFileVisitor<Path>() {
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					String targetFile = sourceFolderPath.relativize(file).toString();

			if (isWindows) {
						targetFile = targetFile.replace("\\", "/");
					}
					RestClient.logInfo(contextKey, "Zipping file: " + targetFile);

					zos.putNextEntry(new ZipEntry(targetFile));
					try (InputStream bis = Files.newInputStream(file)) {
						byte[] buffer = new byte[32768];  // Increased buffer size
						int bytesRead;
						while ((bytesRead = bis.read(buffer)) != -1) {
							zos.write(buffer, 0, bytesRead);
						}
					}
					zos.closeEntry();
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException ex) {
			logger.error("Error during zip", ex);
			throw ex;  // Rethrow exception for better error handling
		}
	}

	public boolean unzip(String sourceFile, String targetDirectory, String contextKey) {
		boolean unzipped = false;
		try (InputStream in = Files.newInputStream(Path.of(sourceFile));
			 ZipInputStream zipInputStream = new ZipInputStream(in)) {
			ZipEntry zipEntry = zipInputStream.getNextEntry();
			while (zipEntry != null) {
				Path file = Path.of(targetDirectory, zipEntry.getName());

				// Check if the directory structure needs to be created
				Files.createDirectories(file.getParent());

				try (OutputStream os = Files.newOutputStream(file);
					 BufferedOutputStream bos = new BufferedOutputStream(os, 32768)) { // Increased buffer size
					byte[] buffer = new byte[32768];
					int len;
					while ((len = zipInputStream.read(buffer)) > 0) {
						bos.write(buffer, 0, len);
					}
				}
				zipEntry = zipInputStream.getNextEntry();
				unzipped = true;
			}
		} catch (IOException e) {
			logger.error("Error while unzipping", e);
		}
		return unzipped;
	}
}
