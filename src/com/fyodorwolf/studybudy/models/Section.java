package com.fyodorwolf.studybudy.models;

import java.util.ArrayList;
import java.util.HashMap;

public class Section{
	public long id;
	public String name;
	public ArrayList<Deck> decks = new ArrayList<Deck>();
	
	private HashMap<Long,Deck> deckIds = new HashMap<Long,Deck>();
	
	public Section(long id, String name) {
		this.id = id;
		this.name = name;
	}

	public void addDeck(Deck deck) {
		if(deckIds.get(deck.id) == null){
			decks.add(deck);
			deckIds.put(deck.id, decks.get(decks.size() - 1));
		}
	}
	
	public Deck getDeckById(long deckId){
		return deckIds.get(deckId);
	}
}