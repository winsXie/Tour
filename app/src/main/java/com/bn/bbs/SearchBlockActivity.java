package com.bn.bbs;

import java.lang.ref.WeakReference;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.bn.thread.ImgAsyncDownload;
import com.bn.tour.R;
import com.bn.util.Constant;
import com.bn.util.NetTransUtil;

import android.app.Activity;
import android.app.ActionBar.LayoutParams;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

public class SearchBlockActivity extends Activity
{
	private ImgAsyncDownload imgdownload;//异步加载线程
	private BlockAdapter adapter;
	private ScrollListener scrolistener;
	private JSONObject json_search;//存储block数据的JSONObject
	private JSONArray jarr_search;//根据JSONObject得到的JSONArray（adapter的数据源）
	private EditText edt_search;
	private Button bloSearch;
	private ListView listview;
	private View footer;
	private LinearLayout footerLayout;
	private LoadThread loadthread;

	private static final int ITEMCOUTN = 10;//分批加载每次加载的条目数
	private boolean loading = false;
	private boolean hadTotalLoaded = false;// 分批加载完成标志位

	private Handler handler;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_searchblock);

		handler = new MyHandler(this);
		edt_search = (EditText) this.findViewById(R.id.ETsearch);
		bloSearch = (Button) this.findViewById(R.id.BTsearch);

		edt_search.requestFocus();
		footer = getLayoutInflater().inflate(R.layout.footer, null);
		footerLayout = (LinearLayout) footer.findViewById(R.id.LLfooter);

		bloSearch.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				if (!TextUtils.isEmpty(edt_search.getText().toString()))
				{
					loadthread = new LoadThread(
							edt_search.getText().toString(), 0, "false", 0);
					loadthread.start();
					hadTotalLoaded = false;// 重置标志位
					edt_search.clearFocus();
				}
				else
				{
					Toast.makeText(SearchBlockActivity.this, "请输入内容！",
							Toast.LENGTH_LONG).show();
				}
			}
		});

	}

	private static class MyHandler extends Handler
	{
		WeakReference<SearchBlockActivity> mActivity;

		public MyHandler(SearchBlockActivity activity)
		{
			mActivity = new WeakReference<SearchBlockActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg)
		{
			SearchBlockActivity currActivity = mActivity.get();
			switch (msg.what)
				{//第一次搜索查询
				case 0:
					if ((String) msg.obj != null)
					{
						currActivity.json_search = JSONObject
								.fromObject((String) msg.obj);
						currActivity.jarr_search = currActivity.json_search
								.getJSONArray("results");
						if (currActivity.jarr_search.size() < ITEMCOUTN)
						{
							currActivity.hadTotalLoaded = true;
						}
						currActivity.initBlock();
					}
					else
					{
						if (currActivity.jarr_search != null
								&& currActivity.jarr_search.size() > 0)
						{
							currActivity.jarr_search = new JSONArray();
							currActivity.adapter.notifyDataSetChanged();
						}
						Toast.makeText(currActivity, "未匹配到相符信息！",
								Toast.LENGTH_LONG).show();
					}
					break;
					//分批加载查询
				case 1:
					String get_json = (String) msg.obj;
					if (get_json == null)
					{
						currActivity.hadTotalLoaded = true;
					}
					else
					{
						JSONObject json_new = JSONObject.fromObject(get_json);
						JSONArray jarr_new = json_new.getJSONArray("results");
						if (jarr_new.size() < ITEMCOUTN)
						{
							currActivity.hadTotalLoaded = true;
						}
						String json2 = jarr_new.toString();
						currActivity.jarr_search.add(
								currActivity.jarr_search.size(),
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
				}

		}
	}

	private void initBlock()
	{
		listview = (ListView) this.findViewById(R.id.LVblock_search);
		adapter = new BlockAdapter();
		listview.setAdapter(adapter);
		scrolistener = new ScrollListener();
		listview.setOnScrollListener(scrolistener);

		listview.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					final int arg2, long arg3)
			{
				Constant.currBloId = jarr_search.getJSONObject(arg2).getString(
						"blo_id");
				Constant.currBlockInfo = jarr_search.getJSONObject(arg2);
				// 将版块的点击数量+1(Android)
				new Thread()
				{

					@Override
					public void run()
					{
						NetTransUtil.addBloCliCount(jarr_search.getJSONObject(
								arg2).getString("blo_id"));
					}

				}.start();
				Intent intent = new Intent();
				intent.setClass(SearchBlockActivity.this, TopicActivity.class);
				SearchBlockActivity.this.startActivity(intent);
			}
		});

	}

	private class BlockAdapter extends BaseAdapter
	{
		ViewHolder viewholder;

		@Override
		public int getCount()
		{
			return jarr_search.size();
		}

		@Override
		public Object getItem(int position)
		{
			return jarr_search.getJSONObject(position);
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
			final JSONObject json_temp = jarr_search.getJSONObject(position);
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
				viewholder.imvBloPic.setImageResource(R.drawable.defaultimage);
				imgdownload.imageDownload(viewholder.imvBloPic);
			}

			viewholder.imvBloPic.setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View v)
				{
					Intent intent = new Intent(SearchBlockActivity.this,
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

				loadthread = new LoadThread(edt_search.getText().toString(),
						totalcount / ITEMCOUTN, "false", 1);
				loadthread.start();
			}

		}

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState)
		{
			// TODO Auto-generated method stub

		}

	}

	//按条件搜索版块线程
	private class LoadThread extends Thread
	{
		String name;
		int page;
		String bool;
		int what;

		public LoadThread(String name, int page, String bool, int what)
		{
			this.name = name;
			this.page = page;
			this.bool = bool;
			this.what = what;
		}

		@Override
		public void run()
		{
			String json_new_block;
			try
			{
				json_new_block = NetTransUtil.getNfBloinfoByName(name, page,
						bool);
				handler.obtainMessage(what, json_new_block).sendToTarget();
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

}
