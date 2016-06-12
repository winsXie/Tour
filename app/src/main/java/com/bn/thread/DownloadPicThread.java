package com.bn.thread;

import com.bn.util.BitmapUtil;

import android.graphics.Bitmap;
import android.os.Handler;
import android.widget.ImageView;

/**
 * ���ڷ��б�ĵ���ͼƬ���أ��ȼ�鱾���Ƿ񻺴棬û������������أ� ֮����ڱ��أ���õ�ͼƬ���Ǿ���ѹ����ͼƬ
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
				// �˷���������ui�߳���
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
