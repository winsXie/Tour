package com.bn.getnativepicture;

import java.io.Serializable;
import java.util.List;

import com.bn.tour.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.AdapterView.OnItemClickListener;

public class PicAlbumActivity extends Activity
{
	private List<ImageBucket> dataList;//����б�
	private GridView gridView;//��ʾ����GridView
	private ImageBucketAdapter adapter;// �Զ����������
	private AlbumHelper helper;//���ϵͳͼƬ��Ϣ��

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image_bucket);

		helper = AlbumHelper.getHelper();
		helper.init(getApplicationContext());

		initData();
		initView();

	}

	//���ϵͳͼƬ��Ϣ
	private void initData()
	{
		dataList = helper.getImagesBucketList(false);
	}

	//��ʼ��GridView
	private void initView()
	{
		gridView = (GridView) findViewById(R.id.gridview);
		adapter = new ImageBucketAdapter(PicAlbumActivity.this, dataList);
		gridView.setAdapter(adapter);

		gridView.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{
				Intent intent = new Intent(PicAlbumActivity.this,
						ImageGridActivity.class);
				intent.putExtra("imagelist",
						(Serializable) dataList.get(position).imageList);
				startActivity(intent);
				finish();
			}

		});
	}
}
