package com.healthy.ui.menupanel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.healthy.R;
import com.healthy.logic.HealthyApplication;
import com.healthy.logic.RequestListener;
import com.healthy.ui.base.ViewCategories;
import com.healthy.ui.friends.FriendsRequestParam;
import com.healthy.ui.friends.FriendsResponseBean;
import com.healthy.util.HealthyUtil;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.Toast;

/** 从左侧滑出的主菜单 */
public class MenuPanel {
	
	public static boolean hasNewMessage = false;

	// private HealthyApplication mApplication;
	private Context mContext;
	private View mMenuPanel;// 菜单面板视图
	public static MenuPanelAdapter mAdapter;
	public static int mChooesId = 0;// 选择的菜单项目的id
	private ExpandableListView mMenu;
	private List<Map<String, Object>> mGroup = new ArrayList<Map<String, Object>>();// 菜单组
	private List<List<Map<String, Object>>> mChild = new ArrayList<List<Map<String, Object>>>();// 子菜单

	private String[] mGroupName;
	private String[] mChildBrowse;
	private String[] mChildOptions;
	private String[] mChildAbout;

	private int[] mChildBrowseIcon = { R.drawable.ic_default,
			R.drawable.ic_default, R.drawable.ic_default,
			R.drawable.ic_default, R.drawable.ic_default };
	private int[] mChildOptionsIcon = { R.drawable.ic_default,
			R.drawable.ic_default };
	private int[] mChildAboutIcon = { R.drawable.ic_default,
			R.drawable.ic_default, R.drawable.ic_default, R.drawable.ic_default };

	private onChangeViewListener mOnChangeViewListener;

	private ProgressDialog mWaitDialog;

