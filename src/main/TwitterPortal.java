package main;

import twitter4j.TwitterFactory;
import twitter4j.Twitter;
import twitter4j.Status;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.auth.OAuth2Token;
import twitter4j.User;

import java.util.*;

public class TwitterPortal {

	private OAuth2Token token;
	private Twitter twitter;
	private ConfigurationBuilder cb;
	public static final int DEFAULT_NUM_OF_TWEETS = 1000;
	private int numberOfTweets = DEFAULT_NUM_OF_TWEETS;
	private static TwitterPortal instance;
	
	/**
	 * Constructor for a TwitterPortal
	 */
	private TwitterPortal() {
		token = getOAuth2Token();

		cb = new ConfigurationBuilder();
		
		cb.setApplicationOnlyAuthEnabled(true);
		cb.setOAuthConsumerKey("cEAQZd44w6LUZbDpbzqBz3ViP");
		cb.setOAuthConsumerSecret("Bp9fhrg8nkUDBJ5AILA9IH7ldtNSrzxGo5N6PYgOcCBpNYKnGf");
		cb.setOAuth2TokenType(token.getTokenType());
		cb.setOAuth2AccessToken(token.getAccessToken());
		
		twitter = new TwitterFactory(cb.build()).getInstance();
	}
	
	/**
	 * Method to get an instance of TwitterPortal.
	 * @return Instance of TwitterPortal
	 */
	public static TwitterPortal getInstance() {
		if (instance == null) {
			instance = new TwitterPortal();
		}
		return instance;
	}
	
	/**
	 * Method to create an OAuth2Token.
	 * @return An OAuth2Token.
	 */
	private OAuth2Token getOAuth2Token() {
		ConfigurationBuilder cb;
		cb = new ConfigurationBuilder();
		cb.setApplicationOnlyAuthEnabled(true);
		cb.setOAuthConsumerKey("cEAQZd44w6LUZbDpbzqBz3ViP");
		cb.setOAuthConsumerSecret("Bp9fhrg8nkUDBJ5AILA9IH7ldtNSrzxGo5N6PYgOcCBpNYKnGf");
		
		try {
			token = new TwitterFactory(cb.build()).getInstance().getOAuth2Token();
		} catch (Exception e) {
			System.out.println("Can't get OAuth2 token");
			e.printStackTrace();
			System.exit(0);
		}
		return token;
	}
	
	/**
	 * Method to get the Tweets resulting from the specified query.
	 * @param queryString String to query Twitter for
	 * @return List of Tweets, if query is valid and has results, null otherwise.
	 */
	public ArrayList<Status> query(String queryString) {
		Query query = null;
		QueryResult result = null;
		try {
			query = new Query(queryString);
			result = twitter.search(query);
			return new ArrayList<Status>(result.getTweets());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
	}
	
	/**
	 * Method to get the info about a user based on ID.
	 * @param userID The ID of a user.
	 * @return Instance of that user if it exists, null otherwise.
	 */
	public User getUser(long userID) {
		try {
			User user = twitter.showUser(userID);
			return user;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Method to get the max number of tweets that will be returned form a query.
	 * @return Maximum number of Tweets that will be returned from a query.
	 */
	public int getNumOfTweets() {
		return numberOfTweets;
	}
	
	/**
	 * Method to set the maximum number of Tweets that will be returned form a query.
	 * Value should be between 1 and 1500, inclusive, any value above or below that range 
	 * will be treated as 1500 or 1, respectively.
	 * @return The value that numberOfTweets was set to.
	 */
	public int setNumOfTweets(int num) {
		numberOfTweets = num;
		if (numberOfTweets<1) numberOfTweets = 1;
		else if (numberOfTweets>1500) numberOfTweets = 1500;
		return numberOfTweets;
	}
	
}
