package io.mosip.testrig.dslrig.dataprovider.biometric;

import java.io.FileNotFoundException;
import java.util.Base64;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import com.jamesmurty.utils.XMLBuilder;

import io.mosip.testrig.dslrig.dataprovider.util.CommonUtil;
import io.mosip.testrig.dslrig.dataprovider.variables.VariableManager;

public final class CbeffBirBuilder {

	private static final String XMLNS = "xmlns";
	private static final String XMLNS_URL = "http://standards.iso.org/iso-iec/19785/-3/ed-2/";
	private static final String MAJOR = "Major";
	private static final String MINOR = "Minor";
	private static final String CBEFFVERSION = "CBEFFVersion";
	private static final String VERSION = "Version";
	private static final String FALSE = "false";
	private static final String BDBINFO = "BDBInfo";
	private static final String BIRINFO = "BIRInfo";
	private static final String INTEGRITY = "Integrity";
	private static final String FORMAT = "Format";
	private static final String CREATIONDATE = "CreationDate";
	private static final String ORGANIZATION = "Organization";
	private static final String MOSIP = "Mosip";
	private static final String SUBTYPE = "Subtype";
	private static final String PURPOSE = "Purpose";
	private static final String LEVEL = "Level";
	private static final String SHA_256 = "SHA-256";
	private static final String ENROLL = "Enroll";
	private static final String QUALITY = "Quality";
	private static final String ALGORITHM = "Algorithm";
	private static final String SCORE = "Score";
	private static final String EXCEPTION = "EXCEPTION";
	private static final String OTHERS = "others";
	private static final String ENTRY = "entry";
	private static final String RETRIES = "RETRIES";
	private static final String SDK_SCORE = "SDK_SCORE";
	private static final String FORCE_CAPTURED = "FORCE_CAPTURED";
	private static final String PAYLOAD = "PAYLOAD";
	private static final String SPEC_VERSION = "SPEC_VERSION";

	private CbeffBirBuilder() {
	}

	public static String buildBirIris(String irisInfo, String irisName, String jtwSign, String payload,
			String qualityScore, boolean genarateValidCbeff, String exception, String contextKey)
			throws ParserConfigurationException, FactoryConfigurationError, TransformerException,
			FileNotFoundException {
		String today = CommonUtil.getUTCDateTime(null);
		XMLBuilder builder = XMLBuilder.create("BIR").a(XMLNS, XMLNS_URL)
				.e(VERSION).e(MAJOR).t("1").up().e(MINOR).t("1").up().up().e(CBEFFVERSION).e(MAJOR).t("1").up().e(MINOR)
				.t("1").up().up().e(BIRINFO).e(INTEGRITY).t(FALSE).up().up().e(BDBINFO).e(FORMAT).e(ORGANIZATION)
				.t(MOSIP).up().e("Type").t("9").up().up().e(CREATIONDATE).t(today).up().e("Type").t("Iris").up()
				.e(SUBTYPE).t(irisName).up().e(LEVEL).t("Raw").up().e(PURPOSE).t(ENROLL).up().e(QUALITY).e(ALGORITHM)
				.e(ORGANIZATION).t("HMAC").up().e("Type").t(SHA_256).up().up().e(SCORE)
				.t((int) Math.round(Double.parseDouble(qualityScore)) + "").up().up().up()
				.e("BDB").t(irisInfo).up().up();
		if (jtwSign != null && payload != null) {
			jtwSign = Base64.getEncoder().encodeToString(jtwSign.getBytes());
			builder.e("SB").t(jtwSign).up().

					e(OTHERS).e(ENTRY).a("key", EXCEPTION).t(exception).up().e(ENTRY).a("key", RETRIES).t("1").up()
					.e(ENTRY).a("key", SDK_SCORE).t("0.0").up().e(ENTRY).a("key", FORCE_CAPTURED).t(FALSE).up().e(ENTRY)
					.a("key", PAYLOAD).t(payload).up().e(ENTRY).a("key", SPEC_VERSION).t("0.9.5").up().up();
		}
		return builder.asString(null);
	}

	public static String buildBirFinger(String fingerInfo, String fingerName, String jtwSign, String payload,
			String qualityScore, boolean generateValidCbeff, String exception, String contextKey)
			throws ParserConfigurationException, FactoryConfigurationError, TransformerException,
			FileNotFoundException {
		String today = CommonUtil.getUTCDateTime(null);
		XMLBuilder builder = null;
		String bdbKey = "BDB";
		if (generateValidCbeff == false)
			bdbKey = "invalidBDB";
		builder = XMLBuilder.create("BIR").a(XMLNS, XMLNS_URL).e(VERSION)
				.e(MAJOR).t("1").up().e(MINOR).t("1").up().up().e(CBEFFVERSION).e(MAJOR).t("1").up().e(MINOR).t("1")
				.up().up().e(BIRINFO).e(INTEGRITY).t(FALSE).up().up().e(BDBINFO).e(FORMAT).e(ORGANIZATION).t(MOSIP).up()
				.e("Type").t("7").up().up().e(CREATIONDATE).t(today).up().e("Type").t("Finger").up().e(SUBTYPE)
				.t(fingerName).up().e(LEVEL).t("Raw").up().e(PURPOSE).t(ENROLL).up().e(QUALITY).e(ALGORITHM)
				.e(ORGANIZATION).t("HMAC").up().e("Type").t(SHA_256).up().up().e(SCORE)
				.t((int) Math.round(Double.parseDouble(qualityScore)) + "").up().up().up()
				.e(bdbKey).t(fingerInfo).up().up();
		if (jtwSign != null && payload != null) {
			jtwSign = Base64.getEncoder().encodeToString(jtwSign.getBytes());
			builder.e("SB").t(jtwSign).up().

					e(OTHERS).e(ENTRY).a("key", EXCEPTION).t(exception).up().e(ENTRY).a("key", RETRIES).t("1").up()
					.e(ENTRY).a("key", SDK_SCORE).t("0.0").up().e(ENTRY).a("key", FORCE_CAPTURED).t(FALSE).up().e(ENTRY)
					.a("key", PAYLOAD).t(payload).up().e(ENTRY).a("key", SPEC_VERSION).t("0.9.5").up().up();
		}
		return builder.asString(null);
	}

