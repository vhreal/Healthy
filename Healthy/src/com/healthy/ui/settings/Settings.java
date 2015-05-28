package com.healthy.ui.settings;

import com.healthy.R;
import com.healthy.ui.base.FlipperLayout.OnOpenListener;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class Settings {

	private Context mContext;
	private View mSettings;
	private LinearLayout mPersonalSettings;

	private ImageView mFlipMenu;
	private OnOpenListener mOnOpenListener;

	public Settings(Context context) {
		mContext = context;
		
		mSettings = LayoutInflater.from(mContext).inflate(
				R.layout.page_settings, null);
		mPersonalSettings = (LinearLayout) mSettings
				.findViewById(R.id.personal_settings);
		mFlipMenu = (ImageView) mSettings.findViewById(R.id.flip_menu);

		mPersonalSettings.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(mContext, PersonalSettings.class);
				mContext.startActivity(intent);
			}
		});

		mFlipMenu.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mOnOpenListener.open();
			}
		});
	}

	public View getView() {
		return mSettings;
	}

	public void setOnOpenListener(OnOpenListener onOpenListener) {
		mOnOpenListener = onOpenListener;
	}
}
