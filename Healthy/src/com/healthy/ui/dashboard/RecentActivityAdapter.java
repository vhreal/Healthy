package com.healthy.ui.dashboard;

import java.util.List;

import com.healthy.R;
import com.healthy.logic.model.Measurement;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class RecentActivityAdapter extends BaseAdapter {

	public Context mContext;
	public List<Measurement> mMeasurements;

	public RecentActivityAdapter(Context context, List<Measurement> measurements) {
		mContext = context;
		mMeasurements = measurements;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.item_recent_activities, null);
			holder.mCategory = (TextView) convertView
					.findViewById(R.id.category);
			holder.mMeasurement = (TextView) convertView
					.findViewById(R.id.measurement);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		Measurement measurement = mMeasurements.get(position);
		holder.mCategory.setText(measurement.mKey);
		holder.mMeasurement.setText(measurement.mValue);
		return convertView;
	}

	private class ViewHolder {
		private TextView mCategory;
		private TextView mMeasurement;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mMeasurements.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mMeasurements.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	public void changeData(List<Measurement> measurements) {
		mMeasurements = measurements;
		notifyDataSetChanged();
	}
}
