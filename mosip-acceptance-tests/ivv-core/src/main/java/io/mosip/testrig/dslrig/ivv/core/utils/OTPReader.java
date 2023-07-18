package io.mosip.testrig.dslrig.ivv.core.utils;

import java.io.IOException;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeMultipart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.mail.imap.protocol.FLAGS;




public class OTPReader {
	
	private static final Logger logger = LoggerFactory.getLogger(OTPReader.class);

    public static String readOTP(String recipient) {
        int emailFoundOrNot = 0;
        String otp = null;
        Message[] messages = null;
        Folder emailInbox;

        Properties sysProps = System.getProperties();
        sysProps.setProperty("mail.imap.ssl.enable", "true");
        sysProps.setProperty("mail.store.protocol", "imaps");
        try {
            while (emailFoundOrNot < 1) {
                Session session = Session.getInstance(sysProps, null);
                Store store = session.getStore();
                //store.connect(BaseHelper.email_hostname, BaseHelper.email_username, BaseHelper.email_password);
                store.connect("outlook.office365.com", 993, System.getProperty("ivv.email.server.user"), System.getProperty("ivv.email.server.pass"));
                Thread.sleep(1000);
                emailInbox = store.getFolder("Inbox");
                emailInbox.open(Folder.READ_WRITE);

                messages = emailInbox.getMessages();
                for (int i = messages.length - 1; i < messages.length; i++) {
                    Message message = messages[i];
                    try {
                        if (message.getSubject().equals("Otp message")) {
                            Address[] recipients = message.getRecipients(Message.RecipientType.TO);
                            for (Address address : recipients) {
                                if(recipient.equals(address.toString())){
                                    String msg = getTextFromMessage(message);
                                    otp = getOTP(msg);
                                }
                            }
                            message.setFlag(FLAGS.Flag.SEEN, true);
                            message.setFlag(FLAGS.Flag.DELETED, true);
                            emailFoundOrNot = 1;
                            break;
                        }
                    } catch (MessagingException me) {
                        logger.error("error:"+me.getMessage());
                    }
                }
                emailInbox.close(true);
                store.close();
            }
        } catch (Exception mex) {
            mex.printStackTrace();
            Thread.currentThread().interrupt();
        }
        return otp;
    }

    public static void deleteOTPEmails(String email, String pass) {
        Message[] messages = null;
        Folder emailInbox;

        Properties sysProps = System.getProperties();
        sysProps.setProperty("mail.imap.ssl.enable", "true");
        sysProps.setProperty("mail.store.protocol", "imaps");
        try
        {
            Session session = Session.getInstance(sysProps, null);
            Store store = session.getStore();
            store.connect("outlook.office365.com", 993, email, pass);
            Thread.sleep(1000);
            emailInbox = store.getFolder("Inbox");
            emailInbox.open(Folder.READ_WRITE);

            messages = emailInbox.getMessages();
            for (int i = messages.length - 1; i < messages.length; i++) {
                Message message = messages[i];
                try
                {
                    if (message.getSubject().equals("Otp message")) {
                        message.setFlag(FLAGS.Flag.SEEN, true);
                        message.setFlag(FLAGS.Flag.DELETED, true);
                        break;
                    }
                } catch (MessagingException me) {
                	 logger.error("error:"+me.getMessage());
                }
            }
            emailInbox.close(true);
            store.close();
        } catch (Exception mex) {
            mex.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    public static String readOtpFromOtherUser(String email, String pass) {
        int emailFoundOrNot = 0;
        String otp = null;
        Message[] messages = null;
        Folder emailInbox;

        Properties sysProps = System.getProperties();
        sysProps.setProperty("mail.imap.ssl.enable", "true");
        sysProps.setProperty("mail.store.protocol", "imaps");
        try {
            while (emailFoundOrNot < 1) {
                Session session = Session.getInstance(sysProps, null);
                Store store = session.getStore();
                store.connect("outlook.office365.com", 993, email, pass);
                Thread.sleep(1000);
                emailInbox = store.getFolder("Inbox");
                emailInbox.open(Folder.READ_WRITE);

                messages = emailInbox.getMessages();
                for (int i = messages.length - 1; i < messages.length; i++) {
                    Message message = messages[i];
                    try {
                        if (message.getSubject().equals("Otp message")) {
                            String msg = getTextFromMessage(message);
                            otp = getOTP(msg);
                            message.setFlag(FLAGS.Flag.SEEN, true);
                            message.setFlag(FLAGS.Flag.DELETED, true);
                            emailFoundOrNot = 1;
                            break;
                        }
                    } catch (MessagingException me) {
                    	 logger.error("error:"+me.getMessage());
                    }
                }
                emailInbox.close(true);
                store.close();
            }
        } catch (Exception mex) {
        	 logger.error("error:"+mex.getMessage());
            Thread.currentThread().interrupt();
        }
        return otp;
    }

    private static String getOTP(String result) {
        String[] split = result.split(" ");
        return split[split.length - 1];
    }

    private static String getTextFromMessage(Message message) throws MessagingException, IOException {
        String result = "";
        if (message.isMimeType("text/plain")) {
            result = message.getContent().toString();
        } else if (message.isMimeType("multipart/*")) {
            MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
            result = getTextFromMimeMultipart(mimeMultipart);
        }
        return result;
    }

    private static String getTextFromMimeMultipart(MimeMultipart mimeMultipart) throws MessagingException, IOException {
        String result = "";
        int count = mimeMultipart.getCount();
        for (int i = 0; i < count; i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) {
                result = result + "\n" + bodyPart.getContent();
                break;
            } else if (bodyPart.getContent() instanceof MimeMultipart) {
                result = result + getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent());
            }
        }
        return result;
    }
}
