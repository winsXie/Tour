package com.bn.bbs;

import java.lang.ref.WeakReference;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.bn.thread.ImgAsyncDownload;
import com.bn.tour.R;
import com.bn.util.CheckNetworkStatus;
import com.bn.util.CheckUtil;
import com.bn.util.Constant;
import com.bn.util.NetTransUtil;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class BlockActivity extends Activity
{
	private static final int ITEMCOUTN = 10;//分批加载每次加载的条目数
	private ImgAsyncDownload imgdownload;//异步加载线程
	private BlockAdapter adapter;//listview适配器
	private ScrollListener scrolistener;//滑动监听
	private JSONObject json_block;//存储block数据的JSONObject
	private JSONArray jarr_block;//根据JSONObject得到的JSONArray（adapter的数据源）
	private EditText edt_search;
	private GetBlockThread getBlockThread;//获取block信息线程
	private ListView listview;//展示信息的listview
	private View footer;//listview的footer视图
	private LinearLayout footerLayout;//listview的footer布局
	private boolean loading = false;// 正在加载标志位
	private boolean hadTotalLoaded = false;// 加载完成标志位
	private long exitTime = 0;//初始化第一次按返回键的时间
	private RelativeLayout rlloading, rlloadstauts;//正在加载的缓冲布局和加载出错提示网络连接状态的布局
	private ImageView imvTip;//用于提示网络状态的imageview
	private TextView tvtip;//提示的文字信息
	private Clicklistener clicklistener;//继承自OnclickListener的类，用于处理点击事件

	private Handler handler;//用于处理线程间消息传递的handler

	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_block);
		handler = new MyHandler(this);

		rlloading = (RelativeLayout) this.findViewById(R.id.RLblock_load);
		rlloading.setVisibility(View.VISIBLE);
		clicklistener = new Clicklistener();

		edt_search = (EditText) this.findViewById(R.id.ETsearch);
		footer = getLayoutInflater().inflate(R.layout.footer, null);
		footerLayout = (LinearLayout) footer.findViewById(R.id.LLfooter);

		getBlockThread = new GetBlockThread();
		getBlockThread.start();

		edt_search.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				Intent intent = new Intent();
				intent.setClass(BlockActivity.this, SearchBlockActivity.class);
				startActivity(intent);
			}
		});

	}

	private static class MyHandler extends Handler
	{
		WeakReference<BlockActivity> mActivity;

		public MyHandler(BlockActivity activity)
		{
			mActivity = new WeakReference<BlockActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg)
		{
			BlockActivity currActivity = mActivity.get();
			switch (msg.what)
				{
				case 0:
					if ((String) msg.obj == null)
					{
						currActivity.jarr_block = new JSONArray();
					}
					else
					{
						String block = (String) msg.obj;
						block = CheckUtil.replaceBlank(block);
						currActivity.json_block = JSONObject.fromObject(block);
						currActivity.jarr_block = currActivity.json_block
								.getJSONArray("results");
						if (currActivity.jarr_block.size() < ITEMCOUTN)
						{
							currActivity.hadTotalLoaded = true;
						}
					}
					currActivity.initBlock();
					break;
				case 1:
					String get_json = (String) msg.obj;
					if (get_json == null)
					{
						currActivity.hadTotalLoaded = true;
					}
					else
					{
						get_json = CheckUtil.replaceBlank(get_json);
						JSONObject json_new = JSONObject.fromObject(get_json);
						JSONArray jarr_new = json_new.getJSONArray("results");
						if (jarr_new.size() < ITEMCOUTN)
						{
							currActivity.hadTotalLoaded = true;
						}
						String json2 = jarr_new.toString();
						currActivity.jarr_block.add(
								currActivity.jarr_block.size(),
								json2.substring(1, json2.length() - 1));
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
				case Constant.CONNECTIONTIMEOUT:
					if (currActivity.rlloading.getVisibility() == View.VISIBLE)
					{
						currActivity.rlloading.setVisibility(View.GONE);
					}
					currActivity.rlloadstauts = (RelativeLayout) currActivity
							.findViewById(R.id.RLblock_loadstauts);
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
							.findViewById(R.id.RLblock_loadstauts);
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

	//从网络上获取信息成功后初始化block列表
	private void initBlock()
	{
		listview = (ListView) this.findViewById(R.id.LVblock);
		adapter = new BlockAdapter();
		listview.setAdapter(adapter);
		// 没有数据时设加载完成标志位，防止加载出footer
		if (jarr_block.size() == 0)
		{
			hadTotalLoaded = true;
		}
		scrolistener = new ScrollListener();
		listview.setOnScrollListener(scrolistener);

		listview.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					final int arg2, long arg3)
			{
				Constant.currBloId = jarr_block.getJSONObject(arg2).getString(
						"blo_id");
				Constant.currBlockInfo = jarr_block.getJSONObject(arg2);
				// 将版块的点击数量+1(Android)
				new Thread()
				{

					@Override
					public void run()
					{
						NetTransUtil.addBloCliCount(jarr_block.getJSONObject(
								arg2).getString("blo_id"));
					}

				}.start();
				Intent intent = new Intent();
				intent.setClass(BlockActivity.this, TopicActivity.class);
				BlockActivity.this.startActivity(intent);
			}
		});

		// 去除加载缓冲图标
		if (rlloading.getVisibility() == View.VISIBLE)
		{
			rlloading.setVisibility(View.GONE);
		}
	}

	private class BlockAdapter extends BaseAdapter
	{
		ViewHolder viewholder;

		@Override
		public int getCount()
		{
			return jarr_block.size();
		}

		@Override
		public Object getItem(int position)
		{
			return jarr_block.getJSONObject(position);
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
			final JSONObject json_temp = jarr_block.getJSONObject(position);
			// 重用View
			if (convertView == null)
			{
				convertView = getLayoutInflater().inflate(R.layout.item_block,
						null);
				viewholder = new ViewHolder();

				viewholder.imvBloPic = (ImageView) convertView
						.findViewById(R.id.IMVblopic);
				viewholder.tvBloName = (TextView) convertView
						.findViewById(R.id.TVbloname);

				convertView.setTag(viewholder);// 设置标签
			}
			else
			{
				viewholder = (ViewHolder) convertView.getTag();// 根据标签得到视图
			}

			viewholder.tvBloName.setText(json_temp.getString("blo_name"));

			imgdownload = ImgAsyncDownload.getInstance();

			if (imgdownload != null)
			{
				// 异步下载图片
				viewholder.imvBloPic.setTag(json_temp.getString("blo_pic"));
				viewholder.imvBloPic.setImageResource(R.drawable.defaultblockimg);
				imgdownload.imageDownload(viewholder.imvBloPic);
			}

			viewholder.imvBloPic.setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View v)
				{
					Intent intent = new Intent(BlockActivity.this,
							BlockInfoActivity.class);
					intent.putExtra("READTYPE", true);
					intent.putExtra("BLOCKID", json_temp.getString("blo_id"));
					startActivity(intent);
				}
			});

			return convertView;
		}

		private class ViewHolder
		{
			ImageView imvBloPic;
			TextView tvBloName;
		}

	}

	//加载block信息线程
	private class GetBlockThread extends Thread
	{

		@Override
		public void run()
		{
			try
			{
				String block = NetTransUtil.getNfBlockInfo(0, "false");
				handler.obtainMessage(0, block).sendToTarget();
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
			// 去除提示
			if (rlloadstauts.getVisibility() == View.VISIBLE)
			{
				rlloadstauts.setVisibility(View.GONE);
			}

			if ((getBlockThread != null) && !getBlockThread.isAlive())
			{
				if (rlloading.getVisibility() == View.GONE)
				{
					rlloading.setVisibility(View.VISIBLE);
				}

				getBlockThread = new GetBlockThread();
				getBlockThread.start();
			}
		}

	}

	//滑动监听（用于分批加载）
	private class ScrollListener implements OnScrollListener
	{

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount)
		{
			final int totalcount = totalItemCount;
			int lastposi = listview.getLastVisiblePosition();
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
							Thread.sleep(3000);
							String json_new_block = NetTransUtil
									.getNfBlockInfo(totalcount / ITEMCOUTN,
											"false");
							handler.obtainMessage(1, json_new_block)
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
