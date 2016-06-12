package com.bn.travelnote;

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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bn.thread.ImgAsyncDownload;
import com.bn.tour.R;
import com.bn.user.LookUserInfoActivity;
import com.bn.util.CheckNetworkStatus;
import com.bn.util.CheckUtil;
import com.bn.util.Constant;
import com.bn.util.NetTransUtil;

public class MoreTravelTopicActivity extends Activity
{
	private static final int GET_TRAT = 0;//获取游记主题
	private static final int LOAD_PAGE = 3;//分批加载
	private static final int DISPLAY_TOAST = 6;//展示toast信息
	private JSONObject json_trat;
	private JSONArray jarr_trat;
	private Intent intent;
	private ListView listview_trat;
	private ImgAsyncDownload imgdownload;//异步加载图片线程（listview内图片的加载）
	private TratAdapter tratadapter;
	private GetTratThread getTratThread;
	private RelativeLayout rlloading, rlloadstauts;//正在加载的缓冲布局和加载出错提示网络连接状态的布局
	private ImageView imvTip;//用于提示网络状态的imageview
	private TextView tvtip;
	private Clicklistener clicklistener;
	private ScrollListener scrolllistener;
	private View footer;
	private LinearLayout footerLayout;
	private static final int ITEMCOUTN = 5;//分批加载每次加载的条目数
	private boolean loading = false;// 正在加载标志位
	private boolean hadTotalLoaded = false;// 加载完成标志位

	private Handler handler;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_more_trat);
		handler = new MyHandler(this);

		rlloading = (RelativeLayout) this.findViewById(R.id.RLmoretrat_load);
		rlloading.setVisibility(View.VISIBLE);
		clicklistener = new Clicklistener();

		getTratThread = new GetTratThread();
		getTratThread.start();
	}

	private static class MyHandler extends Handler
	{
		WeakReference<MoreTravelTopicActivity> mActivity;

		public MyHandler(MoreTravelTopicActivity activity)
		{
			mActivity = new WeakReference<MoreTravelTopicActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg)
		{
			MoreTravelTopicActivity currActivity = mActivity.get();
			switch (msg.what)
				{
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
						if (currActivity.jarr_trat.size() < ITEMCOUTN)
						{
							currActivity.hadTotalLoaded = true;
						}
					}
					currActivity.initTratList();
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
						String json1 = currActivity.jarr_trat.toString();
						String json2 = jarr_new.toString();
						// 组装新的json,不能使用add方法，只能add一个元素
						String newjson = json1.substring(0, json1.length() - 1)
								+ "," + json2.substring(1, json2.length());
						currActivity.jarr_trat = JSONArray.fromObject(newjson);
						currActivity.tratadapter.notifyDataSetChanged();
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
						currActivity.listview_trat
								.removeFooterView(currActivity.footer);
					}
					currActivity.loading = false;
					break;
				case DISPLAY_TOAST:
					// 分批加载会发送此消息
					String toast = (String) msg.obj;
					if (toast != null)
					{
						Toast.makeText(currActivity, toast, Toast.LENGTH_SHORT)
								.show();
					}
					// 肯定是没有加载完，去除footer
					if (currActivity.listview_trat.getFooterViewsCount() > 0)
					{
						currActivity.listview_trat
								.removeFooterView(currActivity.footer);
					}
					currActivity.loading = false;// 重置标志位
					break;
				case Constant.CONNECTIONTIMEOUT:
					if (currActivity.rlloading.getVisibility() == View.VISIBLE)
					{
						currActivity.rlloading.setVisibility(View.GONE);
					}
					currActivity.rlloadstauts = (RelativeLayout) currActivity
							.findViewById(R.id.RLmoretrat_loadstauts);
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
							.findViewById(R.id.RLmoretrat_loadstauts);
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

	//初始化列表
	private void initTratList()
	{
		listview_trat = (ListView) this.findViewById(R.id.LVmore_tratop);
		tratadapter = new TratAdapter();
		listview_trat.setAdapter(tratadapter);

		footer = getLayoutInflater().inflate(R.layout.footer, null);
		footerLayout = (LinearLayout) footer.findViewById(R.id.LLfooter);

		// 滑动监听（分批加载）
		scrolllistener = new ScrollListener();
		listview_trat.setOnScrollListener(scrolllistener);

		listview_trat.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3)
			{
				Constant.currTratopId = jarr_trat.getJSONObject(arg2)
						.getString("tratop_id");
				Constant.currTratInfo = jarr_trat.getJSONObject(arg2)
						.toString();
				intent = new Intent(MoreTravelTopicActivity.this, NoteActivity.class);
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
					Intent intent = new Intent(MoreTravelTopicActivity.this,
							LookUserInfoActivity.class);
					intent.putExtra("USERID",
							json_temp.getString("tratop_userid"));
					startActivity(intent);
				}
			});

			return convertView;
		}
	}

	class ViewHolder
	{
		TextView tvTitle, tvPlace, tvUname, tvBrowcount, tvUage, tvTime;
		ImageView imvPic, imvHeadimg, imvSex;
	}

	private class Clicklistener implements OnClickListener
	{

		@Override
		public void onClick(View v)
		{
			// 去除提示
			if (rlloadstauts.getVisibility() == View.VISIBLE)
			{
				rlloadstauts.setVisibility(View.GONE);
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

			if ((getTratThread != null) && !getTratThread.isAlive())
			{
				if (rlloading.getVisibility() == View.GONE)
				{
					rlloading.setVisibility(View.VISIBLE);
				}

				getTratThread = new GetTratThread();
				getTratThread.start();
			}
		}

	}

	//获取游记线程
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

	//滑动监听
	private class ScrollListener implements OnScrollListener
	{
		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, final int totalItemCount)
		{
			int lastposi = listview_trat.getLastVisiblePosition();

			/*
			 * totalItemCount和position都包括header和footer
			 * 只有在滑动到最后一个item，并且在没有全部加载完成，以及没有正在加载的情况下才继续加载下一批
			 */
			if ((lastposi + 1) == totalItemCount && !loading && !hadTotalLoaded)
			{
				loading = true;
				listview_trat.addFooterView(footer);
				new Thread()
				{

					@Override
					public void run()
					{
						try
						{
							Thread.sleep(1 * 1000);
							String json_new_topic = NetTransUtil
									.getTraTopic(totalItemCount / ITEMCOUTN);
							handler.obtainMessage(LOAD_PAGE, json_new_topic)
									.sendToTarget();
						}
						catch (Exception e)
						{
							String mess = e.getClass().getName();
							if (mess.contains("ConnectTimeoutException")
									|| mess.contains("ConnectException"))
							{
								handler.obtainMessage(DISPLAY_TOAST, "连接超时！")
										.sendToTarget();
							}
							else if (mess.contains("SocketTimeoutException"))
							{
								handler.obtainMessage(DISPLAY_TOAST, "读取数据超时！")
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
}
