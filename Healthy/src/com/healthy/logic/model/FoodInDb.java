package com.healthy.logic.model;

/**
 * 该类对应一条饮食记录
 * */
public class FoodInDb {
	public String name;//食物名称
	public float num;//食物质量
	public float calorie;//食物热量
	public String date;//添加日期，字符串格式为yyyy-MM-dd
	public String time;//添加时间，精确到分钟，格式为HH:mm
}
