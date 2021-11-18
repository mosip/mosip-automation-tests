package io.mosip.test.packetcreator.mosippacketcreator.service;

import static java.util.Arrays.copyOfRange;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStore.ProtectionParameter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Base64;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource.PSpecified;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import tss.Tpm;
import tss.TpmFactory;
import tss.tpm.CreatePrimaryResponse;
import tss.tpm.TPM2B_PUBLIC_KEY_RSA;
import tss.tpm.TPMA_OBJECT;
import tss.tpm.TPMS_NULL_SIG_SCHEME;
import tss.tpm.TPMS_PCR_SELECTION;
import tss.tpm.TPMS_RSA_PARMS;
import tss.tpm.TPMS_SENSITIVE_CREATE;
import tss.tpm.TPMS_SIGNATURE_RSASSA;
import tss.tpm.TPMS_SIG_SCHEME_RSASSA;
import tss.tpm.TPMT_HA;
import tss.tpm.TPMT_PUBLIC;
import tss.tpm.TPMT_SYM_DEF_OBJECT;
import tss.tpm.TPMT_TK_HASHCHECK;
import tss.tpm.TPMU_SIGNATURE;
import tss.tpm.TPM_ALG_ID;
import tss.tpm.TPM_HANDLE;
import tss.tpm.TPM_RH;

@Component
public class CryptoUtil {
    Logger logger = LoggerFactory.getLogger(CryptoUtil.class);

    private static final int GCM_NONCE_LENGTH = 12;
    private static final int GCM_AAD_LENGTH = 32;
    private static final String HMAC_ALGORITHM_NAME = "SHA-256";
    private static final String SIGN_ALGORITHM = "SHA256withRSA";

    private static final String KEY_PATH = System.getProperty("user.home");
    private static final String KEYS_DIR = ".mosipkeys";
    private static final String PRIVATE_KEY = "reg.key";

    private static final byte[] NULL_VECTOR = new byte[0];

    @Value("${mosip.test.regclient.encryption.appid}")
    private String encryptionAppId;

    @Value("${mosip.test.keymanager.encryptapi}")
    private String encryptApi;

    @Value("${mosip.test.tpm.available}")
    private boolean tpmAvailable;

    @Value("${mosip.test.tpm.simulator}")
    private boolean tpmSimulator;
    
    @Value("${mosip.test.crypto.prependthumbprint}")
    private boolean prependthumbprint;

    @Autowired
    private APIRequestUtil apiUtil;

    @Value("${mosip.test.baseurl}")
    private String baseUrl;
    
    @Value("${mosip.test.regclient.machineid}")
    private String machineid;
    
    @Value("${mosip.test.persona.configpath}")
	private String personaConfigPath;
   
    @Autowired
    private ContextUtils contextUtils;
   
    private SecureRandom sr = new SecureRandom();

    private static Tpm tpm;

    private static CreatePrimaryResponse signingPrimaryResponse;

    @PostConstruct
    public void initialize() {
        if(tpmAvailable) {
        	if(tpmSimulator)
        		tpm = TpmFactory.localTpmSimulator();
        	else
        		tpm = TpmFactory.platformTpm();
            signingPrimaryResponse = createSigningKey();
        }
    }
    
