package com.bn.myview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

/**
 *自定义ScrollView（解决viewpager不能横向滑动或不流畅问题）
 *重写onInterceptTouchEvent方法，当横向滑动距离大于纵向滑动距离时，ScrollView不纵向滑动
 *
 */
public class MyScrollView extends ScrollView
{
	// 滑动距离及坐标
	private float xDistance, yDistance, xLast, yLast;

	public MyScrollView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev)
	{
		switch (ev.getAction())
			{
			case MotionEvent.ACTION_DOWN:
				xDistance = yDistance = 0f;
				xLast = ev.getX();
				yLast = ev.getY();
				break;
			case MotionEvent.ACTION_MOVE:
				final float curX = ev.getX();
				final float curY = ev.getY();

				xDistance += Math.abs(curX - xLast);
				yDistance += Math.abs(curY - yLast);
				xLast = curX;
				yLast = curY;

				if (xDistance > yDistance)
				{
					return false;
				}
			}

		return super.onInterceptTouchEvent(ev);
	}
}