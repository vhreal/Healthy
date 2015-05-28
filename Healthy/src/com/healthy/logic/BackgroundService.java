package com.healthy.logic;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.PowerManager;

import com.healthy.classifier.Recognizer;
import com.healthy.logic.model.ActivityInDb;
import com.healthy.logic.model.SensorInDb;
import com.healthy.util.LogUtil;

public class BackgroundService extends Service implements SensorEventListener {

	private static final SimpleDateFormat formatter = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	// 将不同的活动按照Activities类中活动的定义顺序存入数组
	// 注意不包括Browsing活动，该活动单独列出
	// private static final String[] activities = new String[] { "stationary",
	// "escalator", "lift", "walking", "jogging", "bicycling",
	// "ascendingStairs", "descendingStairs" };

	private static final String[] activities = new String[] { "stationary",
			"lift", "walking", "jogging", "bicycling", "ascendingStairs",
			"descendingStairs" };

	// 用以检测手机屏幕变化情况
	private PhoneStateReceiver psr;
	private String startBrowsingTime;
	private String endBrowsingTime;

	// Alarm receiver
	private ProcessingTask task;

	// 传感器管理
	private SensorManager sensorManager;
	private Sensor accelerometerSensor;
	private Sensor magneticSensor;

	// 存放采集的数据的
	private SensorInDb sdTmp;
	private SensorInDb[] sdQueue = new SensorInDb[2];// 存放当前要进行处理的传感器数据
	private int currentQueue = 0;// 当前要处理的传感器数据集

	// 定时处理任务
	private AlarmManager am;
	private PendingIntent pi;

	// 用于存储上次的活动信息，判断之后决定是否插入数据库
	private static ActivityInDb tempData = new ActivityInDb();
	// 判断是否保存了temp数据，如果已经保存，就判断类型，相同就将temp插入，更新其步数
	private static boolean tempSaved = false;

	private static final SimpleDateFormat TIMESTAMP_FMT = new SimpleDateFormat(
			"[HH:mm:ss] ");

	private PowerManager.WakeLock wakeLock;

