package com.healthy.db;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.healthy.logic.model.ActivityInDb;
import com.healthy.logic.model.FoodInDb;
import com.healthy.logic.model.LocationInDb;
import com.healthy.logic.model.TrackerListBean;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import static com.healthy.util.Constants.FoodConstants.*;

/** 与数据库相关的工具类 */
public class DBUtil {
	private Context mContext;
	private DBHelper dbHelper;
	private static final SimpleDateFormat FORMATTER = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	/** 创建Healthy数据库 */
	public DBUtil(Context context) {
		mContext = context;
		dbHelper = new DBHelper(context, "healthy.db", null, 1);
	}

	/**
	 * 关闭数据库
	 */
	public void closeDb() {
		dbHelper.close();
	}

	/**
	 * 向表activity_info中插入一个数据
	 * 
	 * @param data
	 */
	public void insertIntoActivity(ActivityInDb data) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put("start_time", data.start_time);
		cv.put("end_time", data.end_time);
		cv.put("kind", data.kind);
		cv.put("strides", data.strideCount);
		System.out.println("Insert Into activity_info row ID--->"
				+ db.insert("activity_info", null, cv) + "-----" + data.kind);
		closeDb();
	}

	/**
	 * 获得最后一个ActivityData，如果记录为空，返回null;
	 * 
	 * @return
	 */
	public ActivityInDb getLastActivity() {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.query("activity_info", null, null, null, null, null,
				null);
		if (cursor.getCount() == 0) {
			cursor.close();
			closeDb();
			return null;
		} else {
			cursor.moveToLast();
			ActivityInDb adata = new ActivityInDb();
			adata.start_time = cursor.getString(1);
			adata.end_time = cursor.getString(1);
			adata.kind = cursor.getString(3);
			adata.strideCount = cursor.getInt(4);
			cursor.close();
			closeDb();
			return adata;
		}
	}

	public void updateStride(int stride) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		Cursor cursor = db.query("activity_info", null, null, null, null, null,
				null);
		cursor.moveToLast();
		int id = cursor.getInt(0);
		cursor.close();
		String sql = "update activity_info set strides = " + stride
				+ " where _id=" + id;
		db.execSQL(sql);
		closeDb();
		System.out.println("Update row ID--->" + id);
	}

	/**
	 * 更新活动记录的结束时间
	 * */
	public void updateActivityEndTime(String endTime) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		Cursor cursor = db.query("activity_info", null, null, null, null, null,
				null);
		if (cursor.getCount() == 0) {
			cursor.close();
			closeDb();
			return;
		}
		cursor.moveToLast();
		if (cursor.getString(3).equalsIgnoreCase("Browsing"))// Browsing
																// 的结束时间是直接写入的，因此无需进行更新
			return;
		int id = cursor.getInt(0);
		cursor.close();
		String sql = "update activity_info set end_time = '" + endTime
				+ "' where _id=" + id;
		db.execSQL(sql);
		closeDb();
	}

	/**
	 * 以天为单位计算Date当天内每项活动的时间、卡路里消耗等信息
	 * 
	 * @param day
	 * @return HashMap<String, HashMao<String,Object>>
	 */
	public HashMap<String, HashMap<String, Object>> getDailyActivityData(
			String date) {
		String strSql = "select * from activity_info where strftime('%Y-%m-%d',start_time)=strftime('%Y-%m-%d',?);";// 查询当天的记录
		return getActivityCalorieDataInATime(strSql, date);
	}

	/**
	 * 获取date当月内，各种活动的统计信息
	 * 
	 * @param date
	 *            规定的查询时间
	 * */
	public HashMap<String, HashMap<String, Object>> getMonthActivityData(
			String date) {
		String strSql = "select * from activity_info where strftime('%Y-%m',start_time) = strftime('%Y-%m',?) ";// 查询date当月记录
		return getActivityCalorieDataInATime(strSql, date);
	}

	private HashMap<String, HashMap<String, Object>> getActivityCalorieDataInATime(
			String strSql, String date) {

		HashMap<String, HashMap<String, Object>> measurements = null;
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery(strSql, new String[] { date });
		if (cursor.getCount() - 1 > 0) {// 由于最后一条记录没有结束时间，因此暂没将其考虑在内
			measurements = new HashMap<String, HashMap<String, Object>>();
			cursor.moveToFirst();
			HashMap<String, Object> measurement;
			String activity, startTime, endTime;
			float duration = 0;
			int strides = 0;

			// 统计各个活动的持续时间和某些活动的步数
			for (int i = 0; i < cursor.getCount() - 1; i++) {
				activity = cursor.getString(3);// 获取活动名字
				// 记录该活动的持续时间
				startTime = cursor.getString(1);
				endTime = cursor.getString(2);
				if (!measurements.containsKey(activity)) {// 当前表中不包含该acitivity
					measurement = new HashMap<String, Object>();
					duration = getDuration(startTime, endTime);
					if (duration == -1)// 时间获取失败
						return null;
					measurement.put("duration", duration);

					// 记录该活动的步数
					strides = cursor.getInt(4);
					measurement.put("strides", strides);
					measurements.put(activity, measurement);

				} else {// 当前表中包含该activity,更新持续时间以及步数统计
					measurement = measurements.get(activity);// 获取相对应的activity统计信息
					// 更新步数持续时间
					duration = (Float) measurement.remove("duration")
							+ getDuration(startTime, endTime);
					measurement.put("duration", duration);
					// 更新步数
					strides = (Integer) measurement.remove("strides")
							+ cursor.getInt(4);
					measurement.put("strides", strides);
					measurements.put(activity, measurement);
				}
				cursor.moveToNext();
			}

			// 由上述已统计出的原始数据，计算distance,speed,calories burned
			SharedPreferences prefs_personal = mContext.getSharedPreferences(
					"personal_info", Context.MODE_PRIVATE);
			float weight = prefs_personal.getFloat("weight", 70); // 默认体重为70kg
			float stride_length = prefs_personal.getFloat("stride", 1.2f) / 100;// 成人复步长度大约为1.2m
			float stair_length = prefs_personal.getFloat("stair_length", 0.2f);// 两阶楼梯的高度，默认为0.2m
			float bicycle_length = prefs_personal.getFloat("bicycle_length",
					0.7f);// 自行车车轮直径，默认为2m
			Iterator<Entry<String, HashMap<String, Object>>> iterator = measurements
					.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<String, HashMap<String, Object>> entry = iterator
						.next();
				activity = entry.getKey();
				measurement = entry.getValue();
				duration = (Float) measurement.get("duration");
				float distance = 0;
				float speed = 0;
				float mets = 0;
				if (activity.equalsIgnoreCase("stationary")
						|| activity.equalsIgnoreCase("lift")
						|| activity.equalsIgnoreCase("escalator")
						|| activity.equalsIgnoreCase("browsing")) {// 非周期的活动
					distance = 0;
					speed = 0;
					mets = 1.5f;
				} else if (activity.equalsIgnoreCase("walking")) {
					distance = (Integer) measurement.get("strides")
							* stride_length;
					speed = distance / duration;
					mets = 0.0272f * speed * 60 + 1.2f;
				} else if (activity.equalsIgnoreCase("jogging")) {
					distance = (Integer) measurement.get("strides")
							* stride_length;
					speed = distance / duration;
					mets = 0.093f * speed * 60 - 4.7f;
				} else if (activity.equalsIgnoreCase("ascendingStairs")) {
					distance = (Integer) measurement.get("strides")
							* stair_length;
					speed = distance / duration;
					mets = 8.0f;
				} else if (activity.equalsIgnoreCase("descendingStairs")) {
					distance = (Integer) measurement.get("strides")
							* stair_length;
					speed = distance / duration;
					mets = 3.0f;
				} else if (activity.equalsIgnoreCase("bicycling")) {
					distance = (Integer) measurement.get("strides")
							* bicycle_length;
					speed = distance / duration;
					mets = 5.5f;
				}
				measurement.put("distance", distance);
				measurement.put("speed", speed);
				measurement.put(
						"calories_burned",
						getCaloriesBurned(mets, duration / 60.0f / 60.0f,
								weight));
			}
		}
		cursor.close();
		closeDb();
		return measurements;
	}

	/**
	 * 计算热量消耗
	 * 
	 * @param mets
	 *            METS值
	 * @param duration
	 *            活动持续时间，单位为h
	 * @param weight
	 *            个人体重 单位为kg
	 * @return 热量消耗 单位为kcal
	 * */
	private float getCaloriesBurned(float mets, float duration, float weight) {
		return (float) (1.05 * mets * duration * weight);
	}

	/**
	 * 计算两个时间节点之间的时间长度,时间字符串格式为"yyyy-MM-dd HH:mm:ss" 单位为s
	 * 
	 * @param start
	 *            开始时间
	 * @param end
	 *            结束时间
	 * */
	private float getDuration(String start, String end) {
		try {
			Date dStart = FORMATTER.parse(start);
			Date dEnd = FORMATTER.parse(end);
			float diff = (dEnd.getTime() - dStart.getTime()) / 1000;
			return diff;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e("com.healty", "getActivityData", e);
			return -1;
		}
	}

	// =========================轨迹跟踪相关数据库操作==========================//

	/**
	 * 在tracker_info表中插入一组数据
	 */
	public void insertNullData() {
		int id = getLastTrackerID() + 1;
		Log.i("tag", "tracker的id:" + id + "");
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
		String date = formatter.format(curDate);
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put("_id", id);
		cv.put("start_time", date);
		db.insert("tracker_info", null, cv);
		closeDb();
	}

	/**
	 * 得到tracker_info表中当前最后一组数据的id编号
	 * 
	 * @return
	 */
	public int getLastTrackerID() {
		int i = 0;
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.query("tracker_info", null, null, null, null, null,
				null);
		if (cursor.moveToFirst() == false) {
			cursor.close();
			closeDb();
			return i;
		} else {
			cursor.moveToLast();
			i = cursor.getInt(0);// 第一列，列数为0
			cursor.close();
			closeDb();
			return i;
		}
	}

	/**
	 * 向location_info表中插入一组数据
	 */
	public void insertIntoLocation(LocationInDb mLocation) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put("_id", mLocation.getId());
		cv.put("longitude", mLocation.getLongitude());
		cv.put("latitude", mLocation.getLatitude());
		cv.put("time", mLocation.getTime());
		db.insert("location_info", null, cv);
		closeDb();
	}

	/**
	 * 当前最新记录对应id是否有对应位置信息
	 */
	public boolean IsStartLocation() {
		int id = getLastTrackerID();
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		String where = "_id =" + id;
		Cursor cursor = db.query("location_info", null, where, null, null,
				null, null);
		if (!cursor.moveToFirst()) {
			cursor.close();
			closeDb();
			return false;
		} else {
			cursor.close();
			closeDb();
			return true;
		}
	}

	/**
	 * 删除当前最新插入tracker_info的id行数据
	 */
	public void deleteTracker() {
		int id = getLastTrackerID();
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		String where = "_id = " + id;
		db.delete("tracker_info", where, null);
		closeDb();
	}
	
	/**
	 * 删除指定tracker_info表中id行数据，还得删除Location_info表中对应id的位置数据
	 */
	public void deleteTrackerById(int id){
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		String where = "_id = " + id;
		Log.i("tag", "dbutil-----"+id);
		db.delete("tracker_info", where, null);
		db.delete("location_info", where, null);
		int lastId = getLastTrackerID();
		Log.i("tag", "最后一条记录已变成----->" + lastId);
		closeDb();
	}

	/**
	 * 取出location_info中最新id的经纬度信息保存到List中
	 */
	public List<GeoPoint> getFromLocation() {
		int id = getLastTrackerID();
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		String where = "_id =" + id;
		String order = "time";
		Cursor cursor = db.query("location_info", null, where, null, null,
				null, order);
		List<GeoPoint> pointList = new ArrayList<GeoPoint>();
		if (cursor.moveToFirst())// 已经移到第一个数据上
		{

			do {
				int longitude = (int) (cursor.getDouble(1) * 1E6);
				int latitude = (int) (cursor.getDouble(2) * 1E6);
				GeoPoint temp = new GeoPoint(longitude, latitude);
				pointList.add(temp);
			} while (cursor.moveToNext());
		}
		cursor.close();
		closeDb();
		return pointList;
	}

	/**
	 * 根据id号，查询location_info信息，按时间排序
	 */
	public List<GeoPoint> queryLocationById(int id) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		String where = "_id =" + id;
		String order = "time";
		Cursor cursor = db.query("location_info", null, where, null, null,
				null, order);
		List<GeoPoint> pointList = new ArrayList<GeoPoint>();
		if (cursor.moveToFirst())// 已经移到第一个数据上
		{

			do {
				int longitude = (int) (cursor.getDouble(1) * 1E6);
				int latitude = (int) (cursor.getDouble(2) * 1E6);
				GeoPoint temp = new GeoPoint(longitude, latitude);
				pointList.add(temp);
			} while (cursor.moveToNext());
		}else {
			pointList = null;
		}
		cursor.close();
		closeDb();
		return pointList;
	}

	/**
	 * 将测量得到的distance插入到对应编号的tracker_info表中
	 */
	public void updateTrackerDistance(String distance) {
		int id = getLastTrackerID();
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put("tracker_distance", distance);
		String whereto = "_id = " + id;
		db.update("tracker_info", cv, whereto, null);
		closeDb();
	}

	/**
	 * 更新当前创建一组tracker_info数据的start_address字段 中文插入数据库，adb查询为乱码，不影响输出！
	 */
	public void updateTrackerAdr(String adr) {
		int id = getLastTrackerID();
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put("start_address", adr);
		String where = "_id = " + id;
		db.update("tracker_info", cv, where, null);
		closeDb();
	}

	/**
	 * 更新tracker_info中end_time字段
	 */
	public void updateTrackerEndTime(String endtime){
		int id = getLastTrackerID();
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put("end_time", endtime);
		String where = "_id =" + id;
		db.update("tracker_info", cv, where, null);
		closeDb();
	}
	/**
	 * 判断一次轨迹跟踪活动类型，取时间内持续时间最长的，更新tracker_info的tracker_type字段
	 */
	public void getTrackerType() {
		int id = getLastTrackerID();
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		String where = "_id =" + id;
		Cursor cursor = db.query("tracker_info", null, where, null, null,
				null, null);
		String type = null, activity = null, start, end, starttime = null, endtime = null;
		cursor.moveToFirst();
		starttime = cursor.getString(5);
		endtime = cursor.getString(6);
		cursor.close();
		String strSql = "SELECT * FROM activity_info WHERE start_time > ? and start_time < ? ORDER BY start_time";
		Cursor cursorto = db.rawQuery(strSql,
				new String[] { starttime, endtime });
		if (cursorto.moveToFirst())// 已经移到第一个数据上
		{

			do {
				Log.i("type",
						cursorto.getString(1) + "," + cursorto.getString(2)
								+ "," + cursorto.getString(3));
			} while (cursorto.moveToNext());
		}
		float duration;
		HashMap<String, Float> measurement = new HashMap<String, Float>();
		if (!cursorto.moveToFirst()) {// 若对应时间段没有活动记录

			type = "stationary";
		} else if (cursorto.getCount() == 1) {
			cursorto.moveToFirst();
			type = cursorto.getString(3);
		} else if (cursorto.getCount() > 1) {
			cursorto.moveToFirst();// 先移动到第一条记录上
			// 统计时间段内各种活动的持续时间
			for (int i = 0; i < cursorto.getCount() - 1; i++) {
				activity = cursorto.getString(3);
				start = cursorto.getString(1);
				end = cursorto.getString(2);
				if (!measurement.containsKey(activity))// 当前表中不包含此活动种类，写入
				{
					duration = getDuration(start, end);
					if (duration == -1) {
						Log.i("tag", "获取持续时间失败！+getTrackerType");
					}
					measurement.put(activity, duration);
				} else// 当前表中以存储此活动，更新其持续时间
				{
					duration = measurement.get(activity)
							+ getDuration(start, end);
					measurement.put(activity, duration);
				}
				cursorto.moveToNext();
			}
			ByValueComparator bvc = new ByValueComparator(measurement);
			List<String> keys = new ArrayList<String>(measurement.keySet());// 得到HashMap中所有的key
			Collections.sort(keys, bvc);
			for (String key : keys) {
				int i = 0;
				if (i == 0) {
					type = key;
					System.out.println("比较了吗？这里是YES");
				}
				i++;
				break;
			}
		}
		System.out.println("这是识别的结果type:" + type);
		ContentValues cv = new ContentValues();
		cv.put("activity_type", type);
		String whereto = "_id = " + id;
		db.update("tracker_info", cv, whereto, null);
		cursorto.close();
		closeDb();

	}
	/**
	 * 从tracker_info中取出相应字段数据，更新到trackerList
	 * 
	 */
	public List<TrackerListBean> getTrackerToList() {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		List<TrackerListBean> listData = new ArrayList<TrackerListBean>();
		Cursor cursor = db.query("tracker_info", null, null, null, null, null,
				"_id");
		cursor.moveToLast();
		if (cursor.moveToLast()) {
			do {
				TrackerListBean trackerData = new TrackerListBean();
				trackerData.setId(cursor.getInt(0));
				trackerData.setType(changeTypeToReadable(cursor.getString(2)));
				trackerData.setLocation(cursor.getString(3));
				trackerData.setDistance(cursor.getString(4));
				trackerData.setTime(changeTimeLayout(cursor.getString(5)));
				listData.add(trackerData);
			} while (cursor.moveToPrevious());
		}
		return listData;
	}

	/**
	 * 将tracker_info表中的时间格式转换为trackerList显示的格式
	 */
	private String changeTimeLayout(String time) {
		String showTime = null;
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		Date date = null;
		try {
			date = formatter.parse(time);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		showTime = formatter.format(date);
		return showTime;
	}

	/**
	 * 将tracker_info表中的类型英文转换为本地语言
	 */
	public String changeTypeToReadable(String type) {
		try {
			Class<?>[] classes = Class.forName("com.healthy.R")
					.getDeclaredClasses();
			for (Class<?> c : classes) {// 对成员内部类进行反射
				if (c.getName().contains("string")) {
					return mContext.getResources().getString(
							c.getField(type).getInt(null));
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e("Healthy", "changeTypeToChinese", e);
		}
		return "error";
	}

	/***
	 * 实现Comparator<>接口，用于比较hashmap键值对中value的大小
	 * 
	 * @return 0 1 -1;
	 * 
	 */
	static class ByValueComparator implements Comparator<String> {
		private HashMap<String, Float> datas = new HashMap<String, Float>();

		public ByValueComparator(HashMap<String, Float> datas) {
			this.datas = datas;
		}

		@Override
		public int compare(String lhs, String rhs) {
			if (!datas.containsKey(rhs) || !datas.containsKey(lhs)) {
				return 0;
			}

			if (datas.get(lhs) < datas.get(rhs)) {
				return 1;
			} else if (datas.get(lhs) == datas.get(rhs)) {
				return 0;
			} else {
				return -1;
			}
		}

	}

	// ======================食物数据库操作相关=============================//
	/**
	 * 向food_info中添加一条数据
	 */
	public void insertIntoFoodInfo(FoodInDb food) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put("name", food.name);
		cv.put("num", food.num);
		cv.put("calorie", food.calorie);
		cv.put("time", food.time);
		cv.put("date", food.date);
		db.insert("food_info", null, cv);
		closeDb();
	}

	/**
	 * 向food_type添加一条数据
	 */
	public void insertIntoFoodType(FoodInDb data) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put("name", data.name);
		cv.put("calorie", data.calorie);
		cv.put("iscommon", 0);
		db.insert("food_type", null, cv);
		closeDb();
	}

	/**
	 * 查询一段时间内的饮食信息
	 * */
	public List<FoodInDb> queryFoodInATime(String strSql, String date) {
		List<FoodInDb> list = new ArrayList<FoodInDb>();
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery(strSql, new String[] { date });
		FoodInDb food;
		while (cursor.moveToNext()) {
			food = new FoodInDb();
			food.name = cursor.getString(cursor.getColumnIndex(NAME));
			food.num = cursor.getInt(cursor.getColumnIndex(NUM));
			food.calorie = cursor.getInt(cursor.getColumnIndex(CALORIE));
			food.time = cursor.getString(cursor.getColumnIndex(TIME));
			food.date = cursor.getString(cursor.getColumnIndex(DATE));
			list.add(food);
		}
		cursor.close();
		db.close();
		return list;
	}
	
	/**
	 * 查询某天的饮食信息
	 */
	public List<FoodInDb> queryDayFood(String date) {
		String strSql = "select * from food_info where strftime('%Y-%m-%d',date)=strftime('%Y-%m-%d',?);";
		return queryFoodInATime(strSql, date);
	}
	
	/**
	 * 查询当月食物摄入
	 * */
	public List<FoodInDb> queryMonthFood(String date) {
		String strSql = "select * from food_info where strftime('%Y-%m',date)=strftime('%Y-%m',?);";
		return queryFoodInATime(strSql, date);
	}

	/**
	 * 删除某条饮食记录
	 */
	public void deleteFood(FoodInDb food) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		String where = "strftime('%Y-%m-%d',date)=strftime('%Y-%m-%d',?) and " +
				"strftime('%H-%M',time)=strftime('%H-%M',?) " +
				"and name=?;";
		db.delete("food_info", where, new String[] { food.date, food.time, food.name });
		db.close();
	}

	/*
	 * 查询常用食物
	 */
	public List<Map<String, Object>> queryFoodType() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Map<String, Object> map;
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cur = db.query("food_type", new String[] { "*" }, null, null,
				null, null, null);
		while (cur.moveToNext()) {
			map = new HashMap<String, Object>();
			map.put("name", cur.getString(cur.getColumnIndex("name")));
			map.put("calorie", cur.getFloat(cur.getColumnIndex("calorie")));
			list.add(map);
		}
		cur.close();
		db.close();
		return list;
	}

	/**
	 * 更新饮食计划时间
	 * 
	 * @param time
	 * @param duration
	 */
	public void updateFoodPlan(String time, int duration, float dayCalorie) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.delete("food_plan", "", new String[] {});// 删除原有计划
		ContentValues cv = new ContentValues();
		cv.put("start_time", time);
		cv.put("duration", duration);
		cv.put("daycalorie", dayCalorie);
		db.insert("food_plan", null, cv);
		closeDb();
	}

	/**
	 * 查询饮食计划
	 */
	public Map<String, Object> queryFoodPlan() {
		Map<String, Object> map = new HashMap<String, Object>();
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cur = db.query("food_plan", new String[] { "*" }, null, null,
				null, null, null);
		while (cur.moveToNext()) {
			map.put("start_time",
					cur.getString(cur.getColumnIndex("start_time")));
			map.put("duration", cur.getInt(cur.getColumnIndex("duration")));
			map.put("daycalorie",
					cur.getFloat(cur.getColumnIndex("daycalorie")));
		}
		cur.close();
		db.close();
		return map;
	}
}
