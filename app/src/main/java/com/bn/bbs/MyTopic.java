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

public class MyTopic extends Fragment
{
	private static final int LOAD_PAGE = 2;//��������
	private static final int ITEMCOUTN = 5;//ÿ�μ��ص���Ŀ��
	private ListView listview;
	private Intent intent;
	private MyTopicAdapter adapter;
	private GetMyTopicThread getmytopic;//��ȡ�������߳�
	private JSONObject json_topic;
	private JSONArray jarr_topic;//�洢��������Ϣ��JSONArray
	private ImgAsyncDownload imgdownload;//�첽����ͼƬ�߳�
	private int delPosition;//��¼ɾ�� item��λ��
	private View topicview;
	private View footer;//listview��footer

	private LinearLayout footerLayout;
	private ScrollListener scrolllistener;
	private boolean loading = false;// ���ڼ��ر�־λ
	private boolean hadTotalLoaded = false;// ������ɱ�־λ

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
		topicview = inflater.inflate(R.layout.my_topic, container, false);

		getmytopic = new GetMyTopicThread();
		getmytopic.start();
		return topicview;
	}

	private static class MyHandler extends Handler
	{
		WeakReference<MyTopic> mActivity;

		public MyHandler(MyTopic activity)
		{
			mActivity = new WeakReference<MyTopic>(activity);
		}

		@Override
		public void handleMessage(Message msg)
		{
			MyTopic currActivity = mActivity.get();
			switch (msg.what)
				{
				case 0:
					if ((String) msg.obj == null)
					{
						currActivity.jarr_topic = new JSONArray();
					}
					else
					{
						currActivity.json_topic = JSONObject
								.fromObject(CheckUtil
										.replaceBlank((String) msg.obj));
						currActivity.jarr_topic = currActivity.json_topic
								.getJSONArray("results");
						if (currActivity.jarr_topic.size() < ITEMCOUTN)
						{
							currActivity.hadTotalLoaded = true;
						}
					}
					currActivity.initView();
					break;
				case 1:
					if ((Boolean) msg.obj)
					{
						currActivity.jarr_topic
								.remove(currActivity.delPosition);
						currActivity.adapter.notifyDataSetChanged();
						Toast.makeText(currActivity.getActivity(), "ɾ���ɹ���",
								Toast.LENGTH_LONG).show();
					}
					break;
				case LOAD_PAGE:
					String get_json = (String) msg.obj;
					// �������Ϊ�գ�����ȫ��������ɣ����־λ
					if (get_json == null)
					{
						currActivity.hadTotalLoaded = true;
					}
					else
					{
						get_json = get_json.replace("\n", "   ");// �滻�س����ţ�������json����
						JSONObject json_new = JSONObject.fromObject(get_json);
						JSONArray jarr_new = json_new.getJSONArray("results");
						if (jarr_new.size() < ITEMCOUTN)
						{
							currActivity.hadTotalLoaded = true;// ȡ�õ�����С��һҳ
																// ��������ȫ���������
						}
						String json1 = currActivity.jarr_topic.toString();
						String json2 = jarr_new.toString();
						// ��װ�µ�json,����ʹ��add������ֻ��addһ��Ԫ��
						String newjson = json1.substring(0, json1.length() - 1)
								+ "," + json2.substring(1, json2.length());
						currActivity.jarr_topic = JSONArray.fromObject(newjson);
						currActivity.adapter.notifyDataSetChanged();
					}

					if (currActivity.hadTotalLoaded)
					{
						TextView tv = new TextView(currActivity.getActivity());
						tv.setText("��ȫ�����أ�");
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
					Toast.makeText(currActivity.getActivity(), "���ӳ�ʱ��",
							Toast.LENGTH_LONG).show();
					break;
				case Constant.SOTIMEOUT:
					Toast.makeText(currActivity.getActivity(), "��ȡ���ݳ�ʱ��",
							Toast.LENGTH_LONG).show();
					break;
				}
		}
	}

	//��ʼ��listview
	private void initView()
	{
		listview = (ListView) topicview.findViewById(R.id.LVmytopic);

		footer = getActivity().getLayoutInflater().inflate(R.layout.footer,
				null);
		footerLayout = (LinearLayout) footer.findViewById(R.id.LLfooter);

		adapter = new MyTopicAdapter();
		listview.setAdapter(adapter);

		// û������ʱ�������ɱ�־λ����ֹ���س�footer
		if (jarr_topic.size() == 0)
		{
			hadTotalLoaded = true;
		}
		// �����������������أ�
		scrolllistener = new ScrollListener();
		listview.setOnScrollListener(scrolllistener);

		listview.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3)
			{
				// TODO Auto-generated method stub
				Constant.currTopicId = jarr_topic.getJSONObject(arg2)
						.getString("top_id");// ����ReplyActivity���ڵ�������

				JSONObject tempinfo = jarr_topic.getJSONObject(arg2);
				tempinfo.put("top_userid", Constant.loginUid);
				tempinfo.put("user_name",
						Constant.currUserInfo.get("user_name"));
				tempinfo.put("user_sex", Constant.currUserInfo.get("user_sex"));
				tempinfo.put("user_age", Constant.currUserInfo.get("user_age"));
				tempinfo.put("user_image",
						Constant.currUserInfo.get("user_image"));

				Constant.currTopicInfo = tempinfo;// ����ReplyActivity��Header����

				intent = new Intent();
				intent.setClass(getActivity(), ReplyActivity.class);
				intent.putExtra("BLONAME", jarr_topic.getJSONObject(arg2)
						.getString("blo_name"));
				startActivity(intent);
			}
		});
	}

	//listview������
	private class MyTopicAdapter extends BaseAdapter
	{
		private ViewHolder viewholder;

		@Override
		public int getCount()
		{
			return jarr_topic.size();
		}

		@Override
		public Object getItem(int position)
		{
			return jarr_topic.getJSONObject(position);
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
			final JSONObject json_temp = jarr_topic.getJSONObject(position);

			// ����View
			if (convertView == null)
			{
				convertView = getActivity().getLayoutInflater().inflate(
						R.layout.item_my_topic, null);
				viewholder = new ViewHolder();

				viewholder.imvPic1 = (ImageView) convertView
						.findViewById(R.id.IMVmytopic_pic1);
				viewholder.imvPic2 = (ImageView) convertView
						.findViewById(R.id.IMVmytopic_pic2);
				viewholder.imvPic3 = (ImageView) convertView
						.findViewById(R.id.IMVmytopic_pic3);
				viewholder.imvHead = (ImageView) convertView
						.findViewById(R.id.IMVmy_topic_head);
				viewholder.imvDel = (ImageView) convertView
						.findViewById(R.id.IMVmy_topic_del);
				viewholder.tvUname = (TextView) convertView
						.findViewById(R.id.TVmy_topic_uname);
				viewholder.tvSendTime = (TextView) convertView
						.findViewById(R.id.TVmy_topic_time);

				viewholder.tvTitle = (TextView) convertView
						.findViewById(R.id.TVmytopic_title);
				viewholder.tvContent = (TextView) convertView
						.findViewById(R.id.TVmytopic_content);
				viewholder.tvReplycount = (TextView) convertView
						.findViewById(R.id.TVmytopic_repcou);
				viewholder.tvBloname = (TextView) convertView
						.findViewById(R.id.TVmytopic_bloname);

				convertView.setTag(viewholder);// ���ñ�ǩ
			}
			else
			{
				viewholder = (ViewHolder) convertView.getTag();// ���ݱ�ǩ�õ���ͼ
			}

			viewholder.tvUname.setText(Constant.currUserInfo
					.getString("user_name"));
			viewholder.tvSendTime.setText(TimeChange.changeTime(json_temp
					.getString("top_sendtime")));
			if (json_temp.getString("top_title").equals("null"))
			{
				viewholder.tvTitle.setVisibility(View.GONE);
			}
			else
			{
				viewholder.tvTitle.setVisibility(View.VISIBLE);
				viewholder.tvTitle.setText(json_temp.getString("top_title"));
			}
			// �������������ñ���
			SpannableString spannable = GetEmotion.getEmotion(getActivity(),
					json_temp.getString("top_content"));
			viewholder.tvContent.setText(spannable);

			viewholder.tvBloname.setText(json_temp.getString("blo_name"));
			viewholder.tvReplycount.setText(json_temp
					.getString("top_replycount"));

			imgdownload = ImgAsyncDownload.getInstance();

			if (imgdownload != null)
			{
				viewholder.imvHead.setTag(Constant.currUserInfo
						.getString("user_image"));
				viewholder.imvHead.setImageResource(R.drawable.defaultimage);// ����ʾԤ��ͼƬ,��ֹ�ڼ������֮ǰ��ʾ��������ͼƬ(view���õ���)

				imgdownload.imageDownload(viewholder.imvHead);

				final String picPath = json_temp.getString("top_pic");// ������ͼƬ
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

					if (!pathArray[0].equals("null"))// ���ص�һ��������ͼƬ
					{
						viewholder.imvPic1.setTag(pathArray[0]);
						viewholder.imvPic1.setVisibility(View.VISIBLE);// ��ImageView��Ϊ�ɼ�
						viewholder.imvPic1
								.setImageResource(R.drawable.default_picture);// ����ʾԤ��ͼƬ,��ֹ�ڼ������֮ǰ��ʾ��������ͼƬ(view���õ���)

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
					if (!pathArray[1].equals("null"))// ���صڶ���������ͼƬ
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
					if (!pathArray[2].equals("null"))// ���ص�����������ͼƬ
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
								boolean flag = NetTransUtil.delTopic(json_temp
										.getString("top_id"));
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
			TextView tvUname, tvSendTime, tvTitle, tvContent, tvBloname,
					tvReplycount;
		}

	}

	/**
	 * ���ͼƬ
	 * 
	 * @param path
	 *            ���ӵ�ͼƬ·��
	 * @param position
	 *            �ڼ���ͼƬ
	 */
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

	//��ȡ�������߳�
	private class GetMyTopicThread extends Thread
	{

		@Override
		public void run()
		{
			try
			{
				String topic = NetTransUtil.getTopicByUid(Constant.loginUid,
						"false", 0 + "");
				if (topic != null)
				{
					handler.obtainMessage(0, topic).sendToTarget();
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

	private class ScrollListener implements OnScrollListener
	{

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount)
		{
			int lastposi = listview.getLastVisiblePosition();

			/*
			 * totalItemCount��position������header��footer
			 * ֻ���ڻ��������һ��item��������û��ȫ��������ɣ��Լ�û�����ڼ��ص�����²ż���������һ��
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
							String json_new_topic = NetTransUtil.getTopicByUid(
									Constant.loginUid,
									"false",
									jarr_topic.getJSONObject(
											jarr_topic.size() - 1).getString(
											"top_id"));
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

	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		// TODO Auto-generated method stub
		// super.onSaveInstanceState(outState);
	}
}