package main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;

import twitter4j.Status;
import twitter4j.User;

import java.util.Date;
import java.util.Calendar;
import java.util.Random;

public class DBManager {
	
	private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	private static final String DB_URL = "jdbc:mysql://localhost:3306/TwitterLeadGen?autoReconnect=true&useSSL=false";
	
	private static final String USER = "twitter_lead_gen";
	private static final String PASS = "dreamBig";
	
	/**
	 * A singleton of this class.
	 */
	private static DBManager instance;
	
	/**
	 * Constructor for DBManager
	 */
	private DBManager() {
		
	}
	
	/**
	 * Method to get an instance of DBManager
	 * @return instance of DBManager
	 */
	public static DBManager getInstance() {
		if (instance == null) {
			instance = new DBManager();
		}
		return instance;
	}
	
	/**
	 * Method to execute a line of SQL in the TwitterLeadGen database
	 * when no response is necessary
	 * @param sql line to execute
	 * @return the line executed, if successful
	 */
	private String executeSql(String sql) {
		Connection conn = null;
		Statement stmt = null;
		
		try {
			conn = DriverManager.getConnection(DB_URL,USER,PASS);
			stmt = conn.createStatement();
			stmt.execute(sql);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			try { stmt.close();} catch (SQLException e) { e.printStackTrace();}
			try { conn.close();} catch (SQLException e) { e.printStackTrace();}
		}
		return sql;
	}
	
	/**
	 * Method to see if the twitter_word_stats table has statistics
	 * for the given word
	 * @param word Word to check table for
	 * @return true if word is in table, false otherwise
	 */
	public boolean hasWord(String word) {
		String sql = String.format("SELECT EXISTS(SELECT 1 FROM twitter_word_stats WHERE word = '%s');",word);
		Connection conn = null;
		Statement stmt = null;
		ResultSet set = null;
		
		boolean hasIt = false;
		try {
			conn = DriverManager.getConnection(DB_URL,USER,PASS);
			stmt = conn.createStatement();
			
			set = stmt.executeQuery(sql);
			
			set.next();
			
			hasIt = set.getBoolean(1);
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try { stmt.close();} catch (SQLException e) { e.printStackTrace();}
			try { conn.close();} catch (SQLException e) { e.printStackTrace();}
			try {  set.close();} catch (SQLException e) { e.printStackTrace();}	
		}
		return hasIt;
		
	}
	
	/**
	 * Adds a word to the twitter_word_stats table with given values.
	 * @param word Word to add
	 * @param relTweets Number of relevant Tweets the word has occurred in
	 * @param irrTweets Number of irrelevant Tweets the word has occurred in
	 * @param totTweets Total number of Tweets the word has occurred in
	 * @return True if the word was added or already existed, false otherwise
	 */
	public boolean addWord(String word, int relTweets, int irrTweets, int totTweets) {
		if (hasWord(word)) {return false;}
		String sql = "INSERT INTO twitter_word_stats "
				+ "(word, relevantCount, irrelevantCount, totalCount) "
				+ String.format("VALUES ('%s', %d, %d, %d)", word, relTweets, irrTweets, totTweets);
		executeSql(sql);
		return hasWord(word);
	}
	
	/**
	 * Method to add a word to the twitter_word_stats table with no recorded occurrences
	 * @param word Word to add
	 * @return True if the word is successfully added, false otherwise
	 */
	public boolean addWord(String word) {
		return addWord(word,0,0,0);
	}
	
	/**
	 * Method to remove a word from the statistics table
	 * @param word Word to remove
	 * @return True if word is successfully removed, false otherwise
	 */
	public boolean removeWord(String word) {
		// Check if word already exists, if so, return false
		if (!hasWord(word)) {return false;}
		
		//Create SQL statement string
		String sql = String.format("DELETE FROM twitter_word_stats WHERE word = '%s'",word);
		
		// Execute statement
		executeSql(sql);
		
		// Check if word exists now, if not, return true, otherwise return false
		return !hasWord(word);
	}
	
