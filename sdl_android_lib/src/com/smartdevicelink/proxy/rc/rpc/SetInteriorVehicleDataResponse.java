package com.smartdevicelink.proxy.rc.rpc;

import java.util.Hashtable;
import java.util.List;

import com.smartdevicelink.protocol.enums.FunctionID;
import com.smartdevicelink.proxy.RPCResponse;
import com.smartdevicelink.proxy.rc.datatypes.InteriorZoneStatus;

public class SetInteriorVehicleDataResponse extends RPCResponse {

	public static final String KEY_INTERIOR_ZONE_STATUSES = "interiorZoneStatuses";

	/**
	*Constructs a newly allocated SetInteriorVehicleDataResponse object
	*/ 	
    public SetInteriorVehicleDataResponse() {
        super(FunctionID.SET_INTERIOR_VEHICLE_DATA);
    }
    /**
    *<p>Constructs a newly allocated SetInteriorVehicleDataResponse object indicated by the Hashtable parameter</p>
    *@param hash The Hashtable to use
    */    
    public SetInteriorVehicleDataResponse(Hashtable<String, Object> hash) {
        super(hash);
    }
    
	@SuppressWarnings("unchecked")
	public List<InteriorZoneStatus> getInteriorZoneStatuses(){
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

	public void setInteriorZoneStatuses(List<InteriorZoneStatus> statuses){
		if (statuses!=null) {
			store.put(KEY_INTERIOR_ZONE_STATUSES, statuses);
		} else {
			store.remove(KEY_INTERIOR_ZONE_STATUSES);
		}
	}
	
}
