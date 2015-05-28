package com.healthy.logic;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.xmlpull.v1.XmlPullParser;

/**
 * 客户端接收的自定义扩展包
 * */
public class ExtensionalIQProvider implements IQProvider {

	@Override
	public IQ parseIQ(XmlPullParser xpp) throws Exception {
		// TODO Auto-generated method stub
		ExtensionalIQ result = new ExtensionalIQ();
		while (true) {
			int eventType = xpp.next();
			if (eventType == XmlPullParser.START_TAG) {
				if ("message".equals(xpp.getName())) {
					result.setMessage(xpp.nextText());// 注意是nextText();
				}
			} else if (eventType == XmlPullParser.END_TAG) {
				if ("ExtensionalIQ".equals(xpp.getName())) {
					break;
				}
			}
		}
		return result;
	}
}
