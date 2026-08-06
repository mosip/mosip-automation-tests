
package io.mosip.testrig.dslrig.ivv.orchestrator;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;


public class TestResources {
	public static Logger logger=Logger.getLogger(TestResources.class);
	public static String resourceFolderName="MosipTemporaryTestResource";;
	public static String resourceTestFolderName="MosipTestResource";;
	public static String jarUrl = TestResources.class.getProtectionDomain().getCodeSource().getLocation().getPath();

	public static void copyPreRegTestResource() {
		try {
			File source = new File(TestResources.getGlobalResourcePaths() + "/preReg");
			File source2 = new File(TestResources.getGlobalResourcePaths()+"/config");
			File source3 = new File(TestResources.getGlobalResourcePaths() + "/idaData");
			File source4 = new File(TestResources.getGlobalResourcePaths() + "/regproc");
			File source6 = new File(TestResources.getGlobalResourcePaths()+"/syncdata");
			File source7 = new File(TestResources.getGlobalResourcePaths()+"/ivv_masterdata");
			File destination = new File(TestResources.getGlobalResourcePaths() + "/"+TestResources.resourceFolderName);
			FileUtils.copyDirectoryToDirectory(source, destination);
			String path=TestResources.getGlobalResourcePaths().replace("classes", "test-classes");

			File destination2 = new File(path);
			FileUtils.copyDirectoryToDirectory(source2, destination2);
			FileUtils.copyDirectoryToDirectory(source, destination2);
			FileUtils.copyDirectoryToDirectory(source3, destination2);
			FileUtils.copyDirectoryToDirectory(source6, destination2);
			FileUtils.copyDirectoryToDirectory(source6, destination);
			FileUtils.copyDirectoryToDirectory(source7, destination2);
			FileUtils.copyDirectoryToDirectory(source7, destination);
			FileUtils.copyDirectoryToDirectory(source4, destination2);
			FileUtils.copyDirectoryToDirectory(source4, destination);
			logger.info("Copied the preReg test resource successfully");
		} catch (Exception e) {
			logger.error("Exception occured while copying the file: "+e.getMessage());
		}
	}

	public static void copyTestResource(String resPath) {
		try {
			String normalized = resPath.startsWith("/") ? resPath.substring(1) : resPath;
			File source = resolveResourceSourceDirectory(normalized);
			if (source == null) {
				if (!materializeResourceFromClasspath(normalized)) {
					logger.info("Skipping copyTestResource; resource not found: " + normalized);
					return;
				}
				source = new File(TestRunner.getClasspathResourceRoot(), normalized);
			}
			if (!source.isDirectory()) {
				logger.info("Skipping copyTestResource; not a directory: " + normalized);
				return;
			}

			File classpathTarget = new File(TestRunner.getClasspathResourceRoot(), normalized);
			mirrorDirectoryIfMissing(source, classpathTarget);

			File destination = new File(TestResources.getGlobalResourcePaths());
			File destChild = new File(destination, normalized);
			if (destChild.exists()) {
				FileUtils.deleteDirectory(destChild);
			}
			FileUtils.copyDirectoryToDirectory(source, destination);
			logger.info("Copied test resource: " + source.getAbsolutePath() + " -> " + destination.getAbsolutePath());
		} catch (Exception e) {
			logger.error("Exception occured while copying the file: "+e.getMessage());
		}
	}

	/**
	 * Resolve a resource folder for IDE/Maven runs. Prefers {@code target/classes}, then
	 * {@code src/main/resources} under the module directory.
	 */
	private static File resolveResourceSourceDirectory(String normalized) {
		File fromOutput = new File(TestRunner.getClasspathResourceRoot(), normalized);
		if (fromOutput.isDirectory()) {
			return fromOutput;
		}

		for (File moduleRoot : candidateModuleRoots()) {
			File fromSourceTree = new File(moduleRoot, "src/main/resources/" + normalized.replace('/', File.separatorChar));
			if (fromSourceTree.isDirectory()) {
				return fromSourceTree;
			}
		}
		return null;
	}

	private static File[] candidateModuleRoots() {
		String userDir = System.getProperty("user.dir");
		File classpathRoot = new File(TestRunner.getClasspathResourceRoot());
		File fromTarget = classpathRoot.getParentFile() != null && classpathRoot.getParentFile().getParentFile() != null
				? classpathRoot.getParentFile().getParentFile()
				: null;
		if (fromTarget != null && new File(fromTarget, "src/main/resources").isDirectory()) {
			return new File[] { fromTarget, new File(userDir) };
		}
		return new File[] { new File(userDir) };
	}

