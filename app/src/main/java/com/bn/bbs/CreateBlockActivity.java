package com.bn.bbs;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bn.tour.R;
import com.bn.util.BitmapUtil;
import com.bn.util.Constant;
import com.bn.util.NetTransUtil;

public class CreateBlockActivity extends Activity {
    private static final int CREATE_BLOCK = 0;//创建版块的what值
    private TextView tvCancle, tvCreate;
    private EditText etName, etIntro;
    private ImageView addPic;

    private Intent intent;
    private File temp = new File(Environment.getExternalStorageDirectory()
            + "/tourTemp.jpg");//存储拍照图片的路径

    private Handler handler;//用于线程间通信的handler

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_block);
        handler = new MyHandler(this);

        tvCancle = (TextView) this.findViewById(R.id.TVcreate_blo_cancle);
        tvCreate = (TextView) this.findViewById(R.id.TVcreate_blo_ok);
        etName = (EditText) this.findViewById(R.id.ETcreate_blo_name);
        etIntro = (EditText) this.findViewById(R.id.ETcreate_blo_intro);
        addPic = (ImageView) this.findViewById(R.id.IMVblock_pic);

        ClickListener listener = new ClickListener();
        tvCancle.setOnClickListener(listener);
        tvCreate.setOnClickListener(listener);
        addPic.setOnClickListener(listener);

    }

    private static class MyHandler extends Handler {
        WeakReference<CreateBlockActivity> mActivity;

        public MyHandler(CreateBlockActivity activity) {
            mActivity = new WeakReference<CreateBlockActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            CreateBlockActivity currActivity = mActivity.get();
            switch (msg.what) {
                case CREATE_BLOCK:
                    String blockid = (String) msg.obj;
                    if (blockid == null) {
                        Toast.makeText(currActivity, "创建失败！", Toast.LENGTH_LONG)
                                .show();
                    } else {
                        BitmapUtil.lastSize = 0;
                        BitmapUtil.pathlist.clear();
                        BitmapUtil.bmplist.clear();
                        BitmapUtil.templist.clear();
                        BitmapUtil.delTempPic();

                        currActivity.intent = new Intent(currActivity,
                                MyBlockActivity.class);
                        currActivity.intent.putExtra("BLOCKID", blockid);
                        currActivity.setResult(1, currActivity.intent);
                        currActivity.finish();
                    }
                    break;
                case Constant.CONNECTIONTIMEOUT:
                    Toast.makeText(currActivity, "连接超时！", Toast.LENGTH_LONG)
                            .show();
                    break;
                case Constant.SOTIMEOUT:
                    Toast.makeText(currActivity, "读取数据超时！", Toast.LENGTH_LONG)
                            .show();
                    break;
            }
        }
    }

    private class ClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            switch (v.getId()) {
                case R.id.TVcreate_blo_cancle:
                    BitmapUtil.lastSize = 0;
                    BitmapUtil.pathlist.clear();
                    BitmapUtil.bmplist.clear();
                    BitmapUtil.templist.clear();
                    BitmapUtil.delTempPic();

                    intent = new Intent(CreateBlockActivity.this, MyBlockActivity.class);
                    setResult(2, intent);
                    finish();
                    break;
                case R.id.TVcreate_blo_ok:
                    if (TextUtils.isEmpty(etName.getText().toString())) {
                        Toast.makeText(CreateBlockActivity.this, "版块名称不能为空！",
                                Toast.LENGTH_LONG).show();
                    } else {
                        final Map<String, String> paramap = new HashMap<String, String>();
                        paramap.put("blo_name", etName.getText().toString());
                        paramap.put("blo_userid", Constant.loginUid);
                        if (TextUtils.isEmpty(etIntro.getText().toString())) {
                            paramap.put("blo_intro", "null");
                        } else {
                            paramap.put("blo_intro", etIntro.getText()
                                    .toString());
                        }

                        new Thread() {

                            @Override
                            public void run() {
                                try {
                                    String blockid = NetTransUtil
                                            .createBlock(paramap);
                                    handler.obtainMessage(CREATE_BLOCK, blockid)
                                            .sendToTarget();
                                } catch (Exception e) {
                                    String mess = e.getClass().getName();
                                    if (mess.contains("ConnectTimeoutException")) {
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

                    }
                    break;
                case R.id.IMVblock_pic:
                    ShowPickDialog();
                    break;
            }
        }

    }

    //显示选择照片方式的dialoge（从相册选择或拍照）
    private void ShowPickDialog() {
        new AlertDialog.Builder(this).setTitle("设置头像...")
                .setNegativeButton("相册", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Intent intent = new Intent(Intent.ACTION_PICK);
                        intent.setType("image/*");

                        startActivityForResult(intent, 1);

                    }
                })
                .setPositiveButton("拍照", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();

                        Intent intent = new Intent(
                                "android.media.action.IMAGE_CAPTURE");
                        File file = new File(Environment
                                .getExternalStorageDirectory(), "tourTemp.jpg");
                        Uri imageUri = Uri.fromFile(file);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                        startActivityForResult(intent, 2);

                    }
                }).show();
    }

    //执行完startActivityForResult返回后调用 的方法
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // 如果是直接从相册获取
            case 1:
                Log.d("ActivityStatus=====>", "case 1");
                if (data != null && data.getData() != null) {
                    Uri uri = data.getData();

                    startPhotoZoom(uri);
                }

                break;
            // 如果是调用相机拍照时
            case 2:
                Log.d("ActivityStatus=====>", "case 2");
                if (temp.exists()) {
                    startPhotoZoom(Uri.fromFile(temp));
                }
                break;
            // 取得裁剪后的图片
            case 3:
                Log.d("ActivityStatus=====>", "case 3");
                if (data != null) {

                    Bitmap bitmap = data.getParcelableExtra("data");
                    // 设置图片背景
                    addPic.setImageBitmap(bitmap);

                    BitmapUtil.templist.clear();
                    BitmapUtil.saveTempPic(bitmap, "tourTemp.jpg");

                    if (temp.exists()) {
                        temp.delete();
                    }

                }
                break;
            default:
                break;

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //裁剪图片
    public void startPhotoZoom(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // 下面这个crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 150);
        intent.putExtra("outputY", 150);
        intent.putExtra("outputFormat", "JPEG");// 图片格式
        intent.putExtra("noFaceDetection", true);// 取消人脸识别
        intent.putExtra("return-data", true);

        startActivityForResult(intent, 3);
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

            intent = new Intent(CreateBlockActivity.this, MyBlockActivity.class);
            setResult(2, intent);
        }

        return super.onKeyDown(keyCode, event);
    }

}
