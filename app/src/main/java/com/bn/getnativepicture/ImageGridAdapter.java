package com.bn.getnativepicture;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bn.getnativepicture.BitmapCache.ImageCallback;
import com.bn.tour.R;
import com.bn.util.BitmapUtil;

/**
 * 显示系统图库专辑内的图片
 * 
 */
public class ImageGridAdapter extends BaseAdapter
{

	private TextCallback textcallback = null;// 选择图片个数回调接口
	private Activity act;
	private List<ImageItem> dataList;//图片信息列表
	// 用LinkedHashMap保证插入集合的先后顺序,不用HashMap,不能保证顺序
	public Map<String, String> map = new LinkedHashMap<String, String>();
	private BitmapCache cache;
	private Handler mHandler;
	private int selectTotal = 0;//当前总共选择的图片个数
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

	// 选择图片个数回调接口
	public static interface TextCallback
	{
		public void onListen(int count);
	}

	//设置选择个数接口
	public void setTextCallback(TextCallback listener)
	{
		textcallback = listener;
	}

	public ImageGridAdapter(Activity act, List<ImageItem> list, Handler mHandler)
	{
		this.act = act;
		dataList = list;
		cache = new BitmapCache();
		this.mHandler = mHandler;
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
	public Object getItem(int position)
	{
		return null;
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	class Holder
	{
		private ImageView iv;
		private ImageView selected;
		private TextView text;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent)
	{
		final Holder holder;

		if (convertView == null)
		{
			holder = new Holder();
			convertView = View.inflate(act, R.layout.item_image_grid, null);
			holder.iv = (ImageView) convertView.findViewById(R.id.image);
			holder.selected = (ImageView) convertView
					.findViewById(R.id.isselected);
			holder.text = (TextView) convertView
					.findViewById(R.id.item_image_grid_text);
			convertView.setTag(holder);
		}
		else
		{
			holder = (Holder) convertView.getTag();
		}
		final ImageItem item = dataList.get(position);

		holder.iv.setTag(item.imagePath);
		holder.iv.setImageResource(R.drawable.default_picture);
		cache.displayBmp(holder.iv, item.thumbnailPath, item.imagePath,
				callback);
		if (item.isSelected)
		{
			holder.selected.setImageResource(R.drawable.icon_data_select);
			holder.text.setBackgroundResource(R.drawable.bgd_relatly_line);
		}
		else
		{
			holder.selected.setImageResource(-1);
			holder.text.setBackgroundColor(0x00000000);
		}
		holder.iv.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				String path = dataList.get(position).imagePath;
				File file = new File(path);
				if (file.exists())
				{
					if ((BitmapUtil.pathlist.size() + selectTotal) < BitmapUtil.maxCount)
					{
						item.isSelected = !item.isSelected;
						if (item.isSelected)
						{
							holder.selected
									.setImageResource(R.drawable.icon_data_select);
							holder.text
									.setBackgroundResource(R.drawable.bgd_relatly_line);
							selectTotal++;
							if (textcallback != null)
								textcallback.onListen(BitmapUtil.pathlist.size() + selectTotal);
							map.put(path, path);

						}
						else if (!item.isSelected)
						{
							holder.selected.setImageResource(-1);
							holder.text.setBackgroundColor(0x00000000);
							selectTotal--;
							if (textcallback != null)
								textcallback.onListen(BitmapUtil.pathlist.size() + selectTotal);
							map.remove(path);
						}
					}
					else if ((BitmapUtil.pathlist.size() + selectTotal) >= BitmapUtil.maxCount)
					{
						if (item.isSelected == true)
						{// 取消选中的图片
							item.isSelected = !item.isSelected;
							holder.selected.setImageResource(-1);
							holder.text.setBackgroundColor(0x00000000);
							selectTotal--;
							if (textcallback != null)
								textcallback.onListen(BitmapUtil.pathlist.size() + selectTotal);
							map.remove(path);

						}
						else
						{
							Message message = Message.obtain(mHandler, 0);
							message.sendToTarget();
						}
					}
				}
				else
				{
					Toast.makeText(act, "文件不存在！", Toast.LENGTH_SHORT).show();
				}
			}

		});

		return convertView;
	}
}
