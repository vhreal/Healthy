package com.healthy.logic.model;

public class TrackerListBean {

	private int id;
	private String userId;
	private String time;
	private String type;
	private String distance;
	private String location;
	
	public TrackerListBean (){}
	public TrackerListBean (int id){
		this.id =id;
	}
	
	public TrackerListBean(int id,String time, String type, String distance, String location)
	{
		this.id = id;
		this.time=time;
		this.type = type;
		this.distance = distance;
		this.location = location;
	}
	public TrackerListBean(int id, String userId, String time, String type, String distance, String location)
	{
		this.id=id;
		this.userId=userId;
		this.time=time;
		this.type=type;
		this.distance=distance;
		this.location=location;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDistance() {
		return distance;
	}

	public void setDistance(String distance) {
		this.distance = distance;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

}
