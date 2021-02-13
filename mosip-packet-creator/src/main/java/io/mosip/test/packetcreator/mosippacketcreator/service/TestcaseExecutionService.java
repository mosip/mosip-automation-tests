package io.mosip.test.packetcreator.mosippacketcreator.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TestcaseExecutionService {

	@Value("${mosip.test.testcase.propertypath:../deploy/testcases.properties}")
	private String propertyPath;
	   
	public String execute(String testcaseId) {
		String result = "Success";
		Properties props = new Properties();
		try(InputStream input = new FileInputStream(propertyPath)){
			props.load( input);
			if(props.containsKey(testcaseId)) {
				String testcaseFilePath = props.get(testcaseId).toString();
				Path filePath  = Path.of(testcaseFilePath);
				
				ProcessBuilder pb = new ProcessBuilder("cmd", "/c", filePath.getFileName().toString());
				File dir = filePath.getParent().toFile();
				pb.directory(dir);
				Process p = pb.start();
			}
			else
				result = "{Failed : testcaseID not found}";

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			result = "{Failed}";
		} 
		return result;
	}
}