	/**
	 * Method to get the WordObject representation of a word in the table
	 * @return WordObject representation of word, null if it is not in the table
	 */
	public WordObject getInfoForWord(String word) {
		// Check if has word, return null if not.
		if (!hasWord(word)) return null;
		
		// Create sql statement
		String sql = String.format("SELECT * FROM twitter_word_stats WHERE word = '%s'",word);
		
		// Declare connections, statement, and result set
		Connection conn = null;
		Statement stmt = null;
		ResultSet set = null;
		
		// Create WordObject
		WordObject result = null;
		try {
			//Connect,create statement
			conn = DriverManager.getConnection(DB_URL,USER,PASS);
			stmt = conn.createStatement();
			
			set = stmt.executeQuery(sql);
			
			set.next();
			
			//Get info from results and put in WordObject
			int rel = set.getInt("relevantCount");
			int irr = set.getInt("irrelevantCount");
			int tot = set.getInt("totalCount");
			
			result = new WordObject(word, rel, irr, tot);
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try { stmt.close();} catch (SQLException e) { e.printStackTrace();}
			try { conn.close();} catch (SQLException e) { e.printStackTrace();}
			try {  set.close();} catch (SQLException e) { e.printStackTrace();}	
		}
		
		// return WordObject
		return result;
	}
	
	/**
	 * Increments the value of relevant and total in the twitter_word_stats 
	 * table for the given word
	 * @param word Word to increment
	 * @return The new 'relevant' value for the word , -1 if word does not exist
	 */
	public int addRelevantInstance(String word) {
		// check if has word, if not return -1
		if (!hasWord(word)) return -1;
		
		// create sql statement string
		String sql = String.format("UPDATE twitter_word_stats SET relevantCount = relevantCount+1, totalCount = totalCount + 1 WHEN word = '%s'", word);
		
		// execute sql
		executeSql(sql);
		
		// return new relevantCount
		return getInfoForWord(word).getRelevant();
	}
	
	/**
	 * Increments the value of irrelevant and total in the twitter_word_stats
	 * table for the given word
	 * @param word Word to increment
	 * @return The new 'irrelevant' value for the word, -1 if word does not exist
	 */
	public int addIrrelevantInstance(String word) {
		// check if has word, if not return -1
		if (!hasWord(word)) return -1;
		
		// create sql statement string
		String sql = String.format("UPDATE twitter_word_stats SET irrelevantCount = irrelevantCount+1, totalCount = totalCount + 1 WHEN word = '%s'", word);
		
		// execute sql
		executeSql(sql);
		
		// return new irrelevantCount
		return getInfoForWord(word).getIrrelevant();
	}
	
	/**
	 * Adds the info for the given Status to the all_tweets table
	 * @param status Tweet to add to table
	 * @return the ID of the added Tweet, Long.MIN_VALUE if Tweet already in table
	 */
	public long addTweet(Status status) {
		//Check if tweet is in table, if so, return Long.MIN_VALUE
		if (hasTweet(status)) return Long.MIN_VALUE;
		
		// make sure any single quotes in the status text are replaced with \'
		
		//get TIMESTAMP represenation of date
		Calendar c = Calendar.getInstance();
		c.setTime(status.getCreatedAt());
		String dateString = String.format("'%4d-%02d-%02d %02d:%02d:%02d'", c.get(Calendar.YEAR),c.get(Calendar.MONTH)+1,c.get(Calendar.DATE),
				c.get(Calendar.HOUR_OF_DAY),c.get(Calendar.MINUTE),c.get(Calendar.SECOND));
		//Create sql statement to add tweet
		String sql = "INSERT INTO all_tweets (id, date, text, userID, lang, retweetCount, favoriteCount, hasBeenProcessed) "
				+ String.format("VALUES (%d, %s, '%s', %d, '%s', %d, %d, 0)",	status.getId(),
																			dateString,
																			status.getText().replaceAll("['\"\\\\]","\\\\$0"),
																			status.getUser().getId(),
																			status.getLang(),
																			status.getRetweetCount(),
																			status.getFavoriteCount());
		
		//Execute sql
		executeSql(sql);
		
		//return the tweet id
		return status.getId();
	}
	
