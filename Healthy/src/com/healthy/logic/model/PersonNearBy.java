package com.healthy.logic.model;

import android.graphics.Bitmap;

public class PersonNearBy {
	
	private String username;
	private int longitude;
	private int latitude;
	private String lastUpdateTime;
	private String address;
	private Bitmap avatar;
	
	public PersonNearBy(){}
	
	public PersonNearBy(String username, int longitude, int latitude, String lastUpdateTime, String address, Bitmap avatar) {
		this.username = username;
		this.longitude = longitude;
		this.latitude = latitude;
		this.lastUpdateTime = lastUpdateTime;
		this.address = address;
		this.avatar = avatar;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public int getLongitude() {
		return longitude;
	}

	public void setLongitude(int longitude) {
		this.longitude = longitude;
	}

	public int getLatitude() {
		return latitude;
	}

	public void setLatitude(int latitude) {
		this.latitude = latitude;
	}

	public String getLastUpdateTime() {
		return lastUpdateTime;
	}

	public void setLastUpdateTime(String lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}

	public Bitmap getAvatar() {
		return avatar;
	}

	public void setAvatar(Bitmap avatar) {
		this.avatar = avatar;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}
	
	
}
