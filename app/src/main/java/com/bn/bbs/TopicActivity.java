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
 * @author Administrator 主题帖（只有在加载所在版块置顶帖和置精帖成功后才会初始化界面）
 */
public class TopicActivity extends Activity {
    private static final int GET_TOPTOPIC = 0;//获取所在版块的置顶帖（handler的what值）
    private static final int GET_BESTTOPIC = 1;//获取所在版块的置精帖
    private static final int GET_TOPIC = 2;//获取所在版块的主题帖
    private static final int LOAD_PAGE = 3;//分批加载
    private static final int UPDATE_TOPIC = 4;//更新主题帖列表（发表主题帖成功后）
    private static final int ADD_ZAN = 5;//为主题帖点赞
    private static final int DISPLAY_TOAST = 6;//展示toast提示/**/
    private static final int PUBLISH_TOPIC = 8;//发表主题帖
    private ListView listview;
    private TopicAdapter topicadapter;//listview适配器
    private ImgAsyncDownload imgdownload;//异步加载图片线程（listview内图片的加载）
    private DownloadPicThread loadThread;//异步加载图片线程（非listview内图片的加载）
    private JSONObject json_topic, json_bloinfo, json_toptopic, json_best;//存储获取的信息的JSONObject
    private JSONArray jarr_topic, jarr_toptopic;//根据JSONObject得到的JSONArray
    private View footer;//listview的footerview

    private LinearLayout footerLayout;//footer布局
    private ScrollListener scrolllistener;//滑动监听（用于分批加载）
    private GetTopicThread getTopicThread;//获得主题帖线程
    private GetHeaderThread getHeaderThread;//获得header内容线程（置顶和置精帖）

    private static final int ITEMCOUTN = 5;//分批加载每次加载的条目数
    private boolean loading = false;// 正在加载标志位
    private boolean hadTotalLoaded = false;// 加载完成标志位
    private int zanPosition;//点赞item的位置

    private Intent intent;

    private RelativeLayout rlloading, rlloadstauts;//正在加载的缓冲布局和加载出错提示网络连接状态的布局
    private ImageView imvTip;//用于提示网络状态的imageview
    private TextView tvtip;//提示的文字信息
    private ClickListener clickListener = new ClickListener();//继承自OnclickListener的类，用于处理点击事件

