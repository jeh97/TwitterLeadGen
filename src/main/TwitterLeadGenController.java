package main;

import twitter4j.Status;
import java.util.ArrayList;
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
		controller.run();
	}
	
	/**
	 * Runs the controller, checking for new email responses, 
	 * fetching new tweets, filtering the tweets, sending the 
	 * digest
	 */
	public void run() {
		//checkMail();
		ArrayList<Status> statuses = fetchStatuses();
		ArrayList<StatusObject> probs = getProbabilities(statuses);
		processRatings(probs);
	}
	
	/**
	 * Checks for any new emails
	 * @return true if it processed a new valid email, false otherwise
	 */
	public boolean checkMail() {
		return false;
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
	
	
}
