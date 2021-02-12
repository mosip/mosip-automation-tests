package org.mosip.dataprovider;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;

import javax.imageio.ImageIO;

import org.mosip.dataprovider.util.DataProviderConstants;
import org.mosip.dataprovider.util.Gender;

import io.cucumber.messages.internal.com.google.common.io.Files;


public class PhotoProvider {
	static String Photo_File_Format = "/photo_%02d.jpg";
	
	static String getPhoto(int idx, String gender) {
		String encodedImage="";
		try {
			//JPEG2000
			String photoFile = String.format(Photo_File_Format, idx);
			BufferedImage img = ImageIO.read(new File(DataProviderConstants.RESOURCE+"Photos/" + gender.toLowerCase() + photoFile));
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
			String [] names = ImageIO.getWriterFormatNames();
			for(String s: names)
				System.out.println(s);
			
			ImageIO.write(img, "jpg", baos);
			baos.flush();
		
			encodedImage = Base64.getEncoder().encodeToString(encodeFaceImageData(baos.toByteArray()));
			
			//encodedImage = Base64.getEncoder().encodeToString(baos.toByteArray());
			baos.close();
		//	encodedImage = java.net.URLEncoder.encode(encodedImage, "ISO-8859-1");
		} catch (IOException e) {
			
			e.printStackTrace();
		}             
		return encodedImage;
	}
	static void splitImages() {
		///125 x129
		try {
			final BufferedImage source = ImageIO.read(new File(DataProviderConstants.RESOURCE+"Photos/female/celebrities.jpg"));
			int idx =0;
			for (int y = 0; y < source.getHeight()-129; y += 129) {
				for (int x = 0; x < source.getWidth()-125; x += 125) {
					ImageIO.write(source.getSubimage(x, y, 125, 129), "jpg", new File(DataProviderConstants.RESOURCE+"Photos/female/photo_" + idx++ + ".jpg"));
				}
			}			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
			ex.printStackTrace();
		}
		return baos.toByteArray();
		
	}
		
	public static void main(String [] args) throws IOException {
		//splitImages();
		String strImg = getPhoto(21,Gender.Male.name());
		Files.write(strImg.getBytes(), new File( "c:\\temp\\photo.txt"));
		//System.out.println(strImg);
		
	}
}