    /**
     * 处理各个线程的消息
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

        TextView tvBloname = (TextView) this.findViewById(R.id.TVtopic_bloname);// 所在版块名称
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
                    // 如果数据为空，则已全部加载完成，设标志位
                    if (get_json == null) {
                        currActivity.hadTotalLoaded = true;
                    } else {
                        get_json = CheckUtil.replaceBlank(get_json);// 替换回车符号，否则构造json报错
                        JSONObject json_new = JSONObject.fromObject(get_json);
                        JSONArray jarr_new = json_new.getJSONArray("results");
                        if (jarr_new.size() < ITEMCOUTN) {
                            currActivity.hadTotalLoaded = true;// 取得的数据小于一页
                            // 的条数，全部加载完成
                        }
                        String json1 = currActivity.jarr_topic.toString();
                        String json2 = jarr_new.toString();
                        // 组装新的json,不能使用add方法，只能add一个元素
                        String newjson = json1.substring(0, json1.length() - 1)
                                + "," + json2.substring(1, json2.length());
                        currActivity.jarr_topic = JSONArray.fromObject(newjson);
                        currActivity.topicadapter.notifyDataSetChanged();
                    }

                    if (currActivity.hadTotalLoaded) {
                        TextView tv = new TextView(currActivity);
                        tv.setText("已全部加载！");
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
                        currActivity.listview.setSelection(0);// 滚动到header
                    }

                    // 去除加载缓冲
                    if (currActivity.rlloading.getVisibility() == View.VISIBLE) {
                        currActivity.rlloading.setVisibility(View.GONE);
                    }
                    break;
                case ADD_ZAN:
                    String mess = (String) msg.obj;
                    if (mess != null) {
                        if (mess.equals("您已赞过此对象！")) {
                            Toast.makeText(currActivity, mess,
                                    Toast.LENGTH_LONG).show();
                        } else if (mess.equals("点赞成功！")) {
                            String zancount = Integer
                                    .parseInt(currActivity.jarr_topic
                                            .getJSONObject(
                                                    currActivity.zanPosition)
                                            .getString("top_zancount"))
                                    + 1 + "";
                            currActivity.jarr_topic.getJSONObject(
                                    currActivity.zanPosition).put(
                                    "top_zancount", zancount);// 改变数据源

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
                        Toast.makeText(currActivity, "数据传输失败！",
                                Toast.LENGTH_LONG).show();
                    }
                    break;
                case DISPLAY_TOAST:
                    // 在分批加载，发帖成功后返回，会发送此消息
                    String toast = (String) msg.obj;
                    if (toast != null) {
                        Toast.makeText(currActivity, toast, Toast.LENGTH_SHORT)
                                .show();
                    }
                    // 去除加载缓冲
                    if (currActivity.rlloading.getVisibility() == View.VISIBLE) {
                        currActivity.rlloading.setVisibility(View.GONE);
                    }
                    // 肯定是没有加载完，去除footer
                    if (currActivity.listview.getFooterViewsCount() > 0) {
                        currActivity.listview
                                .removeFooterView(currActivity.footer);
                    }
                    currActivity.loading = false;// 重置标志位
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

                    if (CheckNetworkStatus.checkNetworkAvailable(currActivity)) {// 手机网络可用
                        currActivity.tvtip.setText("网络错误，点击重试");
                        Toast.makeText(currActivity, "连接超时！", Toast.LENGTH_LONG)
                                .show();
                    } else {
                        currActivity.tvtip.setText("请检查网络连接！");
                        Toast.makeText(currActivity, "网络不可用！请检查网络连接！",
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
                    currActivity.tvtip.setText("点击重新加载");
                    currActivity.rlloadstauts.setVisibility(View.VISIBLE);
                    currActivity.imvTip
                            .setOnClickListener(currActivity.clickListener);
                    Toast.makeText(currActivity, "读取数据超时！", Toast.LENGTH_LONG)
                            .show();
                    break;
            }
        }
    }

    /**
     * 初始化listview设置adapter
     */
    private void initView() {
        ImageView imvBack = (ImageView) this.findViewById(R.id.IMVtopic_back);
        ImageView imvEditTopic = (ImageView) this
                .findViewById(R.id.IMVedit_topic);

        listview = (ListView) this.findViewById(R.id.LVtopic);
        this.addHeader();
        // 分批加载的footer提示
        footer = getLayoutInflater().inflate(R.layout.footer, null);
        footerLayout = (LinearLayout) footer.findViewById(R.id.LLfooter);

        topicadapter = new TopicAdapter();
        listview.setAdapter(topicadapter);

        // 没有数据时设加载完成标志位，防止加载出footer
        if (jarr_topic.size() == 0) {
            hadTotalLoaded = true;
        }
        // 滑动监听（分批加载）
        scrolllistener = new ScrollListener();
        listview.setOnScrollListener(scrolllistener);

        imvBack.setOnClickListener(clickListener);
        imvEditTopic.setOnClickListener(clickListener);

        // ListView的item监听
        listview.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                                    final int arg2, long arg3) {
                if (arg2 != 0) {
                    // 因为有一个Header所以要-1
                    Constant.currTopicId = jarr_topic.getJSONObject(arg2 - 1)
                            .getString("top_id");// 设置ReplyActivity所在的主题帖

                    Constant.currTopicInfo = jarr_topic.getJSONObject(arg2 - 1);// 设置ReplyActivity的Header内容
                    // 将主题帖的浏览数+1(Android)
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

        // 去除加载缓冲图标
        if (rlloading.getVisibility() == View.VISIBLE) {
            rlloading.setVisibility(View.GONE);
        }
    }

    /**
     * 设置header
     */
    private void addHeader() {
        // 获得Header的View
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
        // 设置版块内容
        imvBloPic.setImageResource(R.drawable.defaultimage);// 预设图片
        // 另起一个线程加载Header里的图片（先从SD卡中获取，SD卡没有再从网络加载）
        loadThread = new DownloadPicThread(json_bloinfo.getString("blo_pic"),
                imvBloPic, handler);
        loadThread.start();
        tvBloName.setText(json_bloinfo.getString("blo_name"));
        tvTopicCount.setText(json_bloinfo.getString("blo_topcount"));

        // 设置置顶帖是否显示，以及内容
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

        // 设置置精帖是否显示，以及内容
        if (json_best != null) {
            llbest.setVisibility(View.VISIBLE);
            tvTitle.setText(json_best.getString("top_title"));
        } else {
            llbest.setVisibility(View.GONE);
        }

		/*
         * 添加监听
		 */
        // 版块图片监听
        imvBloPic.setOnClickListener(clickListener);

        // 置顶/置精帖LinearLayout监听
        lltop1.setOnClickListener(clickListener);
        lltop2.setOnClickListener(clickListener);
        llbest.setOnClickListener(clickListener);

        // 给ListView添加Header
        listview.addHeaderView(view, null, false);
    }

    // ListView的BaseAdapter适配器
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
            final JSONObject json_temp = jarr_topic.getJSONObject(position);// 取得每个item的json数据
            // 重用View
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

                convertView.setTag(viewholder);// 设置标签
            } else {
                viewholder = (ViewHolder) convertView.getTag();// 根据标签得到视图
            }

            viewholder.tvUserName.setText(json_temp.getString("user_name"));
            viewholder.tvUserAge.setText(json_temp.getString("user_age") + "岁");
            if (json_temp.getString("top_title").equals("null")) {
                viewholder.tvTitle.setVisibility(View.GONE);
            } else {
                viewholder.tvTitle.setVisibility(View.VISIBLE);
                viewholder.tvTitle.setText(json_temp.getString("top_title"));
            }
            // 主题帖内容设置表情
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
                viewholder.tvZancount.setText("赞");
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

