package com.healthy.ui.message;

import java.util.List;

import com.healthy.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class MessageAdapter extends BaseAdapter{
	
	private Context mContext;
	private List<String> mMessage;
	
	public MessageAdapter (Context context, List<String> message){
		mContext = context;
		mMessage = message;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mMessage.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mMessage.get(position);
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
			convertView = LayoutInflater.from(mContext).inflate(R.layout.item_message, null);
			holder.usernameText = (TextView)convertView.findViewById(R.id.message_from_name);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		String nameText = mMessage.get(position);
		holder.usernameText.setText(nameText);
		return convertView;
	}
	
	static class ViewHolder{
		TextView usernameText;
	}

}
