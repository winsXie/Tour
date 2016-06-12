package com.bn.tour;

import com.bn.tour.R;
import com.bn.util.BitmapUtil;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

/**
 * ��ʾѡ���ͼƬgridview��������
 * 
 */
public class GridAddPicAdapter extends BaseAdapter {
	private LayoutInflater inflater; // ��ͼ����
	private Context context;
	private Handler handler;

	public GridAddPicAdapter(Context context, Handler handler) {
		this.context = context;
		this.inflater = LayoutInflater.from(context);
		this.handler = handler;
	}

	/**
	 * ������ʾ��ͼƬ
	 */
	public void update() {
		loading();
	}

	public int getCount() {
		return (BitmapUtil.bmplist.size() + 1);
	}

	public Object getItem(int arg0) {
		return null;
	}

	public long getItemId(int arg0) {
		return 0;
	}

	/**
	 * ListView Item����
	 */
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.item_publish_pic, parent,
					false);
			holder = new ViewHolder();
			holder.image = (ImageView) convertView
					.findViewById(R.id.item_grida_image);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		if (position == BitmapUtil.bmplist.size()) {

			if (position == BitmapUtil.maxCount) {
				holder.image.setVisibility(View.GONE);
			} else {// view����ʱҪע�⣬����ĳ�����Ͳ���������������£�һ��Ҫ����ȫ�棨view�Ƿ�ɼ���ͼƬ��������ܵ���ͼƬ���һ�ɼ��Բ���
				holder.image.setVisibility(View.VISIBLE);
				holder.image.setImageBitmap(BitmapFactory.decodeResource(
						context.getResources(),
						R.drawable.icon_addpic_unfocused));
			}
		} else {
			holder.image.setVisibility(View.VISIBLE);
			holder.image.setImageBitmap(BitmapUtil.bmplist.get(position));
		}

		return convertView;
	}

	public class ViewHolder {
		public ImageView image;
	}

	public void loading() {
		new Thread(new Runnable() {
			public void run() {
				while (true) {
					if (BitmapUtil.lastSize == BitmapUtil.pathlist.size()) {// ����viewpage����ʱҪ���˾������½���
						Message message = new Message();
						message.what = 1;
						handler.sendMessage(message);
						break;
					} else {
						try {// ֻ������µ�ͼƬ������ѡ���ͼƬ��ʼ��ӣ���֮ǰѡ��Ĳ����������
							String path = BitmapUtil.pathlist
									.get(BitmapUtil.lastSize);
							Bitmap bm = BitmapUtil.revitionImageSize(path);// ��Ҫ�ς����������ϵĈDƬ
							String name = path.substring(path.lastIndexOf("/"),
									path.length());
							BitmapUtil.saveTempPic(bm, name);

							BitmapUtil.bmplist.add(bm);
							BitmapUtil.lastSize += 1;

						} catch (Exception e) {

							e.printStackTrace();
						}
					}
				}
			}
		}).start();
	}
}
