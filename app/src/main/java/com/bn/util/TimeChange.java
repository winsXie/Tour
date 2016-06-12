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
		    long diff = (d1.getTime() - d2.getTime())/1000;//ת��������
		    
		    if(diff<15)
		    {
		    	changedTime="�ո�";
		    }
		    else if(diff<60)//С��һ����
		    {
		    	changedTime=diff+"��ǰ";
		    }
		    else if(diff<60*60)//С��һСʱ
		    {
		    	changedTime=(diff/60)+"����ǰ";
		    }
		    else if(diff<24*60*60)//С��һ��
		    {
		    	changedTime=(diff/(60*60))+"Сʱǰ";
		    }
		    else //���ڵ���һ��
		    {
		    	diff=diff/(60*60*24);//ת��������
		    	if(diff==1)
		    	{
		    		changedTime="����";
		    	}
		    	else if(diff<7)
		    	{
		    		changedTime=diff+"��ǰ";
		    	}
		    	else if(diff<14)
		    	{
		    		changedTime="һ��ǰ";
		    	}
		    	else
		    	{
		    		changedTime=time.substring(0, 10); //��������ֱ�ӷ�������
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
