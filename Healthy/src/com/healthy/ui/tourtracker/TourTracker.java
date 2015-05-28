package com.healthy.ui.tourtracker;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.MKGeneralListener;
import com.baidu.mapapi.map.MKEvent;
import com.baidu.mapapi.utils.DistanceUtil;
import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.healthy.R;
import com.healthy.logic.HealthyApplication;
import com.healthy.logic.model.LocationInDb;
import com.healthy.logic.model.TrackerListBean;
import com.healthy.ui.base.FlipperLayout.OnOpenListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

/**
 * 运动估计跟踪界面
 * 
 * @author zc
 * */
public class TourTracker implements OnScrollListener {

	private View mTourTracker;
	private Context mContext;
	private OnOpenListener mOnOpenListener;
	private ImageView mFlipMenu;
	private LocationClient mLocationClient = null;// 定位服务相关
	private static final String KEY = "39E3359ab5dccbb33dae0b5621dab52e";
	private BMapManager mBMapManager = null;
	private int mIsInsertAdr = 0;
	private ListView mListView;
	private Button mTrackerBtn;
	private ImageView mNodataView;
	private ImageView mRefreshView;
	private ProgressBar mRefreshProgress;
	private TrackerListAdapter mTrackerListAdapter;

	private int mCount = 0;
	public Boolean mIsStart;
	private List<TrackerListBean> listItems = new ArrayList<TrackerListBean>();
	private List<TrackerListBean> mListItems = new ArrayList<TrackerListBean>();
	private List<GeoPoint> mPointList = new ArrayList<GeoPoint>();
	private String mDistance;
	private boolean mToBottom = false;
	private int deleteId;// 要删除的ID

	public TourTracker(Context context) {
		mContext = context;
		mTourTracker = LayoutInflater.from(mContext).inflate(
				R.layout.page_tour_tracker, null);
		mFlipMenu = (ImageView) mTourTracker
				.findViewById(R.id.tracker_flip_menu);
		mListView = (ListView) mTourTracker.findViewById(R.id.history_track);
		mTrackerBtn = (Button) mTourTracker.findViewById(R.id.start_track);
		mNodataView = (ImageView) mTourTracker.findViewById(R.id.nodata_view);
		mRefreshView = (ImageView) mTourTracker.findViewById(R.id.refresh_btn);
		mRefreshProgress = (ProgressBar) mTourTracker
				.findViewById(R.id.refresh_progress);
		mLocationClient = new LocationClient(mContext);
		mIsStart = false;

		initListView();
		setListener();

		mListView.setOnScrollListener(this);
	}

	private void initLocation() {
		mLocationClient.registerLocationListener(new BDLocationListener() {

			@Override
			public void onReceivePoi(BDLocation arg0) {

			}

			@Override
			public void onReceiveLocation(BDLocation location) {
				if (location == null)
					Toast.makeText(mContext, "获取位置信息失败，请检查网络！",
							Toast.LENGTH_SHORT).show();
				if (mIsInsertAdr == 0) {
					// 将第一次定位地址插入到tracker_info表中的start_address字段
					String adr = location.getAddrStr();
					HealthyApplication.mDbUtil.updateTrackerAdr(adr);
					mIsInsertAdr++;
				}
				Log.i("tag",
						location.getLongitude() + "-----"
								+ location.getLatitude());
				int id = HealthyApplication.mDbUtil.getLastTrackerID();
				LocationInDb locationInDb = new LocationInDb();
				locationInDb.setId(id);
				locationInDb.setLongitude(location.getLongitude());
				locationInDb.setLatitude(location.getLatitude());
				locationInDb.setTime(getNowtime());
				HealthyApplication.mDbUtil.insertIntoLocation(locationInDb);
			}
		});
	}

