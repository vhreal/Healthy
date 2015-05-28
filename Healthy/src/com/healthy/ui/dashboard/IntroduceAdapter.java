package com.healthy.ui.dashboard;

import java.util.List;

import com.healthy.R;
import com.healthy.logic.model.Introduce;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class IntroduceAdapter extends BaseAdapter{

	private Context mContext;
	private List<Introduce> mIntroduce;
	
	public IntroduceAdapter (Context context, List<Introduce> introduce){
		mContext = context;
		mIntroduce = introduce;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mIntroduce.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mIntroduce.get(position);
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
			convertView = LayoutInflater.from(mContext).inflate(R.layout.item_introduce, null);
			holder.nameText = (TextView)convertView.findViewById(R.id.introduce_name);
			holder.describeText = (TextView)convertView.findViewById(R.id.introduce_describe);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		Introduce introduce = mIntroduce.get(position);
		holder.nameText.setText(introduce.getName());
		holder.describeText.setText(introduce.getDescribe());
		return convertView;
	}
	
	private class ViewHolder {
		TextView nameText;
		TextView describeText;
	}

}
