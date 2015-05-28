package com.healthy.ui.dashboard;

import com.healthy.R;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.ViewFlipper;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class ChartDataActivity extends Activity {

	private CaloriesDetail mCaloriesDetail;
	private RecentActivities mRecentActivities;
	private RadioGroup mDashboardCategory;
	private ImageView mBack;
	private ViewFlipper mContent;
	private View mChartData;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_recent_chart);
		
		LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mChartData = inflater.inflate(R.layout.activity_recent_chart, null);
		mContent = (ViewFlipper) findViewById(R.id.content);
		mDashboardCategory = (RadioGroup) findViewById(R.id.dashboard_category);
		mBack = (ImageView)findViewById(R.id.back_chart_btn);
		
		mRecentActivities = new RecentActivities(this);
		mCaloriesDetail = new CaloriesDetail(this);
		ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.FILL_PARENT);

		mContent.addView(mRecentActivities.getView(), params);
		mContent.addView(mCaloriesDetail.getView(), params);

		setListener();
		
	}

	private void setListener() {

		mDashboardCategory
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						// TODO Auto-generated method stub
						switch (checkedId) {
						case R.id.dashboard_recent_activities:
							mContent.setInAnimation(AnimationUtils
									.loadAnimation(ChartDataActivity.this,
											R.anim.slide_in_left));
							mContent.setOutAnimation(AnimationUtils
									.loadAnimation(ChartDataActivity.this,
											R.anim.slide_out_right));
							mContent.setDisplayedChild(0);

							break;
						case R.id.dashboard_calories_detail:
							mContent.setInAnimation(AnimationUtils
									.loadAnimation(ChartDataActivity.this,
											R.anim.slide_in_right));
							mContent.setOutAnimation(AnimationUtils
									.loadAnimation(ChartDataActivity.this,
											R.anim.slide_out_left));
							mContent.setDisplayedChild(1);

							break;
						}
					}
				});
		mBack.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ChartDataActivity.this.finish();
				overridePendingTransition(0,R.anim.roll_down);
			}
		});
	}
	
	public View getView() {
		return mChartData;
	}
	
	@Override
	protected void onPause() {
		overridePendingTransition(0,R.anim.roll_down);
		super.onPause();
	}

	@Override
	protected void onResume() {
        overridePendingTransition(R.anim.roll_up,R.anim.roll);
		super.onResume();
	}
}