    public byte[] encrypt(byte[] data, String referenceId, String contextKey) throws Exception {
    	
    	if(contextKey != null && !contextKey.equals("")) {
    		
    		Properties props = contextUtils.loadServerContext(contextKey);
    		props.forEach((k,v)->{
    			if(k.toString().equals("mosip.test.baseurl")) {
    				baseUrl = v.toString().trim();
    			}
    			
    		});
    	}
        JSONObject encryptObj = new JSONObject();
        
        encryptObj.put("aad", getRandomBytes(GCM_AAD_LENGTH));
        encryptObj.put("applicationId", encryptionAppId);
       // encryptObj.put("data", org.apache.commons.codec.binary.Base64.encodeBase64String(data));
        encryptObj.put("data", org.apache.commons.codec.binary.Base64.encodeBase64URLSafeString(data));
        encryptObj.put("prependThumbprint", prependthumbprint);
        encryptObj.put("referenceId", referenceId);
        encryptObj.put("salt", getRandomBytes(GCM_NONCE_LENGTH));
        encryptObj.put("timeStamp",APIRequestUtil.getUTCDateTime(null));

        JSONObject wrapper = new JSONObject();
        wrapper.put("id", "mosip.registration.sync");
        wrapper.put("requesttime", APIRequestUtil.getUTCDateTime(LocalDateTime.now(ZoneOffset.UTC)));
        wrapper.put("version", "1.0");
        wrapper.put("request", encryptObj);

        JSONObject secretObject = apiUtil.post(baseUrl, baseUrl+encryptApi, wrapper);
        byte[] encBytes = org.apache.commons.codec.binary.Base64.decodeBase64(secretObject.getString("data"));
        return mergeEncryptedData(encBytes, org.apache.commons.codec.binary.Base64.decodeBase64(encryptObj.getString("salt")),
                org.apache.commons.codec.binary.Base64.decodeBase64(encryptObj.getString("aad")));
    }

    public boolean encryptPacket(byte[] data, String referenceId, String packetLocation, String contextKey) throws  Exception {
        byte[] encData = encrypt(data, referenceId, contextKey);
        try(FileOutputStream fos = new FileOutputStream(packetLocation)){
            fos.write(encData);
            fos.flush();
            return true;
        }
    }

    public byte[] encrypt(byte[] data, String referenceId, LocalDateTime timestamp, String contextKey) throws Exception {
        JSONObject encryptObj = new JSONObject();

        if(contextKey != null && !contextKey.equals("")) {
    		
    		Properties props = contextUtils.loadServerContext(contextKey);
    		props.forEach((k,v)->{
    			if(k.toString().equals("mosip.test.baseurl")) {
    				baseUrl = v.toString().trim();
    			}
    			
    		});
    	}
        encryptObj.put("aad", getRandomBytes(GCM_AAD_LENGTH));
        encryptObj.put("applicationId", encryptionAppId);
      //  encryptObj.put("data", org.apache.commons.codec.binary.Base64.encodeBase64String(data));
          encryptObj.put("data", org.apache.commons.codec.binary.Base64.encodeBase64URLSafeString(data));
        encryptObj.put("prependThumbprint", prependthumbprint);
        encryptObj.put("referenceId", referenceId);
        encryptObj.put("salt", getRandomBytes(GCM_NONCE_LENGTH));
        encryptObj.put("timeStamp", APIRequestUtil.getUTCDateTime(timestamp));

        JSONObject wrapper = new JSONObject();
        wrapper.put("id", "mosip.registration.sync");
        wrapper.put("requesttime", APIRequestUtil.getUTCDateTime(LocalDateTime.now(ZoneOffset.UTC)));
        wrapper.put("version", "1.0");
        wrapper.put("request", encryptObj);

        JSONObject secretObject = apiUtil.post(baseUrl, baseUrl+encryptApi, wrapper);
        byte[] encBytes = org.apache.commons.codec.binary.Base64.decodeBase64(secretObject.getString("data"));
        byte[] mergeddata = mergeEncryptedData(encBytes, org.apache.commons.codec.binary.Base64.decodeBase64(encryptObj.getString("salt")),
                org.apache.commons.codec.binary.Base64.decodeBase64(encryptObj.getString("aad")));

        //test(org.apache.commons.codec.binary.Base64.encodeBase64String(mergeddata), referenceId, encryptObj);
        return mergeddata;
    }
    

    public String decrypt(String data) throws Exception {
		PrivateKeyEntry privateKeyEntry = loadP12();
		byte[] dataBytes = org.apache.commons.codec.binary.Base64.decodeBase64(data);
		byte[] data1 = decryptData(dataBytes, privateKeyEntry);
		String strData = new String(data1);
		return strData;
	}
    private static int getSplitterIndex(byte[] encryptedData, int keyDemiliterIndex, String keySplitter) {
		final byte keySplitterFirstByte = keySplitter.getBytes()[0];
		final int keySplitterLength = keySplitter.length();
		for (byte data : encryptedData) {
			if (data == keySplitterFirstByte) {
				final String keySplit = new String(
						copyOfRange(encryptedData, keyDemiliterIndex, keyDemiliterIndex + keySplitterLength));
				if (keySplitter.equals(keySplit)) {
					break;
				}
			}
			keyDemiliterIndex++;
		}
		return keyDemiliterIndex;
	}

