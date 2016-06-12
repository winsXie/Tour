package com.bn.user;

import java.lang.ref.WeakReference;

import net.sf.json.JSONObject;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bn.tour.MainMyActivity;
import com.bn.tour.R;
import com.bn.util.CheckNetworkStatus;
import com.bn.util.Constant;
import com.bn.util.NetTransUtil;

public class LoginActivity extends Activity
{
	private static final int LOGIN = 0;
	private ImageButton login;
	private EditText uid, pwd;
	private TextView tip;//ע����һ�ε�¼ʱ����ʾ
	private CheckBox remember;//�Ƿ��ס����
	private String user_id, upass;//�û�id������
	private JSONObject json_login;//�ύ��¼������õ���Ϣ
	public SharedPreferences sp;//���ڴ洢�û���������
	private RelativeLayout rlloading;

	private Handler handler;

	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_my_login);
		String userid = getIntent().getStringExtra("USERID");//ע����һ�ε�¼���д˲���
		handler = new MyHandler(this);

		rlloading = (RelativeLayout) this.findViewById(R.id.RLlogin_load);
		uid = (EditText) this.findViewById(R.id.ETlogin_uid);
		pwd = (EditText) this.findViewById(R.id.ETlogin_password);
		tip = (TextView) this.findViewById(R.id.TVtip);
		
		remember = (CheckBox) this.findViewById(R.id.check_login);
		sp = this.getSharedPreferences("tour", Context.MODE_PRIVATE);

		if (userid == null)
		{
			// ��ͨ��¼���ȼ���Ƿ��ס����
			String id = sp.getString("userid", null);
			String pass = sp.getString("password", null);
			if (id == null || pass == null)//�ϴε�¼û��ѡ���ס����
			{
				remember.setChecked(false);
			}
			else
			{
				uid.setText(id);
				pwd.setText(pass);
				remember.setChecked(true);
			}
		}
		else
		{// ע����½
			uid.setText(userid);
			tip.setVisibility(View.VISIBLE);
			tip.setText("���˺�IDΪ�û���¼ʱ��Ψһ�Ϸ��˺ţ����μǣ�");
		}

		uid.setSelection(uid.getText().toString().length());
		login = (ImageButton) findViewById(R.id.login_denglu);
		login.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				login.setEnabled(false);
				user_id = uid.getText().toString();
				upass = pwd.getText().toString();

				if (CheckNetworkStatus.checkNetworkAvailable(LoginActivity.this))
				{
					if (TextUtils.isEmpty(user_id) || TextUtils.isEmpty(upass))
					{
						Toast.makeText(LoginActivity.this, "����д������Ϣ��",
								Toast.LENGTH_LONG).show();
						login.setEnabled(true);
					}
					else
					{
						rlloading.setVisibility(View.VISIBLE);
						new Thread()
						{

							@Override
							public void run()
							{

								try
								{
									String mess = NetTransUtil.login(user_id);
									handler.obtainMessage(LOGIN, mess)
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
				else
				{
					Toast.makeText(LoginActivity.this, "���粻���ã������������ӣ�",
							Toast.LENGTH_LONG).show();
					login.setEnabled(true);
				}
			}
		});
	}

	private static class MyHandler extends Handler
	{
		WeakReference<LoginActivity> mActivity;

		public MyHandler(LoginActivity activity)
		{
			mActivity = new WeakReference<LoginActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg)
		{
			LoginActivity currActivity = mActivity.get();
			switch (msg.what)
				{
				case LOGIN:
					String mess = (String) msg.obj;
					if (mess == null)
					{
						Toast.makeText(currActivity, "�޴��û���", Toast.LENGTH_LONG)
								.show();
						if (currActivity.rlloading.getVisibility() == View.VISIBLE)
						{
							currActivity.rlloading.setVisibility(View.GONE);
						}
						currActivity.login.setEnabled(true);
					}
					else
					{
						currActivity.json_login = JSONObject.fromObject(mess);
						String forbidden = currActivity.json_login
								.getString("user_forbidden");
						if (forbidden.equals("true"))
						{
							Toast.makeText(currActivity, "���û��ѱ����ã�",
									Toast.LENGTH_LONG).show();
							if (currActivity.rlloading.getVisibility() == View.VISIBLE)
							{
								currActivity.rlloading.setVisibility(View.GONE);
							}
							currActivity.login.setEnabled(true);
						}
						else
						{
							String passWord = currActivity.json_login
									.getString("user_password");
							if (passWord.equals(currActivity.upass))
							{
								Toast.makeText(currActivity, "��¼�ɹ���",
										Toast.LENGTH_LONG).show();
								Constant.isLogin = true;
								Constant.loginUid = currActivity.user_id;
								MainMyActivity.isInitView = false;

								SharedPreferences.Editor editor = currActivity.sp
										.edit();
								if (currActivity.remember.isChecked())
								{
									editor.putString("userid",
											currActivity.user_id);
									editor.putString("password",
											currActivity.upass);
								}
								else
								{
									editor.putString("userid", null);
									editor.putString("password", null);
								}
								editor.commit();

								currActivity.finish();
							}
							else
							{
								Toast.makeText(currActivity, "�������",
										Toast.LENGTH_LONG).show();
								if (currActivity.rlloading.getVisibility() == View.VISIBLE)
								{
									currActivity.rlloading
											.setVisibility(View.GONE);
								}
								currActivity.login.setEnabled(true);
							}
						}
					}
					break;
				case Constant.CONNECTIONTIMEOUT:
					// ȥ�����ػ���
					if (currActivity.rlloading.getVisibility() == View.VISIBLE)
					{
						currActivity.rlloading.setVisibility(View.GONE);
					}
					if (CheckNetworkStatus.checkNetworkAvailable(currActivity))
					{// �ֻ��������
						Toast.makeText(currActivity, "���ӳ�ʱ��", Toast.LENGTH_LONG)
								.show();
					}
					else
					{
						Toast.makeText(currActivity, "���粻���ã������������ӣ�",
								Toast.LENGTH_LONG).show();
					}
					currActivity.login.setEnabled(true);
					break;
				case Constant.SOTIMEOUT:
					// ȥ�����ػ���
					if (currActivity.rlloading.getVisibility() == View.VISIBLE)
					{
						currActivity.rlloading.setVisibility(View.GONE);
					}
					Toast.makeText(currActivity, "��ȡ���ݳ�ʱ��", Toast.LENGTH_LONG)
							.show();
					currActivity.login.setEnabled(true);
					break;
				}
		}
	}

}
