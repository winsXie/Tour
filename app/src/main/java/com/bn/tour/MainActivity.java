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
	private static final int AUTOSLIP = 0;//广告条自动滑动（what值）
	private static final int GETPUSH = 1;//获取推荐的广告
	private static final int GETRECOTOP = 2;//获取推荐的主题帖
	private static final int ADD_ZAN = 3;//为主题帖点赞
	//因为要在viewpager中循环展示图片，用Map来存储各个veiw被添加到viewpager中的位置
	private Map<String, Integer> viewmap = new HashMap<String, Integer>();
	private int currposi = 0;//viewpager当前显示的位置

	private JSONObject json_push, json_recotopic;//存储数据的JSONObject对象
	private JSONArray jarr_push, jarr_recotopic;//通过JSONObject得到的JSONArray
	private ViewPager mViewPager;//展示广告条的viewpager
	private MyPagerAdapter pagerAdapter;
	private ListView listview;//显示推荐主题的listview
	private ImgAsyncDownload imgdownload;//异步加载图片线程
	private RecoTopicAdapter adapter;
	private ArrayList<ImageView> imageViewlist;//用于存储广告图片的list
	private GetPushThread pushThread;
	private GetRecoTopThread recoTopThread;
	private LinearLayout pointGroup;//广告条底部原点布局
	private Intent intent;
	private int zanPosition;//点赞主题所在位置
	private long exitTime = 0;//初始化第一次按返回键的时间
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

		// 检测SD卡
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
						if (mess.equals("您已赞过此对象！"))
						{
							Toast.makeText(currActivity, mess,
									Toast.LENGTH_LONG).show();
						}
						else if (mess.equals("点赞成功！"))
						{
							String zancount = Integer
									.parseInt(currActivity.jarr_recotopic
											.getJSONObject(
													currActivity.zanPosition)
											.getString("top_zancount"))
									+ 1 + "";
							currActivity.jarr_recotopic.getJSONObject(
									currActivity.zanPosition).put(
									"top_zancount", zancount);// 改变数据源

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
						Toast.makeText(currActivity, "数据传输失败！",
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
							.findViewById(R.id.RLmain_loadstauts);
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

	//初始化广告
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
						handler.removeMessages(AUTOSLIP);// ACTION_DOWN事件有时捕捉不到，写在MOVE里
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

	//初始化广告条布局
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
			imv.setTag(i + "");//为ImageView设置tag标识
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

			// 构造小点
			View v = new View(this);
			// 设置小点的宽和高
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(30, 30);
			// 设置小点的间隔
			lp.setMargins(50, 0, 0, 0);
			v.setLayoutParams(lp);
			v.setEnabled(false);
			// 设置小点的背景，这个背景是使用xml文件画的一个小圆点
			v.setBackgroundResource(R.drawable.pointer_selector);
			// 把小点添加到它的布局文件中
			pointGroup.addView(v);
		}
	}

	//初始化主题列表
	private void initList()
	{
		listview = (ListView) this.findViewById(R.id.LV_reco_topic);
		listview.setFocusable(false);// 防止初始化时滑动到listview的第一项
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
						.getString("top_blockid");// 设置ReplyActivity所在的版块ID
				Constant.currTopicId = jarr_recotopic.getJSONObject(arg2)
						.getString("top_id");// 设置ReplyActivity所在的主题帖
				Constant.currTopicInfo = jarr_recotopic.getJSONObject(arg2);// 设置ReplyActivity的Header内容
				// 将主题帖的浏览数+1(Android)
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

		// 去除加载缓冲图标
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
			// 重用View
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

				convertView.setTag(viewholder);// 设置标签
			}
			else
			{
				viewholder = (ViewHolder) convertView.getTag();// 根据标签得到视图
			}

			viewholder.tvUserName.setText(json_temp.getString("user_name"));
			viewholder.tvUserAge.setText(json_temp.getString("user_age") + "岁");
			viewholder.tvTitle.setText(json_temp.getString("top_title"));
			// 主题帖内容设置表情
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

			if (json_temp.getString("user_sex").equals("男"))
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
				// 异步下载图片
				// 加载头像
				viewholder.imvHeadImg.setTag(json_temp.getString("user_image"));
				viewholder.imvHeadImg.setImageResource(R.drawable.defaultimage);
				imgdownload.imageDownload(viewholder.imvHeadImg);

				final String picPath = json_temp.getString("top_pic");// 主题帖图片
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
	 * 浏览图片
	 * 
	 * @param path
	 *            帖子的图片路径
	 * @param position
	 *            第几张图片
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
	 * 设置listView的高度
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

	//获取广告线程
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

	//获取推荐主题线程
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
			// 去除提示
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
		// 停止滑动
		handler.removeMessages(AUTOSLIP);
		super.onPause();
	}

	@Override
	protected void onResume()
	{
		// 如果viewpager还没有初始化，不发送滑动消息
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

	//ViewPager适配器
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

		// 当某一页滑出去的时候，将其销毁
		@Override
		public void destroyItem(ViewGroup container, int position, Object object)
		{
			View view = imageViewlist.get(position % imageViewlist.size());
			int posi = viewmap.get(view.getTag());
			// 判断要remove的view是否正在显示
			if (!(posi == currposi || posi == currposi - 1 || posi == currposi + 1))
			{
				container.removeView(view);
			}
		}

		// 向容器中添加图片，由于我们要实现循环滑动的效果，所以要对position取模
		@Override
		public Object instantiateItem(ViewGroup container, int position)
		{
			View view = imageViewlist.get(position % imageViewlist.size());
			/*
			 * 先判断要初始化的view是否已经添加到container中，若存在先将之前的view移除再添加
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

	//Viewpager页面改变监听
	private class MyOnPageChangeListener implements OnPageChangeListener
	{

		private int previousPoint = 0;

		// 开始
		@Override
		public void onPageScrollStateChanged(int arg0)
		{
		}

		// 正在进行时
		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2)
		{
		}

		// 结束
		@Override
		public void onPageSelected(int position)
		{
			currposi = position;
			// 当页面滑动结束时，先对页面位置取模
			position = position % imageViewlist.size();
			// // 将上一个点的可用性设置为false
			pointGroup.getChildAt(previousPoint).setEnabled(false);
			// // 把当前点的可用性设置为true
			pointGroup.getChildAt(position).setEnabled(true);
			// // 把当前位置值赋值给previousPoint
			previousPoint = position;
		}
	}

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
