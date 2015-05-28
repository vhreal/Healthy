package com.healthy.ui.friends;

import java.util.List;

import com.healthy.R;
import com.healthy.logic.model.RankingBean;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class RankingAdapter extends BaseAdapter{
	
	private Context mContext;
	private List<RankingBean> mRankingList;
	
	public RankingAdapter(Context context, List<RankingBean> rankingList){
		mContext = context;
		mRankingList = rankingList;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mRankingList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mRankingList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.item_ranking, null);
			holder.id = (TextView)convertView.findViewById(R.id.friends_ranking_number);
			holder.image = (ImageView)convertView.findViewById(R.id.friends_ranking_image);
			holder.username = (TextView)convertView.findViewById(R.id.friends_ranking_username);
			holder.calories = (TextView)convertView.findViewById(R.id.friends_ranking_kcal);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		RankingBean bean = mRankingList.get(position);
		holder.id.setText(bean.getId()+"");
		holder.image.setImageBitmap(bean.getAvatar());
		holder.username.setText(bean.getUsername());
		holder.calories.setText(bean.getCalories()+"kcal");
		
		return convertView;
	}
	
	static class ViewHolder{
		public TextView id;
		public ImageView image;
		public TextView username;
		public TextView calories;
	}
}
