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
	private static final int CHANG_HEADIMG = 0;//����ͷ��
	private ImageView headimage;
	private Button editInfo, chanpass, editQianm;
	private TextView tvName, tvBack, tvEnter;
	private EditText etQianm;
	private String oldImgpath = null;//ԭͷ��·��
	private File temp = new File(Environment.getExternalStorageDirectory()
			+ "/tourTemp.jpg");//�洢����ͼƬ��·��

	private Intent intent;
	private boolean isChange = false;//�Ƿ�ı���ͷ��

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

						Toast.makeText(currActivity, "�޸ĳɹ���",
								Toast.LENGTH_SHORT).show();

						MainMyActivity.isChanged = true;
						currActivity.finish();
					}
					else
					{
						Toast.makeText(currActivity, "�޸�ʧ�ܣ�",
								Toast.LENGTH_SHORT).show();
						currActivity.finish();
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

	//��ʼ������
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
				BitmapUtil.templist.clear();// �����ʱ�ļ�
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

	//���ط��ؼ��������ʱ����
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
		// ��ʱ��ʾ����
		tvName.setText(Constant.currUserInfo.getString("user_name"));
		if (!Constant.currUserInfo.getString("user_qianming").equals("null"))
		{
			etQianm.setText(Constant.currUserInfo.getString("user_qianming"));
		}

		super.onRestart();
	}

}