	private boolean allowActivityRecog = true;// 是否允许活动识别

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		// 搜集传感器数据（加速度和方向数据）
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			sdQueue[currentQueue].xAcc.add((double) event.values[0]);
			sdQueue[currentQueue].yAcc.add((double) event.values[1]);
			sdQueue[currentQueue].zAcc.add((double) event.values[2]);
		} else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
			sdQueue[currentQueue].MagxData.add((double) event.values[0]);
			sdQueue[currentQueue].MagyData.add((double) event.values[1]);
			sdQueue[currentQueue].MagzData.add((double) event.values[2]);
		}

	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();

		System.out.println("创建服务");
		
		sdQueue[0] = new SensorInDb();
		sdQueue[1] = new SensorInDb();
		sdQueue[0].clearData();
		sdQueue[1].clearData();

		acquireWakelock();

		if (isScreenOn()) {// 屏幕在初始状态下打开
			allowActivityRecog = false;// 禁止活动识别结果插入数据库
			startBrowsingTime = formatter.format(new Date(System
					.currentTimeMillis()));// 记录手机屏幕点亮的时间
			// 更新最后活动记录的时间
			HealthyApplication.mDbUtil.updateActivityEndTime(startBrowsingTime);
		}

		// 在服务进程创建后，注册传感器，采集数据
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		accelerometerSensor = sensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		magneticSensor = sensorManager
				.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		sensorManager.registerListener(this, accelerometerSensor,
				SensorManager.SENSOR_DELAY_GAME);
		sensorManager.registerListener(this, magneticSensor,
				SensorManager.SENSOR_DELAY_GAME);

		// 检测手机屏幕的开关
		psr = new PhoneStateReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		filter.addAction(Intent.ACTION_SCREEN_ON);
		registerReceiver(psr, filter);

		task = new ProcessingTask();
		registerReceiver(task, new IntentFilter("ACTION_ALARM"));
		am = (AlarmManager) getSystemService(ALARM_SERVICE);
		pi = PendingIntent.getBroadcast(this, 0, new Intent("ACTION_ALARM"), 0);
		long now = System.currentTimeMillis() + 10000;
		am.setInexactRepeating(AlarmManager.RTC_WAKEUP, now, 10000, pi);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		
		System.out.println("销毁服务");

		// 在服务进程结束时，注销传感器
		sensorManager.unregisterListener(this, accelerometerSensor);
		sensorManager.unregisterListener(this, magneticSensor);

		// 在服务进程结束时，停止接收广播，不再记录屏幕
		unregisterReceiver(psr);
		unregisterReceiver(task);
		am.cancel(pi);

		// 在服务进程结束时，关闭数据库
		HealthyApplication.mDbUtil.closeDb();

		releaseWakelock();
		super.onDestroy();
	}

	/**
	 * 用来监听手机屏幕的开启关闭状态
	 * 
	 * */
	class PhoneStateReceiver extends BroadcastReceiver {

		public void onReceive(Context context, Intent intent) {

			if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {// 手机屏幕开启
				allowActivityRecog = false;// 禁止活动识别结果插入数据库
				startBrowsingTime = formatter.format(new Date(System
						.currentTimeMillis()));// 记录手机屏幕点亮的时间
				// 更新最后活动记录的时间
				HealthyApplication.mDbUtil
						.updateActivityEndTime(startBrowsingTime);
				tempSaved=false;//清空已缓存的活动类型
			} else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {// 手机屏幕关闭
				sensorManager.unregisterListener(BackgroundService.this);
				sensorManager.registerListener(BackgroundService.this,
						accelerometerSensor, SensorManager.SENSOR_DELAY_GAME);
				sensorManager.registerListener(BackgroundService.this,
						magneticSensor, SensorManager.SENSOR_DELAY_GAME);

				allowActivityRecog = true;// 允许将活动识别结果插入数据库
				endBrowsingTime = formatter.format(new Date(System
						.currentTimeMillis()));// 记录手机屏幕关闭的时间

				// 插入Browsing记录
				ActivityInDb data = new ActivityInDb();
				data.kind = "browsing";
				data.start_time = startBrowsingTime;
				data.end_time = endBrowsingTime;
				data.strideCount = 0;
				HealthyApplication.mDbUtil.insertIntoActivity(data);
			}
		}
	}

	class ProcessingTask extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			// TODO Auto-generated method stub
			if (allowActivityRecog) {// 允许识别活动
				// 处理当前10s中的数据
				sdTmp = sdQueue[currentQueue];// 浅拷贝
				currentQueue = (currentQueue + 1) % 2;
				if (sdTmp.xAcc.size() != 0) {
					// 开始识别活动种类
					int result = Recognizer.recognize(sdTmp,
							BackgroundService.this);

					int strideCount = Recognizer.strideCount;
					// 允许活动识别的时候，才可以将结果插入数据库中
					// 在内层仍然需要进行此判断的原因是屏幕打开，置allowActivityRecog为false时候，程序已经执行到此处
					if(allowActivityRecog)
						insertIntoDB(activities[result - 1], strideCount);

					// 打印日志信息
					System.out.println("activity----->"
							+ activities[result - 1] + "-----strideCount----->"
							+ strideCount);
					LogUtil.addLog(TIMESTAMP_FMT.format(new Date())
							+ activities[result - 1] + " " + strideCount);
				} else {
					System.out.println("There are no data");
					LogUtil.addLog(TIMESTAMP_FMT.format(new Date())
							+ "There are no data");
				}
				sdTmp.clearData();
			} else {
				sdQueue[currentQueue].clearData();
				currentQueue = (currentQueue + 1) % 2;
			}

		}

	}

	/**
	 * 用来将除Browsing活动之外的其它活动插入到数据库中 注：Browsing活动是直接通过DBUtil插入数据库中的
	 * 
	 * @param kind
	 *            种类：手机状态或者活动状态
	 * @param flag
	 *            若为0，则为手机信息。不为零，则为活动步数
	 */
	public void insertIntoDB(String kind, int flag) {
		// TODO Auto-generated method stub
		if (activityKindChanged(kind) == 1) {// 跟数据库中的最后一条记录相比，活动发生变化

			if (kind.equals(activities[0]) || kind.equals(activities[1])) {// 从其他活动变为静止或者电梯，则将数据插入
				ActivityInDb data = new ActivityInDb();
				data.kind = kind;
				SimpleDateFormat formatter = new SimpleDateFormat(
						"yyyy-MM-dd HH:mm:ss");
				Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
				String time = formatter.format(curDate);
				data.start_time = time;
				data.end_time = "---";
				data.strideCount = flag;
				HealthyApplication.mDbUtil.updateActivityEndTime(time);
				HealthyApplication.mDbUtil.insertIntoActivity(data);

			} else {// 如果是改变为其他活动，则进行判断

				if (!allowActivityRecog) {// 当前手机处于browsing状态，将tempData的数据直接插入数据库
					if (tempSaved) {
						tempSaved = false;//清空已缓存的活动类型
					}
				} else {// 当前手机处于非browsing状态

					if (tempSaved) {// 先判断是否保存了上次结果，如果保存了，则进行判断
						if (kind.equals(tempData.kind)) {// 判断该次结果与保存的上次结果是否相同，若相同，则更新上次数据的步数，然后插入数据库
							tempData.strideCount += flag;
							HealthyApplication.mDbUtil
									.updateActivityEndTime(tempData.start_time);
							HealthyApplication.mDbUtil
									.insertIntoActivity(tempData);
							tempSaved = false;
						} else {// 若这次结果与上次结果不同，则用这次的结果替换上次的结果
							tempData.kind = kind;
							SimpleDateFormat formatter = new SimpleDateFormat(
									"yyyy-MM-dd HH:mm:ss");
							Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
							String time = formatter.format(curDate);
							tempData.start_time = time;
							tempData.end_time = "---";
							tempData.strideCount = flag;
							tempSaved = true;
						}

					} else {// 如果没保存，则进行保存
						tempData.kind = kind;
						SimpleDateFormat formatter = new SimpleDateFormat(
								"yyyy-MM-dd HH:mm:ss");
						Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
						String time = formatter.format(curDate);
						tempData.start_time = time;
						tempData.end_time = "---";
						tempData.strideCount = flag;
						tempSaved = true;
					}
				}
			}
		} else if (activityKindChanged(kind) == 0) {// 活动没有发生变化，直接更细数据库最后一条数据
			updateStrides(flag);
		} else {
			ActivityInDb data = new ActivityInDb();
			data.kind = kind;
			SimpleDateFormat formatter = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
			Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
			String time = formatter.format(curDate);
			data.start_time = time;
			data.end_time = "---";
			data.strideCount = flag;
			HealthyApplication.mDbUtil.insertIntoActivity(data);
		}
	}

	/**
	 * 更新,更新步数，若flag为零，则不用更新
	 * 
	 * @param flag
	 */
	private static void updateStrides(int flag) {
		// TODO Auto-generated method stub
		if (flag == 0) {
			return;
		}
		int strides = HealthyApplication.mDbUtil.getLastActivity().strideCount
				+ flag;
		HealthyApplication.mDbUtil.updateStride(strides);
	}

	/**
	 * 
	 * @param kind
	 * @return -1：如果记录为空 0：种类没有改变 1：种类发生改变
	 */
	private static int activityKindChanged(String kind) {
		// TODO Auto-generated method stub
		ActivityInDb lastActivity = HealthyApplication.mDbUtil
				.getLastActivity();
		if (lastActivity == null) {
			return -1;
		}
		if (lastActivity.kind.equals(kind)) {
			return 0;
		} else {
			return 1;
		}
	}

	public static void exitAPP(Context context) {
		// TODO Auto-generated method stub
		android.os.Process.killProcess(android.os.Process.myPid());
		// 关掉全部activity

	}

	private void acquireWakelock() {
		if (wakeLock == null) {
			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, this
					.getClass().getCanonicalName());
			wakeLock.acquire();
		}
	}

	private void releaseWakelock() {
		if (wakeLock != null && wakeLock.isHeld()) {
			wakeLock.release();
			wakeLock = null;
		}
	}

	/**
	 * 查看当前屏幕是否处于点亮状态
	 * */
	private boolean isScreenOn() {
		PowerManager powerManager = (PowerManager) this
				.getSystemService(Context.POWER_SERVICE);
		return powerManager.isScreenOn();
	}
}
