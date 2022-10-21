package io.mosip.ivv.orchestrator;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.IntStream;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.admin.fw.util.AdminTestUtil;
import io.mosip.admin.fw.util.TestCaseDTO;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.kernel.core.crypto.spi.CryptoCoreSpec;
import io.mosip.kernel.crypto.jce.core.CryptoCore;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.core.util.HMACUtils2;
//import io.mosip.kernel.cryptomanager.dto.CryptomanagerRequestDto;
//import io.mosip.kernel.cryptomanager.dto.CryptomanagerResponseDto;
//import io.mosip.kernel.cryptomanager.service.CryptomanagerService;
//import io.mosip.kernel.cryptomanager.service.impl.CryptomanagerServiceImpl;
//import io.mosip.kernel.keygenerator.bouncycastle.util.KeyGeneratorUtils;
//import io.mosip.kernel.keymanagerservice.util.KeymanagerUtil;
import io.mosip.testscripts.GetWithParam;
import io.mosip.testscripts.GetWithQueryParam;
import io.mosip.testscripts.PatchWithPathParam;
import io.mosip.testscripts.PutWithPathParam;
import io.mosip.testscripts.SimplePost;
import io.mosip.testscripts.SimplePut;
import io.restassured.response.Response;

public class RegprocStatusHelper extends BaseTestCaseUtil {
	
}
//
//	static Logger logger = Logger.getLogger(RegprocStatusHelper.class);
//
//	//private KeymanagerUtil keymanagerUtil=new KeymanagerUtil();
//	
//	//private CryptomanagerService cryptomanagerService=new CryptomanagerServiceImpl();
//	private static final String GetCertificate = "regproc/GetCertificate/GetCert.yml";
//	
//	private CryptoCoreSpec<byte[], byte[], SecretKey, PublicKey, PrivateKey, String> cryptoCore =new CryptoCore();
//
//	private static final String OperatorOnboard = "regproc/OperatorOnboard/operatorOnboard.yml";
//	
//	SimplePost simplepost = new SimplePost();
//	PatchWithPathParam patchwithpathparam = new PatchWithPathParam();
//	SimplePut simpleput = new SimplePut();
//	PutWithPathParam putwithpathparam = new PutWithPathParam();
//	GetWithParam getWithParam = new GetWithParam();
//	GetWithQueryParam getWithQueryParam = new GetWithQueryParam();
//	AdminTestUtil adminTestUtil=new AdminTestUtil();
//	
//	public RegprocStatusHelper()
//	{
//		
//	}
//	
////	RegprocStatusHelper(CryptomanagerService cryptomanagerService,KeymanagerUtil keymanagerUtil,CryptoCoreSpec<byte[], byte[], SecretKey, PublicKey, PrivateKey, String> cryptoCore)
////	{
////		this.cryptomanagerService=cryptomanagerService;
////		this.keymanagerUtil=keymanagerUtil;
////		this.cryptoCore=cryptoCore;
////	}
////		
//	
//	public String getCert() throws RigInternalError {
//		try {
//			String thumbprint=null;
//			String lastSyncTime = null;
//			Object[] testObjPost = getWithParam.getYmlTestData(GetCertificate);
//
//			TestCaseDTO testPost = (TestCaseDTO) testObjPost[0];
//
//			getWithParam.test(testPost);
//			Response response = getWithParam.response;
//
//			if (response != null) {
//				JSONObject jsonResp = new JSONObject(response.getBody().asString());
//				String cert = jsonResp.getJSONObject("response").getString("certificate");
//				byte[] b = DigestUtils.sha256(cert.getBytes());
//				 thumbprint = io.mosip.kernel.core.util.CryptoUtil.encodeToURLSafeBase64(b);
//
//				logger.info(thumbprint);
//
//			}
//			return thumbprint;
//		} catch (Exception e) {
//			throw new RigInternalError(e.getMessage());
//
//		}
//		
//	}
//
//	/**
//	 * convert certificate
//	 * @return
//	 * @throws RigInternalError
//	 */
//	public Certificate ConvertCert() throws RigInternalError {
//		try {
//			String thumbprint=null;
//			Certificate certificate=null;
//			String lastSyncTime = null;
//			Object[] testObjPost = getWithParam.getYmlTestData(GetCertificate);
//
//			TestCaseDTO testPost = (TestCaseDTO) testObjPost[0];
//
//			getWithParam.test(testPost);
//			Response response = getWithParam.response;
//
//			if (response != null) {
//				JSONObject jsonResp = new JSONObject(response.getBody().asString());
//				String cert = jsonResp.getJSONObject("response").getString("certificate");
//				
//				 certificate = keymanagerUtil.convertToCertificate(cert);
//				
//
//			}
//			return certificate;
//		} catch (Exception e) {
//			throw new RigInternalError(e.getMessage());
//
//		}
//		
//	}
//
//	/**
//	 * Method to insert specified number of 0s in the beginning of the given string
//	 * 
//	 * @param string
//	 * @param count  - number of 0's to be inserted
//	 * @return bytes
//	 */
//	private byte[] prependZeros(byte[] string, int count) {
//		byte[] newBytes = new byte[string.length + count];
//		int i = 0;
//		for (; i < count; i++) {
//			newBytes[i] = 0;
//		}
//
//		for (int j = 0; i < newBytes.length; i++, j++) {
//			newBytes[i] = string[j];
//		}
//
//		return newBytes;
//	}
//	
//	
//	/**
//	 * Method to return the XOR of the given strings
//	 * 
//	 */
//	private byte[] getXOR(String timestamp, String transactionId) {
//		logger.info(
//				"Started getting XOR of timestamp and transactionId");
//
//		byte[] timestampBytes = timestamp.getBytes();
//		byte[] transactionIdBytes = transactionId.getBytes();
//		// Lengths of the given strings
//		int timestampLength = timestampBytes.length;
//		int transactionIdLength = transactionIdBytes.length;
//
//		// Make both the strings of equal lengths
//		// by inserting 0s in the beginning
//		if (timestampLength > transactionIdLength) {
//			transactionIdBytes = prependZeros(transactionIdBytes, timestampLength - transactionIdLength);
//		} else if (transactionIdLength > timestampLength) {
//			timestampBytes = prependZeros(timestampBytes, transactionIdLength - timestampLength);
//		}
//
//		// Updated length
//		int length = Math.max(timestampLength, transactionIdLength);
//		byte[] xorBytes = new byte[length];
//
//		// To store the resultant XOR
//		for (int i = 0; i < length; i++) {
//			xorBytes[i] = (byte) (timestampBytes[i] ^ transactionIdBytes[i]);
//		}
//
//		logger.info(
//				"Returning XOR of timestamp and transactionId");
//
//		return xorBytes;
//	}
//	
//	/**
//	 * Gets the last bytes.
//	 *
//	 * @param xorBytes
//	 * @param lastBytesNum the last bytes num
//	 * @return the last bytes
//	 */
//	private byte[] getLastBytes(byte[] xorBytes, int lastBytesNum) {
//		assert (xorBytes.length >= lastBytesNum);
//		return Arrays.copyOfRange(xorBytes, xorBytes.length - lastBytesNum, xorBytes.length);
//	}
//	
//	/**
//	 * Split encrypted data.
//	 *
//	 * @param data the data
//	 * @return the splitted encrypted data
//	 */
//	public SplittedEncryptedData splitEncryptedData(String data) {
//		byte[] dataBytes = CryptoUtil.decodeURLSafeBase64(data);
//		byte[][] splits = splitAtFirstOccurance(dataBytes,
//				String.valueOf("#KEY_SPLITTER#").getBytes());
//		return new SplittedEncryptedData(CryptoUtil.encodeToURLSafeBase64(splits[0]), CryptoUtil.encodeToURLSafeBase64(splits[1]));
//	}
//	
//	
//	/**
//	 * The Class SplittedEncryptedData.
//	 */
//	public static class SplittedEncryptedData {
//		private String encryptedSessionKey;
//		private String encryptedData;
//
//		public SplittedEncryptedData() {
//			super();
//		}
//
//		public SplittedEncryptedData(String encryptedSessionKey, String encryptedData) {
//			super();
//			this.encryptedData = encryptedData;
//			this.encryptedSessionKey = encryptedSessionKey;
//		}
//
//		public String getEncryptedData() {
//			return encryptedData;
//		}
//
//		public void setEncryptedData(String encryptedData) {
//			this.encryptedData = encryptedData;
//		}
//
//		public String getEncryptedSessionKey() {
//			return encryptedSessionKey;
//		}
//
//		public void setEncryptedSessionKey(String encryptedSessionKey) {
//			this.encryptedSessionKey = encryptedSessionKey;
//		}
//
//	}
//	
//	/**
//	 * Split at first occurance.
//	 *
//	 * @param strBytes the str bytes
//	 * @param sepBytes the sep bytes
//	 * @return the byte[][]
//	 */
//	private static byte[][] splitAtFirstOccurance(byte[] strBytes, byte[] sepBytes) {
//		int index = findIndex(strBytes, sepBytes);
//		if (index >= 0) {
//			byte[] bytes1 = new byte[index];
//			byte[] bytes2 = new byte[strBytes.length - (bytes1.length + sepBytes.length)];
//			System.arraycopy(strBytes, 0, bytes1, 0, bytes1.length);
//			System.arraycopy(strBytes, (bytes1.length + sepBytes.length), bytes2, 0, bytes2.length);
//			return new byte[][] { bytes1, bytes2 };
//		} else {
//			return new byte[][] { strBytes, new byte[0] };
//		}
//	}
//	
//	/**
//	 * Find index.
//	 *
//	 * @param arr    the arr
//	 * @param subarr the subarr
//	 * @return the int
//	 */
//	private static int findIndex(byte arr[], byte[] subarr) {
//		int len = arr.length;
//		int subArrayLen = subarr.length;
//		return IntStream.range(0, len).filter(currentIndex -> {
//			if ((currentIndex + subArrayLen) <= len) {
//				byte[] sArray = new byte[subArrayLen];
//				System.arraycopy(arr, currentIndex, sArray, 0, subArrayLen);
//				return Arrays.equals(sArray, subarr);
//			}
//			return false;
//		}).findFirst() // first occurence
//				.orElse(-1); // No element found
//	}
//	
//	private synchronized SplittedEncryptedData getSessionKey(Map<String, Object> requestMap, byte[] data) {
//		logger.info(
//				"Getting sessionKey for User Onboard Authentication with IDA");
//
//		String timestamp = (String) requestMap.get("timestamp");
//		byte[] xorBytes = getXOR(timestamp, "1234567890");
//		byte[] saltLastBytes = getLastBytes(xorBytes, 12);
//		byte[] aadLastBytes = getLastBytes(xorBytes, 16);
//
//		//CryptomanagerRequestDto cryptomanagerRequestDto = new CryptomanagerRequestDto();
//		cryptomanagerRequestDto.setAad(CryptoUtil.encodeToURLSafeBase64(aadLastBytes));
//		cryptomanagerRequestDto.setApplicationId("IDA");
//		cryptomanagerRequestDto.setData(CryptoUtil.encodeToURLSafeBase64(data));
//		cryptomanagerRequestDto.setReferenceId("INTERNAL");
//		cryptomanagerRequestDto.setSalt(CryptoUtil.encodeToURLSafeBase64(saltLastBytes));
//		cryptomanagerRequestDto.setTimeStamp(DateUtils.getUTCCurrentDateTime());
//		//Note: As thumbprint is sent as part of request, there is no need to prepend thumbprint in encrypted data
//		cryptomanagerRequestDto.setPrependThumbprint(false);
//		//CryptomanagerResponseDto cryptomanagerResponseDto = cryptomanagerService.encrypt(cryptomanagerRequestDto);
//
//		logger.info(
//				"Returning the sessionKey for User Onboard Authentication with IDA");
//		return splitEncryptedData(cryptomanagerResponseDto.getData());
//	}
//
//	/* will work
//	
//	private LinkedHashMap<String, Object> buildDataBlock(String bioType, String bioSubType, String bioValue,
//			String previousHash) throws NoSuchAlgorithmException {
//		
//		LinkedHashMap<String, Object> dataBlock = new LinkedHashMap<>();
//		Map<String, Object> data = new HashMap<>();
//		data.put("timestamp",
//				DateUtils.formatToISOString(DateUtils.getUTCCurrentDateTime	()));	
//		data.put("bioType", bioType);
//		data.put("bioSubType", bioSubType);
//		//SplittedEncryptedData responseMap = getSessionKey(data, attributeISO);
//		///////////
//		SplittedEncryptedData responseMap=encryptBiometrics(bioValue,DateUtils.formatToISOString(DateUtils.getUTCCurrentDateTime()),"1234567890",true);
//		
//		SplittedEncryptedData encryptBiometrics(@RequestBody String bioValue,
//				@RequestParam(name = "timestamp", required = false) @Nullable String timestamp,
//				@RequestParam(name = "transactionId", required = false) @Nullable String transactionId,
//				@RequestParam(name = "isInternal", required = false) @Nullable boolean isInternal)
//		/////////////
//		data.put("bioValue", responseMap.getEncryptedData());
//		data.put("transactionId", "1234567890");
//		data.put("purpose", "Auth");
//		data.put("env", "Staging");
//		String dataBlockJsonString = "";
//		try {
//			dataBlockJsonString = new ObjectMapper().writeValueAsString(data);
//			dataBlock.put("data",
//					CryptoUtil.encodeToURLSafeBase64(dataBlockJsonString.getBytes()));
//		} catch (IOException exIoException) {
//			exIoException.printStackTrace();
//		}
//
//		String presentHash = HMACUtils2.digestAsPlainText(dataBlockJsonString.getBytes());
//		String concatenatedHash = previousHash + presentHash;
//		String finalHash = HMACUtils2.digestAsPlainText(concatenatedHash.getBytes());
//
//		dataBlock.put("hash", finalHash);
//		dataBlock.put("sessionKey", responseMap.getEncryptedSessionKey());
//
//		//Signing encrypted auth biometrics is not required
//
//		logger.info(
//				"Returning the dataBlock for User Onboard Authentication with IDA");
//
//		return dataBlock;
//	}
//	*/
//	@SuppressWarnings("unchecked")
//	private Map<String, Object> getIdaAuthResponse(Map<String, Object> idaRequestMap, Map<String, Object> requestMap
//			, Certificate certificate,String thumbprint) {
//		try {
//
//			PublicKey publicKey = certificate.getPublicKey();
//			idaRequestMap.put("thumbprint", thumbprint);
//
//			logger.info( "Getting Symmetric Key.....");
//			// Symmetric key alias session key
//			KeyGenerator keyGenerator = KeyGeneratorUtils.getKeyGenerator("AES", 256);
//			// Generate AES Session Key
//			final SecretKey symmentricKey = keyGenerator.generateKey();
//
//			logger.info("preparing request.....");
//			// request
//			idaRequestMap.put("request",
//					CryptoUtil.encodeToURLSafeBase64(cryptoCore.symmetricEncrypt(symmentricKey,
//							new ObjectMapper().writeValueAsString(requestMap).getBytes(), null)));
//
//			logger.info("preparing request HMAC.....");
//			// requestHMAC
//			idaRequestMap.put("requestHMAC",
//					CryptoUtil.encodeToURLSafeBase64(cryptoCore.symmetricEncrypt(symmentricKey, HMACUtils2
//							.digestAsPlainText(new ObjectMapper().writeValueAsString(requestMap).getBytes()).getBytes(),
//							null)));
//
//			logger.info("preparing request Session Key.....");
//			// requestSession Key
//			idaRequestMap.put("requestSessionKey",
//					CryptoUtil.encodeToURLSafeBase64(cryptoCore.asymmetricEncrypt(publicKey, symmentricKey.getEncoded())));
//
//			logger.info("Ida Auth rest calling.....");
//
////			LinkedHashMap<String, Object> onBoardResponse = (LinkedHashMap<String, Object>) serviceDelegateUtil.post(
////					RegistrationConstants.ON_BOARD_IDA_VALIDATION, idaRequestMap,
////					RegistrationConstants.JOB_TRIGGER_POINT_SYSTEM);
//
//			return idaRequestMap;
//
//		} catch (Exception regBasedCheckedException) {
//			
//		}
//		return null;
//
//	}
//	
//	/*
//	
//	public void operatoronboard(String thumbprint,String personFilePathvalue) throws RigInternalError {
//		try {
//			
//			Object[] testObjPost = simplepost.getYmlTestData(OperatorOnboard);
//
//			TestCaseDTO testPost = (TestCaseDTO) testObjPost[0];
//
//			String inputJson = adminTestUtil.getJsonFromTemplate(testPost.getInput(), testPost.getInputTemplate());
//			
//			inputJson = JsonPrecondtion.parseAndReturnJsonContent(inputJson,
//					thumbprint, "thumbprint");
//
//			List<String> modalityList = new ArrayList<>();
//			//modalityList.add(E2EConstants.FACEFETCH);
//			//modalityList.add(E2EConstants.FINGERFETCH);
//		  String bioType=E2EConstants.FINGERBIOTYPE;
//			switch(bioType) {
//			case E2EConstants.FACEBIOTYPE:
//				modalityList.add(E2EConstants.FACEFETCH);
//				//modalityToLog = bioType;
//				//modalityKeyTogetBioValue = E2EConstants.FACEFETCH;
//				break;
//			case E2EConstants.IRISBIOTYPE:
//				modalityList.add(E2EConstants.IRISFETCH);
//				//modalityToLog = bioSubType+"_"+bioType;
//				//modalityKeyTogetBioValue = (bioSubType.equalsIgnoreCase("left"))? E2EConstants.LEFT_EYE:E2EConstants.RIGHT_EYE;
//				break;
//			case E2EConstants.FINGERBIOTYPE:
//				modalityList.add(E2EConstants.FINGERFETCH);
//				//modalityToLog = bioSubType;
//				//modalityKeyTogetBioValue = bioSubType;
//				break;
//			default:
//				throw new RigInternalError("Given BIO Type in device property file is not valid");
//			}
//			String bioResponse = packetUtility.retrieveBiometric(personFilePathvalue, modalityList);
//			String bioValue = JsonPrecondtion.getValueFromJson(bioResponse, "Left IndexFinger");
//			List<Map<String, Object>> listOfBiometric = new ArrayList<>();
//			Map<String, Object> requestMap = new LinkedHashMap<>();
//			//Byte format 
//			String previousHash = HMACUtils2.digestAsPlainText("".getBytes());
//			for (String dto : modalityList) {
//				//BiometricType bioType = Biometric.getSingleTypeByAttribute(dto.getBioAttribute());
//				String bioSubType = "Left IndexFinger";//getSubTypesAsString(bioType, dto.getBioAttribute());
////				LinkedHashMap<String, Object> dataBlock = buildDataBlock(bioType, bioSubType,
////						bioValue, previousHash);
////				dataBlock.put("thumbprint", thumbprint);
////				previousHash = (String) dataBlock.get("hash");
////				listOfBiometric.add(dataBlock);
//			}
//			
//			requestMap.put("biometrics", listOfBiometric);
//			requestMap.put("timestamp",
//					DateUtils.formatToISOString(DateUtils.getUTCCurrentDateTime()));
//			Map<String, Object> idaRequestMap = new LinkedHashMap<>();
//			Map<String, Object> response = getIdaAuthResponse(idaRequestMap, requestMap,
//					ConvertCert(),"thumb");
//		
//			
//			
//			testPost.setInput(inputJson);
//			
//			
//			simplepost.test(testPost);
//			//Response response = simplepost.response;
//
//			if (response != null) {
//				//JSONObject jsonResp = new JSONObject(response.getBody().asString());
//				//String cert = jsonResp.getJSONObject("response").getString("certificate");
//				
//			}
//		} catch (Exception e) {
//			throw new RigInternalError(e.getMessage());
//
//		}
//		
//		*/
//
		
	//}