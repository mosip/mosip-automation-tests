package org.mosip.dataprovider.mds;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import io.mosip.biometrics.util.face.EyeColour;
import io.mosip.biometrics.util.face.FaceCaptureDeviceTechnology;
import io.mosip.biometrics.util.face.FaceCaptureDeviceType;
import io.mosip.biometrics.util.face.FaceCaptureDeviceVendor;
import io.mosip.biometrics.util.face.FaceCertificationFlag;
import io.mosip.biometrics.util.face.FaceEncoder;
import io.mosip.biometrics.util.face.FaceFormatIdentifier;
import io.mosip.biometrics.util.face.FaceImageType;
import io.mosip.biometrics.util.face.FaceQualityAlgorithmIdentifier;
import io.mosip.biometrics.util.face.FaceQualityAlgorithmVendorIdentifier;
import io.mosip.biometrics.util.face.FaceQualityBlock;
import io.mosip.biometrics.util.face.FaceVersionNumber;
import io.mosip.biometrics.util.face.Features;
import io.mosip.biometrics.util.face.Gender;
import io.mosip.biometrics.util.face.HairColour;
import io.mosip.biometrics.util.face.HeightCodes;
import io.mosip.biometrics.util.face.ImageColourSpace;
import io.mosip.biometrics.util.face.ImageDataType;
import io.mosip.biometrics.util.face.LandmarkPoints;
import io.mosip.biometrics.util.face.PostAcquisitionProcessing;
import io.mosip.biometrics.util.face.SpatialSamplingRateLevel;
import io.mosip.biometrics.util.face.TemporalSequenceFlags;
import io.mosip.biometrics.util.finger.AnnotationBlock;
import io.mosip.biometrics.util.finger.CommentBlock;
import io.mosip.biometrics.util.finger.FingerCaptureDeviceTechnology;
import io.mosip.biometrics.util.finger.FingerCaptureDeviceType;
import io.mosip.biometrics.util.finger.FingerCaptureDeviceVendor;
import io.mosip.biometrics.util.finger.FingerCertificationBlock;
import io.mosip.biometrics.util.finger.FingerCertificationFlag;
import io.mosip.biometrics.util.finger.FingerEncoder;
import io.mosip.biometrics.util.finger.FingerFormatIdentifier;
import io.mosip.biometrics.util.finger.FingerImageBitDepth;
import io.mosip.biometrics.util.finger.FingerImageCompressionType;
import io.mosip.biometrics.util.finger.FingerImpressionType;
import io.mosip.biometrics.util.finger.FingerPosition;
import io.mosip.biometrics.util.finger.FingerQualityAlgorithmIdentifier;
import io.mosip.biometrics.util.finger.FingerQualityAlgorithmVendorIdentifier;
import io.mosip.biometrics.util.finger.FingerQualityBlock;
import io.mosip.biometrics.util.finger.FingerScaleUnitType;
import io.mosip.biometrics.util.finger.FingerVersionNumber;
import io.mosip.biometrics.util.finger.SegmentationBlock;
import io.mosip.biometrics.util.iris.EyeLabel;
import io.mosip.biometrics.util.iris.ImageFormat;
import io.mosip.biometrics.util.iris.ImageType;
import io.mosip.biometrics.util.iris.IrisCaptureDeviceTechnology;
import io.mosip.biometrics.util.iris.IrisCaptureDeviceType;
import io.mosip.biometrics.util.iris.IrisCaptureDeviceVendor;
import io.mosip.biometrics.util.iris.IrisCertificationFlag;
import io.mosip.biometrics.util.iris.IrisEncoder;
import io.mosip.biometrics.util.iris.IrisFormatIdentifier;
import io.mosip.biometrics.util.iris.IrisImageBitDepth;
import io.mosip.biometrics.util.iris.IrisImageCompressionType;
import io.mosip.biometrics.util.iris.IrisQualityAlgorithmIdentifier;
import io.mosip.biometrics.util.iris.IrisQualityAlgorithmVendorIdentifier;
import io.mosip.biometrics.util.iris.IrisQualityBlock;
import io.mosip.biometrics.util.iris.IrisVersionNumber;
import io.mosip.biometrics.util.iris.Orientation;

