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

import org.mosip.iso.face.EyeColour;
import org.mosip.iso.face.FaceCaptureDeviceTechnology;
import org.mosip.iso.face.FaceCaptureDeviceType;
import org.mosip.iso.face.FaceCaptureDeviceVendor;
import org.mosip.iso.face.FaceCertificationFlag;
import org.mosip.iso.face.FaceEncoder;
import org.mosip.iso.face.FaceFormatIdentifier;
import org.mosip.iso.face.FaceImageType;
import org.mosip.iso.face.FaceQualityAlgorithmIdentifier;
import org.mosip.iso.face.FaceQualityAlgorithmVendorIdentifier;
import org.mosip.iso.face.FaceQualityBlock;
import org.mosip.iso.face.FaceVersionNumber;
import org.mosip.iso.face.Features;
import org.mosip.iso.face.Gender;
import org.mosip.iso.face.HairColour;
import org.mosip.iso.face.HeightCodes;
import org.mosip.iso.face.ImageColourSpace;
import org.mosip.iso.face.ImageDataType;
import org.mosip.iso.face.LandmarkPoints;
import org.mosip.iso.face.PostAcquisitionProcessing;
import org.mosip.iso.face.SpatialSamplingRateLevel;
import org.mosip.iso.face.TemporalSequenceFlags;
import org.mosip.iso.finger.AnnotationBlock;
import org.mosip.iso.finger.CommentBlock;
import org.mosip.iso.finger.FingerCaptureDeviceTechnology;
import org.mosip.iso.finger.FingerCaptureDeviceType;
import org.mosip.iso.finger.FingerCaptureDeviceVendor;
import org.mosip.iso.finger.FingerCertificationBlock;
import org.mosip.iso.finger.FingerCertificationFlag;
import org.mosip.iso.finger.FingerEncoder;
import org.mosip.iso.finger.FingerFormatIdentifier;
import org.mosip.iso.finger.FingerImageBitDepth;
import org.mosip.iso.finger.FingerImageCompressionType;
import org.mosip.iso.finger.FingerImpressionType;
import org.mosip.iso.finger.FingerPosition;
import org.mosip.iso.finger.FingerQualityAlgorithmIdentifier;
import org.mosip.iso.finger.FingerQualityAlgorithmVendorIdentifier;
import org.mosip.iso.finger.FingerQualityBlock;
import org.mosip.iso.finger.FingerScaleUnitType;
import org.mosip.iso.finger.FingerVersionNumber;
import org.mosip.iso.finger.SegmentationBlock;
import org.mosip.iso.iris.EyeLabel;
import org.mosip.iso.iris.ImageFormat;
import org.mosip.iso.iris.ImageType;
import org.mosip.iso.iris.IrisCaptureDeviceTechnology;
import org.mosip.iso.iris.IrisCaptureDeviceType;
import org.mosip.iso.iris.IrisCaptureDeviceVendor;
import org.mosip.iso.iris.IrisCertificationFlag;
import org.mosip.iso.iris.IrisEncoder;
import org.mosip.iso.iris.IrisFormatIdentifier;
import org.mosip.iso.iris.IrisImageBitDepth;
import org.mosip.iso.iris.IrisImageCompressionType;
import org.mosip.iso.iris.IrisQualityAlgorithmIdentifier;
import org.mosip.iso.iris.IrisQualityAlgorithmVendorIdentifier;
import org.mosip.iso.iris.IrisQualityBlock;
import org.mosip.iso.iris.IrisVersionNumber;
import org.mosip.iso.iris.Orientation;

import io.cucumber.messages.internal.com.google.common.io.Files;

import org.mosip.iso.face.CrossReference;
import org.mosip.iso.face.Expression;

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
	public void convertFinger(String inStream, String outFile, String biometricSubType) throws IOException {
		
		byte[] imageData = Base64.getDecoder().decode(inStream);
		
		//ImageDataType imageDataType = ImageDataType.JPEG2000_LOSS_LESS;
		ByteArrayInputStream bis = new ByteArrayInputStream(imageData);
		
		BufferedImage buffImg  = ImageIO.read(bis);


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
		
		byte [] isoData = FingerEncoder.convertFingerImageToISO19794_4_2011 
				(
						formatIdentifier, versionNumber, certificationFlag, 
						sourceType, deviceVendor, deviceType,
						captureDate, noOfRepresentations,
						qualityBlocks, certificationBlocks, 
						fingerPosition, representationNo, scaleUnitType, 
						captureDeviceSpatialSamplingRateHorizontal, captureDeviceSpatialSamplingRateVertical, 
						imageSpatialSamplingRateHorizontal, imageSpatialSamplingRateVertical,
						bitDepth, compressionType,
						impressionType, lineLengthHorizontal, lineLengthVertical,
						noOfFingerPresent, imageData, 
						segmentationBlock, annotationBlock, commentBlock
				);
		if (isoData != null)
		{
			Files.write(isoData,new File(outFile));
		
		}
    				
	}
	public void convertIris(String inStream, String outFile, String biometricSubType) throws IOException {
		
		byte[] imageData = Base64.getDecoder().decode(inStream);
		
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
		ImageFormat imageFormat = ImageFormat.MONO_JPEG_LOSS_LESS;//0A
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
		
		byte [] isoData = IrisEncoder.convertIrisImageToISO19794_6_2011 
			(
				formatIdentifier, versionNumber,
				certificationFlag, captureDate, 
				noOfRepresentations, representationNo, noOfEyesPresent,
				eyeLabel, imageType, imageFormat,
				horizontalOrientation, verticalOrientation, compressionType,
				imageWidth, imageHeight, bitDepth, range, rollAngleOfEye, rollAngleUncertainty, 
				irisCenterSmallestX, irisCenterLargestX, irisCenterSmallestY, irisCenterLargestY, 
				irisDiameterSmallest, irisDiameterLargest,
				sourceType, deviceVendor, deviceType, 
				qualityBlocks, 
				imageData, imageWidth, imageHeight
			);
		if (isoData != null)
		{
			Files.write(isoData,new File(outFile));
		
		}
	}

	public void convertFace(String inStream, String outFile) throws IOException {
		
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
		
		byte[] imageData = Base64.getDecoder().decode(inStream);
		
		ImageDataType imageDataType = ImageDataType.JPEG2000_LOSS_LESS;
		
		ByteArrayInputStream bis = new ByteArrayInputStream(imageData);
		
		BufferedImage buffImg  = ImageIO.read(bis);
		int imageWidth = buffImg.getWidth();
		int imageHeight = buffImg.getHeight();
		
		byte [] isoData = FaceEncoder.convertFaceImageToISO19794_5_2011 
				(
					formatIdentifier, versionNumber, 
					certificationFlag, temporalSequenceFlags, 
					captureDate, noOfRepresentations, noOfLandMarkPoints, 
					gender, eyeColour, featureMask, 
					hairColour, subjectHeight, expression, 
					features, poseAngle, poseAngleUncertainty, 
					faceImageType, sourceType, deviceVendor, deviceType, 
					qualityBlock, imageData, imageWidth, imageHeight, 
					imageDataType, spatialSamplingRateLevel, 
					postAcquisitionProcessing, crossReference, 
					imageColourSpace, landmarkPoints
				); 
		
		if (isoData != null)
		{
			Files.write(isoData,new File(outFile));
		
		}
	}
}
