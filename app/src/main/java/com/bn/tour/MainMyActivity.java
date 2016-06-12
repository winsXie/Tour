package com.bn.tour;

import java.io.File;
import java.lang.ref.WeakReference;

import net.sf.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bn.bbs.MyBlockActivity;
import com.bn.bbs.MyPostActivity;
import com.bn.message.MyMessageActivity;
import com.bn.plan.MyPlanActivity;
import com.bn.thread.DownloadPicThread;
import com.bn.travelnote.MyTravelTopicActivity;
import com.bn.user.ChangeHeadActivity;
import com.bn.user.LoginActivity;
import com.bn.user.RegisterActivity;
import com.bn.util.Constant;
import com.bn.util.NetTransUtil;

public class MainMyActivity extends Activity {
    private static final int GET_BASEINFO = 0;//获得用户的基本信息
    private ImageButton login;//登录
    private ImageButton register;//注册
    private ImageButton option;//选项按钮
    private TextView name, level, jifen;
    private ImageView headImage;//头像

    private RelativeLayout rlMyBlock, rlMyTopic, rlMyNote, rlMyPlan,
            rlMyMessage, rlClearCache;
    private Clicklistener clicklistener;
    private Intent intent;
    private GetBaseInfo getBaseInfo;//获得用户基本信息线程
    public static boolean isInitView = false;// 初始化界面标志位
    public static boolean isChanged = false;// 信息改变标志位
    private long exitTime = 0;//第一次按返回键的初始化时间

    private Handler handler;

