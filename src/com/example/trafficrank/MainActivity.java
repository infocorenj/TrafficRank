package com.example.trafficrank;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import android.net.TrafficStats;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity 
{
	private List<AppInfo> mlistAppInfo = null;
	private ListView list = null;
	private SharedPreferences sp;
	private Editor editor;  
	//private ProgressBar mProgressBar;	
	//private Handler handler = new Handler();
    private ProgressDialog progressDialog = null;
    private Button bt_wifi;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//�־û�����
		sp = this.getSharedPreferences("SP", MODE_PRIVATE);
		editor = sp.edit();
		
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
		
		//�жϲ���ʾ
		boolean isInit = sp.getBoolean("isInit", false);
		if (!isInit) 
		{
			progressDialog = ProgressDialog.show(MainActivity.this, "���Ե�...", "���ݿ��ʼ����...", true);
			InitDatabaseTask task = new InitDatabaseTask();
			task.execute(new String[]{});
			
			editor.putBoolean("isInit", true);													
			editor.commit();	
		}
		else
		{
			//updateTraffic();	
			progressDialog = ProgressDialog.show(MainActivity.this, "���Ե�...", "���ݼ�����...", true);
			UpdateTask task = new UpdateTask();
			task.execute(new String[]{});			
		}					
	}
	
	//�ڽ�������ʾ����
	public void showTraffic()
	{
		//��Layout�����ListView  
       	list = (ListView) findViewById(R.id.ListView);  
        mlistAppInfo = new ArrayList<AppInfo>();        
        mlistAppInfo = getInstalledApps();              	
        
        // ��ѯ����Ӧ�ó�����Ϣ
        BrowseApplicationInfoAdapter browseAppAdapter = new BrowseApplicationInfoAdapter(
                this, mlistAppInfo);
        
        list.setAdapter(browseAppAdapter); 
        
		// ��������
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
	
	//��ȡ�����Ѿ���װ��app����Ϣ
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
	
	//��������appInfo������������icon��label
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
			
			//����UID ��ȡ���ݿ��е�3G����
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
	
	//������ʱͳ�Ʒ���
	public void startTrafficService()
	{
		Intent serviceIntent = new Intent(this, trafficService.class);				
		startService(serviceIntent);	
	}
	
	//��ʼ�����ݿ�
	public void initDatabase()
	{
		try 
		{
			SQLiteDatabase db = openOrCreateDatabase("test.db", Context.MODE_PRIVATE, null);  
			db.execSQL("DROP TABLE IF EXISTS traffic");
			//����traffic��
			db.execSQL("CREATE TABLE traffic (id INTEGER PRIMARY KEY AUTOINCREMENT,  uid INTEGER, " +
					"wifi_1 INTEGER, wifi_2 INTEGER, wifi_total INTEGER, last_total INTEGER," +
					"since_boot INTEGER, total INTEGER, flag INTEGER, shuju_traffic INTEGER )");
			
			//��ȡ����app��UID
			List<AppInfo> listAppInfo = getAllInstalledAppsUID();
			boolean isWifiAlive = Util.isWifiAvailable(MainActivity.this);
			
			for(int i = 0; i < listAppInfo.size(); i++)
			{
				AppInfo appInfo = listAppInfo.get(i);
				
				int uid = appInfo.getAppUid();
				//����������������ֻͳ�����ڿ�ʼ��������֮ǰ�Ĳ���
				long since_boot = TrafficStats.getUidRxBytes(uid) + TrafficStats.getUidTxBytes(uid);	
				//��ֵ����Ϊ-2����Ҫ�ж�
				if(since_boot < 0 )
					since_boot = 0;
				
				//���ó�ʼֵ
				ContentValues values = new ContentValues();	
				values.put("id", i+1);
				values.put("uid", uid);
				values.put("wifi_1", since_boot);
				values.put("wifi_2", -1);
				values.put("wifi_total", since_boot);
				values.put("last_total", 0);
				values.put("since_boot", 0);
				values.put("total", 0);
				
				if(isWifiAlive)
				{
					values.put("flag", 1);
				}
				else
				{
					values.put("flag", 0);
				}
				
				values.put("shuju_traffic", 0);
				
				db.insert("traffic", null, values);	
			}					
		} 
		catch (Exception e) 
		{
			// TODO: handle exception
			Toast toast = Toast.makeText(getApplicationContext(),
				     "���ݿ��ʼ��ʧ�� ��", Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
			
			editor.putBoolean("isInit", false);													
			editor.commit();
		}					
	}
	
	//��������app�ĵ�ǰ�������������ݿ�
	public void updateTraffic()
	{
		//�����ִ̨�е������ں�̨�߳�ִ��  
    	List<AppInfo> listAppInfo = getAllInstalledAppsUID();
	
		for(AppInfo appInfo : listAppInfo)
		{
			int uid = appInfo.getAppUid();
			
			//�����ݿ�		
			SQLiteDatabase db = openOrCreateDatabase("test.db",  Context.MODE_PRIVATE, null); 				
			
			//���������ڵ�����
			long since_boot = TrafficStats.getUidRxBytes(uid) + TrafficStats.getUidTxBytes(uid);	
			if(since_boot < 0)
				since_boot = 0;		
			
			//�����ݿ��ж�ȡ��Ӧ����
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
			
			//������Ӧ��ֵ
			ContentValues cv = new ContentValues();  	
			cv.put("total", total);		
			
			//3G����
			long shujuTraffic = 0;
			
			//�����ǰwifi�ѹر�
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
			
			//wifi����
			double wifiTraffic = (double)total - shujuTraffic;		
			
			cv.put("wifi_total", wifiTraffic);
			
			db.update("traffic", cv, "uid = ?", new String[]{String.valueOf(uid)});								
		}	      		
	}
	
	//��ȡĳһUID��3G����
	public int getTrafficOfUid(int uid)
	{
		int shuju_traffic = 0;
		
		try 
		{
			//�����ݿ�		
			SQLiteDatabase db = openOrCreateDatabase("test.db",  Context.MODE_PRIVATE, null); 
		
			Cursor c = db.rawQuery("SELECT * FROM traffic WHERE uid = ?", new String[]{String.valueOf(uid)});
			while (c.moveToNext()) 
			{  
				shuju_traffic = c.getInt(c.getColumnIndex("shuju_traffic"));						
				break;
			}
		} 
		catch (Exception e)
		{
			// TODO: handle exception
		}
				
		return shuju_traffic;
	}
	
	//�첽ˢ����������
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
	    	//�ڵ���publishProgress֮�󱻵��ã���ui�߳�ִ��  
	        //mProgressBar.setProgress(progress[0]);//���½������Ľ���  
	    }  

	     protected void onPostExecute(String result) 
	     {
	    	 //��̨����ִ����֮�󱻵��ã���ui�߳�ִ��  
	         if(result != null)
	         {  
	        	 progressDialog.dismiss();	             
	             showTraffic();
	             Toast.makeText(MainActivity.this, "����ˢ�����", Toast.LENGTH_LONG).show();
	         }
	         else 
	         {  
	             Toast.makeText(MainActivity.this, "����ˢ��ʧ��", Toast.LENGTH_LONG).show();  
	         }  
	     }  
	       
	     protected void onPreExecute () 
	     {
	    	 //�� doInBackground(Params...)֮ǰ�����ã���ui�߳�ִ��  	         
	         //mProgressBar.setProgress(0);//��������λ  
	     }  
	       
	     protected void onCancelled () 
	     {
	    	 //��ui�߳�ִ��  
	         //mProgressBar.setProgress(0);//��������λ  
	     }        
	}  
	
	//�첽�������ݿ��ʼ��
	class InitDatabaseTask extends AsyncTask<String, Integer, String> 
	{			  
	    @Override  
	    protected String doInBackground(String... params) 
	    {		    	
	    	initDatabase();
	        return "";  
	    }  
	      
	    protected void onProgressUpdate(Integer... progress) 
	    {
	    	//�ڵ���publishProgress֮�󱻵��ã���ui�߳�ִ��  
	        //mProgressBar.setProgress(progress[0]);//���½������Ľ���  
	    }  

	     protected void onPostExecute(String result) 
	     {
	    	 //��̨����ִ����֮�󱻵��ã���ui�߳�ִ��  
	         if(result != null)
	         {  
	        	 progressDialog.dismiss();	             
	             showTraffic();
	             Toast.makeText(MainActivity.this, "���ݿ��ʼ�����", Toast.LENGTH_LONG).show();
	         }
	         else 
	         {  
	             Toast.makeText(MainActivity.this, "���ݿ��ʼ��ʧ��", Toast.LENGTH_LONG).show();  
	         }  
	     }  
	       
	     protected void onPreExecute () 
	     {
	    	 //�� doInBackground(Params...)֮ǰ�����ã���ui�߳�ִ��  	         
	         //mProgressBar.setProgress(0);//��������λ  
	     }  
	       
	     protected void onCancelled () 
	     {
	    	 //��ui�߳�ִ��  
	         //mProgressBar.setProgress(0);//��������λ  
	     }        
	}  
}

//�Ƚ�������С
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


