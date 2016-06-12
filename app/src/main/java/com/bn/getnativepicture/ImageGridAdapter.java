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
 * ��ʾϵͳͼ��ר���ڵ�ͼƬ
 * 
 */
public class ImageGridAdapter extends BaseAdapter
{

	private TextCallback textcallback = null;// ѡ��ͼƬ�����ص��ӿ�
	private Activity act;
	private List<ImageItem> dataList;//ͼƬ��Ϣ�б�
	// ��LinkedHashMap��֤���뼯�ϵ��Ⱥ�˳��,����HashMap,���ܱ�֤˳��
	public Map<String, String> map = new LinkedHashMap<String, String>();
	private BitmapCache cache;
	private Handler mHandler;
	private int selectTotal = 0;//��ǰ�ܹ�ѡ���ͼƬ����
	//��ʾͼƬ�ص��ӿ�
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

	// ѡ��ͼƬ�����ص��ӿ�
	public static interface TextCallback
	{
		public void onListen(int count);
	}

	//����ѡ������ӿ�
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
						{// ȡ��ѡ�е�ͼƬ
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
					Toast.makeText(act, "�ļ������ڣ�", Toast.LENGTH_SHORT).show();
				}
			}

		});

		return convertView;
	}
}
