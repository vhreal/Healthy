package com.healthy.ui.dashboard;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.healthy.R;
import com.healthy.logic.HealthyApplication;
import com.healthy.logic.model.FoodInDb;
import com.healthy.ui.base.LineChartView;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

/** 热量摄入支出情况 */
public class CaloriesDetail {

	private View mCaloriesDetail;
	private Context mContext;
	private LineChartView mLineChart;
	private Calendar mCalendar;
	private ImageView prevDate;
	private ImageView nextDate;
	private TextView curDate;

	public CaloriesDetail(Context context) {
		mContext = context;
		mCaloriesDetail = LayoutInflater.from(mContext).inflate(
				R.layout.page_calories_detail, null);

		mLineChart = (LineChartView) mCaloriesDetail
				.findViewById(R.id.line_chart);
		prevDate = (ImageView) mCaloriesDetail.findViewById(R.id.prev_date);
		nextDate = (ImageView) mCaloriesDetail.findViewById(R.id.next_date);
		curDate = (TextView) mCaloriesDetail.findViewById(R.id.cur_date);

		init();
		setListener();

	}

	/** 初始化与控件相关联的数据 */
	public void init() {
		mCalendar = Calendar.getInstance();
		curDate.setText(DateFormat.format("yyyy年", mCalendar.getTime()));
		setCalotrieValue();
	}

	private void setListener() {
		prevDate.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO 自动生成的方法存根
				mCalendar.add(Calendar.YEAR, -1);
				curDate.setText(DateFormat.format("yyyy年", mCalendar.getTime()));
				setCalotrieValue();
			}
		});
		nextDate.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO 自动生成的方法存根
				mCalendar.add(Calendar.YEAR, 1);
				curDate.setText(DateFormat.format("yyyy年", mCalendar.getTime()));
				setCalotrieValue();
			}
		});
	}

	public View getView() {
		return mCaloriesDetail;
	}

	/**
	 * 计算各月消耗与摄入值
	 */
	private void setCalotrieValue() {
		float[] calorieResult = new float[12];
		DecimalFormat df = new DecimalFormat("0.00");

		// 计算消耗的变量
		HashMap<String, HashMap<String, Object>> measurements;// 存放一个月内的全部记录
		HashMap<String, Object> measurement;// 某一个运动的
		float[] calorieBurned = new float[12];

		// 计算摄入的变量
		float[] calorieEat = new float[12];
		List<FoodInDb> foodList;// 一个月内的全部摄入食物数据

		for (int i = 0; i < calorieBurned.length; i++) {// 初始化数据
			calorieBurned[i] = 0;
			calorieEat[i] = 0;
		}

		for (int i = 0; i < 12; i++) {
			// 定位时间
			mCalendar.set(Calendar.MONTH, i);
			// 查询计算当月消耗
			measurements = new HashMap<String, HashMap<String, Object>>();
			measurements = HealthyApplication.mDbUtil
					.getMonthActivityData(DateFormat.format("yyyy-MM-dd",
							mCalendar.getTime()).toString());
			if (measurements != null) {// 本地存有当月热量消耗数据
				Iterator<Entry<String, HashMap<String, Object>>> iterator = measurements
						.entrySet().iterator();
				while (iterator.hasNext()) {
					Map.Entry<String, HashMap<String, Object>> entry = iterator
							.next();
					measurement = entry.getValue();
					calorieBurned[i] += Float.valueOf(measurement.get(
							"calories_burned").toString());
				}
			}

			// 查询计算当月摄入
			foodList = HealthyApplication.mDbUtil.queryMonthFood(mCalendar
					.get(Calendar.YEAR) + "-" + mCalendar.get(Calendar.MONTH));
			if (foodList != null) {// 本地存有热量摄入数据
				for (FoodInDb food : foodList) {
					calorieEat[i] += food.calorie * food.num / 100;
				}
			}

			calorieResult[i] = Float.valueOf(df.format(calorieBurned[i]
					- calorieEat[i]));// 消耗热量-摄入热量
		}
		mLineChart.setItemValues(calorieResult);
	}

}
