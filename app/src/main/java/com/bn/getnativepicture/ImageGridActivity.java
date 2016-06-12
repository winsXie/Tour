package com.bn.getnativepicture;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import com.bn.getnativepicture.ImageGridAdapter.TextCallback;
import com.bn.tour.R;
import com.bn.util.BitmapUtil;

public class ImageGridActivity extends Activity
{
	private List<ImageItem> dataList;
	private GridView gridView;
	private ImageGridAdapter adapter;
	private Button bt;
	private Handler mHandler;

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_image_grid);
		mHandler = new MyHandler(this);

		dataList = (List<ImageItem>) getIntent().getSerializableExtra(
				"imagelist");

		initView();
		bt = (Button) findViewById(R.id.bt);
		bt.setText("完成" + "(" + BitmapUtil.pathlist.size() + "/"
				+ BitmapUtil.maxCount + ")");
		bt.setOnClickListener(new OnClickListener()
		{

			public void onClick(View v)
			{
				Set<String> ketSet = adapter.map.keySet();
				Iterator<String> it = ketSet.iterator();
				if (BitmapUtil.pathlist.size() < BitmapUtil.maxCount)
				{
					while (it.hasNext())
					{
						BitmapUtil.pathlist.add(it.next());
					}
				}
				finish();
			}

		});
	}

	private static class MyHandler extends Handler
	{
		WeakReference<ImageGridActivity> mActivity;

		public MyHandler(ImageGridActivity activity)
		{
			mActivity = new WeakReference<ImageGridActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg)
		{
			ImageGridActivity currActivity = mActivity.get();
			switch (msg.what)
				{
				case 0:
					Toast.makeText(currActivity,
							"最多选择" + BitmapUtil.maxCount + "张图片",
							Toast.LENGTH_SHORT).show();
					break;

				default:
					break;
				}
		}
	}

	private void initView()
	{
		gridView = (GridView) findViewById(R.id.gridview);
		gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
		adapter = new ImageGridAdapter(ImageGridActivity.this, dataList,
				mHandler);
		gridView.setAdapter(adapter);
		adapter.setTextCallback(new TextCallback()
		{
			public void onListen(int count)
			{
				bt.setText("完成" + "(" + count + "/" + BitmapUtil.maxCount + ")");
			}
		});
	}

}
