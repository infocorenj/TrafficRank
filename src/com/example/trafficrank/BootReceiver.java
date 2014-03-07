package com.example.trafficrank;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.TrafficStats;


public class BootReceiver extends BroadcastReceiver 
{	
	@Override
	public void onReceive(Context context, Intent intent) 
	{
		try 
		{
			if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) 
			{
					
			}
			else if(intent.getAction().equals(Intent.ACTION_USER_PRESENT))
			{
				if(!Util.isServiceRunning(context, "com.example.trafficrank.trafficService"))
				{
					Intent newIntent = new Intent(context, trafficService.class);					
					context.startService(newIntent);
					
					//checkShutdownCorrect(context);
				}
			}		
			else 
			{
				
			}
		} catch (Exception e) 
		{
			// TODO: handle exception
		}		
	}
	
	//非正常关机导致流量变少
	public void checkShutdownCorrect(Context context)
	{
		try 
		{
			SQLiteDatabase db = context.openOrCreateDatabase("test.db",  Context.MODE_PRIVATE, null);  
			Cursor cursor = db.rawQuery("SELECT * FROM traffic", new String[]{});
			
			long total = 0;
			long wifi_1 = 0;
			
			while(cursor.moveToNext())
			{
				int uid = cursor.getInt(cursor.getColumnIndex("uid"));
			
				wifi_1 = cursor.getInt(cursor.getColumnIndex("wifi_1"));	
				total = cursor.getInt(cursor.getColumnIndex("total"));	
				
				//开机到现在的流量
				long since_boot = 0;	
				try 
				{
					since_boot = TrafficStats.getUidRxBytes(uid) + TrafficStats.getUidTxBytes(uid);	
				} 
				catch (Exception e) 
				{
					since_boot = 0;
				}
				
				if(since_boot < 0)
					since_boot = 0;	
				
				//非正常关机
				if(since_boot - wifi_1 < 0)
				{
					//wifi_1 归零， flag 状态根据网络状态确定， last_total = total;				
					ContentValues cv = new ContentValues(); 
					cv.put("wifi_1", 0);
					cv.put("last_total", total);
					cv.put("flag", Util.isWifiAvailable(context));
					
					db.update("traffic", cv, "uid = ?", new String[]{String.valueOf(uid)});
				}				
			}	
		} 
		catch (Exception e)
		{
			// TODO: handle exception
		}
	}
}