	public MenuPanel(Context context) {
		// mApplication = application;
		mContext = context;
		mMenuPanel = LayoutInflater.from(context).inflate(R.layout.menupanel,
				null);
		mMenu = (ExpandableListView) mMenuPanel
				.findViewById(R.id.menupanel_list);

		mWaitDialog = new ProgressDialog(mContext);
		mWaitDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mWaitDialog.setMessage("正在登出...");
		mWaitDialog.setIndeterminate(true);
		mWaitDialog.setCancelable(false);

		init();
		setListener();
		mMenuPanel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				System.out.println("被点击");
			}
		});
	}

	private void init() {
		initData();
		mAdapter = new MenuPanelAdapter(mContext, mGroup, mChild);
		mMenu.setAdapter(mAdapter);
		for (int i = 0; i < mGroup.size(); i++) {
			mMenu.expandGroup(i);
		}
	}
	
	private void initData() {

		mGroupName = mContext.getResources().getStringArray(
				R.array.menupanel_group_names);
		mChildBrowse = mContext.getResources().getStringArray(
				R.array.menupanel_browse_child_names);
		mChildOptions = mContext.getResources().getStringArray(
				R.array.menupanel_options_child_names);
		mChildAbout = mContext.getResources().getStringArray(
				R.array.menupanel_about_child_names);

		getGroupList();
		getChildList();
	}

	private void getGroupList() {
		for (int i = 0; i < mGroupName.length; i++) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("name", mGroupName[i]);
			mGroup.add(map);
		}
	}

	private void getChildList() {

		for (int i = 0; i < mGroupName.length; i++) {
			if (i == 0) {// 浏览
				List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
				for (int j = 0; j < mChildBrowse.length; j++) {
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("icon", mChildBrowseIcon[j]);
					map.put("name", mChildBrowse[j]);
					map.put("click", false);
					list.add(map);
				}
				mChild.add(list);
			} else if (i == 1) {// 操作
				List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
				for (int j = 0; j < mChildOptions.length; j++) {
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("icon", mChildOptionsIcon[j]);
					map.put("name", mChildOptions[j]);
					map.put("click", false);
					list.add(map);
				}
				mChild.add(list);
			} else if (i == 2) {// 关于
				List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
				for (int j = 0; j < mChildAbout.length; j++) {
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("icon", mChildAboutIcon[j]);
					map.put("name", mChildAbout[j]);
					map.put("click", false);
					list.add(map);
				}
				mChild.add(list);
			}
		}

		// 默认选择常用组第一项
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("icon", mChildBrowseIcon[0]);
		map.put("name", mChildBrowse[0]);
		map.put("click", true);
		mChild.get(0).set(0, map);
	}

	private void setListener() {

		mMenu.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {

			@Override
			public boolean onGroupClick(ExpandableListView parent, View v,
					int groupPosition, long id) {
				// TODO Auto-generated method stub
				return true;
			}
		});

		mMenu.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				// TODO Auto-generated method stub
				if (groupPosition == 0) {// 点击第一组菜单
					mChooesId = childPosition;
					mAdapter.notifyDataSetChanged();
					if (mOnChangeViewListener != null) {
						switch (childPosition) {
						case 0:// 概览
							mOnChangeViewListener
									.onChangeView(ViewCategories.DASHBOARD);
							break;
						case 1:// 食物
							mOnChangeViewListener
									.onChangeView(ViewCategories.FOODS);
							break;
						case 2:// 运动轨迹
							mOnChangeViewListener
									.onChangeView(ViewCategories.TOUR_TRACKER);
							break;
						case 3:// 朋友
							mOnChangeViewListener
									.onChangeView(ViewCategories.FRIENDS);
							break;
						case 4://消息中心
							mOnChangeViewListener.onChangeView(ViewCategories.MESSAGE_CENTER);
							hasNewMessage = false;
							break;
						}
					}

				} else if (groupPosition == 1) {
					mChooesId = childPosition;
					mAdapter.notifyDataSetChanged();
					switch (childPosition) {
					case 0:// 设置
						mOnChangeViewListener.onChangeView(ViewCategories.SETTINGS);
						break;
					case 1:// 注销登录
						logout();
						break;
					}
				} else if (groupPosition == 2) {
					// mChooesId = childPosition;
					// mAdapter.notifyDataSetChanged();
					if (mOnChangeViewListener != null) {
						switch (childPosition) {
						case 0:// Guide
							mOnChangeViewListener
									.onChangeView(ViewCategories.GUIDE);
							break;
						case 1:// Check for updates
							break;
						case 2:// Feedback
							mOnChangeViewListener
									.onChangeView(ViewCategories.FEEDBACK);
							break;
						case 3:// About Healthy
							mOnChangeViewListener
									.onChangeView(ViewCategories.ABOUT_HEALTHY);
							break;
						}
					}

				}
				return true;
			}
		});
	}

	public View getView() {
		return mMenuPanel;
	}

	public interface onChangeViewListener {
		public abstract void onChangeView(int arg0);
	}

	public void setOnChangeViewListener(
			onChangeViewListener onChangeViewListener) {
		mOnChangeViewListener = onChangeViewListener;
	}

	/**
	 * 注销用户登录函数
	 * */
	private void logout() {
		if (HealthyUtil.getInstance().getLoginedUser() == null) {// 尚未进行登录
			Toast.makeText(mContext, "您尚未进行登录操作", Toast.LENGTH_SHORT).show();
			return;
		}
		FriendsRequestParam param = new FriendsRequestParam();
		param.setTaskCategory(FriendsRequestParam.TASK_LOGOUT);
		RequestListener<FriendsResponseBean> listener = new RequestListener<FriendsResponseBean>() {

			@Override
			public void onStart() {
				// TODO Auto-generated method stub
				handler.sendEmptyMessage(0);
			}

			@Override
			public void onComplete(FriendsResponseBean bean) {
				// TODO Auto-generated method stub
				if (bean.getResult() == FriendsResponseBean.SUCCESS) {// 登出成功
					handler.sendEmptyMessage(1);
				} else {
					Message msg = Message.obtain(handler, 2, bean);
					msg.sendToTarget();
				}
			}
		};
		HealthyApplication.mAsyncHealthy.logout(param, listener);
	}

	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case 0:
				mWaitDialog.show();
				break;
			case 1:
				mWaitDialog.cancel();
				Toast.makeText(mContext, "登出成功", Toast.LENGTH_SHORT).show();
				break;
			case 2:
				mWaitDialog.cancel();
				FriendsResponseBean bean = (FriendsResponseBean) msg.obj;
				Toast.makeText(mContext, bean.toString(), Toast.LENGTH_SHORT)
						.show();
				break;
			}
		}

	};

}
