package com.healthy.ui.menupanel;

import java.util.List;
import java.util.Map;

import com.healthy.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MenuPanelAdapter extends BaseExpandableListAdapter{

	private Context mContext = null;
	private List<Map<String, Object>> mGroup = null;
	private List<List<Map<String, Object>>> mChild = null;
	private LayoutInflater mInflater = null;
	
	public MenuPanelAdapter(Context context, List<Map<String, Object>> group,
			List<List<Map<String, Object>>> child) {
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
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder = null;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.menupanel_child, null);
			holder = new ViewHolder();
			holder.mChildIcon = (ImageView) convertView
					.findViewById(R.id.menupanel_child_lftimg);
			holder.mChildName = (TextView) convertView
					.findViewById(R.id.menupanel_child_name);
			holder.mNewMessage = (ImageView)convertView
					.findViewById(R.id.menupanel_icon_new);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		//设置菜单项的图标
		holder.mChildIcon.setImageDrawable(mContext.getResources().getDrawable(
				Integer.parseInt(mChild.get(groupPosition).get(childPosition)
						.get("icon").toString())));
		//设置new图标隐藏
		holder.mNewMessage.setVisibility(View.GONE);
		//设置菜单项的名字
		String childName = mChild.get(groupPosition).get(childPosition).get("name").toString();
		if (childName.equals("消息中心") && MenuPanel.hasNewMessage) {
			holder.mChildName.setText(childName);
			holder.mNewMessage.setVisibility(View.VISIBLE);
		}
		holder.mChildName.setText(childName);
		
		if (childPosition == MenuPanel.mChooesId && groupPosition == 0) {
			convertView.setBackgroundResource(R.drawable.bg_menupanel_child_selected);
		} else {
			convertView.setBackgroundResource(R.drawable.bg_menupanel_child_normal);
		}
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
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder = null;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.menupanel_group, null);
			holder = new ViewHolder();
			holder.mGroupName = (TextView) convertView
					.findViewById(R.id.menupanel_group_name);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
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
		private ImageView mChildIcon;
		private TextView mChildName;
		private ImageView mNewMessage;
	}

}