    private final static int THUMBPRINT_LENGTH = 32;
    private final static String RSA_ECB_OAEP_PADDING = "RSA/ECB/OAEPWITHSHA-256ANDMGF1PADDING";
    
    public static byte[] decryptData(byte[] requestData, PrivateKeyEntry privateKey) throws Exception {
		String keySplitter = "#KEY_SPLITTER#";
		SecretKey symmetricKey = null;
		byte[] encryptedData = null;
		byte[] encryptedSymmetricKey = null;
		final int cipherKeyandDataLength = requestData.length;
		final int keySplitterLength = keySplitter.length();

		int keyDemiliterIndex = getSplitterIndex(requestData, 0, keySplitter);
		byte[] encryptedKey = copyOfRange(requestData, 0, keyDemiliterIndex);
		try {
			encryptedData = copyOfRange(requestData, keyDemiliterIndex + keySplitterLength, cipherKeyandDataLength);
			// byte[] dataThumbprint = Arrays.copyOfRange(encryptedKey, 0,
			// THUMBPRINT_LENGTH);
			encryptedSymmetricKey = Arrays.copyOfRange(encryptedKey, THUMBPRINT_LENGTH, encryptedKey.length);
			// byte[] certThumbprint =
			// getCertificateThumbprint(privateKey.getCertificate());

			/*
			 * if (!Arrays.equals(dataThumbprint, certThumbprint)) { throw new
			 * Exception("Error in generating Certificate Thumbprint."); }
			 */

			byte[] decryptedSymmetricKey = asymmetricDecrypt(privateKey.getPrivateKey(),
					((RSAPrivateKey) privateKey.getPrivateKey()).getModulus(), encryptedSymmetricKey);
			symmetricKey = new SecretKeySpec(decryptedSymmetricKey, 0, decryptedSymmetricKey.length, "AES");
			return symmetricDecrypt(symmetricKey, encryptedData, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		throw new Exception("Not able to decrypt the data.");
	}
    /**
	 * 
	 * @param privateKey
	 * @param keyModulus
	 * @param data
	 * @return
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidAlgorithmParameterException
	 * @throws InvalidKeyException
	 */
	private static byte[] asymmetricDecrypt(PrivateKey privateKey, BigInteger keyModulus, byte[] data)
			throws Exception {

		Cipher cipher;
		try {
			cipher = Cipher.getInstance(RSA_ECB_OAEP_PADDING);
			OAEPParameterSpec oaepParams = new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256,
					PSpecified.DEFAULT);
			cipher.init(Cipher.DECRYPT_MODE, privateKey, oaepParams);
			return cipher.doFinal(data);
		} catch (java.security.NoSuchAlgorithmException e) {
			throw new NoSuchAlgorithmException(e);
		} catch (NoSuchPaddingException e) {
			throw new NoSuchPaddingException(e.getMessage());
		} catch (java.security.InvalidKeyException e) {
			throw new InvalidKeyException(e);
		} catch (InvalidAlgorithmParameterException e) {
			throw new InvalidAlgorithmParameterException(e);
		}
	}

	private static byte[] symmetricDecrypt(SecretKey key, byte[] data, byte[] aad) {
		byte[] output = null;
		try {
			Cipher cipher = Cipher.getInstance("AES/GCM/PKCS5Padding");
			byte[] randomIV = Arrays.copyOfRange(data, data.length - cipher.getBlockSize(), data.length);
			SecretKeySpec keySpec = new SecretKeySpec(key.getEncoded(), "AES");
			GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(128, randomIV);

			cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmParameterSpec);
			if (aad != null && aad.length != 0) {
				cipher.updateAAD(aad);
			}
			output = cipher.doFinal(Arrays.copyOf(data, data.length - cipher.getBlockSize()));
		} catch (Exception e) {

		}
		return output;
	}