            if (json_temp.getString("user_sex").equals("男")) {
                viewholder.imvSex.setImageResource(R.drawable.sex_man);
            } else {
                viewholder.imvSex.setImageResource(R.drawable.sex_woman);
            }

            viewholder.tvZancount.setTag(position);
            imgdownload = ImgAsyncDownload.getInstance();

            if (imgdownload != null) {
                // 异步下载图片
                // 加载头像
                viewholder.imvHeadImg.setTag(json_temp.getString("user_image"));
                viewholder.imvHeadImg.setImageResource(R.drawable.defaultimage);
                imgdownload.imageDownload(viewholder.imvHeadImg);

                final String picPath = json_temp.getString("top_pic");// 主题帖图片
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

                    if (!pathArray[0].equals("null"))// 加载第一张主题帖图片
                    {
                        viewholder.imvPic1.setTag(pathArray[0]);
                        viewholder.imvPic1.setVisibility(View.VISIBLE);// 将ImageView设为可见
                        viewholder.imvPic1
                                .setImageResource(R.drawable.default_picture);// 先显示预设图片,防止在加载完成之前显示出其他的图片(view重用导致)

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
                    if (!pathArray[1].equals("null"))// 加载第二张主题帖图片
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
                    if (!pathArray[2].equals("null"))// 加载第三张主题帖图片
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
     * 浏览图片
     *
     * @param path     帖子的图片路径
     * @param position 第几张图片
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

    // OnClickListener点击监听类
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
                            .getString("top_id");// 设置ReplyActivity所在的主题帖
                    Constant.currTopicInfo = jarr_toptopic.getJSONObject(0);// 设置ReplyActivity的Header内容

                    intent = new Intent();
                    intent.setClass(TopicActivity.this, ReplyActivity.class);
                    intent.putExtra("BLONAME",
                            json_bloinfo.getString("blo_name"));
                    TopicActivity.this.startActivity(intent);
                    break;
                case R.id.LLblo_top2:
                    Constant.currTopicId = jarr_toptopic.getJSONObject(1)
                            .getString("top_id");// 设置ReplyActivity所在的主题帖
                    Constant.currTopicInfo = jarr_toptopic.getJSONObject(1);// 设置ReplyActivity的Header内容

                    intent = new Intent();
                    intent.setClass(TopicActivity.this, ReplyActivity.class);
                    intent.putExtra("BLONAME",
                            json_bloinfo.getString("blo_name"));
                    TopicActivity.this.startActivity(intent);
                    break;
                case R.id.LLblo_best:
                    Constant.currTopicId = json_best.getString("top_id");// 设置ReplyActivity所在的主题帖
                    Constant.currTopicInfo = json_best;// 设置ReplyActivity的Header内容

                    intent = new Intent();
                    intent.setClass(TopicActivity.this, ReplyActivity.class);
                    intent.putExtra("BLONAME",
                            json_bloinfo.getString("blo_name"));
                    TopicActivity.this.startActivity(intent);
                    break;
                case R.id.IMVloadtip:
                    // 去除提示
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
     * @author Administrator 滑动监听
     */
    private class ScrollListener implements OnScrollListener {

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {
            int lastposi = listview.getLastVisiblePosition();

			/*
             * totalItemCount和position都包括header和footer
			 * 只有在滑动到最后一个item，并且在没有全部加载完成，以及没有正在加载的情况下才继续加载下一批
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
                                handler.obtainMessage(DISPLAY_TOAST, "连接超时！")
                                        .sendToTarget();
                            } else if (mess.contains("SocketTimeoutException")) {
                                handler.obtainMessage(DISPLAY_TOAST, "读取数据超时！")
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
     * @author Administrator 取得主题帖线程
     */
    private class GetTopicThread extends Thread {
        boolean isContinue = true;// 继续加载主题帖标志位，如果加载置顶置精失败，将不会继续加载主题帖

        @Override
        public void run() {
            getHeaderThread = new GetHeaderThread();
            getHeaderThread.start();
            try {
				/*
				 * 将获得置顶置精帖线程加入，保证在获得在获得置顶置精帖之后再加载版块内的帖子
				 * 设置最大等待时间，防止因getHeaderThread线程加载时间过长导致GetTopicThread线程不再等待而去加载主题帖
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
     * @author Administrator 加载header线程
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
                getTopicThread.isContinue = false;// 设置标志位
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
     * 用于发表主题帖后回调更新
     * 从指定的activity返回时会调用此方法(根据requestCode判断返回的是哪个activity，根据resultCode处理返回的不同结果
     * ， data只有在指定activity关闭时（onDestroy()执行）才会返回一个非空的值)
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PUBLISH_TOPIC:
                if (resultCode == 1) {
                    if (data != null) {
                        // 刚刚发表的主题帖ID，据此取得主题帖信息
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
                                                    DISPLAY_TOAST, "连接超时！")
                                                    .sendToTarget();
                                        } else if (mess
                                                .contains("SocketTimeoutException")) {
                                            handler.obtainMessage(
                                                    DISPLAY_TOAST, "读取数据超时！")
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
