package com.bn.tour;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.SpannableString;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bn.bbs.ReplyActivity;
import com.bn.thread.DownloadPicThread;
import com.bn.thread.ImgAsyncDownload;
import com.bn.user.LoginActivity;
import com.bn.user.LookUserInfoActivity;
import com.bn.util.CheckNetworkStatus;
import com.bn.util.CheckUtil;
import com.bn.util.Constant;
import com.bn.util.GetEmotion;
import com.bn.util.NetTransUtil;
import com.bn.util.TimeChange;

public class MainActivity extends Activity
{
	private static final int AUTOSLIP = 0;//������Զ�������whatֵ��
	private static final int GETPUSH = 1;//��ȡ�Ƽ��Ĺ��
	private static final int GETRECOTOP = 2;//��ȡ�Ƽ���������
	private static final int ADD_ZAN = 3;//Ϊ����������
	//��ΪҪ��viewpager��ѭ��չʾͼƬ����Map���洢����veiw����ӵ�viewpager�е�λ��
	private Map<String, Integer> viewmap = new HashMap<String, Integer>();
	private int currposi = 0;//viewpager��ǰ��ʾ��λ��

	private JSONObject json_push, json_recotopic;//�洢���ݵ�JSONObject����
	private JSONArray jarr_push, jarr_recotopic;//ͨ��JSONObject�õ���JSONArray
	private ViewPager mViewPager;//չʾ�������viewpager
	private MyPagerAdapter pagerAdapter;
	private ListView listview;//��ʾ�Ƽ������listview
	private ImgAsyncDownload imgdownload;//�첽����ͼƬ�߳�
	private RecoTopicAdapter adapter;
	private ArrayList<ImageView> imageViewlist;//���ڴ洢���ͼƬ��list
	private GetPushThread pushThread;
	private GetRecoTopThread recoTopThread;
	private LinearLayout pointGroup;//������ײ�ԭ�㲼��
	private Intent intent;
	private int zanPosition;//������������λ��
	private long exitTime = 0;//��ʼ����һ�ΰ����ؼ���ʱ��
	private RelativeLayout rlloading, rlloadstauts;
	private ImageView imvTip;
	private TextView tvtip;
	private Clicklistener clicklistener;
	private Handler handler;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		handler = new MyHandler(this);

		rlloading = (RelativeLayout) this.findViewById(R.id.RLmain_load);
		rlloading.setVisibility(View.VISIBLE);
		clicklistener = new Clicklistener();

