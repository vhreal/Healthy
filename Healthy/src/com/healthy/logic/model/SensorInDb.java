package com.healthy.logic.model;

import java.util.ArrayList;
import java.util.List;

public class SensorInDb{
	
	public List<Double> xAcc = new ArrayList<Double>();
	public List<Double> yAcc = new ArrayList<Double>();
	public List<Double> zAcc = new ArrayList<Double>();

	public List<Double> MagxData = new ArrayList<Double>();
	public List<Double> MagyData = new ArrayList<Double>();
	public List<Double> MagzData = new ArrayList<Double>();
	
	public void clearData(){
		xAcc.clear();
		yAcc.clear();
		zAcc.clear();
		MagxData.clear();
		MagyData.clear();
		MagzData.clear();
	}
	
}
