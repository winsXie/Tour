package com.bn.message;

import java.lang.ref.WeakReference;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bn.thread.ImgAsyncDownload;
import com.bn.tour.R;
import com.bn.user.LookUserInfoActivity;
import com.bn.util.CheckUtil;
import com.bn.util.Constant;
import com.bn.util.NetTransUtil;

public class AcceptMessage extends Fragment
{
	private static final int GET_UNREADMESS = 0;//获得未读留言
	private static final int GET_HADREADMESS = 1;//获得已读留言
	private static final int MARK_MESS = 2;//标记为已读
	private static final int DEL_ACCEMESS = 3;//删除留言

	private TextView tvUnRead, tvHasRead;
	private ListView lvUnRead, lvHasRead;
	private MessAdapter unRead, hasRead;
	private JSONObject json_unread, json_read;
	private JSONArray jarr_unread, jarr_read;
	private ImgAsyncDownload imagedownload;//异步加载图片线程
	private View acceptView;
	private GetLeaMessThread getLeaMess;
	private GetReadMessThread getReadMess;
	private int tempPosition;
	private boolean isDelUnRead = false;//要删除的是否为未读留言

	private Handler handler;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		Log.d("ActivityStatus=====>", "AcceptMessage=====onCreate=====");
		handler = new MyHandler(this);
		super.onCreate(savedInstanceState);
	}

	private static class MyHandler extends Handler
	{
		WeakReference<AcceptMessage> mActivity;

		public MyHandler(AcceptMessage activity)
		{
			mActivity = new WeakReference<AcceptMessage>(activity);
		}

		@Override
		public void handleMessage(Message msg)
		{
			AcceptMessage currActivity = mActivity.get();
			switch (msg.what)
				{
				case GET_UNREADMESS:
					String unreadmess = (String) msg.obj;
					if (unreadmess == null)
					{
						currActivity.jarr_unread = new JSONArray();
					}
					else
					{
						unreadmess = CheckUtil.replaceBlank((String) msg.obj);
						currActivity.json_unread = JSONObject
								.fromObject(unreadmess);
						currActivity.jarr_unread = currActivity.json_unread
								.getJSONArray("results");
					}
					currActivity.initUnreadView();
					break;
				case GET_HADREADMESS:
					String readmess = (String) msg.obj;
					if (readmess == null)
					{
						currActivity.jarr_read = new JSONArray();
					}
					else
					{
						readmess = CheckUtil.replaceBlank((String) msg.obj);
						currActivity.json_read = JSONObject
								.fromObject(readmess);
						currActivity.jarr_read = currActivity.json_read
								.getJSONArray("results");
					}
					currActivity.initReadView();
					break;
				case MARK_MESS:
					if ((Boolean) msg.obj)
					{
						currActivity.jarr_read.add(0, currActivity.jarr_unread
								.getJSONObject(currActivity.tempPosition));
						currActivity.jarr_unread
								.remove(currActivity.tempPosition);

						currActivity.unRead.notifyDataSetChanged();
						currActivity.hasRead.notifyDataSetChanged();

						currActivity.setListViewHeight(currActivity.lvHasRead);
						// 记录为空listview不再显示
						if (currActivity.jarr_unread.size() == 0)
						{
							currActivity.lvUnRead.setVisibility(View.GONE);
						}
						else
						{
							currActivity
									.setListViewHeight(currActivity.lvUnRead);
						}
					}
					break;
				case DEL_ACCEMESS:
					if ((Boolean) msg.obj)
					{
						if (currActivity.isDelUnRead)
						{
							currActivity.jarr_unread
									.remove(currActivity.tempPosition);
							currActivity.unRead.notifyDataSetChanged();
							if (currActivity.jarr_unread.size() == 0)
							{
								currActivity.lvUnRead.setVisibility(View.GONE);
							}
							else
							{
								currActivity
										.setListViewHeight(currActivity.lvUnRead);
							}
						}
						else
						{
							currActivity.jarr_read
									.remove(currActivity.tempPosition);
							currActivity.hasRead.notifyDataSetChanged();
							currActivity
									.setListViewHeight(currActivity.lvHasRead);
						}
					}
					else
					{
						Toast.makeText(currActivity.getActivity(), "删除失败！",
								Toast.LENGTH_LONG).show();
					}
					break;
				case Constant.CONNECTIONTIMEOUT:
					Toast.makeText(currActivity.getActivity(), "连接超时！",
							Toast.LENGTH_LONG).show();
					break;
				case Constant.SOTIMEOUT:
					Toast.makeText(currActivity.getActivity(), "读取数据超时！",
							Toast.LENGTH_LONG).show();
					break;
				}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		Log.d("ActivityStatus=====>", "AcceptMessage=====onCreateView=====");
		acceptView = inflater
				.inflate(R.layout.my_mess_accept, container, false);
		tvUnRead = (TextView) acceptView.findViewById(R.id.TVmymess_unread);
		tvHasRead = (TextView) acceptView.findViewById(R.id.TVmymess_read);

		getLeaMess = new GetLeaMessThread();
		getReadMess = new GetReadMessThread();
		getLeaMess.start();
		getReadMess.start();

		return acceptView;
	}

	//初始化未读留言
	private void initUnreadView()
	{
		lvUnRead = (ListView) acceptView.findViewById(R.id.LVmymess_unread);

		// 没有未读留言时， 不设置adapter；listview隐藏
		if (jarr_unread.size() != 0)
		{
			unRead = new MessAdapter(jarr_unread, true);
			lvUnRead.setAdapter(unRead);
			setListViewHeight(lvUnRead);
		}
		else
		{
			lvUnRead.setVisibility(View.GONE);
		}

		tvUnRead.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				if (jarr_unread.size() != 0)
				{
					switch (lvUnRead.getVisibility())
						{
						case View.VISIBLE:
							lvUnRead.setVisibility(View.GONE);
							break;
						case View.GONE:
							lvUnRead.setVisibility(View.VISIBLE);
							break;
						}
				}

			}
		});

	}

	//初始化已读留言
	private void initReadView()
	{
		lvHasRead = (ListView) acceptView.findViewById(R.id.LVmymess_read);

		hasRead = new MessAdapter(jarr_read, false);
		lvHasRead.setAdapter(hasRead);
		setListViewHeight(lvHasRead);

		tvHasRead.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				switch (lvHasRead.getVisibility())
					{
					case View.VISIBLE:
						lvHasRead.setVisibility(View.GONE);
						break;
					case View.GONE:
						lvHasRead.setVisibility(View.VISIBLE);
						break;
					}
			}
		});
	}

	private class MessAdapter extends BaseAdapter
	{
		ViewHolder viewholder;
		JSONArray jarr_mess = null;

		boolean show = true;

		public MessAdapter(JSONArray jarr_mess, boolean show)
		{
			this.jarr_mess = jarr_mess;
			this.show = show;
		}

		@Override
		public int getCount()
		{
			return jarr_mess.size();
		}

		@Override
		public Object getItem(int position)
		{
			return jarr_mess.getJSONObject(position);
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
			final JSONObject json_temp = jarr_mess.getJSONObject(position);
			if (convertView == null)
			{
				viewholder = new ViewHolder();
				convertView = getActivity().getLayoutInflater().inflate(
						R.layout.item_mymess_accept, null);
				viewholder.imvHeadimg = (ImageView) convertView
						.findViewById(R.id.IMVmymess_acce_headimg);
				viewholder.tvName = (TextView) convertView
						.findViewById(R.id.TVmymess_acce_uname);
				viewholder.tvDate = (TextView) convertView
						.findViewById(R.id.TVmymess_acce_date);
				viewholder.tvContent = (TextView) convertView
						.findViewById(R.id.TVmess_acce_content);
				viewholder.tvMark = (TextView) convertView
						.findViewById(R.id.TVmymess_acce_markread);
				viewholder.imvReply = (ImageView) convertView
						.findViewById(R.id.IMVmymess_acce_reply);
				viewholder.imvDel = (ImageView) convertView
						.findViewById(R.id.IMVmymess_acce_del);
				viewholder.rlMark = (RelativeLayout) convertView
						.findViewById(R.id.RLmymess_markmess);

				convertView.setTag(viewholder);
			}
			else
			{
				viewholder = (ViewHolder) convertView.getTag();
			}
			viewholder.imvHeadimg.setTag(json_temp.getString("user_image"));
			if (imagedownload == null)
			{
				imagedownload = ImgAsyncDownload.getInstance();
			}
			imagedownload.imageDownload(viewholder.imvHeadimg);

			if (show)
			{
				viewholder.rlMark.setVisibility(View.VISIBLE);
			}
			else
			{
				viewholder.rlMark.setVisibility(View.GONE);
			}

			viewholder.tvName.setText(json_temp.getString("user_name"));
			viewholder.tvDate.setText(json_temp.getString("mess_date")
					.substring(0, 16));
			viewholder.tvContent.setText(json_temp.getString("mess_content"));

			viewholder.imvHeadimg.setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View v)
				{
					Intent intent = new Intent(getActivity(),
							LookUserInfoActivity.class);
					intent.putExtra("USERID",
							json_temp.getString("mess_userid"));
					startActivity(intent);
				}
			});

			viewholder.tvMark.setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View v)
				{
					tempPosition = position;
					new Thread()
					{

						@Override
						public void run()
						{
							try
							{
								boolean flag = NetTransUtil
										.markLeaveMess(json_temp
												.getString("mess_id"));
								handler.obtainMessage(MARK_MESS, flag)
										.sendToTarget();
							}
							catch (Exception e)
							{
								e.printStackTrace();
							}
						}

					}.start();
				}
			});

			viewholder.imvReply.setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View v)
				{
					Intent intent = new Intent(getActivity(),
							LeaveMessageActivity.class);
					intent.putExtra("ACCEPTUID",
							json_temp.getString("mess_userid"));
					startActivity(intent);
				}
			});

			viewholder.imvDel.setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View v)
				{
					tempPosition = position;

					new Thread()
					{

						@Override
						public void run()
						{
							try
							{
								if (show)// 从未读留言中删除
								{
									isDelUnRead = true;
									boolean flag = NetTransUtil
											.delAcceMess(jarr_unread
													.getJSONObject(position)
													.getString("mess_id"));
									handler.obtainMessage(DEL_ACCEMESS, flag)
											.sendToTarget();
								}
								else
								{
									isDelUnRead = false;
									boolean flag = NetTransUtil
											.delAcceMess(jarr_read
													.getJSONObject(position)
													.getString("mess_id"));
									handler.obtainMessage(DEL_ACCEMESS, flag)
											.sendToTarget();
								}

							}
							catch (Exception e)
							{
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
			TextView tvName, tvDate, tvContent, tvMark;
			ImageView imvHeadimg, imvReply, imvDel;
			RelativeLayout rlMark;
		}

	}

	/*
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

	//获取未读留言
	private class GetLeaMessThread extends Thread
	{

		@Override
		public void run()
		{
			try
			{
				String leamess = NetTransUtil.checkLeaMess(Constant.loginUid);
				handler.obtainMessage(GET_UNREADMESS, leamess).sendToTarget();
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

	private class GetReadMessThread extends Thread
	{

		@Override
		public void run()
		{
			try
			{
				String readmess = NetTransUtil.checkReadMess(Constant.loginUid);
				handler.obtainMessage(GET_HADREADMESS, readmess).sendToTarget();
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

	@Override
	public void onSaveInstanceState(Bundle outState)
	{
	}

}
