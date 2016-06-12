package com.bn.travelnote;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.bn.getnativepicture.PicAlbumActivity;
import com.bn.thread.LocationThread;
import com.bn.tour.EditPhotoActivity;
import com.bn.tour.R;
import com.bn.util.BitmapUtil;
import com.bn.util.CheckUtil;
import com.bn.util.Constant;
import com.bn.util.NetTransUtil;

public class WriteNoteActivity extends Activity
{
	private static final int WRITE_NOTE = 0;//写游记
	private TextView tvCancle,tvPublish,tvDate;
	private EditText etContent,etPlace;
	private ImageView imbAddpic,imvPic,imvPlace;
	private String tratopid = null;//所在的游记主题id
	private String trat_pic = null;//游记主题的背景图片（用于判断是否更新图片）
	private Handler handler;
	private Intent intent;

	@SuppressLint("SimpleDateFormat")
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_writenote);

		handler = new MyHandler(this);

		tratopid = this.getIntent().getStringExtra("TRATID");
		trat_pic = this.getIntent().getStringExtra("TRATPIC");
		BitmapUtil.maxCount = 1;

		tvCancle = (TextView) this.findViewById(R.id.TVwrite_note_cancle);
		tvPublish = (TextView) this.findViewById(R.id.TVwrite_note_send);
		tvDate = (TextView) this.findViewById(R.id.TVnote_date);
		etContent = (EditText) this.findViewById(R.id.ETnote_content);
		imbAddpic = (ImageView) this.findViewById(R.id.IMBnote_addpic);
		imvPic = (ImageView) this.findViewById(R.id.IMVnote_picture);
		imvPlace = (ImageView) this.findViewById(R.id.IMVnote_place);
		etPlace = (EditText) this.findViewById(R.id.ETnote_place);

		// 一定要传整个application的context,不是单个Activity的context
		LocationThread location = new LocationThread(getApplicationContext(),
				handler);
		location.start();// 网络定位

		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
		String time = fmt.format(new Date());
		tvDate.setText(time);

		ClickListener clicklistener = new ClickListener();
		tvCancle.setOnClickListener(clicklistener);
		tvPublish.setOnClickListener(clicklistener);
		imbAddpic.setOnClickListener(clicklistener);
		imvPic.setOnClickListener(clicklistener);
		imvPlace.setOnClickListener(clicklistener);
	}

	private static class MyHandler extends Handler
	{
		WeakReference<WriteNoteActivity> mActivity;

		public MyHandler(WriteNoteActivity activity)
		{
			mActivity = new WeakReference<WriteNoteActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg)
		{
			WriteNoteActivity currActivity = mActivity.get();
			switch (msg.what)
				{
				case WRITE_NOTE:
					String noteid = (String) msg.obj;
					if (noteid != null)
					{
						Toast.makeText(currActivity, "发表成功！", Toast.LENGTH_LONG)
								.show();
						BitmapUtil.pathlist.clear();
						BitmapUtil.bmplist.clear();
						BitmapUtil.templist.clear();
						BitmapUtil.lastSize = 0;
						BitmapUtil.delTempPic();

						currActivity.intent = new Intent(currActivity,
								MyNoteActivity.class);
						currActivity.intent.putExtra("NOTEID", noteid);

						currActivity.setResult(1, currActivity.intent);
						currActivity.finish();
					}
					else
					{
						Toast.makeText(currActivity, "发表失败！", Toast.LENGTH_LONG)
								.show();
					}
					break;
				case 1:
					String place = (String) msg.obj;
					currActivity.etPlace.setText(place);// 定位成功，显示地址
					break;
				case 2:
					Toast.makeText(currActivity, "自动定位失败\n请检查网络设置！",
							Toast.LENGTH_LONG).show();
					break;
				}
			super.handleMessage(msg);
		}
	}

	//实现弹出框布局
	public class PopupWindows extends PopupWindow
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
					Intent intent = new Intent(WriteNoteActivity.this,
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

	private static final int TAKE_PICTURE = 0x000000;
	private String path = "";

	public void photo()
	{
		Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
		File file = new File(Environment.getExternalStorageDirectory(),
				String.valueOf(System.currentTimeMillis()) + ".jpg");
		path = file.getPath();
		Uri imageUri = Uri.fromFile(file);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
		startActivityForResult(intent, TAKE_PICTURE);
	}

	// OnClickListener点击监听类
	private class ClickListener implements OnClickListener
	{
		@Override
		public void onClick(View v)
		{
			switch (v.getId())
				{
				case R.id.TVwrite_note_cancle:
					BitmapUtil.pathlist.clear();
					BitmapUtil.bmplist.clear();
					BitmapUtil.templist.clear();
					BitmapUtil.lastSize = 0;
					BitmapUtil.delTempPic();

					intent = new Intent(WriteNoteActivity.this, MyNoteActivity.class);
					setResult(2, intent);
					finish();
					break;
				case R.id.TVwrite_note_send:
					if (!TextUtils.isEmpty(etContent.getText().toString()))
					{
						if (!TextUtils.isEmpty(etPlace.getText().toString()))
						{

							final Map<String, String> paraMap = new HashMap<String, String>();
							paraMap.put("note_userid", Constant.loginUid);
							paraMap.put("note_tratopid", tratopid);
							// 去除回车制表符
							paraMap.put("note_content", CheckUtil
									.replaceBlank(etContent.getText()
											.toString()));
							paraMap.put("note_place", CheckUtil
									.replaceBlank(etPlace.getText().toString()));
							if (trat_pic == null)
							{
								paraMap.put("trat_pic", "null");
							}
							else
							{
								paraMap.put("trat_pic", trat_pic);
							}

							new Thread()
							{

								@Override
								public void run()
								{
									try
									{
										String noteid = NetTransUtil
												.writeTraNote(paraMap);
										handler.obtainMessage(WRITE_NOTE,
												noteid).sendToTarget();
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
							Toast.makeText(WriteNoteActivity.this, "请填写游记地点！",
									Toast.LENGTH_LONG).show();
						}
					}
					else
					{
						Toast.makeText(WriteNoteActivity.this, "请输入游记内容！",
								Toast.LENGTH_LONG).show();
					}
					break;
				case R.id.IMBnote_addpic:
					new PopupWindows(WriteNoteActivity.this, imbAddpic);
					break;
				case R.id.IMVnote_picture:
					Intent intent = new Intent(WriteNoteActivity.this,
							EditPhotoActivity.class);
					intent.putExtra("ID", 0);
					startActivity(intent);
					break;
				case R.id.IMVnote_place:
					// 一定要传整个application的context,不是单个Activity的context
					LocationThread location = new LocationThread(
							getApplicationContext(), handler);
					location.start();// 网络定位
					break;
				}
		}
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		switch (requestCode)
			{
			case TAKE_PICTURE:
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

	@Override
	protected void onDestroy()
	{
		BitmapUtil.pathlist.clear();
		BitmapUtil.bmplist.clear();
		BitmapUtil.templist.clear();
		BitmapUtil.lastSize = 0;
		BitmapUtil.delTempPic();
		super.onDestroy();
	}

	@Override
	protected void onStart()
	{
		if (BitmapUtil.pathlist.size() == 0)
		{
			imbAddpic.setVisibility(View.VISIBLE);
			imvPic.setVisibility(View.GONE);
		}
		else
		{
			imbAddpic.setVisibility(View.GONE);
			imvPic.setVisibility(View.VISIBLE);

			try
			{
				Bitmap bm = null;
				if (BitmapUtil.lastSize == 0)
				{
					String path = BitmapUtil.pathlist.get(BitmapUtil.lastSize);
					bm = BitmapUtil.revitionImageSize(path);
					/*
					 * 只有在列表为空时才添加，要进行判断，否则每次运行到onStart()方法时都添加一次，导致预览时会出现多个相同的图片
					 */
					String name = path.substring(path.lastIndexOf("/"),
							path.length());
					BitmapUtil.saveTempPic(bm, name);
					BitmapUtil.bmplist.add(bm);

					BitmapUtil.lastSize++;
				}
				else
				{
					bm = BitmapUtil.bmplist.get(0);
				}

				if (bm != null)
				{
					imvPic.setImageBitmap(bm);
				}

			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		super.onStart();
	}

	//拦截返回键，清除临时数据
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)
		{
			BitmapUtil.pathlist.clear();
			BitmapUtil.bmplist.clear();
			BitmapUtil.templist.clear();
			BitmapUtil.lastSize = 0;
			BitmapUtil.delTempPic();

			intent = new Intent(WriteNoteActivity.this, MyNoteActivity.class);
			setResult(2, intent);
		}
		return super.onKeyDown(keyCode, event);
	}

}
