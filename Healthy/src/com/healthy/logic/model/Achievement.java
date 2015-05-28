package com.healthy.logic.model;

public class Achievement {
	private String pre;
	private String achievement;
	
	public Achievement(String pre, String achievement)
	{
		this.pre = pre;
		this.achievement = achievement;
	}

	public String getPre() {
		return pre;
	}

	public void setPre(String pre) {
		this.pre = pre;
	}

	public String getAchievement() {
		return achievement;
	}

	public void setAchievement(String achievement) {
		this.achievement = achievement;
	}
	
	
}
