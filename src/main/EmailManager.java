package main;

import java.util.Properties;
import java.util.ArrayList;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.NoSuchProviderException;
import javax.mail.Store;

public class EmailManager {
	private static final String USER = "TR.TwitterLG@gmail.com";
	private static final String PASS = "dreamBig";
	private static final String HOST = "pop.gmail.com";
	
	private Properties sendProps;
	private Properties receiveProps;
	private Session sendSession;
	private Session receiveSession;
	private Folder inboxFolder;
	private Store store;
	private Folder archiveFolder;
	
	private static EmailManager instance;
	
	/**
	 * Constructor for EmailManager
	 */
	private EmailManager() {
		inboxFolder = null;
		store = null;
		
		sendProps = new Properties();
		sendProps.put("mail.smtp.auth", "true");
		sendProps.put("mail.smtp.starttls.enable", "true");
		sendProps.put("mail.smtp.host", "smtp.gmail.com");
		sendProps.put("mail.smtp.port", "587");
		

		sendSession = Session.getInstance(sendProps,
				new javax.mail.Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(USER, PASS);
					}
				});
		
		receiveProps = new Properties();
		receiveProps.put("mail.pop3.host", HOST);
		receiveProps.put("mail.pop3.port", "995");
		receiveProps.put("mail.pop3.starttls.enable", "true");
		receiveSession = Session.getInstance(receiveProps);
	}
	
	/**
	 * Method to get an instance of EmailManager.
	 * @return Instance of EmailManager
	 */
	public static EmailManager getInstance() {
		if (instance == null) {
			instance = new EmailManager();
		}
		return instance;
	}
	
	public boolean sendEmail(String to, String subject, String body) {
		ArrayList<String> tos = new ArrayList<String>();
		tos.add(to);
		return sendEmail(tos,subject,body);
	}
	
	/**
	 * Method to send an email with the given recipient, subject, and body.
	 * @param to Recipient, should be a valid email address
	 * @param subject Subject line of the email
	 * @param body What will be contained in the body of the email
	 * @return True if the email successfully sent, false otherwise
	 */
	public boolean sendEmail(ArrayList<String> to, String subject, String body) {
		
		try {
			// make new message
			Message message = new MimeMessage(sendSession);
			
			// set from
			message.setFrom(new InternetAddress(USER));

			
			// set to
			int size = to.size();
			if (size < 1) return false;
			for (int i = 0; i < size; i++) {
				message.setRecipients(Message.RecipientType.TO,
						InternetAddress.parse(to.get(i)));
			}
			
			// set subject
			message.setSubject(subject);
			
			//set body
			message.setText(body);
			
			// send the message
			Transport.send(message);
		} catch (MessagingException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public boolean openFolders() {
		try {
			//create the POP3 store object and connect with the pop server
			store = receiveSession.getStore("pop3s");
			
			store.connect(HOST,USER,PASS);
			
			//create inbox and archive folder objects and open them
			inboxFolder = store.getFolder("INBOX");
			inboxFolder.open(Folder.READ_WRITE);
			
			
		} catch (NoSuchProviderException e) {
			e.printStackTrace();
			return false;
		} catch (MessagingException e) {
			e.printStackTrace();
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	/**
	 * Method to open the email folder, returning the email inbox.
	 * @return Inbox of the email account
	 */
	public Message[] getEmails() {
		Message[] messages = null;
		if (inboxFolder == null) return null;
		try {
			//retrieve the message from the folder in an array and print it
			messages = inboxFolder.getMessages();
			
		} catch (NoSuchProviderException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return messages;
	}
	
	/**
	 * Flags the given message for deletion.
	 * @param message Message to delete.
	 * @return True if the email is successfully deleted, false otherwise.
	 */
	public boolean flagToDelete(Message message) {
		boolean success = false;
		try {
			message.setFlag(Flags.Flag.DELETED,true);
			success = true;
		} catch (NoSuchProviderException e) {
			e.printStackTrace();
			success = false;
		} catch (MessagingException e) {
			e.printStackTrace();
			success = false;
		} catch (Exception e) {
			e.printStackTrace();
			success = false;
		}
		return success;
	}
	
	/**
	 * Moves the given message to the Archive folder.
	 * @param message Message to archive
	 * @return True if the email is successfully archived, false otherwise.
	 */
	public boolean moveToArchive(Message message) {
		try {
			Message[] messages = {message};
			inboxFolder.copyMessages(messages, archiveFolder);
			message.setFlag(Flags.Flag.DELETED, true);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * Marks the given message's SEEN flag. Inbox Folder must be open
	 * @param message message to set
	 * @param seen whether it is seen or not
	 * @return true if successful, false otherwise
	 */
	public boolean markSeen(Message message,boolean seen) {
		if (inboxFolder == null) {
			return false;
		}
		try {
			message.setFlag(Flags.Flag.SEEN, seen);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * Closes the open folder
	 * @return false if folder not open or not able to close, true if closed.
	 */
	public boolean closeFolders() {
		try {
			if(inboxFolder != null) {
				inboxFolder.close(true);
				inboxFolder = null;
				store.close();
				store = null;
			} else {
				return false;
			}	
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public static void main(String [] args) throws MessagingException {
		System.out.println("getInstance");
		EmailManager manager = EmailManager.getInstance();
		System.out.println("openFolders");
		manager.openFolders();
		System.out.println("getEmails");
		Message[] messages = manager.getEmails();
		System.out.println("end");
		for (int i = 0, n = messages.length; i < n; i++) {
			Message message = messages[i];
			System.out.println("---------------------------------");
			System.out.println("Email Number "+(i+1));
			System.out.println("Subject: "+message.getSubject());
			System.out.println("From: "+message.getFrom()[0]);
			System.out.println("Text: " + message.getContentType().toString());
		}
		System.out.println("closeFolders");
		manager.closeFolders();
	}
	
}
