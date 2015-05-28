package com.healthy.ui.friends;

import com.healthy.R;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ViewFlipper;

public class FriendsAdd extends Activity{
	private ImageView mBack;
	private ViewFlipper mContent;
	private ImageView mTitleSearchBtn;
	private ImageView mTitleLocationBtn;
	
	private FriendsSearch mSearchFriends;
	private FriendsLocaiton mLocationFriends;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_add_friends);
		
		mBack = (ImageView)findViewById(R.id.back_friends_btn);
		mContent = (ViewFlipper)findViewById(R.id.friends_add_flipper);
		mTitleSearchBtn = (ImageView)findViewById(R.id.title_search_btn);
		mTitleLocationBtn = (ImageView)findViewById(R.id.title_location_btn);
		
		mSearchFriends = new FriendsSearch(this);
		mLocationFriends = new FriendsLocaiton(this);
		ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
		mContent.addView(mLocationFriends.getView(), params);
		mContent.addView(mSearchFriends.getView(), params);
		
		setListener();
	}
	
	private void setListener(){
		mBack.setOnClickListener( new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
				overridePendingTransition(0,R.anim.roll_down);
			}
		});
		
		mTitleLocationBtn.setOnClickListener(titleListener);
		mTitleSearchBtn.setOnClickListener(titleListener);
	}
	
	OnClickListener titleListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			switch(v.getId()){
			case R.id.title_search_btn:
				mContent.setInAnimation(AnimationUtils
						.loadAnimation(FriendsAdd.this,
								R.anim.slide_in_right));
				mContent.setOutAnimation(AnimationUtils
						.loadAnimation(FriendsAdd.this,
								R.anim.slide_out_left));
				mContent.setDisplayedChild(1);
				break;
			case R.id.title_location_btn:
				mContent.setInAnimation(AnimationUtils
						.loadAnimation(FriendsAdd.this,
								R.anim.slide_in_left));
				mContent.setOutAnimation(AnimationUtils
						.loadAnimation(FriendsAdd.this,
								R.anim.slide_out_right));
				mContent.setDisplayedChild(0);
				break;
			}
			
		}
	};

	@Override
	protected void onDestroy() {
		overridePendingTransition(0,R.anim.roll_down);
		mLocationFriends.mapDestroy();
		super.onDestroy();
	}
	
	@Override
	protected void onPause() {
		overridePendingTransition(0,R.anim.roll_down);
		super.onPause();
	}
	
	
}
