package com.bn.travelnote;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bn.thread.LocationThread;
import com.bn.tour.R;
import com.bn.util.CheckUtil;
import com.bn.util.Constant;
import com.bn.util.NetTransUtil;

public class CreateTravelTopicActivity extends Activity
{
	private static final int CREATE_TRAT = 0;//�����μ�����
	private TextView tvCancle, tvOk;//���ء�ȷ�ϼ�
	private EditText etTratName, etPlace;//��������ơ��ص�

	private Handler handler;

	@SuppressLint("SimpleDateFormat")
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_cretrat);
		handler = new MyHandler(this);

		LocationThread location = new LocationThread(getApplicationContext(),
				handler);
		location.start();// ���綨λ

		TextView tvDate = (TextView) this.findViewById(R.id.TVcreate_trat_date);
		tvCancle = (TextView) this.findViewById(R.id.TVcreate_trat_cancle);
		tvOk = (TextView) this.findViewById(R.id.TVcreate_trat_ok);
		etTratName = (EditText) this.findViewById(R.id.ETcreate_trat_name);
		etPlace = (EditText) this.findViewById(R.id.ETcreate_trat__place);
		ImageView imvPlace = (ImageView) this
				.findViewById(R.id.IMVcreate_trat__place);

		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
		String time = fmt.format(new Date());
		tvDate.setText(time);

		ClickListener listener = new ClickListener();
		tvCancle.setOnClickListener(listener);
		tvOk.setOnClickListener(listener);
		imvPlace.setOnClickListener(listener);

	}

	private static class MyHandler extends Handler
	{
		WeakReference<CreateTravelTopicActivity> mActivity;

		public MyHandler(CreateTravelTopicActivity activity)
		{
			mActivity = new WeakReference<CreateTravelTopicActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg)
		{
			CreateTravelTopicActivity currActivity = mActivity.get();
			switch (msg.what)
				{
				case CREATE_TRAT:
					String tratid = (String) msg.obj;
					if (tratid == null)
					{
						Toast.makeText(currActivity, "����ʧ�ܣ�", Toast.LENGTH_LONG)
								.show();
					}
					else
					{
						Toast.makeText(currActivity, "�����ɹ���", Toast.LENGTH_LONG)
								.show();
						Intent intent = new Intent(currActivity,
								MyTravelTopicActivity.class);
						intent.putExtra("TRATID", tratid);
						currActivity.setResult(1, intent);
						currActivity.finish();
					}
					break;
				case 1:
					String place = (String) msg.obj;
					currActivity.etPlace.setText(place);// ��λ�ɹ�����ʾ��ַ
					break;
				case 2:
					Toast.makeText(currActivity, "�Զ���λʧ��\n�����������ã�",
							Toast.LENGTH_LONG).show();
					break;
				}
		}
	}

	private class ClickListener implements OnClickListener
	{

		@Override
		public void onClick(View v)
		{
			// TODO Auto-generated method stub
			switch (v.getId())
				{
				case R.id.TVcreate_trat_cancle:
					Intent intent = new Intent(CreateTravelTopicActivity.this,
							MyTravelTopicActivity.class);

					setResult(2, intent);
					finish();
					break;
				case R.id.TVcreate_trat_ok:
					if (TextUtils.isEmpty(etTratName.getText().toString()))
					{
						Toast.makeText(CreateTravelTopicActivity.this, "�������μ��������ƣ�",
								Toast.LENGTH_SHORT).show();
					}
					else
					{
						if (TextUtils.isEmpty(etPlace.getText().toString()))
						{
							Toast.makeText(CreateTravelTopicActivity.this, "�������μǵص㣡",
									Toast.LENGTH_SHORT).show();
						}
						else
						{

							new Thread()
							{

								@Override
								public void run()
								{
									String tratname = CheckUtil
											.replaceBlank(etTratName.getText()
													.toString());
									String tratplace = CheckUtil
											.replaceBlank(etPlace.getText()
													.toString());

									try
									{
										String tratid = NetTransUtil
												.creTraTopic(Constant.loginUid,
														tratname, tratplace);
										handler.obtainMessage(CREATE_TRAT,
												tratid).sendToTarget();
									}
									catch (Exception e)
									{
										// TODO Auto-generated catch block
										e.printStackTrace();
									}

								}

							}.start();

						}
					}
					break;
				case R.id.IMVcreate_trat__place:
					// һ��Ҫ������application��context,���ǵ���Activity��context
					LocationThread location = new LocationThread(
							getApplicationContext(), handler);
					location.start();// ���綨λ
					break;
				}
		}

	}
}
