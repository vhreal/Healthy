package com.healthy.ui.foods;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

import com.healthy.logic.model.FoodInDb;

public class FoodXMLHandler extends DefaultHandler {
	private List<FoodInDb> mFoodList;
	private boolean inFood=false;
	private boolean inName = false;
	private boolean inCalorie = false;
	private FoodInDb curFoodData;
	
	private String tagName;
	
	public FoodXMLHandler(){
		mFoodList = new ArrayList<FoodInDb>();
	}
	
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		// TODO 自动生成的方法存根
		super.startElement(uri, localName, qName, attributes);
		tagName = localName.length()!=0?localName:qName;
		tagName = tagName.toLowerCase().trim();
		//判断是否在food标签内
		if(tagName.equals("food")){
			inFood = true;
			curFoodData = new FoodInDb();
		}
		//判断food内的子标签
		if(inFood){
			if(tagName.equals("name")){
				inName = true;
			}else if(tagName.equals("calorie")){
				inCalorie = true;
			}
		}
		Log.i("uri", uri);
		
	}
	
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		// TODO 自动生成的方法存根
		super.endElement(uri, localName, qName);
		tagName = localName.length()!=0?localName:qName;
		tagName = tagName.toLowerCase().trim();
		
		if(tagName.equals("food")){
			inFood = false;
			mFoodList.add(curFoodData);
			Log.i("tagfood", curFoodData.name+"@"+curFoodData.calorie);
		}
		
		if(inFood){
			if(tagName.equals("name")){
				inName = false;
			}else if(tagName.equals("calorie")){
				inCalorie = false;
			}
		}
	}
	
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		// TODO 自动生成的方法存根
		super.characters(ch, start, length);
		
		if(inName){
			curFoodData.name =new String(ch,start,length);
		}else if(inCalorie){
			curFoodData.calorie = Float.valueOf(new String(ch,start,length));
		}
	}
	
	public List<FoodInDb> getFoodList(){
		Log.i("tagfood",mFoodList.size()+"");
		return mFoodList;
	}

}
