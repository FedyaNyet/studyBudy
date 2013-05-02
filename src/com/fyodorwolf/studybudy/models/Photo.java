package com.fyodorwolf.studybudy.models;

public class Photo {
	public int id;
	public String filename;
	public int orderNum;
	
	public Photo(int _id, String filename, int orderNum){
		this.id = _id;
		this.filename = filename;
		this.orderNum = orderNum;
	}
}