import io.cucumber.messages.internal.com.google.common.io.Files;
import io.mosip.biometrics.util.ConvertRequestDto;
import io.mosip.biometrics.util.face.CrossReference;
import io.mosip.biometrics.util.face.Expression;

public class ISOConverter {
	
	static Map<String,FingerPosition> mapFingerPos = new HashMap<String, FingerPosition>();
	static {
		mapFingerPos.put("Right Thumb", FingerPosition.RIGHT_THUMB);
		mapFingerPos.put("Right IndexFinger", FingerPosition.RIGHT_INDEX_FINGER);
		mapFingerPos.put("Right MiddleFinger", FingerPosition.RIGHT_MIDDLE_FINGER);
		mapFingerPos.put("Right RingFinger", FingerPosition.RIGHT_RING_FINGER);
		mapFingerPos.put("Right LittleFinger", FingerPosition.RIGHT_LITTLE_FINGER);
		mapFingerPos.put("Left Thumb", FingerPosition.LEFT_THUMB);
		mapFingerPos.put("Left IndexFinger", FingerPosition.LEFT_INDEX_FINGER);
		mapFingerPos.put("Left MiddleFinger", FingerPosition.LEFT_MIDDLE_FINGER);
		mapFingerPos.put("Left RingFinger", FingerPosition.LEFT_RING_FINGER);
		mapFingerPos.put("Left LittleFinger", FingerPosition.LEFT_LITTLE_FINGER);
				
	}
	public static int getFingerPos(String bioSubType) {
		FingerPosition fingerPosition = mapFingerPos.get(bioSubType);
		return fingerPosition.ordinal();
	}
	public byte [] convertFinger(byte[] inStream, String outFile, String biometricSubType) throws Exception {
		
		byte[] imageData = inStream;// Base64.getDecoder().decode(inStream);
		
		//ImageDataType imageDataType = ImageDataType.JPEG2000_LOSS_LESS;
		//	ByteArrayInputStream bis = new ByteArrayInputStream(imageData);
		
		// BufferedImage buffImg  = ImageIO.read(bis);


		FingerPosition fingerPosition = mapFingerPos.get(biometricSubType);
		FingerFormatIdentifier formatIdentifier = FingerFormatIdentifier.FORMAT_FIR;
		FingerVersionNumber versionNumber = FingerVersionNumber.VERSION_020;
		FingerCertificationFlag certificationFlag = FingerCertificationFlag.UNSPECIFIED;

		FingerCaptureDeviceTechnology sourceType = FingerCaptureDeviceTechnology.UNSPECIFIED;
		FingerCaptureDeviceVendor deviceVendor = FingerCaptureDeviceVendor.UNSPECIFIED;
		FingerCaptureDeviceType deviceType = FingerCaptureDeviceType.UNSPECIFIED;
		Date captureDate = new Date ();// the date instance		      		 
		int noOfRepresentations= (int)0x0001;
		FingerQualityAlgorithmVendorIdentifier  algorithmVendorIdentifier = FingerQualityAlgorithmVendorIdentifier.NIST;
		FingerQualityAlgorithmIdentifier qualityAlgorithmIdentifier = FingerQualityAlgorithmIdentifier.NIST;

		int quality = 80; 
		FingerQualityBlock [] qualityBlocks = new FingerQualityBlock [] { new FingerQualityBlock ((byte)quality , algorithmVendorIdentifier, qualityAlgorithmIdentifier)};
		FingerCertificationBlock[] certificationBlocks = null; 
	
		int representationNo = (int)0x0000; 
		FingerScaleUnitType scaleUnitType = FingerScaleUnitType.PIXELS_PER_INCH; 
		int captureDeviceSpatialSamplingRateHorizontal = 500; 
		int captureDeviceSpatialSamplingRateVertical = 500;
		int imageSpatialSamplingRateHorizontal = 500; 
		int imageSpatialSamplingRateVertical = 500;
		FingerImageBitDepth bitDepth = FingerImageBitDepth.BPP_08;
		FingerImpressionType impressionType = FingerImpressionType.UNKNOWN;
		int lineLengthHorizontal = 0;
		int lineLengthVertical = 0;
		
		int noOfFingerPresent = (int)0x0001;
		SegmentationBlock segmentationBlock = null;
		AnnotationBlock annotationBlock = null;
		CommentBlock commentBlock = null;
		FingerImageCompressionType compressionType = FingerImageCompressionType.JPEG_2000_LOSS_LESS;
		ConvertRequestDto convertRequestDto=new ConvertRequestDto();
		convertRequestDto.setBiometricSubType("imageData");
		convertRequestDto.setImageType(0);
		convertRequestDto.setInputBytes(imageData);
		convertRequestDto.setModality("Finger");
		convertRequestDto.setPurpose("Registration");
		convertRequestDto.setVersion("ISO19794_4_2011");
//		byte [] isoData = FingerEncoder.convertFingerImageToISO19794_4_2011 
//				(
//						formatIdentifier, versionNumber, certificationFlag, 
//						sourceType, deviceVendor, deviceType,
//						captureDate, noOfRepresentations,
//						qualityBlocks, certificationBlocks, 
//						fingerPosition, representationNo, scaleUnitType, 
//						captureDeviceSpatialSamplingRateHorizontal, captureDeviceSpatialSamplingRateVertical, 
//						imageSpatialSamplingRateHorizontal, imageSpatialSamplingRateVertical,
//						bitDepth, compressionType,
//						impressionType, lineLengthHorizontal, lineLengthVertical,
//						noOfFingerPresent, imageData, 
//						segmentationBlock, annotationBlock, commentBlock
//				);
		byte [] isoData = FingerEncoder.convertFingerImageToISO(convertRequestDto);
		if (isoData != null && outFile != null)
		{
			Files.write(isoData,new File(outFile));
		
		}
    	return isoData;		
	}
	public byte [] convertIris(byte[] inStream, String outFile, String biometricSubType) throws Exception {
		
		byte[] imageData =  inStream;//Base64.getDecoder().decode(inStream);
		
		//ImageDataType imageDataType = ImageDataType.JPEG2000_LOSS_LESS;
		ByteArrayInputStream bis = new ByteArrayInputStream(imageData);
		
		BufferedImage buffImg  = ImageIO.read(bis);
		int imageWidth = buffImg.getWidth();
		int imageHeight = buffImg.getHeight();
		IrisFormatIdentifier formatIdentifier = IrisFormatIdentifier.FORMAT_IIR;
		IrisVersionNumber versionNumber = IrisVersionNumber.VERSION_020;
		IrisCertificationFlag certificationFlag = IrisCertificationFlag.UNSPECIFIED;

		Date captureDate = new Date ();// the date instance		      		 

		IrisQualityAlgorithmVendorIdentifier algorithmVendorIdentifier = IrisQualityAlgorithmVendorIdentifier.ALGORITHM_VENDOR_IDENTIFIER;
		IrisQualityAlgorithmIdentifier qualityAlgorithmIdentifier = IrisQualityAlgorithmIdentifier.ALGORITHM_IDENTIFIER;
    		    	    	
		EyeLabel eyeLabel = EyeLabel.UNSPECIFIED;
		if (biometricSubType.equals("Left"))
			eyeLabel = EyeLabel.LEFT;
		else if (biometricSubType.equals("Right"))
			eyeLabel = EyeLabel.RIGHT;
		int range = 0x0000;
		int rollAngleOfEye = 0xFFFF;//ANGLE_UNDEFINED
		int rollAngleUncertainty = 0xFFFF; //UNCERTAIN_UNDEFINED
		int irisCenterSmallestX = 0x0000; 	//COORDINATE_UNDEFINED
		int irisCenterLargestX = 0x0000; 	//COORDINATE_UNDEFINED 
		int irisCenterSmallestY = 0x0000; 	//COORDINATE_UNDEFINED 
		int irisCenterLargestY = 0x0000; 	//COORDINATE_UNDEFINED 
		int irisDiameterSmallest = 0x0000; 	//COORDINATE_UNDEFINED 
		int irisDiameterLargest = 0x0000; 	//COORDINATE_UNDEFINED
		ImageType imageType = ImageType.CROPPED;
		ImageFormat imageFormat = ImageFormat.MONO_JPEG2000_LOSS_LESS;//0A
		Orientation horizontalOrientation = Orientation.UNDEFINED;
		Orientation verticalOrientation = Orientation.UNDEFINED;
		IrisImageCompressionType compressionType = IrisImageCompressionType.UNDEFINED;
		IrisImageBitDepth bitDepth = IrisImageBitDepth.BPP_8;
		int noOfRepresentations = (int)0x0001;
		int representationNo = (int)0x0001;
		int noOfEyesPresent = (int)0x0001;
		IrisCaptureDeviceTechnology sourceType = IrisCaptureDeviceTechnology.CMOS_OR_CCD;
		IrisCaptureDeviceVendor deviceVendor = IrisCaptureDeviceVendor.UNSPECIFIED;
		IrisCaptureDeviceType deviceType = IrisCaptureDeviceType.UNSPECIFIED;
		int quality = 80; 
		IrisQualityBlock [] qualityBlocks = new IrisQualityBlock [] { new IrisQualityBlock ((byte)quality , algorithmVendorIdentifier, qualityAlgorithmIdentifier)};
		ConvertRequestDto convertRequestDto=new ConvertRequestDto();
		convertRequestDto.setBiometricSubType("imageData");
		convertRequestDto.setImageType(0);
		convertRequestDto.setInputBytes(imageData);
		convertRequestDto.setModality("Iris");
		convertRequestDto.setPurpose("Registration");
		convertRequestDto.setVersion("ISO19794_4_2011");
//		byte [] isoData = IrisEncoder.convertIrisImageToISO19794_6_2011 
//			(
//				formatIdentifier, versionNumber,
//				certificationFlag, captureDate, 
//				noOfRepresentations, representationNo, noOfEyesPresent,
//				eyeLabel, imageType, imageFormat,
//				horizontalOrientation, verticalOrientation, compressionType,
//				imageWidth, imageHeight, bitDepth, range, rollAngleOfEye, rollAngleUncertainty, 
//				irisCenterSmallestX, irisCenterLargestX, irisCenterSmallestY, irisCenterLargestY, 
//				irisDiameterSmallest, irisDiameterLargest,
//				sourceType, deviceVendor, deviceType, 
//				qualityBlocks, 
//				imageData, imageWidth, imageHeight
//			);
		byte [] isoData = IrisEncoder.convertIrisImageToISO(convertRequestDto); 
		if (isoData != null && outFile != null)
		{
			Files.write(isoData,new File(outFile));
		
		}
		return isoData;
	}

