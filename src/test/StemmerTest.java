package test;

import org.junit.Test;

import util.Stemmer;
import static org.junit.Assert.*;

public class StemmerTest {
	Stemmer stemmer = Stemmer.getInstance();
	
	@Test
	public void testStem() {
		assertEquals(stemmer.stem("communities"),"community");
		assertEquals(stemmer.stem("communities"),"community");
		assertEquals(stemmer.stem("communities"),"community");
		assertEquals(stemmer.stem("communities"),"community");
		
	}
	
	
	
}
