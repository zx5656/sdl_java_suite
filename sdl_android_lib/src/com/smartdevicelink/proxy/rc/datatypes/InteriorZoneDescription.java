package com.smartdevicelink.proxy.rc.datatypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import com.smartdevicelink.proxy.RPCStruct;
import com.smartdevicelink.proxy.rc.enums.InteriorDataType;

public class InteriorZoneDescription extends RPCStruct{

	public static final String KEY_ZONE			= "zone";
	public static final String KEY_DATA_TYPES	= "interiorDataTypes";
	
	
	/**
	 * Constructs a newly allocated InteriorZoneDescription object
	 */
	public InteriorZoneDescription() { }

	/**
	 * Constructs a newly allocated InteriorZoneDescription object using the supplied params
	 */
	public InteriorZoneDescription(InteriorZone zone,InteriorDataType dataType) { 
		setZone(zone);
		setDataTypes(Arrays.asList(dataType)); //TODO check this works
	}
	
	/**
	 * Constructs a newly allocated InteriorZoneDescription object using the supplied params
	 */
	public InteriorZoneDescription(InteriorZone zone,List<InteriorDataType> dataTypes) { 
		setZone(zone);
		setDataTypes(dataTypes);
	}
	/**
	 * Constructs a newly allocated InteriorZoneDescription object indicated by the Hashtable parameter
	 * @param hash The Hashtable to use
	 */      
	public InteriorZoneDescription(Hashtable<String, Object> hash) {
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
	
	//TODO make sure this is still how we are doing it as an array
	
	@SuppressWarnings("unchecked")
	public List<InteriorDataType> getDataTypes(){
        if (store.get(KEY_DATA_TYPES) instanceof List<?>) {
        	List<?> list = (List<?>)store.get( KEY_DATA_TYPES);
        	if (list != null && list.size() > 0) {
        		Object obj = list.get(0);
        		if (obj instanceof String) {
                	return (List<InteriorDataType>) list;
        		}
        	}
        }
        return null;
	}

	public void setDataTypes(List<InteriorDataType> dataTypes){
		if (dataTypes!=null) {
			store.put(KEY_DATA_TYPES, dataTypes);
		} else {
			store.remove(KEY_DATA_TYPES);
		}
	}
}
