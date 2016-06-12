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
 * �����б�ͼƬ�ļ��أ�ʹ���ڴ�ͱ���˫���棬����tag����������ͼƬ����ֹ����
 * 
 */
public class ImgAsyncDownload
{
	private HashMap<String, MyAsyncTask> map = new HashMap<String, MyAsyncTask>();//�洢AsyncTask��map
	private Map<String, SoftReference<Bitmap>> imageCaches = new HashMap<String, SoftReference<Bitmap>>();//�洢Bitmap��������
	private Map<String, List<ImageView>> backmap = new HashMap<String, List<ImageView>>();//list�д�ŵ��ǵȴ�����ͼƬ��ImageView

	private ImgAsyncDownload()
	{
	}

	private static final ImgAsyncDownload imgDownloader = new ImgAsyncDownload();

	/**
	 * ����ģʽ
	 * 
	 * @return
	 */
	public static ImgAsyncDownload getInstance()
	{
		return imgDownloader;
	}

	/**
	 * ����ͼƬ
	 * 
	 * @param imageview
	 *            ��Ҫ����ͼƬ��imageview��URL����tag�У�����URLȥ����
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
				softRefeBitmap = mBitmap.get();// �����Ѿ�������(����Ϊnull)
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
						// ����Ӧ��url��Ӧ�����������
						map.put(picUrl, task);
						// ������ͼƬ��imageview����list�еȴ�����
						List<ImageView> imgviewList = new ArrayList<ImageView>();
						imgviewList.add(imageview);

						backmap.put(picUrl, imgviewList);
					}
					else
					{
						List<ImageView> imgviewList = backmap.get(picUrl);
						if (imgviewList != null)// �Ѿ�
												// ������һ��List<ImageView>,�������к�������һ���ȴ����õ�imageview
						{
							if (!imgviewList.contains(imageview))// �ж��Ƿ��Ѿ����˸�imageview
							{
								imgviewList.add(imageview);// ����imageview���뵽list��
							}
						}
					}
				}
			}
		}
	}

	/**
	 * �ж��Ƿ�Ҫ���´����첽�߳�����ͼƬĬ��Ϊtrue��
	 * 
	 * @param picUrl
	 *            ͼƬURL
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
	 * task��ɺ�Ϊ����������ͬtag��imageview����ͼƬ
	 * 
	 * @param tag
	 *            ΪͼƬ���õ�URL
	 * @param bitmap
	 *            �첽������ɺ󷵻ص�bitmap����
	 */
	private void setImage(String tag, Bitmap bitmap)
	{
		List<ImageView> imgviewList = backmap.get(tag);
		if (imgviewList != null)
		{
			for (int i = 0; i < imgviewList.size(); i++)
			{
				// �����жϣ���������
				if (tag.equals(imgviewList.get(i).getTag()))
				{
					imgviewList.get(i).setImageBitmap(bitmap);
				}

			}
		}

		backmap.remove(tag);
	}

	/**
	 * �첽����ͼƬ�ķ���
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
			// û�м��سɹ�����������
			if (result != null)
			{
				setImage(picUrl, result);
			}

			// ��url��Ӧ��task�Ѿ�������ɣ���map�н���ɾ��
			if (map != null && map.get(picUrl) != null)
			{
				map.remove(picUrl);
			}

			super.onPostExecute(result);
		}

	}
}
