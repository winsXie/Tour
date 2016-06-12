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
    private static final int GET_BASEINFO = 0;//����û��Ļ�����Ϣ
    private ImageButton login;//��¼
    private ImageButton register;//ע��
    private ImageButton option;//ѡ�ť
    private TextView name, level, jifen;
    private ImageView headImage;//ͷ��

    private RelativeLayout rlMyBlock, rlMyTopic, rlMyNote, rlMyPlan,
            rlMyMessage, rlClearCache;
    private Clicklistener clicklistener;
    private Intent intent;
    private GetBaseInfo getBaseInfo;//����û�������Ϣ�߳�
    public static boolean isInitView = false;// ��ʼ�������־λ
    public static boolean isChanged = false;// ��Ϣ�ı��־λ
    private long exitTime = 0;//��һ�ΰ����ؼ��ĳ�ʼ��ʱ��

    private Handler handler;

    //��ʼ��option
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);// ���õڶ����ж����Menu���������ļ�
        return true;
    }

    //option�������
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_zhuxiao:
                if (Constant.isLogin) {
                    Constant.isLogin = false;
                    setContentView(R.layout.main_my_unlogin);
                    initLoginView();
                    Toast.makeText(MainMyActivity.this, "ע���ɹ���",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainMyActivity.this, "Ŀǰ��δ��¼��",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.menu_about:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setIcon(R.drawable.ic_mylauncher);
                builder.setTitle("����");
                builder.setMessage("��Ӧ�ù�  ��������ѧ  ���У�");
                builder.setPositiveButton("ȷ��", null);
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
                    Toast.makeText(currActivity, "���ӳ�ʱ��", Toast.LENGTH_LONG)
                            .show();
                    break;
                case Constant.SOTIMEOUT:
                    Toast.makeText(currActivity, "��ȡ���ݳ�ʱ��", Toast.LENGTH_LONG)
                            .show();
                    break;
            }
        }
    }

    //��ʼ����¼���棨δ��¼��
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

    //��ʼ����½���棨�ѵ�¼��
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

    //�ؼ��������
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

    //��ʾ��ʾ�Ի����Ƿ��������
    private void showDialog() {
        AlertDialog.Builder dialogBuilder = new Builder(MainMyActivity.this);
        dialogBuilder.setTitle("��ʾ");
        dialogBuilder
                .setMessage("��⵽������ʹ��  �ƶ����������������������¼���ͼƬ��ķ������������������Ҫ�����");
        dialogBuilder.setPositiveButton("����ģʽ^_^", new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                clearCachePic();
            }
        });

        dialogBuilder.setNegativeButton("ʡʡ����~", new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        dialogBuilder.create().show();

    }

    //��鵱ǰ��������������
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

    //���ͼƬ���棨�����ļ���
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

        Toast.makeText(MainMyActivity.this, "�����ɣ�", Toast.LENGTH_LONG).show();
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

    //ÿ����ʾ��ǰActivityʱ��Ҫ����Ƿ��Ѿ���¼���Ƿ��Ѿ���ʼ�����棬�û������Ƿ��Ѿ��������ı�
    @Override
    protected void onRestart() {
        if (Constant.isLogin) {
            // ���½���
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

                isChanged = false;// �ûر�־λ
            }

        }
        super.onRestart();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // TODO Auto-generated method stub
        // super.onSaveInstanceState(outState);
    }

    //��д���ؼ���ʵ�ְ������˳�����
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // ͨ����ȡ��ǰʱ�䣬�ڼ���ʱ������ж��Ƿ��˳�����һ�ΰ��϶������˳�����ΪexitTime����ʼ��Ϊ0��
            // ʱ����ǵ�ǰʱ���1970��1��1���賿��ʱ�䣬������ֵ��֮�����жϾ��ϴΰ���ʱ���Ƿ����2000ms
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(this, "�ٰ�һ���˳�����", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();// �����ϴΰ���ʱ��
            } else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
