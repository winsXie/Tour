package com.bn.user;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bn.tour.R;
import com.bn.util.Constant;
import com.bn.util.NetTransUtil;

public class PublishQianmActivity extends Activity
{

	private String qianm;

	private Handler handler;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_publishqianm);
		handler = new MyHandler(this);

		final EditText etQianm = (EditText) this
				.findViewById(R.id.ETmy_qianm_qianm);
		TextView tvBack = (TextView) findViewById(R.id.TVmy_qianm_back);
		TextView tvEnter = (TextView) findViewById(R.id.TVmy_qianm_publish);

		tvBack.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				finish();
			}
		});

		tvEnter.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				qianm = etQianm.getText().toString();
				if (!TextUtils.isEmpty(qianm))
				{
					new Thread()
					{
						@Override
						public void run()
						{
							try
							{
								String mess = NetTransUtil.setQianMing(
										Constant.loginUid, qianm);
								handler.obtainMessage(0, mess).sendToTarget();
							}
							catch (Exception e)
							{
								String mess = e.getClass().getName();
								if (mess.contains("ConnectTimeoutException"))
								{
									handler.obtainMessage(
											Constant.CONNECTIONTIMEOUT)
											.sendToTarget();
								}
								else if (mess
										.contains("SocketTimeoutException"))
								{
									handler.obtainMessage(Constant.SOTIMEOUT)
											.sendToTarget();
								}
								e.printStackTrace();
							}
						}
					}.start();
				}
				else
				{
					Toast.makeText(PublishQianmActivity.this, "签名不能为空哦！",
							Toast.LENGTH_LONG).show();
				}

			}
		});
	}

	private static class MyHandler extends Handler
	{
		WeakReference<PublishQianmActivity> mActivity;

		public MyHandler(PublishQianmActivity activity)
		{
			mActivity = new WeakReference<PublishQianmActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg)
		{
			PublishQianmActivity currActivity = mActivity.get();
			switch (msg.what)
				{
				case 0:
					String mess = (String) msg.obj;
					if (mess != null)
					{
						Toast.makeText(currActivity, "发表成功！",
								Toast.LENGTH_SHORT).show();
						Constant.currUserInfo.put("user_qianming",
								currActivity.qianm);
						currActivity.finish();
					}
					else
					{
						Toast.makeText(currActivity, "发表失败！",
								Toast.LENGTH_SHORT).show();
						currActivity.finish();
					}
					break;
				case Constant.CONNECTIONTIMEOUT:
					Toast.makeText(currActivity, "连接超时！", Toast.LENGTH_LONG)
							.show();
					break;
				case Constant.SOTIMEOUT:
					Toast.makeText(currActivity, "读取数据超时！", Toast.LENGTH_LONG)
							.show();
					break;
				}
		}
	}

}
