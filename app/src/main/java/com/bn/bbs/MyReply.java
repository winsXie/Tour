package com.bn.bbs;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import android.app.ActionBar.LayoutParams;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.view.Gravity;
import android.view.LayoutInflater;
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
import android.widget.TextView;
import android.widget.Toast;

import com.bn.thread.ImgAsyncDownload;
import com.bn.tour.R;
import com.bn.tour.ScanPicActivity;
import com.bn.util.CheckUtil;
import com.bn.util.Constant;
import com.bn.util.GetEmotion;
import com.bn.util.NetTransUtil;
import com.bn.util.TimeChange;

public class MyReply extends Fragment
{
	private static final int LOAD_PAGE = 2;//分批加载
	private static final int ITEMCOUTN = 5;//每次加载的条目数
	private static final int GET_TOPICINFO = 3;//获取所在主题帖的信息
	private ListView listview;
	private Intent intent;
	private MyReplyAdapter adapter;
	private GetMyReplyThread getmyreply;
	private View replyview;//回复帖视图
	private JSONObject json_reply;
	private JSONArray jarr_reply;
	private ImgAsyncDownload imgdownload;//异步加载图片线程
	private int delPosition;//记录删除 item的位置
	private int floor;//回复帖所在楼层
	private View footer;//listview的footer

	private LinearLayout footerLayout;
	private ScrollListener scrolllistener;
	private boolean loading = false;// 正在加载标志位
	private boolean hadTotalLoaded = false;// 加载完成标志位

