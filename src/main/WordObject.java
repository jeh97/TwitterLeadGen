package main;

public class WordObject implements Comparable<WordObject> {
	public static final String LINK = "0Links";
	public static final String HASHTAG = "0Hashtags";
	public static final String AT = "0Ats";
	
	private String word;
	private int relevant;
	private int irrelevant;
	private int total;
	private Double probability;
	
	/**
	 * Creates a new WordObject representing 'word' with no occurrences
	 * @param word Word to create
	 */
	WordObject(String word) {
		this.word = word;
		relevant = 0;
		irrelevant = 0;
		total = 0;
		probability = null;
	}
	
	/**
	 * Creates a new WordObject representing 'word' with the given
	 * occurrence counts
	 * @param word Word to create
	 * @param rel Relevant instances of the word
	 * @param irr Irrelevant instances of the word
	 * @param tot Total instances of the word
	 */
	WordObject(String word, int rel, int irr, int tot) {
		this.word = word;
		relevant = rel;
		irrelevant = irr;
		total = tot;
	}

	/**
	 * Method to get the String representation of the word
	 * @return String of word
	 */
	public String getWord() {return word;}
	
	/**
	 * Method to get the number of relevant occurrences of the word
	 * @return Relevant occurrences of the word
	 */
	public int getRelevant() {return relevant;}
	
	/**
	 * Method to get the number of irrelevant occurrences of the word
	 * @return Irrelevant occurrences of the word
	 */
	public int getIrrelevant() {return irrelevant;}
	
	/**
	 * Method to get the total number of occurrences of the word
	 * @return Total occurrences of the word
	 */
	public int getTotal() {return total;}
	
	/**
	 * Method to increment the number of relevant occurrences and total occurrences
	 * @return The new value of the relevant occurrences
	 */
	public int addRelevant() {relevant++;total++; return relevant;}
	
	/**
	 * Method to increment the number of irrelevant occurrences and total occurrences
	 * @return The new value of the irrelevant occurrences
	 */
	public int addIrrelevant() {irrelevant++; total++; return irrelevant;}
	
	/**
	 * Returns probability
	 * @return word probability, null if none assigned
	 */
	public Double getProbability() {
		return probability;
	}
	
	/**
	 * Sets probability to given value
	 * @param prob new probability
	 */
	public void setProbability(Double prob) {
		probability = prob;
	}
	
	public int compareTo(WordObject o) {
		return word.compareTo(o.getWord());
	}
	
	public boolean equals(Object o) {
		try {
			WordObject other = (WordObject) o;
			return word.equals(other.word);
		} catch (Exception e) {
			return false;
		}
	}
	
	public String toString() {
		return word;
	}
	
}
