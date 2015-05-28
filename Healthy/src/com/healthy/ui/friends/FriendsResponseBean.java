package com.healthy.ui.friends;

import com.healthy.logic.model.ResponseBean;

public class FriendsResponseBean extends ResponseBean{

	private String mMessage;//½á¹ûÄÚÈİ

	public void setMessage(String message){
		mMessage=message;
	}
	
	public String getMessage(){
		return mMessage;
	}
	
}
