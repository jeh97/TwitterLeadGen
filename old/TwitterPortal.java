import twitter4j.TwitterFactory;
import twitter4j.Twitter;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.auth.OAuth2Token;

import jxl.*;
import jxl.write.*;
import jxl.write.Number;
import jxl.write.biff.RowsExceededException;

import java.util.*;
import java.io.File;
import java.io.IOException;


public class TwitterPortal {
	private OAuth2Token token;
	private Twitter twitter;
	private ConfigurationBuilder cb;
	
	private final int NUMBER_OF_TWEETS = 1000; //number <= 1500
	
	public TwitterPortal() {
		token = null;
		getOAuth2Token();

		cb = new ConfigurationBuilder();
		
		cb.setApplicationOnlyAuthEnabled(true);
		cb.setOAuthConsumerKey("cEAQZd44w6LUZbDpbzqBz3ViP");
		cb.setOAuthConsumerSecret("Bp9fhrg8nkUDBJ5AILA9IH7ldtNSrzxGo5N6PYgOcCBpNYKnGf");
		cb.setOAuth2TokenType(token.getTokenType());
		cb.setOAuth2AccessToken(token.getAccessToken());
		
		twitter = new TwitterFactory(cb.build()).getInstance();
	}
	public Twitter getTwitter() {return twitter;}
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
	public void queryAndPrintResults(String queryString) throws TwitterException {
		Query query = new Query(queryString);
		QueryResult result = twitter.search(query);
		
		printTweets(result);
	}
	public static void printTweets(QueryResult results) {
		for (Status status : results.getTweets()) {

			Calendar sent = DateToCalendar(status.getCreatedAt());
			System.out.print(getDateAsString(sent));
			System.out.printf("  @%s:%s\n",status.getUser().getScreenName(),status.getText());
		}
	}
	public void writeTweetsToExcel(String queryString, String filename) {
		Query query = new Query(queryString);
		long lastID = Long.MAX_VALUE;
		int lastTweetsSize = 0;
		int pagesSinceNewTweetsFound = 0;
		ArrayList<Status> tweets = new ArrayList<Status>();
		while ((tweets.size() < NUMBER_OF_TWEETS) && (pagesSinceNewTweetsFound < 4)) {
			if (NUMBER_OF_TWEETS - tweets.size() > 100) {
				query.setCount(100);
			} else {
				query.setCount(NUMBER_OF_TWEETS - tweets.size());
			}
			try {
				QueryResult result = twitter.search(query);
				tweets.addAll(result.getTweets());
				if (tweets.size() == lastTweetsSize) {
					pagesSinceNewTweetsFound++;
				} else {
					lastTweetsSize = tweets.size();
					pagesSinceNewTweetsFound = 0;
				}
				System.out.printf("Gathered %d tweets.\n",tweets.size());
				for (Status t: tweets) {
					if (t.getId() < lastID) { lastID = t.getId(); }
				}
			} catch (TwitterException te) {
				System.out.println("Couldn't connect: "+te);
				te.printStackTrace();
			}
			query.setMaxId(lastID-1);
		}
		try {
			String fn = String.format("/Users/Jacob/Documents/Repositories/TwitterLeadGen/Excel/%s",filename);
			File exlFile = new File(fn);
			WritableWorkbook writableWorkbook = Workbook.createWorkbook(exlFile);
			WritableSheet writableSheet = writableWorkbook.createSheet("Sheet1", 0);
			
			Label queryLabel = new Label(0,0,queryString);
			Label dateLabel = new Label(1,0,"Date");
			Label authorLabel = new Label(2,0,"Author");
			Label iDLabel = new Label(3,0,"ID");
			Label contentLabel = new Label(4,0,"Text");
			writableSheet.addCell(queryLabel);
			writableSheet.addCell(dateLabel);
			writableSheet.addCell(authorLabel);
			writableSheet.addCell(iDLabel);
			writableSheet.addCell(contentLabel);
			int count = 1;
			for (Status status : tweets) {
				if (status.getLang().equals("en") && !status.isRetweet()) {
					DateTime date = new DateTime(1,count,status.getCreatedAt());
					Label author = new Label(2,count,String.format("@%s",status.getUser().getScreenName()));
					Number id =  new Number(3,count,status.getId());
					Label content = new Label(4,count,status.getText());
				
					writableSheet.addCell(date);
					writableSheet.addCell(author);
					writableSheet.addCell(id);
					writableSheet.addCell(content);
					count++;
				}
			}
			writableWorkbook.write();
			writableWorkbook.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (RowsExceededException e) {
			e.printStackTrace();
		} catch (WriteException e) {
			e.printStackTrace();
		}
	}
	
	public static void doQuery(TwitterPortal portal,String queryString) {
		System.out.printf("\n<%s>\n",queryString);
		portal.writeTweetsToExcel(queryString,queryString+".xls");
	}
	
	public static void main(String [] args) throws TwitterException {
		System.out.println("--Program beginning--");
		
		TwitterPortal portal = new TwitterPortal();
		
		System.out.println(portal.twitter.showUser(1719508897).getLocation());
		
		//doQuery(portal,"new game android working");
		//doQuery(portal,"new game google play working");
		//doQuery(portal,"new game ios working");
		//doQuery(portal,"new game app store working");
		//doQuery(portal,"xxx");
		//doQuery(portal,"xxx");
		//doQuery(portal,"xxx");
		

		//System.out.println("\n<XXX>");
		//writeTweetsToExcel("XXX",twitter,"XXX.xls");
		
		
		System.out.println("\n--Program complete--");
	}
	
	public static Calendar DateToCalendar(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);;
		return cal;
	}
	public static String getDateAsString(Calendar currentTime) {
		String day = "";
		switch (currentTime.get(Calendar.DAY_OF_WEEK)) {
		case 1: day = "Sunday"; break;
		case 2: day = "Monday"; break;
		case 3: day = "Tuesday"; break;
		case 4: day = "Wednesday"; break;
		case 5: day = "Thursday"; break;
		case 6: day = "Friday"; break;
		case 7: day = "Saturday";
		}
		String result = String.format("%-9s %2d / %02d / %4d  %2d:%2d", day,
											currentTime.get(Calendar.DATE),
											currentTime.get(Calendar.MONTH),
											currentTime.get(Calendar.YEAR),
											currentTime.get(Calendar.HOUR_OF_DAY),
											currentTime.get(Calendar.MINUTE));
		return result;
	}
}