	/**
	 * Adds the info for the given User to the all_users table
	 * @param user User to add to the table
	 * @return the ID of the added User, Long.MIN_VALUE if User already in table
	 */
	public long addUser(User user) {
		//Check if user is in table, if so, return Long.MIN_VALUE
		if (hasUser(user)) return Long.MIN_VALUE;
		
		Calendar c = Calendar.getInstance();
		c.setTime(user.getCreatedAt());
		String dateString = String.format("%4d-%02d-%02d\\ %02d:%02d:%02d", c.get(Calendar.YEAR),c.get(Calendar.MONTH)+1,c.get(Calendar.DATE),
				c.get(Calendar.HOUR_OF_DAY),c.get(Calendar.MINUTE),c.get(Calendar.SECOND));
		
		//create sql statement to add user
		String sql = "INSERT INTO all_users (id, screenName, name, createdAt, "
				+ "description, lang, url, verified, location, timezone, statusesCount, "
				+ "followersCount, friendsCount, favoritesCount) "
				+ String.format("VALUES (%d, '%s'. '%s', %s, '%s', '%s', '%s', %d, '%s', '%s', %d, %d, %d, %d)",
						user.getId(),
						user.getScreenName(),
						user.getName(),
						dateString,
						user.getDescription(),
						user.getLang(),
						user.getURL(),
						user.isVerified(),
						user.getLocation(),
						user.getTimeZone(),
						user.getStatusesCount(),
						user.getFollowersCount(),
						user.getFriendsCount(),
						user.getFavouritesCount());
		
		// execute sql
		executeSql(sql);
		
		//return user id
		return user.getId();
	}
	
	/**
	 * Determines whether a given user's info has already populated the all_users table
	 * @param user User to check
	 * @return true if the User is in the table, false otherwise
	 */
	public boolean hasUser(User user) {
		// Create sql statement
		String sql = String.format("SELECT EXISTS(SELECT 1 FROM all_users WHERE id = %d);",user.getId());
		
		// Declare flag
		boolean exists = false;
		
		// Declare connections, statement, and result set
		Connection conn = null;
		Statement stmt = null;
		ResultSet set = null;
		
		try {
			//Connect,create statement
			conn = DriverManager.getConnection(DB_URL,USER,PASS);
			stmt = conn.createStatement();
			
			set = stmt.executeQuery(sql);
			
			set.next();
			
			exists = set.getBoolean(1);
			
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try { stmt.close();} catch (SQLException e) { e.printStackTrace();}
			try { conn.close();} catch (SQLException e) { e.printStackTrace();}
			try {  set.close();} catch (SQLException e) { e.printStackTrace();}	
		}
		
		// return whether it exists or not
		return exists;
	}

	/**
	 * Determines whether a given Tweet has already populated the all_tweets table
	 * @param id Id of Tweet to check
	 * @return true if the Status is already in the table, false otherwise
	 */
	public boolean hasTweet(Long id) {
		// Create sql statement
		String sql = String.format("SELECT EXISTS(SELECT 1 FROM all_tweets WHERE id = %d);",id);
		
		// Declare connections, statement, and result set
		Connection conn = null;
		Statement stmt = null;
		ResultSet set = null;
		
		//declare flag
		boolean exists = false;
		
		try {
			//Connect,create statement
			conn = DriverManager.getConnection(DB_URL,USER,PASS);
			stmt = conn.createStatement();
			
			set = stmt.executeQuery(sql);
			
			set.next();
			
			exists = set.getBoolean(1);
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try { stmt.close();} catch (SQLException e) { e.printStackTrace();}
			try { conn.close();} catch (SQLException e) { e.printStackTrace();}
			try {  set.close();} catch (SQLException e) { e.printStackTrace();}	
		}
		
		// return whether it exists or not
		return exists;
	}
	
