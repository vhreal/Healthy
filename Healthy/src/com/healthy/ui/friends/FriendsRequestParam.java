package com.healthy.ui.friends;

import java.util.HashMap;
import java.util.Map;

import com.healthy.logic.RequestParam;

public class FriendsRequestParam extends RequestParam{
	
	public static final int TASK_REGISTER=0;//执行注册任务
	public static final int TASK_LOGIN=1;//执行登录任务
	public static final int TASK_LOGOUT=2;//用户登出
	public static final int TASK_GET_FRIENDS_BY_CALORIES=3;//获取好友排名
	public static final int TASK_GET_PERSONS_NEARBY=4;//查看周围的人
	public static final int TASK_UPLOAD_AVATAR=5;//上传头像
	public static final int TASK_DOWNLOAD_AVATAR=6;//下载头像
	public static final int TASK_GET_FRIENDS_BY_KEYWORD=7;//通过关键字获取好友
	public static final int TASK_ADD_FRIENDS=8;//发送添加好友请求
	public static final int TASK_ACCEPT_FRIENDS=9;//接受好友请求
	public static final int TASK_REFUSE_FRIENDS=10;//拒绝好友请求
	
	private Map<String, Object> mMap=new HashMap<String,Object>();
	private int mTaskCategory=-1;//执行任务的种类
	
	public FriendsRequestParam(){}
	
	public FriendsRequestParam(int taskCategory){
		mTaskCategory=taskCategory;
	}
			
	@Override
	public Map<String, Object> getParams() {
		// TODO Auto-generated method stub
		return mMap;
	}
	
	/**
	 * 添加参量
	 * @param key 参量key
	 * @param value 参量值
	 * */
	public void addParam(String key, Object value){
		mMap.put(key,value);
	}
	
	/**
	 * 设置任务类型
	 * @param taskCategory 任务类型 登录或者注册
	 * */
	public void setTaskCategory(int taskCategory){
		mTaskCategory=taskCategory;
	}
	
	public int getTaskCategory(){
		return mTaskCategory;
	}

}
