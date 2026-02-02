package io.mosip.testrig.dslrig.dataprovider.mds;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.imageio.ImageIO;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;

import io.mosip.biometrics.util.CommonUtil;
import io.mosip.biometrics.util.ConvertRequestDto;
import io.mosip.biometrics.util.face.*;
import io.mosip.biometrics.util.finger.*;
import io.mosip.biometrics.util.iris.*;

public class ISOConverter {

	private static final Logger logger = LoggerFactory.getLogger(ISOConverter.class);

	/* ================= FINGER POSITION MAP ================= */
	static Map<String, Integer> mapFingerPos = new HashMap<>();
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

	/* ================= DETERMINISTIC RANDOM ================= */
	private static SecureRandom seededRandom(byte[] input) throws Exception {
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		byte[] seed = md.digest(input);
		SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
		random.setSeed(seed);
		return random;
	}

	/* ================= DETERMINISTIC IMAGE MUTATION ================= */
	private static byte[] deterministicMutate(byte[] input) throws Exception {
		BufferedImage img = ImageIO.read(new ByteArrayInputStream(input));
		if (img == null) {
			throw new IllegalStateException("Invalid image input");
		}

		SecureRandom random = seededRandom(input);
		int x = random.nextInt(img.getWidth());
		int y = random.nextInt(img.getHeight());
		img.setRGB(x, y, img.getRGB(x, y) ^ 0x00010101);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(img, "jpg", baos);
		return baos.toByteArray();
	}

	/* ================= FINGER ================= */
	public byte[] convertFinger(byte[] inStream, String outFile, String biometricSubType, String purpose)
			throws Exception {

		logger.info("FINGER INPUT SHA-256: {}", DigestUtils.sha256Hex(inStream));

		byte[] mutated = deterministicMutate(inStream);
		SecureRandom random = seededRandom(inStream);
		int quality = 90 + random.nextInt(6);

		byte[] jp2bytes = CommonUtil.convertJPEGToJP2UsingOpenCV(mutated, quality);

		ConvertRequestDto dto = new ConvertRequestDto();
		dto.setBiometricSubType(biometricSubType);
		dto.setImageType(0);
		dto.setInputBytes(jp2bytes);
		dto.setModality("Finger");
		dto.setPurpose(purpose);
		dto.setVersion("ISO19794_4_2011");

		byte[] iso = convertFingerImageToISO(dto);

		if (iso != null && outFile != null) {
			Files.write(iso, new File(outFile));
		}
		return iso;
	}

	/* ================= IRIS ================= */
	public byte[] convertIris(byte[] inStream, String outFile, String biometricSubType) throws Exception {

		logger.info("IRIS INPUT SHA-256: {}", DigestUtils.sha256Hex(inStream));

		byte[] mutated = deterministicMutate(inStream);
		SecureRandom random = seededRandom(inStream);
		int quality = 90 + random.nextInt(6);

		byte[] jp2bytes = CommonUtil.convertJPEGToJP2UsingOpenCV(mutated, quality);

		ConvertRequestDto dto = new ConvertRequestDto();
		dto.setBiometricSubType(biometricSubType);
		dto.setImageType(0);
		dto.setInputBytes(jp2bytes);
		dto.setModality("Iris");
		dto.setPurpose("Registration");
		dto.setVersion("ISO19794_6_2011");

		byte[] iso = convertIrisImageToISO(dto);

		if (iso != null && outFile != null) {
			Files.write(iso, new File(outFile));
		}
		return iso;
	}

	/* ================= FACE ================= */
	public byte[] convertFace(byte[] inStream, String outFile) throws Exception {

		logger.info("FACE INPUT SHA-256: {}", DigestUtils.sha256Hex(inStream));

		byte[] mutated = deterministicMutate(inStream);
		SecureRandom random = seededRandom(inStream);
		int quality = 90 + random.nextInt(6);

		byte[] jp2bytes = CommonUtil.convertJPEGToJP2UsingOpenCV(mutated, quality);

		ConvertRequestDto dto = new ConvertRequestDto();
		dto.setBiometricSubType("");
		dto.setImageType(0);
		dto.setInputBytes(jp2bytes);
		dto.setModality("Face");
		dto.setPurpose("Registration");
		dto.setVersion("ISO19794_5_2011");

		byte[] iso = convertFaceImageToISO(dto);

		if (iso != null && outFile != null) {
			Files.write(iso, new File(outFile));
		}
		return iso;
	}

