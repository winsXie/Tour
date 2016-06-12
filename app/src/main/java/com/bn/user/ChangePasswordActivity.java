package com.bn.user;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bn.tour.R;
import com.bn.util.Constant;
import com.bn.util.NetTransUtil;

public class ChangePasswordActivity extends Activity
{
	private Button ok;
	private EditText oldpwd, newpwd;

	private Handler handler;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_changepwd);

		handler = new MyHandler(this);
		oldpwd = (EditText) findViewById(R.id.oldPassword);
		newpwd = (EditText) findViewById(R.id.newPassword);
		ok = (Button) findViewById(R.id.ChangePwd_ok);

		ok.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				ok.setEnabled(false);

				final String oldPwd = oldpwd.getText().toString();
				final String newPwd = newpwd.getText().toString();

				if (TextUtils.isEmpty(oldPwd) || TextUtils.isEmpty(newPwd))
				{
					Toast.makeText(ChangePasswordActivity.this, "密码不能为空！",
							Toast.LENGTH_SHORT).show();
					ok.setEnabled(true);
				}
				else
				{
					new Thread()
					{
						@Override
						public void run()
						{
							try
							{
								String mess = NetTransUtil.changePass(
										Constant.loginUid, oldPwd, newPwd);
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
			}
		});
	}

	private static class MyHandler extends Handler
	{
		WeakReference<ChangePasswordActivity> mActivity;

		public MyHandler(ChangePasswordActivity activity)
		{
			mActivity = new WeakReference<ChangePasswordActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg)
		{
			ChangePasswordActivity currActivity = mActivity.get();
			switch (msg.what)
				{
				case 0:
					String mess = (String) msg.obj;
					if (mess != null)
					{
						if (mess.equals("修改成功！"))
						{
							Toast.makeText(currActivity, "修改成功！",
									Toast.LENGTH_SHORT).show();
							currActivity.finish();
						}
						else
						{
							Toast.makeText(currActivity, mess,
									Toast.LENGTH_SHORT).show();
							currActivity.ok.setEnabled(true);
						}
					}
					else
					{
						Toast.makeText(currActivity, "修改失败，请重试！",
								Toast.LENGTH_SHORT).show();
						currActivity.ok.setEnabled(true);
					}
					break;
				case Constant.CONNECTIONTIMEOUT:
					Toast.makeText(currActivity, "连接超时！", Toast.LENGTH_LONG)
							.show();
					currActivity.ok.setEnabled(true);
					break;
				case Constant.SOTIMEOUT:
					Toast.makeText(currActivity, "读取数据超时！", Toast.LENGTH_LONG)
							.show();
					currActivity.ok.setEnabled(true);
					break;
				}
		}
	}
}
