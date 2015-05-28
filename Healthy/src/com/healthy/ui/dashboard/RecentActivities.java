package com.healthy.ui.dashboard;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.healthy.R;
import com.healthy.logic.HealthyApplication;
import com.healthy.logic.model.Measurement;
import com.healthy.ui.base.PieChartView;
import com.healthy.ui.base.PieChartView.OnCompleteRotating;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/** 最近活动分布情况 */
public class RecentActivities {

	private View mRecentActivity;
	private View mChartLayout;
	private ImageView mEmptyChart;
	private Context mContext;
	private PieChartView mPieChartView;// 活动分布的饼图
	private RecentActivityAdapter mAdapter;
	private ListView mDisplay;
	private String[] mActivities;// 活动名称
	private float[] mRatios;// 各种活动所占的比例

	// 活动时间的调节与显示
	private ImageView mPreDate;
	private ImageView mNextDate;
	private TextView mCurDate;
	private Calendar mCalendar = Calendar.getInstance();

	private HashMap<String, HashMap<String, Object>> mMeasurements;// 活动统计数据

	public RecentActivities(Context context) {
		mContext = context;
		mRecentActivity = LayoutInflater.from(mContext).inflate(
				R.layout.page_recent_activities, null);
		mPieChartView = (PieChartView) mRecentActivity
				.findViewById(R.id.pie_chart);
		mChartLayout = mRecentActivity.findViewById(R.id.chart_layout);
		mEmptyChart = (ImageView) mRecentActivity
				.findViewById(R.id.empty_chart);
		mDisplay = (ListView) mRecentActivity.findViewById(R.id.display);
		mPreDate = (ImageView) mRecentActivity.findViewById(R.id.prev_date);
		mNextDate = (ImageView) mRecentActivity.findViewById(R.id.next_date);
		mCurDate = (TextView) mRecentActivity.findViewById(R.id.cur_date);
		init();
	}

	public View getView() {
		return mRecentActivity;
	}

	/** 数据的进行初始化 */
	public void init() {
		// 获取当前日期，完成初始化
		Date date = new Date();
		mCalendar.setTime(date);
		mCurDate.setText(DateFormat.format("yyyy-MM", date).toString());
		initData(date);
		setListener();
	}

	private void initData(Date date) {
		// 从数据库中读取数据，并绑定控件
		if (!getActivityData(DateFormat.format("yyyy-MM-dd", date).toString())) {// 未找到当月相关数据
			mChartLayout.setVisibility(View.GONE);
			mEmptyChart.setVisibility(View.VISIBLE);
		} else {// 查询到相关数据
			mChartLayout.setVisibility(View.VISIBLE);
			mEmptyChart.setVisibility(View.GONE);
		}
	}

	/**
	 * @return 当月没有相关数据，返回false，否则返回true
	 * */
	private boolean getActivityData(String date) {
		mMeasurements = HealthyApplication.mDbUtil.getMonthActivityData(date);
		if (mMeasurements == null)
			return false;
		mActivities = new String[mMeasurements.size()];
		String[] activities = new String[mMeasurements.size()];
		mRatios = new float[mMeasurements.size()];
		Iterator<Entry<String, HashMap<String, Object>>> iterator = mMeasurements
				.entrySet().iterator();
		int i = 0;
		long sum = 0;
		while (iterator.hasNext()) {
			Entry<String, HashMap<String, Object>> entry = iterator.next();
			String key = entry.getKey();// 获取活动类别
			mActivities[i] = new String(key);
			
			activities[i] = HealthyApplication.mDbUtil.changeTypeToReadable(new String(key));
			HashMap<String, Object> measurement = (HashMap<String, Object>) entry// 对应活动数据
					.getValue();
			mRatios[i] = (Float) measurement.get("duration");
			sum += mRatios[i];
			i++;
		}
		for (i = 0; i < mMeasurements.size(); i++) {
			mRatios[i] /= sum;
		}
		mPieChartView.initData(activities, mRatios);
		return true;
	}

	private void setListener() {
		mPieChartView.setOnCompleteRotatingListener(new OnCompleteRotating() {
			@Override
			public void onCompleteRotating(int pos) {
				// TODO Auto-generated method stub
				Message msg = Message.obtain(handler);
				msg.arg1 = pos;
				msg.what = 0;
				msg.sendToTarget();
			}
		});

		/* 上一个月 */
		mPreDate.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mCalendar.add(Calendar.MONTH, -1);
				mCurDate.setText(DateFormat.format("yyyy-MM",
						mCalendar.getTime()));
				initData(mCalendar.getTime());
			}
		});

		/* 下一个月 */
		mNextDate.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mCalendar.add(Calendar.MONTH, 1);
				mCurDate.setText(DateFormat.format("yyyy-MM",
						mCalendar.getTime()));
				initData(mCalendar.getTime());
			}
		});
	}

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			List<Measurement> measurementList = new ArrayList<Measurement>();
			HashMap<String, Object> map = mMeasurements
					.get(mActivities[msg.arg1]);

			// 步数统计
			measurementList.add(new Measurement("步数:", map.get("strides")
					.toString()));

			// 活动持续时间，以小时为单位
			measurementList.add(new Measurement("时长:", String.format(
					"%.2f", (Float) map.get("duration") / 3600.0f) + " h"));

			// 活动距离，以m为单位
			measurementList.add(new Measurement("距离:", String.format(
					"%.2f", map.get("distance")) + " m"));

			// 速度
			measurementList.add(new Measurement("速度:", String.format("%.2f",
					map.get("speed")) + " m/s"));

			// 热量消耗
			measurementList.add(new Measurement("卡路里消耗:", String.format(
					"%.2f", map.get("calories_burned")) + " kcal"));
			mAdapter = new RecentActivityAdapter(mContext, measurementList);
			mDisplay.setAdapter(mAdapter);
		}

	};

}
