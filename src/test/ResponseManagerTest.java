package test;

import org.junit.Test;

import main.ResponseManager;

import static org.junit.Assert.*;
import java.util.ArrayList;

public class ResponseManagerTest {
	ResponseManager manager = ResponseManager.getInstance();
	@Test
	public void testCheckNewResponses(){
		
	}
	
	@Test
	public void testIsValidResponse() {
		
	}
	
	@Test
	public void testIsValidSubject() {
		String subject = "Re: TwitterLeadGen: Daily digest #004  5 Jul, 2016";
		assertTrue(manager.isValidSubject(subject));
		subject = "Re: TwitterLeadGen: Daily digest #04  5 Jul, 2016";
		assertFalse(manager.isValidSubject(subject));
				
	}
	
	@Test
	public void testIsValidFormat() {
		String body = "1. I\n2. I\n3. I\n4. I\n5. I\n\n\n" 
				+ "> On Jul 5, 2016, at 4:40 PM, tr.twitterlg@gmail.com wrote:\n>\n"
				+ "> TwitterLeadGen: Daily digest #006\n>\n"
				+ "> 1. fix your app @NewYorker -- and at the very least tweet us to tell us you're working on it. denied access to the new issue. #techwoes\n"
				+ ">               -@simplepretty\n>\n"
				+ "> 2. RT @LanghamCreekCCS: Class of 2017: working on your Common App this summer? No worries, your info will rollover when new cycle opens... htt…\n"
				+ ">               -@langhamcreekhs\n>\n"
				+ "> 3. RT @Traveltoghana: We are working so hard to give you the new experience of hospitality.. #TraveltoGhana #Android #App #ExploreGhana https:…\n"
				+ ">               -@AbeikuSantana\n>\n"
				+ "> 4. We are working so hard to give you the new experience of hospitality.. #TraveltoGhana #Android #App #ExploreGhana https://t.co/GGYaSygXbt\n"
				+ ">               -@Traveltoghana\n>\n"
				+ "> 5. PayPal is apparently working on a new app for Windows Phone [Update: Maybe not] - - https://t.co/g4i1BmiuUb\n"
				+ ">               -@esteban27\n>\n"
				+ "> Tweet IDs:\n"
				+ "> 1. <<00750307346744766464>>\n> 2. <<00750305917086736384>>\n> 3. <<00750265367033937920>>\n"
				+ "> 4. <<00750262938989170688>>\n> 5. <<00750261490154807297>>\n";
		
		assertTrue(manager.isValidFormat(body, 6));
		
		body = "1. I\n2. I\n3. I\n4. I\n\n\n\n" 
				+ "> On Jul 5, 2016, at 4:40 PM, tr.twitterlg@gmail.com wrote:\n>\n"
				+ "> TwitterLeadGen: Daily digest #006\n>\n"
				+ "> 1. fix your app @NewYorker -- and at the very least tweet us to tell us you're working on it. denied access to the new issue. #techwoes\n"
				+ ">               -@simplepretty\n>\n"
				+ "> 2. RT @LanghamCreekCCS: Class of 2017: working on your Common App this summer? No worries, your info will rollover when new cycle opens... htt…\n"
				+ ">               -@langhamcreekhs\n>\n"
				+ "> 3. RT @Traveltoghana: We are working so hard to give you the new experience of hospitality.. #TraveltoGhana #Android #App #ExploreGhana https:…\n"
				+ ">               -@AbeikuSantana\n>\n"
				+ "> 4. We are working so hard to give you the new experience of hospitality.. #TraveltoGhana #Android #App #ExploreGhana https://t.co/GGYaSygXbt\n"
				+ ">               -@Traveltoghana\n>\n"
				+ "> 5. PayPal is apparently working on a new app for Windows Phone [Update: Maybe not] - - https://t.co/g4i1BmiuUb\n"
				+ ">               -@esteban27\n>\n"
				+ "> Tweet IDs:\n"
				+ "> 1. <<00750307346744766464>>\n> 2. <<00750305917086736384>>\n> 3. <<00750265367033937920>>\n"
				+ "> 4. <<00750262938989170688>>\n> 5. <<00750261490154807297>>\n";
		assertFalse(manager.isValidFormat(body, 6));
	}
	
