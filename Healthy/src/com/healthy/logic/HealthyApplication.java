package com.healthy.logic;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Application;
import android.content.Intent;
import android.graphics.Bitmap;

import com.healthy.db.DBUtil;

public class HealthyApplication extends Application {

	public static String APPLICATION_PATH;// 该软件的存放路径
	public static DBUtil mDbUtil;
	public static AsyncHealthy mAsyncHealthy = AsyncHealthy.getInstance();
	public static Bitmap mapAvatar;//头像
	public static List<String> keyResult;//关键字搜索
	public static float calories;//月消耗卡路里量
	public static String mRanking;//卡路里消耗返回字符串
	public static Map<String, SoftReference<Bitmap>> imageCache;
	public static float phoneScale;
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		APPLICATION_PATH = getExternalFilesDir(null).getAbsolutePath();
		mDbUtil = new DBUtil(getApplicationContext());// 本地数据库工具类
		imageCache = new HashMap<String, SoftReference<Bitmap>>();
		phoneScale = getResources().getDisplayMetrics().density;
		// 在界面调试的时候没有打开服务，正式调试的时候不要忘记修改！！！
		// 启动后台服务BackgroundService
		Intent intent = new Intent(this, BackgroundService.class);
		startService(intent);
	}

}
