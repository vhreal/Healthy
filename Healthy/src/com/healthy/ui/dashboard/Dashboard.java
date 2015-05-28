package com.healthy.ui.dashboard;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.healthy.R;
import com.healthy.logic.HealthyApplication;
import com.healthy.logic.model.FoodInDb;
import com.healthy.ui.base.FlipperLayout.OnOpenListener;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.ViewFlipper;
/**
 * Dashboard界面，包括两个子页面RecentActivity 和CaloriesDetail, zc
 * 
 */
public class Dashboard {

	private View mDashboard;
	private Context mContext;
	/*本月消耗摄入卡路里显示*/
	private TextView mConsumerText;
	private TextView mIntakeText;
	private TextView mSumText;
	
	private ViewFlipper mContent;//radiogroup屏幕切换实现
	private RadioGroup mAllCategory;
	private DayActivities mTodayActivity;
	private AchievementActivities mAchieveActivity;
	
	private ImageView mFlipMenu;
	private OnOpenListener mOnOpenListener;

	public Dashboard(Context context) {

		mContext = context;
		mDashboard = LayoutInflater.from(mContext).inflate(
				R.layout.page_dashboard, null);
		
		mContent = (ViewFlipper)mDashboard.findViewById(R.id.dashboard_content_flipper);
		mAllCategory = (RadioGroup)mDashboard.findViewById(R.id.dashboard_radiogroup);
		mConsumerText = (TextView)mDashboard.findViewById(R.id.dashboard_consume_sum);
		mIntakeText = (TextView)mDashboard.findViewById(R.id.dashboard_intake_sum);
		mSumText = (TextView)mDashboard.findViewById(R.id.dashboard_sum);
		mFlipMenu = (ImageView)mDashboard.findViewById(R.id.flip_menu);
		
		mTodayActivity = new DayActivities(mContext);
		mAchieveActivity = new AchievementActivities(mContext);
		ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,ViewGroup.LayoutParams.FILL_PARENT);
		
		mContent.addView(mTodayActivity.getView(), params);
		mContent.addView(mAchieveActivity.getView(), params);
		
		initSum();
		setListener();
		
	}

	public View getView() {
		return mDashboard;
	}
	
	private void setListener()
	{	
		mAllCategory.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
			
				switch(checkedId){
				case R.id.dashboard_movement_index_btn:
					mContent.setInAnimation(AnimationUtils
							.loadAnimation(mContext,
									R.anim.slide_in_left));
					mContent.setOutAnimation(AnimationUtils
							.loadAnimation(mContext,
									R.anim.slide_out_right));
					mContent.setDisplayedChild(0);
					break;
				case R.id.dashboard_achievement_btn:
					mContent.setInAnimation(AnimationUtils
							.loadAnimation(mContext,
									R.anim.slide_in_right));
					mContent.setOutAnimation(AnimationUtils
							.loadAnimation(mContext,
									R.anim.slide_out_left));
					mContent.setDisplayedChild(1);
					break;
				case R.id.dashboard_healthy_btn://暂时不用健康提示
					mContent.setInAnimation(AnimationUtils
							.loadAnimation(mContext,
									R.anim.slide_in_right));
					mContent.setOutAnimation(AnimationUtils
							.loadAnimation(mContext,
									R.anim.slide_out_left));
					mContent.setDisplayedChild(2);
					break;
				}
			}
		});
		mFlipMenu.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mOnOpenListener.open();
			}
		});
		
	}

	/**
	 * 计算当前月卡路里消耗和摄入量， 算出合计
	 */
	private void initSum() {
		Date date = new Date();
		DecimalFormat df = new DecimalFormat("0.00");
		// 计算消耗的变量
		HashMap<String, HashMap<String, Object>> measurements;// 存放一个月内的全部记录
		HashMap<String, Object> measurement;// 某一个运动的
		float caloriesBurned = 0;

		// 计算摄入的变量
		List<FoodInDb> foodList;
		float caloriesEat = 0;

		// 合计
		float caloriesResult = 0;
		// 当月消耗
		measurements = HealthyApplication.mDbUtil
				.getMonthActivityData(DateFormat.format("yyyy-MM-dd", date)
						.toString());
		if (measurements == null) {
			mConsumerText.setText("0kcal");
		} else {
			Iterator<Entry<String, HashMap<String, Object>>> iterator = measurements
					.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<String, HashMap<String, Object>> entry = iterator.next();
				measurement = entry.getValue();
				caloriesBurned += Float.valueOf(measurement.get(
						"calories_burned").toString());
			}
			HealthyApplication.calories = caloriesBurned;
			mConsumerText.setText(df.format(caloriesBurned) + "kcal");
		}

		// 当月摄入
		foodList = HealthyApplication.mDbUtil
				.queryMonthFood(new SimpleDateFormat("yyyy-MM-dd").format(date));// 国外月份从0开始，要加1
		if (foodList.size() == 0) {
			mIntakeText.setText("0kcal");
		} else {
			for (FoodInDb food : foodList) {
				caloriesEat += food.calorie * food.num / 100;
			}
			mIntakeText.setText(caloriesEat + "kcal");
		}

		caloriesResult = Float.valueOf(df.format(caloriesBurned - caloriesEat));
		mSumText.setText(caloriesResult + "kcal");
	}
	
	public void setOnOpenListener(OnOpenListener onOpenListener) {
		mOnOpenListener = onOpenListener;
	}
	
	
}
