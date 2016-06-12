package com.bn.travelnote;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.SparseIntArray;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bn.thread.DownloadPicThread;
import com.bn.thread.ImgAsyncDownload;
import com.bn.tour.R;
import com.bn.tour.ScanPicActivity;
import com.bn.user.LoginActivity;
import com.bn.user.LookUserInfoActivity;
import com.bn.util.CheckUtil;
import com.bn.util.Constant;
import com.bn.util.NetTransUtil;

public class NoteActivity extends Activity
{
	private static final int GET_NOTE = 0;//获取游记
	private static final int UPDATE_PIC = 1;//更新图片个数
	private static final int ADD_ZAN = 2;//点赞
	private static final int ITEMCOUTN = 5;//每次加载条目数
	private static final int LOAD_PAGE = 3;//分批加载
	private ListView listview;
	private NoteAdapter noteadapter;
	private ImgAsyncDownload imgdownload;//异步加载图片线程（listview内图片的加载）
	private DownloadPicThread loadThread;
	private JSONObject json_note, json_tratinfo;
	private JSONArray jarr_note;
	private GetNoteThread getNoteThread;
	private TextView tvPiccount, tvZancount;
	private ScrollListener scrolllistener;
	private View footer;

	private LinearLayout footerLayout;

	private boolean loading = false;
	private boolean hadTotalLoaded = false;

	private Intent intent;

	private ArrayList<String> picpathList = new ArrayList<String>();
	private SparseIntArray sparse = new SparseIntArray();

	private ClickListener clickListener = new ClickListener();

