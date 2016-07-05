package test;

import org.junit.Test;

import main.DBManager;

import static org.junit.Assert.*;

public class MyUnitTest {
	@Test
	public void testHasWord() {
		DBManager db = DBManager.getInstance();
		
		assertTrue(db.hasWord("a"));
		assertTrue(db.hasWord("accelerate"));
		assertTrue(db.hasWord("medium"));
		assertTrue(db.hasWord("spam"));
		assertTrue(db.hasWord("version"));
		assertTrue(db.hasWord("zurich"));
		assertFalse(db.hasWord("albatross"));
		assertFalse(db.hasWord("mummy"));
		assertFalse(db.hasWord("yodle"));
		assertFalse(db.hasWord("unanimous"));
		
		
	}
	
	@Test
	public void testAddWord() {
		DBManager db DBManager.getInstance();
		
		assertTrue()
	}
}
