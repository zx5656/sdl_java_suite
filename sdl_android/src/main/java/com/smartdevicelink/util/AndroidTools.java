package com.smartdevicelink.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;

import com.smartdevicelink.transport.TransportConstants;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class AndroidTools {
	/**
	 * Check to see if a component is exported
	 * @param context object used to retrieve the package manager
	 * @param name of the component in question
	 * @return true if this component is tagged as exported
	 */
	public static boolean isServiceExported(Context context, ComponentName name) {
	    try {
	        ServiceInfo serviceInfo = context.getPackageManager().getServiceInfo(name, PackageManager.GET_META_DATA);
	        return serviceInfo.exported;
	    } catch (NameNotFoundException e) {
	        e.printStackTrace();
	    }
	    return false;
	}
	/**
	 * Get all SDL enabled apps. If the package name is null, it will return all apps. However, if the package name is included, the
	 * resulting hash map will not include the app with that package name.
	 * @param context
	 * @param myPackageName
	 * @return
	 */
	public static HashMap<String,ResolveInfo> getSdlEnabledApps(Context context, String myPackageName){
		Intent intent = new Intent(TransportConstants.START_ROUTER_SERVICE_ACTION);
		List<ResolveInfo> infos = context.getPackageManager().queryBroadcastReceivers(intent, 0);
		HashMap<String,ResolveInfo> sdlMultiList = new HashMap<String,ResolveInfo>();
		for(ResolveInfo info: infos){
			if(info.activityInfo.applicationInfo.packageName.equals(myPackageName)){
				continue; //Ignoring my own package
			}
			sdlMultiList.put(info.activityInfo.packageName, info);
		}
		return sdlMultiList;
	}

    /**
     * Sort a list of ResolveInfo (installed SDL enabled apps) objects by most recent update time of apps
     * @param pm PackageManager, used to resolve package info
     * @param list the list being sorted
     * @return the list if null or empty; otherwise, the list sorted
     */
	public static List<ResolveInfo> sortAppsByUpdateTime (final PackageManager pm, List<ResolveInfo> list) {
        if (list == null || list.isEmpty()) {
            return  list;
        }
        Collections.sort(list, new Comparator<ResolveInfo>() {
            @Override
            public int compare(ResolveInfo resolveInfo, ResolveInfo t1) {
                try {
                    PackageInfo thisPack = pm.getPackageInfo(resolveInfo.activityInfo.packageName, 0);
                    PackageInfo itPack = pm.getPackageInfo(t1.activityInfo.packageName, 0);
                    if (thisPack.lastUpdateTime < itPack.lastUpdateTime) {
                        return -1;
                    } else if (thisPack.lastUpdateTime > itPack.lastUpdateTime) {
                        return 1;
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                return 0;
            }
        });
        return list;
    }
}
