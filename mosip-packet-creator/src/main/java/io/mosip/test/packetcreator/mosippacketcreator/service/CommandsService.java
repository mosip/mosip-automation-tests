package io.mosip.test.packetcreator.mosippacketcreator.service;

import java.io.File;
import java.io.FileInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Properties;
import java.util.UUID;

import org.springframework.util.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.Config;
import io.kubernetes.client.util.KubeConfig;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import java.io.FileReader;

@Service
public class CommandsService {

	@Value("${mosip.test.testcase.propertypath:../deploy/testcases.properties}")
	private String propertyPath;
	
	@Value("${mosip.test.uploads:../deploy/uploads}")
	private String uploadPath;

    @Value("${mosip.test.baseurl}")
    private String baseUrl;
   
    @Autowired
    private ContextUtils contextUtils;
   
	 private static final Logger logger = LoggerFactory.getLogger(CommandsService.class);
		
	public String writeToFile(Properties requestData, long offset) throws IOException {
		
		//take file name
		String filePath = requestData.getProperty("filePath");
		String base64data =  requestData.getProperty("base64data");
		byte[] data = Base64.decode(base64data.getBytes());
		File myFile = new File (filePath);
		//Create the accessor with read-write access.
		RandomAccessFile accessor = new RandomAccessFile (myFile, "rws");

		accessor.seek(offset);

		accessor.write(data);
		accessor.close();
		return filePath;
	}
	public String getAllPods(String contextKey) throws ApiException, IOException {
 		Properties props = contextUtils.loadServerContext(contextKey);
 		if(props.contains("mosip.test.baseurl")) {
 			
 			baseUrl = props.getProperty("mosip.test.baseUrl");
 			
 		}
 		 String kubeConfigPath =  "../deploy/kube/mzcluster.config";

 		Reader reader = new FileReader(kubeConfigPath);


 	    // loading the out-of-cluster config, a kubeconfig from file-system
 	    ApiClient client =
 	        ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(reader)).build();

 	    // set the global default api-client to the in-cluster one from above
 	    Configuration.setDefaultApiClient(client);

 	    // the CoreV1Api loads default api-client from global configuration.
 	    CoreV1Api api = new CoreV1Api();

        V1PodList list = api.listPodForAllNamespaces(null, null, null, null, null, null, null, null, null);
        for (V1Pod item : list.getItems()) {
            System.out.println(item.getMetadata().getName());
        }
        return "";
	}
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
	public String storeFile(MultipartFile file) throws IOException {
		String fileExtension = "";
		String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
		fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
		File uploadFolder = new File(uploadPath);
		if(!uploadFolder.exists() || !uploadFolder.isDirectory()) {
			uploadFolder.mkdir();
		}
		String fileName = UUID.randomUUID().toString() + fileExtension;
		 Path targetLocation = Path.of( uploadPath  + "/" + fileName);
		 Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
		 return targetLocation.toString();
	}
}
