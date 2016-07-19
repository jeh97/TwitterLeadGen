package main;

import twitter4j.Status;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TreeMap;

public class TwitterLeadGenController {
	private TwitterPortal portal;
	private RelevanceFilter filter;
	private RatingsHandler ratings;
	private ResponseManager response;
	
	private String[] searchPhrases = {"working new app", "new android app", "new ios app", "developing new app"};
	
	private static TwitterLeadGenController instance;
	
	/**
	 * Constructor for twitterLeadGenController
	 */
	private TwitterLeadGenController() {
		portal = TwitterPortal.getInstance();
		filter = RelevanceFilter.getInstance();
		ratings = RatingsHandler.getInstance();
		response = ResponseManager.getInstance();
		
	}
	
	/**
	 * Gets an instance of TwitterLeadGenController
	 * @return instance
	 */
	public static TwitterLeadGenController getInstance() {
		if (instance == null) {
			instance = new TwitterLeadGenController();
		}
		return instance;
	}
	
	public static void main(String [] args) {
		TwitterLeadGenController controller = TwitterLeadGenController.getInstance();
		
		Calendar calendar = null;
		int lastDay = -1;
		int currDay = -1;
		
		while (true) {
			System.out.println();
			calendar = Calendar.getInstance();
			currDay = calendar.get(Calendar.DAY_OF_YEAR);
			
			printDate(calendar);
			System.out.printf("currDay = %d\n", currDay);
			System.out.printf("lastDay = %d\n", lastDay);
			
			if (currDay != lastDay && calendar.get(Calendar.HOUR_OF_DAY) >= 6) {
				System.out.printf("TwitterLeadGen has not been run today, and it is not earlier than 6AM.\n");
				lastDay = currDay;
				try {
					System.out.printf("Running TwitterLeadGenController\n");
					controller.run();
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				System.out.printf("TwitterLeadGen has already been run today.\n");
			}
			try {
				System.out.println("Checking again in 3 hours\n\n");
				long delay = 540000L;
				System.out.println("|--------------------|");
				System.out.print(  " ");
				for (int i = 0; i < 12; i++) {
					try {
						Thread.sleep(delay);
					} catch (Exception e) {
						e.printStackTrace();
						System.out.println();
					}
					System.out.print("*");
				}
				System.out.println();
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Runs the controller, checking for new email responses, 
	 * fetching new tweets, filtering the tweets, sending the 
	 * digest
	 */
	public void run() {
		System.out.println("Checking mail...");
		checkMail();
		System.out.println("Querying Twitter...");
		ArrayList<Status> statuses = fetchStatuses();
		System.out.println("Calculating relevance probabilities...");
		ArrayList<StatusObject> probs = getProbabilities(statuses);
		System.out.println("Sending daily digest...");
		processRatings(probs);
	}
	
	/**
	 * Checks for any new emails
	 * @return true if it processed a new valid email, false otherwise
	 */
	public boolean checkMail() {
		response.checkNewResponses();
		return true;
	}
	
	/**
	 * Collects a single ArrayList containing all the statuses returned by 
	 * all the search phrases in searchPhrases
	 * @return ArrayList of statuses
	 */
	public ArrayList<Status> fetchStatuses() {
		//go through each search phrase
		ArrayList<Status> tweets = new ArrayList<Status>();
		int len = searchPhrases.length;
		for (int i = 0; i < len; i++) {
			tweets.addAll(portal.query(searchPhrases[i]));
		}
		len = tweets.size();
		for (int i = len-1; i >= 0; i--) {
			if (tweets.get(i).isRetweet()) {
				tweets.remove(i);
			}
		}
		
		return tweets;
	}
	
	/**
	 * Gets the irrelevance probabilities for all of the statuses it is given
	 * @param statuses Statuses to process
	 * @return Map of probabilities to their respective statuses
	 */
	public ArrayList<StatusObject> getProbabilities(ArrayList<Status> statuses) {
		
		ArrayList<StatusObject> statusProbabilities = new ArrayList<StatusObject>();
		
		//for each status calculate probability
		
		for (int i = 0; i < statuses.size(); i++) {
			Status current = statuses.get(i);
			Double probability = filter.getProbability(current.getText());
			statusProbabilities.add(new StatusObject(probability, current));
		}
		
		return statusProbabilities;
	}
	
	/**
	 * Executes the post processing on statuses with respect to their probabilities
	 * @param probabilities Probabilities and statuses to process
	 */
	public void processRatings(ArrayList<StatusObject> probabilities) {
		ratings.processWithRatings(probabilities);
	}

	public static void printDate(Calendar currentTime) {
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
		
		System.out.printf("%-9s %2d / %02d / %4d  %2d:%02d\n", day,
											currentTime.get(Calendar.DATE),
											currentTime.get(Calendar.MONTH),
											currentTime.get(Calendar.YEAR),
											currentTime.get(Calendar.HOUR_OF_DAY),
											currentTime.get(Calendar.MINUTE));
		
	}
	
}
