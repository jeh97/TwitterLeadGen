package main;

import javax.mail.Message;
import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.internet.InternetAddress;

import util.Stemmer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.ArrayList;

public class ResponseManager {
	private EmailManager manager;
	private DBManager db;
	private static ResponseManager instance;
	private Stemmer stemmer;
	
	/**
	 * Constructor for ResponseManager.
	 */
	private ResponseManager() {
		db = DBManager.getInstance();
		manager = EmailManager.getInstance();
		stemmer = Stemmer.getInstance();
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
		
		//open email folder
		manager.openFolders();
		
		//get emails
		Message[] messages = manager.getEmails();
		
		//process the unseen emails
		processMessages(messages);
		
		//close email folder
		manager.closeFolders();
		
		return true;
	}
	
	/**
	 * Method to determine whether a given message is a valid response, meaning
	 * it is in response to a daily digest email, and it is in the correct format.
	 * @param message Message to check
	 * @return True if it is a valid email to process, false otherwise
	 */
	public boolean isValidResponse(Message message) {
		try {
			//check if it is being received from the right email
			String address = ((InternetAddress)message.getFrom()[0]).getAddress();
			if (!address.equals(RatingsHandler.RECIPIENT)) {
				return false;
			}
			
			//check if it has the right subject
			String subject = message.getSubject();
			if(!isValidSubject(subject)) {
				return false;
			}
			
			int index = getEmailIndex(message.getSubject());
			
			//check if the email body is in the correct format
			if (!isValidFormat(message.getContent().toString(),index)) {
				return false;
			}
			//		check that it has an index that can be interpereted
			
			//		check that it has enough info in the response (that it has 
			//			the same number of relevant/irrelevant tweets marked as
			//			there are tweet ids
			
			// check that the email has not been responded to yet
			
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	/**
	 * Determines whether the given subject is a valid subject for a response email
	 * @param subject Subject to check
	 * @return True if it is valid, false otherwise
	 */
	public boolean isValidSubject(String subject) {
		//Create pattern regex
		String pattern = "Re: TwitterLeadGen: Daily digest #\\d\\d\\d *\\d "
				+ "(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec), \\d\\d\\d\\d$";
		
		//create pattern object
		Pattern r = Pattern.compile(pattern);
		
		//use matcher
		Matcher m = r.matcher(subject);
		
		// if matches, return true, else false
		return m.matches();
	}
	
	/**
	 * Method to determine whether a given message is in the valid format.
	 * @param message Message to check
	 * @return True if it in a valid format, false otherwise
	 */
	public boolean isValidFormat(String text, int index) {
		try {
			int numberOfTweets = db.getEmailNumberOfTweets(index);
			
			if (index == -1) return false;
			
			String body = text;
			
			//check that there is either an I or an R for each tweet it expects
			for (int i = 0; i < numberOfTweets; i++) {
				if(body.charAt(i*5+3) != 'I' && body.charAt(i*5+3) != 'R') {
					return false;
				}
			}
			
			//check that there are the right number of tweet IDs at the bottom of the message			
			if (getEmailTweetIDs(body).size() != numberOfTweets) {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * Method to perform the processing of a group of new messages.
	 * @param messages Messages to process
	 * @return True if all messages are processed successfully, false otherwise
	 */
	public boolean processMessages(Message[] messages) {
		boolean successful = true;
		try {
			//loop through emails
			for (int i = 0; i < messages.length; i++) {
				Message message = messages[i];
				if (message.isSet(Flags.Flag.SEEN) == true) {
					successful = successful && processMessage(message);
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		return successful;
	}
	
	/**
	 * Gets the email index from a message
	 * @param message Message
	 * @return Index of the email, or -1 if unsuccessful
	 */
	public int getEmailIndex(String subject) {
		try {
			int index = Integer.parseInt(subject.substring(34, 37));
			return index;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}
	
	/**
	 * Gets the tweet IDs of the Tweets in the email
	 * @param body Body of the email
	 * @return ArrayList of the Tweet IDs
	 */
	public ArrayList<Long> getEmailTweetIDs(String body) {
		ArrayList<Long> ids = new ArrayList<Long>();
		try {
			int pos = 0;
			
			while (body.indexOf("<<",pos) != -1) {
				int start = body.indexOf("<<",pos);
				int end = body.indexOf(">>",pos);
				System.out.printf("Start is %d and end is %d\n",start,end);
				if (end-start == 22) {
					ids.add(Long.parseLong(body.substring(start+2,end)));
				}
				pos = end+2;
			}
			return ids;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Returns an arraylist of booleans representing whether each email was relevant or not
	 * @param message
	 * @return
	 */
	public ArrayList<Boolean> getRelevanceInfo(String body, int index) {
		ArrayList<Boolean> info = new ArrayList<Boolean>();
		try {
			int numberOfTweets = db.getEmailNumberOfTweets(index);
			
			for (int i = 0; i < numberOfTweets; i++) {
				if(body.charAt(i*5+3) == 'R') {
					info.add(Boolean.TRUE);
				} else {
					info.add(Boolean.FALSE);
				}
			}
			return info;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Method to perform the processing of a single message.
	 * @param message Message to process
	 * @return True if the message is processed successfully, false otherwise
	 */
	public boolean processMessage(Message message) {
		try {
			String body = message.getContent().toString();
			
			int index = -1;
			//check if message is valid response
			if (!isValidResponse(message)) {
				return false;
			} else {
				index = getEmailIndex(message.getSubject());
				db.emailResponseRecorded(index);
			}
			
			//get tweet IDs, and get whether they were marked as relevant or not
			ArrayList<Long> ids = getEmailTweetIDs(body);
			ArrayList<Boolean> info = getRelevanceInfo(body, index);
			
			//for each one, add the words info to the DB
			for (int i = 0; i < ids.size(); i++) {
				String text = db.getTweetText(ids.get(i));
				recordStats(text,info.get(i));
			}
			
			//mark email as having had a response
			message.setFlag(Flags.Flag.SEEN, true);
			
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		return false;
	}
	
	/**
	 * Records the statistics for the words in 'text' into twitter_word_stats
	 * @param text
	 * @param relevant
	 * @return true if successful, false otherwise
	 */
	public boolean recordStats(String text, boolean relevant) {
		try {
			String[] splitStrings = text.split("\\s+");
			int len = splitStrings.length;
			
			for (int i = 0; i < len; i++) {
				String stem = stemString(splitStrings[i]);
				if (!db.hasWord(stem)) {
					db.addWord(stem);
				}
				if (relevant) {
					db.addRelevantInstance(stem);
				} else {
					db.addIrrelevantInstance(stem);
				}
			}
			
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	

	/**
	 * Method to simplify a string, and convert it to its stem.
	 * @param str String to stem
	 * @return The stem of the string if found, otherwise the string with all non-letters removed
	 */
	public String stemString(String str) {
		String stem = str;
		// check if it is a link, hashtag, or at
		if (str.indexOf("http") != -1) {
			return WordObject.LINK;
		} else if (str.indexOf("@") != -1) {
			return WordObject.AT;
		} else if (str.indexOf("#") != -1) {
			return WordObject.HASHTAG;
		}
		//reduce it to only letters
		String newStr = str.replaceAll("[^A-Za-z]+","");
		stem = stemmer.stem(newStr);
		
		return stem;
	}
	
}
