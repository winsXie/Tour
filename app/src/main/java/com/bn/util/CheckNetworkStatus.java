package com.bn.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * ¼ì²éÍøÂç×´Ì¬
 *
 */
public class CheckNetworkStatus
{
	public static boolean checkNetworkAvailable(Context context)
	{
		ConnectivityManager connectivity = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity == null)
		{
			return false;
		}
		else
		{
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null)
			{
				for (int i = 0; i < info.length; i++)
				{
					if (info[i].getState() == NetworkInfo.State.CONNECTED)
					{
						NetworkInfo netWorkInfo = info[i];
						if (netWorkInfo.getType() == ConnectivityManager.TYPE_WIFI)
						{
							return true;
						}
						else if (netWorkInfo.getType() == ConnectivityManager.TYPE_MOBILE)
						{
							return true;
						}
					}
				}
			}
		}

		return false;
	}
}
