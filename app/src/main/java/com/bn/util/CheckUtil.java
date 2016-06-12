package com.bn.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CheckUtil
{
	/**
	 * ��֤����
	 * 
	 * @param email
	 * @return
	 */
	public static boolean checkEmail(String email)
	{
		boolean flag = false;
		try
		{
			String check = "^([a-z0-9A-Z]+[-|_|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
			Pattern regex = Pattern.compile(check);
			Matcher matcher = regex.matcher(email);
			flag = matcher.matches();
		}
		catch (Exception e)
		{
			flag = false;
		}
		return flag;
	}
	
	
	/**ȥ���ַ����е��Ʊ�����س��������з�����ֹ����jsonʱ����
	 * @param str
	 * @return
	 */
	public static String replaceBlank(String str)
	{
		String dest = "";
		if (str != null)
		{
			Pattern p = Pattern.compile("\\t|\r|\n");
			Matcher m = p.matcher(str);
			dest = m.replaceAll("");
		}
		return dest;
	}
}
