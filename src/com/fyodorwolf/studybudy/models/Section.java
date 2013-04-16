package com.fyodorwolf.studybudy.models;

import java.util.ArrayList;

public class Section{
	public long id;
	public String name;
	public ArrayList<Deck> decks = new ArrayList<Deck>();
	
	public Section(long id, String name) {
		this.id = id;
		this.name = name;
	}
}