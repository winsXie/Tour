package com.bn.bbs;

import java.lang.ref.WeakReference;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bn.tour.R;
import com.bn.util.CheckUtil;
import com.bn.util.Constant;
import com.bn.util.NetTransUtil;

public class MyBlockActivity extends Activity
{
	private static final int GET_BLOCK = 0;//获取block信息
	private static final int UPDATE_BLOCK = 1;//更新block信息（创建版块成功之后更新列表）
	private TextView tvNorm, tvIspass, tvForb;
	//展示版块信息的listview（正常活跃的版块、禁用的版块、待审核和审核未通过的版块）
	private ListView normListView, forbListView, ispassListView;

	private JSONObject json_block;//存储所有版块信息的JSONObject
	private JSONArray jarr_block, jarr_norm, jarr_ispass, jarr_forb;//存储相应信息的JSONArray
	private GetBlockThread getBlock;

	//相应的adapter
	private MyBlockAdapter adapter_norm = null;
	private MyBlockAdapter adapter_ispass = null;
	private MyBlockAdapter adapter_forb = null;

	private Handler handler;

	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_block);
		handler = new MyHandler(this);

		tvNorm = (TextView) this.findViewById(R.id.TVmyblo_norm);
		tvForb = (TextView) this.findViewById(R.id.TVmyblo_forb);
		tvIspass = (TextView) this.findViewById(R.id.TVmyblo_ispass);
		Button buCreate = (Button) this.findViewById(R.id.BUmyblo_create);

		getBlock = new GetBlockThread();
		getBlock.start();
		buCreate.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				if (Integer.parseInt(Constant.currUserInfo
						.getString("user_level")) < 10)
				{
					Toast.makeText(MyBlockActivity.this, "你的等级未达到10级\n没有权限创建版块！",
							Toast.LENGTH_LONG).show();
				}
				else
				{
					Intent intent = new Intent(MyBlockActivity.this,
							CreateBlockActivity.class);
					startActivityForResult(intent, 1);
				}
			}
		});

	}

	private static class MyHandler extends Handler
	{
		WeakReference<MyBlockActivity> mActivity;

		public MyHandler(MyBlockActivity activity)
		{
			mActivity = new WeakReference<MyBlockActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg)
		{
			MyBlockActivity currActivity = mActivity.get();
			switch (msg.what)
				{
				case GET_BLOCK:
					String block = (String) msg.obj;
					if (block == null)
					{
						currActivity.jarr_block = new JSONArray();
					}
					else
					{
						block = CheckUtil.replaceBlank((String) msg.obj);
						currActivity.json_block = JSONObject.fromObject(block);
						currActivity.jarr_block = currActivity.json_block
								.getJSONArray("results");
					}
					currActivity.setView(currActivity.jarr_block);
					break;
				case UPDATE_BLOCK:
					String bloinfo = (String) msg.obj;
					if (bloinfo != null)
					{
						currActivity.jarr_ispass.add(0, bloinfo);
						if (currActivity.adapter_ispass == null)
						{
							currActivity.initIspassView();
						}
						else
						{
							currActivity.adapter_ispass.notifyDataSetChanged();
							currActivity
									.setListViewHeight(currActivity.ispassListView);
						}

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

	//初始化正常版块listview
	private void initNormView()
	{
		normListView = (ListView) this.findViewById(R.id.LVmyblo_norm);
		if (jarr_norm.size() != 0)
		{
			adapter_norm = new MyBlockAdapter(jarr_norm, true);
			normListView.setAdapter(adapter_norm);
			setListViewHeight(normListView);
			normListView.setOnItemClickListener(new OnItemClickListener()
			{

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3)
				{
					Intent intent = new Intent(MyBlockActivity.this,
							BlockInfoActivity.class);
					intent.putExtra("READTYPE", false);
					intent.putExtra("BLOCKID", jarr_norm.getJSONObject(arg2)
							.getString("blo_id"));
					startActivity(intent);
				}
			});
		}
		else
		{
			normListView.setVisibility(View.GONE);
		}

		tvNorm.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				if (jarr_norm.size() != 0)
				{
					switch (normListView.getVisibility())
						{
						case View.VISIBLE:
							normListView.setVisibility(View.GONE);
							break;
						case View.GONE:
							normListView.setVisibility(View.VISIBLE);
							break;
						}
				}

			}
		});
	}

	//初始化待审核版块listview
	private void initIspassView()
	{
		ispassListView = (ListView) this.findViewById(R.id.LVmyblo_ispass);
		if (jarr_ispass.size() != 0)
		{
			adapter_ispass = new MyBlockAdapter(jarr_ispass, false);
			ispassListView.setAdapter(adapter_ispass);
			setListViewHeight(ispassListView);
			ispassListView.setOnItemClickListener(new OnItemClickListener()
			{

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3)
				{
					Intent intent = new Intent(MyBlockActivity.this,
							BlockInfoActivity.class);
					if (jarr_ispass.getJSONObject(arg2).getString("blo_ispass")
							.equals("0"))
					{
						intent.putExtra("READTYPE", true);// 待审核期间不允许编辑
					}
					else
					{
						intent.putExtra("READTYPE", false);
					}
					intent.putExtra("BLOCKID", jarr_ispass.getJSONObject(arg2)
							.getString("blo_id"));
					startActivity(intent);
				}
			});
		}

		tvIspass.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				if (jarr_ispass.size() != 0)
				{
					switch (ispassListView.getVisibility())
						{
						case View.VISIBLE:
							ispassListView.setVisibility(View.GONE);
							break;
						case View.GONE:
							ispassListView.setVisibility(View.VISIBLE);
							break;
						}
				}

			}
		});

	}

	//初始化禁用版块listview
	private void initForbView()
	{
		forbListView = (ListView) this.findViewById(R.id.LVmyblo_forb);
		if (jarr_forb.size() != 0)
		{
			adapter_forb = new MyBlockAdapter(jarr_forb, true);
			forbListView.setAdapter(adapter_forb);
			setListViewHeight(forbListView);
			forbListView.setOnItemClickListener(new OnItemClickListener()
			{

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3)
				{
					Intent intent = new Intent(MyBlockActivity.this,
							BlockInfoActivity.class);
					intent.putExtra("READTYPE", true);
					intent.putExtra("BLOCKID", jarr_forb.getJSONObject(arg2)
							.getString("blo_id"));
					startActivity(intent);
				}
			});
		}

		tvForb.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				if (jarr_forb.size() != 0)
				{
					switch (forbListView.getVisibility())
						{
						case View.VISIBLE:
							forbListView.setVisibility(View.GONE);
							break;
						case View.GONE:
							forbListView.setVisibility(View.VISIBLE);
							break;
						}
				}

			}
		});

	}

	private class MyBlockAdapter extends BaseAdapter
	{
		JSONArray jarr_block = null;
		boolean ispass = false;
		ViewHolder viewholder;

		public MyBlockAdapter(JSONArray jarr_block, boolean ispass)
		{
			this.jarr_block = jarr_block;
			this.ispass = ispass;
		}

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
		public View getView(int position, View convertView, ViewGroup parent)
		{
			JSONObject json_temp = jarr_block.getJSONObject(position);

			if (convertView == null)
			{
				convertView = getLayoutInflater().inflate(
						R.layout.item_my_block, null);
				viewholder = new ViewHolder();
				viewholder.blo_name = (TextView) convertView
						.findViewById(R.id.TVmyblo_name);
				viewholder.blo_topicCount = (TextView) convertView
						.findViewById(R.id.TVmyblo_topcount);
				viewholder.blo_date = (TextView) convertView
						.findViewById(R.id.TVmyblo_date);
				viewholder.blo_status = (TextView) convertView
						.findViewById(R.id.TVmyblo_status);

				convertView.setTag(viewholder);
			}
			else
			{
				viewholder = (ViewHolder) convertView.getTag();// 根据标签得到视图
			}
			// 0blo_id,1blo_name,2blo_intro,3blo_createdate,4blo_clickcount,5blo_topcount,6blo_isdelete,7blo_ispass,8blo_pic
			viewholder.blo_name.setText(json_temp.getString("blo_name"));
			viewholder.blo_topicCount.setText(json_temp
					.getString("blo_topcount"));
			viewholder.blo_date.setText(json_temp.getString("blo_createdate")
					.substring(0, 10));

			if (ispass)
			{
				if (json_temp.getString("blo_isdelete").equals("false"))
				{
					viewholder.blo_status.setTextColor(getResources().getColor(
							R.color.black));
					viewholder.blo_status.setText("活跃");
				}
				else
				{
					viewholder.blo_status.setTextColor(getResources().getColor(
							R.color.red));
					viewholder.blo_status.setText("封禁");
				}
			}
			else
			{
				if (json_temp.getString("blo_ispass").equals("0"))
				{
					viewholder.blo_status.setTextColor(getResources().getColor(
							R.color.black));
					viewholder.blo_status.setText("待审核");
				}
				else
				{
					viewholder.blo_status.setTextColor(getResources().getColor(
							R.color.red));
					viewholder.blo_status.setText("未通过");
				}
			}

			return convertView;
		}

		class ViewHolder
		{
			TextView blo_name, blo_topicCount, blo_date, blo_status;
		}

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		// TODO Auto-generated method stub
		switch (requestCode)
			{
			case 1:
				if (resultCode == 1)
				{
					if (data != null)
					{

						final String blockid = data.getStringExtra("BLOCKID");
						if (blockid != null)
						{
							new Thread()
							{

								@Override
								public void run()
								{
									try
									{
										String bloinfo = NetTransUtil
												.getBlockById(blockid);
										handler.obtainMessage(UPDATE_BLOCK,
												bloinfo).sendToTarget();
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
	}

	//根据获得的所有版块信息拆分数据，设置各个adapter的数据源
	public void setView(JSONArray jarr_block)
	{
		// 初始化
		jarr_norm = new JSONArray();
		jarr_ispass = new JSONArray();
		jarr_forb = new JSONArray();

		if (jarr_block.size() == 0)
		{
		}
		else
		{
			JSONObject json_temp;
			for (int i = 0; i < jarr_block.size(); i++)
			{
				json_temp = jarr_block.getJSONObject(i);
				if (json_temp.getString("blo_ispass").equals("0"))// 待审核
				{
					jarr_ispass.add(json_temp);
				}
				else if (json_temp.getString("blo_ispass").equals("1"))// 审核通过
				{
					if (json_temp.getString("blo_isdelete").equals("false"))
					{
						jarr_norm.add(json_temp);// 正常
					}
					else
					{
						jarr_forb.add(json_temp);// 禁用
					}
				}
				else
				{
					jarr_ispass.add(json_temp);// 审核未通过
				}
			}
		}

		initNormView();
		initIspassView();
		initForbView();

		ScrollView sc = (ScrollView) this.findViewById(R.id.SVmyblock);
		sc.smoothScrollTo(0, 0);// 将scrollview移至顶端

	}

	/**
	 * 动态设置ListView的高度,当scrollview嵌套listview(设为wapcontent)时,
	 * listview只显示一行，需动态设置高度
	 * 
	 * @param listView
	 */
	public void setListViewHeight(ListView listView)
	{
		if (listView == null)
			return;

		ListAdapter listAdapter = listView.getAdapter();
		if (listAdapter == null)
		{
			// pre-condition
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

	private class GetBlockThread extends Thread
	{

		@Override
		public void run()
		{
			try
			{
				String block = NetTransUtil
						.getNfBloinfoByUid(Constant.loginUid);
				handler.obtainMessage(GET_BLOCK, block).sendToTarget();
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
