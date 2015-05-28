package com.healthy.ui.friends;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.Executor;

import org.jivesoftware.smack.XMPPException;

import android.util.Log;

import com.healthy.logic.HealthyApplication;
import com.healthy.logic.RequestListener;
import com.healthy.util.HealthyException;
import com.healthy.util.HealthyUtil;

import static com.healthy.ui.friends.FriendsRequestParam.*;
import static com.healthy.ui.friends.FriendsResponseBean.*;

/**
 * 负责用户注册，登录，注销的账户辅助类
 * 
 * @author knlnzhao
 * */
public class FriendsHelper {

	/**
	 * 用户登录
	 * */
	public void ansynLogin(Executor pool, final FriendsRequestParam param,
			final RequestListener<FriendsResponseBean> listener) {
		ansynExecute(pool, param, listener);
	}

	/**
	 * 用户登出
	 * */
	public void ansynLogout(Executor pool, final FriendsRequestParam param,
			final RequestListener<FriendsResponseBean> listener) {
		ansynExecute(pool, param, listener);
	}

	/**
	 * 用户注册
	 * */
	public void ansynRegister(Executor pool, final FriendsRequestParam param,
			final RequestListener<FriendsResponseBean> listener) {
		ansynExecute(pool, param, listener);
	}
	
	/**
	 * 用户头像上传
	 * @author zc
	 */
	public void ansynUploadAvatar(Executor pool, final FriendsRequestParam param,
			final RequestListener<FriendsResponseBean> listener){
		ansynExecute(pool, param, listener);
	}
	
	/**
	 * 用户头像下载
	 * @author zc
	 */
	public void ansynDownloadAvatar(Executor pool, final FriendsRequestParam param,
			final RequestListener<FriendsResponseBean> listener){
		ansynExecute(pool, param, listener);
	}
	

	/**
	 * 异步获取用户状态列表
	 * */
	public void ansynGetFriendsByCalories(Executor pool,
			final FriendsRequestParam param,
			final RequestListener<FriendsResponseBean> listener) {
		ansynExecute(pool, param, listener);
	}

	/**
	 * 查找附近的人
	 * */
	public void ansyGetPersonsNearby(Executor pool,
			final FriendsRequestParam param,
			final RequestListener<FriendsResponseBean> listener) {
		ansynExecute(pool, param, listener);
	}
	
	/**
	 * 根据关键字查找用户
	 * 
	 */
	public void ansyGetPersonsByKeyWord(Executor pool,
			final FriendsRequestParam param,
			final RequestListener<FriendsResponseBean> listener){
		ansynExecute(pool, param, listener);
	}
	
	/**
	 * 添加好友
	 * @return
	 */
	public void ansyAddFriendsRequest(Executor pool,
			final FriendsRequestParam param,
			final RequestListener<FriendsResponseBean> listener){
		ansynExecute(pool, param, listener);
	}
	
	/**
	 * 接受添加好友请求
	 * @return
	 */
	public void ansyAcceptFriendsRequest(Executor pool,
			final FriendsRequestParam param,
			final RequestListener<FriendsResponseBean> listener){
		ansynExecute(pool, param, listener);
	}
	
	/**
	 * 拒绝添加好友请求
	 * @return
	 */
	public void ansyRefuseFriendsRequest(Executor pool,
			final FriendsRequestParam param,
			final RequestListener<FriendsResponseBean> listener){
		ansynExecute(pool, param, listener);
	}

	public String getLoginedUser() {
		return HealthyUtil.getInstance().getLoginedUser();
	}

	/**
	 * 异步执行
	 * */
	private void ansynExecute(Executor pool, final FriendsRequestParam param,
			final RequestListener<FriendsResponseBean> listener) {
		pool.execute(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				listener.onStart();
				FriendsResponseBean bean = execute(param);
				listener.onComplete(bean);
			}

		});
	}

	/**
	 * 执行任务
	 * */
	public FriendsResponseBean execute(FriendsRequestParam param) {
		Map<String, Object> map = param.getParams();
		FriendsResponseBean bean = new FriendsResponseBean();
		try {
			switch (param.getTaskCategory()) {
			case TASK_LOGIN:// 执行登录操作
				String name = (String) map.get("name");
				String password = (String) map.get("password");
				HealthyUtil.getInstance().login(name, password);
				bean.setResult(SUCCESS);
				bean.setInfo("登录成功");
				break;
			case TASK_REGISTER:// 执行注册操作
				name = (String) map.get("name");
				password = (String) map.get("password");
				HealthyUtil.getInstance().register(name, password);
				bean.setResult(SUCCESS);
				bean.setInfo("注册成功");
				break;
			case TASK_LOGOUT:// 执行登出操作
				HealthyUtil.getInstance().logout();
				bean.setResult(SUCCESS);
				bean.setInfo("登出成功");
				break;
			case TASK_GET_FRIENDS_BY_CALORIES:// 获取用户排名
				int p = (Integer)map.get("p");
				int psize = (Integer)map.get("psize");
				float calories = (Float)map.get("calories");
				HealthyApplication.mRanking = HealthyUtil.getInstance().getFriendsByCalories(p, psize, calories);
				bean.setResult(SUCCESS);
				bean.setInfo("获得排名");
				break;
			case TASK_GET_PERSONS_NEARBY:// 查找附近的人
				int longitude = (Integer)map.get("longitude");
				int latitude = (Integer)map.get("latitude");
				String info = HealthyUtil.getInstance().getPersonsNearby(longitude, latitude, 3000, 0, 20);
				Log.i("tag", info);
				bean.setInfo(info);//后面三个参数可改动
				bean.setResult(SUCCESS);
				break;
			case TASK_UPLOAD_AVATAR://上传选择头像
				InputStream avatarStream = (InputStream)map.get("avatar");
				try {
					HealthyUtil.getInstance().uploadUserAvatra(avatarStream);
				} catch (XMPPException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				bean.setResult(SUCCESS);
				bean.setInfo("上传成功");
				break;
			case TASK_DOWNLOAD_AVATAR://下载用户头像
				name = (String)map.get("username");
				try {
					HealthyApplication.mapAvatar = HealthyUtil.getInstance().getUserAvatar(name);
				} catch (XMPPException e) {
					e.printStackTrace();
				}
				bean.setResult(SUCCESS);
				bean.setInfo("下载成功");
				break;
			case TASK_GET_FRIENDS_BY_KEYWORD://关键字查找
				String keyword = (String)map.get("keyword");
				HealthyApplication.keyResult = HealthyUtil.getInstance().searchUser(keyword);
				bean.setResult(SUCCESS);
				bean.setInfo("查找完毕");
				break;
			case TASK_ADD_FRIENDS://添加好友
				String username = (String)map.get("username");
				HealthyUtil.getInstance().addFriend(username, "");
				bean.setResult(SUCCESS);
				bean.setInfo("好友请求已发送");
				break;
			case TASK_ACCEPT_FRIENDS:
				String acceptname = (String)map.get("username");
				HealthyUtil.getInstance().acceptFriendRequest(acceptname);
				bean.setResult(SUCCESS);
				bean.setInfo("接受好友请求");
				break;
			case TASK_REFUSE_FRIENDS:
				String refusename = (String)map.get("username");
				HealthyUtil.getInstance().rejectFriendRequest(refusename);
				bean.setResult(SUCCESS);
				bean.setInfo("拒绝好友请求");
				break;
			}
			
		} catch (HealthyException e) {
			bean.setResult(ERROR);
			bean.setInfo(e.getMessage());
		}
		return bean;
	}

}
