package com.healthy.ui.foods;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.healthy.R;
import com.healthy.logic.HealthyApplication;
import com.healthy.logic.model.FoodInDb;


public class FoodEditActivity extends Activity {

	private RadioGroup mFoodCategory;
	private ViewFlipper mContent;
	private ImageView mBack;// 返回按钮

	/**
	 * 普通食物界面中控件
	 */
	private Button mAddFoodBtnCommon;
	private EditText mFoodNumEditCommon;
	private TextView mCalorieTextCommon;
	private TextView mTotalCalorieCommon;
	private Spinner mSpinnerCommon;
	private ArrayAdapter<String> mAdapterCommon;

	/**
	 * 自定义食物界面中控件
	 */
	private Button mAddFoodBtnCustom;// 添加自定义食物
	private EditText mFoodNameEditCustom;// 编辑食物名称
	private EditText mUnitCalorieEditCustom;// 编辑食物单位热量（kcal/100g）
	private EditText mFoodNumEditCustom;// 编辑食物质量
	private TextView mTotalCalorieTextCustom;// 显示食物总热量

	private float mUnitCalorieCustom;
	private float mFoodNumCustom;
	private String mFoodNameCustom="";

	/*
	 * 数据处理变量
	 */
	private float mFoodNum;// 输入的食物质量
	private FoodInDb mFoodData;
	

