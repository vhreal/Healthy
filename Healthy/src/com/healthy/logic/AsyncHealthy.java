package com.healthy.logic;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.healthy.ui.friends.FriendsHelper;
import com.healthy.ui.friends.FriendsRequestParam;
import com.healthy.ui.friends.FriendsResponseBean;

/**
 * 执行异步任务的类 实现ConnectionListener接口，以便连接异常断开时进行重连
 * 
 * @author knlnzhao
 * 
 * */
public class AsyncHealthy {

	private Executor mPool;

	private FriendsHelper mFriendsHelper = new FriendsHelper();
	private static AsyncHealthy mInstance;

	private AsyncHealthy() {
		mPool = Executors.newFixedThreadPool(5);
	}

	public synchronized static AsyncHealthy getInstance() {
		if (mInstance == null)
			mInstance = new AsyncHealthy();
		return mInstance;
	}

	/**
	 * 实现用户登录
	 * */
	public void login(FriendsRequestParam param,
			RequestListener<FriendsResponseBean> listener) {
		mFriendsHelper.ansynLogin(mPool, param, listener);
	}

	/**
	 * 实现用户注册
	 * */
	public void register(FriendsRequestParam param,
			RequestListener<FriendsResponseBean> listener) {
		mFriendsHelper.ansynRegister(mPool, param, listener);
	}

	/**
	 * 注销用户登录
	 * */
	public void logout(FriendsRequestParam param,
			RequestListener<FriendsResponseBean> listener) {
		mFriendsHelper.ansynLogout(mPool, param, listener);
	}
	
	/**
	 * 实现用户头像上传
	 */
	public void uploadAvatar(FriendsRequestParam param,
			RequestListener<FriendsResponseBean> listener){
		mFriendsHelper.ansynUploadAvatar(mPool, param, listener);
	}
	
	/**
	 * 下载用户头像
	 */
	public void downloadAvatar(FriendsRequestParam param,
			RequestListener<FriendsResponseBean> listener){
		mFriendsHelper.ansynDownloadAvatar(mPool, param, listener);
	}

	/**
	 * 根据当月的热量消耗情况，获取好友排名
	 * */
	public void getFriendsByCalories(FriendsRequestParam param,
			RequestListener<FriendsResponseBean> listener) {
		mFriendsHelper.ansynGetFriendsByCalories(mPool, param, listener);
	}

	/**
	 * 获取附近的人
	 * */
	public void getPersonsNearBy(FriendsRequestParam param,
			RequestListener<FriendsResponseBean> listener) {
		mFriendsHelper.ansyGetPersonsNearby(mPool, param, listener);
	}

	/**
	 * 根据关键字得到好友信息
	 */
	public  void getPersonsByKeyWord(FriendsRequestParam param,
			RequestListener<FriendsResponseBean> listener){
		mFriendsHelper.ansyGetPersonsByKeyWord(mPool, param, listener);
	}
	
	/**
	 * 添加好友信息
	 */
	public void addFriendsRequest(FriendsRequestParam param,
			RequestListener<FriendsResponseBean> listener){
		mFriendsHelper.ansyAddFriendsRequest(mPool, param, listener);
	}
	
	/**
	 * 接受添加好友请求
	 */
	public void acceptFriendsRequest(FriendsRequestParam param,
			RequestListener<FriendsResponseBean> listener){
		mFriendsHelper.ansyAcceptFriendsRequest(mPool, param, listener);
	}
	
	/**
	 * 拒绝添加好友请求
	 */
	public void refuseFriendsRequest(FriendsRequestParam param,
			RequestListener<FriendsResponseBean> listener){
		mFriendsHelper.ansyRefuseFriendsRequest(mPool, param, listener);
	}
	
}
