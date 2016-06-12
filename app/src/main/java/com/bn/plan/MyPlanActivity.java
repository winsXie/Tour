package com.bn.plan;

import java.lang.ref.WeakReference;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bn.tour.R;
import com.bn.util.CheckUtil;
import com.bn.util.Constant;
import com.bn.util.NetTransUtil;

public class MyPlanActivity extends Activity
{
	private static final int GET_PLAN = 0;//获取计划（what值）
	private static final int LOAD_PAGE = 1;//分批加载
	private static final int UPDATE_PLAN = 2;//更新计划（发布计划成功后）
	private static final int DEL_PLAN = 3;//删除计划
	private static final int MATCH_PLAN = 4;//匹配计划
	private static final int ITEMCOUTN = 5;//分批加载每次加载的条目数
	private ListView listview;
	private MyPlanAdapter adapter;
	private JSONObject json_plan;
	private JSONArray jarr_plan;
	private GetPlanThread getPlanThread;

	private ScrollListener scrolllistener;
	private View footer;
	private boolean loading = false;//正在加载标志位
	private boolean hadTotalLoaded = false;// 分批加载完成标志位
	private int delPosition;//要删除的item所在位置

	private LinearLayout footerLayout;

	private Handler handler;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_plan);
		handler = new MyHandler(this);

		getPlanThread = new GetPlanThread();
		getPlanThread.start();

		Button buPubPlan = (Button) this.findViewById(R.id.BTpublish_plan);

		buPubPlan.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				Intent intent = new Intent(MyPlanActivity.this, PublishPlanActivity.class);
				startActivityForResult(intent, 1);
			}
		});

	}

	private static class MyHandler extends Handler
	{
		WeakReference<MyPlanActivity> mActivity;

		public MyHandler(MyPlanActivity activity)
		{
			mActivity = new WeakReference<MyPlanActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg)
		{
			MyPlanActivity currActivity = mActivity.get();
			switch (msg.what)
				{
				case GET_PLAN:
					String plan = (String) msg.obj;
					if (plan == null)
					{
						currActivity.jarr_plan = new JSONArray();
						currActivity.hadTotalLoaded = true;
					}
					else
					{
						plan = CheckUtil.replaceBlank(plan);
						currActivity.json_plan = JSONObject.fromObject(plan);
						currActivity.jarr_plan = currActivity.json_plan
								.getJSONArray("results");
						if (currActivity.jarr_plan.size() < ITEMCOUTN)
						{
							currActivity.hadTotalLoaded = true;
						}
					}
					currActivity.initList();
					break;
				case LOAD_PAGE:
					String get_json = (String) msg.obj;
					// 如果数据为空，则已全部加载完成，设标志位
					if (get_json == null)
					{
						currActivity.hadTotalLoaded = true;
					}
					else
					{
						get_json = CheckUtil.replaceBlank(get_json);// 替换回车符号，否则构造json报错
						JSONObject json_new = JSONObject.fromObject(get_json);
						JSONArray jarr_new = json_new.getJSONArray("results");

						if (jarr_new.size() < ITEMCOUTN)
						{
							currActivity.hadTotalLoaded = true;// 取得的数据小于一页
																// 的条数，全部加载完成
						}
						String json1 = currActivity.jarr_plan.toString();
						String json2 = jarr_new.toString();
						// 组装新的json,不能使用add方法，只能add一个元素
						String newjson = json1.substring(0, json1.length() - 1)
								+ "," + json2.substring(1, json2.length());
						currActivity.jarr_plan = JSONArray.fromObject(newjson);

						currActivity.adapter.notifyDataSetChanged();
					}

					if (currActivity.hadTotalLoaded)
					{
						TextView tv = new TextView(currActivity);
						tv.setText("已全部加载！");
						LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
								LayoutParams.MATCH_PARENT,
								LayoutParams.MATCH_PARENT);
						tv.setGravity(Gravity.CENTER);
						tv.setTextSize(20);
						tv.setLayoutParams(lp);
						currActivity.footerLayout.removeAllViews();
						currActivity.footerLayout.addView(tv);

					}
					else
					{
						currActivity.listview
								.removeFooterView(currActivity.footer);
					}
					currActivity.loading = false;
					break;
				case UPDATE_PLAN:
					String newplan = (String) msg.obj;
					if (newplan != null)
					{
						currActivity.jarr_plan.add(0, newplan);
						currActivity.adapter.notifyDataSetChanged();
						currActivity.listview.setSelection(0);
					}
					break;
				case MATCH_PLAN:
					String matchplan = (String) msg.obj;
					if (matchplan != null)
					{
						Intent intent = new Intent(currActivity,
								MatchPlanActivity.class);
						intent.putExtra("MATCHPLAN", matchplan);
						currActivity.startActivity(intent);
					}
					else
					{
						Toast.makeText(currActivity, "未匹配到其他人与您相同的计划！",
								Toast.LENGTH_LONG).show();
					}
					break;
				case DEL_PLAN:
					if ((Boolean) msg.obj)
					{
						currActivity.jarr_plan.remove(currActivity.delPosition);
						currActivity.adapter.notifyDataSetChanged();
						Toast.makeText(currActivity, "计划已删除！",
								Toast.LENGTH_SHORT).show();
					}
					else
					{
						Toast.makeText(currActivity, "删除失败！",
								Toast.LENGTH_SHORT).show();
					}
					break;
				}
			super.handleMessage(msg);
		}
	}

	private class MyPlanAdapter extends BaseAdapter
	{

		ViewHolder viewholder;

		@Override
		public int getCount()
		{
			// TODO Auto-generated method stub
			return jarr_plan.size();
		}

		@Override
		public Object getItem(int position)
		{
			return jarr_plan.get(position);
		}

		@Override
		public long getItemId(int position)
		{
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent)
		{
			final JSONObject json_temp = jarr_plan.getJSONObject(position);
			if (convertView == null)
			{
				convertView = getLayoutInflater().inflate(
						R.layout.item_my_plan, null);
				viewholder = new ViewHolder();

				viewholder.tvTitle = (TextView) convertView
						.findViewById(R.id.TVmyplan_title);
				viewholder.tvStaPlace = (TextView) convertView
						.findViewById(R.id.TVmyplan_staplace);
				viewholder.tvEndPlace = (TextView) convertView
						.findViewById(R.id.TVmyplan_endplace);
				viewholder.tvStaDate = (TextView) convertView
						.findViewById(R.id.TVmyplan_stadate);
				viewholder.tvMatch = (TextView) convertView
						.findViewById(R.id.TVmyplan_match);
				viewholder.imvDel = (ImageView) convertView
						.findViewById(R.id.IMVmy_plan_del);

				convertView.setTag(viewholder);
			}
			else
			{
				viewholder = (ViewHolder) convertView.getTag();
			}

			viewholder.tvTitle.setText(json_temp.getString("plan_title"));
			viewholder.tvStaPlace.setText(json_temp
					.getString("plan_startplace"));
			viewholder.tvEndPlace.setText(json_temp.getString("plan_endplace"));
			viewholder.tvStaDate.setText(json_temp.getString("plan_startdate")
					.substring(0, 10) + "出发");

			viewholder.tvMatch.setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View v)
				{
					// TODO Auto-generated method stub
					new Thread()
					{

						@Override
						public void run()
						{
							try
							{
								handler.obtainMessage(
										MATCH_PLAN,
										NetTransUtil.matchPlan(
												json_temp.getString("plan_id"),
												json_temp
														.getString("plan_endplace"),
												json_temp
														.getString("plan_startdate"),
												json_temp
														.getString("plan_enddate")))
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
			});

			viewholder.imvDel.setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View v)
				{
					// TODO Auto-generated method stub
					delPosition = position;
					new Thread()
					{

						@Override
						public void run()
						{
							try
							{
								handler.obtainMessage(
										DEL_PLAN,
										NetTransUtil.delPlan(json_temp
												.getString("plan_id")))
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
			});

			return convertView;
		}

		class ViewHolder
		{
			ImageView imvDel;
			TextView tvTitle, tvStaPlace, tvEndPlace, tvStaDate, tvMatch;
		}
	}

	//初始化列表
	private void initList()
	{
		listview = (ListView) this.findViewById(R.id.LVmyplan);

		footer = getLayoutInflater().inflate(R.layout.footer, null);
		footerLayout = (LinearLayout) footer.findViewById(R.id.LLfooter);

		adapter = new MyPlanAdapter();
		listview.setAdapter(adapter);

		scrolllistener = new ScrollListener();
		listview.setOnScrollListener(scrolllistener);

		listview.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3)
			{
				// TODO Auto-generated method stub
				Intent intent = new Intent(MyPlanActivity.this, PlanDetailsActivity.class);
				intent.putExtra("READTYPE", false);
				intent.putExtra("PLANID", jarr_plan.getJSONObject(arg2)
						.getString("plan_id"));
				startActivity(intent);
			}
		});

	}

	private class GetPlanThread extends Thread
	{

		@Override
		public void run()
		{
			try
			{
				String plan = NetTransUtil.getUserPlan(Constant.loginUid,
						0 + "");
				handler.obtainMessage(GET_PLAN, plan).sendToTarget();
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
					handler.obtainMessage(Constant.SOTIMEOUT).sendToTarget();
				}
				e.printStackTrace();
			}
		}

	}

	private class ScrollListener implements OnScrollListener
	{

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount)
		{
			int lastposi = listview.getLastVisiblePosition();

			/*
			 * totalItemCount和position都包括header和footer
			 * 只有在滑动到最后一个item，并且在没有全部加载完成，以及没有正在加载的情况下才继续加载下一批
			 */
			if ((lastposi + 1) == totalItemCount && !loading && !hadTotalLoaded)
			{
				loading = true;
				listview.addFooterView(footer);
				new Thread()
				{

					@Override
					public void run()
					{
						try
						{
							Thread.sleep(1 * 1000);
							String json_new_topic = NetTransUtil.getUserPlan(
									Constant.loginUid,
									jarr_plan.getJSONObject(
											jarr_plan.size() - 1).getString(
											"plan_id"));
							handler.obtainMessage(LOAD_PAGE, json_new_topic)
									.sendToTarget();
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
		}

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState)
		{
		}

	}

	//执行完startActivityForResult返回后调用 的方法
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		// TODO Auto-generated method stub
		switch (requestCode)
			{
			case 1:
				if (resultCode == 1)
				{
					if (data != null)
					{
						final String planid = data.getStringExtra("PLANID");
						if (planid != null)
						{
							new Thread()
							{

								@Override
								public void run()
								{
									try
									{
										handler.obtainMessage(
												UPDATE_PLAN,
												NetTransUtil
														.getPlanById(planid))
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
					}

				}
				break;
			}
		super.onActivityResult(requestCode, resultCode, data);
	}

}
