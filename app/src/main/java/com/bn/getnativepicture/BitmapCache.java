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
 * 系统图库图片的显示 先检查要显示的图片是否是专辑图片，如果是直接显示缩略图， 否则要根据原图片的路径压缩图片进行显示
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
	 * 显示图片
	 * 
	 * @param iv
	 *            要显示图片的imageview
	 * @param thumbPath
	 *            所在专辑缩略图的路径
	 * @param sourcePath
	 *            图片原路径
	 * @param callback
	 *            显示图片回调接口
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
					{// 先试图获取缩略图
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
	 * 压缩图片
	 * 
	 * @param path图片的路径
	 * @return 压缩后的Bitmap
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
	 * 图片显示回调接口
	 */
	public interface ImageCallback
	{
		public void imageLoad(ImageView imageView, Bitmap bitmap,
				Object... params);
	}
}
