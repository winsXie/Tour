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
	private static final int GET_REPLY = 0;//��ȡ�ظ�����handler��whatֵ��
	private static final int UPDATE_PIC = 1;//֪ͨGridAddPicAdapter���ݸı�
	private static final int UPDATE_REPLY = 2;//���»ظ������ڽ����лظ���ȫ�����غ��ô˷������£�
	private static final int LOAD_PAGE = 3;//��������
	private static final int PUBLISH_REPLY = 4;//����ظ���
	private static final int ADD_ZAN = 5;//Ϊ�ظ�������
	private static final int DISPLAY_TOAST = 6;//չʾtoast��Ϣ
	private static final int TAKE_PHOTO = 0x000000;//����ϵͳ���չ���
	private int floor;//�ظ�������¥�㣨������ת��ָ��¥�㣩
	private String path = "";//���պ��ͼƬ·��
	private ListView listview;
	private ReplyAdapter replyadapter;
	private ImgAsyncDownload imgdownload;//�첽����ͼƬ�̣߳�listview��ͼƬ�ļ��أ�
	private DownloadPicThread loadThread;//�첽����ͼƬ�̣߳���listview��ͼƬ�ļ��أ�
	private GetReplyThread getReplyThread;//��ȡ�ظ����߳�
	private JSONObject json_reply, json_tiopicinfo;
	private JSONArray jarr_reply;//����JSONObject�õ���JSONArray
	private View footer;//listview��footerview

	private LinearLayout footerLayout;//footer����
	private ScrollListener scrolllistener;//�������������ڷ������أ�
	private static final int ITEMCOUTN = 5;//��������ÿ�μ��ص���Ŀ��
	private boolean loading = false;// ���ڼ��ر�־λ
	private boolean hadTotalLoaded = false;// ������ɱ�־λ
	private boolean isToLast = false;// �Ƿ�Ҫһֱ���ص��ײ���־λ�����ڻظ���ˢ�½��棬һֱ�������һ�㣩
	private boolean isToTarget = false;//�Ƿ�Ҫ������ָ��λ�ã����ҵĻظ�����������Ӧ�Ļظ���ֱ�ӻ�����ָ��¥�㣩
	private Intent intent;
	private int zanPosition;//����item��λ��

	private String answerid;// ���ظ������ӵ�ID����������������Ҳ����Ϊ�ظ���
	private String to_userid;// ���ظ����ӵ��û�ID
	private ClickListener clicklistener;

	private EditText etMess;//�ظ�����
	private Button replybt;//��ʾ�ظ�¥���button
	private ImageView mFace;
	private InputMethodManager imm;//����̹���
	private GridViewFaceAdapter mGVFaceAdapter;//����������
	private GridView gridViewFace, gridViewPic;
	private Button buSendMess;//���Ͱ�ť
	private ImageView addPic;
	private boolean faceShow = true;//�Ƿ���ʾ����
	private boolean picShow = true;//�Ƿ���ʾѡ���ͼƬ
	private GridAddPicAdapter picAdapter;//���ͼƬ������
	private RelativeLayout rlloading, rlloadstauts;
	private ImageView imvTip;//������ʾ����״̬��imageview
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

		BitmapUtil.maxCount = 3;// �����ܹ�����ͼƬ���������

		// ����̹�����
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
					currActivity.initView();// ��ʼ��listview����ʱjarr_reply�Ѿ��ǿ�
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

					// ȥ�����ػ���
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

					currActivity.loading = false;// ���ñ�־λ
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
						{// ����¥����ڻظ�����������¥�㱻ɾ����
							/*
							 * ���Բ�ȡ��isToTarget��־λ���������ֻ�����ֿ��ܣ�
							 * ��һ�֣���δ���ص�����¥�㣬��Ҫ�������أ�����������������
							 * �ڶ��֣���ȫ�����أ�������¥����ڻظ�����������¥�㱻ɾ�����������û�
							 */
							currActivity.listview
									.setSelection(currActivity.replyadapter
											.getCount() - 1);
						}
						else
						{
							currActivity.listview
									.setSelection(currActivity.floor);
							currActivity.isToTarget = false;// ȡ��������ָ��λ�ñ�־λ
						}
					}
					break;
				case PUBLISH_REPLY:
					final String repid = (String) msg.obj;// ���صĸշ���Ļظ���ID
					if (repid != null)
					{
						Toast.makeText(currActivity, "����ɹ���",
								Toast.LENGTH_SHORT).show();
						// ��ո�����ʱ�洢
						BitmapUtil.lastSize = 0;
						BitmapUtil.pathlist.clear();
						BitmapUtil.bmplist.clear();
						BitmapUtil.templist.clear();
						BitmapUtil.delTempPic();
						currActivity.etMess.setText("");
						if (currActivity.imm.isActive(currActivity.etMess))
						{// ���������
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
						 * �ظ���ɺ�Ҫ���»ظ������������ظ���ɺ����ڵ�¥�� ��Ϊ�漰���������أ�Ҫ���ǻظ�ʱ�Ƿ��Ѿ�ȫ��������ɣ�
						 * ��ȫ��������ɣ�hadTotalLoaded����Ϊtrue��ֻ���ȡ�ղŻظ������ݣ�
						 * �ӵ�json��notifyDataSetChanged�󣬹����� ���һ�����ɣ����ᴥ����������
						 * �����Ҫ���س����еĻظ���
						 * ���ᴥ���������أ�����Ϊ�Զ��������أ����ոջظ��ľ������һ�㣬�����ٵ�����ȡ��Ϣ
						 */
						if (!currActivity.hadTotalLoaded)
						{// δȫ�����أ�Ҫһֱ���ص����һ�����ᴥ���������أ���Ϊ��ʱhadTotalLoadedΪfalse��
							currActivity.isToLast = true;//���ñ�־λ��ֱ�ӻ����ײ�
							//������ʽû��ʵ�ֻ������ܣ�ֻ���ô˷���
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

							// ȥ�����ػ���
							if (currActivity.rlloading.getVisibility() == View.VISIBLE)
							{
								currActivity.rlloading.setVisibility(View.GONE);
							}
						}
						else
						{// ��ȫ������
							new Thread()
							{

								@Override
								public void run()
								{
									try
									{// ��ȡ�ղŻظ���������Ϣ
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
						// ȥ�����ػ���
						if (currActivity.rlloading.getVisibility() == View.VISIBLE)
						{
							currActivity.rlloading.setVisibility(View.GONE);
						}
						Toast.makeText(currActivity, "����ʧ�ܣ�������",
								Toast.LENGTH_SHORT).show();
					}
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
									.parseInt(currActivity.jarr_reply
											.getJSONObject(
													currActivity.zanPosition)
											.getString("rep_zancount"))
									+ 1 + "";
							currActivity.jarr_reply.getJSONObject(
									currActivity.zanPosition).put(
									"rep_zancount", zancount);// �ı�����Դ

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
						Toast.makeText(currActivity, "���ݴ���ʧ�ܣ�",
								Toast.LENGTH_LONG).show();
					}
					break;
				case DISPLAY_TOAST:
					// �ڷ������أ������ɹ��󷵻أ��ᷢ�ʹ���Ϣ
					String toast = (String) msg.obj;
					if (toast != null)
					{
						Toast.makeText(currActivity, toast, Toast.LENGTH_SHORT)
								.show();
					}
					// ȥ�����ػ���
					if (currActivity.rlloading.getVisibility() == View.VISIBLE)
					{
						currActivity.rlloading.setVisibility(View.GONE);
					}
					// �϶���û�м����꣬ȥ��footer
					if (currActivity.listview.getFooterViewsCount() > 0)
					{
						currActivity.listview
								.removeFooterView(currActivity.footer);
					}
					currActivity.loading = false;// ���ñ�־λ
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
							.findViewById(R.id.RLreply_loadstauts);
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
			super.handleMessage(msg);
		}
	}

	/**
	 * ��ʼ��listview
	 */
	private void initView()
	{
		listview = (ListView) this.findViewById(R.id.LVreply);

		json_tiopicinfo = JSONObject.fromObject(Constant.currTopicInfo);
		answerid = json_tiopicinfo.getString("top_id");// ��Ĭ��Ϊ�ظ�������
		to_userid = json_tiopicinfo.getString("top_userid");
		ImageView imvBack = (ImageView) this.findViewById(R.id.IMVreply_back);

		this.addHeader();

		footer = getLayoutInflater().inflate(R.layout.footer, null);
		footerLayout = (LinearLayout) footer.findViewById(R.id.LLfooter);

		replyadapter = new ReplyAdapter();
		listview.setAdapter(replyadapter);// ����������

		if (floor != 0)
		{
			if (floor >= replyadapter.getCount())
			{// ����¥����ڻظ�����������¥�㱻ɾ����
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
						"rep_id");// �����ظ������ӵ�ID��Ϊ��ǰ�ظ�����ID
				to_userid = jarr_reply.getJSONObject(arg2 - 1).getString(
						"rep_userid");// �����ظ������ӵ��û�ID��Ϊ��ǰ�ظ������û�ID

				replybt.setVisibility(View.VISIBLE);
				replybt.setText("�ظ�"
						+ jarr_reply.getJSONObject(arg2 - 1).getString(
								"rep_floor") + "¥��");
				etMess.setHint("");

				etMess.requestFocus();
				etMess.setSelection(etMess.getText().length());
				gridViewFace.setVisibility(View.GONE);
				gridViewPic.setVisibility(View.GONE);
				imm.showSoftInput(etMess, InputMethodManager.SHOW_FORCED);
			}
		});

		// û������ʱ�������ɱ�־λ����ֹ���س�footer
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
						etMess.setHint("�ظ�¥����");
					}
				}
				return false;
			}
		});

		// ȥ�����ػ���ͼ��
		if (rlloading.getVisibility() == View.VISIBLE)
		{
			rlloading.setVisibility(View.GONE);
		}
	}

	/**
	 * ��ʼ�������ͼƬGridView
	 */
	private void initGridView()
	{
		// ��ʼ������ؼ�
		mGVFaceAdapter = new GridViewFaceAdapter(this);
		gridViewFace = (GridView) findViewById(R.id.GV_faces);
		gridViewFace.setAdapter(mGVFaceAdapter);
		gridViewFace
				.setOnItemClickListener(new AdapterView.OnItemClickListener()
				{
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id)
					{
						// ����ı���
						SpannableString ss = new SpannableString(view.getTag()
								.toString());
						Drawable d = getResources().getDrawable(
								(int) mGVFaceAdapter.getItemId(position));
						d.setBounds(0, 0, 70, 70);// ���ñ���ͼƬ����ʾ��С
						ImageSpan span = new ImageSpan(d,
								ImageSpan.ALIGN_BOTTOM);
						ss.setSpan(span, 0, view.getTag().toString().length(),
								Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						// �ڹ�����ڴ��������
						etMess.getText().insert(etMess.getSelectionStart(), ss);
					}
				});

		// ��ʼ��ͼƬGridView
		gridViewPic = (GridView) findViewById(R.id.GV_picture);
		picAdapter = new GridAddPicAdapter(this, handler);
		gridViewPic.setAdapter(picAdapter);

		gridViewPic.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3)
			{// ��ǰactivity��û��finish()(û��destroy,����stop)
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

	//����listview��header
	// listview���Header
	private void addHeader()
	{
		// ����ListView��Header(�����������������)
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

		imvHeadimg.setImageResource(R.drawable.defaultimage);// Ԥ��ͼƬ
		// ����һ���̼߳���Header���ͼƬ���ȴ�SD���л�ȡ��SD��û���ٴ�������أ�
		loadThread = new DownloadPicThread(
				json_tiopicinfo.getString("user_image"), imvHeadimg, handler);
		loadThread.start();

		// ����������ͼƬ
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

		// ����sexͼƬ
		if (json_tiopicinfo.getString("user_sex").equals("��"))
		{
			imvSex.setImageResource(R.drawable.sex_man);
		}
		else
		{
			imvSex.setImageResource(R.drawable.sex_woman);
		}

		tvName.setText(json_tiopicinfo.getString("user_name"));
		tvAge.setText(json_tiopicinfo.getString("user_age") + "��");
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

		// ������ͷ�����
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
			// ����View
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

				convertView.setTag(viewholder);// ���ñ�ǩ
			}
			else
			{
				viewholder = (ViewHolder) convertView.getTag();// ���ݱ�ǩ�õ���ͼ
			}

			viewholder.tvUserName.setText(json_temp.getString("user_name"));
			viewholder.tvUserAge.setText(json_temp.getString("user_age") + "��");
			// �������������ñ���
			SpannableString spannable = GetEmotion.getEmotion(
					ReplyActivity.this, json_temp.getString("rep_content"));
			viewholder.tvContent.setText(spannable);
			viewholder.tvFloor.setText(json_temp.getString("rep_floor") + " ¥");
			viewholder.tvSendTime.setText(TimeChange.changeTime(json_temp
					.getString("rep_time")));

			System.out.println(json_temp.getString("rep_time"));
			System.out.println(TimeChange.changeTime(json_temp
					.getString("rep_time")));

			if (json_temp.getString("rep_zancount").equals("0"))
			{
				viewholder.tvZancount.setText("��");
			}
			else
			{
				viewholder.tvZancount.setText(json_temp
						.getString("rep_zancount"));
			}

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
						// ���������
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
							content = content.replace("\n", " ");// �滻�س����ţ�������json����
							if (!TextUtils.isEmpty(content)
									&& content.length() > 15)
							{
								// �洢������map
								final Map<String, String> paraMap = new HashMap<String, String>();
								if (replybt.getVisibility() == View.VISIBLE)
								{// �ظ��ظ���
									paraMap.put("rep_topicid",
											Constant.currTopicId);
									paraMap.put("rep_answerid", answerid);
									paraMap.put("rep_userid", Constant.loginUid);
									paraMap.put("rep_answeruid", to_userid);// �ظ����ӵ��û�ID
									paraMap.put("rep_content", replybt
											.getText().toString() + content);
								}
								else
								{// �ظ�������
									paraMap.put("rep_topicid",
											Constant.currTopicId);
									paraMap.put("rep_answerid",
											Constant.currTopicId);
									paraMap.put("rep_userid", Constant.loginUid);

									// ���������û�ID
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
								Toast.makeText(ReplyActivity.this, "����̫��������",
										Toast.LENGTH_SHORT).show();
							}
						}
						else
						{
							Toast.makeText(ReplyActivity.this,
									"���粻���ã������������ӣ�", Toast.LENGTH_LONG).show();
						}
					}
					else
					{
						intent = new Intent(ReplyActivity.this, LoginActivity.class);
						startActivity(intent);
					}
					break;
				case R.id.IMVloadtip:
					// ȥ����ʾ
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
								handler.obtainMessage(DISPLAY_TOAST, "���ӳ�ʱ��")
										.sendToTarget();
							}
							else if (mess.contains("SocketTimeoutException"))
							{
								handler.obtainMessage(DISPLAY_TOAST, "��ȡ���ݳ�ʱ��")
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

	//ʵ�ֵ����򲼾�
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

	//����ϵͳ����
	// �����ֻ���������
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

	//ִ����startActivityForResult���غ���� �ķ���
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
					 * ��ȡͼƬ����ת�Ƕȣ���Щϵͳ�����յ�ͼƬ��ת�ˣ��е�û����ת 
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
	 * ���ػظ����߳�
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

	//���ط��ؼ��������ʱ����
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
