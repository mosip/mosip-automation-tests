package io.mosip.test.packetcreator.mosippacketcreator.service;

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
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

// import io.mosip.kernel.core.exception.FileNotFoundException;
import variables.VariableManager;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.mosip.dataprovider.test.partnerManagement.CertificateUploader;
import org.mosip.dataprovider.test.partnerManagement.CertificateGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Component
public class CertificateService {
    
    private static final Logger logger = LoggerFactory.getLogger(CertificateService.class);

    public String uploadCACertificate(String certificateData){

        String resp;
        try{
            // String certificateData = readCertificate(certificateFile);
            resp = CertificateUploader.uploadCACertificate(certificateData, "Auth");
            return resp;
        }
        // catch(IOException e){
        //     return "certificate file not found";
        // }
        catch(Exception e){
            logger.error("uploadCACertificate",e);
            return "Failed at service";
        }
        
    }

    public String uploadStoredCACertificate(String certificateFile){

        String certificateData;
        try{
            certificateData = readCertificate(certificateFile);
        }
        catch(IOException e){
            return "certificate file not found";
        }

        return CertificateUploader.uploadCACertificate(certificateData, "Auth");

    }

    public String generateAndUploadRootCertificate(String issuer, String alias, int validYears){

        KeyPairGenerator kpg;
        try{
            kpg = KeyPairGenerator.getInstance("RSA");
        }
        catch(NoSuchAlgorithmException ex){
            return "No such algorithm exception";
        }

        kpg.initialize(2048);
        Security.addProvider(new BouncyCastleProvider());
        KeyPair keyPair = kpg.generateKeyPair();

        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();
        String certificateString;
        Certificate certificate;
        try{
            certificate = CertificateGenerator.createRootCertificate(publicKey, privateKey, issuer, alias, validYears);
            // certificate = CertificateGenerator.createMasterCert(publicKey, privateKey, issuer, alias, validYears);
            certificateString = CertificateGenerator.exportPublicKeyPem(certificate);
        }
        catch(Exception ex){
            return "certificate generation failed\n" + ex.toString();
        }

        try{
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(null, null);
            keyStore.setKeyEntry(alias, privateKey, alias.toCharArray(), new Certificate []{certificate});

            String file_path = VariableManager.getVariableValue("certificatePath").toString() + "CA/" + alias + ".p12";

            FileOutputStream fOut = new FileOutputStream(file_path);
		 
            keyStore.store(fOut, alias.toCharArray());

        }
        catch(Exception ex){
            return "local keyStore failure " + ex.getMessage();
        }


        return uploadCACertificate(certificateString);
        // return "done";
        
    }

    public String generateAndUploadIntCertificate(String issuer, String alias, int validYears, String rootAlias){

        PrivateKey rootPrivateKey;
        X509Certificate rootCertificate; 

        //load root cert
        try{
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            
            String file_path = VariableManager.getVariableValue("certificatePath").toString() + "CA/" + rootAlias + ".p12";
            FileInputStream fIn = new FileInputStream(file_path);

            keyStore.load(fIn, rootAlias.toCharArray());
            rootPrivateKey = (PrivateKey) keyStore.getKey(rootAlias, rootAlias.toCharArray());
            rootCertificate = (X509Certificate) keyStore.getCertificate(rootAlias);

        }
        catch(FileNotFoundException ex){
            return "Root certificate does not exist locally.";
        }
        catch(Exception ex){
            return "rootKeyStore failure";
        }

        //generate new keyPair
        KeyPairGenerator kpg;
        try{
            kpg = KeyPairGenerator.getInstance("RSA");
        }
        catch(NoSuchAlgorithmException ex){
            return "No such algorithm exception";
        }

        kpg.initialize(2048);
        Security.addProvider(new BouncyCastleProvider());
        KeyPair keyPair = kpg.generateKeyPair();

        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();
        String certificateString;
        Certificate certificate;

        //create new certificate
        try{
            certificate = CertificateGenerator.createIntCertificate(publicKey, rootPrivateKey, rootCertificate, issuer, alias, validYears);
            // certificate = CertificateGenerator.createIntermediateCert(publicKey, rootPrivateKey, rootCertificate, issuer, alias, validYears);
            certificateString = CertificateGenerator.exportPublicKeyPem(certificate);
        }
        catch(Exception ex){
            return "certificate generation failed\n" + ex.toString();
        }

        //store new certificate
        try{
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(null, null);
            
            keyStore.setKeyEntry(alias, privateKey, alias.toCharArray(), new Certificate []{certificate});

            String file_path = VariableManager.getVariableValue("certificatePath").toString() + "CA/" + alias + ".p12";

            FileOutputStream fOut = new FileOutputStream(file_path);
		 
            keyStore.store(fOut, alias.toCharArray());

        }
        catch(Exception ex){
            return "local keyStore failure";
        }
        

        return uploadCACertificate(certificateString);
        // return "done";

    }

