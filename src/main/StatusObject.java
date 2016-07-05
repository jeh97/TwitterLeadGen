package main;

import twitter4j.Status;

public class StatusObject implements Comparable<StatusObject> {
	private Double probability;
	private Status status;
	
	public StatusObject(Double prob, Status status) {
		probability = prob;
		this.status = status;
	}
	
	public Double getProbability() {
		return probability;
	}
	
	public Status getStatus() {
		return status;
	}
	
	public int compareTo(StatusObject o) {
		return this.probability.compareTo(o.getProbability());
	}
}
