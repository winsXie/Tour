package com.bn.thread;

import com.bn.util.BitmapUtil;

import android.graphics.Bitmap;
import android.os.Handler;
import android.widget.ImageView;

/**
 * 用于非列表的单个图片加载，先检查本地是否缓存，没有则从网络下载； 之后存在本地，获得的图片都是经过压缩的图片
 * 
 */
public class DownloadPicThread extends Thread
{
	private ImageView imageview;
	private String url;
	private Handler handler;
	private Bitmap bitmap = null;

	@Override
	public void run()
	{
		bitmap = BitmapUtil.getBitmapFromFile(url.substring(url
				.lastIndexOf("/") + 1));
		if (bitmap == null)
		{
			bitmap = BitmapUtil.getBitmapFromNet(url);
		}

		if (bitmap != null)
		{
			handler.post(new Runnable()
			{
				// 此方法运行在ui线程中
				@Override
				public void run()
				{
					imageview.setImageBitmap(bitmap);
				}
			});
		}
		super.run();
	}

	public DownloadPicThread(String url, ImageView imageview, Handler handler)
	{
		this.url = url;
		this.imageview = imageview;
		this.handler = handler;
	}

}