	public boolean hasTweetWithText(String text) {
		// make sure any single quotes in the status text are replaced with \'
		text = text.replaceAll("['\"\\\\]","\\\\$0");
		
		// Create sql statement
		String sql = String.format("SELECT EXISTS(SELECT 1 FROM all_tweets WHERE text = '%s');",text);
		
		// Declare connections, statement, and result set
		Connection conn = null;
		Statement stmt = null;
		ResultSet set = null;
		
		//declare flag
		boolean exists = false;
		
		try {
			//Connect,create statement
			conn = DriverManager.getConnection(DB_URL,USER,PASS);
			stmt = conn.createStatement();
			
			set = stmt.executeQuery(sql);
			
			set.next();
			
			exists = set.getBoolean(1);
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try { stmt.close();} catch (SQLException e) { e.printStackTrace();}
			try { conn.close();} catch (SQLException e) { e.printStackTrace();}
			try {  set.close();} catch (SQLException e) { e.printStackTrace();}	
		}
		
		// return whether it exists or not
		return exists;
	}
	
	/**
	 * Determines whether a given Tweet has already populated the all_tweets table
	 * @param status Status to check
	 * @return true if the Status is already in the table, false otherwise
	 */
	public boolean hasTweet(Status status) {
		return hasTweet(status.getId());
	}
	
	/**
	 * Returns the Text of a tweet with the given ID
	 * @param id
	 * @return
	 */
	public String getTweetText(Long id) {
		// Check if has word, return null if not.
		if (!hasTweet(id)) return null;
		// Create sql statement
		String sql = String.format("SELECT text FROM all_tweets WHERE id = '%d'",id);
		
		// Declare connections, statement, and result set
		Connection conn = null;
		Statement stmt = null;
		ResultSet set = null;
			
		// Create WordObject
		String result = null;
		try {
			//Connect,create statement
			conn = DriverManager.getConnection(DB_URL,USER,PASS);
			stmt = conn.createStatement();
			
			set = stmt.executeQuery(sql);
				
			set.next();
			
			result = set.getString(1);
				
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try { stmt.close();} catch (SQLException e) { e.printStackTrace();}
			try { conn.close();} catch (SQLException e) { e.printStackTrace();}
			try {  set.close();} catch (SQLException e) { e.printStackTrace();}	
		}
		return result;
	}
	
	/**
	 * Method to set the irrelevance probability of a given tweet
	 * @param status Tweet to set probability for
	 * @param probability Probability value (0.0-1.0)
	 * @return If successful, ID of the Tweet. Long.MIN_VALUE otherwise.
	 */
	public long setProbability(Status status, double probability) {
		//check if id already has status, if so, return Long.MIN_VALUE
		if (hasProbability(status)) return Long.MIN_VALUE;
		
		//create sql statement
		String sql = String.format("UPDATE all_tweets SET probability = %f",probability);
		
		//execute statement
		executeSql(sql);
		
		//return the id
		return status.getId();
	}
	
