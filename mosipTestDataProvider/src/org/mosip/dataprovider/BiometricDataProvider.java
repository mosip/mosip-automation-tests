package org.mosip.dataprovider;
import java.io.BufferedReader;
import java.io.File;
//import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
//import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
//import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
//import java.time.LocalDateTime;
//import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
//import java.util.Arrays;
import java.util.Base64;
import java.util.Hashtable;
import java.util.List;

import javax.xml.bind.DatatypeConverter;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.codec.binary.Hex;
//import org.apache.commons.io.IOUtils;
//import org.apache.commons.lang3.tuple.Pair;
import org.mosip.dataprovider.models.BiometricDataModel;
import org.mosip.dataprovider.models.IrisDataModel;
import org.mosip.dataprovider.models.MosipIDSchema;
import org.mosip.dataprovider.util.CommonUtil;
import org.mosip.dataprovider.util.DataProviderConstants;
import org.mosip.dataprovider.util.FPClassDistribution;


import com.jamesmurty.utils.XMLBuilder;
//import java.util.Date;

import variables.VariableManager;


/*
import io.mosip.kernel.cbeffutil.container.impl.CbeffContainerImpl;

import io.mosip.kernel.cbeffutil.impl.CbeffImpl;
import io.mosip.kernel.core.cbeffutil.common.CbeffValidator;
import io.mosip.kernel.core.cbeffutil.entity.BDBInfo;
import io.mosip.kernel.core.cbeffutil.entity.BIR;
import io.mosip.kernel.core.cbeffutil.entity.BIRInfo;
import io.mosip.kernel.core.cbeffutil.entity.BIRVersion;
import io.mosip.kernel.core.cbeffutil.jaxbclasses.BIRType;
import io.mosip.kernel.core.cbeffutil.jaxbclasses.ProcessedLevelType;
import io.mosip.kernel.core.cbeffutil.jaxbclasses.PurposeType;
import io.mosip.kernel.core.cbeffutil.jaxbclasses.QualityType;
import io.mosip.kernel.core.cbeffutil.jaxbclasses.RegistryIDType;
import io.mosip.kernel.core.cbeffutil.jaxbclasses.SingleType;
*/
public class BiometricDataProvider {

	
	
	static String buildBirIris(String irisInfo, String irisName) throws ParserConfigurationException, FactoryConfigurationError, TransformerException, FileNotFoundException {
		String today = CommonUtil.getUTCDateTime(null);
		XMLBuilder builder = XMLBuilder.create("BIR")
				.a("xmlns", "http://standards.iso.org/iso-iec/19785/-3/ed-2/")
				.e("Version").e("Major").t("1").up().e("Minor").t("1").up().up()
				.e("CBEFFVersion").e("Major").t("1").up().e("Minor").t("1").up().up()
				.e("BIRInfo").e("Integrity").t("false").up().up()
				.e("BDBInfo").e("Format").e("Organization").t("Mosip").up().e("Type").t("9").up().up()
					.e("CreationDate").t(today).up().e("Type").t("Iris").up()
					.e("Subtype").t(irisName).up().e("Level").t("Raw").up().e("Purpose").t("Enroll").up()
					.e("Quality").e("Algorithm").e("Organization").t("HMAC").up().e("Type").t("SHA-256").up().up().e("Score").t("100").up()
				.up().up()
				.e("BDB").t(irisInfo).up().up();
					
		//PrintWriter writer = new PrintWriter(new FileOutputStream("cbeffout-finger"+ fingerName+ ".xml"));
		//builder.toWriter(true, writer, null);
				
				
		return builder.asString(null);
	}

