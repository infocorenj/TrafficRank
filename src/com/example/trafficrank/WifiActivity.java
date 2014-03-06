package com.example.trafficrank;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.example.trafficrank.MainActivity.UpdateTask;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.TrafficStats;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class WifiActivity extends Activity
{
	private List<AppInfo> mlistAppInfo = null;
	private ListView list = null;
	private ProgressDialog progressDialog = null;
	private Button bt_3g;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wifi);
		
		//updateTraffic();	
		progressDialog = ProgressDialog.show(WifiActivity.this, "请稍等...", "数据加载中...", true);
		UpdateTask task = new UpdateTask();
		task.execute(new String[]{});
		
		bt_3g = (Button)findViewById(R.id.button_3g);
		bt_3g.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	//异步刷新流量数据
	class UpdateTask extends AsyncTask<String, Integer, String> 
	{			  
	    @Override  
	    protected String doInBackground(String... params) 
	    {		    	
	    	updateTraffic();
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
	        	 progressDialog.dismiss();	             
	             showTraffic();
	             Toast.makeText(WifiActivity.this, "数据刷新完成", Toast.LENGTH_LONG).show();
	         }
	         else 
	         {  
	             Toast.makeText(WifiActivity.this, "数据刷新失败", Toast.LENGTH_LONG).show();  
	         }  
	     }  
	       
	     protected void onPreExecute () 
	     {
	    	 //在 doInBackground(Params...)之前被调用，在ui线程执行  	         
	         //mProgressBar.setProgress(0);//进度条复位  
	     }  
	       
	     protected void onCancelled () 
	     {
	    	 //在ui线程执行  
	         //mProgressBar.setProgress(0);//进度条复位  
	     }        
	}  
	
	//更新所有app的当前流量数据至数据库
	public void updateTraffic()
	{
		//处理后台执行的任务，在后台线程执行  
    	List<AppInfo> listAppInfo = getAllInstalledAppsUID();
	
		for(AppInfo appInfo : listAppInfo)
		{
			int uid = appInfo.getAppUid();
			
			//打开数据库		
			SQLiteDatabase db = openOrCreateDatabase("test.db",  Context.MODE_PRIVATE, null); 				
			
			//开机到现在的流量
			long since_boot = TrafficStats.getUidRxBytes(uid) + TrafficStats.getUidTxBytes(uid);	
			if(since_boot < 0)
				since_boot = 0;		
			
			//从数据库中读取想应数据
			long total = 0;
			long wifi_1 = 0;		
			long wifi_total = 0;
			long last_total = 0;
			long flag = -1;
			
			Cursor c = db.rawQuery("SELECT * FROM traffic WHERE uid = ?", new String[]{String.valueOf(uid)});
			while (c.moveToNext()) 
			{  
				wifi_1 = c.getInt(c.getColumnIndex("wifi_1"));			
				wifi_total = c.getInt(c.getColumnIndex("wifi_total"));
				last_total = c.getInt(c.getColumnIndex("last_total"));
				flag = c.getInt(c.getColumnIndex("flag"));
				break;
			}
					
			total = last_total + since_boot;
			
			//存入相应的值
			ContentValues cv = new ContentValues();  	
			cv.put("total", total);		
			
			//3G流量
			long shujuTraffic = 0;
			
			//如果当前wifi已关闭
			if(flag == 0)
			{
				shujuTraffic = total - wifi_total;			
			}
			else 
			{
				if(since_boot - wifi_1 < 0)
				{
					shujuTraffic = total - wifi_total;				
					cv.put("wifi_1", 0);								 
				}
				else 
				{
					shujuTraffic = total - wifi_total - (since_boot - wifi_1);
					cv.put("wifi_1", since_boot);
				}		
			}	
			
			cv.put("shuju_traffic", shujuTraffic);
			
			//wifi流量
			double wifiTraffic = (double)total - shujuTraffic;		
			
			cv.put("wifi_total", wifiTraffic);
			
			db.update("traffic", cv, "uid = ?", new String[]{String.valueOf(uid)});								
		}	      		
	}
	
	//获取所有已经安装的app的信息
	public List<AppInfo>  getAllInstalledAppsUID()
	{
		List<AppInfo> listAppInfo = new ArrayList<AppInfo>();
		
		PackageManager  pm = getPackageManager();
		List<PackageInfo> packinfos = pm
                .getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES
                        | PackageManager.GET_PERMISSIONS);				
			
		for (PackageInfo info : packinfos) 
		{
			AppInfo appInfo = new AppInfo();			
			appInfo.setAppUid(info.applicationInfo.uid);
									
			listAppInfo.add(appInfo);
		}
		
		return listAppInfo;
	}
	
	//在界面上显示流量
	public void showTraffic()
	{
		//绑定Layout里面的ListView  
       	list = (ListView) findViewById(R.id.ListView_wifi);  
        mlistAppInfo = new ArrayList<AppInfo>();        
        mlistAppInfo = getInstalledApps();              	
        
        // 查询所有应用程序信息
        BrowseApplicationInfoAdapter browseAppAdapter = new BrowseApplicationInfoAdapter(
                this, mlistAppInfo);
        
        list.setAdapter(browseAppAdapter); 
        		
	}
	
	//更新所有appInfo，加入wifi流量、icon、label
	public List<AppInfo>  getInstalledApps()
	{
		List<AppInfo> listAppInfo = new ArrayList<AppInfo>();
		
		PackageManager  pm = getPackageManager();
		List<PackageInfo> packinfos = pm
                .getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES
                        | PackageManager.GET_PERMISSIONS);				
			
		for (PackageInfo info : packinfos) 
		{
			AppInfo appInfo = new AppInfo();
			appInfo.setAppIcon(info.applicationInfo.loadIcon(pm));
			appInfo.setAppUid(info.applicationInfo.uid);
			
			//根据UID 读取数据库中的3G流量
			int traffic = getTrafficOfUid(info.applicationInfo.uid);
			appInfo.setAppTraffic(String.format("%.2f", (traffic/1024.0/1024.0)));	
						
			String appName = info.applicationInfo.loadLabel(pm).toString();			
			if(appName.length() > 4)
			{
				appInfo.setAppLabel(appName.substring(0, 4) + "...");
			}
			else 
			{
				appInfo.setAppLabel(appName);
			}
			if(traffic >= 0)
				listAppInfo.add(appInfo);
		}
		
		ComparatorUser comparator=new ComparatorUser();
		Collections.sort(listAppInfo, comparator);
		  
		return listAppInfo;
	}
	
	//比较流量大小
	class ComparatorUser implements Comparator
	{
		public int compare(Object arg0, Object arg1) 
		{
			AppInfo user0 = (AppInfo) arg0;
			AppInfo user1 = (AppInfo) arg1;
			
			int flag = user1.getAppTraffic().compareTo(user0.getAppTraffic());
					
			return flag;		
		}	 
	} 
	
	//读取某一UID的wifi流量
	public int getTrafficOfUid(int uid)
	{
		int wifi_traffic = 0;
		
		try 
		{
			//打开数据库		
			SQLiteDatabase db = openOrCreateDatabase("test.db",  Context.MODE_PRIVATE, null); 
		
			Cursor c = db.rawQuery("SELECT * FROM traffic WHERE uid = ?", new String[]{String.valueOf(uid)});
			while (c.moveToNext()) 
			{  
				wifi_traffic = c.getInt(c.getColumnIndex("wifi_total"));						
				break;
			}
		} 
		catch (Exception e)
		{
			// TODO: handle exception
		}
				
		return wifi_traffic;
	}
}