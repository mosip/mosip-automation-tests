package io.mosip.testrig.dslrig.packetcreator.service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import io.mosip.testrig.dslrig.dataprovider.test.partnerManagement.CertificateGenerator;
import io.mosip.testrig.dslrig.dataprovider.test.partnerManagement.CertificateUploader;
import io.mosip.testrig.dslrig.dataprovider.variables.VariableManager;

@Component
public class CertificateService {

	private static final Logger logger = LoggerFactory.getLogger(CertificateService.class);

	public String uploadCACertificate(String certificateData, String contextKey) {

		String resp;
		try {
			resp = CertificateUploader.uploadCACertificate(certificateData, "Auth", contextKey);
			return resp;
		} catch (Exception e) {
			logger.error("uploadCACertificate", e);
			return "Failed at service";
		}

	}

	public String uploadStoredCACertificate(String certificateFile, String contextKey) {

		String certificateData;
		try {
			certificateData = readCertificate(certificateFile, contextKey);
		} catch (IOException e) {
			return "certificate file not found";
		}

		return CertificateUploader.uploadCACertificate(certificateData, "Auth", contextKey);

	}

	public String generateAndUploadRootCertificate(String issuer, String alias, int validYears, String contextKey) {

		KeyPairGenerator kpg;
		try {
			kpg = KeyPairGenerator.getInstance("RSA");
		} catch (NoSuchAlgorithmException ex) {
			return "No such algorithm exception";
		}

		kpg.initialize(2048);
		Security.addProvider(new BouncyCastleProvider());
		KeyPair keyPair = kpg.generateKeyPair();

		PublicKey publicKey = keyPair.getPublic();
		PrivateKey privateKey = keyPair.getPrivate();
		String certificateString;
		Certificate certificate;
		boolean generatedCertificate = false;

		String file_path = VariableManager.getVariableValue(contextKey, "certificatePath").toString() + "CA/" + alias
				+ ".p12";

		try (FileOutputStream fOut = new FileOutputStream(file_path);) {
			certificate = CertificateGenerator.createRootCertificate(publicKey, privateKey, issuer, alias, validYears);
			certificateString = CertificateGenerator.exportPublicKeyPem(certificate);
			generatedCertificate = true;

			KeyStore keyStore = KeyStore.getInstance("PKCS12");
			keyStore.load(null, null);
			keyStore.setKeyEntry(alias, privateKey, alias.toCharArray(), new Certificate[] { certificate });
			keyStore.store(fOut, alias.toCharArray());

		} catch (Exception ex) {
			if (!generatedCertificate) {
				return "certificate generation failed\n" + ex.toString();
			} else {
				return "local keyStore failure " + ex.getMessage();
			}

		}

		return uploadCACertificate(certificateString, contextKey);

	}

	public String generateAndUploadIntCertificate(String issuer, String alias, int validYears, String rootAlias,
			String contextKey) {

		PrivateKey rootPrivateKey;
		X509Certificate rootCertificate;
		String file_path = VariableManager.getVariableValue(contextKey, "certificatePath").toString() + "CA/"
				+ rootAlias + ".p12";

		// load root cert
		try (FileInputStream fIn = new FileInputStream(file_path);) {
			KeyStore keyStore = KeyStore.getInstance("PKCS12");

			keyStore.load(fIn, rootAlias.toCharArray());
			rootPrivateKey = (PrivateKey) keyStore.getKey(rootAlias, rootAlias.toCharArray());
			rootCertificate = (X509Certificate) keyStore.getCertificate(rootAlias);

		} catch (FileNotFoundException ex) {
			return "Root certificate does not exist locally.";
		} catch (Exception ex) {
			return "rootKeyStore failure";
		}

		// generate new keyPair
		KeyPairGenerator kpg;
		try {
			kpg = KeyPairGenerator.getInstance("RSA");
		} catch (NoSuchAlgorithmException ex) {
			return "No such algorithm exception";
		}

		kpg.initialize(2048);
		Security.addProvider(new BouncyCastleProvider());
		KeyPair keyPair = kpg.generateKeyPair();

		PublicKey publicKey = keyPair.getPublic();
		PrivateKey privateKey = keyPair.getPrivate();
		String certificateString;
		Certificate certificate;

		// create new certificate
		try {
			certificate = CertificateGenerator.createIntCertificate(publicKey, rootPrivateKey, rootCertificate, issuer,
					alias, validYears);
			certificateString = CertificateGenerator.exportPublicKeyPem(certificate);
		} catch (Exception ex) {
			return "certificate generation failed\n" + ex.toString();
		}
		file_path = VariableManager.getVariableValue(contextKey, "certificatePath").toString() + "CA/" + alias + ".p12";

		// store new certificate
		try (FileOutputStream fOut = new FileOutputStream(file_path);) {
			KeyStore keyStore = KeyStore.getInstance("PKCS12");
			keyStore.load(null, null);

			keyStore.setKeyEntry(alias, privateKey, alias.toCharArray(), new Certificate[] { certificate });

			keyStore.store(fOut, alias.toCharArray());

		} catch (Exception ex) {
			return "local keyStore failure";
		}

		return uploadCACertificate(certificateString, contextKey);

	}

