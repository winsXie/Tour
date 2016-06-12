package com.bn.plan;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bn.message.LeaveMessageActivity;
import com.bn.thread.ImgAsyncDownload;
import com.bn.tour.R;
import com.bn.user.LookUserInfoActivity;

public class MatchPlanActivity extends Activity
{
	private ListView listview;
	private JSONObject json_matchplan;//存储匹配计划的JSONObject
	private JSONArray jarr_matchplan;//根据JSONObject得到的JSONArray（adapter的数据源）
	private ImgAsyncDownload imgdownload;//异步加载图片线程
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_matchplan);

		String matchplan=getIntent().getStringExtra("MATCHPLAN");
		if(matchplan!=null)
		{
			json_matchplan=JSONObject.fromObject(matchplan);
			jarr_matchplan=json_matchplan.getJSONArray("results");
		}
		else
		{
			Toast.makeText(this, "数据加载失败，请稍后重试！", Toast.LENGTH_LONG).show();
		}
		
		listview=(ListView) this.findViewById(R.id.LVmatch_plan);
		matchPlanAdapter adapter=new matchPlanAdapter();
		listview.setAdapter(adapter);
		
		listview.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3)
			{
				// TODO Auto-generated method stub
				Intent intent=new Intent(MatchPlanActivity.this,PlanDetailsActivity.class);
				intent.putExtra("READTYPE", true);
				intent.putExtra("PLANID", jarr_matchplan.getJSONObject(arg2).getString("plan_id"));
				startActivity(intent);
			}
		});
	}

	private class matchPlanAdapter extends BaseAdapter
	{
		ViewHolder viewholder;

		@Override
		public int getCount()
		{
			// TODO Auto-generated method stub
			return jarr_matchplan.size();
		}

		@Override
		public Object getItem(int position)
		{
			// TODO Auto-generated method stub
			return jarr_matchplan.get(position);
		}

		@Override
		public long getItemId(int position)
		{
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent)
		{
			final JSONObject json_temp = jarr_matchplan.getJSONObject(position);
			if (convertView == null)
			{
				convertView = getLayoutInflater().inflate(
						R.layout.item_my_matchplan, null);
				viewholder = new ViewHolder();

				viewholder.imvHeadimg = (ImageView) convertView
						.findViewById(R.id.IMVmatchplan_head);
				viewholder.imvSex = (ImageView) convertView
						.findViewById(R.id.IMVmatchplan_sex);

				viewholder.tvUserName = (TextView) convertView
						.findViewById(R.id.TVmatchplan_uname);
				viewholder.tvAge = (TextView) convertView
						.findViewById(R.id.TVmatchplan_uage);
				viewholder.tvTitle = (TextView) convertView
						.findViewById(R.id.TVmatchplan_plan_title);
				viewholder.tvStaPlace = (TextView) convertView
						.findViewById(R.id.TVmatchplan_plan_staplace);
				viewholder.tvEndPlace = (TextView) convertView
						.findViewById(R.id.TVmatchplan_plan_endplace);
				viewholder.tvStaDate = (TextView) convertView
						.findViewById(R.id.TVmatchplan_plan_stadate);
				viewholder.tvTogether = (TextView) convertView
						.findViewById(R.id.TVmatchplan_plan_together);

				convertView.setTag(viewholder);
			}
			else
			{
				viewholder = (ViewHolder) convertView.getTag();
			}

			
			if (json_temp.getString("user_sex").equals("男"))
			{
				viewholder.imvSex.setImageResource(R.drawable.sex_man);
			}
			else
			{
				viewholder.imvSex.setImageResource(R.drawable.sex_woman);
			}

			viewholder.tvUserName.setText(json_temp.getString("user_name"));
			viewholder.tvAge.setText(json_temp.getString("user_age") + "岁");
			viewholder.tvTitle.setText(json_temp.getString("plan_title"));
			viewholder.tvStaPlace.setText(json_temp.getString("plan_startplace"));
			viewholder.tvEndPlace.setText(json_temp.getString("plan_endplace"));
			viewholder.tvStaDate.setText(json_temp.getString("plan_startdate")+" 出发");

			imgdownload = ImgAsyncDownload.getInstance();
			if (imgdownload != null)
			{
				viewholder.imvHeadimg.setTag(json_temp.getString("user_image"));
				viewholder.imvHeadimg.setImageResource(R.drawable.defaultimage);
				imgdownload.imageDownload( viewholder.imvHeadimg);
			}
			
			viewholder.imvHeadimg.setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View v)
				{
					Intent intent = new Intent(MatchPlanActivity.this, LookUserInfoActivity.class);
					intent.putExtra("USERID", json_temp.getString("plan_userid"));
					startActivity(intent);
				}
			});
			
			viewholder.tvTogether.setOnClickListener(new OnClickListener()
			{
				
				@Override
				public void onClick(View v)
				{
					// TODO Auto-generated method stub
					Intent intent=new Intent(MatchPlanActivity.this,LeaveMessageActivity.class);
					intent.putExtra("ACCEPTUID", json_temp.getString("plan_userid"));
					System.out.println(json_temp.getString("plan_userid"));
					startActivity(intent);
				}
			});
			

			return convertView;
		}

		class ViewHolder
		{
			TextView tvUserName, tvAge, tvTitle, tvStaPlace, tvEndPlace,
					tvStaDate, tvTogether;
			ImageView imvHeadimg, imvSex;
		}

	}

}
