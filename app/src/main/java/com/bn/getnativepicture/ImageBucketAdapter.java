package com.bn.getnativepicture;

import java.util.List;

import com.bn.getnativepicture.BitmapCache.ImageCallback;
import com.bn.tour.R;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 显示系统图库专辑
 * 
 */
public class ImageBucketAdapter extends BaseAdapter
{
	private Activity act;

	private List<ImageBucket> dataList;//相册列表
	private BitmapCache cache;
	//显示图片回调接口
	private ImageCallback callback = new ImageCallback()
	{
		@Override
		public void imageLoad(ImageView imageView, Bitmap bitmap,
				Object... params)
		{
			if (imageView != null && bitmap != null)
			{
				String url = (String) params[0];
				if (url != null && url.equals((String) imageView.getTag()))
				{
					((ImageView) imageView).setImageBitmap(bitmap);
				}
			}
		}
	};

	public ImageBucketAdapter(Activity act, List<ImageBucket> list)
	{
		this.act = act;
		dataList = list;
		cache = new BitmapCache();
	}

	@Override
	public int getCount()
	{
		int count = 0;
		if (dataList != null)
		{
			count = dataList.size();
		}
		return count;
	}

	@Override
	public Object getItem(int arg0)
	{
		return null;
	}

	@Override
	public long getItemId(int arg0)
	{
		return arg0;
	}

	class Holder
	{
		private ImageView iv;
		private TextView name;
		private TextView count;
	}

	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2)
	{
		Holder holder;
		if (arg1 == null)
		{
			holder = new Holder();
			arg1 = View.inflate(act, R.layout.item_image_bucket, null);
			holder.iv = (ImageView) arg1.findViewById(R.id.image);
			holder.name = (TextView) arg1.findViewById(R.id.name);
			holder.count = (TextView) arg1.findViewById(R.id.count);
			arg1.setTag(holder);
		}
		else
		{
			holder = (Holder) arg1.getTag();
		}
		ImageBucket item = dataList.get(arg0);
		holder.count.setText("" + item.count);
		holder.name.setText(item.bucketName);
		if (item.imageList != null && item.imageList.size() > 0)
		{
			String thumbPath = item.imageList.get(0).thumbnailPath;// 获得该图片集中第一个图片的缩略图路径
			String sourcePath = item.imageList.get(0).imagePath;// 获得该图片集中第一个图片的原图路径
			holder.iv.setTag(sourcePath);
			cache.displayBmp(holder.iv, thumbPath, sourcePath, callback);
		}
		else
		{
			holder.iv.setImageBitmap(null);
		}
		return arg1;
	}

}