	public String generateAndUploadPartnerCertificate(String issuer, String alias, int validYears, String rootAlias,
			String PartnerID, String contextKey) {

		PrivateKey rootPrivateKey;
		X509Certificate rootCertificate;
		String file_path = VariableManager.getVariableValue(contextKey, "certificatePath").toString() + "CA/"
				+ rootAlias + ".p12";
		// load signing cert
		try (FileInputStream fIn = new FileInputStream(file_path);) {
			KeyStore keyStore = KeyStore.getInstance("PKCS12");

			keyStore.load(fIn, rootAlias.toCharArray());
			rootPrivateKey = (PrivateKey) keyStore.getKey(rootAlias, rootAlias.toCharArray());
			rootCertificate = (X509Certificate) keyStore.getCertificate(rootAlias);

		} catch (FileNotFoundException ex) {
			return "Root certificate does not exist locally.";
		} catch (Exception ex) {
			return "rootKeyStore failure";
		}

		// generate new keyPair
		KeyPairGenerator kpg;
		try {
			kpg = KeyPairGenerator.getInstance("RSA");
		} catch (NoSuchAlgorithmException ex) {
			return "No such algorithm exception";
		}

		kpg.initialize(2048);
		Security.addProvider(new BouncyCastleProvider());
		KeyPair keyPair = kpg.generateKeyPair();

		PublicKey publicKey = keyPair.getPublic();
		PrivateKey privateKey = keyPair.getPrivate();
		String certificateString;
		Certificate certificate;

		// create new certificate
		try {
			certificate = CertificateGenerator.createPartnerCertificate(publicKey, rootPrivateKey, rootCertificate,
					issuer, alias, validYears);
			certificateString = CertificateGenerator.exportPublicKeyPem(certificate);
		} catch (Exception ex) {
			return "certificate generation failed\n" + ex.toString();
		}
		file_path = VariableManager.getVariableValue(contextKey, "certificatePath").toString() + "partner/" + alias
				+ ".p12";
		// store new certificate
		try (FileOutputStream fOut = new FileOutputStream(file_path);) {
			KeyStore keyStore = KeyStore.getInstance("PKCS12");
			keyStore.load(null, null);
			keyStore.setKeyEntry(alias, privateKey, alias.toCharArray(), new Certificate[] { certificate });

			keyStore.store(fOut, alias.toCharArray());

		} catch (Exception ex) {
			return "local keyStore failure " + ex.getMessage();
		}

		return uploadPartnerCertificate(certificateString, alias, PartnerID, contextKey);

	}

	public String uploadPartnerCertificate(String certificateData, String orgName, String partnerID,
			String contextKey) {

		String resp;
		try {
			resp = CertificateUploader.uploadPartnerString(certificateData, orgName, partnerID, "Auth", contextKey);
			return resp;
		} catch (Exception e) {
			logger.error("UploadPartnerCertificate", e);
			return "Failed at service";
		}
	}

	public String readCertificate(MultipartFile file) throws IOException {

		String fileExtension = "";
		String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
		fileExtension = originalFileName.substring(originalFileName.lastIndexOf('.'));

		if (!fileExtension.equals(".cer")) {
			throw new IOException("wrong filetype");
		}

		InputStream fileStream = file.getInputStream();
		return IOUtils.toString(fileStream, StandardCharsets.UTF_8);
	}

	public String readCertificate(String name, String contextKey) throws IOException {

		String path = VariableManager.getVariableValue(contextKey, "certificatePath") + name;
		return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);

	}
}
