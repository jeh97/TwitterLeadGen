package main;

import javax.mail.Message;

public class ResponseManager {
	private EmailManager manager;
	private static ResponseManager instance;
	
	/**
	 * Constructor for ResponseManager.
	 */
	private ResponseManager() {
		manager = EmailManager.getInstance();
	}
	
	/**
	 * Method to get an instance of ResponseManager.
	 * @return Instance of ResponseManager
	 */
	public static ResponseManager getInstance() {
		if (instance == null) {
			instance = new ResponseManager();
		}
		return instance;
	}
	
	public static void main(String[] args) {
		ResponseManager response = ResponseManager.getInstance();
		response.checkNewResponses();
	}
	
	/**
	 * Method to look for and process any new emails that have been recieved.
	 * @return True if the inbox is successfully processed, false otherwise
	 */
	public boolean checkNewResponses() {
		manager.openFolders();
		Message[] messages = manager.getEmails();
		try {
			for (int i = 0; i < messages.length; i++) {
				Message message = messages[i];
				System.out.println("--------------------");
				System.out.println("Email Number "+(i+1));
				System.out.println("Subject: "+message.getSubject());
				System.out.println("From: "+message.getFrom()[0]);
				System.out.println("Text: "+message.getContent());
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * Method to determine whether a given message is a valid response, meaning
	 * it is in response to a daily digest email, and it is in the correct format.
	 * @param message Message to check
	 * @return True if it is a valid email to process, false otherwise
	 */
	private boolean isValidResponse(Message message) {
		return false;
	}
	
	/**
	 * Method to determine whether a given message is in the valid format.
	 * @param message Message to check
	 * @return True if it in a valid format, false otherwise
	 */
	private boolean isValidFormat(Message message) {
		return false;
	}
	
	/**
	 * Method to perform the processing of a group of new messages.
	 * @param messages Messages to process
	 * @return True if all messages are processed successfully, false otherwise
	 */
	private boolean processMessages(Message[] messages) {
		return false;
	}
	
	/**
	 * Method to perform the processing of a single message.
	 * @param message Message to process
	 * @return True if the message is processed successfully, false otherwise
	 */
	private boolean processMessage(Message message) {
		return false;
	}
	
}
