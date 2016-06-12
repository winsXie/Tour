package com.bn.plan;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;
import android.app.Activity;
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
import com.bn.util.Constant;
import com.bn.util.NetTransUtil;

public class PlanDetailsActivity extends Activity
{
	private static final int GET_PLANINFO = 0;//获得计划详情
	private static final int EDIT_PLAN = 1;//编辑计划
	private TextView tvCancle, tvPublish, tvSendTime;
	private EditText etTitle, etStaPlace, etEndPlace, etStaDate, etEndDate,
			etWay, etIntro;
	private DatePicker dpStaDate, dpEndDate;//设置日期的DatePicker
	private ClickListener listener;
	private JSONObject json_plan;

	private boolean onlyRead = true;//读写方式标志位
	private String planid;//当前plan的id

	private Handler handler;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_plan_details);
		handler = new MyHandler(this);

		onlyRead = getIntent().getBooleanExtra("READTYPE", true);
		planid = getIntent().getStringExtra("PLANID");
		new Thread()
		{

			@Override
			public void run()
			{
				try
				{
					String planInfo = NetTransUtil.getPlanById(planid);
					if (planInfo != null)
					{
						handler.obtainMessage(GET_PLANINFO, planInfo)
								.sendToTarget();
					}
				}
				catch (Exception e)
				{
					String mess = e.getClass().getName();
					if (mess.contains("ConnectTimeoutException"))
					{
						handler.obtainMessage(Constant.CONNECTIONTIMEOUT)
								.sendToTarget();
					}
					else if (mess.contains("SocketTimeoutException"))
					{
						handler.obtainMessage(Constant.SOTIMEOUT)
								.sendToTarget();
					}
					e.printStackTrace();
				}
			}
		}.start();

	}

	private static class MyHandler extends Handler
	{
		WeakReference<PlanDetailsActivity> mActivity;

		public MyHandler(PlanDetailsActivity activity)
		{
			mActivity = new WeakReference<PlanDetailsActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg)
		{
			PlanDetailsActivity currActivity = mActivity.get();
			switch (msg.what)
				{
				case GET_PLANINFO:
					currActivity.json_plan = JSONObject
							.fromObject((String) msg.obj);
					currActivity.initView();
					break;
				case EDIT_PLAN:
					if ((Boolean) msg.obj)
					{
						Toast.makeText(currActivity, "修改成功！",
								Toast.LENGTH_SHORT).show();
						currActivity.finish();
					}
					else
					{
						Toast.makeText(currActivity, "修改失败！",
								Toast.LENGTH_SHORT).show();
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

	//初始化界面
	private void initView()
	{
		tvCancle = (TextView) this.findViewById(R.id.TVpublish_plan_cancle);
		tvPublish = (TextView) this.findViewById(R.id.TVpublish_plan_send);
		tvSendTime = (TextView) this.findViewById(R.id.TVplan_date);
		etTitle = (EditText) this.findViewById(R.id.ETplan_title);
		etStaPlace = (EditText) this.findViewById(R.id.ETplan_staplace);
		etEndPlace = (EditText) this.findViewById(R.id.ETplan_endplace);
		etStaDate = (EditText) this.findViewById(R.id.ETplan_stadate);
		etEndDate = (EditText) this.findViewById(R.id.ETplan_enddate);
		etWay = (EditText) this.findViewById(R.id.ETplan_way);
		etIntro = (EditText) this.findViewById(R.id.ETplan_intro);

		if (!onlyRead)
		{
			tvPublish.setText("编辑");
		}
		etTitle.setText(json_plan.getString("plan_title"));
		etStaPlace.setText(json_plan.getString("plan_startplace"));
		etEndPlace.setText(json_plan.getString("plan_endplace"));
		etStaDate.setText(json_plan.getString("plan_startdate"));
		etEndDate.setText(json_plan.getString("plan_enddate"));
		etWay.setText(json_plan.getString("plan_way"));

		if (json_plan.getString("plan_intro").equals("null"))
		{
			etIntro.setText("该用户对此计划未做详细说明");
		}
		else
		{
			etIntro.setText(json_plan.getString("plan_intro"));
		}

		tvSendTime.setText(json_plan.getString("plan_sendtime")
				.substring(0, 10));

		listener = new ClickListener();

		tvCancle.setOnClickListener(listener);
		tvPublish.setOnClickListener(listener);
	}

	//初始化DatePicker
	private void initDatePicker()
	{
		// 初始化DatePicker
		final Calendar cal = Calendar.getInstance();
		final int nowyear = cal.get(Calendar.YEAR);// 记录下系统当前日期
		final int nowmonth = cal.get(Calendar.MONTH);
		final int nowday = cal.get(Calendar.DAY_OF_MONTH);

		String startdate = json_plan.getString("plan_startdate");
		dpStaDate.init(Integer.parseInt(startdate.substring(0, 4)),
				Integer.parseInt(startdate.substring(5, 7)) - 1,
				Integer.parseInt(startdate.substring(8, startdate.length())),
				new OnDateChangedListener()
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

		String enddate = json_plan.getString("plan_enddate");
		dpEndDate.init(Integer.parseInt(enddate.substring(0, 4)),
				Integer.parseInt(enddate.substring(5, 7)) - 1,
				Integer.parseInt(enddate.substring(8, startdate.length())),
				new OnDateChangedListener()
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

	//用户在编辑模式下启用对EditText的编辑
	private void setEnable()
	{
		etTitle.setEnabled(true);
		etStaPlace.setEnabled(true);
		etEndPlace.setEnabled(true);
		etStaDate.setEnabled(true);
		etEndDate.setEnabled(true);
		etWay.setEnabled(true);
		etIntro.setEnabled(true);
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
					if (onlyRead)
					{
						finish();
					}
					else
					{
						if (tvPublish.getText().equals("编辑"))
						{
							tvPublish.setText("确认");
							if (json_plan.getString("plan_intro")
									.equals("null"))
							{
								etIntro.setText("");
							}

							dpStaDate = (DatePicker) PlanDetailsActivity.this
									.findViewById(R.id.DPsta_date);
							dpEndDate = (DatePicker) PlanDetailsActivity.this
									.findViewById(R.id.DPend_date);
							initDatePicker();
							setEnable();
							etStaDate.setOnClickListener(listener);
							etEndDate.setOnClickListener(listener);
						}
						else
						{
							if (TextUtils.isEmpty(etTitle.getText())
									|| TextUtils.isEmpty(etStaPlace.getText())
									|| TextUtils.isEmpty(etEndPlace.getText())
									|| TextUtils.isEmpty(etStaDate.getText())
									|| TextUtils.isEmpty(etEndDate.getText())
									|| TextUtils.isEmpty(etWay.getText()))
							{
								Toast.makeText(PlanDetailsActivity.this, "请将信息填写完整！",
										Toast.LENGTH_SHORT).show();
							}
							else
							{
								final Map<String, String> param = new HashMap<String, String>();
								param.put("plan_id", planid);
								param.put("plan_title", etTitle.getText()
										.toString());
								param.put("plan_startplace", etStaPlace
										.getText().toString());
								param.put("plan_endplace", etEndPlace.getText()
										.toString());
								param.put("plan_startdate", etStaDate.getText()
										.toString());
								param.put("plan_enddate", etEndDate.getText()
										.toString());
								param.put("plan_way", etWay.getText()
										.toString());

								if (TextUtils.isEmpty(etIntro.getText()))
								{
									param.put("plan_intro", "null");
								}
								else
								{
									param.put("plan_intro", etIntro.getText()
											.toString());
								}

								new Thread()
								{

									@Override
									public void run()
									{
										try
										{
											boolean flag = NetTransUtil
													.editPlan(param);
											handler.obtainMessage(EDIT_PLAN,
													flag).sendToTarget();
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
					}
					break;
				}
		}
	}

}
