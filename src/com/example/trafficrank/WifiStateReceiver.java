package com.example.trafficrank;

import java.util.ArrayList;
import java.util.List;

import com.bileisoft.rank.TrafficRank;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.TrafficStats;

public class WifiStateReceiver extends BroadcastReceiver 
{
	@Override
	public void onReceive(Context context, Intent intent) 
	{
		TrafficRank trafficRank = new TrafficRank(context, "test.db", "traffic", "sp");
		trafficRank.updateForWifiStateChange();
	}
}
