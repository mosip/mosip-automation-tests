package io.mosip.testrig.dslrig.ivv.e2e.methods;


import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class RenameFpDataFiles {
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
		
		String dirPath = "D:\\centralized\\mountvolume\\profile_resource\\fp_data";
		System.out.println("dirPath " + dirPath);
		
		List<File> lst=new LinkedList<File>();
		for(int i=1; i <= 100; i++) {

			lst = listFiles(dirPath +
					String.format("/Impression_"+i+"/fp_1/", i));
		//Left_Thumb, Left_Index, Left_Middle, Left_Ring, Left_Little, Right_Thumb, Right_Index, Right_Middle, Right_Ring, Right_Little]
			for(File eachfile:lst) {
				if(eachfile.getName().equalsIgnoreCase("1.jpg")) eachfile.renameTo(new File(dirPath +String.format("/Impression_"+i+"/fp_1/")+"Left_Thumb.jpeg"));
				if(eachfile.getName().equalsIgnoreCase("2.jpg")) eachfile.renameTo(new File(dirPath +String.format("/Impression_"+i+"/fp_1/")+"Left_Index.jpeg"));
				if(eachfile.getName().equalsIgnoreCase("3.jpg")) eachfile.renameTo(new File(dirPath +String.format("/Impression_"+i+"/fp_1/")+"Left_Middle.jpeg"));
				if(eachfile.getName().equalsIgnoreCase("4.jpg")) eachfile.renameTo(new File(dirPath +String.format("/Impression_"+i+"/fp_1/")+"Left_Ring.jpeg"));
				if(eachfile.getName().equalsIgnoreCase("5.jpg")) eachfile.renameTo(new File(dirPath +String.format("/Impression_"+i+"/fp_1/")+"Left_Little.jpeg"));
				if(eachfile.getName().equalsIgnoreCase("6.jpg")) eachfile.renameTo(new File(dirPath +String.format("/Impression_"+i+"/fp_1/")+"Right_Thumb.jpeg"));
				if(eachfile.getName().equalsIgnoreCase("7.jpg")) eachfile.renameTo(new File(dirPath +String.format("/Impression_"+i+"/fp_1/")+"Right_Index.jpeg"));
				if(eachfile.getName().equalsIgnoreCase("8.jpg")) eachfile.renameTo(new File(dirPath +String.format("/Impression_"+i+"/fp_1/")+"Right_Middle.jpeg"));
				if(eachfile.getName().equalsIgnoreCase("9.jpg")) eachfile.renameTo(new File(dirPath +String.format("/Impression_"+i+"/fp_1/")+"Right_Ring.jpeg"));
				if(eachfile.getName().equalsIgnoreCase("10.jpg")) eachfile.renameTo(new File(dirPath +String.format("/Impression_"+i+"/fp_1/")+"Right_Little.jpeg"));
				
			}
		
	}
}
}