	private void setListener() {

		mFlipMenu.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				if (mOnOpenListener != null) {
					mOnOpenListener.open();
				}
			}
		});

		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				int id = listItems.get(arg2).getId();
				Intent intent = new Intent();
				intent.setClass(mContext, HistoryTrackerActivity.class);
				Bundle bundle = new Bundle();
				bundle.putInt("id", id);
				intent.putExtras(bundle);
				mContext.startActivity(intent);
				((Activity) mContext).overridePendingTransition(R.anim.roll_up,
						R.anim.roll);
			}
		});

		mListView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				deleteId = listItems.get(arg2).getId();
				showDialog();
				return true;
			}
		});

		mTrackerBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!mIsStart) {
					HealthyApplication.mDbUtil.insertNullData();
					mTrackerBtn.setText(R.string.stop_tracker);
					requestLocation();
					initBMap();
					mIsStart = true;
				} else {
					mTrackerBtn.setText(R.string.start_tracker);
					mIsStart = false;
					if (!HealthyApplication.mDbUtil.IsStartLocation()) {
						HealthyApplication.mDbUtil.deleteTracker();
						Toast.makeText(mContext, "亲，还没开始记录就结束了，太快了吧！",
								Toast.LENGTH_SHORT).show();
					} else {
						mLocationClient.stop();
						hd.sendEmptyMessage(0);
					}
					mIsInsertAdr = 0;
				}
			}
		});

		mRefreshView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				refreshTrackerData();
			}
		});
	}

	private void requestLocation() {
		initLocation();
		setLocationOption();
		mLocationClient.start();
		mLocationClient.requestLocation();
	}

	public void setOnOpenListener(OnOpenListener onOpenListener) {
		mOnOpenListener = onOpenListener;
	}

	private void refreshTrackerData() {

		mRefreshView.setVisibility(View.GONE);
		mRefreshProgress.setVisibility(View.VISIBLE);
		listItems.clear();
		mListItems = HealthyApplication.mDbUtil.getTrackerToList();
		mCount = mListItems.size();
		if (mCount == 0) {
			mListView.setVisibility(View.GONE);
			mNodataView.setVisibility(View.VISIBLE);
		} else {
			mListView.setVisibility(View.VISIBLE);
			mNodataView.setVisibility(View.GONE);
			if (mCount >= 5) {
				for (int i = 0; i < 5; i++)
					listItems.add(mListItems.get(i));
			} else if (0 < mCount && mCount < 5) {
				for (int i = 0; i < mCount; i++)
					listItems.add(mListItems.get(i));
			}
			mTrackerListAdapter = new TrackerListAdapter(mContext, listItems);
			mListView.setAdapter(mTrackerListAdapter);
			mTrackerListAdapter.notifyDataSetChanged();
		}

		mRefreshView.setVisibility(View.VISIBLE);
		mRefreshProgress.setVisibility(View.GONE);
	}

	Handler hd = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:// 停止记录轨迹
				HealthyApplication.mDbUtil.updateTrackerEndTime(getNowtime());
				mPointList = HealthyApplication.mDbUtil.getFromLocation();
				HealthyApplication.mDbUtil.getTrackerType();
				mDistance = computeDistance(mPointList);
				HealthyApplication.mDbUtil.updateTrackerDistance(mDistance);
				refreshTrackerData();
				break;
			case 1:
				loadMoreData();
				mTrackerListAdapter.notifyDataSetChanged();
				break;
			}
		}
	};

	public View getView() {
		return mTourTracker;
	}

	/**
	 * 得到当前的时间，格式yyyy-MM-dd HH:mm:ss
	 * 
	 * @return 当前时间字符串
	 */
	public String getNowtime() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
		String date = formatter.format(curDate);
		return date;
	}

	// 设置定位相关参数
	public void setLocationOption() {
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);// 打开GPS
		option.setAddrType("all");// 返回地址信息
		option.setCoorType("bd09ll");// 返回的结果为百度经纬度
		option.setScanSpan(10000);// 设置发起定位间隔时间为10S
		mLocationClient.setLocOption(option);
	}

	/**
	 * 运动轨迹listview初始化
	 */
	private void initListView() {
		listItems.clear();
		mListItems = HealthyApplication.mDbUtil.getTrackerToList();
		mCount = mListItems.size();
		Log.i("tag", "mCount:" + mCount);
		if (mCount == 0) {
			mListView.setVisibility(View.GONE);
			mNodataView.setVisibility(View.VISIBLE);
		} else if (mCount >= 5) {
			for (int i = 0; i < 5; i++)
				listItems.add(mListItems.get(i));
		} else if (0 < mCount && mCount < 5) {
			for (int i = 0; i < mCount; i++)
				listItems.add(mListItems.get(i));
		}
		mTrackerListAdapter = new TrackerListAdapter(mContext, listItems);
		mListView.setAdapter(mTrackerListAdapter);
	}

	/**
	 * 加载 更多数据
	 */
	private void loadMoreData() {
		int count = mTrackerListAdapter.getCount();
		if (count + 5 <= mCount) {
			for (int i = count; i < count + 5; i++) {
				mTrackerListAdapter.addTrackerItems(mListItems.get(i));
			}
		} else {
			for (int i = count; i < mCount; i++) {
				mTrackerListAdapter.addTrackerItems(mListItems.get(i));
			}
		}
		if (mTrackerListAdapter.getCount() == mListItems.size()) {
		}
		// 可添加listview底部事件
	}

	/**
	 * 百度地图初始化
	 */
	public void initBMap() {
		if (mBMapManager == null) {
			mBMapManager = new BMapManager(mContext);
			mBMapManager.init(KEY, new MKGeneralListener() {

				@Override
				public void onGetPermissionState(int error) {
					// TODO Auto-generated method stub
					if (error == MKEvent.ERROR_PERMISSION_DENIED) {
						// 授权Key错误：
						Toast.makeText(mContext,
								"请在 DemoApplication.java文件输入正确的授权Key！",
								Toast.LENGTH_LONG).show();
					}
				}

				@Override
				public void onGetNetworkState(int error) {
					// TODO Auto-generated method stub
					if (error == MKEvent.ERROR_NETWORK_CONNECT) {
						Toast.makeText(mContext, "您的网络出错啦！", Toast.LENGTH_LONG)
								.show();
					} else if (error == MKEvent.ERROR_NETWORK_DATA) {
						Toast.makeText(mContext, "输入正确的检索条件！",
								Toast.LENGTH_LONG).show();
					}
				}
			});
		}
	}

	/**
	 * 用于计算一次轨迹追踪各个百度经纬度直接的距离
	 */
	public String computeDistance(List<GeoPoint> point) {
		double distance = 0;
		String sDistance = "0米";
		for (int i = 0; i < point.size(); i++) {
			Log.i("geopoint", point.get(i).getLongitudeE6() + ","
					+ point.get(i).getLatitudeE6());
		}
		if (mPointList.size() <= 1) {
			return sDistance;
		} else {
			for (int i = 0; i < mPointList.size() - 1; i++) {
				GeoPoint mPointOne = new GeoPoint(mPointList.get(i)
						.getLongitudeE6(), mPointList.get(i).getLatitudeE6());
				GeoPoint mPointTwo = new GeoPoint(mPointList.get(i++)
						.getLongitudeE6(), mPointList.get(i++).getLatitudeE6());
				double temp = DistanceUtil.getDistance(mPointOne, mPointTwo);
				distance += temp;
				Log.i("distance", "测距！！————————>" + temp + "");
			}
		}
		if (distance < 1000)// 判断测距结果不到1000显示单位为米 ，大于一千单位为千米
		{
			sDistance = (int) distance + "米";
		} else {
			double distanceKM = distance / 1000;
			BigDecimal bg = new BigDecimal(distanceKM);// 保留小数后一位
			double distanceTwo = bg.setScale(1, BigDecimal.ROUND_HALF_UP)
					.doubleValue();
			sDistance = distanceTwo + "千米";
		}

		return sDistance;

	}

	private void showDialog() {
		new AlertDialog.Builder(mContext).setTitle("删除轨迹")
				.setMessage("您确定删除这条轨迹？")
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						Log.i("tag", "要删除记录编号是:" + deleteId);
						HealthyApplication.mDbUtil.deleteTrackerById(deleteId);//删除长按条目内容
						
						dialog.dismiss();
						mTrackerListAdapter.notifyDataSetChanged();
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).show();
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		int lastVisibleItemIndex = firstVisibleItem + visibleItemCount - 1;// 获取最后一个可见Item的索引(0-based)
		if (totalItemCount - 1 == lastVisibleItemIndex) {
			mToBottom = true;
		} else {
			mToBottom = false;
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (scrollState == SCROLL_STATE_IDLE && mToBottom) {
			hd.sendEmptyMessage(1);
		}
	}

	public void overTracker() {
		if (!HealthyApplication.mDbUtil.IsStartLocation()) {
			HealthyApplication.mDbUtil.deleteTracker();
		} else {
			mLocationClient.stop();
			hd.sendEmptyMessage(0);
		}
	}
}
