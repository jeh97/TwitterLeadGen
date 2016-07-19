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
import java.lang.Math;

public class RelevanceFilter {
	
	public static final double WEIGHT_0_1 = .25;
	public static final double WEIGHT_1_3 = 1.8;
	public static final double WEIGHT_3_7 = 2.0;
	public static final double WEIGHT_7_15 = 1.8;
	public static final double WEIGHT_15_PLUS = 1.3;
	
	public boolean VERBOSE = false;
	
	private Map<String,WordObject> words;
	private int relTweets,   irrTweets,   totTweets;
	private int relAts,      irrAts,      totAts;
	private int relLinks,    irrLinks,    totLinks;
	private int relHashtags, irrHashtags, totHashtags;
	private ArrayList<String> stopWords;
	private String[] stopWordsArray = {"be","on","rt","a","the","and","but","to","for","of","of","in","with","i","this","it","out","now"};
	public RelevanceFilter() {
		words = new TreeMap<String,WordObject>();
		stopWords = new ArrayList<String>(Arrays.asList(stopWordsArray));
		String filename = "/Users/Jacob/Documents/Repositories/TwitterLeadGen/Excel/Stats.xls";
		File statsFile = new File(filename);
		Workbook statsWB = null;
		try {
			statsWB = Workbook.getWorkbook(statsFile);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		Sheet statsSheet = statsWB.getSheet(0);
		System.out.println(statsSheet.getCell(1,1).getType());
		relTweets = (int)((NumberCell)(statsSheet.getCell(1,1))).getValue();
		irrTweets = (int)((NumberCell)statsSheet.getCell(2,1)).getValue();
		totTweets = (int)((NumberCell)statsSheet.getCell(3,1)).getValue();
		

		relLinks = (int)((NumberCell)statsSheet.getCell(1,2)).getValue();
		irrLinks = (int)((NumberCell)statsSheet.getCell(2,2)).getValue();
		totLinks = (int)((NumberCell)statsSheet.getCell(3,2)).getValue();
		words.put(WordObject.LINK,new WordObject(WordObject.LINK,relLinks,irrLinks,totLinks));

		relHashtags = (int)((NumberCell)statsSheet.getCell(1,3)).getValue();
		irrHashtags = (int)((NumberCell)statsSheet.getCell(2,3)).getValue();
		totHashtags = (int)((NumberCell)statsSheet.getCell(3,3)).getValue();
		words.put(WordObject.HASHTAG,new WordObject(WordObject.HASHTAG,relHashtags,irrHashtags,totHashtags));

		relAts = (int)((NumberCell)statsSheet.getCell(1,4)).getValue();
		irrAts = (int)((NumberCell)statsSheet.getCell(2,4)).getValue();
		totAts = (int)((NumberCell)statsSheet.getCell(3,4)).getValue();
		words.put(WordObject.AT,new WordObject(WordObject.AT,relAts,irrAts,totAts));
		
		int totalWords = (int)((NumberCell)statsSheet.getCell(3,8)).getValue();
		
		for (int row = 10; row < totalWords+10; row++) {
			String word = statsSheet.getCell(0,row).getContents();
			int rel = (int)((NumberCell)statsSheet.getCell(1,row)).getValue();
			int irr = (int)((NumberCell)statsSheet.getCell(2,row)).getValue();
			int tot = (int)((NumberCell)statsSheet.getCell(3,row)).getValue();
			if (VERBOSE) 
				System.out.printf("Adding word [%-14s] with rel [%3d] irr [%3d] and tot [%3d]\n", word,rel,irr,tot);
			WordObject newWord = new WordObject(word,rel,irr,tot);
			words.put(word, newWord);
		}
	}
	
	public Map<Double,Status> getQueryResultProbabilities(ArrayList<Status> tweets) {
		HashMap<Double,Status> probs = new HashMap<Double,Status>();
		
		Stemmer stemmer = new Stemmer();
		
		String text;
		
		if (VERBOSE) 
			System.out.printf("There are %d tweets\n",tweets.size());
		
		int tweetCount = 0;
		for (Status status : tweets) {
			tweetCount++;
			if (VERBOSE) 
				System.out.printf("On tweet %d\n", tweetCount);
			boolean hasLink = false;
			boolean hasAt = false;
			boolean hasHash = false;
			
			text = status.getText().toLowerCase();
			String[] splitStrings = text.split(" ");
			int len = splitStrings.length;
			Map<WordObject,Double> wordProbabilities = new TreeMap<WordObject,Double>();
			
			if (VERBOSE) 
				System.out.printf("\t<<%s>>\n", text);
			
			for (int c = 0; c < len; c++) {
				String current = splitStrings[c];;
				if (current.indexOf("http")!=-1) {
					if (VERBOSE)
						System.out.println("\tHas Link");
					hasLink = true;
				} else if (current.indexOf('@') != -1) {
					if (VERBOSE)
						System.out.println("\tHas At");
					hasAt = true;
				} else if (current.indexOf('#') != -1) {
					if (VERBOSE)
						System.out.println("\tHas Hashtag");
					hasHash = true;
				} else {
					current = current.replaceAll("[^A-Za-z]+", "");
					current = stemmer.StemWordWithWordNet(current);
					if (current != null && current.length() > 0) {
						if (VERBOSE)
							System.out.printf("\tlooking at word: %s\n", current);
						WordObject curWord = words.get(current);
						if ((stopWords.indexOf(current) != -1 ) && !wordProbabilities.containsKey(curWord)) {
							if (VERBOSE)
								System.out.println("\t\tFirst occurance of WORD");
							if (curWord != null) {
								if (VERBOSE)
									System.out.println("\t\tWord in stats page");
								int relTweetsWithCurrent = curWord.getRel();
								int irrTweetsWithCurrent = curWord.getIrr();
								int totTweetsWithCurrent = curWord.getTotal();
								if (VERBOSE)
									System.out.printf("\t\t\tRelevant: %3d   Irrelevant: %3d\n", relTweetsWithCurrent,irrTweetsWithCurrent);
								if (totTweetsWithCurrent>=3)  {
									double probability = irrTweetsWithCurrent/(double)totTweetsWithCurrent;
									if (probability > 0) {
										wordProbabilities.put(curWord,Double.valueOf(probability));
										if (VERBOSE)
											System.out.printf("\t\t\tProbability = %f\n", probability);	
									} else {
										if (VERBOSE)
											System.out.println("\t\t\tIgnored 0 probability");
									}
								} else {
									if (VERBOSE)
										System.out.println("\t\t\tNot enough occurances to dermine probability");
								}
							} else {
								if (VERBOSE)
									System.out.println("\t\tWord not in stats page");
							}
						} else {
							if (VERBOSE)
								System.out.println("\t\tAlready contains word");
						}
					} else {
						if (VERBOSE)
							System.out.printf("\tNOT looking at word: %s\n", current);
					}
				}
			}
			
			if (hasLink) {
				double probability = irrLinks/(double)totLinks;
				wordProbabilities.put(words.get(WordObject.LINK),Double.valueOf(probability));
			} else if (hasAt) {
				double probability = irrAts/(double)totAts;
				wordProbabilities.put(words.get(WordObject.AT),Double.valueOf(probability));
			} else if (hasHash) {
				double probability = irrHashtags/(double)totHashtags;
				wordProbabilities.put(words.get(WordObject.HASHTAG),Double.valueOf(probability));
			}
			
			
			Double sum = Double.valueOf(0.0);
			int i= 0;
			for (Map.Entry<WordObject, Double> entry : wordProbabilities.entrySet()) {
				Double cur = entry.getValue();
				int dataSize = entry.getKey().getTotal();
				double multiplier = 1;
				if (dataSize>=totTweets*15.0/31.0) {
					multiplier = WEIGHT_15_PLUS;
				} else if (dataSize>=totTweets*7.0/31.0) {
					multiplier = WEIGHT_7_15;
				} else if (dataSize>=totTweets*3.0/31.0) {
					multiplier = WEIGHT_3_7;
				} else if (dataSize>=totTweets/31.0) {
					multiplier = WEIGHT_1_3;
				} else {
					multiplier = WEIGHT_0_1;
				}
				if (VERBOSE)
					System.out.printf("Probability %3d: %f\n", i,cur);
				Double log1 = Math.log(1.0 - cur);
				Double log2 = Math.log(cur);
				sum += (log1-log2)*multiplier;
				if (VERBOSE)
					System.out.printf("log1 = %6.4f, log2 = %6.4f, sum = %6.4f\n", log1,log2,sum);
				i++;
			}
			Double step1 = Math.exp(sum);
			Double step2 = 1.0+step1;
			Double pFinal = 1.0/step2;
			if (VERBOSE)
				System.out.printf("step1 = %6.4f, step2 = %6.4f, pFinal = %6.4f\n",step1,step2,pFinal);
			probs.put(pFinal,status);
			
			
		}
		
		return probs;
	}
	
	public static ArrayList<Status> getQuerys(Twitter twitter, String queryString, int numberOfTweets) {
		Query query = new Query(queryString);
		long lastID = Long.MAX_VALUE;
		int lastTweetsSize = 0;
		int pagesSinceNewTweetsFound = 0;
		ArrayList<Status> tweets = new ArrayList<Status>();
		while ((tweets.size() < numberOfTweets) && (pagesSinceNewTweetsFound < 3)) {
			if (numberOfTweets - tweets.size() > 100) {
				query.setCount(100);
			} else {
				query.setCount(numberOfTweets - tweets.size());
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
		return tweets;
	}
	
	
	
	public static void main(String [] args) throws TwitterException {
		TwitterPortal portal = new TwitterPortal();
		Twitter twitter = portal.getTwitter();
		
		RelevanceFilter filter = new RelevanceFilter();
		
		ArrayList<Status> result = getQuerys(twitter,"working app new",400);
		TreeMap<Double,Status> probs = new TreeMap<Double,Status>(filter.getQueryResultProbabilities(result));
		System.out.printf("%d tweets with probabilities\n", probs.size());
		for (Map.Entry<Double, Status> entry : probs.entrySet()) {
			System.out.printf("%05.2f probability that <%s> is NOT relevant\n", entry.getKey()*100,entry.getValue().getText());
		}
		
	}
}
