package com.example.trafficrank;


import com.bileisoft.rank.TrafficRank;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


/**
 * 
 */
public class ShutDownReceiver extends BroadcastReceiver 
{
	@Override
	public void onReceive(Context context, Intent intent) 
	{	
		TrafficRank trafficRank = new TrafficRank(context, "test.db", "traffic", "sp");
		trafficRank.saveTrafficData();
	}
}
