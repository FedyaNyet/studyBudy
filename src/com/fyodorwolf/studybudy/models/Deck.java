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
	
	public Card getCardWithId(long _id){
		//just assume no one will mess with it.. (VERY BAD PROGRAMMING!)
		Card myCard = new Card(_id, "", "", 0, 0);
		for(Card card : cards){
			if(card.id == _id){
				myCard = card;
			}
		}
		return myCard;
	}
}
