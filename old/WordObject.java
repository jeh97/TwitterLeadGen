
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class WordObject implements Comparable<WordObject>{
	
	public static final String LINK  = "0Links";
	public static final String HASHTAG = "0Hashtags";
	public static final String AT = "0Ats";
	
	private String word;
	private int relevant;
	private int irrelevant;
	private int total;
	
	WordObject(String word) {
		this.word = word;
		relevant = 0;
		irrelevant = 0;
		total = 0;
	}
	
	WordObject(String word, int rel, int irr, int tot) {
		this.word = word;
		relevant = rel;
		irrelevant = irr;
		total = tot;
	}
	public String getWord() {return word;}
	public int getRel() {return relevant;}
	public int getIrr() {return irrelevant;}
	public int getTotal() {return total;}
	
	public void addRel() {relevant++;total++;}
	public void addIrr() {irrelevant++; total++;}
	
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
