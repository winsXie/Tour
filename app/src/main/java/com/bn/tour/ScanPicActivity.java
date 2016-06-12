package com.bn.tour;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bn.thread.LoadPicFromNet;
import com.bn.util.BitmapUtil;
import com.bn.util.Constant;

public class ScanPicActivity extends Activity {
    private ArrayList<View> listViews = null;//���ͼƬ��ͼ��list
    private SparseArray<LoadPicFromNet> sparse = new SparseArray<LoadPicFromNet>();//���LoadPicFromNet�̶߳����SparseArray
    private List<Bitmap> listbitmap = new ArrayList<Bitmap>();//���ͼƬ��Bitmap����
    private List<String> picpathList;//��������ͼƬ·��
    private ViewPager pager;
    private MyPageAdapter adapter;//viewpager������
    private int position = 0;//��ʾ�ڼ���ͼƬ��Ĭ��Ϊ0
    private String savePath = "/tour/savePic";//����ͼƬ���ֻ���·��
    private LoadPicFromNet loadThread;//ֱ�Ӵ��������ͼƬ�߳�
    private Handler handler;
    private TextView tvcount;//ͼƬ����
    private PhotoViewAttacher attacher;

    @SuppressLint("HandlerLeak")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanpicture);

//		listViews = new ArrayList<View>();
//		sparse = new SparseArray<LoadPicFromNet>();
//		listbitmap = new ArrayList<Bitmap>();

        handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                listbitmap.set(msg.what, (Bitmap) msg.obj);
                // ����ͼƬ
                PhotoView img = (PhotoView) listViews.get(msg.what)
                        .findViewById(R.id.IMVscanpic);
                img.setImageBitmap((Bitmap) msg.obj);
                attacher = new PhotoViewAttacher(img);
                attacher.update();
                // ȥ������ͼ��
                ((ProgressBar) listViews.get(msg.what).findViewById(
                        R.id.loadpicbar)).setVisibility(View.GONE);
                sparse.delete(msg.what);
                super.handleMessage(msg);
            }

        };

        int currposi = this.getIntent().getIntExtra("POSITION", 0);
        picpathList = (List<String>) this.getIntent().getStringArrayListExtra(
                "PATHLIST");

        for (int i = 0; i < picpathList.size(); i++) {
            if (listViews == null)
                listViews = new ArrayList<View>();
            View view = getLayoutInflater().inflate(R.layout.scanpicview, null);

            listViews.add(view);
            listbitmap.add(null);
        }

        RelativeLayout photo_relativeLayout = (RelativeLayout) findViewById(R.id.pho_relativeLayout02);
        photo_relativeLayout.setBackgroundColor(0x70000000);

        tvcount = (TextView) findViewById(R.id.TVpic_count);

        Button btsave = (Button) findViewById(R.id.BTsave_pic);
        btsave.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (listbitmap.get(position) != null) {
                    BitmapUtil.setBitmapToFile(
                            savePath,
                            picpathList.get(position)
                                    .substring(
                                            picpathList.get(position)
                                                    .lastIndexOf("/") + 1),
                            listbitmap.get(position));
                    Toast.makeText(
                            ScanPicActivity.this,
                            "ͼƬ�ѱ�����" + Constant.sdRootPath + savePath + "/�ļ�����",
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(ScanPicActivity.this, "ͼƬ����δ��ɣ�",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        pager = (ViewPager) findViewById(R.id.viewpager);

        adapter = new MyPageAdapter();
        pager.setAdapter(adapter);
        pager.setOnPageChangeListener(pageChangeListener);
        pager.setCurrentItem(currposi);
        if (currposi == 0) {
            tvcount.setText(1 + "/" + picpathList.size());
            loadPic(currposi);
        }
    }

    //viewpagerҳ��ı����
    private OnPageChangeListener pageChangeListener = new OnPageChangeListener() {

        public void onPageSelected(int arg0) {
            position = arg0;// ��ǰpage������
            tvcount.setText(arg0 + 1 + "/" + picpathList.size());

            loadPic(arg0);
        }

        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        public void onPageScrollStateChanged(int arg0) {
        }
    };

    class MyPageAdapter extends PagerAdapter {
        public int getCount() {
            return listViews.size();
        }


        /*
         * ��дgetItemPosition()������������notifyDataSetChangedʱ��
         * ��getItemPosition������Ϊ�ķ���POSITION_NONE���Ӷ��ﵽǿ��viewpager�ػ�����item��Ŀ�ġ�
         * �ɽ��Viewpager�ڵ���notifyDataSetChanged()ʱ��������ˢ�¡�
         */
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        public void destroyItem(View arg0, int arg1, Object arg2) {
            ((ViewPager) arg0).removeView(listViews.get(arg1));
        }

        public Object instantiateItem(View arg0, int arg1) {
            try {
                ((ViewPager) arg0).addView(listViews.get(arg1), 0);

            } catch (Exception e) {
            }
            return listViews.get(arg1);
        }

        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

    }

    /**
     * ����ͼƬ
     *
     * @param positionͼƬ��viewpager�е�λ��
     */
    private void loadPic(int position) {
        if (listbitmap.get(position) == null)// û�м��س�ͼƬ
        {
            /*
             * �ȼ�鱾���Ƿ��Ѿ�������ԭͼ������ֱ�Ӵӱ��ؼ��أ��������̴߳�������� ���ؼ��س���������´��������
			 */
            String picurl = picpathList.get(position);
            String imageName = picurl.substring(picurl.lastIndexOf("/") + 1);
            File sourcefile = new File(
                    Constant.sdRootPath + Constant.sourcePic, imageName);

            if (sourcefile.exists()) {
                Bitmap data;
                try {
                    data = BitmapFactory.decodeStream(new FileInputStream(
                            sourcefile));
                    if (data != null) {
                        listbitmap.set(position, data);
                        // ����ͼƬ
                        PhotoView img = (PhotoView) listViews.get(position)
                                .findViewById(R.id.IMVscanpic);
                        img.setImageBitmap(data);
                        attacher = new PhotoViewAttacher(img);
                        attacher.update();
                        return;
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

            }

            loadThread = sparse.get(position);
            if (loadThread == null)// û�п������̼߳���
            {
                ((ProgressBar) listViews.get(position).findViewById(
                        R.id.loadpicbar)).setVisibility(View.VISIBLE);
                loadThread = new LoadPicFromNet(position, picurl, handler);
                loadThread.start();
                sparse.put(position, loadThread);
            } else {
                if (!loadThread.isAlive())// �̴߳��ڣ������������(���ܻ���Ϊ����ԭ��û�м��سɹ�)
                {
                    // ���¿����̼߳���
                    loadThread = new LoadPicFromNet(position, picurl, handler);
                    loadThread.start();
                    sparse.put(position, loadThread);
                } else {// �̻߳�������,�����ȴ�
                }
            }

        }
    }
}
