package test;

import org.junit.Test;

import main.DBManager;
import main.WordObject;

import static org.junit.Assert.*;

public class DBManagerTest {
	
	private DBManager db = DBManager.getInstance();
	
	@Test
	public void testHasWord() {
		assertTrue(db.hasWord("a"));
		assertTrue(db.hasWord("accelerate"));
		assertTrue(db.hasWord("medium"));
		assertTrue(db.hasWord("spam"));
		assertTrue(db.hasWord("version"));
		assertTrue(db.hasWord("zurich"));
		assertFalse(db.hasWord("aligator"));
		assertFalse(db.hasWord("mummy"));
		assertFalse(db.hasWord("yodle"));
		assertFalse(db.hasWord("unanimous"));
		
		
	}
	
	@Test
	public void testAddWord() {
		assertTrue(db.addWord("albatross"));
		assertTrue(db.addWord("accelerate"));
	}
	
	@Test
	public void testGetInfoForWord() {
		assertEquals(db.getInfoForWord("a").getRelevant(),54);
		assertEquals(db.getInfoForWord("a").getIrrelevant(),366);
		assertEquals(db.getInfoForWord("a").getTotal(),420);
		assertNull(db.getInfoForWord("aligator"));
	}
	
	@Test
	public void testAddRelevantInstance() {
		int beforeRel, afterRel, beforeTot, afterTot;
		WordObject a = db.getInfoForWord("a");
		beforeRel = a.getRelevant();
		beforeTot = a.getTotal();
		afterRel = db.addRelevantInstance("a");
		afterTot = db.getInfoForWord("a").getTotal();
		assertEquals(beforeRel+1,afterRel);
		assertEquals(beforeTot+1,afterTot);
		assertEquals(db.addRelevantInstance("aligator"),-1);
	}
	
	@Test
	public void testAddIrrelevantInstance() {
		int beforeIrr, afterIrr, beforeTot, afterTot;
		WordObject a = db.getInfoForWord("a");
		beforeIrr = a.getRelevant();
		beforeTot = a.getTotal();
		afterIrr = db.addRelevantInstance("a");
		afterTot = db.getInfoForWord("a").getTotal();
		assertEquals(beforeIrr+1,afterIrr);
		assertEquals(beforeTot+1,afterTot);
		assertEquals(db.addRelevantInstance("aligator"),-1);
	}
	
	@Test
	public void testAddTweet() {
		
	}
	
	@Test
	public void testAddUser() {
		
	}
	
	@Test
	public void testHasUser() {
		
	}
	
	@Test
	public void testSetProbability() {
		
	}
	
	@Test
	public void testHasProbability() {
		
	}
	
	@Test
	public void testHaveSent() {
		
	}
	
	@Test
	public void testSetAsSent() {
		
	}
	
	@Test
	public void testEmailIDExists() {
		
	}
	
	@Test
	public void  testEmailResponseRecorded() {
		
	}
	
	@Test
	public void testGetEmailIndex() {
		
	}
	
	@Test
	public void testGetEmailDate() {
		
	}
	
	@Test
	public void testAddSentEmail() {
		
	}
	
	@Test
	public void testHasTweetWithText() {
		assertTrue(db.hasTweetWithText("fix your app @NewYorker -- and at the very least tweet us to tell us you're working on it. denied access to the new issue. #techwoes"));
	}
	
	
	
	/*public static void main(String [] args) {
		
		testHasWord();
		testAddWord();
	}*/
	
	
	
	
	
	
	
	
	
	
}
