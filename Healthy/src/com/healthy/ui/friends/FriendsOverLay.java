package com.healthy.ui.friends;

import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.map.ItemizedOverlay;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.OverlayItem;
import com.healthy.R;
import com.healthy.logic.HealthyApplication;
import com.healthy.logic.RequestListener;

public class FriendsOverLay extends ItemizedOverlay<OverlayItem> {

	private Context mContext;
	private MapView mMapView;
	private View mPopup;
	private PopupWindow mMapPop;
	private TextView mNamePop;
	private TextView mTimePop;
	private ImageView mAddPop;
	private ImageView mRemove;
	private String nameText;
	private String timeText;
	private int clickNumber;

	public FriendsOverLay(Context context, Drawable arg0, MapView arg1) {
		super(arg0, arg1);
		mContext = context;
		mMapView = arg1;
	}

	@Override
	protected int getIndexToDraw(int arg0) {
		// TODO Auto-generated method stub
		return super.getIndexToDraw(arg0);
	}

	@Override
	protected boolean onTap(int arg0) {
		// 点击是件处理
		nameText = getAllItem().get(arg0).getTitle().toString();
		timeText = getAllItem().get(arg0).getSnippet().toString();
		clickNumber = arg0;
		createPopup();
		return super.onTap(arg0);
	}

	@Override
	public void addItem(List<OverlayItem> arg0) {
		// TODO Auto-generated method stub
		super.addItem(arg0);
	}

	@Override
	public boolean removeItem(OverlayItem arg0) {
		return super.removeItem(arg0);
	}

	@Override
	public boolean updateItem(OverlayItem arg0) {
		// TODO Auto-generated method stub
		return super.updateItem(arg0);
	}

	private void createPopup() {
		mPopup = LayoutInflater.from(mMapView.getContext()).inflate(
				R.layout.popup_map_friends, null);
		mMapPop = new PopupWindow(mPopup, LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		mNamePop = (TextView) mPopup.findViewById(R.id.map_username_popup);
		mAddPop = (ImageView) mPopup.findViewById(R.id.map_add_friends);
		mTimePop = (TextView) mPopup.findViewById(R.id.map_time_popup);
		mRemove = (ImageView) mPopup.findViewById(R.id.map_item_remove);
		mMapPop.setContentView(mPopup);
		mMapPop.setFocusable(true);
		mMapPop.setBackgroundDrawable(new BitmapDrawable());
		mMapPop.showAtLocation(mMapView, Gravity.BOTTOM | Gravity.CENTER, 0, 0);

		mNamePop.setText(nameText);
		mTimePop.setText("上次登录:" + timeText);

		mRemove.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				removeItem(getAllItem().get(clickNumber));// 调用父类的方法
				mMapView.refresh();
				mMapPop.dismiss();
			}
		});

		mAddPop.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				mAddPop.setClickable(false);
				SharedPreferences sp = mContext.getSharedPreferences(
						"user_info", 0);
				String username = sp.getString("username", "");
				if (username.equalsIgnoreCase(nameText)) {
					Toast.makeText(mContext, "亲，不能添加自己！", Toast.LENGTH_SHORT)
							.show();
				} else {
					FriendsRequestParam params = new FriendsRequestParam(
							FriendsRequestParam.TASK_ADD_FRIENDS);
					params.addParam("username", nameText);
					if (params.getTaskCategory() == FriendsRequestParam.TASK_ADD_FRIENDS)
						HealthyApplication.mAsyncHealthy.addFriendsRequest(
								params,
								new RequestListener<FriendsResponseBean>() {

									@Override
									public void onStart() {

									}

									@Override
									public void onComplete(
											FriendsResponseBean bean) {
										Message msg = Message.obtain(handler);
										msg.what = 1;
										msg.obj = bean;
										msg.sendToTarget();
									}
								});
				}
			}

		});
	}

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 0:
				break;
			case 1:
				FriendsResponseBean bean = (FriendsResponseBean) msg.obj;
				if (bean.getResult() == FriendsResponseBean.ERROR) {// 任务执行失败

				} else {
					if (bean.toString().equalsIgnoreCase("好友请求已发送")) {
						Log.i("tag", "好友请求发送成功");
					}
				}

				break;
			}
		}
	};

}
