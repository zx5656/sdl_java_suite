package com.smartdevicelink.proxy.rc.rpc;

import java.util.Hashtable;

import com.smartdevicelink.protocol.enums.FunctionID;
import com.smartdevicelink.proxy.RPCRequest;

/**
 * Called to retrieve the available zones and supported control types
 * @author Joey Grover
 *
 */
public class GetInteriorVehicleDataCapabilities extends RPCRequest {

	
	/**
	*Constructs a newly allocated GetInteriorVehicleDataCapabilities object
	*/ 	
    public GetInteriorVehicleDataCapabilities() {
        super(FunctionID.GET_INTERIOR_VEHICLE_DATA_CAPABILITIES);
    }
    /**
    *<p>Constructs a newly allocated GetInteriorVehicleDataCapabilities object indicated by the Hashtable parameter</p>
    *@param hash The Hashtable to use
    */    
    public GetInteriorVehicleDataCapabilities(Hashtable<String, Object> hash) {
        super(hash);
    }
    
}
