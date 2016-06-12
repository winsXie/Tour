package com.bn.myview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 *自定义ListView（解决首页ListView动态设置高度后仍然显示不全，和ScrollView滑动冲突）
 *使ListView不滑动
 */
public class NoScrollListview extends ListView
{

	public NoScrollListview(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	/**
	 * 设置不滚动
	 */
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
				MeasureSpec.AT_MOST);
		super.onMeasure(widthMeasureSpec, expandSpec);

	}

}