package main;

public class MessageObject {
	private String text;
	private Long index;
	private int numberOfTweets;
	private String subject;
	
	public MessageObject(String subject, String text, Long index, int numberOfTweets) {
		this.subject = subject;
		this.text = text;
		this.index = index;
		this.numberOfTweets = numberOfTweets;
	}
	
	public MessageObject(String subject, String text) {
		this.subject = subject;
		this.text = text;
		this.index = (long) (-1);
		this.numberOfTweets = -1;
	}
	
	public String getSubject() {
		return subject;
	}
	
	public String getText() {
		return text;
	}
	
	public Long getIndex() {
		return index;
	}
	
	public int getNumberOfTweets() {
		return numberOfTweets;
	}
	
	public void setNumberOfTweets(int num) {
		numberOfTweets = num;
	}
	
}
