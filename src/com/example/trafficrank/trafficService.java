package com.example.trafficrank;

import java.util.Timer;
import java.util.TimerTask;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.TrafficStats;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

/**
 * Android Service ʾ��
 * 
 * @author dev
 * 
 */
public class trafficService extends Service
{	
	private final Timer timer = new Timer();
	private TimerTask task;	
	private Handler handler;
	private int uid;
	
	@Override
	public IBinder onBind(Intent intent) 
	{		
		return null;
	}
	
	@Override
	public void onCreate() 
	{		
		super.onCreate();
		
		handler = new Handler() 
		{
		    @Override
		    public void handleMessage(Message msg) {
		        // TODO Auto-generated method stub
		        // Ҫ��������
		        super.handleMessage(msg);
		        
		        try
		        {			        	
					updateTraffic();
					notification("TrafficRank has updated", "��������");
				} 
		        catch (Exception e)
		        {
					// TODO: handle exception
		        	notification("TrafficRank update error", "��������");
				}
		        
		    }
		};
	}
	
	@Override
	public void onStart(Intent intent, int startId) 
	{		
		super.onStart(intent, startId);
		
		try 
		{
			task = new TimerTask() 
			{  
			    @Override  
			    public void run() 
			    {  
			        // TODO Auto-generated method stub  
			        Message message = new Message();  
			        message.what = 1;  
			        handler.sendMessage(message);  
			    }  
			}; 	
			
			notification("TrafficRank service is running", "service is running in the background");							
			timer.schedule(task, 2000, 1000 * 3600); //3600
			
		} catch (Exception e) {
			// TODO: handle exception
		}						
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{		
		return super.onStartCommand(intent, flags, startId);
	}
	
	public void notification(String text, String tishi)
	{
		NotificationManager manager = (NotificationManager) this  
		        .getSystemService(Context.NOTIFICATION_SERVICE);  
        // ����һ��Notification  
        Notification notification = new Notification();  
        // ������ʾ���ֻ����ϱߵ�״̬����ͼ��  
        notification.icon = R.drawable.ic_launcher;  
        // ����ǰ��notification���ŵ�״̬���ϵ�ʱ����ʾ����  
        notification.tickerText = text;  
          
        /*** 
         * notification.contentIntent:һ��PendingIntent���󣬵��û������״̬���ϵ�ͼ��ʱ����Intent�ᱻ���� 
         * notification.contentView:���ǿ��Բ���״̬����ͼ����Ƿ�һ��view 
         * notification.deleteIntent ����ǰnotification���Ƴ�ʱִ�е�intent 
         * notification.vibrate ���ֻ���ʱ������������ 
         */  
        // ���������ʾ  
        //notification.defaults=Notification.DEFAULT_SOUND;  
        // audioStreamType��ֵ����AudioManager�е�ֵ�������������ģʽ  
        notification.audioStreamType= android.media.AudioManager.ADJUST_LOWER;  
          
        //�±ߵ�������ʽ�����������  
        //notification.sound = Uri.parse("file:///sdcard/notification/ringer.mp3");   
        //notification.sound = Uri.withAppendedPath(Audio.Media.INTERNAL_CONTENT_URI, "6");   
        Intent intent = new Intent(this, MainActivity.class);  
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);  
        // ���״̬����ͼ����ֵ���ʾ��Ϣ����  
        notification.setLatestEventInfo(this, "������ʾ��", tishi, pendingIntent);  
        manager.notify(1, notification);  
	}
	
	//���µ�ǰ����
	public void updateTraffic()throws Exception
	{						
		//�����ݿ�		
		SQLiteDatabase db = openOrCreateDatabase("test.db",  Context.MODE_PRIVATE, null); 				
		Cursor cursor = db.rawQuery("SELECT * FROM traffic", new String[]{});
				
		while (cursor.moveToNext()) 
		{  
			int uid = cursor.getInt(cursor.getColumnIndex("uid"));
			
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
}
