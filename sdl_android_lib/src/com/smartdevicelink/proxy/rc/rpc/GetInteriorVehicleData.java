package com.smartdevicelink.proxy.rc.rpc;

import java.util.Hashtable;
import java.util.List;

import com.smartdevicelink.protocol.enums.FunctionID;
import com.smartdevicelink.proxy.RPCRequest;
import com.smartdevicelink.proxy.rc.datatypes.InteriorZoneDescription;

public class GetInteriorVehicleData extends RPCRequest {

	
	public static final String KEY_INTERIOR_ZONE_DESCRIPTION = "interiorZoneDescription";
	public static final String KEY_SUBSCRIBE = "subscribe";

	
	/**
	*Constructs a newly allocated GetInteriorVehicleData object
	*/ 	
    public GetInteriorVehicleData() {
        super(FunctionID.GET_INTERIOR_VEHICLE_DATA);
    }
    /**
    *<p>Constructs a newly allocated GetInteriorVehicleData object indicated by the Hashtable parameter</p>
    *@param hash The Hashtable to use
    */    
    public GetInteriorVehicleData(Hashtable<String, Object> hash) {
        super(hash);
    }

	
	@SuppressWarnings("unchecked")
	public List<InteriorZoneDescription> getInteriorZoneDescriptions(){
        if (store.get(KEY_INTERIOR_ZONE_DESCRIPTION) instanceof List<?>) {
        	List<?> list = (List<?>)store.get( KEY_INTERIOR_ZONE_DESCRIPTION);
        	if (list != null && list.size() > 0) {
        		Object obj = list.get(0);
        		if (obj instanceof String) {
                	return (List<InteriorZoneDescription>) list;
        		}
        	}
        }
        return null;
	}

	public void setInteriorZoneDescriptions(List<InteriorZoneDescription> zoneDescriptions){
		if (zoneDescriptions!=null) {
			store.put(KEY_INTERIOR_ZONE_DESCRIPTION, zoneDescriptions);
		} else {
			store.remove(KEY_INTERIOR_ZONE_DESCRIPTION);
		}
	}
	
	public Boolean getSubscribed(){
		return (Boolean) store.get(KEY_SUBSCRIBE);
	}

	public void setSubscribed(Boolean subscribe){
		if (subscribe!=null) {
			store.put(KEY_SUBSCRIBE, subscribe);
		} else {
			store.remove(KEY_SUBSCRIBE);
		}
	}
    
}
