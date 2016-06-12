package com.bn.myview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * �޸�ViewPager����onTouchEvent()��onInterceptTouchEvent()�в�������쳣��Ϣ���������׳�
 * ���ͼƬԤ��ʱ���ȷŴ���СͼƬ���µĲ����쳣
 * 
 */
public class ViewPagerModify extends android.support.v4.view.ViewPager
{

	public ViewPagerModify(Context context)
	{
		super(context);
	}

	public ViewPagerModify(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev)
	{
		// ����Ƿ��������쳣��ʹ�䲻�����׳�
		try
		{
			return super.onTouchEvent(ev);
		}
		catch (IllegalArgumentException ex)
		{
			ex.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev)
	{
		try
		{
			return super.onInterceptTouchEvent(ev);
		}
		catch (IllegalArgumentException ex)
		{
			ex.printStackTrace();
		}
		return false;
	}
}
