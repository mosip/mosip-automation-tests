package io.mosip.test.packetcreator.mosippacketcreator.service;

import java.io.File;
import java.io.FileInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
public class TestcaseExecutionService {

	@Value("${mosip.test.testcase.propertypath:../deploy/testcases.properties}")
	private String propertyPath;
	   
	 private static final Logger logger = LoggerFactory.getLogger(TestcaseExecutionService.class);
		
	public String execute(String testcaseId, boolean bSync) {
		String result = "Success";
		Properties props = new Properties();
		
		logger.info("execute Testcase:"+ testcaseId );
		
		try(InputStream input = new FileInputStream(propertyPath)){
			props.load( input);
			if(props.containsKey(testcaseId)) {
				String testcaseFilePath = props.get(testcaseId).toString();
				Path filePath  = Path.of(testcaseFilePath);
				
				ProcessBuilder pb = new ProcessBuilder("cmd", "/c", filePath.getFileName().toString());
				File dir = filePath.getParent().toFile();
				pb.directory(dir);
				Process p = pb.start();
				logger.info("Exec Testcase:"+ testcaseId + ":"+ "folder:" + dir.toString()+":"+ filePath.getFileName().toString());
				if(bSync) {
					try {
						p.waitFor();
						logger.info("Exec Testcase:"+ testcaseId + " execution completed");
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
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
