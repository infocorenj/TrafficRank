package com.example.trafficrank;

import com.bileisoft.rank.TrafficRank;

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
				TrafficRank trafficRank = new TrafficRank(context, "test.db", "traffic", "sp");
				trafficRank.checkShutdown();
				
				if(!Util.isServiceRunning(context, "com.example.trafficrank.trafficService"))
				{
					Intent newIntent = new Intent(context, trafficService.class);					
					context.startService(newIntent);
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
}