	// 用于加载存储在数据库中的常用食物
	private List<Map<String, Object>> mFoodList;
	private String[] mFoodsName;// 数据与mFoodList相对应
	private float[] mFoodsCalorie;// 数据与mFoodList相对应
	private String mFoodName;// mFoodsName[]中一项
	private float mFoodCalorie;// mFoodsCalorie[]中一项

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO 自动生成的方法存根
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_foodedit);

		mFoodCategory = (RadioGroup) findViewById(R.id.food_category);
		mContent = (ViewFlipper) findViewById(R.id.content);

		mBack = (ImageView) findViewById(R.id.foodedit_back);

		setListener();

		initCommonFoodViews();
		initCustomFoodViews();
	}

	private void setListener() {

		mFoodCategory.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
				switch (checkedId) {
				case R.id.common_food:
					mContent.setInAnimation(AnimationUtils.loadAnimation(
							FoodEditActivity.this, R.anim.slide_in_left));
					mContent.setOutAnimation(AnimationUtils.loadAnimation(
							FoodEditActivity.this, R.anim.slide_out_right));
					mContent.setDisplayedChild(0);
					break;
				case R.id.custom_food:
					mContent.setInAnimation(AnimationUtils.loadAnimation(
							FoodEditActivity.this, R.anim.slide_in_right));
					mContent.setOutAnimation(AnimationUtils.loadAnimation(
							FoodEditActivity.this, R.anim.slide_out_left));
					mContent.setDisplayedChild(1);
					break;
				}
			}
		});

		mBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO 自动生成的方法存根
				finish();
				overridePendingTransition(0, R.anim.roll_down);
			}
		});
	}

	private void initCommonFoodViews() {

		mSpinnerCommon = (Spinner) findViewById(R.id.food_spinner);
		mFoodNumEditCommon = (EditText) findViewById(R.id.food_numedit);
		mCalorieTextCommon = (TextView) findViewById(R.id.food_calorie);
		mTotalCalorieCommon = (TextView) findViewById(R.id.food_totalcalorie);
		mAddFoodBtnCommon = (Button) findViewById(R.id.addnow_btn);

		/*
		 * 默认显示
		 */
		if (mFoodNumEditCommon.getText() == null
				|| "".equals(mFoodNumEditCommon.getText().toString())) {
			mFoodNum = 0;
		} else {
			mFoodNum = Float
					.parseFloat(mFoodNumEditCommon.getText().toString());
		}

		mFoodList = HealthyApplication.mDbUtil.queryFoodType();
		mFoodsName = new String[mFoodList.size()];
		mFoodsCalorie = new float[mFoodList.size()];
		for (int i = 0; i < mFoodList.size(); i++) {
			mFoodsName[i] = (String) mFoodList.get(i).get("name");
			mFoodsCalorie[i] = (Float) mFoodList.get(i).get("calorie");
		}

		mAdapterCommon = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, mFoodsName);
		mAdapterCommon
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpinnerCommon.setAdapter(mAdapterCommon);
		mSpinnerCommon.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO 自动生成的方法存根
				mFoodName = mFoodsName[position];
				mFoodCalorie = mFoodsCalorie[position];
				mCalorieTextCommon.setText(mFoodCalorie + "kcal/100g");
				mTotalCalorieCommon.setText((mFoodCalorie * mFoodNum/100) + "kcal");
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO 自动生成的方法存根

			}
		});

		mFoodNumEditCommon.addTextChangedListener(new TextWatcher() {

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
				if (mFoodNumEditCommon.getText() == null
						|| "".equals(mFoodNumEditCommon.getText().toString())) {
					mFoodNum = 0;
				} else {
					mFoodNum = Float.parseFloat(mFoodNumEditCommon.getText()
							.toString());
				}
				mTotalCalorieCommon.setText((mFoodCalorie * mFoodNum/100) + "kcal");
			}
		});

		// 添加通用食物
		mAddFoodBtnCommon.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO 自动生成的方法存根
				if (mFoodNum == 0) {
					Toast.makeText(getApplicationContext(), "请输入数量",
							Toast.LENGTH_SHORT).show();
					return;
				} else {
					mFoodData = new FoodInDb();
					Date date=new Date();
					mFoodData.name = mFoodName;
					mFoodData.num = mFoodNum;
					mFoodData.calorie = mFoodCalorie;
					mFoodData.time = new SimpleDateFormat("HH:mm").format(date);
					mFoodData.date = new SimpleDateFormat("yyyy-MM-dd").format(date);
					HealthyApplication.mDbUtil.insertIntoFoodInfo(mFoodData);
					finish();
					overridePendingTransition(0, R.anim.roll_down);
				}
			}
		});
	}

	private void initCustomFoodViews() {

		mAddFoodBtnCustom = (Button) findViewById(R.id.customfood_addnow_btn);
		mFoodNameEditCustom = (EditText) findViewById(R.id.customfood_nameedit);
		mUnitCalorieEditCustom = (EditText) findViewById(R.id.customfood_unitedit);
		mFoodNumEditCustom = (EditText) findViewById(R.id.customfood_food_numedit);
		mTotalCalorieTextCustom = (TextView) findViewById(R.id.customfood_food_totalcalurie);

		if (mUnitCalorieEditCustom.getText() == null
				|| "".equals(mUnitCalorieEditCustom.getText().toString())) {
			mUnitCalorieCustom = 0;
		} else {
			mUnitCalorieCustom = Float.parseFloat(mUnitCalorieEditCustom
					.getText().toString());
		}

		if (mFoodNumEditCustom.getText() == null
				|| "".equals(mFoodNumEditCustom.getText().toString())) {
			mFoodNumCustom = 0;
		} else {
			mFoodNumCustom = Float.parseFloat(mFoodNumEditCustom.getText()
					.toString());
		}

		mFoodNameEditCustom.addTextChangedListener(new TextWatcher() {

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
				mFoodNameCustom = mFoodNameEditCustom.getText().toString();
			}
		});

		mUnitCalorieEditCustom.addTextChangedListener(new TextWatcher() {

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
				if (mUnitCalorieEditCustom.getText() == null
						|| "".equals(mUnitCalorieEditCustom.getText()
								.toString())) {
					mUnitCalorieCustom = 0;
				} else {
					mUnitCalorieCustom = Float
							.parseFloat(mUnitCalorieEditCustom.getText()
									.toString());
				}
				mTotalCalorieTextCustom.setText(mUnitCalorieCustom
						* mFoodNumCustom/100 + "calories");
			}
		});

		mFoodNumEditCustom.addTextChangedListener(new TextWatcher() {

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
				if (mFoodNumEditCustom.getText().toString() == null
						|| "".equals(mFoodNumEditCustom.getText().toString())) {
					mFoodNumCustom = 0;
				} else {
					mFoodNumCustom = Float.parseFloat(mFoodNumEditCustom
							.getText().toString());
				}
				mTotalCalorieTextCustom.setText(mUnitCalorieCustom * mFoodNumCustom/100
						+ "kcal");
			}
		});

		// 添加自定义食物
		mAddFoodBtnCustom.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO 自动生成的方法存根
				if ("".equals(mFoodNameCustom)) {
					Toast.makeText(getApplicationContext(), "请输入名称",
							Toast.LENGTH_SHORT).show();
					return;
				} else if (mUnitCalorieCustom == 0) {
					Toast.makeText(getApplicationContext(), "请输入单位卡路里",
							Toast.LENGTH_SHORT).show();
					return;
				} else if ((mFoodNumCustom == 0)) {
					Toast.makeText(getApplicationContext(), "请输入数量",
							Toast.LENGTH_SHORT).show();
					return;
				} else {
					mFoodData = new FoodInDb();
					Date date=new Date();
					mFoodData.name = mFoodNameCustom;
					mFoodData.num = mFoodNumCustom;
					mFoodData.calorie = mUnitCalorieCustom;
					mFoodData.time = new SimpleDateFormat("HH:mm").format(date);
					mFoodData.date = new SimpleDateFormat("yyyy-MM-dd").format(date);
					HealthyApplication.mDbUtil.insertIntoFoodType(mFoodData);
					HealthyApplication.mDbUtil.insertIntoFoodInfo(mFoodData);
					finish();
					overridePendingTransition(0, R.anim.roll_down);
				}
			}
		});
	}

}
