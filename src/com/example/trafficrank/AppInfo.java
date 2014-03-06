package com.example.trafficrank;

import android.graphics.drawable.Drawable;

public class AppInfo 
{  
    private Drawable appIcon ;  
    private String appTraffic;
    private String appLabel;
    private int appUid;
    
    public AppInfo(){}
    
   
    public Drawable getAppIcon() 
    {
        return appIcon;
    }
    
    public void setAppIcon(Drawable appIcon) 
    {
        this.appIcon = appIcon;
    }  
    
    public String getAppTraffic()
    {
    	return appTraffic;
    }
    
    public void setAppTraffic(String appTraffic)
    {
    	this.appTraffic = appTraffic;
    }
    
    public String getAppLabel()
    {
    	return appLabel;
    }
    
    public void setAppLabel(String appLabel)
    {
    	this.appLabel = appLabel;
    }
    
    public int getAppUid()
    {
    	return appUid;
    }
    
    public void setAppUid(int uid)
    {
    	this.appUid = uid;
    }
}