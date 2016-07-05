package util;
import net.didion.jwnl.*;
import net.didion.jwnl.data.*;
import net.didion.jwnl.dictionary.*;

import java.io.*;
import java.util.Vector;
import java.util.Map;
import java.util.HashMap;

public class Stemmer {
	private int MaxWordLength = 50;
	private Dictionary dic;
	private MorphologicalProcessor morph;
	private boolean IsInitialized = false;
	public HashMap<String,String> AllWords = null;
	
	private static Stemmer instance;
	
	public static void main(String [] args) {
		Stemmer stemmer = new Stemmer();
		

		System.out.println(stemmer.stemWordWithWordNet("communities"));
		System.out.println(stemmer.stemWordWithWordNet("thank"));
		System.out.println(stemmer.stemWordWithWordNet("u"));
		System.out.println(stemmer.stemWordWithWordNet("for"));
		System.out.println(stemmer.stemWordWithWordNet("your"));
		System.out.println(stemmer.stemWordWithWordNet("patience"));
		System.out.println(stemmer.stemWordWithWordNet("the"));
		System.out.println(stemmer.stemWordWithWordNet("app"));
		System.out.println(stemmer.stemWordWithWordNet("is"));
		System.out.println(stemmer.stemWordWithWordNet("now"));
		System.out.println(stemmer.stemWordWithWordNet("working"));
		System.out.println(stemmer.stemWordWithWordNet("again"));
		System.out.println(stemmer.stemWordWithWordNet("make"));
		System.out.println(stemmer.stemWordWithWordNet("a"));
		System.out.println(stemmer.stemWordWithWordNet("new"));
		System.out.println(stemmer.stemWordWithWordNet("search"));
		
	}
	
	/**
	 * establishes connection to the WordNet database
	 */
	private Stemmer () {
		AllWords = new HashMap<String,String>();
		try {
			JWNL.initialize(new FileInputStream
					("/Users/Jacob/Documents/Repositories/TwitterLeadGen/jwnl14-rc2/config/file_properties.xml"));
			dic = Dictionary.getInstance();
			morph = dic.getMorphologicalProcessor();
			// ((AbstractCachingDictionary)dic).
			//      setCacheCapacity (10000);
			IsInitialized = true;
		} catch (FileNotFoundException e) {
			System.out.println("Error initializing Stemmer: file_properties.xml not found");
			e.printStackTrace();
		} catch (JWNLException e) {
			System.out.println("Error initializing Stemmer: "+e.toString());
			e.printStackTrace();
		}
	}
	
	public static Stemmer getInstance() {
		if (instance == null) {
			instance = new Stemmer();
		}
		return instance;
	}
	
	public void Unload() {
		dic.close();
		Dictionary.uninstall();
		JWNL.shutdown();
	}
	/**
	 * stems a word with wordnet
	 * @param word word to stem
	 * @return the stemmed word or null if it was not found in wordnet
	 */
	public String stemWordWithWordNet (String word) {
		if (!IsInitialized) {
			return word;
		}
		if (word == null) {return null;}
		if (morph == null) {morph = dic.getMorphologicalProcessor();}
		IndexWord w;
		try {
			w = morph.lookupBaseForm(POS.VERB,word);
			if (w != null) {
				return w.getLemma().toString();
			} 
			w = morph.lookupBaseForm(POS.NOUN, word);
			if (w != null) {
				return w.getLemma().toString();
			}
			w = morph.lookupBaseForm(POS.ADVERB,word);
			if (w != null) {
				return w.getLemma().toString();
			}
		} catch (JWNLException e) {
			
		}
		return word;
	}
	
	public String stem(String word) {
		String stemmedword = (String) AllWords.get(word);
		if (stemmedword != null) {
			return stemmedword;
		}
		stemmedword = stemWordWithWordNet(word);
		if (stemmedword != null) {
			AllWords.put(word, stemmedword);
			return stemmedword;
		}
		AllWords.put(word,word);
		return word;
	}
	
	public Vector<Object> stem(Vector<Object> words) {
		if (!IsInitialized) return words;
		for (int i = 0; i < words.size(); i++) {
			words.set(i, stem((String)words.get(i)));
		}
		return words;
	}
}
