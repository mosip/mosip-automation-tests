package io.mosip.ivv.e2e.methods;


import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

public class RenameFaceDataFiles {
public static List<File> listFiles(String dirPath){
		
		List<File> lstFiles = new ArrayList<File>();
		
		File dir = new File(dirPath);
	    File[] files = dir.listFiles();
	    for(File f: files)
	    	lstFiles.add(f);
	    
	    return lstFiles;
	    
	}

	public static void main(String[] args)
	{
		
		String dirPath = "D:\\centralized\\mountvolume\\profile_resource\\face_data\\";
		System.out.println("dirPath " + dirPath);
		
		   File folder = new File(dirPath);
	        File[] listOfFiles = folder.listFiles();
		
	        ///////////////////
	        Integer count =0;
	        String s=count.toString();
	        
	        for(File eachfile:listOfFiles) {
	        	count++;
	        	s= String.format("%04d", count);
				eachfile.renameTo(new File(dirPath+"face"+s+".jpg"));
				
			}
	        
//	        for (int i = 0; i < listOfFiles.length; i++) {
//
//	            if (listOfFiles[i].isFile()) {
//
//	                File f = new File(dirPath+listOfFiles[i].getName()); 
//
//	                f.renameTo(new File("c:\\Projects\\sample\\"+i+".txt"));
//	            }
//	        }

	        System.out.println("conversion is done");
	    }
	}
	        
//	        ////////////////
//	        
//		List<File> lst=new LinkedList<File>();
//	
//			lst = listFiles(new File(dirPath+"\"));"
//					
//					//String.format("/Face (%02d).jpg", i));
//		
//			for(File eachfile:lst) {
//				eachfile.renameTo(new File(eachfile.getName().replace(" ", "")));
//				eachfile.renameTo(new File(eachfile.getName().replace("(", "")));
//				eachfile.renameTo(new File(eachfile.getName().replace(")", "")));
//				
//			}
//		
//	}
//
//}