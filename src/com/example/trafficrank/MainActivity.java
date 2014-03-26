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
	
	//�ڽ�������ʾ����
	public void showTraffic()
	{
		//��Layout�����ListView  
       	list = (ListView) findViewById(R.id.ListView);                 	
        
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
	
	//������ʱͳ�Ʒ���
	public void startTrafficService()
	{
		Intent serviceIntent = new Intent(this, trafficService.class);				
		startService(serviceIntent);	
	}
	
	//�첽ˢ����������
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
	    	//�ڵ���publishProgress֮�󱻵��ã���ui�߳�ִ��  
	        //mProgressBar.setProgress(progress[0]);//���½������Ľ���  
	    }  

	     protected void onPostExecute(String result) 
	     {
	    	 //��̨����ִ����֮�󱻵��ã���ui�߳�ִ��  
	         if(result != null)
	         {  	        	 
	        	 showTraffic();
	        	 progressDialog.dismiss();
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
	    	 progressDialog = ProgressDialog.show(MainActivity.this, "���Ե�...", "���ݼ�����...", true);
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
	    	trafficRank.initDatabase();
	    	mlistAppInfo = trafficRank.getTrafficDatas(1);  
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
	             showTraffic();
	             progressDialog.dismiss();	  
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
	    	 progressDialog = ProgressDialog.show(MainActivity.this, "���Ե�...", "���ݿ��ʼ����...", true);
	     }  
	       
	     protected void onCancelled () 
	     {
	    	 //��ui�߳�ִ��  
	         //mProgressBar.setProgress(0);//��������λ  
	     }        
	} 
}
