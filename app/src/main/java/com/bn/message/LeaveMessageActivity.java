package com.bn.message;

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

public class LeaveMessageActivity extends Activity
{
	private static final int LEAVE_MESSAGE = 0;//����whatֵ
	private TextView tvCancle, tvSend;
	private EditText etContent;
	private String acceuid;//�������Ե��û�id

	private Handler handler;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_leavemess);
		handler = new MyHandler(this);

		acceuid = getIntent().getStringExtra("ACCEPTUID");

		etContent = (EditText) this.findViewById(R.id.ETleave_mess_content);
		tvCancle = (TextView) this.findViewById(R.id.TVleav_mess_cancle);
		tvSend = (TextView) this.findViewById(R.id.TVleav_mess_send);

		tvCancle.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				finish();
			}
		});

		tvSend.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				tvSend.setEnabled(false);
				if (TextUtils.isEmpty(etContent.getText()))
				{
					Toast.makeText(LeaveMessageActivity.this, "�������ݲ���Ϊ�գ�",
							Toast.LENGTH_SHORT).show();
					tvSend.setEnabled(true);
				}
				else
				{
					if (acceuid != null)
					{
						new Thread()
						{

							@Override
							public void run()
							{
								try
								{
									boolean flag = NetTransUtil.leaveMessage(
											Constant.loginUid, acceuid,
											etContent.getText().toString());
									handler.obtainMessage(LEAVE_MESSAGE, flag)
											.sendToTarget();
								}
								catch (Exception e)
								{
									e.printStackTrace();
								}
							}

						}.start();
					}
					else
					{
						Toast.makeText(LeaveMessageActivity.this, "δָ�����Ͷ���",
								Toast.LENGTH_SHORT).show();
						tvSend.setEnabled(true);
					}
				}

			}
		});

	}

	private static class MyHandler extends Handler
	{
		WeakReference<LeaveMessageActivity> mActivity;

		public MyHandler(LeaveMessageActivity activity)
		{
			mActivity = new WeakReference<LeaveMessageActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg)
		{
			LeaveMessageActivity currActivity = mActivity.get();
			switch (msg.what)
				{
				case LEAVE_MESSAGE:
					if ((Boolean) msg.obj)
					{
						Toast.makeText(currActivity, "�����ѷ��ͣ�",
								Toast.LENGTH_SHORT).show();
						currActivity.finish();
					}
					else
					{
						Toast.makeText(currActivity, "����ʧ�ܣ�",
								Toast.LENGTH_SHORT).show();
						currActivity.tvSend.setEnabled(true);
					}
					break;
				case Constant.CONNECTIONTIMEOUT:
					Toast.makeText(currActivity, "���ӳ�ʱ��", Toast.LENGTH_LONG)
							.show();
					currActivity.tvSend.setEnabled(true);
					break;
				case Constant.SOTIMEOUT:
					Toast.makeText(currActivity, "��ȡ���ݳ�ʱ��", Toast.LENGTH_LONG)
							.show();
					currActivity.tvSend.setEnabled(true);
					break;
				}
		}
	}

}
