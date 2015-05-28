package com.healthy.ui.friends;

import com.healthy.R;
import com.healthy.logic.HealthyApplication;
import com.healthy.logic.RequestListener;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import static com.healthy.ui.friends.FriendsRequestParam.TASK_REGISTER;
import static com.healthy.util.Constants.*;

public class RegisterActivity extends Activity {

	private ImageView mBack;

	/* 注册 */
	private EditText mRegisterName;
	private EditText mRegisterPassword;
	private EditText mRegisterAgainPwd;
	private Button mRegisterBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		mBack = (ImageView) findViewById(R.id.account_back);
		
		initRegisterViews();

		setListener();
	}

	private void initRegisterViews() {
		mRegisterName = (EditText) findViewById(R.id.raccount);
		mRegisterPassword = (EditText) findViewById(R.id.rpassword);
		mRegisterAgainPwd = (EditText) findViewById(R.id.ragain_pwd);
		mRegisterBtn = (Button) findViewById(R.id.register);
	}

	private void setListener() {

		mBack.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				setResult(ActivityResultCode.ERROR);
				finish();
				overridePendingTransition(0, R.anim.roll_down);
			}
		});

		mRegisterBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				String name = mRegisterName.getText().toString().trim();
				String password = mRegisterPassword.getText().toString().trim();
				if (name.equals("") || password.equals("")) {
					Toast.makeText(RegisterActivity.this, "用户名或密码不能为空",
							Toast.LENGTH_SHORT).show();
					return;
				}
				if (pwdIsSame()) {
					FriendsRequestParam param = new FriendsRequestParam(
							TASK_REGISTER);
					param.addParam("name", name);
					param.addParam("password", password);

					if (param.getTaskCategory() == TASK_REGISTER) {
						HealthyApplication.mAsyncHealthy.register(param,
								listener);
					}
				} else {
					Toast.makeText(RegisterActivity.this, "两次输入密码不一致",
							Toast.LENGTH_SHORT).show();
					mRegisterName.setText("");
					mRegisterPassword.setText("");
					mRegisterAgainPwd.setText("");
				}

			}
		});

		
	}

	private boolean pwdIsSame() {
		String pwdText = mRegisterPassword.getText().toString().trim();
		String agpwdText = mRegisterAgainPwd.getText().toString().trim();
		if (pwdText.equals(agpwdText)) {
			return true;
		} else {
			return false;
		}
	}

	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case 0:// 开始任务，显示加载进度条
				
				break;
			case 1:// 登陆操作结束
				FriendsResponseBean bean = (FriendsResponseBean) msg.obj;
				if (bean.getResult() == FriendsResponseBean.ERROR) {// 任务执行失败
					mRegisterName.setText("");
					mRegisterPassword.setText("");
					mRegisterAgainPwd.setText("");
					Toast.makeText(RegisterActivity.this, bean.toString(),
							Toast.LENGTH_SHORT).show();
				} else {
					
					if (bean.toString().equalsIgnoreCase("注册成功")){//跳转到登录view
						SharedPreferences mSP = getSharedPreferences("user_info", 0);
						mSP.edit().putString("username", mRegisterName.getText().toString().trim()).commit();
						mSP.edit().putString("password", mRegisterPassword.getText().toString().trim()).commit();
						Toast.makeText(RegisterActivity.this, bean.toString(),
								Toast.LENGTH_SHORT).show();
						finish();
						overridePendingTransition(0, R.anim.roll_down);
					}
				}
				break;

			}
		}

	};

	RequestListener<FriendsResponseBean> listener = new RequestListener<FriendsResponseBean>() {

		@Override
		public void onStart() {
			// TODO Auto-generated method stub
			handler.sendEmptyMessage(0);
		}

		@Override
		public void onComplete(FriendsResponseBean bean) {
			// TODO Auto-generated method stub
			Message msg = Message.obtain(handler);
			msg.what = 1;
			msg.obj = bean;
			msg.sendToTarget();
		}
	};

}
