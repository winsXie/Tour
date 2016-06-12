package com.bn.user;

import java.lang.ref.WeakReference;

import net.sf.json.JSONObject;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bn.message.LeaveMessageActivity;
import com.bn.thread.DownloadPicThread;
import com.bn.tour.R;
import com.bn.util.Constant;
import com.bn.util.NetTransUtil;

public class LookUserInfoActivity extends Activity
{
	private static final int GET_BASEINFO = 0;//获得用户基本信息
	private String uid;
	private DownloadPicThread loadThread;
	private TextView tvUserName, tvAge, tvLev, tvQianm, tvCity, tvOccup,
			tvWcity, tvMail;
	private ImageView imvHeadImg, imvSex;
	private JSONObject userinfo;
	private Button btSendMess;

	private Handler handler;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lookuserinfo);

		handler = new MyHandler(this);
		uid = getIntent().getStringExtra("USERID");

		if (uid != null)
		{
			new Thread()
			{

				@Override
				public void run()
				{
					try
					{
						String mess = NetTransUtil.getBasedInfo(uid);
						handler.obtainMessage(GET_BASEINFO, mess)
								.sendToTarget();
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}

			}.start();
		}
	}

	private static class MyHandler extends Handler
	{
		WeakReference<LookUserInfoActivity> mActivity;

		public MyHandler(LookUserInfoActivity activity)
		{
			mActivity = new WeakReference<LookUserInfoActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg)
		{
			LookUserInfoActivity currActivity = mActivity.get();
			switch (msg.what)
				{
				case GET_BASEINFO:
					String info = (String) msg.obj;
					if (info != null)
					{
						currActivity.userinfo = JSONObject.fromObject(info);
						currActivity.initView();
					}
					break;
				case Constant.CONNECTIONTIMEOUT:
					Toast.makeText(currActivity, "连接超时！", Toast.LENGTH_LONG)
							.show();
					break;
				case Constant.SOTIMEOUT:
					Toast.makeText(currActivity, "读取数据超时！", Toast.LENGTH_LONG)
							.show();
				}
		}
	}

	//根据数据初始化信息
	private void initView()
	{
		imvHeadImg = (ImageView) this.findViewById(R.id.IMVuinfo_headimg);
		imvSex = (ImageView) this.findViewById(R.id.IMVuinfo_sex);

		tvUserName = (TextView) this.findViewById(R.id.TVuinfo_name);
		tvAge = (TextView) this.findViewById(R.id.TVuinfo_age);
		tvLev = (TextView) this.findViewById(R.id.TVuinfo_level);
		tvQianm = (TextView) this.findViewById(R.id.uinfo_qianming);
		tvCity = (TextView) this.findViewById(R.id.uinfo_city);
		tvOccup = (TextView) this.findViewById(R.id.uinfo_occup);
		tvWcity = (TextView) this.findViewById(R.id.uinfo_wantcity);
		tvMail = (TextView) this.findViewById(R.id.uinfo_mail);
		btSendMess = (Button) this.findViewById(R.id.BTinfo_send_mess);

		loadThread = new DownloadPicThread(userinfo.getString("user_image"),
				imvHeadImg, handler);
		loadThread.start();

		if (userinfo.getString("user_sex").equals("男"))
		{
			imvSex.setImageResource(R.drawable.sex_man);
		}
		else
		{
			imvSex.setImageResource(R.drawable.sex_woman);
		}

		tvUserName.setText(userinfo.getString("user_name"));
		tvAge.setText(userinfo.getString("user_age") + "岁");
		tvCity.setText(userinfo.getString("user_city"));
		tvLev.setText("等级  " + userinfo.getString("user_level"));
		if (!userinfo.getString("user_qianming").equals("null"))
		{
			tvQianm.setText(userinfo.getString("user_qianming"));
		}

		if (!userinfo.getString("user_occupation").equals("null"))
		{
			tvOccup.setText(userinfo.getString("user_occupation"));
		}
		if (!userinfo.getString("user_wantcity").equals("null"))
		{
			tvWcity.setText(userinfo.getString("user_wantcity"));
		}
		if (!userinfo.getString("user_mail").equals("null"))
		{
			tvMail.setText(userinfo.getString("user_mail"));
		}

		btSendMess.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				if (Constant.isLogin)
				{
					if (Constant.loginUid.equals(uid))
					{
						Toast.makeText(LookUserInfoActivity.this, "不能给自己留言哦！",
								Toast.LENGTH_LONG).show();
					}
					else
					{
						Intent intent = new Intent(LookUserInfoActivity.this,
								LeaveMessageActivity.class);
						intent.putExtra("ACCEPTUID", uid);
						startActivity(intent);
					}

				}
				else
				{
					Intent intent = new Intent(LookUserInfoActivity.this, LoginActivity.class);
					startActivity(intent);
				}
			}
		});
	}

}