	/* ================= FINGER ISO ================= */
	public static byte[] convertFingerImageToISO(ConvertRequestDto dto) throws Exception {

		BufferedImage img = CommonUtil.getBufferedImage(dto);

		return FingerEncoder.convertFingerImageToISO19794_4_2011(FingerFormatIdentifier.FORMAT_FIR,
				FingerVersionNumber.VERSION_020, FingerCertificationFlag.UNSPECIFIED,
				FingerCaptureDeviceTechnology.UNSPECIFIED, FingerCaptureDeviceVendor.UNSPECIFIED,
				FingerCaptureDeviceType.UNSPECIFIED, new Date(0), 1,
				new FingerQualityBlock[] { new FingerQualityBlock(50, FingerQualityAlgorithmVendorIdentifier.NIST,
						FingerQualityAlgorithmIdentifier.NIST) },
				null, getFingerPosition(dto.getBiometricSubType()), 0, FingerScaleUnitType.PIXELS_PER_INCH, 500, 500,
				500, 500, FingerImageBitDepth.BPP_08, FingerImageCompressionType.JPEG_2000_LOSS_LESS,
				FingerImpressionType.UNKNOWN, img.getWidth(), img.getHeight(), 1, dto.getInputBytes(), null, null,
				null);
	}

	/* ================= IRIS ISO ================= */
	public static byte[] convertIrisImageToISO(ConvertRequestDto dto) throws Exception {

		BufferedImage img = CommonUtil.getBufferedImage(dto);

		return IrisEncoder.convertIrisImageToISO19794_6_2011(IrisFormatIdentifier.FORMAT_IIR,
				IrisVersionNumber.VERSION_020, IrisCertificationFlag.UNSPECIFIED, new Date(0), 1, 1, 1,
				getEyeLabel(dto.getBiometricSubType()), ImageType.CROPPED, ImageFormat.MONO_JPEG2000,
				Orientation.UNDEFINED, Orientation.UNDEFINED, IrisImageCompressionType.JPEG_LOSSLESS_OR_NONE,
				img.getWidth(), img.getHeight(), IrisImageBitDepth.BPP_08, 0, 0xFFFF, 0xFFFF, 0, 0, 0, 0, 0, 0,
				IrisCaptureDeviceTechnology.CMOS_OR_CCD, IrisCaptureDeviceVendor.UNSPECIFIED,
				IrisCaptureDeviceType.UNSPECIFIED,
				new IrisQualityBlock[] { new IrisQualityBlock(80, IrisQualityAlgorithmVendorIdentifier.UNSPECIFIED,
						IrisQualityAlgorithmIdentifier.UNSPECIFIED) },
				dto.getInputBytes(), img.getWidth(), img.getHeight());
	}

	/* ================= FACE ISO ================= */
	public static byte[] convertFaceImageToISO(ConvertRequestDto dto) throws Exception {

		BufferedImage img = CommonUtil.getBufferedImage(dto);

		return FaceEncoder
				.convertFaceImageToISO19794_5_2011(FaceFormatIdentifier.FORMAT_FAC, FaceVersionNumber.VERSION_030,
						FaceCertificationFlag.UNSPECIFIED, TemporalSequenceFlags.ONE_REPRESENTATION, new Date(0), 1, 0,
						Gender.UNKNOWN, EyeColour.UNSPECIFIED, HairColour.UNSPECIFIED, HeightCodes.UNSPECIFIED, 0,
						Features.FEATURES_ARE_SPECIFIED, new int[] { 0, 0, 0 }, new int[] { 0, 0, 0 },
						FaceImageType.FULL_FRONTAL, FaceCaptureDeviceTechnology.VIDEO_FRAME_ANALOG_CAMERA,
						FaceCaptureDeviceVendor.UNSPECIFIED, FaceCaptureDeviceType.UNSPECIFIED,
						new FaceQualityBlock[] { new FaceQualityBlock(40,
								FaceQualityAlgorithmVendorIdentifier.ALGORITHM_VENDOR_IDENTIFIER_0001,
								FaceQualityAlgorithmIdentifier.ALGORITHM_IDENTIFIER_0001) },
						dto.getInputBytes(), img.getWidth(), img.getHeight(), ImageDataType.JPEG2000_LOSS_LESS,
						SpatialSamplingRateLevel.SPATIAL_SAMPLING_RATE_LEVEL_180, 0, CrossReference.BASIC,
						ImageColourSpace.BIT_24_RGB, null, null);
	}

	/* ================= HELPERS ================= */
	private static int getEyeLabel(String biometricSubType) {
		if (Objects.isNull(biometricSubType)) {
			return EyeLabel.UNSPECIFIED;
		}

		return biometricSubType.equalsIgnoreCase("Right") ? EyeLabel.RIGHT
				: biometricSubType.equalsIgnoreCase("Left") ? EyeLabel.LEFT : EyeLabel.UNSPECIFIED;
	}

	static int getFingerPosition(String biometricSubType) {
		return mapFingerPos.getOrDefault(biometricSubType, FingerPosition.UNKNOWN);
	}
}
