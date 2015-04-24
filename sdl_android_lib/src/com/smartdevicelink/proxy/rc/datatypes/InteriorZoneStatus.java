package com.smartdevicelink.proxy.rc.datatypes;

import java.util.Hashtable;

import com.smartdevicelink.proxy.RPCStruct;

public class InteriorZoneStatus extends RPCStruct {
	
	public static final String KEY_ZONE		= "zone";
	public static final String KEY_DATA		= "data";
	
	
	/**
	 * Constructs a newly allocated InteriorZoneStatus object
	 */
	public InteriorZoneStatus() { }

	/**
	 * Constructs a newly allocated InteriorZoneStatus object indicated by the Hashtable parameter
	 * @param hash The Hashtable to use
	 */      
	public InteriorZoneStatus(Hashtable<String, Object> hash) {
		super(hash);
	}
	
	
	public InteriorZone getZone(){
		return (InteriorZone) store.get(KEY_ZONE);
	}

	public void setZone(InteriorZone zone){
		if (zone!=null) {
			store.put(KEY_ZONE, zone);
		} else {
			store.remove(KEY_ZONE);
		}
	}
	
	public InteriorData getInteriorData(){
		return (InteriorData) store.get(KEY_DATA);
	}

	public void setInteriorData(InteriorData data){
		if (data!=null) {
			store.put(KEY_DATA, data);
		} else {
			store.remove(KEY_DATA);
		}
	}
}