	public static String buildBirFace(String faceInfo, String jtwSign, String payload, String qualityScore,
			boolean genarateValidCbeff, String exception, String contextKey) throws ParserConfigurationException,
			FactoryConfigurationError, TransformerException, FileNotFoundException {
		String today = CommonUtil.getUTCDateTime(null);
		XMLBuilder builder = XMLBuilder.create("BIR").a(XMLNS, XMLNS_URL)
				.e(VERSION).e(MAJOR).t("1").up().e(MINOR).t("1").up().up().e(CBEFFVERSION).e(MAJOR).t("1").up().e(MINOR)
				.t("1").up().up().e(BIRINFO).e(INTEGRITY).t(FALSE).up().up().e(BDBINFO).e(FORMAT).e(ORGANIZATION)
				.t(MOSIP).up().e("Type").t("8").up().up().e(CREATIONDATE).t(today).up().e("Type").t("Face").up()
				.e(SUBTYPE).t("").up().e(LEVEL).t("Raw").up().e(PURPOSE).t(ENROLL).up().e(QUALITY).e(ALGORITHM)
				.e(ORGANIZATION).t("HMAC").up().e("Type").t(SHA_256).up().up().e(SCORE)
				.t((int) Math.round(Double.parseDouble(qualityScore)) + "").up().up().up()
				.e("BDB").t(faceInfo).up().up();
		if (jtwSign != null && payload != null) {
			jtwSign = Base64.getEncoder().encodeToString(jtwSign.getBytes());
			builder.e("SB").t(jtwSign).up().

					e(OTHERS).e(ENTRY).a("key", EXCEPTION).t(exception).up().e(ENTRY).a("key", RETRIES).t("1").up()
					.e(ENTRY).a("key", SDK_SCORE).t("0.0").up().e(ENTRY).a("key", FORCE_CAPTURED).t(FALSE).up().e(ENTRY)
					.a("key", PAYLOAD).t(payload).up().e(ENTRY).a("key", SPEC_VERSION).t("0.9.5").up().up();

		}
		if (Double.parseDouble(qualityScore) >= 80)
			VariableManager.setVariableValue(contextKey, "Biometric_Quality-Face", "level-9");
		else
			VariableManager.setVariableValue(contextKey, "Biometric_Quality-Face", "level-2");
		return builder.asString(null);
	}

	public static String buildBirExceptionPhoto(String faceInfo, String jtwSign, String payload, String qualityScore,
			boolean genarateValidCbeff, String exception, String contextKey) throws ParserConfigurationException,
			FactoryConfigurationError, TransformerException, FileNotFoundException {
		String today = CommonUtil.getUTCDateTime(null);
		XMLBuilder builder = XMLBuilder.create("BIR").a(XMLNS, XMLNS_URL)
				.e(VERSION).e(MAJOR).t("1").up().e(MINOR).t("1").up().up().e(CBEFFVERSION).e(MAJOR).t("1").up().e(MINOR)
				.t("1").up().up().e(BIRINFO).e(INTEGRITY).t(FALSE).up().up().e(BDBINFO).e(FORMAT).e(ORGANIZATION)
				.t(MOSIP).up().e("Type").t("8").up().up().e(CREATIONDATE).t(today).up().e("Type").t("ExceptionPhoto")
				.up().e(SUBTYPE).t("").up().e(LEVEL).t("Raw").up().e(PURPOSE).t(ENROLL).up().e(QUALITY).e(ALGORITHM)
				.e(ORGANIZATION).t("HMAC").up().e("Type").t(SHA_256).up().up().e(SCORE)
				.t((int) Math.round(Double.parseDouble(qualityScore)) + "").up().up().up()
				.e("BDB").t(faceInfo).up().up();
		if (jtwSign != null && payload != null) {
			jtwSign = Base64.getEncoder().encodeToString(jtwSign.getBytes());
			builder.e("SB").t(jtwSign).up().

					e(OTHERS).e(ENTRY).a("key", EXCEPTION).t(exception).up().e(ENTRY).a("key", RETRIES).t("1").up()
					.e(ENTRY).a("key", SDK_SCORE).t("0.0").up().e(ENTRY).a("key", FORCE_CAPTURED).t(FALSE).up().e(ENTRY)
					.a("key", PAYLOAD).t(payload).up().e(ENTRY).a("key", SPEC_VERSION).t("0.9.5").up().up();

		}

		return builder.asString(null);
	}
}
