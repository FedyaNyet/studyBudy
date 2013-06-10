package com.fyodorwolf.studybudy.models;

import java.io.File;

public class Photo extends File {
	
	private static final long serialVersionUID = 1L;
	public int id;
	public String filename;
	public int orderNum;

	public Photo(int _id, String filename, int orderNum){
		super(filename);
		this.id = _id;
		this.filename = filename;
		this.orderNum = orderNum;
	}
	
	public Photo(String filename){
		super(filename);
		this.filename = filename;
		this.orderNum = Integer.parseInt(filename.substring(0, filename.lastIndexOf(".")));
	}
}
