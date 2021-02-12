package org.mosip.dataprovider.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jakarta.mail.BodyPart;
import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.internet.MimeMultipart;
import org.jsoup.Jsoup;

public class ReadEmail {
	public static String FROM_MATH ="info@mosip.io";
	public static String gmailPOPHost = "pop.gmail.com";
	static String mailStoreType = "pop3";  
	static String username= "sanath.test.mosip@gmail.com";  
	static String password= "Sanath@Mosip@123";//change accordingly  
	static String regexpattern = "\\d+";
	
	public static List<String> getOtps(){
		List<String> otps = new ArrayList<String>();
		
		List<String> mails =  receiveEmail(gmailPOPHost, mailStoreType, username, password);  
		for(String s: mails) {
			System.out.println("S==" +s);
			 Pattern pattern = Pattern.compile(regexpattern);
			 Matcher matcher = pattern.matcher(s);
			 if (matcher.find())
			 {
				 System.out.println(matcher.group());
				 otps.add(matcher.group());
			 }
		 }
		return otps;
	}
	 public static List<String> receiveEmail(String pop3Host, String storeType,  
	  String user, String password) {  
		 List<String> mailMessage = new ArrayList<String>();
	  try {  
	   //1) get the session object  
	   Properties properties = new Properties();  
	   properties.put("mail.pop3.host", pop3Host);  
	   properties.put("mail.pop3.port", "995");
	   properties.put("mail.pop3.starttls.enable", "true");
	   properties.put("mail.pop3.ssl.enable", "true");
	      
	   Session emailSession = Session.getInstance(properties);
	   
    // emailSession.setDebug(true);
			
	   //2) create the POP3 store object and connect with the pop server  
	   Store emailStore = emailSession.getStore(storeType);  
	   emailStore.connect(user, password);  
	  
	   //3) create the folder object and open it  
	   Folder emailFolder = emailStore.getFolder("INBOX");  
	   emailFolder.open(Folder.READ_WRITE);  
	  
	   //4) retrieve the messages from the folder in an array and print it  
	   Message[] messages = emailFolder.getMessages();  
	   for (int i = 0; i < messages.length; i++) {  
	    Message message = messages[i];  
	    System.out.println("---------------------------------");  
	    System.out.println("Email Number " + (i + 1));  
	    System.out.println("Subject: " + message.getSubject());  
	    System.out.println("From: " + message.getFrom()[0]);
	    MimeMultipart content =  (MimeMultipart) message.getContent();
	    String bodyMsg =  getTextFromMimeMultipart(content);
	   
	   
	      if(message.getFrom()[0].toString().equals(FROM_MATH)) {
	    	  mailMessage.add(bodyMsg);
	    	  message.setFlag(Flags.Flag.DELETED, true);
	      }
	   }
	   //5) close the store and folder objects  
	   emailFolder.close(false);  
	   emailStore.close();  
	  
	  } catch (MessagingException|IOException e) {
		  e.printStackTrace();
	  }   

	  return mailMessage;
	 }  
	  
	 public static void main(String[] args) {  

		 List<String> otps = getOtps();
		 for(String ss: otps) {
			 System.out.println(ss);
		 }	  
	 }  
	 private static String getTextFromMimeMultipart( MimeMultipart mimeMultipart)  throws MessagingException, IOException{
		 String result = "";
		 int count = mimeMultipart.getCount();
		 for (int i = 0; i < count; i++) {
		        BodyPart bodyPart = mimeMultipart.getBodyPart(i);
		        if (bodyPart.isMimeType("text/plain")) {
		            result = result + "\n" + bodyPart.getContent();
		            break; // without break same text appears twice in my tests
		        } else if (bodyPart.isMimeType("text/html")) {
		            String html = (String) bodyPart.getContent();
		            result = result + "\n" + Jsoup.parse(html).text();
		        } else if (bodyPart.getContent() instanceof MimeMultipart){
		            result = result + getTextFromMimeMultipart((MimeMultipart)bodyPart.getContent());
		        }
		 }
		 return result;
	}

}
