package edu.cmu.sphinx.sample.dialog.lt.dto;

public class BehaviorContext {
	private String code;
	private String year;
	
	private String previousStatement;
	private String previousState;
	private String nextState;
	
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getYear() {
		return year;
	}
	public void setYear(String year) {
		this.year = year;
	}
	public String getPreviousStatement() {
		return previousStatement;
	}
	public void setPreviousStatement(String previousStatement) {
		this.previousStatement = previousStatement;
	}
	public void setPreviousState(String previousState) {
		this.previousState = previousState; 
	}
	public void setNextState(String nextState) {
		this.nextState = nextState; 
	}
	public String getPreviousState() {
		return previousState;
	}
	public String getNextState() {
		return nextState;
	}

}
