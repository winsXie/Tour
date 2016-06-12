package com.bn.getnativepicture;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore.Images.Media;
import android.provider.MediaStore.Images.Thumbnails;

/**
 * 获得系统图库内的图片信息
 * 先查找专辑，得到专辑列表，包括专辑显示的缩略图，
 * 之后再遍历所有的图片，根据所在专辑分类，构造集合
 */
public class AlbumHelper
{
	private Context context;//应用程序上下文
	private ContentResolver cr;

	// 存放缩略图列表
	private HashMap<String, String> thumbnailList = new HashMap<String, String>();
	private HashMap<String, ImageBucket> bucketList = new HashMap<String, ImageBucket>();

	private static AlbumHelper instance;

	private AlbumHelper()
	{
	}

	public static AlbumHelper getHelper()
	{
		if (instance == null)
		{
			instance = new AlbumHelper();
		}
		return instance;
	}

	/**
	 * 初始化，获得ContentResolver
	 */
	public void init(Context context)
	{
		if (this.context == null)
		{
			this.context = context;
			cr = context.getContentResolver();
		}
	}

	/**
	 * 查询缩略图列表信息
	 */
	private void getThumbnail()
	{
		String[] projection = { Thumbnails._ID, Thumbnails.IMAGE_ID,
				Thumbnails.DATA };
		Cursor cursor = cr.query(Thumbnails.EXTERNAL_CONTENT_URI, projection,
				null, null, null);
		getThumbnailColumnData(cursor);
	}

	/**
	 * 获得详细数据
	 */
	private void getThumbnailColumnData(Cursor cur)
	{
		if (cur.moveToFirst())
		{
			int image_id;
			String image_path;
			int image_idColumn = cur.getColumnIndex(Thumbnails.IMAGE_ID);
			int dataColumn = cur.getColumnIndex(Thumbnails.DATA);

			do
			{
				image_id = cur.getInt(image_idColumn);
				image_path = cur.getString(dataColumn);
				thumbnailList.put("" + image_id, image_path);
			} while (cur.moveToNext());
		}
	}

	/**
	 * 初始化标志位
	 */
	boolean hasBuildImagesBucketList = false;

	/**
	 * 构建图片信息
	 */
	void buildImagesBucketList()
	{
		getThumbnail();

		// 查询所有图片的信息
		String columns[] = new String[] { Media._ID, Media.BUCKET_ID,
				Media.DATA, Media.BUCKET_DISPLAY_NAME };
		Cursor cur = cr.query(Media.EXTERNAL_CONTENT_URI, columns, null, null,
				Media.DATE_ADDED+" desc");
		if (cur.moveToFirst())
		{
			int photoIDIndex = cur.getColumnIndexOrThrow(Media._ID);
			int photoPathIndex = cur.getColumnIndexOrThrow(Media.DATA);
			int bucketDisplayNameIndex = cur
					.getColumnIndexOrThrow(Media.BUCKET_DISPLAY_NAME);
			int bucketIdIndex = cur.getColumnIndexOrThrow(Media.BUCKET_ID);

			// 将图片按照所在专辑归类（添加到bucket.imageList中）
			do
			{
				String _id = cur.getString(photoIDIndex);
				String path = cur.getString(photoPathIndex);
				String bucketName = cur.getString(bucketDisplayNameIndex);
				String bucketId = cur.getString(bucketIdIndex);

				ImageBucket bucket = bucketList.get(bucketId);
				if (bucket == null)
				{
					bucket = new ImageBucket();
					bucketList.put(bucketId, bucket);
					bucket.imageList = new ArrayList<ImageItem>();
					bucket.bucketName = bucketName;
				}
				bucket.count++;
				ImageItem imageItem = new ImageItem();
				imageItem.imageId = _id;
				imageItem.imagePath = path;
				/*
				 * 如果当前图片是所在专辑显示的图片，则thumbnailList.get(_id)结果不为空，
				 * 否则imageItem.thumbnailPath为null（在展示图片时会用到--->BitmapCache.displayBmp()）
				 */
				imageItem.thumbnailPath = thumbnailList.get(_id);
				bucket.imageList.add(imageItem);

			} while (cur.moveToNext());
		}

		hasBuildImagesBucketList = true;
	}

	/**
	 * 获得专辑列表
	 */
	public List<ImageBucket> getImagesBucketList(boolean refresh)
	{
		if (refresh || (!refresh && !hasBuildImagesBucketList))
		{
			buildImagesBucketList();
		}
		List<ImageBucket> tmpList = new ArrayList<ImageBucket>();
		Iterator<Entry<String, ImageBucket>> itr = bucketList.entrySet()
				.iterator();
		while (itr.hasNext())
		{
			Map.Entry<String, ImageBucket> entry = (Map.Entry<String, ImageBucket>) itr
					.next();
			tmpList.add(entry.getValue());
		}
		return tmpList;
	}
}
