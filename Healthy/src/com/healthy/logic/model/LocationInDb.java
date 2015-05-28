package com.healthy.logic.model;

public class LocationInDb {

	private int id;
	private double longitude;
	private double latitude;
	private String time;
	
	public LocationInDb(){}
	
	public LocationInDb(int id, double longitude, double latitude, String time)
	{
		this.id=id;
		this.longitude=longitude;
		this.latitude=latitude;
		this.time=time;
	}
	

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	
}