	static String buildBirFinger(String fingerInfo, String fingerName) throws ParserConfigurationException, FactoryConfigurationError, TransformerException, FileNotFoundException {
		String today = CommonUtil.getUTCDateTime(null);
		
		XMLBuilder builder = XMLBuilder.create("BIR")
				.a("xmlns", "http://standards.iso.org/iso-iec/19785/-3/ed-2/")
				.e("Version").e("Major").t("1").up().e("Minor").t("1").up().up()
				.e("CBEFFVersion").e("Major").t("1").up().e("Minor").t("1").up().up()
				.e("BIRInfo").e("Integrity").t("false").up().up()
				.e("BDBInfo").e("Format").e("Organization").t("Mosip").up().e("Type").t("7").up().up()
					.e("CreationDate").t(today).up().e("Type").t("Finger").up()
					.e("Subtype").t(fingerName).up().e("Level").t("Raw").up().e("Purpose").t("Enroll").up()
					.e("Quality").e("Algorithm").e("Organization").t("HMAC").up().e("Type").t("SHA-256").up().up().e("Score").t("100").up()
				.up().up()
				.e("BDB").t(fingerInfo).up().up();
					
		//PrintWriter writer = new PrintWriter(new FileOutputStream("cbeffout-finger"+ fingerName+ ".xml"));
		//builder.toWriter(true, writer, null);
				
				
		return builder.asString(null);
	}
	static String buildBirFace(String faceInfo) throws ParserConfigurationException, FactoryConfigurationError, TransformerException, FileNotFoundException {
		String today = CommonUtil.getUTCDateTime(null);
		
		XMLBuilder builder = XMLBuilder.create("BIR")
				.a("xmlns", "http://standards.iso.org/iso-iec/19785/-3/ed-2/")
				.e("Version").e("Major").t("1").up().e("Minor").t("1").up().up()
				.e("CBEFFVersion").e("Major").t("1").up().e("Minor").t("1").up().up()
				.e("BIRInfo").e("Integrity").t("false").up().up()
				.e("BDBInfo").e("Format").e("Organization").t("Mosip").up().e("Type").t("8").up().up()
					.e("CreationDate").t(today).up().e("Type").t("Face").up()
					.e("Level").t("Raw").up().e("Purpose").t("Enroll").up()
					.e("Quality").e("Algorithm").e("Organization").t("HMAC").up().e("Type").t("SHA-256").up().up().e("Score").t("100").up()
				.up().up()
				.e("BDB").t(faceInfo).up().up();
					
		//PrintWriter writer = new PrintWriter(new FileOutputStream("cbeffout-finger"+ fingerName+ ".xml"));
		//builder.toWriter(true, writer, null);
				
				
		return builder.asString(null);
	}
	public static String toCBEFF(List<String> bioFilter,BiometricDataModel biometricDataModel, String toFile) throws Exception {
		String retXml = "";
		
		XMLBuilder builder = XMLBuilder.create("BIR")
				.a("xmlns", "http://standards.iso.org/iso-iec/19785/-3/ed-2/")		
				.e("BIRInfo").e("Integrity").t("false").up().up();
		
		builder.getDocument().setXmlStandalone(true);
		
		//Step 1: convert finger print
		String [] fingerPrint = biometricDataModel.getFingerPrint();
		//for(int i=0; i< fingerPrint.length; i++) {
		int i=0;
		for(String finger :bioFilter) {
			if(finger.toLowerCase().contains("eye") || finger.toLowerCase().equals("face"))
				continue;
			i = Arrays.asList(DataProviderConstants.schemaNames).indexOf(finger);
			if(i >=0) {
				String strFinger = DataProviderConstants.displayFingerName[i];	
				String strFingerXml = buildBirFinger(   fingerPrint[i],strFinger);
				XMLBuilder fbuilder = XMLBuilder.parse(strFingerXml);
				builder = builder.importXMLBuilder(fbuilder);
			}
			
		}
		
		//Step 2: Add Face
		if(bioFilter.contains("Face")) {
			if(biometricDataModel.getEncodedPhoto() != null) {
				String faceXml = buildBirFace( biometricDataModel.getEncodedPhoto());
				builder = builder.importXMLBuilder( XMLBuilder.parse( faceXml));
			}
		}
		
		// Step 3: Add IRIS
		IrisDataModel irisInfo =  biometricDataModel.getIris();
		if(irisInfo != null) {
			String irisXml ="";
			if(bioFilter.contains("leftEye")) {
				irisXml = buildBirIris( irisInfo.getLeft(), "Left");
				builder = builder.importXMLBuilder( XMLBuilder.parse( irisXml));
			}
			if(bioFilter.contains("rightEye")) {
				
				irisXml = buildBirIris( irisInfo.getRight(), "Right");
				builder = builder.importXMLBuilder( XMLBuilder.parse( irisXml));
			}
		}
		
		if(toFile != null) {
			PrintWriter writer = new PrintWriter(new FileOutputStream(toFile));
			builder.toWriter(true, writer, null);
		}
		retXml = builder.asString(null);
		return retXml;
	}
	
