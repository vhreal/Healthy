package com.healthy.logic.model;

import android.graphics.Bitmap;

public class RankingBean {
	private int id;
	private Bitmap avatar;
	private String username;
	private String calories;
	
	public RankingBean(){}
	
	public RankingBean (int id, Bitmap avatar, String username, String calories){
		this.id=id;
		this.avatar = avatar;
		this.username = username;
		this.calories = calories;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
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

	public String getCalories() {
		return calories;
	}

	public void setCalories(String calories) {
		this.calories = calories;
	}
	
	
}
