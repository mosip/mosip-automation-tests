package io.mosip.testrig.dslrig.dataprovider.mds;



import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Files;

import io.mosip.biometrics.util.CommonUtil;
import io.mosip.biometrics.util.ConvertRequestDto;
import io.mosip.biometrics.util.face.CrossReference;
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
import io.mosip.testrig.dslrig.dataprovider.BiometricDataProvider;

public class ISOConverter {
	private static final Logger logger = LoggerFactory.getLogger(ISOConverter.class);

	static Map<String,Integer> mapFingerPos = new HashMap<String, Integer>();
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
		Integer fingerPosition = mapFingerPos.get(bioSubType);
		return fingerPosition;
	}
	public byte[] convertFinger(
	        byte[] inStream,
	        String outFile,
	        String biometricSubType,
	        String purpose) throws Exception {

	    /* ================= INPUT LOGGING ================= */
	    logger.info("FINGER INPUT received");
	    logger.info("FINGER subtype: {}", biometricSubType);
	    logger.info("FINGER purpose: {}", purpose);
	    logger.info("FINGER INPUT byte length: {}",
	            inStream != null ? inStream.length : "null");
	    logger.info("FINGER INPUT SHA-256: {}",
	            inStream != null ? DigestUtils.sha256Hex(inStream) : "null");

	    /* ================= JPEG → JP2 ================= */
	    byte[] jp2bytes = CommonUtil.convertJPEGToJP2UsingOpenCV(inStream, 95);

	    logger.info("FINGER JP2 generated");
	    logger.info("FINGER JP2 byte length: {}", jp2bytes.length);
	    logger.info("FINGER JP2 SHA-256: {}", DigestUtils.sha256Hex(jp2bytes));

	    /* ================= PREPARE REQUEST ================= */
	    ConvertRequestDto convertRequestDto = new ConvertRequestDto();
	    convertRequestDto.setBiometricSubType(biometricSubType);
	    convertRequestDto.setImageType(0);
	    convertRequestDto.setInputBytes(jp2bytes);
	    convertRequestDto.setModality("Finger");
	    convertRequestDto.setPurpose(purpose);
	    convertRequestDto.setVersion("ISO19794_4_2011");

	    /* ================= JP2 → ISO ================= */
	    byte[] isoData = convertFingerImageToISO(convertRequestDto);

	    /* ================= OUTPUT LOGGING ================= */
	    logger.info("FINGER ISO generated");
	    logger.info("FINGER ISO byte length: {}",
	            isoData != null ? isoData.length : "null");
	    logger.info("FINGER ISO SHA-256: {}",
	            isoData != null ? DigestUtils.sha256Hex(isoData) : "null");

	    if (isoData != null && outFile != null) {
	        Files.write(isoData, new File(outFile));
	        logger.info("FINGER ISO written to file: {}", outFile);
	    }

	    return isoData;
	}

	
	public byte[] convertIris(byte[] inStream, String outFile, String biometricSubType) throws Exception {

	    // ================= INPUT LOGGING =================
	    logger.info("IRIS INPUT subtype: {}", biometricSubType);
	    logger.info("IRIS INPUT byte length: {}", inStream != null ? inStream.length : "null");
	    logger.info("IRIS INPUT SHA-256: {}",
	            inStream != null ? DigestUtils.sha256Hex(inStream) : "null");

	    // Convert JPEG → JP2
	    byte[] jp2bytes = CommonUtil.convertJPEGToJP2UsingOpenCV(inStream, 95);

	    logger.info("IRIS JP2 generated");
	    logger.info("IRIS JP2 byte length: {}", jp2bytes.length);
	    logger.info("IRIS JP2 SHA-256: {}", DigestUtils.sha256Hex(jp2bytes));

	    ConvertRequestDto convertRequestDto = new ConvertRequestDto();
	    convertRequestDto.setBiometricSubType(biometricSubType);
	    convertRequestDto.setImageType(0);
	    convertRequestDto.setInputBytes(jp2bytes);
	    convertRequestDto.setModality("Iris");
	    convertRequestDto.setPurpose("Registration");
	    convertRequestDto.setVersion("ISO19794_6_2011");

	    // Convert JP2 → ISO
	    byte[] isoData = convertIrisImageToISO(convertRequestDto);

	    // ================= OUTPUT LOGGING =================
	    logger.info("IRIS ISO generated");
	    logger.info("IRIS ISO byte length: {}", isoData != null ? isoData.length : "null");
	    logger.info("IRIS ISO SHA-256: {}",
	            isoData != null ? DigestUtils.sha256Hex(isoData) : "null");

	    if (isoData != null && outFile != null) {
	        Files.write(isoData, new File(outFile));
	        logger.info("IRIS ISO written to file: {}", outFile);
	    }

	    return isoData;
	}
	
	public static byte[] convertIrisImageToISO(ConvertRequestDto convertRequestDto) throws Exception {
		if (convertRequestDto.getVersion().equals("ISO19794_6_2011")) {
			long formatIdentifier = IrisFormatIdentifier.FORMAT_IIR;
			long versionNumber = IrisVersionNumber.VERSION_020;
			int certificationFlag = IrisCertificationFlag.UNSPECIFIED;

			Date captureDate = new Date(0);// the date instance

			int algorithmVendorIdentifier = IrisQualityAlgorithmVendorIdentifier.UNSPECIFIED;
			int qualityAlgorithmIdentifier = IrisQualityAlgorithmIdentifier.UNSPECIFIED;
			int eyeLabel = getEyeLabel(convertRequestDto.getBiometricSubType());

			int imageType = convertRequestDto.getPurpose().equalsIgnoreCase("AUTH") ? ImageType.CROPPED_AND_MASKED
					: ImageType.CROPPED;
			int imageFormat = ImageFormat.MONO_JPEG2000;// 0A
			int horizontalOrientation = Orientation.UNDEFINED;
			int verticalOrientation = Orientation.UNDEFINED;
			int compressionType = convertRequestDto.getPurpose().equalsIgnoreCase("AUTH")
					? IrisImageCompressionType.JPEG_LOSSY
					: IrisImageCompressionType.JPEG_LOSSLESS_OR_NONE;

			int bitDepth = IrisImageBitDepth.BPP_08;

			int range = 0x0000;
			int rollAngleOfEye = 0xFFFF;// ANGLE_UNDEFINED
			int rollAngleUncertainty = 0xFFFF; // UNCERTAIN_UNDEFINED
			int irisCenterSmallestX = 0x0000; // COORDINATE_UNDEFINED
			int irisCenterLargestX = 0x0000; // COORDINATE_UNDEFINED
			int irisCenterSmallestY = 0x0000; // COORDINATE_UNDEFINED
			int irisCenterLargestY = 0x0000; // COORDINATE_UNDEFINED
			int irisDiameterSmallest = 0x0000; // COORDINATE_UNDEFINED
			int irisDiameterLargest = 0x0000; // COORDINATE_UNDEFINED

			int sourceType = IrisCaptureDeviceTechnology.CMOS_OR_CCD;
			int deviceVendor = IrisCaptureDeviceVendor.UNSPECIFIED;
			int deviceType = IrisCaptureDeviceType.UNSPECIFIED;

			int noOfRepresentations = 0x0001;
			int representationNo = 0x0001;
			int noOfEyesPresent = 0x0001;

			int quality = 80;
			IrisQualityBlock[] qualityBlocks = new IrisQualityBlock[] {
					new IrisQualityBlock(quality, algorithmVendorIdentifier, qualityAlgorithmIdentifier) };
			BufferedImage bufferedImage = CommonUtil.getBufferedImage(convertRequestDto);
			int imageWidth = bufferedImage.getWidth();
			int imageHeight = bufferedImage.getHeight();

			return IrisEncoder.convertIrisImageToISO19794_6_2011(formatIdentifier, versionNumber, certificationFlag, captureDate,
					noOfRepresentations, representationNo, noOfEyesPresent, eyeLabel, imageType, imageFormat,
					horizontalOrientation, verticalOrientation, compressionType, imageWidth, imageHeight, bitDepth,
					range, rollAngleOfEye, rollAngleUncertainty, irisCenterSmallestX, irisCenterLargestX,
					irisCenterSmallestY, irisCenterLargestY, irisDiameterSmallest, irisDiameterLargest, sourceType,
					deviceVendor, deviceType, qualityBlocks, convertRequestDto.getInputBytes(), imageWidth,
					imageHeight);
		}
		throw new UnsupportedOperationException();
	}
	
	private static byte getEyeLabel(String biometricSubType) {
		if (Objects.isNull(biometricSubType))
			return EyeLabel.UNSPECIFIED;

		switch (biometricSubType) {
		case "Right":
			return EyeLabel.RIGHT;
		case "Left":
			return EyeLabel.LEFT;
		default:
			return EyeLabel.UNSPECIFIED;
		}
	}


	public byte[] convertFace(byte[] inStream, String outFile) throws Exception {

	    /* ================= INPUT LOGGING ================= */
	    logger.info("FACE INPUT received");
	    logger.info("FACE INPUT byte length: {}", inStream != null ? inStream.length : "null");
	    logger.info("FACE INPUT SHA-256: {}",
	            inStream != null ? DigestUtils.sha256Hex(inStream) : "null");

	    /* ================= JPEG → JP2 ================= */
	    byte[] jp2bytes = CommonUtil.convertJPEGToJP2UsingOpenCV(inStream, 95);

	    logger.info("FACE JP2 generated");
	    logger.info("FACE JP2 byte length: {}", jp2bytes.length);
	    logger.info("FACE JP2 SHA-256: {}", DigestUtils.sha256Hex(jp2bytes));

	    /* ================= PREPARE REQUEST ================= */
	    ConvertRequestDto convertRequestDto = new ConvertRequestDto();
	    convertRequestDto.setBiometricSubType("");
	    convertRequestDto.setImageType(0);
	    convertRequestDto.setInputBytes(jp2bytes);
	    convertRequestDto.setModality("Face");
	    convertRequestDto.setPurpose("Registration");
	    convertRequestDto.setVersion("ISO19794_5_2011");

	    /* ================= JP2 → ISO ================= */
	    byte[] isoData = convertFaceImageToISO(convertRequestDto);

	    /* ================= OUTPUT LOGGING ================= */
	    logger.info("FACE ISO generated");
	    logger.info("FACE ISO byte length: {}", isoData != null ? isoData.length : "null");
	    logger.info("FACE ISO SHA-256: {}",
	            isoData != null ? DigestUtils.sha256Hex(isoData) : "null");

	    if (isoData != null && outFile != null) {
	        Files.write(isoData, new File(outFile));
	        logger.info("FACE ISO written to file: {}", outFile);
	    }

	    return isoData;
	}
	
	public static byte[] convertFingerImageToISO(ConvertRequestDto convertRequestDto) throws Exception {
		if (convertRequestDto.getVersion().equals("ISO19794_4_2011")) {
			long formatIdentifier = FingerFormatIdentifier.FORMAT_FIR;
			long versionNumber = FingerVersionNumber.VERSION_020;
			int certificationFlag = FingerCertificationFlag.UNSPECIFIED;
			int sourceType = FingerCaptureDeviceTechnology.UNSPECIFIED;
			int deviceVendor = FingerCaptureDeviceVendor.UNSPECIFIED;
			int deviceType = FingerCaptureDeviceType.UNSPECIFIED;
			Date captureDate = new Date(0);// the date instance
			int noOfRepresentations = 0x0001;

			int algorithmVendorIdentifier = FingerQualityAlgorithmVendorIdentifier.NIST;
			int qualityAlgorithmIdentifier = FingerQualityAlgorithmIdentifier.NIST;

			int quality = 50;
			FingerQualityBlock[] qualityBlocks = new FingerQualityBlock[] {
					new FingerQualityBlock(quality, algorithmVendorIdentifier, qualityAlgorithmIdentifier) };
			FingerCertificationBlock[] certificationBlocks = null;
			int fingerPosition = getFingerPosition(convertRequestDto.getBiometricSubType());
			int representationNo = 0x00;
			int scaleUnitType = FingerScaleUnitType.PIXELS_PER_INCH;
			int captureDeviceSpatialSamplingRateHorizontal = 500;
			int captureDeviceSpatialSamplingRateVertical = 500;
			int imageSpatialSamplingRateHorizontal = 500;
			int imageSpatialSamplingRateVertical = 500;
			int bitDepth = FingerImageBitDepth.BPP_08;
			
			@SuppressWarnings({ "java:S3358" })
			int compressionType =  convertRequestDto.getPurpose().equalsIgnoreCase("AUTH") ? convertRequestDto.getImageType() == 0 ? FingerImageCompressionType.JPEG_2000_LOSSY
					: FingerImageCompressionType.WSQ : FingerImageCompressionType.JPEG_2000_LOSS_LESS;
			int impressionType = FingerImpressionType.UNKNOWN;

			BufferedImage bufferedImage = CommonUtil.getBufferedImage(convertRequestDto);
			int lineLengthHorizontal = bufferedImage.getWidth();
			int lineLengthVertical = bufferedImage.getHeight();

			int noOfFingerPresent = 0x01;
			SegmentationBlock segmentationBlock = null;
			AnnotationBlock annotationBlock = null;
			CommentBlock[] commentBlocks = null;

			return FingerEncoder.convertFingerImageToISO19794_4_2011(formatIdentifier, versionNumber, certificationFlag, sourceType,
					deviceVendor, deviceType, captureDate, noOfRepresentations, qualityBlocks, certificationBlocks,
					fingerPosition, representationNo, scaleUnitType, captureDeviceSpatialSamplingRateHorizontal,
					captureDeviceSpatialSamplingRateVertical, imageSpatialSamplingRateHorizontal,
					imageSpatialSamplingRateVertical, bitDepth, compressionType, impressionType, lineLengthHorizontal,
					lineLengthVertical, noOfFingerPresent, convertRequestDto.getInputBytes(), segmentationBlock,
					annotationBlock, commentBlocks);
		}
		throw new UnsupportedOperationException();
	}
	private static int getFingerPosition(String biometricSubType) {
		if (biometricSubType == null)
			return FingerPosition.UNKNOWN;

		switch (biometricSubType) {
		case "Right Thumb":
			return FingerPosition.RIGHT_THUMB;
		case "Right IndexFinger":
			return FingerPosition.RIGHT_INDEX_FINGER;
		case "Right MiddleFinger":
			return FingerPosition.RIGHT_MIDDLE_FINGER;
		case "Right RingFinger":
			return FingerPosition.RIGHT_RING_FINGER;
		case "Right LittleFinger":
			return FingerPosition.RIGHT_LITTLE_FINGER;
		case "Left Thumb":
			return FingerPosition.LEFT_THUMB;
		case "Left IndexFinger":
			return FingerPosition.LEFT_INDEX_FINGER;
		case "Left MiddleFinger":
			return FingerPosition.LEFT_MIDDLE_FINGER;
		case "Left RingFinger":
			return FingerPosition.LEFT_RING_FINGER;
		case "Left LittleFinger":
			return FingerPosition.LEFT_LITTLE_FINGER;
		default:
			return FingerPosition.UNKNOWN;
		}
	}
	
	public static byte[] convertFaceImageToISO(ConvertRequestDto convertRequestDto) throws Exception {
		if (convertRequestDto.getVersion().equals("ISO19794_5_2011")) {
			long formatIdentifier = FaceFormatIdentifier.FORMAT_FAC;
			long versionNumber = FaceVersionNumber.VERSION_030;
			int certificationFlag = FaceCertificationFlag.UNSPECIFIED;
			int temporalSequenceFlags = TemporalSequenceFlags.ONE_REPRESENTATION;

			Date captureDate = new Date(0);// the date instance

			int noOfLandMarkPoints = 0x00;
			int algorithmVendorIdentifier = FaceQualityAlgorithmVendorIdentifier.ALGORITHM_VENDOR_IDENTIFIER_0001;
			int qualityAlgorithmIdentifier = FaceQualityAlgorithmIdentifier.ALGORITHM_IDENTIFIER_0001;

			int gender = Gender.UNKNOWN;
			int eyeColour = EyeColour.UNSPECIFIED;
			int featureMask = 0;
			int subjectHeight = HeightCodes.UNSPECIFIED;
			int hairColour = HairColour.UNSPECIFIED;
			int expression = 0;
			int features = Features.FEATURES_ARE_SPECIFIED;
			int[] poseAngle = { 0, 0, 0 };
			int[] poseAngleUncertainty = { 0, 0, 0 };
			int faceImageType = FaceImageType.FULL_FRONTAL;
			int imageColourSpace = ImageColourSpace.BIT_24_RGB;
			int sourceType = FaceCaptureDeviceTechnology.VIDEO_FRAME_ANALOG_CAMERA;
			int deviceVendor = FaceCaptureDeviceVendor.UNSPECIFIED;
			int deviceType = FaceCaptureDeviceType.UNSPECIFIED;

			int spatialSamplingRateLevel = SpatialSamplingRateLevel.SPATIAL_SAMPLING_RATE_LEVEL_180;
			int postAcquisitionProcessing = 0;
			int crossReference = CrossReference.BASIC;
			LandmarkPoints[] landmarkPoints = null;
			int noOfRepresentations = 0x0001;

			int quality = 40;
			FaceQualityBlock[] qualityBlock = new FaceQualityBlock[] {
					new FaceQualityBlock(quality, algorithmVendorIdentifier, qualityAlgorithmIdentifier) };
			int imageDataType = convertRequestDto.getPurpose().equalsIgnoreCase("AUTH") ? ImageDataType.JPEG2000_LOSSY
					: ImageDataType.JPEG2000_LOSS_LESS;

			BufferedImage bufferedImage = CommonUtil.getBufferedImage(convertRequestDto);
			int imageWidth = bufferedImage.getWidth();
			int imageHeight = bufferedImage.getHeight();
			byte[] threeDInformationAndData = null;

			return FaceEncoder.convertFaceImageToISO19794_5_2011(formatIdentifier, versionNumber, certificationFlag,
					temporalSequenceFlags, captureDate, noOfRepresentations, noOfLandMarkPoints, gender, eyeColour,
					hairColour, subjectHeight, expression, features, poseAngle, poseAngleUncertainty, faceImageType,
					sourceType, deviceVendor, deviceType, qualityBlock, convertRequestDto.getInputBytes(), imageWidth,
					imageHeight, imageDataType, spatialSamplingRateLevel, postAcquisitionProcessing, crossReference,
					imageColourSpace, landmarkPoints, threeDInformationAndData);
		}
		throw new UnsupportedOperationException();
	}

	

}
