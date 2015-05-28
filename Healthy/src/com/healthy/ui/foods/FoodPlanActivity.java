package com.healthy.ui.foods;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


import com.healthy.R;
import com.healthy.logic.HealthyApplication;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class FoodPlanActivity extends Activity {

	private ViewFlipper mContent;
	private ImageView mBackImage;
	@SuppressWarnings("unused")
	private Calendar mCalendar;
	private int mCurView = 0;// 当前viewFlipper显示的View
	// private float mCalorieOfPlan=0;
	/*
	 * viewOne中
	 */
	private EditText mCurWeightEdit, mAimWeightEdit,mAgeEdit,mHeightEdit;
	private float mCurWeight = 0, mAimWeight = 0, mDifWeight = 0;
	private Button mNextStepBtn;
	private String mSexualString="男";
	private int mAge=0;
	private float mHeight;
	RadioGroup mSexSelect;
	RadioButton mBoyBtn,mGirlBtn;

	/*
	 * viewTwo中
	 */
	@SuppressWarnings("unused")
	private TextView[] mTimeTextArray = new TextView[4];
	//private Button[] mSelectBtn = new Button[4];
	//private RelativeLayout[] mLevelSelectView = new RelativeLayout[4];
	private Button mDoneBtn;
	private int mSelectTag = -1;// -1代表未选择，0123分别代表四个级别
	private int[] mDuration = new int[4];// 存放四种计划的持续时间
	ListView mPlanList;
	FoodPlanListAdapter mAdapter;
	List<HashMap<String,Object>> mlist=new ArrayList<HashMap<String,Object>>();
	HashMap<String,Object> map;
	
	float[] mEachDayCalorie = new float[4];//四种级别每天摄入
	float[] mEachDayLose = new float[4];//每天减掉

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_foodplan);
		init();
	}

	private void init() {
		mBackImage = (ImageView) findViewById(R.id.foodplan_back);
		mContent = (ViewFlipper) findViewById(R.id.content);
		viewOneInit();
		viewTwoInit();
		setLisenter();

	}

	private void viewOneInit() {
		mCurWeightEdit = (EditText) findViewById(R.id.foodplan_curweight);
		mAimWeightEdit = (EditText) findViewById(R.id.foodplan_aimweight);
		mNextStepBtn = (Button) findViewById(R.id.foodplan_nextBtn);
		mSexSelect = (RadioGroup)findViewById(R.id.sexual);
		mAgeEdit = (EditText)findViewById(R.id.foodplan_age);
		mHeightEdit = (EditText)findViewById(R.id.foodplan_height);
		mBoyBtn = (RadioButton)findViewById(R.id.radioMale);
		mGirlBtn = (RadioButton)findViewById(R.id.radioFemale);
		getSharedPrefrences();
	}
	
	private void getSharedPrefrences(){
		SharedPreferences sp = getSharedPreferences("personal_info", 0);
		mCurWeightEdit.setText(sp.getFloat("weight", 0.0f)+"");
		mCurWeight = sp.getFloat("weight", 0.0f);
		mHeightEdit.setText(sp.getFloat("height", 0.0f)+"");
		mHeight = sp.getFloat("height", 0.0f);
		mAgeEdit.setText(sp.getInt("age", 0)+"");
		mAge =sp.getInt("age", 0);
		mSexualString = sp.getString("sex", "男");
		if(mSexualString.equals("男")){
			mBoyBtn.setChecked(true);
		}else{
			mGirlBtn.setChecked(true);
		}
	}

	private void viewTwoInit() {
		mPlanList = (ListView)findViewById(R.id.foodplan_list);
		mDoneBtn = (Button) findViewById(R.id.foodplan_done);
		getList();
		mAdapter = new FoodPlanListAdapter(this,mlist,mSelectTag,mEachDayCalorie);
		mPlanList.setAdapter(mAdapter);	
	}

	private void setLisenter() {

		mBackImage.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO 自动生成的方法存根
				switch (mCurView) {
				case 0:
					finish();
					overridePendingTransition(0, R.anim.roll_down);
					break;
				case 1:
					mContent.setInAnimation(AnimationUtils.loadAnimation(
							FoodPlanActivity.this, R.anim.slide_in_left));
					mContent.setOutAnimation(AnimationUtils.loadAnimation(
							FoodPlanActivity.this, R.anim.slide_out_right));
					mContent.setDisplayedChild(0);
					mCurView = 0;
					break;
				}
			}
		});

		/*
		 * viewOne
		 */
		mNextStepBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO 自动生成的方法存根
				if (mCurWeight == 0 || mAimWeight == 0) {
					mDifWeight = 0;
				}
				if (mDifWeight == 0) {
					Toast.makeText(getApplicationContext(), "无需制定计划",
							Toast.LENGTH_SHORT).show();
				} else if (mDifWeight < 0) {
					Toast.makeText(getApplicationContext(), "亲，这是减肥饮食计划",
							Toast.LENGTH_SHORT).show();
				}else if(mAge==0){
					Toast.makeText(getApplicationContext(), "请输入年龄",
							Toast.LENGTH_SHORT).show();
				} else if(mHeight==0){
					Toast.makeText(getApplicationContext(), "请输入身高",
							Toast.LENGTH_SHORT).show();
				}else {
					getEachDayCalorie();
					countDuration();
					getList();
					mAdapter.notifyDataSetChanged();
					mContent.setInAnimation(AnimationUtils.loadAnimation(
							FoodPlanActivity.this, R.anim.slide_in_right));
					mContent.setOutAnimation(AnimationUtils.loadAnimation(
							FoodPlanActivity.this, R.anim.slide_out_left));
					mContent.setDisplayedChild(1);
					mCurView = 1;
				}
			}
		});
		mCurWeightEdit.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO 自动生成的方法存根

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO 自动生成的方法存根

			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO 自动生成的方法存根
				if (mCurWeightEdit.getText().toString() == null
						|| "".equals(mCurWeightEdit.getText().toString())) {
					mCurWeight = 0;
				} else {
					mCurWeight = Float.parseFloat(mCurWeightEdit.getText()
							.toString());
				}
				mDifWeight = mCurWeight - mAimWeight;
			}
		});
		mAimWeightEdit.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO 自动生成的方法存根

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO 自动生成的方法存根

			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO 自动生成的方法存根
				if (mAimWeightEdit.getText().toString() == null
						|| "".equals(mAimWeightEdit.getText().toString())) {
					mAimWeight = 0;
				} else {
					mAimWeight = Float.parseFloat(mAimWeightEdit.getText()
							.toString());
				}
				mDifWeight = mCurWeight - mAimWeight;
			}
		});
		
		mSexSelect.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO 自动生成的方法存根
				switch(checkedId)
				{
				case R.id.radioMale:
					mSexualString = mBoyBtn.getText().toString();
					break;
				case R.id.radioFemale:
					mSexualString = mGirlBtn.getText().toString();
					break;
				}
				Log.i("type", checkedId+"");
				RadioButton rb = (RadioButton)findViewById(group.getCheckedRadioButtonId());
				mSexualString = rb.getText().toString();
			}
		});
		
		mAgeEdit.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO 自动生成的方法存根
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO 自动生成的方法存根
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				// TODO 自动生成的方法存根
				if(mAgeEdit.getText()==null||mAgeEdit.getText().toString().equals("")){
					mAge=0;
				}else{
					mAge = Integer.parseInt(mAgeEdit.getText().toString());
				}
			}
		});
		
		mHeightEdit.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO 自动生成的方法存根
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO 自动生成的方法存根
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				// TODO 自动生成的方法存根
				if(mHeightEdit.getText()==null||mHeightEdit.getText().toString().equals("")){
					mHeight = 0;
				}else{
					mHeight = Float.parseFloat(mHeightEdit.getText().toString());
				}
			}
		});
		
		/*
		 * view_two
		 */
		/*mLevelSelectView[0].setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO 自动生成的方法存根
				selectBtnClear();
				mSelectBtn[0]
						.setBackgroundResource(R.drawable.ic_foodplan_selected);
				mSelectTag = 0;
			}
		});*/
		mDoneBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO 自动生成的方法存根
				Date mDate = new Date();				
				if (mSelectTag == -1) {
					Toast.makeText(getApplicationContext(), "请选择级别",
							Toast.LENGTH_SHORT).show();
					return;
				}
				Intent in = new Intent();
				switch (mSelectTag) {
				case 0:
					in.putExtra("plan", mEachDayCalorie[0]);
					HealthyApplication.mDbUtil.updateFoodPlan(DateFormat.format("yyyy-MM-dd", mDate).toString(), mDuration[0], mEachDayCalorie[0]);
					break;
				case 1:
					in.putExtra("plan", mEachDayCalorie[1]);
					HealthyApplication.mDbUtil.updateFoodPlan(DateFormat.format("yyyy-MM-dd", mDate).toString(), mDuration[1], mEachDayCalorie[1]);
					break;
				case 2:
					in.putExtra("plan", mEachDayCalorie[2]);
					HealthyApplication.mDbUtil.updateFoodPlan(DateFormat.format("yyyy-MM-dd", mDate).toString(), mDuration[2], mEachDayCalorie[2]);
					break;
				case 3:
					in.putExtra("plan", mEachDayCalorie[3]);
					HealthyApplication.mDbUtil.updateFoodPlan(DateFormat.format("yyyy-MM-dd", mDate).toString(), mDuration[3], mEachDayCalorie[3]);
					break;
				}
				setResult(1, in);
				finish();
				overridePendingTransition(0, R.anim.roll_down);
			}
		});
		
		mPlanList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO 自动生成的方法存根
				mSelectTag = position;
				mAdapter.setTag(position);
				Log.i("show", "点击");
			}
		});
		

	}
	
	private void countDuration() {
		mDuration[0] = (int) mDifWeight * 7500 / 250;
		mDuration[1] = (int) mDifWeight * 7500 / 500;
		mDuration[2] = (int) mDifWeight * 7500 / 750;
		mDuration[3] = (int) mDifWeight * 7500 / 1000;
		for(int i=0;i<4;i++){
			mDuration[i] = (int) (mDifWeight * 7500/mEachDayLose[i]);
		}
	}

	private void getList(){
		mlist.clear();
		for(int i=0;i<4;i++){
			map = new HashMap<String, Object>();
			map.put("duration", mDuration[i]);
			mlist.add(map);
		}
	}
	
	private void getEachDayCalorie(){
		float eachEat; 
		if(mSexualString.equals("男")){
			eachEat = 66+13.7f*mCurWeight+5*mHeight-6.8f*mAge;
		}else {
			eachEat = 65+9.6f*mCurWeight+1.7f*mHeight-4.7f*mAge;
		}
		for(int i=0;i<4;i++){
			mEachDayLose[i] = eachEat*(i+1)/8;
			mEachDayCalorie[i]= (float)(Math.round((eachEat - mEachDayLose[i])*100))/100;
		}
	}
	
}
