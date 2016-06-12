package com.bn.tour;

import android.app.ActivityGroup;
import android.app.LocalActivityManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

@SuppressWarnings("deprecation")
public abstract class MyActivityGroup extends ActivityGroup
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
	}

	private ViewGroup container;
	private LocalActivityManager localActivityManager;

	//获得视图的容器
	abstract protected ViewGroup getContainer();

	abstract protected void initRadioBtns();

	//设置显示的Activity
	protected void setContainerView(String activityName,
			Class<?> activityClassTye)
	{
		if (null == localActivityManager)
		{
			localActivityManager = getLocalActivityManager();
		}
		if (null == container)
		{
			container = getContainer();
		}

		container.removeAllViews();

		// 四个Activity都不使用CLEAR_TOP模式
		if ((activityName.equals("MainActivity")))
		{
			Intent intent = new Intent(this, activityClassTye);
			localActivityManager.startActivity(activityName, intent);
		}
		else if ((activityName.equals("ExploreActivity")))
		{
			Intent intent = new Intent(this, activityClassTye);
			localActivityManager.startActivity(activityName, intent);

		}
		else if ((activityName.equals("BlockActivity")))
		{
			Intent intent = new Intent(this, activityClassTye);
			localActivityManager.startActivity(activityName, intent);
		}
		else
		{
			Intent intent = new Intent(this, activityClassTye);
			localActivityManager.startActivity(activityName, intent);
		}

		//获取window中的最顶层view添加到容器
		container.addView(localActivityManager.getActivity(activityName)
				.getWindow().getDecorView(), new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		/*
		 * API 11 以上某些控件，包括 Fragment还有ActivityGroup，在调用saveInstanceState存在Bug
		 * 所以在此不调用super.()方法
		 */
		// super.onSaveInstanceState(outState);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		//OnkeyDown()事件全权交给当前显示的子Activity来处理
		return getCurrentActivity().onKeyDown(keyCode, event);
	}

}