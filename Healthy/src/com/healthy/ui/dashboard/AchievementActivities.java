package com.healthy.ui.dashboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.healthy.R;
import com.healthy.logic.HealthyApplication;
import com.healthy.logic.model.Achievement;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;

public class AchievementActivities {

	private Context mContext;
	private View mAchievement;
	private ListView mAchievementList;
	private ImageView mAchievementBtn;
	
	private List<Achievement> mAchievementData;
	
	private HashMap<String, HashMap<String, Object>> mDayMeasurements;//当天活动统计数据
	private HashMap<String,Object> mMeasurement;//某个活动的各项数据统计
	
	public AchievementActivities(Context context)
	{
		mContext = context;
		mAchievement = LayoutInflater.from(mContext).inflate(R.layout.page_achievement, null);
		mAchievementList = (ListView)mAchievement.findViewById(R.id.achievement_list);
		mAchievementBtn = (ImageView)mAchievement.findViewById(R.id.achievement_imagebtn);
		mAchievementData  = new ArrayList<Achievement>();
		
		mAchievementBtn.setOnClickListener( new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(mContext,AchievementIntroduceActivity.class);
				mContext.startActivity(intent);
				((Activity) mContext).overridePendingTransition(R.anim.roll_up,
						R.anim.roll);
			}
		});
		initFirst();
		initTrackerAchieve();
		initMovement();
		initListView();
	}
	
	public View getView()
	{
		return mAchievement;
	}
	
	/**
	 * 是否第一次使用--初出茅庐
	 * 临时每次都加载
	 */
	private void initFirst()
	{
		Achievement achievement = new Achievement("欢迎您来到健康达人-", "初出茅庐");
		mAchievementData.add(achievement);
	}
	
	/**
	 * 与轨迹相关的成就
	 * 1.成功记录第一条轨迹---轨迹新手
	 * 2.拥有50条轨迹---轨迹狂
	 * 3.拥有100条轨迹---轨迹达人
	 * 4.拥有500条轨迹---怀旧的人
	 */
	private void initTrackerAchieve(){
		int count=0;
		count = HealthyApplication.mDbUtil.getLastTrackerID();
		if(count > 0){
			Achievement achievement = new Achievement("恭喜您获得了成就-", "轨迹新手");
			mAchievementData.add(achievement);
			if(count >= 50){
				Achievement achievementTwo = new Achievement("恭喜您获得了成就-", "轨迹狂");
				mAchievementData.add(achievementTwo);
				if(count >= 100){
					Achievement achievementThree = new Achievement("恭喜您获得了成就-", "轨迹达人");
					mAchievementData.add(achievementThree);
					if(count >= 500){
						Achievement achievementFour = new Achievement("恭喜您获得了成就-", "怀旧的人");
						mAchievementData.add(achievementFour);
					}
				}
			}
		}
		
	}
	
	/**
	 * 运动系成就
	 * 1.静止超过8小时---稳坐如山
	 * 2.静止超过6小时---该运动了
	 * 2.走路超过6000步---散步达人（每天）
	 * 3.走路超过半小时---悠闲散步（每天）
	 * 4.走路超过1万步---健康达标(这个是最新报告说明)
	 * 5.走路超过800万---行万里路(这个不科学)
	 * 6.第一次跑步---跑步了
	 * 7.跑步半小时---跑步控（每天）
	 * 8.跑步累计超过100小时---真·跑步
	 * 注：累计的需要改动，这里时间较短，直接查询本地数据库
	 */
	private void initMovement()
	{
		Date date = new Date();
		mDayMeasurements = new HashMap<String, HashMap<String,Object>>();
		mDayMeasurements = HealthyApplication.mDbUtil.getDailyActivityData(DateFormat.format("yyyy-MM-dd", date).toString());
		if(mDayMeasurements!=null){
			if(mDayMeasurements.containsKey("stationary")){
				mMeasurement = mDayMeasurements.get("stationary");
				float time = Float.valueOf(mMeasurement.get("duration").toString());
				if(time>21600){
					Achievement achievement = new Achievement("可怜的您获得了成就-", "该运动了");
					mAchievementData.add(achievement);
				}
				if(time>28800){
					Achievement achievement = new Achievement("可怜的您获得了成就-", "稳坐如山");
					mAchievementData.add(achievement);
				}
			}
			if(mDayMeasurements.containsKey("walking")){
				mMeasurement = mDayMeasurements.get("walking");
				float stride = Float.valueOf(mMeasurement.get("strides").toString());
				if(stride>6000){
					Achievement achievement = new Achievement("恭喜您获得了成就-", "散步达人");
					mAchievementData.add(achievement);
				}
				if(stride>10000){
					Achievement achievement = new Achievement("恭喜您获得了成就-", "健康生活");
					mAchievementData.add(achievement);
				}
			}
			if(mDayMeasurements.containsKey("jogging")){
				mMeasurement = mDayMeasurements.get("jogging");
				float time = Float.valueOf(mMeasurement.get("duration").toString());
				if(time>0){
					Achievement achievement = new Achievement("恭喜您获得了成就-", "跑步了");
					mAchievementData.add(achievement);
				}
				if(time>1800){
					Achievement achievement = new Achievement("恭喜您获得了成就-", "跑步控");
					mAchievementData.add(achievement);
				}
			}
		}
	}
	
	private void initListView()
	{
		Collections.reverse(mAchievementData);//list倒序
		AchievementActivityAdapter adapter = new AchievementActivityAdapter(mContext, mAchievementData);
		mAchievementList.setAdapter(adapter);
	}
	
}
