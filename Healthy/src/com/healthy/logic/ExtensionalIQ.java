package com.healthy.logic;

import org.jivesoftware.smack.packet.IQ;

/**
 * 客户端发送的经过扩展的IQ包
 * */
public class ExtensionalIQ extends IQ{
	
	public static final String ELEMENT = "ExtensionalIQ";
	public static final String NAME_SPACE = "custom:iq:healthy";
	private String mMessage;//IQ的内容
	
	public String getMessage(){
		return mMessage;
	}
	
	public void setMessage(String message){
		mMessage=message;
	}

	@Override
	public String getChildElementXML() {
		// TODO Auto-generated method stub
		StringBuilder sb=new StringBuilder();
		sb.append("<").append(ELEMENT).append(" xmlns=\"")
        .append(NAME_SPACE).append("\">");
		sb.append("<message>").append(getMessage()).append("</message>");
		sb.append("</").append(ELEMENT).append(">");
		return sb.toString();
	}

}