		// ���SD��
		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState()))
		{
			Constant.hasSDcard = true;
			Constant.sdRootPath = Environment.getExternalStorageDirectory()
					.getPath();
		}

		mViewPager = (ViewPager) this.findViewById(R.id.viewpager);
		pointGroup = (LinearLayout) this.findViewById(R.id.show_pointer);

		pushThread = new GetPushThread();
		recoTopThread = new GetRecoTopThread();
		pushThread.start();
		recoTopThread.start();

	}

	private static class MyHandler extends Handler
	{
		WeakReference<MainActivity> mActivity;

		public MyHandler(MainActivity activity)
		{
			mActivity = new WeakReference<MainActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg)
		{
			MainActivity currActivity = mActivity.get();
			switch (msg.what)
				{
				case AUTOSLIP:
					currActivity.mViewPager
							.setCurrentItem(currActivity.mViewPager
									.getCurrentItem() + 1);
					currActivity.handler
							.sendEmptyMessageDelayed(AUTOSLIP, 3000);
					break;
				case GETPUSH:
					if ((String) msg.obj == null)
					{
						currActivity.jarr_push = new JSONArray();
					}
					else
					{
						String push = (String) msg.obj;
						push = CheckUtil.replaceBlank(push);
						currActivity.json_push = JSONObject.fromObject(push);
						currActivity.jarr_push = currActivity.json_push
								.getJSONArray("results");
						currActivity.initPush();
					}
					
					break;
				case GETRECOTOP:
					if ((String) msg.obj == null)
					{
						currActivity.jarr_recotopic = new JSONArray();
					}
					else
					{
						String topic = (String) msg.obj;
						topic = CheckUtil.replaceBlank(topic);
						currActivity.json_recotopic = JSONObject.fromObject(topic);
						currActivity.jarr_recotopic = currActivity.json_recotopic
								.getJSONArray("results");
					}
					currActivity.initList();
					break;
				case ADD_ZAN:
					String mess = (String) msg.obj;
					if (mess != null)
					{
						if (mess.equals("�����޹��˶���"))
						{
							Toast.makeText(currActivity, mess,
									Toast.LENGTH_LONG).show();
						}
						else if (mess.equals("���޳ɹ���"))
						{
							String zancount = Integer
									.parseInt(currActivity.jarr_recotopic
											.getJSONObject(
													currActivity.zanPosition)
											.getString("top_zancount"))
									+ 1 + "";
							currActivity.jarr_recotopic.getJSONObject(
									currActivity.zanPosition).put(
									"top_zancount", zancount);// �ı�����Դ

							TextView tv = (TextView) currActivity.listview
									.findViewWithTag(currActivity.zanPosition)
									.findViewById(R.id.TVreco_top_zan);
							tv.setText(zancount);

							Toast.makeText(currActivity, mess,
									Toast.LENGTH_LONG).show();
						}
						else
						{
							Toast.makeText(currActivity, mess,
									Toast.LENGTH_LONG).show();
						}
					}
					else
					{
						Toast.makeText(currActivity, "���ݴ���ʧ�ܣ�",
								Toast.LENGTH_LONG).show();
					}
					break;
				case Constant.CONNECTIONTIMEOUT:
					if (currActivity.rlloading.getVisibility() == View.VISIBLE)
					{
						currActivity.rlloading.setVisibility(View.GONE);
					}
					currActivity.rlloadstauts = (RelativeLayout) currActivity
							.findViewById(R.id.RLmain_loadstauts);
					currActivity.imvTip = (ImageView) currActivity
							.findViewById(R.id.IMVloadtip);
					currActivity.tvtip = (TextView) currActivity
							.findViewById(R.id.TVloadtip);
					currActivity.imvTip
							.setImageResource(R.drawable.network_error);

					if (CheckNetworkStatus.checkNetworkAvailable(currActivity))
					{// �ֻ��������
						currActivity.tvtip.setText("������󣬵������");
						Toast.makeText(currActivity, "���ӳ�ʱ��", Toast.LENGTH_LONG)
								.show();
					}
					else
					{
						currActivity.tvtip.setText("�����������ӣ�");
						Toast.makeText(currActivity, "���粻���ã������������ӣ�",
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
							.findViewById(R.id.RLmain_loadstauts);
					currActivity.imvTip = (ImageView) currActivity
							.findViewById(R.id.IMVloadtip);
					currActivity.tvtip = (TextView) currActivity
							.findViewById(R.id.TVloadtip);
					currActivity.imvTip.setImageResource(R.drawable.refresh);
					currActivity.tvtip.setText("������¼���");
					currActivity.rlloadstauts.setVisibility(View.VISIBLE);
					currActivity.imvTip
							.setOnClickListener(currActivity.clicklistener);
					Toast.makeText(currActivity, "��ȡ���ݳ�ʱ��", Toast.LENGTH_LONG)
							.show();
					break;
				}
		}
	}

	//��ʼ�����
	public void initPush()
	{
		initViewPagerView();
		pagerAdapter = new MyPagerAdapter();
		mViewPager.setAdapter(pagerAdapter);
		mViewPager.setOnPageChangeListener(new MyOnPageChangeListener());
		mViewPager.setOnTouchListener(new OnTouchListener()
		{

			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				switch (event.getAction())
					{
					case MotionEvent.ACTION_DOWN:
						handler.removeMessages(AUTOSLIP);
						break;
					case MotionEvent.ACTION_MOVE:
						handler.removeMessages(AUTOSLIP);// ACTION_DOWN�¼���ʱ��׽������д��MOVE��
						break;
					case MotionEvent.ACTION_UP:
						handler.sendEmptyMessageDelayed(AUTOSLIP, 3000);
						break;
					case MotionEvent.ACTION_CANCEL:
						break;
					}
				return false;
			}
		});
		mViewPager.setCurrentItem(imageViewlist.size() * 20);
		handler.sendEmptyMessageDelayed(AUTOSLIP, 3000);
	}

	//��ʼ�����������
	private void initViewPagerView()
	{
		if (imageViewlist == null)
			imageViewlist = new ArrayList<ImageView>();
		for (int i = 0; i < jarr_push.size(); i++)
		{
			ImageView imv = new ImageView(this);
			imv.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.MATCH_PARENT));
			imv.setScaleType(ImageView.ScaleType.CENTER_CROP);
			imv.setImageResource(R.drawable.default_picture);
			imv.setTag(i + "");//ΪImageView����tag��ʶ
			imv.setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View v)
				{
					// TODO Auto-generated method stub
					Intent intent = new Intent(MainActivity.this,
							PushDetailsActivity.class);
					intent.putExtra(
							"PUSHINFO",
							jarr_push.getJSONObject(
									mViewPager.getCurrentItem()
											% jarr_push.size()).toString());
					startActivity(intent);
				}
			});

			DownloadPicThread loadThread;
			loadThread = new DownloadPicThread(jarr_push.getJSONObject(i)
					.getString("push_pic"), imv, handler);
			loadThread.start();
			imageViewlist.add(imv);

			// ����С��
			View v = new View(this);
			// ����С��Ŀ�͸�
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(30, 30);
			// ����С��ļ��
			lp.setMargins(50, 0, 0, 0);
			v.setLayoutParams(lp);
			v.setEnabled(false);
			// ����С��ı��������������ʹ��xml�ļ�����һ��СԲ��
			v.setBackgroundResource(R.drawable.pointer_selector);
			// ��С����ӵ����Ĳ����ļ���
			pointGroup.addView(v);
		}
	}

	//��ʼ�������б�
	private void initList()
	{
		listview = (ListView) this.findViewById(R.id.LV_reco_topic);
		listview.setFocusable(false);// ��ֹ��ʼ��ʱ������listview�ĵ�һ��
		adapter = new RecoTopicAdapter();
		listview.setAdapter(adapter);
		setListViewHeight(listview);

		listview.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3)
			{
				Constant.currBloId = jarr_recotopic.getJSONObject(arg2)
						.getString("top_blockid");// ����ReplyActivity���ڵİ��ID
				Constant.currTopicId = jarr_recotopic.getJSONObject(arg2)
						.getString("top_id");// ����ReplyActivity���ڵ�������
				Constant.currTopicInfo = jarr_recotopic.getJSONObject(arg2);// ����ReplyActivity��Header����
				// ���������������+1(Android)
				new Thread()
				{

					@Override
					public void run()
					{
						NetTransUtil.addTopBrowCount(Constant.currTopicId);
					}

				}.start();

				intent = new Intent();
				intent.setClass(MainActivity.this, ReplyActivity.class);
				intent.putExtra("BLONAME", jarr_recotopic.getJSONObject(arg2)
						.getString("blo_name"));
				MainActivity.this.startActivity(intent);
			}
		});

		// ȥ�����ػ���ͼ��
		if (rlloading.getVisibility() == View.VISIBLE)
		{
			rlloading.setVisibility(View.GONE);
		}

	}

	private class RecoTopicAdapter extends BaseAdapter
	{
		private ViewHolder viewholder;

		@Override
		public int getCount()
		{
			return jarr_recotopic.size();
		}

		@Override
		public Object getItem(int position)
		{
			return jarr_recotopic.getJSONObject(position);
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
			final JSONObject json_temp = jarr_recotopic.getJSONObject(position);
			// ����View
			if (convertView == null)
			{
				convertView = getLayoutInflater().inflate(
						R.layout.item_reco_topic, null);
				viewholder = new ViewHolder();

				viewholder.imvHeadImg = (ImageView) convertView
						.findViewById(R.id.IMVreco_top_head);
				viewholder.imvSex = (ImageView) convertView
						.findViewById(R.id.IMVreco_top_sex);
				viewholder.tvSendTime = (TextView) convertView
						.findViewById(R.id.TVreco_top_sendtime);
				viewholder.tvUserName = (TextView) convertView
						.findViewById(R.id.TVreco_top_name);
				viewholder.tvUserAge = (TextView) convertView
						.findViewById(R.id.TVreco_top_age);
				viewholder.tvTitle = (TextView) convertView
						.findViewById(R.id.TVreco_top_title);
				viewholder.tvContent = (TextView) convertView
						.findViewById(R.id.TVreco_top_content);
				viewholder.imvPic1 = (ImageView) convertView
						.findViewById(R.id.IMVreco_top_pic1);
				viewholder.imvPic2 = (ImageView) convertView
						.findViewById(R.id.IMVreco_top_pic2);
				viewholder.imvPic3 = (ImageView) convertView
						.findViewById(R.id.IMVreco_top_pic3);
				viewholder.imvZan = (ImageView) convertView
						.findViewById(R.id.IMVreco_top_zan);
				viewholder.tvBrowcount = (TextView) convertView
						.findViewById(R.id.TVreco_top_brow);
				viewholder.tvZancount = (TextView) convertView
						.findViewById(R.id.TVreco_top_zan);
				viewholder.tvReplycount = (TextView) convertView
						.findViewById(R.id.TVreco_top_reply);

				convertView.setTag(viewholder);// ���ñ�ǩ
			}
			else
			{
				viewholder = (ViewHolder) convertView.getTag();// ���ݱ�ǩ�õ���ͼ
			}

			viewholder.tvUserName.setText(json_temp.getString("user_name"));
			viewholder.tvUserAge.setText(json_temp.getString("user_age") + "��");
			viewholder.tvTitle.setText(json_temp.getString("top_title"));
			// �������������ñ���
			SpannableString spannable = GetEmotion.getEmotion(
					MainActivity.this, json_temp.getString("top_content"));
			viewholder.tvContent.setText(spannable);
			viewholder.tvSendTime.setText(TimeChange.changeTime(json_temp
					.getString("top_sendtime")));
			viewholder.tvBrowcount
					.setText(json_temp.getString("top_browcount"));
			viewholder.tvZancount.setText(json_temp.getString("top_zancount"));
			viewholder.tvReplycount.setText(json_temp
					.getString("top_replycount"));

			if (json_temp.getString("user_sex").equals("��"))
			{
				viewholder.imvSex.setImageResource(R.drawable.sex_man);
			}
			else
			{
				viewholder.imvSex.setImageResource(R.drawable.sex_woman);
			}

			viewholder.tvZancount.setTag(position);
			imgdownload = ImgAsyncDownload.getInstance();

			if (imgdownload != null)
			{
				// �첽����ͼƬ
				// ����ͷ��
				viewholder.imvHeadImg.setTag(json_temp.getString("user_image"));
				viewholder.imvHeadImg.setImageResource(R.drawable.defaultimage);
				imgdownload.imageDownload(viewholder.imvHeadImg);

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

			viewholder.imvHeadImg.setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View v)
				{
					Intent intent = new Intent(MainActivity.this,
							LookUserInfoActivity.class);
					intent.putExtra("USERID", json_temp.getString("top_userid"));
					startActivity(intent);
				}
			});

			viewholder.imvZan.setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View v)
				{
					if (Constant.isLogin)
					{
						zanPosition = position;
						new Thread()
						{

							@Override
							public void run()
							{

								try
								{
									String mess = NetTransUtil.addTopicZan(
											Constant.loginUid,
											json_temp.getString("top_id"));
									handler.obtainMessage(ADD_ZAN, mess)
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
					else
					{
						intent = new Intent(MainActivity.this, LoginActivity.class);
						startActivity(intent);
					}
				}
			});

			return convertView;
		}

		private class ViewHolder
		{
			ImageView imvHeadImg, imvSex, imvPic1, imvPic2, imvPic3, imvZan;
			TextView tvUserName, tvUserAge, tvSendTime, tvTitle, tvContent,
					tvBrowcount, tvZancount, tvReplycount;
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
		intent = new Intent(MainActivity.this, ScanPicActivity.class);
		intent.putExtra("POSITION", position);
		intent.putStringArrayListExtra("PATHLIST", picpathList);
		startActivity(intent);
	}

	/**
	 * ����listView�ĸ߶�
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

	//��ȡ����߳�
	private class GetPushThread extends Thread
	{

		@Override
		public void run()
		{
			try
			{
				String push = NetTransUtil.getPush("false");
				handler.obtainMessage(GETPUSH, push).sendToTarget();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

	}

	//��ȡ�Ƽ������߳�
	private class GetRecoTopThread extends Thread
	{

		@Override
		public void run()
		{
			try
			{
				String topic = NetTransUtil.getRecoTopic(0);
				handler.obtainMessage(GETRECOTOP, topic).sendToTarget();
			}
			catch (Exception e)
			{
				// TODO Auto-generated catch block
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
			// ȥ����ʾ
			if (rlloadstauts.getVisibility() == View.VISIBLE)
			{
				rlloadstauts.setVisibility(View.GONE);
			}

			if ((pushThread != null) && !pushThread.isAlive())
			{
				if (rlloading.getVisibility() == View.GONE)
				{
					rlloading.setVisibility(View.VISIBLE);
				}

				pushThread = new GetPushThread();
				pushThread.start();
			}

			if ((recoTopThread != null) && !recoTopThread.isAlive())
			{
				if (rlloading.getVisibility() == View.GONE)
				{
					rlloading.setVisibility(View.VISIBLE);
				}

				recoTopThread = new GetRecoTopThread();
				recoTopThread.start();
			}
		}

	}

	@Override
	protected void onPause()
	{
		// ֹͣ����
		handler.removeMessages(AUTOSLIP);
		super.onPause();
	}

	@Override
	protected void onResume()
	{
		// ���viewpager��û�г�ʼ���������ͻ�����Ϣ
		if (pagerAdapter != null)
		{
			handler.sendEmptyMessageDelayed(AUTOSLIP, 3000);
		}
		super.onResume();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		// TODO Auto-generated method stub
		// super.onSaveInstanceState(outState);
	}

	//ViewPager������
	private class MyPagerAdapter extends PagerAdapter
	{
		@Override
		public int getCount()
		{
			return Integer.MAX_VALUE;
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1)
		{
			return arg0 == arg1;
		}

		// ��ĳһҳ����ȥ��ʱ�򣬽�������
		@Override
		public void destroyItem(ViewGroup container, int position, Object object)
		{
			View view = imageViewlist.get(position % imageViewlist.size());
			int posi = viewmap.get(view.getTag());
			// �ж�Ҫremove��view�Ƿ�������ʾ
			if (!(posi == currposi || posi == currposi - 1 || posi == currposi + 1))
			{
				container.removeView(view);
			}
		}

		// �����������ͼƬ����������Ҫʵ��ѭ��������Ч��������Ҫ��positionȡģ
		@Override
		public Object instantiateItem(ViewGroup container, int position)
		{
			View view = imageViewlist.get(position % imageViewlist.size());
			/*
			 * ���ж�Ҫ��ʼ����view�Ƿ��Ѿ���ӵ�container�У��������Ƚ�֮ǰ��view�Ƴ������
			 */
			if (view.getParent() != null)
			{
				container.removeView(view);
			}
			container.addView(view);
			viewmap.put((String) view.getTag(), position);
			return view;
		}
	}

	//Viewpagerҳ��ı����
	private class MyOnPageChangeListener implements OnPageChangeListener
	{

		private int previousPoint = 0;

		// ��ʼ
		@Override
		public void onPageScrollStateChanged(int arg0)
		{
		}

		// ���ڽ���ʱ
		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2)
		{
		}

		// ����
		@Override
		public void onPageSelected(int position)
		{
			currposi = position;
			// ��ҳ�滬������ʱ���ȶ�ҳ��λ��ȡģ
			position = position % imageViewlist.size();
			// // ����һ����Ŀ���������Ϊfalse
			pointGroup.getChildAt(previousPoint).setEnabled(false);
			// // �ѵ�ǰ��Ŀ���������Ϊtrue
			pointGroup.getChildAt(position).setEnabled(true);
			// // �ѵ�ǰλ��ֵ��ֵ��previousPoint
			previousPoint = position;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			// ͨ����ȡ��ǰʱ�䣬�ڼ���ʱ������ж��Ƿ��˳�����һ�ΰ��϶������˳�����ΪexitTime����ʼ��Ϊ0��
			// ʱ����ǵ�ǰʱ���1970��1��1���賿��ʱ�䣬������ֵ��֮�����жϾ��ϴΰ���ʱ���Ƿ����2000ms
			if ((System.currentTimeMillis() - exitTime) > 2000)
			{
				Toast.makeText(this, "�ٰ�һ���˳�����", Toast.LENGTH_SHORT).show();
				exitTime = System.currentTimeMillis();// �����ϴΰ���ʱ��
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
