package org.mosip.dataprovider.util;

import java.io.File;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;

import javax.xml.bind.DatatypeConverter;


public class CommonUtil {

	public static String getHexEncodedHash(byte[] data) throws Exception{
        try{
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(data);
            return DatatypeConverter.printHexBinary(messageDigest.digest()).toUpperCase();
        } catch(Exception ex){
            throw new Exception("Invalid getHexEncodedHash "+ ex.getMessage());
        }
    }
	
	public static String toCaptialize(String text) {
		return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
	}
	//generate count no of random numbers in range 0 - (max-1)
	public static int[] generateRandomNumbers(int count, int max, int min) {
		int [] rand_nums = new int[ count];
		 
		Random rand = new Random(); 
	     
		for(int i=0; i < count; i++) {
			rand_nums[i] = rand.nextInt((max - min) + 1) + min;
		}
	
		return rand_nums;
	}
	public static String getUTCDateTime(LocalDateTime time) {
		String DATEFORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(DATEFORMAT);
        if (time == null){
            time = LocalDateTime.now(TimeZone.getTimeZone("UTC").toZoneId());
        }  
		String utcTime = time.format(dateFormat);
		return utcTime;
    }
	public static String readFromJSONFile(String filePath) {
		
		StringBuilder builder  = new StringBuilder();
		FileReader reader;
		try {
			reader = new FileReader(filePath);
		
			char[] cbuf = new char[1024];
			int n=0;
			while((n=reader.read(cbuf)) >0) {
				builder.append(new String(cbuf,0, n));
			}
			reader.close();
		} catch (IOException e) {
		//	e.printStackTrace();
		}
		
		return builder.toString();
	
	}
	public static void CopyRecursivly(Path sourceDirectory, Path targetDirectory) throws IOException {
		
		   
	        // Traverse the file tree and copy each file/directory.
	        Files.walk(sourceDirectory)
	                .forEach(sourcePath -> {
	                  
	                Path targetPath = targetDirectory.resolve(sourceDirectory.relativize(sourcePath));
	                       
                    try {
						Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
              
            });
	}
	public static String generateRandomString(int len) {
		Random rand = new Random();
		StringBuilder builder = new StringBuilder();
		String alphabet = "abcdefghijklmn opqrstuvwxyz_123456789";
		
		if(len == 0)
			len = 20;
		
	    for (int i = 0; i < len; i++) {
	      builder.append( alphabet.charAt(rand.nextInt(alphabet.length())) );
	    } 
		return builder.toString();
	}
	public static List<File> listFiles(String dirPath){
		
		List<File> lstFiles = new ArrayList<File>();
		
		File dir = new File(dirPath);
	    File[] files = dir.listFiles();
	    for(File f: files)
	    	lstFiles.add(f);
	    
	    return lstFiles;
	    
	}
}
