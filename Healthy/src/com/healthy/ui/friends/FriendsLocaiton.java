package com.healthy.ui.friends;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.OverlayItem;
import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.healthy.R;
import com.healthy.logic.HealthyApplication;
import com.healthy.logic.RequestListener;
import com.healthy.logic.model.PersonNearBy;
import com.healthy.util.AsyncImageDownLoader;
import com.healthy.util.AsyncImageDownLoader.ImageCallback;

/**
 * 查看附近好友
 * @author zc
 */
public class FriendsLocaiton {
	
	private Context mContext;
	private View mLocationFriends;
	
	private MapView mMapView = null;
	private BMapManager mBMapManager = null;
	private LocationClient mLocationClient = null;// 定位服务相关
	private LocationClientOption mOption ;
	private static final String KEY = "39E3359ab5dccbb33dae0b5621dab52e";
	private OverlayItem item;
	private FriendsOverLay mFriendsLocation;
	private List<PersonNearBy> personList;
	private AsyncImageDownLoader mImageLoader;
	private List<OverlayItem> itemList;
	private Bitmap backgroundBmp;
	private Bitmap frontDefault;
	
	public FriendsLocaiton (Context context) {
		mContext = context;
		mBMapManager = new BMapManager(mContext);
		mBMapManager.init(KEY, null);
		mLocationFriends = LayoutInflater.from(mContext).inflate(R.layout.flipper_friends_location, null);
		mMapView = (MapView)mLocationFriends.findViewById(R.id.location_friends_map);
		lastLocation();
		backgroundBmp = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.map_avatar_frame_n);
		frontDefault = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.friend_ava_empty);
		
		initLocation();
	}
	
	public View getView(){
		return mLocationFriends;
	}
	
	private void lastLocation(){//起初显示上次的位置信息,等待定位结果
		SharedPreferences mSP = mContext.getSharedPreferences("location_cache", 0);
		int longitude = mSP.getInt("longitude", 0);
		int latitude = mSP.getInt("latitude", 0);
		GeoPoint mLastPoint = new GeoPoint(latitude, longitude);
		mMapView.getController().setZoom(17);
		mMapView.getController().setCenter(mLastPoint);
	}
	
	private void initLocation(){
		setLocationOption();
		mLocationClient = new LocationClient(mContext, mOption);
		mLocationClient.registerLocationListener(mBDListener);
		mLocationClient.start();
		mLocationClient.requestLocation();
	}
	
	private void initMapView(BDLocation location){
		//GeoPoint构造函数是（纬度，经度）
		GeoPoint mPoint = new GeoPoint( (int)(location.getLatitude()*1E6),(int)(location.getLongitude()*1E6));
		mMapView.setBuiltInZoomControls(true);
		mMapView.getController().setZoom(17);
		mMapView.getController().setCenter(mPoint);
		mMapView.getController().animateTo(mPoint);
		
		initNearbyFriends(mPoint);//得到附近好友信息
	}
	
	private void initOverlay(){
		itemList = new ArrayList<OverlayItem>();
		mFriendsLocation = new FriendsOverLay(mContext,null, mMapView);
		mMapView.getOverlays().clear();
		
		if(personList.size()>0){
			for(int i = 0 ; i < personList.size() ; i++){
				GeoPoint tempPoint = new GeoPoint(personList.get(i).getLatitude(), personList.get(i).getLongitude());
				item = new OverlayItem(tempPoint, personList.get(i).getUsername(), personList.get(i).getLastUpdateTime());
				item.setMarker(new BitmapDrawable(personList.get(i).getAvatar()));
				item.setAnchor(OverlayItem.ALIGN_BOTTON);
				itemList.add(item);
				getAvatarMarker(i);
			}
			mFriendsLocation.addItem(itemList);
		}
		mMapView.getOverlays().add(mFriendsLocation);
		mMapView.refresh();
	}
	
	private void getAvatarMarker(final int index){
		mImageLoader = new AsyncImageDownLoader();
		Bitmap bitmapCache = mImageLoader.loadImage(personList.get(index).getUsername(), new ImageCallback() {
			
			public void imageLoaded(Bitmap bitmap) {
				// TODO Auto-generated method stub
				if(bitmap!=null)
					itemList.get(index).setMarker(new BitmapDrawable(combinateImage(backgroundBmp, bitmap)));
				mFriendsLocation.updateItem(itemList.get(index));
				mMapView.refresh();
			}
		});
		
		if(bitmapCache!=null){
			itemList.get(index).setMarker(new BitmapDrawable(combinateImage(backgroundBmp, bitmapCache)));
			mFriendsLocation.updateItem(itemList.get(index));
		}
	}
	
	private void setLocationOption(){
		mOption = new LocationClientOption();
		mOption.setOpenGps(true);
		mOption.setPriority(LocationClientOption.GpsFirst);//优先GPS定位
		mOption.setCoorType("bd09ll");// 返回的结果为百度经纬度
		mOption.setScanSpan(2000);// 设置发起定位间隔时间为1分钟
		mOption.setAddrType("all");
		mOption.setProdName("healthy");
		mOption.disableCache(true);
	}
	
	private void initNearbyFriends(GeoPoint point){
		FriendsRequestParam params = new FriendsRequestParam(FriendsRequestParam.TASK_GET_PERSONS_NEARBY);
		params.addParam("longitude", point.getLongitudeE6());
		params.addParam("latitude", point.getLatitudeE6());
		params.addParam("radius", 3000);//搜索附近半径
		params.addParam("p", 0);
		params.addParam("psize", 20);//每页数量设置
		if(params.getTaskCategory()==FriendsRequestParam.TASK_GET_PERSONS_NEARBY)
			HealthyApplication.mAsyncHealthy.getPersonsNearBy(params, mReListener);
	}
	
	Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch(msg.what){
			case 0:
				break;
			case 1:
				personList = new ArrayList<PersonNearBy>();
				FriendsResponseBean bean = (FriendsResponseBean) msg.obj;
				if(bean.getResult()==FriendsResponseBean.ERROR){
					Toast.makeText(mContext, "获取附近好友失败,请重试！", Toast.LENGTH_SHORT).show();
				}else{
					if(!bean.getInfo().equals("")){
						
						try {
							JSONObject object = new JSONObject(bean.getInfo());
							JSONArray array = new JSONArray(object.getString("persons"));
							for(int i = 0 ; i < array.length() ; i++ ){
								JSONObject tempObject = array.getJSONObject(i);
								PersonNearBy person = new PersonNearBy();
								person.setUsername(tempObject.getString("name"));
								person.setLongitude(tempObject.getInt("longitude"));
								person.setLatitude(tempObject.getInt("latitude"));
								person.setLastUpdateTime(tempObject.getString("lastUpdate"));
								person.setAvatar(combinateImage(backgroundBmp, frontDefault));
								personList.add(person);
							}
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						initOverlay();
					}
				}
				break;
			}
		}
		
	};
	
	RequestListener<FriendsResponseBean> mReListener = new RequestListener<FriendsResponseBean>() {
		
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
	
	BDLocationListener mBDListener = new BDLocationListener() {
		
		@Override
		public void onReceivePoi(BDLocation location) {
				
		}
		
		@Override
		public void onReceiveLocation(BDLocation location) {
			// TODO Auto-generated method stub
			if(location!=null){
				Log.i("tag", "自身位置信息："+location.getLongitude()+"---"+location.getLatitude());
				initMapView(location);
				mLocationClient.stop();
				SharedPreferences mSp = mContext.getSharedPreferences("location_cache", 0);
				mSp.edit().putInt("longitude", (int)(location.getLongitude()*1E6)).commit();
				mSp.edit().putInt("latitude", (int)(location.getLatitude()*1E6)).commit();
			}	
		}
	};
	
	private Bitmap combinateImage(Bitmap background, Bitmap front){
		Bitmap bitmap = null;
		bitmap = AsyncImageDownLoader.combineDrawable(AsyncImageDownLoader.changeBitmapWH(backgroundBmp, getSize(75), getSize(86)), AsyncImageDownLoader.toRoundBitmap(front,getSize(67),getSize(67)));
		return bitmap;
	}
	
	//转换成适合自身分辨率大小的尺寸
	private int getSize(int size){
		float changeSize = size*mContext.getResources().getDisplayMetrics().density;
		return (int)changeSize;
	}
	public void mapDestroy(){
		mMapView.destroy();
		if(mBMapManager!=null){
			mBMapManager.destroy();
			mBMapManager = null;
		}
	}
	
	public void mapPaused(){
		mMapView.onPause();
		if(mBMapManager!=null){
			mBMapManager.stop();
		}
	}
	
	public void mapResume(){
		mMapView.onResume();
		if(mBMapManager!=null){
			mBMapManager.start();
		}
	}
}
