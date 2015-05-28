package com.healthy.logic.model;

/**
 * 响应结果模型
 * */
public class ResponseBean {
	
	public static final int ERROR = 0;// 失败
	public static final int SUCCESS = 1;// 成功

	private int mResult = -1;// 结果代码
	private String mInfo = "暂无相关信息";// 结果信息提示

	public int getResult() {
		return mResult;
	}

	public void setResult(int result) {
		mResult = result;
	}

	public String getInfo() {
		return mInfo;
	}

	public void setInfo(String info) {
		mInfo = info;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return mInfo;
	}
}
