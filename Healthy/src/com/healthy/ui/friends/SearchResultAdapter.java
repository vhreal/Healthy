package com.healthy.ui.friends;

import java.util.List;

import com.healthy.R;
import com.healthy.logic.HealthyApplication;
import com.healthy.logic.RequestListener;
import com.healthy.logic.model.SearchResultBean;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class SearchResultAdapter extends BaseAdapter {

	private Context mContext;
	private List<SearchResultBean> mResult;

	public SearchResultAdapter(Context context, List<SearchResultBean> result) {
		mContext = context;
		mResult = result;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mResult.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mResult.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.item_search_result, null);
			holder.avatarImage = (ImageView) convertView
					.findViewById(R.id.friends_search_avatar);
			holder.nameText = (TextView) convertView
					.findViewById(R.id.friends_search_name);
			holder.addBtn = (ImageView)convertView.findViewById(R.id.friends_search_add);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		SearchResultBean sr = mResult.get(position);
		final String selectedID = mResult.get(position).getUsername();
		holder.avatarImage.setImageBitmap(sr.getAvatar());
		holder.nameText.setText(sr.getUsername());
		holder.addBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				holder.addBtn.setBackgroundResource(R.drawable.rec_friend_added);
				holder.addBtn.setClickable(false);
				SharedPreferences sp = mContext.getSharedPreferences("user_info", 0);
				String username = sp.getString("username", "");
				if(username.equals(selectedID)){
					Toast.makeText(mContext, "亲，不能添加自己！", Toast.LENGTH_SHORT).show();
				}else{
					FriendsRequestParam params = new FriendsRequestParam(FriendsRequestParam.TASK_ADD_FRIENDS);
					Log.i("tag", "添加的好友是:"+selectedID);
					params.addParam("username", selectedID);
					if(params.getTaskCategory()==FriendsRequestParam.TASK_ADD_FRIENDS)
						HealthyApplication.mAsyncHealthy.addFriendsRequest(params, new RequestListener<FriendsResponseBean>() {
							
							@Override
							public void onStart() {
								
							}
							
							@Override
							public void onComplete(FriendsResponseBean bean) {
								Message msg = Message.obtain(handler);
								msg.what = 1;
								msg.obj = bean;
								msg.sendToTarget();
							}
						});
				}
				}
				
		});
		return convertView;
	}

	final static class ViewHolder {
		TextView nameText;
		ImageView avatarImage;
		ImageView addBtn;
	}
	
	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch(msg.what){
			case 0:
				break;
			case 1:
				FriendsResponseBean bean = (FriendsResponseBean)msg.obj;
				if(bean.getResult()==FriendsResponseBean.ERROR){//任务执行失败
					
				}else{
					if(bean.toString().equalsIgnoreCase("好友请求已发送")){
						Log.i("tag", "好友请求发送成功");
					}
				}
				
				break;
			}
		}
	};

}