    public PrivateKeyEntry loadP12() throws Exception {
    	KeyStore mosipKeyStore = KeyStore.getInstance("PKCS12");
    	InputStream in = getClass().getClassLoader().getResourceAsStream("partner.p12");
    	mosipKeyStore.load(in, "password@123".toCharArray());
    	ProtectionParameter password = new PasswordProtection("password@123".toCharArray());
    	PrivateKeyEntry privateKeyEntry = (PrivateKeyEntry) mosipKeyStore.getEntry("partner", password);
    	return privateKeyEntry;
    }
    /*private void test(String requestBody, String refId,JSONObject encryptObj) throws Exception {
        byte[] packet = org.apache.commons.codec.binary.Base64.decodeBase64(requestBody);
        byte[] nonce = Arrays.copyOfRange(packet, 0, GCM_NONCE_LENGTH);
        byte[] aad = Arrays.copyOfRange(packet, GCM_NONCE_LENGTH, GCM_NONCE_LENGTH + GCM_AAD_LENGTH);
        byte[] encryptedData = Arrays.copyOfRange(packet, GCM_NONCE_LENGTH + GCM_AAD_LENGTH, packet.length);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("applicationId", "REGISTRATION");
        jsonObject.put("referenceId", refId);
        jsonObject.put("aad", org.apache.commons.codec.binary.Base64.encodeBase64String(aad));
        jsonObject.put("salt", org.apache.commons.codec.binary.Base64.encodeBase64String(nonce));
        jsonObject.put("data", org.apache.commons.codec.binary.Base64.encodeBase64String(encryptedData));
        jsonObject.put("timeStamp", encryptObj.get("timeStamp"));

        JSONObject wrapper = new JSONObject();
        wrapper.put("id", "mosip.cryptomanager.decrypt");
        wrapper.put("version", "1.0");
        wrapper.put("requesttime",  apiUtil.getUTCDateTime(null));
        wrapper.put("request", jsonObject);

        JSONObject secretObject = apiUtil.post("https://qa2.mosip.net/v1/keymanager/decrypt", wrapper);
        logger.info("decrypt respose >>>>>>>>>>>>>>>> {}", secretObject);

        String plaindata = new String(Base64.getDecoder().decode(secretObject.getString("data")));
        logger.info("decrypt plaindata >>>>>>>>>>>>>>>> {}", plaindata);
    }*/

    public String getHash(byte[] data) throws Exception{
        try{
            MessageDigest messageDigest = MessageDigest.getInstance(HMAC_ALGORITHM_NAME);
            messageDigest.update(data);
            return Base64.getUrlEncoder().encodeToString(messageDigest.digest());
        } catch(Exception ex){
            logger.error("Cryptoutil getHash err ", ex);
            throw new Exception("Invalid crypto util");
        }
    }

    public String getHexEncodedHash(byte[] data) throws Exception{
        try{
            MessageDigest messageDigest = MessageDigest.getInstance(HMAC_ALGORITHM_NAME);
            messageDigest.update(data);
            return DatatypeConverter.printHexBinary(messageDigest.digest()).toUpperCase();
        } catch(Exception ex){
            logger.error("Cryptoutil getHexEncodedHash err ", ex);
            throw new Exception("Invalid crypto util");
        }
    }

    public byte[] sign(byte[] dataToSign,String contextKey) throws Exception {
        try {
            if(tpmAvailable) {
                CreatePrimaryResponse signingKey = createSigningKey();
                TPMU_SIGNATURE signedData = tpm.Sign(signingKey.handle,
                        TPMT_HA.fromHashOf(TPM_ALG_ID.SHA256, dataToSign).digest, new TPMS_NULL_SIG_SCHEME(),
                        TPMT_TK_HASHCHECK.nullTicket());
                logger.info("Completed Signing data using TPM");
                return ((TPMS_SIGNATURE_RSASSA) signedData).sig;
            }
            
            Signature sign = Signature.getInstance(SIGN_ALGORITHM);
            sign.initSign(getMachinePrivateKey(contextKey));

            try(ByteArrayInputStream in = new ByteArrayInputStream(dataToSign)) {
                byte[] buffer = new byte[2048];
                int len = 0;
                while((len = in.read(buffer)) != -1) {
                    sign.update(buffer, 0, len);
                }
                return sign.sign();
            }
        } catch (Exception ex) {
            logger.error("Failed to sign data", ex);
            throw new Exception("Failed to sign data");
        }
    }

