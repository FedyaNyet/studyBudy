package com.fyodorwolf.studybudy.models;

import java.util.ArrayList;


public class Deck{
	
	public long id;
	public String name;

	public ArrayList<Card> cards = new ArrayList<Card>();
	
	public Deck(long id, String name){
		this.id = id;
		this.name = name;
	}
	
	public ArrayList<Card> getCards(){
		return this.cards;
	}
	
}
