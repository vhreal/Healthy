package com.healthy.ui.dashboard;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.healthy.R;
import com.healthy.logic.HealthyApplication;
import com.healthy.ui.base.TextProgressBar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

/** 计算每天几种运动时间，如走路、跑步、静止 **/
public class DayActivities {

	private Context mContext;
	private View mTodayActivity;
	private ImageView mStarOne;
	private ImageView mStarTwo;
	private ImageView mStarThree;
	private ImageView mStarFour;
	private ImageView mStarFive;
	private TextProgressBar mRunning;
	private TextProgressBar mWalking;
	private TextProgressBar mStatic;
	private TextProgressBar mBrowsing;
	private Button mChartBtn;

	private HashMap<String, HashMap<String, Object>> mMeasurements;// 当天活动统计数据
	private HashMap<String, Object> mMeasurement;// 某个活动的各项数据统计

	public DayActivities(Context context) {
		mContext = context;
		mTodayActivity = LayoutInflater.from(mContext).inflate(
				R.layout.page_day_activities, null);
		mStarOne = (ImageView) mTodayActivity.findViewById(R.id.star_one_view);
		mStarTwo = (ImageView) mTodayActivity.findViewById(R.id.star_two_view);
		mStarThree = (ImageView) mTodayActivity
				.findViewById(R.id.star_three_view);
		mStarFour = (ImageView) mTodayActivity
				.findViewById(R.id.star_four_view);
		mStarFive = (ImageView) mTodayActivity
				.findViewById(R.id.star_five_view);
		mRunning = (TextProgressBar) mTodayActivity
				.findViewById(R.id.dashboard_running_text);
		mWalking = (TextProgressBar) mTodayActivity
				.findViewById(R.id.dashboard_walking_text);
		mStatic = (TextProgressBar) mTodayActivity
				.findViewById(R.id.dashboard_static_text);
		mBrowsing = (TextProgressBar) mTodayActivity
				.findViewById(R.id.dashboard_browsing_text);
		mChartBtn = (Button) mTodayActivity.findViewById(R.id.dash_chart_btn);

		init();
		setListener();
	}

	public View getView() {
		return mTodayActivity;
	}

	private void init() {
		Date date = new Date();
		mMeasurements = new HashMap<String, HashMap<String, Object>>();
		mMeasurements = HealthyApplication.mDbUtil
				.getDailyActivityData(DateFormat.format("yyyy-MM-dd", date)
						.toString());
		initStar();
		initProgressBar();
	}

	/**
	 * 初始化进度条
	 */
	private void initProgressBar() {
		float runningPercent = 0, walkingPercent = 0, staticPercent = 0, browsingPercent = 0, sum = 0;
		mMeasurement = new HashMap<String, Object>();
		if (mMeasurements != null) {
			Iterator<Entry<String, HashMap<String, Object>>> iterator = mMeasurements
					.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<String, HashMap<String, Object>> entry = iterator
						.next();
				mMeasurement = entry.getValue();
				sum += Float.valueOf(mMeasurement.get("duration").toString());
				Log.i("tag", entry.getKey() + ":"
						+ mMeasurement.get("duration").toString());
			}
			if (mMeasurements.containsKey("jogging")) {
				mMeasurement = mMeasurements.get("jogging");
				runningPercent = Float.valueOf(mMeasurement.get("duration")
						.toString()) / sum;
			}
			if (mMeasurements.containsKey("walking")) {
				mMeasurement = mMeasurements.get("walking");
				walkingPercent = Float.valueOf(mMeasurement.get("duration")
						.toString()) / sum;
			}
			if (mMeasurements.containsKey("stationary")) {
				mMeasurement = mMeasurements.get("stationary");
				staticPercent = Float.valueOf(mMeasurement.get("duration")
						.toString()) / sum;
			}
			if (mMeasurements.containsKey("browsing")) {
				mMeasurement = mMeasurements.get("browsing");
				browsingPercent = Float.valueOf(mMeasurement.get("duration")
						.toString()) / sum;
			}
			mRunning.setProgress((int) (runningPercent * 100));
			mWalking.setProgress((int) (walkingPercent * 100));
			mStatic.setProgress((int) (staticPercent * 100));
			mBrowsing.setProgress((int)(browsingPercent * 100));
		}
	}

	/**
	 * 初始化运动指数
	 */
	private void initStar() {
		if (mMeasurements != null) {
			if (mMeasurements.containsKey("jogging")
					|| mMeasurements.containsKey("walking")) {
				mStarOne.setImageResource(R.drawable.star_mark);
				mMeasurement = mMeasurements.get("walking");
				if (mMeasurements.containsKey("walking")
						&& Integer.valueOf(mMeasurement.get("strides")
								.toString()) > 1200) {
					mStarTwo.setImageResource(R.drawable.star_mark);
					if (mMeasurements.containsKey("walking")
							&& Integer.valueOf(mMeasurement.get("strides")
									.toString()) > 2000
							&& mMeasurements.containsKey("jogging")) {
						mStarThree.setImageResource(R.drawable.star_mark);
						HashMap<String, Object> mMeasurementJog = new HashMap<String, Object>();
						mMeasurementJog = mMeasurements.get("jogging");// jogging为慢跑即跑步
						if (mMeasurements.containsKey("walking")
								&& Integer.valueOf(mMeasurement.get("strides")
										.toString()) > 2000
								&& mMeasurements.containsKey("jogging")
								&& Float.valueOf(mMeasurementJog
										.get("duration").toString()) > 3000) {
							mStarFour.setImageResource(R.drawable.star_mark);
							if (mMeasurement.containsKey("walking")
									&& Integer.valueOf(mMeasurement.get(
											"strides").toString()) > 10000) {
								mStarFive
										.setImageResource(R.drawable.star_mark);
							}
						}

					}
				}

			}
		}

	}

	private void setListener() {
		mChartBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Intent intent = new Intent(mContext, ChartDataActivity.class);
				mContext.startActivity(intent);
				((Activity) mContext).overridePendingTransition(R.anim.roll_up,
						R.anim.roll);
			}
		});
	}
}
