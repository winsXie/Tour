package com.bn.user;

import java.io.File;
import java.lang.ref.WeakReference;

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
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bn.thread.DownloadPicThread;
import com.bn.tour.MainMyActivity;
import com.bn.tour.R;
import com.bn.util.BitmapUtil;
import com.bn.util.Constant;
import com.bn.util.NetTransUtil;

public class ChangeHeadActivity extends Activity implements OnClickListener
{
	private static final int CHANG_HEADIMG = 0;//更换头像
	private ImageView headimage;
	private Button editInfo, chanpass, editQianm;
	private TextView tvName, tvBack, tvEnter;
	private EditText etQianm;
	private String oldImgpath = null;//原头像路径
	private File temp = new File(Environment.getExternalStorageDirectory()
			+ "/tourTemp.jpg");//存储拍照图片的路径

	private Intent intent;
	private boolean isChange = false;//是否改变了头像

	private Handler handler;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_chanheadimg);
		handler = new MyHandler(this);
		init();
	}

	private static class MyHandler extends Handler
	{
		WeakReference<ChangeHeadActivity> mActivity;

		public MyHandler(ChangeHeadActivity activity)
		{
			mActivity = new WeakReference<ChangeHeadActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg)
		{
			ChangeHeadActivity currActivity = mActivity.get();
			switch (msg.what)
				{
				case CHANG_HEADIMG:
					String picurl = (String) msg.obj;
					if (picurl != null)
					{
						Constant.currUserInfo.put("user_image", picurl);

						Toast.makeText(currActivity, "修改成功！",
								Toast.LENGTH_SHORT).show();

						MainMyActivity.isChanged = true;
						currActivity.finish();
					}
					else
					{
						Toast.makeText(currActivity, "修改失败！",
								Toast.LENGTH_SHORT).show();
						currActivity.finish();
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

	//初始化界面
	private void init()
	{
		headimage = (ImageView) findViewById(R.id.IMVmy_change_head);
		tvBack = (TextView) findViewById(R.id.TVmy_change_back);
		tvEnter = (TextView) findViewById(R.id.TVmy_change_enter);
		tvName = (TextView) findViewById(R.id.TVmy_change_uname);

		etQianm = (EditText) findViewById(R.id.ETmy_change_qianm);
		editQianm = (Button) findViewById(R.id.BTmy_change_editqianm);
		editInfo = (Button) findViewById(R.id.BTmy_change_editinfo);
		chanpass = (Button) findViewById(R.id.BTmy_change_chanpass);

		if (Constant.currUserInfo != null)
		{
			oldImgpath = Constant.currUserInfo.getString("user_image");
			DownloadPicThread loadThread = new DownloadPicThread(oldImgpath,
					headimage, handler);
			loadThread.start();

		}

		tvName.setText(Constant.currUserInfo.getString("user_name"));
		if (!Constant.currUserInfo.getString("user_qianming").equals("null"))
		{
			etQianm.setText(Constant.currUserInfo.getString("user_qianming"));
		}

		headimage.setOnClickListener(this);
		editQianm.setOnClickListener(this);
		chanpass.setOnClickListener(this);
		editInfo.setOnClickListener(this);
		tvBack.setOnClickListener(this);
		tvEnter.setOnClickListener(this);
	}

	public void onClick(View v)
	{
		// TODO Auto-generated method stub
		switch (v.getId())
			{
			case R.id.IMVmy_change_head:
				ShowPickDialog();
				break;
			case R.id.BTmy_change_editinfo:
				intent = new Intent(ChangeHeadActivity.this,
						EditUserInfoActivity.class);
				startActivity(intent);
				break;
			case R.id.BTmy_change_chanpass:
				intent = new Intent(ChangeHeadActivity.this,
						ChangePasswordActivity.class);
				startActivity(intent);
				break;
			case R.id.BTmy_change_editqianm:
				intent = new Intent(ChangeHeadActivity.this,
						PublishQianmActivity.class);
				startActivity(intent);
				break;
			case R.id.TVmy_change_back:
				BitmapUtil.templist.clear();// 清除临时文件
				BitmapUtil.delTempPic();
				finish();
				break;
			case R.id.TVmy_change_enter:
				if (!isChange || BitmapUtil.templist.size() == 0)
				{
					finish();
				}
				else
				{
					new Thread()
					{
						@Override
						public void run()
						{
							try
							{
								String picurl = NetTransUtil
										.changeHeadimg(Constant.loginUid);
								handler.obtainMessage(CHANG_HEADIMG, picurl)
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
									handler.obtainMessage(Constant.SOTIMEOUT)
											.sendToTarget();
								}
								e.printStackTrace();
							}
						}
					}.start();

				}
				break;
			default:
				break;
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
					headimage.setImageBitmap(bitmap);

					BitmapUtil.templist.clear();
					BitmapUtil.saveTempPic(bitmap, "tourTemp.jpg");

					if (temp.exists())
					{
						temp.delete();
					}
					isChange = true;
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

	//拦截返回键，清除临时数据
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)
		{
			BitmapUtil.templist.clear();
			BitmapUtil.delTempPic();
		}
		
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onRestart()
	{
		// 及时显示更新
		tvName.setText(Constant.currUserInfo.getString("user_name"));
		if (!Constant.currUserInfo.getString("user_qianming").equals("null"))
		{
			etQianm.setText(Constant.currUserInfo.getString("user_qianming"));
		}

		super.onRestart();
	}

}
