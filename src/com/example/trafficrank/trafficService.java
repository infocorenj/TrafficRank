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
 * Android Service 示例
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
		        // 要做的事情
		        super.handleMessage(msg);
		        
		        try
		        {			        	
					updateTraffic();
					notification("TrafficRank has updated", "更新流量");
				} 
		        catch (Exception e)
		        {
					// TODO: handle exception
		        	notification("TrafficRank update error", "更新流量");
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
        // 创建一个Notification  
        Notification notification = new Notification();  
        // 设置显示在手机最上边的状态栏的图标  
        notification.icon = R.drawable.ic_launcher;  
        // 当当前的notification被放到状态栏上的时候，提示内容  
        notification.tickerText = text;  
          
        /*** 
         * notification.contentIntent:一个PendingIntent对象，当用户点击了状态栏上的图标时，该Intent会被触发 
         * notification.contentView:我们可以不在状态栏放图标而是放一个view 
         * notification.deleteIntent 当当前notification被移除时执行的intent 
         * notification.vibrate 当手机震动时，震动周期设置 
         */  
        // 添加声音提示  
        //notification.defaults=Notification.DEFAULT_SOUND;  
        // audioStreamType的值必须AudioManager中的值，代表着响铃的模式  
        notification.audioStreamType= android.media.AudioManager.ADJUST_LOWER;  
          
        //下边的两个方式可以添加音乐  
        //notification.sound = Uri.parse("file:///sdcard/notification/ringer.mp3");   
        //notification.sound = Uri.withAppendedPath(Audio.Media.INTERNAL_CONTENT_URI, "6");   
        Intent intent = new Intent(this, MainActivity.class);  
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);  
        // 点击状态栏的图标出现的提示信息设置  
        notification.setLatestEventInfo(this, "内容提示：", tishi, pendingIntent);  
        manager.notify(1, notification);  
	}
	
	//更新当前数据
	public void updateTraffic()throws Exception
	{						
		//打开数据库		
		SQLiteDatabase db = openOrCreateDatabase("test.db",  Context.MODE_PRIVATE, null); 				
		Cursor cursor = db.rawQuery("SELECT * FROM traffic", new String[]{});
				
		while (cursor.moveToNext()) 
		{  
			int uid = cursor.getInt(cursor.getColumnIndex("uid"));
			
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
}
