package io.mosip.testrig.dslrig.dataprovider;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.mosip.testrig.dslrig.dataprovider.variables.VariableManager;


public class PhotoProvider {
	private static final Logger logger = LoggerFactory.getLogger(PhotoProvider.class);
	static String Photo_File_Format = "/face%04d.jpg";
	//static byte[][] getPhoto(int idx, String gender,String contextKey) {
		static byte[][] getPhoto(String contextKey) {
			
		//String encodedImage="";
		//String hexHash ="";
		byte[] bencoded =null;
		byte[] bData = null;
		try {
			//JPEG2000
					
					String dirPath = VariableManager.getVariableValue(contextKey, "mountPath").toString()
					+ VariableManager.getVariableValue(contextKey, "mosip.test.persona.facedatapath").toString();
					
//			File dir = new File(dirPath);
//
//			File listDir[] = dir.listFiles();
//			int numberOfSubfolders = listDir.length;
			
			        File dir = new File(dirPath);
			        FileFilter filter = new FileFilter() {
			            @Override
			            public boolean accept(File pathname) {
			                return !pathname.isDirectory();
			            }
			        };
			        File[] listDir = dir.listFiles(filter);
			        int numberOfSubfolders = listDir.length;
			        System.out.println(listDir.length);

			int min = 1;
			int max = numberOfSubfolders;
			int randomNumber = (int) (Math.random() * (max - min)) + min;
			String beforescenario = VariableManager.getVariableValue(contextKey, "scenario").toString();
			String afterscenario = beforescenario.substring(0, beforescenario.indexOf(':'));

			int currentScenarioNumber = Integer.valueOf(afterscenario);

			// If the available impressions are less than scenario number, pick the random
			// one

			// otherwise pick the impression of same of scenario number
			int impressionToPick = (currentScenarioNumber < numberOfSubfolders) ? currentScenarioNumber : randomNumber;

			System.out.println("currentScenarioNumber=" + currentScenarioNumber + " numberOfSubfolders="
					+ numberOfSubfolders + " impressionToPick=" + impressionToPick);
					File file=new File(dirPath + String.format(Photo_File_Format, impressionToPick));

					Object val = VariableManager.getVariableValue(VariableManager.NS_DEFAULT,"enableExternalBiometricSource");
			boolean bExternalSrc = false;
			BufferedImage img = null;
			
			if(val != null )
				bExternalSrc = Boolean.valueOf(val.toString());
			
			if(bExternalSrc) {
				//folder where all bio input available
				String bioSrc = VariableManager.getVariableValue(VariableManager.NS_DEFAULT,"externalBiometricsource").toString();
				
				//String srcpath = "C:\\Mosip.io\\external-data\\CBEFF Validated\\jp2\\Face.jp2";
				img = ImageIO.read(new File(bioSrc +"Face.jp2" ));
			}
			else
			{
				img=ImageIO.read(file);
				System.out.println("Image picked from this path="+ file);
			
			}
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
			/*String [] names = ImageIO.getWriterFormatNames();
			for(String s: names)
				System.out.println(s);
			*/
			ImageIO.write(img, "jpg", baos);
			baos.flush();
			bData = baos.toByteArray();
			bencoded = encodeFaceImageData(bData);
			
		//	encodedImage = Base64.getEncoder().encodeToString(encodeFaceImageData(bData));
			baos.close();
			
		//	hexHash = CommonUtil.getHexEncodedHash(bData);

			
		} catch (Exception e) {
			
			logger.error(e.getMessage());
		}             
		return new byte[][] {bencoded, bData};
	}
	static void splitImages(String contextKey) {
		///125 x129
		try {
			final BufferedImage source = ImageIO.read(new File(VariableManager.getVariableValue(contextKey,"mountPath").toString()+VariableManager.getVariableValue(contextKey,"mosip.test.persona.facedatapath").toString()+"/female/celebrities.jpg"));
			int idx =0;
			for (int y = 0; y < source.getHeight()-129; y += 129) {
				for (int x = 0; x < source.getWidth()-125; x += 125) {
					ImageIO.write(source.getSubimage(x, y, 125, 129), "jpg", new File(VariableManager.getVariableValue(contextKey,"mountPath").toString()+VariableManager.getVariableValue(contextKey,"mosip.test.persona.facedatapath").toString()+"/female/photo_" + idx++ + ".jpg"));
				}
			}			
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		
	}
	static public byte[] encodeFaceImageData(byte[] faceImage) {

		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();


		try (DataOutputStream dout = new DataOutputStream(baos)) {

			byte[] format = new byte[4];
			
			dout.write(format);
			byte[] version = new byte[4];
			dout.write(version);
			int recordLength = 100;
			dout.writeInt(recordLength);
			
			short numberofRepresentionRecord = 1;
			dout.writeShort(numberofRepresentionRecord);
			
			byte certificationFlag =1;
			dout.writeByte(certificationFlag);
			
			byte[] temporalSequence = new byte[2];
			dout.write(temporalSequence);
			
			//int representationLength =16;
			//dout.writeInt(representationLength);
			
			
			ByteArrayOutputStream rdoutArray = new ByteArrayOutputStream();
			
			try (DataOutputStream rdout = new DataOutputStream(rdoutArray)) {
				byte[] captureDetails = new byte[14];
				rdout.write(captureDetails, 0, 14);
				byte noOfQualityBlocks =0;
				rdout.writeByte(noOfQualityBlocks);

				short noOfLandmarkPoints =0;
				rdout.writeShort(noOfLandmarkPoints);
				byte[] facialInformation = new byte[15];
				rdout.write(facialInformation, 0, 15);

				byte faceType = 1;
				rdout.writeByte(faceType);
				byte imageDataType = 0;
				rdout.writeByte(imageDataType);
				
				byte[] otherImageInformation = new byte[9];
				rdout.write(otherImageInformation, 0, otherImageInformation.length);
				int lengthOfImageData = faceImage.length;
				rdout.writeInt(lengthOfImageData);

				rdout.write(faceImage, 0, lengthOfImageData);

			}
			byte[] representationData = rdoutArray.toByteArray();
			int representationLength = representationData.length +4;
			dout.writeInt(representationLength);
			
			dout.write(representationData, 0, representationData.length);
		
		}catch(Exception ex) {
			logger.error(ex.getMessage());
		}
		return baos.toByteArray();
		
	}
		
	public static void main(String [] args) throws IOException {
		//splitImages();
		//byte[][] strImg = getPhoto(21,Gender.Male.name(),"contextKey");
		//Files.write(strImg[0].getBytes(), new File( "c:\\temp\\photo.txt"));
		//System.out.println(strImg);
		
	}
	public static byte[][] loadPhoto(String faceFile) {

		byte[] bencoded =null;
		byte[] bData = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		BufferedImage img;
		try {
			img = ImageIO.read(new File(faceFile));
			ImageIO.write(img, "jpg", baos);
			baos.flush();
			bData = baos.toByteArray();
			bencoded = encodeFaceImageData(bData);
			baos.close();
		
	    
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	         
		return new byte[][] {bencoded, bData};

	}
}