	private static void mirrorDirectoryIfMissing(File source, File target) throws IOException {
		if (source.getCanonicalFile().equals(target.getCanonicalFile())) {
			return;
		}
		if (target.isDirectory() && hasAnyFile(target)) {
			return;
		}
		if (target.exists()) {
			FileUtils.deleteDirectory(target);
		}
		target.getParentFile().mkdirs();
		FileUtils.copyDirectory(source, target);
	}

	static boolean materializeResourceFromClasspath(String normalizedPath) throws IOException {
		ClassLoader loader = TestResources.class.getClassLoader();
		if (loader == null) {
			return false;
		}

		String prefix = normalizedPath.endsWith("/") ? normalizedPath : normalizedPath + "/";
		boolean copied = false;
		Enumeration<URL> urls = loader.getResources(prefix);
		while (urls.hasMoreElements()) {
			URL url = urls.nextElement();
			if ("file".equals(url.getProtocol())) {
				Path sourcePath = toPath(url);
				if (Files.isDirectory(sourcePath)) {
					File destChild = new File(TestRunner.getClasspathResourceRoot(), normalizedPath);
					mirrorDirectoryIfMissing(sourcePath.toFile(), destChild);
					copied = true;
				}
				continue;
			}
			if ("jar".equals(url.getProtocol()) && copyResourceTreeFromJarUrl(url, normalizedPath,
					new File(TestRunner.getClasspathResourceRoot()))) {
				copied = true;
			}
		}
		return copied && new File(TestRunner.getClasspathResourceRoot(), normalizedPath).isDirectory();
	}

	static boolean copyTestResourceFromClasspath(String normalizedPath, File destinationRoot) throws IOException {
		File source = resolveResourceSourceDirectory(normalizedPath);
		if (source != null) {
			File destChild = new File(destinationRoot, normalizedPath);
			if (destChild.exists()) {
				FileUtils.deleteDirectory(destChild);
			}
			FileUtils.copyDirectory(source, destChild);
			return destChild.isDirectory() && hasAnyFile(destChild);
		}
		if (materializeResourceFromClasspath(normalizedPath)) {
			source = new File(TestRunner.getClasspathResourceRoot(), normalizedPath);
			File destChild = new File(destinationRoot, normalizedPath);
			if (destChild.exists()) {
				FileUtils.deleteDirectory(destChild);
			}
			FileUtils.copyDirectory(source, destChild);
			return destChild.isDirectory() && hasAnyFile(destChild);
		}
		return false;
	}

	private static boolean copyResourceTreeFromJarUrl(URL rootUrl, String normalizedPath, File destinationRoot)
			throws IOException {
		JarURLConnection connection = (JarURLConnection) rootUrl.openConnection();
		try (JarFile jarFile = connection.getJarFile()) {
			String prefix = normalizedPath.endsWith("/") ? normalizedPath : normalizedPath + "/";
			boolean copied = false;
			Enumeration<JarEntry> entries = jarFile.entries();
			while (entries.hasMoreElements()) {
				JarEntry entry = entries.nextElement();
				if (!entry.getName().startsWith(prefix) || entry.isDirectory()) {
					continue;
				}
				String relativePath = entry.getName().substring(prefix.length());
				File destFile = new File(destinationRoot, normalizedPath + "/" + relativePath);
				destFile.getParentFile().mkdirs();
				try (InputStream in = jarFile.getInputStream(entry)) {
					Files.copy(in, destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
				}
				copied = true;
			}
			return copied;
		}
	}

	private static Path toPath(URL url) throws IOException {
		try {
			return Paths.get(url.toURI());
		} catch (URISyntaxException e) {
			throw new IOException("Invalid resource URL: " + url, e);
		}
	}

	private static boolean hasAnyFile(File dir) {
		File[] children = dir.listFiles();
		if (children == null) {
			return false;
		}
		for (File child : children) {
			if (child.isFile()) {
				return true;
			}
			if (child.isDirectory() && hasAnyFile(child)) {
				return true;
			}
		}
		return false;
	}

	public static String getResourcePath() {
		return TestRunner.getGlobalResourcePath()+"/";
	}
	public static String getGlobalResourcePaths() {
		return TestRunner.getGlobalResourcePath();
	}


	public static String checkRunType() {
		return TestRunner.checkRunType();
	}

	public static void removeOldMosipTempTestResource() {
		File authTestFile = new File(TestResources.getGlobalResourcePaths() + "/"+TestResources.resourceFolderName);
		if (authTestFile.exists())
			if (deleteDirectory(authTestFile))
				logger.info("Old "+TestResources.resourceFolderName+" folder successfully deleted!!");
			else
				logger.error("Old "+TestResources.resourceFolderName+" folder not deleted.");
	}

	public static boolean deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            File[] children = dir.listFiles();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDirectory(children[i]);
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
	}

}
