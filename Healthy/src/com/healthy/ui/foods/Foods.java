package com.healthy.ui.foods;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.healthy.R;
import com.healthy.logic.HealthyApplication;
import com.healthy.logic.model.FoodInDb;
import com.healthy.ui.base.FlipperLayout.OnOpenListener;
import com.healthy.ui.foods.FoodListAdapter.ViewUpdateListener;
import com.healthy.util.Constants;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.text.Spanned;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

/** 热量摄入输入界面 */
public class Foods implements ViewUpdateListener {

	private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat(
			"yyyy-MM-dd");
	private static final SimpleDateFormat TIME_FORMATTER = new SimpleDateFormat(
			"HH:mm");
	private static final String TEXT_FORMAT = "<font color='#00bfff'>%s</font> kcal";
	private static final DecimalFormat DF = new DecimalFormat("0.00");

	private View mFoods;
	private Context mContext;
	private ImageView mFlipMenu;
	private OnOpenListener mOnOpenListener;

	// 活动时间的调节与显示
	private ImageView mPreDate;
	private ImageView mNextDate;
	private TextView mCurDate;
	private Calendar mCalendar = Calendar.getInstance();
	private int isToday = 0;// 当为0的时候，为今天

	// 食物列表
	private ExpandableListView mFoodsList;
	private List<Map<String, Object>> mGroup = new ArrayList<Map<String, Object>>();// 吃饭时间
	private List<List<FoodInDb>> mChild = new ArrayList<List<FoodInDb>>();// 食物
	private float mTotalCalorie;
	private String[] mGroupName;
	private String[] mGroupCriteria;
	private FoodListAdapter mAdapter;

	private ImageView mFoodPlan;
	private ImageView mFoodAdd;// 添加食物按钮

	// 饮食计划
	private String mPlanStartDate = "";// 存储格式“yyyy-MM-dd”
	private int mPlanDuration;// 计划持续时间

	// 显示当前日期食物摄入与热量消耗情况
	private TextView mRemainingCalorie;// 显示还可以摄入多少热量的食物
	private float mCalorieOfPlan = 2000;// 计划每天摄入的热量
	private ImageView mFigCanStilEat;
	private int[] mFigIndexCanStilEat = { R.drawable.gauge_under,
			R.drawable.gauge_zone, R.drawable.gauge_over };
	private int mCurrentIndexCanStilEat = 0;

	public Foods(Context context) {
		mContext = context;
		mFoods = LayoutInflater.from(mContext).inflate(R.layout.page_foods,
				null);
		init();
		setListener();
	}

	public void init() {

		mPreDate = (ImageView) mFoods.findViewById(R.id.prev_date);
		mNextDate = (ImageView) mFoods.findViewById(R.id.next_date);
		mCurDate = (TextView) mFoods.findViewById(R.id.cur_date);
		mFoodsList = (ExpandableListView) mFoods.findViewById(R.id.foods_list);
		mFoodPlan = (ImageView) mFoods.findViewById(R.id.food_plan);
		mFoodAdd = (ImageView) mFoods.findViewById(R.id.food_add);
		mRemainingCalorie = (TextView) mFoods
				.findViewById(R.id.tip_can_stil_eat);
		mFigCanStilEat = (ImageView) mFoods
				.findViewById(R.id.fig_can_still_eat);

		mFlipMenu = (ImageView) mFoods.findViewById(R.id.flip_menu);

		// 获取当前日期，完成初始化
		Date date = new Date();
		mCalendar.setTime(date);
		mCurDate.setText(DateFormat.format("yyyy-MM-dd", date).toString());

		initData();
		showPlanCalori();

	}