	@Test
	public void testProcessMessages() {
		
	}
	
	@Test
	public void testProcessMessage() {
		
	}
	
	@Test
	public void testGetEmailIndex() {
		String subject = "Re: TwitterLeadGen: Daily digest #006 5 Jul, 2016";
		assertEquals(6,manager.getEmailIndex(subject));
	}
	
	@Test
	public void testGetEmailTweetIDs() {
		String body = "1. I\n2. I\n3. I\n4. I\n5. I\n\n\n" 
				+ "> On Jul 5, 2016, at 4:40 PM, tr.twitterlg@gmail.com wrote:\n>\n"
				+ "> TwitterLeadGen: Daily digest #006\n>\n"
				+ "> 1. fix your app @NewYorker -- and at the very least tweet us to tell us you're working on it. denied access to the new issue. #techwoes\n"
				+ ">               -@simplepretty\n>\n"
				+ "> 2. RT @LanghamCreekCCS: Class of 2017: working on your Common App this summer? No worries, your info will rollover when new cycle opens... htt…\n"
				+ ">               -@langhamcreekhs\n>\n"
				+ "> 3. RT @Traveltoghana: We are working so hard to give you the new experience of hospitality.. #TraveltoGhana #Android #App #ExploreGhana https:…\n"
				+ ">               -@AbeikuSantana\n>\n"
				+ "> 4. We are working so hard to give you the new experience of hospitality.. #TraveltoGhana #Android #App #ExploreGhana https://t.co/GGYaSygXbt\n"
				+ ">               -@Traveltoghana\n>\n"
				+ "> 5. PayPal is apparently working on a new app for Windows Phone [Update: Maybe not] - - https://t.co/g4i1BmiuUb\n"
				+ ">               -@esteban27\n>\n"
				+ "> Tweet IDs:\n"
				+ "> 1. <<00750307346744766464>>\n> 2. <<00750305917086736384>>\n> 3. <<00750265367033937920>>\n"
				+ "> 4. <<00750262938989170688>>\n> 5. <<00750261490154807297>>\n";
		
		ArrayList<Long> ids = manager.getEmailTweetIDs(body);
		assertEquals(5,ids.size());
	}
	
	@Test
	public void testGetRelevanceInfo() {
		String body = "1. I\n2. I\n3. I\n4. I\n5. I\n\n\n" 
				+ "> On Jul 5, 2016, at 4:40 PM, tr.twitterlg@gmail.com wrote:\n>\n"
				+ "> TwitterLeadGen: Daily digest #006\n>\n"
				+ "> 1. fix your app @NewYorker -- and at the very least tweet us to tell us you're working on it. denied access to the new issue. #techwoes\n"
				+ ">               -@simplepretty\n>\n"
				+ "> 2. RT @LanghamCreekCCS: Class of 2017: working on your Common App this summer? No worries, your info will rollover when new cycle opens... htt…\n"
				+ ">               -@langhamcreekhs\n>\n"
				+ "> 3. RT @Traveltoghana: We are working so hard to give you the new experience of hospitality.. #TraveltoGhana #Android #App #ExploreGhana https:…\n"
				+ ">               -@AbeikuSantana\n>\n"
				+ "> 4. We are working so hard to give you the new experience of hospitality.. #TraveltoGhana #Android #App #ExploreGhana https://t.co/GGYaSygXbt\n"
				+ ">               -@Traveltoghana\n>\n"
				+ "> 5. PayPal is apparently working on a new app for Windows Phone [Update: Maybe not] - - https://t.co/g4i1BmiuUb\n"
				+ ">               -@esteban27\n>\n"
				+ "> Tweet IDs:\n"
				+ "> 1. <<00750307346744766464>>\n> 2. <<00750305917086736384>>\n> 3. <<00750265367033937920>>\n"
				+ "> 4. <<00750262938989170688>>\n> 5. <<00750261490154807297>>\n";
		ArrayList<Boolean> info = manager.getRelevanceInfo(body, 6);
		assertEquals(false,info.get(0));
	}
	
	@Test
	public void testRecordStats() {
		
	}
	
}
