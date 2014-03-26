package com.example.trafficrank;

import java.util.List;

import com.bileisoft.rank.AppInfo;
import com.bileisoft.rank.TrafficRank;

import android.app.Activity;
import android.app.ProgressDialog;
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
	private TrafficRank trafficRank;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wifi);
		this.trafficRank = new TrafficRank(this, "test.db", "traffic", "sp");
		
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
	
	//�첽ˢ����������
	class UpdateTask extends AsyncTask<String, Integer, String> 
	{			  
	    @Override  
	    protected String doInBackground(String... params) 
	    {		    	
	    	mlistAppInfo = trafficRank.getTrafficDatas(0);
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
	             Toast.makeText(WifiActivity.this, "����ˢ�����", Toast.LENGTH_LONG).show();
	         }
	         else 
	         {  
	             Toast.makeText(WifiActivity.this, "����ˢ��ʧ��", Toast.LENGTH_LONG).show();  
	         }  
	     }  
	       
	     protected void onPreExecute () 
	     {
	    	 //�� doInBackground(Params...)֮ǰ�����ã���ui�߳�ִ��  	         
	         //mProgressBar.setProgress(0);//��������λ  
	    	 progressDialog = ProgressDialog.show(WifiActivity.this, "���Ե�...", "���ݼ�����...", true);
	     }  
	       
	     protected void onCancelled () 
	     {
	    	 //��ui�߳�ִ��  
	         //mProgressBar.setProgress(0);//��������λ  
	     }        
	}  
		
	//�ڽ�������ʾ����
	public void showTraffic()
	{
		//��Layout�����ListView  
       	list = (ListView) findViewById(R.id.ListView_wifi);  
    
        // ��ѯ����Ӧ�ó�����Ϣ
        BrowseApplicationInfoAdapter browseAppAdapter = new BrowseApplicationInfoAdapter(
                this, mlistAppInfo);
        
        list.setAdapter(browseAppAdapter);        		
	}
}