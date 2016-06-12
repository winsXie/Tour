package com.bn.getnativepicture;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.ImageView;

/**
 * ϵͳͼ��ͼƬ����ʾ �ȼ��Ҫ��ʾ��ͼƬ�Ƿ���ר��ͼƬ�������ֱ����ʾ����ͼ�� ����Ҫ����ԭͼƬ��·��ѹ��ͼƬ������ʾ
 */
public class BitmapCache
{
	private Handler h = new Handler();
	private HashMap<String, SoftReference<Bitmap>> imageCache = new HashMap<String, SoftReference<Bitmap>>();

	private void put(String path, Bitmap bmp)
	{
		if (!TextUtils.isEmpty(path) && bmp != null)
		{
			imageCache.put(path, new SoftReference<Bitmap>(bmp));
		}
	}

	/**
	 * ��ʾͼƬ
	 * 
	 * @param iv
	 *            Ҫ��ʾͼƬ��imageview
	 * @param thumbPath
	 *            ����ר������ͼ��·��
	 * @param sourcePath
	 *            ͼƬԭ·��
	 * @param callback
	 *            ��ʾͼƬ�ص��ӿ�
	 */
	public void displayBmp(final ImageView iv, final String thumbPath,
			final String sourcePath, final ImageCallback callback)
	{
		if (TextUtils.isEmpty(thumbPath) && TextUtils.isEmpty(sourcePath))
		{
			return;
		}

		final String path;
		final boolean isThumbPath;
		if (!TextUtils.isEmpty(thumbPath))
		{
			path = thumbPath;
			isThumbPath = true;
		}
		else if (!TextUtils.isEmpty(sourcePath))
		{
			path = sourcePath;
			isThumbPath = false;
		}
		else
		{
			return;
		}

		if (imageCache.containsKey(path))
		{
			SoftReference<Bitmap> reference = imageCache.get(path);
			Bitmap bmp = reference.get();
			if (bmp != null)
			{
				iv.setImageBitmap(bmp);
				return;
			}
		}

		new Thread()
		{
			Bitmap thumb;

			public void run()
			{

				try
				{
					if (isThumbPath)
					{// ����ͼ��ȡ����ͼ
						thumb = BitmapFactory.decodeFile(thumbPath);
						if (thumb == null)
						{
							thumb = revitionImageSize(sourcePath);
						}
					}
					else
					{
						thumb = revitionImageSize(sourcePath);
					}
				}
				catch (Exception e)
				{
				}
				put(path, thumb);

				if (callback != null)
				{
					h.post(new Runnable()
					{
						@Override
						public void run()
						{
							callback.imageLoad(iv, thumb, sourcePath);
						}
					});
				}
			}
		}.start();

	}

	/**
	 * ѹ��ͼƬ
	 * 
	 * @param pathͼƬ��·��
	 * @return ѹ�����Bitmap
	 * @throws IOException
	 */
	private Bitmap revitionImageSize(String path) throws IOException
	{
		BufferedInputStream in = new BufferedInputStream(new FileInputStream(
				new File(path)));
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(in, null, options);
		in.close();
		int i = 0;
		Bitmap bitmap = null;
		while (true)
		{
			if ((options.outWidth >> i <= 256)
					&& (options.outHeight >> i <= 256))
			{
				in = new BufferedInputStream(
						new FileInputStream(new File(path)));
				options.inSampleSize = (int) Math.pow(2.0D, i);
				options.inJustDecodeBounds = false;
				bitmap = BitmapFactory.decodeStream(in, null, options);
				break;
			}
			i += 1;
		}
		return bitmap;
	}

	/**
	 * ͼƬ��ʾ�ص��ӿ�
	 */
	public interface ImageCallback
	{
		public void imageLoad(ImageView imageView, Bitmap bitmap,
				Object... params);
	}
}
