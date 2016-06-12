package com.bn.bbs;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bn.getnativepicture.PicAlbumActivity;
import com.bn.thread.DownloadPicThread;
import com.bn.thread.ImgAsyncDownload;
import com.bn.tour.EditPhotoActivity;
import com.bn.tour.GridAddPicAdapter;
import com.bn.tour.R;
import com.bn.tour.ScanPicActivity;
import com.bn.user.LoginActivity;
import com.bn.user.LookUserInfoActivity;
import com.bn.util.BitmapUtil;
import com.bn.util.CheckNetworkStatus;
import com.bn.util.CheckUtil;
import com.bn.util.Constant;
import com.bn.util.GetEmotion;
import com.bn.util.NetTransUtil;
import com.bn.util.TimeChange;

public class ReplyActivity extends Activity
{
	private static final int GET_REPLY = 0;//获取回复帖（handler的what值）
	private static final int UPDATE_PIC = 1;//通知GridAddPicAdapter数据改变
	private static final int UPDATE_REPLY = 2;//更新回复帖（在将所有回复帖全部加载后用此方法更新）
	private static final int LOAD_PAGE = 3;//分批加载
	private static final int PUBLISH_REPLY = 4;//发表回复帖
	private static final int ADD_ZAN = 5;//为回复帖点赞
	private static final int DISPLAY_TOAST = 6;//展示toast信息
	private static final int TAKE_PHOTO = 0x000000;//调用系统拍照功能
	private int floor;//回复帖所在楼层（用于跳转到指定楼层）
	private String path = "";//拍照后的图片路径
	private ListView listview;
	private ReplyAdapter replyadapter;
	private ImgAsyncDownload imgdownload;//异步加载图片线程（listview内图片的加载）
	private DownloadPicThread loadThread;//异步加载图片线程（非listview内图片的加载）
	private GetReplyThread getReplyThread;//获取回复帖线程
	private JSONObject json_reply, json_tiopicinfo;
	private JSONArray jarr_reply;//根据JSONObject得到的JSONArray
	private View footer;//listview的footerview

	private LinearLayout footerLayout;//footer布局
	private ScrollListener scrolllistener;//滑动监听（用于分批加载）
	private static final int ITEMCOUTN = 5;//分批加载每次加载的条目数
	private boolean loading = false;// 正在加载标志位
	private boolean hadTotalLoaded = false;// 加载完成标志位
	private boolean isToLast = false;// 是否要一直加载到底部标志位（用于回复后刷新界面，一直滚到最后一层）
	private boolean isToTarget = false;//是否要滑动到指定位置（从我的回复帖界面点击相应的回复帖直接滑动到指定楼层）
	private Intent intent;
	private int zanPosition;//点赞item的位置

	private String answerid;// 所回复的帖子的ID，可能是主题帖，也可能为回复帖
	private String to_userid;// 所回复帖子的用户ID
	private ClickListener clicklistener;

	private EditText etMess;//回复内容
	private Button replybt;//提示回复楼层的button
	private ImageView mFace;
	private InputMethodManager imm;//软键盘管理
	private GridViewFaceAdapter mGVFaceAdapter;//表情适配器
	private GridView gridViewFace, gridViewPic;
	private Button buSendMess;//发送按钮
	private ImageView addPic;
	private boolean faceShow = true;//是否显示表情
	private boolean picShow = true;//是否显示选择的图片
	private GridAddPicAdapter picAdapter;//添加图片适配器
	private RelativeLayout rlloading, rlloadstauts;
	private ImageView imvTip;//用于提示网络状态的imageview
	private TextView tvtip;