	/**
	 * Determines whether a given Tweet has already been assigned a probability
	 * @param status Tweet to check
	 * @return True if it exists in the table and already has a probability, false otherwise
	 */
	public boolean hasProbability(Status status) {
		// Create sql statement
		String sql = String.format("SELECT probability FROM all_tweets WHERE id = %d",status.getId());

		// Declare connections, statement, and result set
		Connection conn = null;
		Statement stmt = null;
		ResultSet set = null;
		
		//declare flag
		boolean hasProb = false;
		
		try {
			//Connect,create statement
			conn = DriverManager.getConnection(DB_URL,USER,PASS);
			stmt = conn.createStatement();
			
			set = stmt.executeQuery(sql);
			
			set.next();
			
			hasProb = set.getObject(1) == null;
		
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try { stmt.close();} catch (SQLException e) { e.printStackTrace();}
			try { conn.close();} catch (SQLException e) { e.printStackTrace();}
			try {  set.close();} catch (SQLException e) { e.printStackTrace();}	
		}
		
		// return whether it exists or not
		return hasProb;
	}
	
	/**
	 * Determines whether a given Tweet has already been included in an email
	 * @param status Tweet to check
	 * @return True if it exists in the table and has already been sent, false otherwise
	 */
	public boolean haveSent(Status status) {
		// Create sql statement
		String sql = String.format("SELECT hasBeenProcessed FROM all_tweets WHERE id = %d",status.getId());

		// Declare connections, statement, and result set
		Connection conn = null;
		Statement stmt = null;
		ResultSet set = null;
		
		//declare flag
		boolean sent = false;
		
		try {
			//Connect,create statement
			conn = DriverManager.getConnection(DB_URL,USER,PASS);
			stmt = conn.createStatement();
			
			set = stmt.executeQuery(sql);
			
			set.next();
			
			sent = set.getBoolean(1);
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try { stmt.close();} catch (SQLException e) { e.printStackTrace();}
			try { conn.close();} catch (SQLException e) { e.printStackTrace();}
			try {  set.close();} catch (SQLException e) { e.printStackTrace();}	
		}
		
		// return whether it exists or not
		return sent;
	}
	
	/**
	 * Sets the given Tweet as having already been sent in an email digest.
	 * @param status Tweet to set as sent
	 * @return If successful, the ID of the Tweet. Long.MIN_VALUE otherwise
	 */
	public long setAsSent(Status status) {
		// check if tweet exits, if not, return Long.MIN_VALUE
		if (!hasTweet(status)) return Long.MIN_VALUE;
		
		// create sql statement
		String sql = String.format("UPDATE all_tweets SET hasBeenProcessed = 1 WHERE id = %d",status.getId());
		
		// execute sql statement
		executeSql(sql);
		
		// return id
		return status.getId();
	}
	
	/**
	 * Method to check if there exists and email with the given index.
	 * @param index index to check for
	 * @return True if the index is already assigned to an email, False otherwise
	 */
	public boolean emailIndexExists(int index) {
		//create sql statement
		String sql = String.format("SELECT EXISTS(SELECT 1 FROM sent_emails WHERE index = '%d');",index);
		
		//declare connections
		Connection conn = null;
		Statement stmt = null;
		ResultSet set = null;
		
		//declare flag
		boolean hasIt = false;
		try {
			conn = DriverManager.getConnection(DB_URL,USER,PASS);
			stmt = conn.createStatement();
			
			set = stmt.executeQuery(sql);
			
			set.next();
			
			hasIt = set.getBoolean(1);
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try { stmt.close();} catch (SQLException e) { e.printStackTrace();}
			try { conn.close();} catch (SQLException e) { e.printStackTrace();}
			try {  set.close();} catch (SQLException e) { e.printStackTrace();}	
		}
		return hasIt;
	}
	
	/**
	 * Flags the email associated with the given ID as having been responded to, 
	 * used to keep duplicate responses being recorded
	 * @param id ID of the email that was responded to
	 * @return True if exists in table and hasResponse is now true, false otherwise
	 */
	public boolean emailResponseRecorded(int index) {
		//check if id exists, if not, return false
		if (!emailIndexExists(index)) return false;
		
		//create statement
		String sql = String.format("UPDATE send_emails SET hasResponse = 1 WHERE indexValue = %d", index);
		
		//execute statement
		executeSql(sql);
		
		//return true
		return true;
	}
	