	/*
	
	public static String toCBEFF(String [] prints) throws Exception {
		String retXml = "";
		RegistryIDType format = new RegistryIDType();
		format.setOrganization("257");
		format.setType("7");
		QualityType Qtype = new QualityType();
		Qtype.setScore(new Long(90));
		RegistryIDType algorithm = new RegistryIDType();
		algorithm.setOrganization("HMAC");
		algorithm.setType("SHA-256");
		Qtype.setAlgorithm(algorithm);
		ArrayList<BIR> createList = new ArrayList<BIR>();
		int i=0;
		Finger [] fingerType = Finger.values();
		
		for(i=0; i < prints.length; i++) {
			byte[] byteArray = prints[i].getBytes();
			try {

				BIR finger = new BIR.BIRBuilder().withBdb(byteArray )
						.withVersion(new BIRVersion.BIRVersionBuilder().withMinor(1).withMajor(1).build())
						.withCbeffversion(new BIRVersion.BIRVersionBuilder().withMinor(1).withMajor(1).build())
						.withBirInfo(new BIRInfo.BIRInfoBuilder().withIntegrity(false).build())
						.withBdbInfo(new BDBInfo.BDBInfoBuilder().withFormat(format)
						.withQuality(Qtype).withType(Arrays.asList(SingleType.FINGER))
						.withSubtype(Arrays.asList(fingerName[i])) // fingerType[i].name()))
						.withPurpose(PurposeType.ENROLL).withLevel(ProcessedLevelType.RAW)
						.withCreationDate(LocalDateTime.now(ZoneId.of("UTC"))).build())
						.build();
				
				createList.add(finger);
		
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		//CbeffImpl cbeffImpl = new CbeffImpl();
		CbeffContainerImpl cbeffContainer = new CbeffContainerImpl();
		BIRType bir = cbeffContainer.createBIRType(createList);
		byte[] xsd;
		try (InputStream xsdBytes = new FileInputStream("src/main/resource/schema/updatedcbeff.xsd")) {
			xsd = IOUtils.toByteArray(xsdBytes);
		}
		byte[] createXml = CbeffValidator.createXMLBytes(bir, xsd);
		//cbeffImpl.loadXSD();
		//byte[] createXml = cbeffImpl.createXML(createList);
		retXml  = new String(createXml);
	
		return retXml;
		
	}
	*/
	 
