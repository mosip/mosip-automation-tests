package io.mosip.testrig.dslrig.ivv.e2e.methods;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class RenameFpDataFiles {
	public static List<File> listFiles(String dirPath) {

		List<File> lstFiles = new ArrayList<File>();

		File dir = new File(dirPath);
		File[] files = dir.listFiles();
		for (File f : files)
			lstFiles.add(f);

		return lstFiles;

	}

	public static void main(String[] args) {
	}
}