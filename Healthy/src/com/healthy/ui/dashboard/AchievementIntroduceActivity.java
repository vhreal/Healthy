package com.healthy.ui.dashboard;

import java.util.ArrayList;
import java.util.List;

import com.healthy.R;
import com.healthy.logic.model.Introduce;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ListView;

public class AchievementIntroduceActivity extends Activity{

	private ListView mIntroduce;
	private ImageView mBack;
	private List<Introduce> mIntroduceData;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_introduce_achieve);
		mIntroduce = (ListView)findViewById(R.id.achieve_introduce_list);
		mBack = (ImageView)findViewById(R.id.back_introduce_btn);
		
		mBack.setOnClickListener( new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
				overridePendingTransition(0,R.anim.roll_down);
			}
		});
		
		setListData();
	}
	
	private void setListData(){
		mIntroduceData = new ArrayList<Introduce>();
		mIntroduceData.add(new Introduce("初出茅庐", "第一次使用健康达人获得成就"));
		mIntroduceData.add(new Introduce("轨迹新手", "成功记录第一条轨迹"));
		mIntroduceData.add(new Introduce("轨迹狂", "拥有50条轨迹"));
		mIntroduceData.add(new Introduce("轨迹达人", "拥有100条轨迹"));
		mIntroduceData.add(new Introduce("怀旧的人", "拥有500条轨迹"));
		mIntroduceData.add(new Introduce("该运动了", "当天静止超过6小时"));
		mIntroduceData.add(new Introduce("稳坐如山", "当天静止超过8小时"));
		mIntroduceData.add(new Introduce("散步达人", "当天走路超过6000步"));
		mIntroduceData.add(new Introduce("悠闲散步", "当天走路超过半小时"));
		mIntroduceData.add(new Introduce("健康生活", "当天走路超过1万步"));
		mIntroduceData.add(new Introduce("行万里路", "累计走过800万步"));
		mIntroduceData.add(new Introduce("跑步了", "当天跑步了"));
		mIntroduceData.add(new Introduce("跑步控", "当天跑步半小时"));
		mIntroduceData.add(new Introduce("真·跑步", "累计跑步超过100小时"));
		
		IntroduceAdapter adapter = new IntroduceAdapter(this, mIntroduceData);
		mIntroduce.setAdapter(adapter);
	}
	
	@Override
	protected void onPause() {
		overridePendingTransition(0,R.anim.roll_down);
		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}
	
	
	
}
