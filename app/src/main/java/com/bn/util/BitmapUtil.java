package com.bn.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;

public class BitmapUtil
{
	public static int maxCount;
	public static int lastSize = 0;
	public static List<Bitmap> bmplist = new ArrayList<Bitmap>();
	public static List<String> pathlist = new ArrayList<String>();
	public static List<String> templist = new ArrayList<String>();

	// 从文件中获取图片
	public static Bitmap getBitmapFromFile(String imageName)
	{
		Bitmap bitmap = null;
		if (imageName != null)
		{
			File file = null;

			try
			{
				if (Constant.hasSDcard)
				{
					String filepath = Constant.sdRootPath + Constant.cachePath
							+ "/" + imageName;
					file = new File(filepath);
					if (file.exists())
					{
						bitmap = BitmapFactory
								.decodeStream(new FileInputStream(file));
					}
				}
			} catch (Exception e)
			{
				e.printStackTrace();
				bitmap = null;
			}
		}
		return bitmap;
	}

	// 浏览图片，直接从网络获取(浏览原图)
	public static Bitmap scanPicture(String picurl)
	{
		Bitmap data = null;

		String imageName = picurl.substring(picurl.lastIndexOf("/") + 1);

		try
		{
			URL url = new URL("http://" + Constant.severIp
					+ ":8080/tourServer/" + picurl);
			URLConnection conn = url.openConnection();
			conn.setConnectTimeout(3 * 1000);
			conn.setReadTimeout(10 * 1000);
			InputStream bitmap_data = url.openStream();
			data = BitmapFactory.decodeStream(bitmap_data);
			setBitmapToFile(Constant.sourcePic, imageName, data);
		} catch (Exception e)
		{
			e.printStackTrace();
			data = null;
		}

		return data;

	}

	// 从网络中获取图片(###压缩之后存储到本地###)
	public static Bitmap getBitmapFromNet(String picPath)
	{
		Bitmap data = null;

		try
		{
			URL url = new URL("http://" + Constant.severIp
					+ ":8080/tourServer/" + picPath);
			URLConnection conn = url.openConnection();
			conn.setConnectTimeout(2 * 1000);
			conn.setReadTimeout(3 * 1000);
			InputStream bitmap_data = url.openStream();
			data = BitmapFactory.decodeStream(bitmap_data);
			if (data != null)
			{
				data = comp(data);// 压缩图片
			}
		} catch (Exception e)
		{
			e.printStackTrace();
			data = null;
		}

		if (Constant.hasSDcard && data != null)
		{

			String imageName = picPath.substring(picPath.lastIndexOf("/") + 1);
			if (!setBitmapToFile(Constant.cachePath, imageName, data))
			{
				delFailedFile(Constant.cachePath, imageName);
			}
		}

		return data;
	}

	// 将图片存到本地(存在SD卡)
	public static boolean setBitmapToFile(String cachePath, String imageName,
			Bitmap bitmap)
	{
		if (imageName != null)
		{
			String dirpath = Constant.sdRootPath + cachePath;
			// 先判断缓存目录是否存在，不存在则创建目录
			File dirfile = new File(dirpath + "/");
			if (!dirfile.exists())
			{
				dirfile.mkdirs();
			}

			File picfile = new File(dirpath, imageName);
			FileOutputStream fos = null;

			try
			{
				fos = new FileOutputStream(picfile);
				if (imageName.contains(".png") || imageName.contains(".PNG"))
				{
					bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
				} else
				{
					bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
				}
				fos.flush();
				if (fos != null)
				{
					fos.close();
				}
				return true;
			} catch (Exception e)
			{
				e.printStackTrace();
				return false;
			}
		} else
		{
			return false;
		}
	}

	// 从本地删除图片文件(存到本地未成功时)
	public static void delFailedFile(String cachePath, String imageName)
	{
		if (cachePath != null && imageName != null)
		{
			File file = new File(Constant.sdRootPath + cachePath, imageName);
			if (file != null && file.exists())
			{
				file.delete();
			}
		}
	}

