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
	private static final int QUERY_BLOCK = 0;//查询版块信息（发送handler时的what值）
	private static final int EDIT_BLOCK = 1;//编辑版块信息
	private TextView tvBack, tvOk, tvBloname, tvBloclicou, tvTopiccou, tvDate,
			tvChangepic;
	private EditText etIntro;
	private ImageView bloPic;
	private boolean onlyRead = true;//设置读写类型标志位（是否为只读）
	private boolean enEdit = false;//是否启用编辑标志位
	private ClickListener listener;
	private String blockid;//当前所查询的blockid
	private GetBlockInfoThread getBlock;//获取block信息线程

	private File temp = new File(Environment.getExternalStorageDirectory()
			+ "/tourTemp.jpg");//存储拍照图片的路径

	private JSONObject json_bloinfo;//存储block信息的JSONObject

	private Handler handler;//用于线程间通信的handler

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
						Toast.makeText(currActivity, "查询信息失败！",
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

						Toast.makeText(currActivity, "修改成功！", Toast.LENGTH_LONG)
								.show();
						currActivity.finish();
					}
					else
					{
						Toast.makeText(currActivity, "修改失败！", Toast.LENGTH_LONG)
								.show();
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

	//获取信息成功后初始化iew
	private void initView()
	{
		tvOk = (TextView) this.findViewById(R.id.TVbloinfo_ok);
		tvOk.setOnClickListener(listener);
		if (!onlyRead)
		{
			tvOk.setText("编辑");
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
						if (tvOk.getText().equals("编辑"))
						{
							enEdit = true;
							tvOk.setText("确认");
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

	//显示选择照片方式的dialoge（从相册选择或拍照）
	private void ShowPickDialog()
	{
		new AlertDialog.Builder(this).setTitle("设置头像...")
				.setNegativeButton("相册", new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int which)
					{
						dialog.dismiss();
						Intent intent = new Intent(Intent.ACTION_PICK);
						intent.setType("image/*");

						startActivityForResult(intent, 1);

					}
				})
				.setPositiveButton("拍照", new DialogInterface.OnClickListener()
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

	//执行完startActivityForResult返回后调用 的方法
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		switch (requestCode)
			{
			// 如果是直接从相册获取
			case 1:
				if (data != null && data.getData() != null)
				{
					Uri uri = data.getData();

					startPhotoZoom(uri);
				}

				break;
			// 如果是调用相机拍照时
			case 2:
				if (temp.exists())
				{
					startPhotoZoom(Uri.fromFile(temp));
				}
				break;
			// 取得裁剪后的图片
			case 3:
				if (data != null)
				{

					Bitmap bitmap = data.getParcelableExtra("data");
					// 设置图片背景
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

	//裁剪图片
	public void startPhotoZoom(Uri uri)
	{
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		// 下面这个crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
		intent.putExtra("crop", "true");
		// aspectX aspectY 是宽高的比例
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		// outputX outputY 是裁剪图片宽高
		intent.putExtra("outputX", 150);
		intent.putExtra("outputY", 150);
		intent.putExtra("outputFormat", "JPEG");// 图片格式
		intent.putExtra("noFaceDetection", true);// 取消人脸识别
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

	//拦截返回键，清除临时数据
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
