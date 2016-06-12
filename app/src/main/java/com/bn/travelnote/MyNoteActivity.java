package com.bn.travelnote;

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
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bn.thread.DownloadPicThread;
import com.bn.thread.ImgAsyncDownload;
import com.bn.tour.R;
import com.bn.tour.ScanPicActivity;
import com.bn.util.CheckUtil;
import com.bn.util.Constant;
import com.bn.util.NetTransUtil;

public class MyNoteActivity extends Activity {

    private static final int GET_NOTE = 0;//��ȡ�μ�
    private static final int UPDATE_PIC = 1;//����ͼƬ�ĸ������������غͷ����μ�ʱ���õ���
    private static final int UPDATE_NOTE = 2;//�����μǣ������μǺ�
    private static final int ITEMCOUTN = 5;//��������ÿ�μ��ص���Ŀ��
    private static final int LOAD_PAGE = 3;//��������
    private static final int DEL_TRANOTE = 4;//ɾ���μ�
    private ListView listview;
    private NoteAdapter noteadapter;
    private ImgAsyncDownload imgdownload;//�첽����ͼƬ�̣߳�listview��ͼƬ�ļ��أ�
    private DownloadPicThread loadThread;//�첽����ͼƬ�̣߳���listview��ͼƬ�ļ��أ�
    private JSONObject json_note, json_tratinfo;
    private JSONArray jarr_note;
    private GetNoteThread getNoteThread;
    private TextView tvPiccount;
    private ImageView imvWriteNote;
    private ScrollListener scrolllistener;//�������������ڷ������أ�
    private ClickListener clicklistener;
    private View footer;

    private LinearLayout footerLayout;
    private String tratpic;

    private boolean loading = false;
    private boolean hadTotalLoaded = false;
    private int delPosition;

    private Intent intent;

