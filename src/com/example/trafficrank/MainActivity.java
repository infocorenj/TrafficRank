package com.example.trafficrank;

import java.util.ArrayList;
import java.util.List;

import com.bileisoft.rank.AppInfo;
import com.bileisoft.rank.TrafficRank;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity 
{
	private List<AppInfo> mlistAppInfo = null;
	private ListView list = null;
    private ProgressDialog progressDialog = null;
    private Button bt_wifi;
	private TrafficRank trafficRank ;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mlistAppInfo = new ArrayList<AppInfo>();  
		
		bt_wifi = (Button) findViewById(R.id.button_wifi);
		bt_wifi.setOnClickListener(new View.OnClickListener() 
		{			
			@Override
			public void onClick(View v) 
			{
				// TODO Auto-generated method stub
				Intent intent = new Intent(MainActivity.this, WifiActivity.class);
				startActivity(intent);
			}
		});
				
		boolean isInit = false;
		
		try 
		{
			trafficRank = new TrafficRank(this, "test.db", "traffic", "sp");
			isInit = trafficRank.getTrafficRankDBInit();
			
			if (!isInit) 
			{			
				InitDatabaseTask task = new InitDatabaseTask();
				task.execute(new String[]{});			
			}
			else
			{						
				UpdateTask task = new UpdateTask();
				task.execute(new String[]{});								 				
			}					
			
		} 
		catch (Exception e)
		{
			// TODO: handle exception
		}			
	}
	
	//在界面上显示流量
	public void showTraffic()
	{
		//绑定Layout里面的ListView  
       	list = (ListView) findViewById(R.id.ListView);                 	
        
        // 查询所有应用程序信息
        BrowseApplicationInfoAdapter browseAppAdapter = new BrowseApplicationInfoAdapter(
                this, mlistAppInfo);
        
        list.setAdapter(browseAppAdapter); 
        
		// 开启服务
		if (!Util.isServiceRunning(this,"com.example.trafficrank.trafficService")) 
		{
			startTrafficService();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	//开启定时统计服务
	public void startTrafficService()
	{
		Intent serviceIntent = new Intent(this, trafficService.class);				
		startService(serviceIntent);	
	}
	
	//异步刷新流量数据
	class UpdateTask extends AsyncTask<String, Integer, String> 
	{			  
	    @Override  
	    protected String doInBackground(String... params) 
	    {		    	
	    	mlistAppInfo = trafficRank.getTrafficDatas(1);
	        return "";  
	    }  
	      
	    protected void onProgressUpdate(Integer... progress) 
	    {
	    	//在调用publishProgress之后被调用，在ui线程执行  
	        //mProgressBar.setProgress(progress[0]);//更新进度条的进度  
	    }  

	     protected void onPostExecute(String result) 
	     {
	    	 //后台任务执行完之后被调用，在ui线程执行  
	         if(result != null)
	         {  	        	 
	        	 showTraffic();
	        	 progressDialog.dismiss();
	             Toast.makeText(MainActivity.this, "数据刷新完成", Toast.LENGTH_LONG).show();
	         }
	         else 
	         {  
	             Toast.makeText(MainActivity.this, "数据刷新失败", Toast.LENGTH_LONG).show();  
	         }  
	     }  
	       
	     protected void onPreExecute () 
	     {
	    	 //在 doInBackground(Params...)之前被调用，在ui线程执行  	         
	         //mProgressBar.setProgress(0);//进度条复位 
	    	 progressDialog = ProgressDialog.show(MainActivity.this, "请稍等...", "数据加载中...", true);
	     }  
	       
	     protected void onCancelled () 
	     {
	    	 //在ui线程执行  
	         //mProgressBar.setProgress(0);//进度条复位  
	     }        
	}  
	
	//异步处理数据库初始化
	class InitDatabaseTask extends AsyncTask<String, Integer, String> 
	{			  
	    @Override  
	    protected String doInBackground(String... params) 
	    {		    	
	    	trafficRank.initDatabase();
	    	mlistAppInfo = trafficRank.getTrafficDatas(1);  
	        return "";  
	    }  
	      
	    protected void onProgressUpdate(Integer... progress) 
	    {
	    	//在调用publishProgress之后被调用，在ui线程执行  
	        //mProgressBar.setProgress(progress[0]);//更新进度条的进度  
	    }  

	     protected void onPostExecute(String result) 
	     {
	    	 //后台任务执行完之后被调用，在ui线程执行  
	         if(result != null)
	         {  	        	    	            
	             showTraffic();
	             progressDialog.dismiss();	  
	             Toast.makeText(MainActivity.this, "数据库初始化完成", Toast.LENGTH_LONG).show();
	         }
	         else 
	         {  
	             Toast.makeText(MainActivity.this, "数据库初始化失败", Toast.LENGTH_LONG).show();  
	         }  
	     }  
	       
	     protected void onPreExecute () 
	     {
	    	 //在 doInBackground(Params...)之前被调用，在ui线程执行  	         
	         //mProgressBar.setProgress(0);//进度条复位  
	    	 progressDialog = ProgressDialog.show(MainActivity.this, "请稍等...", "数据库初始化中...", true);
	     }  
	       
	     protected void onCancelled () 
	     {
	    	 //在ui线程执行  
	         //mProgressBar.setProgress(0);//进度条复位  
	     }        
	} 
}
