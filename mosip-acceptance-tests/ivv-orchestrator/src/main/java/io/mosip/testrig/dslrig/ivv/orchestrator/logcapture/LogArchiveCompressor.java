package io.mosip.testrig.dslrig.ivv.orchestrator.logcapture;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Streams a directory tree into a zip file (memory-efficient for large logs).
 */
public final class LogArchiveCompressor {

	private LogArchiveCompressor() {
	}

	public static void zipDirectory(Path sourceDir, Path zipFile) throws IOException {
		Files.createDirectories(zipFile.getParent());
		try (OutputStream fos = Files.newOutputStream(zipFile);
				ZipOutputStream zos = new ZipOutputStream(fos)) {
			Files.walkFileTree(sourceDir, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					if (!attrs.isRegularFile()) {
						return FileVisitResult.CONTINUE;
					}
					Path relative = sourceDir.relativize(file);
					ZipEntry entry = new ZipEntry(relative.toString().replace('\\', '/'));
					entry.setTime(attrs.lastModifiedTime().toMillis());
					zos.putNextEntry(entry);
					try (BufferedInputStream in = new BufferedInputStream(Files.newInputStream(file))) {
						in.transferTo(zos);
					}
					zos.closeEntry();
					return FileVisitResult.CONTINUE;
				}
			});
		}
	}
}
