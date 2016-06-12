package com.bn.message;

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

public class MyMessageActivity extends FragmentActivity
{

	private ViewPager mViewPager;
	private TextView tvAcceMess, tSendMess;

	private FragmentPagerAdapter mAdapter;
	private List<Fragment> mFragments = new ArrayList<Fragment>();
	private int currposition = 0;

	@Override
	protected void onCreate(Bundle arg0)
	{
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		setContentView(R.layout.activity_my_message);

		mViewPager = (ViewPager) this.findViewById(R.id.mess_viewpager);
		tvAcceMess = (TextView) this.findViewById(R.id.TVmymess_accmess);
		tSendMess = (TextView) this.findViewById(R.id.TVmymess_sendmess);
		AcceptMessage tb1 = new AcceptMessage();
		SendMessage tb2 = new SendMessage();
		mFragments.add(tb1);
		mFragments.add(tb2);

		mAdapter = new FragmentPagerAdapter(getSupportFragmentManager())
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

		mViewPager.setAdapter(mAdapter);
		mViewPager.setCurrentItem(currposition);

		mViewPager.setOnPageChangeListener(new OnPageChangeListener()
		{
			@Override
			public void onPageScrollStateChanged(int arg0)
			{
				// TODO Auto-generated method stub

			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2)
			{
				// TODO Auto-generated method stub

			}

			@Override
			public void onPageSelected(int position)
			{
				switch (position)
					{
					case 0:
						tvAcceMess.setBackgroundColor(getResources().getColor(
								R.color.white));
						tSendMess.setBackgroundColor(getResources().getColor(
								R.color.littlegray));
						break;
					case 1:
						tvAcceMess.setBackgroundColor(getResources().getColor(
								R.color.littlegray));
						tSendMess.setBackgroundColor(getResources().getColor(
								R.color.white));
						break;
					}
			}

		});

	}

	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		// TODO Auto-generated method stub
		/*
		 * API 11 ����ĳЩ�ؼ������� Fragment����ActivityGroup���ڵ���saveInstanceState����Bug
		 * �����ڴ˲�����super.()����
		 */
		// super.onSaveInstanceState(outState);
	}

}
