package com.bn.bbs;

import java.io.File;
import java.lang.ref.WeakReference;

import net.sf.json.JSONObject;

import com.bn.thread.DownloadPicThread;
import com.bn.tour.R;
import com.bn.util.BitmapUtil;
import com.bn.util.Constant;
import com.bn.util.NetTransUtil;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class BlockInfoActivity extends Activity
{
	private static final int QUERY_BLOCK = 0;//��ѯ�����Ϣ������handlerʱ��whatֵ��
	private static final int EDIT_BLOCK = 1;//�༭�����Ϣ
	private TextView tvBack, tvOk, tvBloname, tvBloclicou, tvTopiccou, tvDate,
			tvChangepic;
	private EditText etIntro;
	private ImageView bloPic;
	private boolean onlyRead = true;//���ö�д���ͱ�־λ���Ƿ�Ϊֻ����
	private boolean enEdit = false;//�Ƿ����ñ༭��־λ
	private ClickListener listener;
	private String blockid;//��ǰ����ѯ��blockid
	private GetBlockInfoThread getBlock;//��ȡblock��Ϣ�߳�

	private File temp = new File(Environment.getExternalStorageDirectory()
			+ "/tourTemp.jpg");//�洢����ͼƬ��·��

	private JSONObject json_bloinfo;//�洢block��Ϣ��JSONObject

	private Handler handler;//�����̼߳�ͨ�ŵ�handler

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_blockinfo);
		handler = new MyHandler(this);

		onlyRead = getIntent().getBooleanExtra("READTYPE", true);
		blockid = getIntent().getStringExtra("BLOCKID");

		getBlock = new GetBlockInfoThread();
		getBlock.start();
		tvBack = (TextView) this.findViewById(R.id.TVbloinfo_back);
		listener = new ClickListener();
		tvBack.setOnClickListener(listener);
	}

	private static class MyHandler extends Handler
	{
		WeakReference<BlockInfoActivity> mActivity;

		public MyHandler(BlockInfoActivity activity)
		{
			mActivity = new WeakReference<BlockInfoActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg)
		{
			BlockInfoActivity currActivity = mActivity.get();
			switch (msg.what)
				{
				case QUERY_BLOCK:
					String blockinfo = (String) msg.obj;
					if (blockinfo == null)
					{
						Toast.makeText(currActivity, "��ѯ��Ϣʧ�ܣ�",
								Toast.LENGTH_LONG).show();
					}
					else
					{
						currActivity.json_bloinfo = JSONObject
								.fromObject(blockinfo);
						currActivity.initView();
					}
					break;
				case EDIT_BLOCK:
					if ((Boolean) msg.obj)
					{
						BitmapUtil.lastSize = 0;
						BitmapUtil.pathlist.clear();
						BitmapUtil.bmplist.clear();
						BitmapUtil.templist.clear();
						BitmapUtil.delTempPic();

						Toast.makeText(currActivity, "�޸ĳɹ���", Toast.LENGTH_LONG)
								.show();
						currActivity.finish();
					}
					else
					{
						Toast.makeText(currActivity, "�޸�ʧ�ܣ�", Toast.LENGTH_LONG)
								.show();
					}
					break;
				case Constant.CONNECTIONTIMEOUT:
					Toast.makeText(currActivity, "���ӳ�ʱ��", Toast.LENGTH_LONG)
							.show();
					break;
				case Constant.SOTIMEOUT:
					Toast.makeText(currActivity, "��ȡ���ݳ�ʱ��", Toast.LENGTH_LONG)
							.show();
					break;
				}

		}
	}

	//��ȡ��Ϣ�ɹ����ʼ��iew
	private void initView()
	{
		tvOk = (TextView) this.findViewById(R.id.TVbloinfo_ok);
		tvOk.setOnClickListener(listener);
		if (!onlyRead)
		{
			tvOk.setText("�༭");
		}

		tvBloname = (TextView) this.findViewById(R.id.TVbloname);
		tvBloclicou = (TextView) this.findViewById(R.id.TVblo_clickcount);
		tvTopiccou = (TextView) this.findViewById(R.id.TVtopic_count);
		tvDate = (TextView) this.findViewById(R.id.TVblo_date);
		tvChangepic = (TextView) this.findViewById(R.id.TVbloinfo_changepic);
		etIntro = (EditText) this.findViewById(R.id.ETbloinfo_intro);
		bloPic = (ImageView) this.findViewById(R.id.IMVblopic);

		DownloadPicThread loadThread = new DownloadPicThread(
				json_bloinfo.getString("blo_pic"), bloPic, handler);
		loadThread.start();

		tvBloname.setText(json_bloinfo.getString("blo_name"));
		tvBloclicou.setText(json_bloinfo.getString("blo_clickcount"));
		tvTopiccou.setText(json_bloinfo.getString("blo_topcount"));
		tvDate.setText(json_bloinfo.getString("blo_createdate")
				.substring(0, 10));
		if (!json_bloinfo.getString("blo_intro").equals("null"))
		{
			etIntro.setText(json_bloinfo.getString("blo_intro"));
		}
	}

	private class ClickListener implements OnClickListener
	{
		@Override
		public void onClick(View v)
		{
			// TODO Auto-generated method stub
			switch (v.getId())
				{
				case R.id.TVbloinfo_back:
					if (!onlyRead)
					{
						BitmapUtil.lastSize = 0;
						BitmapUtil.pathlist.clear();
						BitmapUtil.bmplist.clear();
						BitmapUtil.templist.clear();
						BitmapUtil.delTempPic();
					}
					finish();
					break;
				case R.id.TVbloinfo_ok:
					if (onlyRead)
					{
						finish();
					}
					else
					{
						if (tvOk.getText().equals("�༭"))
						{
							enEdit = true;
							tvOk.setText("ȷ��");
							tvChangepic.setVisibility(View.VISIBLE);
							etIntro.setEnabled(true);
							bloPic.setOnClickListener(listener);
						}
						else
						{
							final String intro;
							if (TextUtils.isEmpty(etIntro.getText().toString()))
							{
								intro = "null";
							}
							else
							{
								intro = etIntro.getText().toString();
							}

							new Thread()
							{

								@Override
								public void run()
								{
									try
									{
										boolean flag = NetTransUtil.editBlock(
												json_bloinfo
														.getString("blo_id"),
												intro);
										handler.obtainMessage(EDIT_BLOCK, flag)
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
										else if (mess
												.contains("SocketTimeoutException"))
										{
											handler.obtainMessage(
													Constant.SOTIMEOUT)
													.sendToTarget();
										}
										e.printStackTrace();
									}

								}

							}.start();
						}
					}
					break;
				case R.id.IMVblopic:
					if (enEdit)
					{
						ShowPickDialog();
					}
					else
					{
					}
					break;
				}
		}

	}

	//��ʾѡ����Ƭ��ʽ��dialoge�������ѡ������գ�
	private void ShowPickDialog()
	{
		new AlertDialog.Builder(this).setTitle("����ͷ��...")
				.setNegativeButton("���", new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int which)
					{
						dialog.dismiss();
						Intent intent = new Intent(Intent.ACTION_PICK);
						intent.setType("image/*");

						startActivityForResult(intent, 1);

					}
				})
				.setPositiveButton("����", new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int whichButton)
					{
						dialog.dismiss();

						Intent intent = new Intent(
								"android.media.action.IMAGE_CAPTURE");
						File file = new File(Environment
								.getExternalStorageDirectory(), "tourTemp.jpg");
						Uri imageUri = Uri.fromFile(file);
						intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
						startActivityForResult(intent, 2);

					}
				}).show();
	}

	//ִ����startActivityForResult���غ���� �ķ���
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		switch (requestCode)
			{
			// �����ֱ�Ӵ�����ȡ
			case 1:
				if (data != null && data.getData() != null)
				{
					Uri uri = data.getData();

					startPhotoZoom(uri);
				}

				break;
			// ����ǵ����������ʱ
			case 2:
				if (temp.exists())
				{
					startPhotoZoom(Uri.fromFile(temp));
				}
				break;
			// ȡ�òü����ͼƬ
			case 3:
				if (data != null)
				{

					Bitmap bitmap = data.getParcelableExtra("data");
					// ����ͼƬ����
					bloPic.setImageBitmap(bitmap);

					BitmapUtil.templist.clear();
					BitmapUtil.saveTempPic(bitmap, "tourTemp.jpg");

					if (temp.exists())
					{
						temp.delete();
					}

				}
				break;
			default:
				break;

			}
		super.onActivityResult(requestCode, resultCode, data);
	}

	//�ü�ͼƬ
	public void startPhotoZoom(Uri uri)
	{
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		// �������crop=true�������ڿ�����Intent��������ʾ��VIEW�ɲü�
		intent.putExtra("crop", "true");
		// aspectX aspectY �ǿ�ߵı���
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		// outputX outputY �ǲü�ͼƬ���
		intent.putExtra("outputX", 150);
		intent.putExtra("outputY", 150);
		intent.putExtra("outputFormat", "JPEG");// ͼƬ��ʽ
		intent.putExtra("noFaceDetection", true);// ȡ������ʶ��
		intent.putExtra("return-data", true);

		startActivityForResult(intent, 3);
	}

	private class GetBlockInfoThread extends Thread
	{

		@Override
		public void run()
		{
			try
			{
				String block = NetTransUtil.getBlockById(blockid);
				handler.obtainMessage(QUERY_BLOCK, block).sendToTarget();
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

	//���ط��ؼ��������ʱ����
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)
		{
			if (!onlyRead)
			{
				BitmapUtil.lastSize = 0;
				BitmapUtil.pathlist.clear();
				BitmapUtil.bmplist.clear();
				BitmapUtil.templist.clear();
				BitmapUtil.delTempPic();
			}
		}

		return super.onKeyDown(keyCode, event);
	}

}
