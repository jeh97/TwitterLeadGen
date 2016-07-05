package main;

import util.Stemmer;

import java.util.ArrayList;
import java.util.Arrays;
import java.lang.Math;

public class RelevanceFilter {
	/**
	 * Weight applied to any words with a total occurrence count
	 * equaling less than 1/32nd of the total number of bodies of text
	 * encountered.
	 */
	public static final double WEIGHT_0_1     = 0.25;
	/**
	 * Weight applied to any words with a total occurrence count
	 * equaling between 1/32nd and 1/16th of the total number of
	 * bodies of text encountered.
	 */
	public static final double WEIGHT_1_2     = 0.75;
	/**
	 * Weight applied to any words with a total occurrence count
	 * equaling between 1/16th and 1/8th of the total number of
	 * bodies of text encountered.
	 */
	public static final double WEIGHT_2_4     = 1.80;
	/**
	 * Weight applied to any words with a total occurrence count
	 * equaling between 1/8th and 1/4th of the total number of
	 * bodies of text encountered.
	 */
	public static final double WEIGHT_4_8     = 2.00;
	/**
	 * Weight applied to any words with a total occurrence count
	 * equaling between 1/4th and one half of the total number
	 * of bodies of text encountered.
	 */
	public static final double WEIGHT_8_16 	  = 1.80;
	/**
	 * Weight applied to any words with a total occurrence count
	 * equaling more than one half of the total number of bodies of text
	 * encountered.
	 */
	public static final double WEIGHT_16_PLUS = 1.3;
	
	private static final String TOTAL = "00Total";
	
	private static String [] stopWordsArray = {"be","on","rt","a","the","and","but","to","for","of","in","with","i","this","it","out","now","an"};
	private static ArrayList<String> stopWords;
	private Stemmer stemmer;
	private static RelevanceFilter instance;
	private DBManager db;
	
	/**
	 * Constructor for RelevanceFilter
	 */
	private RelevanceFilter() {
		db = DBManager.getInstance();
		stopWords = new ArrayList<String>(Arrays.asList(stopWordsArray));
		stemmer = Stemmer.getInstance();
	}
	
	/**
	 * Method to get an instance of RelevanceFilter
	 * @return Instance of RelevanceFilter
	 */
	public static RelevanceFilter getInstance() {
		if (instance == null) {
			instance = new RelevanceFilter();
		}
		return instance;
	}
	
	/**
	 * Method to get the relevance probability of the given word, returning null if the 
	 * word is a STOPWORD or if it is not found in the Database
	 * @param word Word to get probability for
	 * @return Word object of the word and its probability that that the body of 
	 * text is irrelevant given it contains the given word
	 */
	public WordObject getWordProbability(String word) {
		WordObject str = null;
		//stem word
		String stem = stemString(word.toLowerCase());
		
		//check if its a stop word, if so, return null
		if (stopWords.indexOf(stem) != -1) {
			return null;
		}
		
		// get number of relevant counts, irrelevant counts, and total counts
		str = db.getInfoForWord(stem);
		if (str == null) return null;
		
		// get probability that a tweet is irrelevant given it contains this word
		str.setProbability(str.getIrrelevant()/(double)str.getTotal());
		
		// return the WordObject
		return str;
	}
	
	/**
	 * Method to get the relevance probability of a given body of 
	 * text
	 * @param text Body of text to get probability for
	 * @return Probability that the given body of text is relevant,
	 * given the words it contains
	 */
	public double getProbability(String text) {
		//get total count
		int total = db.getInfoForWord(TOTAL).getTotal();
		
		//split text into words by whitespace
		String[] splitStrings = text.split("\\s+");
		int len = splitStrings.length;
		
		//for each word, getWordProbability, while collecting them them with weights
		double eta = 0; //greek letter eta, represents the sum of log(1-prob)-log(prop) for each word
		for (int i = 0; i < len; i++) {
			//get WordObject with probability
			WordObject current = getWordProbability(splitStrings[i]);
			
			//check if null, if not, do probability calculations
			if (current != null) {
				double prob = current.getProbability();
				
				//get weight
				double weight;
				if (current.getTotal() >= total/2) {
					weight = WEIGHT_16_PLUS;
				} else if (current.getTotal() > total/4) {
					weight = WEIGHT_8_16;
				} else if (current.getTotal() > total/8) {
					weight = WEIGHT_4_8;
				} else if (current.getTotal() > total/16) {
					weight = WEIGHT_2_4;
				} else if (current.getTotal() > total/32) {
					weight = WEIGHT_1_2;
				} else {
					weight = WEIGHT_0_1;
				}
				
				//get value to add to eta if the word occurs more than 3 times and 
				//its probability is between 0 and 1, exclusive
				double val = 0;
				if (current.getTotal() <= 3 || prob <= 0 || prob >=1) {
					val = Math.log(1-prob) - Math.log(prob);
				}
				
				//add to eta with weight
				eta += val * weight;
			}
		}
		
		
		//do final operations on the sum
		double probability = 1.0/(1.0+Math.exp(eta));
		
		
		//return the probability
		return probability;
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
