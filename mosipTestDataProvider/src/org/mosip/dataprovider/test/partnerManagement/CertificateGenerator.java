package org.mosip.dataprovider.test.partnerManagement;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
  
import org.bouncycastle.asn1.DERBMPString;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.jce.PrincipalUtil;
import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.jce.interfaces.PKCS12BagAttributeCarrier;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;
import org.bouncycastle.x509.X509V1CertificateGenerator;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.bouncycastle.x509.extension.AuthorityKeyIdentifierStructure;
// import org.bouncycastle.x509.extension.SubjectKeyIdentifierStructure;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.bouncycastle.cert.X509AttributeCertificateHolder;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v1CertificateBuilder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cert.jcajce.JcaX509v1CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.mosip.dataprovider.util.CommonUtil;

import groovy.console.ui.SystemOutputInterceptor;



public class CertificateGenerator {

	static X509V3CertificateGenerator  v3CertGen = new X509V3CertificateGenerator();
	static X509V1CertificateGenerator  v1CertGen = new X509V1CertificateGenerator();
	static String KEY_ALGORITHM = "RSA";
	static String  ENCRYPTION_TYPE ="SHA256WITHRSA"; 	//"SHA1WithRSAEncryption";
	
	KeyStore store;
	String passwd;
	String storeFileName;
	// KeyPairGenerator kpg;
	
	public CertificateGenerator(String storeName, String passwd) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
		storeFileName = storeName;
		this.passwd = passwd;
		store = KeyStore.getInstance("PKCS12");
		store.load(null, passwd.toCharArray());
		
		// kpg = KeyPairGenerator.getInstance("RSA");

