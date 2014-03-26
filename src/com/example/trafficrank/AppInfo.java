package com.example.trafficrank;

import android.graphics.drawable.Drawable;

public class AppInfo 
{  
    private Drawable appIcon ;  
    private String appLabel;
    private int appUid;
    private int traffic;
    
    public AppInfo(){}
      
    public Drawable getAppIcon() 
    {
        return appIcon;
    }
    
    public void setAppIcon(Drawable appIcon) 
    {
        this.appIcon = appIcon;
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