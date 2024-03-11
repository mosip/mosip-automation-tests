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
	// Uses java.util.zip to create zip file

	/*
	 * public void zipFolder(Path sourceFolderPath, Path zipPath, String contextKey)
	 * throws IOException { try (ZipOutputStream zos = new ZipOutputStream(new
	 * FileOutputStream(zipPath.toFile()))) { Files.walkFileTree(sourceFolderPath,
	 * new SimpleFileVisitor<Path>() { public FileVisitResult visitFile(Path file,
	 * BasicFileAttributes attrs) throws IOException {
	 * 
	 * String targetFile = sourceFolderPath.relativize(file).toString();
	 * RestClient.logInfo(contextKey, "OS=>" + System.getProperty("os.name")); if
	 * (System.getProperty("os.name").toLowerCase().contains("windows")) targetFile
	 * = targetFile.replace("\\", "/");
	 * 
	 * RestClient.logInfo(contextKey,
	 * " In ZipUtils : zipFolder ()--- sourceFolderPath : " + sourceFolderPath +
	 * " zipPath : " + zipPath + " targetFile : " + targetFile);
	 * zos.putNextEntry(new ZipEntry(targetFile)); Files.copy(file, zos);
	 * zos.closeEntry(); return FileVisitResult.CONTINUE; } }); } catch (IOException
	 * ex) { logger.error("Error during zip", ex); }
	 * 
	 * }
	 */

	public void zipFolder(Path sourceFolderPath, BufferedOutputStream stream, String contextKey) throws IOException {
		try (ZipOutputStream zos = new ZipOutputStream(stream)) {
			Files.walkFileTree(sourceFolderPath, new SimpleFileVisitor<Path>() {
				private ZipEntry zipEntry;

				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					String targetFile = sourceFolderPath.relativize(file).toString();
					if (System.getProperty("os.name").toLowerCase().contains("windows")) {
						targetFile = targetFile.replace("\\", "/");
					}
					RestClient.logInfo(contextKey, "OS=>" + System.getProperty("os.name"));
//					RestClient.logInfo(contextKey, "In ZipUtils : zipFolder ()--- sourceFolderPath : "
//							+ sourceFolderPath + " zipPath : " + zipPath + " targetFile : " + targetFile);
					zipEntry = new ZipEntry(targetFile);
					zos.putNextEntry(zipEntry);
					try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file.toFile()))) {
						byte[] buffer = new byte[8192]; // Experiment with different buffer sizes
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
		}
	}

	public boolean unzip(String sourceFile, String targetDirectory, String contextKey) {
		boolean unzipped = false;
		try (InputStream in = Files.newInputStream(Path.of(sourceFile));
				ZipInputStream zipInputStream = new ZipInputStream(in)) {
			ZipEntry zipEntry = zipInputStream.getNextEntry();
			while (zipEntry != null) {
				String fileName = zipEntry.getName();
				Path file = Path.of(targetDirectory, fileName);
				if (file.toFile().createNewFile()) {
					try (OutputStream os = Files.newOutputStream(file);
							BufferedOutputStream bos = new BufferedOutputStream(os)) {
						byte[] buffer = new byte[8192]; // Experiment with different buffer sizes
						int len;
						while ((len = zipInputStream.read(buffer)) > 0) {
							bos.write(buffer, 0, len);
						}
						// Note: Flushing moved outside the loop
						bos.flush();
					}
					zipEntry = zipInputStream.getNextEntry();
					unzipped = true;
				}
			}
		} catch (IOException e) {
			logger.error("Error while unzip", e);
			unzipped = false;
		}
		return unzipped;
	}

	/*
	 * public boolean unzip(String sourceFile, String targetDirectory,String
	 * contextKey) { boolean unzipped = false; try(InputStream in =
	 * Files.newInputStream(Path.of(sourceFile)); ZipInputStream zipInputStream =
	 * new ZipInputStream(in)){ ZipEntry zipEntry = zipInputStream.getNextEntry();
	 * 
	 * while (zipEntry != null) { String fileName = zipEntry.getName(); Path file =
	 * Path.of(targetDirectory,fileName); if(file.toFile().createNewFile()) { try
	 * (OutputStream os = Files.newOutputStream(file)) { byte[] buffer = new
	 * byte[1024]; int len; while ((len = zipInputStream.read(buffer)) > 0) {
	 * os.write(buffer, 0, len); os.flush(); } } zipEntry =
	 * zipInputStream.getNextEntry(); unzipped = true; }
	 * 
	 * 
	 * } } catch (IOException e) { logger.error("Error while unzip", e); unzipped =
	 * false; } return unzipped; }
	 */
}
