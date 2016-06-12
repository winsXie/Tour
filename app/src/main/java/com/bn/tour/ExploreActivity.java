package com.bn.tour;

import java.lang.ref.WeakReference;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bn.plan.PlanDetailsActivity;
import com.bn.thread.ImgAsyncDownload;
import com.bn.travelnote.MoreTravelTopicActivity;
import com.bn.travelnote.NoteActivity;
import com.bn.user.LookUserInfoActivity;
import com.bn.util.CheckNetworkStatus;
import com.bn.util.CheckUtil;
import com.bn.util.Constant;
import com.bn.util.NetTransUtil;
import com.bn.util.TimeChange;

public class ExploreActivity extends Activity
{
	private static final int GET_PLAN = 0;//获取结伴同行的计划
	private static final int GET_TRAT = 1;//获取推荐的游记主题
	private JSONObject json_plan, json_trat;
	private JSONArray jarr_plan, jarr_trat;
	private Intent intent;
	private ListView listview_plan, listview_trat;
	private ImgAsyncDownload imgdownload;//异步加载图片线程（listview内图片的加载）
	private PlanAdapter planadapter;
	private TratAdapter tratadapter;
	private GetPlanThread getPlanThread;
	private GetTratThread getTratThread;
	private long exitTime = 0;//初始化第一次按返回键的时间
	private RelativeLayout rlloading, rlloadstauts;//正在加载的缓冲布局和加载出错提示网络连接状态的布局
	private ImageView imvTip;//用于提示网络状态的imageview
	private TextView tvMoretrat, tvtip;
	private Clicklistener clicklistener;