	private Handler handler;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		handler = new MyHandler(this);
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		replyview = inflater.inflate(R.layout.my_reply, container, false);
		getmyreply = new GetMyReplyThread();
		getmyreply.start();
		return replyview;
	}

	private static class MyHandler extends Handler
	{
		WeakReference<MyReply> mActivity;

		public MyHandler(MyReply activity)
		{
			mActivity = new WeakReference<MyReply>(activity);
		}

		@Override
		public void handleMessage(Message msg)
		{
			MyReply currActivity = mActivity.get();
			switch (msg.what)
				{
				case 0:
					String json = (String) msg.obj;
					if (json == null)
					{
						currActivity.jarr_reply = new JSONArray();
					}
					else
					{
						json = CheckUtil.replaceBlank(json);
						currActivity.json_reply = JSONObject.fromObject(json);
						currActivity.jarr_reply = currActivity.json_reply
								.getJSONArray("results");
						if (currActivity.jarr_reply.size() < ITEMCOUTN)
						{
							currActivity.hadTotalLoaded = true;
						}
					}
					currActivity.initView();
					break;
				case 1:
					if ((Boolean) msg.obj)
					{
						currActivity.jarr_reply
								.remove(currActivity.delPosition);
						currActivity.adapter.notifyDataSetChanged();
						Toast.makeText(currActivity.getActivity(), "删除成功！",
								Toast.LENGTH_LONG).show();
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
						get_json = CheckUtil.replaceBlank(get_json);
						JSONObject json_new = JSONObject.fromObject(get_json);
						JSONArray jarr_new = json_new.getJSONArray("results");
						if (jarr_new.size() < ITEMCOUTN)
						{
							currActivity.hadTotalLoaded = true;// 取得的数据小于一页
																// 的条数，全部加载完成
						}
						String json1 = currActivity.jarr_reply.toString();
						String json2 = jarr_new.toString();
						// 组装新的json,不能使用add方法，只能add一个元素
						String newjson = json1.substring(0, json1.length() - 1)
								+ "," + json2.substring(1, json2.length());
						currActivity.jarr_reply = JSONArray.fromObject(newjson);
						currActivity.adapter.notifyDataSetChanged();
					}

					if (currActivity.hadTotalLoaded)
					{
						TextView tv = new TextView(currActivity.getActivity());
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
				case GET_TOPICINFO:
					String mess = (String) msg.obj;
					if (mess != null)
					{
						mess = CheckUtil.replaceBlank(mess);
						JSONObject temp = JSONObject.fromObject(mess);
						Constant.currTopicInfo = temp;// 设置ReplyActivity的Header内容
						currActivity.intent = new Intent();
						currActivity.intent
								.setClass(currActivity.getActivity(),
										ReplyActivity.class);
						currActivity.intent.putExtra("BLONAME",
								temp.getString("blo_name"));
						currActivity.intent.putExtra("POSITION",
								currActivity.floor);
						currActivity.startActivity(currActivity.intent);

					}
					else
					{
						Toast.makeText(currActivity.getActivity(), "获取信息失败！",
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

	//初始化listview
	private void initView()
	{
		listview = (ListView) replyview.findViewById(R.id.LVmyreply);

		footer = getActivity().getLayoutInflater().inflate(R.layout.footer,
				null);
		footerLayout = (LinearLayout) footer.findViewById(R.id.LLfooter);

		adapter = new MyReplyAdapter();
		listview.setAdapter(adapter);

		// 没有数据时设加载完成标志位，防止加载出footer
		if (jarr_reply.size() == 0)
		{
			hadTotalLoaded = true;
		}
		// 滑动监听（分批加载）
		scrolllistener = new ScrollListener();
		listview.setOnScrollListener(scrolllistener);

		listview.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3)
			{
				// TODO Auto-generated method stub
				Constant.currTopicId = jarr_reply.getJSONObject(arg2)
						.getString("rep_topicid");// 设置ReplyActivity所在的主题帖

				floor = Integer.parseInt(jarr_reply.getJSONObject(arg2)
						.getString("rep_floor"));
				new Thread()
				{

					@Override
					public void run()
					{
						try
						{
							String topicinfo = NetTransUtil
									.getTopicById(Constant.currTopicId);
							handler.obtainMessage(GET_TOPICINFO, topicinfo)
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
	}

	private class MyReplyAdapter extends BaseAdapter
	{
		private ViewHolder viewholder;

		@Override
		public int getCount()
		{
			return jarr_reply.size();
		}

		@Override
		public Object getItem(int position)
		{
			return jarr_reply.getJSONObject(position);
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
			final JSONObject json_temp = jarr_reply.getJSONObject(position);

			// 重用View
			if (convertView == null)
			{
				convertView = getActivity().getLayoutInflater().inflate(
						R.layout.item_my_reply, null);
				viewholder = new ViewHolder();

				viewholder.imvPic1 = (ImageView) convertView
						.findViewById(R.id.IMVmy_reply_pic1);
				viewholder.imvPic2 = (ImageView) convertView
						.findViewById(R.id.IMVmy_reply_pic2);
				viewholder.imvPic3 = (ImageView) convertView
						.findViewById(R.id.IMVmy_reply_pic3);
				viewholder.imvHead = (ImageView) convertView
						.findViewById(R.id.IMVmy_reply_head);
				viewholder.imvDel = (ImageView) convertView
						.findViewById(R.id.IMVmy_reply_del);
				viewholder.tvUname = (TextView) convertView
						.findViewById(R.id.TVmy_reply_uname);
				viewholder.tvSendTime = (TextView) convertView
						.findViewById(R.id.TVmy_reply_time);

				viewholder.tvTitle = (TextView) convertView
						.findViewById(R.id.TVmy_reply_toptitle);
				viewholder.tvContent = (TextView) convertView
						.findViewById(R.id.TVmy_reply_content);
				viewholder.tvReplycount = (TextView) convertView
						.findViewById(R.id.TVmy_reply_repcou);

				convertView.setTag(viewholder);// 设置标签
			}
			else
			{
				viewholder = (ViewHolder) convertView.getTag();// 根据标签得到视图
			}

			viewholder.tvUname.setText(Constant.currUserInfo
					.getString("user_name"));
			viewholder.tvSendTime.setText(TimeChange.changeTime(json_temp
					.getString("rep_time")));
			// 回复帖内容设置表情
			SpannableString spannable = GetEmotion.getEmotion(getActivity(),
					json_temp.getString("rep_content"));
			viewholder.tvContent.setText(spannable);

			// 原帖标题，标题为空设置为主题帖内容
			if (json_temp.getString("top_title").equals("null"))
			{
				spannable = GetEmotion.getEmotion(getActivity(), json_temp
						.getString("top_content").toString());
				viewholder.tvTitle.setText(spannable);
			}
			else
			{
				viewholder.tvTitle.setText(json_temp.getString("top_title"));
			}

			viewholder.tvReplycount.setText(json_temp
					.getString("top_replycount"));

			imgdownload = ImgAsyncDownload.getInstance();

			if (imgdownload != null)
			{
				viewholder.imvHead.setTag(Constant.currUserInfo
						.getString("user_image"));
				viewholder.imvHead.setImageResource(R.drawable.defaultimage);// 先显示预设图片,防止在加载完成之前显示出其他的图片(view重用导致)

				imgdownload.imageDownload(viewholder.imvHead);

				final String picPath = json_temp.getString("rep_pic");// 主题帖图片
				if (picPath.equals("null"))
				{
					viewholder.imvPic1.setVisibility(View.GONE);
					viewholder.imvPic2.setVisibility(View.GONE);
					viewholder.imvPic3.setVisibility(View.GONE);
				}
				else
				{
					String[] pathArray = { "null", "null", "null" };
					String[] temp = picPath.split(",");
					for (int i = 0; i < temp.length; i++)
					{
						pathArray[i] = temp[i];
					}

					if (!pathArray[0].equals("null"))// 加载第一张主题帖图片
					{
						viewholder.imvPic1.setTag(pathArray[0]);
						viewholder.imvPic1.setVisibility(View.VISIBLE);// 将ImageView设为可见
						viewholder.imvPic1
								.setImageResource(R.drawable.default_picture);// 先显示预设图片,防止在加载完成之前显示出其他的图片(view重用导致)

						imgdownload.imageDownload(viewholder.imvPic1);

						viewholder.imvPic1
								.setOnClickListener(new OnClickListener()
								{
									@Override
									public void onClick(View v)
									{
										scanPic(picPath, 0);
									}
								});
					}
					else
					{
						viewholder.imvPic1.setVisibility(View.GONE);
					}
					if (!pathArray[1].equals("null"))// 加载第二张主题帖图片
					{
						viewholder.imvPic2.setTag(pathArray[1]);
						viewholder.imvPic2.setVisibility(View.VISIBLE);
						viewholder.imvPic2
								.setImageResource(R.drawable.default_picture);
						imgdownload.imageDownload(viewholder.imvPic2);

						viewholder.imvPic2
								.setOnClickListener(new OnClickListener()
								{
									@Override
									public void onClick(View v)
									{
										scanPic(picPath, 1);
									}
								});
					}
					else
					{
						viewholder.imvPic2.setVisibility(View.GONE);
					}
					if (!pathArray[2].equals("null"))// 加载第三张主题帖图片
					{
						viewholder.imvPic3.setTag(pathArray[2]);
						viewholder.imvPic3.setVisibility(View.VISIBLE);
						viewholder.imvPic3
								.setImageResource(R.drawable.default_picture);
						imgdownload.imageDownload(viewholder.imvPic3);

						viewholder.imvPic3
								.setOnClickListener(new OnClickListener()
								{
									@Override
									public void onClick(View v)
									{
										scanPic(picPath, 2);
									}
								});
					}
					else
					{
						viewholder.imvPic3.setVisibility(View.GONE);
					}

				}

			}

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
										.delRepTopic(json_temp
												.getString("rep_id"));
								handler.obtainMessage(1, flag).sendToTarget();
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

		private class ViewHolder
		{
			ImageView imvHead, imvPic1, imvPic2, imvPic3, imvDel;
			TextView tvUname, tvSendTime, tvTitle, tvContent, tvReplycount;
		}

	}

	//浏览图片
	private void scanPic(String path, int position)
	{
		String[] picpath = path.split(",");
		ArrayList<String> picpathList = new ArrayList<String>();

		for (int i = 0; i < picpath.length; i++)
		{
			picpathList.add(picpath[i]);
		}
		intent = new Intent(getActivity(), ScanPicActivity.class);
		intent.putExtra("POSITION", position);
		intent.putStringArrayListExtra("PATHLIST", picpathList);
		startActivity(intent);
	}

	//滑动监听
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
							String json_new_topic = NetTransUtil.getRepByUid(
									Constant.loginUid,
									"false",
									jarr_reply.getJSONObject(
											jarr_reply.size() - 1).getString(
											"rep_id"));
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

	//获取回复帖线程
	private class GetMyReplyThread extends Thread
	{

		@Override
		public void run()
		{
			try
			{
				String reply = NetTransUtil.getRepByUid(Constant.loginUid,
						"false", "R00000000");
				if (reply != null)
				{
					handler.obtainMessage(0, reply).sendToTarget();
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
