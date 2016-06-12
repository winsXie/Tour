package com.bn.thread;

import com.bn.util.BitmapUtil;

import android.graphics.Bitmap;
import android.os.Handler;

/**
 * Ԥ��ͼƬ�����̣߳��ȼ�鱾���Ƿ��Ѿ�������ԭͼ��δѹ���������ѻ�����ӱ��ض�ȡ�� ����ֱ�Ӵ�����������ԭͼ��֮����ڱ���
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
