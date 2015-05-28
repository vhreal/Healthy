package com.healthy.logic.model;

public class Introduce {
	
	private String name;
	private String describe;
	
	public Introduce (String name, String describe){
		this.name = name;
		this.describe = describe;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescribe() {
		return describe;
	}

	public void setDescribe(String describe) {
		this.describe = describe;
	}
	
	
}
