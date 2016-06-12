package com.bn.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.annotation.SuppressLint;

public class TimeChange
{
	@SuppressLint("SimpleDateFormat")
	public static String changeTime(String time)
	{
		String changedTime=time;
		
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
		try
		{
			Date d1 = new Date(System.currentTimeMillis());
			Date d2 = fmt.parse(time);
		    long diff = (d1.getTime() - d2.getTime())/1000;//转换成秒数
		    
		    if(diff<15)
		    {
		    	changedTime="刚刚";
		    }
		    else if(diff<60)//小于一分钟
		    {
		    	changedTime=diff+"秒前";
		    }
		    else if(diff<60*60)//小于一小时
		    {
		    	changedTime=(diff/60)+"分钟前";
		    }
		    else if(diff<24*60*60)//小于一天
		    {
		    	changedTime=(diff/(60*60))+"小时前";
		    }
		    else //大于等于一天
		    {
		    	diff=diff/(60*60*24);//转换成天数
		    	if(diff==1)
		    	{
		    		changedTime="昨天";
		    	}
		    	else if(diff<7)
		    	{
		    		changedTime=diff+"天前";
		    	}
		    	else if(diff<14)
		    	{
		    		changedTime="一周前";
		    	}
		    	else
		    	{
		    		changedTime=time.substring(0, 10); //大于两周直接返回日期
		    	}
		    	
		    	
		    }
		}
		catch (ParseException e)
		{
			e.printStackTrace();
		}
		
		return changedTime;
	}
	
	public static String calcuTime(Calendar calendar1,Calendar calendar2)
	{
		long milliseconds1 = calendar1.getTimeInMillis();
		long milliseconds2 = calendar2.getTimeInMillis();
		long diff = milliseconds2 - milliseconds1;
		long diffYears = diff / (24 * 60 * 60 * 1000)/365;
		
		return diffYears+"";
	}
}