    private ArrayList<String> picpathList = new ArrayList<String>();//�������ͼƬ·����list
    private SparseIntArray sparse = new SparseIntArray();//sparse�м�¼����ͼƬ���ڵ�item��position��Ӧ��������ͼƬ�е�����

    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_note);
        handler = new MyHandler(this);
        json_tratinfo = JSONObject.fromObject(getIntent().getStringExtra(
                "TRATINFO"));
        tratpic = json_tratinfo.getString("tratop_pic");

        getNoteThread = new GetNoteThread();
        getNoteThread.start();

    }

    private static class MyHandler extends Handler {
        WeakReference<MyNoteActivity> mActivity;

        public MyHandler(MyNoteActivity activity) {
            mActivity = new WeakReference<MyNoteActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MyNoteActivity currActivity = mActivity.get();
            switch (msg.what) {
                case GET_NOTE:
                    String note = (String) msg.obj;
                    if (note == null) {
                        currActivity.jarr_note = new JSONArray();
                        currActivity.hadTotalLoaded = true;
                    } else {
                        note = CheckUtil.replaceBlank(note);
                        currActivity.json_note = JSONObject.fromObject(note);
                        currActivity.jarr_note = currActivity.json_note
                                .getJSONArray("results");
                        if (currActivity.jarr_note.size() < ITEMCOUTN) {
                            currActivity.hadTotalLoaded = true;
                        }
                    }
                    currActivity.initView();
                    break;
                case UPDATE_PIC:
                    currActivity.tvPiccount.setText("ͼƬ��"
                            + currActivity.picpathList.size() + "��");
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
                        String json1 = currActivity.jarr_note.toString();
                        String json2 = jarr_new.toString();
                        // ��װ�µ�json,����ʹ��add������ֻ��addһ��Ԫ��
                        String newjson = json1.substring(0, json1.length() - 1)
                                + "," + json2.substring(1, json2.length());
                        currActivity.jarr_note = JSONArray.fromObject(newjson);
                        // ��ʱadapter�Ѿ��Զ������ˣ���noteadapter.getCount()�������ȡ�ϴε���Ŀ�����ɹ����õ����ǵ�ǰ�Ѹ��µ���Ŀ

                        currActivity.noteadapter.notifyDataSetChanged();
                        currActivity.loadPicpathThread(
                                currActivity.noteadapter.getCount()
                                        - jarr_new.size(),
                                currActivity.jarr_note);// ����ͼƬ����
                    }

                    if (currActivity.hadTotalLoaded) {
                        TextView tv = new TextView(currActivity);
                        tv.setText("��ȫ�����أ�");
                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                LayoutParams.MATCH_PARENT,
                                LayoutParams.MATCH_PARENT);
                        tv.setGravity(Gravity.CENTER);
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
                case UPDATE_NOTE:
                    String newnote = (String) msg.obj;
                    if (newnote != null) {
                        currActivity.jarr_note.add(0, newnote);
                        currActivity.noteadapter.notifyDataSetChanged();
                        currActivity.sparse.clear();
                        currActivity.picpathList.clear();
                        currActivity.loadPicpathThread(0,
                                currActivity.jarr_note);// ����ͼƬ
                        currActivity.listview.setSelection(0);
                    }
                    break;
                case DEL_TRANOTE:
                    if ((Boolean) msg.obj) {
                        currActivity.jarr_note.remove(currActivity.delPosition);
                        currActivity.noteadapter.notifyDataSetChanged();
                        Toast.makeText(currActivity, "�μ���ɾ����",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(currActivity, "ɾ��ʧ�ܣ�",
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    }

    //��ʼ������
    private void initView() {
        listview = (ListView) this.findViewById(R.id.LVmy_note);
        imvWriteNote = (ImageView) this.findViewById(R.id.IMVmy_note_writenote);
        clicklistener = new ClickListener();// һ��Ҫ��new�����ڼӼ���

        this.addHeader();
        this.loadPicpathThread(0, jarr_note);

        footer = getLayoutInflater().inflate(R.layout.footer, null);
        footerLayout = (LinearLayout) footer.findViewById(R.id.LLfooter);

        noteadapter = new NoteAdapter();
        listview.setAdapter(noteadapter);
        // �����������������أ�
        scrolllistener = new ScrollListener();
        listview.setOnScrollListener(scrolllistener);

        imvWriteNote.setOnClickListener(clicklistener);
    }

    //Ϊlistview���header
    public void addHeader() {
        // ���Header��View
        View view = getLayoutInflater().inflate(R.layout.header_my_note, null);

        ImageView imvHeadPic = (ImageView) view
                .findViewById(R.id.IMVheader_mynote_trat_pic);

        TextView tvBrowcount = (TextView) view
                .findViewById(R.id.TVheader_mynote_trat_browcount);
        TextView tvZancount = (TextView) view
                .findViewById(R.id.TVheader_mynote_trat_zancount);
        TextView tvTime = (TextView) view
                .findViewById(R.id.TVheader_mynote_trat_sendtime);
        TextView tvTratname = (TextView) view
                .findViewById(R.id.TVheader_mynote_tratop_name);
        TextView tvTratplace = (TextView) view
                .findViewById(R.id.TVheader_mynote_trat_place);
        tvPiccount = (TextView) view
                .findViewById(R.id.TVheader_mynote_pic_count);

        // ����ͼƬ
        if (!tratpic.equals("null")) {
            loadThread = new DownloadPicThread(tratpic, imvHeadPic, handler);
            loadThread.start();
        }

        tvBrowcount.setText(json_tratinfo.getString("tratop_browcount")
                + "���Ķ�   /");

        tvZancount.setText(json_tratinfo.getString("tratop_zancount"));
        tvTime.setText(json_tratinfo.getString("tratop_createdate").substring(
                0, 10));
        tvTratname.setText(json_tratinfo.getString("tratop_name"));
        tvTratplace.setText(json_tratinfo.getString("tratop_place"));
        /*
		 * ��Ӽ���
		 */
        tvPiccount.setOnClickListener(clicklistener);

        // ��ListView���Header
        listview.addHeaderView(view, null, false);
    }

    /**
     * ���Ż�������̬����������ͼƬ����Ŀ�������е�ͼƬURL�ŵ�picpathList�У����Ԥ��ʱ��Ϊ��������ȥ
     * sparse�м�¼����ͼƬ���ڵ�item��position��Ӧ��������ͼƬ�е�����
     *
     * @param position��ʼɨ���λ�� ����Ӧitem��position��
     * @param jarr            ���˴�ɨ�������Դ��
     */
    public void loadPicpathThread(final int position, final JSONArray jarr) {
        new Thread() {
            public void run() {
                for (int i = position; i < jarr.size(); i++) {
                    if (!jarr.getJSONObject(i).getString("note_pic")
                            .equals("null")) {
                        picpathList.add(jarr.getJSONObject(i).getString(
                                "note_pic"));
                        sparse.put(i, picpathList.size() - 1);// ��ͼƬ�������򣨸���position��
                    }
                }

                handler.sendEmptyMessage(UPDATE_PIC);

            }
        }.start();
    }

    // ListView��BaseAdapter������
    private class NoteAdapter extends BaseAdapter {
        private ViewHolder viewholder;

        @Override
        public int getCount() {
            return jarr_note.size();
        }

        @Override
        public Object getItem(int position) {
            return jarr_note.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView,
                            ViewGroup parent) {
            final JSONObject json_temp = jarr_note.getJSONObject(position);// ȡ��ÿ��item��json����

            // ����View
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.item_note,
                        null);
                viewholder = new ViewHolder();

                viewholder.imvPic = (ImageView) convertView
                        .findViewById(R.id.IMVnote_pic);
                viewholder.imvDel = (ImageView) convertView
                        .findViewById(R.id.IMVnote_del);
                viewholder.tvCreateTime = (TextView) convertView
                        .findViewById(R.id.TVnote_time);

                viewholder.tvContent = (TextView) convertView
                        .findViewById(R.id.TVnote_content);
                viewholder.tvPlace = (TextView) convertView
                        .findViewById(R.id.TVnote_place);

                convertView.setTag(viewholder);// ���ñ�ǩ
            } else {
                viewholder = (ViewHolder) convertView.getTag();// ���ݱ�ǩ�õ���ͼ
            }

            viewholder.imvDel.setVisibility(View.VISIBLE);

            viewholder.tvCreateTime.setText(json_temp.getString("note_date")
                    .substring(0, 10));// time
            viewholder.tvPlace.setText(json_temp.getString("note_place"));// place
            viewholder.tvContent.setText(json_temp.getString("note_content"));// content

            String picUrl = json_temp.getString("note_pic");
            if (!picUrl.equals("null")) {
                viewholder.imvPic.setVisibility(View.VISIBLE);// ��ImageView��Ϊ�ɼ�

                imgdownload = ImgAsyncDownload.getInstance();

                if (imgdownload != null) {
                    // �첽����ͼƬ
                    // ImageView����tag,ͼƬ�첽������ɺ󣬸���tag�ҵ���ӦImageView����ͼƬ����ֹ����
                    viewholder.imvPic.setTag(picUrl);
                    viewholder.imvPic
                            .setImageResource(R.drawable.default_picture);// һ��Ҫ������Ĭ��ͼƬ��ȥ���أ����򻹻���ִ���
                    imgdownload.imageDownload(viewholder.imvPic);
                }

                viewholder.imvPic.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        intent = new Intent(MyNoteActivity.this, ScanPicActivity.class);
                        intent.putExtra("POSITION", sparse.get(position));
                        intent.putStringArrayListExtra("PATHLIST", picpathList);
                        startActivity(intent);
                    }
                });
            } else {
                viewholder.imvPic.setVisibility(View.GONE);
            }

            viewholder.imvDel.setOnClickListener(new OnClickListener() {
                boolean isChangePic = false;// �Ƿ���Ҫ�ı������ͼƬ

                @Override
                public void onClick(View v) {
                    delPosition = position;
                    if (json_temp.getString("note_pic").equals("null")) {
                        del();
                    } else {
                        if (json_temp.getString("note_pic").equals(tratpic)) {
                            isChangePic = true;
                            del();
                        } else {
                            del();
                        }
                    }
                }

                public void del() {
                    new Thread() {

                        @Override
                        public void run() {
                            try {
                                boolean flag = NetTransUtil.delTravelNote(
                                        json_temp.getString("note_id"),
                                        isChangePic, Constant.currTratopId);
                                handler.obtainMessage(DEL_TRANOTE, flag)
                                        .sendToTarget();
                            } catch (Exception e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }

                        }

                    }.start();
                }
            });

            return convertView;
        }

        private class ViewHolder {
            ImageView imvPic, imvDel;
            TextView tvCreateTime, tvContent, tvPlace;
        }

    }

    private class ScrollListener implements OnScrollListener {

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {
            int lastposi = listview.getLastVisiblePosition();

            Log.d("position", "onScroll(first=" + firstVisibleItem
                    + ",visibleItemCount=" + visibleItemCount + ",total="
                    + totalItemCount + "==lastposi====" + lastposi + ")");
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
                                    .getNfNoteOfTrat(
                                            Constant.currTratopId,
                                            "false",
                                            jarr_note.getJSONObject(
                                                    jarr_note.size() - 1)
                                                    .getString("note_id"));
                            handler.obtainMessage(LOAD_PAGE, json_new_topic)
                                    .sendToTarget();
                        } catch (Exception e) {
                            String mess = e.getClass().getName();
                            if (mess.contains("ConnectTimeoutException")) {
                                handler.obtainMessage(
                                        Constant.CONNECTIONTIMEOUT)
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

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
        }

    }

    // OnClickListener���������
    private class ClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.TVheader_mynote_pic_count:
                    if (picpathList.size() != 0) {
                        intent = new Intent(MyNoteActivity.this, ScanPicActivity.class);
                        intent.putExtra("POSITION", 0);
                        intent.putStringArrayListExtra("PATHLIST", picpathList);
                        startActivity(intent);
                    }
                    break;
                case R.id.IMVmy_note_writenote:
                    intent = new Intent(MyNoteActivity.this, WriteNoteActivity.class);
                    intent.putExtra("TRATID",
                            json_tratinfo.getString("tratop_id"));
                    System.out.println("====="
                            + json_tratinfo.getString("tratop_id"));
                    intent.putExtra("TRATPIC", tratpic);
                    startActivityForResult(intent, 0);
                    break;

            }

        }
    }

    private class GetNoteThread extends Thread {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            try {
                String note = NetTransUtil.getNfNoteOfTrat(
                        Constant.currTratopId, "false", 0 + "");
                handler.obtainMessage(GET_NOTE, note).sendToTarget();
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

    //ִ����startActivityForResult���غ���� �ķ���
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 0:
                if (resultCode == 1) {
                    if (data != null) {
                        // �ոշ����������ID���ݴ�ȡ����������Ϣ
                        final String noteid = data.getStringExtra("NOTEID");
                        if (noteid != null) {
                            new Thread() {

                                @Override
                                public void run() {
                                    try {
                                        String noteinfo = NetTransUtil
                                                .getNoteById(noteid);
                                        handler.obtainMessage(UPDATE_NOTE,
                                                noteinfo).sendToTarget();
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

                    }

                }
                break;
        }

    }

}
