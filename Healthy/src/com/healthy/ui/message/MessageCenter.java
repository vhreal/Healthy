package com.healthy.ui.message;

import java.util.ArrayList;
import java.util.List;

import org.jivesoftware.smack.packet.Packet;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.view.ViewPager.LayoutParams;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.healthy.R;
import com.healthy.logic.HealthyApplication;
import com.healthy.logic.RequestListener;
import com.healthy.ui.MainActivity;
import com.healthy.ui.base.FlipperLayout.OnOpenListener;
import com.healthy.ui.friends.FriendsRequestParam;
import com.healthy.ui.friends.FriendsResponseBean;
import com.healthy.ui.menupanel.MenuPanel;
import com.healthy.util.HealthyUtil;
import com.healthy.util.HealthyUtil.SubscribePacketListener;

public class MessageCenter {

	private Context mContext;
	private View mMessageCenter;
	private ListView mMessageLV;
	private ImageView mFlipMenu;
	private OnOpenListener mOnOpenListener;
	private List<String> mMessageList;
	private PopupWindow mMessagePopup;
	private LinearLayout mMessageEmpty;
	private String mMessageFrom;
	private String mMessageClick;
	private int mClickNumber;
	private MessageAdapter mMessageAdapter;

	// popupwindow
	private View mPopup;
	private Button mAcceptBtn, mRefuseBtn, mDismissBtn;

	public MessageCenter(Context context) {
		mContext = context;
		mMessageCenter = LayoutInflater.from(mContext).inflate(
				R.layout.page_message, null);

		mMessageLV = (ListView) mMessageCenter.findViewById(R.id.message_list);
		mFlipMenu = (ImageView) mMessageCenter.findViewById(R.id.flip_menu);
		mMessageEmpty = (LinearLayout) mMessageCenter.findViewById(R.id.message_empty_layout);

		mMessageList = new ArrayList<String>();
		HealthyUtil.getInstance().addSubscribePacketListtener(listener);

		setListener();

	}

	private void setListener() {
		mFlipMenu.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mOnOpenListener.open();
			}
		});

		mMessageLV.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				mClickNumber = arg2;
				mMessageClick = arg0.getItemAtPosition(arg2).toString();// 得到所点击item的用户名
				createPopup();
			}
		});
	}

	private void initLV() {
		if (mMessageList.size() == 0) {
			mMessageEmpty.setVisibility(View.VISIBLE);
		} else {
			mMessageEmpty.setVisibility(View.GONE);
			mMessageAdapter = new MessageAdapter(mContext, mMessageList);
			mMessageLV.setAdapter(mMessageAdapter);
		}

	}

	public View getView() {
		if (HealthyUtil.getInstance().getLoginedUser() == null) {
			Log.i("tag", "没有已登录用户");
			mMessageEmpty.setVisibility(View.VISIBLE);
		} else {
			initLV();
		}
		return mMessageCenter;
	}

	public void setOnOpenListener(OnOpenListener onOpenListener) {
		mOnOpenListener = onOpenListener;
	}

	OnClickListener itemListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			mMessagePopup.dismiss();
			switch (v.getId()) {
			case R.id.message_accept:
				mMessageList.remove(mClickNumber);
				mMessageAdapter.notifyDataSetChanged();
				FriendsRequestParam paramsAccept = new FriendsRequestParam(
						FriendsRequestParam.TASK_ACCEPT_FRIENDS);
				paramsAccept.addParam("username", mMessageClick);
				if (paramsAccept.getTaskCategory() == FriendsRequestParam.TASK_ACCEPT_FRIENDS)
					HealthyApplication.mAsyncHealthy.acceptFriendsRequest(
							paramsAccept, requestListener);
				break;
			case R.id.message_refuse:
				mMessageList.remove(mClickNumber);
				mMessageAdapter.notifyDataSetChanged();
				FriendsRequestParam paramsRefuse = new FriendsRequestParam(
						FriendsRequestParam.TASK_REFUSE_FRIENDS);
				paramsRefuse.addParam("username", mMessageClick);
				if (paramsRefuse.getTaskCategory() == FriendsRequestParam.TASK_REFUSE_FRIENDS)
					HealthyApplication.mAsyncHealthy.refuseFriendsRequest(
							paramsRefuse, requestListener);
				break;
			default:
				break;
			}

		}
	};

	SubscribePacketListener listener = new SubscribePacketListener() {

		@Override
		public void processPacket(Packet packet) {// 目前只有添加好友的信息
			Log.i("tag", "接收到信息" + packet.toString());
			String[] messageFrom = packet.getFrom().split("@");
			mMessageFrom = messageFrom[0];
			mMessageList.add(mMessageFrom);

			notifyNewMessage();
		}
	};

	private void notifyNewMessage() {
		NotificationManager notificationManager = (NotificationManager) mContext
				.getSystemService(Activity.NOTIFICATION_SERVICE);
		Notification notification = new Notification(R.drawable.ic_application,
				"您有一条新的消息！", System.currentTimeMillis());
		Intent intent = new Intent(mContext, MainActivity.class);
		intent.putExtra("info", "friendrequest");
		PendingIntent pi = PendingIntent.getActivity(mContext, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		notification.setLatestEventInfo(mContext, "新消息", "您有一条新的好友请求", pi);
		notification.defaults = Notification.DEFAULT_ALL;
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notificationManager.notify(R.string.app_name, notification);
		MenuPanel.hasNewMessage = true;
		mContext.sendBroadcast(new Intent("com.healthy.action.messages"));
	}

	private void createPopup() {
		mPopup = LayoutInflater.from(mContext).inflate(R.layout.popup_message,
				null);
		mMessagePopup = new PopupWindow(mPopup, LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT);
		mMessagePopup.setContentView(mPopup);
		mAcceptBtn = (Button) mPopup.findViewById(R.id.message_accept);
		mRefuseBtn = (Button) mPopup.findViewById(R.id.message_refuse);
		mDismissBtn = (Button) mPopup.findViewById(R.id.message_dismiss);

		mDismissBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mMessagePopup.dismiss();
			}
		});

		mAcceptBtn.setOnClickListener(itemListener);
		mRefuseBtn.setOnClickListener(itemListener);

		mMessagePopup.setFocusable(true);// 设置可点击
		mMessagePopup.setWidth(LayoutParams.FILL_PARENT);
		mMessagePopup.setHeight(LayoutParams.WRAP_CONTENT);
		mMessagePopup.setBackgroundDrawable(new BitmapDrawable());
		mMessagePopup.showAtLocation(mMessageCenter, Gravity.BOTTOM
				| Gravity.CENTER_HORIZONTAL, 0, 0);
		// 设置空背景点击其他地方可消失

	}

	RequestListener<FriendsResponseBean> requestListener = new RequestListener<FriendsResponseBean>() {

		@Override
		public void onStart() {
			// TODO Auto-generated method stub
		}

		@Override
		public void onComplete(FriendsResponseBean bean) {
			// TODO Auto-generated method stub
		}
	};
}
