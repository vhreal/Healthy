package com.healthy.ui.friends;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.healthy.R;
import com.healthy.logic.HealthyApplication;
import com.healthy.logic.RequestListener;
import com.healthy.logic.model.SearchResultBean;
import com.healthy.ui.base.LoadingView;
import com.healthy.util.AsyncImageDownLoader;
import com.healthy.util.AsyncImageDownLoader.ImageCallback;

public class FriendsSearch {

	private Context mContext;
	private View mSearchFriends;
	private EditText mFriendsName;
	private Button mFriendsSearch;
	private ListView mFriendsList;
	private ImageView mStartView;
	private LoadingView mSearchLoading;
	private SearchResultAdapter mSearchAdapter;
	private List<SearchResultBean> mSearchBeanList;
	private String mKeyWord;
	private AsyncImageDownLoader mImageLoader;

	public FriendsSearch(Context context) {
		mContext = context;
		mSearchFriends = LayoutInflater.from(mContext).inflate(
				R.layout.flipper_friends_search, null);
		mFriendsName = (EditText) mSearchFriends
				.findViewById(R.id.friends_search_edit);
		mFriendsSearch = (Button) mSearchFriends
				.findViewById(R.id.friends_search_btn);
		mFriendsList = (ListView) mSearchFriends
				.findViewById(R.id.friends_search_list);
		mStartView = (ImageView)mSearchFriends.findViewById(R.id.friends_search_no);
		mSearchLoading = (LoadingView)mSearchFriends.findViewById(R.id.friends_search_loading);
		setListener();
	}

	private void setListener() {
		mFriendsSearch.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mKeyWord = mFriendsName.getText().toString().trim();
				if (mKeyWord.equals("")) {
					Toast.makeText(mContext, "输入为空!", Toast.LENGTH_SHORT)
							.show();
				} else {
					mStartView.setVisibility(View.GONE);
					mSearchLoading.setVisibility(View.VISIBLE);
					FriendsRequestParam param = new FriendsRequestParam(
							FriendsRequestParam.TASK_GET_FRIENDS_BY_KEYWORD);
					param.addParam("keyword", mKeyWord);
					if (param.getTaskCategory() == FriendsRequestParam.TASK_GET_FRIENDS_BY_KEYWORD)
						HealthyApplication.mAsyncHealthy.getPersonsByKeyWord(
								param, listener);
				}
			}
		});
	}
	
	private void initListItem(){
		Bitmap mDefaultBmp = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.friend_ava_empty);
		mSearchBeanList = new ArrayList<SearchResultBean>();
		for(int i = 0; i<HealthyApplication.keyResult.size();i++){
			SearchResultBean mBean = new SearchResultBean();
			mBean.setUsername(HealthyApplication.keyResult.get(i));
			mBean.setAvatar(changeImageStyle(mDefaultBmp));
			mSearchBeanList.add(mBean);
			getAvatar(i);
		}
		
		mSearchAdapter = new SearchResultAdapter(mContext,mSearchBeanList);
		mFriendsList.setAdapter(mSearchAdapter);
		mSearchLoading.setVisibility(View.GONE);
		mFriendsList.setVisibility(View.VISIBLE);
	}
	
	private void getAvatar(final int index){
		mImageLoader = new AsyncImageDownLoader();
		Bitmap bitmapCache = mImageLoader.loadImage(mSearchBeanList.get(index).getUsername(), new ImageCallback() {
			
			public void imageLoaded(Bitmap bitmap) {
				if(bitmap!=null)
					mSearchBeanList.get(index).setAvatar(changeImageStyle(bitmap));
				mSearchAdapter.notifyDataSetChanged();
			}
		});
		
		if(bitmapCache!=null)
			mSearchBeanList.get(index).setAvatar(changeImageStyle(bitmapCache));
			
	}
	
	private Bitmap changeImageStyle(Bitmap bitmap){
		Bitmap tempBitmap = AsyncImageDownLoader.toRoundCorner(AsyncImageDownLoader.changeBitmapWH(bitmap, (int)42*HealthyApplication.phoneScale, (int)42*HealthyApplication.phoneScale), 4);
		return tempBitmap;
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
					if(bean.toString().equalsIgnoreCase("查找完毕")){
						initListItem();
					}
				}
				
				break;
			}
		}
	};
	
	RequestListener<FriendsResponseBean> listener = new RequestListener<FriendsResponseBean>() {

		@Override
		public void onStart() {
			handler.sendEmptyMessage(0);
		}

		@Override
		public void onComplete(FriendsResponseBean bean) {
			Message msg = Message.obtain(handler);
			msg.what = 1;
			msg.obj = bean;
			msg.sendToTarget();
		}
	};
	
	public View getView(){
		return mSearchFriends;
	}
}