	private Handler handler;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_note);
		handler = new MyHandler(this);
		json_tratinfo = JSONObject.fromObject(Constant.currTratInfo);

		getNoteThread = new GetNoteThread();
		getNoteThread.start();
	}

	private static class MyHandler extends Handler
	{
		WeakReference<NoteActivity> mActivity;

		public MyHandler(NoteActivity activity)
		{
			mActivity = new WeakReference<NoteActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg)
		{
			NoteActivity currActivity = mActivity.get();
			switch (msg.what)
				{
				case GET_NOTE:
					String note = (String) msg.obj;
					if (note == null)
					{
						currActivity.jarr_note = new JSONArray();
						currActivity.hadTotalLoaded = true;
					}
					else
					{
						note = CheckUtil.replaceBlank(note);
						currActivity.json_note = JSONObject.fromObject(note);
						currActivity.jarr_note = currActivity.json_note
								.getJSONArray("results");
						if (currActivity.jarr_note.size() < ITEMCOUTN)
						{
							currActivity.hadTotalLoaded = true;
						}
					}
					currActivity.initView();
					break;
				case UPDATE_PIC:
					currActivity.tvPiccount.setText("图片（"
							+ currActivity.picpathList.size() + "）");
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
						get_json = CheckUtil.replaceBlank(get_json);// 替换回车符号，否则构造json报错
						JSONObject json_new = JSONObject.fromObject(get_json);
						JSONArray jarr_new = json_new.getJSONArray("results");

						if (jarr_new.size() < ITEMCOUTN)
						{
							currActivity.hadTotalLoaded = true;// 取得的数据小于一页
																// 的条数，全部加载完成
						}
						String json1 = currActivity.jarr_note.toString();
						String json2 = jarr_new.toString();
						// 组装新的json,不能使用add方法，只能add一个元素
						String newjson = json1.substring(0, json1.length() - 1)
								+ "," + json2.substring(1, json2.length());
						currActivity.jarr_note = JSONArray.fromObject(newjson);
						// 此时adapter已经自动更新了，用noteadapter.getCount()方法想获取上次的条目数不成功，得到的是当前已更新的数目

						currActivity.noteadapter.notifyDataSetChanged();
						currActivity.loadPicpathThread(
								currActivity.noteadapter.getCount()
										- jarr_new.size(),
								currActivity.jarr_note);// 更新图片个数
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
									.parseInt(currActivity.json_tratinfo
											.getString("tratop_zancount"))
									+ 1 + "";
							currActivity.json_tratinfo.put("tratop_zancount",
									zancount);// 改变数据源

							currActivity.tvZancount.setText(zancount);

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

	private void initView()
	{
		listview = (ListView) this.findViewById(R.id.LVnote);
		this.addHeader();
		this.loadPicpathThread(0, jarr_note);

		footer = getLayoutInflater().inflate(R.layout.footer, null);
		footerLayout = (LinearLayout) footer.findViewById(R.id.LLfooter);

		noteadapter = new NoteAdapter();
		listview.setAdapter(noteadapter);
		// 滑动监听（分批加载）
		scrolllistener = new ScrollListener();
		listview.setOnScrollListener(scrolllistener);

	}

	public void addHeader()
	{
		// 获得Header的View
		View view = getLayoutInflater().inflate(R.layout.header_note, null);

		ImageView imvHeadPic = (ImageView) view
				.findViewById(R.id.IMVheader_trat_pic);
		ImageView imvHeadimg = (ImageView) view
				.findViewById(R.id.IMVheader_trat_head);
		ImageView imvZan = (ImageView) view
				.findViewById(R.id.IMVheader_trat_zan);
		ImageView imvSex = (ImageView) view
				.findViewById(R.id.IMVheader_trat_usex);

		TextView tvUname = (TextView) view
				.findViewById(R.id.TVheader_trat_uname);
		TextView tvBrowcount = (TextView) view
				.findViewById(R.id.TVheader_trat_browcount);
		tvZancount = (TextView) view.findViewById(R.id.TVheader_trat_zancount);
		TextView tvTime = (TextView) view
				.findViewById(R.id.TVheader_trat_createtime);
		TextView tvAge = (TextView) view.findViewById(R.id.TVheader_trat_uage);
		TextView tvTratname = (TextView) view
				.findViewById(R.id.TVheader_tratop_name);
		TextView tvTratplace = (TextView) view
				.findViewById(R.id.TVheader_trat_place);
		tvPiccount = (TextView) view.findViewById(R.id.TVheader_pic_count);

		// 背景图片
		if (!json_tratinfo.getString("tratop_pic").equals("null"))
		{
			loadThread = new DownloadPicThread(
					json_tratinfo.getString("tratop_pic"), imvHeadPic, handler);
			loadThread.start();
		}
		imvHeadimg.setImageResource(R.drawable.defaultimage);// 预设图片
		// 另起一个线程加载Header里的图片（先从SD卡中获取，SD卡没有再从网络加载）
		loadThread = new DownloadPicThread(
				json_tratinfo.getString("user_image"), imvHeadimg, handler);
		loadThread.start();

		tvUname.setText(json_tratinfo.getString("user_name"));
		tvBrowcount
				.setText(json_tratinfo.getString("tratop_browcount") + "次阅读");

		if (json_tratinfo.getString("tratop_zancount").equals("0"))
		{
			tvZancount.setText("赞");
		}
		else
		{
			tvZancount.setText(json_tratinfo.getString("tratop_zancount"));
		}
		// 设置sex图片
		if (json_tratinfo.getString("user_sex").equals("男"))
		{
			imvSex.setImageResource(R.drawable.sex_man);
		}
		else
		{
			imvSex.setImageResource(R.drawable.sex_woman);
		}

		tvAge.setText(json_tratinfo.getString("user_age") + "岁");
		tvTime.setText(json_tratinfo.getString("tratop_createdate").substring(
				0, 10));
		tvTratname.setText(json_tratinfo.getString("tratop_name"));
		tvTratplace.setText(json_tratinfo.getString("tratop_place"));
		/*
		 * 添加监听
		 */
		imvHeadimg.setOnClickListener(clickListener);
		imvZan.setOnClickListener(clickListener);
		tvPiccount.setOnClickListener(clickListener);

		// 给ListView添加Header
		listview.addHeaderView(view, null, false);
	}

	/**
	 * 随着滑动，动态更新主题中图片的数目，将所有的图片URL放到picpathList中，点击预览时作为参数传过去
	 * map中记录的是图片所在的item的position与在所有图片中的索引
	 * 
	 * @param position开始扫描的位置
	 *            （对应item的position）
	 * @param jarr
	 *            （此次扫描的数据源）
	 */
	public void loadPicpathThread(final int position, final JSONArray jarr)
	{
		new Thread()
		{
			public void run()
			{
				for (int i = position; i < jarr.size(); i++)
				{
					if (!jarr.getJSONObject(i).getString("note_pic")
							.equals("null"))
					{
						picpathList.add(jarr.getJSONObject(i).getString(
								"note_pic"));
						sparse.put(i, picpathList.size() - 1);// 对图片进行排序（根据position）
					}
				}

				handler.sendEmptyMessage(UPDATE_PIC);
			}
		}.start();
	}

	// ListView的BaseAdapter适配器
	private class NoteAdapter extends BaseAdapter
	{
		private ViewHolder viewholder;

		@Override
		public int getCount()
		{
			return jarr_note.size();
		}

		@Override
		public Object getItem(int position)
		{
			return jarr_note.get(position);
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
			JSONObject json_temp = jarr_note.getJSONObject(position);// 取得每个item的json数据

			// 重用View
			if (convertView == null)
			{
				convertView = getLayoutInflater().inflate(R.layout.item_note,
						null);
				viewholder = new ViewHolder();

				viewholder.imvPic = (ImageView) convertView
						.findViewById(R.id.IMVnote_pic);
				viewholder.tvCreateTime = (TextView) convertView
						.findViewById(R.id.TVnote_time);

				viewholder.tvContent = (TextView) convertView
						.findViewById(R.id.TVnote_content);
				viewholder.tvPlace = (TextView) convertView
						.findViewById(R.id.TVnote_place);

				convertView.setTag(viewholder);// 设置标签
			}
			else
			{
				viewholder = (ViewHolder) convertView.getTag();// 根据标签得到视图
			}

			viewholder.tvCreateTime.setText(json_temp.getString("note_date")
					.substring(0, 10));// time
			viewholder.tvPlace.setText(json_temp.getString("note_place"));// place
			viewholder.tvContent.setText(json_temp.getString("note_content"));// content

			String picUrl = json_temp.getString("note_pic");
			if (!picUrl.equals("null"))
			{
				viewholder.imvPic.setVisibility(View.VISIBLE);// 将ImageView设为可见

				imgdownload = ImgAsyncDownload.getInstance();

				if (imgdownload != null)
				{
					// 异步下载图片
					// ImageView设置tag,图片异步加载完成后，根据tag找到相应ImageView设置图片，防止错乱
					viewholder.imvPic.setTag(picUrl);
					// 一定要先设置默认图片再去加载，否则还会出现错乱
					viewholder.imvPic
							.setImageResource(R.drawable.default_picture);
					imgdownload.imageDownload(viewholder.imvPic);
				}

				viewholder.imvPic.setOnClickListener(new OnClickListener()
				{

					@Override
					public void onClick(View v)
					{
						intent = new Intent(NoteActivity.this,
								ScanPicActivity.class);
						intent.putExtra("POSITION", sparse.get(position));
						intent.putStringArrayListExtra("PATHLIST", picpathList);
						startActivity(intent);
					}
				});
			}
			else
			{
				viewholder.imvPic.setVisibility(View.GONE);
			}

			return convertView;
		}

		private class ViewHolder
		{
			ImageView imvPic;
			TextView tvCreateTime, tvContent, tvPlace;
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
							String json_new_topic = NetTransUtil
									.getNfNoteOfTrat(
											Constant.currTratopId,
											"false",
											jarr_note.getJSONObject(
													jarr_note.size() - 1)
													.getString("note_id"));
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

	// OnClickListener点击监听类
	private class ClickListener implements OnClickListener
	{
		@Override
		public void onClick(View v)
		{
			switch (v.getId())
				{
				case R.id.IMVheader_trat_head:
					Intent intent = new Intent(NoteActivity.this,
							LookUserInfoActivity.class);
					intent.putExtra("USERID",
							json_tratinfo.getString("tratop_userid"));
					startActivity(intent);
					break;
				case R.id.IMVheader_trat_zan:
					if (Constant.isLogin)
					{
						new Thread()
						{

							@Override
							public void run()
							{

								try
								{
									String mess = NetTransUtil
											.addTratZan(
													Constant.loginUid,
													json_tratinfo
															.getString("tratop_id"),
													json_tratinfo
															.getString("tratop_userid"));
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
						intent = new Intent(NoteActivity.this, LoginActivity.class);
						startActivity(intent);
					}
					break;
				case R.id.TVheader_pic_count:
					if (picpathList.size() != 0)
					{
						intent = new Intent(NoteActivity.this,
								ScanPicActivity.class);
						intent.putExtra("POSITION", 0);
						intent.putStringArrayListExtra("PATHLIST", picpathList);
						startActivity(intent);
					}
					break;
				}

		}
	}

	private class GetNoteThread extends Thread
	{

		@Override
		public void run()
		{
			// TODO Auto-generated method stub
			try
			{
				String note = NetTransUtil.getNfNoteOfTrat(
						Constant.currTratopId, "false", 0 + "");
				handler.obtainMessage(GET_NOTE, note).sendToTarget();
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