	public static BiometricDataModel getBiometricData(Boolean bFinger) {
	
		BiometricDataModel data = new BiometricDataModel();
		
		File tmpDir;
	
		if(bFinger) {
			Boolean bAnguli = Boolean.parseBoolean( VariableManager.getVariableValue("enableAnguli").toString());
			if(bAnguli) {
			
				try {
					tmpDir = Files.createTempDirectory("fps").toFile();
					Hashtable<Integer, List<File>> prints = generateFingerprint(tmpDir.getAbsolutePath(), 10, 2, 4, FPClassDistribution.arch );
					List<File> firstSet = prints.get(1);
			
					String [] fingerPrints = new String[10];
					String [] fingerPrintHash = new String[10];
					
					int index = 0;
					for(File f: firstSet) {
						
						if(index >9) break;
						 Path path = Paths.get(f.getAbsolutePath());
						 byte[] fdata = Files.readAllBytes(path);
						 fingerPrints[index]= Base64.getEncoder().encodeToString(fdata);
						 
						 //fingerPrints[index]= Hex.encodeHexString( fdata ) ;
						// fingerPrints[index]=   fingerPrints[index].toUpperCase();
						 
						 //delete file
						try {
							fingerPrintHash[index] =CommonUtil.getHexEncodedHash(fdata);
							
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						 
						 index++;
						 
					}
					data.setFingerPrint(fingerPrints);
					data.setFingerHash(fingerPrintHash);
					
					tmpDir.deleteOnExit();
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else
			{
				//reach cached finger prints from folder 
				String dirPath = DataProviderConstants.RESOURCE +"/fingerprints/";
				Hashtable<Integer, List<File>> tblFiles = new Hashtable<Integer, List<File>>();
				for(int i=1; i <= 2; i++) {
					
					List<File> lst = CommonUtil.listFiles(dirPath +
							String.format("/Impression_%d/fp_1/", i));
					tblFiles.put(i,lst);
				}
				String [] fingerPrints = new String[10];
				String [] fingerPrintHash = new String[10];
				
				List<File> firstSet = tblFiles.get(1);
				
				int index = 0;
				for(File f: firstSet) {
					
					if(index >9) break;
					 Path path = Paths.get(f.getAbsolutePath());
					 byte[] fdata;
					try {
						fdata = Files.readAllBytes(path);
						fingerPrints[index]= Base64.getEncoder().encodeToString(fdata);

						fingerPrintHash[index] =CommonUtil.getHexEncodedHash(fdata);
				
					} catch (  Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					index++;
					 
				}
				data.setFingerPrint(fingerPrints);
				data.setFingerHash(fingerPrintHash);
				
			}
		}
		return data;
	}
	
	//generate using Anguli
	
	static Hashtable<Integer, List<File>> generateFingerprint(String outDir, int nFingerPrints,
			int nImpressionsPerPrints , int nThreads, FPClassDistribution classDist){
		
		Hashtable<Integer, List<File>> tblFiles = new Hashtable<Integer, List<File>>();

		//C:\Mosip.io\gitrepos\biometric-data\anguli
		String[] commands = {DataProviderConstants.ANGULI_PATH+"/Anguli.exe",
				"-outdir" , outDir, "-numT",String.format("%d", nThreads),"-num",
				String.format("%d", nFingerPrints) ,"-ni",
				String.format("%d", nImpressionsPerPrints),"-cdist", classDist.name() };

		ProcessBuilder pb = new ProcessBuilder(commands);
		pb.directory(new File(DataProviderConstants.ANGULI_PATH));
		 
		try {
			Process proc = pb.start(); // rt.exec(commands);
			BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
			// Read any errors from the attempted command
		//	System.out.println("Error:\n");
			String s;
			
			while (( s = stdError.readLine()) != null) {
			    System.out.println(s);
			}
			//read from outdir
			for(int i=1; i <= nImpressionsPerPrints; i++) {
				
				List<File> lst = CommonUtil.listFiles(outDir +
						String.format("/Impression_%d/fp_1/", i));
				tblFiles.put(i,lst);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return tblFiles;
	}
	//Left Eye, Right Eye
	static List<IrisDataModel> generateIris(int count) throws Exception{
	
		List<IrisDataModel> retVal = new ArrayList<IrisDataModel>();
		
		String srcPath = DataProviderConstants.RESOURCE + "/iris/IITD Database/";
		int []index = CommonUtil.generateRandomNumbers(count, 224, 1);
		
		IrisDataModel m = new IrisDataModel();
		
		for(int i=0; i < count; i++) {
			String fPathL = srcPath + String.format("%03d", index[i]) + "/01_L.bmp";
			String fPathR = srcPath + String.format("%03d", index[i]) + "/05_R.bmp";
			
			 String leftIrisData ="";
			 String rightIrisData = "";
			 String irisHash = "";
			if(Files.exists(Paths.get(fPathL))) {
				 byte[] fdata = Files.readAllBytes(Paths.get(fPathL));
				leftIrisData = Hex.encodeHexString( fdata ) ;	
				irisHash = CommonUtil.getHexEncodedHash(fdata);
				m.setLeftHash(irisHash);
			}
			if(Files.exists(Paths.get(fPathR))) {
				 byte[] fdata = Files.readAllBytes(Paths.get(fPathR));
				rightIrisData = Hex.encodeHexString( fdata ) ;	
				irisHash = CommonUtil.getHexEncodedHash(fdata);
				m.setRightHash(irisHash);
			}
			if(leftIrisData.equals(""))
				leftIrisData = rightIrisData;
			else
			if(rightIrisData.equals(""))
					rightIrisData = leftIrisData;
			m.setLeft(leftIrisData);
			m.setRight(rightIrisData);
			retVal.add( m);
		}
		
		return retVal;
	}
	public static void main(String[] args) {
		
		BiometricDataModel bio= getBiometricData(true);
		
		/*
		Hashtable<Integer, List<File>> tblFiles =generateFingerprint(DataProviderConstants.ANGULI_PATH+"/fps",10, 2,4, FPClassDistribution.arch);
		Set<Integer> k = tblFiles.keySet();

		k.forEach( (key)->{
			List<File> lst = tblFiles.get(key);
			lst.forEach( (f)->{
				System.out.println(f.getAbsolutePath());
				
			});
			
		});
		*/
		
		String xml ="";
		List<String> lstBioAttributes = new ArrayList<String>();
		lstBioAttributes.add("leftEye");
		lstBioAttributes.add("rightEye");
		
		try {
			xml = toCBEFF(lstBioAttributes, bio, "cbeffallfingersOut.xml");
/*
			CbeffContainerImpl cbeffContainer = new CbeffContainerImpl();
			//C:\temp\10002300012\REGISTRATION_CLIENT\NEW\rid_id\individualBiometrics_bio_CBEFF.xml
			byte[] xsd, xmlData;
			try (InputStream xsdBytes = new FileInputStream("src/main/resource/schema/updatedcbeff.xsd")) {
				xsd = IOUtils.toByteArray(xsdBytes);
			}
	
		try (InputStream xmlBytes = new FileInputStream("C:\\temp\\10002300012\\REGISTRATION_CLIENT\\NEW\\rid_id\\individualBiometrics_bio_CBEFF.xml")) {
				xmlData = IOUtils.toByteArray(xmlBytes);
			}

			//Boolean bret = cbeffContainer.validateXML(xmlData, xsd);
			
			Boolean bret = cbeffContainer.validateXML(xml.getBytes(), xsd);
			System.out.println("Valid ?" + bret);

*/

			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(xml);
	}
}
