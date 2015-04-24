package com.smartdevicelink.proxy.rc.rpc;

import java.util.Hashtable;
import java.util.List;

import com.smartdevicelink.protocol.enums.FunctionID;
import com.smartdevicelink.proxy.RPCResponse;
import com.smartdevicelink.proxy.rc.datatypes.InteriorZoneDescription;

public class GetInteriorVehicleDataCapabilitiesResponse extends RPCResponse {

	public static final String KEY_INTERIOR_ZONE_CAPABILITIES = "zoneCapabilities";

	
	/**
	*Constructs a newly allocated GetInteriorVehicleDataCapabilities object
	*/ 	
    public GetInteriorVehicleDataCapabilitiesResponse() {
        super(FunctionID.GET_INTERIOR_VEHICLE_DATA_CAPABILITIES);
    }
    /**
    *<p>Constructs a newly allocated GetInteriorVehicleDataCapabilities object indicated by the Hashtable parameter</p>
    *@param hash The Hashtable to use
    */    
    public GetInteriorVehicleDataCapabilitiesResponse(Hashtable<String, Object> hash) {
        super(hash);
    }
    
	//TODO make sure this is still how we are doing it as an array
	
	@SuppressWarnings("unchecked")
	public List<InteriorZoneDescription> getInteriorCapabilities(){
        if (store.get(KEY_INTERIOR_ZONE_CAPABILITIES) instanceof List<?>) {
        	List<?> list = (List<?>)store.get( KEY_INTERIOR_ZONE_CAPABILITIES);
        	if (list != null && list.size() > 0) {
        		Object obj = list.get(0);
        		if (obj instanceof String) {
                	return (List<InteriorZoneDescription>) list;
        		}
        	}
        }
        return null;
	}

	public void setInteriorCapabilities(List<InteriorZoneDescription> zoneCapabilities){
		if (zoneCapabilities!=null) {
			store.put(KEY_INTERIOR_ZONE_CAPABILITIES, zoneCapabilities);
		} else {
			store.remove(KEY_INTERIOR_ZONE_CAPABILITIES);
		}
	}  
    
}
