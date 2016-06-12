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
 * 显示选择的图片gridview的适配器
 * 
 */
public class GridAddPicAdapter extends BaseAdapter {
	private LayoutInflater inflater; // 视图容器
	private Context context;
	private Handler handler;

	public GridAddPicAdapter(Context context, Handler handler) {
		this.context = context;
		this.inflater = LayoutInflater.from(context);
		this.handler = handler;
	}

	/**
	 * 更新显示的图片
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
	 * ListView Item设置
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
			} else {// view重用时要注意，满足某条件和不满足条件的情况下，一定要设置全面（view是否可见、图片）否则可能导致图片错乱或可见性不对
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
					if (BitmapUtil.lastSize == BitmapUtil.pathlist.size()) {// 进入viewpage返回时要靠此句代码更新界面
						Message message = new Message();
						message.what = 1;
						handler.sendMessage(message);
						break;
					} else {
						try {// 只添加最新的图片（从新选择的图片开始添加），之前选择的不再重新添加
							String path = BitmapUtil.pathlist
									.get(BitmapUtil.lastSize);
							Bitmap bm = BitmapUtil.revitionImageSize(path);// 將要上傳到服務器上的圖片
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
