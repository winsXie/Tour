package com.bn.util;

import net.sf.json.JSONObject;

public class Constant
{//115.28.152.240
	public static String severIp="192.168.56.1";//服务器IP地址
	public static boolean hasSDcard=false;//是否有SD卡
	public static String sdRootPath;//SD卡根目录
	public static String cachePath="/tour";//图片缓存目录
	public static String tempPath="/tour/temp/";//存储将要上传的已被压缩的图片
	public static String sourcePic="/tour/sourcePic/";//浏览原图后在本地的存储路径
	public static boolean isLogin;//是否登录
	public static String loginUid;//当前用户id
	public static JSONObject currUserInfo;//当前用户信息
	public static String currBloId;//当前所在版块id
	public static String currTopicId;//当前所在主题帖id
	public static String currTratopId;//当前所在游记主题id
	public static String currTratInfo;//当前所在游记主题的信息
	public static JSONObject currTopicInfo;//当前所在主题帖的信息
	public static JSONObject currBlockInfo;//当前所在版块的信息
	
	public static final int CONNECTIONTIMEOUT=-1;//连接超时
	public static final int SOTIMEOUT=-2;//读取数据超时
	
	/*
	 * command
	 */
	public static final String GET_PUSH="GET_PUSH";//获取广告
	public static final String GET_RECOTOPIC="GET_RECOTOPIC";//获得推荐的主题帖
	public static final String GET_TOGEPLAN="GET_TOGEPLAN";// 根据发布时间及用户等级获得3条计划（发现---结伴同游）
	public static final String GET_RECOTRATOP="GET_RECOTRATOP";//获取推荐的游记主题
	public static final String GET_NFBLOCKINFO="GET_NFBLOCKINFO";// 获得所有正常或禁用的版块（共用）
	public static final String QUERY_BLOBYNAME="QUERY_BLOBYNAME";// 根据版块名称查询所有的正常或禁用的用户的版块信息（共用）
	public static final String GET_TOPTOPIC="GET_TOPTOPIC";//获得一个版块内的置顶帖（共用）
	public static final String GET_BESTTOPIC="GET_BESTTOPIC";//获得一个版块内的置精帖	（共用）
	public static final String GET_NFTOPICOFBLOCK="GET_NFTOPICOFBLOCK";//获得一个版块内的所有正常或禁用的主题帖（共用）	
	public static final String PUBLISH_TOPIC="PUBLISH_TOPIC";//发表主题帖
	public static final String GET_NFREPTOPIC="GET_NFREPTOPIC";//获得一个主题帖的所有正常或禁用的回复帖（共用）
	public static final String PUBLISH_REPLY="PUBLISH_REPLY";//发表回复帖
	public static final String QUERY_REPBYID="QUERY_REPBYID";// 根据回复帖ID查询信息
	public static final String QUERY_TOPICBYID="QUERY_TOPICBYID";// 根据主题帖ID查询信息
	public static final String QUERY_PLANBYID="QUERY_PLANBYID";// 根据计划ID查询相应计划的详细信息(Android)
	public static final String EDIT_PLAN="EDIT_PLAN";//修改计划
	public static final String GET_NOTEOFTRAT="GET_NOTEOFTRAT";// 获得一个游记主题内的所有正常或禁用的游记（共用）(Android不提供对游记点赞功能，可对相应的游记主题点赞)
	public static final String LOGIN="LOGIN";//登录
	public static final String REGISTER="REGISTER";//注册
	public static final String GET_BASEINFO="GET_BASEINFO";// 获得个人基本资料(Android)
	public static final String CHANGE_HEADIMG="CHANGE_HEADIMG";//设置用户头像
	public static final String SETQIANMING="SETQIANMING";//设置签名
	public static final String CHANGE_PASSWORD="CHANGE_PASSWORD";//设置签名
	public static final String EDIT_BASEDINFO="EDIT_BASEDINFO";//编辑个人基本资料
	public static final String GET_TOPICBYUID="GET_TOPICBYUID";// 根据用户ID查询所有的主题帖(Android)
	public static final String DEL_TOPIC="DEL_TOPIC";//删除主题帖
	public static final String GET_REPLYBYUID="GET_REPLYBYUID";// 根据用户ID查询所有的回复帖(Android)
	public static final String DEL_REPLY="DEL_REPLY";//删除回复帖
	public static final String GET_TRATBYUID="GET_TRATBYUID";// 根据用户ID查询游记主题(Android)
	public static final String DEL_TRANOTE="DEL_TRANOTE";//删除游记
	public static final String WRITE_NOTE="WRITE_NOTE";//写游记
	public static final String QUERY_NOTEBYID="QUERY_NOTEBYID";//根据游记ID查询游记信息
	public static final String CREATE_TRAT="CREATE_TRAT";//创建游记主题
	public static final String QUERY_TRATBYID="QUERY_TRATBYID";//根据游记主题ID查询主题信息
	public static final String GET_USERPLAN="GET_USERPLAN";//获得用户的出行计划
	public static final String PUBLISH_PLAN="PUBLISH_PLAN";//发布出行计划
	public static final String DEL_PLAN="DEL_PLAN";//删除计划
	public static final String MATCH_PLAN="MATCH_PLAN";//匹配出行计划
	public static final String CHECK_LEAMESS="CHECK_LEAMESS";//查询未读留言
	public static final String GET_READMESS="GET_READMESS";//查询已读留言
	public static final String GET_SENDMESS="GET_SENDMESS";//查询指定用户发出的留言
	public static final String MARK_MESS="MARK_MESS";//标记留言为已读
	public static final String DEL_SENDMESS="DEL_SENDMESS";//删除发出的留言
	public static final String DEL_ACCEMESS="DEL_ACCEMESS";//删除接收的留言
	public static final String LEAVE_MESSAGE="LEAVE_MESSAGE";//留言
	public static final String CREATE_BLOCK="CREATE_BLOCK";//创建版块
	public static final String GET_BLOINFOBYUID="GET_BLOINFOBYUID";// 根据用户ID查询所有的正常和禁用的用户的版块信息（共用）
	public static final String QUERY_BLOCKBYID="QUERY_BLOCKBYID";//根据版块ID查询版块信息
	public static final String EDIT_BLOCK="EDIT_BLOCK";//编辑版块信息
	public static final String ADD_TOPICZAN="ADD_TOPICZAN";// 主题帖点赞
	public static final String ADD_REPLYZAN="ADD_REPLYZAN";// 回复帖点赞
	public static final String ADD_TRATZAN="ADD_TRATZAN";// 游记主题点赞
	public static final String ADD_BLOCLICK="ADD_BLOCLICK";// 将版块的点击数量+1(Android)
	public static final String ADD_TOPICBROW="ADD_TOPICBROW";// 将主题帖的浏览数量+1(Android)
}
