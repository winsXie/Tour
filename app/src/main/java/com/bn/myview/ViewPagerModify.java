package com.bn.myview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * 修改ViewPager，在onTouchEvent()和onInterceptTouchEvent()中捕获参数异常信息，不向上抛出
 * 解决图片预览时过度放大缩小图片导致的参数异常
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
		// 捕获非法参数的异常，使其不向上抛出
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
