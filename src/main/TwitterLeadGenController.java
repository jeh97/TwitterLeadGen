package main;

import twitter4j.Status;
import java.util.ArrayList;
import java.util.Calendar;
import java.io.BufferedReader;
import java.io.FileReader;

public class TwitterLeadGenController {
	private TwitterPortal portal;
	private RelevanceFilter filter;
	private RatingsHandler ratings;
	private ResponseManager response;
	
	private final String SEARCH_PHRASES_FILE = "./TwitterLeadGen/util/search_phrases.txt";
	private final String USER_BLACKLIST_FILE = "./TwitterLeadGen/util/search_phrases.txt";
	
	
	
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
		long nextCheck = -1;
		
		while (true) {
			System.out.println();
			calendar = Calendar.getInstance();
			currDay = calendar.get(Calendar.DAY_OF_YEAR);
			
			printDate(calendar);
			System.out.printf("currDay = %d\n", currDay);
			System.out.printf("lastDay = %d\n", lastDay);
			
			if (currDay != lastDay) {
				System.out.printf("TwitterLeadGen has not been run today.");
				lastDay = currDay;
				System.out.println("Running TwitterLeadGen");
				try {
					controller.run();
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				System.out.printf("TwitterLeadGen has already been run today.\n");
			}
			System.out.println("\nChecking again in 3 hours");
			long delay = 10800000L;
			nextCheck = System.currentTimeMillis()+delay;
			while (System.currentTimeMillis() < nextCheck) {
				try {
					Thread.sleep(nextCheck - System.currentTimeMillis());
				} catch (Exception e) {
					e.printStackTrace();
				}
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
		
		try (BufferedReader br = new BufferedReader(new FileReader(SEARCH_PHRASES_FILE))) {
			String line;
			while ((line = br.readLine()) != null) {
				tweets.addAll(portal.query(line.replaceAll(System.lineSeparator(), "")));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		String blacklist = "";
		ArrayList<String> user_blacklist = new ArrayList<String>();
		try (BufferedReader br = new BufferedReader(new FileReader(USER_BLACKLIST_FILE))) {
			String line;
			while ((line = br.readLine()) != null) {
				blacklist += line;
				user_blacklist.add(line.replaceAll(System.lineSeparator(), ""));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		int len = tweets.size();
		for (int i = len-1; i >= 0; i--) {
			if (tweets.get(i).isRetweet()) {
				tweets.remove(i);
			} else if (blacklist.indexOf(tweets.get(i).getUser().getScreenName().toLowerCase()) != -1) {
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
