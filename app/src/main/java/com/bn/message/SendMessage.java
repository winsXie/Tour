package com.bn.message;

import java.lang.ref.WeakReference;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bn.thread.ImgAsyncDownload;
import com.bn.tour.R;
import com.bn.user.LookUserInfoActivity;
import com.bn.util.CheckUtil;
import com.bn.util.Constant;
import com.bn.util.NetTransUtil;

public class SendMessage extends Fragment
{
	private static final int GET_SENDMESS = 0;//获取发出的留言（handler的what值）
	private static final int DEL_SENDMESS = 1;//删除留言
	private ListView lvsend;
	private JSONObject json_mess;
	private JSONArray jarr_mess;
	private View messview;
	private GetSendMessThread getSendMess;
	private ImgAsyncDownload imgdownload;//异步加载图片线程
	private int delPosition;//删除留言所在位置
	private SendMessAdapter adapter;

	private Handler handler;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		handler = new MyHandler(this);
		super.onCreate(savedInstanceState);
	}

	private static class MyHandler extends Handler
	{
		WeakReference<SendMessage> mActivity;

		public MyHandler(SendMessage activity)
		{
			mActivity = new WeakReference<SendMessage>(activity);
		}

		@Override
		public void handleMessage(Message msg)
		{
			SendMessage currActivity = mActivity.get();
			switch (msg.what)
				{
				case GET_SENDMESS:
					String mess = (String) msg.obj;
					if (mess == null)
					{
						currActivity.jarr_mess = new JSONArray();
					}
					else
					{
						mess = CheckUtil.replaceBlank((String) msg.obj);
						currActivity.json_mess = JSONObject.fromObject(mess);
						currActivity.jarr_mess = currActivity.json_mess
								.getJSONArray("results");
					}
					currActivity.initListView();
					break;
				case DEL_SENDMESS:
					if ((Boolean) msg.obj)
					{
						currActivity.jarr_mess.remove(currActivity.delPosition);
						currActivity.adapter.notifyDataSetChanged();
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
		messview = inflater.inflate(R.layout.my_mess_send, container, false);

		getSendMess = new GetSendMessThread();
		getSendMess.start();
		return messview;
	}

	//初始化列表
	private void initListView()
	{
		lvsend = (ListView) messview.findViewById(R.id.LVmymess_send);

		adapter = new SendMessAdapter();
		lvsend.setAdapter(adapter);
	}

	private class SendMessAdapter extends BaseAdapter
	{
		ViewHolder viewholder;

		@Override
		public int getCount()
		{
			return jarr_mess.size();
		}

		@Override
		public Object getItem(int position)
		{
			// TODO Auto-generated method stub
			return jarr_mess.getJSONObject(position);
		}

		@Override
		public long getItemId(int position)
		{
			// TODO Auto-generated method stub
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
						R.layout.item_mymess_send, null);
				viewholder.tvAcceUname = (TextView) convertView
						.findViewById(R.id.TVmymess_send_uname);
				viewholder.tvContent = (TextView) convertView
						.findViewById(R.id.TVmymess_send_content);
				viewholder.tvDate = (TextView) convertView
						.findViewById(R.id.TVmymess_send_date);
				viewholder.imvDel = (ImageView) convertView
						.findViewById(R.id.IMVmymess_send_del);
				viewholder.imvHeadimg = (ImageView) convertView
						.findViewById(R.id.IMVmymess_send_headimg);

				convertView.setTag(viewholder);
			}
			else
			{
				viewholder = (ViewHolder) convertView.getTag();
			}

			viewholder.tvAcceUname.setText(json_temp.getString("user_name"));
			viewholder.tvContent.setText(json_temp.getString("mess_content"));
			viewholder.tvDate.setText(json_temp.getString("mess_date")
					.substring(0, 16));

			viewholder.imvHeadimg.setTag(json_temp.getString("user_image"));
			if (imgdownload == null)
			{
				imgdownload = ImgAsyncDownload.getInstance();
			}

			imgdownload.imageDownload(viewholder.imvHeadimg);

			viewholder.imvHeadimg.setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View v)
				{
					Intent intent = new Intent(getActivity(),
							LookUserInfoActivity.class);
					intent.putExtra("USERID",
							json_temp.getString("mess_acceptuid"));
					startActivity(intent);
				}
			});

			viewholder.imvDel.setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View v)
				{
					delPosition = position;
					new Thread()
					{

						@Override
						public void run()
						{
							try
							{
								boolean flag = NetTransUtil
										.delSendMess(json_temp
												.getString("mess_id"));
								handler.obtainMessage(DEL_SENDMESS, flag)
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

			return convertView;
		}

		class ViewHolder
		{
			TextView tvAcceUname, tvContent, tvDate;
			ImageView imvHeadimg, imvDel;
		}

	}

	private class GetSendMessThread extends Thread
	{

		@Override
		public void run()
		{
			try
			{
				String leamess = NetTransUtil.getSendMess(Constant.loginUid);
				handler.obtainMessage(GET_SENDMESS, leamess).sendToTarget();
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
		// TODO Auto-generated method stub
		// super.onSaveInstanceState(outState);
	}
}
