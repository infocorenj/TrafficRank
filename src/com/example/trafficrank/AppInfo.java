package com.example.trafficrank;

import android.R.integer;
import android.graphics.drawable.Drawable;

public class AppInfo 
{  
    private Drawable appIcon ;  
    private String appTraffic;//用于显示
    private String appLabel;
    private int appUid;
    private int traffic;//用于比较
    
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
    
    public int getTraffic()
    {
    	return traffic;
    }
    
    public void setTraffic(int traffic)
    {
    	this.traffic = traffic;
    }
}