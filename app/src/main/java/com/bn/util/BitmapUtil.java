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

	// ���ļ��л�ȡͼƬ
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

	// ���ͼƬ��ֱ�Ӵ������ȡ(���ԭͼ)
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

	// �������л�ȡͼƬ(###ѹ��֮��洢������###)
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
				data = comp(data);// ѹ��ͼƬ
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

	// ��ͼƬ�浽����(����SD��)
	public static boolean setBitmapToFile(String cachePath, String imageName,
			Bitmap bitmap)
	{
		if (imageName != null)
		{
			String dirpath = Constant.sdRootPath + cachePath;
			// ���жϻ���Ŀ¼�Ƿ���ڣ��������򴴽�Ŀ¼
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

	// �ӱ���ɾ��ͼƬ�ļ�(�浽����δ�ɹ�ʱ)
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

	// �洢��Ҫ�ϴ����Ѿ�ѹ����ͼƬ
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

	// ɾ����ʱ�洢���ϴ�ͼƬ
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

	// ѹ��ͼƬ(�ϴ�ʹ��)
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

	// ����ѹ������ ===������浽�����أ��ڶ�����
	public static Bitmap compressImage(Bitmap image)
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.JPEG, 100, baos);// ����ѹ������������100��ʾ��ѹ������ѹ��������ݴ�ŵ�baos��
		int options = 100;
		while (baos.toByteArray().length / 1024 > 100)
		{ // ѭ���ж����ѹ����ͼƬ�Ƿ����100kb,���ڼ���ѹ��
			baos.reset();// ����baos�����baos
			options -= 10;// ÿ�ζ�����10
			image.compress(Bitmap.CompressFormat.JPEG, options, baos);// ����ѹ��options%����ѹ��������ݴ�ŵ�baos��

		}
		ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());// ��ѹ���������baos��ŵ�ByteArrayInputStream��
		Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);// ��ByteArrayInputStream��������ͼƬ
		return bitmap;
	}

	// ͼƬ��������Сѹ������������BitmapͼƬѹ����===������浽�����أ���һ����
	public static Bitmap comp(Bitmap image)
	{

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		while (baos.toByteArray().length / 1024 > 300)
		{// �ж����ͼƬ����300kb,����ѹ������������ͼƬ��BitmapFactory.decodeStream��ʱ���
			baos.reset();// ����baos�����baos
			image.compress(Bitmap.CompressFormat.JPEG, 50, baos);// ����ѹ��50%����ѹ��������ݴ�ŵ�baos��
		}
		ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
		BitmapFactory.Options newOpts = new BitmapFactory.Options();
		// ��ʼ����ͼƬ����ʱ��options.inJustDecodeBounds ���true��
		newOpts.inJustDecodeBounds = true;
		Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
		newOpts.inJustDecodeBounds = false;
		int w = newOpts.outWidth;
		int h = newOpts.outHeight;
		// ���������ֻ��Ƚ϶���800*480�ֱ��ʣ����ԸߺͿ���������Ϊ
		float hh = 800f;// �������ø߶�Ϊ800f
		float ww = 480f;// �������ÿ��Ϊ480f
		// ���űȡ������ǹ̶��������ţ�ֻ�ø߻��߿�����һ�����ݽ��м��㼴��
		int be = 1;// be=1��ʾ������
		if (w >= h && w > ww)
		{// �����ȴ�Ļ����ݿ�ȹ̶���С����
			be = (int) (newOpts.outWidth / ww);
		} else if (w < h && h > hh)
		{// ����߶ȸߵĻ����ݿ�ȹ̶���С����
			be = (int) (newOpts.outHeight / hh);
		}
		if (be <= 0)
			be = 1;
		newOpts.inSampleSize = be;// �������ű���
		// ���¶���ͼƬ��ע���ʱ�Ѿ���options.inJustDecodeBounds ���false��
		isBm = new ByteArrayInputStream(baos.toByteArray());
		bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
		return compressImage(bitmap);// ѹ���ñ�����С���ٽ�������ѹ��
	}

	/**
	 * ��ȡͼƬ���ԣ���ת�ĽǶ�
	 * 
	 * @param path
	 *            ͼƬ����·��
	 * @return degree��ת�ĽǶ�
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
	 * ��תͼƬ
	 */
	public static Bitmap rotaingImageView(int angle, Bitmap bitmap)
	{
		// ��תͼƬ ����
		Matrix matrix = new Matrix();
		matrix.postRotate(angle);
		// �����µ�ͼƬ
		Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
				bitmap.getWidth(), bitmap.getHeight(), matrix, true);
		return resizedBitmap;
	}
}