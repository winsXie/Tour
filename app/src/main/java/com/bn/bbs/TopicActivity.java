package com.bn.bbs;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableString;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bn.thread.DownloadPicThread;
import com.bn.thread.ImgAsyncDownload;
import com.bn.tour.R;
import com.bn.tour.ScanPicActivity;
import com.bn.user.LoginActivity;
import com.bn.user.LookUserInfoActivity;
import com.bn.util.CheckNetworkStatus;
import com.bn.util.CheckUtil;
import com.bn.util.Constant;
import com.bn.util.GetEmotion;
import com.bn.util.NetTransUtil;
import com.bn.util.TimeChange;

/**
 * @author Administrator ��������ֻ���ڼ������ڰ���ö������þ����ɹ���Ż��ʼ�����棩
 */
public class TopicActivity extends Activity {
    private static final int GET_TOPTOPIC = 0;//��ȡ���ڰ����ö�����handler��whatֵ��
    private static final int GET_BESTTOPIC = 1;//��ȡ���ڰ����þ���
    private static final int GET_TOPIC = 2;//��ȡ���ڰ���������
    private static final int LOAD_PAGE = 3;//��������
    private static final int UPDATE_TOPIC = 4;//�����������б������������ɹ���
    private static final int ADD_ZAN = 5;//Ϊ����������
    private static final int DISPLAY_TOAST = 6;//չʾtoast��ʾ/**/
    private static final int PUBLISH_TOPIC = 8;//����������
    private ListView listview;
    private TopicAdapter topicadapter;//listview������
    private ImgAsyncDownload imgdownload;//�첽����ͼƬ�̣߳�listview��ͼƬ�ļ��أ�
    private DownloadPicThread loadThread;//�첽����ͼƬ�̣߳���listview��ͼƬ�ļ��أ�
    private JSONObject json_topic, json_bloinfo, json_toptopic, json_best;//�洢��ȡ����Ϣ��JSONObject
    private JSONArray jarr_topic, jarr_toptopic;//����JSONObject�õ���JSONArray
    private View footer;//listview��footerview

    private LinearLayout footerLayout;//footer����
    private ScrollListener scrolllistener;//�������������ڷ������أ�
    private GetTopicThread getTopicThread;//����������߳�
    private GetHeaderThread getHeaderThread;//���header�����̣߳��ö����þ�����

    private static final int ITEMCOUTN = 5;//��������ÿ�μ��ص���Ŀ��
    private boolean loading = false;// ���ڼ��ر�־λ
    private boolean hadTotalLoaded = false;// ������ɱ�־λ
    private int zanPosition;//����item��λ��

    private Intent intent;

    private RelativeLayout rlloading, rlloadstauts;//���ڼ��صĻ��岼�ֺͼ��س�����ʾ��������״̬�Ĳ���
    private ImageView imvTip;//������ʾ����״̬��imageview
    private TextView tvtip;//��ʾ��������Ϣ
    private ClickListener clickListener = new ClickListener();//�̳���OnclickListener���࣬���ڴ������¼�

    /**
     * ��������̵߳���Ϣ
     */
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic);
        handler = new MyHandler(this);

        rlloading = (RelativeLayout) this.findViewById(R.id.RLtopic_load);
        rlloading.setVisibility(View.VISIBLE);

        getTopicThread = new GetTopicThread();
        getTopicThread.start();

        TextView tvBloname = (TextView) this.findViewById(R.id.TVtopic_bloname);// ���ڰ������
        json_bloinfo = Constant.currBlockInfo;
        tvBloname.setText(json_bloinfo.getString("blo_name"));
    }

    private static class MyHandler extends Handler {
        WeakReference<TopicActivity> mActivity;

        public MyHandler(TopicActivity activity) {
            mActivity = new WeakReference<TopicActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            TopicActivity currActivity = mActivity.get();
            switch (msg.what) {
                case GET_TOPTOPIC:
                    if ((String) msg.obj == null) {
                        currActivity.jarr_toptopic = new JSONArray();
                    } else {
                        currActivity.json_toptopic = JSONObject
                                .fromObject(CheckUtil
                                        .replaceBlank((String) msg.obj));
                        currActivity.jarr_toptopic = currActivity.json_toptopic
                                .getJSONArray("results");
                    }
                    break;
                case GET_BESTTOPIC:
                    if ((String) msg.obj != null) {
                        currActivity.json_best = JSONObject
                                .fromObject((String) msg.obj);
                    }
                    break;
                case GET_TOPIC:
                    if ((String) msg.obj == null) {
                        currActivity.jarr_topic = new JSONArray();
                    } else {
                        currActivity.json_topic = JSONObject
                                .fromObject(CheckUtil
                                        .replaceBlank((String) msg.obj));
                        currActivity.jarr_topic = currActivity.json_topic
                                .getJSONArray("results");
                        if (currActivity.jarr_topic.size() < ITEMCOUTN) {
                            currActivity.hadTotalLoaded = true;
                        }
                    }
                    currActivity.initView();
                    break;
                case LOAD_PAGE:
                    String get_json = (String) msg.obj;
                    // �������Ϊ�գ�����ȫ��������ɣ����־λ
                    if (get_json == null) {
                        currActivity.hadTotalLoaded = true;
                    } else {
                        get_json = CheckUtil.replaceBlank(get_json);// �滻�س����ţ�������json����
                        JSONObject json_new = JSONObject.fromObject(get_json);
                        JSONArray jarr_new = json_new.getJSONArray("results");
                        if (jarr_new.size() < ITEMCOUTN) {
                            currActivity.hadTotalLoaded = true;// ȡ�õ�����С��һҳ
                            // ��������ȫ���������
                        }
                        String json1 = currActivity.jarr_topic.toString();
                        String json2 = jarr_new.toString();
                        // ��װ�µ�json,����ʹ��add������ֻ��addһ��Ԫ��
                        String newjson = json1.substring(0, json1.length() - 1)
                                + "," + json2.substring(1, json2.length());
                        currActivity.jarr_topic = JSONArray.fromObject(newjson);
                        currActivity.topicadapter.notifyDataSetChanged();
                    }

                    if (currActivity.hadTotalLoaded) {
                        TextView tv = new TextView(currActivity);
                        tv.setText("��ȫ�����أ�");
                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                LayoutParams.MATCH_PARENT,
                                LayoutParams.MATCH_PARENT);
                        lp.setMargins(30, 0, 30, 0);
                        tv.setBackgroundColor(currActivity.getResources()
                                .getColor(R.color.white));
                        tv.setGravity(Gravity.CENTER);
                        tv.setTextColor(currActivity.getResources().getColor(
                                R.color.gray));
                        tv.setTextSize(20);
                        tv.setLayoutParams(lp);
                        currActivity.footerLayout.removeAllViews();
                        currActivity.footerLayout.addView(tv);

                    } else {
                        currActivity.listview
                                .removeFooterView(currActivity.footer);
                    }
                    currActivity.loading = false;
                    break;
                case UPDATE_TOPIC:
                    String topicinfo = (String) msg.obj;
                    if (topicinfo != null) {
                        currActivity.jarr_topic.add(0, topicinfo);
                        currActivity.topicadapter.notifyDataSetChanged();
                        currActivity.listview.setSelection(0);// ������header
                    }

                    // ȥ�����ػ���
                    if (currActivity.rlloading.getVisibility() == View.VISIBLE) {
                        currActivity.rlloading.setVisibility(View.GONE);
                    }
                    break;
                case ADD_ZAN:
                    String mess = (String) msg.obj;
                    if (mess != null) {
                        if (mess.equals("�����޹��˶���")) {
                            Toast.makeText(currActivity, mess,
                                    Toast.LENGTH_LONG).show();
                        } else if (mess.equals("���޳ɹ���")) {
                            String zancount = Integer
                                    .parseInt(currActivity.jarr_topic
                                            .getJSONObject(
                                                    currActivity.zanPosition)
                                            .getString("top_zancount"))
                                    + 1 + "";
                            currActivity.jarr_topic.getJSONObject(
                                    currActivity.zanPosition).put(
                                    "top_zancount", zancount);// �ı�����Դ

                            TextView tv = (TextView) currActivity.listview
                                    .findViewWithTag(currActivity.zanPosition)
                                    .findViewById(R.id.TVtopic_zancount);
                            tv.setText(zancount);

                            Toast.makeText(currActivity, mess,
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(currActivity, mess,
                                    Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(currActivity, "���ݴ���ʧ�ܣ�",
                                Toast.LENGTH_LONG).show();
                    }
                    break;
                case DISPLAY_TOAST:
                    // �ڷ������أ������ɹ��󷵻أ��ᷢ�ʹ���Ϣ
                    String toast = (String) msg.obj;
                    if (toast != null) {
                        Toast.makeText(currActivity, toast, Toast.LENGTH_SHORT)
                                .show();
                    }
                    // ȥ�����ػ���
                    if (currActivity.rlloading.getVisibility() == View.VISIBLE) {
                        currActivity.rlloading.setVisibility(View.GONE);
                    }
                    // �϶���û�м����꣬ȥ��footer
                    if (currActivity.listview.getFooterViewsCount() > 0) {
                        currActivity.listview
                                .removeFooterView(currActivity.footer);
                    }
                    currActivity.loading = false;// ���ñ�־λ
                    break;
                case Constant.CONNECTIONTIMEOUT:
                    if (currActivity.rlloading.getVisibility() == View.VISIBLE) {
                        currActivity.rlloading.setVisibility(View.GONE);
                    }
                    currActivity.rlloadstauts = (RelativeLayout) currActivity
                            .findViewById(R.id.RLtopic_loadstauts);
                    currActivity.imvTip = (ImageView) currActivity
                            .findViewById(R.id.IMVloadtip);
                    currActivity.tvtip = (TextView) currActivity
                            .findViewById(R.id.TVloadtip);
                    currActivity.imvTip
                            .setImageResource(R.drawable.network_error);

                    if (CheckNetworkStatus.checkNetworkAvailable(currActivity)) {// �ֻ��������
                        currActivity.tvtip.setText("������󣬵������");
                        Toast.makeText(currActivity, "���ӳ�ʱ��", Toast.LENGTH_LONG)
                                .show();
                    } else {
                        currActivity.tvtip.setText("�����������ӣ�");
                        Toast.makeText(currActivity, "���粻���ã������������ӣ�",
                                Toast.LENGTH_LONG).show();
                    }

                    currActivity.rlloadstauts.setVisibility(View.VISIBLE);
                    currActivity.imvTip
                            .setOnClickListener(currActivity.clickListener);
                    break;
                case Constant.SOTIMEOUT:
                    if (currActivity.rlloading.getVisibility() == View.VISIBLE) {
                        currActivity.rlloading.setVisibility(View.GONE);
                    }
                    currActivity.rlloadstauts = (RelativeLayout) currActivity
                            .findViewById(R.id.RLtopic_loadstauts);
                    currActivity.imvTip = (ImageView) currActivity
                            .findViewById(R.id.IMVloadtip);
                    currActivity.tvtip = (TextView) currActivity
                            .findViewById(R.id.TVloadtip);
                    currActivity.imvTip.setImageResource(R.drawable.refresh);
                    currActivity.tvtip.setText("������¼���");
                    currActivity.rlloadstauts.setVisibility(View.VISIBLE);
                    currActivity.imvTip
                            .setOnClickListener(currActivity.clickListener);
                    Toast.makeText(currActivity, "��ȡ���ݳ�ʱ��", Toast.LENGTH_LONG)
                            .show();
                    break;
            }
        }
    }

    /**
     * ��ʼ��listview����adapter
     */
    private void initView() {
        ImageView imvBack = (ImageView) this.findViewById(R.id.IMVtopic_back);
        ImageView imvEditTopic = (ImageView) this
                .findViewById(R.id.IMVedit_topic);

        listview = (ListView) this.findViewById(R.id.LVtopic);
        this.addHeader();
        // �������ص�footer��ʾ
        footer = getLayoutInflater().inflate(R.layout.footer, null);
        footerLayout = (LinearLayout) footer.findViewById(R.id.LLfooter);

        topicadapter = new TopicAdapter();
        listview.setAdapter(topicadapter);

        // û������ʱ�������ɱ�־λ����ֹ���س�footer
        if (jarr_topic.size() == 0) {
            hadTotalLoaded = true;
        }
        // �����������������أ�
        scrolllistener = new ScrollListener();
        listview.setOnScrollListener(scrolllistener);

        imvBack.setOnClickListener(clickListener);
        imvEditTopic.setOnClickListener(clickListener);

        // ListView��item����
        listview.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                                    final int arg2, long arg3) {
                if (arg2 != 0) {
                    // ��Ϊ��һ��Header����Ҫ-1
                    Constant.currTopicId = jarr_topic.getJSONObject(arg2 - 1)
                            .getString("top_id");// ����ReplyActivity���ڵ�������

                    Constant.currTopicInfo = jarr_topic.getJSONObject(arg2 - 1);// ����ReplyActivity��Header����
                    // ���������������+1(Android)
                    new Thread() {

                        @Override
                        public void run() {
                            NetTransUtil.addTopBrowCount(Constant.currTopicId);
                        }

                    }.start();

                    intent = new Intent();
                    intent.setClass(TopicActivity.this, ReplyActivity.class);
                    intent.putExtra("BLONAME",
                            json_bloinfo.getString("blo_name"));
                    TopicActivity.this.startActivity(intent);
                }
            }
        });

        // ȥ�����ػ���ͼ��
        if (rlloading.getVisibility() == View.VISIBLE) {
            rlloading.setVisibility(View.GONE);
        }
    }

    /**
     * ����header
     */
    private void addHeader() {
        // ���Header��View
        View view = getLayoutInflater().inflate(R.layout.header_topic, null);

        ImageView imvBloPic = (ImageView) view
                .findViewById(R.id.IMVheader_top_blopic);

        TextView tvBloName = (TextView) view
                .findViewById(R.id.TVheader_top_bloname);
        TextView tvTopicCount = (TextView) view
                .findViewById(R.id.TVheader_top_topic_count);
        TextView tvTitle1 = (TextView) view
                .findViewById(R.id.TVheader_top_topic_title1);
        TextView tvTitle2 = (TextView) view
                .findViewById(R.id.TVheader_top_topic_title2);
        TextView tvTitle = (TextView) view
                .findViewById(R.id.TVheader_top_topic_title);

        LinearLayout lltop1 = (LinearLayout) view.findViewById(R.id.LLblo_top1);
        LinearLayout lltop2 = (LinearLayout) view.findViewById(R.id.LLblo_top2);
        LinearLayout llbest = (LinearLayout) view.findViewById(R.id.LLblo_best);
        // ���ð������
        imvBloPic.setImageResource(R.drawable.defaultimage);// Ԥ��ͼƬ
        // ����һ���̼߳���Header���ͼƬ���ȴ�SD���л�ȡ��SD��û���ٴ�������أ�
        loadThread = new DownloadPicThread(json_bloinfo.getString("blo_pic"),
                imvBloPic, handler);
        loadThread.start();
        tvBloName.setText(json_bloinfo.getString("blo_name"));
        tvTopicCount.setText(json_bloinfo.getString("blo_topcount"));

        // �����ö����Ƿ���ʾ���Լ�����
        if (jarr_toptopic != null) {
            if (jarr_toptopic.size() == 0) {
            } else if (jarr_toptopic.size() == 1) {
                lltop1.setVisibility(View.VISIBLE);
                tvTitle1.setText(jarr_toptopic.getJSONObject(0).getString(
                        "top_title"));
            } else {
                lltop1.setVisibility(View.VISIBLE);
                lltop2.setVisibility(View.VISIBLE);
                tvTitle1.setText(jarr_toptopic.getJSONObject(0).getString(
                        "top_title"));
                tvTitle2.setText(jarr_toptopic.getJSONObject(1).getString(
                        "top_title"));
            }
        }

        // �����þ����Ƿ���ʾ���Լ�����
        if (json_best != null) {
            llbest.setVisibility(View.VISIBLE);
            tvTitle.setText(json_best.getString("top_title"));
        } else {
            llbest.setVisibility(View.GONE);
        }

		/*
         * ��Ӽ���
		 */
        // ���ͼƬ����
        imvBloPic.setOnClickListener(clickListener);

        // �ö�/�þ���LinearLayout����
        lltop1.setOnClickListener(clickListener);
        lltop2.setOnClickListener(clickListener);
        llbest.setOnClickListener(clickListener);

        // ��ListView���Header
        listview.addHeaderView(view, null, false);
    }

    // ListView��BaseAdapter������
    private class TopicAdapter extends BaseAdapter {
        private ViewHolder viewholder;

        @Override
        public int getCount() {
            return jarr_topic.size();
        }

        @Override
        public Object getItem(int position) {
            return jarr_topic.getJSONObject(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView,
                            ViewGroup parent) {
            final JSONObject json_temp = jarr_topic.getJSONObject(position);// ȡ��ÿ��item��json����
            // ����View
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.item_topic,
                        null);
                viewholder = new ViewHolder();

                viewholder.imvHeadImg = (ImageView) convertView
                        .findViewById(R.id.IMVtopic_head);
                viewholder.imvSex = (ImageView) convertView
                        .findViewById(R.id.IMVtopic_sex);
                viewholder.tvSendTime = (TextView) convertView
                        .findViewById(R.id.TVtopic_sendtime);
                viewholder.tvUserName = (TextView) convertView
                        .findViewById(R.id.TVtopic_uname);
                viewholder.tvUserAge = (TextView) convertView
                        .findViewById(R.id.TVtopic_uage);
                viewholder.tvTitle = (TextView) convertView
                        .findViewById(R.id.TVtopic_title);
                viewholder.tvContent = (TextView) convertView
                        .findViewById(R.id.TVtopic_content);
                viewholder.imvPic1 = (ImageView) convertView
                        .findViewById(R.id.IMVtopic_pic1);
                viewholder.imvPic2 = (ImageView) convertView
                        .findViewById(R.id.IMVtopic_pic2);
                viewholder.imvPic3 = (ImageView) convertView
                        .findViewById(R.id.IMVtopic_pic3);
                viewholder.imvZan = (ImageView) convertView
                        .findViewById(R.id.IMVtopic_zan);
                viewholder.imvBrow = (ImageView) convertView
                        .findViewById(R.id.IMVtopic_brow);
                viewholder.imvZan = (ImageView) convertView
                        .findViewById(R.id.IMVtopic_zan);
                viewholder.tvBrowcount = (TextView) convertView
                        .findViewById(R.id.TVtopic_browcount);
                viewholder.tvZancount = (TextView) convertView
                        .findViewById(R.id.TVtopic_zancount);
                viewholder.tvReplycount = (TextView) convertView
                        .findViewById(R.id.TVtopic_replycount);

                convertView.setTag(viewholder);// ���ñ�ǩ
            } else {
                viewholder = (ViewHolder) convertView.getTag();// ���ݱ�ǩ�õ���ͼ
            }

            viewholder.tvUserName.setText(json_temp.getString("user_name"));
            viewholder.tvUserAge.setText(json_temp.getString("user_age") + "��");
            if (json_temp.getString("top_title").equals("null")) {
                viewholder.tvTitle.setVisibility(View.GONE);
            } else {
                viewholder.tvTitle.setVisibility(View.VISIBLE);
                viewholder.tvTitle.setText(json_temp.getString("top_title"));
            }
            // �������������ñ���
            SpannableString spannable = GetEmotion.getEmotion(
                    TopicActivity.this, json_temp.getString("top_content"));
            viewholder.tvContent.setText(spannable);
            viewholder.tvSendTime.setText(TimeChange.changeTime(json_temp
                    .getString("top_sendtime")));
            if (json_temp.getString("top_browcount").equals("0")) {
                viewholder.imvBrow.setVisibility(View.INVISIBLE);
                viewholder.tvBrowcount.setText("");
            } else {
                viewholder.imvBrow.setVisibility(View.VISIBLE);
                viewholder.tvBrowcount.setText(json_temp
                        .getString("top_browcount"));
            }

            if (json_temp.getString("top_zancount").equals("0")) {
                viewholder.tvZancount.setText("��");
            } else {
                viewholder.tvZancount.setText(json_temp
                        .getString("top_zancount"));
            }

            if (json_temp.getString("top_replycount").equals("0")) {
                viewholder.tvReplycount.setText("");
            } else {
                viewholder.tvReplycount.setText(json_temp
                        .getString("top_replycount"));
            }

            if (json_temp.getString("user_sex").equals("��")) {
                viewholder.imvSex.setImageResource(R.drawable.sex_man);
            } else {
                viewholder.imvSex.setImageResource(R.drawable.sex_woman);
            }

            viewholder.tvZancount.setTag(position);
            imgdownload = ImgAsyncDownload.getInstance();

            if (imgdownload != null) {
                // �첽����ͼƬ
                // ����ͷ��
                viewholder.imvHeadImg.setTag(json_temp.getString("user_image"));
                viewholder.imvHeadImg.setImageResource(R.drawable.defaultimage);
                imgdownload.imageDownload(viewholder.imvHeadImg);

                final String picPath = json_temp.getString("top_pic");// ������ͼƬ
                if (picPath.equals("null")) {
                    viewholder.imvPic1.setVisibility(View.GONE);
                    viewholder.imvPic2.setVisibility(View.GONE);
                    viewholder.imvPic3.setVisibility(View.GONE);
                } else {
                    String[] pathArray = {"null", "null", "null"};
                    String[] temp = picPath.split(",");
                    for (int i = 0; i < temp.length; i++) {
                        pathArray[i] = temp[i];
                    }

                    if (!pathArray[0].equals("null"))// ���ص�һ��������ͼƬ
                    {
                        viewholder.imvPic1.setTag(pathArray[0]);
                        viewholder.imvPic1.setVisibility(View.VISIBLE);// ��ImageView��Ϊ�ɼ�
                        viewholder.imvPic1
                                .setImageResource(R.drawable.default_picture);// ����ʾԤ��ͼƬ,��ֹ�ڼ������֮ǰ��ʾ��������ͼƬ(view���õ���)

                        imgdownload.imageDownload(viewholder.imvPic1);

                        viewholder.imvPic1
                                .setOnClickListener(new OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        scanPic(picPath, 0);
                                    }
                                });
                    } else {
                        viewholder.imvPic1.setVisibility(View.GONE);
                    }
                    if (!pathArray[1].equals("null"))// ���صڶ���������ͼƬ
                    {
                        viewholder.imvPic2.setTag(pathArray[1]);
                        viewholder.imvPic2.setVisibility(View.VISIBLE);
                        viewholder.imvPic2
                                .setImageResource(R.drawable.default_picture);
                        imgdownload.imageDownload(viewholder.imvPic2);

                        viewholder.imvPic2
                                .setOnClickListener(new OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        scanPic(picPath, 1);
                                    }
                                });
                    } else {
                        viewholder.imvPic2.setVisibility(View.GONE);
                    }
                    if (!pathArray[2].equals("null"))// ���ص�����������ͼƬ
                    {
                        viewholder.imvPic3.setTag(pathArray[2]);
                        viewholder.imvPic3.setVisibility(View.VISIBLE);
                        viewholder.imvPic3
                                .setImageResource(R.drawable.default_picture);
                        imgdownload.imageDownload(viewholder.imvPic3);

                        viewholder.imvPic3
                                .setOnClickListener(new OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        scanPic(picPath, 2);
                                    }
                                });
                    } else {
                        viewholder.imvPic3.setVisibility(View.GONE);
                    }

                }
            }
            viewholder.imvHeadImg.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(TopicActivity.this,
                            LookUserInfoActivity.class);
                    intent.putExtra("USERID", json_temp.getString("top_userid"));
                    startActivity(intent);
                }
            });

            viewholder.imvZan.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (Constant.isLogin) {
                        zanPosition = position;
                        new Thread() {

                            @Override
                            public void run() {

                                try {
                                    String mess = NetTransUtil.addTopicZan(
                                            Constant.loginUid,
                                            json_temp.getString("top_id"));
                                    handler.obtainMessage(ADD_ZAN, mess)
                                            .sendToTarget();
                                } catch (Exception e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }

                            }

                        }.start();

                    } else {
                        intent = new Intent(TopicActivity.this, LoginActivity.class);
                        startActivity(intent);
                    }
                }
            });

            return convertView;
        }

        private class ViewHolder {
            ImageView imvHeadImg, imvPic1, imvPic2, imvPic3, imvSex, imvBrow,
                    imvZan;
            TextView tvUserName, tvUserAge, tvTitle, tvContent, tvBrowcount,
                    tvZancount, tvReplycount, tvSendTime;
        }

    }

    /**
     * ���ͼƬ
     *
     * @param path     ���ӵ�ͼƬ·��
     * @param position �ڼ���ͼƬ
     */
    private void scanPic(String path, int position) {
        String[] picpath = path.split(",");
        ArrayList<String> picpathList = new ArrayList<String>();

        for (int i = 0; i < picpath.length; i++) {
            picpathList.add(picpath[i]);
        }
        intent = new Intent(TopicActivity.this, ScanPicActivity.class);
        intent.putExtra("POSITION", position);
        intent.putStringArrayListExtra("PATHLIST", picpathList);
        startActivity(intent);
    }

    // OnClickListener���������
    private class ClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.IMVtopic_back:
                    TopicActivity.this.finish();
                    break;
                case R.id.IMVedit_topic:

                    if (Constant.isLogin) {
                        intent = new Intent(TopicActivity.this,
                                PublishTopicActivity.class);
                        startActivityForResult(intent, PUBLISH_TOPIC);
                    } else {
                        intent = new Intent(TopicActivity.this, LoginActivity.class);
                        startActivity(intent);
                    }
                    break;
                case R.id.LLblo_top1:
                    Constant.currTopicId = jarr_toptopic.getJSONObject(0)
                            .getString("top_id");// ����ReplyActivity���ڵ�������
                    Constant.currTopicInfo = jarr_toptopic.getJSONObject(0);// ����ReplyActivity��Header����

                    intent = new Intent();
                    intent.setClass(TopicActivity.this, ReplyActivity.class);
                    intent.putExtra("BLONAME",
                            json_bloinfo.getString("blo_name"));
                    TopicActivity.this.startActivity(intent);
                    break;
                case R.id.LLblo_top2:
                    Constant.currTopicId = jarr_toptopic.getJSONObject(1)
                            .getString("top_id");// ����ReplyActivity���ڵ�������
                    Constant.currTopicInfo = jarr_toptopic.getJSONObject(1);// ����ReplyActivity��Header����

                    intent = new Intent();
                    intent.setClass(TopicActivity.this, ReplyActivity.class);
                    intent.putExtra("BLONAME",
                            json_bloinfo.getString("blo_name"));
                    TopicActivity.this.startActivity(intent);
                    break;
                case R.id.LLblo_best:
                    Constant.currTopicId = json_best.getString("top_id");// ����ReplyActivity���ڵ�������
                    Constant.currTopicInfo = json_best;// ����ReplyActivity��Header����

                    intent = new Intent();
                    intent.setClass(TopicActivity.this, ReplyActivity.class);
                    intent.putExtra("BLONAME",
                            json_bloinfo.getString("blo_name"));
                    TopicActivity.this.startActivity(intent);
                    break;
                case R.id.IMVloadtip:
                    // ȥ����ʾ
                    if (rlloadstauts.getVisibility() == View.VISIBLE) {
                        rlloadstauts.setVisibility(View.GONE);
                    }

                    if ((getTopicThread != null) && !getTopicThread.isAlive()) {
                        if (rlloading.getVisibility() == View.GONE) {
                            rlloading.setVisibility(View.VISIBLE);
                        }

                        getTopicThread = new GetTopicThread();
                        getTopicThread.start();
                    }
                    break;
            }
        }
    }

    /**
     * @author Administrator ��������
     */
    private class ScrollListener implements OnScrollListener {

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {
            int lastposi = listview.getLastVisiblePosition();

			/*
             * totalItemCount��position������header��footer
			 * ֻ���ڻ��������һ��item��������û��ȫ��������ɣ��Լ�û�����ڼ��ص�����²ż���������һ��
			 */
            if ((lastposi + 1) == totalItemCount && !loading && !hadTotalLoaded) {
                loading = true;
                listview.addFooterView(footer);
                new Thread() {

                    @Override
                    public void run() {
                        try {
                            Thread.sleep(1 * 1000);
                            String json_new_topic = NetTransUtil
                                    .getNfTopicOfBlo(
                                            Constant.currBloId,
                                            "false",
                                            jarr_topic.getJSONObject(
                                                    jarr_topic.size() - 1)
                                                    .getString("top_id"));
                            handler.obtainMessage(LOAD_PAGE, json_new_topic)
                                    .sendToTarget();
                        } catch (Exception e) {
                            String mess = e.getClass().getName();
                            if (mess.contains("ConnectTimeoutException")
                                    || mess.contains("ConnectException")) {
                                handler.obtainMessage(DISPLAY_TOAST, "���ӳ�ʱ��")
                                        .sendToTarget();
                            } else if (mess.contains("SocketTimeoutException")) {
                                handler.obtainMessage(DISPLAY_TOAST, "��ȡ���ݳ�ʱ��")
                                        .sendToTarget();
                            }
                            e.printStackTrace();
                        }
                    }

                }.start();
            }
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
        }

    }

    /**
     * @author Administrator ȡ���������߳�
     */
    private class GetTopicThread extends Thread {
        boolean isContinue = true;// ����������������־λ����������ö��þ�ʧ�ܣ��������������������

        @Override
        public void run() {
            getHeaderThread = new GetHeaderThread();
            getHeaderThread.start();
            try {
				/*
				 * ������ö��þ����̼߳��룬��֤�ڻ���ڻ���ö��þ���֮���ټ��ذ���ڵ�����
				 * �������ȴ�ʱ�䣬��ֹ��getHeaderThread�̼߳���ʱ���������GetTopicThread�̲߳��ٵȴ���ȥ����������
				 */
                getHeaderThread.join(9 * 1000);
            } catch (InterruptedException e1) {
                isContinue = false;
                e1.printStackTrace();
            }
            if (isContinue) {
                try {
                    String topic = NetTransUtil.getNfTopicOfBlo(
                            Constant.currBloId, "false", 0 + "");
                    handler.obtainMessage(GET_TOPIC, topic).sendToTarget();
                } catch (Exception e) {
                    String mess = e.getClass().getName();
                    if (mess.contains("ConnectTimeoutException")
                            || mess.contains("ConnectException")) {
                        handler.obtainMessage(Constant.CONNECTIONTIMEOUT)
                                .sendToTarget();
                    } else if (mess.contains("SocketTimeoutException")) {
                        handler.obtainMessage(Constant.SOTIMEOUT)
                                .sendToTarget();
                    }
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * @author Administrator ����header�߳�
     */
    private class GetHeaderThread extends Thread {

        @Override
        public void run() {
            try {
                String toptopic = NetTransUtil.getTopTopic(Constant.currBloId);
                handler.obtainMessage(GET_TOPTOPIC, toptopic).sendToTarget();
                String besttopic = NetTransUtil
                        .getBestTopic(Constant.currBloId);
                handler.obtainMessage(GET_BESTTOPIC, besttopic).sendToTarget();
            } catch (Exception e) {
                getTopicThread.isContinue = false;// ���ñ�־λ
                String mess = e.getClass().getName();
                if (mess.contains("ConnectTimeoutException")
                        || mess.contains("ConnectException")) {
                    handler.obtainMessage(Constant.CONNECTIONTIMEOUT)
                            .sendToTarget();
                } else if (mess.contains("SocketTimeoutException")) {
                    handler.obtainMessage(Constant.SOTIMEOUT).sendToTarget();
                }
                e.printStackTrace();
            }
        }

    }

    /*
     * ���ڷ�����������ص�����
     * ��ָ����activity����ʱ����ô˷���(����requestCode�жϷ��ص����ĸ�activity������resultCode�����صĲ�ͬ���
     * �� dataֻ����ָ��activity�ر�ʱ��onDestroy()ִ�У��Ż᷵��һ���ǿյ�ֵ)
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PUBLISH_TOPIC:
                if (resultCode == 1) {
                    if (data != null) {
                        // �ոշ����������ID���ݴ�ȡ����������Ϣ
                        final String topicid = data.getStringExtra("TOPICID");
                        if (topicid != null) {
                            if (rlloading.getVisibility() == View.GONE) {
                                rlloading.setVisibility(View.VISIBLE);
                            }
                            new Thread() {

                                @Override
                                public void run() {
                                    try {
                                        String topicinfo = NetTransUtil
                                                .getTopicById(topicid);
                                        handler.obtainMessage(UPDATE_TOPIC,
                                                topicinfo).sendToTarget();
                                    } catch (Exception e) {
                                        String mess = e.getClass().getName();
                                        if (mess.contains("ConnectTimeoutException")
                                                || mess.contains("ConnectException")) {
                                            handler.obtainMessage(
                                                    DISPLAY_TOAST, "���ӳ�ʱ��")
                                                    .sendToTarget();
                                        } else if (mess
                                                .contains("SocketTimeoutException")) {
                                            handler.obtainMessage(
                                                    DISPLAY_TOAST, "��ȡ���ݳ�ʱ��")
                                                    .sendToTarget();
                                        }
                                        e.printStackTrace();
                                    }
                                }

                            }.start();
                        }

                    }

                }
                break;
        }
    }

}