	private void initData() {
		mGroupName = mContext.getResources().getStringArray(
				R.array.foods_group_names);
		mGroupCriteria = mContext.getResources().getStringArray(
				R.array.foods_group_criteria);  
		getGroupList();
		getChildList();
		mAdapter = new FoodListAdapter(mContext, mGroup, mChild);
		mAdapter.setViewUpdateListener(this);
		mFoodsList.setAdapter(mAdapter);
		for (int i = 0; i < mGroup.size(); i++) {// 将所有的子目录全部显示
			mFoodsList.expandGroup(i);
		}
		getTotalCalorie();
		queryFoodplan();
	}

	private void getGroupList() {
		for (int i = 0; i < mGroupName.length; i++) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("name", mGroupName[i]);
			map.put("time", mGroupCriteria[i]);
			mGroup.add(map);
		}
	}

	private void getChildList() {

		// 查询当日食物
		List<FoodInDb> list = HealthyApplication.mDbUtil
				.queryDayFood(DATE_FORMATTER.format(mCalendar.getTime()));

		List<FoodInDb> breakfast = new ArrayList<FoodInDb>();// 早餐队列
		List<FoodInDb> lunch = new ArrayList<FoodInDb>();// 午餐队列
		List<FoodInDb> dinner = new ArrayList<FoodInDb>();// 晚餐队列
		List<FoodInDb> midSnack = new ArrayList<FoodInDb>();// 夜宵队列
		mChild.add(breakfast);
		mChild.add(lunch);
		mChild.add(dinner);
		mChild.add(midSnack);

		// 根据不同的饮食时间，添加到不同的类目中
		for (FoodInDb food : list) {
			if (compareTime(food.time, mGroupCriteria[0]) < 0)
				breakfast.add(food);
			else if (compareTime(food.time, mGroupCriteria[1]) < 0)
				lunch.add(food);
			else if (compareTime(food.time, mGroupCriteria[2]) < 0)
				dinner.add(food);
			else
				midSnack.add(food);
		}

		getTotalCalorie();
	}

	/**
	 * 比较两个时间的先后
	 * 
	 * @param time1
	 * @param time2
	 * @return 如果time1晚发生，返回1，如果早发生返回-1，否则返回0
	 * 
	 * */
	private int compareTime(String time1, String time2) {
		try {
			Date date1 = TIME_FORMATTER.parse(time1);
			Date date2 = TIME_FORMATTER.parse(time2);
			if (date1.getTime() - date2.getTime() > 0)
				return 1;
			if (date1.getTime() - date2.getTime() < 0)
				return -1;
			return 0;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e("Healthy", "compareTime", e);
		}
		return 0;
	}

	/**
	 * 计算当天calorie总和
	 */
	private void getTotalCalorie() {
		mTotalCalorie = 0;
		for (List<FoodInDb> list : mChild) {
			for (FoodInDb mData : list) {
				mTotalCalorie += (mData.calorie * mData.num / 100);
			}
		}
	}

	public View getView() {
		return mFoods;
	}

	private void setListener() {

		/* 前一天 */
		mPreDate.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mCalendar.add(Calendar.DAY_OF_MONTH, -1);
				mCurDate.setText(DateFormat.format("yyyy-MM-dd",
						mCalendar.getTime()));
				updateFoodData(true);
				isToday--;
				if (isToday == 0)
					mFoodAdd.setVisibility(View.VISIBLE);
				else
					mFoodAdd.setVisibility(View.INVISIBLE);// 如果查看的不是当天记录，禁止添加食物
				showPlanCalori();
			}
		});

		/* 后一天 */
		mNextDate.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mCalendar.add(Calendar.DAY_OF_MONTH, 1);
				mCurDate.setText(DateFormat.format("yyyy-MM-dd",
						mCalendar.getTime()));
				updateFoodData(true);
				isToday++;
				if (isToday == 0)
					mFoodAdd.setVisibility(View.VISIBLE);
				else
					mFoodAdd.setVisibility(View.INVISIBLE);// 如果查看的不是当天记录，禁止添加食物
				showPlanCalori();
			}
		});

		// 添加饮食计划
		mFoodPlan.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO 自动生成的方法存根
				Intent newIntent = new Intent(mContext, FoodPlanActivity.class);
				((Activity) mContext).startActivityForResult(newIntent,
						Constants.ActivityRequestCode.FOODPLAN);
				((Activity) mContext).overridePendingTransition(R.anim.roll_up,
						R.anim.roll);
			}
		});

		// 添加食物
		mFoodAdd.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(mContext, FoodEditActivity.class);
				((Activity) mContext).startActivityForResult(intent,
						Constants.ActivityRequestCode.FOODADD);
				((Activity) mContext).overridePendingTransition(R.anim.roll_up,
						R.anim.roll);
			}
		});

		mFlipMenu.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mOnOpenListener.open();
			}
		});

		mFoodsList
				.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {

					@Override
					public boolean onGroupClick(ExpandableListView parent,
							View v, int groupPosition, long id) {
						// TODO Auto-generated method stub
						return true;
					}
				});
	}

	/**
	 * 剩余可摄入热量
	 * */
	private void showPlanCalori() {
		if (!checkPlanDuration()) {
			mCalorieOfPlan = 2000;
		}
		String tip_can_stil_eat;

		if (mTotalCalorie >= mCalorieOfPlan) {
			tip_can_stil_eat = String.format("已超出计划 " + TEXT_FORMAT,
					DF.format(mTotalCalorie - mCalorieOfPlan));
			mCurrentIndexCanStilEat = 2;
		} else {
			if (mTotalCalorie < 2.0 / 3.0 * mCalorieOfPlan)
				mCurrentIndexCanStilEat = 0;
			else
				mCurrentIndexCanStilEat = 1;
			tip_can_stil_eat = String.format("还可以摄入 " + TEXT_FORMAT,
					DF.format(mCalorieOfPlan - mTotalCalorie));
		}
		Spanned spt = Html.fromHtml(tip_can_stil_eat);
		mRemainingCalorie.setText(spt);
		mFigCanStilEat
				.setImageResource(mFigIndexCanStilEat[mCurrentIndexCanStilEat]);
	}

	/**
	 * 查询数据库中关于饮食计划部分
	 */
	private void queryFoodplan() {
		Map<String, Object> tempMap = new HashMap<String, Object>();
		tempMap = HealthyApplication.mDbUtil.queryFoodPlan();
		if (!tempMap.isEmpty()) {
			mPlanStartDate = tempMap.get("start_time").toString();
			mPlanDuration = Integer
					.parseInt(tempMap.get("duration").toString());
			mCalorieOfPlan = Float
					.valueOf(tempMap.get("daycalorie").toString());
		}
	}

	/**
	 * 更新数据
	 */
	public void updateFoodData(boolean viewChange) {
		mChild.clear();
		getChildList();
		mAdapter.notifyDataSetChanged();
		queryFoodplan();
		showPlanCalori();
	}

	/**
	 * 计算饮食计划剩余时间
	 */
	private boolean checkPlanDuration() {
		if (mPlanStartDate.equals("") || mPlanStartDate == null)
			return false;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date tempDate = null;
		long timeDiff;
		try {
			tempDate = sdf.parse(mPlanStartDate);
		} catch (ParseException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
		Calendar startCalendar = Calendar.getInstance();
		startCalendar.setTime(tempDate);
		timeDiff = (mCalendar.getTimeInMillis() - startCalendar
				.getTimeInMillis()) / (3600 * 1000 * 24);
		if (mPlanDuration >= timeDiff) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void updateView(boolean viewChange) {
		// TODO Auto-generated method stub
		updateFoodData(viewChange);
		Log.i("test", "update");
	}

	public void setPlanCalori(float calorieOfPlan) {
		mCalorieOfPlan = calorieOfPlan;
	}

	public void setOnOpenListener(OnOpenListener onOpenListener) {
		mOnOpenListener = onOpenListener;
	}
}