	private Handler handler;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_explore);
		handler = new MyHandler(this);

		rlloading = (RelativeLayout) this.findViewById(R.id.RLexplore_load);
		rlloading.setVisibility(View.VISIBLE);
		clicklistener = new Clicklistener();

		getPlanThread = new GetPlanThread();
		getTratThread = new GetTratThread();
		getPlanThread.start();
		getTratThread.start();

		tvMoretrat = (TextView) this.findViewById(R.id.TVmore_tratop);
		tvMoretrat.setOnClickListener(clicklistener);
	}

	private static class MyHandler extends Handler
	{
		WeakReference<ExploreActivity> mActivity;

		public MyHandler(ExploreActivity activity)
		{
			mActivity = new WeakReference<ExploreActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg)
		{
			ExploreActivity currActivity = mActivity.get();

			switch (msg.what)
				{
				case GET_PLAN:
					if ((String) msg.obj == null)
					{
						currActivity.jarr_plan = new JSONArray();
					}
					else
					{
						String plan = (String) msg.obj;
						plan = CheckUtil.replaceBlank(plan);
						currActivity.json_plan = JSONObject
								.fromObject(plan);
						currActivity.jarr_plan = currActivity.json_plan
								.getJSONArray("results");
					}
					currActivity.initPlanList();
					break;
				case GET_TRAT:
					if ((String) msg.obj == null)
					{
						currActivity.jarr_trat = new JSONArray();
					}
					else
					{
						String trat = (String) msg.obj;
						trat = CheckUtil.replaceBlank(trat);
						currActivity.json_trat = JSONObject
								.fromObject(trat);
						currActivity.jarr_trat = currActivity.json_trat
								.getJSONArray("results");
					}
					currActivity.initTratList();
					break;
				case Constant.CONNECTIONTIMEOUT:
					if (currActivity.rlloading.getVisibility() == View.VISIBLE)
					{
						currActivity.rlloading.setVisibility(View.GONE);
					}
					currActivity.rlloadstauts = (RelativeLayout) currActivity
							.findViewById(R.id.RLexplore_loadstauts);
					currActivity.imvTip = (ImageView) currActivity
							.findViewById(R.id.IMVloadtip);
					currActivity.tvtip = (TextView) currActivity
							.findViewById(R.id.TVloadtip);
					currActivity.imvTip
							.setImageResource(R.drawable.network_error);

					if (CheckNetworkStatus.checkNetworkAvailable(currActivity))
					{// 手机网络可用
						currActivity.tvtip.setText("网络错误，点击重试");
						Toast.makeText(currActivity, "连接超时！", Toast.LENGTH_LONG)
								.show();
					}
					else
					{
						currActivity.tvtip.setText("请检查网络连接！");
						Toast.makeText(currActivity, "网络不可用！请检查网络连接！",
								Toast.LENGTH_LONG).show();
					}

					currActivity.rlloadstauts.setVisibility(View.VISIBLE);
					currActivity.imvTip
							.setOnClickListener(currActivity.clicklistener);
					break;
				case Constant.SOTIMEOUT:
					if (currActivity.rlloading.getVisibility() == View.VISIBLE)
					{
						currActivity.rlloading.setVisibility(View.GONE);
					}
					currActivity.rlloadstauts = (RelativeLayout) currActivity
							.findViewById(R.id.RLexplore_loadstauts);
					currActivity.imvTip = (ImageView) currActivity
							.findViewById(R.id.IMVloadtip);
					currActivity.tvtip = (TextView) currActivity
							.findViewById(R.id.TVloadtip);
					currActivity.imvTip.setImageResource(R.drawable.refresh);
					currActivity.tvtip.setText("点击重新加载");
					currActivity.rlloadstauts.setVisibility(View.VISIBLE);
					currActivity.imvTip
							.setOnClickListener(currActivity.clicklistener);
					Toast.makeText(currActivity, "读取数据超时！", Toast.LENGTH_LONG)
							.show();
					break;
				}
		}
	}

	//旅行计划的适配器
	private class PlanAdapter extends BaseAdapter
	{
		ViewHolder viewholder;

		@Override
		public int getCount()
		{
			return jarr_plan.size();
		}

		@Override
		public Object getItem(int position)
		{

			return jarr_plan.getJSONObject(position);
		}

		@Override
		public long getItemId(int position)
		{
			return position;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent)
		{
			final JSONObject json_temp = jarr_plan.getJSONObject(position);
			if (convertView == null)
			{
				viewholder = new ViewHolder();
				convertView = getLayoutInflater().inflate(
						R.layout.item_explore_together, null);
				viewholder.imvHeadimg = (ImageView) convertView
						.findViewById(R.id.IMVtoge_head);
				viewholder.imvSex = (ImageView) convertView
						.findViewById(R.id.IMVtoge_sex);
				viewholder.tvName = (TextView) convertView
						.findViewById(R.id.TVtoge_username);
				viewholder.tvAge = (TextView) convertView
						.findViewById(R.id.TVtoge_userage);
				viewholder.tvTime = (TextView) convertView
						.findViewById(R.id.TVtoge_sendtime);
				viewholder.tvPlace = (TextView) convertView
						.findViewById(R.id.TVtoge_endplace);

				convertView.setTag(viewholder);
			}
			else
			{
				viewholder = (ViewHolder) convertView.getTag();
			}
			viewholder.tvName.setText(json_temp.getString("user_name"));
			viewholder.tvAge.setText(json_temp.getString("user_age") + "岁");
			viewholder.tvTime.setText(TimeChange.changeTime(json_temp
					.getString("plan_sendtime")));
			viewholder.tvPlace.setText(json_temp.getString("plan_endplace"));

			if (json_temp.getString("user_sex").equals("男"))// sex
			{
				viewholder.imvSex.setImageResource(R.drawable.sex_man);
			}
			else
			{
				viewholder.imvSex.setImageResource(R.drawable.sex_woman);
			}

			imgdownload = ImgAsyncDownload.getInstance();
			if (imgdownload != null)
			{
				// 异步下载图片
				viewholder.imvHeadimg.setTag(json_temp.getString("user_image"));
				viewholder.imvHeadimg.setImageResource(R.drawable.defaultimage);
				imgdownload.imageDownload(viewholder.imvHeadimg);
			}

			viewholder.imvHeadimg.setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View v)
				{
					Intent intent = new Intent(ExploreActivity.this,
							LookUserInfoActivity.class);
					intent.putExtra("USERID",
							json_temp.getString("plan_userid"));
					startActivity(intent);
				}
			});
			return convertView;
		}

		class ViewHolder
		{
			TextView tvName, tvAge, tvTime, tvPlace;
			ImageView imvHeadimg, imvSex;
		}

	}

	//推荐游记主题的适配器
	private class TratAdapter extends BaseAdapter
	{
		ViewHolder viewholder;

		@Override
		public int getCount()
		{
			return jarr_trat.size();
		}

		@Override
		public Object getItem(int position)
		{
			return jarr_trat.getJSONObject(position);
		}

		@Override
		public long getItemId(int position)
		{
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			Log.d("position", position + "");
			final JSONObject json_temp = jarr_trat.getJSONObject(position);
			if (convertView == null)
			{
				viewholder = new ViewHolder();
				convertView = getLayoutInflater().inflate(
						R.layout.item_tratopic, null);
				viewholder.imvPic = (ImageView) convertView
						.findViewById(R.id.IMVtrat_pic);
				viewholder.imvHeadimg = (ImageView) convertView
						.findViewById(R.id.IMVtrat_head);
				viewholder.imvSex = (ImageView) convertView
						.findViewById(R.id.IMVtrat_usex);
				viewholder.tvTitle = (TextView) convertView
						.findViewById(R.id.TVtrat_title);
				viewholder.tvPlace = (TextView) convertView
						.findViewById(R.id.TVtrat_place);
				viewholder.tvUname = (TextView) convertView
						.findViewById(R.id.TVtrat_uname);
				viewholder.tvUage = (TextView) convertView
						.findViewById(R.id.TVtrat_uage);
				viewholder.tvBrowcount = (TextView) convertView
						.findViewById(R.id.TVtrat_browcount);
				viewholder.tvTime = (TextView) convertView
						.findViewById(R.id.TVtrat_sendtime);

				convertView.setTag(viewholder);
			}
			else
			{
				viewholder = (ViewHolder) convertView.getTag();
			}
			viewholder.tvTitle.setText(json_temp.getString("tratop_name"));
			viewholder.tvPlace.setText(json_temp.getString("tratop_place"));
			viewholder.tvUname.setText(json_temp.getString("user_name"));
			viewholder.tvUage.setText(json_temp.getString("user_age") + "岁");
			viewholder.tvBrowcount.setText(json_temp
					.getString("tratop_browcount") + "次阅读");
			viewholder.tvTime.setText(json_temp.getString("tratop_createdate")
					.substring(0, 10));

			if (json_temp.getString("user_sex").equals("男"))// sex
			{
				viewholder.imvSex.setImageResource(R.drawable.sex_man);
			}
			else
			{
				viewholder.imvSex.setImageResource(R.drawable.sex_woman);
			}

			imgdownload = ImgAsyncDownload.getInstance();
			if (imgdownload != null)
			{
				// 异步下载图片
				viewholder.imvHeadimg.setTag(json_temp.getString("user_image"));
				viewholder.imvHeadimg.setImageResource(R.drawable.defaultimage);
				imgdownload.imageDownload(viewholder.imvHeadimg);
				String tratPic = json_temp.getString("tratop_pic");
				if (!tratPic.equals("null"))
				{
					viewholder.imvPic.setTag(tratPic);
					viewholder.imvPic
							.setImageResource(R.drawable.tratop_background);
					imgdownload.imageDownload(viewholder.imvPic);
				}
				else
				{// 防止图片错乱
					viewholder.imvPic
							.setImageResource(R.drawable.tratop_background);
				}
			}

			viewholder.imvHeadimg.setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View v)
				{
					Intent intent = new Intent(ExploreActivity.this,
							LookUserInfoActivity.class);
					intent.putExtra("USERID",
							json_temp.getString("tratop_userid"));
					startActivity(intent);
				}
			});

			return convertView;
		}

		class ViewHolder
		{
			TextView tvTitle, tvPlace, tvUname, tvBrowcount, tvUage, tvTime;
			ImageView imvPic, imvHeadimg, imvSex;
		}

	}

	//初始化计划
	private void initPlanList()
	{
		listview_plan = (ListView) this.findViewById(R.id.LVexpl_together);
		planadapter = new PlanAdapter();
		listview_plan.setAdapter(planadapter);
		setListViewHeight(listview_plan);

		listview_plan.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3)
			{
				// TODO Auto-generated method stub
				Log.d("position",
						jarr_plan.getJSONObject(arg2).getString("plan_id"));
				intent = new Intent(ExploreActivity.this, PlanDetailsActivity.class);
				intent.putExtra("READTYPE", true);
				intent.putExtra("PLANID", jarr_plan.getJSONObject(arg2)
						.getString("plan_id"));
				startActivity(intent);
			}
		});

		// 去除加载缓冲图标
		if (rlloading.getVisibility() == View.VISIBLE)
		{
			rlloading.setVisibility(View.GONE);
		}
	}

	//初始化推荐主题
	private void initTratList()
	{
		listview_trat = (ListView) this.findViewById(R.id.LVexpl_tratop);
		tratadapter = new TratAdapter();
		listview_trat.setAdapter(tratadapter);
		setListViewHeight(listview_trat);

		listview_trat.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3)
			{
				Log.d("position",
						jarr_trat.getJSONObject(arg2).getString("tratop_id"));
				Constant.currTratopId = jarr_trat.getJSONObject(arg2)
						.getString("tratop_id");
				Constant.currTratInfo = jarr_trat.getJSONObject(arg2)
						.toString();
				intent = new Intent(ExploreActivity.this, NoteActivity.class);
				intent.putExtra("READTYPE", true);
				startActivity(intent);
			}
		});

		// 去除加载缓冲图标
		if (rlloading.getVisibility() == View.VISIBLE)
		{
			rlloading.setVisibility(View.GONE);
		}

	}

	//动态设置listview的高度
	public void setListViewHeight(ListView listView)
	{
		if (listView == null)
			return;

		ListAdapter listAdapter = listView.getAdapter();
		if (listAdapter == null)
		{
			return;
		}

		int totalHeight = 0;
		for (int i = 0; i < listAdapter.getCount(); i++)
		{
			View listItem = listAdapter.getView(i, null, listView);
			listItem.measure(0, 0);
			totalHeight += listItem.getMeasuredHeight();
		}

		ViewGroup.LayoutParams params = listView.getLayoutParams();
		params.height = totalHeight
				+ (listView.getDividerHeight() * (listAdapter.getCount() - 1));
		listView.setLayoutParams(params);
	}

	private class GetPlanThread extends Thread
	{

		@Override
		public void run()
		{
			try
			{
				String plan = NetTransUtil.getTogetherPlan();
				handler.obtainMessage(GET_PLAN, plan).sendToTarget();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

	}

	private class GetTratThread extends Thread
	{

		@Override
		public void run()
		{
			try
			{
				String trat = NetTransUtil.getTraTopic(0);
				handler.obtainMessage(GET_TRAT, trat).sendToTarget();
			}
			catch (Exception e)
			{
				String mess = e.getClass().getName();
				if (mess.contains("ConnectTimeoutException")
						|| mess.contains("ConnectException"))
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

	private class Clicklistener implements OnClickListener
	{

		@Override
		public void onClick(View v)
		{
			switch (v.getId())
				{
				case R.id.TVmore_tratop:
					intent = new Intent(ExploreActivity.this,
							MoreTravelTopicActivity.class);
					startActivity(intent);
					break;
				case R.id.IMVloadtip:
					// 去除提示
					if (rlloadstauts.getVisibility() == View.VISIBLE)
					{
						rlloadstauts.setVisibility(View.GONE);
					}

					if ((getPlanThread != null) && !getPlanThread.isAlive())
					{
						if (rlloading.getVisibility() == View.GONE)
						{
							rlloading.setVisibility(View.VISIBLE);
						}

						getPlanThread = new GetPlanThread();
						getPlanThread.start();
					}

					if ((getTratThread != null) && !getTratThread.isAlive())
					{
						if (rlloading.getVisibility() == View.GONE)
						{
							rlloading.setVisibility(View.VISIBLE);
						}

						getTratThread = new GetTratThread();
						getTratThread.start();
					}
					break;
				}
		}

	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		// TODO Auto-generated method stub
		// super.onSaveInstanceState(outState);
	}

	//重写返回键，实现按两次退出程序
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			// 通过获取当前时间，在计算时间差来判断是否退出，第一次按肯定不会退出，因为exitTime被初始化为0，
			// 时间差是当前时间距1970年1月1日凌晨的时间，大于阈值，之后再判断距上次按的时间是否大于2000ms
			if ((System.currentTimeMillis() - exitTime) > 2000)
			{
				Toast.makeText(this, "再按一次退出悠悠", Toast.LENGTH_SHORT).show();
				exitTime = System.currentTimeMillis();// 重置上次按下时间
			}
			else
			{
				finish();
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

}