	// 存储将要上传的已经压缩的图片
	public static void saveTempPic(Bitmap bitmap, String name)
	{
		File dir = new File(Constant.sdRootPath + Constant.tempPath);
		if (!dir.exists())
		{
			dir.mkdirs();
		}
		File file = new File(Constant.sdRootPath + Constant.tempPath + name);
		if (file.exists())
		{
			file.delete();
		}

		try
		{
			FileOutputStream out = new FileOutputStream(file);
			if (name.contains(".png") || name.contains(".PNG"))
			{
				bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
			} else
			{
				bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
			}
			out.flush();
			if (out != null)
			{
				out.close();
				templist.add(Constant.sdRootPath + Constant.tempPath + name);
			}
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	// 删除临时存储的上传图片
	public static void delTempPic()
	{
		File tempfile = new File(Constant.sdRootPath + Constant.tempPath);
		if (tempfile.exists())
		{
			for (File file : tempfile.listFiles())
			{
				if (file.isFile() && file.exists())
				{
					file.delete();
				}
			}
		}
	}

	// 压缩图片(上传使用)
	public static Bitmap revitionImageSize(String path) throws IOException
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
			if ((options.outWidth >> i <= 720)
					&& (options.outHeight >> i <= 1024))
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

	// 质量压缩方法 ===从网络存到到本地（第二步）
	public static Bitmap compressImage(Bitmap image)
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.JPEG, 100, baos);// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
		int options = 100;
		while (baos.toByteArray().length / 1024 > 100)
		{ // 循环判断如果压缩后图片是否大于100kb,大于继续压缩
			baos.reset();// 重置baos即清空baos
			options -= 10;// 每次都减少10
			image.compress(Bitmap.CompressFormat.JPEG, options, baos);// 这里压缩options%，把压缩后的数据存放到baos中

		}
		ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());// 把压缩后的数据baos存放到ByteArrayInputStream中
		Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);// 把ByteArrayInputStream数据生成图片
		return bitmap;
	}

	// 图片按比例大小压缩方法（根据Bitmap图片压缩）===从网络存到到本地（第一步）
	public static Bitmap comp(Bitmap image)
	{

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		while (baos.toByteArray().length / 1024 > 300)
		{// 判断如果图片大于300kb,进行压缩避免在生成图片（BitmapFactory.decodeStream）时溢出
			baos.reset();// 重置baos即清空baos
			image.compress(Bitmap.CompressFormat.JPEG, 50, baos);// 这里压缩50%，把压缩后的数据存放到baos中
		}
		ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
		BitmapFactory.Options newOpts = new BitmapFactory.Options();
		// 开始读入图片，此时把options.inJustDecodeBounds 设回true了
		newOpts.inJustDecodeBounds = true;
		Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
		newOpts.inJustDecodeBounds = false;
		int w = newOpts.outWidth;
		int h = newOpts.outHeight;
		// 现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
		float hh = 800f;// 这里设置高度为800f
		float ww = 480f;// 这里设置宽度为480f
		// 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
		int be = 1;// be=1表示不缩放
		if (w >= h && w > ww)
		{// 如果宽度大的话根据宽度固定大小缩放
			be = (int) (newOpts.outWidth / ww);
		} else if (w < h && h > hh)
		{// 如果高度高的话根据宽度固定大小缩放
			be = (int) (newOpts.outHeight / hh);
		}
		if (be <= 0)
			be = 1;
		newOpts.inSampleSize = be;// 设置缩放比例
		// 重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
		isBm = new ByteArrayInputStream(baos.toByteArray());
		bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
		return compressImage(bitmap);// 压缩好比例大小后再进行质量压缩
	}

	/**
	 * 读取图片属性：旋转的角度
	 * 
	 * @param path
	 *            图片绝对路径
	 * @return degree旋转的角度
	 */
	public static int readPictureDegree(String path)
	{
		int degree = 0;
		try
		{
			ExifInterface exifInterface = new ExifInterface(path);
			int orientation = exifInterface.getAttributeInt(
					ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);
			switch (orientation)
			{
			case ExifInterface.ORIENTATION_ROTATE_90:
				degree = 90;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				degree = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				degree = 270;
				break;
			}
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return degree;
	}

	/*
	 * 旋转图片
	 */
	public static Bitmap rotaingImageView(int angle, Bitmap bitmap)
	{
		// 旋转图片 动作
		Matrix matrix = new Matrix();
		matrix.postRotate(angle);
		// 创建新的图片
		Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
				bitmap.getWidth(), bitmap.getHeight(), matrix, true);
		return resizedBitmap;
	}
}