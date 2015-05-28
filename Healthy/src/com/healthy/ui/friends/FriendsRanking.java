package com.healthy.ui.friends;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.healthy.R;
import com.healthy.logic.HealthyApplication;
import com.healthy.logic.RequestListener;
import com.healthy.logic.model.RankingBean;
import com.healthy.util.AsyncImageDownLoader;
import com.healthy.util.AsyncImageDownLoader.ImageCallback;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

/**
 * 好友的卡路里消耗排名
 * @author zc
 *
 */
public class FriendsRanking {
	
	private Context mContext;
	private View mFriendsRanking;
	private ListView mRankingList;
	private List<RankingBean> mRankingListData;
	private AsyncImageDownLoader mImageLoader;
	private RankingAdapter mRankingAdapter;
	
	public FriendsRanking(Context context){
		mContext = context;
		mFriendsRanking = LayoutInflater.from(mContext).inflate(R.layout.activity_ranking, null);
		mRankingList = (ListView)mFriendsRanking.findViewById(R.id.friends_ranking_list);
		initListView();
	}
	
	private void initListView(){
		FriendsRequestParam params = new FriendsRequestParam(FriendsRequestParam.TASK_GET_FRIENDS_BY_CALORIES);
		params.addParam("p", 0);
		params.addParam("psize", 10);
		params.addParam("calories", HealthyApplication.calories);
		if(params.getTaskCategory()==FriendsRequestParam.TASK_GET_FRIENDS_BY_CALORIES)
			HealthyApplication.mAsyncHealthy.getFriendsByCalories(params, listener);
		
	}
	
	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case 0:// 开始任务，显示加载进度条

				break;
			case 1:// 任务结束，隐藏加载进度条，执行相关任务逻辑
				FriendsResponseBean bean = (FriendsResponseBean) msg.obj;
				if (bean.getResult() == FriendsResponseBean.ERROR) {// 任务执行失败
					Toast.makeText(mContext, "请重试！", Toast.LENGTH_SHORT).show();
				} else {
					if(bean.toString().equalsIgnoreCase("获得排名")){
						mRankingListData = new ArrayList<RankingBean>();
						Bitmap mDefaultBmp = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.friend_ava_empty);
						try {
							JSONObject object = new JSONObject(HealthyApplication.mRanking);
							JSONArray array = new JSONArray(object.getString("friends"));
							for(int i = 0 ; i < array.length();i++){
								JSONObject tempObject = array.getJSONObject(i);
								RankingBean tempBean = new RankingBean();
								tempBean.setId(i+1);
								tempBean.setUsername(tempObject.getString("name"));
								tempBean.setCalories(tempObject.getString("calories"));
								tempBean.setAvatar(changeImageStyle(mDefaultBmp));
								mRankingListData.add(tempBean);
								getAvatar(i);
							}
							mRankingAdapter= new RankingAdapter(mContext, mRankingListData);
							mRankingList.setAdapter(mRankingAdapter);
							mRankingAdapter.notifyDataSetChanged();
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}
				break;
			}
		}

	};
	
	private void getAvatar(final int index){
		mImageLoader = new AsyncImageDownLoader();
		Bitmap bitmapCache = mImageLoader.loadImage(mRankingListData.get(index).getUsername(), new ImageCallback() {
			
			public void imageLoaded(Bitmap bitmap) {
				if(bitmap!=null)
					mRankingListData.get(index).setAvatar(changeImageStyle(bitmap));
				mRankingAdapter.notifyDataSetChanged();
			}
		});
		
		if(bitmapCache!=null){
			mRankingListData.get(index).setAvatar(changeImageStyle(bitmapCache));
		}
			
	}
	
	private Bitmap changeImageStyle(Bitmap bitmap){
		Bitmap tempBitmap = AsyncImageDownLoader.toRoundCorner(AsyncImageDownLoader.changeBitmapWH(bitmap, 36, 36), 4);
		return tempBitmap;
	}
	
	RequestListener<FriendsResponseBean> listener = new RequestListener<FriendsResponseBean>() {

		@Override
		public void onStart() {
			// TODO Auto-generated method stub
			handler.sendEmptyMessage(0);
		}

		@Override
		public void onComplete(FriendsResponseBean bean) {
			// TODO Auto-generated method stub
			Message msg = Message.obtain(handler);
			msg.what = 1;
			msg.obj = bean;
			msg.sendToTarget();
		}
	};
	
	public View getView(){
		return mFriendsRanking;
	}
}
