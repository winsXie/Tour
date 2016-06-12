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
import android.util.Log;
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

import com.bn.thread.ImgAsyncDownload;
import com.bn.tour.R;
import com.bn.util.CheckUtil;
import com.bn.util.Constant;
import com.bn.util.NetTransUtil;

public class MyTravelTopicActivity extends Activity
{
	private static final int GET_TRATOPIC = 0;//获取游记主题
	private static final int UPDATE_TRAT = 1;//更新游记主题
	private static final int LOAD_PAGE = 3;//分批加载
	private static final int ITEMCOUTN = 5;//每次加载条目数
	private JSONObject json_trat;
	private JSONArray jarr_trat;
	private Intent intent;
	private ListView listview;
	private Button btCreTopic;
	private ImgAsyncDownload imgdownload;
	private TratAdapter tratadapter;
	private GetTratThread getTratThread;

	private View footer;

	private LinearLayout footerLayout;
	private ScrollListener scrolllistener;
	private boolean loading = false;
	private boolean hadTotalLoaded = false;

	private Handler handler;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_traveltopic);
		handler = new MyHandler(this);

		getTratThread = new GetTratThread();
		getTratThread.start();

		btCreTopic = (Button) this.findViewById(R.id.BTmy_trat_create);
		btCreTopic.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				if (listview == null)
				{
					Toast.makeText(MyTravelTopicActivity.this, "数据初始化失败！",
							Toast.LENGTH_LONG).show();
				}
				else
				{
					Intent intent = new Intent(MyTravelTopicActivity.this,
							CreateTravelTopicActivity.class);
					startActivityForResult(intent, 0);
				}
			}
		});
	}

	private static class MyHandler extends Handler
	{
		WeakReference<MyTravelTopicActivity> mActivity;

		public MyHandler(MyTravelTopicActivity activity)
		{
			mActivity = new WeakReference<MyTravelTopicActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg)
		{
			MyTravelTopicActivity currActivity = mActivity.get();
			switch (msg.what)
				{
				case GET_TRATOPIC:
					String trat = (String) msg.obj;
					if (trat == null)
					{
						currActivity.jarr_trat = new JSONArray();
						currActivity.hadTotalLoaded = true;
					}
					else
					{
						trat = CheckUtil.replaceBlank(trat);
						currActivity.json_trat = JSONObject.fromObject(trat);
						currActivity.jarr_trat = currActivity.json_trat
								.getJSONArray("results");
						if (currActivity.jarr_trat.size() < ITEMCOUTN)
						{
							currActivity.hadTotalLoaded = true;// 取得的数据小于一页
																// 的条数，全部加载完成
						}
					}
					currActivity.initTratList();
					break;
				case UPDATE_TRAT:
					String tarinfo = (String) msg.obj;
					if (tarinfo != null)
					{
						currActivity.jarr_trat.add(0, tarinfo);
						currActivity.tratadapter.notifyDataSetChanged();
						currActivity.listview.setSelection(0);
					}
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
						currActivity.listview
								.removeFooterView(currActivity.footer);
					}
					currActivity.loading = false;
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
			JSONObject json_temp = jarr_trat.getJSONObject(position);
			if (convertView == null)
			{
				viewholder = new ViewHolder();
				convertView = getLayoutInflater().inflate(
						R.layout.item_my_travelnote, null);
				viewholder.imvPic = (ImageView) convertView
						.findViewById(R.id.IMVmy_trat_pic);
				viewholder.tvTitle = (TextView) convertView
						.findViewById(R.id.TVmy_trat_title);
				viewholder.tvPlace = (TextView) convertView
						.findViewById(R.id.TVmy_trat_place);
				viewholder.tvBrowcount = (TextView) convertView
						.findViewById(R.id.TVmy_trat_browcount);
				viewholder.tvTime = (TextView) convertView
						.findViewById(R.id.TVmy_trat_sendtime);

				convertView.setTag(viewholder);
			}
			else
			{
				viewholder = (ViewHolder) convertView.getTag();
			}
			viewholder.tvTitle.setText(json_temp.getString("tratop_name"));
			viewholder.tvPlace.setText(json_temp.getString("tratop_place"));
			viewholder.tvBrowcount.setText(json_temp
					.getString("tratop_browcount") + "次阅读");
			viewholder.tvTime.setText(json_temp.getString("tratop_createdate")
					.substring(0, 10));

			imgdownload = ImgAsyncDownload.getInstance();
			if (imgdownload != null)
			{
				// 异步下载图片
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
			return convertView;
		}

		class ViewHolder
		{
			TextView tvTitle, tvPlace, tvBrowcount, tvTime;
			ImageView imvPic;
		}

	}

	//初始化列表
	private void initTratList()
	{
		listview = (ListView) this.findViewById(R.id.LVmy_tranote_tratop);
		tratadapter = new TratAdapter();
		listview.setAdapter(tratadapter);

		footer = getLayoutInflater().inflate(R.layout.footer, null);
		footerLayout = (LinearLayout) footer.findViewById(R.id.LLfooter);

		scrolllistener = new ScrollListener();
		listview.setOnScrollListener(scrolllistener);

		listview.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3)
			{
				Log.d("position",
						jarr_trat.getJSONObject(arg2).getString("tratop_id"));
				Constant.currTratopId = jarr_trat.getJSONObject(arg2)
						.getString("tratop_id");
				intent = new Intent(MyTravelTopicActivity.this, MyNoteActivity.class);
				intent.putExtra("TRATINFO", jarr_trat.getJSONObject(arg2)
						.toString());
				startActivity(intent);
			}
		});

		btCreTopic = (Button) this.findViewById(R.id.BTmy_trat_create);
	}

	private class GetTratThread extends Thread
	{

		@Override
		public void run()
		{
			try
			{
				String trat = NetTransUtil.getTratopByUid(Constant.loginUid,
						"false", 0 + "");
				handler.obtainMessage(GET_TRATOPIC, trat).sendToTarget();
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
							String json_new_topic = NetTransUtil
									.getTratopByUid(
											Constant.loginUid,
											"false",
											jarr_trat.getJSONObject(
													jarr_trat.size() - 1)
													.getString("tratop_id"));
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

	//用于创建主题后调用
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		// TODO Auto-generated method stub
		switch (requestCode)
			{
			case 0:
				if (resultCode == 1)
				{
					if (data != null)
					{
						// 刚刚发表的主题帖ID，据此取得主题帖信息
						final String tratid = data.getStringExtra("TRATID");
						if (tratid != null)
						{
							new Thread()
							{

								@Override
								public void run()
								{
									try
									{
										String tratinfo = NetTransUtil
												.getTratById(tratid);
										handler.obtainMessage(UPDATE_TRAT,
												tratinfo).sendToTarget();
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

				}
				break;
			}
		super.onActivityResult(requestCode, resultCode, data);
	}

}
