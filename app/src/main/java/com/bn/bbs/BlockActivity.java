package com.bn.bbs;

import java.lang.ref.WeakReference;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.bn.thread.ImgAsyncDownload;
import com.bn.tour.R;
import com.bn.util.CheckNetworkStatus;
import com.bn.util.CheckUtil;
import com.bn.util.Constant;
import com.bn.util.NetTransUtil;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class BlockActivity extends Activity
{
	private static final int ITEMCOUTN = 10;//��������ÿ�μ��ص���Ŀ��
	private ImgAsyncDownload imgdownload;//�첽�����߳�
	private BlockAdapter adapter;//listview������
	private ScrollListener scrolistener;//��������
	private JSONObject json_block;//�洢block���ݵ�JSONObject
	private JSONArray jarr_block;//����JSONObject�õ���JSONArray��adapter������Դ��
	private EditText edt_search;
	private GetBlockThread getBlockThread;//��ȡblock��Ϣ�߳�
	private ListView listview;//չʾ��Ϣ��listview
	private View footer;//listview��footer��ͼ
	private LinearLayout footerLayout;//listview��footer����
	private boolean loading = false;// ���ڼ��ر�־λ
	private boolean hadTotalLoaded = false;// ������ɱ�־λ
	private long exitTime = 0;//��ʼ����һ�ΰ����ؼ���ʱ��
	private RelativeLayout rlloading, rlloadstauts;//���ڼ��صĻ��岼�ֺͼ��س�����ʾ��������״̬�Ĳ���
	private ImageView imvTip;//������ʾ����״̬��imageview
	private TextView tvtip;//��ʾ��������Ϣ
	private Clicklistener clicklistener;//�̳���OnclickListener���࣬���ڴ������¼�

	private Handler handler;//���ڴ����̼߳���Ϣ���ݵ�handler

	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_block);
		handler = new MyHandler(this);

		rlloading = (RelativeLayout) this.findViewById(R.id.RLblock_load);
		rlloading.setVisibility(View.VISIBLE);
		clicklistener = new Clicklistener();

		edt_search = (EditText) this.findViewById(R.id.ETsearch);
		footer = getLayoutInflater().inflate(R.layout.footer, null);
		footerLayout = (LinearLayout) footer.findViewById(R.id.LLfooter);

		getBlockThread = new GetBlockThread();
		getBlockThread.start();

		edt_search.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				Intent intent = new Intent();
				intent.setClass(BlockActivity.this, SearchBlockActivity.class);
				startActivity(intent);
			}
		});

	}

	private static class MyHandler extends Handler
	{
		WeakReference<BlockActivity> mActivity;

		public MyHandler(BlockActivity activity)
		{
			mActivity = new WeakReference<BlockActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg)
		{
			BlockActivity currActivity = mActivity.get();
			switch (msg.what)
				{
				case 0:
					if ((String) msg.obj == null)
					{
						currActivity.jarr_block = new JSONArray();
					}
					else
					{
						String block = (String) msg.obj;
						block = CheckUtil.replaceBlank(block);
						currActivity.json_block = JSONObject.fromObject(block);
						currActivity.jarr_block = currActivity.json_block
								.getJSONArray("results");
						if (currActivity.jarr_block.size() < ITEMCOUTN)
						{
							currActivity.hadTotalLoaded = true;
						}
					}
					currActivity.initBlock();
					break;
				case 1:
					String get_json = (String) msg.obj;
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
							currActivity.hadTotalLoaded = true;
						}
						String json2 = jarr_new.toString();
						currActivity.jarr_block.add(
								currActivity.jarr_block.size(),
								json2.substring(1, json2.length() - 1));
						currActivity.adapter.notifyDataSetChanged();
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
					currActivity.loading = false;
					break;
				case Constant.CONNECTIONTIMEOUT:
					if (currActivity.rlloading.getVisibility() == View.VISIBLE)
					{
						currActivity.rlloading.setVisibility(View.GONE);
					}
					currActivity.rlloadstauts = (RelativeLayout) currActivity
							.findViewById(R.id.RLblock_loadstauts);
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
							.findViewById(R.id.RLblock_loadstauts);
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

	//�������ϻ�ȡ��Ϣ�ɹ����ʼ��block�б�
	private void initBlock()
	{
		listview = (ListView) this.findViewById(R.id.LVblock);
		adapter = new BlockAdapter();
		listview.setAdapter(adapter);
		// û������ʱ�������ɱ�־λ����ֹ���س�footer
		if (jarr_block.size() == 0)
		{
			hadTotalLoaded = true;
		}
		scrolistener = new ScrollListener();
		listview.setOnScrollListener(scrolistener);

		listview.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					final int arg2, long arg3)
			{
				Constant.currBloId = jarr_block.getJSONObject(arg2).getString(
						"blo_id");
				Constant.currBlockInfo = jarr_block.getJSONObject(arg2);
				// �����ĵ������+1(Android)
				new Thread()
				{

					@Override
					public void run()
					{
						NetTransUtil.addBloCliCount(jarr_block.getJSONObject(
								arg2).getString("blo_id"));
					}

				}.start();
				Intent intent = new Intent();
				intent.setClass(BlockActivity.this, TopicActivity.class);
				BlockActivity.this.startActivity(intent);
			}
		});

		// ȥ�����ػ���ͼ��
		if (rlloading.getVisibility() == View.VISIBLE)
		{
			rlloading.setVisibility(View.GONE);
		}
	}

	private class BlockAdapter extends BaseAdapter
	{
		ViewHolder viewholder;

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
		public View getView(final int position, View convertView,
				ViewGroup parent)
		{
			final JSONObject json_temp = jarr_block.getJSONObject(position);
			// ����View
			if (convertView == null)
			{
				convertView = getLayoutInflater().inflate(R.layout.item_block,
						null);
				viewholder = new ViewHolder();

				viewholder.imvBloPic = (ImageView) convertView
						.findViewById(R.id.IMVblopic);
				viewholder.tvBloName = (TextView) convertView
						.findViewById(R.id.TVbloname);

				convertView.setTag(viewholder);// ���ñ�ǩ
			}
			else
			{
				viewholder = (ViewHolder) convertView.getTag();// ���ݱ�ǩ�õ���ͼ
			}

			viewholder.tvBloName.setText(json_temp.getString("blo_name"));

			imgdownload = ImgAsyncDownload.getInstance();

			if (imgdownload != null)
			{
				// �첽����ͼƬ
				viewholder.imvBloPic.setTag(json_temp.getString("blo_pic"));
				viewholder.imvBloPic.setImageResource(R.drawable.defaultblockimg);
				imgdownload.imageDownload(viewholder.imvBloPic);
			}

			viewholder.imvBloPic.setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View v)
				{
					Intent intent = new Intent(BlockActivity.this,
							BlockInfoActivity.class);
					intent.putExtra("READTYPE", true);
					intent.putExtra("BLOCKID", json_temp.getString("blo_id"));
					startActivity(intent);
				}
			});

			return convertView;
		}

		private class ViewHolder
		{
			ImageView imvBloPic;
			TextView tvBloName;
		}

	}

	//����block��Ϣ�߳�
	private class GetBlockThread extends Thread
	{

		@Override
		public void run()
		{
			try
			{
				String block = NetTransUtil.getNfBlockInfo(0, "false");
				handler.obtainMessage(0, block).sendToTarget();
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

			if ((getBlockThread != null) && !getBlockThread.isAlive())
			{
				if (rlloading.getVisibility() == View.GONE)
				{
					rlloading.setVisibility(View.VISIBLE);
				}

				getBlockThread = new GetBlockThread();
				getBlockThread.start();
			}
		}

	}

	//�������������ڷ������أ�
	private class ScrollListener implements OnScrollListener
	{

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount)
		{
			final int totalcount = totalItemCount;
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
							Thread.sleep(3000);
							String json_new_block = NetTransUtil
									.getNfBlockInfo(totalcount / ITEMCOUTN,
											"false");
							handler.obtainMessage(1, json_new_block)
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
	protected void onSaveInstanceState(Bundle outState)
	{
		// TODO Auto-generated method stub
		// super.onSaveInstanceState(outState);
	}

	//��д���ؼ���ʵ�ְ������˳�����
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
