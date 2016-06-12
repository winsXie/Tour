package com.bn.util;

import java.io.File;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.io.IOUtils;

public class NetTransUtil {
    // 获取广告
    public static String getPush(String bool) throws Exception {
        String mess = null;
        HttpClient client = new HttpClient();
        client.getHttpConnectionManager().getParams()
                .setConnectionTimeout(5 * 1000);
        client.getHttpConnectionManager().getParams().setSoTimeout(10 * 1000);
        PostMethod post = new PostMethod("http://" + Constant.severIp
                + ":8080/tourServer/tour");
        post.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,
                "UTF-8");
        post.setRequestHeader("CMD", Constant.GET_PUSH);
        post.addParameter("bool", bool);

        try {
            client.executeMethod(post);
            String str = new String(IOUtils.toByteArray(post
                    .getResponseBodyAsStream()), "utf-8");
            if (!str.equals("null")) {
                mess = str;
            }
        } catch (ConnectTimeoutException e) {
            throw e;
        } catch (SocketTimeoutException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            post.releaseConnection();
            ((SimpleHttpConnectionManager) client.getHttpConnectionManager())
                    .shutdown();
        }
        return mess;
    }

    // 获得推荐的主题帖
    public static String getRecoTopic(int page) throws Exception {
        String mess = null;

        HttpClient client = new HttpClient();
        client.getHttpConnectionManager().getParams()
                .setConnectionTimeout(5 * 1000);
        client.getHttpConnectionManager().getParams().setSoTimeout(10 * 1000);

        PostMethod post = new PostMethod("http://" + Constant.severIp
                + ":8080/tourServer/tour");
        post.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,
                "UTF-8");
        post.setRequestHeader("CMD", Constant.GET_RECOTOPIC);
        post.addParameter("page", page + "");

        try {
            client.executeMethod(post);
            String str = new String(IOUtils.toByteArray(post
                    .getResponseBodyAsStream()), "utf-8");
            if (!str.equals("null")) {
                mess = str;
            }
        } catch (ConnectTimeoutException e) {
            throw e;
        } catch (SocketTimeoutException e) {
            throw e;
        } catch (ConnectException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            post.releaseConnection();
            ((SimpleHttpConnectionManager) client.getHttpConnectionManager())
                    .shutdown();
        }
        return mess;
    }

    // 根据发布时间及用户等级获得3条计划（发现---结伴同游）
    public static String getTogetherPlan() throws Exception {
        String mess = null;
        HttpClient client = new HttpClient();
        client.getHttpConnectionManager().getParams()
                .setConnectionTimeout(5 * 1000);
        client.getHttpConnectionManager().getParams().setSoTimeout(10 * 1000);
        PostMethod post = new PostMethod("http://" + Constant.severIp
                + ":8080/tourServer/tour");
        post.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,
                "UTF-8");
        post.setRequestHeader("CMD", Constant.GET_TOGEPLAN);

        try {
            client.executeMethod(post);
            String str = new String(IOUtils.toByteArray(post
                    .getResponseBodyAsStream()), "utf-8");
            if (!str.equals("null")) {
                mess = str;
            }
        } catch (ConnectTimeoutException e) {
            throw e;
        } catch (SocketTimeoutException e) {
            throw e;
        } catch (ConnectException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            post.releaseConnection();
            ((SimpleHttpConnectionManager) client.getHttpConnectionManager())
                    .shutdown();
        }
        return mess;
    }

    // 获取推荐的游记主题
    public static String getTraTopic(int page) throws Exception {
        String mess = null;
        HttpClient client = new HttpClient();
        client.getHttpConnectionManager().getParams()
                .setConnectionTimeout(5 * 1000);
        client.getHttpConnectionManager().getParams().setSoTimeout(10 * 1000);
        PostMethod post = new PostMethod("http://" + Constant.severIp
                + ":8080/tourServer/tour");
        post.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,
                "UTF-8");
        post.setRequestHeader("CMD", Constant.GET_RECOTRATOP);
        post.addParameter("page", page + "");

        try {
            client.executeMethod(post);
            String str = new String(IOUtils.toByteArray(post
                    .getResponseBodyAsStream()), "utf-8");
            if (!str.equals("null")) {
                mess = str;
            }
        } catch (ConnectTimeoutException e) {
            throw e;
        } catch (SocketTimeoutException e) {
            throw e;
        } catch (ConnectException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            post.releaseConnection();
            ((SimpleHttpConnectionManager) client.getHttpConnectionManager())
                    .shutdown();
        }
        return mess;
    }

    // 获得所有正常或禁用的版块（共用）
    public static String getNfBlockInfo(int page, String bool) throws Exception {
        String mess = null;
        HttpClient client = new HttpClient();
        client.getHttpConnectionManager().getParams()
                .setConnectionTimeout(5 * 1000);
        client.getHttpConnectionManager().getParams().setSoTimeout(10 * 1000);
        PostMethod post = new PostMethod("http://" + Constant.severIp
                + ":8080/tourServer/tour");
        post.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,
                "UTF-8");
        post.setRequestHeader("CMD", Constant.GET_NFBLOCKINFO);
        post.addParameter("page", page + "");
        post.addParameter("bool", bool);

        try {
            client.executeMethod(post);
            String str = new String(IOUtils.toByteArray(post
                    .getResponseBodyAsStream()), "utf-8");
            if (!str.equals("null")) {
                mess = str;
            }
        } catch (ConnectTimeoutException e) {
            throw e;
        } catch (SocketTimeoutException e) {
            throw e;
        } catch (ConnectException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            post.releaseConnection();
            ((SimpleHttpConnectionManager) client.getHttpConnectionManager())
                    .shutdown();
        }
        return mess;
    }

    // 根据版块名称查询所有的正常或禁用的用户的版块信息（共用）
    public static String getNfBloinfoByName(String name, int page, String bool)
            throws Exception {
        String mess = null;
        HttpClient client = new HttpClient();
        client.getHttpConnectionManager().getParams()
                .setConnectionTimeout(5 * 1000);
        client.getHttpConnectionManager().getParams().setSoTimeout(10 * 1000);
        PostMethod post = new PostMethod("http://" + Constant.severIp
                + ":8080/tourServer/tour");
        post.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,
                "UTF-8");
        post.setRequestHeader("CMD", Constant.QUERY_BLOBYNAME);
        post.addParameter("name", name);
        post.addParameter("page", page + "");
        post.addParameter("bool", bool);

        try {
            client.executeMethod(post);
            String str = new String(IOUtils.toByteArray(post
                    .getResponseBodyAsStream()), "utf-8");
            if (!str.equals("null")) {
                mess = str;
            }
        } catch (ConnectTimeoutException e) {
            throw e;
        } catch (SocketTimeoutException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            post.releaseConnection();
            ((SimpleHttpConnectionManager) client.getHttpConnectionManager())
                    .shutdown();
        }
        return mess;
    }

    // 获得一个版块内的置顶帖（共用）
    public static String getTopTopic(String bloid) throws Exception {
        String mess = null;
        HttpClient client = new HttpClient();
        client.getHttpConnectionManager().getParams()
                .setConnectionTimeout(5 * 1000);
        client.getHttpConnectionManager().getParams().setSoTimeout(10 * 1000);
        PostMethod post = new PostMethod("http://" + Constant.severIp
                + ":8080/tourServer/tour");
        post.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,
                "UTF-8");
        post.setRequestHeader("CMD", Constant.GET_TOPTOPIC);
        post.addParameter("bloid", bloid);

        try {
            client.executeMethod(post);
            String str = new String(IOUtils.toByteArray(post
                    .getResponseBodyAsStream()), "utf-8");
            if (!str.equals("null")) {
                mess = str;
            }
        } catch (ConnectTimeoutException e) {
            throw e;
        } catch (SocketTimeoutException e) {
            throw e;
        } catch (ConnectException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            post.releaseConnection();
            ((SimpleHttpConnectionManager) client.getHttpConnectionManager())
                    .shutdown();
        }
        return mess;
    }

    // 获得一个版块内的置精帖（共用）
    public static String getBestTopic(String bloid) throws Exception {
        String mess = null;
        HttpClient client = new HttpClient();
        client.getHttpConnectionManager().getParams()
                .setConnectionTimeout(5 * 1000);
        client.getHttpConnectionManager().getParams().setSoTimeout(10 * 1000);
        PostMethod post = new PostMethod("http://" + Constant.severIp
                + ":8080/tourServer/tour");
        post.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,
                "UTF-8");
        post.setRequestHeader("CMD", Constant.GET_BESTTOPIC);
        post.addParameter("bloid", bloid);

        try {
            client.executeMethod(post);
            String str = new String(IOUtils.toByteArray(post
                    .getResponseBodyAsStream()), "utf-8");
            if (!str.equals("null")) {
                mess = str;
            }
        } catch (ConnectTimeoutException e) {
            throw e;
        } catch (SocketTimeoutException e) {
            throw e;
        } catch (ConnectException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            post.releaseConnection();
            ((SimpleHttpConnectionManager) client.getHttpConnectionManager())
                    .shutdown();
        }
        return mess;
    }

    // 获得一个版块内的所有正常或禁用的主题帖（共用）
    public static String getNfTopicOfBlo(String bloid, String bool,
                                         String lastid) throws Exception {
        String mess = null;
        HttpClient client = new HttpClient();
        client.getHttpConnectionManager().getParams()
                .setConnectionTimeout(5 * 1000);
        client.getHttpConnectionManager().getParams().setSoTimeout(10 * 1000);
        PostMethod post = new PostMethod("http://" + Constant.severIp
                + ":8080/tourServer/tour");
        post.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,
                "UTF-8");
        post.setRequestHeader("CMD", Constant.GET_NFTOPICOFBLOCK);
        post.addParameter("bloid", bloid);
        post.addParameter("bool", bool);
        post.addParameter("lastid", lastid);

        try {
            client.executeMethod(post);
            String str = new String(IOUtils.toByteArray(post
                    .getResponseBodyAsStream()), "utf-8");
            if (!str.equals("null")) {
                mess = str;
            }
        } catch (ConnectTimeoutException e) {
            throw e;
        } catch (SocketTimeoutException e) {
            throw e;
        } catch (ConnectException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            post.releaseConnection();
            ((SimpleHttpConnectionManager) client.getHttpConnectionManager())
                    .shutdown();
        }
        return mess;
    }

    // 发表主题帖
    public static String publishTopic(Map<String, String> topicinfo)
            throws Exception {
        String topicid = null;
        HttpClient client = new HttpClient();
        client.getHttpConnectionManager().getParams()
                .setConnectionTimeout(5 * 1000);
        client.getHttpConnectionManager().getParams().setSoTimeout(10 * 1000);
        PostMethod post = new PostMethod("http://" + Constant.severIp
                + ":8080/tourServer/tour");
        post.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,
                "UTF-8");
        post.setRequestHeader("CMD", Constant.PUBLISH_TOPIC);

        Part[] parts;
        int picCount = BitmapUtil.pathlist.size();
        int paraCount = topicinfo.size();

        Set<String> keyset = topicinfo.keySet();
        Iterator<String> iteor = keyset.iterator();

        if (picCount == 0) {
            parts = new Part[paraCount];
        } else {
            parts = new Part[picCount + paraCount];

            for (int i = 0; i < picCount; i++) {
                File file = new File(BitmapUtil.templist.get(i));
                FilePart filepart = new FilePart(file.getName(), file);
                parts[i] = filepart;
            }
        }

        for (int j = 0; iteor.hasNext(); j++) {
            String param = iteor.next();
            parts[picCount + j] = new StringPart(param, topicinfo.get(param),
                    "UTF-8");
        }

        post.setRequestEntity(new MultipartRequestEntity(parts, post
                .getParams()));

        try {
            client.executeMethod(post);
            String str = new String(IOUtils.toByteArray(post
                    .getResponseBodyAsStream()), "utf-8");
            if (!str.equals("null")) {
                topicid = str;
            }
        } catch (ConnectTimeoutException e) {
            throw e;
        } catch (SocketTimeoutException e) {
            throw e;
        } catch (ConnectException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            post.releaseConnection();
            ((SimpleHttpConnectionManager) client.getHttpConnectionManager())
                    .shutdown();
        }

        return topicid;
    }

    // 根据主题帖ID查询信息
    public static String getTopicById(String topicid) throws Exception {
        String mess = null;
        HttpClient client = new HttpClient();
        client.getHttpConnectionManager().getParams()
                .setConnectionTimeout(5 * 1000);
        client.getHttpConnectionManager().getParams().setSoTimeout(10 * 1000);
        PostMethod post = new PostMethod("http://" + Constant.severIp
                + ":8080/tourServer/tour");
        post.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,
                "UTF-8");
        post.setRequestHeader("CMD", Constant.QUERY_TOPICBYID);
        post.addParameter("topicid", topicid);

        try {
            client.executeMethod(post);
            String str = new String(IOUtils.toByteArray(post
                    .getResponseBodyAsStream()), "utf-8");
            if (!str.equals("null")) {
                mess = str;
            }
        } catch (ConnectTimeoutException e) {
            throw e;
        } catch (SocketTimeoutException e) {
            throw e;
        } catch (ConnectException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            post.releaseConnection();
            ((SimpleHttpConnectionManager) client.getHttpConnectionManager())
                    .shutdown();
        }
        return mess;
    }

    // 获得一个主题帖的所有正常或禁用的回复帖（共用）
    public static String getNfRepOfTopic(String topid, String bool, String floor)
            throws Exception {
        String mess = null;
        HttpClient client = new HttpClient();
        client.getHttpConnectionManager().getParams()
                .setConnectionTimeout(5 * 1000);
        client.getHttpConnectionManager().getParams().setSoTimeout(10 * 1000);
        PostMethod post = new PostMethod("http://" + Constant.severIp
                + ":8080/tourServer/tour");
        post.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,
                "UTF-8");
        post.setRequestHeader("CMD", Constant.GET_NFREPTOPIC);
        post.addParameter("topicid", topid);
        post.addParameter("bool", bool);
        post.addParameter("floor", floor);

        try {
            client.executeMethod(post);
            String str = new String(IOUtils.toByteArray(post
                    .getResponseBodyAsStream()), "utf-8");
            if (!str.equals("null")) {
                mess = str;
            }
        } catch (ConnectTimeoutException e) {
            throw e;
        } catch (SocketTimeoutException e) {
            throw e;
        } catch (ConnectException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            post.releaseConnection();
            ((SimpleHttpConnectionManager) client.getHttpConnectionManager())
                    .shutdown();
        }
        return mess;
    }

    // 发表回复帖
    public static String publishRepTopic(Map<String, String> repinfo)
            throws Exception {
        String repid = null;
        HttpClient client = new HttpClient();
        client.getHttpConnectionManager().getParams()
                .setConnectionTimeout(5 * 1000);
        client.getHttpConnectionManager().getParams().setSoTimeout(10 * 1000);
        PostMethod post = new PostMethod("http://" + Constant.severIp
                + ":8080/tourServer/tour");
        post.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,
                "UTF-8");
        post.setRequestHeader("CMD", Constant.PUBLISH_REPLY);

        Part[] parts;
        int picCount = BitmapUtil.pathlist.size();
        int paraCount = repinfo.size();

        Set<String> keyset = repinfo.keySet();
        Iterator<String> iteor = keyset.iterator();

        if (picCount == 0) {
            parts = new Part[paraCount];
        } else {
            parts = new Part[picCount + paraCount];

            for (int i = 0; i < picCount; i++) {
                File file = new File(BitmapUtil.templist.get(i));
                FilePart filepart = new FilePart(file.getName(), file);
                parts[i] = filepart;
            }
        }

        for (int j = 0; iteor.hasNext(); j++) {
            String param = iteor.next();
            parts[picCount + j] = new StringPart(param, repinfo.get(param),
                    "UTF-8");
        }

        post.setRequestEntity(new MultipartRequestEntity(parts, post
                .getParams()));

        try {
            client.executeMethod(post);
            String str = new String(IOUtils.toByteArray(post
                    .getResponseBodyAsStream()), "utf-8");
            if (!str.equals("null")) {
                repid = str;
            }
        } catch (ConnectTimeoutException e) {
            throw e;
        } catch (SocketTimeoutException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            post.releaseConnection();
            ((SimpleHttpConnectionManager) client.getHttpConnectionManager())
                    .shutdown();
        }

        return repid;
    }

    // 根据回复帖ID查询信息
    public static String getRepById(String repid) throws Exception {
        String mess = null;
        HttpClient client = new HttpClient();
        client.getHttpConnectionManager().getParams()
                .setConnectionTimeout(5 * 1000);
        client.getHttpConnectionManager().getParams().setSoTimeout(10 * 1000);
        PostMethod post = new PostMethod("http://" + Constant.severIp
                + ":8080/tourServer/tour");
        post.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,
                "UTF-8");
        post.setRequestHeader("CMD", Constant.QUERY_REPBYID);
        post.addParameter("repid", repid);

        try {
            client.executeMethod(post);
            String str = new String(IOUtils.toByteArray(post
                    .getResponseBodyAsStream()), "utf-8");
            if (!str.equals("null")) {
                mess = str;
            }
        } catch (ConnectTimeoutException e) {
            throw e;
        } catch (SocketTimeoutException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            post.releaseConnection();
            ((SimpleHttpConnectionManager) client.getHttpConnectionManager())
                    .shutdown();
        }
        return mess;
    }

    // 根据计划ID查询相应计划的详细信息(Android)
    public static String getPlanById(String planid) throws Exception {
        String mess = null;
        HttpClient client = new HttpClient();
        client.getHttpConnectionManager().getParams()
                .setConnectionTimeout(5 * 1000);
        client.getHttpConnectionManager().getParams().setSoTimeout(10 * 1000);
        PostMethod post = new PostMethod("http://" + Constant.severIp
                + ":8080/tourServer/tour");
        post.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,
                "UTF-8");
        post.setRequestHeader("CMD", Constant.QUERY_PLANBYID);
        post.addParameter("planid", planid);

        try {
            client.executeMethod(post);
            String str = new String(IOUtils.toByteArray(post
                    .getResponseBodyAsStream()), "utf-8");
            if (!str.equals("null")) {
                mess = str;
            }
        } catch (ConnectTimeoutException e) {
            throw e;
        } catch (SocketTimeoutException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            post.releaseConnection();
            ((SimpleHttpConnectionManager) client.getHttpConnectionManager())
                    .shutdown();
        }
        return mess;
    }

    // 修改计划
    public static boolean editPlan(Map<String, String> planinfo)
            throws Exception {
        boolean flag = false;
        HttpClient client = new HttpClient();
        client.getHttpConnectionManager().getParams()
                .setConnectionTimeout(5 * 1000);
        client.getHttpConnectionManager().getParams().setSoTimeout(10 * 1000);
        PostMethod post = new PostMethod("http://" + Constant.severIp
                + ":8080/tourServer/tour");
        post.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,
                "UTF-8");
        post.setRequestHeader("CMD", Constant.EDIT_PLAN);

        Set<String> keyset = planinfo.keySet();
        Iterator<String> iteor = keyset.iterator();

        while (iteor.hasNext()) {
            String param = iteor.next();
            post.addParameter(param, planinfo.get(param));
        }

        try {
            client.executeMethod(post);
            String str = new String(IOUtils.toByteArray(post
                    .getResponseBodyAsStream()), "utf-8");
            if (str.equals("ok")) {
                flag = true;
            }
        } catch (ConnectTimeoutException e) {
            throw e;
        } catch (SocketTimeoutException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            post.releaseConnection();
            ((SimpleHttpConnectionManager) client.getHttpConnectionManager())
                    .shutdown();
        }
        return flag;
    }

    // 获得一个游记主题内的所有正常或禁用的游记（共用）(Android不提供对游记点赞功能，可对相应的游记主题点赞)
    public static String getNfNoteOfTrat(String tratid, String bool,
                                         String lastid) throws Exception {
        String mess = null;
        HttpClient client = new HttpClient();
        client.getHttpConnectionManager().getParams()
                .setConnectionTimeout(5 * 1000);
        client.getHttpConnectionManager().getParams().setSoTimeout(10 * 1000);
        PostMethod post = new PostMethod("http://" + Constant.severIp
                + ":8080/tourServer/tour");
        post.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,
                "UTF-8");
        post.setRequestHeader("CMD", Constant.GET_NOTEOFTRAT);
        post.addParameter("tratid", tratid);
        post.addParameter("bool", bool);
        post.addParameter("lastid", lastid);

        try {
            client.executeMethod(post);
            String str = new String(IOUtils.toByteArray(post
                    .getResponseBodyAsStream()), "utf-8");
            if (!str.equals("null")) {
                mess = str;
            }
        } catch (ConnectTimeoutException e) {
            throw e;
        } catch (SocketTimeoutException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            post.releaseConnection();
            ((SimpleHttpConnectionManager) client.getHttpConnectionManager())
                    .shutdown();
        }
        return mess;
    }

    // 登录
    public static String login(String uid) throws Exception {
        String mess = null;
        HttpClient client = new HttpClient();
        client.getHttpConnectionManager().getParams()
                .setConnectionTimeout(5 * 1000);
        client.getHttpConnectionManager().getParams().setSoTimeout(10 * 1000);
        PostMethod post = new PostMethod("http://" + Constant.severIp
                + ":8080/tourServer/tour");
        post.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,
                "UTF-8");
        post.setRequestHeader("CMD", Constant.LOGIN);
        post.addParameter("uid", uid);

        try {
            client.executeMethod(post);
            String str = new String(IOUtils.toByteArray(post
                    .getResponseBodyAsStream()), "utf-8");
            if (!str.equals("null")) {
                mess = str;
            }
        } catch (ConnectTimeoutException e) {
            throw e;
        } catch (SocketTimeoutException e) {
            throw e;
        } catch (ConnectException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            post.releaseConnection();
            ((SimpleHttpConnectionManager) client.getHttpConnectionManager())
                    .shutdown();
        }
        return mess;
    }

    // 获得个人基本资料(Android)
    public static String getBasedInfo(String uid) throws Exception {
        String mess = null;
        HttpClient client = new HttpClient();
        client.getHttpConnectionManager().getParams()
                .setConnectionTimeout(5 * 1000);
        client.getHttpConnectionManager().getParams().setSoTimeout(10 * 1000);
        PostMethod post = new PostMethod("http://" + Constant.severIp
                + ":8080/tourServer/tour");
        post.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,
                "UTF-8");
        post.setRequestHeader("CMD", Constant.GET_BASEINFO);
        post.addParameter("uid", uid);

        try {
            client.executeMethod(post);
            String str = new String(IOUtils.toByteArray(post
                    .getResponseBodyAsStream()), "utf-8");
            if (!str.equals("null")) {
                mess = str;
            }
        } catch (ConnectTimeoutException e) {
            throw e;
        } catch (SocketTimeoutException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            post.releaseConnection();
            ((SimpleHttpConnectionManager) client.getHttpConnectionManager())
                    .shutdown();
        }
        return mess;
    }

    // 注册新账号(Android)
    public static String register(Map<String, String> userinfo)
            throws Exception {
        String mess = null;
        HttpClient client = new HttpClient();
        client.getHttpConnectionManager().getParams()
                .setConnectionTimeout(5 * 1000);
        client.getHttpConnectionManager().getParams().setSoTimeout(10 * 1000);
        PostMethod post = new PostMethod("http://" + Constant.severIp
                + ":8080/tourServer/tour");
        post.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,
                "UTF-8");
        post.setRequestHeader("CMD", Constant.REGISTER);
        Set<String> keyset = userinfo.keySet();
        Iterator<String> iteor = keyset.iterator();

        while (iteor.hasNext()) {
            String param = iteor.next();
            post.addParameter(param, userinfo.get(param));
        }

        try {
            client.executeMethod(post);
            String str = new String(IOUtils.toByteArray(post
                    .getResponseBodyAsStream()), "utf-8");
            if (!str.equals("fail")) {
                mess = str;
            }
        } catch (ConnectTimeoutException e) {
            throw e;
        } catch (SocketTimeoutException e) {
            throw e;
        } catch (ConnectException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            post.releaseConnection();
            ((SimpleHttpConnectionManager) client.getHttpConnectionManager())
                    .shutdown();
        }
        return mess;
    }

    // 设置用户头像
    public static String changeHeadimg(String uid) throws Exception {
        String repid = null;
        HttpClient client = new HttpClient();
        client.getHttpConnectionManager().getParams()
                .setConnectionTimeout(5 * 1000);
        client.getHttpConnectionManager().getParams().setSoTimeout(10 * 1000);
        PostMethod post = new PostMethod("http://" + Constant.severIp
                + ":8080/tourServer/tour");
        post.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,
                "UTF-8");
        post.setRequestHeader("CMD", Constant.CHANGE_HEADIMG);

        File file = new File(BitmapUtil.templist.get(0));

        Part[] parts = {new FilePart(file.getName(), file),
                new StringPart("uid", uid, "UTF-8")};

        post.setRequestEntity(new MultipartRequestEntity(parts, post
                .getParams()));

        try {
            client.executeMethod(post);
            String str = new String(IOUtils.toByteArray(post
                    .getResponseBodyAsStream()), "utf-8");
            if (!str.equals("null")) {
                repid = str;
            }
        } catch (ConnectTimeoutException e) {
            throw e;
        } catch (SocketTimeoutException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            post.releaseConnection();
            ((SimpleHttpConnectionManager) client.getHttpConnectionManager())
                    .shutdown();
        }

        return repid;
    }

    // 设置签名
    public static String setQianMing(String uid, String qianm) throws Exception {
        String mess = null;
        HttpClient client = new HttpClient();
        client.getHttpConnectionManager().getParams()
                .setConnectionTimeout(5 * 1000);
        client.getHttpConnectionManager().getParams().setSoTimeout(10 * 1000);
        PostMethod post = new PostMethod("http://" + Constant.severIp
                + ":8080/tourServer/tour");
        post.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,
                "UTF-8");
        post.setRequestHeader("CMD", Constant.SETQIANMING);
        post.addParameter("uid", uid);
        post.addParameter("qianm", qianm);

        try {
            client.executeMethod(post);
            String str = new String(IOUtils.toByteArray(post
                    .getResponseBodyAsStream()), "utf-8");
            if (str.equals("ok")) {
                mess = str;
            }
        } catch (ConnectTimeoutException e) {
            throw e;
        } catch (SocketTimeoutException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            post.releaseConnection();
            ((SimpleHttpConnectionManager) client.getHttpConnectionManager())
                    .shutdown();
        }
        return mess;
    }

    // 更改密码
    public static String changePass(String uid, String oldpass, String newpass)
            throws Exception {
        String mess = null;
        HttpClient client = new HttpClient();
        client.getHttpConnectionManager().getParams()
                .setConnectionTimeout(5 * 1000);
        client.getHttpConnectionManager().getParams().setSoTimeout(10 * 1000);
        PostMethod post = new PostMethod("http://" + Constant.severIp
                + ":8080/tourServer/tour");
        post.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,
                "UTF-8");
        post.setRequestHeader("CMD", Constant.CHANGE_PASSWORD);
        post.addParameter("uid", uid);
        post.addParameter("oldpass", oldpass);
        post.addParameter("newpass", newpass);

        try {
            client.executeMethod(post);
            String str = new String(IOUtils.toByteArray(post
                    .getResponseBodyAsStream()), "utf-8");
            if (!str.equals("null")) {
                mess = str;
            }
        } catch (ConnectTimeoutException e) {
            throw e;
        } catch (SocketTimeoutException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            post.releaseConnection();
            ((SimpleHttpConnectionManager) client.getHttpConnectionManager())
                    .shutdown();
        }
        return mess;
    }

    // 编辑个人基本资料
    public static String editBasedInfo(Map<String, String> userinfo)
            throws Exception {
        String info = null;
        HttpClient client = new HttpClient();
        client.getHttpConnectionManager().getParams()
                .setConnectionTimeout(5 * 1000);
        client.getHttpConnectionManager().getParams().setSoTimeout(10 * 1000);
        PostMethod post = new PostMethod("http://" + Constant.severIp
                + ":8080/tourServer/tour");
        post.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,
                "UTF-8");
        post.setRequestHeader("CMD", Constant.EDIT_BASEDINFO);
        Set<String> keyset = userinfo.keySet();
        Iterator<String> iteor = keyset.iterator();

        while (iteor.hasNext()) {
            String param = iteor.next();
            post.addParameter(param, userinfo.get(param));
        }

        try {
            client.executeMethod(post);
            String str = new String(IOUtils.toByteArray(post
                    .getResponseBodyAsStream()), "utf-8");
            if (!str.equals("fail")) {
                info = str;
            }
        } catch (ConnectTimeoutException e) {
            throw e;
        } catch (SocketTimeoutException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            post.releaseConnection();
            ((SimpleHttpConnectionManager) client.getHttpConnectionManager())
                    .shutdown();
        }
        return info;
    }

    // 根据用户ID查询所有的主题帖(Android)
    public static String getTopicByUid(String uid, String bool, String lastid)
            throws Exception {
        String mess = null;
        HttpClient client = new HttpClient();
        client.getHttpConnectionManager().getParams()
                .setConnectionTimeout(5 * 1000);
        client.getHttpConnectionManager().getParams().setSoTimeout(10 * 1000);
        PostMethod post = new PostMethod("http://" + Constant.severIp
                + ":8080/tourServer/tour");
        post.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,
                "UTF-8");
        post.setRequestHeader("CMD", Constant.GET_TOPICBYUID);
        post.addParameter("uid", uid);
        post.addParameter("bool", bool);
        post.addParameter("lastid", lastid);

        try {
            client.executeMethod(post);
            String str = new String(IOUtils.toByteArray(post
                    .getResponseBodyAsStream()), "utf-8");
            if (!str.equals("null")) {
                mess = str;
            }
        } catch (ConnectTimeoutException e) {
            throw e;
        } catch (SocketTimeoutException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            post.releaseConnection();
            ((SimpleHttpConnectionManager) client.getHttpConnectionManager())
                    .shutdown();
        }
        return mess;
    }

    // 删除指定的主题帖（共用）
    public static boolean delTopic(String topicid) throws Exception {
        boolean flag = false;
        HttpClient client = new HttpClient();
        client.getHttpConnectionManager().getParams()
                .setConnectionTimeout(5 * 1000);
        client.getHttpConnectionManager().getParams().setSoTimeout(10 * 1000);
        PostMethod post = new PostMethod("http://" + Constant.severIp
                + ":8080/tourServer/tour");
        post.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,
                "UTF-8");
        post.setRequestHeader("CMD", Constant.DEL_TOPIC);
        post.addParameter("topicid", topicid);

        try {
            client.executeMethod(post);
            String str = new String(IOUtils.toByteArray(post
                    .getResponseBodyAsStream()), "utf-8");
            if (str.equals("ok")) {
                flag = true;
            }
        } catch (ConnectTimeoutException e) {
            throw e;
        } catch (SocketTimeoutException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            post.releaseConnection();
            ((SimpleHttpConnectionManager) client.getHttpConnectionManager())
                    .shutdown();
        }
        return flag;
    }

    // 根据用户ID查询所有的回复帖(Android)
    public static String getRepByUid(String uid, String bool, String lastid)
            throws Exception {
        String mess = null;
        HttpClient client = new HttpClient();
        client.getHttpConnectionManager().getParams()
                .setConnectionTimeout(5 * 1000);
        client.getHttpConnectionManager().getParams().setSoTimeout(10 * 1000);
        PostMethod post = new PostMethod("http://" + Constant.severIp
                + ":8080/tourServer/tour");
        post.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,
                "UTF-8");
        post.setRequestHeader("CMD", Constant.GET_REPLYBYUID);
        post.addParameter("uid", uid);
        post.addParameter("bool", bool);
        post.addParameter("lastid", lastid);

        try {
            client.executeMethod(post);
            String str = new String(IOUtils.toByteArray(post
                    .getResponseBodyAsStream()), "utf-8");
            if (!str.equals("null")) {
                mess = str;
            }
        } catch (ConnectTimeoutException e) {
            throw e;
        } catch (SocketTimeoutException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            post.releaseConnection();
            ((SimpleHttpConnectionManager) client.getHttpConnectionManager())
                    .shutdown();
        }
        return mess;
    }

    // 删除回复帖
    public static boolean delRepTopic(String repid) throws Exception {
        boolean flag = false;
        HttpClient client = new HttpClient();
        client.getHttpConnectionManager().getParams()
                .setConnectionTimeout(5 * 1000);
        client.getHttpConnectionManager().getParams().setSoTimeout(10 * 1000);
        PostMethod post = new PostMethod("http://" + Constant.severIp
                + ":8080/tourServer/tour");
        post.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,
                "UTF-8");
        post.setRequestHeader("CMD", Constant.DEL_REPLY);
        post.addParameter("repid", repid);

        try {
            client.executeMethod(post);
            String str = new String(IOUtils.toByteArray(post
                    .getResponseBodyAsStream()), "utf-8");
            if (str.equals("ok")) {
                flag = true;
            }
        } catch (ConnectTimeoutException e) {
            throw e;
        } catch (SocketTimeoutException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            post.releaseConnection();
            ((SimpleHttpConnectionManager) client.getHttpConnectionManager())
                    .shutdown();
        }
        return flag;
    }

    // 根据用户ID获得所有正常或禁用的游记主题（共用）
    public static String getTratopByUid(String uid, String bool, String lastid)
            throws Exception {
        String mess = null;
        HttpClient client = new HttpClient();
        client.getHttpConnectionManager().getParams()
                .setConnectionTimeout(5 * 1000);
        client.getHttpConnectionManager().getParams().setSoTimeout(10 * 1000);
        PostMethod post = new PostMethod("http://" + Constant.severIp
                + ":8080/tourServer/tour");
        post.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,
                "UTF-8");
        post.setRequestHeader("CMD", Constant.GET_TRATBYUID);
        post.addParameter("uid", uid);
        post.addParameter("bool", bool);
        post.addParameter("lastid", lastid);

        try {
            client.executeMethod(post);
            String str = new String(IOUtils.toByteArray(post
                    .getResponseBodyAsStream()), "utf-8");
            if (!str.equals("null")) {
                mess = str;
            }
        } catch (ConnectTimeoutException e) {
            throw e;
        } catch (SocketTimeoutException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            post.releaseConnection();
            ((SimpleHttpConnectionManager) client.getHttpConnectionManager())
                    .shutdown();
        }
        return mess;
    }

    // 删除游记
    public static boolean delTravelNote(String tranid, boolean ischange,
                                        String tratid) throws Exception {
        boolean flag = false;
        HttpClient client = new HttpClient();
        client.getHttpConnectionManager().getParams()
                .setConnectionTimeout(5 * 1000);
        client.getHttpConnectionManager().getParams().setSoTimeout(10 * 1000);
        PostMethod post = new PostMethod("http://" + Constant.severIp
                + ":8080/tourServer/tour");
        post.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,
                "UTF-8");
        post.setRequestHeader("CMD", Constant.DEL_TRANOTE);
        post.addParameter("tranid", tranid);
        if (ischange) {
            post.addParameter("ischange", "true");
        } else {
            post.addParameter("ischange", "false");
        }
        post.addParameter("tratid", tratid);

        try {
            client.executeMethod(post);
            String str = new String(IOUtils.toByteArray(post
                    .getResponseBodyAsStream()), "utf-8");
            if (str.equals("ok")) {
                flag = true;
            }
        } catch (ConnectTimeoutException e) {
            throw e;
        } catch (SocketTimeoutException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            post.releaseConnection();
            ((SimpleHttpConnectionManager) client.getHttpConnectionManager())
                    .shutdown();
        }
        return flag;
    }

    // 写游记
    public static String writeTraNote(Map<String, String> noteinfo)
            throws Exception {
        String noteid = null;
        HttpClient client = new HttpClient();
        client.getHttpConnectionManager().getParams()
                .setConnectionTimeout(5 * 1000);
        client.getHttpConnectionManager().getParams().setSoTimeout(10 * 1000);
        PostMethod post = new PostMethod("http://" + Constant.severIp
                + ":8080/tourServer/tour");
        post.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,
                "UTF-8");
        post.setRequestHeader("CMD", Constant.WRITE_NOTE);

        Part[] parts;
        int picCount = BitmapUtil.templist.size();
        int paraCount = noteinfo.size();

        System.out.println(picCount + "picCount===");
        System.out.println(paraCount + "paraCount===");

        Set<String> keyset = noteinfo.keySet();
        Iterator<String> iteor = keyset.iterator();

        if (picCount == 0) {
            parts = new Part[paraCount];
        } else {
            parts = new Part[picCount + paraCount];

            for (int i = 0; i < picCount; i++) {
                File file = new File(BitmapUtil.templist.get(i));
                FilePart filepart = new FilePart(file.getName(), file);
                parts[i] = filepart;
            }
        }

        for (int j = 0; iteor.hasNext(); j++) {
            String param = iteor.next();
            parts[picCount + j] = new StringPart(param, noteinfo.get(param),
                    "UTF-8");
        }

        post.setRequestEntity(new MultipartRequestEntity(parts, post
                .getParams()));

        try {
            client.executeMethod(post);
            String str = new String(IOUtils.toByteArray(post
                    .getResponseBodyAsStream()), "utf-8");
            if (!str.equals("null")) {
                noteid = str;
            }
        } catch (ConnectTimeoutException e) {
            throw e;
        } catch (SocketTimeoutException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            post.releaseConnection();
            ((SimpleHttpConnectionManager) client.getHttpConnectionManager())
                    .shutdown();
        }

        return noteid;
    }

    // 根据游记ID查询游记信息
    public static String getNoteById(String noteid) throws Exception {
        String mess = null;
        HttpClient client = new HttpClient();
        client.getHttpConnectionManager().getParams()
                .setConnectionTimeout(5 * 1000);
        client.getHttpConnectionManager().getParams().setSoTimeout(10 * 1000);
        PostMethod post = new PostMethod("http://" + Constant.severIp
                + ":8080/tourServer/tour");
        post.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,
                "UTF-8");
        post.setRequestHeader("CMD", Constant.QUERY_NOTEBYID);
        post.addParameter("noteid", noteid);

        try {
            client.executeMethod(post);
            String str = new String(IOUtils.toByteArray(post
                    .getResponseBodyAsStream()), "utf-8");
            if (!str.equals("null")) {
                mess = str;
            }
        } catch (ConnectTimeoutException e) {
            throw e;
        } catch (SocketTimeoutException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            post.releaseConnection();
            ((SimpleHttpConnectionManager) client.getHttpConnectionManager())
                    .shutdown();
        }
        return mess;
    }

    // 创建游记主题
    public static String creTraTopic(String uid, String tratop_name,
                                     String tratop_place) throws Exception {
        String tratid = null;
        HttpClient client = new HttpClient();
        client.getHttpConnectionManager().getParams()
                .setConnectionTimeout(5 * 1000);
        client.getHttpConnectionManager().getParams().setSoTimeout(10 * 1000);
        PostMethod post = new PostMethod("http://" + Constant.severIp
                + ":8080/tourServer/tour");
        post.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,
                "UTF-8");
        post.setRequestHeader("CMD", Constant.CREATE_TRAT);
        post.addParameter("uid", uid);
        post.addParameter("tratop_name", tratop_name);
        post.addParameter("tratop_place", tratop_place);

        try {
            client.executeMethod(post);
            String str = new String(IOUtils.toByteArray(post
                    .getResponseBodyAsStream()), "utf-8");
            if (!str.equals("fail")) {
                tratid = str;
            }
        } catch (ConnectTimeoutException e) {
            throw e;
        } catch (SocketTimeoutException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            post.releaseConnection();
            ((SimpleHttpConnectionManager) client.getHttpConnectionManager())
                    .shutdown();
        }
        return tratid;
    }

    // 根据游记主题ID查询主题信息
    public static String getTratById(String tratid) throws Exception {
        String mess = null;
        HttpClient client = new HttpClient();
        client.getHttpConnectionManager().getParams()
                .setConnectionTimeout(5 * 1000);
        client.getHttpConnectionManager().getParams().setSoTimeout(10 * 1000);
        PostMethod post = new PostMethod("http://" + Constant.severIp
                + ":8080/tourServer/tour");
        post.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,
                "UTF-8");
        post.setRequestHeader("CMD", Constant.QUERY_TRATBYID);
        post.addParameter("tratid", tratid);

        try {
            client.executeMethod(post);
            String str = new String(IOUtils.toByteArray(post
                    .getResponseBodyAsStream()), "utf-8");
            if (!str.equals("null")) {
                mess = str;
            }
        } catch (ConnectTimeoutException e) {
            throw e;
        } catch (SocketTimeoutException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            post.releaseConnection();
            ((SimpleHttpConnectionManager) client.getHttpConnectionManager())
                    .shutdown();
        }
        return mess;
    }

    // 获得用户的出行计划
    public static String getUserPlan(String uid, String lastid)
            throws Exception {
        String mess = null;
        HttpClient client = new HttpClient();
        client.getHttpConnectionManager().getParams()
                .setConnectionTimeout(5 * 1000);
        client.getHttpConnectionManager().getParams().setSoTimeout(10 * 1000);
        PostMethod post = new PostMethod("http://" + Constant.severIp
                + ":8080/tourServer/tour");
        post.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,
                "UTF-8");
        post.setRequestHeader("CMD", Constant.GET_USERPLAN);
        post.addParameter("uid", uid);
        post.addParameter("lastid", lastid);

        try {
            client.executeMethod(post);
            String str = new String(IOUtils.toByteArray(post
                    .getResponseBodyAsStream()), "utf-8");
            if (!str.equals("null")) {
                mess = str;
            }
        } catch (ConnectTimeoutException e) {
            throw e;
        } catch (SocketTimeoutException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            post.releaseConnection();
            ((SimpleHttpConnectionManager) client.getHttpConnectionManager())
                    .shutdown();
        }
        return mess;
    }

    // 发表出行计划
    public static String publishPlan(Map<String, String> planinfo)
            throws Exception {
        String planid = null;
        HttpClient client = new HttpClient();
        client.getHttpConnectionManager().getParams()
                .setConnectionTimeout(5 * 1000);
        client.getHttpConnectionManager().getParams().setSoTimeout(10 * 1000);
        PostMethod post = new PostMethod("http://" + Constant.severIp
                + ":8080/tourServer/tour");
        post.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,
                "UTF-8");
        post.setRequestHeader("CMD", Constant.PUBLISH_PLAN);
        Set<String> keyset = planinfo.keySet();
        Iterator<String> iteor = keyset.iterator();

        while (iteor.hasNext()) {
            String param = iteor.next();
            post.addParameter(param, planinfo.get(param));
        }

        try {
            client.executeMethod(post);
            String str = new String(IOUtils.toByteArray(post
                    .getResponseBodyAsStream()), "utf-8");
            if (!str.equals("fail")) {
                planid = str;
            }
        } catch (ConnectTimeoutException e) {
            throw e;
        } catch (SocketTimeoutException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            post.releaseConnection();
            ((SimpleHttpConnectionManager) client.getHttpConnectionManager())
                    .shutdown();
        }
        return planid;
    }

    // 删除计划
    public static boolean delPlan(String planid) throws Exception {
        boolean flag = false;
        HttpClient client = new HttpClient();
        client.getHttpConnectionManager().getParams()
                .setConnectionTimeout(5 * 1000);
        client.getHttpConnectionManager().getParams().setSoTimeout(10 * 1000);
        PostMethod post = new PostMethod("http://" + Constant.severIp
                + ":8080/tourServer/tour");
        post.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,
                "UTF-8");
        post.setRequestHeader("CMD", Constant.DEL_PLAN);
        post.addParameter("planid", planid);

        try {
            client.executeMethod(post);
            String str = new String(IOUtils.toByteArray(post
                    .getResponseBodyAsStream()), "utf-8");
            if (str.equals("ok")) {
                flag = true;
            }
        } catch (ConnectTimeoutException e) {
            throw e;
        } catch (SocketTimeoutException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            post.releaseConnection();
            ((SimpleHttpConnectionManager) client.getHttpConnectionManager())
                    .shutdown();
        }
        return flag;
    }

    // 匹配计划
    public static String matchPlan(String planid, String endplace,
                                   String startdate, String enddate) throws Exception {
        String mess = null;
        HttpClient client = new HttpClient();
        client.getHttpConnectionManager().getParams()
                .setConnectionTimeout(5 * 1000);
        client.getHttpConnectionManager().getParams().setSoTimeout(10 * 1000);
        PostMethod post = new PostMethod("http://" + Constant.severIp
                + ":8080/tourServer/tour");
        post.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,
                "UTF-8");
        post.setRequestHeader("CMD", Constant.MATCH_PLAN);
        post.addParameter("planid", planid);
        post.addParameter("endplace", endplace);
        post.addParameter("startdate", startdate);
        post.addParameter("enddate", enddate);

        try {
            client.executeMethod(post);
            String str = new String(IOUtils.toByteArray(post
                    .getResponseBodyAsStream()), "utf-8");
            if (!str.equals("null")) {
                mess = str;
            }
        } catch (ConnectTimeoutException e) {
            throw e;
        } catch (SocketTimeoutException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            post.releaseConnection();
            ((SimpleHttpConnectionManager) client.getHttpConnectionManager())
                    .shutdown();
        }
        return mess;
    }

    // 查询未读留言
    public static String checkLeaMess(String uid) throws Exception {
        String mess = null;
        HttpClient client = new HttpClient();
        client.getHttpConnectionManager().getParams()
                .setConnectionTimeout(5 * 1000);
        client.getHttpConnectionManager().getParams().setSoTimeout(10 * 1000);
        PostMethod post = new PostMethod("http://" + Constant.severIp
                + ":8080/tourServer/tour");
        post.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,
                "UTF-8");
        post.setRequestHeader("CMD", Constant.CHECK_LEAMESS);
        post.addParameter("uid", uid);

        try {
            client.executeMethod(post);
            String str = new String(IOUtils.toByteArray(post
                    .getResponseBodyAsStream()), "utf-8");
            if (!str.equals("null")) {
                mess = str;
            }
        } catch (ConnectTimeoutException e) {
            throw e;
        } catch (SocketTimeoutException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            post.releaseConnection();
            ((SimpleHttpConnectionManager) client.getHttpConnectionManager())
                    .shutdown();
        }
        return mess;
    }

    // 查询已读留言
    public static String checkReadMess(String uid) throws Exception {
        String mess = null;
        HttpClient client = new HttpClient();
        client.getHttpConnectionManager().getParams()
                .setConnectionTimeout(5 * 1000);
        client.getHttpConnectionManager().getParams().setSoTimeout(10 * 1000);
        PostMethod post = new PostMethod("http://" + Constant.severIp
                + ":8080/tourServer/tour");
        post.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,
                "UTF-8");
        post.setRequestHeader("CMD", Constant.GET_READMESS);
        post.addParameter("uid", uid);

        try {
            client.executeMethod(post);
            String str = new String(IOUtils.toByteArray(post
                    .getResponseBodyAsStream()), "utf-8");
            if (!str.equals("null")) {
                mess = str;
            }
        } catch (ConnectTimeoutException e) {
            throw e;
        } catch (SocketTimeoutException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            post.releaseConnection();
            ((SimpleHttpConnectionManager) client.getHttpConnectionManager())
                    .shutdown();
        }
        return mess;
    }

    // 查询指定用户发出的留言
    public static String getSendMess(String uid) throws Exception {
        String mess = null;
        HttpClient client = new HttpClient();
        client.getHttpConnectionManager().getParams()
                .setConnectionTimeout(5 * 1000);
        client.getHttpConnectionManager().getParams().setSoTimeout(10 * 1000);
        PostMethod post = new PostMethod("http://" + Constant.severIp
                + ":8080/tourServer/tour");
        post.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,
                "UTF-8");
        post.setRequestHeader("CMD", Constant.GET_SENDMESS);
        post.addParameter("uid", uid);

        try {
            client.executeMethod(post);
            String str = new String(IOUtils.toByteArray(post
                    .getResponseBodyAsStream()), "utf-8");
            if (!str.equals("null")) {
                mess = str;
            }
        } catch (ConnectTimeoutException e) {
            throw e;
        } catch (SocketTimeoutException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            post.releaseConnection();
            ((SimpleHttpConnectionManager) client.getHttpConnectionManager())
                    .shutdown();
        }
        return mess;
    }

    // 标记已读过的留言(Android)
    public static boolean markLeaveMess(String lmsid) throws Exception {
        boolean flag = false;
        HttpClient client = new HttpClient();
        client.getHttpConnectionManager().getParams()
                .setConnectionTimeout(5 * 1000);
        client.getHttpConnectionManager().getParams().setSoTimeout(10 * 1000);
        PostMethod post = new PostMethod("http://" + Constant.severIp
                + ":8080/tourServer/tour");
        post.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,
                "UTF-8");
        post.setRequestHeader("CMD", Constant.MARK_MESS);
        post.addParameter("lmsid", lmsid);

        try {
            client.executeMethod(post);
            String str = new String(IOUtils.toByteArray(post
                    .getResponseBodyAsStream()), "utf-8");
            if (str.equals("ok")) {
                flag = true;
            }
        } catch (ConnectTimeoutException e) {
            throw e;
        } catch (SocketTimeoutException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            post.releaseConnection();
            ((SimpleHttpConnectionManager) client.getHttpConnectionManager())
                    .shutdown();
        }
        return flag;
    }

    // 删除发出的留言
    public static boolean delSendMess(String lmsid) throws Exception {
        boolean flag = false;
        HttpClient client = new HttpClient();
        client.getHttpConnectionManager().getParams()
                .setConnectionTimeout(5 * 1000);
        client.getHttpConnectionManager().getParams().setSoTimeout(10 * 1000);
        PostMethod post = new PostMethod("http://" + Constant.severIp
                + ":8080/tourServer/tour");
        post.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,
                "UTF-8");
        post.setRequestHeader("CMD", Constant.DEL_SENDMESS);
        post.addParameter("lmsid", lmsid);

        try {
            client.executeMethod(post);
            String str = new String(IOUtils.toByteArray(post
                    .getResponseBodyAsStream()), "utf-8");
            if (str.equals("ok")) {
                flag = true;
            }
        } catch (ConnectTimeoutException e) {
            throw e;
        } catch (SocketTimeoutException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            post.releaseConnection();
            ((SimpleHttpConnectionManager) client.getHttpConnectionManager())
                    .shutdown();
        }
        return flag;
    }

    // 删除接收的留言
    public static boolean delAcceMess(String lmsid) throws Exception {
        boolean flag = false;
        HttpClient client = new HttpClient();
        client.getHttpConnectionManager().getParams()
                .setConnectionTimeout(5 * 1000);
        client.getHttpConnectionManager().getParams().setSoTimeout(10 * 1000);
        PostMethod post = new PostMethod("http://" + Constant.severIp
                + ":8080/tourServer/tour");
        post.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,
                "UTF-8");
        post.setRequestHeader("CMD", Constant.DEL_ACCEMESS);
        post.addParameter("lmsid", lmsid);

        try {
            client.executeMethod(post);
            String str = new String(IOUtils.toByteArray(post
                    .getResponseBodyAsStream()), "utf-8");
            if (str.equals("ok")) {
                flag = true;
            }
            System.out.println(str);
        } catch (ConnectTimeoutException e) {
            throw e;
        } catch (SocketTimeoutException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            post.releaseConnection();
            ((SimpleHttpConnectionManager) client.getHttpConnectionManager())
                    .shutdown();
        }
        return flag;
    }

    // 留言(Android)
    public static boolean leaveMessage(String uid, String acceuid,
                                       String content) throws Exception {
        boolean flag = false;
        HttpClient client = new HttpClient();
        client.getHttpConnectionManager().getParams()
                .setConnectionTimeout(5 * 1000);
        client.getHttpConnectionManager().getParams().setSoTimeout(10 * 1000);
        PostMethod post = new PostMethod("http://" + Constant.severIp
                + ":8080/tourServer/tour");
        post.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,
                "UTF-8");
        post.setRequestHeader("CMD", Constant.LEAVE_MESSAGE);
        post.addParameter("uid", uid);
        post.addParameter("acceuid", acceuid);
        post.addParameter("content", content);

        try {
            client.executeMethod(post);
            String str = new String(IOUtils.toByteArray(post
                    .getResponseBodyAsStream()), "utf-8");
            if (str.equals("ok")) {
                flag = true;
            }
        } catch (ConnectTimeoutException e) {
            throw e;
        } catch (SocketTimeoutException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            post.releaseConnection();
            ((SimpleHttpConnectionManager) client.getHttpConnectionManager())
                    .shutdown();
        }
        return flag;
    }

    // 创建版块
    public static String createBlock(Map<String, String> blockinfo)
            throws Exception {
        String blockid = null;
        HttpClient client = new HttpClient();
        client.getHttpConnectionManager().getParams()
                .setConnectionTimeout(5 * 1000);
        client.getHttpConnectionManager().getParams().setSoTimeout(10 * 1000);
        PostMethod post = new PostMethod("http://" + Constant.severIp
                + ":8080/tourServer/tour");
        post.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,
                "UTF-8");
        post.setRequestHeader("CMD", Constant.CREATE_BLOCK);

        Part[] parts;
        int picCount = BitmapUtil.templist.size();
        int paraCount = blockinfo.size();

        Set<String> keyset = blockinfo.keySet();
        Iterator<String> iteor = keyset.iterator();

        if (picCount == 0) {
            parts = new Part[paraCount];
        } else {
            parts = new Part[picCount + paraCount];

            for (int i = 0; i < picCount; i++) {
                File file = new File(BitmapUtil.templist.get(i));
                FilePart filepart = new FilePart(file.getName(), file);
                parts[i] = filepart;
            }
        }

        for (int j = 0; iteor.hasNext(); j++) {
            String param = iteor.next();
            parts[picCount + j] = new StringPart(param, blockinfo.get(param),
                    "UTF-8");
        }

        post.setRequestEntity(new MultipartRequestEntity(parts, post
                .getParams()));

        try {
            client.executeMethod(post);
            String str = new String(IOUtils.toByteArray(post
                    .getResponseBodyAsStream()), "utf-8");
            if (!str.equals("null")) {
                blockid = str;
            }
        } catch (ConnectTimeoutException e) {
            throw e;
        } catch (SocketTimeoutException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            post.releaseConnection();
            ((SimpleHttpConnectionManager) client.getHttpConnectionManager())
                    .shutdown();
        }

        return blockid;
    }

    // 根据用户ID查询所有的正常和禁用的用户的版块信息（共用）
    public static String getNfBloinfoByUid(String uid) throws Exception {
        String mess = null;
        HttpClient client = new HttpClient();
        client.getHttpConnectionManager().getParams()
                .setConnectionTimeout(5 * 1000);
        client.getHttpConnectionManager().getParams().setSoTimeout(10 * 1000);
        PostMethod post = new PostMethod("http://" + Constant.severIp
                + ":8080/tourServer/tour");
        post.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,
                "UTF-8");
        post.setRequestHeader("CMD", Constant.GET_BLOINFOBYUID);
        post.addParameter("uid", uid);

        try {
            client.executeMethod(post);
            String str = new String(IOUtils.toByteArray(post
                    .getResponseBodyAsStream()), "utf-8");
            if (!str.equals("null")) {
                mess = str;
            }
        } catch (ConnectTimeoutException e) {
            throw e;
        } catch (SocketTimeoutException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            post.releaseConnection();
            ((SimpleHttpConnectionManager) client.getHttpConnectionManager())
                    .shutdown();
        }
        return mess;
    }

    // 根据版块ID查询版块信息
    public static String getBlockById(String bid) throws Exception {
        String mess = null;
        HttpClient client = new HttpClient();
        client.getHttpConnectionManager().getParams()
                .setConnectionTimeout(5 * 1000);
        client.getHttpConnectionManager().getParams().setSoTimeout(10 * 1000);
        PostMethod post = new PostMethod("http://" + Constant.severIp
                + ":8080/tourServer/tour");
        post.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,
                "UTF-8");
        post.setRequestHeader("CMD", Constant.QUERY_BLOCKBYID);
        post.addParameter("bid", bid);

        try {
            client.executeMethod(post);
            String str = new String(IOUtils.toByteArray(post
                    .getResponseBodyAsStream()), "utf-8");
            if (!str.equals("null")) {
                mess = str;
            }
        } catch (ConnectTimeoutException e) {
            throw e;
        } catch (SocketTimeoutException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            post.releaseConnection();
            ((SimpleHttpConnectionManager) client.getHttpConnectionManager())
                    .shutdown();
        }
        return mess;
    }

    // 编辑版块信息
    public static boolean editBlock(String bid, String intro) throws Exception {
        boolean flag = false;
        HttpClient client = new HttpClient();
        client.getHttpConnectionManager().getParams()
                .setConnectionTimeout(5 * 1000);
        client.getHttpConnectionManager().getParams().setSoTimeout(10 * 1000);
        PostMethod post = new PostMethod("http://" + Constant.severIp
                + ":8080/tourServer/tour");
        post.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,
                "UTF-8");
        post.setRequestHeader("CMD", Constant.EDIT_BLOCK);

        Part[] parts;
        if (BitmapUtil.templist.size() == 0) {
            parts = new Part[2];
            parts[0] = new StringPart("bid", bid, "UTF-8");
            parts[1] = new StringPart("intro", intro, "UTF-8");

        } else {
            File file = new File(BitmapUtil.templist.get(0));

            if (file.exists()) {
                parts = new Part[3];
                parts[0] = new FilePart(file.getName(), file);
                parts[1] = new StringPart("bid", bid, "UTF-8");
                parts[2] = new StringPart("intro", intro, "UTF-8");
            } else {
                parts = new Part[2];
                parts[0] = new StringPart("bid", bid, "UTF-8");
                parts[1] = new StringPart("intro", intro, "UTF-8");
            }
        }

        post.setRequestEntity(new MultipartRequestEntity(parts, post
                .getParams()));

        try {
            client.executeMethod(post);
            String str = new String(IOUtils.toByteArray(post
                    .getResponseBodyAsStream()), "utf-8");
            if (str.equals("ok")) {
                flag = true;
            }
        } catch (ConnectTimeoutException e) {
            throw e;
        } catch (SocketTimeoutException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            post.releaseConnection();
            ((SimpleHttpConnectionManager) client.getHttpConnectionManager())
                    .shutdown();
        }

        return flag;
    }

    // 主题帖点赞
    public static String addTopicZan(String uid, String topicid)
            throws Exception {
        String mess = null;
        HttpClient client = new HttpClient();
        client.getHttpConnectionManager().getParams()
                .setConnectionTimeout(2 * 1000);
        client.getHttpConnectionManager().getParams().setSoTimeout(3 * 1000);
        PostMethod post = new PostMethod("http://" + Constant.severIp
                + ":8080/tourServer/tour");
        post.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,
                "UTF-8");
        post.setRequestHeader("CMD", Constant.ADD_TOPICZAN);
        post.addParameter("uid", uid);
        post.addParameter("topicid", topicid);

        try {
            client.executeMethod(post);
            String str = new String(IOUtils.toByteArray(post
                    .getResponseBodyAsStream()), "utf-8");
            if (str != null) {
                mess = str;
            }
        } catch (ConnectTimeoutException e) {
            throw e;
        } catch (SocketTimeoutException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            post.releaseConnection();
            ((SimpleHttpConnectionManager) client.getHttpConnectionManager())
                    .shutdown();
        }
        return mess;
    }

    // 回复帖点赞
    public static String addReplyZan(String uid, String replyid)
            throws Exception {
        String mess = null;
        HttpClient client = new HttpClient();
        client.getHttpConnectionManager().getParams()
                .setConnectionTimeout(2 * 1000);
        client.getHttpConnectionManager().getParams().setSoTimeout(3 * 1000);
        PostMethod post = new PostMethod("http://" + Constant.severIp
                + ":8080/tourServer/tour");
        post.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,
                "UTF-8");
        post.setRequestHeader("CMD", Constant.ADD_REPLYZAN);
        post.addParameter("uid", uid);
        post.addParameter("replyid", replyid);

        try {
            client.executeMethod(post);
            String str = new String(IOUtils.toByteArray(post
                    .getResponseBodyAsStream()), "utf-8");
            if (str != null) {
                mess = str;
            }
        } catch (ConnectTimeoutException e) {
            throw e;
        } catch (SocketTimeoutException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            post.releaseConnection();
            ((SimpleHttpConnectionManager) client.getHttpConnectionManager())
                    .shutdown();
        }
        return mess;
    }

    // 游记主题点赞
    public static String addTratZan(String uid, String tratid, String touid)
            throws Exception {
        String mess = null;
        HttpClient client = new HttpClient();
        client.getHttpConnectionManager().getParams()
                .setConnectionTimeout(2 * 1000);
        client.getHttpConnectionManager().getParams().setSoTimeout(3 * 1000);
        PostMethod post = new PostMethod("http://" + Constant.severIp
                + ":8080/tourServer/tour");
        post.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,
                "UTF-8");
        post.setRequestHeader("CMD", Constant.ADD_TRATZAN);
        post.addParameter("uid", uid);
        post.addParameter("tratid", tratid);
        post.addParameter("touid", touid);

        try {
            client.executeMethod(post);
            String str = new String(IOUtils.toByteArray(post
                    .getResponseBodyAsStream()), "utf-8");
            if (str != null) {
                mess = str;
            }
        } catch (ConnectTimeoutException e) {
            throw e;
        } catch (SocketTimeoutException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            post.releaseConnection();
            ((SimpleHttpConnectionManager) client.getHttpConnectionManager())
                    .shutdown();
        }
        return mess;
    }

    // 将版块的点击数量+1(Android)
    public static void addBloCliCount(String bloid) {
        HttpClient client = new HttpClient();
        client.getHttpConnectionManager().getParams()
                .setConnectionTimeout(2 * 1000);
        client.getHttpConnectionManager().getParams().setSoTimeout(3 * 1000);
        PostMethod post = new PostMethod("http://" + Constant.severIp
                + ":8080/tourServer/tour");
        post.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,
                "UTF-8");
        post.setRequestHeader("CMD", Constant.ADD_BLOCLICK);
        post.addParameter("bloid", bloid);

        try {
            client.executeMethod(post);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            post.releaseConnection();
            ((SimpleHttpConnectionManager) client.getHttpConnectionManager())
                    .shutdown();
        }
    }

    // 将主题帖的浏览数+1(Android)
    public static void addTopBrowCount(String topicid) {
        HttpClient client = new HttpClient();
        client.getHttpConnectionManager().getParams()
                .setConnectionTimeout(2 * 1000);
        client.getHttpConnectionManager().getParams().setSoTimeout(3 * 1000);
        PostMethod post = new PostMethod("http://" + Constant.severIp
                + ":8080/tourServer/tour");
        post.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,
                "UTF-8");
        post.setRequestHeader("CMD", Constant.ADD_TOPICBROW);
        post.addParameter("topicid", topicid);

        try {
            client.executeMethod(post);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            post.releaseConnection();
            ((SimpleHttpConnectionManager) client.getHttpConnectionManager())
                    .shutdown();
        }
    }

}
