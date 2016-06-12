package com.bn.thread;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bn.util.BitmapUtil;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

/**
 * 用于列表图片的加载，使用内存和本地双缓存，根据tag来加载设置图片，防止错乱
 * 
 */
public class ImgAsyncDownload
{
	private HashMap<String, MyAsyncTask> map = new HashMap<String, MyAsyncTask>();//存储AsyncTask的map
	private Map<String, SoftReference<Bitmap>> imageCaches = new HashMap<String, SoftReference<Bitmap>>();//存储Bitmap的软引用
	private Map<String, List<ImageView>> backmap = new HashMap<String, List<ImageView>>();//list中存放的是等待设置图片的ImageView

	private ImgAsyncDownload()
	{
	}

	private static final ImgAsyncDownload imgDownloader = new ImgAsyncDownload();

	/**
	 * 单例模式
	 * 
	 * @return
	 */
	public static ImgAsyncDownload getInstance()
	{
		return imgDownloader;
	}

	/**
	 * 加载图片
	 * 
	 * @param imageview
	 *            需要加载图片的imageview，URL存在tag中，根据URL去加载
	 */
	public void imageDownload(ImageView imageview)
	{
		String picUrl = null;
		if (imageview != null && (picUrl = (String) imageview.getTag()) != null)
		{
			SoftReference<Bitmap> mBitmap = imageCaches.get(picUrl);
			Bitmap softRefeBitmap = null;
			if (mBitmap != null)
			{
				softRefeBitmap = mBitmap.get();// 可能已经被回收(可能为null)
			}
			if (softRefeBitmap != null)
			{
				imageview.setImageBitmap(softRefeBitmap);
			}
			else
			{
				String imageName = picUrl
						.substring(picUrl.lastIndexOf("/") + 1);
				Bitmap bitmap = null;
				if ((bitmap = BitmapUtil.getBitmapFromFile(imageName)) != null)
				{
					imageview.setImageBitmap(bitmap);
					imageCaches.put(picUrl, new SoftReference<Bitmap>(bitmap));
				}
				else
				{
					if (isNeedCreateTask(picUrl))
					{
						MyAsyncTask task = new MyAsyncTask(picUrl);
						task.execute();
						// 将对应的url对应的任务存起来
						map.put(picUrl, task);
						// 将请求图片的imageview存在list中等待设置
						List<ImageView> imgviewList = new ArrayList<ImageView>();
						imgviewList.add(imageview);

						backmap.put(picUrl, imgviewList);
					}
					else
					{
						List<ImageView> imgviewList = backmap.get(picUrl);
						if (imgviewList != null)// 已经
												// 创建了一个List<ImageView>,并且其中含有至少一个等待设置的imageview
						{
							if (!imgviewList.contains(imageview))// 判断是否已经有了该imageview
							{
								imgviewList.add(imageview);// 将该imageview加入到list中
							}
						}
					}
				}
			}
		}
	}

	/**
	 * 判断是否要重新创建异步线程下载图片默认为true。
	 * 
	 * @param picUrl
	 *            图片URL
	 * @return
	 */
	private boolean isNeedCreateTask(String picUrl)
	{
		boolean isneed = true;
		MyAsyncTask mtask = map.get(picUrl);
		if (mtask != null)
		{
			isneed = false;
		}
		return isneed;
	}

	/**
	 * task完成后，为其他具有相同tag的imageview设置图片
	 * 
	 * @param tag
	 *            为图片设置的URL
	 * @param bitmap
	 *            异步加载完成后返回的bitmap数据
	 */
	private void setImage(String tag, Bitmap bitmap)
	{
		List<ImageView> imgviewList = backmap.get(tag);
		if (imgviewList != null)
		{
			for (int i = 0; i < imgviewList.size(); i++)
			{
				// 必须判断，否则会错乱
				if (tag.equals(imgviewList.get(i).getTag()))
				{
					imgviewList.get(i).setImageBitmap(bitmap);
				}

			}
		}

		backmap.remove(tag);
	}

	/**
	 * 异步下载图片的方法
	 * 
	 * @author yanbin
	 * 
	 */
	private class MyAsyncTask extends AsyncTask<String, Void, Bitmap>
	{
		private String picUrl;

		public MyAsyncTask(String picPath)
		{
			this.picUrl = picPath;
		}

		@Override
		protected Bitmap doInBackground(String... params)
		{
			Bitmap data = null;

			try
			{
				data = BitmapUtil.getBitmapFromNet(picUrl);
				if (data != null)
				{
					imageCaches.put(picUrl, new SoftReference<Bitmap>(data));
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

			return data;
		}

		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(Bitmap result)
		{
			// 没有加载成功不进行设置
			if (result != null)
			{
				setImage(picUrl, result);
			}

			// 该url对应的task已经下载完成，从map中将其删除
			if (map != null && map.get(picUrl) != null)
			{
				map.remove(picUrl);
			}

			super.onPostExecute(result);
		}

	}
}
