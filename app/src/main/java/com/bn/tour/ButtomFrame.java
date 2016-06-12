package com.bn.tour;

import com.bn.bbs.BlockActivity;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;

public class ButtomFrame extends MyActivityGroup implements
		OnCheckedChangeListener,OnClickListener
{
	//导航页各个Activity的name
	private static final String CONTENT_ACTIVITY_NAME_0 = "MainActivity";
	private static final String CONTENT_ACTIVITY_NAME_1 = "ExploreActivity";
	private static final String CONTENT_ACTIVITY_NAME_2 = "BlockActivity";
	private static final String CONTENT_ACTIVITY_NAME_3 = "MainMyActivity";

	private RadioButton[] Radiobutton;//界面底部的按钮

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		setContentView(R.layout.main_frame);
		super.onCreate(savedInstanceState);

		initRadioBtns();
	}

	protected ViewGroup getContainer()
	{
		return (ViewGroup) findViewById(R.id.container);
	}

	/*
	 * 初始化单选按钮
	 * 
	 * @see com.bn.tour.MyActivityGroup#initRadioBtns()
	 */
	protected void initRadioBtns()
	{
		Radiobutton = new RadioButton[4];

		Radiobutton[0] = (RadioButton) findViewById(R.id.RadioButton_main);
		Radiobutton[0].setBackgroundResource(R.drawable.mainpage_pressed);
		Radiobutton[1] = (RadioButton) findViewById(R.id.RadioButton_explore);
		Radiobutton[1].setBackgroundResource(R.drawable.mainpage_finder_normal);
		Radiobutton[2] = (RadioButton) findViewById(R.id.RadioButton_block);
		Radiobutton[2].setBackgroundResource(R.drawable.mainpage_block_normal);
		Radiobutton[3] = (RadioButton) findViewById(R.id.RadioButton_my);
		Radiobutton[3].setBackgroundResource(R.drawable.mainpage_my_normal);

		// 设置选中状态监听
		for (int i = 0; i < Radiobutton.length; i++)
		{
			Radiobutton[i].setOnCheckedChangeListener(this);
			Radiobutton[i].setOnClickListener(this);
		}

		Radiobutton[0].setChecked(true);// 默认选中第一个按钮（主页）
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
	{
		if (isChecked)
		{
			switch (buttonView.getId())
				{
				case R.id.RadioButton_main:
				{
					setContainerView(CONTENT_ACTIVITY_NAME_0,
							MainActivity.class);
					Radiobutton[0]
							.setBackgroundResource(R.drawable.mainpage_pressed);
					Radiobutton[1]
							.setBackgroundResource(R.drawable.mainpage_finder_normal);
					Radiobutton[2]
							.setBackgroundResource(R.drawable.mainpage_block_normal);
					Radiobutton[3]
							.setBackgroundResource(R.drawable.mainpage_my_normal);
					break;
				}
				case R.id.RadioButton_explore:
				{

					setContainerView(CONTENT_ACTIVITY_NAME_1,
							ExploreActivity.class);
					Radiobutton[1]
							.setBackgroundResource(R.drawable.mainpage_finder_pressed);
					Radiobutton[0]
							.setBackgroundResource(R.drawable.mainpage_normal);
					Radiobutton[2]
							.setBackgroundResource(R.drawable.mainpage_block_normal);
					Radiobutton[3]
							.setBackgroundResource(R.drawable.mainpage_my_normal);
					break;
				}

				case R.id.RadioButton_block:
				{
					setContainerView(CONTENT_ACTIVITY_NAME_2,
							BlockActivity.class);
					Radiobutton[2]
							.setBackgroundResource(R.drawable.mainpage_block_pressed);
					Radiobutton[0]
							.setBackgroundResource(R.drawable.mainpage_normal);
					Radiobutton[1]
							.setBackgroundResource(R.drawable.mainpage_finder_normal);
					Radiobutton[3]
							.setBackgroundResource(R.drawable.mainpage_my_normal);
					break;
				}
				case R.id.RadioButton_my:
				{
					setContainerView(CONTENT_ACTIVITY_NAME_3,
							MainMyActivity.class);
					Radiobutton[3]
							.setBackgroundResource(R.drawable.mainpage_my_pressed);
					Radiobutton[0]
							.setBackgroundResource(R.drawable.mainpage_normal);
					Radiobutton[1]
							.setBackgroundResource(R.drawable.mainpage_finder_normal);
					Radiobutton[2]
							.setBackgroundResource(R.drawable.mainpage_block_normal);
					break;
				}
				default:
					break;
				}
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		// TODO Auto-generated method stub
		/*
		 * API 11 以上某些控件，包括 Fragment还有ActivityGroup，在调用saveInstanceState存在Bug
		 * 所以在此不调用super.()方法
		 */
		// super.onSaveInstanceState(outState);
	}

	@Override
	public void onClick(View v)
	{
	}

}