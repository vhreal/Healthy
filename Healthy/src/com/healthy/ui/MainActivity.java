package com.healthy.ui;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup.LayoutParams;
import android.widget.Toast;

import com.healthy.R;
import com.healthy.logic.BackgroundService;
import com.healthy.ui.base.FlipperLayout;
import com.healthy.ui.base.FlipperLayout.OnOpenListener;
import com.healthy.ui.base.ViewCategories;
import com.healthy.ui.dashboard.Dashboard;
import com.healthy.ui.foods.Foods;
import com.healthy.ui.friends.Friends;
import com.healthy.ui.menupanel.MenuBroadcastReceiver;
import com.healthy.ui.menupanel.MenuPanel;
import com.healthy.ui.menupanel.MenuPanel.onChangeViewListener;
import com.healthy.ui.message.MessageCenter;
import com.healthy.ui.settings.Settings;
import com.healthy.ui.tourtracker.TourTracker;
import com.healthy.util.Constants.ActivityRequestCode;
import com.healthy.util.Constants.ActivityResultCode;
import com.healthy.util.HealthyUtil;
import com.healthy.util.Tools;

public class MainActivity extends Activity implements OnOpenListener {

	private long BACK_TIME = 0l;
	private FlipperLayout mRoot;
	private MenuBroadcastReceiver receiver = null;

	/* 头像名称 */
	private static final String IMAGE_FILE_NAME = "faceImage.jpg";

	// 各个页面
	private MenuPanel mMenuPanel;
	private Dashboard mDashboard;
	private Foods mFoods;
	private TourTracker mTourTracker;
	private Friends mFriends;
	private Settings mSettings;
	private MessageCenter mMessage;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		mRoot = new FlipperLayout(MainActivity.this);
		LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT);
		mRoot.setLayoutParams(params);

		mMenuPanel = new MenuPanel(this);
		mDashboard = new Dashboard(this);
		mMessage = new MessageCenter(this);
		
		mRoot.addView(mMenuPanel.getView(), params);
		mRoot.addView(mDashboard.getView(), params);
		
		setSharedPreferences();
		setContentView(mRoot);
		setListener();
		if (receiver == null) {
			initBroadcast();
		}
		System.out.println("oncreate");
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Intent intent = super.getIntent();
		if (intent.getStringExtra("info") != null && intent.getStringExtra("info").equals("friendrequest")) {
			if(HealthyUtil.getInstance().getLoginedUser()!=null){
				mRoot.changeContentView(mMessage.getView());
				MenuPanel.hasNewMessage = false;
				MenuPanel.mAdapter.notifyDataSetChanged();
				MenuPanel.mChooesId = 4;
			}else{
				mRoot.changeContentView(mFriends.getView());
			}
			
		}
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
	}

	private void initBroadcast(){
		receiver = new MenuBroadcastReceiver();
		IntentFilter filter = new IntentFilter("com.healthy.action.messages");
		super.registerReceiver(receiver, filter);
	}
	
	private void setSharedPreferences(){
		SharedPreferences prefs = this.getSharedPreferences("personal_info", MODE_PRIVATE);
		if (!prefs.contains("sex")) {
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString("sex", "男");
			editor.putInt("age",25);
			editor.putFloat("height",175);
			editor.putFloat("weight",70);
			editor.putFloat("stride",100);
			editor.putFloat("stair_length", 0.2f);// 两阶楼梯的高度，默认为0.2m
			editor.putFloat("bicycle_length", 0.7f);// 自行车车轮直径，默认为2m
			editor.commit();
		}
	}


	private void setListener() {

		mMenuPanel.setOnChangeViewListener(new onChangeViewListener() {

			@Override
			public void onChangeView(int arg0) {
				// TODO Auto-generated method stub
				switch (arg0) {
				case ViewCategories.DASHBOARD:
					mRoot.changeContentView(mDashboard.getView());
					break;
				case ViewCategories.FOODS:
					if(mFoods==null){
						mFoods = new Foods(MainActivity.this);
						mFoods.setOnOpenListener(MainActivity.this);
					}
					mRoot.changeContentView(mFoods.getView());
					break;
				case ViewCategories.TOUR_TRACKER:
					if(mTourTracker==null){
						mTourTracker = new TourTracker(MainActivity.this);
						mTourTracker.setOnOpenListener(MainActivity.this);
					}
					mRoot.changeContentView(mTourTracker.getView());
					break;
				case ViewCategories.FRIENDS:
					if(mFriends==null){
						mFriends = new Friends(MainActivity.this);
						mFriends.setOnOpenListener(MainActivity.this);
					}	
					mRoot.changeContentView(mFriends.getView());
					break;
				case ViewCategories.SETTINGS:
					if(mSettings==null){
						mSettings = new Settings(MainActivity.this);
						mSettings.setOnOpenListener(MainActivity.this);
					}
					mRoot.changeContentView(mSettings.getView());
					break;
				case ViewCategories.MESSAGE_CENTER:	
					mRoot.changeContentView(mMessage.getView());
					break;
				}
			}
		});
		
		mDashboard.setOnOpenListener(this);
		mMessage.setOnOpenListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		// 在该处判断是否关闭服务
		Intent intent = new Intent(this, BackgroundService.class);
		stopService(intent);
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		// TODO Auto-generated method stub
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_DOWN) {
			if ((System.currentTimeMillis() - BACK_TIME) > 2000) {
				Toast.makeText(this,
						getResources().getString(R.string.tip_exit),
						Toast.LENGTH_SHORT).show();
				if (mTourTracker!=null && mTourTracker.mIsStart == true)
					mTourTracker.overTracker();
				BACK_TIME = System.currentTimeMillis();
				open();
			} else {
				BackgroundService.exitAPP(this);
			}
			return true;
		} else
			return super.dispatchKeyEvent(event);
	}

	@Override
	public void open() {
		// TODO Auto-generated method stub
		if (mRoot.getScreenState() == FlipperLayout.MENU_STATE_CLOSE) {
			mRoot.open();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO 自动生成的方法存根
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
		case ActivityRequestCode.FOODADD:
			mFoods.updateFoodData(false);
			Log.i("request", resultCode + "");
			break;
		case ActivityRequestCode.FOODPLAN:
			if (resultCode == ActivityResultCode.SUCCESS) {
				mFoods.setPlanCalori(Float.parseFloat(data.getExtras()
						.get("plan").toString()));

				mFoods.updateFoodData(false);
			}
			break;
		case ActivityRequestCode.IMAGE:
			if (resultCode != 0)
				mFriends.startPhotoZoom(data.getData());
			break;
		case ActivityRequestCode.CAMERA:
			if (resultCode != 0) {//在拍照过程中退出程序，异常判断
				if (Tools.hasSdcard()) {
					File tempFile = new File(
							Environment.getExternalStorageDirectory()
									+ IMAGE_FILE_NAME);
					mFriends.startPhotoZoom(Uri.fromFile(tempFile));
				} else {
					Toast.makeText(MainActivity.this, "未找到存储卡，无法存储照片！",
							Toast.LENGTH_LONG).show();
				}
			}
			break;
		case ActivityRequestCode.CANCEL:
			if (data != null) {
				Log.i("tag", "图像已经截取开始上传！");
				mFriends.getImageToView(data);
			}
			break;
		}
		
	}

}
