package com.bn.tour;

import java.util.ArrayList;
import java.util.List;

import com.bn.util.BitmapUtil;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class EditPhotoActivity extends Activity
{
	private ArrayList<View> pageViews = null;//存储各个视图的list
	private ViewPager pager;
	private MyPageAdapter adapter;
	private int count;

	public List<Bitmap> bmp = new ArrayList<Bitmap>();//存储图片Bitmap的列表
	public List<String> pathlist = new ArrayList<String>();//存储图片路径的列表
	public List<String> templist = new ArrayList<String>();//存储已经压缩完成的将要上传的图片路径的列表
	public int lastSize;//上次选中图片的个数

	private RelativeLayout photo_relativeLayout;

	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_photo);

		photo_relativeLayout = (RelativeLayout) findViewById(R.id.photo_relativeLayout);
		photo_relativeLayout.setBackgroundColor(0x70000000);

		//拷贝原始数据
		bmp = BitmapUtil.bmplist;
		pathlist = BitmapUtil.pathlist;
		templist = BitmapUtil.templist;
		lastSize = BitmapUtil.lastSize;

		Button exit = (Button) findViewById(R.id.edit_photo_cancle);
		exit.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				BitmapUtil.bmplist = bmp;
				BitmapUtil.pathlist = pathlist;
				BitmapUtil.templist = templist;
				BitmapUtil.lastSize = lastSize;
				finish();
			}
		});
		Button del = (Button) findViewById(R.id.edit_photo_del);
		del.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				if (pageViews.size() == 1)
				{
					BitmapUtil.bmplist.clear();
					BitmapUtil.pathlist.clear();
					BitmapUtil.templist.clear();
					BitmapUtil.lastSize = 0;
					finish();
				}
				else
				{
					bmp.remove(count);
					pathlist.remove(count);
					templist.remove(count);
					lastSize--;

					pager.removeAllViews();
					pageViews.remove(count);
					adapter.setListViews(pageViews);
					adapter.notifyDataSetChanged();
				}
			}
		});
		Button enter = (Button) findViewById(R.id.edit_photo_enter);
		enter.setOnClickListener(new View.OnClickListener()
		{

			public void onClick(View v)
			{

				BitmapUtil.bmplist = bmp;
				BitmapUtil.pathlist = pathlist;
				BitmapUtil.templist = templist;
				BitmapUtil.lastSize = lastSize;
				finish();

			}
		});

		pager = (ViewPager) findViewById(R.id.viewpager);
		pager.setOnPageChangeListener(pageChangeListener);// �˼�����ɾ��ͼƬʱ���õ���ɾ��ͼƬ��
		for (int i = 0; i < bmp.size(); i++)
		{
			initListViews(bmp.get(i));//
		}

		adapter = new MyPageAdapter(pageViews);// 构�?adapter
		pager.setAdapter(adapter);// 设置适配�?
		Intent intent = getIntent();
		int id = intent.getIntExtra("ID", 0);
		pager.setCurrentItem(id);
	}

	private void initListViews(Bitmap bm)
	{
		if (pageViews == null)
			pageViews = new ArrayList<View>();
		ImageView img = new ImageView(this);// 构�?textView对象
		img.setBackgroundColor(0xff000000);
		img.setImageBitmap(bm);
		img.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
		pageViews.add(img);// 添加view
	}

	private OnPageChangeListener pageChangeListener = new OnPageChangeListener()
	{

		public void onPageSelected(int arg0)
		{
			count = arg0;// ��ǰpage������
		}

		public void onPageScrolled(int arg0, float arg1, int arg2)
		{
		}

		public void onPageScrollStateChanged(int arg0)
		{
		}
	};

	class MyPageAdapter extends PagerAdapter
	{

		private ArrayList<View> listViews;// content

		private int size;// 页数

		public MyPageAdapter(ArrayList<View> listViews)
		{// 构�?函数
			// 初始化viewpager的时候给的一个页�?
			this.listViews = listViews;
			size = listViews == null ? 0 : listViews.size();
		}

		public void setListViews(ArrayList<View> listViews)
		{// 自己写的�?��方法用来添加数据
			this.listViews = listViews;
			size = listViews == null ? 0 : listViews.size();
		}

		// 获取当前窗体界面�?
		public int getCount()
		{// 返回数量
			return size;
		}

		public int getItemPosition(Object object)
		{
			return POSITION_NONE;
		}

		// �?��position位置的界�?
		public void destroyItem(View arg0, int arg1, Object arg2)
		{// �?��view对象
			((ViewPager) arg0).removeView(listViews.get(arg1 % size));
		}

		public void finishUpdate(View arg0)
		{
		}

		// 初始化position位置的界�?
		public Object instantiateItem(View arg0, int arg1)
		{// 返回view对象
			try
			{
				((ViewPager) arg0).addView(listViews.get(arg1 % size), 0);

			}
			catch (Exception e)
			{
			}
			return listViews.get(arg1 % size);
		}

		// 判断是否由对象生成界�?
		public boolean isViewFromObject(View arg0, Object arg1)
		{
			return arg0 == arg1;
		}

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if(keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)
		{
			BitmapUtil.bmplist = bmp;
			BitmapUtil.pathlist = pathlist;
			BitmapUtil.templist = templist;
			BitmapUtil.lastSize = lastSize;
		}
		return super.onKeyDown(keyCode, event);
	}

}
