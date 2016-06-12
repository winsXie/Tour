package com.bn.user;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bn.thread.LocationThread;
import com.bn.tour.R;
import com.bn.util.CheckNetworkStatus;
import com.bn.util.Constant;
import com.bn.util.NetTransUtil;

public class RegisterActivity extends Activity
{
	private static final int REGISTER = 0;//注册
	private ImageButton bok;
	private EditText name, password, password2, city, etBirth;
	private RadioButton sex_nan;
	private DatePicker birth;//出生日期
	private LinearLayout date;
	private RelativeLayout rlloading;

	private Handler handler;

	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_register);
		handler = new MyHandler(this);

		rlloading = (RelativeLayout) this.findViewById(R.id.RLregister_load);
		name = (EditText) findViewById(R.id.ETregi_uname);
		password = (EditText) findViewById(R.id.ETregi_password);
		password2 = (EditText) findViewById(R.id.ETregi_password2);
		sex_nan = (RadioButton) findViewById(R.id.nan);
		city = (EditText) findViewById(R.id.ETregi_city);
		etBirth = (EditText) findViewById(R.id.ETregi_birth);
		date = (LinearLayout) findViewById(R.id.LLBirth);
		birth = (DatePicker) findViewById(R.id.dateBirth);
		bok = (ImageButton) findViewById(R.id.IMBregi_enter);

		etBirth.setFocusable(false);// 生日编辑框设为不可聚焦
		sex_nan.setChecked(true);// 性别默认选择男

		LocationThread location = new LocationThread(getApplicationContext(),
				handler);
		location.start();// 网络定位

		// 初始化DatePicker
		final Calendar cal = Calendar.getInstance();
		final int nowyear = cal.get(Calendar.YEAR);// 记录下系统当前日期
		final int nowmonth = cal.get(Calendar.MONTH);
		final int nowday = cal.get(Calendar.DAY_OF_MONTH);
		birth.init(2000, 0, 1, new OnDateChangedListener()
		{
			Calendar cal2 = Calendar.getInstance();

			@Override
			public void onDateChanged(DatePicker view, int year,
					int monthOfYear, int dayOfMonth)
			{
				cal2.set(year, monthOfYear, dayOfMonth);
				if (cal2.after(cal))
				{
					try
					{
						birth.updateDate(nowyear, nowmonth, nowday);// 若选择的日期大于当前日期，置为当前日期
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}

				}
				else
				{
					monthOfYear = monthOfYear + 1;
					etBirth.setText(year + "-" + monthOfYear + "-" + dayOfMonth);
				}
			}

		});

		etBirth.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{

				switch (date.getVisibility())
					{
					case View.VISIBLE:
						date.setVisibility(View.GONE);
						break;
					case View.GONE:
						date.setVisibility(View.VISIBLE);
						break;
					}
			}
		});

		bok.setOnClickListener(new ImageButton.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				bok.setEnabled(false);// 禁用按钮，防止重复提交

				String username = name.getText().toString();
				String spassword = password.getText().toString();
				String spassword2 = password2.getText().toString();
				String strcity = city.getText().toString();
				String birth = etBirth.getText().toString();

				if (TextUtils.isEmpty(username) || TextUtils.isEmpty(spassword)
						|| TextUtils.isEmpty(spassword2)
						|| TextUtils.isEmpty(strcity)
						|| TextUtils.isEmpty(birth))
				{
					Toast.makeText(RegisterActivity.this, "请填写完整信息！",
							Toast.LENGTH_SHORT).show();
					bok.setEnabled(true);// 启用按钮
				}
				else
				{
					if (!spassword.equals(spassword2))
					{
						String msg = "密码不一致!";
						Toast.makeText(RegisterActivity.this, msg,
								Toast.LENGTH_SHORT).show();
						bok.setEnabled(true);
					}
					else
					{
						String sex = "男";
						if (sex_nan.isChecked())
						{
							sex = "男";
						}
						else
						{
							sex = "女";
						}
						final Map<String, String> paramap = new HashMap<String, String>();
						paramap.put("user_name", username);
						paramap.put("user_password", spassword);
						paramap.put("user_sex", sex);
						paramap.put("user_birth", birth);
						paramap.put("user_city", strcity);

						rlloading.setVisibility(View.VISIBLE);
						new Thread()
						{

							@Override
							public void run()
							{
								try
								{
									String backuid = NetTransUtil
											.register(paramap);
									handler.obtainMessage(REGISTER, backuid)
											.sendToTarget();
								}
								catch (Exception e)
								{
									String mess = e.getClass().getName();
									if (mess.contains("ConnectTimeoutException")
											|| mess.contains("ConnectException"))
									{
										handler.obtainMessage(
												Constant.CONNECTIONTIMEOUT)
												.sendToTarget();
									}
									else if (mess
											.contains("SocketTimeoutException"))
									{
										handler.obtainMessage(
												Constant.SOTIMEOUT)
												.sendToTarget();
									}
									e.printStackTrace();
								}

							}

						}.start();

					}
				}

			}
		});
	}

	private static class MyHandler extends Handler
	{
		WeakReference<RegisterActivity> mActivity;

		public MyHandler(RegisterActivity activity)
		{
			mActivity = new WeakReference<RegisterActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg)
		{
			RegisterActivity currActivity = mActivity.get();
			switch (msg.what)
				{
				case REGISTER:
					String regi_uid = (String) msg.obj;
					if (regi_uid == null)
					{
						Toast.makeText(currActivity, "注册失败，请稍后重试！",
								Toast.LENGTH_SHORT).show();
						currActivity.bok.setEnabled(true);
					}
					else
					{
						Constant.loginUid = regi_uid;
						String toast = "  注册成功\n  您的账号ID为\n  " + regi_uid;
						Toast.makeText(currActivity, toast, Toast.LENGTH_LONG)
								.show();

						Intent intent = new Intent();
						intent.setClass(currActivity, LoginActivity.class);
						intent.putExtra("USERID", regi_uid);
						currActivity.startActivity(intent);
						currActivity.finish();
					}
					break;
				case 1:
					String place = (String) msg.obj;
					currActivity.city.setText(place.substring(0,
							place.indexOf("市") + 1));// 定位成功，显示地址
					break;
				case 2:
					Toast.makeText(currActivity, "自动定位失败\n请检查网络设置！",
							Toast.LENGTH_LONG).show();
					break;
				case Constant.CONNECTIONTIMEOUT:
					// 去除加载缓冲
					if (currActivity.rlloading.getVisibility() == View.VISIBLE)
					{
						currActivity.rlloading.setVisibility(View.GONE);
					}
					if (CheckNetworkStatus.checkNetworkAvailable(currActivity))
					{// 手机网络可用
						Toast.makeText(currActivity, "连接超时！", Toast.LENGTH_LONG)
								.show();
					}
					else
					{
						Toast.makeText(currActivity, "网络不可用！请检查网络连接！",
								Toast.LENGTH_LONG).show();
					}
					currActivity.bok.setEnabled(true);
					break;
				case Constant.SOTIMEOUT:
					// 去除加载缓冲
					if (currActivity.rlloading.getVisibility() == View.VISIBLE)
					{
						currActivity.rlloading.setVisibility(View.GONE);
					}
					Toast.makeText(currActivity, "读取数据超时！", Toast.LENGTH_LONG)
							.show();
					currActivity.bok.setEnabled(true);
					break;
				}
		}
	}
}
