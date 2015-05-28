package com.healthy.ui.foods;

import java.util.List;
import java.util.Map;

import com.healthy.R;
import com.healthy.logic.HealthyApplication;
import com.healthy.logic.model.FoodInDb;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.TextView;

public class FoodListAdapter extends BaseExpandableListAdapter {

	private Context mContext = null;
	private List<Map<String, Object>> mGroup = null;
	private List<List<FoodInDb>> mChild = null;
	private LayoutInflater mInflater = null;
	private ViewUpdateListener mViewUpdateListener;

	public FoodListAdapter(Context context, List<Map<String, Object>> group,
			List<List<FoodInDb>> child) {
		mContext = context;
		mGroup = group;
		mChild = child;
		mInflater = LayoutInflater.from(mContext);
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return mChild.get(groupPosition).get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return childPosition;
	}

	@Override
	public View getChildView(final int groupPosition, final int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder = null;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.foodlist_child, null);
			holder = new ViewHolder();
			holder.mChildCalorie = (TextView) convertView// 食物热量
					.findViewById(R.id.food_calorie);
			holder.mChildName = (TextView) convertView// 食物名称
					.findViewById(R.id.food_name);  
			holder.mChildNum = (TextView) convertView// 食物质量
					.findViewById(R.id.food_num);
			holder.mChildBtn = (TextView) convertView// 删除按钮
					.findViewById(R.id.delete_btn);
			holder.mChildTime = (TextView) convertView.findViewById(R.id.food_time);// 饮食记录添加时间
			holder.mLowerLine = convertView.findViewById(R.id.lower_line);// 下半部分时间线
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		if (groupPosition == mGroup.size() - 1
				&& childPosition == mChild.get(groupPosition).size() - 1) {
			holder.mLowerLine.setVisibility(View.INVISIBLE);// 设定最后group最后一个child，下半部分时间线不可见
		} else {
			holder.mLowerLine.setVisibility(View.VISIBLE);
		}
		
		holder.mChildCalorie.setText(mChild.get(groupPosition).get(
				childPosition).calorie
				* mChild.get(groupPosition).get(childPosition).num / 100 + "kcal");
		holder.mChildName
				.setText(mChild.get(groupPosition).get(childPosition).name);
		holder.mChildNum
				.setText(mChild.get(groupPosition).get(childPosition).num + "g");
		holder.mChildTime.setText(mChild.get(groupPosition).get(childPosition).time);
		holder.mChildBtn.setOnClickListener(new OnClickListener() {// 删除当前食物记录

					@Override
					public void onClick(View v) {
						final AlertDialog alertDlg = new AlertDialog.Builder(
								mContext).create();
						alertDlg.show();
						Window window = alertDlg.getWindow();
						window.setContentView(R.layout.food_alertdialog);
						Button okBtn = (Button) window
								.findViewById(R.id.ok_btn);
						okBtn.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								// TODO
								if (mViewUpdateListener != null) {
									HealthyApplication.mDbUtil
											.deleteFood(mChild.get(groupPosition).remove(childPosition));
									mViewUpdateListener.updateView(false);
								}
								alertDlg.cancel();
							}
						});
						Button cancelBtn = (Button) window
								.findViewById(R.id.cancel_btn);
						cancelBtn.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								alertDlg.cancel();
							}
						});
					}
				});
		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		// TODO Auto-generated method stub
		return mChild.get(groupPosition).size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		// TODO Auto-generated method stub
		return mGroup.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		// TODO Auto-generated method stub
		return mGroup.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		// TODO Auto-generated method stub
		return groupPosition;
	}

	@Override
	public View getGroupView(final int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		
		ViewHolder holder = null;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.foodlist_group, null);
			holder = new ViewHolder();
			holder.mGroupName = (TextView) convertView
					.findViewById(R.id.foods_group_name);
			holder.mUpperLine = convertView.findViewById(R.id.upper_line);
			holder.mLowerLine=convertView.findViewById(R.id.lower_line);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		if (groupPosition == 0) {
			holder.mUpperLine.setVisibility(View.INVISIBLE);// 设置上半部分时间线不可见
		} else  holder.mUpperLine.setVisibility(View.VISIBLE);
		
		if (groupPosition==mGroup.size()-1 && mChild.get(groupPosition).size()==0){
			holder.mLowerLine.setVisibility(View.INVISIBLE);
		}else holder.mLowerLine.setVisibility(View.VISIBLE);
		
		holder.mGroupName.setText(mGroup.get(groupPosition).get("name")
				.toString());
		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return true;
	}

	private class ViewHolder {
		private TextView mGroupName;
		private TextView mChildCalorie;
		private TextView mChildName;
		private TextView mChildNum;
		private TextView mChildTime;
		private TextView mChildBtn;
		private View mUpperLine;
		private View mLowerLine;
	}

	public interface ViewUpdateListener {
		public abstract void updateView(boolean vieChange);
	}

	public void setViewUpdateListener(ViewUpdateListener viewUpdateListener) {
		this.mViewUpdateListener = viewUpdateListener;
	}

}
