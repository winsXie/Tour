package com.bn.bbs;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.widget.TextView;

import com.bn.tour.R;

public class MyPostActivity extends FragmentActivity
{

	private ViewPager mViewPager;//用于展示我的主题帖和我的回复帖的viewpager
	private FragmentPagerAdapter fragmentAdapter;//viewpager适配器
	private List<Fragment> mFragments = new ArrayList<Fragment>();//存储Fragment的list

	private TextView tvTopic, tvReply;//上侧的标题栏
	private int currposition = 0;//当前所在的item

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_topic);

		mViewPager = (ViewPager) this.findViewById(R.id.my_post_viewpager);
		initview();

		fragmentAdapter = new FragmentPagerAdapter(getSupportFragmentManager())
		{

			@Override
			public Fragment getItem(int arg0)
			{
				// TODO Auto-generated method stub
				return mFragments.get(arg0);
			}

			@Override
			public int getCount()
			{
				// TODO Auto-generated method stub
				return mFragments.size();
			}

		};

		mViewPager.setAdapter(fragmentAdapter);//设置适配器
		mViewPager.setCurrentItem(currposition);//切换界面

		// 添加页面改变监听
		mViewPager.setOnPageChangeListener(new OnPageChangeListener()
		{
			@Override
			public void onPageScrollStateChanged(int arg0)
			{
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2)
			{
			}

			@Override
			public void onPageSelected(int position)
			{
				switch (position)
					{
					case 0:
						tvTopic.setBackgroundColor(getResources().getColor(
								R.color.white));
						tvReply.setBackgroundColor(getResources().getColor(
								R.color.littlegray));
						break;
					case 1:
						tvTopic.setBackgroundColor(getResources().getColor(
								R.color.littlegray));
						tvReply.setBackgroundColor(getResources().getColor(
								R.color.white));
						break;
					}
			}

		});

	}

	//初始化界面
	public void initview()
	{
		tvTopic = (TextView) this.findViewById(R.id.TVmy_post_topic);
		tvReply = (TextView) this.findViewById(R.id.TVmy_post_reply);
		MyTopic tab1 = new MyTopic();
		MyReply tab2 = new MyReply();
		mFragments.add(tab1);
		mFragments.add(tab2);

	}

	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		// TODO Auto-generated method stub
		/*
		 * API 11 以上某些控件，包括 Fragment还有ActivityGroup，在调用saveInstanceState存在Bug
		 * 所以在此不调用super.()方法
		 */
		// super.onSaveInstanceState(outState);
	}
}