	private Handler handler;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reply);
		handler = new MyHandler(this);

		rlloading = (RelativeLayout) this.findViewById(R.id.RLreply_load);
		rlloading.setVisibility(View.VISIBLE);
		clicklistener = new ClickListener();

		getReplyThread = new GetReplyThread();
		getReplyThread.start();

		TextView tvBloname = (TextView) this.findViewById(R.id.TVreply_bloname);
		String bloname = getIntent().getStringExtra("BLONAME");
		tvBloname.setText(bloname);

		floor = getIntent().getIntExtra("POSITION", 0);

		BitmapUtil.maxCount = 3;// 设置能够发送图片的最大数量

		// 软键盘管理类
		imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
	}

	private static class MyHandler extends Handler
	{
		WeakReference<ReplyActivity> mActivity;

		public MyHandler(ReplyActivity activity)
		{
			mActivity = new WeakReference<ReplyActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg)
		{
			final ReplyActivity currActivity = mActivity.get();
			switch (msg.what)
				{
				case GET_REPLY:
					if ((String) msg.obj == null)
					{
						currActivity.jarr_reply = new JSONArray();
					}
					else
					{
						currActivity.json_reply = JSONObject
								.fromObject(CheckUtil
										.replaceBlank((String) msg.obj));
						currActivity.jarr_reply = currActivity.json_reply
								.getJSONArray("results");
						if (currActivity.jarr_reply.size() < ITEMCOUTN)
						{
							currActivity.hadTotalLoaded = true;
						}
					}
					currActivity.initView();// 初始化listview，此时jarr_reply已经非空
					currActivity.initGridView();
					break;
				case UPDATE_PIC:
					currActivity.picAdapter.notifyDataSetChanged();
					break;
				case UPDATE_REPLY:
					String json_rep = (String) msg.obj;
					if (json_rep != null)
					{
						currActivity.jarr_reply.add(
								currActivity.jarr_reply.size(), json_rep);
						currActivity.replyadapter.notifyDataSetChanged();
						currActivity.listview
								.setSelection(currActivity.replyadapter
										.getCount() - 1);
					}

					// 去除加载缓冲
					if (currActivity.rlloading.getVisibility() == View.VISIBLE)
					{
						currActivity.rlloading.setVisibility(View.GONE);
					}
					break;
				case LOAD_PAGE:
					String get_json = (String) msg.obj;
					if (get_json == null)
					{
						currActivity.hadTotalLoaded = true;
					}
					else
					{
						JSONObject json_new = JSONObject.fromObject(CheckUtil
								.replaceBlank(get_json));
						JSONArray jarr_new = json_new.getJSONArray("results");
						if (jarr_new.size() < ITEMCOUTN)
						{
							currActivity.hadTotalLoaded = true;
						}
						String json1 = currActivity.jarr_reply.toString();
						String json2 = jarr_new.toString();
						String newjson = json1.substring(0, json1.length() - 1)
								+ "," + json2.substring(1, json2.length());
						currActivity.jarr_reply = JSONArray.fromObject(newjson);
						currActivity.replyadapter.notifyDataSetChanged();
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

					currActivity.loading = false;// 设置标志位
					if (currActivity.isToLast)
					{
						currActivity.listview
								.setSelection(currActivity.replyadapter
										.getCount() - 1);
					}

					if (currActivity.isToTarget)
					{
						if (currActivity.floor >= currActivity.replyadapter
								.getCount())
						{// 所在楼层大于回复帖总数（有楼层被删除）
							/*
							 * 可以不取消isToTarget标志位，这种情况只有两种可能，
							 * 第一种：还未加载到所在楼层，需要继续加载，继续触发分批加载
							 * 第二种：已全部加载，但所在楼层大于回复帖总数（有楼层被删除），不需置回
							 */
							currActivity.listview
									.setSelection(currActivity.replyadapter
											.getCount() - 1);
						}
						else
						{
							currActivity.listview
									.setSelection(currActivity.floor);
							currActivity.isToTarget = false;// 取消滑动到指定位置标志位
						}
					}
					break;
				case PUBLISH_REPLY:
					final String repid = (String) msg.obj;// 返回的刚发表的回复帖ID
					if (repid != null)
					{
						Toast.makeText(currActivity, "发表成功！",
								Toast.LENGTH_SHORT).show();
						// 清空各种临时存储
						BitmapUtil.lastSize = 0;
						BitmapUtil.pathlist.clear();
						BitmapUtil.bmplist.clear();
						BitmapUtil.templist.clear();
						BitmapUtil.delTempPic();
						currActivity.etMess.setText("");
						if (currActivity.imm.isActive(currActivity.etMess))
						{// 隐藏软键盘
							currActivity.imm.hideSoftInputFromWindow(
									currActivity.etMess.getWindowToken(), 0);
						}
						currActivity.etMess.clearFocus();
						if (currActivity.gridViewFace.getVisibility() == View.VISIBLE)
						{
							currActivity.gridViewFace.setVisibility(View.GONE);
							currActivity.faceShow = true;
						}

						if (currActivity.gridViewPic.getVisibility() == View.VISIBLE)
						{
							currActivity.gridViewPic.setVisibility(View.GONE);
							currActivity.picShow = true;
						}

						currActivity.picAdapter.update();

						/*
						 * 回复完成后要更新回复帖，滚动到回复完成后所在的楼层 因为涉及到分批加载，要考虑回复时是否已经全部加载完成，
						 * 若全部加载完成，hadTotalLoaded已置为true，只需获取刚才回复的内容，
						 * 加到json里notifyDataSetChanged后，滚到到 最后一条即可，不会触发分批加载
						 * 否则就要加载出所有的回复帖
						 * （会触发分批加载，设置为自动分批加载），刚刚回复的就在最后一层，不用再单独获取信息
						 */
						if (!currActivity.hadTotalLoaded)
						{// 未全部加载，要一直加载到最后一条（会触发分批加载，因为此时hadTotalLoaded为false）
							currActivity.isToLast = true;//设置标志位，直接滑到底部
							//其他方式没有实现滑动功能，只能用此方法
							currActivity.listview.post(new Runnable()
							{
								
								@Override
								public void run()
								{
									 currActivity.listview
									 .setSelection(currActivity.replyadapter
									 .getCount() - 1);
								}
							});

							// 去除加载缓冲
							if (currActivity.rlloading.getVisibility() == View.VISIBLE)
							{
								currActivity.rlloading.setVisibility(View.GONE);
							}
						}
						else
						{// 已全部加载
							new Thread()
							{

								@Override
								public void run()
								{
									try
									{// 获取刚才回复的帖子信息
										String repinfo = NetTransUtil
												.getRepById(repid);
										currActivity.handler.obtainMessage(
												UPDATE_REPLY, repinfo)
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
					}
					else
					{
						// 去除加载缓冲
						if (currActivity.rlloading.getVisibility() == View.VISIBLE)
						{
							currActivity.rlloading.setVisibility(View.GONE);
						}
						Toast.makeText(currActivity, "发表失败！请重试",
								Toast.LENGTH_SHORT).show();
					}
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
									.parseInt(currActivity.jarr_reply
											.getJSONObject(
													currActivity.zanPosition)
											.getString("rep_zancount"))
									+ 1 + "";
							currActivity.jarr_reply.getJSONObject(
									currActivity.zanPosition).put(
									"rep_zancount", zancount);// 改变数据源

							TextView tv = (TextView) currActivity.listview
									.findViewWithTag(currActivity.zanPosition)
									.findViewById(R.id.TVreply_zancount);
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
				case DISPLAY_TOAST:
					// 在分批加载，发帖成功后返回，会发送此消息
					String toast = (String) msg.obj;
					if (toast != null)
					{
						Toast.makeText(currActivity, toast, Toast.LENGTH_SHORT)
								.show();
					}
					// 去除加载缓冲
					if (currActivity.rlloading.getVisibility() == View.VISIBLE)
					{
						currActivity.rlloading.setVisibility(View.GONE);
					}
					// 肯定是没有加载完，去除footer
					if (currActivity.listview.getFooterViewsCount() > 0)
					{
						currActivity.listview
								.removeFooterView(currActivity.footer);
					}
					currActivity.loading = false;// 重置标志位
					break;
				case Constant.CONNECTIONTIMEOUT:
					if (currActivity.rlloading.getVisibility() == View.VISIBLE)
					{
						currActivity.rlloading.setVisibility(View.GONE);
					}
					currActivity.rlloadstauts = (RelativeLayout) currActivity
							.findViewById(R.id.RLreply_loadstauts);
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
							.findViewById(R.id.RLreply_loadstauts);
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
			super.handleMessage(msg);
		}
	}

	/**
	 * 初始化listview
	 */
	private void initView()
	{
		listview = (ListView) this.findViewById(R.id.LVreply);

		json_tiopicinfo = JSONObject.fromObject(Constant.currTopicInfo);
		answerid = json_tiopicinfo.getString("top_id");// 先默认为回复主题帖
		to_userid = json_tiopicinfo.getString("top_userid");
		ImageView imvBack = (ImageView) this.findViewById(R.id.IMVreply_back);

		this.addHeader();

		footer = getLayoutInflater().inflate(R.layout.footer, null);
		footerLayout = (LinearLayout) footer.findViewById(R.id.LLfooter);

		replyadapter = new ReplyAdapter();
		listview.setAdapter(replyadapter);// 设置适配器

		if (floor != 0)
		{
			if (floor >= replyadapter.getCount())
			{// 所在楼层大于回复帖总数（有楼层被删除）
				isToTarget = true;
				listview.setSelection(replyadapter.getCount() - 1);
			}
			else
			{
				listview.setSelection(floor);
			}
		}

		listview.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3)
			{
				answerid = jarr_reply.getJSONObject(arg2 - 1).getString(
						"rep_id");// 将所回复的帖子的ID置为当前回复帖的ID
				to_userid = jarr_reply.getJSONObject(arg2 - 1).getString(
						"rep_userid");// 将所回复的帖子的用户ID置为当前回复帖的用户ID

				replybt.setVisibility(View.VISIBLE);
				replybt.setText("回复"
						+ jarr_reply.getJSONObject(arg2 - 1).getString(
								"rep_floor") + "楼：");
				etMess.setHint("");

				etMess.requestFocus();
				etMess.setSelection(etMess.getText().length());
				gridViewFace.setVisibility(View.GONE);
				gridViewPic.setVisibility(View.GONE);
				imm.showSoftInput(etMess, InputMethodManager.SHOW_FORCED);
			}
		});

		// 没有数据时设加载完成标志位，防止加载出footer
		if (jarr_reply.size() == 0)
		{
			hadTotalLoaded = true;
		}
		scrolllistener = new ScrollListener();
		listview.setOnScrollListener(scrolllistener);

		mFace = (ImageView) this.findViewById(R.id.IMVface);
		addPic = (ImageView) findViewById(R.id.IMVadd_pic);
		etMess = (EditText) this.findViewById(R.id.ETmess);
		replybt = (Button) this.findViewById(R.id.button);
		buSendMess = (Button) this.findViewById(R.id.BTsend_mess);

		mFace.setOnClickListener(clicklistener);
		addPic.setOnClickListener(clicklistener);
		etMess.setOnClickListener(clicklistener);
		buSendMess.setOnClickListener(clicklistener);
		imvBack.setOnClickListener(clicklistener);

		etMess.setOnKeyListener(new OnKeyListener()
		{

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event)
			{
				// TODO Auto-generated method stub
				if (keyCode == KeyEvent.KEYCODE_DEL
						&& event.getAction() == KeyEvent.ACTION_DOWN)
				{
					if (etMess.getText().length() == 0)
					{
						replybt.setVisibility(View.GONE);
						etMess.setHint("回复楼主：");
					}
				}
				return false;
			}
		});

		// 去除加载缓冲图标
		if (rlloading.getVisibility() == View.VISIBLE)
		{
			rlloading.setVisibility(View.GONE);
		}
	}

	/**
	 * 初始化表情和图片GridView
	 */
	private void initGridView()
	{
		// 初始化表情控件
		mGVFaceAdapter = new GridViewFaceAdapter(this);
		gridViewFace = (GridView) findViewById(R.id.GV_faces);
		gridViewFace.setAdapter(mGVFaceAdapter);
		gridViewFace
				.setOnItemClickListener(new AdapterView.OnItemClickListener()
				{
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id)
					{
						// 插入的表情
						SpannableString ss = new SpannableString(view.getTag()
								.toString());
						Drawable d = getResources().getDrawable(
								(int) mGVFaceAdapter.getItemId(position));
						d.setBounds(0, 0, 70, 70);// 设置表情图片的显示大小
						ImageSpan span = new ImageSpan(d,
								ImageSpan.ALIGN_BOTTOM);
						ss.setSpan(span, 0, view.getTag().toString().length(),
								Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						// 在光标所在处插入表情
						etMess.getText().insert(etMess.getSelectionStart(), ss);
					}
				});

		// 初始化图片GridView
		gridViewPic = (GridView) findViewById(R.id.GV_picture);
		picAdapter = new GridAddPicAdapter(this, handler);
		gridViewPic.setAdapter(picAdapter);

		gridViewPic.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3)
			{// 当前activity并没有finish()(没有destroy,而是stop)
				if (arg2 == BitmapUtil.bmplist.size())
				{
					new PopupWindows(ReplyActivity.this, gridViewPic);
				}
				else
				{
					Intent intent = new Intent(ReplyActivity.this,
							EditPhotoActivity.class);
					intent.putExtra("ID", arg2);
					startActivity(intent);
				}
			}
		});

	}

	//设置listview的header
	// listview添加Header
	private void addHeader()
	{
		// 加载ListView的Header(所点击的主题帖内容)
		View view = getLayoutInflater().inflate(R.layout.header_reply, null);

		ImageView imvHeadimg = (ImageView) view
				.findViewById(R.id.IMVheader_reply_head);
		ImageView imvSex = (ImageView) view
				.findViewById(R.id.IMVheader_reply_sex);
		ImageView imvPic1 = (ImageView) view
				.findViewById(R.id.IMVheader_reply_pic1);
		ImageView imvPic2 = (ImageView) view
				.findViewById(R.id.IMVheader_reply_pic2);
		ImageView imvPic3 = (ImageView) view
				.findViewById(R.id.IMVheader_reply_pic3);

		TextView tvName = (TextView) view
				.findViewById(R.id.TVheader_reply_uname);
		TextView tvAge = (TextView) view.findViewById(R.id.TVheader_reply_uage);
		TextView tvTime = (TextView) view
				.findViewById(R.id.TVheader_reply_sendtime);
		TextView tvTitle = (TextView) view
				.findViewById(R.id.TVheader_reply_title);
		TextView tvContent = (TextView) view
				.findViewById(R.id.TVheader_reply_content);

		String[] picPath = { "null", "null", "null" };
		String picurl = json_tiopicinfo.getString("top_pic");
		if (!picurl.equals("null"))
		{
			String[] temp = picurl.split(",");
			for (int i = 0; i < temp.length; i++)
			{
				picPath[i] = temp[i];
			}
		}

		imvHeadimg.setImageResource(R.drawable.defaultimage);// 预设图片
		// 另起一个线程加载Header里的图片（先从SD卡中获取，SD卡没有再从网络加载）
		loadThread = new DownloadPicThread(
				json_tiopicinfo.getString("user_image"), imvHeadimg, handler);
		loadThread.start();

		// 设置主题帖图片
		if (!picPath[0].equals("null"))
		{
			imvPic1.setImageResource(R.drawable.default_picture);
			loadThread = new DownloadPicThread(picPath[0], imvPic1, handler);
			loadThread.start();
		}
		else
		{
			imvPic1.setVisibility(View.GONE);
		}
		if (!picPath[1].equals("null"))
		{
			imvPic2.setImageResource(R.drawable.default_picture);
			loadThread = new DownloadPicThread(picPath[1], imvPic2, handler);
			loadThread.start();
		}
		else
		{
			imvPic2.setVisibility(View.GONE);
		}
		if (!picPath[2].equals("null"))
		{
			imvPic3.setImageResource(R.drawable.default_picture);
			loadThread = new DownloadPicThread(picPath[2], imvPic3, handler);
			loadThread.start();
		}
		else
		{
			imvPic3.setVisibility(View.GONE);
		}

		// 设置sex图片
		if (json_tiopicinfo.getString("user_sex").equals("男"))
		{
			imvSex.setImageResource(R.drawable.sex_man);
		}
		else
		{
			imvSex.setImageResource(R.drawable.sex_woman);
		}

		tvName.setText(json_tiopicinfo.getString("user_name"));
		tvAge.setText(json_tiopicinfo.getString("user_age") + "岁");
		tvTime.setText(TimeChange.changeTime(json_tiopicinfo
				.getString("top_sendtime")));
		if (json_tiopicinfo.getString("top_title").equals("null"))
		{
			tvTitle.setVisibility(View.GONE);
		}
		else
		{
			tvTitle.setVisibility(View.VISIBLE);
			tvTitle.setText(json_tiopicinfo.getString("top_title"));
		}

		SpannableString spannable = GetEmotion.getEmotion(ReplyActivity.this,
				json_tiopicinfo.getString("top_content").toString());
		tvContent.setText(spannable);

		// 主题帖头像监听
		imvHeadimg.setOnClickListener(clicklistener);
		listview.addHeaderView(view, null, false);
	}

	private class ReplyAdapter extends BaseAdapter
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
				convertView = getLayoutInflater().inflate(R.layout.item_reply,
						null);
				viewholder = new ViewHolder();

				viewholder.imvHeadImg = (ImageView) convertView
						.findViewById(R.id.IMVreply_head);
				viewholder.imvSex = (ImageView) convertView
						.findViewById(R.id.IMVreply_sex);
				viewholder.tvSendTime = (TextView) convertView
						.findViewById(R.id.TVreply_sendtime);
				viewholder.tvFloor = (TextView) convertView
						.findViewById(R.id.TVreply_floor);
				viewholder.tvUserName = (TextView) convertView
						.findViewById(R.id.TVreply_uname);
				viewholder.tvUserAge = (TextView) convertView
						.findViewById(R.id.TVreply_uage);
				viewholder.tvContent = (TextView) convertView
						.findViewById(R.id.TVreply_content);
				viewholder.imvPic1 = (ImageView) convertView
						.findViewById(R.id.IMVreply_pic1);
				viewholder.imvPic2 = (ImageView) convertView
						.findViewById(R.id.IMVreply_pic2);
				viewholder.imvPic3 = (ImageView) convertView
						.findViewById(R.id.IMVreply_pic3);
				viewholder.imvZan = (ImageView) convertView
						.findViewById(R.id.IMVreply_zan);
				viewholder.tvZancount = (TextView) convertView
						.findViewById(R.id.TVreply_zancount);

				convertView.setTag(viewholder);// 设置标签
			}
			else
			{
				viewholder = (ViewHolder) convertView.getTag();// 根据标签得到视图
			}

			viewholder.tvUserName.setText(json_temp.getString("user_name"));
			viewholder.tvUserAge.setText(json_temp.getString("user_age") + "岁");
			// 主题帖内容设置表情
			SpannableString spannable = GetEmotion.getEmotion(
					ReplyActivity.this, json_temp.getString("rep_content"));
			viewholder.tvContent.setText(spannable);
			viewholder.tvFloor.setText(json_temp.getString("rep_floor") + " 楼");
			viewholder.tvSendTime.setText(TimeChange.changeTime(json_temp
					.getString("rep_time")));

			System.out.println(json_temp.getString("rep_time"));
			System.out.println(TimeChange.changeTime(json_temp
					.getString("rep_time")));

			if (json_temp.getString("rep_zancount").equals("0"))
			{
				viewholder.tvZancount.setText("赞");
			}
			else
			{
				viewholder.tvZancount.setText(json_temp
						.getString("rep_zancount"));
			}

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

				final String picPath = json_temp.getString("rep_pic");
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
					Intent intent = new Intent(ReplyActivity.this,
							LookUserInfoActivity.class);
					intent.putExtra("USERID", json_temp.getString("rep_userid"));
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
									String mess = NetTransUtil.addReplyZan(
											Constant.loginUid,
											json_temp.getString("rep_id"));
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
						intent = new Intent(ReplyActivity.this, LoginActivity.class);
						startActivity(intent);
					}
				}
			});

			return convertView;
		}

		private class ViewHolder
		{
			ImageView imvHeadImg, imvPic1, imvPic2, imvPic3, imvSex, imvZan;
			TextView tvUserName, tvUserAge, tvFloor, tvContent, tvZancount,
					tvSendTime;
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
		intent = new Intent(ReplyActivity.this, ScanPicActivity.class);
		intent.putExtra("POSITION", position);
		intent.putStringArrayListExtra("PATHLIST", picpathList);
		startActivity(intent);
	}

	private class ClickListener implements OnClickListener
	{
		@Override
		public void onClick(View v)
		{
			switch (v.getId())
				{
				case R.id.IMVreply_back:
					BitmapUtil.lastSize = 0;
					BitmapUtil.pathlist.clear();
					BitmapUtil.bmplist.clear();
					BitmapUtil.templist.clear();
					BitmapUtil.delTempPic();
					finish();
					break;
				case R.id.IMVheader_reply_head:
					Intent intent = new Intent(ReplyActivity.this,
							LookUserInfoActivity.class);
					intent.putExtra("USERID",
							json_tiopicinfo.getString("top_userid"));
					startActivity(intent);
					break;
				case R.id.IMVface:
					if (faceShow)
					{
						// 隐藏软键盘
						imm.hideSoftInputFromWindow(mFace.getWindowToken(), 0);
						gridViewPic.setVisibility(View.GONE);
						gridViewFace.setVisibility(View.VISIBLE);
						faceShow = false;
						picShow = true;
					}
					else
					{
						gridViewFace.setVisibility(View.GONE);
						imm.showSoftInput(etMess, 0);
						faceShow = true;
					}
					break;
				case R.id.ETmess:
					gridViewFace.setVisibility(View.GONE);
					gridViewPic.setVisibility(View.GONE);
					imm.showSoftInput(etMess, 0);
					faceShow = true;
					picShow = true;
					break;
				case R.id.IMVadd_pic:
					if (picShow)
					{
						gridViewFace.setVisibility(View.GONE);
						imm.hideSoftInputFromWindow(addPic.getWindowToken(), 0);
						gridViewPic.setVisibility(View.VISIBLE);
						picShow = false;
						faceShow = true;
					}
					else
					{
						gridViewPic.setVisibility(View.GONE);
						imm.showSoftInput(etMess, 0);
						picShow = true;
					}

					break;
				case R.id.BTsend_mess:
					if (Constant.isLogin)
					{
						if (CheckNetworkStatus
								.checkNetworkAvailable(ReplyActivity.this))
						{
							String content = etMess.getText().toString();
							content = content.replace("\n", " ");// 替换回车符号，否则构造json报错
							if (!TextUtils.isEmpty(content)
									&& content.length() > 15)
							{
								// 存储参数的map
								final Map<String, String> paraMap = new HashMap<String, String>();
								if (replybt.getVisibility() == View.VISIBLE)
								{// 回复回复帖
									paraMap.put("rep_topicid",
											Constant.currTopicId);
									paraMap.put("rep_answerid", answerid);
									paraMap.put("rep_userid", Constant.loginUid);
									paraMap.put("rep_answeruid", to_userid);// 回复帖子的用户ID
									paraMap.put("rep_content", replybt
											.getText().toString() + content);
								}
								else
								{// 回复主题帖
									paraMap.put("rep_topicid",
											Constant.currTopicId);
									paraMap.put("rep_answerid",
											Constant.currTopicId);
									paraMap.put("rep_userid", Constant.loginUid);

									// 主题帖的用户ID
									paraMap.put("rep_answeruid",
											json_tiopicinfo
													.getString("top_userid"));

									paraMap.put("rep_content", content);
								}

								if (rlloading.getVisibility() == View.GONE)
								{
									rlloading.setVisibility(View.VISIBLE);
								}
								new Thread()
								{

									@Override
									public void run()
									{
										try
										{
											String repid = NetTransUtil
													.publishRepTopic(paraMap);
											handler.obtainMessage(
													PUBLISH_REPLY, repid)
													.sendToTarget();
										}
										catch (Exception e)
										{
											e.printStackTrace();
										}

									}

								}.start();
							}
							else
							{
								Toast.makeText(ReplyActivity.this, "内容太少啦！！",
										Toast.LENGTH_SHORT).show();
							}
						}
						else
						{
							Toast.makeText(ReplyActivity.this,
									"网络不可用，请检查网络连接！", Toast.LENGTH_LONG).show();
						}
					}
					else
					{
						intent = new Intent(ReplyActivity.this, LoginActivity.class);
						startActivity(intent);
					}
					break;
				case R.id.IMVloadtip:
					// 去除提示
					if (rlloadstauts.getVisibility() == View.VISIBLE)
					{
						rlloadstauts.setVisibility(View.GONE);
					}

					if ((getReplyThread != null) && !getReplyThread.isAlive())
					{
						if (rlloading.getVisibility() == View.GONE)
						{
							rlloading.setVisibility(View.VISIBLE);
						}

						getReplyThread = new GetReplyThread();
						getReplyThread.start();
					}
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
							Thread.sleep(100);
							String json_new_reply = NetTransUtil
									.getNfRepOfTopic(
											Constant.currTopicId,
											"false",
											jarr_reply.getJSONObject(
													jarr_reply.size() - 1)
													.getString("rep_floor"));
							handler.obtainMessage(LOAD_PAGE, json_new_reply)
									.sendToTarget();
						}
						catch (Exception e)
						{
							String mess = e.getClass().getName();
							if (mess.contains("ConnectTimeoutException")
									|| mess.contains("ConnectException"))
							{
								handler.obtainMessage(DISPLAY_TOAST, "连接超时！")
										.sendToTarget();
							}
							else if (mess.contains("SocketTimeoutException"))
							{
								handler.obtainMessage(DISPLAY_TOAST, "读取数据超时！")
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

	//实现弹出框布局
	private class PopupWindows extends PopupWindow
	{

		@SuppressWarnings("deprecation")
		public PopupWindows(Context mContext, View parent)
		{

			View view = View
					.inflate(mContext, R.layout.item_popupwindows, null);
			view.startAnimation(AnimationUtils.loadAnimation(mContext,
					R.anim.fade_ins));
			LinearLayout ll_popup = (LinearLayout) view
					.findViewById(R.id.ll_popup);
			ll_popup.startAnimation(AnimationUtils.loadAnimation(mContext,
					R.anim.push_bottom_in_2));

			setWidth(LayoutParams.MATCH_PARENT);
			setHeight(LayoutParams.MATCH_PARENT);
			setBackgroundDrawable(new BitmapDrawable());
			setFocusable(true);
			setOutsideTouchable(true);
			setContentView(view);
			showAtLocation(parent, Gravity.BOTTOM, 0, 0);
			update();

			Button bt1 = (Button) view
					.findViewById(R.id.item_popupwindows_camera);
			Button bt2 = (Button) view
					.findViewById(R.id.item_popupwindows_Photo);
			Button bt3 = (Button) view
					.findViewById(R.id.item_popupwindows_cancel);
			bt1.setOnClickListener(new OnClickListener()
			{
				public void onClick(View v)
				{
					photo();
					dismiss();
				}
			});
			bt2.setOnClickListener(new OnClickListener()
			{
				public void onClick(View v)
				{
					Intent intent = new Intent(ReplyActivity.this,
							PicAlbumActivity.class);
					startActivity(intent);
					dismiss();
				}
			});
			bt3.setOnClickListener(new OnClickListener()
			{
				public void onClick(View v)
				{
					dismiss();
				}
			});

		}
	}

	//调用系统照相
	// 调用手机照相拍照
	public void photo()
	{
		Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
		File file = new File(Environment.getExternalStorageDirectory(),
				String.valueOf(System.currentTimeMillis()) + ".jpg");
		path = file.getPath();
		Uri imageUri = Uri.fromFile(file);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
		startActivityForResult(intent, TAKE_PHOTO);
	}

	//执行完startActivityForResult返回后调用 的方法
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		switch (requestCode)
			{
			case TAKE_PHOTO:
				if (BitmapUtil.pathlist.size() < BitmapUtil.maxCount
						&& resultCode == -1)
				{
					BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();  
					bitmapOptions.inSampleSize = 8;  
					File file = new File(path);  
					/** 
					 * 获取图片的旋转角度，有些系统把拍照的图片旋转了，有的没有旋转 
					 */  
					int degree = BitmapUtil.readPictureDegree(file.getAbsolutePath());  
					Bitmap cameraBitmap = BitmapFactory.decodeFile(path, bitmapOptions);
					Bitmap newbitmap = BitmapUtil.rotaingImageView(degree, cameraBitmap);  
					
					if (file.exists()) {
						file.delete();
					}
					try {
						FileOutputStream out = new FileOutputStream(
								file);
						newbitmap.compress(Bitmap.CompressFormat.PNG,
								100, out);
						out.flush();
						out.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					BitmapUtil.pathlist.add(path);
				}
				break;
			}
	}

	/**
	 * 加载回复帖线程
	 */
	private class GetReplyThread extends Thread
	{

		@Override
		public void run()
		{
			try
			{
				String reply = NetTransUtil.getNfRepOfTopic(
						Constant.currTopicId, "false", 1 + "");
				handler.obtainMessage(GET_REPLY, reply).sendToTarget();
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

	//拦截返回键，清除临时数据
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)
		{
			BitmapUtil.lastSize = 0;
			BitmapUtil.pathlist.clear();
			BitmapUtil.bmplist.clear();
			BitmapUtil.templist.clear();
			BitmapUtil.delTempPic();
		}

		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy()
	{
		BitmapUtil.lastSize = 0;
		BitmapUtil.pathlist.clear();
		BitmapUtil.bmplist.clear();
		BitmapUtil.templist.clear();
		BitmapUtil.delTempPic();
		super.onDestroy();
	}

	@Override
	protected void onStart()
	{
		if (gridViewPic != null)
		{
			picAdapter.update();
		}

		super.onStart();
	}

}