    private PrivateKey getMachinePrivateKey(String contextKey) throws Exception {
    	String filePath=null;
		if (contextKey != null && !contextKey.equals("")) {

			Properties props = contextUtils.loadServerContext(contextKey);
			props.forEach((k, v) -> {
				if (k.toString().equals("mosip.test.regclient.machineid")) {
					machineid = v.toString().trim();
				}

			});
		}
		File folder = new File(String.valueOf(personaConfigPath) + File.separator +"privatekeys"+File.separator);
		File[] listOfFiles = folder.listFiles();
		for (File file : listOfFiles) {
		    if (file.isFile()) {
				if (file.getName().contains(contextKey +"."+ machineid)) {
					filePath = file.getAbsolutePath();
					break;
				}
		    }
		}
    	//byte[] key = Files.readAllBytes(Path.of(KEY_PATH ,KEYS_DIR ,PRIVATE_KEY));
		if(filePath==null ||filePath.isEmpty() )
			throw new Exception("privatekey file not found"); 
		System.out.println("PRIVATEKEY FILE PATH::"+filePath);
    	byte[] key = Files.readAllBytes(Path.of(filePath));
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(key);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(keySpec);
    }

    /**
     * 
     * @param length in bytes
     * @return base64 value of the random byte
     */
    private String getRandomBytes(int length){
        byte[] rand = new byte[length];
        sr.nextBytes(rand);
       // return org.apache.commons.codec.binary.Base64.encodeBase64String(rand);
        return org.apache.commons.codec.binary.Base64.encodeBase64URLSafeString(rand);
    }

    private byte[] mergeEncryptedData(byte[] encryptedData, byte[] nonce, byte[] aad) {
        byte[] finalEncData = new byte[encryptedData.length + GCM_AAD_LENGTH + GCM_NONCE_LENGTH];
        System.arraycopy(nonce, 0, finalEncData, 0, nonce.length);
        System.arraycopy(aad, 0, finalEncData, nonce.length, aad.length);
        System.arraycopy(encryptedData, 0, finalEncData, nonce.length + aad.length,	encryptedData.length);
        return finalEncData;
    }

    private CreatePrimaryResponse createSigningKey() {
        logger.info("Creating the Key from Platform TPM");

        if(signingPrimaryResponse != null)
            return signingPrimaryResponse;

        TPMT_PUBLIC template = new TPMT_PUBLIC(TPM_ALG_ID.SHA1,
                new TPMA_OBJECT(TPMA_OBJECT.fixedTPM, TPMA_OBJECT.fixedParent, TPMA_OBJECT.sign,
                        TPMA_OBJECT.sensitiveDataOrigin, TPMA_OBJECT.userWithAuth),
                new byte[0],
                new TPMS_RSA_PARMS(new TPMT_SYM_DEF_OBJECT(TPM_ALG_ID.NULL, 0, TPM_ALG_ID.NULL),
                        new TPMS_SIG_SCHEME_RSASSA(TPM_ALG_ID.SHA256), 2048, 65537),
                new TPM2B_PUBLIC_KEY_RSA());

        TPM_HANDLE primaryHandle = TPM_HANDLE.from(TPM_RH.ENDORSEMENT);

        TPMS_SENSITIVE_CREATE dataToBeSealedWithAuth = new TPMS_SENSITIVE_CREATE(NULL_VECTOR, NULL_VECTOR);

        logger.info("Completed creating the Signing Key from Platform TPM");

        //everytime this is called key never changes until unless either seed / template change.
        signingPrimaryResponse = tpm.CreatePrimary(primaryHandle, dataToBeSealedWithAuth, template,
                NULL_VECTOR, new TPMS_PCR_SELECTION[0]);
        return signingPrimaryResponse;
    }
    

}
