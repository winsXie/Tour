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

	//�����ͼ������
	abstract protected ViewGroup getContainer();

	abstract protected void initRadioBtns();

	//������ʾ��Activity
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

		// �ĸ�Activity����ʹ��CLEAR_TOPģʽ
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

		//��ȡwindow�е����view��ӵ�����
		container.addView(localActivityManager.getActivity(activityName)
				.getWindow().getDecorView(), new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		/*
		 * API 11 ����ĳЩ�ؼ������� Fragment����ActivityGroup���ڵ���saveInstanceState����Bug
		 * �����ڴ˲�����super.()����
		 */
		// super.onSaveInstanceState(outState);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		//OnkeyDown()�¼�ȫȨ������ǰ��ʾ����Activity������
		return getCurrentActivity().onKeyDown(keyCode, event);
	}

}