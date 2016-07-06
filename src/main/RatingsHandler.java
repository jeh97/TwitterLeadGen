package main;

import java.util.Map;
import java.util.ArrayList;
import twitter4j.Status;
import twitter4j.User;

import java.util.Calendar;

public class RatingsHandler {
	
	public static final int DEFAULT_NUMBER_TO_SEND = 10;
	public static final String RECIPIENT = "Social@tapreason.com";
	private static int NUMBER_TO_SEND = DEFAULT_NUMBER_TO_SEND;
	private EmailManager manager;
	private DBManager db;
	private static RatingsHandler instance;
	
	/**
	 * Constructor for RatingsHandler
	 */
	private RatingsHandler() {
		manager = EmailManager.getInstance();
		db = DBManager.getInstance();
	}
	
	/**
	 * Method to get the singleton of RatingsHandler
	 * @return RatingsHandler instance
	 */
	public static RatingsHandler getInstance() {
		if (instance == null) { 
			instance = new RatingsHandler();
		}
		return instance;
	}
	
	/**
	 * Method to execute the postprocessing of Tweets according to their ratings.
	 * @param ratingsAndStatuses A Map of the tweets and their ratings
	 * @return True if the method executes successfully, false otherwise
	 */
	public boolean processWithRatings(ArrayList<StatusObject> ratingsAndStatuses) {
		// get top statuses
		ArrayList<StatusObject> top = getTopStatuses(ratingsAndStatuses);
		
		// send those statuses
		int emailIndex = sendTopStatuses(top);
		
		return false;
	}
	
	/**
	 * Method to reduce the given list of Tweets to only the top ones. The number included
	 * in the returned Map is determined by NUMBER_TO_SEND.
	 * @param ratingsAndStatuses Given list of Tweets and ratings
	 * @return Shortened list of Tweets and ratings
	 */
	private ArrayList<StatusObject> getTopStatuses(ArrayList<StatusObject> ratingsAndStatuses) {
		ArrayList<StatusObject> top = new ArrayList<StatusObject>();
		
		int count = 0;
		//go through statuses until NUMBER_TO_SEND have been collected
		while (top.size()<NUMBER_TO_SEND && count < ratingsAndStatuses.size()) {
			StatusObject current = ratingsAndStatuses.get(count);
			if (!db.hasTweet(current.getStatus()) && !db.hasTweetWithText(current.getStatus().getText())) {
				top.add(current);
				db.addTweet(current.getStatus());
			}
			count++;
		}
		
		return top;
	}
	
	/**
	 * Method to send the highest rated Tweets in the daily digest email
	 * @param topRatingsAndStatuses Tweets to send
	 * @return The Email index of the sent email. -1 if no email is sent.
	 */
	public int sendTopStatuses(ArrayList<StatusObject> topStatusObjects) {
		if (topStatusObjects.size() == 0 || topStatusObjects == null) {
			return -1;
		}
		
		//int id = db.getNewEmailID();
		
		int index = db.addSentEmail(Calendar.getInstance().getTime(),topStatusObjects.size());
		
		String[] message = getMessageText(topStatusObjects,index);
		
		manager.sendEmail(RECIPIENT, message[0], message[1]);
		
		return index;
	}
	
	/**
	 * Creates the body of the email given the statuses to send, the email index, and email id
	 * @param statusesToSend Statuses to include in the email
	 * @param index Email index
	 * @param id Email ID
	 * @return String of the message
	 */
	private String[] getMessageText(ArrayList<StatusObject> statusesToSend, int index) {
		Calendar currentTime = Calendar.getInstance();
		String [] months = {"Jan","Feb","Mar","Apr","May","Jun",
		                    "Jul","Aug","Sep","Oct","Nov","Dec"};
		// Index
		String subject = String.format("TwitterLeadGen: Daily digest #%03d ",index);
		// Day of month
		subject = subject + String.format("%2d ", currentTime.get(Calendar.DAY_OF_MONTH));
		// Month of year
		subject = subject + String.format("%s, ",months[currentTime.get(Calendar.MONTH)]);
		// year
		subject = subject + String.format("%4d ",currentTime.get(Calendar.YEAR));
		
		//get first line
		String text = String.format("TwitterLeadGen: Daily digest #%03d\n\n",index);
		//get end lines
		String endOfText = "Tweet IDs:\n";
		//loop through statuses, adding them to successive entries
		for (int count = 0; count < statusesToSend.size(); count++) {
			Double prob = statusesToSend.get(count).getProbability();
			Status tweet = statusesToSend.get(count).getStatus();
			String url = String.format("https://twitter.com/statuses/%d",statusesToSend.get(count).getStatus().getId());
			text = text + String.format("%2d. %s\n\t\t-@%s\n\t\t%s\n\n", count+1,tweet.getText(),tweet.getUser().getScreenName(),url);
			endOfText = endOfText + String.format("%2d. <<%020d>>\n", count+1, tweet.getId());
		}
		
		text = text + endOfText;
		
		String[] message = {subject,text};
		return message;
	}
	
}