    public String generateAndUploadPartnerCertificate(String issuer, String alias, int validYears, String rootAlias, String PartnerID){

        PrivateKey rootPrivateKey;
        X509Certificate rootCertificate; 

        //load signing cert
        try{
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            
            String file_path = VariableManager.getVariableValue("certificatePath").toString() + "CA/" + rootAlias + ".p12";
            FileInputStream fIn = new FileInputStream(file_path);

            keyStore.load(fIn, rootAlias.toCharArray());
            rootPrivateKey = (PrivateKey) keyStore.getKey(rootAlias, rootAlias.toCharArray());
            rootCertificate = (X509Certificate) keyStore.getCertificate(rootAlias);

        }
        catch(FileNotFoundException ex){
            return "Root certificate does not exist locally.";
        }
        catch(Exception ex){
            return "rootKeyStore failure";
        }

        //generate new keyPair
        KeyPairGenerator kpg;
        try{
            kpg = KeyPairGenerator.getInstance("RSA");
        }
        catch(NoSuchAlgorithmException ex){
            return "No such algorithm exception";
        }

        kpg.initialize(2048);
        Security.addProvider(new BouncyCastleProvider());
        KeyPair keyPair = kpg.generateKeyPair();

        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();
        String certificateString;
        Certificate certificate;

        //create new certificate
        try{
            // certificate = CertificateGenerator.createRootCertificate(publicKey, privateKey, issuer, alias, validYears);
            // certificate = CertificateGenerator.createIntCertificate(publicKey, rootPrivateKey, rootCertificate, issuer, alias, validYears);
            certificate = CertificateGenerator.createPartnerCertificate(publicKey, rootPrivateKey, rootCertificate, issuer, alias, validYears);
            certificateString = CertificateGenerator.exportPublicKeyPem(certificate);
        }
        catch(Exception ex){
            return "certificate generation failed\n" + ex.toString();
        }

        //store new certificate
        try{
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(null, null);
            keyStore.setKeyEntry(alias, privateKey, alias.toCharArray(), new Certificate []{certificate});

            String file_path = VariableManager.getVariableValue("certificatePath").toString() + "partner/" + alias + ".p12";

            FileOutputStream fOut = new FileOutputStream(file_path);
		 
            keyStore.store(fOut, alias.toCharArray());

        }
        catch(Exception ex){
            return "local keyStore failure " + ex.getMessage();
        }

        return uploadPartnerCertificate(certificateString, alias, PartnerID);
        // return "done";
        


    }

    public String uploadPartnerCertificate(String certificateData, String orgName, String partnerID){
        
        String resp;
        try{
            resp = CertificateUploader.uploadPartnerString(certificateData, orgName, partnerID, "Auth");
            return resp;
        }
        catch(Exception e){
            logger.error("UploadPartnerCertificate", e);
            return "Failed at service";
        }
    }


    // public String uploadPartnerCertificate(MultipartFile certificateFile, String orgName, String partnerID){

    //     String resp;
        
    //     try{
    //         String certificateData = readCertificate(certificateFile);
    //         // resp = CertificateUploader.uploadPartnerString(certificateData, orgName, partnerID, "Auth");
    //         return certificateData;
    //     }
    //     catch(IOException e){
    //         return "certificate file not found";
    //     }
    //     catch(Exception e){
    //         logger.error("UploadPartnerCertificate", e);
    //         return "Failed at service";
    //     }
    // }

    public String readCertificate(MultipartFile file) throws IOException{

        String fileExtension = "";
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        fileExtension = originalFileName.substring(originalFileName.lastIndexOf('.'));

        if(fileExtension != ".cer"){
            throw new IOException("wrong filetype");
        }

        InputStream fileStream = file.getInputStream();
        return IOUtils.toString(fileStream, StandardCharsets.UTF_8);
    }

    public String readCertificate(String name) throws IOException{

        String path = VariableManager.getVariableValue("certificatePath") + name;
        String content = new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
        return content;
        
    }
}
