package com.bn.thread;

import com.bn.util.BitmapUtil;

import android.graphics.Bitmap;
import android.os.Handler;

/**
 * 预览图片加载线程，先检查本地是否已经缓存了原图（未压缩），若已缓存则从本地读取， 否则直接从网络上下载原图，之后存在本地
 * 
 */
public class LoadPicFromNet extends Thread
{
	private int what;
	private String url;
	private Handler handler;
	private Bitmap bitmap;

	public LoadPicFromNet(int what, String url, Handler handler)
	{
		this.what = what;
		this.url = url;
		this.handler = handler;
	}

	@Override
	public void run()
	{
		bitmap = BitmapUtil.scanPicture(url);

		if (bitmap != null)
		{
			handler.obtainMessage(what, bitmap).sendToTarget();
		}
		super.run();
	}

}
