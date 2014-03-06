package com.example.trafficrank;

import java.util.ArrayList;
import java.util.List;
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
		try 
		{
			List<AppInfo> listAppInfo = new ArrayList<AppInfo>();
			listAppInfo = getInstalledAppsUID(context);
			
			//�������״̬�����ı䣬�ж�wifi�Ƿ����			
			boolean isWifiAvailable = Util.isWifiAvailable(context);
			SQLiteDatabase db =  context.openOrCreateDatabase("test.db",  Context.MODE_PRIVATE, null);  
				
			for(AppInfo appInfo : listAppInfo)
			{
				int uid = appInfo.getAppUid();
									
				if(isWifiAvailable)
				{
					//����
					//��¼��ǰuidӦ�õ�����.
					long wifi_1 = TrafficStats.getUidRxBytes(uid) + TrafficStats.getUidTxBytes(uid);							
					if(wifi_1 <0 )
						wifi_1 = 0;
					
					ContentValues cv = new ContentValues();  
					cv.put("wifi_1", wifi_1);  
					cv.put("flag", 1);
					
					db.update("traffic", cv, "uid = ?", new String[]{String.valueOf(uid)});
				}
				else
				{
					//����ر�
					//���౾��wifi������ uidӦ�õ� ����				
					Cursor c = db.rawQuery("SELECT * FROM traffic WHERE uid = ?", new String[]{String.valueOf(uid)});
					
					long wifi_1 = 0;
					long wifi_total = 0;
					long last_total = 0;
					long flag = -1;
					
					while (c.moveToNext()) 
					{  
						wifi_1 = c.getInt(c.getColumnIndex("wifi_1"));
						wifi_total = c.getInt(c.getColumnIndex("wifi_total"));
						last_total =  c.getInt(c.getColumnIndex("last_total"));
						flag = c.getInt(c.getColumnIndex("flag"));
						break;
					}
					
					//�ж��Ƿ��Ǵ�wifi�л�����������
					if(flag == 1)
					{
						long wifi_2 = TrafficStats.getUidRxBytes(uid) + TrafficStats.getUidTxBytes(uid);	
						if(wifi_2 < 0)
							wifi_2 = 0;								 												
						
						ContentValues cv = new ContentValues(); 
						
						if(wifi_2 - wifi_1 < 0)
						{
							cv.put("wifi_total", 0 + wifi_total);
						}
						else 
						{
							cv.put("wifi_total", wifi_2 - wifi_1 + wifi_total);
						}
						cv.put("wifi_1", 0);
						cv.put("wifi_2", -1);
						cv.put("since_boot", 0);
						cv.put("total", last_total + wifi_2);
						cv.put("flag", 0);


						db.update("traffic", cv, "uid = ?", new String[]{String.valueOf(uid)});
					}
					else
					{
						
					}
				}
			}															
		} 
		catch (Exception e) 
		{
			// TODO: handle exception
			//��ʼ�����ݿ�
		}
    }
	
	//��ȡ�����Ѿ���װ��app����Ϣ
	public List<AppInfo>  getInstalledAppsUID(Context context)
	{
		List<AppInfo> listAppInfo = new ArrayList<AppInfo>();
		
		PackageManager  pm = context.getPackageManager();
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
}
