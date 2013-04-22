package com.fyodorwolf.studybudy.models;

public class Card{

	public static final int STATUS_NONE = 0;
	public static final int STATUS_CORRECT = 1;
	public static final int STATUS_WRONG = 2;
	
	public long id;
	public String question;
	public String answer;
	public int status;
	public int positionInDeck;
	
	public Card(long id, String question, String answer, int status, int positionInDeck){
		this.id = id;                 
		this.question = question;         
		this.answer = answer;           
		this.status = status;              
		this.positionInDeck = positionInDeck;      
	}
}
