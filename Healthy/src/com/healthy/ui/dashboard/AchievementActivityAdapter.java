package com.healthy.ui.dashboard;

import java.util.List;

import com.healthy.R;
import com.healthy.logic.model.Achievement;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class AchievementActivityAdapter extends BaseAdapter{
	
	private Context mContext;
	private List<Achievement> mAchievement;
	
	public AchievementActivityAdapter(Context context, List<Achievement> achievement)
	{
		mContext = context;
		mAchievement = achievement;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mAchievement.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mAchievement.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if(convertView == null){
			holder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.item_achievement, null);
			holder.preIntroduce = (TextView)convertView.findViewById(R.id.achievement_preintroduce);
			holder.achievement = (TextView)convertView.findViewById(R.id.achievement_text);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		Achievement achievement = mAchievement.get(position);
		holder.preIntroduce.setText(achievement.getPre());
		holder.achievement.setText(achievement.getAchievement());
		return convertView;
	}
	
	private class ViewHolder
	{
		TextView preIntroduce;
		TextView achievement;
	}
}