		// kpg.initialize(2048);


	}
	
	
	public static Certificate createMasterCert(PublicKey pubKey, PrivateKey privKey, String issuer, String alias, int validYears) throws Exception{
      String  subject = issuer; 
  
      v1CertGen.setSerialNumber(BigInteger.valueOf(1));
          v1CertGen.setIssuerDN(new X509Principal(issuer));
         v1CertGen.setNotBefore(new Date(System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 1));
          v1CertGen.setNotAfter(new Date(System.currentTimeMillis() + (1000L * 60 * 60 * 24 * 365 * validYears)));
          v1CertGen.setSubjectDN(new X509Principal(subject));
          v1CertGen.setPublicKey(pubKey);
          v1CertGen.setSignatureAlgorithm(ENCRYPTION_TYPE);
  
      X509Certificate cert = v1CertGen.generateX509Certificate(privKey);
    
      cert.checkValidity(new Date());
  
          cert.verify(pubKey);
  
          PKCS12BagAttributeCarrier   bagAttr = (PKCS12BagAttributeCarrier)cert;
  
              //
              // this is actually optional - but if you want to have control
              // over setting the friendly name this is the way to do it...
              //
             bagAttr.setBagAttribute(
                  PKCSObjectIdentifiers.pkcs_9_at_friendlyName,
                  new DERBMPString(alias));
      
         return cert;
    }

	public static X509Certificate createRootCertificate(PublicKey publicKey, PrivateKey privateKey, String issuerString, String alias, int validYears) throws Exception{

		ContentSigner signer = new JcaContentSignerBuilder(ENCRYPTION_TYPE).setProvider("BC").build(privateKey);
		X500Name issuer = new X500Name(issuerString);
		X500Name subject = issuer;
		
		
		X509v3CertificateBuilder certificateBuilder = new JcaX509v3CertificateBuilder(
			issuer,
			BigInteger.valueOf(1),
			new Date(System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 1),
			new Date(System.currentTimeMillis() + (1000L * 60 * 60 * 24 * 365 * validYears)), 
			subject, 
			publicKey
			);
		
		// X509v1CertificateBuilder certificateBuilder = new JcaX509v1CertificateBuilder(issuer, BigInteger.valueOf(1),
		// new Date(System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 1),
		// new Date(System.currentTimeMillis() + (1000L * 60 * 60 * 24 * 365 * validYears)),
		//  subject,
		// publicKey);

		JcaX509ExtensionUtils extensionUtils = new JcaX509ExtensionUtils();
		certificateBuilder.addExtension(Extension.basicConstraints, true, new BasicConstraints(true));
		certificateBuilder.addExtension(Extension.subjectKeyIdentifier, false, extensionUtils.createSubjectKeyIdentifier(publicKey));

		X509CertificateHolder certificateHolder = certificateBuilder.build(signer);
		X509Certificate certificate = new JcaX509CertificateConverter().setProvider("BC").getCertificate(certificateHolder);

		return certificate;

		

	}

	public static X509Certificate createIntCertificate(PublicKey publicKey, PrivateKey CAprivateKey, X509Certificate rootCertificate, String subjectString, String alias, int validYears)
	throws Exception{

		X500Name issuer = new X500Name(rootCertificate.getIssuerX500Principal().getName());
		X500Name subject = new X500Name(subjectString);
		BigInteger serial = BigInteger.valueOf(3);
		PKCS10CertificationRequestBuilder p10Builder = new JcaPKCS10CertificationRequestBuilder(subject, publicKey);

		JcaContentSignerBuilder contentSignerBuilder = new JcaContentSignerBuilder(ENCRYPTION_TYPE).setProvider("BC");
		
		
		ContentSigner contentSigner = contentSignerBuilder.build(CAprivateKey);
		PKCS10CertificationRequest p10 = p10Builder.build(contentSigner);

		X509v3CertificateBuilder certificateBuilder = new X509v3CertificateBuilder(
			issuer,
			serial, 
			new Date(System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 1), 
			new Date(System.currentTimeMillis() + (1000L * 60 * 60 * 24 * validYears)), 
			p10.getSubject(), 
			p10.getSubjectPublicKeyInfo()
		);

		JcaX509ExtensionUtils extensionUtils = new JcaX509ExtensionUtils();

		certificateBuilder.addExtension(Extension.authorityKeyIdentifier, false, extensionUtils.createAuthorityKeyIdentifier(rootCertificate));
		certificateBuilder.addExtension(Extension.subjectKeyIdentifier, false, extensionUtils.createSubjectKeyIdentifier(p10.getSubjectPublicKeyInfo()));
		certificateBuilder.addExtension(Extension.basicConstraints, true, new BasicConstraints(true));

		// certificateBuilder.addExtension(Extension.subjectAlternativeName, false, new DERBMPString(alias));
		
		X509CertificateHolder certificateHolder = certificateBuilder.build(contentSigner);
		X509Certificate certificate = new JcaX509CertificateConverter().setProvider("BC").getCertificate(certificateHolder);

		certificate.verify(rootCertificate.getPublicKey(), "BC");

		// PKCS12BagAttributeCarrier   bagAttr = (PKCS12BagAttributeCarrier)certificate;
 
        //  //
        //  // this is actually optional - but if you want to have control
        // // over setting the friendly name this is the way to do it...
        //  //
        //  bagAttr.setBagAttribute(
        //      PKCSObjectIdentifiers.pkcs_9_at_friendlyName,
        //      new DERBMPString(alias));

		return certificate;

	}	
	public static X509Certificate createPartnerCertificate(PublicKey publicKey, PrivateKey CAprivateKey, X509Certificate rootCertificate, String subjectString, String alias, int validYears)
	throws Exception{

		X500Name issuer = new X500Name(rootCertificate.getIssuerX500Principal().getName());
		X500Name subject = new X500Name(subjectString);
		BigInteger serial = BigInteger.valueOf(3);
		PKCS10CertificationRequestBuilder p10Builder = new JcaPKCS10CertificationRequestBuilder(subject, publicKey);

		JcaContentSignerBuilder contentSignerBuilder = new JcaContentSignerBuilder(ENCRYPTION_TYPE).setProvider("BC");
		
		
		ContentSigner contentSigner = contentSignerBuilder.build(CAprivateKey);
		PKCS10CertificationRequest p10 = p10Builder.build(contentSigner);

		X509v3CertificateBuilder certificateBuilder = new X509v3CertificateBuilder(
			issuer,
			serial, 
			new Date(System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 1), 
			new Date(System.currentTimeMillis() + (1000L * 60 * 60 * 24 * 365 * validYears)), 
			p10.getSubject(), 
			p10.getSubjectPublicKeyInfo()
		);

		JcaX509ExtensionUtils extensionUtils = new JcaX509ExtensionUtils();

		certificateBuilder.addExtension(Extension.basicConstraints, true, new BasicConstraints(false));
		certificateBuilder.addExtension(Extension.authorityKeyIdentifier, false, extensionUtils.createAuthorityKeyIdentifier(rootCertificate));
		certificateBuilder.addExtension(Extension.subjectKeyIdentifier, false, extensionUtils.createSubjectKeyIdentifier(p10.getSubjectPublicKeyInfo()));
		// certificateBuilder.addExtension(Extension.subjectAlternativeName, false, new DERBMPString(alias));
		
		X509CertificateHolder certificateHolder = certificateBuilder.build(contentSigner);
		X509Certificate certificate = new JcaX509CertificateConverter().setProvider("BC").getCertificate(certificateHolder);

		certificate.verify(rootCertificate.getPublicKey(), "BC");

		return certificate;

	}
	
	
	public static Certificate createIntermediateCert(
			PublicKey       pubKey,
			PrivateKey      caPrivKey,
			X509Certificate caCert, String issuer, String alias, int validYears)
			throws Exception {

        Hashtable                   attrs = new Hashtable();
         Vector                      order = new Vector();
 
         attrs  =StringtoAttr(issuer);


         order.addElement(X509Principal.C);
         order.addElement(X509Principal.O);
         order.addElement(X509Principal.OU);
         order.addElement(X509Principal.EmailAddress);
     
         v3CertGen.reset();
 
         v3CertGen.setSerialNumber(BigInteger.valueOf(3));
         v3CertGen.setIssuerDN(PrincipalUtil.getSubjectX509Principal(caCert));
         v3CertGen.setNotBefore(new Date(System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 1));
         v3CertGen.setNotAfter(new Date(System.currentTimeMillis() + (1000L * 60 * 60 * 24 * validYears)));
         v3CertGen.setSubjectDN(new X509Principal(order, attrs));
         v3CertGen.setPublicKey(pubKey);
         v3CertGen.setSignatureAlgorithm(ENCRYPTION_TYPE);
 
         //
         // extensions
         //
		JcaX509ExtensionUtils r = new JcaX509ExtensionUtils();

         v3CertGen.addExtension(
             X509Extensions.SubjectKeyIdentifier,
             false,
             r.createSubjectKeyIdentifier(pubKey) );
 
         v3CertGen.addExtension(
             X509Extensions.AuthorityKeyIdentifier,
             false,
             new AuthorityKeyIdentifierStructure(caCert));
 
         v3CertGen.addExtension(
             X509Extensions.BasicConstraints,
             true,
             new BasicConstraints(0));
 
         X509Certificate cert = v3CertGen.generateX509Certificate(caPrivKey);
 
         cert.checkValidity(new Date());
 
         cert.verify(caCert.getPublicKey());
 
         PKCS12BagAttributeCarrier   bagAttr = (PKCS12BagAttributeCarrier)cert;
 
         //
         // this is actually optional - but if you want to have control
        // over setting the friendly name this is the way to do it...
         //
         bagAttr.setBagAttribute(
             PKCSObjectIdentifiers.pkcs_9_at_friendlyName,
             new DERBMPString(alias));

         return cert;
     }
		     
	 public static Certificate createCert(
        PublicKey       pubKey,
       PrivateKey      caPrivKey,
       PublicKey       caPubKey,
       String caIssuer, String alias,
       String issuer,
       int validYears) throws Exception{

		 Hashtable                   sAttrs = new Hashtable();
		 Vector                      sOrder = new Vector();

		 sAttrs  =StringtoAttr(caIssuer);

       
       sOrder.addElement(X509Principal.C);
        sOrder.addElement(X509Principal.O);
        sOrder.addElement(X509Principal.OU);
       sOrder.addElement(X509Principal.EmailAddress);

       Hashtable                   attrs = new Hashtable();
       Vector                      order = new Vector();
       attrs = StringtoAttr(issuer);
       order.addElement(X509Principal.C);
       order.addElement(X509Principal.O);
       order.addElement(X509Principal.L);
       order.addElement(X509Principal.CN);
       order.addElement(X509Principal.EmailAddress);
       v3CertGen.reset();
	  
	   v3CertGen.setSerialNumber(BigInteger.valueOf(3));
	   v3CertGen.setIssuerDN(new X509Principal(sOrder, sAttrs));
	   v3CertGen.setNotBefore(new Date(System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 1));
	   v3CertGen.setNotAfter(new Date(System.currentTimeMillis() + (1000L * 60 * 60 * 24 * validYears)));
	   v3CertGen.setSubjectDN(new X509Principal(order, attrs));
	   v3CertGen.setPublicKey(pubKey);
	   v3CertGen.setSignatureAlgorithm(ENCRYPTION_TYPE);

	   JcaX509ExtensionUtils r = new JcaX509ExtensionUtils();
       v3CertGen.addExtension(
	             X509Extensions.SubjectKeyIdentifier,
	             false,
	            r.createSubjectKeyIdentifier(pubKey));
	 
	   v3CertGen.addExtension(
	          X509Extensions.AuthorityKeyIdentifier,
	            false,
	             new AuthorityKeyIdentifierStructure(caPubKey));
	        X509Certificate cert = v3CertGen.generateX509Certificate(caPrivKey);
	 
	   cert.checkValidity(new Date());
	
	   cert.verify(caPubKey);
	 
	   PKCS12BagAttributeCarrier   bagAttr = (PKCS12BagAttributeCarrier)cert;
	   

      bagAttr.setBagAttribute(
	            PKCSObjectIdentifiers.pkcs_9_at_friendlyName,
	            new DERBMPString(alias));
	  bagAttr.setBagAttribute(
	               PKCSObjectIdentifiers.pkcs_9_at_localKeyId,
	               r.createSubjectKeyIdentifier(pubKey));
	   
	  	return cert;
	}
	
	public static Hashtable StringtoAttr(String str){
		 Hashtable attrs = new Hashtable();
		   
		 Properties props = CommonUtil.String2Props(str);
         props.forEach( (k,v) ->{
          	 if(k.equals("C"))
          		 attrs.put(X509Principal.C, v);
          	else
          	if(k.equals("O"))
          		 attrs.put(X509Principal.O, v);	 
          	else
              if(k.equals("OU"))
              	attrs.put(X509Principal.OU, v);	 
          	else
              if(k.equals("OU"))
              	attrs.put(X509Principal.OU, v);	 
            else
            if(k.equals("L"))
            	attrs.put(X509Principal.L, v);	 
            else
            if(k.equals("CN"))
                attrs.put(X509Principal.CN, v);	 
            else
            if(k.equals("E"))
            	attrs.put(X509Principal.EmailAddress, v);	 
                                   		 
           });
        return attrs;
	 }
	public void save() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
		 
		 FileOutputStream fOut = new FileOutputStream(storeFileName);
		 
		 store.store(fOut, passwd.toCharArray());
		 
	}
	// public KeyPair getNewKeyPair() {
	// 	KeyPair keyPair;
	// 	keyPair = kpg.generateKeyPair();
	// 	return keyPair;
	// }
	
	public static String exportPublicKeyPem(Certificate cert) throws IOException, CertificateEncodingException {
		
		StringWriter writer = new StringWriter();
		// PemWriter pemWriter = new PemWriter(writer);
		JcaPEMWriter pemWriter = new JcaPEMWriter(writer);

		
		
		pemWriter.writeObject(cert);
		pemWriter.flush();
		pemWriter.close();
		String strCert = writer.toString();
		
		// // System.out.println(strCert);
		
		strCert = strCert.replace("\r\n", "\n");
		

		
		
		return strCert;

	}

	public static String exportPublicKeyPemOld(Certificate cert) throws IOException, CertificateEncodingException {
		
		StringWriter writer = new StringWriter();
		PemWriter pemWriter = new PemWriter(writer);
		pemWriter.writeObject(new PemObject("CERTIFICATE", cert.getEncoded()));
		pemWriter.flush();
		pemWriter.close();
		String strCert = writer.toString();
		
		// System.out.println(strCert);
		
		strCert = strCert.replace("\r\n", "\n");
		// System.out.println(strCert);

		// StringWriter certificateOut = new StringWriter();
		// cert
        // certificateOut.write("-----BEGIN CERTIFICATE-----".getBytes());
        // certificateOut.write(Base64.encode(certificate.getEncoded()));
        // certificateOut.write("-----END CERTIFICATE-----".getBytes());
        // certificateOut.close();
		
		return strCert;

	}



	 public static void main(String[]    args) throws Exception {
		
		 KeyPairGenerator kpg;
		 kpg = KeyPairGenerator.getInstance("RSA");

		kpg.initialize(2048);

		 Security.addProvider(new BouncyCastleProvider());
		 Certificate[] chain = new Certificate[3];
		 KeyPair keyPair;
			keyPair = kpg.generateKeyPair();
		PublicKey caPubKey = keyPair.getPublic();
		PrivateKey caPrivKey = keyPair.getPrivate();
		keyPair = kpg.generateKeyPair();
		PublicKey intPubKey = keyPair.getPublic();
		PrivateKey intPrivKey = keyPair.getPrivate();
		
		keyPair = kpg.generateKeyPair();
		PublicKey pubKey = keyPair.getPublic();
		PrivateKey privKey = keyPair.getPrivate();
		long date = System.currentTimeMillis();
		chain[2] = createMasterCert(caPubKey, caPrivKey,
				"OU = Bouncy Primary Certificate",
				"Root CA",3
				);
		
		chain[1] = createIntermediateCert(intPubKey, caPrivKey, (X509Certificate)chain[2],
				"C=IN, O=EFG Company, OU=Certificate, E=abc@efg.com",
				"Intermediate CA",3);
		chain[0] = createCert(pubKey, intPrivKey, intPubKey,
				"C=IN, O=EFG Company, OU=Certificate, E=abc@efg.com",
				"Partner Org",
				"C=IN, O=ABC Bank,L=Bangalore,CN=ABC Bank, OU=Account Opening,E=bank@efg.com",
				
				3
			);
		
		// KeyStore store = KeyStore.getInstance("PKCS12");
		 
		//  store.load(null, null);
		//  store.setKeyEntry("Root CA", caPrivKey, null, new Certificate[] {chain[2] });
		//  store.setKeyEntry("Inter CA", intPrivKey, null, new Certificate[] {chain[1],chain[2] });
				 
		//  store.setKeyEntry("ABC Company", privKey, null, chain);
		 
		 
		//  FileOutputStream fOut = new FileOutputStream("id.p12");
		 
		//  store.store(fOut, "abc123".toCharArray());



         
         String s;
		//  s = exportPublicKeyPemOld(chain[0]);
        //  System.out.println(s);
		//  s = exportPublicKeyPemOld(chain[1]);
        //  System.out.println(s);
		//  s = exportPublicKeyPemOld(chain[2]);
        //  System.out.println(s);

		
		X509Certificate c = createRootCertificate(caPubKey, caPrivKey,
				"OU = Bouncy Primary Certificate,O = The Legion of the Bouncy Castle,C = AU",
				"Root CA",3
				);
		
		X509Certificate c1 = createIntCertificate(intPubKey, caPrivKey, (X509Certificate) chain[2], "C=IN, O=EFG Company, OU=Certificate, E=abc@efg.com", 
																		"Intermediate CA", 3);

		X509Certificate c2 = createPartnerCertificate(pubKey, intPrivKey, c1, "C=IN, O=ABC Bank,L=Bangalore,CN=ABC Bank, OU=Account Opening,E=bank@efg.com",
		"Partner Org", 3);

		
		// byte[] a = c.getEncoded();
		// System.out.println(a.length);

		 
		//  s = exportPublicKeyPem(c);
        //  System.out.println(s);
		// s = exportPublicKeyPemOld(chain[2]);
		// System.out.println(s);
		
		
		  s = exportPublicKeyPem(c1);
         System.out.println(s);
		 s = exportPublicKeyPem(chain[1]);
         System.out.println(s);

		
		

	 }
			           
	
}
