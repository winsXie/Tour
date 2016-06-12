package com.bn.plan;

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
import android.widget.TextView;
import android.widget.Toast;

import com.bn.tour.R;
import com.bn.util.CheckUtil;
import com.bn.util.Constant;
import com.bn.util.NetTransUtil;

public class PublishPlanActivity extends Activity
{
	private TextView tvCancle, tvPublish;
	private EditText etTitle, etStaPlace, etEndPlace, etStaDate, etEndDate, etWay,
			etIntro;
	private DatePicker dpStaDate, dpEndDate;

	private Handler handler;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_publish_plan);
		handler = new MyHandler(this);

		tvCancle = (TextView) this.findViewById(R.id.TVpublish_plan_cancle);
		tvPublish = (TextView) this.findViewById(R.id.TVpublish_plan_send);
		etTitle = (EditText) this.findViewById(R.id.ETplan_title);
		etStaPlace = (EditText) this.findViewById(R.id.ETplan_staplace);
		etEndPlace = (EditText) this.findViewById(R.id.ETplan_endplace);
		etStaDate = (EditText) this.findViewById(R.id.ETplan_stadate);
		etEndDate = (EditText) this.findViewById(R.id.ETplan_enddate);
		etWay = (EditText) this.findViewById(R.id.ETplan_way);
		etIntro = (EditText) this.findViewById(R.id.ETplan_intro);
		dpStaDate = (DatePicker) this.findViewById(R.id.DPsta_date);
		dpEndDate = (DatePicker) this.findViewById(R.id.DPend_date);

		ClickListener listener = new ClickListener();
		etStaDate.setFocusable(false);
		etEndDate.setFocusable(false);
		etStaDate.setOnClickListener(listener);
		etEndDate.setOnClickListener(listener);
		tvCancle.setOnClickListener(listener);
		tvPublish.setOnClickListener(listener);

		initDatePicker();

	}

	private static class MyHandler extends Handler
	{
		WeakReference<PublishPlanActivity> mActivity;

		public MyHandler(PublishPlanActivity activity)
		{
			mActivity = new WeakReference<PublishPlanActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg)
		{
			PublishPlanActivity currActivity = mActivity.get();
			switch (msg.what)
				{
				case 0:
					String planid = (String) msg.obj;
					if (planid != null)
					{
						Toast.makeText(currActivity, "发表成功！",
								Toast.LENGTH_SHORT).show();

						Intent intent = new Intent(currActivity, MyPlanActivity.class);
						intent.putExtra("PLANID", planid);

						currActivity.setResult(1, intent);
						currActivity.finish();
					}
					else
					{
						Toast.makeText(currActivity, "发表失败！请重试",
								Toast.LENGTH_SHORT).show();
					}
					break;
				}
			super.handleMessage(msg);
		}
	}

	private void initDatePicker()
	{
		// 初始化DatePicker
		final Calendar cal = Calendar.getInstance();
		final int nowyear = cal.get(Calendar.YEAR);// 记录下系统当前日期
		final int nowmonth = cal.get(Calendar.MONTH);
		final int nowday = cal.get(Calendar.DAY_OF_MONTH);

		dpStaDate.init(nowyear, nowmonth, nowday, new OnDateChangedListener()
		{
			Calendar cal2 = Calendar.getInstance();

			@Override
			public void onDateChanged(DatePicker view, int year,
					int monthOfYear, int dayOfMonth)
			{
				cal2.set(year, monthOfYear, dayOfMonth);
				if (cal2.before(cal))
				{
					dpStaDate.updateDate(nowyear, nowmonth, nowday);// 若选择的日期小于当前日期，置为当前日期
				}
				else
				{
					monthOfYear = monthOfYear + 1;
					etStaDate.setText(year + "-" + monthOfYear + "-"
							+ dayOfMonth);
				}

			}

		});

		dpEndDate.init(nowyear, nowmonth, nowday, new OnDateChangedListener()
		{
			Calendar cal2 = Calendar.getInstance();

			@Override
			public void onDateChanged(DatePicker view, int year,
					int monthOfYear, int dayOfMonth)
			{
				cal2.set(year, monthOfYear, dayOfMonth);
				if (cal2.before(cal))
				{
					dpEndDate.updateDate(nowyear, nowmonth, nowday);// 若选择的日期小于当前日期，置为当前日期
				}
				else
				{
					monthOfYear = monthOfYear + 1;
					etEndDate.setText(year + "-" + monthOfYear + "-"
							+ dayOfMonth);
				}
			}

		});
	}

	class ClickListener implements OnClickListener
	{

		@Override
		public void onClick(View v)
		{
			// TODO Auto-generated method stub
			switch (v.getId())
				{
				case R.id.ETplan_stadate:
					switch (dpStaDate.getVisibility())
						{
						case View.VISIBLE:
							dpStaDate.setVisibility(View.GONE);
							break;
						case View.GONE:
							dpStaDate.setVisibility(View.VISIBLE);
							break;
						}
					dpEndDate.setVisibility(View.GONE);
					break;
				case R.id.ETplan_enddate:
					dpStaDate.setVisibility(View.GONE);
					switch (dpEndDate.getVisibility())
						{
						case View.VISIBLE:
							dpEndDate.setVisibility(View.GONE);
							break;
						case View.GONE:
							dpEndDate.setVisibility(View.VISIBLE);
							break;
						}
					break;
				case R.id.TVpublish_plan_cancle:
					finish();
					break;
				case R.id.TVpublish_plan_send:
					if (TextUtils.isEmpty(etTitle.getText())
							|| TextUtils.isEmpty(etStaPlace.getText())
							|| TextUtils.isEmpty(etEndPlace.getText())
							|| TextUtils.isEmpty(etStaDate.getText())
							|| TextUtils.isEmpty(etEndDate.getText())
							|| TextUtils.isEmpty(etWay.getText()))
					{
						Toast.makeText(PublishPlanActivity.this, "请将信息填写完整！",
								Toast.LENGTH_LONG).show();
					}
					else
					{
						final Map<String, String> paramap = new HashMap<String, String>();
						paramap.put("plan_userid", Constant.loginUid);
						paramap.put("plan_title", CheckUtil
								.replaceBlank(etTitle.getText().toString()));
						paramap.put("plan_startplace", CheckUtil
								.replaceBlank(etStaPlace.getText().toString()));
						paramap.put("plan_endplace", CheckUtil
								.replaceBlank(etEndPlace.getText().toString()));
						paramap.put("plan_startdate", CheckUtil
								.replaceBlank(etStaDate.getText().toString()));
						paramap.put("plan_enddate", CheckUtil
								.replaceBlank(etEndDate.getText().toString()));
						paramap.put("plan_way", CheckUtil.replaceBlank(etWay
								.getText().toString()));

						if (TextUtils.isEmpty(etIntro.getText().toString()))
						{
							paramap.put("plan_intro", "null");
						}
						else
						{
							paramap.put("plan_intro", CheckUtil
									.replaceBlank(etIntro.getText().toString()));
						}

						new Thread()
						{

							@Override
							public void run()
							{
								try
								{
									handler.obtainMessage(0,
											NetTransUtil.publishPlan(paramap))
											.sendToTarget();
								}
								catch (Exception e)
								{
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}

						}.start();

					}
					break;
				}
		}
	}

}
