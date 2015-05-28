package com.healthy.logic.model;

import android.graphics.Bitmap;

public class SearchResultBean {
	
	private Bitmap avatar;
	private String username;
	
	public SearchResultBean(){}
	
	public SearchResultBean(String username, Bitmap avatar){
		this.avatar = avatar;
		this.username = username;
	}

	public Bitmap getAvatar() {
		return avatar;
	}

	public void setAvatar(Bitmap avatar) {
		this.avatar = avatar;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
	
	
}
