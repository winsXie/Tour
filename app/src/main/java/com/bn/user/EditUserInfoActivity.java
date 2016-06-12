package com.bn.user;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bn.tour.MainMyActivity;
import com.bn.tour.R;
import com.bn.util.CheckUtil;
import com.bn.util.Constant;
import com.bn.util.NetTransUtil;
import com.bn.util.TimeChange;

public class EditUserInfoActivity extends Activity {
    private static final int CHANGE_INFO = 0;//�����û�����
    private EditText name, age, city, mail, occupa, wcity;
    private TextView cancle, enter;

    private LinearLayout llBirth;
    private RadioButton sex_nan, sex_nv;
    private DatePicker Birth;
    private JSONObject userInfo;
    private String newBirth;// ���õ��µĳ�������
    private Map<String, String> paramap = new HashMap<String, String>();

    private Handler handler;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_userinfo);
        handler = new MyHandler(this);
        init();
    }

    private static class MyHandler extends Handler {
        WeakReference<EditUserInfoActivity> mActivity;

        public MyHandler(EditUserInfoActivity activity) {
            mActivity = new WeakReference<EditUserInfoActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            EditUserInfoActivity currActivity = mActivity.get();
            switch (msg.what) {
                case CHANGE_INFO:
                    String userinfo = (String) msg.obj;
                    if (userinfo != null) {
                        Toast.makeText(currActivity, "�༭�ɹ���",
                                Toast.LENGTH_SHORT).show();
                        // ���±��ش洢����Ϣ
                        Constant.currUserInfo = JSONObject.fromObject(userinfo);

                        MainMyActivity.isChanged = true;
                        currActivity.finish();
                    } else {
                        Toast.makeText(currActivity, "�༭ʧ�ܣ����Ժ����ԣ�",
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Constant.CONNECTIONTIMEOUT:
                    Toast.makeText(currActivity, "���ӳ�ʱ��", Toast.LENGTH_LONG)
                            .show();
                    currActivity.enter.setEnabled(true);
                    break;
                case Constant.SOTIMEOUT:
                    Toast.makeText(currActivity, "��ȡ���ݳ�ʱ��", Toast.LENGTH_LONG)
                            .show();
                    currActivity.enter.setEnabled(true);
                    break;
            }
        }
    }

    // ��ʼ������ʵ��
    private void init() {
        name = (EditText) findViewById(R.id.ETeditinfo_user_name);
        age = (EditText) findViewById(R.id.ETeditinfo_user_age);
        city = (EditText) findViewById(R.id.ETeditinfo_user_city);
        mail = (EditText) findViewById(R.id.ETeditinfo_user_mail);
        occupa = (EditText) findViewById(R.id.ETeditinfo_user_occup);
        wcity = (EditText) findViewById(R.id.ETeditinfo_user_wantcity);

        llBirth = (LinearLayout) findViewById(R.id.LLeditinfo_Birth);
        sex_nan = (RadioButton) findViewById(R.id.edit_info_nan);
        sex_nv = (RadioButton) findViewById(R.id.edit_info_nv);
        Birth = (DatePicker) findViewById(R.id.edit_info_birth);
        cancle = (TextView) findViewById(R.id.TVeditinfo_cancle);
        enter = (TextView) findViewById(R.id.TVeditinfo_enter);

        userInfo = Constant.currUserInfo;
        newBirth = userInfo.getString("user_birth");
        name.setText(userInfo.getString("user_name"));
        age.setText(userInfo.getString("user_age"));
        city.setText(userInfo.getString("user_city"));
        if (!userInfo.getString("user_mail").equals("null")) {
            mail.setText(userInfo.getString("user_mail"));
        }
        if (!userInfo.getString("user_occupation").equals("null")) {
            occupa.setText(userInfo.getString("user_occupation"));
        }
        if (!userInfo.getString("user_wantcity").equals("null")) {
            wcity.setText(userInfo.getString("user_wantcity"));
        }
        if (userInfo.getString("user_sex").equals("��")) {
            sex_nan.setChecked(true);
        } else {
            sex_nv.setChecked(true);
        }

        // ��ʼ��DatePicker
        final Calendar cal = Calendar.getInstance();
        final int nowyear = cal.get(Calendar.YEAR);// ��¼��ϵͳ��ǰ����
        final int nowmonth = cal.get(Calendar.MONTH);
        final int nowday = cal.get(Calendar.DAY_OF_MONTH);
        String tempbirth = userInfo.getString("user_birth");
        System.out.println(tempbirth + "=====");
        Birth.init(Integer.parseInt(tempbirth.substring(0, 4)),
                Integer.parseInt(tempbirth.substring(5, 7)) - 1,
                Integer.parseInt(tempbirth.substring(8, tempbirth.length())),
                new OnDateChangedListener() {
                    Calendar cal2 = Calendar.getInstance();

                    @Override
                    public void onDateChanged(DatePicker view, int year,
                                              int monthOfYear, int dayOfMonth) {
                        cal2.set(year, monthOfYear, dayOfMonth);
                        if (cal2.after(cal)) {
                            Birth.updateDate(nowyear, nowmonth, nowday);// ��ѡ������ڴ��ڵ�ǰ���ڣ���Ϊ��ǰ����
                        }
                        monthOfYear = monthOfYear + 1;
                        newBirth = year + "-" + monthOfYear + "-" + dayOfMonth;// ����������
                        age.setText(TimeChange.calcuTime(cal2, cal));// ��������
                    }

                });

        age.setFocusable(false);

        ClickListener listener = new ClickListener();

        age.setOnClickListener(listener);
        cancle.setOnClickListener(listener);
        enter.setOnClickListener(listener);

    }

    private class ClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.ETeditinfo_user_age:
                    switch (llBirth.getVisibility()) {
                        case View.VISIBLE:
                            llBirth.setVisibility(View.GONE);
                            break;
                        case View.GONE:
                            llBirth.setVisibility(View.VISIBLE);
                            break;
                    }
                    break;
                case R.id.TVeditinfo_cancle:
                    finish();
                    break;
                case R.id.TVeditinfo_enter:
                    enter.setEnabled(false);// ��ֹ�ظ��ύ
                    // ���жϱ�����Ϣ�Ƿ�Ϊ��
                    if (TextUtils.isEmpty(name.getText())
                            || TextUtils.isEmpty(city.getText())) {
                        Toast.makeText(EditUserInfoActivity.this,
                                "�û��� �� ���ڳ��� ����Ϊ�գ�", Toast.LENGTH_LONG).show();
                        enter.setEnabled(true);
                    } else {// �ж������ʽ
                        String mailstr = mail.getText().toString();
                        if (!TextUtils.isEmpty(mailstr)) {
                            if (CheckUtil.checkEmail(mailstr)) {
                                paramap.put("user_mail", mailstr);// ���䲻Ϊ���ҺϷ�
                                submitParams();
                            } else {
                                Toast.makeText(EditUserInfoActivity.this,
                                        "�Ƿ������䣡", Toast.LENGTH_LONG).show();
                                enter.setEnabled(true);
                            }
                        } else {
                            paramap.put("user_mail", "null");// ����Ϊ��
                            submitParams();
                        }

                    }
                    break;
            }
        }

    }

    //�ύ������Ϣ
    private void submitParams() {
        paramap.put("user_id", Constant.loginUid);
        paramap.put("user_name", name.getText().toString());

        if (sex_nan.isChecked()) {
            paramap.put("user_sex", "��");
        } else {
            paramap.put("user_sex", "Ů");
        }

        paramap.put("user_birth", newBirth);
        System.out.println(newBirth + "=newBirth====");
        paramap.put("user_city", city.getText().toString());

        if (TextUtils.isEmpty(occupa.getText())) {
            paramap.put("user_occupation", "null");
        } else {
            paramap.put("user_occupation", occupa.getText().toString());
        }

        if (TextUtils.isEmpty(wcity.getText())) {
            paramap.put("user_wantcity", "null");
        } else {
            paramap.put("user_wantcity", wcity.getText().toString());
        }

        new Thread() {

            @Override
            public void run() {
                try {
                    String info = NetTransUtil.editBasedInfo(paramap);
                    handler.obtainMessage(CHANGE_INFO, info).sendToTarget();
                } catch (Exception e) {
                    String mess = e.getClass().getName();
                    if (mess.contains("ConnectTimeoutException")) {
                        handler.obtainMessage(Constant.CONNECTIONTIMEOUT)
                                .sendToTarget();
                    } else if (mess.contains("SocketTimeoutException")) {
                        handler.obtainMessage(Constant.SOTIMEOUT)
                                .sendToTarget();
                    }
                    e.printStackTrace();
                }

            }

        }.start();

    }

}
