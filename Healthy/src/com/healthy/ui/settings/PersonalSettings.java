package com.healthy.ui.settings;

import com.healthy.R;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

public class PersonalSettings extends Activity {

	private RadioGroup sexGroup;
	private RadioButton girlBtn;
	private RadioButton boyBtn;

	private EditText ageText;
	private EditText heightText;
	private EditText weightText;
	private EditText strideText;

	private ImageView mBack;
	private Button mSave;
	private String sexString = "男";
	private int mAge;
	private float mHeight;
	private float mWeight;
	private float mStride;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_personal);

		sexGroup = (RadioGroup) findViewById(R.id.personal_sex_choose);
		girlBtn = (RadioButton) findViewById(R.id.personal_radioFemale);
		boyBtn = (RadioButton) findViewById(R.id.personal_radioMale);
		ageText = (EditText) findViewById(R.id.personal_age_text);
		heightText = (EditText) findViewById(R.id.personal_height_text);
		weightText = (EditText) findViewById(R.id.personal_weight_text);
		strideText = (EditText) findViewById(R.id.personal_stride_text);
		mBack = (ImageView) findViewById(R.id.back_personal_btn);
		mSave = (Button) findViewById(R.id.personal_btn);
		getSharePrefrences();
		setListener();
	}

	private void setListener() {

		mBack.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
				overridePendingTransition(0, R.anim.roll_down);
			}
		});
		
		mSave.setOnClickListener( new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				checkEdit();
				
			}
		});
		
		sexGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
				switch(checkedId){
				case R.id.personal_radioMale:
					sexString = boyBtn.getText().toString();
					Log.i("tag", sexString);
					break;
				case R.id.personal_radioFemale:
					sexString = girlBtn.getText().toString();
					break;
				}
			}
		});
		
		
	}
	
	private void getSharePrefrences(){
		SharedPreferences sp = getSharedPreferences("personal_info", 0);
		String sex = sp.getString("sex", "男");
		if(sex.equals("男")){
			boyBtn.setChecked(true);
		}else{
			girlBtn.setChecked(true);
		}
		ageText.setText(sp.getInt("age", 0)+"");
		heightText.setText(sp.getFloat("height", 0.0f)+"");
		weightText.setText(sp.getFloat("weight", 0.0f)+"");
		strideText.setText(sp.getFloat("stride", 0.0f)+"");
	}
	
	private void writeInSharePrefrences(){
		
		SharedPreferences sp = getSharedPreferences("personal_info", 0);
		sp.edit().putString("sex", sexString).commit();
		sp.edit().putInt("age", mAge).commit();
		sp.edit().putFloat("height", mHeight).commit();
		sp.edit().putFloat("weight", mWeight).commit();
		sp.edit().putFloat("stride", mStride).commit();
		
		
	}
	
	private void checkEdit(){
		if(ageText.getText().toString().equals("")){
			Toast.makeText(PersonalSettings.this, "请输入年龄", Toast.LENGTH_SHORT).show();
		}else{
			mAge = Integer.parseInt(ageText.getText().toString());
			if(heightText.getText().toString().equals("")){
				Toast.makeText(PersonalSettings.this, "请输入身高", Toast.LENGTH_SHORT).show();
			}else{
				mHeight = Float.parseFloat(heightText.getText().toString());
				if(weightText.getText().toString().equals("")){
					Toast.makeText(PersonalSettings.this, "请输入体重", Toast.LENGTH_SHORT).show();
				}else{
					mWeight = Float.parseFloat(weightText.getText().toString());
					if(strideText.getText().toString().equals("")){
						Toast.makeText(PersonalSettings.this, "请输入步长", Toast.LENGTH_SHORT).show();
					}else{
						mStride = Float.parseFloat(strideText.getText().toString());
						writeInSharePrefrences();
						Toast.makeText(PersonalSettings.this, "个人信息保存成功", Toast.LENGTH_SHORT).show();
						finish();
					}
				}
			}
		}
	}

}
