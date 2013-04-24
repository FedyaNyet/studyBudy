package com.fyodorwolf.studybudy.models;

import android.util.SparseIntArray;

import com.fyodorwolf.studybudy.R;


public class Card{

	public static final int STATUS_NONE = 0;
	public static final int STATUS_CORRECT = 1;
	public static final int STATUS_WRONG = 2;
	
	public SparseIntArray statusResources = new SparseIntArray();
	
	public long id;
	public String question;
	public String answer;
	public int status;
	public int positionInDeck;
	
	public Card(long id){
		this.id = id;                 
		this.question = "";         
		this.answer = "";           
		this.status = 0;              
		this.positionInDeck = 0;  
		statusResources.put(STATUS_NONE, 0);
		statusResources.put(STATUS_CORRECT, R.drawable.correct);
		statusResources.put(STATUS_WRONG, R.drawable.wrong); 
	}
	
	public Card(long id, String question, String answer, int status, int positionInDeck){       
		this(id);
		this.question = question;         
		this.answer = answer;           
		this.status = status;              
		this.positionInDeck = positionInDeck;
	}
	
	public int getResourceStatusImage(){  
		return statusResources.get(this.status);
	}
	
	public String toString(){
		return "Card("+Long.toString(this.id)+", \""+this.question+"\", \""+this.answer+"\", "+Integer.toString(this.status)+", "+Integer.toString(this.positionInDeck)+")";
	}
}
