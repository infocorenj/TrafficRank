package com.example.trafficrank;

import java.util.List;

import android.R.integer;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Util
{
	public static boolean isWifiAvailable(Context context)throws Exception
	{
		boolean isWifiAvailable = false;
		
		ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifiNetworkInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
							
		if(wifiNetworkInfo != null && wifiNetworkInfo.isAvailable())
		{
			if(wifiNetworkInfo.isConnected())
			{
				isWifiAvailable = true;
			}
			else
			{
				
			}
		}
		else
		{
			
		}
		
		return isWifiAvailable;
	}
	
	/** 
     * 用来判断服务是否运行. 
     * @param context 
     * @param className 判断的服务名字 
     * @return true 在运行 false 不在运行 
     */
	public static boolean isServiceRunning(Context mContext, String className) {
		boolean isRunning = false;
		ActivityManager activityManager = (ActivityManager) mContext
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningServiceInfo> serviceList = activityManager
				.getRunningServices(30);
		if (!(serviceList.size() > 0)) {
			return false;
		}
		for (int i = 0; i < serviceList.size(); i++) {
			if (serviceList.get(i).service.getClassName().equals(className) == true) {
				isRunning = true;
				break;
			}
		}
		return isRunning;
	}
}