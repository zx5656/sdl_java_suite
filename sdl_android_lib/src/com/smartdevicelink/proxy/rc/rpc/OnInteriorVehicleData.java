package com.smartdevicelink.proxy.rc.rpc;

import java.util.Hashtable;
import java.util.List;

import com.smartdevicelink.protocol.enums.FunctionID;
import com.smartdevicelink.proxy.RPCNotification;
import com.smartdevicelink.proxy.rc.datatypes.InteriorZoneStatus;

public class OnInteriorVehicleData extends RPCNotification {

	public static final String KEY_INTERIOR_ZONE_STATUSES = "interiorZoneStatuses";

	/**
	*Constructs a newly allocated OnInteriorVehicleData object
	*/ 	
    public OnInteriorVehicleData() {
        super(FunctionID.ON_INTERIOR_VEHICLE_DATA);
    }
    /**
    *<p>Constructs a newly allocated OnInteriorVehicleData object indicated by the Hashtable parameter</p>
    *@param hash The Hashtable to use
    */    
    public OnInteriorVehicleData(Hashtable<String, Object> hash) {
        super(hash);
    }
    
    
	//TODO make sure this is still how we are doing it as an array
	
	@SuppressWarnings("unchecked")
	public List<InteriorZoneStatus> getDataTypes(){
        if (store.get(KEY_INTERIOR_ZONE_STATUSES) instanceof List<?>) {
        	List<?> list = (List<?>)store.get( KEY_INTERIOR_ZONE_STATUSES);
        	if (list != null && list.size() > 0) {
        		Object obj = list.get(0);
        		if (obj instanceof String) {
                	return (List<InteriorZoneStatus>) list;
        		}
        	}
        }
        return null;
	}

	public void setDataTypes(List<InteriorZoneStatus> zoneStatuses){
		if (zoneStatuses!=null) {
			store.put(KEY_INTERIOR_ZONE_STATUSES, zoneStatuses);
		} else {
			store.remove(KEY_INTERIOR_ZONE_STATUSES);
		}
	}
    
}