	/**
	 * Method to get the send date of the email with the given ID.
	 * @param id ID of the email
	 * @return Date associated with the email, null none found or email does not exist.
	 */
	public Date getEmailDate(int index) {
		//check if email exists, if not return null
		if (!emailIndexExists(index)) return null;
		
		//create sql statement
		String sql = String.format("SELECT EXISTS(SELECT * FROM sent_emails WHERE indexValue = '%d');",index);
		
		Date date = null;
		
		//declare connections
		Connection conn = null;
		Statement stmt = null;
		ResultSet set = null;
		
		try {
			conn = DriverManager.getConnection(DB_URL,USER,PASS);
			stmt = conn.createStatement();
			
			set = stmt.executeQuery(sql);
			
			set.next();
			
			date = set.getDate(1);
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try { stmt.close();} catch (SQLException e) { e.printStackTrace();}
			try { conn.close();} catch (SQLException e) { e.printStackTrace();}
			try {  set.close();} catch (SQLException e) { e.printStackTrace();}	
		}
		return date;
	}
	
	/**
	 * Returns the next available index
	 * @return index
	 */
	public int getNextIndex() {
		//create sql statement
		String sql = "SELECT MAX(indexValue) AS indexValue FROM sent_emails;";
		
		//declare connections
		Connection conn = null;
		Statement stmt = null;
		ResultSet set = null;
		
		//declare value
		int index = -1;
		try {
			conn = DriverManager.getConnection(DB_URL,USER,PASS);
			stmt = conn.createStatement();
			
			set = stmt.executeQuery(sql);
			
			set.next();
			
			index = set.getInt(1);
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try { stmt.close();} catch (SQLException e) { e.printStackTrace();}
			try { conn.close();} catch (SQLException e) { e.printStackTrace();}
			try {  set.close();} catch (SQLException e) { e.printStackTrace();}	
		}
		return index+1;
	}
	
	
	/**
	 * Method to add the details of a newly sent email.
	 * @param id The ID associated with that email
	 * @param date The date the email was sent
	 * @return index, if id is taken, returns -1
	 */
	public int addSentEmail(Date date, int numberOfTweets) {
		//find index
		int index = getNextIndex();
		
		//Get string representation of date
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		String dateString = String.format("%4d-%02d-%02d %02d:%02d:%02d", c.get(Calendar.YEAR),c.get(Calendar.MONTH)+1,c.get(Calendar.DATE),
				c.get(Calendar.HOUR_OF_DAY),c.get(Calendar.MINUTE),c.get(Calendar.SECOND));
		
		// create statement
		String sql = "INSERT INTO sent_emails (indexValue, date, hasResponse, numberOfTweets) "
				+ String.format("VALUES ( %d, '%s', 0, %d)", index, dateString, numberOfTweets);
		
		//execute statement
		executeSql(sql);
		
		//return index
		return index;
	}
	
	/**
	 * Gets the number of tweets expected from a given email
	 * @param index Index of the email
	 * @return The numberOfTweets for that email, -1 if unsuccessful
	 */
	public int getEmailNumberOfTweets(int index) {
		//create sql statement
		String sql = "SELECT numberOfTweets FROM sent_emails WHERE "
				+ String.format("indexValue = %d;",index);
		
		//declare connections
		Connection conn = null;
		Statement stmt = null;
		ResultSet set = null;
		int numberOfTweets = -1;
		try {
			conn = DriverManager.getConnection(DB_URL,USER,PASS);
			stmt = conn.createStatement();
			
			set = stmt.executeQuery(sql);
			
			set.next();
			
			numberOfTweets = set.getInt(1);
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try { stmt.close();} catch (SQLException e) { e.printStackTrace();}
			try { conn.close();} catch (SQLException e) { e.printStackTrace();}
			try {  set.close();} catch (SQLException e) { e.printStackTrace();}	
		}
		return numberOfTweets;
	}
}
