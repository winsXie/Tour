package com.bn.tour;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class SplashActivity extends Activity
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);

		new Handler().postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				Intent intent = new Intent(SplashActivity.this,
						ButtomFrame.class);
				SplashActivity.this.startActivity(intent);
				SplashActivity.this.finish();
			}
		}, 3000);// 3000为间隔的时间-毫秒

	}
}