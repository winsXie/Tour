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
    private static final int CHANGE_INFO = 0;//更改用户资料
    private EditText name, age, city, mail, occupa, wcity;
    private TextView cancle, enter;

    private LinearLayout llBirth;
    private RadioButton sex_nan, sex_nv;
    private DatePicker Birth;
    private JSONObject userInfo;
    private String newBirth;// 设置的新的出生日期
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
                        Toast.makeText(currActivity, "编辑成功！",
                                Toast.LENGTH_SHORT).show();
                        // 更新本地存储的信息
                        Constant.currUserInfo = JSONObject.fromObject(userinfo);

                        MainMyActivity.isChanged = true;
                        currActivity.finish();
                    } else {
                        Toast.makeText(currActivity, "编辑失败，请稍后重试！",
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Constant.CONNECTIONTIMEOUT:
                    Toast.makeText(currActivity, "连接超时！", Toast.LENGTH_LONG)
                            .show();
                    currActivity.enter.setEnabled(true);
                    break;
                case Constant.SOTIMEOUT:
                    Toast.makeText(currActivity, "读取数据超时！", Toast.LENGTH_LONG)
                            .show();
                    currActivity.enter.setEnabled(true);
                    break;
            }
        }
    }

    // 初始化方法实现
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
        if (userInfo.getString("user_sex").equals("男")) {
            sex_nan.setChecked(true);
        } else {
            sex_nv.setChecked(true);
        }

        // 初始化DatePicker
        final Calendar cal = Calendar.getInstance();
        final int nowyear = cal.get(Calendar.YEAR);// 记录下系统当前日期
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
                            Birth.updateDate(nowyear, nowmonth, nowday);// 若选择的日期大于当前日期，置为当前日期
                        }
                        monthOfYear = monthOfYear + 1;
                        newBirth = year + "-" + monthOfYear + "-" + dayOfMonth;// 设置新生日
                        age.setText(TimeChange.calcuTime(cal2, cal));// 设置年龄
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
                    enter.setEnabled(false);// 防止重复提交
                    // 先判断必填信息是否为空
                    if (TextUtils.isEmpty(name.getText())
                            || TextUtils.isEmpty(city.getText())) {
                        Toast.makeText(EditUserInfoActivity.this,
                                "用户名 和 所在城市 不能为空！", Toast.LENGTH_LONG).show();
                        enter.setEnabled(true);
                    } else {// 判断邮箱格式
                        String mailstr = mail.getText().toString();
                        if (!TextUtils.isEmpty(mailstr)) {
                            if (CheckUtil.checkEmail(mailstr)) {
                                paramap.put("user_mail", mailstr);// 邮箱不为空且合法
                                submitParams();
                            } else {
                                Toast.makeText(EditUserInfoActivity.this,
                                        "非法的邮箱！", Toast.LENGTH_LONG).show();
                                enter.setEnabled(true);
                            }
                        } else {
                            paramap.put("user_mail", "null");// 邮箱为空
                            submitParams();
                        }

                    }
                    break;
            }
        }

    }

    //提交参数信息
    private void submitParams() {
        paramap.put("user_id", Constant.loginUid);
        paramap.put("user_name", name.getText().toString());

        if (sex_nan.isChecked()) {
            paramap.put("user_sex", "男");
        } else {
            paramap.put("user_sex", "女");
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
