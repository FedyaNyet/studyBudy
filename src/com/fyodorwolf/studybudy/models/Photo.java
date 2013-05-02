package com.fyodorwolf.studybudy.models;

public class Photo {
	public int _id;
	public String filename;
	public int orderNum;
	
	public Photo(int _id, String filename, int orderNum){
		this._id = _id;
		this.filename = filename;
		this.orderNum = orderNum;
	}
}