    //初始化option
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);// 调用第二步中定义的Menu界面描述文件
        return true;
    }

    //option点击监听
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_zhuxiao:
                if (Constant.isLogin) {
                    Constant.isLogin = false;
                    setContentView(R.layout.main_my_unlogin);
                    initLoginView();
                    Toast.makeText(MainMyActivity.this, "注销成功！",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainMyActivity.this, "目前尚未登录！",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.menu_about:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setIcon(R.drawable.ic_mylauncher);
                builder.setTitle("关于");
                builder.setMessage("本应用归  华北理工大学  所有！");
                builder.setPositiveButton("确定", null);
                builder.show();
                break;
            case R.id.menu_quit:
                System.exit(0);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new MyHandler(this);
        if (!Constant.isLogin) {
            setContentView(R.layout.main_my_unlogin);
            initLoginView();
        } else {
            setContentView(R.layout.main_my_logined);
            initHadLoginView();
        }

    }

    private static class MyHandler extends Handler {
        WeakReference<MainMyActivity> mActivity;

        public MyHandler(MainMyActivity activity) {
            mActivity = new WeakReference<MainMyActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainMyActivity currActivity = mActivity.get();
            switch (msg.what) {
                case GET_BASEINFO:
                    String userinfo = (String) msg.obj;
                    if (userinfo != null) {
                        JSONObject json_baseinfo = JSONObject
                                .fromObject(userinfo);
                        Constant.currUserInfo = json_baseinfo;
                        currActivity.name.setText(json_baseinfo
                                .getString("user_name"));
                        currActivity.level.setText(json_baseinfo
                                .getString("user_level"));
                        currActivity.jifen.setText(json_baseinfo
                                .getString("user_experience"));

                        DownloadPicThread loadThread = new DownloadPicThread(
                                json_baseinfo.getString("user_image"),
                                currActivity.headImage, currActivity.handler);
                        loadThread.start();
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

    //初始化登录界面（未登录）
    private void initLoginView() {
        clicklistener = new Clicklistener();
        login = (ImageButton) findViewById(R.id.my_login);
        login.setOnClickListener(clicklistener);
        register = (ImageButton) findViewById(R.id.my_register);
        register.setOnClickListener(clicklistener);

        option = (ImageButton) findViewById(R.id.my_set);
        option.setOnClickListener(clicklistener);

        isInitView = true;
    }

    //初始化登陆界面（已登录）
    private void initHadLoginView() {
        clicklistener = new Clicklistener();

        headImage = (ImageView) findViewById(R.id.IMVmy_head);
        name = (TextView) findViewById(R.id.TVmy_uname);
        level = (TextView) findViewById(R.id.TVmy_level);
        jifen = (TextView) findViewById(R.id.TVmy_jifen);

        rlMyBlock = (RelativeLayout) findViewById(R.id.RLmy_block);
        rlMyTopic = (RelativeLayout) findViewById(R.id.RLmy_topic);
        rlMyNote = (RelativeLayout) findViewById(R.id.RLmy_note);
        rlMyPlan = (RelativeLayout) findViewById(R.id.RLmy_plan);
        rlMyMessage = (RelativeLayout) findViewById(R.id.RLmy_message);
        rlClearCache = (RelativeLayout) findViewById(R.id.RLmy_clearcache);
        option = (ImageButton) findViewById(R.id.my_set);

        headImage.setOnClickListener(clicklistener);
        rlMyBlock.setOnClickListener(clicklistener);
        rlMyTopic.setOnClickListener(clicklistener);
        rlMyNote.setOnClickListener(clicklistener);
        rlMyPlan.setOnClickListener(clicklistener);
        rlMyMessage.setOnClickListener(clicklistener);
        rlClearCache.setOnClickListener(clicklistener);
        option.setOnClickListener(clicklistener);

        getBaseInfo = new GetBaseInfo();
        getBaseInfo.start();

        isInitView = true;
    }

    //控件点击监听
    private class Clicklistener implements android.view.View.OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.IMVmy_head:
                    intent = new Intent(MainMyActivity.this,
                            ChangeHeadActivity.class);
                    startActivity(intent);
                    break;
                case R.id.my_login:
                    intent = new Intent(MainMyActivity.this, LoginActivity.class);
                    startActivity(intent);
                    break;
                case R.id.my_register:
                    Intent intent = new Intent(MainMyActivity.this,
                            RegisterActivity.class);
                    startActivity(intent);
                    break;
                case R.id.my_set:
                    openOptionsMenu();
                    break;
                case R.id.RLmy_block:
                    intent = new Intent(MainMyActivity.this, MyBlockActivity.class);
                    startActivity(intent);
                    break;
                case R.id.RLmy_topic:
                    intent = new Intent(MainMyActivity.this,
                            MyPostActivity.class);
                    startActivity(intent);
                    break;
                case R.id.RLmy_note:
                    intent = new Intent(MainMyActivity.this, MyTravelTopicActivity.class);
                    startActivity(intent);
                    break;
                case R.id.RLmy_plan:
                    intent = new Intent(MainMyActivity.this, MyPlanActivity.class);
                    startActivity(intent);
                    break;
                case R.id.RLmy_message:
                    intent = new Intent(MainMyActivity.this, MyMessageActivity.class);
                    startActivity(intent);
                    break;
                case R.id.RLmy_clearcache:
                    String netType = checkNetworkType(MainMyActivity.this);
                    if (netType == null) {
                        clearCachePic();
                    } else {
                        if (netType.equals("MOBILE")) {
                            showDialog();
                        } else {
                            clearCachePic();
                        }
                    }
                    break;
            }
        }
    }

    //显示提示对话框，是否清除缓存
    private void showDialog() {
        AlertDialog.Builder dialogBuilder = new Builder(MainMyActivity.this);
        dialogBuilder.setTitle("提示");
        dialogBuilder
                .setMessage("检测到您正在使用  移动数据流量，清除缓存后重新加载图片会耗费您的数据流量，真的要清除吗？");
        dialogBuilder.setPositiveButton("土豪模式^_^", new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                clearCachePic();
            }
        });

        dialogBuilder.setNegativeButton("省省流量~", new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        dialogBuilder.create().show();

    }

    //检查当前的网络连接类型
    private String checkNetworkType(Context context) {
        String type = null;

        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            return null;
        } else {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        NetworkInfo netWorkInfo = info[i];
                        if (netWorkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                            return "WIFI";
                        } else if (netWorkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                            return "MOBILE";
                        }
                    }
                }
            }
        }

        return type;

    }

    //清除图片缓存（本地文件）
    private void clearCachePic() {
        File cachePicFile = new File(Constant.sdRootPath + Constant.cachePath);
        if (cachePicFile.exists()) {
            for (File file : cachePicFile.listFiles()) {
                if (file.isFile() && file.exists()) {
                    file.delete();
                }
            }
        }

        File sourcePicFile = new File(Constant.sdRootPath + Constant.sourcePic);
        if (sourcePicFile.exists()) {
            for (File file : sourcePicFile.listFiles()) {
                if (file.isFile() && file.exists()) {
                    file.delete();
                }
            }
        }

        Toast.makeText(MainMyActivity.this, "清除完成！", Toast.LENGTH_LONG).show();
    }

    private class GetBaseInfo extends Thread {
        @Override
        public void run() {
            try {
                String userinfo = NetTransUtil.getBasedInfo(Constant.loginUid);
                handler.obtainMessage(GET_BASEINFO, userinfo).sendToTarget();
            } catch (Exception e) {
                String mess = e.getClass().getName();
                if (mess.contains("ConnectTimeoutException")) {
                    handler.obtainMessage(Constant.CONNECTIONTIMEOUT)
                            .sendToTarget();
                } else if (mess.contains("SocketTimeoutException")) {
                    handler.obtainMessage(Constant.SOTIMEOUT).sendToTarget();
                }
                e.printStackTrace();
            }
        }
    }

    //每次显示当前Activity时都要检测是否已经登录，是否已经初始化界面，用户资料是否已经发送生改变
    @Override
    protected void onRestart() {
        if (Constant.isLogin) {
            // 更新界面
            if (!isInitView) {
                setContentView(R.layout.main_my_logined);
                initHadLoginView();
            }

            if (isChanged) {
                DownloadPicThread loadThread = new DownloadPicThread(
                        Constant.currUserInfo.getString("user_image"),
                        headImage, handler);
                loadThread.start();

                name.setText(Constant.currUserInfo.getString("user_name"));

                isChanged = false;// 置回标志位
            }

        }
        super.onRestart();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // TODO Auto-generated method stub
        // super.onSaveInstanceState(outState);
    }

    //重写返回键，实现按两次退出程序
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // 通过获取当前时间，在计算时间差来判断是否退出，第一次按肯定不会退出，因为exitTime被初始化为0，
            // 时间差是当前时间距1970年1月1日凌晨的时间，大于阈值，之后再判断距上次按的时间是否大于2000ms
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(this, "再按一次退出悠悠", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();// 重置上次按下时间
            } else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
