package com.bn.tour;

import org.json.JSONException;
import org.json.JSONObject;

import com.bn.thread.DownloadPicThread;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;
import android.widget.TextView;

public class PushDetailsActivity extends Activity
{
	private static Handler handler = new Handler()
	{

		@Override
		public void handleMessage(Message msg)
		{
			// TODO Auto-generated method stub
			super.handleMessage(msg);
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pushdetails);
		try
		{
			JSONObject pushinfo = new JSONObject(getIntent().getStringExtra(
					"PUSHINFO"));
			ImageView imv = (ImageView) this.findViewById(R.id.IMVpush_pic);
			DownloadPicThread loadThread;

			loadThread = new DownloadPicThread(pushinfo.getString("push_pic"),
					imv, handler);
			loadThread.start();

			TextView tv = (TextView) this.findViewById(R.id.TVpush_intro);
			tv.setText(pushinfo.getString("push_intro"));
		}
		catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
