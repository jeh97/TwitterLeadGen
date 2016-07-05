package test;

import org.junit.Test;

import main.RelevanceFilter;

import static org.junit.Assert.*;

public class RelevanceFilterTest {
	RelevanceFilter filter = RelevanceFilter.getInstance();
	
	@Test
	public void testStemString() {
		assertEquals(filter.stemString("https://bit.ly/1234"),"0Links");
		assertEquals("run",filter.stemString("ran"));
		assertEquals(filter.stemString("#TapReason"),"0Hashtags");
		assertEquals(filter.stemString("@TapReason"),"0Ats");
	}
	
	@Test
	public void testGetWordProbability() {

		assertEquals(filter.getWordProbability("announce").getProbability(),Double.valueOf(66.0/68));
		assertNull(filter.getWordProbability("a"));
		assertEquals(filter.getWordProbability("http://bit.ly/1234").getProbability(),Double.valueOf(1280.0/1356.0));
	}
	
	@Test
	public void testGetProbability() {
		Double value = Double.valueOf(filter.getProbability("an act of acclaim"));
		assertEquals(0.9913,0.value,0.0001);
		
		//error = (Double.valueOf(filter.getProbability("And it’s working. New York’s newest ride-hail app is "
		//		+ "everything drivers have ever wante… https://t.co/Ah1EOmvGpP https://t.co/OoOVIq4wKI"));
		
	}
}
