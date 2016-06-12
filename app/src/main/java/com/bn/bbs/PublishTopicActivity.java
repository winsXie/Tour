package com.bn.bbs;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bn.getnativepicture.PicAlbumActivity;
import com.bn.tour.EditPhotoActivity;
import com.bn.tour.GridAddPicAdapter;
import com.bn.tour.R;
import com.bn.user.LoginActivity;
import com.bn.util.BitmapUtil;
import com.bn.util.CheckNetworkStatus;
import com.bn.util.CheckUtil;
import com.bn.util.Constant;
import com.bn.util.NetTransUtil;

public class PublishTopicActivity extends Activity {
    private InputMethodManager imm;//软键盘输入管理器
    private GridViewFaceAdapter mGVFaceAdapter;//表情适配器
    private GridAddPicAdapter picAdapter;//添加图片适配器
    private GridView gridViewFace, gridViewPic;
    private EditText title, content;
    private TextView send;
    private ImageView mFace;
    private Intent intent;
    private RelativeLayout rlloading;//加载提示布局

    private boolean faceShow = true;//是否显示表情标志位
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish_topic);
        handler = new MyHandler(this);

        BitmapUtil.maxCount = 3;
        rlloading = (RelativeLayout) this.findViewById(R.id.RLexplore_load);

        // 初始化基本视图
        this.initView();

        // 初始化表情视图
        this.initGridView();
        // 软键盘管理类
        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
    }

    private static class MyHandler extends Handler {
        WeakReference<PublishTopicActivity> mActivity;

        public MyHandler(PublishTopicActivity activity) {
            mActivity = new WeakReference<PublishTopicActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            PublishTopicActivity currActivity = mActivity.get();
            switch (msg.what) {
                case 0:
                    String topicid = (String) msg.obj;
                    if (topicid != null) {
                        Toast.makeText(currActivity, "发表成功！",
                                Toast.LENGTH_SHORT).show();

                        BitmapUtil.lastSize = 0;
                        BitmapUtil.pathlist.clear();
                        BitmapUtil.bmplist.clear();
                        BitmapUtil.templist.clear();
                        BitmapUtil.delTempPic();

                        currActivity.intent = new Intent(currActivity,
                                TopicActivity.class);
                        currActivity.intent.putExtra("TOPICID", topicid);

                        currActivity.setResult(1, currActivity.intent);
                        currActivity.finish();
                    } else {
                        Toast.makeText(currActivity, "发表失败！请重试",
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
                case 1:
                    currActivity.picAdapter.notifyDataSetChanged();
                    break;
                case Constant.CONNECTIONTIMEOUT:
                    // 去除加载缓冲
                    if (currActivity.rlloading.getVisibility() == View.VISIBLE) {
                        currActivity.rlloading.setVisibility(View.GONE);
                    }
                    if (CheckNetworkStatus.checkNetworkAvailable(currActivity)) {// 手机网络可用
                        Toast.makeText(currActivity, "连接超时！", Toast.LENGTH_LONG)
                                .show();
                    } else {
                        Toast.makeText(currActivity, "网络不可用！请检查网络连接！",
                                Toast.LENGTH_LONG).show();
                    }
                    currActivity.send.setEnabled(true);
                    break;
                case Constant.SOTIMEOUT:
                    // 去除加载缓冲
                    if (currActivity.rlloading.getVisibility() == View.VISIBLE) {
                        currActivity.rlloading.setVisibility(View.GONE);
                    }
                    Toast.makeText(currActivity, "读取数据超时！", Toast.LENGTH_LONG)
                            .show();
                    currActivity.send.setEnabled(true);
                    break;
            }
            super.handleMessage(msg);
        }
    }

    //初始化界面
    private void initView() {
        TextView canle = (TextView) findViewById(R.id.TVpublish_topic_cancle);
        send = (TextView) findViewById(R.id.TVpublish_topic_send);
        title = (EditText) findViewById(R.id.ETpublish_topic_title);
        content = (EditText) findViewById(R.id.ETpublish_topic_content);
        mFace = (ImageView) findViewById(R.id.IMV_face);

        ClickListener clicklistener = new ClickListener();

        title.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (title.isFocused()) {
                    gridViewFace.setVisibility(View.GONE);
                    faceShow = true;
                }
            }
        });

        content.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (content.isFocused()) {
                    gridViewFace.setVisibility(View.GONE);
                    faceShow = true;
                }
            }
        });
        // 设置控件点击事件
        mFace.setOnClickListener(clicklistener);
        canle.setOnClickListener(clicklistener);
        send.setOnClickListener(clicklistener);
    }

    //初始化表情和图片view
    private void initGridView() {
        gridViewFace = (GridView) findViewById(R.id.GV_faces2);
        mGVFaceAdapter = new GridViewFaceAdapter(this);
        gridViewFace.setAdapter(mGVFaceAdapter);
        gridViewFace
                .setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        // 插入的表情
                        SpannableString ss = new SpannableString(view.getTag()
                                .toString());
                        Drawable d = getResources().getDrawable(
                                (int) mGVFaceAdapter.getItemId(position));
                        d.setBounds(0, 0, 70, 70);// 设置表情图片的显示大小
                        ImageSpan span = new ImageSpan(d,
                                ImageSpan.ALIGN_BOTTOM);
                        ss.setSpan(span, 0, view.getTag().toString().length(),
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        // 在光标所在处插入表情
                        content.getText().insert(content.getSelectionStart(),
                                ss);
                    }
                });

        // 初始化图片GridView
        gridViewPic = (GridView) findViewById(R.id.GVpublish_topic_picture);
        picAdapter = new GridAddPicAdapter(this, handler);
        gridViewPic.setAdapter(picAdapter);

        gridViewPic.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {// 当前activity并没有finish()(没有destroy,而是stop)
                if (arg2 == BitmapUtil.bmplist.size()) {
                    new PopupWindows(PublishTopicActivity.this, gridViewPic);
                } else {
                    Intent intent = new Intent(PublishTopicActivity.this,
                            EditPhotoActivity.class);
                    intent.putExtra("ID", arg2);
                    startActivity(intent);
                }
            }
        });

    }

    //实现弹出框布局
    public class PopupWindows extends PopupWindow {
        @SuppressWarnings("deprecation")
        public PopupWindows(Context mContext, View parent) {

            View view = View
                    .inflate(mContext, R.layout.item_popupwindows, null);
            view.startAnimation(AnimationUtils.loadAnimation(mContext,
                    R.anim.fade_ins));
            LinearLayout ll_popup = (LinearLayout) view
                    .findViewById(R.id.ll_popup);
            ll_popup.startAnimation(AnimationUtils.loadAnimation(mContext,
                    R.anim.push_bottom_in_2));

            setWidth(LayoutParams.MATCH_PARENT);
            setHeight(LayoutParams.MATCH_PARENT);
            setBackgroundDrawable(new BitmapDrawable());
            setFocusable(true);
            setOutsideTouchable(true);
            setContentView(view);
            showAtLocation(parent, Gravity.BOTTOM, 0, 0);
            update();

            Button bt1 = (Button) view
                    .findViewById(R.id.item_popupwindows_camera);
            Button bt2 = (Button) view
                    .findViewById(R.id.item_popupwindows_Photo);
            Button bt3 = (Button) view
                    .findViewById(R.id.item_popupwindows_cancel);
            bt1.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    photo();
                    dismiss();
                }
            });
            bt2.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    Intent intent = new Intent(PublishTopicActivity.this,
                            PicAlbumActivity.class);
                    startActivity(intent);
                    dismiss();
                }
            });
            bt3.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    dismiss();
                }
            });

        }
    }

    private static final int TAKE_PICTURE = 0x000000;//请求照相的requestCode值
    private String path = "";

    //调用系统照相
    public void photo() {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        File file = new File(Environment.getExternalStorageDirectory(),
                String.valueOf(System.currentTimeMillis()) + ".jpg");
        path = file.getPath();
        Uri imageUri = Uri.fromFile(file);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, TAKE_PICTURE);
    }

    private class ClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.IMV_face:
                    if (faceShow && !title.isFocused()) {
                        // 隐藏软键盘
                        imm.hideSoftInputFromWindow(mFace.getWindowToken(), 0);
                        gridViewFace.setVisibility(View.VISIBLE);
                        faceShow = false;
                    } else {
                        gridViewFace.setVisibility(View.GONE);
                        imm.showSoftInput(content, 0);
                        faceShow = true;
                    }
                    break;
                case R.id.TVpublish_topic_cancle:
                    BitmapUtil.lastSize = 0;
                    BitmapUtil.pathlist.clear();
                    BitmapUtil.bmplist.clear();
                    BitmapUtil.templist.clear();
                    BitmapUtil.delTempPic();

                    intent = new Intent(PublishTopicActivity.this,
                            TopicActivity.class);

                    setResult(2, intent);
                    finish();
                    break;
                case R.id.TVpublish_topic_send:
                    send.setEnabled(false);
                    if (Constant.isLogin) {
                        if (CheckNetworkStatus
                                .checkNetworkAvailable(PublishTopicActivity.this)) {
                            String mcontent = content.getText().toString();

                            mcontent = CheckUtil.replaceBlank(mcontent);// 替换回车符号，否则构造json报错
                            if (!TextUtils.isEmpty(mcontent)
                                    && mcontent.length() > 15) {
                                final Map<String, String> paraMap = new HashMap<String, String>();
                                paraMap.put("top_blockid", Constant.currBloId);
                                paraMap.put("top_userid", Constant.loginUid);

                                String mtitle = title.getText().toString();
                                mtitle = CheckUtil.replaceBlank(mtitle);
                                if (TextUtils.isEmpty(mtitle)) {
                                    paraMap.put("top_title", "null");
                                } else {
                                    paraMap.put("top_title", title.getText()
                                            .toString());
                                }
                                paraMap.put("top_content", mcontent);

                                rlloading.setVisibility(View.GONE);
                                new Thread() {

                                    @Override
                                    public void run() {
                                        try {
                                            String topicid = NetTransUtil
                                                    .publishTopic(paraMap);
                                            handler.obtainMessage(0, topicid)
                                                    .sendToTarget();
                                        } catch (Exception e) {
                                            String mess = e.getClass()
                                                    .getName();
                                            if (mess.contains("ConnectTimeoutException")
                                                    || mess.contains("ConnectException")) {
                                                handler.obtainMessage(
                                                        Constant.CONNECTIONTIMEOUT)
                                                        .sendToTarget();
                                            } else if (mess
                                                    .contains("SocketTimeoutException")) {
                                                handler.obtainMessage(
                                                        Constant.SOTIMEOUT)
                                                        .sendToTarget();
                                            }
                                            e.printStackTrace();
                                        }

                                    }

                                }.start();
                            } else {
                                Toast.makeText(PublishTopicActivity.this,
                                        "内容太少啦！", Toast.LENGTH_SHORT).show();
                                send.setEnabled(true);
                            }
                        } else {
                            Toast.makeText(PublishTopicActivity.this,
                                    "网络不可用，请检查网络连接！", Toast.LENGTH_LONG).show();
                            send.setEnabled(true);
                        }

                    } else {
                        send.setEnabled(true);
                        intent = new Intent(PublishTopicActivity.this,
                                LoginActivity.class);
                        startActivity(intent);
                    }
                    break;
            }
        }

    }

    //执行完startActivityForResult返回后调用 的方法
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case TAKE_PICTURE:
                if (BitmapUtil.pathlist.size() < BitmapUtil.maxCount
                        && resultCode == -1) {
                    BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
                    bitmapOptions.inSampleSize = 8;
                    File file = new File(path);
                    /**
                     * 获取图片的旋转角度，有些系统把拍照的图片旋转了，有的没有旋转
                     */
                    int degree = BitmapUtil.readPictureDegree(file.getAbsolutePath());
                    Bitmap cameraBitmap = BitmapFactory.decodeFile(path, bitmapOptions);
                    Bitmap newbitmap = BitmapUtil.rotaingImageView(degree, cameraBitmap);

                    if (file.exists()) {
                        file.delete();
                    }
                    try {
                        FileOutputStream out = new FileOutputStream(
                                file);
                        newbitmap.compress(Bitmap.CompressFormat.PNG,
                                100, out);
                        out.flush();
                        out.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    BitmapUtil.pathlist.add(path);
                }
                break;
        }
    }

    //拦截返回键，清除临时数据
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            BitmapUtil.lastSize = 0;
            BitmapUtil.pathlist.clear();
            BitmapUtil.bmplist.clear();
            BitmapUtil.templist.clear();
            BitmapUtil.delTempPic();

            intent = new Intent(PublishTopicActivity.this, TopicActivity.class);
            setResult(2, intent);
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onStart() {
        picAdapter.update();// 更新选择的图片
        super.onStart();
    }

}