	public byte [] convertFace(byte[] inStream, String outFile) throws Exception {
		
		FaceFormatIdentifier formatIdentifier = FaceFormatIdentifier.FORMAT_FAC;
		FaceVersionNumber versionNumber = FaceVersionNumber.VERSION_030;
		FaceCertificationFlag certificationFlag = FaceCertificationFlag.UNSPECIFIED;
		TemporalSequenceFlags temporalSequenceFlags = TemporalSequenceFlags.ONE_REPRESENTATION;
		Date captureDate = new Date ();// the date instance		      		 
		short noOfLandMarkPoints = 0x00;
		FaceQualityAlgorithmVendorIdentifier algorithmVendorIdentifier = FaceQualityAlgorithmVendorIdentifier.ALGORITHM_VENDOR_IDENTIFIER;
		FaceQualityAlgorithmIdentifier qualityAlgorithmIdentifier = FaceQualityAlgorithmIdentifier.ALGORITHM_IDENTIFIER;
		EyeColour eyeColour = EyeColour.UNSPECIFIED;
		int featureMask = 0;
		HeightCodes subjectHeight = HeightCodes.UNSPECIFIED;
		HairColour hairColour = HairColour.UNSPECIFIED;
		Expression expression = Expression.UNSPECIFIED;
		Features features = Features.FEATURES_ARE_SPECIFIED;
		int noOfRepresentations = (int)0x0001;
		Gender gender = Gender.UNKNOWN;
		int [] poseAngle = { 0, 0, 0 };
		int [] poseAngleUncertainty = { 0, 0, 0 };
		FaceImageType faceImageType = FaceImageType.FULL_FRONTAL;
		ImageColourSpace imageColourSpace = ImageColourSpace.BIT_24_RGB;
		FaceCaptureDeviceTechnology sourceType = FaceCaptureDeviceTechnology.VIDEO_FRAME_ANALOG_CAMERA;
		FaceCaptureDeviceVendor deviceVendor = FaceCaptureDeviceVendor.UNSPECIFIED;
		FaceCaptureDeviceType deviceType = FaceCaptureDeviceType.UNSPECIFIED;
		
		SpatialSamplingRateLevel spatialSamplingRateLevel = SpatialSamplingRateLevel.SPATIAL_SAMPLING_RATE_LEVEL_180;
		PostAcquisitionProcessing postAcquisitionProcessing = PostAcquisitionProcessing.CROPPED;
		CrossReference crossReference = CrossReference.BASIC;
		int quality = 80; 
		LandmarkPoints [] landmarkPoints = null;
		FaceQualityBlock [] qualityBlock = new FaceQualityBlock [] { new FaceQualityBlock ((byte)quality , algorithmVendorIdentifier, qualityAlgorithmIdentifier)};
		//Base64.getEncoder().encodeToString
		
		byte[] imageData = inStream;//Base64.getDecoder().decode(inStream);
		
		ImageDataType imageDataType = ImageDataType.JPEG2000_LOSS_LESS;
		
		ByteArrayInputStream bis = new ByteArrayInputStream(imageData);
		
		BufferedImage buffImg  = ImageIO.read(bis);
		int imageWidth = buffImg.getWidth();
		int imageHeight = buffImg.getHeight();
		ConvertRequestDto convertRequestDto=new ConvertRequestDto();
		convertRequestDto.setBiometricSubType("imageData");
		convertRequestDto.setImageType(0);
		convertRequestDto.setInputBytes(imageData);
		convertRequestDto.setModality("Face");
		convertRequestDto.setPurpose("Registration");
		convertRequestDto.setVersion("ISO19794_4_2011");
//		byte [] isoData = FaceEncoder.convertFaceImageToISO19794_5_2011 
//				(
//					formatIdentifier, versionNumber, 
//					certificationFlag, temporalSequenceFlags, 
//					captureDate, noOfRepresentations, noOfLandMarkPoints, 
//					gender, eyeColour, featureMask, 
//					hairColour, subjectHeight, expression, 
//					features, poseAngle, poseAngleUncertainty, 
//					faceImageType, sourceType, deviceVendor, deviceType, 
//					qualityBlock, imageData, imageWidth, imageHeight, 
//					imageDataType, spatialSamplingRateLevel, 
//					postAcquisitionProcessing, crossReference, 
//					imageColourSpace, landmarkPoints
//				); 
//		
		byte [] isoData = FaceEncoder.convertFaceImageToISO(convertRequestDto);
		
		if (isoData != null && outFile != null)
		{
			Files.write(isoData,new File(outFile));
		
		}
		return isoData;
	}
}
